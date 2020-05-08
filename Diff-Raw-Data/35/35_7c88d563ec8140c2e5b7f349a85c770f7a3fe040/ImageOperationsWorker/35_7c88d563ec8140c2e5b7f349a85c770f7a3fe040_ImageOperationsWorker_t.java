 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.osteo.ij.plugin;
 
import ij.IJ;
 import ij.ImagePlus;
 import java.lang.reflect.Method;
 import javax.swing.SwingWorker;
import static org.osteo.ij.plugin.Osteoclasts_.OVERLAY_COLOR;
 
 /**
  *
  * @author davidr
  */
 public class ImageOperationsWorker extends SwingWorker<Object, Object> {
 
     private Osteoclasts_ pluginInstance;
     private String methodToInvoke;
 
     public String getMethodToInvoke() {
         return methodToInvoke;
     }
 
     public void setMethodToInvoke(String methodToInvoke) {
         this.methodToInvoke = methodToInvoke;
     }
 
     public ImageOperationsWorker(Osteoclasts_ pluginInstance) {
         super();
         this.pluginInstance = pluginInstance;
     }
 
     @Override
     protected Object doInBackground() throws Exception {
         if (this.methodToInvoke == null) {
             throw new IllegalAccessException("setMethodToInvoke() before to call doInBackground()");
         }
 
         Class osteoClass = Osteoclasts_.class;
         try {
             Method method = osteoClass.getDeclaredMethod(methodToInvoke);
             System.out.println("Running Method " + this.methodToInvoke + "()");
             method.invoke(pluginInstance);
         } catch (SecurityException e) {
             System.out.println(e.getMessage());
         } catch (NoSuchMethodException e) {
             System.out.println("method not found: " + this.methodToInvoke + "()");
         } catch (Exception e) {
             System.out.println(e.getMessage());
         }
         return null;
     }

    @Override
    protected void done() {
        super.done();

        String rgb = Integer.toHexString(OVERLAY_COLOR.getRGB());
        IJ.run("Overlay Options...", "stroke=#" + rgb + " width=2 fill=none apply");
        IJ.run("Labels...", "color=white font=9 show draw");
    }
    
 }
