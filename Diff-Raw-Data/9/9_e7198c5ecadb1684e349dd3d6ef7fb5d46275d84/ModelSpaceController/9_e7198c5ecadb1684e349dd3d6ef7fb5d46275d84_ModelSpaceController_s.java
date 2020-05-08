 package jDistsim.application.designer.controller;
 
 import jDistsim.application.designer.controller.modelSpaceFeature.*;
 import jDistsim.application.designer.model.ModelSpaceModel;
 import jDistsim.application.designer.view.ModelSpaceView;
 import jDistsim.core.simulation.modules.IModuleFactory;
 import jDistsim.core.simulation.modules.Module;
 import jDistsim.core.simulation.modules.ui.ModuleConnectedPointUI;
 import jDistsim.core.simulation.modules.ui.ModuleUI;
 import jDistsim.utils.common.ModelSpaceListener;
 import jDistsim.utils.common.PandaInjector;
 import jDistsim.utils.logging.Logger;
 import jDistsim.utils.pattern.mvc.AbstractController;
 import jDistsim.utils.pattern.mvc.AbstractFrame;
 
 import java.awt.*;
 import java.awt.datatransfer.Transferable;
 import java.awt.dnd.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 24.11.12
  * Time: 15:01
  */
 public class ModelSpaceController extends AbstractController<ModelSpaceModel> implements DropTargetListener {
 
     private ModelSpaceView view;
     private List<ModelSpaceListener> modelSpaceListeners;
 
     public ModelSpaceController(AbstractFrame mainFrame, ModelSpaceModel model) {
         super(mainFrame, model);
         view = getMainFrame().getView(ModelSpaceView.class);
         modelSpaceListeners = new ArrayList<>();
         initialize();
     }
 
     private void initialize() {
         modelSpaceListeners.add(new ModuleConnectorAction());
         modelSpaceListeners.add(new ModuleMovingAction());
         modelSpaceListeners.add(new SelectedActiveModuleAction());
         modelSpaceListeners.add(new ModuleKeysAction());
 
         new DropTarget(view.getContentPane(), this);
         view.getContentPane().addMouseListener(new ModelSpaceMouseAdapter());
         view.getContentPane().addMouseMotionListener(new ModelSpaceMouseMotionAdapter());
     }
 
     public void unselectedActiveModule() {
         if (getModel().getCurrentSelectedLine() != null) {
             getModel().getCurrentSelectedLine().setActive(false);
         }
 
         if (getModel().getCurrentActiveModule() != null) {
             getModel().getCurrentActiveModule().setActive(false);
             getModel().getCurrentActiveModule().setDefaultBackgroundColor();
             getModel().setCurrentActiveModule(null);
 
             for (ModelSpaceListener modelSpaceListener : modelSpaceListeners)
                 modelSpaceListener.onModelUnselectedActiveModule(getModel().getCurrentActiveModule(), this);
 
             getModel().notifyObservers();
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
 
         getModel().notifyObservers();
         repaint();
     }
 
     public ModelSpaceView getView() {
         return view;
     }
 
     @Override
     public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
         Transferable transferable = dropTargetDragEvent.getTransferable();
         try {
             IModuleFactory moduleFactory = (IModuleFactory) transferable.getTransferData(transferable.getTransferDataFlavors()[0]);
             Module module = moduleFactory.create();
 
             ModuleUI currentDragModule = new ModuleUI(module);
             currentDragModule.setLocation(ModelSpaceHelper.calculateDragLocation(dropTargetDragEvent.getLocation(), currentDragModule.getSize()));
             getModel().setCurrentDragModule(currentDragModule);
 
             view.getContentPane().add(currentDragModule, 0);
             view.getContentPane().repaint();
         } catch (Exception exception) {
             Logger.log(exception);
         }
     }
 
     @Override
     public void dragOver(DropTargetDragEvent dropTargetDragEvent) {
         ModuleUI currentDragModule = getModel().getCurrentDragModule();
         Point location = ModelSpaceHelper.calculateDragLocation(dropTargetDragEvent.getLocation(), currentDragModule.getSize());
         currentDragModule.setLocation(location);
     }
 
     @Override
     public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent) {
     }
 
     @Override
     public void dragExit(DropTargetEvent dte) {
         view.getContentPane().remove(getModel().getCurrentDragModule());
         view.getContentPane().repaint();
     }
 
     @Override
     public void drop(DropTargetDropEvent dropTargetDropEvent) {
         unselectedActiveModule();
 
         try {
             Transferable transferable = dropTargetDropEvent.getTransferable();
             IModuleFactory moduleFactory = (IModuleFactory) transferable.getTransferData(transferable.getTransferDataFlavors()[0]);
 
             ModuleUI currentDragModule = getModel().getCurrentDragModule();
             currentDragModule.setIdentifier(moduleFactory.createIdentifier());
             currentDragModule.addMouseListener(new ModelSpaceModuleMouseAdapter());
             currentDragModule.addMouseMotionListener(new ModelSpaceModuleMouseMotionAdapter());
 
             getModel().getModuleList().put(currentDragModule.getIdentifier(), currentDragModule);
             getModel().getModuleList().notifyObservers();
             for (ModelSpaceListener listener : modelSpaceListeners)
                 listener.onAddedModule(currentDragModule, this);
 
             new PandaInjector(view.getContentPane(), currentDragModule).activate();
         } catch (Exception exception) {
             Logger.log(exception);
         }
     }
 
     private void repaint() {
         getView().getContentPane().repaint();
     }
 
     public void connect(final ModuleUI moduleA, final ModuleConnectedPointUI modulePointA, final ModuleUI moduleB, final ModuleConnectedPointUI modulePointB) {
         final ModuleConnector moduleConnector;
         try {
             moduleConnector = modulePointA.connect(modulePointB);
         } catch (Exception exception) {
             Logger.log(exception);
             return;
         }
 
         modulePointA.getParent().notifyObservers();
         modulePointB.getParent().notifyObservers();
         getModel().getModuleList().notifyObservers("connect");
         moduleConnector.getConnectorLine().addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent mouseEvent) {
                 unselectedActiveModule();
 
                 if (getModel().getCurrentSelectedLine() != null) {
                     getModel().getCurrentSelectedLine().setActive(false);
                 }
                 getModel().setCurrentSelectedLine(moduleConnector.getConnectorLine());
                 getModel().getCurrentSelectedLine().setActive(true);
                 getModel().getCurrentSelectedLine().requestFocus();
             }
         });
 
 
         moduleConnector.getConnectorLine().addKeyListener(new KeyAdapter() {
             @Override
             public void keyReleased(KeyEvent keyEvent) {
                 if (keyEvent.getKeyCode() == KeyEvent.VK_DELETE) {
                     modulePointA.disconnect(modulePointB);
 
                     view.getContentPane().remove(getModel().getCurrentSelectedLine());
                     view.getContentPane().repaint();
                     view.getContentPane().requestFocus();
 
                     getModel().getModuleList().notifyObservers("moduleList");
                 }
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
