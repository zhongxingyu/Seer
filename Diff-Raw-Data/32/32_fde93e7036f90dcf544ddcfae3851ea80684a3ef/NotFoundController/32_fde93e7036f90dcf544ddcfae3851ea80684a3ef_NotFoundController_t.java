 package cms.controller;
 
 import cms.model.model.TemplateEntity;
 import cms.model.model.dto.TemplateDTO;
 import cms.model.service.TemplateService;
 import cms.util.GuiceUtil;
 import java.io.PrintWriter;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.slim3.controller.Controller;
 import org.slim3.controller.Navigation;
 import org.slim3.controller.validator.Errors;
 
 public class NotFoundController extends Controller {
 
 	private TemplateService templateService = GuiceUtil.getService(TemplateService.class);
 
 	public NotFoundController() {
 	}
 
 	public NotFoundController(ServletContext servletContext, HttpServletRequest request, 
 			HttpServletResponse response, String basePath, Errors errors) {
 		this.servletContext = servletContext;
 		this.request = request;
 		this.response = response;
 		this.basePath = basePath;
 		this.errors = errors;
 	}
 
 	@Override
 	public Navigation run() throws Exception {
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
 		PrintWriter out = response.getWriter();
 
 		TemplateEntity templateEntity = templateService.getTemplateByName("notFound");
 		if (templateEntity == null) {
 			out.println("<html>");
 			out.println("<head>");
			out.println("	<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
 			out.println("	<title>Stránka nenalezena</title>");
 			out.println("</head>");
 			out.println("<body>");
 			out.println("	<h1>Stránka nebyla nalezena.</h1>");
 			out.println("</body>");
 			out.println("</html>");
 			
 			return null;
 		}
 
 		TemplateDTO templateDTO = new TemplateDTO(templateEntity);
 		String template = templateDTO.getContent();
 		out.println(template);
 
 		return null;
 	}
 }
