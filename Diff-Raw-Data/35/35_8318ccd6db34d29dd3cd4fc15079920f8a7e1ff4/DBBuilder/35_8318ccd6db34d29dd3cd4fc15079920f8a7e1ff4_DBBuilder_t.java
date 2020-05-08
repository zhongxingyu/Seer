 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package graphdrawer;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.IOException;
 import java.sql.*;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Set;
 import org.gephi.io.database.drivers.PostgreSQLDriver;
 
 import org.gephi.data.attributes.api.AttributeController;
 import org.gephi.data.attributes.api.AttributeModel;
 import org.gephi.graph.api.*;
 import org.gephi.io.database.drivers.SQLDriver;
 import org.gephi.io.database.drivers.SQLUtils;
 import org.gephi.io.exporter.api.ExportController;
 import org.gephi.io.exporter.preview.PNGExporter;
 import org.gephi.io.exporter.preview.SVGExporter;
 import org.gephi.io.importer.api.Container;
 import org.gephi.io.importer.api.EdgeDefault;
 import org.gephi.io.importer.api.ImportController;
 import org.gephi.io.importer.plugin.database.EdgeListDatabaseImpl;
 import org.gephi.io.importer.plugin.database.ImporterEdgeList;
 import org.gephi.io.processor.plugin.DefaultProcessor;
 import org.gephi.layout.plugin.force.StepDisplacement;
 import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
 import org.gephi.preview.api.PreviewController;
 import org.gephi.preview.api.PreviewModel;
 import org.gephi.preview.api.PreviewProperty;
 import org.gephi.preview.types.EdgeColor;
 import org.gephi.preview.types.DependantOriginalColor;
 import org.gephi.project.api.ProjectController;
 import org.gephi.project.api.Workspace;
 import org.openide.util.Lookup;
 
 /**
  *
  * @author sasho
  */
 public class DBBuilder {
     
     private static final String DBNAME = "knowevo";
     private static final String DBUSER = "knowevo";
     private static final String PW = "12345c7890";
     private static final String HOST = "localhost";
     private static final int PORT = 5432;
     private static final SQLDriver SD = new PostgreSQLDriver();
     private static final String URL = SQLUtils.getUrl(SD, HOST, PORT, DBNAME);
     
     private Connection conn;
     private ProjectController pc;
     private Workspace workspace;
 
     private GraphModel graphModel;
     private AttributeModel attributeModel;
     private Graph graph;
     
     private SVGExporter exporter;
     private ExportController ec;
 
     private ScoreMachine smach;
     
     private PreviewModel previewModel;
     
     public static void getGraphFor(String name, int max_depth, String out) 
 	throws SQLException
     {
         DBBuilder dbb = new DBBuilder();
         dbb.buildGraph(name, max_depth);
 	dbb.converge(100);
         dbb.export(out);
     }
     
     private DBBuilder() {
         try {
             conn = SD.getConnection(URL, DBUSER, PW);
             
             pc = Lookup.getDefault().lookup(ProjectController.class);
             pc.newProject();
             workspace = pc.getCurrentWorkspace();
 
             //Get controllers and models
 	    graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
             attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
             
             graph = graphModel.getDirectedGraph();
            
             ec = Lookup.getDefault().lookup(ExportController.class);
             exporter = (SVGExporter) ec.getExporter("svg"); 
             exporter.setWorkspace(workspace);
 
 	    previewModel = Lookup.getDefault().lookup(PreviewController.class).getModel();
 
	    smach = new SimpleCategoryScoreMachine(conn);
         }
         catch (Exception e) {
             e.printStackTrace();
         }
     }
     
     private void export(String name) {
          try {
             ec.exportFile(new File(name), exporter);
         } catch (IOException ex) {
             ex.printStackTrace();
             return;
         }
     }
     
     public static ResultSet getQuery(String query, String[] args, Connection conn) 
 	throws SQLException
     {
 	PreparedStatement s = conn.prepareStatement(query);
         for (int i = 0; i < args.length; i++)
             s.setString(i+1, args[i]);
 	return s.executeQuery();
     }
 
     public ResultSet getQuery(String query, String[] args) 
 	throws SQLException
     {
 	return DBBuilder.getQuery(query, args, this.conn);
     }
 
     public void converge(int times) {
 
 	YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
         layout.setGraphModel(graphModel);
         layout.resetPropertiesValues();
         layout.setOptimalDistance(30f);
 
         layout.initAlgo();
         for (int i = 0; i < times && layout.canAlgo(); i++) {
             layout.goAlgo();
         }
         
         //Preview
         previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
         previewModel.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.BLUE));
         previewModel.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, new Float(0.1f));
 	previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.RED));
         previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT, previewModel.getProperties().getFontValue(PreviewProperty.NODE_LABEL_FONT).deriveFont(14));
     }
       
     private void buildGraph(String name, int max_depth) 
 	throws SQLException
     {
         Queue<Neighbor> queue = new LinkedList<Neighbor>();
        
 	ResultSet rs = this.getQuery("select g.name from "+
 				     " gravebook_article g "+
                     		     "where g.name = ?",
                                       new String[] {name} );
 	
 	if (rs.next()) 
 	    queue.add(new Neighbor(rs.getString("name"), 0));
 	
 	buildGraph(queue, max_depth);    
     }
     
     private void buildGraph(Queue<Neighbor> queue, int max_depth) 
 	throws SQLException
     {
         Set<String> seen = new HashSet<String>();
         Neighbor curr;
         Node parent;
 	Node child;
 	Edge edge;
 	String str;
 	
         while(!queue.isEmpty()) {
             curr = queue.remove();
             if (curr.depth > max_depth)
                 break;
             
 	    seen.add(curr.name);
 	    parent = getOrCreateNode(curr.name);
 	    
             ResultSet rs = getQuery("select to_article_id " +
 				    "from gravebook_article_people " +
 				    "where from_article_id = ?",
                                     new String[] {curr.name});
 
 	    while (rs.next()) {
 		str = rs.getString("to_article_id");
 		//System.out.println(str);
 
 		child = getOrCreateNode(str);
 		if (!seen.contains(str)) {
 		    seen.add(str);
 		    queue.offer(new Neighbor(str, curr.depth+1));
 		}
 		
 		float score = smach.getScore(parent, child);
 		edge = graphModel.factory().newEdge(parent, child, score, true);
 		graph.addEdge(edge);
 	    }
 	    
         }
     }
     
     private Node getOrCreateNode(String name) {
 	Node n = graph.getNode(name);
 	if (n == null) {
 	    n = graphModel.factory().newNode(name);
 	    n.getNodeData().setLabel(name);
 	    graph.addNode(n);
 	}
         return n;
     }
     
     private class Neighbor {
         String name;
         int depth;
         
         public Neighbor(String n, int d) {
             name = n;
             depth = d;
         }
     }
     
     public static void buildData() {
         //Init a project - and therefore a workspace
         ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
         pc.newProject();
         Workspace workspace = pc.getCurrentWorkspace();
         PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();
 
         //Get controllers and models
         ImportController importController = Lookup.getDefault().lookup(ImportController.class);
         GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
         AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
 
         //Import database
         EdgeListDatabaseImpl db = new EdgeListDatabaseImpl();
         db.setDBName("knowevo");
         db.setHost("localhost");
         db.setUsername("knowevo");
         db.setPasswd("12345c7890");
         db.setSQLDriver(new PostgreSQLDriver());
         db.setPort(5432);
         
         db.setNodeQuery("SELECT name as id, name as label from gravebook_article");
         db.setEdgeQuery("SELECT u.name as source, v.name as target, p.id as label,"+
                     "0.0 as weight "+
                     "from gravebook_article u, gravebook_article v,  gravebook_article_people p " +
                     "where p.from_article_id = u.name and p.to_article_id = v.name ");
         ImporterEdgeList edgeListImporter = new ImporterEdgeList();
         Container container = importController.importDatabase(db, edgeListImporter);
         container.setAllowAutoNode(false);      //Don't create missing nodes
         container.getLoader().setEdgeDefault(EdgeDefault.UNDIRECTED);   //Force UNDIRECTED
 
         //Append imported data to GraphAPI
         importController.process(container, new DefaultProcessor(), workspace);
 
         //See if graph is well imported
         UndirectedGraph graph = graphModel.getUndirectedGraph();
         System.out.println("Nodes: " + graph.getNodeCount());
         System.out.println("Edges: " + graph.getEdgeCount());
         
         YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
         layout.setGraphModel(graphModel);
         layout.resetPropertiesValues();
         layout.setOptimalDistance(200f);
 
         layout.initAlgo();
         for (int i = 0; i < 100 && layout.canAlgo(); i++) {
             layout.goAlgo();
         }
         
         //Preview
         model.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
         model.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.GRAY));
         model.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, new Float(0.1f));
         model.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT, model.getProperties().getFontValue(PreviewProperty.NODE_LABEL_FONT).deriveFont(11));
 
         //Export
         ExportController ec = Lookup.getDefault().lookup(ExportController.class);
         try {
             ec.exportFile(new File("test.pdf"));
         } catch (IOException ex) {
             ex.printStackTrace();
             return;
         }
         
 //        String url = SQLUtils.getUrl(db.getSQLDriver(), db.getHost(), db.getPort(), db.getDBName());
 //        Connection connection = null;
 //        try {
 //            //System.err.println("Try to connect at " + url);
 //            connection = db.getSQLDriver().getConnection(url, db.getUsername(), db.getPasswd());
 //            System.err.println("Database connection established");
 //        } catch (SQLException ex) {
 //            if (connection != null) {
 //                try {
 //                    connection.close();
 //                    System.err.println("Database connection terminated");
 //                } catch (Exception e) { /* ignore close errors */ }
 //            }
 //            System.err.println("Failed to connect at " + url);
 //            ex.printStackTrace(System.err);
 //        }
 //        if (connection == null) {
 //            System.err.println("Failed to connect at " + url);
 //        }
 //        
 //        //Close connection
 //        if (connection != null) {
 //            try {
 //                connection.close();
 //                //System.err.println("Database connection terminated");
 //            } catch (Exception e) { /* ignore close errors */ }
 //        }
     }
 
     public abstract class ScoreMachine {
 
 	private Connection smconn;
 
 	public ScoreMachine(Connection c) {
 	    smconn = c;
 	}
 
 	public Connection getConn() {
 	    return smconn;
 	}
 	
 	public abstract float getScore(Node u, Node v) throws SQLException;
     }
     
     public class CategoryScoreMachine extends ScoreMachine {
 	
 	public CategoryScoreMachine(Connection c) {
 	    super(c);
 	}
 	
 	@Override
 	public float getScore(Node u, Node v)
 	    throws SQLException 
 	{
 	    float res = 0;
 	    String un = u.getNodeData().getId();
 	    String vn = v.getNodeData().getId();
 	    
            ResultSet rs = 
 		DBBuilder.getQuery("select c.size "+ 
 				   "from gravebook_category c "+
 				   "where c.name in "+
 				   "(select ga.category_id from gravebook_article_categories ga "+
 				   "where ga.article_id = ?) "+
 				   "and c.name in "+
 				   "(select gb.category_id from gravebook_article_categories gb "+
 				   "where gb.article_id = ?) ",
                                    new String[] {un, vn},
 				   getConn());
 	    
 	    while (rs.next()) 
 		res += 10.0 / rs.getInt("size");
 		
 	    return res;
 	}
     }
    
    public class SimpleCategoryScoreMachine extends ScoreMachine {
	
	public SimpleCategoryScoreMachine(Connection c) {
	    super(c);
	}
	
	@Override
	public float getScore(Node u, Node v)
	    throws SQLException 
	{
	    float res = 0;
	    String un = u.getNodeData().getId();
	    String vn = v.getNodeData().getId();
	    
            ResultSet rs = 
		DBBuilder.getQuery("select count(category_id) as count "+ 
				   "from gravebook_article_categories "+
				   "where article_id = ? and article_id = ? ",
                                   new String[] {un, vn},
				   getConn());
	    
	  
	    if (rs.next()) 
		res += rs.getInt("count");
		
	    return res;
	}
    }
 }
