 package relationbrowser2;
 
 import java.io.*;
 import java.util.ArrayList;
 
 public class Node {
     // <editor-fold defaultstate="collapsed" desc="Varables"> 
     // <editor-fold defaultstate="collapsed" desc="Static Varable"> 
     //list of all Nodes
     static ArrayList<Node> nodeList = new ArrayList<Node>();
     //The key for the data in each node
     static ArrayList<String> attriNames = new ArrayList<String>();
     // </editor-fold>
     // <editor-fold defaultstate="collapsed" desc="Class Varables"> 
     private String name = "";
     private ArrayList<Relation> relations = new ArrayList();
     private ArrayList<String> attriDatas = new ArrayList<String>();
     //int numRelated = 0;
    private Frame.Circle circle;
     // </editor-fold>  
     // </editor-fold> 
     // <editor-fold defaultstate="collapsed" desc="Def">
     public Node(){}
     public Node(String n) {
         name = n;
         nodeList.add(this);
     }
     // </editor-fold> 
     // <editor-fold defaultstate="collapsed" desc="Getters"> 
     static Node getNode(int index){
         return Node.nodeList.get(index);
     }
     static Node getNode(String name){
         for(int i = 0; i<nodeList.size();i++){
             if(name.equals(nodeList.get(i).name)){
                 return nodeList.get(i);
             }
         }
         return null;
     }
     public String getName(){
         return name;
     }
    public Frame.Circle getCircle(){
        return circle;
    }
     public String getData(int i){
         return attriDatas.get(i);
     }
     public String getData(String s){
         return attriDatas.get(attriNames.indexOf(s));
     }
    public Relation getRelation(int i){
         return relations.get(i);
     }
     public int numRelations(){
         return relations.size();
     }
    public Node[] getRelations(){
         Node[] relatedNodes = new Node[relations.size()];
         for (int i = 0; i < relations.size(); i++) {
            relatedNodes[i] = getRelation(i).getOtherNode(this);
         }
         return relatedNodes;
     }
     // </editor-fold>
     // <editor-fold defaultstate="collapsed" desc="Setters"> 
     public void setData(String Name, String Data){
         if(! attriNames.contains(Name)){
             //if the name/key isn't made
             attriNames.add(Name);
             resizeData();
             System.out.println("attribute - "+Name+" added");
         }
         attriDatas.add(attriNames.indexOf(Name), Data);
         System.out.println("attribute - "+Name+" set with the data - "+Data);
     }
     public void setNewRelation(String relationType, Node toNode){
         //relationType not use right now
         if (relationType.equals("oneWay")) {
             Relation r = new oneWayRelation(this, toNode);
             return;
         }
         //new normal Relation
         Relation r = new Relation(this, toNode);
     }
     public void setRelation(Relation r){
         relations.add(r);
     }
     public void setName(String n){
         name = n;
     }
     // </editor-fold>  
     // <editor-fold defaultstate="collapsed" desc="Private"> 
     private static void resizeData(){
         for (int i = 0; i < nodeList.size(); i++) {
             Node testNode = nodeList.get(i);
             if(attriNames.size()!=testNode.attriDatas.size()){
                 for (int j = testNode.attriDatas.size(); j < attriNames.size(); j++) {
                     testNode.attriDatas.add(j, null);
                 }
             }
         }
     }
     // </editor-fold>
     // <editor-fold defaultstate="collapsed" desc="Display"> 
     public void displayCenter(){
         System.out.println("\n\n\n");
         System.out.println(this.name);
         for(int i = 0; i < attriDatas.size();i++){
             if(attriDatas.get(i) != null){
                 System.out.println(attriNames.get(i)+":");
                 System.out.println(attriDatas.get(i));
             }
         }
        Node[] related = this.getRelations();
         System.out.println("related nodes:");
         for (int i = 0; i < related.length; i++) {
             System.out.println("   (" + i + ")" + related[i].name);
         }
 
         try {
             BufferedReader bR = new BufferedReader(new InputStreamReader(System.in));
             String[] C = bR.readLine().split(" ");
             
             int n = Integer.parseInt(C[0]);
             related[n].displayCenter();
             bR.close();
         } catch (Exception ex) {
         }
     }
     public void displayRelated() {
        Node[] related = this.getRelations();
         System.out.print(this.name + " is related to ");
         for (int i = 0; i < related.length; i++) {
             if (i == related.length - 1) {
                 if (i != 0) {
                     System.out.print("and ");
                 }
                 System.out.println("\""+related[i].name + "\".");
                 break;
             }
             System.out.print("\""+related[i].name + "\", ");
         }
     }
     // </editor-fold> 
 }
