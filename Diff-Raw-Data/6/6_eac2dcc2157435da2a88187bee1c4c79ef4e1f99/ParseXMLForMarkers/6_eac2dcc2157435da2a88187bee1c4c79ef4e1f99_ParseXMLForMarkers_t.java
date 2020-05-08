 package textmarker.parse;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 import org.eclipse.ui.texteditor.ITextEditor;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import textmarker.add.AddMarkers;
 import eclippers.patch.editor.extension.PatchContainingEditor;
 import eclippers.patch.editor.markers.PackageDecoratorLightweight;
 
 public class ParseXMLForMarkers {
 
 	//these are for plugins using this tool
 	public static ArrayList<IPath> tempAffected = new ArrayList<IPath>();
 	public static ArrayList<RemovedLine> tempAffectedLines = new ArrayList<RemovedLine>();
 	public static ArrayList<RemovedLine> tempRemovedLines = new ArrayList<RemovedLine>();
 	
 	
 	public static ArrayList<IPath> affected = new ArrayList<IPath>();
 	public static String WORKSPACE_ROOT = ResourcesPlugin.getWorkspace().getRoot().getLocation().toPortableString();
 	
 	public static void parseXML(IProject proj, IEditorPart part, String pathPrefix, String filter) {
 
 		tempAffected = new ArrayList<IPath>();
 		tempAffectedLines = new ArrayList<RemovedLine>();
 		tempRemovedLines = new ArrayList<RemovedLine>();
 		
 		File xmlFile = new File(proj.getLocation() + File.separator + pathPrefix + File.separator + "patch.cfg");
 		ArrayList<RemovedLine> remLines = new ArrayList<RemovedLine>();
 		
 		try {
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder builder;
 			builder = factory.newDocumentBuilder();
 			Document document = builder.parse(xmlFile);
 			String patchName = "";
 			
 			//get the root element
 			Element rootElement = document.getDocumentElement();
 
 			//get each patch element
 			NodeList nl = rootElement.getElementsByTagName("patch");
 			if(nl != null && nl.getLength() > 0) {
 				for(int i = 0 ; i < nl.getLength();i++) {
 					Element patchElement = (Element)nl.item(i);
 					patchName = patchElement.getAttribute("name");
 					String applied = patchElement.getAttribute("applied");
 					
 					NodeList n2 = patchElement.getElementsByTagName("file");
 					if(n2 != null && n2.getLength() > 0) {
 						for (int j = 0; j < n2.getLength(); j++) {
 							Element fileElement = (Element)n2.item(j);
 							String fileName = fileElement.getAttribute("name");// + ".java";
 							String filePath = fileElement.getAttribute("package");
 							filePath = filePath.replaceAll("\\.", "/");
 							String fullPath = proj.getLocation() + File.separator + filePath + File.separator + fileName;
 							String projPath = filePath + File.separator + fileName;
 							
 							File checkFile = new File(fullPath);
 							if(!checkFile.exists()){
 								//try without src directory
 								fullPath = fullPath.replaceFirst("src", ".");
 								checkFile = new File(fullPath);
 								
 								if(!checkFile.exists()){
 									//try under src directory
 									fullPath = proj.getLocation() + File.separator + "src" + File.separator + filePath + File.separator + fileName;
 									checkFile = new File(fullPath);
 								}
 							}
 
 							AddMarkers.clearMarkers(fullPath, patchName, proj);
 							IResource file = proj.findMember(projPath);
 
 							if(!affected.contains(file) && file != null){
 								affected.add(file.getFullPath());
 							}
 							if(filter != null && patchName.equals(filter)){
 								if(!tempAffected.contains(file) && file != null){
 									tempAffected.add(file.getFullPath());
 								}
 							}
 									
 							if(applied.equals("true")){
 								NodeList n3 = fileElement.getElementsByTagName("addline");
 								if(n3 != null && n3.getLength() > 0) {
 									for(int k = 0; k < n3.getLength(); k++) {
 										Element offsetElement = (Element)n3.item(k);
 										int lineNumber = Integer.parseInt(offsetElement.getAttribute("at"));
 										int newLine = Integer.parseInt(((Element)offsetElement.getParentNode()).getAttribute("startApplied"));
 										int originalLine = Integer.parseInt(((Element)offsetElement.getParentNode()).getAttribute("start"));
 										String codeLine = offsetElement.getAttribute("content");
 										int patchLine = Integer.parseInt(offsetElement.getAttribute("patchLine"));
										int offset = newLine - originalLine;
										if(offset != 0)
											offset = offset - 1;
 										
										AddMarkers.addMarkerToFile(patchName, checkFile.getAbsolutePath(), lineNumber - offset, proj, codeLine, true, true, patchLine);
 										RemovedLine rl = new RemovedLine(lineNumber, newLine, originalLine, codeLine, patchLine, checkFile);
 										
 										if(filter != null && patchName.equals(filter)){
 											if(!tempAffected.contains(file) && file != null){
 												tempAffectedLines.add(rl);
 											}
 										}
 									}
 								}
 
 								n3 = fileElement.getElementsByTagName("remline");
 								if(n3 != null && n3.getLength() > 0) {
 									for(int k = 0; k < n3.getLength(); k++) {
 										Element offsetElement = (Element)n3.item(k);
 										int lineNumber = Integer.parseInt(offsetElement.getAttribute("at"));
 										int newLine = Integer.parseInt(((Element)offsetElement.getParentNode()).getAttribute("startApplied"));
 										int originalLine = Integer.parseInt(((Element)offsetElement.getParentNode()).getAttribute("start"));
 										String codeLine = offsetElement.getAttribute("content");
 										int patchLine = Integer.parseInt(offsetElement.getAttribute("patchLine"));
 										int tempLineNum = lineNumber + (newLine - originalLine);
 										
 										//AddMarkers.addMarkerToFile(patchName, checkFile.getAbsolutePath(), lineNumber + (newLine - originalLine), proj, codeLine, true, false, patchLine);
 										RemovedLine rl = new RemovedLine(tempLineNum, newLine, originalLine, codeLine, patchLine, checkFile);
 										remLines.add(rl);
 										
 										if(filter != null && patchName.equals(filter)){
 											if(!tempAffected.contains(file) && file != null){
 												tempRemovedLines.add(rl);
 											}
 										}
 									}
 								}
 							} else {					
 								NodeList n3 = fileElement.getElementsByTagName("offset");
 								if(n3 != null && n3.getLength() > 0) {
 									for(int k = 0; k < n3.getLength(); k++) {
 										Element offsetElement = (Element)n3.item(k);
 										int patchLine = Integer.parseInt(offsetElement.getAttribute("patchLine"));
 										int lineNumber = Integer.parseInt(offsetElement.getAttribute("start"));
 										//enumerate lines of code
 										String lines = "Lines that will be removed:\n";
 										NodeList n4 = offsetElement.getElementsByTagName("remline");									
 										for(int j1 = 0; j1 < n4.getLength(); j1++) {
 											Element r = (Element)n4.item(j1);
 											lines += r.getAttribute("content").replaceAll("\t", "") + "\n";
 										}
 										lines += "\nLines that will be added:\n";
 										n4 = offsetElement.getElementsByTagName("addline");									
 										for(int j1 = 0; j1 < n4.getLength(); j1++) {
 											Element r = (Element)n4.item(j1);
 											lines += r.getAttribute("content").replaceAll("\t", "") + "\n";
 										}
 										AddMarkers.addMarkerToFile(patchName, checkFile.getAbsolutePath(), lineNumber, proj, lines, false, true, patchLine);
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 			
 			//add removed lines at the end
 			RemovedLine[] els = remLines.toArray(new RemovedLine[remLines.size()]);
 			IDocument doc = null;
 			//only mess up the editor if code opened with our editor
 			if(part instanceof PatchContainingEditor){
 				part = (PatchContainingEditor) part;
 				ITextEditor editor = (ITextEditor) part;
 				IDocumentProvider dp = editor.getDocumentProvider();
 				doc = dp.getDocument(editor.getEditorInput());
 				
 				for (int i = 0; i < els.length; i++) {
 					RemovedLine offsetElement = els[i];
 					try {
 						//TODO strikethrough?
 						doc.replace(AddMarkers.getCharStart(offsetElement.lineNumber-1, offsetElement.checkFile)-1, 0, offsetElement.codeLine + "\n");
 						AddMarkers.addRemovedMarkerToFile(patchName, offsetElement.checkFile.getAbsolutePath(), offsetElement.lineNumber-1, proj, offsetElement.codeLine, true, offsetElement.patchLine, doc.get());
 					} catch (BadLocationException e) {
 						e.printStackTrace();
 					}		
 				}
 			}
 			
 			//TODO reveal affected files in package explorer
 			PackageDecoratorLightweight.getDecorator().refresh();
 			
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 }
 
 
