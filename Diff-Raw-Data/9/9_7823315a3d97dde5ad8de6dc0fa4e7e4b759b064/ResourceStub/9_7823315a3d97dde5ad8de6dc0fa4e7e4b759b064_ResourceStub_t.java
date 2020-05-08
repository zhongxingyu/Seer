 /*
  *  BlinzCore - core library of audio, video, and other essential classes.
  *  Copyright (C) 2010  BlinzProject <gtalent2@gmail.com>
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License version 3 as
  *  published by the Free Software Foundation.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.blinz.graphics;
 
 import java.util.ArrayList;
 import org.blinz.util.Client;
 
 /**
  * Super-class for resource stub.
  * @author Blinz Project
  */
 abstract class ResourceStub {
 
     private final class ProcessDependents {
 
         private Client client;
         private int dependents = 0;
 
         /**
          * Constructor
          * @param client the Client to add
          * @param usages the number of references the Client currently has the this ServerResourceStub
          */
         private ProcessDependents(final Client client, final int dependents) {
             this.client = client;
             this.dependents = dependents;
         }
 
         /**
          * Constructor
          */
         private ProcessDependents() {
         }
     }
     private int totalDeps = 1;
     private final ArrayList<ProcessDependents> dependents = new ArrayList<ProcessDependents>();
 
     /**
      * Constructor
      */
     ResourceStub() {
     }
 
     /**
      * Gets the number of dependencies of this stub.
      * @return the number of dependencies of this stub
      */
    final int dependents() {
         return totalDeps;
     }
 
     /**
      * Adds a Client for dependent tracking.
      * @param client the Client to add
      * @param usages the number of references the Client currently has the this ServerResourceStub
      */
     final void addClient(final Client client, final int usages) {
         this.dependents.add(new ProcessDependents(client, usages));
     }
 
     /**
      * Increments the number of references the given Client has to this Stub.
      * @param client the Client who's reference count is to be incremented
      */
     final void incrementClient(final Client client) {
         synchronized (dependents) {
             for (int i = 0; i < dependents.size(); i++) {
                 if (dependents.get(i).client == client) {
                     totalDeps++;
                     dependents.remove(i).dependents++;
                 }
             }
         }
     }
 
     /**
      * Decrements the number of references the given Client has to this Stub.
      * @param client the Client who's reference count is to be decremented
      */
     final void decrementClient(final Client client) {
         synchronized (dependents) {
             for (int i = 0; i < dependents.size(); i++) {
                 if (dependents.get(i).client == client) {
                     totalDeps--;
                     dependents.remove(i).dependents--;
                 }
             }
         }
     }
 
     /**
      * Removes the given client as a dependent.
      * @param client the Client to be removed
      */
     final void removeClient(final Client client) {
         synchronized (dependents) {
             for (int i = 0; i < dependents.size(); i++) {
                 if (dependents.get(i).client == client) {
                     totalDeps -= dependents.remove(i).dependents;
                 }
             }
         }
     }
 }
