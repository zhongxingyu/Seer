 package com.admin.servlet;
 
 import java.io.IOException;
import java.io.PrintWriter;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.admin.owlmanager.OntologyManager;
 
 /**
  * Servlet implementation class AddAnnotationServlet
  */
 @WebServlet("/AddAnnotationServlet")
 public class AddAnnotationServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	
 	private static final String THESIS_PREFIX = "http://localhost/ontologies/ThesisOntology.owl#";
	private static final String RDF_PREFIX = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public AddAnnotationServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		this.doPost(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String annotationName = THESIS_PREFIX + request.getParameter("annotation");
 		String className = request.getParameter("class");
 		
 		OntologyManager manager = new OntologyManager();
 		boolean annotationAdd = true;
 		
 		if (!className.equals("noselect")) {
 			annotationAdd = annotationAdd && manager.addAnnotationURI(annotationName, className);
 		} else {
 			annotationAdd = false;
 		}
 		if (annotationAdd) {
 			if (className.endsWith("Concept")) {
 				String topic = request.getParameter("in-topic");
 				String relationType = request.getParameter("conceptrelation");
 				String relatedConcept = null;
 				if (!relationType.equals("noselect")) {
 					relatedConcept = request.getParameter("related");
 					annotationAdd = annotationAdd && manager.addTriplet(annotationName, THESIS_PREFIX + relationType, relatedConcept);
 				}
 				if ( (!topic.equals("noselect")) && (annotationAdd) ) {
 					annotationAdd = annotationAdd && manager.addTriplet(annotationName, THESIS_PREFIX + "in-topic", topic);
 				} else {
					PrintWriter writer = response.getWriter();
					writer.write("There were errors adding the annotation");
 					manager.deleteResource(annotationName);
					writer.close();
 				}
 			} else {
 				Map<String, String[]> values = request.getParameterMap();
 				Set<String> keySet = values.keySet();
 				Iterator<String> iter = keySet.iterator();
 				while (iter.hasNext()) {
 					String currentKey = iter.next();
 					if ( (!currentKey.equals("class")) && (!currentKey.equals("annotation")) ) {
 						String relation = THESIS_PREFIX + currentKey;
 						String[] objects = values.get(currentKey);
 						for (int i = 0; i < objects.length; i++) {
 							if (!objects[i].equals("noselect")) {
 								if (annotationAdd) {
 									annotationAdd = annotationAdd && manager.addTriplet(annotationName, relation, objects[i]);
 								} else {
									PrintWriter writer = response.getWriter();
									writer.write("There were errors adding the annotation");
 									manager.deleteResource(annotationName);
									writer.close();
 								}
 							}
 						}
 					}
 				}
 			}
 		} else {
			PrintWriter writer = response.getWriter();
			writer.write("There were errors adding the annotation");
 			manager.deleteResource(annotationName);
			writer.close();
 		}
		PrintWriter writer = response.getWriter();
		writer.write("SUCCESS");
 	}
 }
