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
 
 
 package cxa.components.conduits;
 
 import cxa.CxA;
 import cxa.components.StopSignalException;
 import cxa.components.kernel.Kernel;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 /**
  * Basic conduit implementation. Stores the data received from the sending kernel
  * in a queue.
  */
 public class BasicConduit<E> implements Conduit<E,E> {
 
     private final LinkedBlockingQueue<E> queue = new LinkedBlockingQueue<E>();
     private final AtomicBoolean stopped = new AtomicBoolean(false);
     private String ID;
     private CxA cxa;
     private Kernel sender;
     private Kernel receiver;
     
     public void initialize(String id, CxA cxa) {
         this.ID = id;
         this.cxa = cxa;
     }
 
     public void send(E data) {
         queue.offer(data);
         CxA.logger().finest( "Conduit " + ID + ": data sent.");
     }
 
     public E receive() throws StopSignalException {
         int t=0;
         while (true) { //TODO: avoid busy waiting...
             t++;
             E e = queue.poll();
             if (e != null) {
                 CxA.logger().finest( "Conduit " + ID + ": data to be received.");
                 return e;
             } else {
                 if (stopped.get()) {
                     CxA.logger().finest( "Conduit " + ID + ": sending stop signal.");
                     throw new StopSignalException();
                 }
             }
         }
     }
 
 
     public synchronized void unregister(Kernel k) {
        CxA.logger().config("Conduit " + ID + ": UnRegistering kernel " + k.ID() );
        if( k == sender ) {
            sender = null;
        } else if( k == receiver ) {
            receiver = null;
        } else {
           throw new IllegalStateException( "Kernel " + k.ID() + " is not currently registered in conduit " + k.ID() );
        }
        if( sender == null && receiver == null ) {
            queue.clear();
        }
     }
 
     public String ID() {
         return ID;
     }
 
     public void stop() {
         CxA.logger().info("Conduit " + ID + ": Received stop signal." );
         stopped.set(true);
     }
 
     @Override
     public Kernel getSenderKernel() {
         return sender;
     }
 
     @Override
     public Kernel getReceiverKernel() {
         return receiver;
     }
 
     @Override
     public void registerSender( Kernel k ) {
         CxA.logger().config("Conduit " + ID + ": registering sender kernel " + k.ID() );
         if( sender == null) {
             sender = k;
         } else {
             throw new IllegalStateException("The sender kernel is already registered in conduit " + ID );
         }
     }
 
     @Override
     public void registerReceiver( Kernel k ) {
         CxA.logger().config("Conduit " + ID + ": registering receiver kernel " + k.ID() );
         if( receiver == null) {
             receiver = k;
         } else {
             throw new IllegalStateException("The receiver kernel is already registered in conduit " + ID );
         }
     }
 
 
 }
