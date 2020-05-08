 package org.tac.nil.checkpoint;
 
 import com.google.common.base.Throwables;
 import org.apache.commons.io.FileUtils;
 import org.apache.zookeeper.*;
 import org.apache.zookeeper.server.NIOServerCnxnFactory;
 import org.apache.zookeeper.server.ServerCnxnFactory;
 import org.apache.zookeeper.server.ZooKeeperServer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.UnknownHostException;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.atomic.AtomicInteger;
 
 public class Checkpoint {
   public static final int SESSION_TIMEOUT = 60000;
   private static Map<String, Integer> masterMutex = new ConcurrentHashMap<String, Integer>();
   private static ConcurrentHashMap<String, Integer> slaveMutex = new ConcurrentHashMap<String, Integer>();
   private static ZooKeeper zooKeeper = null;
   private static Logger logger = LoggerFactory.getLogger(Checkpoint.class);
   private static Watcher watcher = new Watcher() {
     @Override
     public void process(WatchedEvent watchedEvent) {
       if (Event.EventType.NodeChildrenChanged.equals(watchedEvent.getType())) {
         String path = watchedEvent.getPath();
         logger.debug(String.format("Process ZooKeeper watcher event NodeChildrenChanged: %s", path));
         String checkpointName = getCheckpointNameFromPath(watchedEvent.getPath());
         Integer mutex = masterMutex.get(checkpointName);
         assert null != mutex;
         logger.debug(String.format("Notify master mutex : %s", checkpointName));
         synchronized (mutex) {
           mutex.notifyAll();
         }
         return;
       } else if (Event.EventType.NodeDeleted.equals(watchedEvent.getType())) {
         String path = watchedEvent.getPath();
         logger.debug(String.format("Process ZooKeeper watcher event NodeDeleted: %s", path));
         String checkpointName = getCheckpointNameFromPath(watchedEvent.getPath());
         Integer mutex = slaveMutex.get(checkpointName);
         if (null != mutex) {
           synchronized (mutex) {
             mutex.notifyAll();
           }
         }
         slaveMutex.remove(checkpointName);
         return;
       }
     }
 
     public String getCheckpointNameFromPath(String path) {
       String[] splits = path.split("/");
       if (3 != splits.length) {
         throw new IllegalArgumentException(String.format("Could not get checkpoint name from path %s", path));
       }
       String checkpointName = splits[2];
       return checkpointName;
     }
   };
   private static ServerCnxnFactory serverFactory;
 
   public static void shutdownServer() {
     serverFactory.shutdown();
     zooKeeper = null;
     masterMutex.clear();
     slaveMutex.clear();
   }
 
   public static void startServer(String ipAndPort) {
     String[] split = ipAndPort.split(":");
     startServer(split[0], Integer.valueOf(split[1]));
   }
 
   public static void startServer(String ip, int port) {
     String dataDirectory = System.getProperty("java.io.tmpdir");
     File dir = new File(dataDirectory, "zookeeper").getAbsoluteFile();
     try {
       FileUtils.deleteDirectory(dir);
     } catch (IOException e) {
       Throwables.propagate(e);
     }
     try {
       ZooKeeperServer server = new ZooKeeperServer(dir, dir, 2000);
       serverFactory = NIOServerCnxnFactory.createFactory(new InetSocketAddress(ip, port), 1000);
       serverFactory.startup(server);
     } catch (IOException e) {
       Throwables.propagate(e);
     } catch (InterruptedException e) {
       Throwables.propagate(e);
     }
   }
 
   private static int globalTimeout = 300 * 1000;
 
   public static void setGlobalTimeout(int seconds) {
     globalTimeout = Math.max(0, seconds) * 1000;
   }
 
   public static synchronized void init(String address) {
     logger.debug(String.format("init checkpoint address = %s", address));
     try {
       zooKeeper = new ZooKeeper(address, SESSION_TIMEOUT, watcher);
     } catch (IOException e) {
       Throwables.propagate(e);
     }
     createRootPathIfNeed();
   }
 
   private static void createRootPathIfNeed() {
     if (!hasPath("/checkpoint", false)) {
       createPath("/checkpoint");
     }
   }
 
   private static boolean hasPath(String path, boolean watch) {
     try {
       return null != zooKeeper.exists(path, watch);
     } catch (KeeperException e) {
       Throwables.propagate(e);
     } catch (InterruptedException e) {
       Throwables.propagate(e);
     }
     return false;
   }
 
   private static void createPath(String path) {
     try {
       zooKeeper.create(path, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
     } catch (KeeperException e) {
       Throwables.propagate(e);
     } catch (InterruptedException e) {
       Throwables.propagate(e);
     }
   }
 
   private static void checkInit() {
     if (null == zooKeeper) {
       throw new IllegalStateException("Checkpoint is not init");
     }
   }
 
   public static void add(String checkpointName, int expectedKickCount) {
     logger.debug(String.format("Add checkpoint : %s", checkpointName));
     checkInit();
     String path = String.format("/checkpoint/%s", checkpointName);
     if (hasPath(path, true)) {
       deletePath(path);
     }
     Checkpoint.masterMutex.put(checkpointName, new Integer(expectedKickCount));
     createPath(path);
   }
 
   private static void deletePath(String path) {
     try {
       List<String> children = zooKeeper.getChildren(path, false);
       for (String child : children) {
         zooKeeper.delete(String.format("%s/%s", path, child), -1);
       }
       zooKeeper.delete(path, -1);
     } catch (KeeperException e) {
       Throwables.propagate(e);
     } catch (InterruptedException e) {
       Throwables.propagate(e);
     }
   }
 
   public static void add(String checkpointName) {
     add(checkpointName, 1);
   }
 
   public static void delete(String checkpointName) {
     logger.debug(String.format("Delete checkpoint : %s", checkpointName));
     checkInit();
     String path = String.format("/checkpoint/%s", checkpointName);
     deletePath(path);
     Checkpoint.masterMutex.remove(checkpointName);
     Checkpoint.slaveMutex.remove(checkpointName);
   }
 
   public static void kick(String checkpointName) {
     logger.debug(String.format("Kick checkpoint : %s", checkpointName));
     if (null == zooKeeper) {
       logger.debug("No zookeeper found, ignore kick");
       return;
     }
    try {
      checkInit();
      if (!hasPath(String.format("/checkpoint/%s", checkpointName), true)) {
        logger.debug(String.format("No checkpoint %s found, ignore kick", checkpointName));
        return;
      }
      createPath(String.format("/checkpoint/%s/%s", checkpointName, getCheckpointId()));
      slaveMutex.putIfAbsent(checkpointName, new Integer(1));
      Integer mutex = slaveMutex.get(checkpointName);
      synchronized (mutex) {
        try {
          logger.debug(String.format("Waiting slave mutex : %s", checkpointName));
          mutex.wait(globalTimeout);
        } catch (InterruptedException e) {
          Throwables.propagate(e);
        }
       }
    } catch (Throwable throwable) {
      logger.error(Throwables.getStackTraceAsString(throwable));
     }
   }
 
   private static AtomicInteger kickSeed = new AtomicInteger(1);
 
   private static final long seed = System.nanoTime();
 
   private static String getCheckpointId() {
     try {
       return String.format("%s-%s-%s",
               InetAddress.getLocalHost().getCanonicalHostName(),
               seed,
               kickSeed.addAndGet(1));
     } catch (UnknownHostException e) {
       Throwables.propagate(e);
     }
     return null;
   }
 
   public static void hold(String checkpointName) {
     logger.debug(String.format("Hold checkpoint : %s", checkpointName));
     checkInit();
     assert masterMutex.containsKey(checkpointName);
     Integer mutex = masterMutex.get(checkpointName);
     while (true) {
       synchronized (mutex) {
         int childrenSize = getPathChildrenSize(String.format("/checkpoint/%s", checkpointName));
         if (childrenSize < mutex) {
           try {
             logger.debug(String.format("Waiting master mutex : %s", checkpointName));
             mutex.wait(globalTimeout);
           } catch (InterruptedException e) {
             Throwables.propagate(e);
           }
         } else {
           break;
         }
       }
     }
   }
 
 
   public static void holdAndCont(String checkpointName) {
     hold(checkpointName);
     cont(checkpointName);
   }
 
 
   public static void holdUntilTimeout(String checkpointName, long timeoutSeconds) throws InterruptedException {
     logger.debug(String.format("Hold checkpoint : %s", checkpointName));
     checkInit();
     assert masterMutex.containsKey(checkpointName);
     Integer mutex = masterMutex.get(checkpointName);
     while (true) {
       synchronized (mutex) {
         int childrenSize = getPathChildrenSize(String.format("/checkpoint/%s", checkpointName));
         if (childrenSize < mutex) {
           try {
             logger.debug(String.format("Waiting master mutex : %s", checkpointName));
             mutex.wait(timeoutSeconds * 1000);
           } catch (InterruptedException e) {
             throw e;
           }
         } else {
           break;
         }
       }
     }
   }
 
   private static int getPathChildrenSize(String path) {
     int childrenSize = 0;
     try {
       childrenSize = zooKeeper.getChildren(path, true).size();
     } catch (KeeperException e) {
       Throwables.propagate(e);
     } catch (InterruptedException e) {
       Throwables.propagate(e);
     }
     return childrenSize;
   }
 
   public static void cont(String checkpointName) {
     logger.debug(String.format("Cont checkpoint %s", checkpointName));
     String path = String.format("/checkpoint/%s", checkpointName);
     deletePath(path);
   }
 }
