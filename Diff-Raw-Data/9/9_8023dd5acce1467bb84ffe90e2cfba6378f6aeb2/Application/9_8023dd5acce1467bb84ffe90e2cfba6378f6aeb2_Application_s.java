 package controllers;
 
 import javax.xml.bind.JAXBException;
 
 import edu.cmu.jung.Edge;
 import edu.cmu.jung.UserCoAuthorSubgraph;
 import edu.uci.ics.jung.algorithms.layout.Layout;
 import play.*;
 import play.data.*;
 import play.data.Form.*;
 import play.data.validation.Constraints.Required;
 import play.libs.Json;
 import play.mvc.*;
 import views.html.*;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Paint;
 import java.awt.Stroke;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Queue;
 import java.util.Scanner;
 
 import javax.swing.JFrame;
 import javax.xml.bind.JAXBException;
 
 import org.apache.commons.collections15.Transformer;
 
 import edu.cmu.DBLPProcessor.Coauthorship;
 import edu.cmu.DBLPProcessor.DBLPParser;
 import edu.cmu.DBLPProcessor.DBLPUser;
 import edu.cmu.dataset.DBLPDataSource;
 import edu.cmu.dataset.DatasetInterface;
 import edu.cmu.jung.Node;
 import edu.uci.ics.jung.algorithms.layout.CircleLayout;
 import edu.uci.ics.jung.algorithms.layout.Layout;
 import edu.uci.ics.jung.graph.Graph;
 import edu.uci.ics.jung.graph.SparseMultigraph;
 import edu.uci.ics.jung.graph.util.EdgeType;
 import edu.uci.ics.jung.visualization.VisualizationViewer;
 import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
 import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
 
 public class Application extends Controller {
 
     public static Result index() {
         return ok(index.render(""));
     }
     
     public static class Show {
     	@Required public String name;
     	@Required public Integer level;
     }
     
     public static String myGraph(String name, Integer level) throws JAXBException {
     	UserCoAuthorSubgraph myApp = new UserCoAuthorSubgraph();
 		String result;
 		
 		result = myApp.constructGraph(name,level);;
 		
 		//System.out.println(result);
 				
 		// This builds the graph
 		Layout<Node, Edge> layout = new CircleLayout<Node, Edge>(myApp.g);
 		layout.setSize(new Dimension(650,650));
 		VisualizationViewer<Node, Edge> vv = new VisualizationViewer<Node, Edge>(layout);
 		vv.setPreferredSize(new Dimension(700,700));       
 
 		// Setup up a new vertex to paint transformer...
 		Transformer<Node,Paint> vertexPaint = new Transformer<Node,Paint>() {
 			public Paint transform(Node e) {
 				if(e.getLevel() == 0)
 					return Color.GREEN;
 				else
 					return Color.RED;
 			}
 		};  
 
 		// Set up a new stroke Transformer for the edges
 		float dash[] = {10.0f};
 		final Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
 				BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
 				Transformer<Edge, Stroke> edgeStrokeTransformer = new Transformer<Edge, Stroke>() {
 					public Stroke transform(Edge e) {
 						return edgeStroke;
 					}
 				};	
 
 		vv.setVertexToolTipTransformer(new Transformer<Node, String>() {
 			public String transform(Node e) {
 				return "Name: " + e.getUser().getName() ;
 			}
 		});
 		
 		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
 		//vv.getRenderContext().setEdgeArrowStrokeTransformer(edgeStroke);
 		vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);        
 
 		JFrame frame = new JFrame("Co-authorship Graph View");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().add(vv);
 		frame.pack();
 		frame.setVisible(true);     
 
 		return result;
     	
     }
    public static Result formSubmit(String name, Integer level) throws JAXBException{
     	Form<Show> form = Form.form(Show.class).bindFromRequest();
     	if(form.hasErrors()) {
             return badRequest(index.render("Errors in form"));
         } else {
             Show data = form.get();
             return ok(
                 show.render(myGraph(data.name, data.level))
             		
             );
         }
     }
     
     public static Result myShow(String name, Integer level) throws JAXBException{
             return ok(
                 show.render(myGraph(name, level))
             );
         }
    // }
 }
