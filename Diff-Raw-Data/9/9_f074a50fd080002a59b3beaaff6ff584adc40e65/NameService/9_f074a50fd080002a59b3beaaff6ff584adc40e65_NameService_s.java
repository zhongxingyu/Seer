 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package midgard.naming;
 
 import java.util.Vector;
 import midgard.componentmodel.IComponent;
 import midgard.services.IService;
 import midgard.services.ProxyService;
 
 /**
  *
  * @author fenrrir
  */
 public class NameService extends ProxyService implements INameService {
     private INameService concreteComponent;
 
    public NameService(INameService concreteComponent) {
        super(concreteComponent);
        this.concreteComponent = concreteComponent;
     }
 
     public IComponent resolveName(String name) {
         return concreteComponent.resolveName(name);
     }
 
     
    
 }
