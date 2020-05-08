 package univ_angers.virtuose.webapp;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import univ_angers.virtuose.utils.Full_Map;
 import javax.servlet.*;
 import javax.servlet.annotation.MultipartConfig;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.*;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.document.Document;
 
 import univ_angers.virtuose.rendu.Extract;
 import univ_angers.virtuose.search.Search;
 import univ_angers.virtuose.utils.Writer;
 import univ_angers.virtuose.utils.XmlToHtml;
 
 @WebServlet("/controller")
 @MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
 public class Controller extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static Logger log = Logger.getLogger(Controller.class);
 	private static String saved_map_folder = "src/main/resources/saved_map/";
 	private static String tmp_map_visualisation = "src/main/webapp/tmp/";
 	private String full_map_path;
 	
 	public Controller() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		doPost(request, response);
 	}
 
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		RequestDispatcher disp = null;
 		HttpSession session = request.getSession();
 
 		String action = request.getParameter("action");
 		String keywords = "";
 
 		if (action == null) {
 			
 		} else if(action.equals("full_map")) {
 			String map = (String)session.getAttribute("mapContent");
 
 			log.debug("full_map_path: " + full_map_path);
 			Full_Map fMap = new Full_Map(getMapContent(full_map_path), map);
 			//log.debug("full_map");
 			//log.debug(fMap.getHightLighedMap());
 			
 			session.setAttribute("full_map_path", fMap.getFullMapPath());
 			
 			disp = request.getRequestDispatcher("show_full.jsp");
 			disp.forward(request, response);
 		} else if(action.equals("show")) {
 			List<String> title = (List<String>)session.getAttribute("title");
 			List<String> content = (List<String>)session.getAttribute("content");
			
 			String ids = (String) request.getParameter("id");
 			if(ids == null)
 				ids = "0";
 			session.setAttribute("ids", ids);
 			Integer id = -1;
 			
 			
 			if(ids != null) {
 				try {
 					id = Integer.parseInt((String) request.getParameter("id"));	
 				} catch(Exception e) {
 					id = 0;
 				} 	
 			}
 				
 			int i = 0;
 			for (String c : content) {
 				if (i == id) {
 					//session.setAttribute("map", value);
 					String mapPath = createTmpVisualisation(i,c);
 					session.setAttribute("map",mapPath);
 					session.setAttribute("mapContent",c);
 					session.setAttribute("current_title", title.get(i));
 				}
 				i++;
 			}
 			
 			disp = request.getRequestDispatcher("result.jsp");
 			disp.forward(request, response);
 
 		} else if (action.equals("search")) {
 			// get access to file that is uploaded from client
 			Part p1 = request.getPart("map");
 			InputStream is = p1.getInputStream();
 
 			// read filename which is sent as a part
 			Part p2 = request.getPart("keywords");
 			Scanner s = new Scanner(p2.getInputStream());
 			keywords = s.nextLine(); // read filename from stream
 			log.debug("Controller: keywords: " + keywords);
 
 			// get filename to use on the server
 			String fileName = getFileName(p1);
 			log.info("File name : " + fileName);
 			String outputfile = saved_map_folder + fileName; // get path on the
 																// server
 			this.full_map_path = outputfile;
 			
 			createDirectoryIfNeeded(saved_map_folder);
 
 			FileOutputStream os = new FileOutputStream(outputfile);
 
 			// write bytes taken from uploaded file to target file
 			int ch = is.read();
 			while (ch != -1) {
 				os.write(ch);
 				ch = is.read();
 			}
 
 			// on traite le .mm
 			Search search = new Search();
 			search.index(outputfile);
 			Writer writer = new Writer();
 			writer.proceed(outputfile);
 			os.close();
 
 			s.close();
 
 			// 2. query
 			String querystr = keywords;
 			ArrayList<Document> docs = search.search(querystr);
 			ArrayList<ArrayList<String>> xmls = new ArrayList<ArrayList<String>>();
 			/**
 			 * Generate all xml documents according to the lucene doc that match
 			 * the request
 			 */
 			// Document d = docs.get(0);
 			log.info("results: " + docs.size());
 			ArrayList<String> Ids = new ArrayList<String>();
 			for(Document d: docs){
 			    Ids.add(d.get("id"));
 			}
 			
 			if(docs.size() == 0) {
				disp = request.getRequestDispatcher("no_result.jsp");
 				disp.forward(request, response);
 			}
 			
 			String doc = docs.get(0).get("document");
 			// on suppose que la recherche ne concernera qu'une carte
 			while(Ids.size()>0){
 			    try{
 			        xmls.add(Extract.extract(doc,Ids));
 			    }catch(Exception e){
 			        e.printStackTrace();
 			    }
 			    
 			}
 			log.info("results displayed : "+xmls.size());
 			
 
 			List<String> title = new ArrayList<String>();
 			List<String> content = new ArrayList<String>();
 
 			for (ArrayList<String> xml : xmls) {
 			    //chaque objet contenu dans xmls contiendra un titre et du contenu xml
 			    String xmlTitle = xml.get(0);
 				String result = xml.get(1);
 
 				//log.debug("Controller:result: " + result);
 				//XmlToHtml.start(result);
 				//title.add(XmlToHtml.title);
 				title.add(xmlTitle);
 				content.add(result);
 			}
 
 			// On supprime la map indexée
 			deleteFolder(new File("src/ressources/index"));
 			session.setAttribute("title", title);
 			session.setAttribute("content", content);
 			session.setAttribute("keywords", keywords);
 
 			// On supprime la map
 			/*
 			os = new FileOutputStream(outputfile);
 			os.write('v');
 			os.close();*/
 			
 			String ids = (String) request.getParameter("id");
 			if(ids == null)
 				ids = "0";
 			session.setAttribute("ids", ids);
 			Integer id = -1;
 			
 			
 			if(ids != null) {
 				try {
 					id = Integer.parseInt((String) request.getParameter("id"));	
 				} catch(Exception e) {
 					id = 0;
 				}
 			}
 				
 			int i = 0;
 			for (String c : content) {
 				if (i == id) {
 					//session.setAttribute("map", value);
 					String mapPath = createTmpVisualisation(i,c);
 					session.setAttribute("map",mapPath);
 					session.setAttribute("mapContent",c);
 					session.setAttribute("current_title", title.get(i));
 				}
 				i++;
 			}
 			
 			disp = request.getRequestDispatcher("result.jsp");
 			disp.forward(request, response);
 		}
 	}
 
 	public static void deleteFolder(File folder) {
 		File[] files = folder.listFiles();
 		if (files != null) { // some JVMs return null for empty dirs
 			for (File f : files) {
 				if (f.isDirectory()) {
 					deleteFolder(f);
 				} else {
 					f.delete();
 				}
 			}
 		}
 		folder.delete();
 	}
 
 	private String getFileName(Part part) {
 		String partHeader = part.getHeader("content-disposition");
 		// log.info("Part Header = " + partHeader);
 		for (String cd : part.getHeader("content-disposition").split(";")) {
 			if (cd.trim().startsWith("filename")) {
 				return cd.substring(cd.indexOf('=') + 1).trim()
 						.replace("\"", "");
 			}
 		}
 		return null;
 
 	}
 
 	private void createDirectoryIfNeeded(String directoryName) {
 		File theDir = new File(directoryName);
 
 		// if the directory does not exist, create it
 		if (!theDir.exists()) {
 			log.info("creating directory: " + directoryName);
 			boolean c = theDir.mkdir();
 			if (!c){
 			    log.error("directory not created");
 			}
 		}else{
 		    log.info("the directory "+directoryName+" already exists");
 		}
 	}
 
     private String createTmpVisualisation(int id, String xmlContent){
         String xmlName = "tmp"+id+".xml";
         log.info("creating tempory file ("+xmlName+") to visualised result.");
         String outputfile = tmp_map_visualisation + xmlName; // get path on the
 																// server
 		createDirectoryIfNeeded(tmp_map_visualisation);
 
 		try{
             FileOutputStream os = new FileOutputStream(outputfile);
             /*FileWriter os = new FileWriter(outputfile);
             BufferedWriter out = new BufferedWriter(os);
             out.*/
             os.write(new String().getBytes());
 
 		    // write bytes taken from uploaded file to target file
 			/*int chunkSize=50;
 			String toWrite;
 			while (xmlContent.length()>0) {
 			    if(xmlContent.length()<50){
 			        chunkSize = xmlContent.length();
 			    }
 			    toWrite = xmlContent.substring(0,chunkSize);
 			    if(xmlContent.length()==chunkSize){
 			        xmlContent="";
 			    }else{
 			        xmlContent = xmlContent.substring(chunkSize+1);
 			    }
 				//out.write(toWrite);
 				os.write(toWrite.getBytes());
 			}*/
 			os.write(xmlContent.getBytes());
 			//out.close();
 			os.close();
 			log.info("File created");
 			return xmlName;
 		}catch (Exception e){//Catch exception if any
             System.err.println("Error: " + e.getMessage());
         }
         return null;
     }
     
 	private boolean mapNameExists(String filePath) {
 		File file = new File(filePath);
 		return file.exists();
 	}
 
 	private String getMapContent(String path) {
 		String map = "";
 		BufferedReader br = null;
 		 
 		try {
  
 			String sCurrentLine;
  
 			br = new BufferedReader(new FileReader(path));
  
 			while ((sCurrentLine = br.readLine()) != null) {
 				map += sCurrentLine;
 			}
  
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (br != null)br.close();
 			} catch (IOException ex) {
 				ex.printStackTrace();
 			}
 		}
 		
 		return map;
 	}
 }
