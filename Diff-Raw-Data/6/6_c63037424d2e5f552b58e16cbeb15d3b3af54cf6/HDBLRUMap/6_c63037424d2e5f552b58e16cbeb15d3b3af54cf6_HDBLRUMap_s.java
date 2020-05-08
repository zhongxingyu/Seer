 package com.orbious.util.tokyo;
 
 import java.io.File;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 
 import org.apache.commons.collections15.map.AbstractLinkedMap;
 import org.apache.commons.collections15.map.LRUMap;
 import org.apache.log4j.Logger;
 
 import com.orbious.util.Bytes;
 import com.orbious.util.Loggers;
 
 public class HDBLRUMap<K, V> extends LRUMap<K, V> {
 
   private static final long serialVersionUID = 1L;
 
   private final File filestore;
   private final Class<K> kclazz;
   private final Class<V> vclazz;
   private final boolean readOnly;
 
   private final int tokyoSize;
 
   private HDBStorage hdbs;
   private Logger logger;
 
   public HDBLRUMap(File filestore, Class<K> keyclazz, Class<V> valueclazz,
       int mapSize, int tokyoSize, boolean readOnly) {
     super(mapSize);
 
     this.filestore = filestore;
     this.kclazz = keyclazz;
     this.vclazz = valueclazz;
     this.tokyoSize = tokyoSize;
     this.readOnly = readOnly;
     this.logger = Loggers.logger();
   }
 
   public void load() throws HDBLRUMapException {
     if ( readOnly && !filestore.exists() ) {
       throw new HDBLRUMapException("Cannot open " + filestore.toString() +
           " readOnly, file does not exist?");
     }
 
     hdbs = new HDBStorage(filestore, tokyoSize, readOnly);
     try {
       hdbs.open();
     } catch ( StorageException se ) {
       throw new HDBLRUMapException("Failed to initialise HDB", se);
     }
   }
 
   @Override
   public void clear() {
     super.clear();
     if ( readOnly ) {
       return;
     }
 
     hdbs.clear();
   }
 
   public void close() throws HDBLRUMapException {
     if ( hdbs == null ) {
       return;
     }
 
     logger.info("Closing " + filestore.toString());
 
     boolean error = false;
     if ( !readOnly ) {
       error = closeWrite();
     }
 
     try {
       hdbs.close();
     } catch ( StorageException se ) {
       error = true;
       logger.fatal("Failed to close " + filestore.toString(), se);
 
     }
 
     if ( error ) {
       throw new HDBLRUMapException("Error closing " + filestore.toString() +
           ", please check the error log for more details.");
     }
   }
 
   private boolean closeWrite() {
     if ( readOnly ) {
       return false;
     }
 
     boolean error;
     error = false;
 
     Object[] keys;
     K key;
     V value;
 
     keys  = this.keySet().toArray();
     logger.info("Writing " + keys.length + " keys in memory");
 
     for ( int i = 0; i < keys.length; i++ ) {
       key = kclazz.cast(keys[i]);
       value = this.get(key);
 
       if ( logger.isDebugEnabled() ) {
         logger.debug("Writing key=" + key.toString() + " to " + filestore.getName());
       }
       write(key, value);
     }
 
     return error;
   }
 
   @Override
   public V get(Object key) {
     if ( this.containsKey(key) ) {
       if ( logger.isDebugEnabled() ) {
         logger.debug("Cache hit for key '" + key.toString() + "' in " +
             filestore.getName());
       }
 
       return super.get(key);
     }
 
     if ( logger.isDebugEnabled() ) {
       logger.debug("Cache miss for key '" + key.toString() + "' in " +
           filestore.getName());
     }
 
     return read( kclazz.cast(key) );
   }
 
   private V read(K key) {
     byte[] bkey;
     byte[] bval;
 
     bkey = Bytes.convert(key, kclazz);
     bval = hdbs.read(bkey);
 
     if ( bval == null ) {
       if ( logger.isDebugEnabled() ) {
         logger.debug("Failed to find value for key '" + key.toString() + "'");
       }
       return null;
     }
 
     V val = null;
     try {
       val = vclazz.cast(Bytes.convert(bval, vclazz));
     } catch ( UnsupportedEncodingException uee ) {
       logger.fatal("Failed to convert value for key '" + key.toString() + "'");
       return null;
     }
 
     super.put(key, val);
     return val;
   }
 
   private void write(K key, V value)  {
     if ( readOnly ) {
       return;
     }
 
     hdbs.write(key, kclazz, value, vclazz);
   }
 
   public ArrayList<K> keys() throws HDBLRUMapException {
     ArrayList<K> keys;
     byte[] bkey;
     K key;
 
     keys = new ArrayList<K>();
     try {
       hdbs.iterinit();
     } catch ( StorageException se ) {
       throw new HDBLRUMapException("Error initializing iterator for key retreival", se);
     }
 
     while ( (bkey = hdbs.iternext()) != null ) {
      key = kclazz.cast(bkey);
       if ( !this.containsKey(key) ) {
         keys.add(key);
       }
     }
 
     // add everything in memory
     Object[] memkeys;
     memkeys = this.keySet().toArray();
     for ( int i = 0; i < memkeys.length; i++ ) {
       keys.add(kclazz.cast(memkeys[i]));
     }
 
     return keys;
   }
 
   @Override
   protected boolean removeLRU(AbstractLinkedMap.LinkEntry<K, V> entry) {
     if ( readOnly ) {
       return true;
     }
 
     if ( logger.isDebugEnabled() ) {
       logger.debug("Committing '" + entry.getKey() + "' with '" + entry.getValue() + "'");
     }
 
     write(entry.getKey(), entry.getValue());
     return true;
   }
 }
