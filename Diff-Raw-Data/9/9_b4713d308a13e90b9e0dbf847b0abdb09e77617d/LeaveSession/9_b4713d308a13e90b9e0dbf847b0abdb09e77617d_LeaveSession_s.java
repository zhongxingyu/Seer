 /**
  * Appia: Group communication and protocol composition framework library
  * Copyright 2006 University of Lisbon
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  *
  * Initial developer(s): Alexandre Pinto and Hugo Miranda.
  * Contributor(s): See Appia web page for a list of contributors.
  */
  package org.continuent.appia.protocols.group.leave;
 
 import java.io.PrintStream;
 import java.util.Arrays;
 
 import org.continuent.appia.core.*;
 import org.continuent.appia.core.events.channel.Debug;
 import org.continuent.appia.protocols.group.LocalState;
 import org.continuent.appia.protocols.group.ViewState;
 import org.continuent.appia.protocols.group.intra.PreView;
 import org.continuent.appia.protocols.group.intra.View;
 import org.continuent.appia.protocols.group.intra.ViewChange;
 
 
 
 /**
 * Session that implementes a graceful exit from the group.
  *
  * @author Alexandre Pinto
  * @version 0.1
  * @see org.continuent.appia.protocols.group.leave.LeaveLayer
  */
 public class LeaveSession extends Session {
   
   public LeaveSession(Layer layer) {
     super(layer);
   }
   
   /**
    * Event handler.
    *
    * @see org.continuent.appia.core.Session#handle
    */
   public void handle(Event event) {
     
     // View
     if (event instanceof View) {
       handleView((View)event);
       return;
     }
     
     // PreView
     if (event instanceof PreView) {
       handlePreView((PreView)event);
       return;
     }
     
     // LeaveEvent
     if (event instanceof LeaveEvent) {
       handleLeaveEvent((LeaveEvent)event);
       return;
     }
     
     // ExitEvent
     if (event instanceof ExitEvent) {
       handleExitEvent((ExitEvent)event);
       return;
     }
     
     // Debug
     if (event instanceof Debug) {
       Debug ev=(Debug)event;
       
       if (ev.getQualifierMode() == EventQualifier.ON) {
         if (ev.getOutput() instanceof PrintStream)
           debug=(PrintStream)ev.getOutput();
         else
           debug=new PrintStream(ev.getOutput());
       } else {
         if (ev.getQualifierMode() == EventQualifier.OFF)
           debug=null;
       }
       
       try { ev.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
       return;
     }
     
     debug("Unwanted event (\""+event.getClass().getName()+"\") received. Continued...");
     try { event.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
     
   }
   
   private boolean[] to_leave=new boolean[0];
   private boolean sent_viewchange;
   private boolean sent_preview;
   private ViewState vs;
   private LocalState ls;
   
   private void handleView(View ev) {
     vs=ev.vs;
     ls=ev.ls;
     
     if (to_leave.length != vs.view.length)
       to_leave=new boolean[vs.view.length];
     Arrays.fill(to_leave,false);
     
     sent_viewchange=false;
     sent_preview=false;
     
     try { ev.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
   }
   
   private void handlePreView(PreView ev) {
     int i,j;
     boolean remove=false;
     boolean[] leaving=new boolean[ev.vs.view.length];
     Arrays.fill(leaving,false);
     
     j=0;
     for (i=0 ; i < to_leave.length ; i++) {
       if (to_leave[i]) {
         if ((j=ev.vs.getRank(vs.view[i])) >= 0) {
           leaving[j]=true;
           remove=true;
         }
       }
     }
     
     if (remove) {
       ev.vs.remove(leaving);
       sendExits(ev.getChannel());
     }
     
     try { ev.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
     
     sent_preview=true;
   }
   
   private void handleLeaveEvent(LeaveEvent ev) {
     if (sent_preview)
       return;
     
     if (ev.getDir() == Direction.UP) {
       to_leave[ev.orig]=true;
     } else {
       to_leave[ls.my_rank]=true;
     }
     
     try { ev.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
     
     if (ls.am_coord)
       sendViewChange(ev.getChannel());
   }
   
   private void handleExitEvent(ExitEvent ev) {
     if ((ev.getMessage().popInt() != vs.group.hashCode()) ||
         (ev.getMessage().popInt() != vs.id.hashCode()) ) {
       debug("Exit discarded due to bad Group and/or ViewID");
       return;
     }
     
     if (!to_leave[ls.my_rank]) {
       debug("Exit discarded because i didn't request to leave.");
       return;
     }
     
     ev.group=vs.group;
     ev.view_id=vs.id;
     try { ev.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
     
     //ev.getChannel().end();
   }
   
   private void sendExits(Channel channel) {
     int i;
     
     for (i=0 ; i < to_leave.length ; i++) {
       if (to_leave[i]) {
         try {
           if (i == ls.my_rank) {
             ExitEvent e=new ExitEvent(channel,Direction.UP,this);
             e.group=vs.group;
             e.view_id=vs.id;
             e.go();
             //channel.end();
           } else {
             ExitEvent e=new ExitEvent(channel,Direction.DOWN,this);
             e.getMessage().pushInt(vs.id.hashCode());
             e.getMessage().pushInt(vs.group.hashCode());
             e.dest=vs.addresses[i];
             e.go();
           }
         } catch (AppiaEventException ex) {
           ex.printStackTrace();
         }
       }
     }
   }
   
   private void sendViewChange(Channel channel) {
     if (!sent_viewchange) {
       try {
         ViewChange e=new ViewChange(channel,Direction.DOWN,this,vs.group,vs.id);
         e.go();
         sent_viewchange=true;
       } catch (AppiaEventException ex) {
         ex.printStackTrace();
       }
     }
   }
   
   // DEBUG
   private PrintStream debug=null;
   
   private void debug(String s) {
     if (debug != null)
       debug.println("appia:group:leave:LeaveSession: "+s);
   }
 }
