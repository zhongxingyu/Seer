 package com.jonmort.plugin.aoso;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Iterables;
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 @XmlRootElement
 public class StorageRepresentation {
     private String key;
     private String data;
 
     public StorageRepresentation() {
     }
 
    public StorageRepresentation(String key, String data) {
         this.key = key;
         this.data = data;
     }
 
     @XmlAttribute
     public String getKey() {
         return key;
     }
 
     public void setKey(String key) {
         this.key = key;
     }
 
     @XmlElement
     public String getData() {
 
         return data;
     }
 
     public void setData(String data) {
         this.data = data;
     }
 
     public static StorageRepresentation fromStorage(Storage storage) {
         String key = storage.getIdentifier();
         if (key.startsWith("user/")) {
            int nextSlash = key.indexOf("/", 5);
            key = "user" + key.substring(nextSlash);
         }
        return new StorageRepresentation(key, storage.getData());
     }
 
     public static StorageRepresentation[] fromStorage(Iterable<Storage> storage) {
         return Iterables.toArray(Iterables.transform(storage, storageToRepresentation()), StorageRepresentation.class);
     }
 
     public static Function<Storage, StorageRepresentation> storageToRepresentation() {
         return new Function<Storage, StorageRepresentation>() {
             public StorageRepresentation apply(Storage from) {
                 return fromStorage(from);
             }
         };
     }
 }
