 package it.italiangrid.wnodes.controllers;
 
 import it.italiangrid.portal.dbapi.domain.UserInfo;
 import it.italiangrid.portal.dbapi.services.UserInfoService;
 import it.italiangrid.wnodes.exception.WnodesPortletException;
 import it.italiangrid.wnodes.model.KeyPair;
 import it.italiangrid.wnodes.utils.UserServiceUtil;
 import it.italiangrid.wnodes.utils.WnodesConfig;
 import it.italiangrid.wnodes.utils.impl.UserServiceUtilImpl;
 
 import java.io.IOException;
 
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.portlet.PortletConfig;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.portlet.bind.annotation.ActionMapping;
 import com.liferay.portal.kernel.servlet.SessionErrors;
 import com.liferay.portal.kernel.servlet.SessionMessages;
 import com.liferay.portal.kernel.util.JavaConstants;
 import com.liferay.portal.kernel.util.WebKeys;
 import com.liferay.portal.model.User;
 
 @Controller("uploadController")
 @RequestMapping(value = "VIEW")
 public class UploadController {
 	
 	@Autowired
 	private UserInfoService userInfoService;
 	
 	private static final Logger log = Logger.getLogger(UploadController.class);
 	
 	/**
 	 * Upload the user's ssh key pair.
 	 * @param request
 	 * @param response
 	 */
 	@ActionMapping(params = "myaction=uploadKeyPair")
 	public void uploadKeyPair(@ModelAttribute KeyPair keyPair, BindingResult bndingResult, ActionRequest request, ActionResponse response) {
 		User user = (User) request.getAttribute(WebKeys.USER);
 		if (user != null) {
 			UserServiceUtil service = new UserServiceUtilImpl(user.getUserId());
 //			MultipartFile keyMultipartFile = request.getFile("key");
 //		    MultipartFile keyPubMultipartFile = request.getFile("keyPub");
 		    
 			if(keyPair.getPrivateKey()==null)
 				log.error("private key null");
 			if(keyPair.getPublicKey()==null)
 				log.error("public key null");
 			if(keyPair.getPrivateKey()!=null && keyPair.getPublicKey()!=null){
 				
 			    try {
 			    	UserInfo userInfo = userInfoService.findByMail(user.getEmailAddress());
 					service.storeKeys(keyPair, userInfo);
 					SessionMessages.add(request, "keys-uploaded-successfully");
 					
 					String users = WnodesConfig.getProperties("active.users");
 					
 					if(!users.isEmpty()){
 						users+=";";
 					}
 					users+=user.getUserId();
 					WnodesConfig.setProperties("active.users", users);
 					
 				} catch (IllegalStateException e) {
 					SessionErrors.add(request, "system-exception");
 					e.printStackTrace();
 				} catch (IOException e) {
 					SessionErrors.add(request, "keys-not-uploaded");
 					e.printStackTrace();
 				} catch (WnodesPortletException e) {
 					SessionErrors.add(request, "config-problem");
 					e.printStackTrace();
 				}
 			}else{
 				SessionErrors.add(request, "keys-null");
 			}
 		}
 		PortletConfig portletConfig = (PortletConfig)request.getAttribute(JavaConstants.JAVAX_PORTLET_CONFIG);
 		SessionMessages.add(request, portletConfig.getPortletName() + SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_ERROR_MESSAGE);
 		response.setRenderParameter("myaction", "showKeyManagement");
 	}
 	
 	@ActionMapping(params = "myaction=destroyKeyPair")
 	public void destroyKeypair(ActionRequest request, ActionResponse response){
 		User user = (User) request.getAttribute(WebKeys.USER);
 		if (user != null) {
 			UserServiceUtil service = new UserServiceUtilImpl(user.getUserId());
 			service.destroyKeyPair();
 			
 			try {
 				String users = WnodesConfig.getProperties("active.users");
				
 				if(users.contains(user.getUserId()+";")){
 					users = users.replaceAll(user.getUserId()+";", "");
 				}
 				if(users.contains(Long.toString(user.getUserId()))){
 					users = users.replaceAll(Long.toString(user.getUserId()), "");
 				}
				
 				WnodesConfig.setProperties("active.users", users);
 			} catch (WnodesPortletException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			SessionMessages.add(request, "keys-deleted-successfully");
 			response.setRenderParameter("myaction", "showKeyManagement");
 		}
 	}
 	
 }
