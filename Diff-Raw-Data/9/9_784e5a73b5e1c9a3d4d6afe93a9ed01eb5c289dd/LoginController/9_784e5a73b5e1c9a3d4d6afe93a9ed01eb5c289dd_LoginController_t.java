 package portal.login.controller;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import javax.portlet.PortletConfig;
 import javax.portlet.PortletURL;
 import javax.portlet.RenderResponse;
 import javax.portlet.RenderRequest;
 import javax.portlet.WindowStateException;
 //import portal.login.domain.Idp;
 //import portal.login.domain.UserInfo;
 //import portal.login.domain.UserToVo;
 //import portal.login.domain.Vo;
 //import portal.login.services.IdpService;
 //import portal.login.services.UserInfoService;
 //import portal.login.services.UserToVoService;
 
 import it.italiangrid.portal.dbapi.domain.Idp;
 import it.italiangrid.portal.dbapi.domain.UserInfo;
 import it.italiangrid.portal.dbapi.domain.UserToVo;
 import it.italiangrid.portal.dbapi.domain.Vo;
 import it.italiangrid.portal.dbapi.services.IdpService;
 import it.italiangrid.portal.dbapi.services.UserInfoService;
 import it.italiangrid.portal.dbapi.services.UserToVoService;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.*;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.support.SessionStatus;
 import org.springframework.web.portlet.bind.annotation.RenderMapping;
 
 import com.liferay.portal.kernel.portlet.LiferayWindowState;
 import com.liferay.portal.kernel.servlet.SessionErrors;
 import com.liferay.portal.kernel.servlet.SessionMessages;
 import com.liferay.portal.kernel.util.JavaConstants;
 import com.liferay.portal.kernel.util.WebKeys;
 import com.liferay.portal.model.User;
 import org.globus.gsi.GlobusCredential;
 import org.globus.gsi.GlobusCredentialException;
 
 /**
  * This class is the controller that authenticate the user and display some
  * information about the downloaded proxyes.
  * 
  * @author dmichelotto
  * 
  */
 
 @Controller("userInfoController")
 @RequestMapping(value = "VIEW")
 public class LoginController {
 
 	/**
 	 * Logger of the class DownloadCertificateController. 
 	 */
 	private static final Logger log = Logger.getLogger(LoginController.class);
 
 	/**
 	 * Attribute for access to the PortalUser database.
 	 */
 	@Autowired
 	private IdpService idpService;
 
 	/**
 	 * Attribute for access to the PortalUser database.
 	 */
 	@Autowired
 	private UserInfoService userInfoService;
 
 	/**
 	 * Attribute for access to the PortalUser database.
 	 */
 	@Autowired
 	private UserToVoService userToVoService;
 
 	/**
 	 * Method for render home.jsp page.
 	 * 
 	 * @return the page file name.
 	 */
 	@RenderMapping
 	public String showLogin(RenderResponse response) {
 		return "home";
 	}
 	
 	@RenderMapping(params = "myaction=success")
 	public String showSuccess(RenderResponse response, SessionStatus status) {
 		status.setComplete();
 		return "success";
 	}
 	
 	@RenderMapping(params = "myaction=successDM")
 	public String showSuccessDM(RenderResponse response, SessionStatus status) {
 		status.setComplete();
 		return "successDM";
 	}
 	
 	@RenderMapping(params = "myaction=error")
 	public String showError(RenderResponse response) {
 		return "error";
 	}
 
 	/**
 	 * Present to the portlet a list of the IDP supported by the portal.
 	 * 
 	 * @return List of Idp that contain all the Idp stored in the database
 	 */
 	@ModelAttribute("idps")
 	public List<Idp> getIdps() {
 		return idpService.getAllIdp();
 	}
 	
 	@ModelAttribute("vaiqui")
 	public String getMydataUrl() {
 
 		/*
 		 * 1 prendi file
 		 * 2 leggi prop proxy.expiration.times
 		 * 3 parsa e metti in array
 		 */
 		
 		
 		//1
 		
 		String contextPath = LoginController.class.getClassLoader()
 				.getResource("").getPath();
 		
 		String result = "";
 
 		File test = new File(contextPath + "/content/MyProxy.properties");
 		if (test.exists()) {
 			
 			try {
 				FileInputStream inStream = new FileInputStream(contextPath
 						+ "/content/MyProxy.properties");
 
 				Properties prop = new Properties();
 
 				prop.load(inStream);
 
 				inStream.close();
 				
 				//2
 				result = prop.getProperty("mydata.url");
 
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		
  		
 		return result;
 	}
 
 	/**
 	 * Present to the portlet if the grid proxy of the user was downloaded or
 	 * not, and destroy the proxy if expired.
 	 * 
 	 * @return the value of the attribute downloaded
 	 */
 	@ModelAttribute("proxyDownloaded")
 	public boolean getProxyDownloaded(RenderRequest request) {
 
 		User user = (User) request.getAttribute(WebKeys.USER);
 		if (user != null) {
 			String dir = System.getProperty("java.io.tmpdir");
 			log.info("Directory = " + dir);
 			
 			UserInfo userInfo = userInfoService.findByUsername(user.getScreenName());
 			List<Vo> vos = userToVoService.findVoByUserId(userInfo
 					.getUserId());
 			File proxyVoFile = null;
 			for (Iterator<Vo> iterator = vos.iterator(); iterator
 					.hasNext();) {
 				Vo vo = (Vo) iterator.next();
 				proxyVoFile = new File(dir + "/users/"
 						+ user.getUserId() + "/x509up."
 						+ vo.getVo());
 
 				if (proxyVoFile.exists()&&vo.getConfigured().equals("true")) {
 					try {
 						GlobusCredential cred = new GlobusCredential(
 								proxyVoFile.toString());
 						if (cred.getTimeLeft() > 0) {
 							return true;
 						} else {
 							proxyVoFile.delete();
 							SessionMessages.add(request, "proxy-expired-deleted");
 						}
 					} catch (GlobusCredentialException e) {
 						e.printStackTrace();
 						log.info("e mò sono cazzi amari");
 					}
 				}
 			}
 
 		}
 		return false;
 	}
 	
 	@ModelAttribute("voNumber")
 	public int getVoNumber(RenderRequest request) {
 
 		User user = (User) request.getAttribute(WebKeys.USER);
 		
 		
 		if (user != null) {
 			String username = user.getScreenName();
 
 			UserInfo userInfo = userInfoService.findByUsername(username);
 			List<Vo> vos = userToVoService.findVoByUserId(userInfo.getUserId());
 			int count = 0;
 			for(Vo vo: vos){
 				if(vo.getConfigured().equals("true"))
 					count++;
 			}
 			return count;
 		}
 		return 0;
 	}
 	
 	/**
 	 * Return to the portlet the list of the user's fqans.
 	 * @param request: session parameter.
 	 * @return the list of the user's fqans.
 	 */
 	@ModelAttribute("userFqans")
 	public String getUserFqans(RenderRequest request) {
 		User user = (User)request.getAttribute(WebKeys.USER);
 		if(user!=null){
 			UserInfo userInfo = userInfoService.findByUsername(user.getScreenName());
 			if(userToVoService.findVoByUserId(userInfo.getUserId()).size()==1){
 				List<UserToVo> utvs = userToVoService.findById(userInfo.getUserId());
 				UserToVo utv = utvs.get(0);
 				Vo vo = userToVoService.findVoByUserId(userInfo.getUserId()).get(0);
 				if(vo.getConfigured().equals("true"))
 					return utv.getFqans();
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Return to the portlet the list of the user's fqans.
 	 * @param request: session parameter.
 	 * @return the list of the user's fqans.
 	 * @throws GlobusCredentialException 
 	 */
 	@ModelAttribute("proxys")
 	public String getProxys(RenderRequest request, RenderResponse response) throws GlobusCredentialException {
 		
 		PortletConfig portletConfig = (PortletConfig)request.getAttribute(JavaConstants.JAVAX_PORTLET_CONFIG);
 		SessionMessages.add(request, portletConfig.getPortletName() + SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_ERROR_MESSAGE);
 		
 		String result = "";
 		User user = (User)request.getAttribute(WebKeys.USER);
 		if(user!=null){
 			UserInfo userInfo = userInfoService.findByUsername(user.getScreenName());
 			List<Vo> vos = userToVoService.findVoByUserId(userInfo
 					.getUserId());
 			String dir = System.getProperty("java.io.tmpdir");
 			File proxyVoFile = null;
 			for (Iterator<Vo> iterator = vos.iterator(); iterator
 					.hasNext();) {
 				Vo vo = (Vo) iterator.next();
 				proxyVoFile = new File(dir + "/users/"
 						+ user.getUserId() + "/x509up."
 						+ vo.getVo());
 				
 				if(proxyVoFile.exists()&&vo.getConfigured().equals("true")){
 					
 //					GlobusCredential cred = new GlobusCredential(
 //							proxyVoFile.toString());
 					if (getExpirationTime(proxyVoFile.getAbsolutePath()) <= 0) {
 						proxyVoFile.delete();
 						SessionMessages.add(request, "proxy-expired-deleted");
 					}else{
 						String role="             no role";
 						try {
 							
 							UserToVo utv = userToVoService.findById(userInfo.getUserId(), vo.getIdVo());
 							
 							String toParse = utv.getFqans();
 							String roles[]=null;
 							if(toParse!=null){
 								roles=toParse.split(";");
 							}
 							
 							log.info("Directory = " + dir);
 
 							//String proxy = dir + "/users/" + user.getUserId() + "/x509up";
 							
 							String cmd = "voms-proxy-info -all -file " + proxyVoFile.toString();
 							
 							log.info("cmd = " + cmd);
 							Process p = Runtime.getRuntime().exec(cmd);
 							InputStream stdout = p.getInputStream();
 							InputStream stderr = p.getErrorStream();
 
 							BufferedReader output = new BufferedReader(new InputStreamReader(
 									stdout));
 							String line = null;
 							
 							List<String> tmpRole = new ArrayList<String>();
 							
 							
 							while ((line = output.readLine()) != null) {
 								log.info("[Stdout] " + line);
 								if(roles!=null){
 									if(line.contains("/"+vo.getVo()+"/Role=NULL")){
 										tmpRole.add(role);
 										log.info("trovato: "+ role);
 									}
 									for(int i=0; i<roles.length; i++){
 										//log.info("check con: "+roles[i]);
 										if(line.contains(roles[i].trim())){
 											tmpRole.add(roles[i].trim());
 											log.info("trovato: "+ roles[i]);
 										}
 									}
 									
 									/*if(line.contains("attribute : ")){
 										check = true;
 									}*/
 								}
 							}
 							if(!tmpRole.isEmpty())
 								role=tmpRole.get(0);
 							log.info("Fine trovato: "+ role);
 							output.close();
 							
 							//boolean error = false;
 
 							BufferedReader brCleanUp = new BufferedReader(
 									new InputStreamReader(stderr));
 							while ((line = brCleanUp.readLine()) != null) {
 								//error= true;
 								log.error("[Stderr] " + line);
 							}
 							/*if(error)
 								SessionErrors.add(request, "voms-proxy-init-problem");*/
 							brCleanUp.close();
 							
 							
 
 						} catch (IOException e) {
 							
 							SessionErrors.add(request, "voms-proxy-init-exception");
 							e.printStackTrace();
 						}
 						
 //						long totalSecs =cred.getTimeLeft();
 						long totalSecs = getExpirationTime(proxyVoFile.getAbsolutePath());
 						long hours = totalSecs / 3600;
 						long minutes = (totalSecs % 3600) / 60;
 						long seconds = totalSecs % 60;
 						PortletURL url = response.createRenderURL();
 						url.setParameter("myaction", "showRenewProxy");
 						url.setParameter("idVo", Integer.toString(vo.getIdVo()));
 						try {
 							url.setWindowState(LiferayWindowState.POP_UP);
 						} catch (WindowStateException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 //						String button = "<input type=\"submit\" value=\"Renew\" onClick=\"location.href=\'"+url+"\';\"></input>";
 						String button = "<div id=\"linkImg\"><a href=\""+url+"\" onmouseover=\"viewTooltip('#renewButton');\" onclick=\"$(this).modal({width:400, height:300, message:true}).open(); return false;\"><img src=\"" + request.getContextPath() + "/images/update.png\" width=\"24\" height=\"24\" style=\"float: right; padding-right:10px;\"/></a></div>";
 						String timeLeft = hours + ":" + minutes +  ":" + seconds;
 						if(hours < 1){
 							timeLeft = "<span style=\"color:red\"><strong>" + timeLeft + "</strong></span>";
 							button += "<div id=\"tooltipImg\"><a href=\"#warning\"><img src=\"" + request.getContextPath() + "/images/alert.png\" width=\"24\" height=\"24\" style=\"float: right; padding-right:10px;\"/></a></div>";
 						}else{
 							timeLeft = "<span style=\"color:#1ea22a\"><strong>" + timeLeft + "</strong></span>";
 							button += "<div id=\"tooltipImg\"><a href=\"#allOK\"><img src=\"" + request.getContextPath() + "/images/NewCheck.png\" width=\"24\" height=\"24\" style=\"float: right; padding-right:10px;\"/></a></div>";
 						}
 						
 						
 						result += "<tr>" +
 									"<td colspan=\"3\" style=\"color: #4c4f50; padding-top: 10px; border-color:#4c4f50; border-style: solid; border-width: 0 0 1px 0;\"><strong>VO: " + vo.getVo() + "</strong></td>" +
 								  "</tr>" +
 								  "<tr style=\"border-color:#4c4f50; border-style: solid; border-width: 0 1px 0 1px; background-color: #afafaf; cursor: pointer;\">" +
 								    "<td onclick=\"$('#shortDetails').show(); $('#details').hide();\" style='width: 60px; padding-left: 5px;'> <strong>Role:</strong>&nbsp&nbsp</td>" +
 								    "<td onclick=\"$('#shortDetails').show(); $('#details').hide();\"> " + role + "&nbsp&nbsp</td>" +
 								    "<td style='width: 70px;  border-color:#4c4f50; border-style: solid; border-width: 0 0 1px 0;'rowspan=\"2\" align=\"right\">" + button + "</td>"  +
 								  "</tr>" +
 								  "<tr style=\"border-color:#4c4f50; border-style: solid; border-width: 0 1px 1px 1px; background-color: #afafaf; cursor: pointer;\">" +
 								    "<td onclick=\"$('#shortDetails').show(); $('#details').hide();\" style='width: 60px; padding-left: 5px;'> <strong>TimeLeft:</strong>&nbsp&nbsp</td>" +
 								    "<td onclick=\"$('#shortDetails').show(); $('#details').hide();\">" + timeLeft + "</td>" +
 								  "</tr> *";
 					}
 				}
 			}
 		}
 		return result;
 	}
 
 	private long getExpirationTime(String proxyFile) {
 
 		long result = 0;
 
 		try {
			String cmd = "voms-proxy-info -timeleft -file " + proxyFile;
 
 			log.info("cmd = " + cmd);
 			Process p = Runtime.getRuntime().exec(cmd);
 			InputStream stdout = p.getInputStream();
 			InputStream stderr = p.getErrorStream();
 
 			BufferedReader output = new BufferedReader(new InputStreamReader(
 					stdout));
 			String line = null;
 
 			long totalSecs=0;
 			
 			while ((line = output.readLine()) != null) {
 				log.info("[Stdout] " + line);
 				totalSecs = Long.parseLong(line);
 				
 //				long hours = totalSecs / 3600;
 //				long minutes = (totalSecs % 3600) / 60;
 //				long seconds = totalSecs % 60;
 //
 //				long[] newResult = { hours, minutes, seconds };
 				result = totalSecs;
 				
 			}
 
 			output.close();
 
 			BufferedReader brCleanUp = new BufferedReader(
 					new InputStreamReader(stderr));
 			while ((line = brCleanUp.readLine()) != null) {
 
 				log.error("[Stderr] " + line);
 			}
 
 			brCleanUp.close();
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 		}
 
 		return result;
 	}
 	
 	@ModelAttribute("shortProxies")
 	public List<String> shortProxies(RenderRequest request){
 		User user = (User)request.getAttribute(WebKeys.USER);
 		
 		List<String> result = new ArrayList<String>();
 		if(user!=null){
 			String dir = System.getProperty("java.io.tmpdir");
 			dir += "/users/"+user.getUserId()+"/";
 			
 			String proxyPrefix = dir + "x509up.";
 			
 			UserInfo userInfo = userInfoService.findByMail(user.getEmailAddress());
 			List<Vo> vos = userToVoService.findVoByUserId(userInfo.getUserId());
 			
 			for(Vo vo: vos){
 				File proxy = new File(proxyPrefix+vo.getVo());
 				
 				if(proxy.exists()){
 					
 					long totalSecs = getExpirationTime(proxy.getAbsolutePath());
 					long hours = totalSecs / 3600;
 					String color = "green";
 					if(hours<5)
 						color = "yellow";
 					if(hours<2)
 						color = "orange";
 					if(hours<1)
 						color = "black";
 					result.add(vo.getVo()+"|"+color+"|"+vo.getIdVo());
 //					result.add(vo.getVo());
 				}
 			}
 			
 		}
 		return result;
 	}
 }
