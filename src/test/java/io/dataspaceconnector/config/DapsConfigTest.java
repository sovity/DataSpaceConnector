/*
 * Copyright 2022 sovity GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  Contributors:
 *       sovity GmbH
 *
 */
package io.dataspaceconnector.config;

import io.dataspaceconnector.model.daps.Daps;
import io.dataspaceconnector.model.daps.DapsDesc;
import io.dataspaceconnector.model.daps.DapsFactory;
import io.dataspaceconnector.repository.DapsRepository;
import io.dataspaceconnector.service.resource.type.DapsService;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {DapsService.class})
class DapsConfigTest {

    @Autowired
    private DapsService dapsService;

    @MockBean
    private DapsRepository dapsRepository;

    @MockBean
    private ConnectorConfig connectorConfig;

    @MockBean
    private DapsFactory dapsFactory;

    @Test
    void whitelist_empty_issuer_whitelisted() throws InvocationTargetException, IllegalAccessException {
        Map<String, Object> claimParams = new HashMap<>();
        final var claims = new DefaultClaims(claimParams).setIssuer("http://example.org");

        final var dapsConfig = new DapsConfig(dapsRepository, connectorConfig);
        final var methods = dapsConfig.getClass().getDeclaredMethods();
        final var isWhitelisted = Arrays.stream(methods).filter(it -> it.getName().equals("isWhitelisted")).findFirst();

        assertTrue(isWhitelisted.isPresent());

        final var isWhitelistedMethod = isWhitelisted.get();
        isWhitelistedMethod.setAccessible(true);
        final var whitelisted = (Boolean) isWhitelistedMethod.invoke(dapsConfig, claims);

        assertTrue(whitelisted);
    }

    @Test
    void whitelist_notempty_issuer_notwhitelisted() throws InvocationTargetException, IllegalAccessException, URISyntaxException, NoSuchFieldException {
        final var dapsDesc = new DapsDesc();
        dapsDesc.setLocation(new URI("http://example.org"));
        dapsDesc.setTitle("test");
        dapsDesc.setDescription("test");
        dapsDesc.setWhitelisted(true);
        final var dapsFactory = new DapsFactory();
        final var daps = dapsFactory.create(dapsDesc);

        Mockito.doReturn(List.of(daps)).when(dapsRepository).findAll();

        final var dapsConfig = new DapsConfig(dapsRepository, connectorConfig);
        final var methods = dapsConfig.getClass().getDeclaredMethods();

        final var ownDapsUrlField = dapsConfig.getClass().getDeclaredField("ownDapsUrl");
        ownDapsUrlField.setAccessible(true);
        ownDapsUrlField.set(dapsConfig, "http://my-daps");

        final var isWhitelisted = Arrays.stream(methods).filter(it -> it.getName().equals("isWhitelisted")).findFirst();

        assertTrue(isWhitelisted.isPresent());

        final var isWhitelistedMethod = isWhitelisted.get();
        isWhitelistedMethod.setAccessible(true);

        Map<String, Object> claimParams = new HashMap<>();
        final var claims = new DefaultClaims(claimParams).setIssuer("http://example-false.org");
        final var whitelisted = (Boolean) isWhitelistedMethod.invoke(dapsConfig, claims);

        assertFalse(whitelisted);
    }

    @Test
    void whitelist_notempty_issuer_whitelisted() throws InvocationTargetException, IllegalAccessException, URISyntaxException, NoSuchFieldException {
        final var dapsDesc = new DapsDesc();
        dapsDesc.setLocation(new URI("http://example.org"));
        dapsDesc.setTitle("test");
        dapsDesc.setDescription("test");
        dapsDesc.setWhitelisted(true);
        final var dapsFactory = new DapsFactory();
        final var daps = dapsFactory.create(dapsDesc);

        Mockito.doReturn(List.of(daps)).when(dapsRepository).findAll();

        final var dapsConfig = new DapsConfig(dapsRepository, connectorConfig);
        final var methods = dapsConfig.getClass().getDeclaredMethods();

        final var ownDapsUrlField = dapsConfig.getClass().getDeclaredField("ownDapsUrl");
        ownDapsUrlField.setAccessible(true);
        ownDapsUrlField.set(dapsConfig, "http://my-daps");

        final var isWhitelisted = Arrays.stream(methods).filter(it -> it.getName().equals("isWhitelisted")).findFirst();

        assertTrue(isWhitelisted.isPresent());

        final var isWhitelistedMethod = isWhitelisted.get();
        isWhitelistedMethod.setAccessible(true);

        Map<String, Object> claimParams = new HashMap<>();
        final var claims = new DefaultClaims(claimParams).setIssuer("http://example.org");
        final var whitelisted = (Boolean) isWhitelistedMethod.invoke(dapsConfig, claims);

        assertTrue(whitelisted);
    }

    @Test
    void whitelist_notempty_issuer_owndaps() throws InvocationTargetException, IllegalAccessException, URISyntaxException, NoSuchFieldException {
        final var dapsDesc = new DapsDesc();
        dapsDesc.setLocation(new URI("http://example.org"));
        dapsDesc.setTitle("test");
        dapsDesc.setDescription("test");
        dapsDesc.setWhitelisted(true);
        final var dapsFactory = new DapsFactory();
        final var daps = dapsFactory.create(dapsDesc);

        Mockito.doReturn(List.of(daps)).when(dapsRepository).findAll();

        final var dapsConfig = new DapsConfig(dapsRepository, connectorConfig);
        final var methods = dapsConfig.getClass().getDeclaredMethods();

        final var ownDapsUrlField = dapsConfig.getClass().getDeclaredField("ownDapsUrl");
        ownDapsUrlField.setAccessible(true);
        ownDapsUrlField.set(dapsConfig, "http://my-daps");

        final var isWhitelisted = Arrays.stream(methods).filter(it -> it.getName().equals("isWhitelisted")).findFirst();

        assertTrue(isWhitelisted.isPresent());

        final var isWhitelistedMethod = isWhitelisted.get();
        isWhitelistedMethod.setAccessible(true);

        Map<String, Object> claimParams = new HashMap<>();
        final var claims = new DefaultClaims(claimParams).setIssuer("http://my-daps");
        final var whitelisted = (Boolean) isWhitelistedMethod.invoke(dapsConfig, claims);

        assertTrue(whitelisted);
    }

    @Test
    void dapslist_notempty_but_no_whitelisted() throws InvocationTargetException, IllegalAccessException, URISyntaxException, NoSuchFieldException {
        final var dapsDesc = new DapsDesc();
        dapsDesc.setLocation(new URI("http://example.org"));
        dapsDesc.setTitle("test");
        dapsDesc.setDescription("test");
        dapsDesc.setWhitelisted(false);
        final var dapsFactory = new DapsFactory();
        final var daps = dapsFactory.create(dapsDesc);

        Mockito.doReturn(List.of(daps)).when(dapsRepository).findAll();

        final var dapsConfig = new DapsConfig(dapsRepository, connectorConfig);
        final var methods = dapsConfig.getClass().getDeclaredMethods();

        final var ownDapsUrlField = dapsConfig.getClass().getDeclaredField("ownDapsUrl");
        ownDapsUrlField.setAccessible(true);
        ownDapsUrlField.set(dapsConfig, "http://my-daps");

        final var isWhitelisted = Arrays.stream(methods).filter(it -> it.getName().equals("isWhitelisted")).findFirst();

        assertTrue(isWhitelisted.isPresent());

        final var isWhitelistedMethod = isWhitelisted.get();
        isWhitelistedMethod.setAccessible(true);

        Map<String, Object> claimParams = new HashMap<>();
        final var claims = new DefaultClaims(claimParams).setIssuer("http://example.org");
        final var whitelisted = (Boolean) isWhitelistedMethod.invoke(dapsConfig, claims);

        assertTrue(whitelisted);
    }

    @Test
    void dapslist_notempty_and_whitelisted_combination() throws InvocationTargetException, IllegalAccessException, URISyntaxException, NoSuchFieldException {
        final var dapsFactory = new DapsFactory();

        final var dapsDesc1 = new DapsDesc();
        dapsDesc1.setLocation(new URI("http://example.org"));
        dapsDesc1.setTitle("test");
        dapsDesc1.setDescription("test");
        dapsDesc1.setWhitelisted(false);
        final var daps1 = dapsFactory.create(dapsDesc1);

        final var dapsDesc2 = new DapsDesc();
        dapsDesc2.setLocation(new URI("http://example2"));
        dapsDesc2.setTitle("test2");
        dapsDesc2.setDescription("test2");
        dapsDesc2.setWhitelisted(true);
        final var daps2 = dapsFactory.create(dapsDesc2);

        final var dapsList = new LinkedList<Daps>();
        dapsList.add(daps1);
        dapsList.add(daps2);

        Mockito.doReturn(dapsList).when(dapsRepository).findAll();

        final var dapsConfig = new DapsConfig(dapsRepository, connectorConfig);
        final var methods = dapsConfig.getClass().getDeclaredMethods();

        final var ownDapsUrlField = dapsConfig.getClass().getDeclaredField("ownDapsUrl");
        ownDapsUrlField.setAccessible(true);
        ownDapsUrlField.set(dapsConfig, "http://my-daps");

        final var isWhitelisted = Arrays.stream(methods).filter(it -> it.getName().equals("isWhitelisted")).findFirst();

        assertTrue(isWhitelisted.isPresent());

        final var isWhitelistedMethod = isWhitelisted.get();
        isWhitelistedMethod.setAccessible(true);

        Map<String, Object> claimParams = new HashMap<>();
        final var claims = new DefaultClaims(claimParams).setIssuer("http://example2");
        final var whitelisted = (Boolean) isWhitelistedMethod.invoke(dapsConfig, claims);
        assertTrue(whitelisted);

        Map<String, Object> claimParams2 = new HashMap<>();
        final var claims2 = new DefaultClaims(claimParams).setIssuer("http://example-false");
        final var whitelisted2 = (Boolean) isWhitelistedMethod.invoke(dapsConfig, claims2);
        assertFalse(whitelisted2);
    }

    @Test
    void persist_whitelist() throws NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        Mockito.doReturn(List.of("http://example.org")).when(connectorConfig).getDapsWhitelist();

        final var dapsConfig = new DapsConfig(dapsRepository, connectorConfig);
        final var methods = dapsConfig.getClass().getDeclaredMethods();

        final var ownDapsUrlField = dapsConfig.getClass().getDeclaredField("ownDapsUrl");
        ownDapsUrlField.setAccessible(true);
        ownDapsUrlField.set(dapsConfig, "http://my-daps");

        final var persistPropertiesWhitelist = Arrays.stream(methods).filter(it -> it.getName().equals("persistPropertiesWhitelist")).findFirst();

        //Check if method exists.
        assertTrue(persistPropertiesWhitelist.isPresent());

        final var persistPropertiesWhitelistMethod = persistPropertiesWhitelist.get();
        persistPropertiesWhitelistMethod.setAccessible(true);
        persistPropertiesWhitelistMethod.invoke(dapsConfig);
    }
}
