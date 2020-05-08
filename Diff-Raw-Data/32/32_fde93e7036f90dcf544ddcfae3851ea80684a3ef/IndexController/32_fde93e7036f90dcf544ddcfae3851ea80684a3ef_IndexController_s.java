 package cms.controller;
 
 import cms.model.model.NewsEntity;
 import cms.model.model.PageEntity;
 import cms.model.model.TemplateEntity;
 import cms.model.model.dto.NewsDTO;
 import cms.model.model.dto.PageDTO;
 import cms.model.model.dto.TemplateDTO;
 import cms.model.service.NewsService;
 import cms.model.service.PageService;
 import cms.model.service.TemplateService;
 import cms.util.GuiceUtil;
 import java.io.PrintWriter;
 import java.util.List;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.slim3.controller.Controller;
 import org.slim3.controller.Navigation;
 import org.slim3.controller.validator.Errors;
 
 public class IndexController extends Controller {
 
 	private PageService pageService = GuiceUtil.getService(PageService.class);
 	private TemplateService templateService = GuiceUtil.getService(TemplateService.class);
 	private NewsService newsService = GuiceUtil.getService(NewsService.class);
 
 	public IndexController() {
 	}
 
 	public IndexController(ServletContext servletContext, HttpServletRequest request, 
 			HttpServletResponse response, String basePath, Errors errors) {
 		this.servletContext = servletContext;
 		this.request = request;
 		this.response = response;
 		this.basePath = basePath;
 		this.errors = errors;
 	}
 
 	@Override
 	public Navigation run() throws Exception {
 		PrintWriter out = response.getWriter();
		response.setContentType("text/html; charset=utf-8"); // TODO nefunguje
//		response.setCharacterEncoding("utf-8");
//		response.setHeader("Content-Type", "text/html; charset=utf-8");
//		System.out.println("R: "+ response.getContentType() +" "+ response.getCharacterEncoding());
 
 		if (basePath.equals("/")) {
 			basePath = "index";
 		}
 		if (basePath.endsWith(".html")) {
 			basePath = basePath.substring(0, basePath.length() - 5);
 		}
 		PageEntity pageEntity = pageService.getPageByUrl(basePath);
 		PageDTO page = new PageDTO(pageEntity);
 		if (pageEntity == null || !page.getVisible()) {
 			NotFoundController notFoundController = new NotFoundController(
 					servletContext, request, response, basePath, errors);
 			notFoundController.run();
 			
 			return null;
 		}
 
 		String template = page.getTemplateContent();
 		
 		template = template.replace("{title}", page.getTitle());
 		template = template.replace("{content}", page.getContent());
 		if (template.contains("{news}")) {
 			String news = getNews();
 			template = template.replace("{news}", news);
 		}
 
 		String[] tagsArray = page.getTags();
 		StringBuilder tags = new StringBuilder();
 		for (String tag : tagsArray) {
 			tags.append(tag).append(" ");
 		}
 		template = template.replace("{tags}", tags);
 		
 		out.println(template);
 
 		return null;
 	}
 
 	public String getNews() {
 		TemplateEntity newsTemplateEntity = templateService.getTemplateByName("news");
 		TemplateEntity newsItemTemplateEntity = templateService.getTemplateByName("newsItem");
 		if (newsTemplateEntity == null || newsItemTemplateEntity == null) {
 			return "";
 		}
 		TemplateDTO newsTemplateDTO = new TemplateDTO(newsTemplateEntity);
 		String newsTemplate = newsTemplateDTO.getContent();
		TemplateDTO newsItemTemplateDTO = new TemplateDTO(newsTemplateEntity);
 		String newsItemTemplate = newsItemTemplateDTO.getContent();
 
 		List<NewsEntity> newsList = newsService.getHomepageNews();
 
 		StringBuilder news = new StringBuilder();
 		for (NewsEntity newsEntity : newsList) {
 			NewsDTO newsDTO = new NewsDTO(newsEntity);
 			String newsItem = newsItemTemplate;
 			newsItem = newsItem.replace("{title}", newsDTO.getTitle());
 			newsItem = newsItem.replace("{text}", newsDTO.getText());
 			newsItem = newsItem.replace("{created}", newsDTO.getCreated());
 			news.append(newsItem);
 		}
 
		newsTemplate = newsTemplate.replace("{news}", news.toString());
 
 		return newsTemplate;
 	}
 }
