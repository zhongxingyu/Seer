 package cn.seu.cose.controller.admin;
 
 import java.io.IOException;
import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import cn.seu.cose.entity.Blog;
 import cn.seu.cose.entity.Designer;
 import cn.seu.cose.service.BlogService;
 import cn.seu.cose.service.DesignerService;
 import cn.seu.cose.view.util.ViewUtil;
 
 
 @Controller
 public class AdminDesignerController extends AbstractController{
 	
 	public static final int PAGE_SIZE = 10;
 	
 	@Autowired
 	DesignerService designerService;
 	
 	@Autowired
 	BlogService blogService;
 	
 	
 	@RequestMapping(value="/admin/designer_list", method=RequestMethod.GET)
 	public void getDesignersDefault(Model model, HttpServletResponse response) {
 		try {
			response.sendRedirect("/admin/designer_list/p1");		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	@RequestMapping(value="/admin/designer_list/p{pageIndex}", method=RequestMethod.GET)
 	public String getDesigners(@PathVariable("pageIndex") int pageIndex, Model model, HttpServletResponse response) {
 		putAdmin(model, response);
 		
 		int index = pageIndex>0 ? pageIndex : 1;
 		List<Designer> list = designerService.getAllDesignersByBaseAndRange(index, PAGE_SIZE);
 		model.addAttribute("designer_list", list);
 		
 		model.addAttribute("pageIndex", pageIndex);
 		model.addAttribute("nextPageIndex", pageIndex+1);
 		model.addAttribute("prePageIndex", pageIndex-1);
 		int pageCount = getPageCount(PAGE_SIZE, designerService.getAllDesignerCount());
 		model.addAttribute("pageCount",pageCount);
 		model.addAttribute("prefix", "designer_list"); 
 		return "admin_designers";
 	}
 	
 	
 	
 	@RequestMapping(value="/admin/designer_list_search-{searchInput}", method=RequestMethod.GET)
 	public String searchDesignerByName(@PathVariable("searchInput") String searchInput, Model model, HttpServletResponse response) {
 		putAdmin(model, response);
 		List<Designer> list = designerService.searchDesignerByName(searchInput);
 		model.addAttribute("designer_list", list);
 		model.addAttribute("pageIndex", 1);
 		model.addAttribute("nextPageIndex", 1);
 		model.addAttribute("prePageIndex", 1);
 		model.addAttribute("pageCount", 1);
 		return "admin_designers";
 	}
 	
 	@RequestMapping(value="/admin/del_designer", method=RequestMethod.POST)
 	public void postDelDesigner(@RequestParam("id") int id, HttpServletResponse response) {
 		designerService.deleteDesigner(id);
 		try {
 			response.sendRedirect(ViewUtil.getContextPath() + "/admin/designer_list");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	@RequestMapping(value="/admin/designer_list_certificated", method=RequestMethod.GET)
 	public void getCertificatedDesignersDefault(Model model, HttpServletResponse response) {
 		try {
			response.sendRedirect("/admin/designer_list_certificated/p1");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	@RequestMapping(value="/admin/designer_list_certificated/p{pageIndex}", method=RequestMethod.GET)
 	public String getCertificatedDesigners(@PathVariable("pageIndex") int pageIndex, Model model, HttpServletResponse response) {
 		putAdmin(model, response);
 		
 		int index = pageIndex>0 ? pageIndex : 1;
 		List<Designer> list = designerService.getTypeDesignersByBaseAndRange(0, index, PAGE_SIZE);
 		model.addAttribute("designer_list", list);
 		
 		model.addAttribute("pageIndex", pageIndex);
 		model.addAttribute("nextPageIndex", pageIndex+1);
 		model.addAttribute("prePageIndex", pageIndex-1);
 		int pageCount = getPageCount(PAGE_SIZE, designerService.getTypeDesignerCount(0));
 		model.addAttribute("pageCount",pageCount);
 		model.addAttribute("prefix", "designer_list_certificated"); 
 		return "admin_designers";
 	}
 	
 	@RequestMapping(value="/admin/designer_list_uncertificated", method=RequestMethod.GET)
 	public void getUnCertificatedDesignersDefault(Model model, HttpServletResponse response) {
 		try {
			response.sendRedirect("/admin/designer_list_uncertificated/p1");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	@RequestMapping(value="/admin/designer_list_uncertificated/p{pageIndex}", method=RequestMethod.GET)
 	public String getUncertificatedDesigners(@PathVariable("pageIndex") int pageIndex, Model model, HttpServletResponse response) {
 		putAdmin(model, response);
 		
 		int index = pageIndex>0 ? pageIndex : 1;
 		List<Designer> list = designerService.getTypeDesignersByBaseAndRange(1, index, PAGE_SIZE);
 		model.addAttribute("designer_list", list);
 		
 		model.addAttribute("pageIndex", pageIndex);
 		model.addAttribute("nextPageIndex", pageIndex+1);
 		model.addAttribute("prePageIndex", pageIndex-1);
 		int pageCount = getPageCount(PAGE_SIZE, designerService.getTypeDesignerCount(1));
 		model.addAttribute("pageCount",pageCount);
 		model.addAttribute("prefix", "designer_list_uncertificated"); 
 		return "admin_designers";
 	}
 	
 	@RequestMapping(value="/admin/cer_designer", method=RequestMethod.POST)
 	public void certificateDesigner(@RequestParam("id") int id, HttpServletResponse response) {
 		designerService.certificateDesignerById(id);
 		try {
 			response.sendRedirect(ViewUtil.getContextPath() + "/admin/designer_list");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@RequestMapping(value="/admin/uncer_designer", method=RequestMethod.POST)
 	public void uncertificateDesigner(@RequestParam("id") int id, HttpServletResponse response) {
 		designerService.uncertificateDesignerById(id);
 		try {
 			response.sendRedirect(ViewUtil.getContextPath() + "/admin/designer_list");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	// 管理blog
 	@RequestMapping("/admin/designer/{designerId}/blog_list/p{pageIndex}")
 	public String blogList(@PathVariable(value="designerId") int designerId, @PathVariable(value="pageIndex") int pageIndex, 
 			 Model model, HttpServletResponse response) {
 		putAdmin(model, response);
 		
 		int index = pageIndex<=0 ? 1 : pageIndex;
 		List<Blog> list = blogService.getBlogsByDesignerIdAndPnAndPageSize(designerId, index, PAGE_SIZE);
 		
 		model.addAttribute("el_list", list);
 		model.addAttribute("designerId", designerId);
 		model.addAttribute("pageIndex", pageIndex);
 		model.addAttribute("nextPageIndex", pageIndex+1);
 		model.addAttribute("prePageIndex", pageIndex-1);
 		int pageCount = getPageCount(PAGE_SIZE, blogService.getBlogCountByDesignerId(designerId));
 		model.addAttribute("pageCount",pageCount);
 		
 		return "admin_blogs";
 	}
 
 	
 	//删除blog
 	@RequestMapping("/admin/designer/del_blog")
 	public void postDelblog(@RequestParam("designerId") int designerId, @RequestParam("blogId") int blogId, Model model, HttpServletResponse response) {
 		putAdmin(model, response);
 		blogService.archiveBlog(blogId);
 		try {
			response.sendRedirect("/admin/designer/"+ designerId +"/blog_list/p1");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}		
 	
 	
 	
 }
