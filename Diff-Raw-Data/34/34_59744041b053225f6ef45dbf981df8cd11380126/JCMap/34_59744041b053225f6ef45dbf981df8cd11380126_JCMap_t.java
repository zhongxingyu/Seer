 package jchazelcast;
 
 import java.io.IOException;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 
 public class JCMap  {
     static   Map<String,Set<EntryListener>> listeners;
     private String name;
     private static JCConnection connection;
 
 
 
     public JCMap(String name) throws IOException {
         if(connection==null)
             connection=JCHazelcast.getCon();
         if(listeners==null)
             listeners = new ConcurrentHashMap<String, Set<EntryListener>>();
         this.name       = name;
     }
 
 
 
    public void put(String flag,boolean noreply,Object key,Object data) throws IOException, ClassNotFoundException {
         put(flag, JCProtocol.DEFAULT_TIMETOLIVE, noreply, key, data);
     }
 
    public void put(String flag,long ttl,boolean noreply,Object key,Object data) throws IOException, ClassNotFoundException {
         connection.sendOp("MPUT " + flag + " " + name + " " + ttl + (noreply ? " noreply " : " "), key, data) ;
         System.out.println(connection.readResponse());
     }
 
    public void putTransient(String flag,long ttl,boolean noreply,Object key,Object data) throws IOException, ClassNotFoundException  {
         connection.sendOp("MPUTTRANSIENT "+flag+" "+name+" "+ttl+  (noreply ? " noreply ":" "),key,data) ;
         System.out.println(connection.readResponse());
     }
 
    public void set(String flag,long ttl,boolean noreply,Object key,Object data) throws IOException, ClassNotFoundException {
         connection.sendOp("MSET "+flag+" "+name+" "+ttl+  (noreply ? " noreply ":" "),key,data) ;
         System.out.println(connection.readResponse());
     }
 
    public void tryPut(String flag,long timeout,Object key,Object data) throws IOException, ClassNotFoundException {
         connection.sendOp("MTRYPUT "+flag+" "+name+" "+timeout+ " ",key,data) ;
         System.out.println(connection.readResponse());
     }
 
    public void putAll(String flag,boolean noreply,Object... data) throws IOException, ClassNotFoundException {
         connection.sendOp("MPUTALL "+flag+" "+name+ (noreply ? " noreply ":" "),data) ;
         System.out.println(connection.readResponse());
     }
 
    public void putAndUnlock(String flag,boolean noreply,Object key,Object data) throws IOException, ClassNotFoundException {
         connection.sendOp("MPUTANDUNLOCK " + flag + " " + name  + (noreply ? " noreply " : " "), key, data) ;
         System.out.println(connection.readResponse());
     }
 
    public void tryLockAndGet(String flag,long timeout,Object key) throws IOException, ClassNotFoundException {
         connection.sendOp("MTRYLOCKANDGET "+flag+" "+name+" "+timeout+ " ",key) ;
         System.out.println(connection.readResponse());
     }
 
    public void get(String flag,Object key) throws IOException, ClassNotFoundException {
         connection.sendOp("MGET " + flag + " " + name + " " , key) ;
         System.out.println(connection.readResponse());
     }
 
    public void getAll(String flag,Object... keys) throws IOException, ClassNotFoundException {
         connection.sendOp("MGETALL " + flag + " " + name + " " , keys) ;
         System.out.println(connection.readResponse());
     }
 
    public void remove(String flag,boolean noreply,Object key) throws IOException, ClassNotFoundException {
         connection.sendOp("MREMOVE " + flag + " " + name + (noreply ? " noreply " : " ") , key) ;
         System.out.println(connection.readResponse());
     }
 
     //should return also #1 in response.
    public void getEntry(String flag,Object key) throws IOException, ClassNotFoundException {
         connection.sendOp("MGETENTRY " + flag + " " + name + " " , key) ;
         System.out.println(connection.readResponse());
     }
 
    public void keySet(String flag,String type) throws IOException, ClassNotFoundException {
         connection.sendOp("KEYSET " + flag + " " + type + " "+name  ) ;
         System.out.println(connection.readResponse());
     }
 
    public void lock(String flag,long timeout,Object key) throws IOException, ClassNotFoundException {
         connection.sendOp("MLOCK " + flag + " " + name + " "+timeout+" ",key  ) ;
         System.out.println(connection.readResponse());
     }
 
    public void unlock(String flag,Object key) throws IOException, ClassNotFoundException {
         connection.sendOp("MUNLOCK " + flag + " " + name + " ",key  ) ;
         System.out.println(connection.readResponse());
     }
 
    public void tryLock(String flag,long timeout,Object key) throws IOException, ClassNotFoundException {
         connection.sendOp("MTRYLOCK " + flag + " " + name + " "+timeout+" ",key  ) ;
         System.out.println(connection.readResponse());
     }
 
    public void isKeyLocked(String flag,Object key) throws IOException, ClassNotFoundException {
         connection.sendOp("MISKEYLOCKED " + flag + " " + name + " ",key  ) ;
         System.out.println(connection.readResponse());
     }
 
    public void lockMap(String flag,long timeout) throws IOException, ClassNotFoundException {
         connection.sendOp("MLOCKMAP " + flag + " "+name+" "+timeout ) ;
         System.out.println(connection.readResponse());
     }
 
    public void unlockMap(String flag,long timeout) throws IOException, ClassNotFoundException {
         connection.sendOp("MUNLOCKMAP " + flag + " "+name+" "+timeout ) ;
         System.out.println(connection.readResponse());
     }
 
    public void forceUnlock(String flag,Object key) throws IOException, ClassNotFoundException {
         connection.sendOp("MFORCEUNLOCK " + flag + " " + name + " ",key  ) ;
         System.out.println(connection.readResponse());
     }
 
    public void containsKey(String flag,Object key) throws IOException, ClassNotFoundException {
         connection.sendOp("MCONTAINSKEY " + flag + " " + name + " map ",key  ) ;
         System.out.println(connection.readResponse());
     }
 
    public void containsValue(String flag,Object value) throws IOException, ClassNotFoundException {
         connection.sendOp("MCONTAINSVALUE " + flag + " " + name + " map ",value  ) ;
         System.out.println(connection.readResponse());
     }
 
    public void putIfAbsent(String flag,boolean noreply,Object key,Object data) throws IOException, ClassNotFoundException  {
         putIfAbsent(flag, JCProtocol.DEFAULT_TIMETOLIVE, noreply, key, data);
     }
 
    public void putIfAbsent(String flag,long ttl,boolean noreply,Object key,Object data) throws IOException, ClassNotFoundException  {
         connection.sendOp("MPUTIFABSENT " + flag + " " + name + " " + ttl + (noreply ? " noreply " : " "), key, data) ;
         System.out.println(connection.readResponse());
     }
 
    public void removeIfSame(String flag,boolean noreply,Object key,Object data) throws IOException, ClassNotFoundException  {
         connection.sendOp("MREMOVEIFSAME " + flag + " " + name +  (noreply ? " noreply " : " "), key, data) ;
         System.out.println(connection.readResponse());
     }
 
    public void replaceIfNotNull(String flag,boolean noreply,Object key,Object data) throws IOException, ClassNotFoundException  {
         connection.sendOp("MREPLACEIFNOTNULL " + flag + " " + name +  (noreply ? " noreply " : " "), key, data) ;
         System.out.println(connection.readResponse());
     }
 
    public void replaceIfSame(String flag,boolean noreply,Object key,Object old_data,Object new_data) throws IOException, ClassNotFoundException  {
         connection.sendOp("MREPLACEIFSAME " + flag + " " + name +  (noreply ? " noreply " : " "), key, old_data,new_data) ;
         System.out.println(connection.readResponse());
     }
 
    public void flush(String flag,boolean noreply) throws IOException, ClassNotFoundException {
         connection.sendOp("MFLUSH " + flag + " "+name+ (noreply ? " noreply " : " ") ) ;
         System.out.println(connection.readResponse());
     }
 
    public void evict(String flag,boolean noreply,Object key) throws IOException, ClassNotFoundException {
         connection.sendOp("MEVICT " + flag + " " + name + (noreply ? " noreply " : " "),key  ) ;
         System.out.println(connection.readResponse());
     }
 
     public void addListener(EntryListener listener,boolean inc) throws IOException, ClassNotFoundException , InterruptedException {
 //        connection.sendOp("MADDLISTENER " + flag + " " + name + " "+inc_value+(noreply ? " noreply " : " "),key  ) ;
 //        System.out.println(connection.readResponse());
         JCListener.init();
         if(JCMap.listeners.get(this.name)==null)
             JCMap.listeners.put(this.name,Collections.newSetFromMap(new ConcurrentHashMap<EntryListener, Boolean>()));
         if(JCListener.addMapListener(this.name,inc))
             JCMap.listeners.get(this.name).add(listener) ;
 
     }
 
    public static int listenersSize(){
        int sum=0;
        for(Set s:listeners.values())
            sum+=s.size();
        return  sum;
    }

    public void removeListener(EntryListener listener) throws IOException, ClassNotFoundException, InterruptedException {
         if(JCMap.listeners.get(this.name)!=null) {

            if(JCMap.listeners.get(this.name).size()==1){
                if(JCListener.removeMapListener(this.name))
                   JCMap.listeners.get(this.name).remove(listener);
            } else{
                JCMap.listeners.get(this.name).remove(listener);
            }
         }
        if(JCMap.listenersSize()==0)
             JCListener.stopListening();
 
     }
 
 //    private void mapAddListener(String flag,boolean inc_value,boolean noreply) throws IOException, ClassNotFoundException {
 //        connection.sendOp("MADDLISTENER " + flag + " " + name + " "+inc_value+(noreply ? " noreply " : " ") ) ;
 //        System.out.println(connection.readResponse());
 //    }
 
 
 }
