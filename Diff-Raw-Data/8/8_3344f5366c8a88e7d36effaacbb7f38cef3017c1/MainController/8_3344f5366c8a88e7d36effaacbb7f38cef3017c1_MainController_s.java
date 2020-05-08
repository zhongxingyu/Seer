 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package hu.elte.graphalgorithms;
 
 import hu.elte.graphalgorithms.algorithms.implementations.BreadthFirstAlgorithm;
 import hu.elte.graphalgorithms.algorithms.implementations.DepthFirstAlgorithm;
 import hu.elte.graphalgorithms.algorithms.implementations.DijkstraAlgorithm;
 import hu.elte.graphalgorithms.algorithms.implementations.KruskalAlgorithm;
 import hu.elte.graphalgorithms.algorithms.interfaces.GraphAlgorithm;
 import hu.elte.graphalgorithms.algorithms.util.ColorableGraphArc;
 import hu.elte.graphalgorithms.algorithms.util.ColorableGraphNode;
 import static hu.elte.graphalgorithms.algorithms.util.ColorableGraphNode.Color.BLACK;
 import static hu.elte.graphalgorithms.algorithms.util.ColorableGraphNode.Color.BLUE;
 import static hu.elte.graphalgorithms.algorithms.util.ColorableGraphNode.Color.GRAY;
 import static hu.elte.graphalgorithms.algorithms.util.ColorableGraphNode.Color.GREEN;
 import static hu.elte.graphalgorithms.algorithms.util.ColorableGraphNode.Color.PURPLE;
 import static hu.elte.graphalgorithms.algorithms.util.ColorableGraphNode.Color.RED;
 import static hu.elte.graphalgorithms.algorithms.util.ColorableGraphNode.Color.WHITE;
 import static hu.elte.graphalgorithms.algorithms.util.ColorableGraphNode.Color.YELLOW;
 import hu.elte.graphalgorithms.core.ExtendedDirectedGraph;
 import hu.elte.graphalgorithms.core.exceptions.ArcAlreadyExistsException;
 import hu.elte.graphalgorithms.core.exceptions.UndirectedGraphRequiredException;
 import hu.elte.graphalgorithms.core.interfaces.Graph;
 import hu.elte.graphalgorithms.exceptions.NotSupportedModeException;
 import java.io.File;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.ResourceBundle;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.application.Platform;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.geometry.Point2D;
 import javafx.scene.control.Button;
 import javafx.scene.control.ComboBox;
 import javafx.scene.control.Label;
 import javafx.scene.control.ListCell;
 import javafx.scene.control.ListView;
 import javafx.scene.control.RadioButton;
 import javafx.scene.control.Slider;
 import javafx.scene.control.TextField;
 import javafx.scene.control.ToggleGroup;
 import javafx.scene.input.KeyEvent;
 import javafx.scene.input.MouseButton;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.layout.AnchorPane;
 import javafx.scene.layout.Pane;
 import javafx.scene.paint.Color;
 import javafx.scene.shape.Circle;
 import javafx.scene.shape.Line;
 import javafx.scene.shape.Polyline;
 import javafx.scene.text.Font;
 import javafx.scene.text.FontWeight;
 import javafx.scene.text.Text;
 import javafx.scene.text.TextAlignment;
 import javafx.stage.FileChooser;
 import javafx.util.Callback;
 import javafx.util.Pair;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import jfx.messagebox.MessageBox;
 
 /**
  *
  * @author nagysan
  */
 public class MainController implements Initializable {
 
     protected static final String ATTACHED_CIRCLE_KEY = "AttachedNode";
     protected static final int ARC_DISTANCE = 8;
     protected static final int KRUSKAL_ALGO_CODE = 0;
     protected static final int DIJKSTRA_ALGO_CODE = 1;
     protected static final int BFS_ALGO_CODE = 2;
     protected static final int DFS_ALGO_CODE = 3;
 
     static Pair<Point2D, Point2D> getPoints(double u, double v, double x, double y, double r) {
         double vx = y - v;
         double vy = -(x - u);
         double length = (double) Math.sqrt(vx * vx + vy * vy);
         vx = vx / length;
         vy = vy / length;
         double a = vy;
         double b = -vx;
         double c = vy * u - vx * v;
         double A = c - a * u;
         double C = a * a + b * b;
         double D = -2 * (A * b + v * a * a);
         double E = A * A + a * a * (v * v - r * r);
         double sy1 = (double) (-D + Math.sqrt(D * D - 4 * C * E)) / (2 * C);
         double sy2 = (double) (-D - Math.sqrt(D * D - 4 * C * E)) / (2 * C);
         double sx1 = (u != x) ? (c - b * sy1) / a : u - r;
         double sx2 = (u != x) ? (c - b * sy2) / a : u + r;
         return new Pair(new Point2D(sx1, sy1), new Point2D(sx2, sy2));
     }
     private Graph<ColorableGraphNode, ColorableGraphArc> graph = new ExtendedDirectedGraph<>();
     private Mode currentMode;
     private Node selectedNode = null;
     private Node selectedNodeInArcMode = null;
     private Arc selectedArc;
     private HashMap<Integer, Node> nodes = new HashMap<>();
     private HashMap<Integer, Arc> arcs = new HashMap<>();
     private GraphAlgorithm<ColorableGraphNode, ColorableGraphArc> algorithm;
     private Node arcTo;
     private boolean animating = false;
     private Timer timer = new Timer();
     @FXML
     private ComboBox<AlgorithmModel> cbAlgoritmSelector;
     @FXML
     private AnchorPane ap;
     @FXML
     private Pane costPane;
     @FXML
     private TextField costBox;
     @FXML
     private RadioButton nodeModeRb;
     @FXML
     private RadioButton arcModeRb;
     @FXML
     private RadioButton deleteModeRb;
     @FXML
     private ToggleGroup modeGroup;
     @FXML
     private Button btRun;
     @FXML
     private Button btClearSelectedNode;
     @FXML
     private Label lbArcCount;
     @FXML
     private Label lbNodeCount;
     @FXML
     private Label lbDirected;
     @FXML
     private ListView<String> lvLog;
     @FXML
     private Button btClearSelectedArc;
     @FXML
     private Button btStep;
     @FXML
     private Button btPlay;
     @FXML
     private Button btStop;
     @FXML
     private Slider slSpeed;
     Arc unboundArc = null;
     private GraphIO graphIO = new GraphIO();
     private EventHandler<MouseEvent> mouseClickedEventHandler;
     private EventHandler<MouseEvent> mouseReleasedEventHandler;
     private EventHandler<MouseEvent> mousePressedEventHandler;
     private EventHandler<MouseEvent> arcPressedEventHandler;
 
     protected void setSelectedNode(Node c) {
         if (c != null) {
             log("Current node: " + c.getNodeId());
             btClearSelectedNode.setDisable(false);
         }
         selectedNode = c;
         refreshGraphView();
     }
 
     protected void setSelectedArc(Arc arc) {
         selectedArc = arc;
         costPane.setVisible(arc != null);
         btClearSelectedArc.setDisable(arc == null);
         if (arc != null) {
             costBox.setText(arc.getGraphArc().getCost().toString());
 
         }
         refreshGraphView();
     }
 
     @Override
     public void initialize(URL url, ResourceBundle rb) {
         currentMode = Mode.NODE;
         assert ap != null : "fx:id=\"ap\" was not injected: check your FXML file 'Main.fxml'.";
         assert arcModeRb != null : "fx:id=\"arcModeRb\" was not injected: check your FXML file 'Main.fxml'.";
         assert btClearSelectedNode != null : "fx:id=\"btClearSelectedNode\" was not injected: check your FXML file 'Main.fxml'.";
         assert btRun != null : "fx:id=\"btRun\" was not injected: check your FXML file 'Main.fxml'.";
         assert cbAlgoritmSelector != null : "fx:id=\"cbAlgoritmSelector\" was not injected: check your FXML file 'Main.fxml'.";
         assert deleteModeRb != null : "fx:id=\"deleteModeRb\" was not injected: check your FXML file 'Main.fxml'.";
         assert modeGroup != null : "fx:id=\"modeGroup\" was not injected: check your FXML file 'Main.fxml'.";
         assert nodeModeRb != null : "fx:id=\"nodeModeRb\" was not injected: check your FXML file 'Main.fxml'.";
         costPane.setVisible(false);
         btClearSelectedArc.setDisable(true);
         btClearSelectedNode.setDisable(true);
         mouseClickedEventHandler = new EventHandler<MouseEvent>() {
             @Override
             public void handle(MouseEvent t) {
                 Node circle = null;
                 if (t.getSource() instanceof Text) {
                     Text label = (Text) t.getSource();
                     circle = (Node) label.getProperties().get(ATTACHED_CIRCLE_KEY);
                 } else {
                     circle = (Node) t.getSource();
                 }
                 if (currentMode.equals(Mode.NODE)) {
                     setSelectedNode(circle);
                 } else if (currentMode.equals(Mode.DELETE)) {
                     Integer nodeId = circle.getNodeId();
                     ArrayList<Integer> removedArcs = getGraph().removeNode(nodeId);
                     if (removedArcs != null && removedArcs.size() > 0) {
                         for (Integer arcId : removedArcs) {
                             ap.getChildren().remove(arcs.get(arcId));
                             ap.getChildren().remove(arcs.get(arcId).getArrowLines()[0]);
                             ap.getChildren().remove(arcs.get(arcId).getArrowLines()[1]);
                             ap.getChildren().remove(arcs.get(arcId).getLabel());
                             arcs.remove(arcId);
                         }
                     }
                     ap.getChildren().remove(circle);
                     getNodes().remove(nodeId);
                     Text label = circle.getLabel();
                     circle.getProperties().clear();
                     ap.getChildren().remove(label);
                     if (selectedNode == circle) {
                         btClearSelectedNode.setDisable(true);
                         setSelectedNode(null);
                     }
                     refreshGraphView();
                 } else if (currentMode.equals(Mode.ARC)) {
 //                        if (selectedNodeInArcMode == null) {
 //                            selectedNodeInArcMode = circle;
 //                        } else {
 //                          
 //
 //                        }
                 }
                 t.consume();
             }
         };
         mouseReleasedEventHandler = new EventHandler<MouseEvent>() {
             @Override
             public void handle(MouseEvent t) {
                 Node circle = null;
                 if (t.getSource() instanceof Text) {
                     Text label = (Text) t.getSource();
                     circle = (Node) label.getProperties().get(ATTACHED_CIRCLE_KEY);
                 } else {
                     circle = (Node) t.getSource();
                 }
                 if (currentMode.equals(Mode.NODE)) {
                     System.out.println("Hello");
                     setSelectedNode(circle);
                 } else if (currentMode.equals(Mode.ARC)) {
                     if (arcTo == null) {
                         ap.getChildren().remove(unboundArc);
                         ap.getChildren().remove(unboundArc.getArrowLines()[0]);
                         ap.getChildren().remove(unboundArc.getArrowLines()[1]);
                     }
                     if (unboundArc != null && selectedNodeInArcMode != null && arcTo != null) {
                         try {
                             //arcTo = circle;
                             Integer u = selectedNodeInArcMode.getNodeId();
                             Integer v = arcTo.getNodeId();
                             log(selectedNodeInArcMode.getNodeId() + "-->" + arcTo.getNodeId());
                             if (getGraph().getArc(u, v) != null) {
                                 MessageBox.show(null, "Többszörös él nem megengedett!", "Hiba", MessageBox.OK);
                                 ap.getChildren().remove(unboundArc);
                                 ap.getChildren().remove(unboundArc.getArrowLines()[0]);
                                 ap.getChildren().remove(unboundArc.getArrowLines()[1]);
                             } else {
                                 if (!u.equals(v)) {
                                     Integer id = getGraph().createArc(u, v, 1.0f, new ColorableGraphArc());
                                     unboundArc.setGraphArc(getGraph().getArc(id));
                                     getArcs().put(id, unboundArc);
                                     Pair<Point2D, Point2D> points1 = getPoints(selectedNodeInArcMode.getCenterX(), selectedNodeInArcMode.getCenterY(), arcTo.getCenterX(), arcTo.getCenterY(), ARC_DISTANCE);
                                     Pair<Point2D, Point2D> points2 = getPoints(arcTo.getCenterX(), arcTo.getCenterY(), selectedNodeInArcMode.getCenterX(), selectedNodeInArcMode.getCenterY(), ARC_DISTANCE);
                                     unboundArc.setNodeToId(v);
                                     unboundArc.setNodeFromId(u);
 
                                     if (getGraph().getArc(v, u) != null) {
                                         Arc otherArc = getArcs().get(getGraph().getPairOfArc(id).getId());
                                         if (u < v) {
                                             moveArc(unboundArc, points1.getKey().getX(), points1.getKey().getY(), points2.getKey().getX(), points2.getKey().getY());
                                             moveArc(otherArc, points2.getValue().getX(), points2.getValue().getY(), points1.getValue().getX(), points1.getValue().getY());
                                         } else {
                                             moveArc(unboundArc, points1.getValue().getX(), points1.getValue().getY(), points2.getValue().getX(), points2.getValue().getY());
                                             moveArc(otherArc, points2.getKey().getX(), points2.getKey().getY(), points1.getKey().getX(), points1.getKey().getY());
 
                                         }
                                     } else {
                                         moveArc(unboundArc, selectedNodeInArcMode.getCenterX(), selectedNodeInArcMode.getCenterY(), arcTo.getCenterX(), arcTo.getCenterY());
                                     }
                                     unboundArc.toBack();
 
                                     refreshGraphView();
                                 } else {
                                     if (u != v) {
                                         MessageBox.show(null, "A hurokélek nem megengedettek!", "Hiba", MessageBox.OK);
                                     }
                                     ap.getChildren().remove(unboundArc);
                                     ap.getChildren().remove(unboundArc.getArrowLines()[0]);
                                     ap.getChildren().remove(unboundArc.getArrowLines()[1]);
                                 }
                             }
 
 
 
 
                             arcTo = null;
                             unboundArc = null;
                             selectedNodeInArcMode = null;
                         } catch (Exception ex) {
                             Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                         }
 
                     }
                 }
                 t.consume();
             }
         };
         mousePressedEventHandler = new EventHandler<MouseEvent>() {
             @Override
             public void handle(MouseEvent t) {
                 Node circle = null;
                 if (t.getSource() instanceof Text) {
                     Text label = (Text) t.getSource();
                     circle = (Node) label.getProperties().get(ATTACHED_CIRCLE_KEY);
                 } else {
                     circle = (Node) t.getSource();
                 }
                 if (currentMode.equals(Mode.NODE)) {
                     System.out.println("Hello");
                     setSelectedNode(circle);
                 } else if (currentMode.equals(Mode.ARC)) {
                     selectedNodeInArcMode = circle;
                     unboundArc = new Arc(circle.getCenterX(), circle.getCenterY(), t.getX(), t.getY());
                     unboundArc.setStrokeWidth(3);
 
                     ap.getChildren().add(unboundArc);
                     ap.getChildren().add(unboundArc.getArrowLines()[0]);
                     ap.getChildren().add(unboundArc.getArrowLines()[1]);
                     unboundArc.addEventFilter(MouseEvent.MOUSE_PRESSED, arcPressedEventHandler);
                 }
                 t.consume();
             }
         };
         arcPressedEventHandler = new EventHandler<MouseEvent>() {
             @Override
             public void handle(MouseEvent t) {
                 Arc l = (Arc) t.getTarget();
                 if (t.getButton().equals(MouseButton.PRIMARY) && currentMode.equals(Mode.ARC)) {
                     l.setColor(Color.RED);
                     setSelectedArc(l);
                 } else if (t.getButton().equals(MouseButton.PRIMARY) && currentMode.equals(Mode.DELETE)) {
                     graph.removeArc(l.getGraphArc().getId());;
                     ap.getChildren().remove(l);
                     ap.getChildren().remove(l.getArrowLines()[0]);
                     ap.getChildren().remove(l.getArrowLines()[1]);
                     ap.getChildren().remove(l.getLabel());
                     arcs.remove(l.getGraphArc().getId());
                 }
                 t.consume();
             }
         };
         initAlgorithmList();
     }
 
     @FXML
     public void onCostBoxKeyTyped(KeyEvent evt) {
         Float cost;
         try {
             cost = Float.parseFloat(costBox.getText());
         } catch (Exception e) {
             cost = selectedArc.getGraphArc().getCost();
         }
         selectedArc.getGraphArc().setCost(cost);
         selectedArc.refreshLabel();
     }
 
     @FXML
     public void mouseMoved(MouseEvent e) {
         //log("Pos:"+e.getX()+"|"+e.getY());
     }
 
     @FXML
     void onAlgorithmChanged(ActionEvent event) {
 
         log(cbAlgoritmSelector.getSelectionModel().getSelectedItem().getName());
     }
 
     private double distance(double ax, double ay, double bx, double by) {
         return Math.sqrt(Math.pow(ax - bx, 2) + Math.pow(ay - by, 2));
     }
 
     @FXML
     public void mouseDragged(MouseEvent e) {
         if (e.getButton().equals(MouseButton.PRIMARY) && currentMode.equals(Mode.NODE)) {
 
             if (selectedNode != null) {
 
                 selectedNode.setCenterX(e.getX());
                 selectedNode.setCenterY(e.getY());
                 //selectedNode.relocate(e.getX() - selectedNode.getRadius(), e.getY() - selectedNode.getRadius());
                 //log(Double.toString(selectedNod.getX())+"|"+Double.toString(selectedNode.getCenterY()));
                 Text label = selectedNode.getLabel();
                 label.setX(e.getX() - label.getLayoutBounds().getWidth() / 2 + 1);
                 label.setY(e.getY() + 5);
                 ArrayList<ColorableGraphArc> allArc = new ArrayList<>(graph.getInboundArcs(selectedNode.getNodeId()));
                 allArc.addAll(graph.getOutboundArcs(selectedNode.getNodeId()));
                 for (ColorableGraphArc p : allArc) {
                     Arc arc = arcs.get(p.getId());
                     Integer u = arc.getNodeFromId();
                     Integer v = arc.getNodeToId();
                     Node nodeTo = nodes.get(v);
                     Node nodeFrom = nodes.get(u);
                     if (graph.getArc(v, u) != null) {
 
                         Pair<Point2D, Point2D> points1 = getPoints(nodeFrom.getCenterX(), nodeFrom.getCenterY(), nodeTo.getCenterX(), nodeTo.getCenterY(), ARC_DISTANCE);
                         Pair<Point2D, Point2D> points2 = getPoints(nodeTo.getCenterX(), nodeTo.getCenterY(), nodeFrom.getCenterX(), nodeFrom.getCenterY(), ARC_DISTANCE);
                         if (u < v) {
                             moveArc(arc, points1.getKey().getX(), points1.getKey().getY(), points2.getKey().getX(), points2.getKey().getY());
                         } else {
                             moveArc(arc, points1.getValue().getX(), points1.getValue().getY(), points2.getValue().getX(), points2.getValue().getY());
                         }
                     } else {
                         moveArc(arc, nodeFrom.getCenterX(), nodeFrom.getCenterY(), nodeTo.getCenterX(), nodeTo.getCenterY());
                     }
                 }
             }
         }
         if (e.getButton().equals(MouseButton.PRIMARY) && currentMode.equals(Mode.ARC)) {
             if (unboundArc != null) {
                 unboundArc.setEnd(e.getX(), e.getY());;
             }
             for (Node c : nodes.values()) {
                 if (distance(e.getX(), e.getY(), c.getCenterX(), c.getCenterY()) < c.getRadius()) {
                     arcTo = c;
                 }
             }
             //ap.getChildren().add(new Node(e.getX(), e.getY(), 1));
         }
     }
 
     @FXML
     public void mouseReleased(MouseEvent e) {
         log("Released");
     }
 
     @FXML
     public void mousePressed(MouseEvent e) throws Exception {
         if (e.getButton().equals(MouseButton.PRIMARY) && currentMode.equals(Mode.NODE)) {
             Integer id = graph.createNode(new ColorableGraphNode());
             final Node c = new Node(id, 20);
             ap.getChildren().add(c);
             nodes.put(id, c);
             c.setCenterX(e.getX());
             c.setCenterY(e.getY());
             Text label = c.getLabel();
             ap.getChildren().add(label);
             c.setFill(getNodeColor(graph.getNode(id).getColor()));
             label.setFill(getNodeTextColor(graph.getNode(id).getColor()));
             label.setFont(Font.font("Times New Roman", FontWeight.BOLD, 14));
             label.setTextAlignment(TextAlignment.CENTER);
             label.setX(e.getX() - label.getLayoutBounds().getWidth() / 2);
             label.setY(e.getY() + 5);
             label.getProperties().put(ATTACHED_CIRCLE_KEY, c);
             setSelectedNode(c);
             label.addEventFilter(MouseEvent.MOUSE_CLICKED, mouseClickedEventHandler);
             c.addEventFilter(MouseEvent.MOUSE_CLICKED, mouseClickedEventHandler);
             c.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleasedEventHandler);
             label.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleasedEventHandler);
             label.addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedEventHandler);
             c.addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedEventHandler);
             refreshGraphView();
         } else if (e.getButton().equals(MouseButton.PRIMARY) && currentMode.equals(Mode.ARC)) {
             clearSelectedArc();
         }
     }
 
     @FXML
     public void mouseClicked(MouseEvent e) {
     }
 
     @FXML
     public void modeChanged() throws NotSupportedModeException {
         if (modeGroup.getSelectedToggle().equals(nodeModeRb)) {
             currentMode = Mode.NODE;
         } else if (modeGroup.getSelectedToggle().equals(arcModeRb)) {
             currentMode = Mode.ARC;
         } else if (modeGroup.getSelectedToggle().equals(deleteModeRb)) {
             currentMode = Mode.DELETE;
         } else {
             throw new NotSupportedModeException();
         }
         log("Current mode: " + currentMode.toString());
     }
 
     private void getPolylineArc(Point2D tip, Point2D tail, int mode) {
         Polyline pl = new Polyline();
         double dy = tip.getY() - tail.getY();
         double dx = tip.getX() - tail.getX();
         double theta = Math.atan2(dy, dx);
         //System.out.println("theta = " + Math.toDegrees(theta));  
         double phi = Math.toRadians(30);
 
         double x1, y1, x2, y2, rho = theta + phi;
         x1 = tip.getX() - 20 * Math.cos(rho);
         y1 = tip.getY() - 20 * Math.sin(rho);
         rho = theta - phi;
         x2 = tip.getX() - 20 * Math.cos(rho);
         y2 = tip.getY() - 20 * Math.sin(rho);
     }
 
     @FXML
     protected void clearGraph() {
         if (MessageBox.show(null, "Biztosan törli a gráfot?", "Megerősítés kérése", MessageBox.YES | MessageBox.NO) == MessageBox.YES) {
             clearFullGraph();
         }
     }
 
     @FXML
     protected void close() {
         if (MessageBox.show(null, "Biztosan kilép?", "Megerősítés kérése", MessageBox.YES | MessageBox.NO) == MessageBox.YES) {
             System.exit(0);
         }
     }
 
     @FXML
     protected void loadGraph() {
         FileChooser fileChooser = new FileChooser();
         fileChooser.setTitle("Gráf mentése...");
         FileNameExtensionFilter filter = new FileNameExtensionFilter("Gráf fájlok", "graph");
         fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Gráf fájlok", "*.graph"));
         File selected = fileChooser.showOpenDialog(null);
         if (selected != null) {
             ap.getChildren().clear();
             arcs.clear();
             nodes.clear();
             graph.clear();
             graphIO.loadGraph(selected, this);
             refreshGraphView();
         }
     }
 
     @FXML
     protected void saveGraph() {
         FileChooser fileChooser = new FileChooser();
         fileChooser.setTitle("Gráf mentése...");
         FileNameExtensionFilter filter = new FileNameExtensionFilter("Gráf fájlok", "graph");
         fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Gráf fájlok", "*.graph"));
         File selected = fileChooser.showSaveDialog(null);
         if (selected != null) {
             graphIO.saveGraph(selected, this);
         }
     }
 
     private Color getNodeColor(ColorableGraphNode.Color nodeColor) {
         Color color = null;
         switch (nodeColor) {
             case WHITE:
                 color = Color.WHITE;
                 break;
             case BLACK:
                 color = Color.BLACK;
                 break;
             case BLUE:
                 color = Color.BLUE;
                 break;
             case GRAY:
                 color = Color.GRAY;
                 break;
             case GREEN:
                 color = Color.GREEN;
                 break;
             case PURPLE:
                 color = Color.PURPLE;
                 break;
             case RED:
                 color = Color.RED;
                 break;
             case YELLOW:
                 color = Color.YELLOW;
                 break;
         }
         return color;
     }
 
     private Color getNodeTextColor(ColorableGraphNode.Color nodeColor) {
         Color textColor = Color.RED;
         switch (nodeColor) {
             case WHITE:
                 textColor = Color.BLACK;
                 break;
             case BLACK:
                 textColor = Color.WHITE;
                 break;
             case BLUE:
                 break;
             case GRAY:
                 textColor = Color.DEEPSKYBLUE;
                 break;
             case GREEN:
                 break;
             case PURPLE:
                 break;
             case RED:
                 textColor = Color.YELLOW;
                 break;
             case YELLOW:
                 textColor = Color.RED;
                 break;
             default:
                 textColor = Color.RED;
                 break;
         }
         return textColor;
     }
 
     private Color getArcColor(ColorableGraphArc.Color arcColor) {
         Color color = null;
         switch (arcColor) {
             case WHITE:
                 color = Color.WHITE;
                 break;
             case BLACK:
                 color = Color.BLACK;
                 break;
             case BLUE:
                 color = Color.BLUE;
                 break;
             case GRAY:
                 color = Color.GRAY;
                 break;
             case GREEN:
                 color = Color.GREEN;
                 break;
             case PURPLE:
                 color = Color.PURPLE;
                 break;
             case RED:
                 color = Color.RED;
                 break;
             case YELLOW:
                 color = Color.YELLOW;
                 break;
         }
         return color;
     }
 
     private void refreshGraphView() {
         for (Node c : nodes.values()) {
             Integer id = c.getNodeId();
             ColorableGraphNode node = graph.getNode(id);
             Color color = getNodeColor(node.getColor());
             Color textColor = getNodeTextColor(node.getColor());
 
             c.setFill(color);
             Text label = c.getLabel();
             label.setFill(textColor);
             if (c == selectedNode) {
                 selectedNode.setStrokeWidth(3);
                 selectedNode.setStroke(Color.RED);
             } else {
                 c.setStrokeWidth(3);
                 c.setStroke(getNodeColor(graph.getNode(c.getNodeId()).getColor()));
             }
             //Tooltip.install(c, new Tooltip(c.getProperties().get(NODE_ID_KEY).toString()));
         }
         for (Arc a : arcs.values()) {
 
             a.setColor(getArcColor(a.graphArc.getColor()));
             if (a == selectedArc) {
                 a.setColor(Color.RED);
             } else {
                 a.setColor(getArcColor(a.getGraphArc().getColor()));
             }
         }
         lbArcCount.setText(Integer.toString(graph.getArcCount()));
         lbNodeCount.setText(Integer.toString(graph.getNodeCount()));
         lbDirected.setText(graph.isDirected() ? "Igen" : "Nem");
 
     }
 
     @FXML
     public void btRunClicked() throws UndirectedGraphRequiredException {
         AlgorithmModel currentAlgorithm = cbAlgoritmSelector.getSelectionModel().getSelectedItem();
         if (currentAlgorithm.needStartNode && selectedNode == null) {
             MessageBox.show(null, "A kiválasztott algoritmus startcsúcsot igényel!", "Figyelmeztetés", MessageBox.OK | MessageBox.ICON_WARNING);
             return;
         }
         if (currentAlgorithm.id == KRUSKAL_ALGO_CODE) {
             if (graph.isDirected()) {
                 MessageBox.show(null, "A kiválasztott algoritmus irányítatlan gráfot igényel!", "Figyelmeztetés", MessageBox.OK | MessageBox.ICON_WARNING);
                 return;
             }
             KruskalAlgorithm ka = new KruskalAlgorithm();
             ka.initialize(graph);
             log(ka.run());
             for (ColorableGraphArc ga : graph.getArcs()) {
                 log(ga.toString());
 
             }
         }
         if (currentAlgorithm.id == BFS_ALGO_CODE) {
             BreadthFirstAlgorithm ba = new BreadthFirstAlgorithm();
             ba.initialize(graph);
             if (selectedNode != null) {
                 ba.run(graph.getNode(selectedNode.getNodeId()));
             } else {
                 ba.run();
             }
         }
 
         if (currentAlgorithm.id == DIJKSTRA_ALGO_CODE) {
             DijkstraAlgorithm da = new DijkstraAlgorithm();
             da.initialize(graph);
             if (selectedNode != null) {
                 da.run(graph.getNode(selectedNode.getNodeId()));
             } else {
                 da.run();
             }
         }
 
         refreshGraphView();
         MessageBox.show(null, "Az algoritmus sikeresen véget ért.", "Algoritmus vége", MessageBox.OK | MessageBox.ICON_INFORMATION);
         clearView();
     }
 
     @FXML
     public void clearSelectedNode() {
         setSelectedNode(null);
         btClearSelectedNode.setDisable(true);
     }
 
     @FXML
     public void clearSelectedArc() {
         setSelectedArc(null);
     }
 
     @FXML
     public void btStopClicked() {
         animating = false;
         clearView();
         if (timer != null) {
             timer.cancel();
             timer.purge();
         }
         timer = null;
         algorithm = null;
         btPlay.setDisable(false);
         btStop.setDisable(true);
         btRun.setDisable(false);
         btStep.setDisable(false);
     }
 
     @FXML
     public void btPlayClicked() {
         AlgorithmModel currentAlgorithm = cbAlgoritmSelector.getSelectionModel().getSelectedItem();
         if (currentAlgorithm.needStartNode && selectedNode == null) {
             MessageBox.show(null, "A kiválasztott algoritmus startcsúcsot igényel!", "Figyelmeztetés", MessageBox.OK | MessageBox.ICON_WARNING);
             return;
         }
         if (currentAlgorithm.id == KRUSKAL_ALGO_CODE) {
             if (graph.isDirected()) {
                 MessageBox.show(null, "A kiválasztott algoritmus irányítatlan gráfot igényel!", "Figyelmeztetés", MessageBox.OK | MessageBox.ICON_WARNING);
                 btRun.setDisable(false);
                 return;
             }
         }
         btStep.setDisable(true);
         btStop.setDisable(false);
         btPlay.setDisable(true);
         animating = true;
         if (timer == null) {
             timer = new Timer();
         }
         timer.schedule(new TimerTask() {
             @Override
             public void run() {
                 if (animating) {
                     btStepClicked();
                 }
             }
         }, 0, (long) Math.ceil(slSpeed.getValue() * 2000));
 
     }
 
     @FXML
     public void btStepClicked() {
         AlgorithmModel currentAlgorithm = cbAlgoritmSelector.getSelectionModel().getSelectedItem();
         if (currentAlgorithm.needStartNode && selectedNode == null) {
             MessageBox.show(null, "A kiválasztott algoritmus startcsúcsot igényel!", "Figyelmeztetés", MessageBox.OK | MessageBox.ICON_WARNING);
             return;
         }
         if (algorithm == null) {
             btRun.setDisable(true);
             btPlay.setDisable(true);
             btStop.setDisable(false);
             clearSelectedArc();
             if (currentAlgorithm.id == BFS_ALGO_CODE) {
                 BreadthFirstAlgorithm ba = new BreadthFirstAlgorithm();
                 ba.initialize(graph);
                 if (selectedNode != null) {
                     ba.start(graph.getNode(selectedNode.getNodeId()));
                 } else {
                     ba.start();
                 }
                 algorithm = ba;
             }
             if (currentAlgorithm.id == DIJKSTRA_ALGO_CODE) {
                 DijkstraAlgorithm da = new DijkstraAlgorithm();
                 da.initialize(graph);
                 if (selectedNode != null) {
                     da.start(graph.getNode(selectedNode.getNodeId()));
                 } else {
                     da.start();
                 }
                 algorithm = da;
             }
             if (currentAlgorithm.id == KRUSKAL_ALGO_CODE) {
                 if (graph.isDirected()) {
                     MessageBox.show(null, "A kiválasztott algoritmus irányítatlan gráfot igényel!", "Figyelmeztetés", MessageBox.OK | MessageBox.ICON_WARNING);
                     btRun.setDisable(false);
                     btPlay.setDisable(false);
                     btStep.setDisable(false);
                     btStop.setDisable(true);
                     return;
                 }
                 KruskalAlgorithm ka = new KruskalAlgorithm();
                 ka.initialize(graph);
                 ka.start();
                 algorithm = ka;
             }
             
             if (currentAlgorithm.id == DFS_ALGO_CODE) {
                if (graph.isDirected()) {
                    MessageBox.show(null, "A kiválasztott algoritmus irányítatlan gráfot igényel!", "Figyelmeztetés", MessageBox.OK | MessageBox.ICON_WARNING);
                    btRun.setDisable(false);
                    btPlay.setDisable(false);
                    btStep.setDisable(false);
                    btStop.setDisable(true);
                    return;
                }
                 DepthFirstAlgorithm da = new DepthFirstAlgorithm();
                 da.initialize(graph);
                 if (selectedNode != null) {
                     da.start(graph.getNode(selectedNode.getNodeId()));
                 } else {
                     da.start();
                 }
                 algorithm = da;
             }
         } else {
             if (algorithm.doStep() == null) {
                 Platform.runLater(new Runnable() {
                     @Override
                     public void run() {
                         animating = false;
                         if (timer != null) {
                             timer.cancel();
                             timer.purge();
                         }
                         timer = null;
                         MessageBox.show(null, "Az algoritmus sikeresen véget ért.", "Algoritmus vége", MessageBox.OK | MessageBox.ICON_INFORMATION);
                         algorithm = null;
                         clearView();
                         btRun.setDisable(false);
                         btPlay.setDisable(false);
                         btStep.setDisable(false);
                         btStop.setDisable(true);
                     }
                 });
             }
 
         }
         refreshGraphView();
     }
 
     public void log(String message) {
         lvLog.getItems().add(message != null ? message : "The message is null!");
         lvLog.scrollTo(lvLog.getItems().size() - 1);
     }
 
     private void initAlgorithmList() {
         cbAlgoritmSelector.getItems().clear();
         cbAlgoritmSelector.getItems().add(new AlgorithmModel("Kruskal algoritmus", KRUSKAL_ALGO_CODE, false));
         cbAlgoritmSelector.getItems().add(new AlgorithmModel("Dijkstra algoritmus", DIJKSTRA_ALGO_CODE, true));
         cbAlgoritmSelector.getItems().add(new AlgorithmModel("Szélességi bejárás", BFS_ALGO_CODE, true));
         cbAlgoritmSelector.getItems().add(new AlgorithmModel("Mélységi bejárás", DFS_ALGO_CODE, true));
         cbAlgoritmSelector.getSelectionModel().selectFirst();
         cbAlgoritmSelector.setCellFactory(new Callback<ListView<AlgorithmModel>, ListCell<AlgorithmModel>>() {
             @Override
             public ListCell<AlgorithmModel> call(ListView<AlgorithmModel> p) {
                 final ListCell<AlgorithmModel> cell = new ListCell<AlgorithmModel>() {
                     @Override
                     protected void updateItem(AlgorithmModel t, boolean bln) {
                         super.updateItem(t, bln);
                         if (t != null) {
                             setText(t.toString());
                         } else {
                             setText(null);
                         }
                     }
                 };
 
                 return cell;
             }
         });
     }
 
     private void clearView() {
         for (ColorableGraphNode cn : graph.getNodes()) {
             cn.setColor(WHITE);
         }
         for (ColorableGraphArc ca : graph.getArcs()) {
             ca.setColor(ColorableGraphArc.Color.BLACK);
         }
         refreshGraphView();
     }
 
     protected void moveArc(Arc arc, double startX, double startY, double endX, double endY) {
         arc.setStart(startX, startY);
         arc.setEnd(endX, endY);
     }
 
     public Graph<ColorableGraphNode, ColorableGraphArc> getGraph() {
         return graph;
     }
 
     public HashMap<Integer, Node> getNodes() {
         return nodes;
     }
 
     public HashMap<Integer, Arc> getArcs() {
         return arcs;
     }
 
     void createNode(int id) {
         ColorableGraphNode cgn = new ColorableGraphNode();
         cgn.setId(id);
         graph.createNode(cgn);
     }
 
     void createArc(int id, int from, int to, float cost) throws ArcAlreadyExistsException {
         ColorableGraphArc cga = new ColorableGraphArc();
         cga.initialize(from, to, cost, id);
         graph.createArc(from, to, cost, cga);
     }
 
     public void clearFullGraph() {
         graph.clear();
         ap.getChildren().clear();
         nodes.clear();
     }
 
     void createNodeView(int id, double x, double y) {
         Node n = new Node(id);
 
         ap.getChildren().add(n);
         nodes.put(id, n);
         n.setCenterX(x);
         n.setCenterY(y);
         n.setNodeId(id);
         Text label = n.getLabel();
         ap.getChildren().add(label);
         n.setFill(getNodeColor(graph.getNode(id).getColor()));
         label.setFill(getNodeTextColor(graph.getNode(id).getColor()));
         label.setFont(Font.font("Times New Roman", FontWeight.BOLD, 14));
         label.setTextAlignment(TextAlignment.CENTER);
         label.setX(x - label.getLayoutBounds().getWidth() / 2);
         label.setY(y + 5);
         label.getProperties().put(ATTACHED_CIRCLE_KEY, n);
         label.addEventFilter(MouseEvent.MOUSE_CLICKED, mouseClickedEventHandler);
         n.addEventFilter(MouseEvent.MOUSE_CLICKED, mouseClickedEventHandler);
         n.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleasedEventHandler);
         label.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleasedEventHandler);
         label.addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedEventHandler);
         n.addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedEventHandler);
         refreshGraphView();
     }
 
     void createArcView(int id, double sx, double sy, double ex, double ey) {
         Arc a = new Arc(sx, sy, ex, ey);
         a.setGraphArc(graph.getArc(id));
         ap.getChildren().add(a);
         a.setStart(sx, sy);
         a.setEnd(ex, ey);
         ap.getChildren().add(a.getArrowLines()[0]);
         ap.getChildren().add(a.getArrowLines()[1]);
         a.toBack();
         a.addEventFilter(MouseEvent.MOUSE_PRESSED, arcPressedEventHandler);
         arcs.put(id, a);
         refreshGraphView();
     }
 
     private static class AlgorithmModel {
 
         String name;
         Integer id;
         Boolean needStartNode;
 
         public AlgorithmModel(String name, Integer id, Boolean needStartNode) {
             this.name = name;
             this.id = id;
             this.needStartNode = needStartNode;
         }
 
         @Override
         public String toString() {
             return name;
         }
 
         private String getName() {
             return name;
         }
     }
 
     private enum Mode {
 
         NODE, ARC, DELETE
     };
 
     protected class Node extends Circle {
 
         protected Integer nodeId;
         protected Text label;
 
         public Node(Integer nodeId) {
             super(20);
             this.nodeId = nodeId;
             label = new Text(nodeId.toString());
 
         }
 
         public Node(Integer nodeId, double d) {
             super(d);
             this.nodeId = nodeId;
             label = new Text(nodeId.toString());
         }
 
         public Integer getNodeId() {
             return nodeId;
         }
 
         public void setNodeId(Integer nodeId) {
             this.nodeId = nodeId;
         }
 
         public Text getLabel() {
             return label;
         }
     }
 
     protected class Arc extends Line {
 
         protected Integer nodeToId;
         protected Integer nodeFromId;
         protected ColorableGraphArc graphArc;
         protected Text label = new Text("1.0");
         protected Line[] arrowLines = new Line[]{new Line(0, 0, 0, 0), new Line(0, 0, 0, 0)};
 
         {
             label.setFill(Color.ANTIQUEWHITE);
             arrowLines[0].setStrokeWidth(3);
             arrowLines[1].setStrokeWidth(3);
         }
 
         public Arc(double d, double d1, double d2, double d3) {
             super(d, d1, d2, d3);
             label.setTextAlignment(TextAlignment.JUSTIFY);
             label.setFont(Font.font("Courier New", FontWeight.EXTRA_BOLD, 12));
             setStrokeWidth(3);
             refreshLabel();
             refreshArrow();
         }
 
         public void setColor(Color color) {
             setStroke(color);
             arrowLines[0].setStroke(color);
             arrowLines[1].setStroke(color);
         }
 
         public void setEnd(double x, double y) {
             setEndX(x);
             setEndY(y);
             refreshLabel();
             refreshArrow();
         }
 
         public void setStart(double x, double y) {
             setStartX(x);
             setStartY(y);
             refreshLabel();
             refreshArrow();
         }
 
         public Integer getNodeToId() {
             return nodeToId;
 
         }
 
         public void setNodeToId(Integer nodeToId) {
             this.nodeToId = nodeToId;
         }
 
         public Integer getNodeFromId() {
             return nodeFromId;
         }
 
         public void setNodeFromId(Integer nodeFromId) {
             this.nodeFromId = nodeFromId;
         }
 
         public ColorableGraphArc getGraphArc() {
             return graphArc;
         }
 
         public void setGraphArc(ColorableGraphArc graphArc) {
             this.graphArc = graphArc;
             nodeFromId = graphArc.getFromId();
             nodeToId = graphArc.getToId();
             ap.getChildren().add(label);
             refreshLabel();
         }
 
         private void refreshLabel() {
             double dx = getEndX() - getStartX();
             double dy = getEndY() - getStartY();
             double length = Math.sqrt(dx * dx + dy * dy);
             double normDX = dx / length;
             double normDY = dy / length;
             label.setX(getEndX() - normDX * length / 3);
             label.setY(getEndY() - normDY * length / 3);
             if (graphArc != null) {
                 label.setText(graphArc.getCost().toString());
             }
 
         }
 
         public Line[] getArrowLines() {
             return arrowLines;
         }
 
         public void refreshArrow() {
 
             arrowLines[0].setStrokeWidth(3);
             arrowLines[1].setStrokeWidth(3);
             double dy = getEndY() - getStartY();
             double dx = getEndX() - getStartX();
             double length = Math.sqrt(dx * dx + dy * dy);
             double normalizedDY = dy / length;
             double normalizedDX = dx / length;
             double theta = Math.atan2(dy, dx);
             //System.out.println("theta = " + Math.toDegrees(theta));  
             double phi = Math.toRadians(25);
 
             double x1, y1, x2, y2, rho = theta + phi;
             x1 = getEndX() - 10 * Math.cos(rho);
             y1 = getEndY() - 10 * Math.sin(rho);
             rho = theta - phi;
             x2 = getEndX() - 10 * Math.cos(rho);
             y2 = getEndY() - 10 * Math.sin(rho);
             if (graphArc == null) {
                 normalizedDX = normalizedDY = 0;
             }
             arrowLines[0].setEndX(getEndX() - normalizedDX * 25);
             arrowLines[0].setEndY(getEndY() - normalizedDY * 25);
             arrowLines[0].setStartX(x1 - normalizedDX * 25);
             arrowLines[0].setStartY(y1 - normalizedDY * 25);
             arrowLines[1].setEndX(getEndX() - normalizedDX * 25);
             arrowLines[1].setEndY(getEndY() - normalizedDY * 25);
             arrowLines[1].setStartX(x2 - normalizedDX * 25);
             arrowLines[1].setStartY(y2 - normalizedDY * 25);
         }
 
         public Text getLabel() {
             return label;
         }
 
         public void setLabel(Text label) {
             this.label = label;
         }
     }
 
     public EventHandler<MouseEvent> getMouseClickedEventHandler() {
         return mouseClickedEventHandler;
     }
 
     public EventHandler<MouseEvent> getMouseReleasedEventHandler() {
         return mouseReleasedEventHandler;
     }
 
     public EventHandler<MouseEvent> getMousePressedEventHandler() {
         return mousePressedEventHandler;
     }
 }
