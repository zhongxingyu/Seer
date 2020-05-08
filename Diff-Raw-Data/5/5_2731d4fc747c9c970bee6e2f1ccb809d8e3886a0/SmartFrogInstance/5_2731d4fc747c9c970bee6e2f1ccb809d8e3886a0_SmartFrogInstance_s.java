 /*
  * SmartFrogInstance.java
  *
  * Created on 22.10.2007, 11:28:46
  *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
  */
 
 package builder.smartfrog;
 
 /**
  *
  * @author dominik
  */
 public class SmartFrogInstance {
 
    private String name;
    private String path;
 
    public SmartFrogInstance() {       
    }
    
    public SmartFrogInstance(String name, String path) {
       this.name = name;
       this.path = path;
    }
 
    public String getName() {
       return name;
    }
 
    public void setName(String name) {
       this.name = name;
    }
 
    public String getPath() {
       return path;
    }
 
    public void setPath(String path) {
       this.path = path;
    }
 }
