 package com.asu.edu;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import javax.servlet.ServletRequest;
 import javax.validation.Valid;
 
 import net.tanesha.recaptcha.ReCaptcha;
 import net.tanesha.recaptcha.ReCaptchaResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.asu.edu.base.dao.intrf.RegisterationDAOImplInterface;
 import com.asu.edu.base.vo.DepartmentVO;
 import com.asu.edu.base.vo.RegisterationVO;
 import com.asu.edu.base.vo.RoleVO;
 import com.asu.edu.cache.MasterCache;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class RegisterationController {
 
 	@Autowired
 	private ReCaptcha reCaptcha = null;
 
 	@Autowired
 	private RegisterationDAOImplInterface registerationDAO = null;
 
 	private static ArrayList<DepartmentVO> deptArray;
 
 	private static ArrayList<RoleVO> rolesArray;
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(RegisterationController.class);
 
 	@RequestMapping(value = "/register", method = RequestMethod.GET)
 	public String home1(Map<String, Object> model) {
 		logger.info("Welcome home! the client locale is ");
 
 		initArrays();
 		
 		model.put("registerationVO", new RegisterationVO());
 		model.put("deptList", deptArray);
 		model.put("roleList", rolesArray);
 
 		return "register";
 	}
 
 	@RequestMapping(value = "/register", method = RequestMethod.POST)
 	public String submitForm(@Valid RegisterationVO registerationVO,
 			BindingResult result,
 			@RequestParam("recaptcha_challenge_field") String challangeField,
 			@RequestParam("recaptcha_response_field") String responseField,
 			ServletRequest servletRequest, Map<String, Object> model) {
 
 		if (!result.hasErrors()) {
 			String remoteAddress = servletRequest.getRemoteAddr();
 			ReCaptchaResponse reCaptchaResponse = this.reCaptcha.checkAnswer(
 					remoteAddress, challangeField, responseField);
 			if (!reCaptchaResponse.isValid()) {
 				FieldError fieldError = new FieldError("registerationVO",
 						"captcha", "Captcha worong. Please try again.");
 				result.addError(fieldError);
 			} else {
 				
 				logger.info("department is : " + registerationVO.getDepartments());
 				logger.info("role is : " + registerationVO.getRoleId());
 				int role_id = registerationVO.getRoleId();
 				if(role_id != 5)
 				{
 					ArrayList<Integer> depts = registerationVO.getDepartments();
 					int no_of_depts = depts.size();
 					if(no_of_depts > 1)
 					{
 						return "register";
 					}
 					if(role_id == 2 && no_of_depts != 0)
 					{
 						return "register";
 					}
 				}
 				if (registerationDAO.registerUser(registerationVO)) {
 					logger.info("Registeration successful");
 					return "redirect:/login";
 				}
 				logger.info("Registeration failed");
 			}
 		}
 
 		logger.info("form has erros !!!");
 		model.put("deptList", deptArray);
 		model.put("roleList", rolesArray);
 
 		return "register";
 	}
 
 	private void initArrays() {
 		try
 		{
 			if (deptArray == null) {
 				deptArray = new ArrayList<DepartmentVO>();
 				Map deptMap =  MasterCache.getDepartmentMap();
 				System.out.println("dept MAP " + deptMap);
 				Iterator it = deptMap.entrySet().iterator();
 				Map.Entry pairs;
 				while (it.hasNext()) {
 					pairs = (Map.Entry)it.next();
 					deptArray.add((DepartmentVO)pairs.getValue());
 				}
 			}
 
 			if (rolesArray == null) {
 				rolesArray = new ArrayList<RoleVO>();
 				Map rolesMap =  MasterCache.getRoleMap();
 				Iterator it = rolesMap.entrySet().iterator();
 				Map.Entry pairs;
 				while (it.hasNext()) {
 					pairs = (Map.Entry)it.next();
 					RoleVO roleVO = (RoleVO)pairs.getValue();
 					if(roleVO.getId() == 1)
 						continue;
 					rolesArray.add((RoleVO)pairs.getValue());
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			e.getStackTrace();
 		}
 
 	}
 	
 }
