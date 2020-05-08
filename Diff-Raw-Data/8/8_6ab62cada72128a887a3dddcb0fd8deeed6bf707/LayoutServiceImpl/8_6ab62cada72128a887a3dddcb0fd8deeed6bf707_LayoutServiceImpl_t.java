 package com.spring.mti.service;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.spring.mti.model.Answer;
 import com.spring.mti.model.Department;
 import com.spring.mti.model.Employe;
 import com.spring.mti.model.address.City;
 import com.spring.mti.model.security.Authorities;
 import com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;
 
 public class LayoutServiceImpl implements LayoutService {
 
 	private Map<String, Object> decorateMapEmploye(Employe item){
 		City c = item.getFk_city();
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("city", c.getName());
 		map.put("region", c.getFk_region().getName());
 		map.put("country", c.getFk_country().getName());
 		Department d = item.getFk_department();
 		if (d != null) {
 			map.put("department", d.getName());
 		} else {
 			map.put("department", "Не указан"); 
 		}	
 		map.put("fio", item.getFio());
 		map.put("id", item.getId());
 		return map;
 	}
 	
 	@Override
 	public List<Object> employeToMapJson(List<Employe> e){
 		List<Object> answer = new ArrayList<Object>();
 		for (Employe item : e){
			try {
				Map<String, Object> map = decorateMapEmploye(item);
				answer.add(map);
			} catch (Exception exept){
				exept.printStackTrace();
			}
 		}
 			return answer;
 	}
 	
 	@Override
 	public List<Object> authorityToMapJson(List<Authorities> e){
 		List<Object> answer = new ArrayList<Object>();
 		for (Authorities item : e){
 			Map<String, Object> map = new HashMap<String, Object>();
 			Employe em = item.getFk_user().getFk_employe(); 
 			if (em != null){
 				map = decorateMapEmploye(em);
 			} else {
 				String non = "Поле не задано";
 				map.put("city", non);
 				map.put("region", non);
 				map.put("country", non);
 				map.put("department", non);
 				map.put("fio", non);
 				map.put("id", non);
 			}	
 			map.put("role", item.getFk_role().getRname());
 			map.put("username", item.getFk_user().getUsername());
 			answer.add(map);
 		}
 			return answer;
 	}
 	
 	@Override
 	public Map<String, Object> queshionProfileToMapJson(List<Answer> a){
 		Map<String, Object> map = new HashMap<String, Object>()	;
 		Answer afirst = a.get(0);
 		map.put("category", afirst.getFk_queshion().getFk_catgory().getCname());
 		map.put("title", afirst.getFk_queshion().getName());
 		map.put("content", afirst.getFk_queshion().getContent());
 		map.put("id", afirst.getFk_queshion().getId());
 		map.put("count_answ", a.size());
 		for (int i = 0; i < a.size(); i++){
 			Answer item = a.get(i);
 			map.put("ans".concat(Integer.toString(i)), Arrays.asList(item.getContent(),item.isValid()));
 		}
 			return map;
 	}
 }
