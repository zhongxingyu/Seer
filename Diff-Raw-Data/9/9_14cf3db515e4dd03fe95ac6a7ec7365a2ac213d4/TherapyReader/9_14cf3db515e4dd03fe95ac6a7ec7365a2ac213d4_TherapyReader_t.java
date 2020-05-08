 package parser;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.input.SAXBuilder;
 
 import dataClasses.TherapyChapter;
 
 public class TherapyReader {
 	
 	private static String body;
 	
 	private ArrayList<TherapyChapter> chapters;
 	
 	public ArrayList<TherapyChapter> getChapters(){
 		return chapters;
 	}
 	
 	
 	public TherapyReader(){
 		chapters = new ArrayList<TherapyChapter>();
 		addChapters();
 	}
 	
 	
 	private void addChapters(){
 		
 		File file = new File("resources/NLH/done/T");
 		File[] files = file.listFiles();
 		
 		for(int i = 0; i < files.length; i++){
 			readXML(files[i]);
 			TherapyChapter tc = new TherapyChapter(files[i].getName(), getResult());
 			chapters.add(tc);
 		}
 		
 	}
 	
 	private String getResult(){
 		return body;
 	}
 	
 	
 	public void readXML(File file){
 		
 		SAXBuilder myBuilder = new SAXBuilder();
 		Document myDoc = null;
 		try {
 			myDoc = myBuilder.build(file);
 		} catch (JDOMException jde) {
 			jde.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		Element root = myDoc.getRootElement();
 		
		// Element tokenized = root.getChild("tokenized");
		
		Element doc = root.getChild("doc");
 		Element body = doc.getChild("body");
 		
 		this.body = body.getText();
 		
 	}
 
 	
 	public String readFile(File filename) throws IOException {
 		
 		BufferedReader in = new BufferedReader (new FileReader(filename));
 		String file = "";
 		String temp;
 		while ((temp = in.readLine()) != null) {
 			file += temp;
 			file += " ";
 		}
 		body = file;
 		
 		return file;
 	}
 
 }
