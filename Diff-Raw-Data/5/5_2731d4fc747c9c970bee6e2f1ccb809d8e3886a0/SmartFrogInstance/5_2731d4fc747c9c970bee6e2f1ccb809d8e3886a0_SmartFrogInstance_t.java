 /*
  * SmartFrogInstance.java
  *
  * Created on 22.10.2007, 11:28:46
  *
  */
 
 package builder.smartfrog;
 
 /**
 * Represents an installation object - name of the SmartFrog environment installation
 * and the path where it's located.
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
