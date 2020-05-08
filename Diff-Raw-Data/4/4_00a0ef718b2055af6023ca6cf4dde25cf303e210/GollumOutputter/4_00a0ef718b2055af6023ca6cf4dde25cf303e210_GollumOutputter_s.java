 package org.ndx.lifestream.rendering.output.gollum;
 
 import java.io.OutputStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.vfs2.FileObject;
 import org.ndx.lifestream.rendering.OutputWriter;
 import org.ndx.lifestream.rendering.model.Input;
 import org.stringtemplate.v4.ST;
 
 /**
  * Simple output of raw content to file
  *
  * @author ndx
  *
  */
 public class GollumOutputter implements OutputWriter {
 	private static final Logger logger = Logger.getLogger(GollumOutputter.class.getName());
 	/**
 	 * Gollum template is simple, non ?
 	 */
 	private ST gollum= new ST("<text>");
 
 	@Override
 	public void write(Input input, FileObject output) {
 		gollum.add("text", input.getText());
 		String resultText  = gollum.render();
 		gollum.remove("text");
 		FileObject resultFile;
 		try {
 			resultFile = output.resolveFile(input.getBasename()+".md");
 			try (OutputStream outputStream = resultFile.getContent().getOutputStream()) {
				IOUtils.write(resultText, outputStream);
 			}
 		} catch (Exception e) {
 			throw new GollumException("unable to output render for input "+input.getBasename(), e);
 		}
 
 	}
 
 }
