 package org.wdbuilder.web;
 
 import java.io.IOException;
 
 import javax.servlet.annotation.WebServlet;
 import javax.xml.bind.JAXBException;
 
 import org.wdbuilder.serialize.html.DiagramImage;
 import org.wdbuilder.web.base.FrameServlet;
 import org.wdbuilder.web.base.ServletInput;
 
 @WebServlet("/diagram")
 public class DiagramServlet extends FrameServlet {
 	private static final long serialVersionUID = 1L;
 
 	@Override
	protected void do4Frame(ServletInput input) throws Exception {
 		printCanvasFrame(input);
 	}
 
 	protected void printCanvasFrame(ServletInput input) throws JAXBException,
 			IOException {
 		new DiagramImage(diagramHelper,
 				serviceFacade.getBlockPluginRepository())
 				.printCanvasFrame(input);
 	}
 }
