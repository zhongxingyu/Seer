 /*  
  * This file is part of CxALite
  *
  *  CxALite is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  CxALite is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
  *
  *  (c) 2009, University of Geneva (Jean-Luc Falcone), jean-luc.falcone@unige.ch
  *
  */
 
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cxa.components.kernel;
 
 import cxa.CxA;
 import cxa.components.PostProcessing;
 import cxa.components.StopSignalException;
 import cxa.components.conduits.Conduit;
 import cxa.components.conduits.ConduitCommon;
 import cxa.components.conduits.ConduitEntrance;
 import cxa.components.conduits.ConduitExit;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author falcone
  */
 public class KernelWrapper implements Runnable {
 
     private Kernel kernel;
     private CxA cxa;
 
     public KernelWrapper(Kernel kernel, CxA cxa) {
         this.cxa = cxa;
         this.kernel = kernel;
     }
 
     public String ID() {
         return kernel.ID();
     }
 
     public Kernel getKernel() {
         return kernel;
     }
 
     public void run() {
         try {
             kernel.mainLoop();
         } catch (StopSignalException ex) {
             propagateStopSignal();
         } finally {
             postProcessing();
             unregisterConduits();
             cxa.stopLatch().countDown();
         }
     }
 
     private void postProcessing() {
         Method[] meths = kernel.getClass().getDeclaredMethods();
         for( Method m: meths ) {
             Annotation[] annos = m.getDeclaredAnnotations();
             for( Annotation a: annos ) {
                 if ( a.annotationType() == PostProcessing.class ) {
                     try {
                         m.setAccessible( true );
                         m.invoke( kernel );
                     } catch ( IllegalAccessException ex ) {
                         Logger.getLogger( KernelWrapper.class.getName() ).log( Level.SEVERE, null, ex );
                     } catch ( IllegalArgumentException ex ) {
                         Logger.getLogger( KernelWrapper.class.getName() ).log( Level.SEVERE, null, ex );
                     } catch ( InvocationTargetException ex ) {
                         Logger.getLogger( KernelWrapper.class.getName() ).log( Level.SEVERE, null, ex );
                     }
                 }
             }
         }
 
     }
 
     private void propagateStopSignal() {
         try {
             Field[] fields = kernel.getClass().getDeclaredFields();
             for (Field f : fields) {
                 f.setAccessible(true);
                 if (f.getType() == ConduitEntrance.class) {
                     ConduitEntrance e = (ConduitEntrance) f.get(kernel);
                     e.stop();
 
                } else if (f.getType() == ConduitEntrance[].class) {
                     ConduitEntrance[] eAry = (ConduitEntrance[]) f.get(kernel);
                     for (ConduitEntrance e : eAry) {
                         e.stop();
                     }
                 }
             }
         } catch (IllegalArgumentException ex) {
             Logger.getLogger(KernelWrapper.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             Logger.getLogger(KernelWrapper.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private void unregisterConduits() {
         try {
             Field[] fields = kernel.getClass().getDeclaredFields();
             for (Field f : fields) {
                 f.setAccessible(true);
                 Class type = f.getType();
                 if (type == ConduitEntrance.class || type == ConduitExit.class) {
                     Conduit c = (Conduit) f.get(kernel);
                     c.unregister(kernel);
                 } else if (type == ConduitEntrance[].class || type == ConduitExit[].class) {
                    Conduit[] ccAry = (Conduit[]) f.get(kernel);
                    for( Conduit c: ccAry ) {
                        c.unregister(kernel);
                    }
                 }
             }
         } catch (IllegalArgumentException ex) {
             Logger.getLogger(KernelWrapper.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             Logger.getLogger(KernelWrapper.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 //
 }
