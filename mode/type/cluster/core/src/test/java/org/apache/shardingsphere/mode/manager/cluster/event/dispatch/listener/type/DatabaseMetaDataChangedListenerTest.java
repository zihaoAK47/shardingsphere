/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.mode.manager.cluster.event.dispatch.listener.type;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.event.builder.RuleConfigurationChangedEventBuilder;
import org.apache.shardingsphere.mode.event.dispatch.DispatchEvent;
import org.apache.shardingsphere.mode.event.dispatch.datasource.node.StorageNodeAlteredEvent;
import org.apache.shardingsphere.mode.event.dispatch.datasource.node.StorageNodeRegisteredEvent;
import org.apache.shardingsphere.mode.event.dispatch.datasource.node.StorageNodeUnregisteredEvent;
import org.apache.shardingsphere.mode.event.dispatch.datasource.unit.StorageUnitAlteredEvent;
import org.apache.shardingsphere.mode.event.dispatch.datasource.unit.StorageUnitRegisteredEvent;
import org.apache.shardingsphere.mode.event.dispatch.datasource.unit.StorageUnitUnregisteredEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.SchemaAddedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.SchemaDeletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.table.TableCreatedOrAlteredEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.table.TableDroppedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.view.ViewCreatedOrAlteredEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.view.ViewDroppedEvent;
import org.apache.shardingsphere.mode.event.dispatch.rule.alter.AlterUniqueRuleItemEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DatabaseMetaDataChangedListenerTest {
    
    private DatabaseMetaDataChangedListener listener;
    
    @Mock
    private EventBusContext eventBusContext;
    
    @BeforeEach
    void setUp() {
        listener = new DatabaseMetaDataChangedListener(eventBusContext);
        setMockedBuilder();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setMockedBuilder() {
        RuleConfigurationChangedEventBuilder ruleConfigChangedEventBuilder = mock(RuleConfigurationChangedEventBuilder.class);
        when(ruleConfigChangedEventBuilder.build(eq("foo_db"), any(DataChangedEvent.class))).thenReturn(Optional.of(new AlterUniqueRuleItemEvent("foo_db", "key", "value", "type")));
        Plugins.getMemberAccessor().set(DatabaseMetaDataChangedListener.class.getDeclaredField("ruleConfigChangedEventBuilder"), listener, ruleConfigChangedEventBuilder);
    }
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertOnChangeWithMetaData(final String name, final String eventKey, final Type type, final Class<? extends DispatchEvent> toBePostedEventType) {
        listener.onChange(new DataChangedEvent(eventKey, "value", type));
        if (null == toBePostedEventType) {
            verify(eventBusContext, times(0)).post(any());
        } else {
            verify(eventBusContext).post(any(toBePostedEventType));
        }
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public final Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of("changeWithoutDatabase", "/metadata", Type.IGNORED, null),
                    Arguments.of("addSchema", "/metadata/foo_db/schemas/foo_schema", Type.ADDED, SchemaAddedEvent.class),
                    Arguments.of("updateSchema", "/metadata/foo_db/schemas/foo_schema", Type.UPDATED, SchemaAddedEvent.class),
                    Arguments.of("deleteSchema", "/metadata/foo_db/schemas/foo_schema", Type.DELETED, SchemaDeletedEvent.class),
                    Arguments.of("ignoreChangeSchema", "/metadata/foo_db/schemas/foo_schema", Type.IGNORED, null),
                    Arguments.of("addTable", "/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version/0", Type.ADDED, TableCreatedOrAlteredEvent.class),
                    Arguments.of("updateTable", "/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version/0", Type.UPDATED, TableCreatedOrAlteredEvent.class),
                    Arguments.of("deleteTable", "/metadata/foo_db/schemas/foo_schema/tables/foo_tbl", Type.DELETED, TableDroppedEvent.class),
                    Arguments.of("invalidAddTable", "/metadata/foo_db/schemas/foo_schema/tables/foo_tbl", Type.ADDED, null),
                    Arguments.of("invalidDeleteTable", "/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version/0", Type.DELETED, null),
                    Arguments.of("addView", "/metadata/foo_db/schemas/foo_schema/views/foo_view/active_version/0", Type.ADDED, ViewCreatedOrAlteredEvent.class),
                    Arguments.of("updateView", "/metadata/foo_db/schemas/foo_schema/views/foo_view/active_version/0", Type.UPDATED, ViewCreatedOrAlteredEvent.class),
                    Arguments.of("deleteView", "/metadata/foo_db/schemas/foo_schema/views/foo_view", Type.DELETED, ViewDroppedEvent.class),
                    Arguments.of("invalidAddView", "/metadata/foo_db/schemas/foo_schema/views/foo_view", Type.ADDED, null),
                    Arguments.of("invalidDeleteView", "/metadata/foo_db/schemas/foo_schema/views/foo_view/active_version/0", Type.DELETED, null),
                    Arguments.of("registerStorageUnit", "/metadata/foo_db/data_sources/units/foo_unit/active_version/0", Type.ADDED, StorageUnitRegisteredEvent.class),
                    Arguments.of("alterStorageUnit", "/metadata/foo_db/data_sources/units/foo_unit/active_version/0", Type.UPDATED, StorageUnitAlteredEvent.class),
                    Arguments.of("unregisterStorageUnit", "/metadata/foo_db/data_sources/units/foo_unit", Type.DELETED, StorageUnitUnregisteredEvent.class),
                    Arguments.of("invalidRegisterStorageNode", "/metadata/foo_db/data_sources/units/foo_unit", Type.ADDED, null),
                    Arguments.of("invalidUnregisterStorageNode", "/metadata/foo_db/data_sources/units/foo_unit/active_version/0", Type.DELETED, null),
                    Arguments.of("ignoreChangeStorageUnit", "/metadata/foo_db/data_sources/units/foo_unit", Type.IGNORED, null),
                    Arguments.of("registerStorageNode", "/metadata/foo_db/data_sources/nodes/foo_node/active_version/0", Type.ADDED, StorageNodeRegisteredEvent.class),
                    Arguments.of("alterStorageNode", "/metadata/foo_db/data_sources/nodes/foo_node/active_version/0", Type.UPDATED, StorageNodeAlteredEvent.class),
                    Arguments.of("unregisterStorageNode", "/metadata/foo_db/data_sources/nodes/foo_node", Type.DELETED, StorageNodeUnregisteredEvent.class),
                    Arguments.of("invalidRegisterStorageNode", "/metadata/foo_db/data_sources/nodes/foo_node", Type.ADDED, null),
                    Arguments.of("invalidUnregisterStorageNode", "/metadata/foo_db/data_sources/nodes/foo_node/active_version/0", Type.DELETED, null),
                    Arguments.of("ignoreChangeStorageNode", "/metadata/foo_db/data_sources/nodes/foo_node", Type.IGNORED, null),
                    Arguments.of("invalidChangeDataSource", "/metadata/foo_db/data_sources/other", Type.ADDED, null),
                    Arguments.of("changeRule", "/metadata/foo_db/schemas/foo_schema/rule/", Type.ADDED, AlterUniqueRuleItemEvent.class));
        }
    }
}