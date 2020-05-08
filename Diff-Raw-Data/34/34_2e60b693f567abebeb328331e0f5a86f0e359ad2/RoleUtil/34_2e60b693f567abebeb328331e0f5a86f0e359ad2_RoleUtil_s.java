 package com.gemantic.killer.util;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.gemantic.common.util.zip.RunLengthEncoding;
 
 public class RoleUtil {
 	private static final Log log = LogFactory.getLog(RoleUtil.class);
 	public static final String Role_Water = "W";
 	public static final String Role_Killer = "K";
 
 	static public Map<String, String> assingRole(
 			Map<String, Integer> role_count, List<String> ls) {
 		log.info("role count " + role_count + " users " + ls);
 		Map<String, String> results = new HashMap();
 		Collections.shuffle(ls);
 		log.info(ls);
 		int index = 0;
 		for (String role : role_count.keySet()) {
 			int count = role_count.get(role);
 			for (int j = index; j < count + index; j++) {
 				results.put(ls.get(j), role);
 			}
 			index = index + count;
 		}
 
 		log.info("result is " + results);
 
 		return results;
 	}
 
 	public static void main(String[] args) {
 		Map<String, Integer> role_count = new HashMap();
 		role_count.put("killer", 1);
 		role_count.put("police", 1);
 		role_count.put("water", 3);
 
 		RoleUtil.assingRole(role_count,
 				Arrays.asList(new String[] { "1", "2", "3", "4", "5" }));
 
 		String r = RoleUtil.setRole(RoleUtil.Role_Water, "3K4W1K");
 		log.info(r);
 		
		Integer maxCount=RoleUtil.getMaxContinueRole(RoleUtil.Role_Water,r);
 		log.info(maxCount);
 
 	}
 	
 
 
 	public static Integer getMaxContinueRole(String role, String content) {
 		///List<String> roles=RunLengthEncodingUtil.decode2List(content,"[0-9]+|[WK]");
 		
 		int max=0;
 		
 		for(int i=0;i<content.length();i++){
 			String str=String.valueOf(content.charAt(i));
 			if(str.equals(role)){
 				int count=Integer.valueOf(content.charAt(i-1)+"");
 				if(count>max){
 					max=count;
 				}
 				
 			}
 			
		}
 		return max;
 	}
 
 	public static String setRole(String role, String content) {
 		if(StringUtils.isBlank(content)){
 			return "1"+role;
 		}
 		String roleStr = RunLengthEncodingUtil.decode(content,"[0-9]+|[WK]");
 
 		roleStr = roleStr + role;
 
 		return RunLengthEncoding.encode(roleStr);
 
 	}
 
 	public static String decodeRole(String role) {
 		// TODO Auto-generated method stub
 		return  RunLengthEncodingUtil.decode(role,"[0-9]+|[WK]");
 	}
 
 }
