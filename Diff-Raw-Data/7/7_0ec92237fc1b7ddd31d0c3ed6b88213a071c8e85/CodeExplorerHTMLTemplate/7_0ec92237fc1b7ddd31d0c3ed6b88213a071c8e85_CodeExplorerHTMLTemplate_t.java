 package org.codefaces.ui.views;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import org.antlr.stringtemplate.*;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.codefaces.ui.CodeFacesUIActivator;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Path;
 
 
 public class CodeExplorerHTMLTemplate {
 
 	private static final String TEMPLATE_PATH = "public/templates/code_editor.html";
 	
 	//Performance consideration. we only read the template once
 	//I would be happy if there is better approach
 	private static String TEMPLATE = initTemplate();
 	 
 	private final String title;
 	private final String lang;
 	private final String resourceUrl;
 	private final String code;
 	
 	/**
 	 * Constructor. There is no need to escape the string before passing them
 	 * into this constructor
 	 * 
 	 * @param title the title of the generated HTML document
 	 * @param lang the language class of the code
 	 * @param resourceURL the path of the language javascript file
 	 * @param code the content of the source code
 	 */
 	public CodeExplorerHTMLTemplate(final String title, final String lang,
 			final String resourceURL, final String code) {
 		this.title = StringEscapeUtils.escapeHtml(title);
 		this.lang = StringEscapeUtils.escapeHtml(lang);
 		this.resourceUrl = StringEscapeUtils.escapeHtml(resourceURL);
 		this.code = StringEscapeUtils.escapeHtml(code);
 	}
 
 	/**
 	 * bind the template variables and return the resultant HTML string
 	 * 
 	 * 
 	 * @return the binded HTML template
 	 */
 	public String toHTML(){
 		StringTemplate template = new StringTemplate(TEMPLATE);
 		template.setAttribute("title", title);
 		template.setAttribute("lang", lang);
 		template.setAttribute("resource_url", resourceUrl);
 		template.setAttribute("code", code);
 		return template.toString();
 	}
 	
 	
 	/**
 	 * @return a html string to initialize the template
 	 */
 	private static String initTemplate(){
		String template = null;
 		try{
			template = readFile(TEMPLATE_PATH);
 		}catch(IOException e){
 			e.printStackTrace();
 		}
		return template;
 	}
 	
 	
 	/**
 	 * Read the template file and return it as a string
 	 * @param path the path of the template
 	 * @return a string representation of that file
 	 * @throws IOException 
 	 * @throws IOException if there is IO problem
 	 */
 	private static String readFile(String path) throws IOException{
 		InputStream stream = FileLocator.openStream(CodeFacesUIActivator.getDefault().getBundle(), new Path(
 				TEMPLATE_PATH), false);
 		StringWriter writer = new StringWriter();
 		try{
 			IOUtils.copy(stream, writer);
 		}
 		finally{
 			stream.close();
 			writer.close();
 		}
 		return writer.toString();
 	}
 
 }
