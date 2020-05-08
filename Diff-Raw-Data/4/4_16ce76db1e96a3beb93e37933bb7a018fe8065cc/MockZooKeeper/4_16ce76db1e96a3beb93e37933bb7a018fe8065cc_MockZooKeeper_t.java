 package org.apache.zookeeper;
 
 import java.lang.reflect.Constructor;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.apache.zookeeper.AsyncCallback.Children2Callback;
 import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
 import org.apache.zookeeper.AsyncCallback.DataCallback;
 import org.apache.zookeeper.AsyncCallback.StatCallback;
 import org.apache.zookeeper.AsyncCallback.StringCallback;
 import org.apache.zookeeper.AsyncCallback.VoidCallback;
 import org.apache.zookeeper.Watcher.Event.EventType;
 import org.apache.zookeeper.Watcher.Event.KeeperState;
 import org.apache.zookeeper.data.ACL;
 import org.apache.zookeeper.data.Stat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.testng.internal.annotations.Sets;
 
 import sun.reflect.ReflectionFactory;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Multimaps;
 import com.google.common.collect.SetMultimap;
 
 @SuppressWarnings({ "deprecation", "restriction", "rawtypes" })
 public class MockZooKeeper extends ZooKeeper {
     private SortedMap<String, String> tree;
     private SetMultimap<String, Watcher> watchers;
     private AtomicBoolean stopped;
 
     private ExecutorService executor;
 
     private AtomicInteger stepsToFail;
     private KeeperException.Code failReturnCode;
 
     public static MockZooKeeper newInstance() {
         try {
             ReflectionFactory rf = ReflectionFactory.getReflectionFactory();
             Constructor objDef = Object.class.getDeclaredConstructor(new Class[0]);
             Constructor intConstr = rf.newConstructorForSerialization(MockZooKeeper.class, objDef);
             MockZooKeeper zk = MockZooKeeper.class.cast(intConstr.newInstance());
             zk.init();
             return zk;
         } catch (RuntimeException e) {
             throw e;
         } catch (Exception e) {
             throw new IllegalStateException("Cannot create object", e);
         }
     }
 
     private void init() {
         executor = Executors.newFixedThreadPool(1);
         tree = Collections.synchronizedSortedMap(new TreeMap<String, String>());
         SetMultimap<String, Watcher> w = HashMultimap.create();
         watchers = Multimaps.synchronizedSetMultimap(w);
         stopped = new AtomicBoolean(false);
         stepsToFail = new AtomicInteger(-1);
         failReturnCode = KeeperException.Code.OK;
     }
 
     private MockZooKeeper(String quorum) throws Exception {
         // This constructor is never called
         super(quorum, 1, new Watcher() {
             @Override
             public void process(WatchedEvent event) {
             }
         });
         assert false;
     }
 
     @Override
     public States getState() {
         return States.CONNECTED;
     }
 
     @Override
     public String create(String path, byte[] data, List<ACL> acl, CreateMode createMode) throws KeeperException,
             InterruptedException {
         checkProgrammedFail();
 
         if (stopped.get())
             throw new KeeperException.ConnectionLossException();
 
         if (tree.containsKey(path)) {
             throw new KeeperException.NodeExistsException(path);
         }
         tree.put(path, new String(data));
         return path;
     }
 
     @Override
     public void create(final String path, final byte[] data, final List<ACL> acl, CreateMode createMode,
             final StringCallback cb, final Object ctx) {
         executor.execute(new Runnable() {
             public void run() {
                 if (getProgrammedFailStatus()) {
                     cb.processResult(failReturnCode.intValue(), path, ctx, null);
                 } else if (stopped.get()) {
                     cb.processResult(KeeperException.Code.CONNECTIONLOSS.intValue(), path, ctx, null);
                 } else if (tree.containsKey(path)) {
                     cb.processResult(KeeperException.Code.NODEEXISTS.intValue(), path, ctx, null);
                 } else {
                     tree.put(path, new String(data));
                     cb.processResult(0, path, ctx, null);
                 }
             }
         });
     }
 
     @Override
     public byte[] getData(String path, Watcher watcher, Stat stat) throws KeeperException {
         checkProgrammedFail();
 
         String value = tree.get(path);
         if (value == null) {
             throw new KeeperException.NoNodeException(path);
         } else {
             if (watcher != null)
                 watchers.put(path, watcher);
             return value.getBytes();
         }
     }
 
     @Override
     public void getData(final String path, boolean watch, final DataCallback cb, final Object ctx) {
         executor.execute(new Runnable() {
             public void run() {
                 if (getProgrammedFailStatus()) {
                     cb.processResult(failReturnCode.intValue(), path, ctx, null, null);
                     return;
                 } else if (stopped.get()) {
                     cb.processResult(KeeperException.Code.ConnectionLoss, path, ctx, null, null);
                     return;
                 }
 
                 String value = tree.get(path);
                 if (value == null) {
                     cb.processResult(KeeperException.Code.NoNode, path, ctx, null, null);
                 } else {
                     cb.processResult(0, path, ctx, value.getBytes(), new Stat());
                 }
             }
         });
     }
 
     @Override
     public void getData(final String path, final Watcher watcher, final DataCallback cb, final Object ctx) {
         executor.execute(new Runnable() {
             public void run() {
                 if (getProgrammedFailStatus()) {
                     cb.processResult(failReturnCode.intValue(), path, ctx, null, null);
                     return;
                 } else if (stopped.get()) {
                     cb.processResult(KeeperException.Code.CONNECTIONLOSS.intValue(), path, ctx, null, null);
                     return;
                 }
 
                 String value = tree.get(path);
                 if (value == null) {
                     cb.processResult(KeeperException.Code.NONODE.intValue(), path, ctx, null, null);
                 } else {
                     if (watcher != null)
                         watchers.put(path, watcher);
 
                     cb.processResult(0, path, ctx, value.getBytes(), new Stat());
                 }
             }
         });
     }
 
     @Override
     public void getChildren(final String path, final Watcher watcher, final ChildrenCallback cb, final Object ctx) {
         executor.execute(new Runnable() {
             public void run() {
                 if (getProgrammedFailStatus()) {
                     cb.processResult(failReturnCode.intValue(), path, ctx, null);
                     return;
                 } else if (stopped.get()) {
                     cb.processResult(KeeperException.Code.ConnectionLoss, path, ctx, null);
                     return;
                 }
 
                 List<String> children = Lists.newArrayList();
                 for (String item : tree.tailMap(path).keySet()) {
                     if (!item.startsWith(path)) {
                         break;
                     } else {
                        if (path.length() >= item.length()) {
                            continue;
                        }

                         String child = item.substring(path.length() + 1);
                         if (!child.contains("/")) {
                             children.add(child);
                         }
                     }
                 }
 
                 cb.processResult(0, path, ctx, children);
                 if (watcher != null)
                     watchers.put(path, watcher);
             }
         });
     }
 
     @Override
     public List<String> getChildren(String path, boolean watch) throws KeeperException, InterruptedException {
         checkProgrammedFail();
 
         if (stopped.get()) {
             throw new KeeperException.ConnectionLossException();
         }
 
         List<String> children = Lists.newArrayList();
         for (String item : tree.tailMap(path).keySet()) {
             if (!item.startsWith(path)) {
                 break;
             } else if (item.equals(path)) {
                 continue;
             } else {
                 String child = item.substring(path.length() + 1);
                 log.debug("path: '{}' -- item: '{}' -- child: '{}'", new Object[] { path, item, child });
                 if (!child.contains("/")) {
                     children.add(child);
                 }
             }
         }
 
         return children;
     }
 
     @Override
     public void getChildren(final String path, boolean watcher, final Children2Callback cb, final Object ctx) {
         executor.execute(new Runnable() {
             public void run() {
                 if (getProgrammedFailStatus()) {
                     cb.processResult(failReturnCode.intValue(), path, ctx, null, null);
                     return;
                 } else if (stopped.get()) {
                     cb.processResult(KeeperException.Code.ConnectionLoss, path, ctx, null, null);
                     return;
                 }
 
                 log.debug("getChildren path={}", path);
                 List<String> children = Lists.newArrayList();
                 for (String item : tree.tailMap(path).keySet()) {
                     log.debug("Checking path {}", item);
                     if (!item.startsWith(path)) {
                         break;
                     } else if (item.equals(path)) {
                         continue;
                     } else {
                         String child = item.substring(path.length() + 1);
                         log.debug("child: '{}'", child);
                         if (!child.contains("/")) {
                             children.add(child);
                         }
                     }
                 }
 
                 log.debug("getChildren done path={} result={}", path, children);
                 cb.processResult(0, path, ctx, children, new Stat());
             }
         });
     }
 
     @Override
     public Stat exists(String path, boolean watch) throws KeeperException, InterruptedException {
         checkProgrammedFail();
 
         if (stopped.get())
             throw new KeeperException.ConnectionLossException();
 
         if (tree.containsKey(path)) {
             return new Stat();
         } else {
             return null;
         }
     }
 
     @Override
     public Stat setData(String path, byte[] data, int version) throws KeeperException, InterruptedException {
         checkProgrammedFail();
 
         if (stopped.get())
             throw new KeeperException.ConnectionLossException();
 
         tree.put(path, new String(data));
         Set<Watcher> toNotify = Sets.newHashSet();
         toNotify.addAll(watchers.get(path));
         watchers.removeAll(path);
 
         for (Watcher watcher : toNotify) {
             watcher.process(new WatchedEvent(EventType.NodeDataChanged, KeeperState.SyncConnected, path));
         }
 
         return new Stat();
     }
 
     @Override
     public void setData(final String path, final byte[] data, int version, final StatCallback cb, final Object ctx) {
         executor.execute(new Runnable() {
             public void run() {
                 if (getProgrammedFailStatus()) {
                     cb.processResult(failReturnCode.intValue(), path, ctx, null);
                     return;
                 } else if (stopped.get()) {
                     cb.processResult(KeeperException.Code.ConnectionLoss, path, ctx, null);
                     return;
                 }
 
                 try {
                     Thread.sleep(5);
                 } catch (InterruptedException e) {
                 }
 
                 tree.put(path, new String(data));
                 cb.processResult(0, path, ctx, new Stat());
 
                 for (Watcher watcher : watchers.get(path)) {
                     watcher.process(new WatchedEvent(EventType.NodeDataChanged, KeeperState.SyncConnected, path));
                 }
 
                 watchers.removeAll(path);
             }
         });
     }
 
     @Override
     public void delete(String path, int version) throws InterruptedException, KeeperException {
         checkProgrammedFail();
 
         if (stopped.get())
             throw new KeeperException.ConnectionLossException();
         if (!tree.containsKey(path))
             throw new KeeperException.NoNodeException(path);
         tree.remove(path);
 
         for (Watcher watcher : watchers.get(path)) {
             watcher.process(new WatchedEvent(EventType.NodeDeleted, KeeperState.SyncConnected, path));
         }
 
         watchers.removeAll(path);
     }
 
     @Override
     public void delete(final String path, int version, final VoidCallback cb, final Object ctx) {
         executor.execute(new Runnable() {
             public void run() {
                 if (getProgrammedFailStatus()) {
                     cb.processResult(failReturnCode.intValue(), path, ctx);
                 } else if (stopped.get()) {
                     cb.processResult(KeeperException.Code.CONNECTIONLOSS.intValue(), path, ctx);
                 } else if (!tree.containsKey(path)) {
                     cb.processResult(KeeperException.Code.NONODE.intValue(), path, ctx);
                 } else {
                     tree.remove(path);
                     cb.processResult(0, path, ctx);
 
                     for (Watcher watcher : watchers.get(path)) {
                         watcher.process(new WatchedEvent(EventType.NodeDeleted, KeeperState.SyncConnected, path));
                     }
 
                     watchers.removeAll(path);
                 }
             }
         });
     }
 
     @Override
     public void close() throws InterruptedException {
     }
 
     public void shutdown() throws InterruptedException {
         stopped.set(true);
         tree.clear();
         watchers.clear();
     }
 
     void checkProgrammedFail() throws KeeperException {
         if (stepsToFail.getAndDecrement() == 0) {
             throw KeeperException.create(failReturnCode);
         }
     }
 
     boolean getProgrammedFailStatus() {
         return stepsToFail.getAndDecrement() == 0;
     }
 
     public void failNow(KeeperException.Code rc) {
         failAfter(0, rc);
     }
 
     public void failAfter(int steps, KeeperException.Code rc) {
         stepsToFail.set(steps);
         failReturnCode = rc;
     }
 
     private static final Logger log = LoggerFactory.getLogger(MockZooKeeper.class);
 }
