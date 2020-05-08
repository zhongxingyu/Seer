 package forms.formController.groovy;
 
 import groovy.lang.Binding;
 import groovy.util.GroovyScriptEngine;
 import groovy.util.ResourceException;
 import groovy.util.ScriptException;
 
import java.io.File;
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet implementation class TestServlet
  */
 @WebServlet("/TestServlet")
 public class TestServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public TestServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		String[] roots = new String[] { "" };
 		GroovyScriptEngine gse = new GroovyScriptEngine(roots);
 		Binding binding = new Binding();
 		binding.setVariable("input", "world");
 		binding.setVariable("output", "");
 		
 		try {
			
			/**TODO
			 * Path only tempoary - must be relative
			 */
			
			gse.run("../../Users/Tom/Desktop/repo/forms/controller/src/main/java/forms/formController/groovy/testRetrieve.groovy", binding);
 		}
 		catch (ResourceException | ScriptException e) {
 			e.printStackTrace();
 		}
 		
 		System.out.println(binding.getVariable("output"));
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		
 
 	}
 
 }
