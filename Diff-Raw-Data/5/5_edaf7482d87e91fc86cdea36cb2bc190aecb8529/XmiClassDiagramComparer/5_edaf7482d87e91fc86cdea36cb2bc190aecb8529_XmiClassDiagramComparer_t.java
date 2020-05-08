 package controller.comparer.xmi;
 
 import java.util.List;
 
 import uml2parser.XmiElement;
 
 import controller.compare.ComparerIntf;
 import controller.upload.FileInfo;
 import controller.upload.UploadProcessorFactory;
 
 public class XmiClassDiagramComparer implements ComparerIntf {
 
 	private XmiClassDiagramParser ClassDiagram1;
 	private XmiClassDiagramParser ClassDiagram2;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param XmiFiles1
 	 * @param XmiFiles2
 	 */
 	public XmiClassDiagramComparer(List<FileInfo> XmiFiles1,
 			List<FileInfo> XmiFiles2) {
 
 		// Process the first file
 		FileInfo classDiagram1Notation = getFile(
 				UploadProcessorFactory.NOTATION_EXTENSION, XmiFiles1);
 		FileInfo classDiagram1Uml = getFile(
 				UploadProcessorFactory.UML_EXTENSION, XmiFiles1);
 
 		ClassDiagram1 = new XmiClassDiagramParser(
 				classDiagram1Uml.getDestFilePath()
 						+ classDiagram1Uml.getFileName(),
 				classDiagram1Notation.getDestFilePath()
 						+ classDiagram1Notation.getFileName());
 
 		// Process the second file
 		FileInfo classDiagram2Notation = getFile(
 				UploadProcessorFactory.NOTATION_EXTENSION, XmiFiles2);
 		FileInfo classDiagram2Uml = getFile(
 				UploadProcessorFactory.UML_EXTENSION, XmiFiles2);
 
 		ClassDiagram2 = new XmiClassDiagramParser(
 				classDiagram2Uml.getDestFilePath()
 						+ classDiagram2Uml.getFileName(),
 				classDiagram2Notation.getDestFilePath()
 						+ classDiagram2Notation.getFileName());
 	}
 
 	// Do not use this method yet. We need to change the interface to accept a
 	// list of FileInfo, since Xmi requires more than 1 file per diagram
 	@Override
	public List<Object> compare(String filePath1, String filePath2, String compareLayer) {
 		return null;
 	}
 

 	/**
 	 * Refactor this method since it can be used in other sources (ex:
 	 * UmlUploadProcessors)
 	 * 
 	 * @param extension
 	 * @param fileList
 	 * @return
 	 */
 	private FileInfo getFile(String extension, List<FileInfo> fileList) {
 		FileInfo info = null;
 		for (int i = 0; i < fileList.size(); i++) {
 			String extn = fileList
 					.get(i)
 					.getFileName()
 					.substring(
 							fileList.get(i).getFileName().lastIndexOf(".") + 1,
 							fileList.get(i).getFileName().length());
 			if (extn.equals(extension)) {
 				info = fileList.get(i);
 			}
 		}
 		return info;
 	}
 
 	//*************************************************************************
 	// Implement and change these stubs to however you like
 	//*************************************************************************
 	public String compareClassNames() {
 		List<XmiElement> list1 = ClassDiagram1.getClassList();
 		List<XmiElement> list2 = ClassDiagram2.getClassList();
 
 		return "";
 	}
 	
 	public String compareProperties() {
 		List<XmiElement> list1 = ClassDiagram1.getClassList();
 		List<XmiElement> list2 = ClassDiagram2.getClassList();
 
 		return "";
 	}
 	
 	public String compareAssociations() {
 		List<XmiElement> list1 = ClassDiagram1.getClassList();
 		List<XmiElement> list2 = ClassDiagram2.getClassList();
 
 		return "";
 	}
 }
