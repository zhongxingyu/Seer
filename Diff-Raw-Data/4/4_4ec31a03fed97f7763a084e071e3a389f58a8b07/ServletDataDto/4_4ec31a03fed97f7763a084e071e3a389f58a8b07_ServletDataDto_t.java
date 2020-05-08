 package com.akjava.gwt.webappmaker.client;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.Cookie;
 
 import com.akjava.gwt.html5.client.file.ui.FileNameAndText;
 import com.akjava.gwt.webappmaker.client.resources.Bundles;
 import com.akjava.lib.common.form.FormData;
 import com.akjava.lib.common.form.FormFieldData;
 import com.akjava.lib.common.form.FormFieldDataDto;
 import com.akjava.lib.common.form.FormFieldDataDto.FormFieldToHiddenTagWithValueFunction;
 import com.akjava.lib.common.form.FormFieldDataPredicates;
 import com.akjava.lib.common.form.Relation;
 import com.akjava.lib.common.functions.HtmlFunctions;
 import com.akjava.lib.common.functions.StringFunctions.RowListStringJoinFunction;
 import com.akjava.lib.common.tag.LabelAndValue;
 import com.akjava.lib.common.tag.Tag;
 import com.akjava.lib.common.utils.TemplateUtils;
 import com.akjava.lib.common.utils.ValuesUtils;

 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.gwt.user.client.Window;
 
 public class ServletDataDto {
 //TODO move better place
 public static final String URL_ADD="add";
 public static final String URL_ADD_CONFIRM="add_confirm";
 public static final String URL_ADD_EXEC="add_exec";
 public static final String URL_SHOW = "show";
 public static final String URL_EDIT = "edit";
 public static final String URL_EDIT_CONFIRM = "edit_confirm";
 public static final String URL_EDIT_EXEC = "edit_exec";
 public static final String URL_DELETE_CONFIRM = "delete";
 public static final String URL_DELETE_EXEC = "delete_exec";
 private ServletDataDto(){}
 
 public static ServletDataToServletWebXmlFunction getServletDataToServletWebXmlFunction(){
 	return ServletDataToServletWebXmlFunction.INSTANCE;
 }
 public enum ServletDataToServletWebXmlFunction implements Function<ServletData,ServletWebXmlData>{
 	INSTANCE;
 	@Override
 	public ServletWebXmlData apply(ServletData data) {
 		
 		ServletWebXmlData xdata=new ServletWebXmlData();
 		xdata.setFullClassName(data.getBasePackage()+data.getLastPackage()+"."+data.getServletClassName());
 		
 		String head="";
 		if(!data.getLastPackage().equals("main")){
 			head="/"+data.getLastPackage();
 		}
 		xdata.setPath(head+data.getPath());
 		
 		xdata.setName(getName(data.getLastPackage(),data.getDataClassName(),data.getServletType()));
 		return xdata;
 	}
 	
 }
 
 public static String getName(String lastPackage,String dataName,String type){
 	if(lastPackage.equals("main")){
 		return dataName+type;
 	}else{
 		return ValuesUtils.toUpperCamel(lastPackage)+dataName+type;
 	}
 }
 
 
 public static class FormDataToMainServletDataFunction implements Function<FormData,List<ServletData>>{
 	private String basePackage;
 	public FormDataToMainServletDataFunction(String basePackage){
 		this.basePackage=basePackage;
 	}
 	@Override
 	public List<ServletData> apply(FormData fdata) {
 		List<ServletData> datas=Lists.newArrayList();
 		datas.add(new ServletData(basePackage,"main",ServletData.TYPE_LIST,fdata,"/index.html"));
 		datas.add(new ServletData(basePackage,"main",ServletData.TYPE_SHOW,fdata,"/show"));
 		
 		return datas;
 	}	
 }
 
 public static class FormDataToAdminServletDataFunction implements Function<FormData,List<ServletData>>{
 	private String basePackage;
 	public FormDataToAdminServletDataFunction(String basePackage){
 		this.basePackage=basePackage;
 	}
 	@Override
 	public List<ServletData> apply(FormData fdata) {
 		List<ServletData> datas=Lists.newArrayList();
 		
 		datas.add(new ServletData(basePackage,"admin",ServletData.TYPE_LIST,fdata,"/index.html"));
 		datas.add(new ServletData(basePackage,"admin",ServletData.TYPE_SHOW,fdata,"/show"));
 		
 		datas.add(new ServletData(basePackage,"admin",ServletData.TYPE_ADD,fdata,"/add"));
 		datas.add(new ServletData(basePackage,"admin",ServletData.TYPE_ADD_CONFIRM,fdata,"/add_confirm"));
 		datas.add(new ServletData(basePackage,"admin",ServletData.TYPE_ADD_EXEC,fdata,"/add_exec"));
 		
 		datas.add(new ServletData(basePackage,"admin",ServletData.TYPE_EDIT,fdata,"/edit"));
 		datas.add(new ServletData(basePackage,"admin",ServletData.TYPE_EDIT_CONFIRM,fdata,"/edit_confirm"));
 		datas.add(new ServletData(basePackage,"admin",ServletData.TYPE_EDIT_EXEC,fdata,"/edit_exec"));
 		
 		
 		datas.add(new ServletData(basePackage,"admin",ServletData.TYPE_DELETE_CONFIRM,fdata,"/delete"));//confirm but link is delete
 		datas.add(new ServletData(basePackage,"admin",ServletData.TYPE_DELETE_EXEC,fdata,"/delete_exec"));
 		return datas;
 	}	
 }
 
 
 /**
  * @deprecated no more used
  * @author aki
  *
  */
 public static class FormDataToOptionValueFunction implements Function<FormFieldData,String>{
 
 	@Override
 	public String apply(FormFieldData data) {
 		String result=null;
 		//switch by type
 		if(data.getType()==FormFieldData.TYPE_CHECK){
 			result=TemplateUtils.createText(Bundles.INSTANCE.createoptionvalue_check().getText(), data.getKey());
 		}else if(data.getType()==FormFieldData.TYPE_SELECT_SINGLE || data.getType()==FormFieldData.TYPE_SELECT_MULTI){
 			
 			Map<String,String> map=new HashMap<String, String>();
 			map.put("key", data.getKey());
 			List<String> optionValues=new ArrayList<String>();
 			for(LabelAndValue lv:data.getOptionValues()){
 				optionValues.add("\""+lv.getValue()+"\"");
 			}
 			map.put("option_value", Joiner.on(",").join(optionValues));
 			result=TemplateUtils.createText(Bundles.INSTANCE.createoptionvalue_single_select().getText(),map);
 		}
 		return result;
 	}
 
 	
 	
 }
 
 public static class FormDataToCreateFormFieldFunction implements Function<FormFieldData,String>{
 
 	@Override
 	public String apply(FormFieldData data) {
 		switch(data.getType()){
 		case FormFieldData.TYPE_TEXT_SHORT:
 		case FormFieldData.TYPE_NUMBER:
 			return TemplateUtils.createText(Bundles.INSTANCE.createforms_text().getText(),data.getKey());
 		case FormFieldData.TYPE_TEXT_LONG:
 			return TemplateUtils.createText(Bundles.INSTANCE.createforms_textarea().getText(),data.getKey());
 		case FormFieldData.TYPE_CHECK:
 			return TemplateUtils.createText(Bundles.INSTANCE.createforms_check().getText(),data.getKey());
 		case FormFieldData.TYPE_SELECT_SINGLE:
 			return TemplateUtils.createText(Bundles.INSTANCE.createforms_single().getText(),data.getKey());
 		case FormFieldData.TYPE_SELECT_MULTI:
 			return TemplateUtils.createText(Bundles.INSTANCE.createforms_multi().getText(),data.getKey());
 		case FormFieldData.TYPE_ID:
 		
 		case FormFieldData.TYPE_CREATE_USER:
 		
 		case FormFieldData.TYPE_MODIFIED_USER:
 			return TemplateUtils.createText(Bundles.INSTANCE.createforms_hidden().getText(),data.getKey());
 		case FormFieldData.TYPE_CREATE_DATE:
 		case FormFieldData.TYPE_MODIFIED_DATE:
 			return TemplateUtils.createText(Bundles.INSTANCE.createforms_date().getText(),data.getKey());
 		default:
 			return null;
 			
 		}
 		
 	}
 
 	
 	
 }
 
 private static class ListOrder{
 	private String key;
 	private boolean asc;
 	public String getKey() {
 		return key;
 	}
 
 	public boolean isAsc() {
 		return asc;
 	}
 
 	public ListOrder(String key,boolean asc){
 		this.key=key;
 		this.asc=asc;
 	}
 }
 private static ListOrder parseListOrder(String listOrder,FormData data){
 	if(listOrder==null){
 		String cdateKey=null;
 		for(FormFieldData fdata:data.getFormFieldDatas()){
 			if(fdata.getType()==FormFieldData.TYPE_CREATE_DATE){
 				cdateKey=fdata.getKey();
 				break;
 			}
 		}
 		String key=cdateKey==null?data.getFormFieldDatas().get(0).getKey():cdateKey;
 		return new ListOrder(key,false);
 	}else{
 		boolean asc=true;
 		if(listOrder.endsWith("asc")){
 			listOrder=listOrder.substring(0, listOrder.length()-3);
 		}else if(listOrder.endsWith("desc")){
 			asc=false;
 			listOrder=listOrder.substring(0, listOrder.length()-4);
 		}
 		return new ListOrder(listOrder.trim(),asc);
 	}
 }
 public static class ServletDataToServletFileFunction implements Function<ServletData,FileNameAndText>{
 
 	@Override
 	public FileNameAndText apply(ServletData data) {
 		FormFieldData keyFieldData=data.getFormData().getIdFieldData();//TODO before check call this method.
 		
 		Map<String,String> map=new HashMap<String,String>();
 		
 		FileNameAndText file=new FileNameAndText();
 		file.setName(data.getServletClassName()+".java");
 		String javaTemplate=null;
 		if(data.getServletType().equals(ServletData.TYPE_LIST)){
 			
 			javaTemplate=Bundles.INSTANCE.list_servlet().getText();
 			
 			if(data.getLastPackage().equals("admin")){
 				map.put("pageSize", ""+data.getFormData().getAdminPageSize());
 				//GWT.log(data.getFormData().getAdminPageOrder());
 				ListOrder listOrder=parseListOrder(data.getFormData().getAdminPageOrder(), data.getFormData());
 				map.put("order_key", listOrder.getKey());
 				map.put("order_asc", ""+listOrder.isAsc());
 			}else{
 				map.put("pageSize", ""+data.getFormData().getPageSize());
 				ListOrder listOrder=parseListOrder(data.getFormData().getPageOrder(), data.getFormData());
 				map.put("order_key", listOrder.getKey());
 				map.put("order_asc", ""+listOrder.isAsc());
 			}
 			
 			/*
 			 * whereValues is define parameters which get from request
 			 * you can list with filter.
 			 */
 			String where="";	
 			for(FormFieldData fdata:data.getFormData().getFormFieldDatas()){
 				if(fdata.isRelativeField()){
 					String whereTemplate=null;
 					if(fdata.getType()==FormFieldData.TYPE_NUMBER){
 						whereTemplate=Bundles.INSTANCE.list_where_number().getText();
 					}else if(fdata.getType()==FormFieldData.TYPE_SELECT_SINGLE||fdata.getType()==FormFieldData.TYPE_SELECT_MULTI){
 						continue;
 					}
 					else{
 						throw new RuntimeException("not supported relation's invalid fieldtype:"+fdata);
 					}
 					Map<String,String> tmp=new HashMap<String, String>();
 					tmp.put("key", fdata.getKey());
 					where+=TemplateUtils.createText(whereTemplate, tmp);
 				}
 			}
 			map.put("whereValues", where);
 			
 		}else if(data.getServletType().equals(ServletData.TYPE_SHOW)){
 			boolean admin=false;
 			if(data.getLastPackage().equals("admin")){
 				admin=true;
 			}
 			javaTemplate=Bundles.INSTANCE.show_servlet().getText();
 			
 			List<Relation> relations=data.getFormData().getChildrens();
 			String relationListMethods="";
 			String relationLists="";
 			
 			if(relations.size()!=0){
 				FormData parentData=data.getFormData();
 				
 				String keyId=parentData.getIdFieldData().getKey();
 				String quot="";
 				if(parentData.getIdFieldData().getType()!=FormFieldData.TYPE_ID && parentData.getIdFieldData().getType()!=FormFieldData.TYPE_NUMBER){
 					quot="'";//future string case
 				}
 				String showNext=Internationals.getMessage("show_next");
 				
 				
 				String methodBase=Bundles.INSTANCE.createrelationlist().getText();
 				for(Relation relation:relations){
 					Map<String,String> tmp=new HashMap<String, String>();
 					FormData childData=relation.getData();
 					//get order
 					ListOrder listOrder=parseListOrder(childData.getSubPageOrder(), childData);
 					tmp.put("pageSize", ""+childData.getSubPageSize());
 					tmp.put("orderKey", listOrder.getKey());
 					tmp.put("orderAsc", ""+listOrder.isAsc());
 					
 				tmp.put("childClass", childData.getClassName());
 				tmp.put("refKey", relation.getKey());
 				tmp.put("keyId", keyId);
 				tmp.put("quot", quot);
 				tmp.put("showNext", showNext);
 				tmp.put("parentClass", parentData.getClassName());
 				
 				tmp.put("admin", admin?"admin_":"");
 				relationListMethods+=TemplateUtils.createAdvancedText(methodBase, tmp);
 				}
 				
 				relationLists+="PersistenceManager manager=PMF.get().getPersistenceManager();\n";
 				String callBase="create${parentClass}${childClass}${u+refKey}List(manager,entity);\n";
 				for(Relation relation:relations){
 					FormData childData=relation.getData();
 					Map<String,String> tmp=new HashMap<String, String>();
 					tmp.put("refKey", relation.getKey());
 					tmp.put("parentClass", parentData.getClassName());
 					tmp.put("childClass", childData.getClassName());
 					relationLists+=TemplateUtils.createAdvancedText(callBase, tmp);
 					}
 			}
 			
 			map.put("relationListMethods", relationListMethods);
 			map.put("relationLists", relationLists);
 		}else if(data.getServletType().equals(ServletData.TYPE_ADD)){
 			javaTemplate=Bundles.INSTANCE.add_servlet().getText();
 			
 			//
 			
 		}else if(data.getServletType().equals(ServletData.TYPE_ADD_CONFIRM)){
 			javaTemplate=Bundles.INSTANCE.add_confirm_servlet().getText();
 		}else if(data.getServletType().equals(ServletData.TYPE_ADD_EXEC)){
 			javaTemplate=Bundles.INSTANCE.add_exec_servlet().getText();
 			
 			List<String> cdateActions=new ArrayList<String>();
 			for(FormFieldData field:data.getFormData().getFormFieldDatas()){
 				if(field.getType()==FormFieldData.TYPE_CREATE_DATE){
 					cdateActions.add(TemplateUtils.createText("entity.set${value}(System.currentTimeMillis());", ValuesUtils.toUpperCamel(field.getKey())));
 				}
 			}
 			map.put("cdate_action", Joiner.on("\n").join(cdateActions));
 			
 			List<String> cuserActions=new ArrayList<String>();
 			for(FormFieldData field:data.getFormData().getFormFieldDatas()){
 				if(field.getType()==FormFieldData.TYPE_CREATE_USER){
 					cuserActions.add(TemplateUtils.createText("entity.set${value}(SharedUtils.getUserId());", ValuesUtils.toUpperCamel(field.getKey())));
 				}
 			}
 			map.put("cuser_action", Joiner.on("\n").join(cuserActions));
 			
 			//create cookie inserting method,which value get from Class Options cookie(name1:name2:etc)
 			String insertCookies="";
			String cookieTemplate="\t\t\t\tif(map.get(\"${value}\")!=null){Cookie c=new Cookie(\"${value}\",map.get(\"${value}\"));c.setMaxAge(SharedUtils.COOKIE_AGE);response.addCookie(c);}\n";
 			for(String cname:data.getFormData().getSupportCookies()){
 				//TODO check valid
 				insertCookies+=TemplateUtils.createText(cookieTemplate, cname);
 			}
 			map.put("insertCookies",insertCookies);
 			
 		}else if(data.getServletType().equals(ServletData.TYPE_EDIT)){
 			javaTemplate=Bundles.INSTANCE.edit_servlet().getText();
 			
 			//Collection<String> methods=Collections2.transform(data.getFormData().getFormFieldDatas(), new FormDataToCreateFormFieldFunction());
 			//String methodText=Joiner.on("\n").skipNulls().join(methods);
 			
 			//map.put("createFormFields", methodText);
 			//javaTemplate=javaTemplate.replace("${createFormFields}", methodText);//because call another templa inside
 			
 		}else if(data.getServletType().equals(ServletData.TYPE_EDIT_CONFIRM)){
 			javaTemplate=Bundles.INSTANCE.edit_confirm_servlet().getText();
 		}else if(data.getServletType().equals(ServletData.TYPE_EDIT_EXEC)){
 			javaTemplate=Bundles.INSTANCE.edit_exec_servlet().getText();
 			
 			//createOptionValues
 			List<String> mdateActions=new ArrayList<String>();
 			for(FormFieldData field:data.getFormData().getFormFieldDatas()){
 				if(field.getType()==FormFieldData.TYPE_MODIFIED_DATE){
 					mdateActions.add(TemplateUtils.createText("entity.set${value}(System.currentTimeMillis());", ValuesUtils.toUpperCamel(field.getKey())));
 				}
 			}
 			map.put("mdate_action", Joiner.on("\n").join(mdateActions));
 			
 			List<String> muserActions=new ArrayList<String>();
 			for(FormFieldData field:data.getFormData().getFormFieldDatas()){
 				if(field.getType()==FormFieldData.TYPE_MODIFIED_USER){
 					muserActions.add(TemplateUtils.createText("entity.set${value}(SharedUtils.getUserId());", ValuesUtils.toUpperCamel(field.getKey())));
 				}
 			}
 			map.put("muser_action", Joiner.on("\n").join(muserActions));
 		}else if(data.getServletType().equals(ServletData.TYPE_DELETE_CONFIRM)){
 			javaTemplate=Bundles.INSTANCE.delete_confirm_servlet().getText();
 		}else if(data.getServletType().equals(ServletData.TYPE_DELETE_EXEC)){
 			javaTemplate=Bundles.INSTANCE.delete_exec_servlet().getText();
 		}
 		
 		if(javaTemplate==null){
 			Window.alert("invalid type:"+data.getServletType());
 		}
 		
 		
 		if(data.getLastPackage().equals("main")){
 			map.put("useCache","true");
 		}else{
 			map.put("useCache","false");
 		}
 		
 		//TODO remove id ,because in future number support  minus or small values.
 				if(keyFieldData.getType()==FormFieldData.TYPE_ID||keyFieldData.getType()==FormFieldData.TYPE_NUMBER){
 					map.put("PARSE_KEY_VALUE", "Long keyValue=Long.parseLong(id);");
 				}else{//type must be text
 					map.put("PARSE_KEY_VALUE", "String keyValue=id;");
 				}
 		
 		
 		map.put("className", data.getServletClassName());
 		map.put("basePackage", data.getBasePackage());
 		map.put("package", data.getBasePackage()+data.getLastPackage());
 		
 		map.put("baseTemplate", data.getLastPackage()+"Base.html");
 		String head="";
 		if(!data.getLastPackage().equals("main")){
 			head=data.getLastPackage()+"_";
 		}
 		String mainTemplate=head+data.getDataClassName()+"_"+ValuesUtils.upperCamelToUnderbar(data.getServletType());
 		map.put("mainTemplate", mainTemplate.toLowerCase()+".html");
 		
 		map.put("title", data.getFormData().getName()+" "+Internationals.getMessage(ValuesUtils.upperCamelToUnderbar(data.getServletType()).toLowerCase()));
 		
 		map.put("mainRowTemplate", mainTemplate.toLowerCase()+"_row"+".html");
 		map.put("dataClassName", data.getDataClassName());
 		
 		map.put("keyId",data.getFormData().getIdFieldData().getKey());
 		
 		String phead="";
 		if(!data.getLastPackage().equals("main")){
 			phead="/"+data.getLastPackage();
 		}
 		String fullpath=phead+data.getPath();
 		map.put("path",fullpath);
 		
 		
 		String path=fullpath;
 		if(!path.endsWith("/")){
 			int last=path.lastIndexOf("/");
 			if(last!=-1){
 				String indexPath=path.substring(0,last+1);
 				map.put("indexPath", indexPath);
 			}
 		}
 		
 		String text=TemplateUtils.createAdvancedText(javaTemplate, map);
 		file.setText(text);
 		
 		return file;
 	}
 	
 	
 }
 
 public static class ServletDataToTemplateFileFunction implements Function<ServletData,List<FileNameAndText>>{
 
 	@Override
 	public List<FileNameAndText> apply(ServletData data) {
 		Map<String,String> map=new HashMap<String,String>();
 		
 		FormFieldData keyFieldData=data.getFormData().getIdFieldData();//TODO before check call this method.
 		
 		List<FileNameAndText> files=new ArrayList<FileNameAndText>();
 		String type=data.getServletType();
 		
 		FileNameAndText file=new FileNameAndText();
 		String head="";
 		if(!data.getLastPackage().equals("main")){
 			head=data.getLastPackage()+"_";
 		}
 		file.setName(head+data.getDataClassName().toLowerCase()+"_"+ValuesUtils.upperCamelToUnderbar(type)+".html");
 		
 		String htmlTemplate=null;
 		
 		
 		
 		if(type.equals(ServletData.TYPE_LIST)){
 			if (data.getLastPackage().equals("admin")) {
 				htmlTemplate = Bundles.INSTANCE.admin_list_html().getText();
 				
 			} else {
 				htmlTemplate = Bundles.INSTANCE.list_html().getText();
 			}
 			
 			
 			FileNameAndText file2=new FileNameAndText();
 			file2.setName(head+data.getDataClassName().toLowerCase()+"_"+type.toLowerCase()+"_row.html");
 			files.add(file2);
 			if(data.getLastPackage().equals("admin")){
 				file2.setText(Bundles.INSTANCE.admin_list_row_html().getText());	
 			}else{
 			file2.setText(Bundles.INSTANCE.list_row_html().getText());
 			}
 			
 			} else if (type.equals(ServletData.TYPE_SHOW)) {
 				
 				if (data.getLastPackage().equals("admin")) {
 					htmlTemplate = Bundles.INSTANCE.admin_show_html().getText();
 					
 				} else {
 					htmlTemplate = Bundles.INSTANCE.show_html().getText();
 				}
 				
 				List<Relation> relations=data.getFormData().getChildrens();
 				
 				String relationLists="";
 				
 				//has children
 				if(relations.size()!=0){
 					boolean admin=false;
 					String relation_rowTemplate=null;
 					
 					if(data.getLastPackage().equals("admin")){
 					relation_rowTemplate=Bundles.INSTANCE.admin_list_row_relation_html().getText();
 					admin=true;
 					}else{
 					relation_rowTemplate=Bundles.INSTANCE.list_row_relation_html().getText();
 					}
 					
 					//create list row file
 					
 					String base=admin?Bundles.INSTANCE.admin_show_sub_list_html().getText():
 							Bundles.INSTANCE.show_sub_list_html().getText();
 					
 					for(Relation relation:relations){
 						FormData children=relation.getData();
 						FormData parent=data.getFormData();
 						
 						String columnName=children.getFieldData(relation.getKey()).getName();//TODO error check
 						String columnKey=relation.getKey();
 						String dirName=children.getClassName().toLowerCase();
 						String title=children.getName()+"("+columnName+") "+Internationals.getMessage("list");
 						String add_link="../"+dirName+"/add?"+columnKey+"=${value_"+keyFieldData.getKey()+"}";
 						String add_title=children.getName()+" "+Internationals.getMessage("add");
 						
 						List<String> names=Lists.transform(children.getFormFieldDatas(), FormFieldDataDto.getFormFieldToNameFunction());
 						List<String> ths=Lists.transform(names
 								, HtmlFunctions.getStringToTHFunction());
 						String table_headers="<th></th>\n"+Joiner.on("\n").join(ths);
 						if(admin){
 							table_headers+="\n<th></th><th></th>";
 						}
 						
 						Map<String,String> tmp=new HashMap<String, String>();
 						//data_Relation & menu_Relation use other template
 						tmp.put("childrenClass", children.getClassName());
 						tmp.put("title", title);
 						tmp.put("add_link", add_link);
 						tmp.put("add_title", add_title);
 						tmp.put("table_headers", table_headers);
 						relationLists+=TemplateUtils.createAdvancedText(base, tmp);
 						
 						//create row file
 						String rowFileName=parent.getClassName().toLowerCase()+"_"+"list_"+children.getClassName().toLowerCase()+"_"+relation.getKey()+"_row.html";
 						if(admin){
 							rowFileName="admin_"+rowFileName;
 						}
 						
 						
 						
 						//columns 
 						Iterable<String> keys=Iterables.transform(children.getFormFieldDatas(), FormFieldDataDto.getFormFieldToKeyFunction());
 						Iterable<String> keyParams=
 								Iterables.transform(keys,new HtmlFunctions.StringToPreFixAndSuffix("<td>${label_","}</td>"));
 						String rowText=relation_rowTemplate.replace("${CHILD_COLUMNS}", Joiner.on("\n").join(keyParams));
 						rowText=rowText.replace("${DIRNAME}", children.getClassName().toLowerCase());
 						String keyName=children.getIdFieldData().getKey();
 						rowText=rowText.replace("${ID_KEY_SUB}",keyName);
 						rowText=rowText.replace("${VALUE_ID_KEY_SUB}","${value_"+keyName+"}");
 						
 						files.add(new FileNameAndText(rowFileName,rowText));
 						
 					}
 				}
 				map.put("relationLists", relationLists);
 
 		}else if(type.equals(ServletData.TYPE_ADD)){
 			htmlTemplate=Bundles.INSTANCE.add_html().getText();
 			//create first td
 			Iterable<FormFieldData> datas=Iterables.filter(data.getFormData().getFormFieldDatas(), FormFieldDataPredicates.getNotAutoCreate());
 			Iterable<String> names=Iterables.transform(datas, FormFieldDataDto.getFormFieldToNameFunction());
 			
 			//create second td
 			Iterable<String> keys=Iterables.transform(datas, FormFieldDataDto.getFormFieldToKeyFunction());
 			
 			Iterable<String> inputTags=
 					Iterables.transform(keys,new HtmlFunctions.StringToPreFixAndSuffix("${form_","}"));
 			
 			//Iterable<Tag> inputTags=Iterables.transform(datas,FormFieldDataDto.getFormFieldToInputTagFunction());
 			//Iterable<String> tagString=Iterables.transform(inputTags, new TagToString());
 			
 			//create tr
 			List<List<String>> vs=new ArrayList<List<String>>();
 			vs.add(Lists.newArrayList(names));
 			vs.add(Lists.newArrayList(inputTags));
 			
 			map.put("add_input_trtd",
 			HtmlFunctions.getStringToTRTDFunction().apply(vs)
 			);
 			
 			
 			map.put("add_confirm_title", Internationals.getMessage("add_confirm"));
 			
 		}else if(type.equals(ServletData.TYPE_ADD_CONFIRM)){
 			
 			
 			htmlTemplate=Bundles.INSTANCE.add_confirm_html().getText();
 			//filter auto generate keys
 			Iterable<FormFieldData> datas=Iterables.filter(data.getFormData().getFormFieldDatas(), FormFieldDataPredicates.getNotAutoCreate());
 			
 			//create label first td
 			Iterable<String> names=Iterables.transform(datas, FormFieldDataDto.getFormFieldToNameFunction());
 			
 			Iterable<String> keys=Iterables.transform(datas, FormFieldDataDto.getFormFieldToKeyFunction());
 			//create line1(hidden) of second td
 			
 			Map<String,String> hmap=new HashMap<String, String>();
 			for(String key:keys){
 				hmap.put(key, "${value_"+key+"}");
 			}
 			Iterable<Tag> inputTags=Iterables.transform(datas,new FormFieldToHiddenTagWithValueFunction(hmap));
 			Iterable<String> tagString=Iterables.transform(inputTags, new TagToString());
 			
 			//create line2(valuetemplate) of second td 
 			Iterable<String> keyParams=
 					Iterables.transform(keys,new HtmlFunctions.StringToPreFixAndSuffix("${label_","}"));
 			
 			//join line1 & line2 to right td
 			List<List<String>> tds=new ArrayList<List<String>>();
 			tds.add(Lists.newArrayList(tagString));
 			tds.add(Lists.newArrayList(keyParams));
 			List<String> rightTds=new RowListStringJoinFunction("\n").apply(tds);
 			
 			//create tr td
 			List<List<String>> vs=new ArrayList<List<String>>();
 			vs.add(Lists.newArrayList(names));
 			vs.add(Lists.newArrayList(rightTds));
 			
 			map.put("confirm_input_trtd",
 			HtmlFunctions.getStringToTRTDFunction().apply(vs)
 			);
 			
 			//map.put("has_error_message", Internationals.getMessage("has_error"));
 			map.put("add_exec_title", Internationals.getMessage("add_exec"));
 			
 			
 			
 			Iterable<String> errorParams=
 					Iterables.transform(keys,new HtmlFunctions.StringToPreFixAndSuffix("${error_","}"));
 			map.put("error_messages", Joiner.on("\n").join(errorParams));
 		}else if(type.equals(ServletData.TYPE_ADD_EXEC)){
 			htmlTemplate=Bundles.INSTANCE.add_exec_html().getText();
 			//handle erros
 			//map.put("has_error_message", Internationals.getMessage("has_error"));
 			Iterable<FormFieldData> datas=Iterables.filter(data.getFormData().getFormFieldDatas(), FormFieldDataPredicates.getNotAutoCreate());
 			Iterable<String> keys=Iterables.transform(datas, FormFieldDataDto.getFormFieldToKeyFunction());
 			Iterable<String> errorParams=
 					Iterables.transform(keys,new HtmlFunctions.StringToPreFixAndSuffix("${error_","}"));
 			map.put("error_messages", Joiner.on("\n").join(errorParams));
 			
 			
 			map.put("add_complete_title", Internationals.getMessage("add_complete"));
 			map.put("list_title", data.getFormData().getName()+" "+Internationals.getMessage("list"));
 			
 		}else if(type.equals(ServletData.TYPE_EDIT)){
 			htmlTemplate=Bundles.INSTANCE.edit_html().getText();
 			
 			
 			//create first td
 			//Iterable<FormFieldData> datas=Iterables.filter(data.getFormData().getFormFieldDatas(), FormFieldDataPredicates.getNotAutoCreate());
 			//need all data
 			Iterable<FormFieldData> datas=data.getFormData().getFormFieldDatas();
 			Iterable<String> names=Iterables.transform(datas, FormFieldDataDto.getFormFieldToNameFunction());
 			//create second td
 			//Iterable<Tag> inputTags=Iterables.transform(datas,FormFieldDataDto.getFormFieldToInputTemplateTagFunction());
 			Iterable<String> keys=Iterables.transform(datas, FormFieldDataDto.getFormFieldToKeyFunction());
 			Iterable<String> inputTags=
 					Iterables.transform(keys,new HtmlFunctions.StringToPreFixAndSuffix("${form_","}"));
 			
 			//Iterable<String> tagString=Iterables.transform(inputTags, new TagToString());
 			
 			//create tr
 			List<List<String>> vs=new ArrayList<List<String>>();
 			vs.add(Lists.newArrayList(names));
 			vs.add(Lists.newArrayList(inputTags));
 			
 			map.put("edit_input_trtd",
 			HtmlFunctions.getStringToTRTDFunction().apply(vs)
 			);
 			
 			
 			map.put("edit_confirm_title", Internationals.getMessage("edit_confirm"));
 			
 		}else if(type.equals(ServletData.TYPE_EDIT_CONFIRM)){
 			
 
 			htmlTemplate=Bundles.INSTANCE.edit_confirm_html().getText();
 			
 			Iterable<FormFieldData> datas=data.getFormData().getFormFieldDatas();
 			
 			//create label first td
 			Iterable<String> names=Iterables.transform(datas, FormFieldDataDto.getFormFieldToNameFunction());
 			
 			Iterable<String> keys=Iterables.transform(datas, FormFieldDataDto.getFormFieldToKeyFunction());
 			//create line1(hidden) of second td
 			
 			Map<String,String> hmap=new HashMap<String, String>();
 			for(String key:keys){
 				hmap.put(key, "${value_"+key+"}");
 			}
 			Iterable<Tag> inputTags=Iterables.transform(datas,new FormFieldToHiddenTagWithValueFunction(hmap));
 			Iterable<String> tagString=Iterables.transform(inputTags, new TagToString());
 			
 			//create line2(valuetemplate) of second td 
 			Iterable<String> keyParams=
 					Iterables.transform(keys,new HtmlFunctions.StringToPreFixAndSuffix("${label_","}"));
 			
 			//join line1 & line2 to right td
 			List<List<String>> tds=new ArrayList<List<String>>();
 			tds.add(Lists.newArrayList(tagString));
 			tds.add(Lists.newArrayList(keyParams));
 			List<String> rightTds=new RowListStringJoinFunction("\n").apply(tds);
 			
 			//create tr td
 			List<List<String>> vs=new ArrayList<List<String>>();
 			vs.add(Lists.newArrayList(names));
 			vs.add(Lists.newArrayList(rightTds));
 			
 			map.put("confirm_input_trtd",
 			HtmlFunctions.getStringToTRTDFunction().apply(vs)
 			);
 			
 			//map.put("has_error_message", Internationals.getMessage("has_error"));
 			Iterable<String> errorParams=
 					Iterables.transform(keys,new HtmlFunctions.StringToPreFixAndSuffix("${error_","}"));
 			map.put("error_messages", Joiner.on("\n").join(errorParams));
 			
 			map.put("edit_exec_title", Internationals.getMessage("edit_exec"));
 		}else if(type.equals(ServletData.TYPE_EDIT_EXEC)){
 			htmlTemplate=Bundles.INSTANCE.edit_exec_html().getText();
 			
 			Iterable<FormFieldData> datas=data.getFormData().getFormFieldDatas();
 			Iterable<String> keys=Iterables.transform(datas, FormFieldDataDto.getFormFieldToKeyFunction());
 			//map.put("has_error_message", Internationals.getMessage("has_error"));
 			Iterable<String> errorParams=
 					Iterables.transform(keys,new HtmlFunctions.StringToPreFixAndSuffix("${error_","}"));
 			map.put("error_messages", Joiner.on("\n").join(errorParams));
 			
 			map.put("edit_complete_title", Internationals.getMessage("edit_complete"));
 			map.put("list_title", data.getFormData().getName()+" "+Internationals.getMessage("list"));
 		}else if(type.equals(ServletData.TYPE_DELETE_CONFIRM)){
 			htmlTemplate=Bundles.INSTANCE.delete_confirm_html().getText();
 			map.put("keyId", data.getFormData().getIdFieldData().getKey());
 			//map.put("has_error_message", Internationals.getMessage("has_error"));
 			map.put("delete_exec_title", Internationals.getMessage("delete_exec"));
 		}else if(type.equals(ServletData.TYPE_DELETE_EXEC)){
 			
 			htmlTemplate=Bundles.INSTANCE.delete_exec_html().getText();
 			//map.put("has_error_message", Internationals.getMessage("has_error"));
 			map.put("delete_complete_title", Internationals.getMessage("delete_complete"));
 			map.put("list_title", data.getFormData().getName()+" "+Internationals.getMessage("list"));
 		}
 		
 		if(htmlTemplate==null){
 			Window.alert("invalid type:"+data.getServletType());
 		}
 		file.setText(htmlTemplate);
 		files.add(file);
 		
 		
 		
 		//headers
 		List<String> names=Lists.transform(data.getFormData().getFormFieldDatas(), FormFieldDataDto.getFormFieldToNameFunction());
 		
 		List<String> ths=Lists.transform(names
 				, HtmlFunctions.getStringToTHFunction());
 		map.put("headers", Joiner.on("\n").join(ths));
 		//columns
 		List<String> keys=Lists.transform(data.getFormData().getFormFieldDatas(), FormFieldDataDto.getFormFieldToKeyFunction());
 		List<String> tds=Lists.transform(
 				Lists.transform(
 				keys
 				,new HtmlFunctions.StringToPreFixAndSuffix("${label_","}"))
 				, HtmlFunctions.getStringToTDFunction());
 		map.put("columns", Joiner.on("\n").join(tds));
 		
 		
 		//name trtd for show
 		List<List<String>> vs=new ArrayList<List<String>>();
 		vs.add(names);
 		vs.add(tds);
 		map.put("name_key_trtd",
 		HtmlFunctions.getStringToTRTDFunction().apply(vs)
 		);
 		
 		map.put("data_name", data.getFormData().getName());
 		map.put("show_title", Internationals.getMessage("show"));
 		map.put("add_title", Internationals.getMessage("add"));
 		map.put("edit_title", Internationals.getMessage("edit"));
 		map.put("delete_title", Internationals.getMessage("delete"));
 		map.put("reset_title", Internationals.getMessage("reset"));
 		
 		//usually value_id is setted,but sometime id field-key name other,should set it.
 		map.put("VALUE_ID_KEY", "${value_"+keyFieldData.getKey()+"}");
 		
 		//usually set id,but sometime different.
 		map.put("ID_KEY", ""+keyFieldData.getKey());
 		
 		
 		
 		
 		for(FileNameAndText fileText:files){
 			if(fileText.getText()==null){
 				throw new RuntimeException("empty body:"+fileText.getName());
 			}
 			String text=TemplateUtils.createAdvancedText(fileText.getText(), map);
 			fileText.setText(text);
 		}
 		
 		return files;
 	}
 	
 	
 	
 }
 
 public static class TagToString implements Function<Tag,String>{
 
 	@Override
 	public String apply(Tag tag) {
 		if(tag.getName().equals("input") && tag.getAttributes().get("type").equals("text")){
 			tag.setAttribute("size", "40");
 		}
 		return tag.toString();
 	}
 	
 }
 
 }
