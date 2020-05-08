 package models.cms;
 
 import play.Logger;
 import play.data.validation.MaxSize;
 import play.data.validation.Required;
 import play.db.jpa.GenericModel;
 import play.modules.search.Field;
 import play.modules.search.Indexed;
 import play.templates.Template;
 import play.templates.TemplateLoader;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Lob;
 import javax.persistence.Table;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 @Entity
 @Indexed
 @Table(name = "cmspage")
 public class CMSPage extends GenericModel {
 
     @Id
     @MaxSize(255)
     @Field(sortable = true)
     public String name;
 
     @Required
     @MaxSize(255)
     @Field(sortable = true)
     public String title;
 
     @Lob
     @Field
     @MaxSize(150)
     public String description;
 
     @Field
     @MaxSize(255)
     public String keywords;
 
     @Lob
     @Field
     @Required
     public String body;
 
     @Field
     @Required
     public String template;
 
     @Field(sortable = true)
     public Date created = new Date();
 
     @Field(sortable = true)
     public Date updated = new Date();
 
     public Boolean published = Boolean.FALSE;
 
     /**
      * Find all cms template available (list all file into the cms views directory).
      *
      * @return
      */
     public static List<String> getAllTemplate() {
         List<String> res = new ArrayList<String>();
         List<Template> templates = TemplateLoader.getAllTemplate();
         for (Template template : templates) {
             if (template.getName().contains("/app/views/cms/") && !template.getName().contains("{module:cms}")) {
                 Logger.debug("Find CMS template :" + template.getName());
                 String name = template.getName().split("/")[template.getName().split("/").length -1].replace(".html", "");
                 Logger.debug("CMS Template name is " + name);
                 res.add(name);
             }
         }
         return res;
     }
 
     /**
      * Find all page by template (order by created date desc).
      *
      * @param template
      * @param onlyPublished ?
      * @return
      */
     public static List<CMSPage> getAllByTemplate(String template, Boolean onlyPublished){
         if(onlyPublished)
             return CMSPage.find("template = ?1 and published = true order by created desc", template).fetch();
         else
             return CMSPage.find("template = ?1 order by created desc", template).fetch();
     }
 
     /**
      * Get the lastest element by template.
      *
      * @param template
      * @return
      */
     public static CMSPage getLastest(String template, Boolean onlyPublished){
         if(onlyPublished)
            return CMSPage.find("template = ?1 AND published = true order by created desc", template, onlyPublished).first();
         else
             return CMSPage.find("template = ?1 order by created desc", template).first();
     }
 
     /**
      * Get the lastest number elements by template.
      *
      * @param template
      * @param number
      * @return
      */
     public static List<CMSPage> getLastests(String template, Boolean onlyPublished, int number){
         if(onlyPublished)
            return CMSPage.find("template = ?1 AND published = ?2 order by created desc", template, onlyPublished).fetch(number);
         else
             return CMSPage.find("template = ?1 order by created desc", template).fetch(number);
     }
 
     /**
      * Get previous item by date by template.
      *
      * @return
      */
     public CMSPage previous() {
         return CMSPage.find("template = ?1 AND published = true AND created < ?2 order by created desc", template, created).first();
     }
 
     /**
      * Get previous item by date by template.
      *
      * @return
      */
     public CMSPage next() {
         return CMSPage.find("template = ?1 AND published = true AND created > ?2 order by created desc",template,  created).first();
     }
 
     @Override
     public CMSPage save(){
         // create the url name for the page
         if(this.name == null) {
             String urlName = title;
             urlName = urlName.replaceAll("[ |'|`|\"]", "-");
             urlName = urlName.replaceAll("[e|é|è|ê]", "e");
             urlName = urlName.replaceAll("[à|a]", "a");
             urlName = urlName.replaceAll("[ï|î]", "i");
 
             // check if already exist and increment a counter
             Integer i = 0;
             String findUrl = urlName;
             while(CMSPage.findById(findUrl) != null) {
                 i++;
                 findUrl = urlName + "-" + i;
             }
             this.name = findUrl;
         }
         // the updated date
         this.updated = new Date();
         _save();
         return this;
     }
 
 }
