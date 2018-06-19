/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.proxy.yaml;

import com.google.common.base.Optional;
import io.shardingsphere.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.core.rule.MasterSlaveRule;
import io.shardingsphere.core.rule.ProxyAuthority;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.yaml.masterslave.YamlMasterSlaveRuleConfiguration;
import io.shardingsphere.core.yaml.sharding.YamlShardingRuleConfiguration;
import io.shardingsphere.jdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingsphere.jdbc.orchestration.internal.OrchestrationFacade;
import io.shardingsphere.jdbc.orchestration.yaml.YamlOrchestrationConfiguration;
import io.shardingsphere.proxy.config.RuleRegistry;
import lombok.Getter;
import lombok.Setter;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Yaml sharding configuration for proxy.
 *
 * @author zhangyonglun
 * @author panjuan
 */
@Getter
@Setter
public final class YamlProxyConfiguration {
    
    private Map<String, DataSourceParameter> dataSources = new HashMap<>();
    
    private YamlMasterSlaveRuleConfiguration masterSlaveRule = new YamlMasterSlaveRuleConfiguration();
    
    private YamlShardingRuleConfiguration shardingRule = new YamlShardingRuleConfiguration();
    
    private ProxyAuthority proxyAuthority = new ProxyAuthority();
    
    private boolean withoutJdbc;
    
    private String transactionMode;
    
    private YamlOrchestrationConfiguration orchestration;
    
    /**
     * Unmarshal yaml sharding configuration from yaml file.
     * 
     * @param yamlFile yaml file
     * @return yaml sharding configuration
     * @throws IOException IO Exception
     *
     */
    public static YamlProxyConfiguration unmarshal(final File yamlFile) throws IOException {
        try (
                FileInputStream fileInputStream = new FileInputStream(yamlFile);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8")
        ) {
            return new Yaml(new Constructor(YamlProxyConfiguration.class)).loadAs(inputStreamReader, YamlProxyConfiguration.class);
        }
    }
    
    /**
     * Unmarshal yaml sharding configuration from yaml bytes.
     * 
     * @param yamlBytes yaml bytes
     * @return yaml sharding configuration
     * @throws IOException IO Exception
     */
    public static YamlProxyConfiguration unmarshal(final byte[] yamlBytes) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(yamlBytes)) {
            return new Yaml(new Constructor(YamlProxyConfiguration.class)).loadAs(inputStream, YamlProxyConfiguration.class);
        }
    }
    
    /**
     * Initialize yaml proxy configuration.
     *
     */
    public void init() {
        if (isUsingRegistryCenter()) {
            initFromRegistryCenter();
        }
    }
    
    private void initFromRegistryCenter() {
        OrchestrationFacade orchestrationFacade = new OrchestrationFacade(obtainOrchestrationConfigurationOptional().get());
        renew(orchestrationFacade.getConfigService().loadDataSourceParameter(), orchestrationFacade.getConfigService().loadProxyConfiguration());
    }
    
    private boolean isUsingRegistryCenter() {
        return null != orchestration && shardingRule.getTables().isEmpty() && masterSlaveRule.getMasterDataSourceName().isEmpty();
    }
    
    /**
     * Renew yaml proxy configuration.
     *
     * @param dataSources data sources
     * @param yamlProxyConfiguration yaml proxy configuration
     */
    public void renew(final Map<String, DataSourceParameter> dataSources, final YamlProxyConfiguration yamlProxyConfiguration) {
        setDataSources(dataSources);
        setMasterSlaveRule(yamlProxyConfiguration.getMasterSlaveRule());
        setShardingRule(yamlProxyConfiguration.getShardingRule());
        setProxyAuthority(yamlProxyConfiguration.getProxyAuthority());
        setWithoutJdbc(yamlProxyConfiguration.isWithoutJdbc());
        setTransactionMode(yamlProxyConfiguration.getTransactionMode());
        setOrchestration(yamlProxyConfiguration.getOrchestration());
        RuleRegistry.getInstance().init(this);
    }
    
    /**
     * Get sharding rule from yaml.
     *
     * @param dataSourceNames data source names
     * @return sharding rule from yaml
     */
    public ShardingRule obtainShardingRule(final Collection<String> dataSourceNames) {
        return new ShardingRule(shardingRule.getShardingRuleConfiguration(), dataSourceNames.isEmpty() ? dataSources.keySet() : dataSourceNames);
    }
    
    /**
     * Get master slave rule from yaml.
     *
     * @return master slave rule.
     */
    public MasterSlaveRule obtainMasterSlaveRule() {
        return null == masterSlaveRule.getMasterDataSourceName() ? new MasterSlaveRule(new MasterSlaveRuleConfiguration("", "", Arrays.asList(""), null))
                : new MasterSlaveRule(masterSlaveRule.getMasterSlaveRuleConfiguration());
    }
    
    /**
     * Get Orchestration configuration from yaml.
     *
     * @return Orchestration configuration
     */
    public Optional<OrchestrationConfiguration> obtainOrchestrationConfigurationOptional() {
        return isUsingRegistryCenter() ? Optional.fromNullable(orchestration.getOrchestrationConfiguration()) : Optional.<OrchestrationConfiguration>absent();
    }
}
