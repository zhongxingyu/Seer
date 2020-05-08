 /*
  * EDACCParameterGraphEditor.java
  *
  * Created on 02.07.2011, 17:44:44
  */
 package edacc.parametergrapheditor;
 
 import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
 import com.mxgraph.layout.mxIGraphLayout;
 import com.mxgraph.model.mxCell;
 import com.mxgraph.model.mxGraphModel;
 import com.mxgraph.swing.mxGraphComponent;
 import com.mxgraph.swing.util.mxMorphing;
 import com.mxgraph.util.mxEvent;
 import com.mxgraph.util.mxEventObject;
 import com.mxgraph.util.mxEventSource;
 import com.mxgraph.util.mxEventSource.mxIEventListener;
 import com.mxgraph.view.mxGraph;
 import com.mxgraph.view.mxMultiplicity;
 import edacc.EDACCApp;
 import edacc.model.DatabaseConnector;
 import edacc.model.ParameterGraphDAO;
 import edacc.model.Solver;
 import edacc.model.SolverDAO;
 import edacc.parameterspace.Parameter;
 import edacc.parameterspace.domain.*;
 import edacc.parameterspace.graph.AndNode;
 import edacc.parameterspace.graph.Edge;
 import edacc.parameterspace.graph.Node;
 import edacc.parameterspace.graph.OrNode;
 import edacc.parameterspace.graph.ParameterGraph;
 import java.awt.Dimension;
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 import javax.swing.SwingConstants;
 import javax.xml.bind.JAXBException;
 
 /**
  *
  * @author simon
  */
 public class ParameterGraphEditor extends javax.swing.JDialog {
 
     mxGraph graph;
     mxGraphComponent graphComponent;
     Solver solver;
     mxCell rootNode;
 
     /** Creates new form EDACCParameterGraphEditor */
     public ParameterGraphEditor(java.awt.Frame parent, boolean modal, Solver solver) throws SQLException, JAXBException {
         super(parent, modal);
         initComponents();
         this.solver = solver;
 
         graph = new mxGraph() {
 
             @Override
             public boolean isCellEditable(Object o) {
                 return false;
             }
 
             @Override
             public String convertValueToString(Object o) {
                 if (o instanceof mxCell) {
                     mxCell cell = (mxCell) o;
                     if (cell.isEdge()) {
                         if (cell.getValue() == null || cell.getValue() instanceof String) {
                             return "";
                         } else {
                             Edge edge = (Edge) cell.getValue();
                             if (edge.getGroup() == 0) {
                                 return "";
                             } else {
                                 return "" + edge.getGroup();
                             }
                         }
                     } else if (cell.isVertex()) {
                         if (cell.getValue() instanceof AndNode) {
                             AndNode node = (AndNode) cell.getValue();
                             if (node.getDomain() == null) {
                                 return "AND";
                             }
                             return "AND\n" + domainToString(node.getDomain());
                         } else if (cell.getValue() instanceof OrNode) {
                             OrNode node = (OrNode) cell.getValue();
                             if (node == null || node.getParameter() == null) {
                                 // TODO: fix
                                 // Parameter is null while moving cell, don't know why
                                 return "";
                             }
                            return "OR\n" + node.getParameter().getName() + "\n" + domainToString(node.getParameter().getDomain());
                         }
                     } else {
                         return super.convertValueToString(o);
                     }
                 } else {
                     return super.convertValueToString(o);
                 }
                 return "None";
             }
         };
 
         /*          graph.setModel(new mxGraphModel() {
         
         @Override
         public Object add(Object o, Object o1, int i) {
         return super.add(o, o1, i);
         }
         
         @Override
         protected void cellAdded(Object o) {
         super.cellAdded(o);
         System.out.println("CELL ADDED " + o.getClass());
         if (o instanceof mxCell) {
         mxCell cell = (mxCell) o;
         if (cell.isEdge()) {
         System.out.println("Edge added " + cell.getSource());
         // if ("AND".equals(cell.get)) {
         //      System.out.println("SOURCE AND");
         // }
         } else if (cell.isVertex()) {
         System.out.println("Vertex added");
         }
         }
         
         }
         
         
         
         @Override
         protected void cellRemoved(Object o) {
         super.cellRemoved(o);
         }
         
         @Override
         public void updateEdgeParent(Object o, Object o1) {
         System.out.println("UPDATE EDGE PARENT " + o + " -> " + o1);
         super.updateEdgeParent(o, o1);
         }
         
         @Override
         public void updateEdgeParents(Object o) {
         super.updateEdgeParents(o);
         }
         
         @Override
         public void updateEdgeParents(Object o, Object o1) {
         super.updateEdgeParents(o, o1);
         }
         
         
         
         
         
         });*/
         //graph.insertVertex(graph.getDefaultParent(),null, "Hello", 20, 20, 80, 30);
 
         graphComponent = new mxGraphComponent(graph);
 
         graphComponent.addKeyListener(new KeyAdapter() {
 
             @Override
             public void keyReleased(KeyEvent e) {
                 ParameterGraphEditor.this.evtKeyReleased(e);
             }
         });
 
         jScrollPane1.setViewportView(graphComponent);
 
         // graphComponent.getGraphControl().setComponentPopupMenu(jPopupMenu1);
         graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
 
             @Override
             public void mouseClicked(MouseEvent e) {
                 if (e.getClickCount() == 2) {
                     System.out.println("Edit");
                     Object o = graph.getSelectionCell();
                     if (o instanceof mxCell) {
                         mxCell cell = (mxCell) o;
                         if (cell.isEdge()) {
                             System.out.println("Edge: " + cell + " - " + cell.getSource() + " -> " + cell.getTarget());
                         } else if (cell.isVertex()) {
                             System.out.println("Vertex: " + cell);
                         }
                     }
                 }
             }
         });
 
         graphComponent.getGraph().getModel().addListener(null, new mxEventSource.mxIEventListener() {
 
             @Override
             public void invoke(Object o, mxEventObject eo) {
                 if ("change".equals(eo.getName())) {
                     for (Object oj : eo.getProperties().values()) {
                         if (oj instanceof ArrayList) {
                             ArrayList list = (ArrayList) oj;
                             for (Object object : list) {
                                 if (object instanceof mxGraphModel.mxChildChange) {
                                     mxGraphModel.mxChildChange ch = (mxGraphModel.mxChildChange) object;
 
                                     // parent is null if removed, we don't want to show a dialog when removing edges
                                     if (ch.getChild() instanceof mxCell && ch.getParent() != null) {
                                         mxCell child = (mxCell) ch.getChild();
 
                                         if (child.isEdge()) {
                                             if (child.getTarget() == null && child.getSource() != null) {
                                                 Point mouse = MouseInfo.getPointerInfo().getLocation();
                                                 Point graphLocation = graphComponent.getLocationOnScreen();
 
                                                 try {
                                                     ICreateNodeDialog dialog = null;
                                                     Parameter parameter = null;
                                                     if (child.getSource().getValue() instanceof AndNode) {
                                                         dialog = new CreateOrNodeDialog(new javax.swing.JFrame(), true, ParameterGraphEditor.this.solver);
                                                     } else {
                                                         OrNode node = (OrNode) child.getSource().getValue();
                                                         parameter = node.getParameter();
                                                        dialog = new CreateAndNodeDialog(new javax.swing.JFrame(), true, node.getParameter(), node.getParameter().getDomain());
                                                     }
                                                     Domain domain = null;
                                                     ((JDialog) dialog).setLocationRelativeTo(ParameterGraphEditor.this);
                                                     ((JDialog) dialog).setSize(new Dimension(800, 600));
                                                     ((JDialog) dialog).setVisible(true);
                                                     while (!dialog.isCancelled() && domain == null) {
                                                         try {
                                                             domain = dialog.getDomain();
                                                             break;
                                                         } catch (InvalidDomainException ex) {
                                                             JOptionPane.showMessageDialog(ParameterGraphEditor.this, ex.getMessage(), "Invalid Domain", JOptionPane.ERROR_MESSAGE);
                                                             ((JDialog) dialog).setVisible(true);
                                                         }
                                                     }
                                                     graph.getModel().beginUpdate();
                                                     if (domain != null) {
                                                         Object node = null;
                                                         if (parameter == null) {
                                                            node = new OrNode(new Parameter(dialog.getParameterName(), domain));
                                                         } else {
                                                             node = new AndNode(parameter, domain);
                                                         }
                                                         mxCell vertex = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, node, mouse.x - graphLocation.x, mouse.y - graphLocation.y, 80, 30, null);
                                                         graph.updateCellSize(vertex);
                                                         graph.insertEdge(graph.getDefaultParent(),
                                                                 null, "",
                                                                 child.getSource(),
                                                                 vertex);
                                                         // child.setTarget((mxCell) graph.insertVertex(graph.getDefaultParent(), null, node, mouse.x - graphLocation.x, mouse.y - graphLocation.y, 80, 30, null));
                                                         // child.setParent(child.getTarget());
                                                         //  ((mxGraphModel) graph.getModel()).updateEdgeParents(graph.getDefaultParent());
 
                                                     }
                                                     graph.getModel().remove(child);
                                                     graph.getModel().endUpdate();
                                                     graph.setSelectionCell(null);
                                                     graph.refresh();
 
                                                 } catch (Exception ex) {
                                                     graph.getModel().remove(child);
                                                     graph.setSelectionCell(null);
                                                     graph.refresh();
                                                     ex.printStackTrace();
                                                 }
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         });
 
         mxMultiplicity[] multiplicities = new mxMultiplicity[1];
 
         multiplicities[0] = new mxMultiplicity(true, null, null, null, 0,
                 "n", null,
                 "Can't connect nodes.",
                 "", true) {
 
             @Override
             public String check(mxGraph mxgrph, Object edge, Object source, Object target, int sourceOut, int targetIn) {
                 if (!(source instanceof mxCell) && !(target instanceof mxCell)) {
                     return "Can't connect nodes.";
                 }
                 mxCell s = (mxCell) source;
                 mxCell t = (mxCell) target;
                 if (s.getValue() instanceof AndNode && t.getValue() instanceof AndNode) {
                     return "Can't connect AND-node to AND-node.";
                 }
                 if (s.getValue() instanceof OrNode && t.getValue() instanceof OrNode) {
                     return "Can't connect OR-node to OR-node";
                 }
 
                 List<mxCell> roots = findRoots();
                 boolean hasCycle = false;
                 for (int i = 0; i < roots.size(); i++) {
                     hasCycle |= hasCycle(roots.get(i), new HashSet<String>(), new HashSet<String>(), (mxCell) target);
                     if (hasCycle) {
                         break;
                     }
                 }
                 if (hasCycle) {
                     return "Can't connect nodes. Graph cannot have cycles.";
                 }
                 return null;
             }
         };
 
         graph.setMultiplicities(multiplicities);
         graph.setMultigraph(false);
         //  graph.setAllowDanglingEdges(false);
         graph.setCellsDisconnectable(false);
         graph.setCellsDeletable(true);
         graph.setCellsCloneable(false);
         graph.setAllowNegativeCoordinates(false);
         
         ParameterGraph parameterGraph = ParameterGraphDAO.loadParameterGraph(solver);
         if (parameterGraph == null) {
             // create root node:
             graph.updateCellSize(rootNode = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, new AndNode(null, null), 20, 20, 80, 30));
         } else {
             graph.getModel().beginUpdate();
             HashMap<String, mxCell> mapNodeIdToCell = new HashMap<String, mxCell>();
             for (Node node : parameterGraph.nodes) {
                 mxCell cell = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, node, 20, 20, 80, 30);
                 if (node == parameterGraph.startNode) {
                     rootNode = cell;
                 }
                 graph.updateCellSize(cell);
                 mapNodeIdToCell.put(node.getId(), cell);
             }
             
             for (Edge edge : parameterGraph.edges) {
                 graph.insertEdge(graph.getDefaultParent(), null, edge, mapNodeIdToCell.get(edge.getSource().getId()), mapNodeIdToCell.get(edge.getTarget().getId()));
             }
             
             graph.getModel().endUpdate();
             
             btnLayoutActionPerformed(null);
         }
         
     }
 
     private String domainToString(Domain domain) {
         if (domain instanceof CategoricalDomain) {
             return "Categorical";
         } else if (domain instanceof FlagDomain) {
             FlagDomain flag = (FlagDomain) domain;
             if (flag.contains(FlagDomain.FLAGS.ON) && (flag.contains(FlagDomain.FLAGS.OFF))) {
                 return "Flag [On, Off]";
             } else if (flag.contains(FlagDomain.FLAGS.ON)) {
                 return "Flag [On]";
             } else {
                 return "Flag [Off]";
             }
         } else if (domain instanceof IntegerDomain) {
             IntegerDomain integer = (IntegerDomain) domain;
             return "Integer [" + integer.getLow() + ", " + integer.getHigh() + "]";
         } else if (domain instanceof MixedDomain) {
             MixedDomain mixed = (MixedDomain) domain;
             String res = "";
             for (int d = 0; d < mixed.getDomains().size(); d++) {
                 res += domainToString(mixed.getDomains().get(d));
                 if (d != mixed.getDomains().size() - 1) {
                     res += "\n";
                 }
             }
             return res;
         } else if (domain instanceof OptionalDomain) {
             return "Optional";
         } else if (domain instanceof OrdinalDomain) {
             return "Ordinal";
         } else if (domain instanceof RealDomain) {
             RealDomain real = (RealDomain) domain;
             return "Real [" + real.getLow() + ", " + real.getHigh() + "]";
         } else {
             return "Unknown";
         }
     }
 
     public boolean hasCycle(mxCell startNode, Set<String> gray, Set<String> black, mxCell defaultTarget) {
         if (gray.contains(startNode.getId())) {
             return true;
         } else if (!black.contains(startNode.getId())) {
             gray.add(startNode.getId());
             Object[] edges = graph.getOutgoingEdges(startNode);
             for (Object o : edges) {
                 mxCell edge = (mxCell) o;
                 mxCell target = (edge.getTarget() != null ? (mxCell) edge.getTarget() : defaultTarget);
                 if (target != null && hasCycle(target, gray, black, defaultTarget)) {
                     return true;
                 }
             }
             black.add(startNode.getId());
             gray.remove(startNode.getId());
         }
         return false;
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jPanel1 = new javax.swing.JPanel();
         btnLayout = new javax.swing.JButton();
         btnSave = new javax.swing.JButton();
         jScrollPane1 = new javax.swing.JScrollPane();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(edacc.EDACCApp.class).getContext().getResourceMap(ParameterGraphEditor.class);
         setTitle(resourceMap.getString("Form.title")); // NOI18N
         setName("Form"); // NOI18N
 
         jPanel1.setName("jPanel1"); // NOI18N
 
         btnLayout.setText(resourceMap.getString("btnLayout.text")); // NOI18N
         btnLayout.setName("btnLayout"); // NOI18N
         btnLayout.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnLayoutActionPerformed(evt);
             }
         });
 
         btnSave.setText(resourceMap.getString("btnSave.text")); // NOI18N
         btnSave.setName("btnSave"); // NOI18N
         btnSave.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSaveActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(btnLayout)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 573, Short.MAX_VALUE)
                 .addComponent(btnSave)
                 .addContainerGap())
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnLayout)
                     .addComponent(btnSave))
                 .addContainerGap(14, Short.MAX_VALUE))
         );
 
         jScrollPane1.setName("jScrollPane1"); // NOI18N
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 715, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnLayoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLayoutActionPerformed
         mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
         layout.setInterRankCellSpacing(25);
        // layout.setFixRoots(true);
         Object cell = graph.getSelectionCell();
 
         if (cell == null
                 || graph.getModel().getChildCount(cell) == 0) {
             cell = graph.getDefaultParent();
         }
 
         graph.getModel().beginUpdate();
         try {
             layout.execute(cell);
         } finally {
             mxMorphing morph = new mxMorphing(graphComponent, 20,
                     1.2, 20);
 
             morph.addListener(mxEvent.DONE, new mxIEventListener() {
 
                 @Override
                 public void invoke(Object sender, mxEventObject evt) {
                     graph.getModel().endUpdate();
                 }
             });
 
             morph.startAnimation();
         }
     }//GEN-LAST:event_btnLayoutActionPerformed
 
     public List<mxCell> findRoots() {
         Object[] cells = graph.getChildCells(graph.getDefaultParent());
         ArrayList<mxCell> roots = new ArrayList<mxCell>();
         for (Object o : cells) {
             if (o instanceof mxCell) {
                 mxCell cell = (mxCell) o;
                 if (cell.isVertex() && graph.getIncomingEdges(cell).length == 0) {
                     roots.add(cell);
                 }
             }
         }
         return roots;
     }
 
     private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
 
         List<mxCell> roots = findRoots();
         
         if (roots.size() != 1) {
             JOptionPane.showMessageDialog(this, "Found " + roots.size() + " roots.", "Error", JOptionPane.ERROR_MESSAGE);
             return;
         }
         mxCell root = (mxCell) roots.get(0);
         if (!(root.getValue() instanceof AndNode) || root != rootNode) {
             // should never happen
             JOptionPane.showMessageDialog(this, "Invalid root node.", "Error", JOptionPane.ERROR_MESSAGE);
             return;
         }
         Set<Node> nodes = new HashSet<Node>();
         List<Edge> edges = new LinkedList<Edge>();
         Set<Parameter> parameters = new HashSet<Parameter>();
         if (!buildParameterGraph(nodes, edges, parameters, root)) {
             // happens if not all leafs are AND nodes
             JOptionPane.showMessageDialog(this, "Invalid graph.", "Error", JOptionPane.ERROR_MESSAGE);
             return ;
         }
         ParameterGraph parameterGraph = new ParameterGraph(nodes, edges, parameters, (AndNode) root.getValue());
         try {
             ParameterGraphDAO.saveParameterGraph(parameterGraph, solver);
             JOptionPane.showMessageDialog(this, "Saved.", "Information", JOptionPane.INFORMATION_MESSAGE);
         } catch (Exception ex) {
             JOptionPane.showMessageDialog(this, "Error while saving:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
         }
         
     }//GEN-LAST:event_btnSaveActionPerformed
 
     public void evtKeyReleased(KeyEvent e) {
         if (e.getKeyCode() == KeyEvent.VK_DELETE) {
             for (Object cell : graph.getSelectionCells()) {
                 // can't remove root node
                 if (cell != rootNode) {
                     graph.removeCells(new Object[] {cell});
                 }
             }
             e.consume();
         }
     }
 
     public boolean buildParameterGraph(Set<Node> nodes, List<Edge> edges, Set<Parameter> parameters, mxCell currentCell) {
         if (currentCell.isVertex()) {
             Node node = (Node) currentCell.getValue();
             node.setId(currentCell.getId());
             nodes.add(node);
             if (node instanceof OrNode) {
                 if (graph.getOutgoingEdges(currentCell).length == 0) {
                     return false;
                 }
                 if (((OrNode) node).getParameter() != null) {
                 parameters.add(((OrNode) node).getParameter());
                 }
             }
             for (Object o : graph.getOutgoingEdges(currentCell)) {
                 mxCell cell = (mxCell) o;
                 if (cell.getValue() == null || cell.getValue() instanceof String) {
                     edges.add(new Edge(node, (Node) cell.getTarget().getValue(), 0));
                 } else {
                     Edge edge = (Edge) cell.getValue();
                     edge.setSource(node);
                     edge.setTarget((Node) cell.getTarget().getValue());
                     edges.add(edge);
                 }
                 if (!buildParameterGraph(nodes, edges, parameters, (mxCell) cell.getTarget())) {
                     return false;
                 }
             }
         }
         return true;
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnLayout;
     private javax.swing.JButton btnSave;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JScrollPane jScrollPane1;
     // End of variables declaration//GEN-END:variables
 }
