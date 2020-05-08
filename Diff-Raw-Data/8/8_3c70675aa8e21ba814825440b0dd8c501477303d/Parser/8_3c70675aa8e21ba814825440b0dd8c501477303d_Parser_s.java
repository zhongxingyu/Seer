 package tcwi.xml;
 import java.io.File;
 import java.io.IOException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import tcwi.TCWIFile.ErrorFile;
 import tcwi.TCWIFile.ParserError;
 import tcwi.TCWIFile.TypeError;
 import tcwi.exception.Exceptions;
 
 /**
  * A XML-Parser for TypeChefWebInt
  * 
  */
 public class Parser {
 	private String setting_file;
 	private Exceptions exception = new Exceptions();
 	
 	/**
 	 * Creates an XML Object
 	 * @param setting_file An absolute Path to a XML-Document
 	 */
 	public Parser(String setting_file){
 		this.setting_file = setting_file;
 	}
 	
 	/**
 	 * Reads a setting in a given setting-file<br>
 	 * @param settings A String arr for the XML-Path<br><br>
 	 * 
 	 * Example:<br>
 	 * &lt;test&gt;<br>
 	 * &nbsp;&nbsp;&lt;node&gt;<br>
 	 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;setting path="/foo/bar/foobar.txt" /&gt;<br>
 	 * &nbsp;&nbsp;&lt;/node&gt;<br>
 	 * &lt;/test&gt;<br><br>
 	 * 
 	 * Your String-Array: <br>
 	 * String[] xpath = {"test","node","setting","path"};<br><br>
 	 * 
 	 * Settings like &lt;test&gt;foobar&lt;/test&gt; are not excepted!<br>
 	 * 
 	 * @return Your setting as a String
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws IOException
 	 */
 	public String read_setting(String[] settings) throws ParserConfigurationException, SAXException, IOException{
 	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 	    DocumentBuilder builder = factory.newDocumentBuilder();
 	    Document document = builder.parse( new File(setting_file) );
 	    NodeList list = document.getChildNodes();
 
 	    //Go to a deeper path
 	    for(int i=0;i<settings.length;i++){
 	    	if(settings.length-2>i){
 		    	for(int j=0;j<list.getLength();j++){
 		    		if(list.item(j).getNodeName()==settings[i]){
 		    			list = list.item(j).getChildNodes();
 		    			break;
 		    		}
 		    	}
 	    	}else{
 	    		//Node found, try to get his specific value
 	    		for(int j=0;j<list.getLength();j++){
 	    			if(list.item(j).getNodeName()==settings[i]){
 	    				NamedNodeMap attr = list.item(j).getAttributes();
 	    				for(int k=0;k<attr.getLength();k++){
 	    					if(attr.item(k).getNodeName()==settings[i+1]){
 	    						return attr.item(k).getNodeValue();
 	    					}
 	    				}
 	    			}
 	    		}
 	    	}
 	    }
 
 		return "";
 	}
 	
 	/**
 	 * Remove whitespaces, tabs and returns from a given string. Whitespaces will be removed
 	 * at the begin and at the end of the string.<br>
 	 * Ex. String "   this is a test  " --> "this is a test"
 	 * @param str
 	 * @return
 	 */
 	private static String removeWhites(String str){
 		str = str.replace("\t", "");
 		str = str.replace("\r", "");
 		str = str.replace("\n", "");
 		
 		String newStr = str;
 		for(int i=0;i<newStr.length();i++){
 			if(newStr.charAt(i)==' '){
 				str = str.substring(1);
 			}else{
 				break;
 			}
 		}
 		newStr = str;
 		for(int i=newStr.length()-1;i>0;i--){
 			if(newStr.charAt(i)==' '){
 				str = str.substring(0,str.length()-1);
 			}else{
 				break;
 			}
 		}
 		return str;
 	}
 	
 	/**
 	 * This method get all data from given .c.xml and returns it as an ErrorFile
 	 * @param path
 	 * @return
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws IOException
 	 */
 	public static ErrorFile getAllErrors(String path) throws ParserConfigurationException, SAXException, IOException{
 		ErrorFile err = new ErrorFile();
 		err.setPath(path.substring(0,path.length()-6));
 		
 		File f = new File(path);
 		err.setFileExist(f.exists());
 		File fDBG = new File(path.substring(0,path.length()-6)+".dbg");
 		if(fDBG.exists()&&!f.exists()){
 			err.setCompileError(true);
 		}else{
 			err.setCompileError(false);
 		}
 		
 		if(err.isFileExist()){
 		    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		    DocumentBuilder builder = factory.newDocumentBuilder();
 		    Document document = builder.parse(new File(path));
 		    NodeList list = document.getChildNodes().item(0).getChildNodes();
 					    
 		    for(int i=0;i<list.getLength();i++){
 		    	if(list.item(i).getNodeName().equals("typeerror")){
 		    		String featurestr = "", msg = "", severity = "", fromFile = "",  fromLine = "", fromCol = "", toFile = "", toLine = "", toCol = "";
 			    	boolean firstPos = true;
 		    		for(int j=0;j<list.item(i).getChildNodes().getLength();j++){
 		    			if(list.item(i).getChildNodes().item(j).getNodeName().equals("featurestr")){
 		    				featurestr = removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(0).getNodeValue());
 		    			}
 		    			if(list.item(i).getChildNodes().item(j).getNodeName().equals("msg")){
 		    				msg = removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(0).getNodeValue());
 		    			}
 		    			if(list.item(i).getChildNodes().item(j).getNodeName().equals("severity")){
 		    				severity = removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(0).getNodeValue());
 		    			}
 		    			if(list.item(i).getChildNodes().item(j).getNodeName().equals("position")){
 		    				for(int k=0;k<list.item(i).getChildNodes().item(j).getChildNodes().getLength();k++){
 			    				if(firstPos==true){
 			    					if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("file")){
 					    				fromFile = removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue());
 			    					}
 			    					if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("line")){
 					    				fromLine = removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue());
 			    					}
 			    					if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("col")){
 					    				fromCol = removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue());
 			    					}
 			    					firstPos = false;
 			    				}else{
 			    					if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("file")){
 					    				toFile = removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue());
 			    					}
 			    					if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("line")){
 					    				toLine = removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue());
 			    					}
 			    					if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("col")){
 					    				toCol = removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue());
 			    					}
 			    				}
 		    				}
 		    			}
 		    		}
 		    		TypeError tErr = new TypeError(featurestr, msg, severity, fromFile,  fromLine, fromCol, toFile, toLine, toCol);
 		    		err.addError(tErr);
 		    	}
 		    	if(list.item(i).getNodeName().equals("parsererror")){
 		    		String featurestr = "", msg = "", file = "", line = "", col = "";
 		    		for(int j=0;j<list.item(i).getChildNodes().getLength();j++){
 		    			if(list.item(i).getChildNodes().item(j).getNodeName().equals("featurestr")){
 		    				featurestr = removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(0).getNodeValue());
 		    			}
 		    			if(list.item(i).getChildNodes().item(j).getNodeName().equals("msg")){
 		    				msg = removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(0).getNodeValue());
 		    			}
 		    			if(list.item(i).getChildNodes().item(j).getNodeName().equals("position")){
 		    				for(int k=0;k<list.item(i).getChildNodes().item(j).getChildNodes().getLength();k++){
 		    					if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("file")){
 				    				file = removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue());
 		    					}
 		    					if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("line")){
 				    				line = removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue());
 		    					}
 		    					if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("col")){
 				    				col = removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue());
 		    					}
 		    				}
 		    			}
 		    		}
 		    		ParserError pErr = new ParserError(featurestr, msg, file, line, col);
 		    		err.addError(pErr);
 		    	}
 		    }
 		}
 	    return err;
 	}
 	
 	public static ProjectFile getProject(String path) throws ParserConfigurationException, SAXException, IOException{
 		ProjectFile p = new ProjectFile();
 	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 	    DocumentBuilder builder = factory.newDocumentBuilder();
 	    Document document = builder.parse(new File(path));
 	    NodeList list = document.getChildNodes().item(0).getChildNodes();
 		
 	    System.out.println(list.item(1).getChildNodes().item(1).getChildNodes().item(1).getChildNodes().item(0).getNodeValue());
 	    
 	    for(int i=0;i<list.getLength();i++){
 	    	if(list.item(i).getNodeName().equals("header")){
 	    		for(int j=0;j<list.item(i).getChildNodes().getLength();j++){
 	    			if(list.item(i).getChildNodes().item(j).getNodeName().equals("project")){
 	    				for(int k=0;k<list.item(i).getChildNodes().item(j).getChildNodes().getLength();k++){
 			    			if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("idname")){
 			    				p.setIdname(removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue()));
 			    			}
 			    			if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("fullname")){
 			    				p.setFullname(removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue()));
 			    			}
 			    			if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("version")){
 			    				p.setVersion(removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue()));
 			    			}
 			    			if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("path")){
 			    				p.setPath(removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue()));
 			    			}
 			    			if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("type")){
 			    				p.setType(removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue()));
 			    			}
 	    				}
 	    			}
 	    			if(list.item(i).getChildNodes().item(j).getNodeName().equals("delta")){
 	    				for(int k=0;k<list.item(i).getChildNodes().item(j).getChildNodes().getLength();k++){
 			    			if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("hasdeltas")){
 			    				p.setHasDeltas(removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue()));
 			    			}
 			    			if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("mainproject")){
 			    				p.setMainproject(removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue()));
 			    			}
 	    				}
 	    			}
 	    			if(list.item(i).getChildNodes().item(j).getNodeName().equals("build")){
 	    				for(int k=0;k<list.item(i).getChildNodes().item(j).getChildNodes().getLength();k++){
 			    			if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("builder")){
 			    				p.setBuilder(removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue()));
 			    			}
 			    			if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("date")){
 			    				p.setDate(removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue()));
 			    			}
 			    			if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("time")){
 			    				p.setTime(removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue()));
 			    			}
 	    				}
 	    			}
 	    			if(list.item(i).getChildNodes().item(j).getNodeName().equals("stats")){
 	    				for(int k=0;k<list.item(i).getChildNodes().item(j).getChildNodes().getLength();k++){
 			    			if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("parsererrors")){
 			    				p.setParsererrors(removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue()));
 			    			}
 			    			if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("typeerrors")){
 			    				p.setTypeerrors(removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue()));
 			    			}
 			    			if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("excludedfiles")){
 			    				p.setExcludedfiles(removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue()));
 			    			}
 			    			if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("compileerrors")){
 			    				p.setCompileerrors(removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue()));
 			    			}
 	    				}
 	    			}
 	    		}
 	    	}
 	    	if(list.item(i).getNodeName().equals("errors")){
 	    		for(int j=0;j<list.item(i).getChildNodes().getLength();j++){
 	    			if(list.item(i).getChildNodes().item(j).getNodeName().equals("file")){
 	    				//TODO: Decision between CompareFile and ErrorFile
 	    				ErrorFile file = new ErrorFile();
 	    				for(int k=0;k<list.item(i).getChildNodes().item(j).getChildNodes().getLength();k++){
 			    			if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("path")){
 			    				file.setPath(removeWhites(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue()));
 			    			}
 			    			if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("fileexist")){
 			    				if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue().equals("true")){
 			    					file.setFileExist(true);
 			    				}else{
 			    					file.setFileExist(false);
 			    				}
 			    			}
 			    			if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("compileerror")){
			    				file.setPath(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue());
 			    				if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(0).getNodeValue().equals("true")){
 			    					file.setCompileError(true);
 			    				}else{
 			    					file.setCompileError(false);
 			    				}
 			    			}
 			    			if(list.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("errorlist")){
 			    				NodeList shortList = list.item(i).getChildNodes().item(j).getChildNodes().item(k).getChildNodes();
 			    				
 			    				for(int l=0;l<shortList.getLength();l++){
 			    					if(shortList.item(l).getNodeName().equals("parsererrors")){
 			    						ParserError pErr = new ParserError();
 		    							for(int m=0;m<shortList.item(l).getChildNodes().getLength();m++){
 		    								if(shortList.item(l).getChildNodes().item(m).getNodeName().equals("featurestr")){
 		    									pErr.setFeaturestr(removeWhites(shortList.item(l).getChildNodes().item(m).getChildNodes().item(0).getNodeValue()));
 		    								}
 		    								if(shortList.item(l).getChildNodes().item(m).getNodeName().equals("msg")){
 		    									pErr.setMsg(removeWhites(shortList.item(l).getChildNodes().item(m).getChildNodes().item(0).getNodeValue()));
 		    								}
 		    								if(shortList.item(l).getChildNodes().item(m).getNodeName().equals("position")){
 		    									for(int n=0;n<shortList.item(l).getChildNodes().item(m).getChildNodes().getLength();n++){
 		    										if(shortList.item(l).getChildNodes().item(m).getChildNodes().item(n).getNodeName().equals("file")){
 		    											pErr.setFile(removeWhites(shortList.item(l).getChildNodes().item(m).getChildNodes().item(n).getChildNodes().item(0).getNodeValue()));
 		    										}
 		    										if(shortList.item(l).getChildNodes().item(m).getChildNodes().item(n).getNodeName().equals("line")){
 		    											pErr.setLine(removeWhites(shortList.item(l).getChildNodes().item(m).getChildNodes().item(n).getChildNodes().item(0).getNodeValue()));
 		    										}
 		    										if(shortList.item(l).getChildNodes().item(m).getChildNodes().item(n).getNodeName().equals("col")){
 		    											pErr.setCol(removeWhites(shortList.item(l).getChildNodes().item(m).getChildNodes().item(n).getChildNodes().item(0).getNodeValue()));
 		    										}
 		    									}
 		    								}
 		    							}
 			    						file.addError(pErr);
 			    					}
 			    					if(shortList.item(l).getNodeName().equals("typeerrors")){
 			    						TypeError tErr = new TypeError();
 			    						boolean firstPos = true;
 		    							for(int m=0;m<shortList.item(l).getChildNodes().getLength();m++){
 		    								if(shortList.item(l).getChildNodes().item(m).getNodeName().equals("featurestr")){
 		    									tErr.setFeaturestr(removeWhites(shortList.item(l).getChildNodes().item(m).getChildNodes().item(0).getNodeValue()));
 		    								}
 		    								if(shortList.item(l).getChildNodes().item(m).getNodeName().equals("severity")){
 		    									tErr.setFeaturestr(removeWhites(shortList.item(l).getChildNodes().item(m).getChildNodes().item(0).getNodeValue()));
 		    								}
 		    								if(shortList.item(l).getChildNodes().item(m).getNodeName().equals("msg")){
 		    									tErr.setMsg(removeWhites(shortList.item(l).getChildNodes().item(m).getChildNodes().item(0).getNodeValue()));
 		    								}
 		    								if(shortList.item(l).getChildNodes().item(m).getNodeName().equals("position")){
 		    									for(int n=0;n<shortList.item(l).getChildNodes().item(m).getChildNodes().getLength();n++){
 			    									if(firstPos){
 			    										if(shortList.item(l).getChildNodes().item(m).getChildNodes().item(n).getNodeName().equals("file")){
 			    											tErr.setFromFile(removeWhites(shortList.item(l).getChildNodes().item(m).getChildNodes().item(n).getChildNodes().item(0).getNodeValue()));
 			    										}
 			    										if(shortList.item(l).getChildNodes().item(m).getChildNodes().item(n).getNodeName().equals("line")){
 			    											tErr.setFromLine(removeWhites(shortList.item(l).getChildNodes().item(m).getChildNodes().item(n).getChildNodes().item(0).getNodeValue()));
 			    										}
 			    										if(shortList.item(l).getChildNodes().item(m).getChildNodes().item(n).getNodeName().equals("col")){
 			    											tErr.setFromCol(removeWhites(shortList.item(l).getChildNodes().item(m).getChildNodes().item(n).getChildNodes().item(0).getNodeValue()));
 			    										}
 				    									firstPos = false;
 			    									}else{
 			    										if(shortList.item(l).getChildNodes().item(m).getChildNodes().item(n).getNodeName().equals("file")){
 			    											tErr.setToFile(removeWhites(shortList.item(l).getChildNodes().item(m).getChildNodes().item(n).getChildNodes().item(0).getNodeValue()));
 			    										}
 			    										if(shortList.item(l).getChildNodes().item(m).getChildNodes().item(n).getNodeName().equals("line")){
 			    											tErr.setToLine(removeWhites(shortList.item(l).getChildNodes().item(m).getChildNodes().item(n).getChildNodes().item(0).getNodeValue()));
 			    										}
 			    										if(shortList.item(l).getChildNodes().item(m).getChildNodes().item(n).getNodeName().equals("col")){
 			    											tErr.setToCol(removeWhites(shortList.item(l).getChildNodes().item(m).getChildNodes().item(n).getChildNodes().item(0).getNodeValue()));
 			    										}
 			    									}
 		    									}
 		    								}
 		    							}
 			    						file.addError(tErr);
 			    					}
 			    				}
 			    			}
 	    				}
 	    				p.addFile(file);
 	    			}
 	    		}
 	    	}
 	    }
 	    return p;
 	}
 
 	public String getSetting_file() {
 		return setting_file;
 	}
 	
 	/**
 	 * A method to get an setting from an given setting-file
 	 * @param globalSettings
 	 * @param xpath
 	 * @return
 	 */
 	public String getSetting(String[] xpath){
 		Parser parser = new Parser(this.setting_file);
 		String pathOutput = "";
 
 		try{
 			pathOutput = parser.read_setting(xpath);
 		} catch (IOException e) {
 			exception.throwException(1, e, true, "");
 		} catch (Exception e) {
 			String path = "";
 			for(int i=0;i<xpath.length;i++){
 				path += xpath[i]+" ";
 			}
 			exception.throwException(2, e, true, path);
 		}
 		return pathOutput;		
 	}
 	
 	/**
 	 * A specific method to get the project-path from the WebInt
 	 * @param globalSettings
 	 * @return
 	 */
 	public String getSetting_ProjectPath(){
 		String[] xpath = {"settings","global","projects","path"};
 		return this.getSetting(xpath);
 	}
 	
 	/**
 	 * A specific method to get the treeview-path
 	 * @param globalSettings
 	 * @return
 	 */
 	public String getSetting_TreeviewPath(){
 		String[] xpath = {"settings","website","generic","treeview","path"};
 		return this.getSetting(xpath);
 	}
 
 	/**
 	 * A specific method to get the treeview-icons-path
 	 * @param globalSettings
 	 * @return
 	 */
 	public String getSetting_TreeviewIconsPath(){
 		String[] xpath = {"settings","website","generic","treeview","icons"};
 		return this.getSetting(xpath);
 	}
 
 	/**
 	 * A specific method to get the project default URI
 	 * @param globalSettings
 	 * @return
 	 */
 	public String getSetting_WebsiteDefaultURI(){
 		String[] xpath = {"settings","global","website","defaultURI"};
 		return this.getSetting(xpath);
 	}
 
 }
