package ru.fix.dynamic.config;


import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * the test is to move zk config from one location to another location. existing properties will be overrided
 */
@Ignore
public class ZookeeperTool {

    private final Logger log = LoggerFactory.getLogger(ZookeeperTool.class);

    private static final String ZK_ROOT = "/cpapsm";

    private static CuratorFramework curatorFramework;
    private static CuratorFramework curatorFrameworkDest;

    @BeforeClass
    public static void setUp() {
        curatorFramework = CuratorFrameworkFactory.newClient("srv-zookeeper-1", new ExponentialBackoffRetry(1000, 10));
        curatorFramework.start();
    }

    @AfterClass
    public static void tearDown() {
        curatorFramework.close();
    }

    @Test
    public void changeProp() throws Exception {
        //curatorFramework.setData()
        //        .forPath("/cpapsm/config/auth.ip.filter.disable", "true".getBytes(StandardCharsets.UTF_8));
        //curatorFramework.setData().forPath("/cpapsm/config/auth.disable", "true".getBytes(StandardCharsets.UTF_8));
        //curatorFramework.setData().forPath("/cpapsm/config/keys.ttl", "5000".getBytes(StandardCharsets.UTF_8));
        //curatorFramework.setData().forPath("/cpapsm/config/keys.count", "48".getBytes(StandardCharsets.UTF_8));
        //byte[] bytes = curatorFramework.getData().forPath("/cpapsm/config/keys.count");
        //System.out.println(new String(bytes));
    }

    @Test
    public void copyProperties() throws Exception {
        Properties boProperties = getAllPropertiesFromPath("/back-office/config");
        Properties foProperties = getAllPropertiesFromPath("/front-office/config");
        Properties swsProperties = getAllPropertiesFromPath("/sws/config");
        Properties spsProperties = getAllPropertiesFromPath("/sps/config");
        Properties smppProperties = getAllPropertiesFromPath("/smpp/config");
        putPropertiesToPath("/config", boProperties);
        putPropertiesToPath("/config", foProperties);
        putPropertiesToPath("/config", swsProperties);
        putPropertiesToPath("/config", spsProperties);
        putPropertiesToPath("/config", smppProperties);
    }

    @Test
    public void copyProfile() throws Exception {
        Properties boProperties = getAllPropertiesFromPath("/cpapsm-test", "/config");
        putPropertiesToPath("/cpapsm-qa", "/config", boProperties);
    }

    @Test
    public void copyProfileToDifferentCluster() throws Exception {
        String srcZkHost = "srv-zookeeper-1";
        String destZkHost = "qa02vaspdn01.ivasp.de";
        String zkRoot = "/cpapsm-check-3";

        CuratorFramework curatorFrameworkDest =
                CuratorFrameworkFactory.newClient(destZkHost, new ExponentialBackoffRetry(1000, 10));
        curatorFrameworkDest.start();

        Properties properties = getAllPropertiesFromPath(zkRoot, "/config", srcZkHost);
        putPropertiesToPath(curatorFrameworkDest, zkRoot, "/config", properties);
    }

    @Test
    public void removeZkRoot() throws Exception {
        String zkRoot = "/cpapsm-test";
        curatorFramework.delete().deletingChildrenIfNeeded().forPath(zkRoot);
    }

    @Test
    public void diff() throws Exception {
        Properties boPropertiesDev = getAllPropertiesFromPath("/cpapsm", "/config");
        Properties boPropertiesQa = getAllPropertiesFromPath("/cpapsm-qa", "/config");

        System.out.println("--------------DIFF property-----------------");
        boPropertiesDev.entrySet().forEach(e -> {
            Object key = e.getKey();

            Object valDev = e.getValue();
            Object valQa = boPropertiesQa.get(key);

            if (!valDev.equals(valQa)) {

                System.out.printf("Property '%s': dev '%s', qa '%s'%n", key, valDev, valQa);
            }

        });
        System.out.println("----------------------------------------------------------");
    }

    @Test
    public void removeConfigs() throws Exception {
        removeNode("/back-office");
        removeNode("/front-office");
        removeNode("/sws");
        removeNode("/sps");
        removeNode("/smpp");
    }

    @Test
    public void findValue() throws Exception {
        String configName = "hbase.table.prefix";
        String configValue = "QA_";

        List<String> rootNameList = getAllZkRoots(null);
        List<String> findInRoot = new ArrayList<>();

        for (String rootName : rootNameList) {
            String zkRoot = "/" + rootName;
            String zkPath = "/config/" + configName;
            String value = getProperty(zkRoot, zkPath, null);
            if (configValue.equals(value)) {
                findInRoot.add(rootName);
            }
        }
        System.out.println(findInRoot);
    }

    private String getProperty(String zkRoot, String nodePath, String zkHost) throws Exception {
        CuratorFramework curatorFramework;
        if (StringUtils.isEmpty(zkHost)) {
            curatorFramework = ZookeeperTool.curatorFramework;
        } else {
            curatorFramework = CuratorFrameworkFactory.newClient(zkHost, new ExponentialBackoffRetry(1000, 10));
            curatorFramework.start();
        }

        String node = zkRoot + nodePath;
        Stat exist = curatorFramework.checkExists().forPath(node);
        if (exist != null) {
            return new String(curatorFramework.getData().forPath(node), StandardCharsets.UTF_8);
        } else {
            return null;
        }
    }

    private void removeNode(String zkRoot, String nodePath) throws Exception {
        String node = zkRoot + nodePath;
        curatorFramework.delete().deletingChildrenIfNeeded().forPath(node);
    }

    private void removeNode(String nodePath) throws Exception {
        removeNode(ZK_ROOT, nodePath);
    }

    private void putPropertiesToPath(String nodePath, Properties properties) throws Exception {
        putPropertiesToPath(ZK_ROOT, nodePath, properties);
    }

    private void putPropertiesToPath(String zkRoot, String nodePath, Properties properties) throws Exception {
        putPropertiesToPath(curatorFramework, zkRoot, nodePath, properties);
    }

    private void putPropertiesToPath(CuratorFramework curatorFramework, String zkRoot,
                                     String nodePath, Properties properties) throws Exception {
        String node = zkRoot + nodePath;
        for (String propName : properties.stringPropertyNames()) {
            String propPath = node + "/" + propName;
            String propVal = properties.getProperty(propName);

            log.info("property:[{}] = [{}]", propPath, propVal);

            Stat stat = curatorFramework.checkExists().forPath(propPath);
            if (stat != null) {
                curatorFramework.setData().forPath(propPath, propVal.getBytes(StandardCharsets.UTF_8));
            } else {
                curatorFramework.create()
                        .creatingParentsIfNeeded()
                        .forPath(propPath, propVal.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private Properties getAllPropertiesFromPath(String nodePath) throws Exception {
        return getAllPropertiesFromPath(ZK_ROOT, nodePath);
    }

    private Properties getAllPropertiesFromPath(String zkRoot, String nodePath, String zkHost) throws Exception {
        CuratorFramework curatorFramework;
        if (StringUtils.isEmpty(zkHost)) {
            curatorFramework = ZookeeperTool.curatorFramework;
        } else {
            curatorFramework = CuratorFrameworkFactory.newClient(zkHost, new ExponentialBackoffRetry(1000, 10));
            curatorFramework.start();
        }

        String node = zkRoot + nodePath;
        Properties allProperties = new Properties();
        Stat exist = curatorFramework.checkExists().forPath(node);
        if (exist != null) {
            for (String property : curatorFramework.getChildren().forPath(node)) {
                allProperties.put(property,
                        new String(curatorFramework.getData().forPath(node + "/" + property), StandardCharsets.UTF_8));
            }
        }
        return allProperties;
    }

    private List<String> getAllZkRoots(String zkHost) throws Exception {
        CuratorFramework curatorFramework;
        if (StringUtils.isEmpty(zkHost)) {
            curatorFramework = ZookeeperTool.curatorFramework;
        } else {
            curatorFramework = CuratorFrameworkFactory.newClient(zkHost, new ExponentialBackoffRetry(1000, 10));
            curatorFramework.start();
        }

        Stat exist = curatorFramework.checkExists().forPath("/");
        List<String> roots = new ArrayList<>();
        if (exist != null) {
            for (String property : curatorFramework.getChildren().forPath("/")) {
                roots.add(property);
            }
        }
        return roots;
    }

    private Properties getAllPropertiesFromPath(String zkRoot, String nodePath) throws Exception {
        return getAllPropertiesFromPath(zkRoot, nodePath, null);
    }
}