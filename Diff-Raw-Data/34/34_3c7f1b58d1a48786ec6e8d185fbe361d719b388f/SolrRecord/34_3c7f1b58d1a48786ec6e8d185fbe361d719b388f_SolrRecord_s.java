 package org.esgf.solr.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class SolrRecord {
 
     private List<Node> arrNodes;
     private List<Node> strNodes;
     private List<Node> miscNodes;
     
     public SolrRecord() {
         this.arrNodes = new ArrayList<Node>();
         this.strNodes = new ArrayList<Node>();
         this.setMiscNodes(new ArrayList<Node>());
     }
 
     public List<String> getMiscFieldNames() {
         List<String> fieldNames = new ArrayList<String>();
         
         for(int i=0;i<miscNodes.size();i++) {
             for(int j=0;j<miscNodes.get(i).getAttributes().getLength();j++) {
                 Node attrNode = miscNodes.get(i).getAttributes().item(j);
                 if(attrNode.getNodeName().equals("name")){
                     String fieldName = attrNode.getNodeValue();
                     fieldNames.add(fieldName);
                 }
             }
         }
         
         return fieldNames;
     }
     
     
     public List<String> getStrFieldNames() {
         List<String> fieldNames = new ArrayList<String>();
         
         for(int i=0;i<strNodes.size();i++) {
             for(int j=0;j<strNodes.get(i).getAttributes().getLength();j++) {
                 Node attrNode = strNodes.get(i).getAttributes().item(j);
                 if(attrNode.getNodeName().equals("name")){
                     String fieldName = attrNode.getNodeValue();
                     fieldNames.add(fieldName);
                 }
             }
         }
         
         return fieldNames;
     }
     
     public List<String> getArrFieldNames() {
         List<String> fieldNames = new ArrayList<String>();
         
         for(int i=0;i<arrNodes.size();i++) {
             for(int j=0;j<arrNodes.get(i).getAttributes().getLength();j++) {
                 Node attrNode = arrNodes.get(i).getAttributes().item(j);
                 if(attrNode.getNodeName().equals("name")){
                     String fieldName = attrNode.getNodeValue();
                     fieldNames.add(fieldName);
                 }
             }
         }
         
         return fieldNames;
     }
 
     public String getMiscField(String field) {
         String value = null;
         for(int i=0;i<miscNodes.size();i++) {
             //System.out.println("i: " + i + " " + arr_nodes.get(i).getNodeName());
             for(int j=0;j<miscNodes.get(i).getAttributes().getLength();j++) {
                 Node attrNode = miscNodes.get(i).getAttributes().item(j);
                 if(attrNode.getNodeName().equals("name")){
                     //System.out.println("\tj: " + j + " " + attrNode.getNodeValue());
                     if(attrNode.getNodeValue().equals(field)) {
                         NodeList nodeList = miscNodes.get(i).getChildNodes();
                         for(int k=0;k<nodeList.getLength();k++) {
                             Node node = nodeList.item(k);
                             value = node.getTextContent();
                         }
                     }
                 }
             }
         }
         return value;
     }
 
     public String getStrField(String field) {
         String value = null;
         for(int i=0;i<strNodes.size();i++) {
             //System.out.println("i: " + i + " " + arr_nodes.get(i).getNodeName());
             for(int j=0;j<strNodes.get(i).getAttributes().getLength();j++) {
                 Node attrNode = strNodes.get(i).getAttributes().item(j);
                 if(attrNode.getNodeName().equals("name")){
                     //System.out.println("\tj: " + j + " " + attrNode.getNodeValue());
                     if(attrNode.getNodeValue().equals(field)) {
                         NodeList nodeList = strNodes.get(i).getChildNodes();
                         for(int k=0;k<nodeList.getLength();k++) {
                             Node node = nodeList.item(k);
                             value = node.getTextContent();
                         }
                     }
                 }
             }
         }
         return value;
     }
 
     public List<String> getArrField(String field) {
         List<String> values = null;
         
         for(int i=0;i<arrNodes.size();i++) {
             //System.out.println("i: " + i + " " + arr_nodes.get(i).getNodeName());
             for(int j=0;j<arrNodes.get(i).getAttributes().getLength();j++) {
                 Node attrNode = arrNodes.get(i).getAttributes().item(j);
                 if(attrNode.getNodeName().equals("name")){
                     //System.out.println("\tj: " + j + " " + attrNode.getNodeValue());
                     if(attrNode.getNodeValue().equals(field)) {
                         NodeList nodeList = arrNodes.get(i).getChildNodes();
                         values = new ArrayList<String>();
                         for(int k=0;k<nodeList.getLength();k++) {
                             Node node = nodeList.item(k);
                            values.add(node.getTextContent());
                         }
                     }
                 }
             }
         }
         
         return values;
     }
     
     
     
     
     /**
      * @return the arrNodes
      */
     public List<Node> getArrNodes() {
         return arrNodes;
     }
 
     /**
      * @param arrNodes the arrNodes to set
      */
     public void setArrNodes(List<Node> arrNodes) {
         this.arrNodes = arrNodes;
     }
 
     /**
      * @return the strNodes
      */
     public List<Node> getStrNodes() {
         return strNodes;
     }
 
     /**
      * @param strNodes the strNodes to set
      */
     public void setStrNodes(List<Node> strNodes) {
         this.strNodes = strNodes;
     }
 
     /**
      * @return the miscNodes
      */
     public List<Node> getMiscNodes() {
         return miscNodes;
     }
 
     /**
      * @param miscNodes the miscNodes to set
      */
     public void setMiscNodes(List<Node> miscNodes) {
         this.miscNodes = miscNodes;
     }
     
     
 }
