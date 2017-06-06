package ru.fix.dynamic.config.zk.spring;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.fix.dynamic.config.api.DynamicPropertyMarshaller;
import ru.fix.dynamic.config.api.JSonPropertyMarshaller;
import ru.fix.dynamic.config.zk.ZkConfigPropertySource;

/**
 * @author Ayrat Zulkarnyaev
 */
@Configuration
public class ZkPropertySourceConfig {

    @Bean
    @ConditionalOnBean(CuratorFramework.class)
    ZkConfigPropertySource zkConfigPropertySource(
            CuratorFramework curatorFramework,
            @Value("${zk-root}") String zkRoot,
            DynamicPropertyMarshaller propertyMarshaller
    ) throws Exception {
        return new ZkConfigPropertySource(curatorFramework, zkRoot + "/config", propertyMarshaller);
    }

    @Bean
    DynamicPropertyMarshaller dynamicPropertyMarshaller() {
        return new JSonPropertyMarshaller();
    }

}
