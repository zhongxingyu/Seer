 package com.xhills.golf_party.meta.sample;
 
//@javax.annotation.Generated(value = { "slim3-gen", "@VERSION@" }, date = "2011-08-18 14:20:53")
 /** */
 public final class HogeMeta extends org.slim3.datastore.ModelMeta<com.xhills.golf_party.model.sample.Hoge> {
 
     /** */
     public final org.slim3.datastore.ModelRefAttributeMeta<com.xhills.golf_party.model.sample.Hoge, org.slim3.datastore.ModelRef<com.xhills.golf_party.model.sample.Foo>, com.xhills.golf_party.model.sample.Foo> fooRef = new org.slim3.datastore.ModelRefAttributeMeta<com.xhills.golf_party.model.sample.Hoge, org.slim3.datastore.ModelRef<com.xhills.golf_party.model.sample.Foo>, com.xhills.golf_party.model.sample.Foo>(this, "fooRef", "fooRef", org.slim3.datastore.ModelRef.class, com.xhills.golf_party.model.sample.Foo.class);
 
     /** */
     public final org.slim3.datastore.CoreAttributeMeta<com.xhills.golf_party.model.sample.Hoge, com.google.appengine.api.datastore.Key> key = new org.slim3.datastore.CoreAttributeMeta<com.xhills.golf_party.model.sample.Hoge, com.google.appengine.api.datastore.Key>(this, "__key__", "key", com.google.appengine.api.datastore.Key.class);
 
     /** */
     public final org.slim3.datastore.StringAttributeMeta<com.xhills.golf_party.model.sample.Hoge> text = new org.slim3.datastore.StringAttributeMeta<com.xhills.golf_party.model.sample.Hoge>(this, "text", "text");
 
     /** */
     public final org.slim3.datastore.CoreAttributeMeta<com.xhills.golf_party.model.sample.Hoge, java.lang.Long> version = new org.slim3.datastore.CoreAttributeMeta<com.xhills.golf_party.model.sample.Hoge, java.lang.Long>(this, "version", "version", java.lang.Long.class);
 
     private static final HogeMeta slim3_singleton = new HogeMeta();
 
     /**
      * @return the singleton
      */
     public static HogeMeta get() {
        return slim3_singleton;
     }
 
     /** */
     public HogeMeta() {
         super("Hoge", com.xhills.golf_party.model.sample.Hoge.class);
     }
 
     @Override
     public com.xhills.golf_party.model.sample.Hoge entityToModel(com.google.appengine.api.datastore.Entity entity) {
         com.xhills.golf_party.model.sample.Hoge model = new com.xhills.golf_party.model.sample.Hoge();
         if (model.getFooRef() == null) {
             throw new NullPointerException("The property(fooRef) is null.");
         }
         model.getFooRef().setKey((com.google.appengine.api.datastore.Key) entity.getProperty("fooRef"));
         model.setKey(entity.getKey());
         model.setText((java.lang.String) entity.getProperty("text"));
         model.setVersion((java.lang.Long) entity.getProperty("version"));
         return model;
     }
 
     @Override
     public com.google.appengine.api.datastore.Entity modelToEntity(java.lang.Object model) {
         com.xhills.golf_party.model.sample.Hoge m = (com.xhills.golf_party.model.sample.Hoge) model;
         com.google.appengine.api.datastore.Entity entity = null;
         if (m.getKey() != null) {
             entity = new com.google.appengine.api.datastore.Entity(m.getKey());
         } else {
             entity = new com.google.appengine.api.datastore.Entity(kind);
         }
         if (m.getFooRef() == null) {
             throw new NullPointerException("The property(fooRef) must not be null.");
         }
         entity.setProperty("fooRef", m.getFooRef().getKey());
         entity.setProperty("text", m.getText());
         entity.setProperty("version", m.getVersion());
         entity.setProperty("slim3.schemaVersion", 1);
         return entity;
     }
 
     @Override
     protected com.google.appengine.api.datastore.Key getKey(Object model) {
         com.xhills.golf_party.model.sample.Hoge m = (com.xhills.golf_party.model.sample.Hoge) model;
         return m.getKey();
     }
 
     @Override
     protected void setKey(Object model, com.google.appengine.api.datastore.Key key) {
         validateKey(key);
         com.xhills.golf_party.model.sample.Hoge m = (com.xhills.golf_party.model.sample.Hoge) model;
         m.setKey(key);
     }
 
     @Override
     protected long getVersion(Object model) {
         com.xhills.golf_party.model.sample.Hoge m = (com.xhills.golf_party.model.sample.Hoge) model;
         return m.getVersion() != null ? m.getVersion().longValue() : 0L;
     }
 
     @Override
     protected void assignKeyToModelRefIfNecessary(com.google.appengine.api.datastore.AsyncDatastoreService ds, java.lang.Object model) {
         com.xhills.golf_party.model.sample.Hoge m = (com.xhills.golf_party.model.sample.Hoge) model;
         if (m.getFooRef() == null) {
             throw new NullPointerException("The property(fooRef) must not be null.");
         }
         m.getFooRef().assignKeyIfNecessary(ds);
     }
 
     @Override
     protected void incrementVersion(Object model) {
         com.xhills.golf_party.model.sample.Hoge m = (com.xhills.golf_party.model.sample.Hoge) model;
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
         com.xhills.golf_party.model.sample.Hoge m = (com.xhills.golf_party.model.sample.Hoge) model;
         writer.beginObject();
         org.slim3.datastore.json.Default encoder0 = new org.slim3.datastore.json.Default();
         if(m.getFooRef() != null && m.getFooRef().getKey() != null){
             writer.setNextPropertyName("fooRef");
             encoder0.encode(writer, m.getFooRef(), maxDepth, currentDepth);
         }
         if(m.getKey() != null){
             writer.setNextPropertyName("key");
             encoder0.encode(writer, m.getKey());
         }
         if(m.getText() != null){
             writer.setNextPropertyName("text");
             encoder0.encode(writer, m.getText());
         }
         if(m.getVersion() != null){
             writer.setNextPropertyName("version");
             encoder0.encode(writer, m.getVersion());
         }
         writer.endObject();
     }
 
     @Override
     protected com.xhills.golf_party.model.sample.Hoge jsonToModel(org.slim3.datastore.json.JsonRootReader rootReader, int maxDepth, int currentDepth) {
         com.xhills.golf_party.model.sample.Hoge m = new com.xhills.golf_party.model.sample.Hoge();
         org.slim3.datastore.json.JsonReader reader = null;
         org.slim3.datastore.json.Default decoder0 = new org.slim3.datastore.json.Default();
         reader = rootReader.newObjectReader("fooRef");
         decoder0.decode(reader, m.getFooRef(), maxDepth, currentDepth);
         reader = rootReader.newObjectReader("key");
         m.setKey(decoder0.decode(reader, m.getKey()));
         reader = rootReader.newObjectReader("text");
         m.setText(decoder0.decode(reader, m.getText()));
         reader = rootReader.newObjectReader("version");
         m.setVersion(decoder0.decode(reader, m.getVersion()));
         return m;
     }
 }
