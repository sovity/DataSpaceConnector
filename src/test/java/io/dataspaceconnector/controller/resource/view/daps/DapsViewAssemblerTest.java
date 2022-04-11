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
package io.dataspaceconnector.controller.resource.view.daps;

import io.dataspaceconnector.controller.resource.type.DapsController;
import io.dataspaceconnector.controller.resource.view.util.SelfLinkHelper;
import io.dataspaceconnector.model.daps.Daps;
import io.dataspaceconnector.model.daps.DapsDesc;
import io.dataspaceconnector.model.daps.DapsFactory;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DapsViewAssemblerTest {

    @Test
    public void create_ValidDaps_returnDapsView() {
        /* ARRANGE */
        final var shouldLookLike = getDaps();
        final var link = new SelfLinkHelper()
                .getSelfLink(shouldLookLike.getId(), DapsController.class);

        /* ACT */
        final var after = getDapsView();

        /* ASSERT */
        assertEquals(after.getLocation(), shouldLookLike.getLocation());
        assertEquals(after.getTitle(), shouldLookLike.getTitle());
        assertTrue(after.getLinks().contains(link));
    }

    private DapsView getDapsView() {
        final var assembler = new DapsViewAssembler();
        return assembler.toModel(getDaps());
    }


    private Daps getDaps() {
        final var factory = new DapsFactory();
        return factory.create(getDapsDesc());
    }

    private DapsDesc getDapsDesc() {
        final var desc = new DapsDesc();
        desc.setLocation(URI.create("https://example.org"));
        desc.setTitle("DAPS");
        return desc;
    }
}
