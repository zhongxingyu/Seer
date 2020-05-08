 package legemiddelbokParser;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.input.SAXBuilder;
 
 /**
  * @author Aleks Gisvold
  *
  */
 public class XMLParser {
 	
 	
 	private boolean fileExisting = false;
 	private File oldFile = new File("");
 	private String result;
 	HTMLStripper stripper;
 	private boolean fromURL;
 	private String title;
 	
 	
 	/**
 	 * Constructor
 	 * Takes configuration file
 	 */
 	public XMLParser(File file, boolean fromURL){
 		
 		this.fromURL = fromURL;
 		
 		readXML(file);
 	}
 	
 	/**
 	 * Reads the content of a XMLfile
 	 * @param file
 	 */
 	public void readXML(File file){
 		
 		SAXBuilder myBuilder = new SAXBuilder();
 		
 		Document myDocument = null;
 		
 		try {
 			myDocument = myBuilder.build(file);
 		
 		} catch(JDOMException jde){
 			
 			jde.printStackTrace();
 			
 		} catch (IOException e){
 			
 			e.printStackTrace();
 		}
 		
 		
 		Element root = myDocument.getRootElement();
 		
 		Element collection = root.getChild("Collection");
 		
 		Element uri = collection.getChild("uri");
 		
 		Element directory = collection.getChild("directory");
 		
 		Element tokenizer = root.getChild("tokenizer");
 		
 		Element sentenceBoundary = tokenizer.getChild("sentenceboundary");
 		
 		Element paragraphBoundary = tokenizer.getChild("paragraphboundary");
 		
 		Element allsmall = tokenizer.getChild("allsmall");
 		
 		Element result = tokenizer.getChild("result");
 		
 		Element title = tokenizer.getChild("title");
 		
 		this.title = title.getText();
 		this.result = result.getText();
 		
 		String URLs = uri.getText();
 		
 		String directoryText = directory.getText();
 		
 		String senBoundary = sentenceBoundary.getText();
 		
 		String penBoundary = paragraphBoundary.getText();
 		
 		String allSmall = allsmall.getText();
 		
 		
 		if(fromURL){
 			ArrayList URIList = parseURI(URLs);
 			
 			for(int i = 0; i < URIList.size(); i++){
 				stripper = new HTMLStripper((String)URIList.get(i), this, senBoundary, penBoundary);
 			}
 			
 		} else {
 			stripper = new HTMLStripper(directoryText, this, senBoundary, penBoundary); //Used when reading from file
 		}
 	}
 	
 	
 	
 	/**
 	 * Finds all URIs from the provided XML-file
 	 * @param URLs
 	 * @return
 	 */
 	public ArrayList parseURI(String URLs){
 		
 		StringTokenizer strTokenizer = new StringTokenizer(URLs);
 		
 		ArrayList URIList = new ArrayList();
 		
 		while(strTokenizer.hasMoreTokens()){
 			
 			URIList.add(strTokenizer.nextToken());
 		}
 		
 		return URIList;
 		
 	}
 	
 	/**
 	 * Makes tokenize.xml file which will be sent to a XML-file
 	 * @param title
 	 * @param url
 	 * @param body
 	 * @throws FileNotFoundException
 	 */
 	public void makeXMLTokenizer(String title, String url, String body) throws FileNotFoundException {
 		
 		String fileContent = "";
 		
 		String encoding = "ISO-8859-15";
 		
 		fileContent = fileContent.replaceAll("<\\?xml version=\"1.0\" encoding=\""+ encoding + "\"\\?>", "");
 		
 		fileContent = fileContent.replaceAll("<tokenized>", "");
 		
 		fileContent = fileContent.replaceAll("</tokenized", "");
 		
 		String xml = url.replaceAll(".htm", ".xml");
 		
 		PrintWriter out = new PrintWriter(new FileOutputStream(result+xml));
 		
 		out.println("<?xml version=\"1.0\" encoding=\""+ encoding+"\"?>");
 		
 		out.println("<tokenized>");
 		
 		out.println(fileContent);
 		
 		out.println("<doc id = \"" + url + "\">");
 		
 		out.println("<title>");
 		
 		out.println(title);
 		
 		out.println("</title>");
 		
 		out.println("<body>");
 		
 		out.println(body);
 		
 		out.println("</body>");
 		
 		out.println("</doc>");
 		
 		out.println("</tokenized>");
 		
 		out.flush();
 		
 		out.close();
 	}
 
 	
 	/**
 	 * Reads the content of a file and saves it to a String
 	 * @param filename
 	 * @return
 	 * @throws IOException
 	 */
 	public String readFile(File filename) throws IOException{
 		
 		BufferedReader in = new BufferedReader(new FileReader(filename));
 		
 		String file = "";
 		
 		String temp;
 		
 		
 		while((temp = in.readLine())!=null){
 			
 			file+=temp;
 			
 			file+=" ";
 		}
 		
 		return file;
 	}
 	
 }
