 package com.tehbeard.beardstat.dataproviders;
 
 import com.google.gson.annotations.Expose;
 import com.tehbeard.beardstat.containers.documents.DocumentRegistry;
 import com.tehbeard.beardstat.containers.documents.IStatDocument;
 import com.tehbeard.beardstat.containers.documents.StatDocument;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author James
  */
 @StatDocument("memo")
 public class MemoDocument implements IStatDocument {
 
     @Expose
     public List<Memo> memos;
 
     public MemoDocument() {
         memos = new ArrayList<Memo>();
     }
 
     public static class Memo {
 
         @Expose
         private final String from;
         @Expose
         private final String msg;
 
         public Memo(String from, String msg) {
             this.from = from;
             this.msg = msg;
 
         }
 
         public String getFrom() {
             return from;
         }
 
         public String getMsg() {
             return msg;
         }
 
         @Override
         public String toString() {
             return "from: " + from + ", " + msg;
         }
     }
     
     public static void main(String[] args){
         DocumentRegistry.registerDocument(MemoDocument.class);
         MemoDocument doc = new MemoDocument();
         System.out.println(DocumentRegistry.instance().toJson(doc,DocumentRegistry.getSerializeAs(doc.getClass())));
     }
 }
