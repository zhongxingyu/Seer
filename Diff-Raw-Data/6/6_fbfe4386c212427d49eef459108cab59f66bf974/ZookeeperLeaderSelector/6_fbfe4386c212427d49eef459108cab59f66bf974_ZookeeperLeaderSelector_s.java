 package example.services;
 
 import com.google.common.io.Closeables;
 import com.netflix.curator.framework.CuratorFramework;
 import com.netflix.curator.framework.CuratorFrameworkFactory;
 import com.netflix.curator.framework.recipes.leader.LeaderSelector;
 import com.netflix.curator.framework.recipes.leader.LeaderSelectorListener;
 import com.netflix.curator.retry.ExponentialBackoffRetry;
 import org.springframework.beans.factory.DisposableBean;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.context.ApplicationListener;
 
 public class ZookeeperLeaderSelector implements ApplicationListener<LeadershipRelinquishedEvent>, InitializingBean, DisposableBean {
 
     private final CuratorFramework client;
     private final LeaderSelector selector;
 
     private volatile boolean running = false;
 
     public ZookeeperLeaderSelector(LeaderSelectorListener listener) throws Exception {
         client = CuratorFrameworkFactory.builder()
                 .retryPolicy(new ExponentialBackoffRetry(100, 1000))
                 .connectString("127.0.0.1:2181")
                 .namespace("Spike")
                 .build();
 
         client.getZookeeperClient().setLog(new CuratorLoggingDriver());
 
        selector = new LeaderSelector(client, "/spike", listener);
     }
 
     @Override
     public void afterPropertiesSet() throws Exception {
         running = true;
         client.start();
         selector.start();
     }
 
     @Override
     public void onApplicationEvent(LeadershipRelinquishedEvent event) {
         if (running) {
             selector.start();
         }
     }
 
     @Override
     public void destroy() throws Exception {
         running = false;
         Closeables.closeQuietly(selector);
         Closeables.closeQuietly(client);
     }
 }
