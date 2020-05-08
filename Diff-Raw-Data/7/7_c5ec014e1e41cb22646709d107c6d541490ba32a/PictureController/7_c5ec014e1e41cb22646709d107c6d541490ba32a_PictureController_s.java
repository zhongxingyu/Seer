 package com.zhiweiwang.datong.controller;
 
 import com.zhiweiwang.datong.DTContants;
 import com.zhiweiwang.datong.DTMessage;
 import com.zhiweiwang.datong.mapper.StudentMapper;
 import com.zhiweiwang.datong.model.User;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.util.FileCopyUtils;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.servlet.ModelAndView;
 import static org.springframework.web.bind.annotation.RequestMethod.GET;
 import static org.springframework.web.bind.annotation.RequestMethod.POST;
 import static com.zhiweiwang.datong.DTContants.IMG_TYPE_ALLOWED;
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 @Controller
 @SessionAttributes(DTContants.USER_IN_SESSION)
 public class PictureController {
 	private Logger logger = LoggerFactory.getLogger(this.getClass());
 
 	@Autowired
 	private StudentMapper studentMapper;
 
 	private String uploadDic ="/upload";
 	
 	@RequestMapping(value = "/pic", method = POST)
 	@ModelAttribute(DTContants.IMG_PATH)
 	public ModelAndView getpic(@RequestParam(required = false, value = "fileToUpload") MultipartFile file,
 			@RequestParam("action") String action, @ModelAttribute(DTContants.USER_IN_SESSION) User user, HttpServletRequest request) {
 		ModelAndView mav = new ModelAndView();
 		if ("add".equals(action)) {
 			String uploadDirPath = request.getSession().getServletContext().getRealPath(uploadDic);
 			MultipartFile image = file;
 			String filetype = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1).toLowerCase();
 			if(fileIsAllowed(filetype) == false){
 				mav.addObject(DTContants.MSG_ERRER, DTMessage.UPLOAD_FILE_NOT_ALLOW);
 				mav.setViewName("redirect:/message");
 				return mav;
 			}
 			logger.debug("uploadDirPath: " + uploadDirPath);
 			//File destFile = new File(uploadDirPath + "/" + image.getOriginalFilename());
 			File destFile = new File(uploadDirPath + "/img" + user.getId()+"."+filetype);
 			try {
 				if (destFile.exists() == false)
 					destFile.createNewFile();
 				FileCopyUtils.copy(image.getBytes(), destFile);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			String destPath = request.getContextPath() + uploadDic+"/" + destFile.getName();
 			logger.debug("destPath: " + destPath);
 			mav.getModel().put(DTContants.IMG_PATH, destPath);
 
 			request.getSession().setAttribute(DTContants.IMG_PATH, destPath);
 			
 		} else if ("del".equals(action)) {
 			studentMapper.updatePic(user.getId(), null);
 			request.getSession().removeAttribute(DTContants.IMG_PATH);
 			mav.setViewName("redirect:/pic");
 		}
 		return mav;
 	}
 
 	@RequestMapping(value = "/pic", method = GET)
 	public ModelAndView get(@ModelAttribute(DTContants.USER_IN_SESSION) User user, HttpSession session) {
 		ModelAndView mav = new ModelAndView();
 		Map<?, ?> student = studentMapper.getStudent(user.getId());
 
 		if(student != null){
 			Object img = student.get(DTContants.IMG_PATH);
 			if(img != null){
 				mav.getModel().put(DTContants.IMG_PATH, img.toString());
 				session.setAttribute(DTContants.IMG_PATH, img.toString());
 			}
 		}
 		return mav;
 	}
 
 	@RequestMapping(value = "/pic/{nid}", method = GET)
 	public ModelAndView getPica(@PathVariable("nid") int id , HttpSession session) {
 		ModelAndView mav = new ModelAndView("pic");
 		Map<?, ?> student = studentMapper.getStudent(id);
 
 		if(student != null){
 			Object img = student.get(DTContants.IMG_PATH);
 			if(img != null){
 				mav.getModel().put(DTContants.IMG_PATH, img.toString());
 				session.setAttribute(DTContants.IMG_PATH, img.toString());
 			}
 		}
 		return mav;
 		
 	}
 
 	private boolean fileIsAllowed(String filetype) {
 		
 		for(String type: IMG_TYPE_ALLOWED){
 			if(type.equals(filetype)){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public String getUploadDic() {
 		return uploadDic;
 	}
 
 	public void setUploadDic(String uploadDic) {
 		this.uploadDic = uploadDic;
 	}
 }
