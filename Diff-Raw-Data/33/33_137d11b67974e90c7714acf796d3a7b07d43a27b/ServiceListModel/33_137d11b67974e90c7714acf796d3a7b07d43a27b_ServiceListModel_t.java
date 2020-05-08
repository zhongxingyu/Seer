 package app.navigps.gui.searchServices.swing;
 
 import app.database.odb.core.ServiceCore;
 import java.util.ArrayList;
 import java.util.Vector;
 import javax.swing.AbstractListModel;
 
 /**
  *
  * @author Grzegorz (wara) Warywoda
  */
 public class ServiceListModel extends AbstractListModel{
 
     private ArrayList<ServiceCore> arrayServices = new ArrayList<ServiceCore>();
 
     public ServiceListModel(){
     }
 
     public ServiceListModel(Vector<ServiceCore> vServiceCore){
         setAllServices(vServiceCore);
     }
 
     @Override
     public int getSize() {
         return arrayServices.size();
     }
 
     @Override
     public Object getElementAt(int index) {
         return arrayServices.get(index);
     }
     
     public void addServices(Vector<ServiceCore> vServiceCore){
         int lastIndex = arrayServices.size()-1;
        for (ServiceCore sc : vServiceCore) {
            if(!arrayServices.contains(sc)){
                arrayServices.add(sc);
            }
        }
        int currentIndex = arrayServices.size()-1;
        fireContentsChanged(this, lastIndex,currentIndex);
     }
 
     public void setAllServices(Vector<ServiceCore> vServiceCore){
         arrayServices.clear();
         arrayServices.addAll(vServiceCore);
         fireContentsChanged(this, 0,arrayServices.size()-1);
     }
 
     public void removeElementAt(int index) {
        arrayServices.remove(index);
        fireIntervalRemoved(this, index, index);
     }
 
     public void removeAll(){
         int index = arrayServices.size()-1;
         arrayServices.clear();
         if(index >=0)
             fireIntervalRemoved(this, 0, index);
     }
 
     public boolean isEmpty(){
         return arrayServices.isEmpty();
     }
 
     @Override
     public String toString() {
         return arrayServices.toString();
     }
 
 }
