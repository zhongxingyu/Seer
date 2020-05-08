 package de.tu_dresden.psy.efml;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.naming.OperationNotSupportedException;
 
 /**
  * implements the main html tag
  * 
  * @author immanuel
  * 
  */
 
 public class HtmlTag implements AnyTag {
 
 	private ArrayList<AnyTag> innerTags;
 
 	public HtmlTag() {
 		innerTags = new ArrayList<AnyTag>();
 	}
 
 	/**
 	 * write the code needed to include the js containing general code
 	 * 
 	 * @param writer
 	 * @throws IOException
 	 */
 	static public void writeAllIncludes(Writer writer) throws IOException {
 		writer.write("	<script type=\"text/javascript\" src=\"storage.js\"></script>\n"
 				+ "	<script type=\"text/javascript\" src=\"logger.js\"></script>\n"
 				+ "	<script type=\"text/javascript\" src=\"tags.js\"></script>\n"
 				+ "	<script type=\"text/javascript\" src=\"logic.js\"></script>\n"
 				+ "	<script type=\"text/javascript\" src=\"hover.js\"></script>\n"
 				+ "	<script type=\"text/javascript\" src=\"endecoder.js\"></script>\n"
 				+ "	<script type=\"text/javascript\" src=\"runway.js\"></script>\n"
 				+ "	<script type=\"text/javascript\" src=\"answer.js\"></script>\n"
 				+ "	<script type=\"text/javascript\" src=\"sniffybutton.js\"></script>");
 	}
 
 	/**
 	 * write the code needed to initialize the used modules
 	 * 
 	 * @param writer
 	 * @throws IOException
 	 */
 	static public void writeAllInitializations(Writer writer)
 			throws IOException {
		
		/**
		 * run runway bugfix
		 */
		
		writer.write("	<script type=\"text/javascript\">\n"
				+ "  RunwayDisplayBugfix();"
				+ "  </script>\n");
		
		/**
		 * initialize session storage handler
		 */
		
		String id_token = java.util.UUID.randomUUID().toString()+"--"
 				+ java.util.UUID.randomUUID().toString();
 		writer.write("	<script type=\"text/javascript\">\n"
 				+ "  myStorage.SetupAutoRestore(sessionStorage,\""
 				+ StringEscape.escapeToJavaScript(id_token) + "\");"
 				+ "  </script>\n");
 	}
 
 	@Override
 	public void open(Writer writer) throws IOException {
 		writer.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n"
 				+ "<html>");
 		/**
 		 * include all scripts
 		 */
 
 		writeAllIncludes(writer);
 
 		/**
 		 * write inner tags
 		 */
 
 		for (Iterator<AnyTag> it = innerTags.iterator(); it.hasNext();) {
 			AnyTag innerTag = it.next();
 			innerTag.open(writer);
 			innerTag.close(writer);
 		}
 	}
 
 	@Override
 	public void close(Writer writer) throws IOException {
 		/**
 		 * write initializations that depend on the variables created by the
 		 * html layout
 		 */
 
 		writeAllInitializations(writer);
 
 		writer.write("</html>\n");
 	}
 
 	@Override
 	public void encloseTag(AnyTag innerTag)
 			throws OperationNotSupportedException {
 		innerTags.add(innerTag);
 	}
 
 }
