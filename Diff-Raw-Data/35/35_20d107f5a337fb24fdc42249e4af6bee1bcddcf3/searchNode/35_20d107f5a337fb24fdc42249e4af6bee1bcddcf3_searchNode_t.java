 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package relationbrowser2;
 
 
 import java.io.*;
 import java.util.ArrayList;
 /*
  *
  * @author francis
  */
 public class searchNode extends Node{
     static ArrayList<Node> searchNodeList = new ArrayList<Node>();
     public searchNode(String n){
         this.setName(n);
         searchNodeList.add(this);
     }
     public void displayCenter(){
        Node[] related = this.getRelations();
         System.out.println("nodes found:");
         for (int i = 0; i < related.length; i++) {
             System.out.println("   (" + i + ")" + related[i].getName());
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
     public void search(String keyword){
         for(int i = 0; i<nodeList.size();i++){
             if(match(keyword,nodeList.get(i).getName())){
                 this.setNewRelation("oneWay", nodeList.get(i));
             }
         }
     }
     public boolean match(String keyword, String searching){
         keyword = keyword.toLowerCase();
         searching = searching.toLowerCase();
         for(int i = 0; i <= searching.length()-keyword.length(); i++){
             for(int j = 0; j < keyword.length(); j++){
                 if(searching.charAt(i)!=keyword.charAt(j)){
                     break;
                 }
                 if(j == keyword.length()-1){
                     return true;
                 }
                 i++;
             }
         }
         return false;
     }
 }
