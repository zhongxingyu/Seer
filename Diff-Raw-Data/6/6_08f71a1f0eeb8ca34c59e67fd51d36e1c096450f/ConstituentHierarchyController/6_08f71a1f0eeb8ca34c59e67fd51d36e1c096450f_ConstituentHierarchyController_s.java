 package com.orangeleap.tangerine.json.controller;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.orangeleap.tangerine.domain.Person;
 import com.orangeleap.tangerine.domain.customization.FieldDefinition;
 import com.orangeleap.tangerine.service.ConstituentService;
 import com.orangeleap.tangerine.service.RelationshipService;
 import com.orangeleap.tangerine.service.SiteService;
 import com.orangeleap.tangerine.service.relationship.PersonTreeNode;
 import com.orangeleap.tangerine.type.PageType;
 import com.orangeleap.tangerine.util.TangerineUserHelper;
 import com.orangeleap.tangerine.web.common.SortInfo;
 
 /**
  * This controller handles JSON requests for populating
  * the grid of gifts.
  * @version 1.0
  */
 @Controller
 public class ConstituentHierarchyController {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     @SuppressWarnings("unchecked")
 	private ModelMap personToMap(Person p) {
 
         ModelMap map = new ModelMap();
         map.put("id", p.getId());    
         map.put("text", p.getOrganizationName());    
         map.put("leaf", false);    
         return map;
 
     }
 
     @Resource(name="constituentService")
     private ConstituentService constituentService;
 
     @Resource(name="relationshipService")
     private RelationshipService relationshipService;
 
     @Resource(name="siteService")
     private SiteService siteService;
 
     @Resource(name="tangerineUserHelper")
     private TangerineUserHelper tangerineUserHelper;
 
     @SuppressWarnings("unchecked")
     @RequestMapping("/constituentHeirarchy.json")
     public ModelMap getTree(HttpServletRequest request)  {
 
     	String id = request.getParameter("node");
     	String memberPersonId = request.getParameter("memberPersonId");
     	String fieldDef = request.getParameter("fieldDef");
 
     	fieldDef = getFieldName(fieldDef);
 
     	logger.debug("constituentHeirarchy.json: id="+id+", memberPersonId="+memberPersonId+", fieldDef="+fieldDef);
 
 
     	List<Map> rows = new ArrayList<Map>();
 
     	try {
 
     		Person person;
     		if (id == null || "".equals(id) || "0".equals(id)) {
     			person = constituentService.readConstituentById(new Long(memberPersonId));
     			if (person == null) return null;
     			populateMaps(person);
     			Person head = relationshipService.getHeadOfTree(person, fieldDef);
     			rows.add(personToMap(head));
     		} else {
     			person = constituentService.readConstituentById(new Long(id));
     			if (person == null) return null;
     			populateMaps(person);
     			PersonTreeNode node = relationshipService.getTree(person, fieldDef, true, false);
     			for (int i = 0; i < node.getChildren().size(); i++) {
     				rows.add(personToMap(node.getChildren().get(i).getPerson()));
     			}
     		}
 
     		ModelMap map = new ModelMap();
     		map.put("_root", rows);
     		return map;
 
     	} catch (Exception e) {
     		logger.error(e);
     		return null;
     	}
     }
 
     private void populateMaps(Person person) {
 		// We want person relationships, so type maps are required.
         Map<String, String> fieldLabelMap = siteService.readFieldLabels(PageType.person, tangerineUserHelper.lookupUserRoles(), Locale.getDefault());
         person.setFieldLabelMap(fieldLabelMap);
 
         Map<String, Object> valueMap = siteService.readFieldValues(PageType.person, tangerineUserHelper.lookupUserRoles(), person);
         person.setFieldValueMap(valueMap);
 
         Map<String, FieldDefinition> typeMap = siteService.readFieldTypes(PageType.person, tangerineUserHelper.lookupUserRoles());
         person.setFieldTypeMap(typeMap);
     	
     }
     
     private String getFieldName(String fieldDef) {
     	fieldDef = fieldDef.substring(fieldDef.indexOf("[")+1);
    	fieldDef = fieldDef.substring(0,fieldDef.length()-1);
     	return fieldDef;
     }
 
     
 }
