 /**
  *  C-Nery - A home automation web application for C-Bus.
  *  Copyright (C) 2008,2009  Dave Oxley <dave@daveoxley.co.uk>.
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as
  *  published by the Free Software Foundation, either version 3 of the
  *  License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of the GNU Affero General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package com.daveoxley.cnery.scenes;
 
 import cnerydb.Scene;
 import cnerydb.SceneActivation;
 import com.daveoxley.cbus.CGateException;
 import com.daveoxley.cbus.CGateSession;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.jboss.seam.Component;
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Scope;
 import org.jboss.seam.annotations.Transactional;
 import org.jboss.seam.async.Asynchronous;
 
 /**
  *
  * @author Dave Oxley <dave@daveoxley.co.uk>
  */
 class ProcessStatusChange extends Asynchronous {
 
     private final static Log log = LogFactory.getLog(ProcessStatusChange.class);
 
     public void execute(Object timer, final String status_change) {
         (new ContextualAsynchronousRequest(timer) {
 
             @Override
             protected void process() {
                 ProcessStatusChangeImpl process = (ProcessStatusChangeImpl)Component.getInstance(ProcessStatusChangeImpl.class);
                 process.doProcess(status_change);
             }
         }).run();
     }
 
     @Override
     public void execute(Object arg0) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     protected void handleException(Exception exception, Object timer) {}
 
     @Name("processStatusChange")
     @Scope(ScopeType.APPLICATION)
     public static class ProcessStatusChangeImpl implements Serializable {
 
         private final ProcessSceneActionImpl async = new ProcessSceneActionImpl();
 
         private final HashMap<String,ArrayList<SceneActionRunner>> groupListeners = new HashMap<String,ArrayList<SceneActionRunner>>();
 
         @In
         private Session cneryDatabase;
 
         @Transactional
         public void doProcess(String status_change) {
             String status_array[] = status_change.split(" ");
             /*if (!status_array[0].equals("lighting"))
                 return;*/
 
             final String function = status_array[1];
             String address = status_array[2];
 
             List<SceneActivation> sceneActivations = null;
             Query q = cneryDatabase.createQuery("from SceneActivation where group_address='" + address + "'");
             sceneActivations = (List<SceneActivation>)q.list();
 
             for (final SceneActivation sceneActivation : sceneActivations) {
                 final Scene scene = sceneActivation.getParent();
                 log.debug("Starting thread for scene " + scene.getName());
                 Thread thread = new Thread(new Runnable() {
 
                     @Override
                     public void run() {
                         async.execute(null, function, scene, sceneActivation);
                     }
                 });
                 thread.start();
             }
 
             synchronized (this) {
                 ArrayList<SceneActionRunner> sceneActionRunners = groupListeners.get(address);
                 if (sceneActionRunners != null) {
                     for (SceneActionRunner sceneActionRunner : sceneActionRunners) {
                         try {
                             sceneActionRunner.runAndSchedule(true);
                         } catch (Exception e) {
                             new CGateException(e);
                         }
                     }
                 }
             }
         }
 
        synchronized void registerGroupListener(String groupAddress, SceneActionRunner sceneActionRunner) {
             ArrayList<SceneActionRunner> sceneActionRunners = groupListeners.get(groupAddress);
             if (sceneActionRunners == null) {
                 sceneActionRunners = new ArrayList<SceneActionRunner>();
                 groupListeners.put(groupAddress, sceneActionRunners);
             }
             if (!sceneActionRunners.contains(sceneActionRunner))
                 sceneActionRunners.add(sceneActionRunner);
         }
 
        synchronized void unregisterGroupListener(String groupAddress, SceneActionRunner sceneActionRunner) {
             ArrayList<SceneActionRunner> sceneActionRunners = groupListeners.get(groupAddress);
             if (sceneActionRunners == null)
                 return;
 
             if (sceneActionRunners.contains(sceneActionRunner))
                 sceneActionRunners.remove(sceneActionRunner);
 
             if (sceneActionRunners.isEmpty())
                 groupListeners.remove(groupAddress);
         }
 
         private class ProcessSceneActionImpl extends Asynchronous {
 
             public void execute(Object timer, final String function, final Scene scene, final SceneActivation sceneActivation) {
                 (new ContextualAsynchronousRequest(timer) {
 
                     @Override
                     protected void process() {
                         log.debug("Running thread for scene " + scene.getName());
                         try {
                             SceneRunner sceneRunner = SceneRunnerFactory.getInstance().getSceneRunner(scene);
                             if (function.equals("on") || function.equals("ramp")) {
                                 if (sceneActivation.getGroupOnAction() == 'A')
                                     sceneRunner.activate();
                                 else if (sceneActivation.getGroupOnAction() == 'R')
                                     sceneRunner.reset();
                             }
                             else {
                                 if (sceneActivation.getGroupOffAction() == 'A')
                                     sceneRunner.activate();
                                 else if (sceneActivation.getGroupOffAction() == 'R')
                                     sceneRunner.reset();
                             }
                         }
                         catch (Exception e) {
                             new CGateException(e);
                         }
                     }
                 }).run();
             }
 
             @Override
             public void execute(Object timer) {
                 throw new UnsupportedOperationException("Not supported yet.");
             }
 
             @Override
             protected void handleException(Exception exception, Object timer) {}
         };
     }
 }
