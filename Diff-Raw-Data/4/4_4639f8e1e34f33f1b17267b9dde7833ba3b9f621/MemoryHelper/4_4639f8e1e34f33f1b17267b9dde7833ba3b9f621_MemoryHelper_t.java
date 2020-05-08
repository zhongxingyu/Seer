 package LinkFuture.Core.MemoryManager;
 
 import LinkFuture.Core.Debugger;
 import LinkFuture.Core.ObjectExtend.MyRunnable;
 
 
 import java.io.IOException;
 import java.nio.file.*;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: cyokin
  * Date: 10/12/13
  * Time: 2:19 PM
  * To change this template use File | Settings | File Templates.
  */
 public class MemoryHelper {
     private static final HashMap<String,MemoryCacheInfo> cachedObject = new HashMap<>();
     private static int MemoryCacheInterval = 1000*5;
     private static Timer timer = null;
 
     public static Object AddMemoryCache(MemoryCacheMetaInfo meta) throws Exception {
         if(!cachedObject.containsKey(meta.getUniqueKey()))
         {
             RunAction(meta);
         }
         return cachedObject.get(meta.getUniqueKey()).CachedObject;
     }
     public static<T> T AddMemoryCache(MemoryCacheMetaInfo meta,Class<T> target ) throws Exception {
            return (T)AddMemoryCache(meta);
     }
     public static void ClearCache(MemoryCacheMetaInfo meta){
         cachedObject.remove(meta.getUniqueKey());
     }
     public static MemoryCacheMetaInfo FindMemoryCache(String cacheSettingName)
     {
         if(!MemoryCacheSettingListInfo.ConfigurationMeta.SettingList.containsKey(cacheSettingName))
         {
             return null;
         }
         MemoryCacheSettingInfo setting =  MemoryCacheSettingListInfo.ConfigurationMeta.SettingList.get(cacheSettingName);
         MemoryCacheMetaInfo meta = new MemoryCacheMetaInfo();
         meta.Duration =  setting.Duration;
         meta.CacheSettingName = setting.CacheSettingName;
         meta.AutoRefresh = setting.AutoRefresh;
         meta.Enable = setting.Enable;
         meta.CacheType = setting.CacheType;
         return meta;
     }
 
     private static void RunAction(MemoryCacheMetaInfo meta) throws Exception {
         StartTime();
         MemoryCacheInfo memory = new MemoryCacheInfo();
         memory.CachedObject = meta.Action.run(meta.Arguments);
         memory.Meta = meta;
         memory.NextExpiredTime = meta.getTimeSpan().GetDate();
         cachedObject.put(meta.getUniqueKey(),memory);
     }
     private static void StartTime() {
         if(timer==null)
         {
             timer = new Timer();
             timer.scheduleAtFixedRate(new TimerTask() {
                 @Override
                 public void run() {
                     refreshMemory();
                 }
             },new Date(),MemoryCacheInterval);
         }
     }
     private static void refreshMemory() {
         long currentTime = new Date().getTime();
         for (MemoryCacheInfo memory:cachedObject.values())
         {
             switch (memory.Meta.CacheType) {
                 case Absolute:
                    if(memory.NextExpiredTime.getTime() <= currentTime)
                     {
                        //TODO: auto refresh always have 5s delay for somehow, will fix it later.
                         if(memory.Meta.AutoRefresh)
                         {
                             try {
                                 new Thread(new MyRunnable(memory.Meta) {
                                     @Override
                                     public void run(Object[] args) {
                                         try {
                                             RunAction((MemoryCacheMetaInfo)args[0]);
                                         } catch (Exception e) {
                                             //do nothing in thread;
                                             Debugger.traceln(e);
                                         }
                                     }
                                 }).start();
                             } catch (Exception e) {
                                 Debugger.traceln(e);
                             }
                         }
                         else {
                             cachedObject.remove(memory.Meta.getUniqueKey());
                         }
                     }
                     break;
                 case Never:
                     continue;
                 case File:
                     break;
             }
         }
     }
     //TODO: will add file monitor later
     private static void AddFileMonitor(MemoryCacheMetaInfo meta) throws IOException {
 //        if(meta.CacheType == MemoryCacheType.File)
 //        {
 //            Path myDir = Paths.get(meta.FilePath);
 //            WatchService watcher = myDir.getFileSystem().newWatchService();
 //            while (true)
 //            {
 //                try {
 //                    WatchKey watchKey = myDir.register(watcher,StandardWatchEventKinds.ENTRY_MODIFY) ;
 //                    for (WatchEvent<?> event : watchKey.pollEvents()) {
 //                        WatchEvent<Path> watchEvent = (WatchEvent<Path>) event;
 //                        WatchEvent.Kind<Path> kind = watchEvent.kind();
 //
 //                        System.out.println(watchEvent.context() + ", count: " +
 //                                watchEvent.count() + ", event: " + watchEvent.kind());
 //                        // prints (loop on the while twice)
 //                        // servers.cfg, count: 1, event: ENTRY_MODIFY
 //                        // servers.cfg, count: 1, event: ENTRY_MODIFY
 //
 //                        switch (kind.name()) {
 //                            case "ENTRY_MODIFY":
 //                                //handleModify(watchEvent.context()); // reload configuration class
 //                                break;
 //                            case "ENTRY_DELETE":
 //                                //handleDelete(watchEvent.context()); // do something else
 //                                break;
 //                            default:
 //                                System.out.println("Event not expected " + event.kind().name());
 //                        }
 //                    }
 //                }
 //                catch (IOException e) {
 //                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 //                }
 //            }
 //        }
     }
 }
