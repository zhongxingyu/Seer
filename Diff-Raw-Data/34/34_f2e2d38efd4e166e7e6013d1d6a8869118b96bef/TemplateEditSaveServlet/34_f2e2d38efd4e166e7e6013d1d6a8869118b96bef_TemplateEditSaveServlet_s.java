 package ajaxAdmin;
 
 import infoResource.ExternalResources;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import dao.DAOFactory;
 import dao.TemplateDAO;
 import entity.Template;
 
 import servlets.BaseServlet;
 
 /**
  * Servlet implementation class TemplateEditSaveServlet
  */
 @WebServlet(name = "TemplateEditor", urlPatterns = { "/TemplateEditor" })
 public class TemplateEditSaveServlet extends BaseServlet {
 	private static final long serialVersionUID = 1L;
 
     /**
      * Default constructor. 
      */
     public TemplateEditSaveServlet() {}
 
 	@Override
 	public void executeCustomCode(HttpServletRequest request,
 			HttpServletResponse response) {
 		int templteID=Integer.parseInt(request.getParameter("tID"));
 		DAOFactory myDaofactory=DAOFactory.getInstance(DAOFactory.MYSQL);
 		TemplateDAO tdao=myDaofactory.createTemplateDAO();
 		Template createdTemplate=tdao.getTemplate(templteID);
 		String RelationContents=request.getParameter("relT"),StoryContents=request.getParameter("storyT");
 		PrintWriter out;
 		try {
 			out=response.getWriter();
 			mofifyFile(ExternalResources.getPrefix()+createdTemplate.getRelationURL(),RelationContents);
 			mofifyFile(ExternalResources.getPrefix()+createdTemplate.getStoryURL(),StoryContents);
 			out.write("File Saved");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void mofifyFile(String fileURL,String text) throws IOException{
 		FileWriter file=new FileWriter(fileURL);
 		file.write(text);
 		file.flush();
 		file.close();
 	}
 }
