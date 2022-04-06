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
 */
package io.dataspaceconnector.model.daps;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

public class DapsFactoryTest {

    final DapsDesc desc = new DapsDesc();
    final DapsFactory factory = new DapsFactory();

    @Test
    void create_validDesc_returnNew() {
        /* ARRANGE */
        final var title = "DAPS";
        desc.setTitle(title);

        /* ACT */
        final var result = factory.create(desc);

        /* ASSERT */
        assertEquals(title, result.getTitle());
    }

    @Test
    void update_newLocation_willUpdate() {
        /* ARRANGE */
        final var desc = new DapsDesc();
        desc.setLocation(URI.create("https://example.org"));
        final var daps = factory.create(new DapsDesc());

        /* ACT */
        final var result = factory.update(daps, desc);

        /* ASSERT */
        assertTrue(result);
        assertEquals(desc.getLocation(), daps.getLocation());
    }

    @Test
    void update_sameLocation_willNotUpdate() {
        /* ARRANGE */
        final var desc = new DapsDesc();
        final var daps = factory.create(new DapsDesc());

        /* ACT */
        final var result = factory.update(daps, desc);

        /* ASSERT */
        assertFalse(result);
        assertEquals(DapsFactory.DEFAULT_URI, daps.getLocation());
    }
}
