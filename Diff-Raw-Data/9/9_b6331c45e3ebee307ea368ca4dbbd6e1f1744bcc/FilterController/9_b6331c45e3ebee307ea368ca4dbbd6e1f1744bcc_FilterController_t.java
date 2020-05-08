 package com.ssm.llp.web.controller.secure;
 
 import com.ssm.llp.core.dao.SsmFilterDao;
 import com.ssm.llp.core.model.SsmFilter;
 import org.hibernate.SessionFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 /**
  * @author rafizan.baharum
  * @since 9/14/13
  */
 @Controller
 @RequestMapping("/secure/filter")
 @Transactional
 public class FilterController extends SecureControllerSupport {
 
     private Logger log = LoggerFactory.getLogger(FilterController.class);
 
     @Autowired
     private SessionFactory sessionFactory;
 
     @Autowired
     private SsmFilterDao filterDao;
 
 
     @RequestMapping(value = "/all", method = {RequestMethod.GET})
     public String all(ModelMap model) {
         model.put("filters", filterDao.find());
         return "secure/filter";
     }
 
     @RequestMapping(value = "/edit/{id}", method = {RequestMethod.GET})
     public String edit(@PathVariable Long id, ModelMap model) {
         model.put("filter", filterDao.findById(id));
         return "secure/filter_edit";
     }
 
     @RequestMapping(value = "/update", method = {RequestMethod.POST})
    public String update(@RequestParam Long id,
                         @RequestParam String name,
                         @RequestParam String description,
                         @RequestParam String error,
                         @RequestParam String script,
                         ModelMap model) {
         SsmFilter n = filterDao.findById(id);
         n.setName(name);
         n.setDescription(description);
        n.setError(error);
         n.setScript(script);
         filterDao.update(n, getCurrentUser());
         sessionFactory.getCurrentSession().flush();
         return "redirect:/secure/filter/edit/" + n.getId();
     }
 }
