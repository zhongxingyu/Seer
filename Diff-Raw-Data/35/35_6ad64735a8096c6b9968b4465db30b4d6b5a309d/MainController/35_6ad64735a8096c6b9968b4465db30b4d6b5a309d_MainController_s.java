 package com.zvidia.backend.controller;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.zvidia.backend.repository.UserRepository;
 import com.zvidia.common.entity.FunctionInfo;
 import com.zvidia.common.entity.RoleInfo;
 import com.zvidia.common.entity.UserInfo;
 import com.zvidia.common.security.SecurityUtils;
 
 /**
  * Created with IntelliJ IDEA. User: jiangzm Date: 13-5-13 Time: 下午8:49 To
  * change this template use File | Settings | File Templates.
  */
 @Controller
 @RequestMapping("/main")
 public class MainController {
 	@Autowired
 	UserRepository userRepository;
 
 	@RequestMapping("")
 	public ModelAndView index() {
 		ModelAndView mv = new ModelAndView("main");
 		String checkUsername = SecurityUtils.checkUsername();
 		UserInfo userInfo = userRepository.findByUsername(checkUsername);
		mv.addObject("department", userInfo.getDepartment());
		mv.addObject("nickName", userInfo.getNickName());
		mv.addObject("userId", userInfo.getId());
		Collection<RoleInfo> authorities = userInfo.getAuthorities();
		Map<String, List<FunctionInfo>> menus = new HashMap<String, List<FunctionInfo>>();
		for (RoleInfo role : authorities) {
			Collection<FunctionInfo> funcs = role.getFuncs();
			for (FunctionInfo func : funcs) {
				String category = func.getCategory();
				List<FunctionInfo> list = menus.get(category);
				if (list == null) {
					list = new ArrayList<FunctionInfo>();
 				}
				list.add(func);
				menus.put(category, list);
 			}
 		}
		mv.addObject("menus", menus);
 		return mv;
 	}
 }
