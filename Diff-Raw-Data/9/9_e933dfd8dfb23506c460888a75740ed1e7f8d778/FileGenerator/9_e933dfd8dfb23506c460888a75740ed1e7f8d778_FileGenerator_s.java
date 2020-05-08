 package de.fraunhofer.esk.openetcs.sysml2b.transformation;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 
 
 import java.util.HashMap;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.papyrus.sysml.blocks.Block;
 import org.eclipse.uml2.uml.Comment;
 import org.eclipse.uml2.uml.Element;
 import org.eclipse.uml2.uml.Model;
 import org.eclipse.uml2.uml.NamedElement;
 import org.eclipse.uml2.uml.Stereotype;
 
 
 // import org.eclipse.uml2.uml.util.UMLUtil;
import de.fraunhofer.esk.openetcs.sysml2b.StructureMapping;
 
 public class FileGenerator {
 	private String pathToBMchs;
 	private Model sysmlModel;
 	
 	public FileGenerator(IFile modelFile) {
 		pathToBMchs = modelFile.getLocation().removeLastSegments(1).toOSString() + "/src/";
 		File file = new File(pathToBMchs);
 		
 		if (!file.exists())	{
 			file.mkdir();
 		}
 		
 		sysmlModel = getModel(modelFile);
 	}
 
 
 	public void generateAndWrite() {
 		HashMap<Element, Comment> mapping = new HashMap<Element, Comment>();
 		
 		// Build a mapping of <Element> to <Comment> to find the associated
 		// comments fast in the transformation
 		for (Element element: sysmlModel.allOwnedElements()) {
 			if (element instanceof Comment) {
 				Comment comment = (Comment) element;
 				
 				for (Element e : comment.getAnnotatedElements()) {
 					mapping.put(e,  comment);
 				}
 			}
 		}
 
 		for (Element element: sysmlModel.allOwnedElements()) {
 			Stereotype stereotype;
 
 			if ((stereotype = element.getAppliedStereotype("SysML::Blocks::Block")) != null) {
 				createMachineFile((Block) element.getStereotypeApplication(stereotype), mapping.get(element));
 			}
 		}
 	}
 	
 	private Model getModel(IFile modelFile)
 	{
 		ResourceSet resourceSet = new ResourceSetImpl();
 		URI uri = URI.createURI(modelFile.getFullPath().toString());
 		Resource resource = resourceSet.getResource(uri, true);
 		Model model = (Model) resource.getContents().get(0);
 		
 		return model;
 	}
 	
 	private void createMachineFile(Block block, Comment comment) {
 		StructureMapping mapping = new StructureMapping();
 		
 		writeToFile(block.getBase_Class().getName() + ".mch", mapping.createMachine(block, comment).toString());
 	}
 	
 	public void writeToFile(String filename, String content) {
 		try {
 			FileWriter fileStream = new FileWriter(pathToBMchs + filename);
 			BufferedWriter out = new BufferedWriter(fileStream);
 			out.write(content);
 			out.close();
 		} catch (IOException ioe) {
 			System.err.println("Error: " + ioe.getMessage());
 		}
 	}
 }
