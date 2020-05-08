 package cz.cvut.fit.hybljan2.apitestingcg.apimodel;
 
 import com.sun.tools.javac.tree.JCTree;
 
 /**
  * Class represents one parameter of method or constructor.
  */
 public class APIMethodParameter {
 
     /**
      * Name of parameter
      */
     private String name;
 
     /**
      * Full name of class.
      */
     private String type;
 
     public APIMethodParameter(String name, String type) {
         this.name = name;
         this.type = type;
     }
 
     public APIMethodParameter(JCTree.JCVariableDecl jcvd) {
         name = jcvd.name.toString();
         type = jcvd.type.toString();
     }
 
     public String getName() {
         return name;
     }
 
     public String getType() {
         return type;
     }
 }
