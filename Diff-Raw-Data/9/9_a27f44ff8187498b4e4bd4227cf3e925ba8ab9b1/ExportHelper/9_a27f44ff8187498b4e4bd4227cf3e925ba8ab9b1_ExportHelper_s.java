 package system.controller;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.logging.Logger;
 
 import org.apache.ibatis.io.Resources;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.SAXReader;
 import org.dom4j.io.XMLWriter;
 
 import system.model.Article;
 import system.model.Attachment;
 import system.model.BASE64Shape;
 import system.model.FilePathShape;
 import system.model.FileShape;
 import system.model.FileWebPathShape;
 import system.model.Node;
 import system.model.Operation;
 import system.model.RelatedNode;
 import system.repository.Config;
 
 import com.founder.enp.dataportal.util.FileUtils;
 import com.founder.enp.dataportal.util.StringUtils;
 
 
 public class ExportHelper {
 
 	protected static Logger log = Logger.getLogger("ExportHelper");
 	public static int count = 0;
 	
 	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
 	static Document moddoc=null; 
 	public static Document getModdoc() throws DocumentException, IOException
 	{
 		if(moddoc==null)
 		{
 			SAXReader builder = new SAXReader();
 			moddoc = builder.read(Resources.getResourceAsReader("interface.xml"));
 		}
 		return moddoc;
 	}
 	
 	/**
 	 * xmlļ
 	 * @param article nullֻɵĿ xml
 	 * @param node
 	 * @param operation
 	 * @return
 	 * @throws IOException
 	 * @throws DocumentException
 	 */
 	public static String article2Xml(Article article,Node node,Operation operation) throws IOException, DocumentException
 	{
 		Document doc = (Document) getModdoc().clone();
 		String outFolder = article == null?calcXMLPath(node):calcXMLPath(article);
 		
 		Element root =doc.getRootElement();
 		
 		Element pckage = root.element("Package");
 		setElementValue(pckage, "SourceSystem", operation.getSourceSystem());
 		setElementValue(pckage, "TargetLib", String.valueOf(operation.getTargetLib()));
 		setElementValue(pckage, "TargetNode", node.getTargetNode());
 		setElementValue(pckage, "TargetNodeId", String.valueOf(node.getTargetNodeId()));
 		setElementValue(pckage, "ArticleType", String.valueOf(operation.getArticleType()));
 		setElementValue(pckage, "PublishState", String.valueOf(operation.getPublishState()));
 		setElementValue(pckage, "ObjectType", String.valueOf(operation.getObjectType()));
 		setElementValue(pckage, "Action", String.valueOf(operation.getAction()));
 		String siteid = Config.getConfig().getConfigProperty("dest-siteid", "0");
 		setElementValue(pckage, "SiteId", siteid);
 		if(article == null){
 			setElementValue(pckage, "ObjectType", "0");
 		}
 //		if(node.getIstopic()==1){
 //			setElementValue(pckage, "ObjectType", "2");
 //		}
 		String xmlfilename = null;
 		if(article != null){
 			Element eleArt = root.element("Article");
 			setElementValue(eleArt, "DocID", Integer.toString(article.getDocID()));
 			Calendar cal = Calendar.getInstance();
 			try {
 				cal.setTime(StringUtils.parseDate(article.getNsdate()));
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 			setElementValue(eleArt, "DisplayOrder", StringUtils.getString(""+article.getDisplayOrder()));
 			setElementValue(eleArt, "JabbarMark", article.getJabbarMark());
 			setElementValue(eleArt, "Introtitle", article.getIntrotitle());
 			setElementValue(eleArt, "Subtitle", article.getSubtitle());
 			setElementValue(eleArt, "Abstract", article.getAbstract());
 			setElementValue(eleArt, "Importance", article.getWordCount());
 			setElementValue(eleArt, "WordCount", article.getWordCount());
 			setElementValue(eleArt, "keyword", article.getkeyword());
 			setElementValue(eleArt, "piccount", "0");
 			setElementValue(eleArt, "Nsdate", article.getNsdate());
 			setElementValue(eleArt, "Source", article.getSource());
 			setElementValue(eleArt, "SourceUrl", article.getSourceUrl());
 			setElementValue(eleArt, "Author", article.getAuthor());
 			setElementValue(eleArt, "Editor", article.getEditor());
 			setElementValue(eleArt, "Liability", article.getLiability());
 			setElementValue(eleArt, "Multiattach", article.getMultiattach());
 			setElementValue(eleArt, "Title", article.getTitle());
 			setElementValue(eleArt, "LinkTitle", article.getLinkTitle());
 			setElementValue(eleArt, "RelatedNode", article.getRelatedNode());
 			setElementValue(eleArt, "RelatedTitle", article.getRelatedTitle());
 			setElementValue(eleArt, "Url", article.getUrl());
 			setElementValue(eleArt, "hasTitlePic", StringUtils.isNull(article.getpiclink().getFileshape().getpath(),true)?"0":"1");
 			setElementValue(eleArt, "Content", article.getContent());
 			
 			Element eleAttach = eleArt.element("Attachement");
 			Element eleFileModule = eleAttach.element("file");
 			eleAttach.clearContent();
 			
 			
 			//бͼƬҪͼƬxmlƬηŵһ
 			
 			if(!StringUtils.isNull(article.getpiclink().getFileshape().getpath(),true)){
 				Element eleFile = eleFileModule.createCopy();
 				Attachment att = article.getpiclink();
				att.setAttname("THIS IS PICLINK REF");
 				eleAttach.add(getAttachmentElement(eleFile,att,article.getNsdate()));
 			}
 			
 			for(Iterator<Attachment> it = article.getAttachements().iterator(); it.hasNext();)
 			{
 				Element eleFile = eleFileModule.createCopy();
 				Attachment pic = it.next();
 				eleAttach.add(getAttachmentElement(eleFile,pic,article.getNsdate()));
 			}
 			String filename = String.valueOf(article.getDocID());
 			if(StringUtils.isNull(filename,true) || "0".equals(filename)){
 				filename = article.getTitle().hashCode()+"";
 			}
 			xmlfilename = outFolder  +  "/" +filename+".xml";
 		}else{
 			Element eleArt = root.element("Article");
 			eleArt.clearContent();
 			String filename = String.valueOf(node.getTargetNodeId());
 			if(StringUtils.isNull(filename,true) || filename.trim().equals("0")){
 				filename = node.getTargetNode().hashCode()+"";
 			}
 			xmlfilename = outFolder  +  "/" +filename+".xml";
 		}
 
 		File out = FileUtils.createNewFile(xmlfilename);
 		
 		FileOutputStream fos = new FileOutputStream(out);
 		
 		OutputFormat format = OutputFormat.createPrettyPrint();
 		format.setEncoding("utf-8");
         XMLWriter writer = new XMLWriter(fos, format);
         writer.write(doc);
         writer.close();
         fos.close();
 		System.out.println("д["+ ++count +"]XML:  "+xmlfilename);
 		return xmlfilename; 
 	}
 	
 
 	public static String node2Xml(Node node,Operation operation) throws IOException, DocumentException
 	{
 		return article2Xml(null, node, operation);
 	}
 	
 	public static String calcXMLPath(Node node) {
 		String outFolder = calcXMLPath("node");
 		return outFolder;
 	}
 
 	private static void setElementValue(Element eleArt, String key,
 			int value) {
 		setElementValue(eleArt, key, String.valueOf(value));
 	}
 
 	//һxmlļ·
 	public static String calcXMLPath(Article article)
 	{
 		String outFolder = null;
 		if(article == null){
 			outFolder = calcXMLPath("notime");
 		}else{
 			outFolder = calcXMLPath(article.getNsdate());
 		}
 		return outFolder;
 	}
 	
 
 	//һxmlļ·
 	public static String calcXMLPath(String pubtime) {
 		String outFolder = Config.getConfig().getConfigProperty("xml-save-path");		
 		StringBuilder sb = new StringBuilder();
 		sb.append(outFolder).append("/");
 		if(org.apache.commons.lang.StringUtils.isBlank(pubtime)){
 			pubtime = "1970-01-01";
 		}
 		if(pubtime.equals("notime") || pubtime.equals("node")){
 			sb.append(pubtime);
 		}else{
 			try {
 				sb.append(sdf.format(StringUtils.parseDate(pubtime)));
 			} catch (Exception e) {
 				sb.append("errortime");
 			}
 		}
 		
 		outFolder = sb.toString();
 		File file = new File(outFolder);
 		file.mkdirs();
 		
 		return outFolder;
 	}
 	
 	/**
 	 * eڵµkeyڵֵ Ϊvalue
 	 * @param e
 	 * @param key
 	 * @param value
 	 */
 	private static void setElementValue(Element e,String key,String value) {
 		Element ee = e.element(key);
 		String text = StringUtils.getString(ee.getTextTrim());
 		text = StringUtils.replace(text, "#{"+key+"}", StringUtils.getString(value));
 //		e.element(key).clearContent();
 //		e.element(key).add(DocumentHelper.createCDATA(StringUtils.getString(value)));
 		ee.clearContent();
 		ee.addCDATA(text);
 	}
 
 	private static void addRelatedNode(Article article,String ch){
 		if(StringUtils.isNull(article.getRelatedNode(),true))
 			article.setRelatedNode(ch);
 		else
 			article.setRelatedNode(article.getRelatedNode()+"||"+ch);
 	}
 	
 	private static void addRelatedTitle(Article article,String ch){
 		if(StringUtils.isNull(article.getRelatedTitle(),true))
 			article.setRelatedTitle(ch);
 		else
 			article.setRelatedTitle(article.getRelatedTitle()+"||"+ch);
 	}
 	
 	public static void addRelatedNode(Article article,RelatedNode relatedNode){
 		addRelatedNode(article, relatedNode.getNodePath());
 		addRelatedTitle(article, relatedNode.getLinktitle());
 	}
 	
 	/**
 	 * "~d~c~b~a"ת "a~b~c~d"
 	 * 
 	 * @param hierarchy
 	 * @return
 	 */
 	public static String reverseHierarchy(String hierarchy){
 		if(StringUtils.isNull(hierarchy,true))
 			return null;
 		String[] nodes = hierarchy.split("~");
 		StringBuffer sb = new StringBuffer();
 		for(int i=nodes.length-1;i>=0;i--){
 			if(StringUtils.isNull(nodes[i],true)){
 				continue;
 			}			
 			sb.append(nodes[i]);
 			if(i!=0){
 				sb.append("~");
 			}
 		}
 		if(sb.charAt(sb.length()-1)=='~'){
 			sb.deleteCharAt(sb.length()-1);
 		}
 		return sb.toString();
 	}
 	private static Element getAttachmentElement(Element eleFile, Attachment att,String pubtime){
 		eleFile.addAttribute("type", String.valueOf(att.getType()));
 		eleFile.addAttribute("length", "0");
 		
 		setElementValue(eleFile, "filename", att.getAttname());
 		setElementValue(eleFile, "attdesc", att.getAttdesc());
 		FileShape shape = att.getFileshape();
 		String path = shape.getpath();
 		if(shape instanceof BASE64Shape){
 			setElementValue(eleFile, "filecode", path);
 			setElementValue(eleFile, "FilePath", "");
 			setElementValue(eleFile, "FileWebPath", "");
 		}else if (shape instanceof FilePathShape){
 			String xmldir = calcXMLPath(pubtime);
 			String basepath = Config.getConfig().getConfigProperty("att-src-path");
 			if(StringUtils.isNull(basepath,true)){
 				basepath = "";
 			}else{
 				basepath += "/";
 			}
 			File outfile = copyFileToDir(basepath+path, xmldir);
 			setElementValue(eleFile, "filecode", "");
 			setElementValue(eleFile, "FilePath", outfile.getName());
 			setElementValue(eleFile, "FileWebPath", "");
 		}else if(shape instanceof FileWebPathShape){
 			setElementValue(eleFile, "filecode", "");
 			setElementValue(eleFile, "FilePath", "");
 			setElementValue(eleFile, "FileWebPath", path);
 		}else{
 			setElementValue(eleFile, "filecode", path);
 			setElementValue(eleFile, "FilePath", path);
 			setElementValue(eleFile, "FileWebPath", path);
 		}
 		return eleFile;
 	}
 	
 
 	/**
 	 * @param inputfile
 	 * @param outputdir
 	 * @return Ŀļ
 	 * @throws Exception
 	 */
 	public static File copyFileToDir(String inputfile, String outputdir) {
 		File in = new File(inputfile);
 		File out = new File(outputdir+"/"+in.getName());
 		
 		if(!in.exists()){
 			log.info("inputfile is not exist: "+inputfile +", failed to copy");
 			return out;
 		}
 		try {
 			File parent = out.getParentFile();
 			if (!parent.exists()) {
 				parent.mkdirs();
 			}
 			if(!out.exists())
 				out.createNewFile();
 			else
 				out.delete();
 			org.apache.commons.io.FileUtils.copyFile(in, out);
 		} catch (IOException e) {
 			System.err.println(e.getMessage());
 		}
 		return out;
 	}
 }
