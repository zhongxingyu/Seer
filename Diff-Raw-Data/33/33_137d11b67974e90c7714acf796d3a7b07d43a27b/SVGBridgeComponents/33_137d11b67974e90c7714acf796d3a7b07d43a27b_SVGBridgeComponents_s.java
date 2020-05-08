 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package app.navigps.gui.svgComponents;
 
 import app.navigps.gui.StatusChangedListener;
 import java.io.File;
 import java.util.LinkedList;
 
 /**
  *
 * @author vara
  */
 public class SVGBridgeComponents implements StatusChangedListener,
                                     UpdateComponentsWhenChangedDoc{
    
     private boolean rederingStatus = true;
 
     private LinkedList<UpdateComponentsWhenChangedDoc> updateComponets = 
 			new LinkedList<UpdateComponentsWhenChangedDoc>();
 
     private LinkedList<StatusChangedListener> statusChanged =
 			new LinkedList<StatusChangedListener>();
     
     private File svgFile= null;
     
     /**
      *
      */
     public SVGBridgeComponents(){}
     
     /**
      *
      * @return
      */
     public boolean isRendering() {
         return rederingStatus;
     }
     
     /**
      *
      * @param rederingStatus
      */
     protected void setRederingStatus(boolean rederingStatus) {
         this.rederingStatus = rederingStatus;
     }
     
     /**
      *
      * @param path
      */
     public void setSvgFileObject(File path){
         svgFile = path;
     }
     
     /**
      *
      * @return
      */
     public final String getAbsoluteFilePath(){
         if(svgFile != null)
             return svgFile.getName();
         return "";
     }
     
     /**
      *
      * @param l
      */
     public void addUpdateComponentslisteners(UpdateComponentsWhenChangedDoc l){
 	
         if(l!=null)
             updateComponets.add(l);
     }
     
     /**
      *
      * @param l
      */
     public void addStatusChangedlistener(StatusChangedListener l){
         if(l!=null)
              statusChanged.add(l);
     }
 
     /**
      *
      * @param str
      */
     @Override
     synchronized public void currentStatusChanged(String str) {
         for (StatusChangedListener ucomp : statusChanged) {
             ucomp.currentStatusChanged(str);
         }
         //System.out.println(str);
     }
 
     /**
      *
      */
     @Override
     public void documentPrepareToModification() {
         Thread t = new Thread(new Runnable() {
             @Override
             public void run() {
                 for (UpdateComponentsWhenChangedDoc ucomp : updateComponets)
                     ucomp.documentPrepareToModification();
             }
         });
         t.start();
     }
 
     /**
      *
      */
     @Override
     public void documentClosed() {
         Thread t = new Thread(new Runnable() {
             @Override
             public void run() {
                 for (UpdateComponentsWhenChangedDoc ucomp : updateComponets)
                     ucomp.documentClosed();
             }
         });
         t.start();
     }
 }
