 package xml2labbook;
 
 import org.concord.LabBook.*;
 import org.concord.CCProbe.*;
 import org.concord.waba.extra.probware.probs.*;
 import extra.util.*;
 import waba.fx.*;
 import graph.*;
 
 import javax.xml.parsers.*;
 import org.xml.sax.*;
 import org.xml.sax.helpers.*;
 import org.w3c.dom.*;
 
 import java.io.*;
 import java.net.*;
 import java.awt.*;
 import java.awt.image.*;
 
 
 
 public class XML2LabBook{
 public static int newIndex = 0;
 
 
 private  static int indent = 0;
 
 public final static int FOLDER_TAG 				= 0;
 public final static int EXPOBJECT_TAG 			= 6;
 public final static int OBJREF_TAG              = 11;
 public final static int IMAGE_TAG 				= 5;
 
 	/*
 public final static int NOTES_TAG 				= 1;
 public final static int DATACOLLECTOR_TAG 		= 2;
 public final static int DRAWING_TAG 			= 3;
 public final static int UNITCONV_TAG 			= 4;
 public final static int SUPERNOTES_TAG 			= 7;
 public final static int GRAPH_TAG               = 8;
 	public final static int PROBE_TAG               = 9;
 	public final static int INTPROBETRANS_TAG       = 10;
 	*/
 
 public final static int UNKNOWN_PROTOCOL 		= 0;
 public final static int FILE_PROTOCOL 			= 1;
 public final static int HTTP_PROTOCOL 			= 2;
 public final static int FTP_PROTOCOL 			= 3;
 
 
 public static String []labBookObjectTAGs = {"FOLDER","NOTES","DATACOLLECTOR","DRAWING","UNITCONV","IMAGE","EXPOBJECT","SUPERNOTES","GRAPH","PROBE","INTPROBETRANS","OBJ-REF"};
 public static String []labBookObjectNames = {"Folder","Notes","Data Collector","Drawing","UnitConvertor","Image",null,"SuperNotes","Graph","ProbeDataSource","IntProbeTrans",null};
 
 public static Document	currentDocument;
 
 public static java.util.Hashtable	docObjects;
 
 public static boolean qtInstalled = false;
 
 public static QTManager qtManager = null;
 
 	public static LabBook labBook = null;
 	public static LabBookSession session = null;
 
 	public static void exit(int code){
 			if(qtManager != null) qtManager.closeQTSession();
 			System.exit(code);
 	}
 
 	public static void main(String []args) throws Exception {
 		try{
 			qtManager = new QTManager();
 			qtManager.openQTSession();
 			qtInstalled = qtManager.qtInstalled;
 		}catch(Throwable t){
 			qtManager = null;
 			qtInstalled = false;
 			System.out.println("QuickTime is not installed");
 		}
 
 
 
 		String xmlFile = "labbook.xml";
 		if(args != null && args.length > 0){
 			xmlFile = args[0];
 		}
 		
 	
         // Step 1: create a DocumentBuilderFactory and configure it
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         // Optional: set various configuration options
         dbf.setValidating(true);
         dbf.setIgnoringComments(true);
         dbf.setIgnoringElementContentWhitespace(true);//???
         dbf.setCoalescing(true);
         // The opposite of creating entity ref nodes is expanding them inline
 //        dbf.setExpandEntityReferences(!createEntityRefs);
 
         DocumentBuilder db = null;
         try {
             db = dbf.newDocumentBuilder();
         } catch (ParserConfigurationException pce) {
             System.err.println(pce);
 			exit(1);
         }
 
 
         // Set an ErrorHandler before parsing
         OutputStreamWriter errorWriter =
             new OutputStreamWriter(System.err, "UTF-8");
         db.setErrorHandler(
             new MyErrorHandler(new PrintWriter(errorWriter, true)));
 
         // Step 3: parse the input file
         Document doc = null;
         try {
             doc = db.parse(new File(xmlFile));
         } catch (SAXException se) {
             System.err.println(se.getMessage());
 			exit(1);
         } catch (IOException ioe) {
             System.err.println(ioe);
 			exit(1);
         }
         
         String rootName = doc.getDocumentElement().getTagName();
        if(rootName == null || !rootName.equals("LABBOOK")){
             System.err.println("LabBook description wasn't found");
 			exit(1);
         }
         
         labBook = createLabBook(doc.getDocumentElement());
    		if(labBook == null){
    			System.out.println("Error creating LabBook");
 			exit(1);
    		}
 
 		session = labBook.getSession();
 
  		LObjDictionary loDict = initRootDictionary();
    		boolean pagingView = doc.getDocumentElement().getAttribute("view").equals("paging");
    		if(pagingView){
 			loDict.viewType = LObjDictionary.PAGING_VIEW;
 		} else {
 			loDict.viewType = LObjDictionary.TREE_VIEW;
    		}
        
 		NodeList nodeList = doc.getDocumentElement().getChildNodes();
 		currentDocument = doc;
 		docObjects = new java.util.Hashtable();
 		for(int i = 0; i < nodeList.getLength(); i++){
 			Node node = nodeList.item(i);
 			int type = node.getNodeType();
 			if(type == Node.ELEMENT_NODE){
 				LabObject obj = getLabBookObject((Element)node);
 				loDict.add(obj);
 			}
    		}
    		   		
 		labBook.commit();
 		labBook.close();
    		
 		exit(0);
 	}
 	
 	
 	public static LabBook createLabBook(Node labBookNode){
 		LabBook 	retBook = null;
 		if(labBookNode == null) return retBook;
 		LabBookDB 	lbDB = null;
 		labBookInit();
 		try{
 			File labBookFile = new File("LabBook.PDB");
 			if(labBookFile.exists()){
 				labBookFile.delete();
 			}
 			lbDB = new LabBookCatalog("LabBook");
 			if(lbDB.getError()){
 				retBook = null;
 			}else{
 				retBook = new LabBook();
 			}
 		}catch(Exception e){
 			e.printStackTrace();
 			retBook = null;
 		}
 		if(retBook != null){
 			LabObject.lBook = retBook;
 			retBook.open(lbDB);
 		}
 		return retBook;
 	}
 	public static void labBookInit(){
 		LabBook.init();
 		LabBook.registerFactory(new DataObjFactory());
 	}
 	
 	public static LabObject getLabBookObject(Element element){
 		if(labBook == null) return null;
 		if(!isElementLabBookObject(element)) return null;
 
 		LabObject labObject = createRegularObject(element);		
 				
 		if(labObject != null){
 			String ID = element.getAttribute("ID");
 			if(ID != null) docObjects.put(ID,labObject);
 			
 			labObject.store();
 		}
 		return labObject;
 	}
 
 	public static boolean validAttr(String value){
 		if(value == null || value.length() == 0) return false;
 		return true;
 	}
 
 	public static LabObject getLabBookObjectFromId(String idref){
 		if(!validAttr(idref)){ 
			System.out.println("*******empty object reference*************");
 			return null;
 		}
 
 		LabObject labObject = null;
 		if(docObjects.containsKey(idref)){
 			labObject = (LabObject)docObjects.get(idref);
 		} else {
 			Element linkElement =  (Element)currentDocument.getElementById(idref);
 			if(linkElement == null){
				System.out.println("*********Can't find object ref: " + idref + "*************);
 				return null;
 			}
 			labObject = createRegularObject(linkElement);
 			docObjects.put(idref, labObject);
 		}
 		System.out.println("Linking Existing object ref: " + idref);
 		return labObject;
 	}
 
 	public static int getIdentifierFromElement(Element element){
 		int retValue = -1;
 		String elName = element.getTagName();
 		for(int i=0; i<labBookObjectTAGs.length; i++){
 			if(elName.equals(labBookObjectTAGs[i])){
 				return i;
 			}
 		}
 		return -1;
 	}
 	
 	public static boolean isElementLabBookObject(Element element){
 		if(element == null) return false;
 		String elementName = element.getTagName();
 		if(elementName == null) return false;
 		for(int i = 0; i < labBookObjectTAGs.length; i++){
 			if(elementName.equals(labBookObjectTAGs[i])) return true;
 		}
 		return true;
 	}
 
 	public static LObjDictionary initRootDictionary(){
 		if(labBook == null) return null;
  		LObjDictionary dict = DefaultFactory.createDictionary();
 		session.storeNew(dict);
 		dict.setName("Home");
 		labBook.store(dict);
 		return dict;
 	}
 	
 	
 	public static int getProtocolFromURL(String url){
 		int retValue = UNKNOWN_PROTOCOL;
 		if(url == null) return retValue;
 		if(url.startsWith("http://")) 	return 	HTTP_PROTOCOL;
 		if(url.startsWith("ftp://")) 	return 	FTP_PROTOCOL;
 		if(url.startsWith("file://")) 	return 	FILE_PROTOCOL;
 		if(url.indexOf("://") < 0)  	return 	FILE_PROTOCOL;
 		return retValue;
 	}
 	
 	public static String convertURLStringToPath(String url){
 		if(getProtocolFromURL(url) != FILE_PROTOCOL) return null;
 		if(url.startsWith("file://")){
 			return url.substring(7);
 		}
 		return url;
 	}
 		
 	public static LabObject createRegularObject(Element element)
 	{
 		if(element == null) return null;
 
 		int id = getIdentifierFromElement(element);
 		LabObject labObject = null;
 		if(id >= 0){		
 			if(id == EXPOBJECT_TAG){//embed object
 				if(labBook != null){
 					String url = convertURLStringToPath(element.getAttribute("url"));
 					if(url != null){
 						LabBookFile imFile = new LabBookFile(url);
 						labObject = labBook.importDB(imFile);
 						System.out.println("Importing Object from file: " + url);
 						imFile.close();
 					}
 				}
 			}else if(id == OBJREF_TAG){
 				String idref = element.getAttribute("ref");
 				labObject = getLabBookObjectFromId(idref);
 			} else {
 				System.out.println("Adding Object to LabBook " + element.getTagName() + 
 								   " ID = "+element.getAttribute("ID"));
 				labObject = createObj(labBookObjectNames[id]);
 				String nameObject = element.getAttribute("name");
 				if(nameObject != null){
 					labObject.setName(nameObject);
 				}else{
 					labObject.setName("");
 				}
 				if(labObject instanceof LObjDictionary){
 					labObject = getFolder(element,(LObjDictionary)labObject);
 				}else if(labObject instanceof LObjDocument){
 					labObject = getNotes(element,(LObjDocument)labObject);
 				}else if(labObject instanceof LObjDataCollector){
 					labObject = getDataCollector(element, (LObjDataCollector)labObject);
 				}else if(labObject instanceof LObjImage){
 					labObject = getImage(element,(LObjImage)labObject);
 				}else if(labObject instanceof LObjCCTextArea){
 					labObject = getSuperNotes(element,(LObjCCTextArea)labObject);
 				} else if(labObject instanceof LObjGraph){
 					labObject = getGraph(element,(LObjGraph)labObject);
 				} else if(labObject instanceof LObjProbeDataSource){
 					labObject = getProbeDataSource(element, (LObjProbeDataSource)labObject);
 				} else if(labObject instanceof LObjIntProbeTrans){
 					labObject = getIntProbeTrans(element, (LObjIntProbeTrans)labObject);
 				}
 				// note that Drawing and UnitConverter get created automatically
 			}
 		}
 
 		return labObject;
 	}
 
 	public static LabObject getFolder(Element element, LObjDictionary folder){
 		if(labBook == null || folder == null) return null;
 		
 		String	urlStr = convertURLStringToPath(element.getAttribute("url"));
 		boolean autoRecursion = ((urlStr != null) && urlStr.length() > 0);
 		File file = null;
 		if(autoRecursion){
 			file = new File(urlStr);
 			autoRecursion = file.exists() && file.isDirectory();
 			if(autoRecursion && (folder.getName() == null || folder.getName().length() < 1)){
 				folder.setName(file.getName());
 			}
 			addFileSystemIntoFolder(folder,file);
 		} else {
 			NodeList nodeList = element.getChildNodes();
 			for(int i = 0; i < nodeList.getLength(); i++){
 				Node node = nodeList.item(i);
 				int type = node.getNodeType();
 				if(type == Node.ELEMENT_NODE){
 					LabObject obj = getLabBookObject((Element)node);
 					if(obj != null) folder.add(obj);
 				}
 	   		}
 	   		
 	   		boolean pagingView = element.getAttribute("view").equals("paging");
 	   		if(pagingView){
 				folder.viewType = LObjDictionary.PAGING_VIEW;
 			} else {
 				folder.viewType = LObjDictionary.TREE_VIEW;
 	   		}
 	   	}
 		folder.store();
    		return (LabObject)folder;
 	}
 	
 	public static void addFileSystemIntoFolder(LObjDictionary dict,File file){
 		if(file == null || !file.exists() || !file.isDirectory()) return;
 		String []files = file.list();
 		if(files == null || files.length < 1) return;
 		for(int i = 0; i < files.length; i++){
 			File fileChild = new File(file,files[i]);
 			if(!fileChild.exists()) continue;
 			if(fileChild.isDirectory()){
 				String objName = labBookObjectNames[FOLDER_TAG];
 				LObjDictionary folderObject = (LObjDictionary)createObj(objName);
 				folderObject.setName(fileChild.getName());
 				addFileSystemIntoFolder(folderObject,fileChild);
 				dict.add(folderObject);
 			}else if(fileChild.isFile()){
 				if(files[i].endsWith(".gif")  || files[i].endsWith(".GIF")  ||
 				   files[i].endsWith(".bmp")  || files[i].endsWith(".BMP")  ||
 				   files[i].endsWith(".png")  || files[i].endsWith(".PNG")  ||
 				   files[i].endsWith(".tiff") || files[i].endsWith(".TIFF") ||
 				   files[i].endsWith(".jpg")  || files[i].endsWith(".JPG")  ||
 				   files[i].endsWith(".jpeg") || files[i].endsWith(".JPEG")){					
 						LObjImage imgObj = getImageFromFile(fileChild);
 						dict.add(imgObj);
 				}else if(files[i].endsWith(".export")){
 					LabBookFile imFile = new LabBookFile(fileChild.getAbsolutePath());
 					LabObject exportObject = labBook.importDB(imFile);
 					imFile.close();
 					if(exportObject != null){
 						dict.add(exportObject);
 						exportObject.store();
 					}
 				}
 			}
 		}
 		dict.store();
 	}
 
 	public static LObjImage getImageFromFile(File file){
 		if(file == null || !file.exists() || file.isDirectory()) return null;
 		String fileName = file.getName();
 		String objName = labBookObjectNames[IMAGE_TAG];
 		LabObject imageObject = createObj(objName);
 		LObjImageView view = null;
 		if(imageObject != null) view = (LObjImageView)imageObject.getView(null,false,null);
 		if(view == null) return null;
 		imageObject.setName(file.getName());
 		if(fileName.endsWith(".bmp") || fileName.endsWith(".BMP")){
 			view.loadImage(file.getAbsolutePath());
 		}else if(fileName.endsWith(".gif") || fileName.endsWith(".GIF") ||
 				 (qtInstalled && (fileName.endsWith(".png") || fileName.endsWith(".PNG") ||
 				  fileName.endsWith(".tiff") || fileName.endsWith(".TIFF"))) ||
 				  fileName.endsWith(".jpg") || fileName.endsWith(".JPG") ||
 				  fileName.endsWith(".jpeg") || fileName.endsWith(".JPEG")){
 					exportImage(file.getAbsolutePath(),view);
 		}
 		imageObject.store();
 		return (LObjImage)imageObject;
 	}
 	
 	public static int makeSNParagraph(waba.util.Vector lines, int nSpaces, CharacterData textNode, 
 									   String linkcolor, boolean link, boolean optional)
 	{
 		String tData = "";
 		if(textNode instanceof CharacterData){
 			tData = ((CharacterData)textNode).getData().trim();
 			if(tData.length() > 0){
 				java.util.StringTokenizer toks = new java.util.StringTokenizer(tData);
 				StringBuffer result = new StringBuffer();
 				while(toks.hasMoreTokens()){
 					result.append(toks.nextToken());
 					result.append(" ");
 				}
 				tData = result.toString();
 				tData.trim();
 				if(nSpaces > 0){
 					for(int p=0; p < nSpaces; p++) tData = "\t"+tData;
 				}
 			}
 		}
 		if(optional && tData.equals("")) return 0;
 		
 		CCStringWrapper wrapper = CCTextArea.createCCStringWrapper(tData,linkcolor,link,null);
 		lines.add(wrapper);
 		return 1;
 	}
 
 
 	public static LabObject getSuperNotes(Element element, LObjCCTextArea superNotes){
 		if(labBook == null || superNotes == null) return null;
 		
 		LObjCCTextAreaView view = (LObjCCTextAreaView)superNotes.getView(null,false,null,session);
 
 		if(view == null) return null;
 		waba.util.Vector lines = new waba.util.Vector();
 
 		NodeList children = element.getChildNodes();
 		if(children != null){
 			int currParagraph = 0;
 			int lastEmbeddedObjParagraph = -1;
 
 			waba.util.Vector components = new waba.util.Vector();
 			waba.util.Vector linkComponents = new waba.util.Vector();
 			for(int i = 0; i < children.getLength(); i++){
 				Node childNode = (Node)children.item(i);
 				if(childNode == null) continue;
 				if(childNode instanceof CharacterData){
 					currParagraph += makeSNParagraph(lines, 0, (CharacterData)childNode, "000000", false, true);
 					
 				} else if(childNode instanceof Element){
 					Element child = (Element)childNode;
 
 					if(child.getTagName().equals("SNPARAGRAPH")){
 						int nSpaces = getIntFromString(child.getAttribute("indent"));					 
 						Node textNode = child.getFirstChild();
 						CharacterData cdNode = null;
 						if(textNode instanceof CharacterData){
 							cdNode = (CharacterData) textNode;
 						} 
 
 						String linkStr = child.getAttribute("link");
 						boolean link = (linkStr == null)?false:linkStr.equals("true");
 						String idref = child.getAttribute("object");
 
 						if(link){
 							LabObject linkObject = getLabBookObjectFromId(idref);
 							if(linkObject == null){
 								System.out.println(" link not valid in snparagraph: " + i);
 								link = false;
 							}else{
 								linkComponents.add(linkObject);
 							}
 						}
 						currParagraph += makeSNParagraph(lines, nSpaces, cdNode, 
 														 child.getAttribute("linkcolor"),link, false);
 					}else if(child.getTagName().equals("EMBOBJ")){
 						String idref = child.getAttribute("object");
 						LabObject embObject = null;
 						Element embElement = null;
 						if(idref == null || idref.trim().length() < 1){
 							embElement = (Element)child.getFirstChild();
 							if(embElement != null){
 								int id = getIdentifierFromElement(embElement);
 								if(id >= 0){
 									embObject = getLabBookObject(embElement);
 									superNotes.setObj(embObject,superNotes.getNumObjs());
 								}
 							}
 						}else{
 							embObject = getLabBookObjectFromId(idref);
 						}
 						if(embObject != null){
 							String tempStr = child.getAttribute("link");
 							boolean link = (tempStr != null && tempStr.equals("true"));
 							String linkColor = child.getAttribute("linkcolor");
 							if(linkColor == null || linkColor.length() < 1) linkColor = "0000FF";
 					
 							LabObjectView embObjView = (link)?embObject.getMinimizedView():embObject.getView(null,false,null);
 							int	prefWidth = -1;
 							int	prefHeight = -1;
 							if(embObjView != null){
 								extra.ui.Dimension dimPref = null;
 								try{
 									dimPref = embObjView.getPreferredSize();
 								}catch(Exception e){
 									dimPref = new extra.ui.Dimension((int)((double)embObject.getName().length()*5.5+0.5),12) ;
 								}
 								if(dimPref != null){
 									prefWidth = dimPref.width;
 									prefHeight = dimPref.height;
 								}
 							}
 							String widthStr = child.getAttribute("w");
 							int w = (widthStr == null || widthStr.length() < 1)?prefWidth:getIntFromString(widthStr);
 							if(w < 10) w = 10;
 							String heightStr = child.getAttribute("h");
 							int h = (heightStr == null || heightStr.length() < 1)?prefHeight:getIntFromString(heightStr);
 							if(h < 10) h = 10;
 							int alignment = LBCompDesc.ALIGNMENT_LEFT;
 							tempStr = child.getAttribute("alignment");
 							if(tempStr != null && tempStr.equals("right")){
 								alignment = LBCompDesc.ALIGNMENT_RIGHT;
 							}
 							tempStr = child.getAttribute("wrapping");
 							boolean wrapping = (tempStr != null && tempStr.equals("true"));
 							if(currParagraph == lastEmbeddedObjParagraph){
 								currParagraph += makeSNParagraph(lines, 0, null, "000000", false, false);
 							}
 							lastEmbeddedObjParagraph = currParagraph;
 							LBCompDesc compDesc = new LBCompDesc(currParagraph,w, h,alignment, wrapping,link);
 							compDesc.linkColor = getIntColorFromStringColor(linkColor);
 							compDesc.setObject(embObject);
 							components.add(compDesc);
 						}					
 					}
 				}
 			}
 			view.createTArea(lines,linkComponents,components);
 
 
 		}
 
 		superNotes.store();
 		return superNotes;
 	
 	}	
 	
 	public static LabObject getDataCollector(Element element, LObjDataCollector dc){
 		if(dc == null) return null;
 
 		NodeList nodeList = element.getChildNodes();
 		LObjGraph graph = null;
 
 		for(int i = 0; i < nodeList.getLength(); i++){
 			Node node = nodeList.item(i);
 			int type = node.getNodeType();
 			if(type == Node.ELEMENT_NODE) {
 				LabObject dcObj = getLabBookObject((Element)node);
 				if(graph == null && dcObj instanceof LObjGraph){
 					graph = (LObjGraph)dcObj;
 				}
 			}
 		}
 
 		if(graph == null) return null;
 		dc.setGraph(graph);
 		return (LabObject)dc;
 	}
 
 	public static LabObject getGraph(Element element, LObjGraph graph)
 	{
 		if(graph == null) return null;
 		
 		NodeList nodeList = element.getChildNodes();
 
 		for(int i = 0; i < nodeList.getLength(); i++){
 			Node node = nodeList.item(i);
 			int type = node.getNodeType();
 			if(type == Node.ELEMENT_NODE) {
 				String tagName = ((Element)node).getTagName();
 				if(tagName.equals("XAXIS")){
 					setupAxis((Element)node, graph.addXAxis());
 				} else if(tagName.equals("YAXIS")){
 					setupAxis((Element)node, graph.addYAxis());
 				} else if(tagName.equals("LINE")){
 					if(!setupLine((Element)node, graph)){
 						return null;
 					}
 				}
 			}
 		}
 
 		String curLine = element.getAttribute("current-line");
 		if(curLine != null && curLine.length() > 0){
 			graph.setCurGSIndex(getIntFromString(curLine));
 		}
 		String title = element.getAttribute("title");
 		if(title != null && title.length() > 0){
 			graph.setTitle(title);
 		}
 
 		return (LabObject)graph;
 	}
 
 	public static void setupAxis(Element element, Axis axis)
 	{
 		float tempFloat;
 
 		float min = axis.getDispMin();
 		float max = axis.getDispMax();
 		String minStr = element.getAttribute("min");
 		if(minStr != null && minStr.length() > 0){
 			tempFloat = getFloatFromString(minStr);
 			if(tempFloat != Float.NaN) min = tempFloat;
 		}
 		String maxStr = element.getAttribute("max");
 		if(maxStr != null && maxStr.length() > 0){
 			tempFloat = getFloatFromString(maxStr);
 			if(tempFloat != Float.NaN) max = tempFloat;
 		}
 		axis.setRange(min, max-min);
 
 		System.out.println("X2L: adding axis: " + axis + " min: " + min +
 						   " max: " + max);
 	}		
 
 	public static boolean setupLine(Element element, LObjGraph graph)
 	{
 		int xAxisNum = 0;
 		int yAxisNum = 0;
 
 		String xAxisStr = element.getAttribute("xaxis");
 		if(xAxisStr != null && xAxisStr.length() > 0){
 			xAxisNum = getIntFromString(xAxisStr);
 		}
 		String yAxisStr = element.getAttribute("yaxis");
 		if(yAxisStr != null && yAxisStr.length() > 0){
 			yAxisNum = getIntFromString(yAxisStr);
 		}
 
 		LabObject dsObj = null;
 		String dsRefStr = element.getAttribute("datasource");
 		if(validAttr(dsRefStr)) dsObj = getLabBookObjectFromId(dsRefStr);
 
 		if(dsObj == null){
 			NodeList nodeList = element.getChildNodes();
 			for(int i = 0; i < nodeList.getLength(); i++){
 				Node node = nodeList.item(i);
 				int type = node.getNodeType();
 				if(type == Node.ELEMENT_NODE){
 					dsObj = getLabBookObject((Element)node);
 					break;
 				}
 			}
 		}
 
 		if(!(dsObj instanceof DataSource)){
 			System.err.println("X2L: the datasource: " + dsObj + " isn't valid");
 			return false;
 		}
 		
 		graph.addDataSource((DataSource)dsObj,true,xAxisNum,yAxisNum,session);
 		return true;
 	}
 
 	public static LabObject getProbeDataSource(Element element, LObjProbeDataSource pds)
 	{
 		if(pds == null) return null;
 
 		String probeStr = element.getAttribute("probe");
 		String interfaceType = element.getAttribute("interface");
 		if(interfaceType != null && interfaceType.length() > 0){
 			int interId = getIntFromString(interfaceType);
 			pds = LObjProbeDataSource.getProbeDataSource(probeStr, interId);
 		} else {
 			pds =  LObjProbeDataSource.getProbeDataSource(probeStr);
 		}
 
 		if(pds == null){
 			System.out.println("X2L: error invalid probe");
 			return null;
 		}
 		session.storeNew(pds);
 
 		CCProb probe = pds.getProbe();
 		if(probe == null){
 			System.out.println("X2L: error invalid probe");
 			return null;
 		}
 
 		boolean invalidProps = false;
 		NodeList nodeList = element.getChildNodes();
 		for(int i = 0; i < nodeList.getLength(); i++){
 			Node node = nodeList.item(i);
 			int type = node.getNodeType();
 			if(type == Node.ELEMENT_NODE){
 				String tagName = ((Element)node).getTagName();
 				if(tagName.equals("PROP")){
 					Element prop = (Element)node;
 					String propName = prop.getAttribute("name");
 					String propVal = prop.getAttribute("value");
 					PropObject propObj = probe.getProperty(propName);
 					if(propObj == null){
 						invalidProps = true;
 						System.out.println("X2L invalid property: " + propName);
 						continue;
 					}
 					propObj.setValue(propVal);
 				}
 			}
 		}
 
 		if(invalidProps){
 			waba.util.Vector props = probe.getProperties();
 			System.out.println("  Valid properties are:");
 			for(int i=0; i<props.getCount(); i++){
 				PropObject propObj = (PropObject)props.get(i);
 				System.out.print("   " + propObj.getName());
 				String [] posVals = propObj.getPossibleValues();
 				if(posVals == null){
 					System.out.println("");
 				} else {
 					System.out.print(": ( ");
 					for(int j=0; j<posVals.length-1; j++){
 						System.out.print("\"" + posVals[j] + "\" | ");
 					}
 					System.out.println("\"" + posVals[posVals.length-1] + "\" )");
 				}
 			}			
 		}
 
 		return pds;
 	}
 
 	public static LabObject getIntProbeTrans(Element element, LObjIntProbeTrans obj)
 	{
 		if(obj == null) return null;
 
 		String quantityName = element.getAttribute("quantity");
 		if(quantityName == null || quantityName.length() <= 0) return null;
 
 		LabObject probeObj = null;
 		String probeRefStr = element.getAttribute("probe");
 		if(validAttr(probeRefStr)) probeObj = getLabBookObjectFromId(probeRefStr);
 
 		if(probeObj == null){
 			NodeList nodeList = element.getChildNodes();
 			for(int i = 0; i < nodeList.getLength(); i++){
 				Node node = nodeList.item(i);
 				int type = node.getNodeType();
 				if(type == Node.ELEMENT_NODE){
 					probeObj = getLabBookObject((Element)node);
 					break;
 				}
 			}
 		}
 
 		if(!(probeObj instanceof LObjProbeDataSource)){
 			System.err.println("X2L: the probe isn't valid");
 			return null;
 		}
 		LObjProbeDataSource probeDS = (LObjProbeDataSource)probeObj;
 
 		DataSource newDS = probeDS.getQuantityDataSource(quantityName, session);
 		if(newDS == null){
 			System.err.println("X2L: the quantity: " + quantityName + 
 							   " is not valid for: " + probeDS.getProbeName());
 			System.err.print("  valid quantities are: ( ");
 			String names []  = probeDS.getQuantityNames();
 			for(int i=0; i<names.length-1; i++){
 				System.err.print(names[i] + " | ");
 			}
 			System.err.println(names[names.length-1] + " )");
 		}
 		return (LabObject)newDS;
 	}
 
 	public static LabObject getNotes(Element element,LObjDocument doc){
 		if(doc == null) return null;
 		int nSpaces = getIntFromString(element.getAttribute("indent"));
 		Node textNode = element.getFirstChild();
 		String tData = "";
 		if(textNode instanceof CharacterData){
 			tData = ((CharacterData)textNode).getData().trim();
         	if(tData.length() > 0){
 				if(nSpaces > 0){
 					for(int p=0; p < nSpaces; p++) tData = "\t"+tData;
 				}
         	}
 		}
 		doc.setText(tData);
 		return (LabObject)doc;
 	}
 		
 	public static LabObject getImage(Element element,LObjImage image){
 		if(image == null) return null;
 
 		String imageURL = convertURLStringToPath(element.getAttribute("url"));
 		if(imageURL != null){
 			LObjImageView view = (LObjImageView)image.getView(null,false,null);
 			if(view != null){
 				if(imageURL.endsWith(".gif") || imageURL.endsWith(".GIF") ||
 				   (qtInstalled && (imageURL.endsWith(".png") || imageURL.endsWith(".PNG") ||
 				   imageURL.endsWith(".tiff") || imageURL.endsWith(".TIFF"))) ||
 				   imageURL.endsWith(".jpg") || imageURL.endsWith(".JPG") ||
 				   imageURL.endsWith(".jpeg") || imageURL.endsWith(".JPEG")){
 						exportImage(imageURL,view);
 				}else{
 					view.loadImage(imageURL);
 				}
 			}
 		}
 		
 		return (LabObject)image;
 	}
 	
 
 	public static LabObject createObj(String objType)
 	{
 		LabObject newObj = null;
 
 		for(int f = 0; f < LabBook.objFactories.length; f++){
 			if(LabBook.objFactories[f] == null) continue;
 			LabObjDescriptor []desc = LabBook.objFactories[f].getLabBookObjDesc();
 			if(desc == null) continue;
 			boolean doExit = false;
 			for(int d = 0; d < desc.length; d++){
 				if(desc[d] == null) continue;
 				if(objType.equals(desc[d].name)){
 					newObj = LabBook.objFactories[f].makeNewObj(desc[d].objType);
 					doExit = true;
 					break;
 				}
 			}
 			if(doExit) break;
 		}
 
 		if(newObj != null){
 			session.storeNew(newObj);
 			if(newIndex == 0){
 				newObj.setName(objType);		    
 			} else {
 				newObj.setName(objType + " " + newIndex);		    
 			}
 			newIndex++;
 		}
 		return newObj;
 	}
 	
 	public static int getIntColorFromStringColor(String linkColor){
 		if(linkColor == null) return 0;
 		String str = linkColor;
 		if(linkColor.length() > 6){
 			str = linkColor.substring(0,6);
 		}else if(linkColor.length() < 6){
 			int addZero = 6 - linkColor.length();
 			for(int i = 0; i < addZero; i++){
 				str += "0";
 			}
 		}
 		int color = 0;
 		int rColor = getIntFromHexString(str.substring(0,2));
 		rColor <<= 16;
 		color |= rColor;
 		int gColor = getIntFromHexString(str.substring(2,4));
 		gColor <<= 8;
 		color |= gColor;
 		int bColor = getIntFromHexString(str.substring(4,6));
 		color |= bColor;
 		return color;
 	}
 	
 	public static float getFloatFromString(String str){
 		float retValue = 0;
 		try{
 			retValue = Float.parseFloat(str);
 		}catch(Exception te){
 			retValue = Float.NaN;
 		}
 		return retValue;		
 	}
 	public static int getIntFromString(String str){
 		int retValue = 0;
 		try{
 			retValue = Integer.parseInt(str);
 		}catch(Exception te){
 			retValue = 0;
 		}
 		return retValue;
 	}
 	public static int getIntFromHexString(String str){
 		int retValue = 0;
 		if(str == null || str.length() < 1) return retValue;
 		int multiplayer = 1;
 		for(int i = str.length() - 1; i >= 0; i--){
 			retValue += (multiplayer*getIntFromHexChar(str.charAt(i)));
 			multiplayer <<= 4;
 		}
 		return retValue;
 	}
 	
 	public static int getIntFromHexChar(char c){
 		if(c >= '0' && c <= '9') return (int)(c - '0');
 		if(c >= 'a' && c <= 'f') return (10 + (int)(c - 'a'));
 		if(c >= 'A' && c <= 'F') return (10 + (int)(c - 'A'));
 		return 0;
 	}
 	public static void exportImage(String str,LObjImageView view){
 		if(!qtInstalled){
 			exportImageWithoutQT(str,view);
 			return;
 		}else if(qtManager != null){
 			qtManager.exportImage(str,view);
 		}
 	}
 
 
 
 	public static void exportImageWithoutQT(String str,LObjImageView view){
 		try{
 			FileInputStream fis = new FileInputStream(str);
 			byte []buffer = new byte[4096];
 			int rb = 0;
 			int currIndex = 0;
 			boolean doExit = false;
 			while(!doExit){
 				rb = fis.read(buffer,currIndex,buffer.length - currIndex);
 				if(rb < 1){
 					doExit = true;
 				}else{
 					currIndex += rb;
 					if(currIndex >= buffer.length){
 						byte []newBuffer = new byte[buffer.length*2];
 						System.arraycopy(buffer,0,newBuffer,0,buffer.length);
 						buffer = newBuffer;
 					}
 				}
 			}
 			java.awt.Image  awtImage = Toolkit.getDefaultToolkit().createImage(buffer);
 			PixelGrabber pg = new PixelGrabber(awtImage,0,0,-1,-1,true);
 			pg.grabPixels();
 			if((pg.status() & ImageObserver.ABORT) != 0) return;
 			int			imageHeight 				= pg.getHeight();
 			int			imageWidth					= pg.getWidth();
 			if(imageHeight <= 0 || imageWidth <= 0) return;
 			Object		pixels 						= pg.getPixels();
 			int			imageBPP 					= 8;
 			int			[]imageCMAP					= org.concord.waba.extra.ui.CCPalette.getPaletteAsInt();
 			int			imageScanlen 				= imageWidth;
 			byte		[]imagePixels				= null;
 			ColorModel cm = pg.getColorModel();
 			if(pixels instanceof byte[]){
 				imagePixels = (byte [])pixels;
 				if(cm instanceof DirectColorModel) return;
 				return;
 			}else if(pixels instanceof int[]){
 				if(cm instanceof IndexColorModel) return;
 				imagePixels				= new byte[imageHeight * imageWidth];
 				int []pgPixel = (int[])pixels;
 				if(pgPixel.length != imageHeight * imageWidth) return;
 				DirectColorModel dcm = (DirectColorModel)cm;
 				for(int p = 0; p < pgPixel.length; p++){
 					int r = dcm.getRed(pgPixel[p]);
 					int g = dcm.getGreen(pgPixel[p]);
 					int b = dcm.getBlue(pgPixel[p]);
 					int pIndex = org.concord.waba.extra.ui.CCPalette.findNearestColor(r,g,b);
 					if(pIndex < 0) pIndex += 256;
 					imagePixels[p] = (byte)pIndex;
 				}
 				if(view != null){
 					view.loadImage(imageWidth,imageHeight,imageBPP,imageCMAP,imageScanlen,imagePixels);
 				}
 				
 			}else{
 				return;
 			}
 		}catch(Exception e){
 		}
 		return;
 	}
 
     // Error handler to report errors and warnings
     private static class MyErrorHandler implements ErrorHandler {
         /** Error handler output goes here */
         private PrintWriter out;
 
         MyErrorHandler(PrintWriter out) {
             this.out = out;
         }
 
         /**
          * Returns a string describing parse exception details
          */
         private String getParseExceptionInfo(SAXParseException spe) {
             String systemId = spe.getSystemId();
             if (systemId == null) {
                 systemId = "null";
             }
             String info = "URI=" + systemId +
                 " Line=" + spe.getLineNumber() +
                 ": " + spe.getMessage();
             return info;
         }
 
         // The following methods are standard SAX ErrorHandler methods.
         // See SAX documentation for more info.
 
         public void warning(SAXParseException spe) throws SAXException {
             out.println("Warning: " + getParseExceptionInfo(spe));
         }
         
         public void error(SAXParseException spe) throws SAXException {
             String message = "Error: " + getParseExceptionInfo(spe);
             throw new SAXException(message);
         }
 
         public void fatalError(SAXParseException spe) throws SAXException {
             String message = "Fatal Error: " + getParseExceptionInfo(spe);
             throw new SAXException(message);
         }
     }
 
 }
