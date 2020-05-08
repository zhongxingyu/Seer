 package com.omartech.tdg.action.seller;
 
 import java.util.List;
 
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.omartech.tdg.mapper.NoticeMapper;
 import com.omartech.tdg.model.ClaimItem;
 import com.omartech.tdg.model.Notice;
 import com.omartech.tdg.model.PasswordKey;
 import com.omartech.tdg.model.Seller;
 import com.omartech.tdg.service.ClaimService;
 import com.omartech.tdg.service.EmailService;
 import com.omartech.tdg.service.PasswordKeyService;
 import com.omartech.tdg.service.seller.SellerAuthService;
 import com.omartech.tdg.utils.ClaimRelation;
 import com.omartech.tdg.utils.UserType;
 
 @Controller
 public class SellerAuthAction {
 	Logger logger = Logger.getLogger(SellerAuthAction.class);
 	@Autowired
 	private SellerAuthService sellerAuthService;
 	@Autowired
 	private NoticeMapper noticeMapper;
 	
 	@Autowired
 	private EmailService emailService;
 	
 	@Autowired
 	private PasswordKeyService passwordKeyService;
 	
 	@Autowired
 	private ClaimService claimService;
 	
 	@ResponseBody
 	@RequestMapping(value="isSellerEmailExist")
 	public boolean isSellerEmailExist(@RequestParam String email){
 		Seller seller = sellerAuthService.getSellerByEmail(email);
 		if(seller == null){
 			return false;
 		}else{
 			return true;
 		}
 	}
 
 	@RequestMapping(value="/loginasseller")
 	public String loginAsSeller(HttpSession session){
 		Seller seller = (Seller) session.getAttribute("seller");
 		if(seller!=null){
 			return "redirect:/seller/welcome";
 		}
 		return "seller/auth/login";
 	}
 	
 	@RequestMapping(value="/seller/logout")
 	public String logout(HttpSession session){
 		session.invalidate();
 		return "redirect:/sellerindex";
 	}
 	
 	@RequestMapping(value="/sellerlogin", method=RequestMethod.POST)
 	public String sellerLogin(@RequestParam(value="email") String email,
 			@RequestParam(value="password") String password, HttpSession session){
 		logger.info("seller login:"+email+" - "+password);
 		Seller seller = sellerAuthService.getSellerByEmailAndPassword(email, password);
 		if(seller!=null){
 			session.setAttribute("seller", seller);
 			return "redirect:/seller/welcome";
 		}else{
 			return "redirect:/loginasseller";
 		}
 	}
 	
 	@RequestMapping(value="/seller/welcome")
 	public ModelAndView welcome(HttpSession session){
 		Seller seller = (Seller) session.getAttribute("seller");
 		Notice notice = noticeMapper.getNoticeByUserType(UserType.SELLER);
 		int sellerId = seller.getId();
 		List<ClaimItem> claimItems = claimService.getClaimItemsBySellerIdAndStatus(sellerId, ClaimRelation.ongoing);
 		return new ModelAndView("seller/auth/welcome").addObject("notice", notice).addObject("claimItems", claimItems);
 	}
 	
 	
 	@RequestMapping(value="/sellerforgetpwd")
 	public String sellerForgetPwd(){
 		return "seller/auth/forget";
 	}
 	
 	@RequestMapping("/forgetasseller")
 	public String forgetAsSeller(@RequestParam String email){
 		PasswordKey key = passwordKeyService.createKey(UserType.SELLER, email);
 		emailService.sendEmailWhenSellerForgetPassword(email, key);
 		return "redirect:/verifysellerpasswordkey?email="+email;
 	}
 	@RequestMapping("/verifysellerpasswordkey")
 	public ModelAndView verifySellerPasswordKey(@RequestParam String email, @RequestParam(value="flag", required=false, defaultValue ="true") boolean flag){
 		return new ModelAndView("/seller/auth/verifykey").addObject("email", email).addObject("flag", flag);
 	}
 	@RequestMapping(value="/confirmsellerpasswordkey", method=RequestMethod.POST)
 	public String confirmSellerPasswordkey(@RequestParam String email, @RequestParam String key){
 		PasswordKey pk = passwordKeyService.getPasswordKey(UserType.SELLER, email);
 		if(pk.getSecret().equals(key)){
 			return "redirect:/showchangesellerpassword?email=" + email;
 		}else{
 			return "redirect:/verifysellerpasswordkey?email="+email+"&flag=false";
 		}
 	}
 	@RequestMapping("/showchangesellerpassword")
 	public ModelAndView showChangePassword(@RequestParam String email){
 		return new ModelAndView("/seller/auth/change-password").addObject("email", email);
 	}
 	@RequestMapping(value = "/changesellerpassword", method = RequestMethod.POST)
 	public ModelAndView changePassword(@RequestParam String email, @RequestParam String password){
 		Seller seller = sellerAuthService.getSellerByEmail(email);
 		String message ="";
 		boolean flag = true;
 		if(seller == null){
 			message  = "this account is not exist.";
 			flag = false;
 		}else{
 			seller.setPassword(password);
 			sellerAuthService.updateSeller(seller);
 		}
 		return new ModelAndView("/seller/auth/change-password-result").addObject("message", message).addObject("flag", flag);
 	}
 	
 	@RequestMapping(value="/registerasseller")
 	public String registerAsSeller(){
 		return "seller/auth/register";
 	}
 	
 	@RequestMapping(value="/sellerregist", method=RequestMethod.POST)
 	public String sellerRegister(
 		@RequestParam(value="email", required=true) String email,
 		@RequestParam(value="password", required=true) String password,
 		@RequestParam String businessName,
 		@RequestParam String firstName,
 		@RequestParam String lastName,
 		@RequestParam String businessAddress,
 		@RequestParam String city,
 		@RequestParam String state,
 		@RequestParam String country,
 		@RequestParam String primaryPhoneNumber,
 		@RequestParam String productLines,
 		@RequestParam String secondPhoneNumber,
 		@RequestParam String companyWebsiteAddress,
 		HttpSession session){
 		Seller checkSeller = sellerAuthService.getSellerByEmail(email);
 		if(checkSeller == null){
 			Seller seller = new Seller();
 			seller.setEmail(email);
 			seller.setPassword(password);
 			seller.setBusinessName(businessName);
 			seller.setFirstName(firstName);
 			seller.setLastName(lastName);
 			seller.setBusinessAddress(businessAddress);
 			seller.setCity(city);
 			seller.setState(state);
 			seller.setCountry(country);
 			seller.setPrimaryPhoneNumber(primaryPhoneNumber);
 			seller.setProductLines(productLines);
 			seller.setSecondPhoneNumber(secondPhoneNumber);
 			seller.setCompanyWebsiteAddress(companyWebsiteAddress);
 			sellerAuthService.insertSeller(seller);
 			session.setAttribute("seller", seller);
 			emailService.sendEmailWhenSellerRegisterSuccess(seller);
 			return "redirect:/seller/welcome";
 		}else{
 			return "redirect:/sellerindex";
 		}
 	}
 	
 	@RequestMapping("/seller/auth/show")
 	public ModelAndView showProfile(HttpSession session){
 		Seller se = (Seller)session.getAttribute("seller");
 		String email = se.getEmail();
 		Seller seller = sellerAuthService.getSellerByEmail(email);
 		return new ModelAndView("/seller/auth/show").addObject("seller", seller);
 	}
 	
 	@RequestMapping("/seller/auth/edit")
 	public ModelAndView editProfile(HttpSession session){
 		Seller se = (Seller)session.getAttribute("seller");
 		String email = se.getEmail();
 		Seller seller = sellerAuthService.getSellerByEmail(email);
 		return new ModelAndView("/seller/auth/modify").addObject("seller", seller);
 	}
 	@RequestMapping("/seller/auth/update")
 	public String editProfile(@RequestParam int sellerId, @RequestParam String password,
 			@RequestParam String primaryPhoneNumber,
 			@RequestParam String secondPhoneNumber,
 			HttpSession session){
 		Seller se = sellerAuthService.getSellerById(sellerId);
 		if(password !=null && password.trim().length()!=0){
 			se.setPassword(password);
 		}
		se.setPrimaryPhoneNumber(primaryPhoneNumber);
 		se.setSecondPhoneNumber(secondPhoneNumber);
 		sellerAuthService.updateSeller(se);
 		return "redirect:/seller/auth/show";
 	}
 
 	public SellerAuthService getSellerAuthService() {
 		return sellerAuthService;
 	}
 
 	public void setSellerAuthService(SellerAuthService sellerAuthService) {
 		this.sellerAuthService = sellerAuthService;
 	}
 
 	public NoticeMapper getNoticeMapper() {
 		return noticeMapper;
 	}
 
 	public void setNoticeMapper(NoticeMapper noticeMapper) {
 		this.noticeMapper = noticeMapper;
 	}
 
 	public EmailService getEmailService() {
 		return emailService;
 	}
 
 	public void setEmailService(EmailService emailService) {
 		this.emailService = emailService;
 	}
 
 	public PasswordKeyService getPasswordKeyService() {
 		return passwordKeyService;
 	}
 
 	public void setPasswordKeyService(PasswordKeyService passwordKeyService) {
 		this.passwordKeyService = passwordKeyService;
 	}
 	
 }
