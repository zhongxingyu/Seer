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
    private String scope;
 
     public StorageRepresentation() {
     }
 
    public StorageRepresentation(String key, String scope, String data) {
         this.key = key;
        this.scope = scope;
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
        String scope = null;
         if (key.startsWith("user/")) {
            int nextSlash = key.indexOf("/", "user/".length());
            key = key.substring(nextSlash + 1);
            scope = "user";
        } else if (key.startsWith("global/")) {
            key = key.substring("global/".length() + 1);
            scope = "global";
         }
        return new StorageRepresentation(key, scope, storage.getData());
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
