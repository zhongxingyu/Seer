 package cliente;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 
 import datos.Producto;
 import tienda.Agente;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.Hashtable;
 import java.util.Vector;
 //import datos.*;
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 //import tienda.*;
 
 /**
  *
  * @author Rachid
  */
 public class Cliente implements ClienteInterface {
 
     Hashtable<String, Producto> productos;
     Agente tienda;
     Registry registry;
     String nombre;
 
     /**
      * @param args the command line arguments
      */
    public void main(String[] args) {
         // TODO code application logic here
         try{
            registry = LocateRegistry.getRegistry();
            tienda=(Agente)registry.lookup("Agente");
             //el codigo del dr es:
             //String response = stub.sayHello();
 	    //System.out.println("response: " + response);
             //falta agregar la interfaz aqui
             
             ClienteApplet applet = new ClienteApplet();
             applet.setVisible(true);
             
         }catch (Exception e){
             System.err.println("Client exception: " + e.toString());
 	    e.printStackTrace();
         }
     }
     /*
     public void encontrarTienda() {
         try {
             Registry registry = LocateRegistry.getRegistry();
             tienda = (Agente) registry.lookup("Agente");
         } catch (Exception ex) {
         }
     }*/
 
     @Override
     public boolean mandarPrecioNuevo(String producto, float nuevoPrecio) throws RemoteException {
         if (productos.containsKey(producto)) {
 
             Producto infoProd;
             infoProd = (Producto) productos.get(producto);
 
             if (infoProd.actualizaPrecio(nuevoPrecio)) {
 
                 return true;
 
             } else {
                 return false;
             }
         } else {
             return false;
         }
     }
     
     public void setNombre(String nombre){
         
         this.nombre = nombre;
     }
 
     public boolean RegistrarUsuario(String str) {
         try {
             if (tienda.registraUsuario(str)) {
                 return true;
             } else {
                 return false;
             }
         } catch (RemoteException e) {
             return false;
         }
     }
 
     @Override
     public boolean mandarProductoNuevo(Producto producto) throws RemoteException {
         if (!productos.containsKey(producto)) {
 
             productos.put(producto.getNombreProducto(),producto);
 
             return true;
         } else {
             return false;
         }
     }
 }
