 package org.lisak.pguide.controllers;
 
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 import geo.gps.Coordinates;
 import org.lisak.pguide.dao.ContentDao;
 import org.lisak.pguide.model.*;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.multipart.MultipartFile;
 
 import javax.imageio.ImageIO;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Formatter;
 import java.util.List;
 import java.util.logging.Logger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: lisak
  * Date: 06.08.13
  * Time: 13:59
  */
 @Controller
 @RequestMapping("/admin")
 public class AdminController {
     private static final Logger log = Logger.getLogger(AdminController.class.getName());
 
     private ContentDao contentDao;
     private CategoryFactory categoryFactory;
 
     public CategoryFactory getCategoryFactory() {
         return categoryFactory;
     }
 
     public void setCategoryFactory(CategoryFactory categoryFactory) {
         this.categoryFactory = categoryFactory;
     }
 
     public ContentDao getContentDao() {
         return contentDao;
     }
 
     public void setContentDao(ContentDao contentDao) {
         this.contentDao = contentDao;
     }
 
     // ******** Article upload & processing **********
     @RequestMapping(value = "/article/{articleId}", method = RequestMethod.GET)
     public String showArticle(@PathVariable("articleId") String articleId,
                               @RequestParam(value = "delete", required = false) String delete,
                               Model model) {
         List<Article> articleList = contentDao.getArticles();
         Collections.sort(articleList, Article.TitleComparator);
         model.addAttribute("articleList", articleList);
 
         Article article;
         if(delete == null) {
             article = (Article) contentDao.get(articleId);
         } else {
             contentDao.delete(articleId);
             article = new Article();
             model.addAttribute(article);
             return "redirect:/admin/article";
         }
         model.addAttribute(article);
         return "admin/articleForm";
     }
 
     @RequestMapping(value = "/article", method = RequestMethod.GET)
     public String newArticle(HttpServletRequest req, Model model) {
 
         List<Article> articleList = contentDao.getArticles();
         model.addAttribute("articleList", articleList);
 
         model.addAttribute(new Article());
         return "admin/articleForm";
     }
 
     @RequestMapping(value = "/article", method = RequestMethod.POST)
     public String saveArticle(Article article) {
         contentDao.save(article);
         return "redirect:/admin/article/" + article.getId();
     }
 
     // ******** Image upload & processing **********
 
     @RequestMapping(value = "/image/{imageId}", method = RequestMethod.GET)
     public String showImage(@PathVariable("imageId") String imageId,
                             @RequestParam(value = "filter", required = false) String filter,
                             @RequestParam(value = "delete", required = false) String delete,
                             Model model) {
         List<Image> imgList;
         if(filter==null) {
             imgList =  contentDao.getImages();
         } else {
             imgList = contentDao.getImages(filter);
             model.addAttribute(filter);
         }
         model.addAttribute("imageList", imgList);
 
         Image image;
         if(delete == null) {
             image = (Image) contentDao.get(imageId);
             model.addAttribute(image);
             return "admin/imageForm";
         } else {
             contentDao.delete(imageId);
             return "redirect:/admin/image";
         }
     }
 
     @RequestMapping(value = "/image", method = RequestMethod.GET)
     public String newImage(@RequestParam(value = "filter", required = false) String filter, Model model) {
         List<Image> imgList;
         if(filter==null) {
             imgList =  contentDao.getImages();
         } else {
             imgList = contentDao.getImages(filter);
             model.addAttribute(filter);
         }
         model.addAttribute("imageList", imgList);
 
         model.addAttribute(new Image());
         return "admin/imageForm";
     }
 
     @RequestMapping(value = "/image", method = RequestMethod.POST)
     public String saveImage(Image image, @RequestParam(value = "imageData") MultipartFile data){
         try {
             if(data.getBytes().length>0) {
                 image.setData(data.getBytes());
             } else {
                 //fetch original image from DB (otherwise NULL will be saved as image data):
                 Image _img = (Image)contentDao.get(image.getId());
                 image.setData(_img.getData());
             }
 
             image.setFileType("jpg");
         }   catch (IOException e) {
             return "admin/imageForm";
         }
         contentDao.save(image);
 
         return "redirect:/admin/image/" + image.getId();
     }
 
     // ******** Profile upload & processing **********
     @RequestMapping(value = "/profile", method = RequestMethod.GET)
     public String newProfile(@RequestParam(value = "filter", required = false) String filter,
                              Model model) {
         List<Profile> profileList;
         profileList =  contentDao.getProfiles();
         model.addAttribute(profileList);
 
         if(filter == null)
             filter = "";
         model.addAttribute("filter", filter);
 
         List<Image> imageList = contentDao.getImages();
         model.addAttribute("strImgList", imageList.toString());
 
         List<Category> categoryList = categoryFactory.getCategoryList();
         model.addAttribute(categoryList);
 
         model.addAttribute("profile", new Profile());
         return "admin/profileForm";
     }
 
     @RequestMapping(value = "/profile/{profileId}", method = RequestMethod.GET)
     public String showProfile(@PathVariable("profileId") String profileId,
                             @RequestParam(value = "delete", required = false) String delete,
                             @RequestParam(value = "filter", required = false) String filter,
                             Model model) {
         List<Profile> profileList;
         profileList =  contentDao.getProfiles();
         model.addAttribute(profileList);
 
         if(filter == null)
             filter = "";
         model.addAttribute("filter", filter);
 
         List<Image> imageList = contentDao.getImages();
         model.addAttribute("strImgList", imageList.toString());
 
 
         List<Category> categoryList = categoryFactory.getCategoryList();
         model.addAttribute(categoryList);
 
         Profile profile;
         if(delete == null) {
             profile = (Profile) contentDao.get(profileId);
             model.addAttribute(profile);
             return "admin/profileForm";
         } else {
             contentDao.delete(profileId);
             return "redirect:/admin/profile";
         }
 
     }
 
     @RequestMapping(value = "/profile", method = RequestMethod.POST)
     public String saveProfile(@ModelAttribute("profile") Profile profile,
                               @RequestParam(value = "filter", required = false) String filter){
         contentDao.save(profile);
         return "redirect:/admin/profile/" + profile.getId() + "?filter=" + filter;
     }
 
     @RequestMapping(value = "/createStaticMap", method = RequestMethod.POST)
     @ResponseStatus(value = HttpStatus.OK)
     public void createStaticMap(@ModelAttribute("id") String id,
                                 @ModelAttribute("gps") String gps,
                                 HttpServletResponse response) throws IOException {
         String staticMapURL = "http://maps.googleapis.com/maps/api/staticmap?";
         String staticMapParams = "zoom=15&size=318x126&sensor=false&" +
                "visual_refresh=true&markers=icon:http://www.pguide.cz/resources/img/coffee-selected.png%%7C%s,%s";
 
         //parameters: id, long, lat
         //called via AJAX/jquery from edit profile page
         // - creates new static google map from specified coordinates
         // - uploads the resulting map to DB as image with ID "{id}-map"
         //returns HTTP 200 when successful
 
         //** prepare URL **
         //parse GPS (it may not be normalized by servlet yet)
         //FIXME: this should be moved to geo.gps
         Coordinates coords = new Coordinates();
         //remove NE completely - it fucks with the default format
         String _buf =  gps.replaceAll("[NE]", "");
         //replace all chars except for [0-9,.] with spaces
         _buf = _buf.replaceAll("[^\\d,.]", " ");
 
         coords.parse(_buf);
 
         StringBuilder sb = new StringBuilder();
         DecimalFormat df = new DecimalFormat("#.00000");
 
         String _fillParams = String.format(staticMapParams,
                 df.format(coords.getLatitude()),
                 df.format(coords.getLongitude()));
 
         URL url = new URL(staticMapURL + _fillParams);
 
         log.info("Static map URL: " + url.toString());
 
         //fetch static google image
         InputStream in = new BufferedInputStream(url.openStream());
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         byte[] buf = new byte[1024];
         int n;
         while (-1!=(n=in.read(buf)))
         {
             out.write(buf, 0, n);
         }
         out.close();
         in.close();
 
         //create & store new image
         Image img = new Image();
         img.setId(id);
         img.setData(out.toByteArray());
         img.setFileType("jpg");
         contentDao.save(img);
     }
 
     @RequestMapping(value = "/emptyreport", method = RequestMethod.GET)
     public String emptyValueReport(Model model) {
         Formatter fm = new Formatter();
         List<String> resultList = new ArrayList<String>();
         List<Article> articleList = contentDao.getArticles();
         for(Article a:articleList) {
             if(a.getTitle() == null || a.getTitle().equals("")) {
                 resultList.add(String.format("Article id <a href='/admin/article/%s'>%s</a>: empty title", a.getId(), a.getId()));
             }
             if(a.getText() == null || a.getText().equals("")) {
                 resultList.add(String.format("Article id <a href='/admin/article/%s'>%s</a>: empty text", a.getId(), a.getId()));
             }
         }
 
         for(Profile p: contentDao.getProfiles()) {
             if(p.getName() == null || p.getName().equals("")) {
                 resultList.add(String.format("Profile id <a href='/admin/profile/%s'>%s</a>: empty name", p.getId(), p.getId()));
             }
             if(p.getUrl() == null || p.getUrl().equals("")) {
                 resultList.add(String.format("Profile id <a href='/admin/profile/%s'>%s</a>: empty url", p.getId(), p.getId()));
             }
             if(p.getGpsCoords() == null || p.getGpsCoords().replaceAll("[0. ]","").equals("")) {
                 resultList.add(String.format("Profile id <a href='/admin/profile/%s'>%s</a>: empty GPS coords", p.getId(), p.getId()));
             }
             if(p.getAddress() == null || p.getAddress().equals("")) {
                 resultList.add(String.format("Profile id <a href='/admin/profile/%s'>%s</a>: empty address", p.getId(), p.getId()));
             }
             if(p.getOpeningHours() == null || p.getOpeningHours().size() == 0) {
                 resultList.add(String.format("Profile id <a href='/admin/profile/%s'>%s</a>: empty opening hours", p.getId(), p.getId()));
             }
             if(p.getPrices() == null || p.getPrices().equals("")) {
                 resultList.add(String.format("Profile id <a href='/admin/profile/%s'>%s</a>: empty prices", p.getId(), p.getId()));
             }
             if(p.getProfileImg() == null || p.getProfileImg().equals("")) {
                 resultList.add(String.format("Profile id <a href='/admin/profile/%s'>%s</a>: empty profile image", p.getId(), p.getId()));
             }
             if(p.getStaticMapImg() == null || p.getStaticMapImg().equals("")) {
                 resultList.add(String.format("Profile id <a href='/admin/profile/%s'>%s</a>: empty static map image", p.getId(), p.getId()));
             }
             if(p.getProfileImg() == null || p.getProfileImg().equals("")) {
                 resultList.add(String.format("Profile id <a href='/admin/profile/%s'>%s</a>: empty profile image", p.getId(), p.getId()));
             }
             if(p.getGallery() == null || p.getGallery().size() == 0) {
                 resultList.add(String.format("Profile id <a href='/admin/profile/%s'>%s</a>: empty image gallery", p.getId(), p.getId()));
             }
             if(p.getText() == null || p.getText().equals("")) {
                 resultList.add(String.format("Profile id <a href='/admin/profile/%s'>%s</a>: empty text", p.getId(), p.getId()));
             }
         }
 
         model.addAttribute("emptyValueList", resultList);
         return "admin/emptyValueReport";
     }
 }
