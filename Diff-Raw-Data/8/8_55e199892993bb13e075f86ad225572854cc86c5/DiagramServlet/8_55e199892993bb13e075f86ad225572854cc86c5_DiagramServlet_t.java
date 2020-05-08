 package org.wdbuilder.web;
 
 import java.io.IOException;
 
 import javax.servlet.annotation.WebServlet;
 import javax.xml.bind.JAXBException;
 
import org.wdbuilder.domain.Diagram;
import org.wdbuilder.input.BlockParameter;
 import org.wdbuilder.serialize.html.DiagramImage;
 import org.wdbuilder.web.base.FrameServlet;
 import org.wdbuilder.web.base.ServletInput;
 
 @WebServlet("/diagram")
 public class DiagramServlet extends FrameServlet {
 	private static final long serialVersionUID = 1L;
 
 	@Override
	protected void do4Frame(ServletInput input) throws Exception {		
		String diagramKey  = BlockParameter.DiagramKey.getString(input);
		Diagram diagram = serviceFacade.getDiagramService().getDiagram(diagramKey);
		input.getState().setDiagram(diagram);		
 		printCanvasFrame(input);
 	}
 
 	protected void printCanvasFrame(ServletInput input) throws JAXBException,
 			IOException {
 		new DiagramImage(diagramHelper,
 				serviceFacade.getBlockPluginRepository())
 				.printCanvasFrame(input);
 	}
 }
