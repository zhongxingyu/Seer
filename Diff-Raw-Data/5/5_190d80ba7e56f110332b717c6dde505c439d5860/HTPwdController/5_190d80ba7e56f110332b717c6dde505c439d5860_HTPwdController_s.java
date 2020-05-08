 package com.vegaasen.htpasswd.web.controller;
 
 import com.vegaasen.htpasswd.model.HTPasswdVariant;
 import com.vegaasen.htpasswd.util.HashingDigester;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.Date;
 
 /**
  * Simple htpasswd generator Controller
  * <p/>
  *
  * TODO: Create a simple REST-based service that provides either:
  * a) XML
  * b) JSON
  *
  * @author vegaasen
  */
 @Controller
 @RequestMapping("/")
 public class HTPwdController {
 
    private static final String MT_TEXT_RICH = "text/richtext";
     private static final String UTF_8 = "UTF-8";
 
     /**
      * Handle requests for processing an htpasswd to be used together with the apache-ftp-server
      * (or in other cases where htpasswd needs to be used)
      *
      * @param request  _
      * @param response _
      * @param usr      UserName
      * @param pwd      Password
      * @param digest   Hashing algorithm
      * @return jsp-mapping
      */
     @RequestMapping(value = "generator", method = {RequestMethod.GET, RequestMethod.POST})
     public ModelAndView handleGenerateRequest(
             final HttpServletRequest request,
             HttpServletResponse response,
             @RequestParam(value = "usr", defaultValue = "") String usr,
             @RequestParam(value = "pwd", defaultValue = "") String pwd,
             @RequestParam(value = "digestType", defaultValue = "ALG_CRYPT") String digest) {
         ModelAndView view = new ModelAndView("generator");
         if (request.getMethod().equals(RequestMethod.POST.name())) {
             if (StringUtils.isNotEmpty(usr) && StringUtils.isNotEmpty(pwd) && StringUtils.isNotEmpty(digest)) {
 
                 HTPasswdVariant htDigest = HTPasswdVariant.find(digest);
 
                 String result = HashingDigester.generateEncryptedPassword(usr, pwd, htDigest);
 
                 view.addObject("v_genpwd", result);
                 view.addObject("v_usr", usr);
                 view.addObject("v_pwd", pwd);
                 view.addObject("v_digestType", digest);
             }
         }
         return view;
     }
 
     /**
      * @param request  _
      * @param response _
      * @param usr      UserName
      * @param pwd      Password
      * @param digest   Hashing algorithm
      * @return a file in mime-type text/richtext containing what has been generated from the /generator-mapping
      * @throws IOException _
      */
    @RequestMapping(value = "htpasswd_{usr}.text", method = RequestMethod.POST)
     public String handleDownloadRequest(
             final HttpServletRequest request,
             HttpServletResponse response,
             @RequestParam(value = "usr") String usr,
             @RequestParam(value = "pwd") String pwd,
             @RequestParam(value = "digestType") String digest)
             throws IOException {
         if (request.getMethod().equals(RequestMethod.POST.name())) {
             if (StringUtils.isNotEmpty(usr) && StringUtils.isNotEmpty(pwd)) {
 
                 final String result = String.format(
                         "# Generated using the {%s}-algorithm\n# Generated on: %s\n%s:%s",
                         digest,
                         new Date(),
                         usr,
                         pwd);
 
                 response.setContentType(MT_TEXT_RICH);
                 response.setCharacterEncoding(UTF_8);
                 response.setContentLength(result.length());
                 ServletOutputStream outputStream = response.getOutputStream();
                 outputStream.write(result.getBytes());
                 outputStream.flush();
             }
         }
 
         return null;
     }
 
 }
