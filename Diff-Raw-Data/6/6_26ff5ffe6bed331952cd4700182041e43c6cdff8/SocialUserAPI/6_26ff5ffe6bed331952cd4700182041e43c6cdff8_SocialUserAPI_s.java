 package pandox.china.controller.api;
 
 import com.google.gson.Gson;
 import org.apache.http.client.fluent.Request;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 import pandox.china.controller.BaseController;
 import pandox.china.exception.ResourceNotFound;
 import pandox.china.model.SocialUser;
 import pandox.china.service.SocialUserService;
 import pandox.china.service.auth.AuthenticationProvider;
 import pandox.china.util.ErrorMessage;
 import pandox.china.util.SuccessMessage;
 import pandox.china.util.ValidadorException;
 import pandox.china.util.constants.SocialConstants;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 @Controller
 @RequestMapping("/user")
 public class SocialUserAPI extends BaseController {
 
 	private static Logger log = Logger.getLogger(SocialUserAPI.class);
 
 	@Autowired
 	private SocialUserService service;
 
     @RequestMapping(value = "", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
     @ResponseBody
     public SocialUser create(@RequestBody SocialUser socialUser) {
         socialUser = service.save(socialUser);
 
         doAutoLogin(socialUser);
         socialUser.setMessage("Usuário cadastrado com sucesso!");
         return socialUser;
     }
 
     @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
     @ResponseBody
     public SocialUser find(@RequestParam("uid")Long uid) {
         SocialUser socialUser = service.findByUid(uid);
 
         if(socialUser == null) {
             throw new ResourceNotFound();
         }else {
             doAutoLogin(socialUser);
             return socialUser;
         }
     }
 
     @RequestMapping(value = "{id}/feed", method = RequestMethod.GET, produces = "application/json")
     @ResponseBody
     public String feed(@PathVariable("id") Long id) throws IOException {
         // TODO Clean this method
         SocialUser user = service.findOne(id);
         if (user != null) {
             StringBuilder fbUrl = new StringBuilder("https://graph.facebook.com/oauth/access_token?client_id=");
             fbUrl.append(SocialConstants.API_KEY);
             fbUrl.append("&client_secret=");
             fbUrl.append(SocialConstants.API_SECRET);
             fbUrl.append("&grant_type=fb_exchange_token&fb_exchange_token=");
             fbUrl.append(user.getAccessToken());
             log.debug("FacebookCALL. GET=" + fbUrl);
 
             String json = Request.Get(fbUrl.toString()).execute().returnContent().asString();
             String[] split = json.split("&");
             log.debug(split);
 
 
             fbUrl = new StringBuilder("https://graph.facebook.com/");
             fbUrl.append(user.getUid());
             fbUrl.append("/feed?limit=20");
             fbUrl.append("&access_token=");
            fbUrl.append(split[0].split("=")[1]);
             log.debug("FacebookCALL. GET=" + fbUrl);
 
             json = Request.Get(fbUrl.toString()).execute().returnContent().asString();
             log.debug(json);
             return json;
         }
 
         return "";
     }
 
 	private void doAutoLogin(SocialUser user) {
 		AuthenticationProvider authenticationProvider = new AuthenticationProvider();
 		Authentication authenticationToken = authenticationProvider.generateSocialUserTokenAuthentication(user);
 		SecurityContextHolder.getContext().setAuthentication(authenticationToken);
 	}
 
 	@ExceptionHandler(ValidadorException.class)
 	public ModelAndView exceptionWebHandler(ValidadorException ex, HttpServletRequest request, HttpServletResponse response) {
 		ModelAndView mv = new ModelAndView("user/index");
 		mv.addObject("message", new ErrorMessage(ex.getErros()));
 		mv.addObject("users", service.findAll());
 		return mv;
 	}
 
 	@ExceptionHandler(ResourceNotFound.class)
     @ResponseStatus(value = HttpStatus.NO_CONTENT, reason = "Recurso não encontrado.")
 	public void resourceNotFoundHandler(ResourceNotFound ex, HttpServletResponse response) {
         log.info("Content Not-Found");
     }
 }
