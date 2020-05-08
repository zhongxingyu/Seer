 package awesome;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Calendar;
 
 import javax.xml.bind.DatatypeConverter;
 import javax.xml.parsers.*;
 import javax.xml.transform.*;
 import javax.xml.transform.dom.*;
 import javax.xml.transform.stream.*;
 import javax.xml.xpath.*;
 
 import org.w3c.dom.*;
 
 import org.w3c.dom.Document;
 
 /**
  * This class is used to store the current working directory and (later)
  * to encapsulate the xml-cache functions. Only one MusicLibrary should exist
  * at the same time.
  *
  */
 
 public class MusicLibrary {
 	private Directory rootDir;
 	private File xmlLocation;
 	private DocumentBuilderFactory docFactory;
 	private DocumentBuilder docBuilder;
 	private Document doc = null;
 	
 	/**
 	 * this constructor reads the cache (if one exists) and builds the directory tree.
 	 * @param rootDir
 	 */
 	public MusicLibrary(Directory rootDir){
 		this.rootDir = rootDir;
 		this.xmlLocation = new File(rootDir.getFile(), "cache.xml");		
 		try {
 			readXML();			
 			DirectoryWalker.buildFileTree(this, rootDir); //scan for mp3s
 		} catch (Exception e) {
 			if(AwesomeID3.getController().getView() != null){
 				AwesomeID3.getController().getView().presentException(e);
 			} else {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * determines whether a cache file already exists.
 	 * @return
 	 */
 	
 	public boolean cacheExists(){
 		return xmlLocation.exists();
 	}
 	
 	/**
 	 * @return the rootDir
 	 */
 	public Directory getRootDirectory() {
 		return rootDir;
 	}
 	
 	private void readXML() throws Exception {		
 		if(!xmlLocation.exists()) return;
 		docFactory = DocumentBuilderFactory.newInstance();
 		docBuilder = docFactory.newDocumentBuilder();
 		doc = docBuilder.parse(xmlLocation);
 		
 		// Todo call buildFromCache on the root diretory tag and replace the music lib with the value
 	}
 	
 	private FilePathInfo buildFromCache(Element elem){
 		NodeList children = elem.getChildNodes();
 		if(elem.getNodeName() == "file") {
 			MP3File mp3 = new MP3File(new File(elem.getAttribute("path")));
 			mp3.setTitle(elem.getElementsByTagName("title").item(0).getFirstChild().getNodeValue());
 			mp3.setArtist(elem.getElementsByTagName("artist").item(0).getFirstChild().getNodeValue());
 			mp3.setAlbum(elem.getElementsByTagName("album").item(0).getFirstChild().getNodeValue());
 			mp3.setYear(elem.getElementsByTagName("year").item(0).getFirstChild().getNodeValue());
 			Element cover = (Element) elem.getElementsByTagName("cover").item(0);
 			if(cover != null){
 				mp3.setCover(DatatypeConverter.parseBase64Binary(cover.getElementsByTagName("data").item(0).getTextContent()));
 				mp3.setCoverMimeType(cover.getAttribute("mimetype"));
 				mp3.setCoverDescription(cover.getElementsByTagName("description").item(0).getNodeValue());
 			}
 			
 			NodeList el = elem.getElementsByTagName("ignoredtag");
 			for(int i = 0; i < el.getLength(); i++){
 				Element itag = (Element) el.item(i);
 				String id = itag.getAttribute("frameid");
 				int size = Integer.parseInt(itag.getAttribute("size"));
 				int flags = Integer.parseInt(itag.getAttribute("flags"));
 				byte[] data = DatatypeConverter.parseBase64Binary(itag.getTextContent());
 				ID3Frame frame = new ID3Frame(id, size, flags, data);
 				mp3.addUnknownFrame(frame);
 			}
 			
 			mp3.setTagSize(Integer.parseInt(elem.getAttribute("tagsize")));
 			mp3.setDirty(false);
 			return mp3;
 		} else {
 			Directory directory = new Directory(new File(elem.getAttribute("path")));
 			for(int i=0; i<children.getLength(); i++) {
 				directory.addChild(buildFromCache((Element) children.item(i)));
 			}
 			return directory;
 		}
 	}
 	
 	/**
 	 * writes the cache for all files stored in the library.
 	 */
 	
 	public void saveXML(){	
 		try {
 			// initializiation
 			docFactory = DocumentBuilderFactory.newInstance();
 			docBuilder = docFactory.newDocumentBuilder();
 			
 			// root elem
 			doc = docBuilder.newDocument();
 			Element rootElement = doc.createElement("cache");
 			rootElement.setAttribute("timestamp", "" + Calendar.getInstance().getTimeInMillis());
 			doc.appendChild(rootElement);
 		
 			// document elems
 			rootElement.appendChild(buildDirTree(rootDir));
 			
 			// write XML
 			TransformerFactory transformerFactory = TransformerFactory.newInstance();
 			Transformer transformer = transformerFactory.newTransformer();
 			DOMSource source = new DOMSource(doc);
 			
 			StreamResult result =  new StreamResult(xmlLocation);
 			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 			transformer.transform(source, result);
 		} catch(Exception ex){
 			if(AwesomeID3.getController().getView() != null){
 				AwesomeID3.getController().getView().presentException(ex);
 			} else {
 				ex.printStackTrace();
 			}
 		}
 	}
 	
 	private Element buildDirTree(FilePathInfo file) {
 		if(file instanceof MP3File) {
 			MP3File f = (MP3File) file;
 			Element mp3 = doc.createElement("file");
 			mp3.setAttribute("name", f.getFile().getName());
 			mp3.setAttribute("path", f.getFile().getPath());
 			mp3.setAttribute("tagsize", "" + f.getTagSize());
 			Element title = doc.createElement("title");
 			title.setTextContent(f.getTitle());
 			Element artist = doc.createElement("artist");
 			artist.setTextContent(f.getArtist());
 			Element album = doc.createElement("album");
 			album.setTextContent(f.getAlbum());
 			Element year = doc.createElement("year");
 			year.setTextContent(f.getYear());
 			
 			mp3.appendChild(title);
 			mp3.appendChild(artist);
 			mp3.appendChild(album);
 			mp3.appendChild(year);
 			
 			if(f.getCover() != null){
 				Element cover = doc.createElement("cover");
 				cover.setAttribute("size", ""+f.getCover().length);
 				Element mimetype = doc.createElement("mimetype");
 				mimetype.setTextContent(f.getCoverMimeType());
 				cover.appendChild(mimetype);
 				Element description = doc.createElement("description");
 				description.setTextContent(f.getCoverDescription());
 				cover.appendChild(description);				
 				Element data = doc.createElement("data");
 				data.setTextContent(DatatypeConverter.printBase64Binary(f.getCover()));
 				cover.appendChild(data);
 				mp3.appendChild(cover);
 			}
 			
 			for(ID3Frame fr : f.getUnknownFrames()){
 				Element frame = doc.createElement("ignoredtag");
 				frame.setAttribute("frameid", fr.getId());
 				frame.setAttribute("size", "" + fr.getSize());
 				frame.setAttribute("flags", "" + fr.getFlags());
 				frame.setTextContent(DatatypeConverter.printBase64Binary(fr.getData()));
 				mp3.appendChild(frame);
 			}
 			
 			return mp3;
 		} else {
 			Element dir = doc.createElement("folder");
 			for(FilePathInfo fpi:file.listFiles()) {
 				dir.appendChild(buildDirTree(fpi));
 			}
 			dir.setAttribute("name", file.getFile().getName());
 			return dir;
 		}
 	}
 	
 	/**
 	 * saves the ID3Tags of all dirty files and rewrites the cache.
 	 * @throws IOException
 	 */
 	
 	public void saveAllDirtyFiles() throws IOException{
 		saveDirtyMP3s(rootDir);
 		saveXML();
 	}
 	
 
 	private void saveDirtyMP3s(Directory dir) throws IOException {
 		for(FilePathInfo fpi : dir.listFiles()){
 			if(fpi.isDirectory()){
 				saveDirtyMP3s((Directory)fpi);
 			} else {
 				MP3File mp3 = (MP3File) fpi;
 				if(mp3.isDirty())
 					ID3Parser.save(mp3);
 			}
 		}
		saveXML();
 	}
 	
 	/**
 	 * determines whether at least one file in the library is dirty.
 	 * @return
 	 */
 	
 	public boolean checkDirty()
 	{		
 		return checkForDirtyMP3s(rootDir);
 	}
 		
 	private boolean checkForDirtyMP3s(Directory dir) {		
 		for(FilePathInfo fpi : dir.listFiles()){
 			if(fpi.isDirectory()){
 				if(checkForDirtyMP3s((Directory)fpi)) return true;				
 			}
 			else {
 				MP3File mp3 = (MP3File) fpi;
 				if(mp3.isDirty()) 
 					return true;
 			}			
 		}	
 		
 		return false;
 	}
 	
 	/**
 	 * reads the data of an mp3 file from the cache if a cache exists and has an entry for the given file.
 	 * Otherwise it returns null.
 	 * @param f 
 	 * @return MP3File with data from cache or null.
 	 * @throws XPathExpressionException
 	 * @throws IOException 
 	 */
 
 	public MP3File parseMP3FromCache(File f) throws XPathExpressionException{
 		if(doc == null) return null;
 		XPathFactory xPathfactory = XPathFactory.newInstance();
 		XPath xpath = xPathfactory.newXPath();
 		XPathExpression expr = xpath.compile("//file[@path='" + f.getPath() + "']");
 		NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
 		Element e = (Element) nl.item(0);
 		if(e != null && xmlLocation.lastModified() > f.lastModified()){
 			return (MP3File) buildFromCache(e);
 		} else 
 			return null;
 	}
 }
 
 
 
 
 
