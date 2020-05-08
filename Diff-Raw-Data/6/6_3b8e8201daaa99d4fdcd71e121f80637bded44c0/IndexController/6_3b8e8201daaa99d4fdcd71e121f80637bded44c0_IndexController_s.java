 package com.cqlybest.www.controller;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpSession;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.CookieValue;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.cqlybest.common.bean.ProductGroup;
 import com.cqlybest.common.bean.template1.Template1Menu;
 import com.cqlybest.common.bean.template1.Template1ProductGroup;
 import com.cqlybest.common.service.DictService;
 import com.cqlybest.common.service.MaldivesService;
 import com.cqlybest.common.service.ProductService;
 import com.cqlybest.common.service.TemplateService;
 
 @Controller
 public class IndexController extends ControllerHelper {
 
 
   @Autowired
   private TemplateService templateService;
   @Autowired
   private ProductService productService;
   @Autowired
   private MaldivesService maldivesService;
   @Autowired
   private DictService dictService;
 
   /**
    * 首页
    */
   @RequestMapping("/index.html")
   public String index(Model model) {
     model.addAttribute("posters", template1Service.getPublishedPosters());// 海报
     model.addAttribute("specials", template1Service.getSpecialProduct(4));// 特价
     model.addAttribute("recommendeds", template1Service.getRecommendedProduct(2));// 推荐
     model.addAttribute("hots", template1Service.getHotProduct(10));// 热门
     List<Template1ProductGroup> items = template1Service.getAllIndexProductGroups();
     List<Map<String, Object>> groups = new ArrayList<Map<String, Object>>(items.size());
     for (Template1ProductGroup item : items) {
       ProductGroup productGroup = item.getProductGroup();
       Map<String, Object> group = new HashMap<String, Object>();
       group.put("group", productGroup);
       group.put("products", productService.queryProducts(productGroup, null, 0, 4));
       groups.add(group);
     }
     model.addAttribute("groups", groups);// 产品组合
     setCommonData(model);
     return templateService.getTemplate() + "/index";
   }
 
   /**
    * 注册
    */
   @RequestMapping("/register.html")
   public String register(Model model) {
     setCommonData(model);
     return templateService.getTemplate() + "/register";
   }
 
   /**
    * 登录
    */
   @RequestMapping("/login.html")
   @SuppressWarnings("deprecation")
   public String login(@RequestParam(required = false) Boolean error,
       @CookieValue(required = false) String username, HttpSession session, Model model) {
     Object exception = session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
     if (Boolean.TRUE.equals(error) && exception != null) {
       model.addAttribute("error", true);
       if (exception instanceof AuthenticationException) {
         Authentication auth = ((AuthenticationException) exception).getAuthentication();
         if (auth != null) {
           model.addAttribute("username", auth.getPrincipal());
         }
       }
       session.removeAttribute("SPRING_SECURITY_LAST_EXCEPTION");
     }
     if (!model.containsAttribute("username")) {
       model.addAttribute("username", username);
     }
     setCommonData(model);
     return templateService.getTemplate() + "/login";
   }
 
   /**
    * 自定义内容页
    */
   @RequestMapping("/page/{id}.html")
   public Object page(@PathVariable String id, Model model) {
     Template1Menu menu = template1Service.get(id);
     if (menu == null || menu.getMenuType() != 1) {
       // 菜单不存在或者不是产品聚合菜单
       return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
     }
 
     model.addAttribute("page", menu);
     setCommonData(model);
     return templateService.getTemplate() + "/page";
   }
 
 }
