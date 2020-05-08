 import java.io.*;
 import java.net.URL;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.Vector;
 import org.htmlcleaner.*;
 
 
 public class Mehrnews extends Site {
 	
 	String tableName= "mehrnews";
 	DataBase db;
 	Logger logger;
 	String tableNameFields = "id, dateOfFetch";
 	Vector<URL> urls;
 	public Mehrnews(String dbFilePath, String savePath){
 		title = "Mehrnews";
 		url = "http://www.mehrnews.ir/txtNewsView_fa.aspx?t=News&Page=1";
 		if(savePath != null){
			this.savePath = savePath + "\\sites\\mehrnews\\";
 			//generating logs save path based on current save path
 			String logsSavePath = this.savePath.substring(0, this.savePath.lastIndexOf('\\'));
 			logsSavePath = logsSavePath.substring(0, logsSavePath.lastIndexOf('\\') + 1);
 			//logsSavePath = logsSavePath.substring(0, logsSavePath.lastIndexOf('\\') + 1);
 			logger = new Logger(logsSavePath + "logs\\");
 		}
 		else{
 			this.savePath = "D:\\crawler\\sites\\mehrnews\\";
 		}
 		db = new DataBase(dbFilePath);
 		urls = new Vector<URL>(500);
 	}
 	
 	//gets the listPage of the address http://www.mehrnews.ir/txtNewsView_fa.aspx?t=News&Page=#{id}
 	//inserts all the links into a vector of URLs and invokes a method that tries to save each link.
 	public String getListPage(int id) throws Exception{
 		String className = "news_title";
 		String format = url.substring(0, url.length()-1) + id;
 		String dateOfThePage = "";
 		String result = savePath + dateOfThePage + ".html";
 		URL mehrnews = new URL(format);
 		
 		CleanerProperties cp = new CleanerProperties();
 		cp.setRecognizeUnicodeChars(true);
 		
 		HtmlCleaner cleaner = new HtmlCleaner(cp);
 		TagNode rootNode = cleaner.clean(mehrnews);
 		
 		TagNode aElements[] = rootNode.getElementsByName("a", true);
 		String classType;
 		String href;
 		for(int i = 0; aElements != null && i < aElements.length; i++){
 			classType = aElements[i].getAttributeByName("class");
 			if(classType != null && classType.equals(className)){
 				href = aElements[i].getAttributeByName("href");
 				href = "http://www.mehrnews.ir/" + href;
 				urls.add(new URL(href));
 			}
 		}
 		this.getPagesInVector(id);
 		return result;
 	}
 	
 	public void getPagesInVector(int id) throws Exception {
 		if(urls.isEmpty()){
 			logger.logEmptyListPage(id);
 			throw new Exception("no links are in vector. Page ID: " + id);
 		}
 		URL url;
 		
 		for(int k = 0; k < urls.size(); k++){
 			url = urls.elementAt(k);
 			getURL(url, id);
 			
 		}
 		urls.clear();
 	}
 
 	public void getURL(URL url, int id) throws Exception{
 		String className = "news_body_print";
 		String newsID = url.toString().substring(45);
 		newsID = "'" + newsID + "'";
 		if(!db.doesLinkExistsInDatabase(tableName, newsID)){
 			//save the page
 			String outputfile = savePath + url.toString().substring(45) + ".txt";
 			
 			FileOutputStream fos = new FileOutputStream(outputfile);
 			Writer out = new OutputStreamWriter(fos, "UTF8");
 			
 			CleanerProperties cp = new CleanerProperties();
 			cp.setRecognizeUnicodeChars(true);
 			HtmlCleaner cleaner = new HtmlCleaner(cp);
 			TagNode rootNode = cleaner.clean(url);
 			
 			TagNode spanElements[] = rootNode.getElementsByName("span", true);
 			String classType;
 			StringBuffer sb;
 			for(int i = 0; spanElements != null && i < spanElements.length; i++){
 				classType = spanElements[i].getAttributeByName("class");
 				if(classType != null && classType.equals(className)){
 					sb = spanElements[i].getText();
 					out.write(sb.toString());
 				}
 			}
 			out.flush();
 			out.close();
 			fos.close();
 			Date currentTime = new Date(System.currentTimeMillis());
 			//newsID already has single quotation!
 			String insertValue = "" + newsID + ",'" + currentTime.toString() + "'";
 			//add link to database
 			db.insert(tableName, insertValue );
 			System.out.println("Saved URL --> " + url.toString());
 		}
 		else{
 			logger.logRepetitive(url.toString() + " page id: " + id);
 			System.out.println("Repetitive URL --> " + url.toString());
 		}
 	}
 
 	public Mehrnews createTable() throws ClassNotFoundException, SQLException{
 		if(!db.tableExists(tableName)){
 			db.createTable(tableName, tableNameFields);
 		}	
 		return this;
 	}
 	
 	public Logger getLogger(){
 		return logger;
 	}
 }
