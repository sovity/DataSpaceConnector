---
layout: default
title: Usage Control
nav_order: 5
description: ""
permalink: /Documentation/v6/UsageControl
parent: Documentation
---

# IDS Usage Control Policies
{: .fs-9 }

Usage policies are an important aspect of IDS, further details are explained on this page.
{: .fs-6 .fw-300 }

[previous version](../v5/usage-control.md)

---

The Dataspace Connector supports usage policies written in the `IDS Usage Control Language` based on
[ODRL](https://www.w3.org/TR/odrl-model/#policy).

"An IDS Contract is implicitly divided to two main sections: the contract specific metadata and the
`IDS Usage Control Policy` of the contract.
The contract specific information (e.g., date when the contract has been issued or references to the
sensitive information about the involved parties) has no effect on the enforcement. However, the
`IDS Usage Control Policy` is the key motive of organizational and technical Usage Control
enforcement.
Furthermore, an `IDS Usage Control Policy` contains several Data Usage Control statements (e.g.,
permissions, prohibitions and obligations) called `IDS Rules` and is specified in the `IDS Usage
Control Language` which is a technology independent language. The technically enforceable rules
shall be transformed to a technology dependent policy (e.g., MYDATA) to facilitate the Usage Control
enforcement of data sovereignty." (p.22, [IDSA Position Paper Usage Control in the IDS](https://internationaldataspaces.org/download/21053/))

## Policy Patterns

Following the specifications of the IDSA Position Paper about Usage Control, the IDS defines 21
policy classes. The Dataspace Connector currently implements nine of these.

| No. | Title                                          | Support | Implementation |
|:----|:-----------------------------------------------|:-------:|:-------|
| 1   | Allow the Usage of the Data                    | x       | provides data usage without any restrictions
| 2   | Connector-restricted Data Usage                | x       | allows data usage for a specific connector
| 3   | Application-restricted Data Usage              | -       |
| 4   | Interval-restricted Data Usage                 | x       | provides data usage within a specified time interval
| 5   | Duration-restricted Data Usage                 | x       | allows data usage for a specified time period
| 6   | Location Restricted Policy                     | -       |
| 7   | Perpetual Data Sale (Payment once)             | -       |
| 8   | Data Rental (Payment frequently)               | -       |
| 9   | Role-restricted Data Usage                     | -       |
| 10  | Purpose-restricted Data Usage Policy           | -       |
| 11  | Event-restricted Usage Policy                  | -       |
| 12  | Restricted Number of Usages                    | x       | allows data usage for n times
| 13  | Security Level Restricted Policy               | x       | allows data access only for connectors with a specified security level
| 14  | Use Data and Delete it After                   | x       | allows data usage within a specified time interval with the restriction to delete it at a specified time stamp
| 15  | Modify Data (in Transit)                       | -       |
| 16  | Modify Data (in Rest)                          | -       |
| 17  | Local Logging                                  | x       | allows data usage and sends logs to a specified Clearing House
| 18  | Remote Notifications                           | x       | allows data usage and sends notification message
| 19  | Attach Policy when Distribute to a Third-party | -       |
| 20  | Distribute only if Encrypted                   | -       |
| 21  | State Restricted Policy                        | -       |

The usage policy is added to the metadata of a resource. The classes at
`io.dataspaceconnector.service.usagecontrol` read, classify, verify, and enforce the policies at
runtime. See how this works on the [provider-side](../../communication/v6/provider.md) and
[consumer-side](../../communication/v6/consumer.md) in the communication guide.

## Policy In Depth
The supported policies are further explained in this section, as well as the meaning of each parameter.
Every supported policy is defined in the `PolicyPattern` ENUM.
The `Rule` class from the Information Model is used to define usage policies. The DSC matches rule objects to a
DSC `PolicyPattern` with the `getPatternByRule` method from the `RuleUtils` class.
The `usagecontrol\RuleValidator` class matches the patterns found in a rule and throws a `PolicyRestrictionException` if
a policy restriction is detected. As soon as a `PolicyRestrictionException` is thrown, the access will be restricted.

### Provide Access
**Description:** This policy simply grants access to the resource.

### Usage During Interval and Usage Until Deletion
**Parameters:** start and end time (of type `ZonedDateTime`, a representation of an instant in the universal timeline)

**Description:**
Both the policies Usage During Interval and Usage Until Deletion use the same parameters and behave in the same way.
They check if the data access time is between the start and end time defined and if that is not the case, a
`PolicyRestrictionException` is thrown.
The access time used is a ZonedDateTime, which represents an instant in the universal timeline, since it also contains
date, time and zone information.
The only difference is that the Usage Until Deletion rule *should* contain a postDuty field with a DELETE action.
This is checked by a scheduled class called `ScheduledDataRemoval` and deletes the data after the interval has passed.
The Usage Until Deletion policy should be used if it is desired that the resource be deleted after the time interval.

### Duration Usage
**Parameters:** a duration, as specified by the `Duration` java class.
(Example: "PT10H" stands for 10 hours,
[see more](https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#abs--))

**Description:** This policy starts a duration for a resource: the resource can only be accessed in the
specified period. The duration starts counting from the artifacts' creation time.
If the consumer tries to access the resource and the current time is over the allowed period a
`PolicyRestrictionException` is thrown and the access is therefore denied.

### Usage Logging
**Prerequisites:** clearing house url in connector configuration

**Description:** This defines the policy to send usage logs to the clearing house.
The clearing house has to be defined in the connectors' configuration.
The logs are sent as IDS Messages to the clearing house with the path "clearingHousePath/agreementId".
The resource that is to be logged is sent in the payload.
It is not possible to specify in the rule to which clearing house should be logged.
If a clearing house is not specified or the message could not be delivered, the data access will still be granted.


### N Times Usage
**Parameters:** a  maximum number of accesses

**Description:** This policy counts the access number of the resource and denies access if the
access number is greater than the maximum number of accesses.
It is recommended to disable automatic contract negotiation if you plan on using this policy, so that the
data consumer does not negotiate a new contract once the maximum number of accesses has been reached.
To disable automatic contract negotiation change the field to ``policy.negotiation=false`` in
application.properties.
(how to negotiate a contract is shown [here](../../CommunicationGuide/v6/Consumer.md ))

### Usage Notification
**Parameters:** url to which usage notifications should be sent to (not limited to clearing house)

**Description:** This policy is similar to Usage Logging, but is not restricted to sending messages to a clearing house.
In the post duty field of the rule, an url can be defined within a constraint to which the DSC will send usage
notifications to. The payload of the logs sent contain target, issuer connector and access time.
If the message could not be sent, the access will still be granted.

### Connector Restricted Usage
**Parameters:** allowed connector URI defined in a rule

**Description:** This policy checks if the issuer connector is equal to a specified connector. The connector id that is
used for this verification is the one provided in the `config.json` file.
A similar check is performed when a contract is negotiated. For the negotiation the connector id is also checked to be
the specified contract consumer.


### Security Profile Restricted Usage
**Parameters:** required connector security profile (BASE_SECURITY_PROFILE, TRUST_SECURITY_PROFILE and
TRUST_PLUS_SECURITY_PROFILE)

**Description:** This policy checks if the connector has a specific security profile. This is verified by analysing the
DAT claims of the message received.

### Prohibit Access
**Parameters:** none

**Description:** This policy denies the access to the resource. A resource can't be shared if it is annotated with
this policy.


## Policy Enforcement
Not all policies are enforced in both data access and provision. Data access policies are checked when the consumer
tries to use the data. Data provision policies are checked before the data is sent from the data provider to the data
consumer. The list of patterns (policies) that are checked is
found in the classes `DataAccessVerifier` and `DataProvisionVerifier` in the `checkPolicy` method.
Policies enforced at data access currently are:
- PROVIDE_ACCESS
- USAGE_DURING_INTERVAL
- USAGE_UNTIL_DELETION
- DURATION_USAGE
- USAGE_LOGGING
- N_TIMES_USAGE
- USAGE_NOTIFICATION

Policies enforced at data provision currently are:
- PROVIDE_ACCESS
- PROHIBIT_ACCESS
- USAGE_DURING_INTERVAL
- USAGE_UNTIL_DELETION
- CONNECTOR_RESTRICTED_USAGE
- SECURITY_PROFILE_RESTRICTED_USAGE

The `RuleValidator.validatePolicy` function has a switch statement that matches the DSC policy patterns. The order of
the policies that will be checked is defined. The `validatePolicy` function is called before an artifact
is retrieved (`ArtifactService` -> `DataAccessVerifier` -> `RuleValidator`)
and before data is provisioned (`DataProvisionVerifier` -> `RuleValidator`).

### Usage Tips

To simplify the process of creating policies, the example endpoints described below can be used.
A request with the desired DSC policy pattern can be sent to ``POST /api/examples/policy`` and the response will be the
rule object that has to be added with the target uri to the data offering.


## Example Endpoint

Examples for each of them can be found by using the endpoint `POST /api/examples/policy`,
which can be accessed in the swagger UI under `_Utils`:

![Swagger Policy Endpoints](../../../assets/images/v6/swagger_policies.png)

The endpoint at `POST /api/examples/policy` is able to process inputs to fill out a policy
automatically. Thus, it does not need to be modified afterwards. Have a look at the schema provided
by the OpenApi specs or displayed by the Swagger UI.

### Provide Access

```json
{ "type": "PROVIDE_ACCESS" }
```

### Prohibit Access

```json
{ "type": "PROHIBIT_ACCESS" }
```

### N Times Usage

```json
{ "type": "N_TIMES_USAGE", "value": "5" }
```

### Usage During Interval

```json
{ "type": "USAGE_DURING_INTERVAL", "start": "2020-07-11T00:00:00Z", "end": "2020-07-11T00:00:00Z" }
```

### Duration Usage

```json
{ "type": "DURATION_USAGE", "duration": "PT1M30.5S" }
```

### Usage Until Deletion

```json
{ "type": "USAGE_UNTIL_DELETION", "start": "2020-07-11T00:00:00Z", "end": "2020-07-11T00:00:00Z", "date": "2020-07-11T00:00:00Z" }
```

### Usage Logging

```json
{ "type": "USAGE_LOGGING" }
```

### Usage Notification

```json
{ "type": "USAGE_NOTIFICATION", "url": "https://localhost:8080/api/ids/data" }
```

### Connector Restricted Usage

```json
{ "type": "CONNECTOR_RESTRICTED_USAGE", "url": "https://localhost:8080" }
```

### Security Profile Restricted Usage

```json
{ "type": "SECURITY_PROFILE_RESTRICTED_USAGE", "profile": "BASE_SECURITY_PROFILE" }
```


## Pattern Examples

### Provide Access
```json
{
  "@context" : {
    "ids" : "https://w3id.org/idsa/core/",
    "idsc" : "https://w3id.org/idsa/code/"
  },
  "@type" : "ids:Permission",
  "@id" : "https://w3id.org/idsa/autogen/permission/14af94cf-2a29-4ddd-8595-945d9a16be4f",
  "ids:description" : [ {
    "@value" : "provide-access",
    "@type" : "http://www.w3.org/2001/XMLSchema#string"
  } ],
  "ids:title" : [ {
    "@value" : "Allow Data Usage",
    "@type" : "http://www.w3.org/2001/XMLSchema#string"
  } ],
  "ids:action" : [ {
    "@id" : "idsc:USE"
  } ],
  "ids:target": [...]
}
```

### Prohibit Access
```json
{
  "@context" : {
    "ids" : "https://w3id.org/idsa/core/",
    "idsc" : "https://w3id.org/idsa/code/"
  },
  "@type" : "ids:Prohibition",
  "@id" : "https://w3id.org/idsa/autogen/prohibition/a838e2a5-d3e8-4891-af73-0f3bf39381ce",
  "ids:description" : [ {
    "@value" : "prohibit-access",
    "@type" : "http://www.w3.org/2001/XMLSchema#string"
  } ],
  "ids:title" : [ {
    "@value" : "Example Usage Policy",
    "@type" : "http://www.w3.org/2001/XMLSchema#string"
  } ],
  "ids:action" : [ {
    "@id" : "idsc:USE"
  } ],
  "ids:target": [...]
}
```

### N Times Usage
```json
{
  "@context" : {
    "ids" : "https://w3id.org/idsa/core/",
    "idsc" : "https://w3id.org/idsa/code/"
  },
  "@type" : "ids:Permission",
  "@id" : "https://w3id.org/idsa/autogen/permission/4ad88c11-a00c-4479-94f6-2a68cce005ea",
  "ids:description" : [ {
    "@value" : "n-times-usage",
    "@type" : "http://www.w3.org/2001/XMLSchema#string"
  } ],
  "ids:title" : [ {
    "@value" : "Example Usage Policy",
    "@type" : "http://www.w3.org/2001/XMLSchema#string"
  } ],
  "ids:action" : [ {
    "@id" : "idsc:USE"
  } ],
  "ids:constraint" : [ {
    "@type" : "ids:Constraint",
    "@id" : "https://w3id.org/idsa/autogen/constraint/a5d77dcd-f838-48e9-bdc1-4b219946f8ac",
    "ids:rightOperand" : {
      "@value" : "5",
      "@type" : "http://www.w3.org/2001/XMLSchema#double"
    },
    "ids:leftOperand" : {
      "@id" : "idsc:COUNT"
    },
    "ids:operator" : {
      "@id" : "idsc:LTEQ"
    }
  } ],
  "ids:target": [...]
}
```

### Duration Usage
```json
{
  "@context" : {
    "ids" : "https://w3id.org/idsa/core/",
    "idsc" : "https://w3id.org/idsa/code/"
  },
  "@type" : "ids:Permission",
  "@id" : "https://w3id.org/idsa/autogen/permission/3b1439a1-4136-4675-b5a0-798ec3148996",
  "ids:description" : [ {
    "@value" : "duration-usage",
    "@type" : "http://www.w3.org/2001/XMLSchema#string"
  } ],
  "ids:title" : [ {
    "@value" : "Example Usage Policy",
    "@type" : "http://www.w3.org/2001/XMLSchema#string"
  } ],
  "ids:action" : [ {
    "@id" : "idsc:USE"
  } ],
  "ids:constraint" : [ {
    "@type" : "ids:Constraint",
    "@id" : "https://w3id.org/idsa/autogen/constraint/b7d8beaf-0765-4d40-b2e9-4eddeda1c89b",
    "ids:rightOperand" : {
      "@value" : "PT1M30.5S",
      "@type" : "http://www.w3.org/2001/XMLSchema#duration"
    },
    "ids:leftOperand" : {
      "@id" : "idsc:ELAPSED_TIME"
    },
    "ids:operator" : {
      "@id" : "idsc:SHORTER_EQ"
    }
  } ],
  "ids:target": [...]
}
```

### Usage During Interval
```json
{
  "@context" : {
    "ids" : "https://w3id.org/idsa/core/",
    "idsc" : "https://w3id.org/idsa/code/"
  },
  "@type" : "ids:Permission",
  "@id" : "https://w3id.org/idsa/autogen/permission/1fcac0c3-8946-4880-a8cc-a7eab0543204",
  "ids:description" : [ {
    "@value" : "usage-during-interval",
    "@type" : "http://www.w3.org/2001/XMLSchema#string"
  } ],
  "ids:title" : [ {
    "@value" : "Example Usage Policy",
    "@type" : "http://www.w3.org/2001/XMLSchema#string"
  } ],
  "ids:action" : [ {
    "@id" : "idsc:USE"
  } ],
  "ids:constraint" : [ {
    "@type" : "ids:Constraint",
    "@id" : "https://w3id.org/idsa/autogen/constraint/28653654-3024-4435-8626-f1878de39c22",
    "ids:rightOperand" : {
      "@value" : "2020-07-11T00:00:00Z",
      "@type" : "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
    },
    "ids:leftOperand" : {
      "@id" : "idsc:POLICY_EVALUATION_TIME"
    },
    "ids:operator" : {
      "@id" : "idsc:AFTER"
    }
  }, {
    "@type" : "ids:Constraint",
    "@id" : "https://w3id.org/idsa/autogen/constraint/c8408f4a-8c65-4894-a17d-4e3999bc0669",
    "ids:rightOperand" : {
      "@value" : "2020-07-11T00:00:00Z",
      "@type" : "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
    },
    "ids:leftOperand" : {
      "@id" : "idsc:POLICY_EVALUATION_TIME"
    },
    "ids:operator" : {
      "@id" : "idsc:BEFORE"
    }
  } ],
  "ids:target": [...]
}
```

### Usage Until Deletion
```json
{
  "@context" : {
    "ids" : "https://w3id.org/idsa/core/",
    "idsc" : "https://w3id.org/idsa/code/"
  },
  "@type" : "ids:Permission",
  "@id" : "https://w3id.org/idsa/autogen/permission/98d47a9d-d4e2-4048-97c2-9c632f5e235f",
  "ids:description" : [ {
    "@value" : "usage-until-deletion",
    "@type" : "http://www.w3.org/2001/XMLSchema#string"
  } ],
  "ids:title" : [ {
    "@value" : "Example Usage Policy",
    "@type" : "http://www.w3.org/2001/XMLSchema#string"
  } ],
  "ids:postDuty" : [ {
    "@type" : "ids:Duty",
    "@id" : "https://w3id.org/idsa/autogen/duty/97b8cc94-fa44-4bed-8036-73d6bc4b69ab",
    "ids:action" : [ {
      "@id" : "idsc:DELETE"
    } ],
    "ids:constraint" : [ {
      "@type" : "ids:Constraint",
      "@id" : "https://w3id.org/idsa/autogen/constraint/90abcfe4-9901-4128-b787-c077a9bd363b",
      "ids:rightOperand" : {
        "@value" : "2020-07-11T00:00:00Z",
        "@type" : "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
      },
      "ids:leftOperand" : {
        "@id" : "idsc:POLICY_EVALUATION_TIME"
      },
      "ids:operator" : {
        "@id" : "idsc:TEMPORAL_EQUALS"
      }
    } ]
  } ],
  "ids:action" : [ {
    "@id" : "idsc:USE"
  } ],
  "ids:constraint" : [ {
    "@type" : "ids:Constraint",
    "@id" : "https://w3id.org/idsa/autogen/constraint/3c218b76-7c32-4fd3-930f-69f728161096",
    "ids:rightOperand" : {
      "@value" : "2020-07-11T00:00:00Z",
      "@type" : "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
    },
    "ids:leftOperand" : {
      "@id" : "idsc:POLICY_EVALUATION_TIME"
    },
    "ids:operator" : {
      "@id" : "idsc:AFTER"
    }
  }, {
    "@type" : "ids:Constraint",
    "@id" : "https://w3id.org/idsa/autogen/constraint/937684bd-0e81-44a5-b4e3-02664a1bf4c9",
    "ids:rightOperand" : {
      "@value" : "2020-07-11T00:00:00Z",
      "@type" : "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
    },
    "ids:leftOperand" : {
      "@id" : "idsc:POLICY_EVALUATION_TIME"
    },
    "ids:operator" : {
      "@id" : "idsc:BEFORE"
    }
  } ],
  "ids:target": [...]
}
```

### Usage Logging
```json
{
  "@context" : {
    "ids" : "https://w3id.org/idsa/core/",
    "idsc" : "https://w3id.org/idsa/code/"
  },
  "@type" : "ids:Permission",
  "@id" : "https://w3id.org/idsa/autogen/permission/cbc802b1-03a7-4563-b6a3-688e9dd4ccdf",
  "ids:description" : [ {
    "@value" : "usage-logging",
    "@type" : "http://www.w3.org/2001/XMLSchema#string"
  } ],
  "ids:title" : [ {
    "@value" : "Example Usage Policy",
    "@type" : "http://www.w3.org/2001/XMLSchema#string"
  } ],
  "ids:postDuty" : [ {
    "@type" : "ids:Duty",
    "@id" : "https://w3id.org/idsa/autogen/duty/f022bf97-5601-4cb9-a241-638906100c18",
    "ids:action" : [ {
      "@id" : "idsc:LOG"
    } ]
  } ],
  "ids:action" : [ {
    "@id" : "idsc:USE"
  } ],
  "ids:target": [...]
}
```

### Usage Notification
```json
{
  "@context" : {
    "ids" : "https://w3id.org/idsa/core/",
    "idsc" : "https://w3id.org/idsa/code/"
  },
  "@type" : "ids:Permission",
  "@id" : "https://w3id.org/idsa/autogen/permission/75050633-0762-47d2-8a06-6a318eaf4b76",
  "ids:description" : [ {
    "@value" : "usage-notification",
    "@type" : "http://www.w3.org/2001/XMLSchema#string"
  } ],
  "ids:title" : [ {
    "@value" : "Example Usage Policy",
    "@type" : "http://www.w3.org/2001/XMLSchema#string"
  } ],
  "ids:postDuty" : [ {
    "@type" : "ids:Duty",
    "@id" : "https://w3id.org/idsa/autogen/duty/6d7cc949-cdea-495a-b88b-6d902ddd017c",
    "ids:action" : [ {
      "@id" : "idsc:NOTIFY"
    } ],
    "ids:constraint" : [ {
      "@type" : "ids:Constraint",
      "@id" : "https://w3id.org/idsa/autogen/constraint/0f940426-d83e-4d2c-a59f-9d5f17ad5f4d",
      "ids:rightOperand" : {
        "@value" : "https://localhost:8080/api/ids/data",
        "@type" : "http://www.w3.org/2001/XMLSchema#anyURI"
      },
      "ids:leftOperand" : {
        "@id" : "idsc:ENDPOINT"
      },
      "ids:operator" : {
        "@id" : "idsc:DEFINES_AS"
      }
    } ]
  } ],
  "ids:action" : [ {
    "@id" : "idsc:USE"
  } ],
  "ids:target": [...]
}
```

### Connector Restricted Usage
```json
{
  "@context" : {
    "ids" : "https://w3id.org/idsa/core/",
    "idsc" : "https://w3id.org/idsa/code/"
  },
  "@type" : "ids:Permission",
  "@id" : "https://w3id.org/idsa/autogen/permission/d504b82f-79dd-4c93-969d-937ab6a1d676",
  "ids:description" : [ {
    "@value" : "connector-restriction",
    "@type" : "http://www.w3.org/2001/XMLSchema#string"
  } ],
  "ids:title" : [ {
    "@value" : "Example Usage Policy",
    "@type" : "http://www.w3.org/2001/XMLSchema#string"
  } ],
  "ids:action" : [ {
    "@id" : "idsc:USE"
  } ],
  "ids:constraint" : [ {
    "@type" : "ids:Constraint",
    "@id" : "https://w3id.org/idsa/autogen/constraint/572c96ec-dd86-4b20-a849-a0ce8c255eee",
    "ids:rightOperand" : {
      "@value" : "https://example.com",
      "@type" : "http://www.w3.org/2001/XMLSchema#anyURI"
    },
    "ids:leftOperand" : {
      "@id" : "idsc:SYSTEM"
    },
    "ids:operator" : {
      "@id" : "idsc:SAME_AS"
    }
  } ],
  "ids:target": [...]
}
```

### Security Profile Restricted Usage

```json
{
  "@context" : {
    "xsd" : "http://www.w3.org/2001/XMLSchema#",
    "ids" : "https://w3id.org/idsa/core/",
    "idsc" : "https://w3id.org/idsa/code/"
  },
  "@type" : "ids:Permission",
  "@id" : "https://w3id.org/idsa/autogen/permission/703226ed-f271-4a74-9db1-6fdc9e481c3c",
  "ids:action" : [ {
    "@id" : "https://w3id.org/idsa/code/USE"
  } ],
  "ids:constraint" : [ {
    "@type" : "ids:Constraint",
    "@id" : "https://w3id.org/idsa/autogen/constraint/b734b74c-27e5-4375-9edd-2fd7ba4a57b4",
    "ids:operator" : {
      "@id" : "https://w3id.org/idsa/code/EQUALS"
    },
    "ids:leftOperand" : {
      "@id" : "https://w3id.org/idsa/code/SECURITY_LEVEL"
    },
    "ids:rightOperand" : {
      "@value" : "https://w3id.org/idsa/code/BASE_SECURITY_PROFILE",
      "@type" : "xsd:string"
    }
  } ],
  "ids:title" : [ {
    "@value" : "Example Usage Policy",
    "@type" : "http://www.w3.org/2001/XMLSchema#string"
  } ],
  "ids:description" : [ {
    "@value" : "security-level-restriction",
    "@type" : "http://www.w3.org/2001/XMLSchema#string"
  } ],
  "ids:target": [...]
}
```
