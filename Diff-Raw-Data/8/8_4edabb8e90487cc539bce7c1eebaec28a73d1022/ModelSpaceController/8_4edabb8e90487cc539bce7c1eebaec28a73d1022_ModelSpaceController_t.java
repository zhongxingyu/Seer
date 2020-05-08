 package jDistsim.application.designer.controller;
 
 import jDistsim.application.designer.controller.modelSpaceFeature.ModuleConnectorAction;
 import jDistsim.application.designer.controller.modelSpaceFeature.ModelSpaceHelper;
 import jDistsim.application.designer.controller.modelSpaceFeature.ModuleMovingAction;
 import jDistsim.application.designer.controller.modelSpaceFeature.SelectedActiveModuleAction;
 import jDistsim.application.designer.controller.modelSpaceFeature.util.ConnectorLine;
 import jDistsim.application.designer.model.ModelSpaceModel;
 import jDistsim.application.designer.view.ModelSpaceView;
 import jDistsim.core.modules.IModuleFactory;
 import jDistsim.core.modules.Module;
 import jDistsim.core.modules.ModuleConnectedPointUI;
 import jDistsim.core.modules.ModuleUI;
 import jDistsim.utils.common.ModelSpaceListener;
 import jDistsim.utils.logging.Logger;
 import jDistsim.utils.pattern.mvc.AbstractController;
 import jDistsim.utils.pattern.mvc.AbstractFrame;
 import jDistsim.utils.pattern.observer.IObserver;
 import jDistsim.utils.pattern.observer.Observable;
 
 import java.awt.*;
 import java.awt.datatransfer.Transferable;
 import java.awt.dnd.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.event.MouseWheelEvent;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 24.11.12
  * Time: 15:01
  */
 public class ModelSpaceController extends AbstractController<ModelSpaceModel> implements DropTargetListener, IObserver {
 
     private ModelSpaceView view;
     private ModuleUI currentDragModule;
     private ConnectorLine currentSelectedLine;
 
     private HashMap<String, ModuleUI> moduleList;
     private List<ModelSpaceListener> modelSpaceListeners;
 
     public ModelSpaceController(AbstractFrame mainFrame, ModelSpaceModel model) {
         super(mainFrame, model);
         view = getMainFrame().getView(ModelSpaceView.class);
         moduleList = new HashMap<>();
         modelSpaceListeners = new ArrayList<>();
         initialize();
     }
 
     private void initialize() {
         getModel().addObserver(this);
 
         modelSpaceListeners.add(new ModuleConnectorAction());
         modelSpaceListeners.add(new ModuleMovingAction());
         modelSpaceListeners.add(new SelectedActiveModuleAction());
 
         new DropTarget(view.getContentPane(), this);
         view.getContentPane().addMouseListener(new ModelSpaceMouseAdapter());
         view.getContentPane().addMouseMotionListener(new ModelSpaceMouseMotionAdapter());
     }
 
     public void unselectedActiveModule() {
         if (currentSelectedLine != null) {
             currentSelectedLine.setActive(false);
         }
 
         if (getModel().getCurrentActiveModule() != null) {
             getModel().getCurrentActiveModule().setActive(false);
             getModel().getCurrentActiveModule().setDefaultBackgroundColor();
            getModel().setCurrentActiveModule(null);
 
             for (ModelSpaceListener modelSpaceListener : modelSpaceListeners)
                 modelSpaceListener.onModelUnselectedActiveModule(getModel().getCurrentActiveModule(), this);
 
             repaint();
         }
     }
 
     public void selectedActiveModule(ModuleUI module) {
         selectedActiveModule(module, true);
     }
 
     public void selectedActiveModule(ModuleUI module, boolean notify) {
         module.setActive(true);
         getModel().setCurrentActiveModule(module);
 
         if (notify)
             for (ModelSpaceListener modelSpaceListener : modelSpaceListeners)
                 modelSpaceListener.onModelSelectedActiveModule(module, this);
 
         repaint();
     }
 
     public ModelSpaceView getView() {
         return view;
     }
 
     public HashMap<String, ModuleUI> getModuleList() {
         return moduleList;
     }
 
     @Override
     public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
         Transferable transferable = dropTargetDragEvent.getTransferable();
         try {
             IModuleFactory moduleFactory = (IModuleFactory) transferable.getTransferData(transferable.getTransferDataFlavors()[0]);
             Module module = moduleFactory.create();
 
             currentDragModule = new ModuleUI(module);
             currentDragModule.setLocation(ModelSpaceHelper.calculateDragLocation(dropTargetDragEvent.getLocation(), currentDragModule.getSize()));
             view.getContentPane().add(currentDragModule, 0);
             view.getContentPane().repaint();
         } catch (Exception exception) {
             Logger.log(exception);
         }
     }
 
     @Override
     public void dragOver(DropTargetDragEvent dropTargetDragEvent) {
         Point location = ModelSpaceHelper.calculateDragLocation(dropTargetDragEvent.getLocation(), currentDragModule.getSize());
         currentDragModule.setLocation(location);
     }
 
     @Override
     public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent) {
     }
 
     @Override
     public void dragExit(DropTargetEvent dte) {
         view.getContentPane().remove(currentDragModule);
         view.getContentPane().repaint();
     }
 
     @Override
     public void drop(DropTargetDropEvent dropTargetDropEvent) {
         unselectedActiveModule();
 
         try {
             Transferable transferable = dropTargetDropEvent.getTransferable();
             IModuleFactory moduleFactory = (IModuleFactory) transferable.getTransferData(transferable.getTransferDataFlavors()[0]);
             currentDragModule.setIdentifier(moduleFactory.createIdentifier());
             currentDragModule.addMouseListener(new ModelSpaceModuleMouseAdapter());
             currentDragModule.addMouseMotionListener(new ModelSpaceModuleMouseMotionAdapter());
             moduleList.put(currentDragModule.getIdentifier(), currentDragModule);
         } catch (Exception exception) {
             Logger.log(exception);
         }
     }
 
     @Override
     public void update(Observable observable, Object arguments) {
     }
 
     private void repaint() {
         getView().getContentPane().repaint();
     }
 
     public void connect(ModuleUI moduleA, ModuleConnectedPointUI modulePointA, ModuleUI moduleB, ModuleConnectedPointUI modulePointB) {
         try {
             modulePointA.getParent().addDependency(moduleB.getModule());
             modulePointB.getParent().addDependency(moduleA.getModule());
         } catch (Exception exception) {
             Logger.log(exception);
             return;
         }
 
         final ModuleConnector moduleConnector = new ModuleConnector(moduleA, modulePointA, moduleB, modulePointB);
         moduleConnector.getConnectorLine().addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent mouseEvent) {
                 unselectedActiveModule();
 
                 if (currentSelectedLine != null) {
                     currentSelectedLine.setActive(false);
                 }
 
                 currentSelectedLine = moduleConnector.getConnectorLine();
                 currentSelectedLine.setActive(true);
                 // view.getContentPane().setComponentZOrder(currentSelectedLine, 0);
             }
         });
         view.getContentPane().add(moduleConnector.getConnectorLine());
         view.getContentPane().repaint();
     }
 
     private class ModelSpaceModuleMouseAdapter extends MouseAdapter {
         @Override
         public void mouseClicked(MouseEvent mouseEvent) {
             for (ModelSpaceListener modelSpaceListener : modelSpaceListeners)
                 modelSpaceListener.moduleClicked(mouseEvent, ModelSpaceController.this);
             repaint();
         }
 
         @Override
         public void mousePressed(MouseEvent mouseEvent) {
             for (ModelSpaceListener modelSpaceListener : modelSpaceListeners)
                 modelSpaceListener.moduleMousePressed(mouseEvent, ModelSpaceController.this);
             repaint();
         }
 
         @Override
         public void mouseReleased(MouseEvent mouseEvent) {
             for (ModelSpaceListener modelSpaceListener : modelSpaceListeners)
                 modelSpaceListener.moduleMouseReleased(mouseEvent, ModelSpaceController.this);
             repaint();
         }
 
         @Override
         public void mouseEntered(MouseEvent mouseEvent) {
             for (ModelSpaceListener modelSpaceListener : modelSpaceListeners)
                 modelSpaceListener.moduleMouseEntered(mouseEvent, ModelSpaceController.this);
             repaint();
         }
 
         @Override
         public void mouseExited(MouseEvent mouseEvent) {
             for (ModelSpaceListener modelSpaceListener : modelSpaceListeners)
                 modelSpaceListener.moduleMouseExited(mouseEvent, ModelSpaceController.this);
             repaint();
         }
 
         @Override
         public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
             for (ModelSpaceListener modelSpaceListener : modelSpaceListeners)
                 modelSpaceListener.moduleMouseWheelMoved(mouseWheelEvent, ModelSpaceController.this);
             repaint();
         }
 
         @Override
         public void mouseDragged(MouseEvent mouseEvent) {
             for (ModelSpaceListener modelSpaceListener : modelSpaceListeners)
                 modelSpaceListener.moduleMouseDragged(mouseEvent, ModelSpaceController.this);
             repaint();
         }
 
         @Override
         public void mouseMoved(MouseEvent mouseEvent) {
             for (ModelSpaceListener modelSpaceListener : modelSpaceListeners)
                 modelSpaceListener.moduleMouseMoved(mouseEvent, ModelSpaceController.this);
             repaint();
         }
     }
 
     private class ModelSpaceMouseAdapter extends MouseAdapter {
         @Override
         public void mouseClicked(MouseEvent mouseEvent) {
             for (ModelSpaceListener modelSpaceListener : modelSpaceListeners)
                 modelSpaceListener.modelSpaceMouseClicked(mouseEvent, ModelSpaceController.this);
             repaint();
         }
 
         @Override
         public void mousePressed(MouseEvent mouseEvent) {
             for (ModelSpaceListener modelSpaceListener : modelSpaceListeners)
                 modelSpaceListener.modelSpaceMousePressed(mouseEvent, ModelSpaceController.this);
             repaint();
         }
 
         @Override
         public void mouseReleased(MouseEvent mouseEvent) {
             for (ModelSpaceListener modelSpaceListener : modelSpaceListeners)
                 modelSpaceListener.modelSpaceMouseReleased(mouseEvent, ModelSpaceController.this);
             repaint();
         }
 
         @Override
         public void mouseEntered(MouseEvent mouseEvent) {
             for (ModelSpaceListener modelSpaceListener : modelSpaceListeners)
                 modelSpaceListener.modelSpaceMouseEntered(mouseEvent, ModelSpaceController.this);
             repaint();
         }
 
         @Override
         public void mouseExited(MouseEvent mouseEvent) {
             for (ModelSpaceListener modelSpaceListener : modelSpaceListeners)
                 modelSpaceListener.modelSpaceMouseExited(mouseEvent, ModelSpaceController.this);
             repaint();
         }
 
         @Override
         public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
             for (ModelSpaceListener modelSpaceListener : modelSpaceListeners)
                 modelSpaceListener.modelSpaceMouseWheelMoved(mouseWheelEvent, ModelSpaceController.this);
             repaint();
         }
 
         @Override
         public void mouseDragged(MouseEvent mouseEvent) {
             for (ModelSpaceListener modelSpaceListener : modelSpaceListeners)
                 modelSpaceListener.modelSpaceMouseDragged(mouseEvent, ModelSpaceController.this);
             repaint();
         }
 
         @Override
         public void mouseMoved(MouseEvent mouseEvent) {
             for (ModelSpaceListener modelSpaceListener : modelSpaceListeners)
                 modelSpaceListener.modelSpaceMouseMoved(mouseEvent, ModelSpaceController.this);
             repaint();
         }
     }
 
     private class ModelSpaceModuleMouseMotionAdapter extends MouseMotionAdapter {
         @Override
         public void mouseDragged(MouseEvent mouseEvent) {
             for (ModelSpaceListener modelSpaceListener : modelSpaceListeners)
                 modelSpaceListener.moduleMotionMouseDragged(mouseEvent, ModelSpaceController.this);
             repaint();
         }
 
         @Override
         public void mouseMoved(MouseEvent mouseEvent) {
             for (ModelSpaceListener modelSpaceListener : modelSpaceListeners)
                 modelSpaceListener.moduleMotionMouseMoved(mouseEvent, ModelSpaceController.this);
             repaint();
         }
     }
 
     private class ModelSpaceMouseMotionAdapter extends MouseMotionAdapter {
         @Override
         public void mouseDragged(MouseEvent mouseEvent) {
             for (ModelSpaceListener modelSpaceListener : modelSpaceListeners)
                 modelSpaceListener.modelSpaceMotionMouseDragged(mouseEvent, ModelSpaceController.this);
             repaint();
         }
 
         @Override
         public void mouseMoved(MouseEvent mouseEvent) {
             for (ModelSpaceListener modelSpaceListener : modelSpaceListeners)
                 modelSpaceListener.modelSpaceMotionMouseMoved(mouseEvent, ModelSpaceController.this);
             repaint();
         }
     }
 }
