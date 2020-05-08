 package com.trendmicro.tme.grapheditor;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlRootElement;
 
 @XmlAccessorType(XmlAccessType.FIELD)
 @XmlRootElement
 public class ExchangeModel {
     public static enum Type {
         QUEUE, TOPIC,
     }
     
     private String name;
     private Type type = Type.QUEUE;
     
     public ExchangeModel(String name) {
         if(name.startsWith("queue:")) {
             this.name = name.substring(6);
         }
         else if(name.startsWith("topic:")) {
             type = Type.TOPIC;
             this.name = name.substring(6);
         }
         else {
             this.name = name;
         }
     }
     
     public String getName() {
         return name;
     }
     
     public Type getType() {
         return type;
     }
     
     public String getFullName() {
         return type.toString().toLowerCase() + ":" + name;
     }
     
     @Override
     public int hashCode() {
         return getFullName().hashCode();
     }
     
     @Override
     public boolean equals(Object obj) {
         if(obj instanceof ExchangeModel) {
             return getFullName().equals(((ExchangeModel) obj).getFullName());
         }
         return false;
     }
 
     public String toSubgraph() {
         StringBuilder sb = new StringBuilder();
         sb.append(String.format("\"%s\" [", getFullName()));
         sb.append(String.format("id=\"input-%s\" ", getFullName()));
         sb.append("shape=record ");
        sb.append("color=red ");
         sb.append(String.format("href=\"javascript:exchange_onclick('%s');\"", getFullName()));
         sb.append("];\n");
         return sb.toString();
     }
 }
