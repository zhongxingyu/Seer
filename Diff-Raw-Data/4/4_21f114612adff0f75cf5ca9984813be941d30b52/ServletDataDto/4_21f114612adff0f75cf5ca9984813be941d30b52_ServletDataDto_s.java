 package com.akjava.gwt.webappmaker.client;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.akjava.gwt.webappmaker.client.resources.Bundles;
 import com.akjava.lib.common.form.FormData;
 import com.akjava.lib.common.form.FormFieldData;
 import com.akjava.lib.common.form.FormFieldDataDto;
 import com.akjava.lib.common.form.FormFieldDataDto.FormFieldToHiddenTagWithValueFunction;
 import com.akjava.lib.common.form.FormFieldDataPredicates;
 import com.akjava.lib.common.functions.HtmlFunctions;
 import com.akjava.lib.common.functions.StringFunctions.RowListStringJoinFunction;
 import com.akjava.lib.common.tag.LabelAndValue;
 import com.akjava.lib.common.tag.Tag;
 import com.akjava.lib.common.utils.TemplateUtils;
 import com.akjava.lib.common.utils.ValuesUtils;
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.gwt.user.client.Window;
 
 public class ServletDataDto {
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
 
 public static class ServletDataToServletFileFunction implements Function<ServletData,FileNameAndText>{
 
 	@Override
 	public FileNameAndText apply(ServletData data) {
 		Map<String,String> map=new HashMap<String,String>();
 		
 		FileNameAndText file=new FileNameAndText();
 		file.setName(data.getServletClassName()+".java");
 		String javaTemplate=null;
 		if(data.getServletType().equals(ServletData.TYPE_LIST)){
 			javaTemplate=Bundles.INSTANCE.list_servlet().getText();
 		}else if(data.getServletType().equals(ServletData.TYPE_SHOW)){
 			javaTemplate=Bundles.INSTANCE.show_servlet().getText();
 		}else if(data.getServletType().equals(ServletData.TYPE_ADD)){
 			javaTemplate=Bundles.INSTANCE.add_servlet().getText();
 		}else if(data.getServletType().equals(ServletData.TYPE_ADD_CONFIRM)){
 			javaTemplate=Bundles.INSTANCE.add_confirm_servlet().getText();
 		}else if(data.getServletType().equals(ServletData.TYPE_ADD_EXEC)){
 			javaTemplate=Bundles.INSTANCE.add_exec_servlet().getText();
 		}else if(data.getServletType().equals(ServletData.TYPE_EDIT)){
 			javaTemplate=Bundles.INSTANCE.edit_servlet().getText();
 			
 			Collection<String> methods=Collections2.transform(data.getFormData().getFormFieldDatas(), new FormDataToOptionValueFunction());
 			String methodText=Joiner.on("\n").skipNulls().join(methods);
 			
 			map.put("createOptionValues", methodText);
 		}else if(data.getServletType().equals(ServletData.TYPE_EDIT_CONFIRM)){
 			javaTemplate=Bundles.INSTANCE.edit_confirm_servlet().getText();
 		}else if(data.getServletType().equals(ServletData.TYPE_EDIT_EXEC)){
 			javaTemplate=Bundles.INSTANCE.edit_exec_servlet().getText();
 			
 			//createOptionValues
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
 		
 		
 		map.put("className", data.getServletClassName());
 		map.put("basePackage", data.getBasePackage());
 		map.put("package", data.getBasePackage()+data.getLastPackage());
 		
 		map.put("baseTemplate", data.getLastPackage()+"Base.html");
 		String head="";
 		if(!data.getLastPackage().equals("main")){
 			head=data.getLastPackage()+"_";
 		}
		String mainTemplate=head+data.getDataClassName()+"_"+data.getServletType();
 		map.put("mainTemplate", mainTemplate.toLowerCase()+".html");
 		
 		map.put("title", data.getFormData().getName()+" "+Internationals.getMessage(ValuesUtils.upperCamelToUnderbar(data.getServletType()).toLowerCase()));
 		
 		map.put("mainRowTemplate", mainTemplate.toLowerCase()+"_row"+".html");
 		map.put("dataClassName", data.getDataClassName());
 		
 		map.put("firstKey",data.getFormData().getFormFieldDatas().get(0).getKey());
 		
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
 			
 			
 			
 			if(data.getLastPackage().equals("admin")){
 			htmlTemplate=Bundles.INSTANCE.admin_list_html().getText();	
 			}else{
 			htmlTemplate=Bundles.INSTANCE.list_html().getText();
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
 					//TODO admin
 					htmlTemplate = Bundles.INSTANCE.show_html().getText();
 				} else {
 					htmlTemplate = Bundles.INSTANCE.show_html().getText();
 				}
 
 		}else if(type.equals(ServletData.TYPE_ADD)){
 			htmlTemplate=Bundles.INSTANCE.add_html().getText();
 			//create first td
 			Iterable<FormFieldData> datas=Iterables.filter(data.getFormData().getFormFieldDatas(), FormFieldDataPredicates.getNotAutoCreate());
 			Iterable<String> names=Iterables.transform(datas, FormFieldDataDto.getFormFieldToNameFunction());
 			//create second td
 			Iterable<Tag> inputTags=Iterables.transform(datas,FormFieldDataDto.getFormFieldToInputTagFunction());
 			Iterable<String> tagString=Iterables.transform(inputTags, new TagToString());
 			
 			//create tr
 			List<List<String>> vs=new ArrayList<List<String>>();
 			vs.add(Lists.newArrayList(names));
 			vs.add(Lists.newArrayList(tagString));
 			
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
 			
 			map.put("has_error_message", Internationals.getMessage("has_error"));
 			map.put("add_exec_title", Internationals.getMessage("add_exec"));
 		}else if(type.equals(ServletData.TYPE_ADD_EXEC)){
 			htmlTemplate=Bundles.INSTANCE.add_exec_html().getText();
 			map.put("has_error_message", Internationals.getMessage("has_error"));
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
 			Iterable<Tag> inputTags=Iterables.transform(datas,FormFieldDataDto.getFormFieldToInputTemplateTagFunction());
 			Iterable<String> tagString=Iterables.transform(inputTags, new TagToString());
 			
 			//create tr
 			List<List<String>> vs=new ArrayList<List<String>>();
 			vs.add(Lists.newArrayList(names));
 			vs.add(Lists.newArrayList(tagString));
 			
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
 			
 			map.put("has_error_message", Internationals.getMessage("has_error"));
 			map.put("edit_exec_title", Internationals.getMessage("edit_exec"));
 		}else if(type.equals(ServletData.TYPE_EDIT_EXEC)){
 			htmlTemplate=Bundles.INSTANCE.edit_exec_html().getText();
 			map.put("has_error_message", Internationals.getMessage("has_error"));
 			map.put("edit_complete_title", Internationals.getMessage("edit_complete"));
 			map.put("list_title", data.getFormData().getName()+" "+Internationals.getMessage("list"));
 		}else if(type.equals(ServletData.TYPE_DELETE_CONFIRM)){
 			htmlTemplate=Bundles.INSTANCE.delete_confirm_html().getText();
 			map.put("has_error_message", Internationals.getMessage("has_error"));
 			map.put("delete_exec_title", Internationals.getMessage("delete_exec"));
 		}else if(type.equals(ServletData.TYPE_DELETE_EXEC)){
 			
 			htmlTemplate=Bundles.INSTANCE.delete_exec_html().getText();
 			map.put("has_error_message", Internationals.getMessage("has_error"));
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
 		
 		map.put("show_title", Internationals.getMessage("show"));
 		map.put("add_title", Internationals.getMessage("add"));
 		map.put("edit_title", Internationals.getMessage("edit"));
 		map.put("delete_title", Internationals.getMessage("delete"));
 		map.put("reset_title", Internationals.getMessage("reset"));
 		
 		for(FileNameAndText fileText:files){
 			String text=TemplateUtils.createAdvancedText(fileText.getText(), map);
 			fileText.setText(text);
 		}
 		
 		return files;
 	}
 	
 	
 	
 }
 
 public static class TagToString implements Function<Tag,String>{
 
 	@Override
 	public String apply(Tag tag) {
 		if(tag.getName().equals("input") && tag.getAttbibutes().get("type").equals("text")){
 			tag.setAttribute("length", "40");
 		}
 		return tag.toString();
 	}
 	
 }
 
 }
