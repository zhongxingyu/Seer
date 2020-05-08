 package com.xlthotel.core.controller;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.PrintWriter;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.JSONObject;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 
 
 import com.xlthotel.core.service.HotelService;
 import com.xlthotel.core.service.MailService;
 import com.xlthotel.core.service.OrderService;
 import com.xlthotel.foundation.common.Condition;
 import com.xlthotel.foundation.common.Constant;
 import com.xlthotel.foundation.common.DateUtils;
 import com.xlthotel.foundation.common.MD5Utils;
 import com.xlthotel.foundation.common.Page;
 import com.xlthotel.foundation.common.PageOrder;
 import com.xlthotel.foundation.common.SimpleConditionImpl;
 import com.xlthotel.foundation.common.SimpleOrderImpl;
 import com.xlthotel.foundation.common.SimplePageImpl;
 import com.xlthotel.foundation.common.SimpleServletRequestUtils;
 import com.xlthotel.foundation.common.SysConfigUtil;
 import com.xlthotel.foundation.common.ValidateCodeUtil;
 import com.xlthotel.foundation.exception.XltHotelException;
 import com.xlthotel.foundation.orm.entity.Complaint;
 import com.xlthotel.foundation.orm.entity.Hotel;
 import com.xlthotel.foundation.orm.entity.HotelComment;
 import com.xlthotel.foundation.orm.entity.Order;
 import com.xlthotel.foundation.orm.entity.UserAddress;
 import com.xlthotel.foundation.orm.entity.UserInfo;
 import com.xlthotel.foundation.orm.entity.UserNotice;
 import com.xlthotel.foundation.service.AddressService;
 import com.xlthotel.foundation.service.ComplaintService;
 import com.xlthotel.foundation.service.HotelCommentService;
 import com.xlthotel.foundation.service.RegionService;
 import com.xlthotel.foundation.service.SequenceGenService;
 import com.xlthotel.foundation.service.UserInfoService;
 import com.xlthotel.foundation.service.UserNoticeWebService;
 import com.xlthotel.foundation.service.UserPointService;
 
 /**
  * @author lxl
  * 
  */
 @Controller
 @RequestMapping("/servlet/user/userinfo.do")
 public class UserinfoController {
 	private static final Log logger = LogFactory
 			.getLog(UserinfoController.class);
 	@Autowired
 	private UserInfoService userInfoService;
 	@Autowired
 	private AddressService addressService;
 	@Autowired
 	RegionService regionService;
 	@Autowired
 	private ValidateCodeUtil validateCodeUtil;
 	@Autowired
 	private HotelCommentService hotelCommentService;
 	@Autowired
 	private ComplaintService complaintService;
 	@Autowired
 	private UserPointService userPointService;
 	@Autowired
 	private UserNoticeWebService userNoticeWebService;
 	@Autowired
 	private SysConfigUtil sysConfigUtil;
 	@Autowired
 	private SequenceGenService sequenceGenService;
 	@Autowired
 	private MailService mailService;
 	@Autowired
 	private HotelService hotelService;
 	@Autowired
 	private OrderService orderService;
 	
 
 	/********************************** 用户信息编辑 *************************************************************/
 	// 初始化用户信息地址
 	@RequestMapping(params = "method=initUserInfo")
 	public ModelAndView initUserInfo(HttpServletRequest request,
 			HttpServletResponse response) {
 
 		try {
 			UserInfo ui = null;
 			UserInfo ui_session = (UserInfo) request.getSession().getAttribute(
 					Constant.USER_WEBSEESION_KEY);
 			ui = this.userInfoService.findUserInfoById(ui_session.getId());
 			Map<String, Object> returnModel = new HashMap<String, Object>();
 			returnModel.put("ui", ui);
 			return new ModelAndView("/userinfo/userManage", returnModel);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>系统出现异常,请稍后尝试。", e);
 		}
 
 	}
 
 	// 用户信息
 	@RequestMapping(method = RequestMethod.POST, params = "method=userInfoSave")
 	public ModelAndView userInfoSave(HttpServletRequest request,
 			HttpServletResponse response) {
 		String fail_msg = "";
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		try {
 			String mobile = SimpleServletRequestUtils.getStringParameter(
 					request, "mobile", "");
 			String birthday = SimpleServletRequestUtils.getStringParameter(
 					request, "birthday", "");
 			String newpass = SimpleServletRequestUtils.getStringParameter(
 					request, "newpass", "");
 			String renewpass = SimpleServletRequestUtils.getStringParameter(
 					request, "renewpass", "");
 			String oldpass = SimpleServletRequestUtils.getStringParameter(
 					request, "oldpass", "");
 			UserInfo ui = null;
 			UserInfo ui_session = (UserInfo) request.getSession().getAttribute(
 					Constant.USER_WEBSEESION_KEY);
 			ui = this.userInfoService.findUserInfoById(ui_session.getId());
 			if (!ui.getRePasswd().equals(MD5Utils.MD5forPwd(oldpass))) {
 				fail_msg = "该用户密码输入错误,修改失败 .";
 				returnModel.put("fail_msg", fail_msg);
 				return new ModelAndView("/msg/error_msg", returnModel);
 			}
 			if (!newpass.isEmpty() && !renewpass.isEmpty()) {
 				if (!newpass.equals(renewpass)) {
 					fail_msg = "该用户两次输入的密码不一致. ";
 					returnModel.put("fail_msg", fail_msg);
 					return new ModelAndView("/msg/error_msg", returnModel);
 				}
 				if (ui.getRePasswd().equals(MD5Utils.MD5forPwd(newpass))) {
 					fail_msg = "该用户输入的新密码与旧密码一致，请重新修改. ";
 					returnModel.put("fail_msg", fail_msg);
 					return new ModelAndView("/msg/error_msg", returnModel);
 				}
 				ui.setRePasswd(MD5Utils.MD5forPwd(renewpass));
 			}
 			if (!mobile.equals(ui.getMobile())) {
 				UserInfo ui_mobile = this.userInfoService
 						.findUserInfoByMOBLE(mobile);
 				if (ui_mobile == null) {
 					fail_msg = "该手机号码 '" + mobile + "' 已经被注册,请重新注册.";
 					returnModel.put("fail_msg", fail_msg);
 					return new ModelAndView("/msg/error_msg", returnModel);
 				}
 			}
 			ui.setBirthday(DateUtils.parse("yyyy-MM-dd", birthday));
 			ui = this.userInfoService.saveUserInfo(ui);
 			request.getSession().removeAttribute(Constant.USER_WEBSEESION_KEY);
 			request.getSession().setAttribute(Constant.USER_WEBSEESION_KEY, ui);
 			returnModel.put("ui", ui);
 			response.sendRedirect(request.getContextPath()
 					+ "/servlet/user/userinfo.do?method=initUserInfo");
 			return null;
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>系统出现异常,请稍后尝试。", e);
 		}
 
 	}
 
 	/**
 	 * 更改邮件后邮件重发
 	 */
 	@RequestMapping(method = RequestMethod.POST, params = "method=checkeMail")
 	public void checkeMail(HttpServletRequest request,
 			HttpServletResponse response) throws XltHotelException {
 
 		try {
 			response.setHeader("Pragma", "No-cache");
 			response.setHeader("Cache-Control", "no-cache");
 			response.setDateHeader("Expires", 0);
 			response.setContentType("text/html;charset=utf-8");
 			String email = SimpleServletRequestUtils.getStringParameter(request, "email", "");
 			String id = SimpleServletRequestUtils.getStringParameter(request, "id", "");
 			UserInfo ui =  this.userInfoService.findUserInfoById(id);
 			PrintWriter out = response.getWriter();
 			if (email.isEmpty()) {
 				out.println(3);
 				out.flush();
 				out.close();
 				return;
 			}
 			UserInfo ui_email = this.userInfoService.findUserInfoByEmail(email);
 			if (ui_email != null) {
 				out.println(2);
 				out.flush();
 				out.close();
 				return;
 			}
 			ui.setEmail(email);
 			ui.setValidateCode(MD5Utils.MD5forPwd(email));
 			ui = this.userInfoService.saveUserInfo(ui);
 			request.getSession().removeAttribute(Constant.USER_WEBSEESION_KEY);
 			// 设置Session中用户
 			request.getSession().setAttribute(Constant.USER_WEBSEESION_KEY,
 					ui);
 			out.println(1);
 			out.flush();
 			out.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>随机验证码出现异常。", e);
 		}
 	}
 
 	/********************************** 用户地址信息编辑 *************************************************************/
 	// 初始化送货地址
 	@RequestMapping(params = "method=initUserAdress")
 	public ModelAndView initUserAdress(HttpServletRequest request,
 			HttpServletResponse response) {
 
 		try {
 			UserInfo ui = null;
 			UserInfo ui_session = (UserInfo) request.getSession().getAttribute(
 					Constant.USER_WEBSEESION_KEY);
 			ui = this.userInfoService.findUserInfoById(ui_session.getId());
 			List addressList = this.addressService.findAddressAllByUserId(ui
 					.getId());
 			Map<String, Object> returnModel = new HashMap<String, Object>();
 			returnModel.put("addressList", addressList);
 			// 初始化省
 			List provinces = this.regionService.GetProvinceList();
 			returnModel.put("provinces", provinces);
 			returnModel.put("ui", ui);
 			return new ModelAndView("/userinfo/userAddressManage", returnModel);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>系统出现异常,请稍后尝试。", e);
 		}
 
 	}
 
 	// 保存送货地址
 	@RequestMapping(method = RequestMethod.POST, params = "method=saveUserAdress")
 	public ModelAndView saveUserAdress(HttpServletRequest request,
 			HttpServletResponse response) {
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		String fail_msg = "";
 		String userName = SimpleServletRequestUtils.getStringParameter(request,
 				"userName", "");
 		String province = SimpleServletRequestUtils.getStringParameter(request,
 				"province", "");
 		String city = SimpleServletRequestUtils.getStringParameter(request,
 				"city", "");
 		String street = SimpleServletRequestUtils.getStringParameter(request,
 				"street", "");
 		String zipCode = SimpleServletRequestUtils.getStringParameter(request,
 				"zipCode", "");
 		String phone = SimpleServletRequestUtils.getStringParameter(request,
 				"phone", "");
 		String mobile = SimpleServletRequestUtils.getStringParameter(request,
 				"mobile", "");
 		String isDefaut = SimpleServletRequestUtils.getStringParameter(request,
 				"isDefaut", "");
 		String id = SimpleServletRequestUtils.getStringParameter(request, "id",
 				"");
 		try {
 			UserInfo ui = null;
 			UserInfo ui_session = (UserInfo) request.getSession().getAttribute(
 					Constant.USER_WEBSEESION_KEY);
 			ui = this.userInfoService.findUserInfoById(ui_session.getId());
 			Long numAddress = this.addressService.getAllAddressNum(ui.getId());
 			if (numAddress > 3) {
 				fail_msg = "用户地址只能保存3个,请删除其他地址后再添加新的地址.";
 				returnModel.put("fail_msg", fail_msg);
 				return new ModelAndView("/msg/error_msg", returnModel);
 
 			}
 			UserAddress address = null;
 			if (id.isEmpty()) {
 				address = new UserAddress();
 			} else {
 				address = this.addressService.loadAddressById(id);
 			}
 
 			address.setName(userName);
 			address.setProvinceId(province);
 			address.setProvinceName(this.regionService
 					.getCityByNodeId(province).getNodeName());
 			address.setStreet(street);
 			address.setCityId(city);
 			address.setCityName(this.regionService.getCityByNodeId(city)
 					.getNodeName());
 			address.setZipCode(zipCode);
 			address.setMobie(mobile);
 			address.setPhone(phone);
 			address.setUserInfo(ui);
 			address.setIsDefaut(Integer.parseInt(isDefaut.equals("") ? "0"
 					: "1"));
 			address = this.addressService.saveorupdateUserAddress(address);
 			if (address.getIsDefaut() == 1) {
 				if (!this.addressService.updateIsDefault(address.getId())) {
 					fail_msg = "设置默认地址出错,请手动重新设置.";
 					returnModel.put("fail_msg", fail_msg);
 					return new ModelAndView("/msg/error_msg", returnModel);
 				}
 			}
 			response.sendRedirect(request.getContextPath()
 					+ "/servlet/user/userinfo.do?method=initUserAdress");
 			return null;
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail_msg = "保存用户地址信息出错，请稍后再尝试.";
 			returnModel.put("fail_msg", fail_msg);
 			return new ModelAndView("/msg/error_msg", returnModel);
 		}
 
 	}
 
 	// 动态设置默认送货地址
 	@RequestMapping(method = RequestMethod.POST, params = "method=setDefault")
 	public void setDefault(HttpServletRequest request,
 			HttpServletResponse response) throws XltHotelException {
 
 		try {
 			response.setHeader("Pragma", "No-cache");
 			response.setHeader("Cache-Control", "no-cache");
 			response.setDateHeader("Expires", 0);
 			response.setContentType("text/html;charset=utf-8");
 			String id = SimpleServletRequestUtils.getStringParameter(request,
 					"id", "");
 			PrintWriter out = response.getWriter();
 			UserAddress address = this.addressService.loadAddressById(id);
 			if (address == null) {
 				out.println(false);
 			} else {
 				address.setIsDefaut(1);
 				boolean flag = this.addressService.updateIsDefault(id);
 				if (flag) {
 					out.println(true);
 				} else {
 					out.println(false);
 				}
 			}
 
 			out.flush();
 			out.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>设置默认地址信息出现异常。", e);
 		}
 
 	}
 
 	// 动态删除送货地址
 	@RequestMapping(method = RequestMethod.POST, params = "method=delAddress")
 	public void delAddress(HttpServletRequest request,
 			HttpServletResponse response) throws XltHotelException {
 
 		try {
 			response.setHeader("Pragma", "No-cache");
 			response.setHeader("Cache-Control", "no-cache");
 			response.setDateHeader("Expires", 0);
 			response.setContentType("text/html;charset=utf-8");
 			String id = SimpleServletRequestUtils.getStringParameter(request,
 					"id", "");
 			PrintWriter out = response.getWriter();
 			UserAddress ad = this.addressService.loadAddressById(id);
 			if (ad != null) {
 				this.addressService.removeAddress(ad);
 				out.println(true);
 			} else {
 				out.println(false);
 			}
 			out.flush();
 			out.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>删除地址信息出现异常。", e);
 		}
 
 	}
 
 	// 编辑送货地址
 	@RequestMapping(method = RequestMethod.POST, params = "method=editAddress")
 	public void editAddress(HttpServletRequest request,
 			HttpServletResponse response) throws XltHotelException {
 
 		try {
 			response.setHeader("Pragma", "No-cache");
 			response.setHeader("Cache-Control", "no-cache");
 			response.setDateHeader("Expires", 0);
 			response.setContentType("application/x-json;charset=utf-8");
 			String id = SimpleServletRequestUtils.getStringParameter(request,
 					"id", "");
 
 			UserAddress ad = this.addressService.loadAddressById(id);
 			if (ad != null) {
 				JSONObject jso = new JSONObject();
 				jso.put("id", ad.getId());
 				jso.put("name", ad.getName());
 				jso.put("provinceId", ad.getProvinceId());
 				jso.put("provinceName", ad.getProvinceName());
 				jso.put("cityId", ad.getCityId());
 				jso.put("cityName", ad.getCityName());
 				jso.put("street", ad.getStreet());
 				jso.put("zipCode", ad.getZipCode());
 				jso.put("phone", ad.getPhone());
 				jso.put("mobie", ad.getMobie());
 				jso.put("isDefaut", ad.getIsDefaut());
 				response.getWriter().print(jso);
 			} else {
 				response.getWriter().print("");
 			}
 			PrintWriter out = response.getWriter();
 			out.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>编辑地址信息出现异常。", e);
 		}
 
 	}
 
 	// 检查送货地址
 	@RequestMapping(method = RequestMethod.POST, params = "method=checkAddress")
 	public void checkAddress(HttpServletRequest request,
 			HttpServletResponse response) throws XltHotelException {
 
 		try {
 			response.setHeader("Pragma", "No-cache");
 			response.setHeader("Cache-Control", "no-cache");
 			response.setDateHeader("Expires", 0);
 			response.setContentType("text/html;charset=utf-8");
 			PrintWriter out = response.getWriter();
 			String id = SimpleServletRequestUtils.getStringParameter(request,
 					"id", "");
 			Long numAddress = this.addressService.getAllAddressNum(id);
 			if (numAddress < 3) {
 				out.println(true);
 			} else {
 				out.println(false);
 			}
 			out.flush();
 			out.close();
 		} catch (Exception e) {
 			throw new XltHotelException("<li>检查地址信息出现异常。", e);
 		}
 	}
 
 	/********************************** 用户评价 *************************************************************/
 	// 查看评价
 	@RequestMapping(params = "method=viewComment")
 	public ModelAndView viewComment(HttpServletRequest request,
 			HttpServletResponse response) {
 		try {
 			UserInfo ui = null;
 			UserInfo ui_session = (UserInfo) request.getSession().getAttribute(
 					Constant.USER_WEBSEESION_KEY);
 			ui = this.userInfoService.findUserInfoById(ui_session.getId());
 			String id = (String) SimpleServletRequestUtils.getStringParameter(
 					request, "id","");
 			HotelComment hotelComment = this.hotelCommentService.findById(id);
 			
 			//获取酒店列表
 			List<Hotel> hotelList = getDefaultHotelList();
 			Map<String, Object> returnModel = new HashMap<String, Object>();
 			returnModel.put("hotelList", hotelList);
 			returnModel.put("hotelComment", hotelComment);
 			returnModel.put("ui", ui);
 			return new ModelAndView("/userinfo/viewComment", returnModel);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>系统出现异常,请稍后尝试。", e);
 		}
 
 	}
 	
 	//评价列表
 	@RequestMapping(params = "method=userCommentList")
 	public ModelAndView userCommentList(HttpServletRequest request,
 			HttpServletResponse response) {
 		String flag = SimpleServletRequestUtils.getStringParameter(request,
 				"flag", "");
 		String liveDate = SimpleServletRequestUtils.getStringParameter(request,
 				"liveDate", "");
 		String hotelId = SimpleServletRequestUtils.getStringParameter(request,
 				"searchHotel", "");
 		String number = SimpleServletRequestUtils.getStringParameter(request,
 				"number", "");
 		try {
 			UserInfo ui = null;
 			UserInfo ui_session = (UserInfo) request.getSession().getAttribute(
 					Constant.USER_WEBSEESION_KEY);
 			ui = this.userInfoService.findUserInfoById(ui_session.getId());
 			
 			Page page = getPageFromRequest(request);
 			PageOrder order = getOrderFromRequest(request);
 			Condition conditions = new SimpleConditionImpl();
 			conditions.putCondition("status", "=", "status", 1);
 			conditions.putCondition("userId", "=", "userId", ui.getId());
 			conditions.setEntityName("HotelComment");
 			if (StringUtils.isNotBlank(hotelId)) {
 				conditions.putCondition("hotelId", "=", "hotelId", hotelId);
 			}
 			if (StringUtils.isNotBlank(number)) {
 				conditions.putCondition("summary", ">", "number",
 						Integer.parseInt(number) * 6);
 			}
 			if (StringUtils.isNotBlank(liveDate)) {
 				conditions.putCondition("liveDate", "=", "liveDate",
 						DateUtils.parse("yyyy-MM-dd", liveDate));
 			}
 			List<HotelComment> comments = this.hotelCommentService
 					.getHotelCommentByConditions(page, conditions, order);
 			//获取酒店列表
 			List<Hotel> hotelList = getDefaultHotelList();
 			Map<String, Object> returnModel = new HashMap<String, Object>();
 			returnModel.put("flag", flag);
 			returnModel.put("hotelList", hotelList);
 			returnModel.put("comments", comments);
 			returnModel.put("page", page);
 			returnModel.put("order", order);
 			returnModel.put("ui", ui);
 			returnModel.put("number", number);
 			returnModel.put("selectedHotelId", hotelId);
 			returnModel.put("liveDate", liveDate);
 			return new ModelAndView("/userinfo/commentList", returnModel);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>系统出现异常,请稍后尝试。", e);
 		}
 
 	}
 
 	// 新增评价
 	@RequestMapping(params = "method=newUserComment")
 	public ModelAndView newUserComment(HttpServletRequest request,
 			HttpServletResponse response) {
 		try {
 			UserInfo ui = null;
 			UserInfo ui_session = (UserInfo) request.getSession().getAttribute(
 					Constant.USER_WEBSEESION_KEY);
 			ui = this.userInfoService.findUserInfoById(ui_session.getId());
 			//获取酒店列表
 			List<Hotel> hotelList = getDefaultHotelList();
 			Map<String, Object> returnModel = new HashMap<String, Object>();
 			returnModel.put("hotelList", hotelList);
 			returnModel.put("ui", ui);
 			return new ModelAndView("/userinfo/userComment", returnModel);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>系统出现异常,请稍后尝试。", e);
 		}
 
 	}
 
 	// 验证码
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
 
 	// 评价
 	@RequestMapping(method = RequestMethod.POST, params = "method=saveComment")
 	public ModelAndView saveComment(HttpServletRequest request,
 			HttpServletResponse response) {
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		String fail_msg = "";
 		String hoteId = SimpleServletRequestUtils.getStringParameter(request,
 				"searchHotel", "");
 		String liveDate = SimpleServletRequestUtils.getStringParameter(request,
 				"liveDate", "");
 		String rating = SimpleServletRequestUtils.getStringParameter(request,
 				"rating", "");
 		String comfort = SimpleServletRequestUtils.getStringParameter(request,
 				"comfort", "");
 		String price = SimpleServletRequestUtils.getStringParameter(request,
 				"price", "");
 		String place = SimpleServletRequestUtils.getStringParameter(request,
 				"place", "");
 		String health = SimpleServletRequestUtils.getStringParameter(request,
 				"health", "");
 		String service = SimpleServletRequestUtils.getStringParameter(request,
 				"service", "");
 		String sleep = SimpleServletRequestUtils.getStringParameter(request,
 				"sleep", "");
 		String isRecommend = SimpleServletRequestUtils.getStringParameter(
 				request, "isRecommend", "");
 		String rcomment = SimpleServletRequestUtils.getStringParameter(request,
 				"rcomment", "");
 		String rcontents = SimpleServletRequestUtils.getStringParameter(
 				request, "rcontents", "");
 		String checkCode = SimpleServletRequestUtils.getStringParameter(
 				request, "checkCode", "");
 		try {
 			UserInfo ui = null;
 			UserInfo ui_session = (UserInfo) request.getSession().getAttribute(
 					Constant.USER_WEBSEESION_KEY);
 			ui = this.userInfoService.findUserInfoById(ui_session.getId());
 			String checkcode_session = (String) request.getSession()
 					.getAttribute(Constant.USER_CHECKCODE_KEY);
 			if (!checkCode.equals(checkcode_session)) {
 				fail_msg = "输入验证码不正确,请重新填写评论";
 				returnModel.put("fail_msg", fail_msg);
 				returnModel.put("flag", "N");
 				request.getSession().removeAttribute(
 						Constant.USER_CHECKCODE_KEY);
 				return new ModelAndView("/userinfo/userComment", returnModel);
 			}
 			request.getSession().removeAttribute(Constant.USER_CHECKCODE_KEY);
 
 			HotelComment hotelComment = new HotelComment();
 			hotelComment.setRating(Integer.parseInt(rating));
 			hotelComment.setComfort(Integer.parseInt(comfort));
 			hotelComment.setPrice(Integer.parseInt(price));
 			hotelComment.setPlace(Integer.parseInt(place));
 			hotelComment.setHealth(Integer.parseInt(health));
 			hotelComment.setService(Integer.parseInt(service));
 			hotelComment.setSleep(Integer.parseInt(sleep));
 			hotelComment.setStatus(0);
 			hotelComment.setLiveDate(DateUtils.parse("yyyy-MM-dd", liveDate));
 			hotelComment.setIsRecommend(Integer.parseInt(isRecommend));
 			hotelComment.setRcomment(rcomment);
 			hotelComment.setRcontents(rcontents);
 			hotelComment.setCreateDate(new Date());
 			hotelComment.setSummary(Integer.parseInt(rating)
 					+ Integer.parseInt(comfort) + Integer.parseInt(price)
 					+ Integer.parseInt(place) + Integer.parseInt(health)
 					+ Integer.parseInt(service) + Integer.parseInt(sleep));
 			hotelComment.setHotel(this.hotelService.find(hoteId));
 			hotelComment.setUserInfo(ui);
 			hotelComment = this.hotelCommentService
 					.saveHotelComment(hotelComment);
 			response.sendRedirect(request.getContextPath()+ "/servlet/user/userinfo.do?method=userCommentList&flag=Y");
 			return null;
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail_msg = "保存酒店评价出错，请稍后再尝试.";
 			returnModel.put("fail_msg", fail_msg);
 			returnModel.put("flag", "N");
 			return new ModelAndView("/msg/error_msg", returnModel);
 		}
 
 	}
 
 	private Page getPageFromRequest(HttpServletRequest request) {
 		Page page = new SimplePageImpl();
 		page.setIndex(SimpleServletRequestUtils.getIntParameter(request,
 				"pageIndex", 0));
 		page.setCount(SimpleServletRequestUtils.getIntParameter(request,
 				"pageCount", 10));
 		return page;
 	}
 
 	private PageOrder getOrderFromRequest(HttpServletRequest request) {
 		PageOrder order = new SimpleOrderImpl();
 		order.setOrderColumn(SimpleServletRequestUtils.getStringParameter(
 				request, "orderColumn", "createDate"));
 		order.setSequence(SimpleServletRequestUtils.getStringParameter(request,
 				"orderSequence", "desc"));
 		return order;
 	}
 
 	/********************************** 用户投诉 ************************************************************/
 	// 投诉列表
 	@RequestMapping(params = "method=userComplaintList")
 	public ModelAndView userComplaintList(HttpServletRequest request,
 			HttpServletResponse response) {
 		String flag = SimpleServletRequestUtils.getStringParameter(request,
 				"flag", "");
 		String hotelId = SimpleServletRequestUtils.getStringParameter(request,
 				"searchHotel", "");
 		try {
 			UserInfo ui = null;
 			UserInfo ui_session = (UserInfo) request.getSession().getAttribute(
 					Constant.USER_WEBSEESION_KEY);
 			ui = this.userInfoService.findUserInfoById(ui_session.getId());
 			Page page = getPageFromRequest(request);
 			PageOrder order = getOrderFromRequest(request);
 			Condition conditions = new SimpleConditionImpl();
 			conditions.putCondition("status", "=", "status", 1);
 			conditions.putCondition("userId", "=", "userId", ui.getId());
 			conditions.setEntityName("Complaint");
 			if (StringUtils.isNotBlank(hotelId)) {
 				conditions.putCondition("hotelId", "=", "hotelId", hotelId);
 			}
 			List<Complaint> complaints = this.complaintService
 					.getComplaintByConditions(page, conditions, order);
 			Map<String, Object> returnModel = new HashMap<String, Object>();
 			returnModel.put("complaints", complaints);
 			returnModel.put("page", page);
 			returnModel.put("flag", flag);
 			returnModel.put("order", order);
 			returnModel.put("ui", ui);
 			returnModel.put("hotelId", hotelId);
 			return new ModelAndView("/userinfo/userComplaintsList", returnModel);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>系统出现异常,请稍后尝试。", e);
 		}
 
 	}
 
 	// 新增投诉
 	@RequestMapping(params = "method=newComplaint")
 	public ModelAndView newComplaint(HttpServletRequest request,
 			HttpServletResponse response) {
 		try {
 			UserInfo ui = null;
 			UserInfo ui_session = (UserInfo) request.getSession().getAttribute(
 					Constant.USER_WEBSEESION_KEY);
 			ui = this.userInfoService.findUserInfoById(ui_session.getId());
 			//获取酒店列表
 			List<Hotel> hotelList = getDefaultHotelList();
 			Map<String, Object> returnModel = new HashMap<String, Object>();
 			returnModel.put("hotelList", hotelList);
 			returnModel.put("ui", ui);
 			return new ModelAndView("/userinfo/userComplaint", returnModel);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>系统出现异常,请稍后尝试。", e);
 		}
 
 	}
 
 	// 保存投诉
 	@RequestMapping(method = RequestMethod.POST, params = "method=saveComplaint")
 	public ModelAndView saveComplaint(HttpServletRequest request,
 			HttpServletResponse response) {
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		String fail_msg = "";
 		String hoteId = SimpleServletRequestUtils.getStringParameter(request,
 				"searchHotel", "");
 
 		String title = SimpleServletRequestUtils.getStringParameter(request,
 				"title", "");
 		String content = SimpleServletRequestUtils.getStringParameter(request,
 				"content", "");
 		String checkCode = SimpleServletRequestUtils.getStringParameter(
 				request, "checkCode", "");
 		try {
 			UserInfo ui = null;
 			UserInfo ui_session = (UserInfo) request.getSession().getAttribute(
 					Constant.USER_WEBSEESION_KEY);
 			ui = this.userInfoService.findUserInfoById(ui_session.getId());
 			String checkcode_session = (String) request.getSession()
 					.getAttribute(Constant.USER_CHECKCODE_KEY);
 			if (!checkCode.equals(checkcode_session)) {
 				fail_msg = "输入验证码不正确,请重新填写投诉";
 				returnModel.put("fail_msg", fail_msg);
 				returnModel.put("flag", "N");
 				request.getSession().removeAttribute(
 						Constant.USER_CHECKCODE_KEY);
 				return new ModelAndView("/userinfo/userComplaint", returnModel);
 			}
 			request.getSession().removeAttribute(Constant.USER_CHECKCODE_KEY);
 			Complaint complaint = new Complaint();
 			complaint.setCreateDate(new Date());
 			complaint.setUserInfo(ui);
 			complaint.setTitle(title);
 			complaint.setContent(content);
 			complaint.setViewNum(0);
 			complaint.setReplyNum(0);
 			complaint.setStatus(0);
 			complaint.setHotel(this.hotelService.find(hoteId));
 			complaint = this.complaintService.saveComplaint(complaint);
 			response.sendRedirect(request.getContextPath()
 					+ "/servlet/user/userinfo.do?method=userComplaintList&flag=Y");
 			return null;
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail_msg = "保存酒店投诉出错，请稍后再尝试.";
 			returnModel.put("fail_msg", fail_msg);
 			returnModel.put("flag", "N");
 			return new ModelAndView("/msg/error_msg", returnModel);
 		}
 
 	}
 	
 	// 查看投诉
 	@RequestMapping(params = "method=viewComplaint")
 	public ModelAndView viewComplaint(HttpServletRequest request,
 			HttpServletResponse response) {
 		try {
 			UserInfo ui = null;
 			UserInfo ui_session = (UserInfo) request.getSession().getAttribute(
 					Constant.USER_WEBSEESION_KEY);
 			ui = this.userInfoService.findUserInfoById(ui_session.getId());
 			String id = (String) SimpleServletRequestUtils.getStringParameter(
 					request, "id","");
 			Complaint complaint = this.complaintService.findById(id);
 			
 			//获取酒店列表
 			List<Hotel> hotelList = getDefaultHotelList();
 			Map<String, Object> returnModel = new HashMap<String, Object>();
 			returnModel.put("hotelList", hotelList);
 			returnModel.put("complaint", complaint);
 			returnModel.put("ui", ui);
 			return new ModelAndView("/userinfo/viewComplaint", returnModel);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>系统出现异常,请稍后尝试。", e);
 		}
 
 	}
 
 
 	/*********************************** 会员公告 *******************************************************************/
 
 	@RequestMapping(method = RequestMethod.GET, params = "method=userNoticeListInit")
 	public ModelAndView userNoticeListInit(HttpServletRequest request,
 			HttpServletResponse response) {
 		try {
 			UserInfo ui = null;
 			UserInfo ui_session = (UserInfo) request.getSession().getAttribute(
 					Constant.USER_WEBSEESION_KEY);
 			ui = this.userInfoService.findUserInfoById(ui_session.getId());
 			Page page = getPageFromRequest(request);
 			Condition conditions = new SimpleConditionImpl();
 			conditions.putCondition("userId", "=", "userId", ui.getId());
 			
 			conditions.setEntityName("UserNotice");
 
 			List<UserNotice> userNoticeList = this.userNoticeWebService
 					.findUserNoticeByConditions(page, conditions, null);
 			Map<String, Object> returnModel = new HashMap<String, Object>();
 			returnModel.put("page", page);
 			returnModel.put("userNoticeList", userNoticeList);
 			returnModel.put("ui", ui);
 
 			return new ModelAndView("/userinfo/userNoticeList", returnModel);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>系统出现异常,请稍后尝试。", e);
 		}
 
 	}
 	
 	@RequestMapping(method = RequestMethod.POST, params = "method=userNoticeList")
 	public ModelAndView userNoticeList(HttpServletRequest request,
 			HttpServletResponse response) {
 		try {
 			UserInfo ui = null;
 			UserInfo ui_session = (UserInfo) request.getSession().getAttribute(
 					Constant.USER_WEBSEESION_KEY);
 			ui = this.userInfoService.findUserInfoById(ui_session.getId());
 			String title = SimpleServletRequestUtils.getStringParameter(request,
 					"title", "");
 			String status = SimpleServletRequestUtils.getStringParameter(request,
 					"status", "");
 			String delFlag = SimpleServletRequestUtils.getStringParameter(request,
 					"delFlag", "");
 			Page page = getPageFromRequest(request);
 			
 
 			Condition conditions = new SimpleConditionImpl();
 			conditions.putCondition("userId", "=", "userId", ui.getId());
 			if (StringUtils.isNotBlank(title)) {
 				conditions.putCondition("title", "like", "title", title);
 			}
 			if (StringUtils.isNotBlank(status)) {
 				conditions.putCondition("status", "=", "title", Integer.valueOf(status));
 			}
 			conditions.setEntityName("UserNotice");
 
 			List<UserNotice> userNoticeList = this.userNoticeWebService
 					.findUserNoticeByConditions(page, conditions, null);
 			Map<String, Object> returnModel = new HashMap<String, Object>();
 			returnModel.put("title", title);
 			returnModel.put("status", status);
 			returnModel.put("delFlag", delFlag);
 			returnModel.put("page", page);
 			returnModel.put("userNoticeList", userNoticeList);
 			returnModel.put("ui", ui);
 
 			return new ModelAndView("/userinfo/userNoticeList", returnModel);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>系统出现异常,请稍后尝试。", e);
 		}
 
 	}
 	
 	@RequestMapping(method = RequestMethod.POST, params = "method=delUserNotice")
 	public void delUserNotice(HttpServletRequest request, HttpServletResponse response)
 			throws XltHotelException {
 
 		try {
 			response.setHeader("Pragma", "No-cache");
 			response.setHeader("Cache-Control", "no-cache");
 			response.setDateHeader("Expires", 0);
 			response.setContentType("text/html;charset=utf-8");
 
 			String id = (String) SimpleServletRequestUtils.getStringParameter(
 					request, "id","");
 
 			UserNotice userNotice = this.userNoticeWebService.load(id);
 			if (userNotice != null) {
 				
 					this.userNoticeWebService.removeUserNotice(userNotice);
 					response.getWriter().print("Y");
 				}else {
 				response.getWriter().print("N");
 			}
 
 			PrintWriter out = response.getWriter();
 			out.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>删除用户组信息出现异常。", e);
 		}
 
 	}
 	
 	// 公告查看
 	@RequestMapping(params = "method=initUserNotice")
 	public ModelAndView initUserNotice(HttpServletRequest request,
 			HttpServletResponse response) {
 		try {
 			UserInfo ui = null;
 			UserInfo ui_session = (UserInfo) request.getSession().getAttribute(
 					Constant.USER_WEBSEESION_KEY);
 			ui = this.userInfoService.findUserInfoById(ui_session.getId());
 			String id = (String) SimpleServletRequestUtils.getStringParameter(
 					request, "id","");
 			UserNotice userNotice = this.userNoticeWebService.load(id);
 			if (userNotice != null) {
 				if(userNotice.getStatus()==0){
 					userNotice.setStatus(1);
 					this.userNoticeWebService.save(userNotice);
 				}
 			}
 			Map<String, Object> returnModel = new HashMap<String, Object>();
 			returnModel.put("userNotice", userNotice);
 			returnModel.put("ui", ui);
 			return new ModelAndView("/userinfo/userNoticeView", returnModel);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>系统出现异常,请稍后尝试。", e);
 		}
 
 	}
 	
 	
 	
 	/*********************************** 订单 *******************************************************************/
 
 	@RequestMapping(method = RequestMethod.GET, params = "method=userOrderInitList")
 	public ModelAndView userOrderInitList(HttpServletRequest request,
 			HttpServletResponse response) {
 		try {
 			UserInfo ui = null;
 			UserInfo ui_session = (UserInfo) request.getSession().getAttribute(
 					Constant.USER_WEBSEESION_KEY);
 			ui = this.userInfoService.findUserInfoById(ui_session.getId());
 			Page page = getPageFromRequest(request);
 			Condition conditions = new SimpleConditionImpl();
 			conditions.putCondition("user_Id", "=", "userId", ui.getId());
 			
 			conditions.setEntityName("Order");
 
 			List<Order> orderList = this.orderService.getOrderList(page, conditions, null);
 			Map<String, Object> returnModel = new HashMap<String, Object>();
 			returnModel.put("page", page);
 			returnModel.put("orderList", orderList);
 			returnModel.put("ui", ui);
 
 			return new ModelAndView("/userinfo/userOrderList", returnModel);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>系统出现异常,请稍后尝试。", e);
 		}
 
 	}
 	
 	@RequestMapping(method = RequestMethod.POST, params = "method=userOrderList")
 	public ModelAndView userOrderList(HttpServletRequest request,
 			HttpServletResponse response) {
 		try {
 			UserInfo ui = null;
 			UserInfo ui_session = (UserInfo) request.getSession().getAttribute(
 					Constant.USER_WEBSEESION_KEY);
 			ui = this.userInfoService.findUserInfoById(ui_session.getId());
 			String orderId = SimpleServletRequestUtils.getStringParameter(request,
 					"orderId", "");
 			String orderDateFrom = SimpleServletRequestUtils.getStringParameter(request,
 					"orderDateFrom", "");
 			String orderDateTo = SimpleServletRequestUtils.getStringParameter(request,
 					"orderDateTo", "");
 			String cancelFlag = SimpleServletRequestUtils.getStringParameter(request,
 					"cancelFlag", "");
 			Page page = getPageFromRequest(request);
 			
 
 			Condition conditions = new SimpleConditionImpl();
 			conditions.putCondition("user_Id", "=", "userId", ui.getId());
 			if (StringUtils.isNotBlank(orderId)) {
 				conditions.putCondition("orderId", "=", "orderId", orderId);
 			}
 			if (StringUtils.isNotBlank(orderDateFrom)) {
 				conditions.putCondition("date(orderDateFrom)", "=", "orderDateFrom",
 						DateUtils.parse("yyyy-MM-dd", orderDateFrom));
 			}
 			if (StringUtils.isNotBlank(orderDateTo)) {
 				System.out.println(DateUtils.parse("yyyy-MM-dd", orderDateTo));
 				conditions.putCondition("date(orderDateTo)", "=", "orderDateTo", DateUtils.parse("yyyy-MM-dd", orderDateTo));
 			}
 			
 			conditions.setEntityName("Order");
 			
 			PageOrder userPageOrder = new SimpleOrderImpl();
 			userPageOrder.setOrderColumn("createDate");
 			userPageOrder.setSequence(PageOrder.Sequence.ASC.toString());
 
 			List<Order> orderList = this.orderService.getOrderList(page, conditions, userPageOrder);
 			Map<String, Object> returnModel = new HashMap<String, Object>();
 			returnModel.put("orderId", orderId);
 			returnModel.put("orderDateFrom", orderDateFrom);
 			returnModel.put("orderDateTo", orderDateTo);
 			returnModel.put("page", page);
 			returnModel.put("cancelFlag", cancelFlag);
 			returnModel.put("orderList", orderList);
 			returnModel.put("ui", ui);
 
 			return new ModelAndView("/userinfo/userOrderList", returnModel);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>系统出现异常,请稍后尝试。", e);
 		}
 
 	}
 	
 	@RequestMapping(method = RequestMethod.POST, params = "method=cancelOrder")
 	public void cancelOrder(HttpServletRequest request, HttpServletResponse response)
 			throws XltHotelException {
 
 		try {
 			response.setHeader("Pragma", "No-cache");
 			response.setHeader("Cache-Control", "no-cache");
 			response.setDateHeader("Expires", 0);
 			response.setContentType("text/html;charset=utf-8");
 
 			String id = (String) SimpleServletRequestUtils.getStringParameter(
 					request, "id","");
 
 			Order order = this.orderService.findOrderById(id);
 			if (order != null) {
 				order.setStatus(Order.Status.Cancel.getValue());
 					this.orderService.save(order);
 					response.getWriter().print("Y");
 				}else {
 				response.getWriter().print("N");
 			}
 			PrintWriter out = response.getWriter();
 			out.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>取消订单信息出现异常。", e);
 		}
 
 	}
 
 	/*********************************** 用户积分 *******************************************************************/
 	@RequestMapping(params = "method=userPointView")
 	public ModelAndView userPointView(HttpServletRequest request,
 			HttpServletResponse response) {
 		try {
 
 			Map<String, Object> returnModel = new HashMap<String, Object>();
 			UserInfo ui = null;
 			UserInfo ui_session = (UserInfo) request.getSession().getAttribute(
 					Constant.USER_WEBSEESION_KEY);
 			ui = this.userInfoService.findUserInfoById(ui_session.getId());
 			returnModel.put("ui", ui);
 			return new ModelAndView("/userinfo/userPointView", returnModel);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>系统出现异常,请稍后尝试。", e);
 		}
 
 	}
 	
 	/*********************************** 检查只有预订了客户才能评论 *******************************************************************/
 	@RequestMapping(method = RequestMethod.POST, params = "method=checkIsOrder")
 	public void checkIsOrder(HttpServletRequest request,
 			HttpServletResponse response) throws XltHotelException {
 
 		try {
 			response.setHeader("Pragma", "No-cache");
 			response.setHeader("Cache-Control", "no-cache");
 			response.setDateHeader("Expires", 0);
 			response.setContentType("text/html;charset=utf-8");
 			String searchHotel = (String) SimpleServletRequestUtils.getStringParameter(
 					request, "searchHotel","");
 			PrintWriter out = response.getWriter();
 			UserInfo ui = null;
 			UserInfo ui_session = (UserInfo) request.getSession().getAttribute(
 					Constant.USER_WEBSEESION_KEY);
 			ui = this.userInfoService.findUserInfoById(ui_session.getId());
 			Page page = getPageFromRequest(request);
 			Condition conditions = new SimpleConditionImpl();
 			conditions.putCondition("user_Id", "=", "userId", ui.getId());
 			conditions.putCondition("hotel_id", "=", "hotelId", searchHotel);
 			conditions.putCondition("status", "=", "status", 4);
 			conditions.setEntityName("Order");
 
 			List<Order> orderList = this.orderService.getOrderList(page, conditions, null);
 			if (orderList!=null&&orderList.size()>0) {
 				out.println(true);
 			} else {
 				out.println(false);
 			}
 			out.flush();
 			out.close();
 		} catch (Exception e) {
 			throw new XltHotelException("<li>检查地址信息出现异常。", e);
 		}
 	}
 	
 	
 	/*********************************** 获取酒店列表 *******************************************************************/
 	private List<Hotel> getDefaultHotelList() {
 		PageOrder hotelListOrder = new SimpleOrderImpl();
 		hotelListOrder.setOrderColumn("name");
 		hotelListOrder.setSequence(PageOrder.Sequence.ASC.toString());
 		List<Hotel> hotelList = hotelService.getHotelList(null, null, hotelListOrder);
 		return hotelList;
 	}
 
 }
