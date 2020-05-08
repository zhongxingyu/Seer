 /*
  * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
  * Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
  * 
  * MONGKIE is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * MONGKIE is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.mongkie.layout.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 import kobic.prefuse.display.DisplayListener;
 import kobic.prefuse.display.NetworkDisplay;
 import org.mongkie.layout.LayoutController;
 import org.mongkie.layout.LayoutModel;
 import org.mongkie.layout.LayoutModelChangeListener;
 import org.mongkie.layout.spi.Layout;
 import org.mongkie.longtask.LongTask;
 import org.mongkie.longtask.progress.Progress;
 import org.mongkie.longtask.progress.ProgressTicket;
 import org.mongkie.visualization.MongkieDisplay;
 import org.mongkie.visualization.VisualizationController;
 import org.mongkie.visualization.workspace.WorkspaceListener;
 import org.openide.util.Lookup;
 import org.openide.util.lookup.ServiceProvider;
 import prefuse.data.Graph;
 
 /**
 *
  * @author Yeongjun Jang <yjjang@kribb.re.kr>
  */
 @ServiceProvider(service = LayoutController.class)
 public class LayoutControllerImpl implements LayoutController, DisplayListener {
 
     private List<LayoutModelChangeListener> listeners;
     private LayoutModelImpl model;
 
     public LayoutControllerImpl() {
         listeners = new ArrayList<LayoutModelChangeListener>();
         Lookup.getDefault().lookup(VisualizationController.class).addWorkspaceListener(new WorkspaceListener() {
             @Override
             public void displaySelected(MongkieDisplay display) {
                 display.addDisplayListener(LayoutControllerImpl.this);
                 LayoutModelImpl old = model;
                 model = display.getLookup().lookup(LayoutModelImpl.class);
                 if (model == null) {
                     model = new LayoutModelImpl(display);
                     display.add(model);
                 } else if (model.getSelectedLayout() != null) {
                     model.loadProperties(model.getSelectedLayout());
                 }
                 fireModelChangeEvent(old, model);
             }
 
             @Override
             public void displayDeselected(MongkieDisplay display) {
                 display.removeDisplayListener(LayoutControllerImpl.this);
                 LayoutModelImpl old = display.getLookup().lookup(LayoutModelImpl.class);
                 if (old.getSelectedLayout() != null) {
                     old.saveProperties(old.getSelectedLayout());
                 }
             }
 
             @Override
             public void displayClosed(MongkieDisplay display) {
                 LayoutModelImpl old = display.getLookup().lookup(LayoutModelImpl.class);
                 if (old != null) {
                     old.getExecutor().cancel();
                 }
             }
 
             @Override
             public void displayClosedAll() {
                 LayoutModelImpl old = model;
                 model = null;
                 fireModelChangeEvent(old, model);
             }
         });
         MongkieDisplay d = Lookup.getDefault().lookup(VisualizationController.class).getDisplay();
         if (d != null) {
             model = d.getLookup().lookup(LayoutModelImpl.class);
             if (model == null) {
                 model = new LayoutModelImpl(d);
                 d.add(model);
             }
         }
     }
 
     @Override
     public void addModelChangeListener(LayoutModelChangeListener l) {
         if (!listeners.contains(l)) {
             listeners.add(l);
         }
     }
 
     @Override
     public void removeModelChangeListener(LayoutModelChangeListener l) {
         listeners.remove(l);
     }
 
     private void fireModelChangeEvent(LayoutModel oldModel, LayoutModel newModel) {
         for (LayoutModelChangeListener l : listeners) {
             l.modelChanged(oldModel, newModel);
         }
     }
 
     @Override
     public LayoutModel getModel() {
         return model;
     }
 
     @Override
     public void setLayout(Layout layout) {
         model.setSelectedLayout(layout);
         if (layout != null) {
             // Inject visualization model when the layout choosed
            layout.setDisplay(model.getDisplay());
         }
     }
 
     @Override
     public void executeLayout() {
         if (model.getSelectedLayout() != null) {
             LayoutRun run = new LayoutRun(model.getSelectedLayout());
             model.getExecutor().execute(run, run);
         }
     }
 
     @Override
     public void stopLayout() {
         model.getExecutor().cancel();
     }
 
     @Override
     public boolean canExecute() {
         return model.getSelectedLayout() != null && !model.isRunning();
     }
 
     @Override
     public boolean canStop() {
         return model.isRunning();
     }
 
     @Override
     public void graphDisposing(NetworkDisplay d, Graph g) {
     }
 
     @Override
     public void graphChanged(NetworkDisplay d, Graph g) {
         if (g.getNodeCount() > 0) {
             fireModelChangeEvent(null, model);
         } else {
             fireModelChangeEvent(model, null);
         }
     }
 
     private static class LayoutRun implements LongTask, Runnable {
 
         private final Layout layout;
         private ProgressTicket progressTicket;
 
         public LayoutRun(Layout layout) {
             this.layout = layout;
         }
 
         @Override
         public void run() {
             Progress.setDisplayName(progressTicket, layout.getBuilder().getName());
             Progress.start(progressTicket);
             try {
                 layout.initAlgo();
                 while (layout.hasNextStep()) {
                     layout.goAlgo();
                 }
                 layout.endAlgo();
             } finally {
                 Progress.finish(progressTicket);
             }
         }
 
         @Override
         public boolean cancel() {
             layout.cancelAlgo();
             return true;
         }
 
         @Override
         public void setProgressTicket(ProgressTicket progressTicket) {
             this.progressTicket = progressTicket;
             if (layout instanceof LongTask) {
                 ((LongTask) layout).setProgressTicket(progressTicket);
             }
         }
     }
 }
