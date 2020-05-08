 package com.test.controller;
 
 
 import com.app.AddedProduct;
 import com.app.Product;
 import com.app.User;
 import com.hibernate.HibernateUtil;
 import com.hibernate.dao.DaoFactory;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 
 @Controller
 public class MainController {
 
     private static final Log LOG = LogFactory.getLog(MainController.class);
     // Autowired - автоматическое связывание
     @Autowired
     private ShaPasswordEncoder encoder;
 
     @RequestMapping(value = "/secured/user/app", method = RequestMethod.GET)
     public String showMainPage(ModelMap model) {
 
         List<Product> products;
 
         // start work with DB
 //        SessionFactory factory = HibernateUtil.getSessionFactory();
 //        Session session = factory.openSession();
 //        products = session.createQuery("from Product").list();
 //        session.close();
 
         // Replacement with DAO
 
         products = DaoFactory.INSTANCE.getProductDAO().getAll();
 
         model.addAttribute("productList", products);
 
         return "secured/user/main"; // name of JSP
     }
 
     @RequestMapping(value = "/secured/profile", method = RequestMethod.GET)
     public String showProfilePage(ModelMap model) {
         // params for JSP
         model.addAttribute("message", "Profile Page");
         return "profile"; // name of JSP
     }
 
     @RequestMapping(value = "/secured/user/app/add_record", method = RequestMethod.POST)
     public void addRecord(HttpServletResponse response,
                           @RequestParam("add_text_value") String mass,
                           @RequestParam("add_text_value_hide") String id ) throws IOException {
 
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         String username = auth.getName(); //get logged in username
 
         Product product = DaoFactory.INSTANCE.getProductDAO().getById(Integer.valueOf(id.substring(8, id.length())));
 
         AddedProduct addedProduct = new AddedProduct();
         addedProduct.setMass(Integer.valueOf(mass));
         addedProduct.setDate(new Date());
         addedProduct.setProduct(product);
         addedProduct.setUserId(DaoFactory.INSTANCE.getUserDAO().getUserId(username));
 
         System.err.println(mass);
         System.err.println(id);
         System.err.println(username);
 
         SessionFactory factory = HibernateUtil.getSessionFactory();
         Session session = factory.openSession();
         session.beginTransaction();
 
         System.out.println("-----------------------");
         System.err.println(addedProduct.getProduct().getProductName());
         System.err.println(addedProduct.getDate());
 
         product.getAddedProductSet().add(addedProduct);
         session.save(addedProduct);
 
         session.getTransaction().commit(); // commit all changes into DB
         session.close();
 
         response.sendRedirect("/calories-culc/secured/user/app/");
     }
 
     @RequestMapping(value = "/secured/admin/products", method = RequestMethod.GET)
     public String showManageProductsPage(ModelMap model) {
 
         List<Product> products;
 
         // start work with DB
 //        SessionFactory factory = HibernateUtil.getSessionFactory();
 //        Session session = factory.openSession();
 //        products = session.createQuery("from Product").list();
 //        session.close();
 
         // Replacement with DAO
         products = DaoFactory.INSTANCE.getProductDAO().getAll();
 
 
         model.addAttribute("productList", products);
 
         return "secured/admin/manage_products";
     }
 
     @RequestMapping(value = "/secured/admin/products/addUser", method = RequestMethod.POST)
     public void /* ModelAndView */ addUser(
             HttpServletResponse response,
             @RequestParam("enterLogin") String login,
             @RequestParam("enterPassword") String password,
             @RequestParam("enterPasswordRe") String passwordRe,
             @RequestParam("enterRole") String role
     ) throws IOException {
 
         LOG.debug("started");
         if (!(password.contains(passwordRe))) {
             // TODO throw an error
             System.out.println("Пароли не совпадают");
         }
 
         User user = new User(login, encoder.encodePassword(password, "myHash"), role);
         System.out.println(user.getLogin() + " " + user.getPassword());
 
         DaoFactory.INSTANCE.getUserDAO().save(user);
 
 
         // redirect
         LOG.debug("redirecting back...");
         // TODO app name is hardcoded, should be a better way to do it
 
         response.sendRedirect("/calories-culc/secured/admin/products/");
     }
 
     @RequestMapping(value = "/secured/admin/products/add", method = RequestMethod.POST)
     public void /* ModelAndView */ addProduct(
             HttpServletResponse response,
             @RequestParam("productName") String productName,
             @RequestParam("calories") String calories,
             @RequestParam("protein") String protein,
             @RequestParam("fat") String fat,
             @RequestParam("carbohydrate") String carbohydrate
     ) throws IOException {
 
         LOG.debug("started");
 
         Double caloriesDouble = Double.parseDouble(calories);
         Double proteinDouble = Double.parseDouble(protein);
         Double fatDouble = Double.parseDouble(fat);
         Double carbohydrateDouble = Double.parseDouble(carbohydrate);
 
         Product newProduct = new Product(productName, caloriesDouble, proteinDouble, fatDouble, carbohydrateDouble);
 
         // work with DB
 //        SessionFactory factory = HibernateUtil.getSessionFactory();
 //        Session session = factory.openSession();
 //        session.beginTransaction();
 //        session.save(newProduct); // save into DB (commit required)
 //        session.getTransaction().commit(); // commit all changes into DB
 //        session.close();
 
         // Replacement with hDAO
         DaoFactory.INSTANCE.getProductDAO().save(newProduct);
 
         // redirect
         LOG.debug("redirecting back...");
         // TODO app name is hardcoded, should be a better way to do it
         response.sendRedirect("/calories-culc/secured/admin/products/");
 
         // this does not work somehow...
         //return new ModelAndView("redirect:/products/");
     }
 
     public ShaPasswordEncoder getEncoder() {
         return encoder;
     }
 
     public void setEncoder(ShaPasswordEncoder encoder) {
         this.encoder = encoder;
     }
 
 }
