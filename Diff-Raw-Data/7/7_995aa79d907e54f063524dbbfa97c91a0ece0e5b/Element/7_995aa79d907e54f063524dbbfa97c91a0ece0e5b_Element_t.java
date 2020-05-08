 
 package projecte_prop;
 
 
 public  class Element {
     static private int id_glob;
     private int id;
     private boolean enabled;
    
    
     public Element() {
         id = id_glob;
         ++id_glob;
         enabled = true;     
     }
     
    public Element(boolean newenabled) {
         id = id_glob;
         ++id_glob;
         enabled = newenabled;
     }
    
    public void iniElements(){
        id_glob = 0;
    }
 
     public int getID(){
         return id;     
     }
     
     public boolean isEnabled(){
         return enabled;       
     }
     
     public void modifyEnabled(boolean newvalue){
         enabled = newvalue;
     }
     
     public void modifyid(int newid){
         id = newid;
     }
 }
