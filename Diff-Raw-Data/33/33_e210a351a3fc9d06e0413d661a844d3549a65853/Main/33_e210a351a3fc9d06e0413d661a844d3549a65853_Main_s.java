 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.swing.BorderFactory;
 import javax.swing.JApplet;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 
 import org.jgraph.JGraph;
 import org.jgraph.graph.AttributeMap;
 import org.jgraph.graph.DefaultGraphCell;
 import org.jgraph.graph.GraphConstants;
 import org.jgraph.graph.GraphModel;
 import org.jgrapht.Graph;
 import org.jgrapht.demo.JGraphAdapterDemo;
 import org.jgrapht.ext.JGraphModelAdapter;
 import org.jgrapht.graph.SimpleDirectedGraph;
 
 // FIXME: Lots of side effects
 public class Main extends JApplet {
     private static final long serialVersionUID = 3256444702936019250L;
     private static final Dimension DEFAULT_SIZE = new Dimension(900, 900);
     private static int sensorRange = 100;
 
     private JGraph jgraph;
     private Set<Sensor> vertices;
     private MouseAdapter mouseAdapter;
 
     private JGraphTFactory jgraphtFactory;
 
 
     public static void main(String[] args) {
         JGraphAdapterDemo applet = new JGraphAdapterDemo();
         applet.init();
         applet.setBackground(ColorTheme.Black);
 
         initFrame(applet);
     }
 
     @Override
     public void init() {
         super.init();
         Sensor.SetRange(sensorRange); // FIXME
         Sensor.SetAngle(3 * Math.PI / 2);
 
         setupButtons();
         initMouseAdapter();
 
         show(new TransmissionGraphFactory(), new TransmissionGraph(createTestSensors()));
 
         resize(DEFAULT_SIZE);
     }
 
     @Override
     public void paint(Graphics g) {
         super.paint(g);
 
         Graphics jgraphGraphics = jgraph.getGraphics();
 
         for (Sensor sensor : vertices) {
             SensorDrawingUtils.PaintSensorRange(jgraphGraphics, sensor);
         }
     }
 
     public void show(JGraphTFactory jgraphtFactory, Graph<Sensor, SensorEdge> jgrapht) {
         this.vertices = jgrapht.vertexSet();
         this.jgraphtFactory = jgraphtFactory;
         JGraphModelAdapter<Sensor, SensorEdge> jgraphAdapter = (new JGraphConverter()).convertFromJGraphT(jgrapht);
         resetJGraph(jgraphAdapter, vertices);
     }
 
     private void resetJGraph(JGraphModelAdapter<Sensor, SensorEdge> jgraphAdapter, Set<Sensor> vertices) {
         if (jgraph != null) {
             remove(jgraph);
         }
 
         jgraph = createJGraph(jgraphAdapter);
         add(jgraph);
         positionSensorsOnJGraph(jgraphAdapter, vertices);
     }
 
     private JGraph createJGraph(GraphModel model) {
         JGraph jgraph = new JGraph(model);
         jgraph.addMouseListener(mouseAdapter);
         jgraph.setPreferredSize(DEFAULT_SIZE);
         jgraph.setBackground(ColorTheme.Black);
         jgraph.setDragEnabled(false);
         jgraph.setAntiAliased(true);
         jgraph.setDropEnabled(false);
         jgraph.setEditable(false);
         jgraph.setMoveBeyondGraphBounds(false);
 
         return jgraph;
     }
 
     private void updateSensorPositionFromCell(GraphModel model, DefaultGraphCell cell) {
         Sensor sensor = (Sensor) model.getValue(cell);
 
         AttributeMap cellAttributes = cell.getAttributes();
         Rectangle2D cellBounds = GraphConstants.getBounds(cellAttributes);
 
        double offset = SensorDrawingUtils.GetSensorScreenWidth()/2.0;
        sensor.setPosition(cellBounds.getCenterX()+offset, cellBounds.getCenterY()+offset);
     }
 
     private void positionSensorsOnJGraph(JGraphModelAdapter<Sensor, SensorEdge> jgraphAdapter, Set<Sensor> sensors) {
         for (Sensor sensor : sensors) {
             positionSensorOnJgraph(jgraphAdapter, sensor);
         }
     }
 
     private void initMouseAdapter() {
         mouseAdapter = new MouseAdapter() {
 
             @Override
             public void mouseReleased(MouseEvent event) {
                 Object[] cells = jgraph.getSelectionCells().clone();
 
                 if (cells == null)
                     return;
 
                 for (Object cellObject : cells) {
                     updateSensorPositionFromCell(jgraph.getModel(), (DefaultGraphCell) cellObject);
                 }
 
                 show(jgraphtFactory, jgraphtFactory.createGraph(vertices));
                 repaint();
             }
 
             @Override
             public void mouseClicked(MouseEvent event) {
 
             }
         };
     }
 
     @SuppressWarnings("unchecked")
     private void positionSensorOnJgraph(JGraphModelAdapter<Sensor, SensorEdge> jgraphAdapter, Sensor sensor) {
         DefaultGraphCell cell = jgraphAdapter.getVertexCell((Object)sensor);
 
         AttributeMap cellAttributes = cell.getAttributes();
         GraphConstants.setBounds(cellAttributes, SensorDrawingUtils.GetBoundsFromSensor(sensor));
 
         AttributeMap cellAttr = new AttributeMap();
         cellAttr.put(cell, cellAttributes);
         jgraphAdapter.edit(cellAttr, null, null, null);
     }
 
     private void setupButtons() {
         Container c = getContentPane();
         c.setBackground(ColorTheme.Black);
         c.setLayout(new FlowLayout());
         JButton omniButton = makeButton(" Show Omnidirectional Graph ");
 
         omniButton.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                 //TODO: Display the omni-graph
             }
         });
 
         JButton directedButton = makeButton(" Show Directed Antennae Graph ");
         directedButton.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                 //TODO: Display the directed-graph
             }
         });
 
         c.add(omniButton);
         c.add(directedButton);
     }
 
     private JButton makeButton(String label) {
         JButton button = new JButton(label);
         button.setBorder(BorderFactory.createLineBorder(ColorTheme.White, 1));
         button.setBackground(ColorTheme.Black);
         button.setForeground(ColorTheme.White);
         button.setMargin(new Insets(10, 100, 10, 10));
 
         return button;
     }
 
     private static void initFrame(JGraphAdapterDemo applet) {
         JFrame frame = new JFrame();
         frame.getContentPane().add(applet);
         frame.setTitle("JGraphT Adapter to JGraph Demo");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.pack();
         frame.setVisible(true);
         frame.setBackground(ColorTheme.Black);
     }
 
     private Set<Sensor> createTestSensors() {
         Set<Sensor> sensors = new HashSet<Sensor>();
         sensors.add(new Sensor(new Point2D.Double(130, 40)));
         sensors.add(new Sensor(new Point2D.Double(130, 100)));
         sensors.add(new Sensor(new Point2D.Double(190, 100)));
 //        sensors.add(new Sensor(new Point2D.Double(10, 10)));
 //        sensors.add(new Sensor(new Point2D.Double(19, 10)));
 //        sensors.add(new Sensor(new Point2D.Double(57, 63)));
         sensors.add(new Sensor(new Point2D.Double(494, 188)));
         sensors.add(new Sensor(new Point2D.Double(105, 347)));
         sensors.add(new Sensor(new Point2D.Double(265, 656)));
         sensors.add(new Sensor(new Point2D.Double(283, 243)));
         sensors.add(new Sensor(new Point2D.Double(249, 301)));
         sensors.add(new Sensor(new Point2D.Double(373, 571)));
 
         return sensors;
     }
 
 
     private ProximityGraph createTestProximityGraph() {
 
         return new ProximityGraph(createTestSensors());
     }
 
     private SimpleDirectedGraph<Sensor, SensorEdge> createTestDirectedGraph() {
         Sensor a = new Sensor(new Point2D.Double(130, 40));
         Sensor b = new Sensor(new Point2D.Double(130, 100));
         Sensor c = new Sensor(new Point2D.Double(190, 100));
 
         SimpleDirectedGraph<Sensor, SensorEdge> graph = new SimpleDirectedGraph<Sensor, SensorEdge>(new SensorEdgeFactory());
         graph.addVertex(a);
         graph.addVertex(b);
         graph.addVertex(c);
         graph.addEdge(a, b);
         graph.addEdge(c, b);
 
         return graph;
     }
 }
