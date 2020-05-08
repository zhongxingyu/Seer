 package com.cafeform.iumfs.twitterfs;
 
 import com.cafeform.iumfs.utilities.StopWatch;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OptionalDataException;
 import java.io.Serializable;
 import java.lang.ref.SoftReference;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import static java.nio.file.StandardOpenOption.WRITE;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 import static java.util.logging.Level.*;
 import java.util.logging.Logger;
 
 /**
  *
  */
 public class DiskStoredArrayList<T> implements List<T>, Serializable
 {
 
     static final Logger logger = Logger.getLogger(DiskStoredArrayList.class.getName());
     private SoftReference<CopyOnWriteArrayList<T>> reference;
     private final Path backupFile;
     private final int writeDelay;
     private final int maxDelay;
     private static final int DEFAULT_WRITE_DELAY = 5000; // msec
     private static final int DEFAULT_MAX_DELAY = 10000; // msec
     private long delayStart = 0;
     private ScheduledFuture future;
     private StopWatch stopWatch;
     
     public DiskStoredArrayList (
             String pathName, 
             boolean useBackup,
             int writeDelay,
             int maxDelay)
             throws IOException
     {
         if (logger.isLoggable(FINER))
         {
             stopWatch = new StopWatch();                
         }
         this.writeDelay = writeDelay;
         this.maxDelay = maxDelay;
         backupFile = Paths.get(pathName);
         Files.createDirectories(backupFile.getParent());
         CopyOnWriteArrayList<T> arrayList;
 
         if (useBackup && Files.exists(backupFile))
         {
             // Create from backup file.
             arrayList = readFile();
         } else
         {
             // Create initial instance            
             arrayList = resetArrayList();
         }
         reference = new SoftReference<>(arrayList);
         writeFile(arrayList);    
     }
 
     public DiskStoredArrayList (String pathName, boolean useBackup)
             throws IOException
     {
         this(pathName, useBackup, DEFAULT_WRITE_DELAY, DEFAULT_MAX_DELAY);
     }
     
     private CopyOnWriteArrayList<T> resetArrayList () throws IOException 
     {
         Files.deleteIfExists(backupFile);
         Files.createFile(backupFile);
         return new CopyOnWriteArrayList<>();
     }
 
     /**
      * Get instance of CopyOnWriteArrayList.
      */
     private CopyOnWriteArrayList<T> getInstance ()
     {
         CopyOnWriteArrayList<T> arrayList;
 
         if (null == (arrayList = reference.get()))
         {
             if (logger.isLoggable(FINER)) {
                 stopWatch.start();
             }
             arrayList = readFile();
             logger.log(FINER, "Read list from " + backupFile.getFileName());
             if (logger.isLoggable(FINER))      
             {
                 logger.log(FINER, 
                         "Read list took " + stopWatch.stop().toString());
             }
             reference = new SoftReference<>(arrayList);
         }
         return arrayList;
     }
 
     synchronized private CopyOnWriteArrayList<T> readFile ()
     {
         // Insatnce is gone. deserialize from file.
         try (ObjectInputStream inputStream
                 = new ObjectInputStream(Files.newInputStream(backupFile)))
         {
             @SuppressWarnings("unchecked")
             CopyOnWriteArrayList<T> arrayList
                     = (CopyOnWriteArrayList<T>) inputStream.readObject();
             return arrayList;
        } catch (OptionalDataException ex)
         {
             logger.log(WARNING, "Backup list file "
                     + backupFile.getFileName() + " is corrupted. Recreating.");
             try
             {
                 // Create initial instance               
                 return resetArrayList();
             } catch (IOException exi)
             {
                 throw new IllegalStateException("Cannot re-create "
                         + backupFile.getFileName(),
                         exi);
             }
        } catch (IOException | ClassNotFoundException ex)
         {
             throw new IllegalStateException("Cannot deserialize "
                     + backupFile.getFileName() + ".", ex);
         }
     }
 
     synchronized private void deferredWriteFile (final CopyOnWriteArrayList<T> arrayList)
     {
         // Cancel existing schedule if exist.
         cancelIfExist();
 
         if (isExired())
         {
             // Expired max wait time. Write it now.
             writeFile(arrayList);
         } else
         {
             ScheduledExecutorService writeScheduler
                     = Executors.newSingleThreadScheduledExecutor();
 
             future = writeScheduler.schedule(
                     new Runnable()
                     {
                         @Override
                         public void run ()
                         {
                             writeFile(arrayList);
                             clearFuture();
                         }
                     },
                     writeDelay,
                     TimeUnit.MILLISECONDS);
             writeScheduler.shutdown();
         }
     }
     
     /**
      * Clear reference to the ScheduledFuture, so that
      * objects lined from ScheduledFuture can be collected by GC.
      */
     synchronized void clearFuture ()
     {
         future = null;
     }
     
     synchronized private void cancelIfExist ()
     {
         if (null != future && !future.isDone())
         {
             // Still waiting to get fred.
             future.cancel(true);
         }
         else 
         {
             // first time or alrady done.
             delayStart = new Date().getTime();                            
         }
         future = null;        
     }
     
     private boolean isExired ()
     {
         long now = new Date().getTime();
         return (now - delayStart) > maxDelay; 
     }
             
 
     synchronized private void writeFile (CopyOnWriteArrayList<T> arrayList)
     {
         try (ObjectOutputStream outputStream = new ObjectOutputStream(
                 Files.newOutputStream(backupFile, WRITE));)
         {
             if (logger.isLoggable(FINER)) {
                 stopWatch.start();
             }
             
             outputStream.writeObject(arrayList);
 
             logger.log(FINE, "Wrote list to " + backupFile.getFileName());
             if (logger.isLoggable(FINER))      
             {
                 logger.log(FINER, 
                         "Write list took " + stopWatch.stop().toString());
             }
         } catch (IOException ex)
         {
             throw new IllegalStateException("Cannot serialize "
                     + backupFile.getFileName() + ".", ex);
         }
     }
 
     @Override
     public int size ()
     {
         return getInstance().size();
     }
 
     @Override
     public boolean isEmpty ()
     {
         return getInstance().isEmpty();
     }
 
     @Override
     public boolean contains (Object o)
     {
         return getInstance().contains(o);
     }
 
     @Override
     public Iterator<T> iterator ()
     {
         return getInstance().iterator();
     }
 
     @Override
     public Object[] toArray ()
     {
         return getInstance().toArray();
     }
 
     @Override
     public <T> T[] toArray (T[] a)
     {
         return getInstance().toArray(a);
     }
 
     @Override
     public boolean add (T e)
     {
         CopyOnWriteArrayList<T> arrayList = getInstance();
         boolean result = arrayList.add(e);
         deferredWriteFile(arrayList);
         return result;
     }
 
     @Override
     public boolean remove (Object o)
     {
         CopyOnWriteArrayList<T> arrayList = getInstance();
         boolean result = arrayList.remove(o);
         writeFile(arrayList);
         return result;
     }
 
     @Override
     public boolean containsAll (Collection<?> c)
     {
         return getInstance().containsAll(c);
     }
 
     @Override
     public boolean addAll (Collection<? extends T> c)
     {
         CopyOnWriteArrayList<T> arrayList = getInstance();
         boolean result = arrayList.addAll(c);
         writeFile(arrayList);
         return result;
     }
 
     @Override
     public boolean addAll (int index, Collection<? extends T> c)
     {
         CopyOnWriteArrayList<T> arrayList = getInstance();
         boolean result = arrayList.addAll(index, c);
         writeFile(arrayList);
         return result;
     }
 
     @Override
     public boolean removeAll (Collection<?> c)
     {
         CopyOnWriteArrayList<T> arrayList = getInstance();
         boolean result = arrayList.removeAll(c);
         writeFile(arrayList);
         return result;
     }
 
     @Override
     public boolean retainAll (Collection<?> c)
     {
         CopyOnWriteArrayList<T> arrayList = getInstance();
         boolean result = arrayList.retainAll(c);
         writeFile(arrayList);
         return result;
     }
 
     @Override
     public void clear ()
     {
         CopyOnWriteArrayList<T> arrayList = getInstance();
         arrayList.clear();
         writeFile(arrayList);
     }
 
     @Override
     public T get (int index)
     {
         return getInstance().get(index);
     }
 
     @Override
     public T set (int index, T element)
     {
         CopyOnWriteArrayList<T> arrayList = getInstance();
         T result = arrayList.set(index, element);
         writeFile(arrayList);
         return result;
     }
 
     @Override
     public void add (int index, T element)
     {
         CopyOnWriteArrayList<T> arrayList = getInstance();
         arrayList.add(index, element);
         writeFile(arrayList);
     }
 
     @Override
     public T remove (int index)
     {
         CopyOnWriteArrayList<T> arrayList = getInstance();
         T result = arrayList.remove(index);
         writeFile(arrayList);
         return result;
     }
 
     @Override
     public int indexOf (Object o)
     {
         return getInstance().indexOf(o);
     }
 
     @Override
     public int lastIndexOf (Object o)
     {
         return getInstance().lastIndexOf(o);
     }
 
     @Override
     public ListIterator<T> listIterator ()
     {
         return getInstance().listIterator();
     }
 
     @Override
     public ListIterator<T> listIterator (int index)
     {
         return getInstance().listIterator(index);
     }
 
     @Override
     public List<T> subList (int fromIndex, int toIndex)
     {
         return getInstance().subList(fromIndex, toIndex);
     }
 
 }
