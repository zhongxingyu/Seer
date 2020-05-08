 
 package org.icemobile.samples.springbasic;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.context.request.WebRequest;
 
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * Session Controller for echoing simple input pages
  */
 @Controller
 @SessionAttributes("QRScanBean")
 public class EchoSessionController {
 
     @ModelAttribute("QRScanBean")
     public QRScanBean initQRScanBean() {
         return new QRScanBean();
     }
 
     @RequestMapping(value = "/qrscan")
     public void doRequest(
             @ModelAttribute("QRScanBean") QRScanBean model) {
     }
    
    @ModelAttribute
    public void ajaxAttribute(WebRequest request, Model model) {
        model.addAttribute("ajaxRequest", AjaxUtils.isAjaxRequest(request));
    }

 
 }
 
