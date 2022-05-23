/*
 * Copyright 2020-2022 Fraunhofer Institute for Software and Systems Engineering
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
package io.dataspaceconnector.service.resource.relation;

import io.dataspaceconnector.common.exception.ErrorMessage;
import io.dataspaceconnector.common.util.UUIDUtils;
import io.dataspaceconnector.common.util.Utils;
import io.dataspaceconnector.model.broker.Broker;
import io.dataspaceconnector.model.resource.OfferedResource;
import io.dataspaceconnector.service.resource.base.OwningRelationService;
import io.dataspaceconnector.service.resource.type.BrokerService;
import io.dataspaceconnector.service.resource.type.OfferedResourceService;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Handles the relation between broker and offered resources.
 */
@Service
@NoArgsConstructor
public class BrokerOfferedResourceLinker extends OwningRelationService<Broker, OfferedResource,
        BrokerService, OfferedResourceService> {

    @Override
    protected final List<OfferedResource> getInternal(final Broker owner) {
        return owner.getOfferedResources();
    }

    /**
     * This method also makes sure the bootstrap ids in entities are
     * converted to the real DSC resource ids.
     * {@inheritDoc}
     */
    @Override
    public void add(final UUID ownerId, final Set<UUID> entities) {
        Utils.requireNonNull(ownerId, ErrorMessage.ENTITYID_NULL);
        Utils.requireNonNull(entities, ErrorMessage.ENTITYSET_NULL);

        if (entities.isEmpty()) {
            // Prevent read call to database for the owner.
            return;
        }

        Set<UUID> correctEntities = new HashSet<>(entities);

        // Search if any id from the entities set is found in the bootstrapId field of a resource,
        // if this is the case, replace it with the correct resource id
        for (OfferedResource e : getManyService().getAll(Pageable.unpaged())) {
            for (var entity : entities) {
                if (e.getBootstrapId() != null) {
                    UUID bootstrapId = UUIDUtils.uuidFromUri(e.getBootstrapId());
                    if (bootstrapId.equals(entity)) {
                        correctEntities.remove(bootstrapId);
                        correctEntities.add(e.getId());
                    }
                }
            }
        }
        throwIfEntityDoesNotExist(correctEntities);
        addInternal(ownerId, correctEntities);
    }
}
