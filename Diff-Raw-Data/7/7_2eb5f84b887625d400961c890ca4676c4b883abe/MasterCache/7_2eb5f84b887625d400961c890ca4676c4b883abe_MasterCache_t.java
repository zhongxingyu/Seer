 package com.asu.edu.cache;
 
import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.asu.edu.base.dao.impl.CommonDAOImpl;
 import com.asu.edu.base.dao.intrf.CommonDAOImplInterface;
 import com.asu.edu.base.vo.DepartmentVO;
 import com.asu.edu.base.vo.RoleVO;
import com.asu.edu.constants.CommonConstants;
 
 public class MasterCache {
 
 	private CommonDAOImplInterface commonDAO;
 	private static LinkedHashMap<Integer, DepartmentVO> department;
 	private static LinkedHashMap<Integer, RoleVO> role;
	private static HashMap<String,String> errorCode = new HashMap<String, String>();
 
 	static {
 		try {
 			reloadMasterCache();
 		} catch (Exception e) {
 
 		}
 	}
 
 	public static Map<Integer, DepartmentVO> getDepartmentMap() {
 		if (department == null) {
 			department = new LinkedHashMap<Integer, DepartmentVO>();
 			CommonDAOImpl dao = new CommonDAOImpl();
 			department = dao.getDepartments();
 		}
 		return department;
 	}
 
 	public static Map<Integer, RoleVO> getRoleMap() {
 		if (role == null) {
 			role = new LinkedHashMap<Integer, RoleVO>();
 			CommonDAOImpl dao = new CommonDAOImpl();
 			role = dao.getRoles();
 		}
 		return role;
 	}
 
 	public static void reloadMasterCache() {
 		getDepartmentMap();
 		getRoleMap();
		errorCode.put("C300", CommonConstants.C300);
 	}
 
 	public static void classStartup() {
 
 	}
 
 }
