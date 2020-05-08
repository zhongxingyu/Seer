 package cn.seu.cose.controller.admin;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import cn.seu.cose.entity.PublicationPojo;
 import cn.seu.cose.service.PublicationService;
 import cn.seu.cose.view.util.ViewUtil;
 
 @Controller
 public class AdminPubController extends AbstractController{
 	
 	public static final int PAGE_SIZE = 10;
 	
 	@Autowired
 	PublicationService pubService;
 	
 	@RequestMapping(value="/admin/pub_list", method=RequestMethod.GET)
 	public void pubListDefault(Model model, HttpServletResponse response) {
 		try {
			response.sendRedirect("/admin/pub_list/p1");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@RequestMapping(value="/admin/pub_list/p{pageIndex}", method=RequestMethod.GET)
 	public String pubList(@PathVariable("pageIndex") int pageIndex, Model model, HttpServletResponse response) {
 		putAdmin(model, response);
 		
 		int index = pageIndex<=0 ? 1 : pageIndex;
 		List<PublicationPojo> list = pubService.getPublicationByIndexAndPageSize4Admin(index, PAGE_SIZE);
 		model.addAttribute("pub_list", list);
 		
 		model.addAttribute("pageIndex", pageIndex);
 		model.addAttribute("nextPageIndex", pageIndex+1);
 		model.addAttribute("prePageIndex", pageIndex-1);
 		int pageCount = getPageCount(PAGE_SIZE, pubService.getPubCount());
 		model.addAttribute("pageCount",pageCount);
 		
 		return "admin_pubs";
 	}
 	
 	@RequestMapping(value="/admin/add_pub", method=RequestMethod.GET)
 	public String getAdd(Model model, HttpServletResponse response) {
 		putAdmin(model, response);
 		return "admin_pubs_add";
 	}
 	
 	@RequestMapping(value="/admin/add_pub", method=RequestMethod.POST)
 	public void postAdd(@RequestParam("title")String title, @RequestParam("intro") String intro, 
 			@RequestParam("images")String images, @RequestParam("linkUrl") String linkUrl, @RequestParam("type") int type, Model model, HttpServletResponse response) {
 		
 		PublicationPojo pub = new PublicationPojo();
 		pub.setTitle(title);
 		pub.setIntro(intro);
 		pub.setLinkUrl(linkUrl);
 		pub.setType(type);
 		pub.setImages(images);
 		pub.setPostTime(new Date());
 		pubService.addPublication(pub);
 	}
 	
 	@RequestMapping(value="/admin/del_pub", method=RequestMethod.POST)
 	public void postDel(@RequestParam("id") String idStr,  HttpServletResponse response) {
 		try {
 			pubService.deletePublication(Integer.parseInt(idStr));
 			response.sendRedirect(ViewUtil.getContextPath() + "/admin/pub_list");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	@RequestMapping(value="/admin/alt_pub-{id}", method=RequestMethod.GET)
 	public String getAlt(@PathVariable("id") String idStr, Model model, HttpServletResponse response) {
 		putAdmin(model, response);
 		PublicationPojo pub = pubService.getPublicationById(Integer.parseInt(idStr));
 		model.addAttribute("pub", pub);
 		return "admin_pubs_alt";
 	}
 	
 	@RequestMapping(value="/admin/alt_pub", method=RequestMethod.POST)
 	public void postAlt(@RequestParam("id") String idStr, @RequestParam("title")String title, @RequestParam("intro") String intro, 
 			@RequestParam("images")String images, @RequestParam("linkUrl") String linkUrl, @RequestParam("type") int type, Model model, HttpServletResponse response) {
 		PublicationPojo pub = new PublicationPojo();
 		pub.setId(Integer.parseInt(idStr));
 		pub.setTitle(title);
 		pub.setIntro(intro);
 		pub.setLinkUrl(linkUrl);
 		pub.setType(type);
 		pub.setImages(images);
 		pub.setPostTime(new Date());
 		pubService.updatePublication(pub);
 	}
 	
 }
