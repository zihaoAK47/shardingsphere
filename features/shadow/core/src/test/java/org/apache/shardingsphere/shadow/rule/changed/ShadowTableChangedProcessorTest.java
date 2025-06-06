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

package org.apache.shardingsphere.shadow.rule.changed;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.spi.rule.RuleItemConfigurationChangedProcessor;
import org.apache.shardingsphere.mode.spi.rule.RuleChangedItemType;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.yaml.config.table.YamlShadowTableConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.apache.shardingsphere.test.matcher.ShardingSphereAssertionMatchers.deepEqual;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShadowTableChangedProcessorTest {
    
    @SuppressWarnings("unchecked")
    private final RuleItemConfigurationChangedProcessor<ShadowRuleConfiguration, ShadowTableConfiguration> processor = TypedSPILoader.getService(
            RuleItemConfigurationChangedProcessor.class, new RuleChangedItemType("shadow", "tables"));
    
    @Test
    void assertSwapRuleItemConfiguration() {
        ShadowTableConfiguration actual = processor.swapRuleItemConfiguration("foo_ds", createYAMLContent());
        assertThat(actual, deepEqual(new ShadowTableConfiguration(Collections.singletonList("foo_ds"), Collections.singletonList("foo_algo"))));
    }
    
    private String createYAMLContent() {
        YamlShadowTableConfiguration yamlConfig = new YamlShadowTableConfiguration();
        yamlConfig.getDataSourceNames().add("foo_ds");
        yamlConfig.getShadowAlgorithmNames().add("foo_algo");
        return YamlEngine.marshal(yamlConfig);
    }
    
    @Test
    void assertFindRuleConfiguration() {
        ShadowRuleConfiguration ruleConfig = mock(ShadowRuleConfiguration.class);
        assertThat(processor.findRuleConfiguration(mockDatabase(ruleConfig)), is(ruleConfig));
    }
    
    private ShardingSphereDatabase mockDatabase(final ShadowRuleConfiguration ruleConfig) {
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        return result;
    }
    
    @Test
    void assertChangeRuleItemConfiguration() {
        ShadowRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        ShadowTableConfiguration toBeChangedItemConfig = new ShadowTableConfiguration(Collections.singletonList("bar_ds"), Collections.singletonList("bar_algo"));
        processor.changeRuleItemConfiguration("foo_tbl", currentRuleConfig, toBeChangedItemConfig);
        assertThat(currentRuleConfig.getTables().size(), is(1));
        assertThat(currentRuleConfig.getTables().get("foo_tbl").getDataSourceNames(), is(Collections.singletonList("bar_ds")));
        assertThat(currentRuleConfig.getTables().get("foo_tbl").getShadowAlgorithmNames(), is(Collections.singletonList("bar_algo")));
    }
    
    @Test
    void assertDropRuleItemConfiguration() {
        ShadowRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        processor.dropRuleItemConfiguration("foo_tbl", currentRuleConfig);
        assertTrue(currentRuleConfig.getTables().isEmpty());
    }
    
    private ShadowRuleConfiguration createCurrentRuleConfiguration() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.getTables().put("foo_tbl", new ShadowTableConfiguration(Collections.singletonList("foo_ds"), Collections.singletonList("foo_algo")));
        return result;
    }
}
