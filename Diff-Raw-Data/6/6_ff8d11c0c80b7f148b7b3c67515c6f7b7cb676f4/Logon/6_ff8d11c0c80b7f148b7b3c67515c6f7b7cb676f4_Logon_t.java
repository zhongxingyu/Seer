 package cyberprime.servlets;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.tomcat.util.http.fileupload.FileItem;
 import org.apache.tomcat.util.http.fileupload.FileUploadException;
 import org.apache.tomcat.util.http.fileupload.RequestContext;
 import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
 import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
 import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;
 
 import cyberprime.entities.Clients;
 import cyberprime.entities.dao.ClientsDAO;
 import cyberprime.util.Constants;
 import cyberprime.util.ImageEncryption;
 import cyberprime.util.ImageValidator;
 
 
 /**
  * Servlet implementation class Logon
  */
 @WebServlet("/Logon")
 public class Logon extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public Logon() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		HttpSession session = request.getSession();
 		String repos = Constants.SAMUEL_PATH;
 		Clients client = new Clients();
 		ImageEncryption en = null;
 		File repo = new File(repos);
 		DiskFileItemFactory factory = new DiskFileItemFactory();
 		String saveFileName = "";
 		String newFileName = "";
 		boolean renameFile;
 		
 			try{
 			factory.setSizeThreshold(2*1024*1024);
 			factory.setRepository(repo);
 	//		if(repo != null)
 	//		System.out.println("Repository :"+repo.getAbsolutePath());
 			ServletFileUpload upload = new ServletFileUpload(factory);
 			RequestContext req = new ServletRequestContext(request);
 			List<FileItem> items = upload.parseRequest(req);
 			Iterator<FileItem> iterator =  items.iterator();
 			
 			while(iterator.hasNext()){
 			FileItem item = iterator.next();
 				if(item.isFormField()){
 					
 				}
 				
 				else{
 					String fileName = item.getName();
					if (fileName.endsWith("\\ISPJ.PNG")) {
						fileName="ISPJ.PNG";
					}
						
 					saveFileName = repos + fileName;
 					File uploadedFile = new File(saveFileName);
 					
 					if(!fileName.isEmpty()){
 						
 						ImageValidator iv = new ImageValidator();
						System.out.println("filename = "+fileName);
 						
 						if(iv.validate(fileName)){
 							try{
 								item.write(uploadedFile);
 								newFileName = session.getId()+fileName.substring(fileName.length()-4); 
 								if(uploadedFile.renameTo(new File(repos+newFileName))){
 									System.out.println("File has been renamed");
 									renameFile = true;
 								}
 								
 								else{
 									System.out.println("File rename failed");
 									renameFile = false;
 
 								}
 								
 							}catch(Exception e){
 								Object obj = new Object();
 								obj = "<p style='color:red'>*Access denied</p>";
 								request.setAttribute("loginResult", obj);
 								request.getRequestDispatcher("templateLogin.jsp").forward(request, response);
 								return;
 							}
 
 							if(renameFile){
 								en = new ImageEncryption(repos+newFileName);
 							}
 
 
 							else{
 								en = new ImageEncryption(uploadedFile.getAbsolutePath());
 							}
 
 							client.setImageHash(en.getHash());
 							client.setImageSize(en.getSize());
 							client.setImageExtension(en.getExtension());
 							session.setAttribute("image",newFileName);
 
 						}
 						
 						else{
 							Object obj = new Object();
 							obj = "<p style='color:red'>*Unauthorized file type or file name</p>";
 							request.setAttribute("regResult", obj);
 							request.getRequestDispatcher("templateLogin.jsp").forward(request, response);
 							return;
 						}
 
 					}
 
 					else{
 						Object obj = new Object();
 						obj = "<p style='color:red'>*Please choose a file</p>";
 						request.setAttribute("regResult", obj);
 						request.getRequestDispatcher("templateLogin.jsp").forward(request, response);
 						return;
 					}
 
 
 				}
 			}
 				response.reset();
 				response.setHeader("Content-Disposition", "inline");
 				response.setHeader("Cache-Control", "no-cache");
 				response.setHeader("Expires", "0");
 				response.setContentType("image/jpg");
 				
 				
 				Clients c = null;
 				c = ClientsDAO.retrieveClient(client);
 					session.setAttribute("client", client);
 					request.getRequestDispatcher("patternLogin.jsp").forward(request, response);	
 					
 			
 			}catch(FileUploadException e){
 				e.printStackTrace();
 			}catch(Exception e){
 				e.printStackTrace();
 			}finally{
 				System.gc();
 			}
 	}
 }
