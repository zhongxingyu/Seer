 package com.xlthotel.core.controller;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.PrintWriter;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.xml.crypto.NoSuchMechanismException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
 
 
 import com.xlthotel.core.service.MailService;
 import com.xlthotel.foundation.common.Constant;
 import com.xlthotel.foundation.common.DateUtils;
 import com.xlthotel.foundation.common.MD5Utils;
 import com.xlthotel.foundation.common.SimpleServletRequestUtils;
 import com.xlthotel.foundation.common.SysConfigUtil;
 import com.xlthotel.foundation.common.SystemPropertyConfig;
 import com.xlthotel.foundation.common.ValidateCodeUtil;
 import com.xlthotel.foundation.exception.XltHotelException;
 import com.xlthotel.foundation.orm.entity.Notice;
 import com.xlthotel.foundation.orm.entity.SysConfig;
 import com.xlthotel.foundation.orm.entity.UserInfo;
 import com.xlthotel.foundation.orm.entity.UserPoint;
 import com.xlthotel.foundation.service.CodeDataService;
 import com.xlthotel.foundation.service.SequenceGenService;
 import com.xlthotel.foundation.service.UserInfoService;
 import com.xlthotel.foundation.service.UserNoticeWebService;
 import com.xlthotel.foundation.service.UserPointService;
 import com.xlthotel.foundation.service.impl.SequenceGen;
 
 import freemarker.template.Configuration;
 import freemarker.template.Template;
 
 /**
  * @author lxl
  * 
  */
 @Controller
 @RequestMapping("/servlet/user/reg.do")
 public class RegController {
 
 	private static final Log logger = LogFactory.getLog(RegController.class);
 	@Autowired
 	CodeDataService codeDataService;
 	@Autowired
 	private ValidateCodeUtil validateCodeUtil;
 	@Autowired
 	private UserInfoService userInfoService;
 	@Autowired
 	private SysConfigUtil sysConfigUtil;
 	@Autowired
 	private SequenceGenService sequenceGenService;
 	@Autowired
 	private MailService mailService;
 	@Autowired
 	private UserPointService userPointService;
 	@Autowired
 	private FreeMarkerConfigurer freeMarkerConfigurer;
 	@Autowired
 	private UserNoticeWebService userNoticeWebService;
 	@Autowired
 	private SystemPropertyConfig systemPropertyConfig;
 	@RequestMapping(params = "method=protocol")
 	public ModelAndView protocol() {
 		return new ModelAndView("/reg/protocol");
 	}
 	//初始化注册
 	@RequestMapping(method = RequestMethod.GET,params = "method=initReg")
 	public ModelAndView initReg(Model model, HttpServletRequest request,
 			HttpServletResponse response) {
 		String fail_msg = "";
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		if(this.sysConfigUtil.getOpenUserReg() == 0 ){
 			fail_msg = "目前系统已经关闭用户注册,请稍后再试.";
 			returnModel.put("fail_msg", fail_msg);
 			return new ModelAndView("/reg/reg_fail_msg", returnModel);
 		}
 		// 初始化下拉证件类型
 		List certTypes = this.codeDataService
 				.findValueByTypeList(Constant.CERT_TYPE);
 		returnModel.put("certTypes", certTypes);
 		return new ModelAndView("/reg/reg", returnModel);
 	}
 	//注册
 	@RequestMapping(method = RequestMethod.POST,params = "method=userReg")
 	public ModelAndView userReg(Model model, HttpServletRequest request,
 			HttpServletResponse response) {
 		String fail_msg = "";
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		if(this.sysConfigUtil.getOpenUserReg() == 0 ){
 			fail_msg = "目前系统已经关闭用户注册,请稍后再试.";
 			returnModel.put("fail_msg", fail_msg);
 			return new ModelAndView("/reg/reg_fail_msg", returnModel);
 		}
 		String userName = SimpleServletRequestUtils.getStringParameter(request, "userName", "");
 		String nickName = SimpleServletRequestUtils.getStringParameter(request, "nickName", "");
 		String birthday  = SimpleServletRequestUtils.getStringParameter(request, "birthday", "");
 		String sex = SimpleServletRequestUtils.getStringParameter(request, "sex", "");
 		String emailinform = SimpleServletRequestUtils.getStringParameter(request, "emailinform", "");
 		String mobile = SimpleServletRequestUtils.getStringParameter(request, "mobile", "");
 		String email = SimpleServletRequestUtils.getStringParameter(request, "email", "");
 		String certType = SimpleServletRequestUtils.getStringParameter(request, "certType", "");
 		String certId = SimpleServletRequestUtils.getStringParameter(request, "certId", "");
 		String checkcode = SimpleServletRequestUtils.getStringParameter(request, "checkcode", "");
 		if(this.sysConfigUtil.isCanNotRegUserName(userName)){
 			fail_msg = "该用户姓名  '" + userName + "' 已经被禁止不允许注册,请重新注册.";
 			returnModel.put("fail_msg", fail_msg);
 			return new ModelAndView("/reg/reg_fail_msg", returnModel);
 		}
 	
 		if(this.sysConfigUtil.getUseForbid() ==1){
 			if(this.sysConfigUtil.isForbidEmail(email)){
 				fail_msg = "该EMAIL地址  '" + email + "' 已经被禁止不允许注册,请重新注册.";
 				returnModel.put("fail_msg", fail_msg);
 				return new ModelAndView("/reg/reg_fail_msg", returnModel);
 			}
 			if(this.sysConfigUtil.isForbidIP(request.getRemoteAddr())){
 				fail_msg = "该IP地址  '" + request.getRemoteAddr() + "' 已经被禁止不允许注册,请重新注册.";
 				returnModel.put("fail_msg", fail_msg);
 				return new ModelAndView("/reg/reg_fail_msg", returnModel);
 			}
 			
 		}
 		
 		String checkcode_session = (String) request.getSession().getAttribute(Constant.USER_CHECKCODE_KEY);
 		if(!checkcode.equals(checkcode_session)){
 			fail_msg = "输入验证码不正确,请重新注册.";
 			returnModel.put("fail_msg", fail_msg);
 			request.getSession().removeAttribute(Constant.USER_CHECKCODE_KEY);
 			return new ModelAndView("/reg/reg_fail_msg", returnModel);
 		}
 		request.getSession().removeAttribute(Constant.USER_CHECKCODE_KEY);
 		/**查看是够有相同名称的人注册*/
 		UserInfo ui = this.userInfoService.findUserInfoByNickName(nickName);
 		if (ui != null) {
 			fail_msg = "该昵称 '"+nickName+"' 已经被注册,请重新注册.";
 			returnModel.put("fail_msg", fail_msg);
 			return new ModelAndView("/reg/reg_fail_msg", returnModel);
         }
 		ui = this.userInfoService.findUserInfoByEmail(email);
 		if (ui != null) {
 			fail_msg = "该EMAIL地址 '"+email+"' 已经被注册,请重新注册.";
 			returnModel.put("fail_msg", fail_msg);
 			return new ModelAndView("/reg/reg_fail_msg", returnModel);
         }
 		ui = this.userInfoService.findUserInfoByMOBLE(mobile);
 		if (ui != null) {
 			fail_msg = "该手机号码 '"+mobile+"' 已经被注册,请重新注册.";
 			returnModel.put("fail_msg", fail_msg);
 			return new ModelAndView("/reg/reg_fail_msg", returnModel);
         }
 		ui = new UserInfo();
 		ui.setUserId(sequenceGenService.userId());
 		ui.setUserName(userName);
 		ui.setRePasswd(MD5Utils.MD5forPwd(mobile));
 		ui.setNickName(this.sysConfigUtil.bestrowScreenNickName(nickName));
 		ui.setBirthday(DateUtils.parse("yyyy-MM-dd", birthday));
 		ui.setEmailinform(Integer.parseInt(emailinform));
 		ui.setSex(sex);
 		ui.setMobile(mobile);
 		ui.setEmail(email);
 		ui.setCertId(certId);
 		ui.setCertType(certType);
 		ui.setRegTime(new Date());
 		ui.setLastLoginIP(request.getRemoteAddr());
 		ui.setIsValidated(0);
 		if(ui.getEmailinform()==2){
 			ui.setValidateCode(MD5Utils.MD5forPwd(ui.getEmail()));
 			ui.setIsValidateDate(ui.getRegTime());
 		}
 		ui.setStatus(0);
 		try {
 			ui = this.userInfoService.saveRegUserInfo(ui);
 			//是否采用邮件进行确认
 			if(this.sysConfigUtil.getUseEmail()==1 && ui.getEmailinform() == 2){
 				Map<String, String> root = new HashMap<String, String>();
 				 root.put("sysHost", systemPropertyConfig.getConfig("system.host"));
				 root.put("nickName", ui.getNickName());
 	             root.put("userName", ui.getUserName());
 	             root.put("userId", ui.getUserId());
 	             root.put("email", ui.getEmail());
 	             root.put("mobile", ui.getMobile());
 	             root.put("validateCode", ui.getValidateCode());
 	             root.put("repassword", ui.getMobile());
 	             root.put("date", DateUtils.getNowDate());
 				 mailService.send("仙龙潭邮箱验证",generateEmailContents("regValidate.ftl",root), email);
 			}
 		} catch (Exception e) {
             e.printStackTrace();
             fail_msg = "系统异常,请重新注册.";
 			returnModel.put("fail_msg", fail_msg);
 			return new ModelAndView("/reg/reg_fail_msg", returnModel);
         }
 		returnModel.put("ui",ui);
 		return new ModelAndView("/reg/reg_succee_msg", returnModel);
 	}
 	//验证码
 	@RequestMapping(params = "method=checkCode")
 	public void checkCode(HttpServletRequest request,
 			HttpServletResponse response,
 			@RequestParam("timestamp") String timestamp)
 			throws XltHotelException {
 
 		try {
 			response.setContentType("image/jpeg");
 			response.setHeader("Pragma", "No-cache");
 			response.setHeader("Cache-Control", "no-cache");
 			response.setDateHeader("Expires", 0);
 			/**
 			 * 生成图片
 			 */
 			BufferedImage image = validateCodeUtil.creatImage();
 			/**
 			 * 输出图片
 			 */
 			ServletOutputStream sos = response.getOutputStream();
 			ByteArrayOutputStream bos = new ByteArrayOutputStream();
 			ImageIO.write(image, "JPEG", bos);
 			byte[] buf = bos.toByteArray();
 			response.setContentLength(buf.length);
 			sos.write(buf);
 			bos.close();
 			sos.close();
 			/**
 			 * 获取随机数
 			 */
 			HttpSession session = request.getSession();
 			session.setAttribute(Constant.USER_CHECKCODE_KEY,
 					validateCodeUtil.getSRand());
 		} catch (Exception e) {
 			throw new XltHotelException("<li>随机验证码出现异常。", e);
 		}
 	}
 	//动态验证验证码
 	@RequestMapping(method = RequestMethod.POST,params = "method=validateCode")
 	public void validateCode(HttpServletRequest request,
 			HttpServletResponse response) throws XltHotelException {
 
 		try {
 			response.setHeader("Pragma", "No-cache");
 			response.setHeader("Cache-Control", "no-cache");
 			response.setDateHeader("Expires", 0);
 			response.setContentType("text/html;charset=utf-8");
 			String validateC = (String) request.getSession().getAttribute(
 					Constant.USER_CHECKCODE_KEY);
 			String veryCode = request.getParameter("checkcode");
 			PrintWriter out = response.getWriter();
 			if (veryCode == null || "".equals(veryCode)) {
 				out.println(false);
 			} else {
 				if (validateC.equals(veryCode)) {
 					out.println(true);
 				} else {
 					out.println(false);
 				}
 			}
 			out.flush();
 			out.close();
 		} catch (Exception e) {
 			throw new XltHotelException("<li>随机验证码出现异常。", e);
 		}
 	}
 	//动态验证验昵称
 	@RequestMapping(method = RequestMethod.POST,params = "method=validateNickName")
 	public void validateNickName(HttpServletRequest request,
 			HttpServletResponse response) throws XltHotelException {
 
 		try {
 			response.setHeader("Pragma", "No-cache");
 			response.setHeader("Cache-Control", "no-cache");
 			response.setDateHeader("Expires", 0);
 			response.setContentType("text/html;charset=utf-8");
 			String nickName = request.getParameter("nickName");
 			PrintWriter out = response.getWriter();
 			if (nickName == null || "".equals(nickName)) {
 				out.println(false);
 			} else {
 				UserInfo ui = this.userInfoService.findUserInfoByNickName(nickName);
 				if (ui==null) {
 					out.println(true);
 				} else {
 					out.println(false);
 				}
 			}
 			out.flush();
 			out.close();
 		} catch (Exception e) {
 			throw new XltHotelException("<li>验证昵称异常。", e);
 		}
 	}
 	//动态验证验电话
 	@RequestMapping(method = RequestMethod.POST,params = "method=validateMobile")
 	public void validateMobile(HttpServletRequest request,
 			HttpServletResponse response) throws XltHotelException {
 
 		try {
 			response.setHeader("Pragma", "No-cache");
 			response.setHeader("Cache-Control", "no-cache");
 			response.setDateHeader("Expires", 0);
 			response.setContentType("text/html;charset=utf-8");
 			String mobile = request.getParameter("mobile");
 			PrintWriter out = response.getWriter();
 			if (mobile == null || "".equals(mobile)) {
 				out.println(false);
 			} else {
 				UserInfo ui = this.userInfoService.findUserInfoByMOBLE(mobile);
 				if (ui==null) {
 					out.println(true);
 				} else {
 					out.println(false);
 				}
 			}
 			out.flush();
 			out.close();
 		} catch (Exception e) {
 			throw new XltHotelException("<li>随机验证码出现异常。", e);
 		}
 	}
 	//动态验证验email
 	@RequestMapping(method = RequestMethod.POST,params = "method=validateEmail")
 	public void validateEmail(HttpServletRequest request,
 			HttpServletResponse response) throws XltHotelException {
 
 		try {
 			response.setHeader("Pragma", "No-cache");
 			response.setHeader("Cache-Control", "no-cache");
 			response.setDateHeader("Expires", 0);
 			response.setContentType("text/html;charset=utf-8");
 			String email = request.getParameter("email");
 			PrintWriter out = response.getWriter();
 			if (email == null || "".equals(email)) {
 				out.println(false);
 			} else {
 				UserInfo ui = this.userInfoService.findUserInfoByEmail(email);
 				if (ui==null) {
 					out.println(true);
 				} else {
 					out.println(false);
 				}
 			}
 			out.flush();
 			out.close();
 		} catch (Exception e) {
 			throw new XltHotelException("<li>随机验证码出现异常。", e);
 		}
 	}
 	//激活用户
 	@RequestMapping(params = "method=regValidate")
 	public ModelAndView regValidate(Model model, HttpServletRequest request,
 			HttpServletResponse response) {
 		String fail_msg = "";
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		String nickName = SimpleServletRequestUtils.getStringParameter(request, "nickName", "");
 		String mobile = SimpleServletRequestUtils.getStringParameter(request, "mobile", "");
 		String email = SimpleServletRequestUtils.getStringParameter(request, "email", "");
 		String validateCode = SimpleServletRequestUtils.getStringParameter(request, "validateCode", "");
 		UserInfo ui = null;
 		if(email.isEmpty()){
 			fail_msg = "用户邮箱验证失败,请稍后再试.";
 			returnModel.put("fail_msg", fail_msg);
 			return new ModelAndView("/reg/reg_validate_fail", returnModel);
 		}
 		ui =  this.userInfoService.findUserInfoByEmail(email);
 		if(ui==null){
 			fail_msg = "用户邮箱验证失败,请稍后再试.";
 			returnModel.put("fail_msg", fail_msg);
 			return new ModelAndView("/reg/reg_validate_fail", returnModel);
 		}
 		if((System.currentTimeMillis()-ui.getIsValidateDate().getTime())>1000 * 60 * 30){
 			fail_msg = "用户邮箱验证失败,邮件验证码超过了有效期.";
 			returnModel.put("fail_msg", fail_msg);
 			return new ModelAndView("/reg/reg_validate_fail", returnModel);
 		};
 		if(!ui.getValidateCode().equals(validateCode)){
 			fail_msg = "用户邮箱验证失败,激活码错误.";
 			returnModel.put("fail_msg", fail_msg);
 			return new ModelAndView("/reg/reg_validate_fail", returnModel);
 		}
 		if(ui.getIsValidated()==2){
 			fail_msg = "您已经激活成功，请<a href=\"javascript:goLogin();\"><font color=\"#0008D1\">登录</font></a>仙龙潭酒店官查看.";
 			returnModel.put("fail_msg", fail_msg);
 			return new ModelAndView("/reg/reg_validate_fail", returnModel);
 		}
 		ui.setIsValidated(2);
 		try {
 			ui = this.userInfoService.saveUserInfo(ui);
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail_msg = "系统异常,用户邮箱验证失败,激活码错误,请稍后再试.";
 			returnModel.put("fail_msg", fail_msg);
 			return new ModelAndView("/reg/reg_validate_fail", returnModel);
 		}
 		returnModel.put("nickName", nickName);
 		returnModel.put("userName", ui.getUserName());
 		returnModel.put("password", mobile);
 		returnModel.put("userId", ui.getUserId());
 		return new ModelAndView("/reg/reg_validate_suc", returnModel);
 	}
 	
 	/**   
      * 邮件重发
      */
 	@RequestMapping(method = RequestMethod.POST,params = "method=reSendEmail")
 	public void reSendEmail(HttpServletRequest request,
 			HttpServletResponse response) throws XltHotelException {
 		String flag = "";
 		try {
 			response.setHeader("Pragma", "No-cache");
 			response.setHeader("Cache-Control", "no-cache");
 			response.setDateHeader("Expires", 0);
 			response.setContentType("text/html;charset=utf-8");
 			String email = SimpleServletRequestUtils.getStringParameter(request, "email", "");
 			String validateCode = SimpleServletRequestUtils.getStringParameter(request, "validateCode", "");
 			String id = SimpleServletRequestUtils.getStringParameter(request, "id", "");
 			PrintWriter out = response.getWriter();
 			UserInfo ui = null;
 			ui =  this.userInfoService.findUserInfoById(id);
 			if(ui==null||ui.getIsValidated()==2){
 				flag = "1";
 				out.println(flag);
 				out.flush();
 				out.close();
 				return;
 			}
 			if(email.isEmpty()||!ui.getValidateCode().equals(validateCode)){
 				flag = "3";
 				out.println(flag);
 				out.flush();
 				out.close();
 				return;
 			}
 			ui.setIsValidateDate(new Date());
 			ui = this.userInfoService.saveUserInfo(ui);
 			//是否采用邮件进行确认
 			if(this.sysConfigUtil.getUseEmail()==1 && ui.getEmailinform() == 2){
 				Map<String, String> root = new HashMap<String, String>();
 				root.put("sysHost", systemPropertyConfig.getConfig("system.host"));
				 root.put("nickName", ui.getNickName());
 	             root.put("userName", ui.getUserName());
 	             root.put("userId", ui.getUserId());
 	             root.put("email", ui.getEmail());
 	             root.put("mobile", ui.getMobile());
 	             root.put("validateCode", ui.getValidateCode());
 	             root.put("repassword", ui.getMobile());
 	             root.put("date", DateUtils.getNowDate());
 				 mailService.send("仙龙潭邮箱验证",generateEmailContents("regValidate.ftl",root), email);
 			}
 			flag = "2";
 			out.println(flag);
 			out.flush();
 			out.close();
 		} catch (Exception e) {
 			throw new XltHotelException("<li>随机验证码出现异常。", e);
 		}
 	}
 	
 	/**   
      * 更改邮件后邮件重发
      */
 	@RequestMapping(method = RequestMethod.POST,params = "method=reSendActiveCode")
 	public void reSendActiveCode(HttpServletRequest request,
 			HttpServletResponse response) throws XltHotelException {
 		
 		try {
 			response.setHeader("Pragma", "No-cache");
 			response.setHeader("Cache-Control", "no-cache");
 			response.setDateHeader("Expires", 0);
 			response.setContentType("text/html;charset=utf-8");
 			String email = SimpleServletRequestUtils.getStringParameter(request, "email", "");
 			String validateCode = SimpleServletRequestUtils.getStringParameter(request, "validateCode", "");
 			String id = SimpleServletRequestUtils.getStringParameter(request, "id", "");
 			PrintWriter out = response.getWriter();
 			if (email.isEmpty()) {
 				out.println(4);
 				out.flush();
 				out.close();
 				return;
 			} 
 			UserInfo ui = this.userInfoService.findUserInfoById(id);
 				if (ui.getIsValidated()==2) {
 					out.println(2);
 					out.flush();
 					out.close();
 					return;
 				} 
 				if (!ui.getValidateCode().equals(validateCode)) {
 					out.println(4);
 					out.flush();
 					out.close();
 					return;
 				}
 			UserInfo ui_email = this.userInfoService.findUserInfoByEmail(email);
 				if (ui_email!=null) {
 					out.println(3);
 					out.flush();
 					out.close();
 					return;
 				}
 			ui.setEmail(email);
 			ui.setValidateCode(MD5Utils.MD5forPwd(email));
 			ui.setIsValidateDate(new Date());
 			if(this.sysConfigUtil.getUseEmail()==1 && ui.getEmailinform() == 2){
 					Map<String, String> root = new HashMap<String, String>();
 		             root.put("nickName", ui.getNickName());
 		             root.put("userName", ui.getUserName());
 		             root.put("userId", ui.getUserId());
 		             root.put("email", ui.getEmail());
 		             root.put("mobile", ui.getMobile());
 		             root.put("validateCode", ui.getValidateCode());
 		             root.put("repassword", ui.getMobile());
 		             root.put("date", DateUtils.getNowDate());
 					 mailService.send("仙龙潭邮箱验证",generateEmailContents("regValidate.ftl",root), email);
 				}
 			ui = this.userInfoService.saveUserInfo(ui);
 			out.println(1);
 			out.flush();
 			out.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>随机验证码出现异常。", e);
 		}
 	}
 	
 	
 	/**   
      * 通过模版产生邮件正文   
      */ 
 	public String generateEmailContents(String templatsName,Map map){
 		try{
 			Configuration configuration = freeMarkerConfigurer.getConfiguration();
 			Template t = configuration.getTemplate(templatsName);
 			return FreeMarkerTemplateUtils.processTemplateIntoString(t, map);
 		}catch(Exception e){
 			e.printStackTrace();
 			return null;
 		}
 	}	
 }
