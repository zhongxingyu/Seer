 package com.weibogrep.indexer;
 
 import java.util.*;
 
 public class IndexItem {
     public long id = -1;
     public String content = "";
     public Date date;
 
     public IndexItem (long v_id, String v_content, Date v_date) {
         id = v_id;
        content = v_content;
         date = v_date;
     }
 }
