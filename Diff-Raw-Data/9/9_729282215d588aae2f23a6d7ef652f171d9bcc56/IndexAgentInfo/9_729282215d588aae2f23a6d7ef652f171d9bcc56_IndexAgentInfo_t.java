package ru.guardz.docmon;
 
 public class IndexAgentInfo {
     private String m_index_name;
     private String m_instance_name;
 
     public String get_index_name() {
         return m_index_name;
     }
 
     public String get_instance_name() {
         return m_instance_name;
     }
 
     public IndexAgentInfo(String index_name, String instance_name) {
         this.m_index_name = index_name;
         this.m_instance_name = instance_name;
     }
 
 }
