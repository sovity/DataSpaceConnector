/*
 * Copyright 2020 Fraunhofer Institute for Software and Systems Engineering
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
 */
package io.dataspaceconnector;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * This is the main application class. The application is started and an openApi bean for the
 * Swagger UI is created.
 */

@SpringBootApplication
@ComponentScan({
    "de.fraunhofer.isst.ids.framework.messaging",
    "io.dataspaceconnector",
    "de.fraunhofer.isst.ids.framework.communication",
    "de.fraunhofer.isst.ids.framework.configuration",
    "de.fraunhofer.isst.ids.framework.daps"
})
public class ConnectorApplication {

    /**
     * The main method.
     * @param args List of arguments.
     */
    public static void main(final String[] args) {
        SpringApplication.run(ConnectorApplication.class, args);
    }

    /**
     * Creates the OpenAPI main description. The description contains general project information
     * such as e.g. title, version and contact information.
     *
     * @return The OpenAPI description.
     * @throws IOException Throws an exception if the properties cannot be loaded from file.
     */
    @Bean
    public OpenAPI customOpenAPI() throws IOException {
        final var properties = new Properties();
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
                                             .getResourceAsStream("application.properties")) {
            // This function may crash (e.g. ill-formatted file). Let it bubble up.
            properties.load(inputStream);
        }

        return new OpenAPI()
            .components(new Components())
            .info(new Info()
                .title(properties.getProperty("title"))
                .description(properties.getProperty("project_desc"))
                .version(properties.getProperty("version"))
                .contact(new Contact()
                    .name(properties.getProperty("organization_name"))
                    .url(properties.getProperty("contact_url"))
                    .email(properties.getProperty("contact_email"))
                )
                .license(new License()
                    .name(properties.getProperty("licence"))
                    .url(properties.getProperty("licence_url")))
            );
    }
}