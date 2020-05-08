 package com.financial.pyramid.web;
 
 import com.financial.pyramid.domain.News;
 import com.financial.pyramid.domain.Video;
 import com.financial.pyramid.service.NewsService;
 import com.financial.pyramid.service.SettingsService;
 import com.financial.pyramid.service.VideoService;
 import com.financial.pyramid.domain.Role;
 import com.financial.pyramid.web.form.AuthenticationForm;
 import com.financial.pyramid.web.form.PageForm;
 import com.financial.pyramid.web.form.QueryForm;
 import com.financial.pyramid.web.form.RegistrationForm;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import java.util.List;
 
 /**
  * User: Danil
  * Date: 05.06.13
  * Time: 23:03
  */
 @Controller
 @RequestMapping("/pyramid")
 public class TabsController {
 
     @Autowired
     protected VideoService videoService;
 
     @Autowired
     protected NewsService newsService;
 
     @Autowired
     protected SettingsService settingsService;
 
 
     @RequestMapping(value = "/home", method = RequestMethod.GET)
     public String home(ModelMap model) {
         model.addAttribute("page-name", "home");
         return "/tabs/home";
     }
 
     @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
     @RequestMapping(value = "/admin", method = RequestMethod.GET)
     public String admin(ModelMap model) {
         return "redirect:/pyramid/admin/user_settings";
     }
 
     @RequestMapping(value = "/training", method = RequestMethod.GET)
     public String training(ModelMap model) {
         List<Video> videos = videoService.find();
         Video video = videos != null && videos.size() > 0 ? videos.get(0) : null;
         String defaultVideo = settingsService.getProperty("youTubeVideoUrl", video != null ? video.getExternalId() : null);
         model.addAttribute("defaultVideo", defaultVideo);
         model.addAttribute("videos", videos);
         model.addAttribute("page-name", "training");
         return "/tabs/training";
     }
 
     @RequestMapping(value = "/news", method = RequestMethod.GET)
     public String newsDefault(ModelMap model) {
         return news(model, 1);
     }
 
     @RequestMapping(value = "/news/{page}", method = RequestMethod.GET)
     public String news(ModelMap model, @PathVariable int page) {
         PageForm<News> newsForm = new PageForm<News>();
         QueryForm form = new QueryForm();
         form.setSort("date");
         form.setOrder("desc");
         form.setPage(page);
         int total = newsService.find().size();
         List<News> list = newsService.findByQuery(form);
         newsForm.setRows(list);
         newsForm.setTotal(total);
         model.addAttribute("newsForm", newsForm);
        model.addAttribute("page-name", "news");
        return "/tabs/news";
     }
 
     @RequestMapping(value = "/office", method = RequestMethod.GET)
     public String office(ModelMap model, HttpSession session, HttpServletRequest request) {
         model.addAttribute("page-name", "office");
         if (request.isUserInRole(Role.USER.name()) || request.isUserInRole(Role.ADMIN.name())) {
             return "/tabs/office";
         }
         model.addAttribute("authentication", new AuthenticationForm());
         return "/tabs/login";
     }
 
     @RequestMapping(value = "/about", method = RequestMethod.GET)
     public String about(ModelMap model) {
         model.addAttribute("page-name", "about");
         return "/tabs/about";
     }
 
     @RequestMapping(value = "/contacts", method = RequestMethod.GET)
     public String contacts(ModelMap model) {
         model.addAttribute("page-name", "contacts");
         return "/tabs/contacts";
     }
 
     @RequestMapping(value = "/registration", method = RequestMethod.GET)
     public String registration(ModelMap model, HttpSession session, HttpServletRequest request) {
         model.addAttribute("page-name", "office");
         model.addAttribute("registration", new RegistrationForm());
         return "tabs/user/registration-form";
     }
 }
