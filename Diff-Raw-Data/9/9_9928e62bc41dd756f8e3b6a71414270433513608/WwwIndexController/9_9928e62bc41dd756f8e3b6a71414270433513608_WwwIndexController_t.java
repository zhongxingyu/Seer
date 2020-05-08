 package com.cqlybest.site.controller.www;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.authentication.AnonymousAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.cqlybest.common.bean.MaldivesIsland;
 import com.cqlybest.common.bean.Page;
 import com.cqlybest.common.bean.page.Section;
 import com.cqlybest.common.controller.ControllerHelper;
 import com.cqlybest.common.service.FriendlyLinkService;
 import com.cqlybest.common.service.MaldivesService;
 import com.cqlybest.common.service.PageService;
 import com.cqlybest.common.service.ProductService;
 import com.cqlybest.common.service.SettingsService;
 import com.cqlybest.common.service.UserService;
 
 @Controller
 public class WwwIndexController extends ControllerHelper {
 
   private static final String ATOM = "[a-z0-9!#$%&'*+/=?^_`{|}~-]";
   private static final String DOMAIN = ATOM + "+(\\." + ATOM + "+)+";
   private static final Pattern EMAIL_PATTERN =
       Pattern
           .compile("^" + ATOM + "+(\\." + ATOM + "+)*@" + DOMAIN + "$", Pattern.CASE_INSENSITIVE);
 
   @Autowired
   private SettingsService settingsService;
   @Autowired
   private PageService pageService;
   @Autowired
   private FriendlyLinkService friendlyLinkService;
   @Autowired
   private MaldivesService maldivesService;
   @Autowired
   private ProductService productService;
   @Autowired
   private UserService userService;
 
   @RequestMapping("/robots.txt")
   public Object robots(HttpServletRequest request) {
     String host = request.getServerName();
     if (host.startsWith("admin.")) {
       return notFound();
     }
     return "/robots";
   }
 
   @RequestMapping({"/", "/index.html"})
   public Object index(@RequestParam(required = false) String cid,
       @RequestParam(required = false) String sub_appkey, HttpServletRequest request, Model model) {
     String host = request.getServerName();
     if (host.startsWith("admin.")) {
       return "/v1/login";
     }
     if (host.startsWith("m.")) {
       Page page = pageService.getPage("www.index");
       model.addAttribute("posters", page.getPosters());// 海报
       model.addAttribute("result", maldivesService.queryIsland(0, Integer.MAX_VALUE));
       model.addAttribute("Settings", settingsService.getSettings());
       model.addAttribute("Links", friendlyLinkService.queryLink(0, Integer.MAX_VALUE));
       return "/v3/index";
     }
 
     if (host.startsWith("weibo.")) {
       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
       if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
         if (StringUtils.isBlank(cid) || StringUtils.isBlank(sub_appkey)) {
           return illegal();
         }
 
         HttpSession session = request.getSession();
         session.setAttribute("cid", StringUtils.isEmpty(cid) ? null : cid);
         session.setAttribute("sub_appkey", StringUtils.isEmpty(sub_appkey) ? null : sub_appkey);
         return "redirect:/weibo/security/proxy";
       }
 
      List<MaldivesIsland> islands = maldivesService.queryIsland(0, Integer.MAX_VALUE).getItems();
       List<MaldivesIsland> result = new ArrayList<>();
       for (MaldivesIsland island : islands) {
         if (!island.getHotelPictures().isEmpty()) {
           result.add(island);
         }
       }
       model.addAttribute("islands", result);
       model.addAttribute("Settings", settingsService.getSettings());
       return "/v4/index";
     }
 
     Page page = pageService.getPage("www.index");
     for (Section section : page.getContents()) {
       int pageSize = Integer.MAX_VALUE;
       if (section.getNumber() != null && section.getNumber() > 0) {
         pageSize = section.getNumber();
       }
       if (Section.TYPE_PRODUCT.equals(section.getType())) {
         section.setQueryResult(productService.queryProduct(section.getPc(), 0, pageSize));
       }
       if (Section.TYPE_MALDIVES.equals(section.getType())) {
         section.setQueryResult(maldivesService.queryIsland(section.getMdc(), 0, pageSize));
       }
     }
     for (Section section : page.getSidebars()) {
       int pageSize = Integer.MAX_VALUE;
       if (section.getNumber() != null && section.getNumber() > 0) {
         pageSize = section.getNumber();
       }
       if (Section.TYPE_PRODUCT.equals(section.getType())) {
         section.setQueryResult(productService.queryProduct(section.getPc(), 0, pageSize));
       }
       if (Section.TYPE_MALDIVES.equals(section.getType())) {
         section.setQueryResult(maldivesService.queryIsland(section.getMdc(), 0, pageSize));
       }
     }
     model.addAttribute("Page", page);
     model.addAttribute("Settings", settingsService.getSettings());
     model.addAttribute("Links", friendlyLinkService.queryLink(0, Integer.MAX_VALUE));
     return "/v5/index/index";
   }
 
  @RequestMapping("/session")
  public Object session() {
    return json(getUser());
  }

   @RequestMapping("/login.html")
   public Object login(HttpServletRequest request, Model model) {
     String host = request.getServerName();
     if (host.startsWith("admin.")) {
       return "/v1/login";
     }
 
     model.addAttribute("Settings", settingsService.getSettings());
     return "/v2/login";
   }
 
   @RequestMapping("/redirect")
   public Object redirect(HttpServletRequest request, Model model) {
     String host = request.getServerName();
     if (host.startsWith("admin.")) {
       return "redirect:/home.do";
     }
 
     return "redirect:/index.html";
   }
 
   @RequestMapping(method = RequestMethod.GET, value = "/soon.html")
   public Object comingSoon(Model model) {
     model.addAttribute("Settings", settingsService.getSettings());
     return "/v5/soon";
   }
 
   @RequestMapping(method = RequestMethod.POST, value = "/soon.html")
   public Object comingSoon(@RequestParam String id, Model model) {
     String type = "mobile";
     if (StringUtils.isBlank(id)) {
       return error("请输入邮件地址或手机号。");
     }
     String account = id.trim();
     if (account.contains("@")) {
       if (!EMAIL_PATTERN.matcher(id).matches()) {
         return error("请输入正确的邮件地址。");
       }
       type = "email";
     }
     if (!id
         .matches("^(139|138|137|136|135|134|147|150|151|152|157|158|159|182|183|184|187|188|130|131|132|155|156|185|186|145|133|153|180|181|189)\\d{8}$")) {
       return error("请输入正确的手机号码。");
     }
 
     userService.subscribe(type, id);
     return ok();
   }
 
 }
