 package jDistsim.application.designer.controller.modelSpaceFeature;
 
 import jDistsim.application.designer.controller.ModelSpaceController;
 import jDistsim.application.designer.controller.modelSpaceFeature.util.ConnectorLine;
 import jDistsim.core.modules.ModuleConnectedPointUI;
 import jDistsim.core.modules.ModuleUI;
 import jDistsim.utils.common.ModelSpaceListener;
 import jDistsim.utils.ui.SwingUtil;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 29.12.12
  * Time: 20:14
  */
 public class ModuleConnectorAction extends ModelSpaceListener {
 
     private final int pointSize = 12;
     private boolean connectedMode = false;
 
     private class ConnectPointHelper extends JComponent {
 
         private ModuleConnectedPointUI moduleConnectedPointUI;
         private Color backgroundColor;
         private Color defaultBackgroundColor;
 
 
         private ConnectPointHelper(ModuleConnectedPointUI parent, int size, Point location) {
             this.moduleConnectedPointUI = parent;
             if (parent.getType() == ModuleConnectedPointUI.Type.INPUT) {
                 defaultBackgroundColor = new Color(246, 147, 32);
             } else {
                 defaultBackgroundColor = new Color(188, 246, 47);
             }
             setLocation(location);
             setSize(size, size);
             setDefaultBackgroundColor();
         }
 
         @Override
         public Dimension getPreferredSize() {
             return new Dimension(getSize().width, getSize().height);
         }
 
         public ModuleUI getOwner() {
             return moduleConnectedPointUI.getOwner();
         }
 
         public void setBackgroundColor(Color color) {
             this.backgroundColor = color;
             repaint();
         }
 
         public void setActiveBackgroundColor() {
             setBackgroundColor(new Color(197, 42, 43));
         }
 
         public void setDefaultBackgroundColor() {
             setBackgroundColor(defaultBackgroundColor);
         }
 
         public ModuleConnectedPointUI getModuleConnectedPointUI() {
             return moduleConnectedPointUI;
         }
 
         @Override
         protected void paintComponent(Graphics graphics) {
             super.paintComponent(graphics);
             Graphics2D graphics2D = (Graphics2D) graphics;
             graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             graphics2D.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
             graphics2D.setColor(new Color(62, 109, 0));
             graphics2D.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
             graphics2D.setColor(backgroundColor);
             graphics2D.fillOval(2, 2, getWidth() - 5, getHeight() - 5);
         }
     }
 
     private List<ConnectPointHelper> connectPointHelperList;
     private ModuleUI currentActiveModule;
 
     public ModuleConnectorAction() {
         connectPointHelperList = new ArrayList<>();
         currentActiveModule = null;
     }
 
     @Override
     public void onModelSelectedActiveModule(ModuleUI module, ModelSpaceController modelSpaceController) {
         removeAlCurrentHelperPoints(modelSpaceController);

        currentActiveModule = module;
         showHelperPoints(module, modelSpaceController);
         showPossibleDependecies(modelSpaceController);
     }
 
     @Override
     public void onModelUnselectedActiveModule(ModuleUI module, ModelSpaceController modelSpaceController) {
         removeAlCurrentHelperPoints(modelSpaceController);
     }
 
     @Override
     public void modelSpaceMotionMouseMoved(MouseEvent mouseEvent, ModelSpaceController modelSpaceController) {
         if (!showHelperPoints(mouseEvent))
             removeAlCurrentHelperPoints(modelSpaceController);
     }
 
     private boolean showHelperPoints(MouseEvent mouseEvent) {
         if (currentActiveModule == null || currentActiveModule.contains(mouseEvent.getPoint()))
             return true;
 
         if (currentActiveModule.isActive())
             return true;
 
         for (ConnectPointHelper pointHelper : connectPointHelperList) {
             if (pointHelper.getBounds().contains(mouseEvent.getPoint())) {
                 return true;
             }
         }
         return false;
     }
 
     // Pokud z modulem tahnu - pak body odstranim
     @Override
     public void moduleMotionMouseDragged(MouseEvent mouseEvent, ModelSpaceController modelSpaceController) {
         removeAlCurrentHelperPoints(modelSpaceController);
     }
 
     // Pokud najedu mysi na modul -> zobrazi se pripojny body (pouze pokud neni nejaky modul aktivni)
     @Override
     public void moduleMouseEntered(MouseEvent mouseEvent, ModelSpaceController modelSpaceController) {
         if (connectedMode)
             return;
 
         // pokud je modul aktivni -> body jsou videt trvale a neni potreba je zobrazovat znovu
         if (currentActiveModule != null && currentActiveModule.isActive())
             return;
 
         ModuleUI moduleUI = getModuleUIFromMouseEvent(mouseEvent);
         removeAlCurrentHelperPoints(modelSpaceController);
         showHelperPoints(moduleUI, modelSpaceController);
         currentActiveModule = moduleUI;
     }
 
     // Pokud jsem tahl mysi a pak pustil tlacitko - body se musi opet objevit
     @Override
     public void moduleMouseReleased(MouseEvent mouseEvent, ModelSpaceController modelSpaceController) {
         if (currentActiveModule != null && !currentActiveModule.isActive())
             return;
 
         showHelperPoints((ModuleUI) mouseEvent.getSource(), modelSpaceController);
         showPossibleDependecies(modelSpaceController);
     }
 
     private void showHelperPoints(ModuleUI moduleUI, ModelSpaceController modelSpaceController) {
         for (ModuleConnectedPointUI connectedPointUI : moduleUI.getConnectedPoints()) {
             addConnectedPointHelper(moduleUI, modelSpaceController, connectedPointUI);
         }
     }
 
     private void showPossibleDependecies(ModelSpaceController modelSpaceController) {
         if (!currentActiveModule.getModule().canOutputConnected())
             return;
 
         for (ModuleUI moduleUI : modelSpaceController.getModuleList().values()) {
             if (!moduleUI.getModule().canInputConnected())
                 continue;
 
             if (moduleUI.getIdentifier().equals(currentActiveModule.getIdentifier()))
                 continue;
 
             for (ModuleConnectedPointUI connectedPointUI : moduleUI.getModuleConnectedPointsForConnectWith(currentActiveModule)) {
                 if (connectedPointUI.getType() == ModuleConnectedPointUI.Type.INPUT) {
                     addConnectedPointHelper(moduleUI, modelSpaceController, connectedPointUI);
                 }
             }
         }
     }
 
     private void removeAlCurrentHelperPoints(ModelSpaceController controller) {
         for (ConnectPointHelper pointHelper : connectPointHelperList) {
             controller.getView().getContentPane().remove(pointHelper);
         }
         connectPointHelperList.clear();
     }
 
     private void addConnectedPointHelper(ModuleUI moduleUI, ModelSpaceController modelSpaceController, ModuleConnectedPointUI connectedPointUI) {
         Point location = ModelSpaceHelper.calculatePointPosition(pointSize, connectedPointUI, moduleUI);
         ConnectPointHelper connectPointHelper = new ConnectPointHelper(connectedPointUI, pointSize, location);
 
         int moduleIndex = SwingUtil.getComponentIndex(moduleUI);
         modelSpaceController.getView().getContentPane().add(connectPointHelper, moduleIndex);
         connectPointHelperList.add(connectPointHelper);
 
         if (connectedPointUI.getParent().canBeConnected())
             if (connectedPointUI.getType() == ModuleConnectedPointUI.Type.OUTPUT) {
                 ConnectorDrawerAdapterFactory connectorDrawerAdapterFactory =
                         new ConnectorDrawerAdapterFactory(
                                 connectedPointUI,
                                 ModelSpaceHelper.calculatePointPosition(0, connectedPointUI, moduleUI),
                                 moduleUI,
                                 new ArrayList<>(modelSpaceController.getModuleList().values()),
                                 modelSpaceController);
                 connectPointHelper.addMouseMotionListener(connectorDrawerAdapterFactory.getMouseMotionAdapter());
                 connectPointHelper.addMouseListener(connectorDrawerAdapterFactory.getMouseAdapter());
             }
     }
 
     public class ConnectorDrawerAdapterFactory {
 
         private ConnectorLine connectorLine;
         private Point initialPosition;
         private Point currentPosition;
         private ModuleUI parentModule;
         private ModuleUI currentSelectedModule;
         private ConnectPointHelper currentSelectedConnectPoint;
         private ModuleConnectedPointUI parentConnectedPoint;
         private ModelSpaceController controller;
 
         public ConnectorDrawerAdapterFactory(ModuleConnectedPointUI parentConnectedPoint, Point initialPosition, ModuleUI module, ArrayList<ModuleUI> modules, ModelSpaceController modelSpaceController) {
             this.parentConnectedPoint = parentConnectedPoint;
             this.controller = modelSpaceController;
             this.initialPosition = initialPosition;
             this.parentModule = module;
         }
 
         public MouseMotionAdapter getMouseMotionAdapter() {
             return new MouseMotionAdapter() {
                 @Override
                 public void mouseDragged(MouseEvent mouseEvent) {
                     onMouseDragged(mouseEvent);
                 }
             };
         }
 
         private void onMouseDragged(MouseEvent mouseEvent) {
             currentPosition = mouseEvent.getPoint();
             currentPosition.setLocation(currentPosition.x - pointSize / 2, currentPosition.y - pointSize / 2);
             Point currentCanvasPosition = new Point(initialPosition.x + currentPosition.x, initialPosition.y + currentPosition.y);
             Point connectorLocation = new Point(Math.min(initialPosition.x, currentCanvasPosition.x), Math.min(initialPosition.y, currentCanvasPosition.y));
             Dimension dimension = new Dimension(Math.abs(currentCanvasPosition.x - initialPosition.x), Math.abs(currentCanvasPosition.y - initialPosition.y));
             if (dimension.getSize().height < 2) {
                 dimension = new Dimension(dimension.width, 2);
             }
 
             connectorLine.setSize(dimension);
             connectorLine.setLocation(connectorLocation);
             connectorLine.setPoints(initialPosition, currentCanvasPosition);
 
             if (currentSelectedConnectPoint != null) {
                 currentSelectedConnectPoint.setDefaultBackgroundColor();
                 currentSelectedConnectPoint.getOwner().setActiveBackgroundColor();
                 currentSelectedConnectPoint = null;
             }
 
             for (ConnectPointHelper pointHelper : connectPointHelperList) {
                 if (pointHelper.getOwner().getIdentifier().equals(parentModule.getIdentifier()))
                     continue;
 
                 if (pointHelper.contains(currentCanvasPosition.x - pointHelper.getX(), currentCanvasPosition.y - pointHelper.getY())) {
                     currentSelectedConnectPoint = pointHelper;
                     currentSelectedConnectPoint.setActiveBackgroundColor();
                     break;
                 }
             }
 
             if (currentSelectedModule != null) {
                 currentSelectedModule.setDefaultBackgroundColor();
                 currentSelectedModule = null;
             }
 
             if (currentSelectedConnectPoint != null) {
                 currentSelectedModule = currentSelectedConnectPoint.getOwner();
                 currentSelectedModule.setActiveBackgroundColor();
             } else {
                 ArrayList<ModuleUI> modules = new ArrayList<>(controller.getModuleList().values());
                 for (ModuleUI moduleUI : modules) {
                     if (moduleUI.getIdentifier().equals(parentModule.getIdentifier()))
                         continue;
 
                     if (!moduleUI.getModule().canInputConnected()) {
                         continue;
                     }
 
                     if (moduleUI.contains(currentCanvasPosition.x - moduleUI.getX(), currentCanvasPosition.y - moduleUI.getY())) {
                         currentSelectedModule = moduleUI;
                         currentSelectedModule.setActiveBackgroundColor();
                         break;
                     }
                 }
             }
         }
 
         public MouseAdapter getMouseAdapter() {
             return new MouseAdapter() {
                 @Override
                 public void mousePressed(MouseEvent e) {
                     showPossibleDependecies(controller);
                     connectedMode = true;
                     connectorLine = new ConnectorLine();
                     connectorLine.setDrawingMode(true);
                     connectorLine.setLocation(initialPosition.x, initialPosition.y);
 
                     JComponent contentPane = controller.getView().getContentPane();
                     contentPane.add(connectorLine);
                     contentPane.repaint();
                 }
 
                 @Override
                 public void mouseReleased(MouseEvent e) {
                     removeAlCurrentHelperPoints(controller);
                     controller.unselectedActiveModule();
                     connectedMode = false;
 
                     if (currentSelectedConnectPoint != null) {
                         currentSelectedConnectPoint.setDefaultBackgroundColor();
                         controller.connect(parentModule, parentConnectedPoint, currentSelectedConnectPoint.getOwner(), currentSelectedConnectPoint.getModuleConnectedPointUI());
                     } else if (currentSelectedModule != null) {
                         currentSelectedModule.setDefaultBackgroundColor();
                         List<ModuleConnectedPointUI> points = currentSelectedModule.getInputPoints();
                         for (ModuleConnectedPointUI pointUI : points) {
                             if (pointUI.getParent().canBeConnected()) {
                                 controller.connect(parentModule, parentConnectedPoint, pointUI.getOwner(), pointUI);
                             }
                         }
                     }
 
                     JComponent contentPane = controller.getView().getContentPane();
                     contentPane.remove(connectorLine);
                     contentPane.repaint();
                 }
             };
         }
     }
 }
