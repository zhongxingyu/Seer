 package com.xhills.golf_party.meta.sample;
 
//@javax.annotation.Generated(value = { "slim3-gen", "@VERSION@" }, date = "2011-08-23 19:52:57")
 /** */
 public final class FooMeta extends org.slim3.datastore.ModelMeta<com.xhills.golf_party.model.sample.Foo> {
 
     /** */
     public final org.slim3.datastore.CoreAttributeMeta<com.xhills.golf_party.model.sample.Foo, java.lang.Long> age = new org.slim3.datastore.CoreAttributeMeta<com.xhills.golf_party.model.sample.Foo, java.lang.Long>(this, "age", "age", long.class);
 
     /** */
     public final org.slim3.datastore.CoreAttributeMeta<com.xhills.golf_party.model.sample.Foo, com.google.appengine.api.datastore.Key> key = new org.slim3.datastore.CoreAttributeMeta<com.xhills.golf_party.model.sample.Foo, com.google.appengine.api.datastore.Key>(this, "__key__", "key", com.google.appengine.api.datastore.Key.class);
 
     /** */
     public final org.slim3.datastore.CoreAttributeMeta<com.xhills.golf_party.model.sample.Foo, java.lang.Long> lastHogeId = new org.slim3.datastore.CoreAttributeMeta<com.xhills.golf_party.model.sample.Foo, java.lang.Long>(this, "lastHogeId", "lastHogeId", long.class);
 
     /** */
     public final org.slim3.datastore.StringAttributeMeta<com.xhills.golf_party.model.sample.Foo> name = new org.slim3.datastore.StringAttributeMeta<com.xhills.golf_party.model.sample.Foo>(this, "name", "name");
 
     /** */
     public final org.slim3.datastore.CoreAttributeMeta<com.xhills.golf_party.model.sample.Foo, java.lang.Long> version = new org.slim3.datastore.CoreAttributeMeta<com.xhills.golf_party.model.sample.Foo, java.lang.Long>(this, "version", "version", java.lang.Long.class);
 
     private static final FooMeta slim3_singleton = new FooMeta();
 
     /**
      * @return the singleton
      */
     public static FooMeta get() {
        return slim3_singleton;
     }
 
     /** */
     public FooMeta() {
         super("Foo", com.xhills.golf_party.model.sample.Foo.class);
     }
 
     @Override
     public com.xhills.golf_party.model.sample.Foo entityToModel(com.google.appengine.api.datastore.Entity entity) {
         com.xhills.golf_party.model.sample.Foo model = new com.xhills.golf_party.model.sample.Foo();
         model.setAge(longToPrimitiveLong((java.lang.Long) entity.getProperty("age")));
         model.setKey(entity.getKey());
         model.setLastHogeId(longToPrimitiveLong((java.lang.Long) entity.getProperty("lastHogeId")));
         model.setName((java.lang.String) entity.getProperty("name"));
         model.setVersion((java.lang.Long) entity.getProperty("version"));
         return model;
     }
 
     @Override
     public com.google.appengine.api.datastore.Entity modelToEntity(java.lang.Object model) {
         com.xhills.golf_party.model.sample.Foo m = (com.xhills.golf_party.model.sample.Foo) model;
         com.google.appengine.api.datastore.Entity entity = null;
         if (m.getKey() != null) {
             entity = new com.google.appengine.api.datastore.Entity(m.getKey());
         } else {
             entity = new com.google.appengine.api.datastore.Entity(kind);
         }
         entity.setProperty("age", m.getAge());
         entity.setProperty("lastHogeId", m.getLastHogeId());
         entity.setProperty("name", m.getName());
         entity.setProperty("version", m.getVersion());
         entity.setProperty("slim3.schemaVersion", 1);
         return entity;
     }
 
     @Override
     protected com.google.appengine.api.datastore.Key getKey(Object model) {
         com.xhills.golf_party.model.sample.Foo m = (com.xhills.golf_party.model.sample.Foo) model;
         return m.getKey();
     }
 
     @Override
     protected void setKey(Object model, com.google.appengine.api.datastore.Key key) {
         validateKey(key);
         com.xhills.golf_party.model.sample.Foo m = (com.xhills.golf_party.model.sample.Foo) model;
         m.setKey(key);
     }
 
     @Override
     protected long getVersion(Object model) {
         com.xhills.golf_party.model.sample.Foo m = (com.xhills.golf_party.model.sample.Foo) model;
         return m.getVersion() != null ? m.getVersion().longValue() : 0L;
     }
 
     @Override
     protected void assignKeyToModelRefIfNecessary(com.google.appengine.api.datastore.AsyncDatastoreService ds, java.lang.Object model) {
     }
 
     @Override
     protected void incrementVersion(Object model) {
         com.xhills.golf_party.model.sample.Foo m = (com.xhills.golf_party.model.sample.Foo) model;
         long version = m.getVersion() != null ? m.getVersion().longValue() : 0L;
         m.setVersion(Long.valueOf(version + 1L));
     }
 
     @Override
     protected void prePut(Object model) {
     }
 
     @Override
     public String getSchemaVersionName() {
         return "slim3.schemaVersion";
     }
 
     @Override
     public String getClassHierarchyListName() {
         return "slim3.classHierarchyList";
     }
 
     @Override
     protected boolean isCipherProperty(String propertyName) {
         return false;
     }
 
     @Override
     protected void modelToJson(org.slim3.datastore.json.JsonWriter writer, java.lang.Object model, int maxDepth, int currentDepth) {
         com.xhills.golf_party.model.sample.Foo m = (com.xhills.golf_party.model.sample.Foo) model;
         writer.beginObject();
         org.slim3.datastore.json.Default encoder0 = new org.slim3.datastore.json.Default();
         writer.setNextPropertyName("age");
         encoder0.encode(writer, m.getAge());
         if(m.getHogeRef() != null){
             writer.setNextPropertyName("hogeRef");
             encoder0.encode(writer, m.getHogeRef());
         }
         if(m.getKey() != null){
             writer.setNextPropertyName("key");
             encoder0.encode(writer, m.getKey());
         }
         writer.setNextPropertyName("lastHogeId");
         encoder0.encode(writer, m.getLastHogeId());
         if(m.getName() != null){
             writer.setNextPropertyName("name");
             encoder0.encode(writer, m.getName());
         }
         if(m.getVersion() != null){
             writer.setNextPropertyName("version");
             encoder0.encode(writer, m.getVersion());
         }
         writer.endObject();
     }
 
     @Override
     protected com.xhills.golf_party.model.sample.Foo jsonToModel(org.slim3.datastore.json.JsonRootReader rootReader, int maxDepth, int currentDepth) {
         com.xhills.golf_party.model.sample.Foo m = new com.xhills.golf_party.model.sample.Foo();
         org.slim3.datastore.json.JsonReader reader = null;
         org.slim3.datastore.json.Default decoder0 = new org.slim3.datastore.json.Default();
         reader = rootReader.newObjectReader("age");
         m.setAge(decoder0.decode(reader, m.getAge()));
         reader = rootReader.newObjectReader("hogeRef");
         reader = rootReader.newObjectReader("key");
         m.setKey(decoder0.decode(reader, m.getKey()));
         reader = rootReader.newObjectReader("lastHogeId");
         m.setLastHogeId(decoder0.decode(reader, m.getLastHogeId()));
         reader = rootReader.newObjectReader("name");
         m.setName(decoder0.decode(reader, m.getName()));
         reader = rootReader.newObjectReader("version");
         m.setVersion(decoder0.decode(reader, m.getVersion()));
         return m;
     }
 }
