 package am.ik.categolj.app.category;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.HandlerMapping;
 
 import am.ik.categolj.app.common.component.PagerComponent;
 import am.ik.categolj.app.common.consts.Const;
 import am.ik.categolj.app.common.util.CategoryUtils;
 import am.ik.categolj.domain.common.consts.LogId;
 import am.ik.categolj.domain.common.util.CommonUtils;
 import am.ik.categolj.domain.model.Category;
 import am.ik.categolj.domain.model.Entry;
 import am.ik.categolj.domain.service.entry.EntryService;
 import am.ik.yalf.logger.Logger;
 
 @Controller
 public class CategoryController {
 
     private static final Logger logger = Logger
             .getLogger(CategoryController.class);
 
     @Inject
     protected EntryService entryService;
 
     @Inject
     protected PagerComponent pager;
 
     @RequestMapping("/category/**")
     public String view(HttpServletRequest request, Model model) {
         return page(request, Const.START_PAGE, model);
     }
 
     @RequestMapping("/page/{page}/category/**")
     public String page(HttpServletRequest request, @PathVariable int page,
             Model model) {
         String path = ((String) request
                 .getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE))
                .replace("/category", "").replace("/page/" + page, "");
         logger.debug(LogId.DCTGL001, path);
 
         List<Category> categories = CategoryUtils
                 .populateCategoriesFromPath(path);
         List<Entry> entries = entryService.getCategorizedEntriesByPage(
                 categories, page, Const.VIEW_COUNT);
 
         int totalPage = CommonUtils.calcTotalPage(entryService
                 .getCategorizeEntryCount(categories));
         String appendPath = Const.CATEGORY_PATH
                 + CategoryUtils.categoryPathString(categories);
         List<String> pagerLink = pager.createPaginationLinks(totalPage, page,
                 entries, appendPath);
 
         model.addAttribute(entries);
         model.addAttribute(Const.PAGER_ATTR, pagerLink);
        model.addAttribute(Const.CATEGORY_LINK_ATTR, CategoryUtils
                .categoryBreadCrumb(categories));
         return "category/view";
     }
 
     @RequestMapping("/all")
     public String all(Model model) {
         Set<String> linkSet = entryService.getAllCategoryLinkSet();
         model.addAttribute(Const.CATEGORY_LINK_SET_ATTR, linkSet);
         return "category/all";
     }
 
     @RequestMapping("/category.json")
     public @ResponseBody
     List<Map<String, String>> allJson(@RequestParam("term") String term) {
         List<String> paths = entryService.getAllCategoryPath(term);
         List<Map<String, String>> response = new ArrayList<Map<String, String>>();
         for (String path : paths) {
             response.add(Collections.singletonMap("label", path));
         }
         return response;
     }
 }
