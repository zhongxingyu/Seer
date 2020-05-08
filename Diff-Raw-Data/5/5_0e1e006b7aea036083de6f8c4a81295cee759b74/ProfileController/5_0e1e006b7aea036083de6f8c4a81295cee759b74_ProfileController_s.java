 package com.imeeting.mvc.controller;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.annotation.PostConstruct;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.multipart.MultipartFile;
 
 import com.imeeting.framework.Configuration;
 import com.imeeting.framework.ContextLoader;
 import com.imeeting.web.user.UserBean;
 import com.richitec.ucenter.model.UserDAO;
 import com.richitec.util.MD5Util;
 import com.richitec.util.RandomString;
 
 @Controller
 @RequestMapping(value="/profile")
 public class ProfileController {
 	
 	private static Log log = LogFactory.getLog(ProfileController.class);
 	
 	private Configuration config;
 	private UserDAO userDao;
 	
 	@PostConstruct
 	public void init(){
 		config = ContextLoader.getConfiguration();
 		userDao = ContextLoader.getUserDAO();
 	}
 	
 	@RequestMapping(value="/changepassword", method=RequestMethod.POST)
 	public @ResponseBody String changePassword(
 			HttpSession session,
 			HttpServletResponse response,
 			@RequestParam(value="oldPwd") String oldPwd, 
 			@RequestParam(value="newPwd") String newPwd,
 			@RequestParam(value="newPwdConfirm") String newPwdConfirm) throws IOException{
 		UserBean user = (UserBean) session.getAttribute(UserBean.SESSION_BEAN);
 		if (!oldPwd.equals(user.getPassword())){
			return "403";
 		}
 		
 		if (newPwd.isEmpty() || !newPwd.equals(newPwdConfirm)){
			return "400";
 		}
 		
 		String md5Password = MD5Util.md5(newPwd);
 		if (userDao.changePassword(user.getName(), md5Password)<=0){
 			return "500";
 		}
 		
 		user.setPassword(md5Password);
 		return "200";
 	}
 	
 	@RequestMapping(value="/avatar", method=RequestMethod.GET)
 	public String uploadAvatar(){
 		return "avatar";
 	}	
 	
 	@RequestMapping(value="/avatar", method=RequestMethod.POST)
 	public String avatarHandler(
 			@RequestParam("username") String username,
 			@RequestParam("avatar") MultipartFile avatarFile) throws IllegalStateException, IOException{
 		log.info("Username: " + username);
 		log.info("File Origin Name: " + avatarFile.getOriginalFilename());
 		log.info("File Name: " + avatarFile.getName());
 		log.info("File Size: " + avatarFile.getSize());
 		String tmpDir = config.getUploadDir();
 		String source_id = "im_" + RandomString.genRandomNum(10);
 		String tmpFile = tmpDir + source_id;
 		avatarFile.transferTo(new File(tmpFile));
 		return "avatar";
 	}	
 }
