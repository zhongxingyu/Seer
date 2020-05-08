 package assignmentImplementation;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Scanner;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import keyValueBaseExceptions.BeginGreaterThanEndException;
 import keyValueBaseExceptions.KeyAlreadyPresentException;
 import keyValueBaseExceptions.KeyNotFoundException;
 import keyValueBaseExceptions.ServiceAlreadyInitializedException;
 import keyValueBaseExceptions.ServiceInitializingException;
 import keyValueBaseExceptions.ServiceNotInitializedException;
 import keyValueBaseInterfaces.KeyValueBase;
 import keyValueBaseInterfaces.Pair;
 import keyValueBaseInterfaces.Predicate;
 
 public class KeyValueBaseImpl implements KeyValueBase<KeyImpl, ValueListImpl> {
     private boolean initialized;
     private IndexImpl index;
     
     private HashMap<KeyImpl, ReentrantReadWriteLock> lockTable;
 
     public KeyValueBaseImpl(IndexImpl index) {
         initialized = false;
         this.index = index;
         
         lockTable = new HashMap<>();
     }
 
     @Override
     public void init(String serverFilename)
             throws ServiceAlreadyInitializedException,
             ServiceInitializingException {
         if (initialized) {
             throw new ServiceAlreadyInitializedException();
         }
 
         Scanner s = null;
         BufferedReader b = null;
 
         try {
             b = new BufferedReader(new FileReader(serverFilename));
             String str;
             Integer prevKey = null;
             ValueListImpl vl = new ValueListImpl();
 
             while ((str = b.readLine()) != null) {
                 s = new Scanner(str);
                 Integer key = s.nextInt();
                 if (prevKey != null && !prevKey.equals(key)) {
                     KeyImpl k = new KeyImpl();
                     k.setKey(prevKey);
                     index.insert(k, vl);
                     vl = new ValueListImpl();
                 }
                 while (s.hasNextInt()) {
                     Integer x = s.nextInt();
                     ValueImpl v = new ValueImpl();
                     v.setValue(x);
                     vl.add(v);
                 }
                 s.close();
                 prevKey = key;
             }
             if (prevKey != null) {
                 KeyImpl k = new KeyImpl();
                 k.setKey(prevKey);
                 index.insert(k, vl);
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             if (s != null) {
                 s.close();
             }
             if (b != null) {
                 try {
                     b.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
 
         initialized = true;
     }
 
     @Override
     public ValueListImpl read(KeyImpl k) throws KeyNotFoundException,
             IOException, ServiceNotInitializedException {
         if (!initialized) {
             throw new ServiceNotInitializedException();
         }
 
         lockRead(k);
         try {
             return index.get(k);
         } finally {
             unlock(k);
         }
     }
 
     @Override
     public void insert(KeyImpl k, ValueListImpl v)
             throws KeyAlreadyPresentException, IOException,
             ServiceNotInitializedException {
         if (!initialized) {
             throw new ServiceNotInitializedException();
         }
 
         lockWrite(k);
         try {
             index.insert(k, v);
         } finally {
             unlock(k);
         }
     }
 
     @Override
     public void update(KeyImpl k, ValueListImpl newV)
             throws KeyNotFoundException, IOException,
             ServiceNotInitializedException {
         if (!initialized) {
             throw new ServiceNotInitializedException();
         }
 
         lockWrite(k);
         try {
             index.update(k, newV);
         } finally {
             unlock(k);
         }
     }
 
     @Override
     public void delete(KeyImpl k) throws KeyNotFoundException,
             ServiceNotInitializedException {
         if (!initialized) {
             throw new ServiceNotInitializedException();
         }
 
         lockWrite(k);
         try {
             index.remove(k);
         } finally {
             unlock(k);
         }
     }
 
     @Override
     public List<ValueListImpl> scan(KeyImpl begin, KeyImpl end,
             Predicate<ValueListImpl> p) throws IOException,
             BeginGreaterThanEndException, ServiceNotInitializedException {
         if (!initialized) {
             throw new ServiceNotInitializedException();
         }
 
         ArrayList<ValueListImpl> list = new ArrayList<ValueListImpl>();
         for (ValueListImpl v : index.scan(begin, end)) {
             if (p.evaluate(v)) {
                 System.out.println("predicate matched");
                 list.add(v);
             }
             else
                 System.out.println("predicate failed");
         }
         return list;
     }
 
     @Override
     synchronized public List<ValueListImpl> atomicScan(KeyImpl begin, KeyImpl end,
             Predicate<ValueListImpl> p) throws IOException,
             BeginGreaterThanEndException, ServiceNotInitializedException {
         if (!initialized) {
             throw new ServiceNotInitializedException();
         }
         
         List<KeyImpl> ks = KeyImpl.getInterval(begin, end);
         
         while (!lockReadMany(ks)) {
             try {
                 wait();
             } catch (InterruptedException e) {
                 // Doesn't matter...
             }
         }
         try {
             return scan(begin, end, p);
         } finally {
             unlock(ks);
         }
     }
 
     @Override
     public void bulkPut(List<Pair<KeyImpl, ValueListImpl>> mappings)
             throws IOException, ServiceNotInitializedException {
         if (!initialized) {
             throw new ServiceNotInitializedException();
         }
         
         ArrayList<KeyImpl> ks = new ArrayList<>();
         for (Pair<KeyImpl, ValueListImpl> p : mappings) {
             ks.add(p.getKey());
         }
 
         while (!lockWriteMany(ks)) {
             try {
                 wait();
             } catch (InterruptedException e) {
                 // Doesn't matter...
             }
         }
         try {
             index.bulkPut(mappings);
         } finally {
             unlock(ks);
         }
     }
     
     private void lockRead(KeyImpl k) {
         ReentrantReadWriteLock l = getLock(k);
         l.readLock().lock();
     }
     
     private boolean lockReadMany(List<KeyImpl> ks) {
         ArrayList<KeyImpl> locked = new ArrayList<>();
         for (KeyImpl k : ks) {
             if (!getLock(k).readLock().tryLock()) {
                 unlock(locked);
                 return false;
             } else {
                 locked.add(k);
             }
         }
         return true;
     }
     
     private void lockWrite(KeyImpl k) {
         ReentrantReadWriteLock l = getLock(k);
         l.writeLock().lock();
        synchronized (this) {
            notifyAll();
        }
     }
     
     private boolean lockWriteMany(List<KeyImpl> ks) {
         ArrayList<KeyImpl> locked = new ArrayList<>();
         for (KeyImpl k : ks) {
             if (!getLock(k).writeLock().tryLock()) {
                 unlock(locked);
                 return false;
             } else {
                 locked.add(k);
             }
         }
         return true;
     }
     
     private void unlock(KeyImpl k) {
         ReentrantReadWriteLock l = getLock(k);
         if (l.isWriteLocked()) {
             l.writeLock().unlock();
         } else {
             l.readLock().unlock();
         }
     }
     
     private void unlock(List<KeyImpl> ks) {
         for (KeyImpl k : ks) {
             unlock(k);
         }
     }
     
     synchronized private ReentrantReadWriteLock getLock(KeyImpl k) {
         ReentrantReadWriteLock l;
         l = lockTable.get(k);
         if (l == null) {
             l = new ReentrantReadWriteLock();
             lockTable.put(k, l);
         }
         return l;
     }
 
 }
