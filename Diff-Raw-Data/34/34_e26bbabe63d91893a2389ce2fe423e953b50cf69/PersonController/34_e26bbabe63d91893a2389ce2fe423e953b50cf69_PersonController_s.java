 package com.gaohui.action;
 
 import org.apache.struts2.freeroute.annotation.ContentBase;
 import org.apache.struts2.freeroute.annotation.MethodType;
 import org.apache.struts2.freeroute.annotation.Route;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author bastengao
  * @date 12-12-16 15:12
  */
 @ContentBase("/pages")
 public class PersonController {
     private static final Logger log = LoggerFactory.getLogger(PersonController.class);
 
     private int id;
 
     @Route("/persons/create")
     public String toCreate() {
         log.debug("toCreate:{}", id);
         return "dispatcher:/my-result.html";
     }
 
     @Route("/create")
     public String toCreate1() {
         return "dispatcher:/my-result.html";
     }
 
     @Route("/persons/create2")
     public String toCreate2() {
         System.out.println("create2");
         return "dispatcher:/my-result2.html";
     }
 
     @Route(value = "/persons/create2", method = MethodType.POST)
     public String toCreate2Post() {
         System.out.println("create2 post");
         return "dispatcher:/my-result2.html";
     }
 
     @Route("/persons/nested/create3")
     public String toCreate3() {
         return null;
     }
 
     @Route(value = "new", method = MethodType.POST)
     public String create() {
         return "content";
     }
 
     // {id} value in path will be set to this#id property.
     @Route(value = "/persons/{id}", method = MethodType.GET)
     public String show() {
         //will render by "/pages/content.html"
         System.out.println(id);
         System.out.println("get");
         return "freemarker:/my-result";
     }
 
     // {id} value in path will be set to this#id property.
    @Route(value = "/persons/{id}", method = MethodType.POST, params = {"_method"})
     public String show2() {
         //will render by "/pages/content.html"
         System.out.println(id);
         System.out.println("post");
         return "freemarker:/my-result";
     }
 
     @Route("/persons/{id}.xml")
     public String showXml() {
         System.out.println(id);
         return "dispatcher:/my-result.html";
     }
 
     @Route("/persons/{id}.jsp")
     public String showJsp() {
         System.out.println(id);
         return "dispatcher:/my-result.jsp";
     }
 
     //@Route("/{id}/edit")
     public String edit() {
         return "content";
     }
 
     @Route(value = "/persons/{id}", method = MethodType.PUT, params = {"_method"})
     public String update() {
         System.out.println("update");
         return "dispatcher:/my-result.jsp";
     }
 
     @Route(value = "/persons/{id}", method = MethodType.DELETE, params = {"_method=delete", "foo!=bar"})
     public String delete() {
         System.out.println("delete");
         return "dispatcher:/my-result.jsp";
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 }
