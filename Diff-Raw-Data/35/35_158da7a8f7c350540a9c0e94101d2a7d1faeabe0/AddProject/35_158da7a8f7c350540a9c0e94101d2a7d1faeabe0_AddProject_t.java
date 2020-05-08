 package myservs;
 
 import java.io.*;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 
 
 /**
  * Servlet implementation class AddProject
  */
 public class AddProject extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static final String TMP_DIR_PATH="/tmp";
 	private File tmpDir;
 	private static final String DESTINATION_DIR_PATH = "/uploads";
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public AddProject() {
         super();
         // TODO Auto-generated constructor stub
         tmpDir=new File(TMP_DIR_PATH);
         if (!tmpDir.isDirectory()){
         	System.out.println(TMP_DIR_PATH+"is not a directory");
         }
     }
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		response.setCharacterEncoding("UTF-8");
 		request.setCharacterEncoding("UTF-8");
 		
 		DiskFileItemFactory fileItemFactory=new DiskFileItemFactory();
 		fileItemFactory.setSizeThreshold(1024*1024);
 		fileItemFactory.setRepository(tmpDir);
 		
 		ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);
 		try{
 			HttpSession userSession=request.getSession();
 			boolean fieldsEmpty=false;
 			
 			Map<String,String> paramMap = new HashMap<String,String>();
 			
 			@SuppressWarnings("rawtypes")
 			List items=uploadHandler.parseRequest(request);
 			@SuppressWarnings("rawtypes")
 			Iterator itr=items.iterator();
 			while(itr.hasNext()){
 				FileItem item = (FileItem) itr.next();
 				boolean isFormField=item.isFormField();
 				if(isFormField){
 					paramMap.put(item.getFieldName(), item.getString());
 					if((item.getFieldName().equals("title")) && item.getString().isEmpty()){
 						fieldsEmpty=true;
 					}
 					if((item.getFieldName().equals("comment")) && item.getString().isEmpty()){
 						fieldsEmpty=true;
 					}
 				}else{
 					if(item.getName().isEmpty()){
 						fieldsEmpty=true;
 					}
 				
 					if(fieldsEmpty==true){
 						userSession.setAttribute("fieldsEmpty", "1");
 					}else{
						String userPath=getServletContext().getRealPath(DESTINATION_DIR_PATH).concat("/").concat(userSession.getAttribute("initId").toString());
 						File userDir=new File(userPath);
 						if(!userDir.exists()){
 							Boolean succeed=new File(userPath).mkdirs();
 							if(succeed==false){
 								System.out.println("Failed creating your personal directory");
 							}
 						}
 						DatabaseMethods dbPoint=new DatabaseMethods();
 						String commonFileName=userPath.concat("/").concat(item.getName());
 						int revision=dbPoint.findFileRevision(commonFileName);
 						revision++;
 						String fileName="r"+revision+"_"+item.getName();
 						File file = new File(userPath, fileName);
 						item.write(file);
 						String realPath=file.toString();
 						userSession.setAttribute("fileUpload", "1");
 						String isoTitle=paramMap.get("title");
 						String title=new String(isoTitle.getBytes("ISO8859_1"),"UTF-8");
 						String isoComment=paramMap.get("comment");
 						String comment=new String(isoComment.getBytes("ISO8859_1"),"UTF-8");
						int insResult=dbPoint.populatePfileTable(title, comment, revision, realPath, userSession.getAttribute("initId").toString(), commonFileName, item.getName());
 						if (insResult==0){
 							userSession.setAttribute("uploadPfiledbError", "1");
 						}		
 					}
 				}
 			}
 		}catch(FileUploadException e){
 			e.printStackTrace();
 		}catch(Exception e){
 			e.printStackTrace();
 		}finally{
 			RequestDispatcher rd=getServletContext().getRequestDispatcher("/addProject.jsp");
 			if(rd!=null){
 				rd.forward(request, response);
 			}
 		}
 	}
 
 }
