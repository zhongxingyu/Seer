 package com.akjava.gwt.webappmaker.client;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.akjava.gwt.html5.client.file.ui.FileNameAndText;
 import com.akjava.gwt.webappmaker.client.resources.Bundles;
 import com.akjava.lib.common.form.FormData;
 import com.akjava.lib.common.form.FormFieldData;
 import com.akjava.lib.common.form.FormFieldDataPredicates;
 import com.akjava.lib.common.form.Validator;
 import com.akjava.lib.common.form.ValidatorTools;
 import com.akjava.lib.common.predicates.StringPredicates;
 import com.akjava.lib.common.utils.TemplateUtils;
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.gwt.core.client.GWT;
 
 public class ValidatorGenerator {
 	private FormData formData;
 	private String packageString;
 	public ValidatorGenerator(FormData formData,String packageString){
 		this.formData=formData;
 		this.packageString=packageString;
 	}
 	public static Joiner joiner=Joiner.on("\n").skipNulls();
 	public String generate(){
 
 		String base=Bundles.INSTANCE.validator().getText();
 
 		Map<String,String> map=new HashMap<String, String>();
 		map.put("package", packageString);
 		map.put("dataClassName",formData.getClassName());
 		
 		if(formData.getFormFieldDatas()!=null){
 			
 			Iterable<FormFieldData> notAutoDatas=Iterables.filter(formData.getFormFieldDatas(),FormFieldDataPredicates.getNotAutoCreate());
 			
 			//createAddKeyLabelMap
 			List<String> createAddKeyLabelMapText=Lists.newArrayList(
 					Iterables.filter(
 						     Iterables.transform(notAutoDatas,getKeyLabelMapFunction()),
 						     StringPredicates.getNotEmpty()
 						  )
 						  );
 			map.put("createAddKeyLabelMap",joiner.join(createAddKeyLabelMapText));
 			//createEditKeyLabelMap
 			List<String> createEditKeyLabelMapText=Lists.newArrayList(
 					Iterables.filter(
 						     Iterables.transform(formData.getFormFieldDatas(),getKeyLabelMapFunction()),
 						     StringPredicates.getNotEmpty()
 						  )
 						  );
 			map.put("createEditKeyLabelMap",joiner.join(createEditKeyLabelMapText));
 			
 			//createDataValidator
 			
 			List<String> createDataValidatorText=Lists.newArrayList(
 					Iterables.filter(
 						     Iterables.transform(formData.getFormFieldDatas(),getCreateDataValidatorFunction()),
 						     StringPredicates.getNotEmpty()
 						  )
 						  );
 			map.put("createDataValidator",joiner.join(createDataValidatorText));
 		}
 		return TemplateUtils.createAdvancedText(base, map);
 	}
 	
 	
 	public KeyLabelMapFunction getKeyLabelMapFunction(){
 		return KeyLabelMapFunction.INSTANCE;
 	}
 	public enum KeyLabelMapFunction implements Function<FormFieldData,String>{
 		INSTANCE
 		;
 		public static String template="keyLabelMap.put(\"${key}\", \"${name}\");";
 		@Override
 		public String apply(FormFieldData fdata) {
 			Map<String,String> map=new HashMap<String, String>();
 			map.put("key", fdata.getKey());
 			map.put("name",fdata.getName());
 			return TemplateUtils.createText(template, map);
 		}
 	}
 	
 	public CreateDataValidatorFunction getCreateDataValidatorFunction(){
 		return CreateDataValidatorFunction.INSTANCE;
 	}
 	public enum CreateDataValidatorFunction implements Function<FormFieldData,String>{
 		INSTANCE
 		;
 		public static String template="validators.put(\"${key}\", ValidatorTools.getValidator(\"${validator}\"));";
 		@Override
 		public String apply(FormFieldData fdata) {
			
 			if(fdata.getValidators()==null || fdata.getValidators().size()==0){
 				return null;
 			}
 			
 			List<String> validatorTexts=new ArrayList<String>();
 			Map<String,String> map=new HashMap<String, String>();
 			map.put("key", fdata.getKey());
 			
 			for(Validator validator:fdata.getValidators()){
 			map.put("validator",validator.toString());
 			validatorTexts.add(TemplateUtils.createText(template, map));
 			}
 			if(fdata.getType()==FormFieldData.TYPE_TEXT_SHORT){
 			if(!ValidatorTools.hasLimitValidator(fdata.getValidators())){
 				map.put("validator","max(512)");
 				validatorTexts.add(TemplateUtils.createText(template,map));
 			}
 			}
 			else if(fdata.getType()==FormFieldData.TYPE_TEXT_LONG){
 				if(!ValidatorTools.hasLimitValidator(fdata.getValidators())){
 					map.put("validator","maxb(1m)");
 					validatorTexts.add(TemplateUtils.createText(template,map));
 				}
 				}
 			else if(fdata.getType()==FormFieldData.TYPE_NUMBER){
 					map.put("validator","asciiNumber");
 					validatorTexts.add(TemplateUtils.createText(template,map));
 				}
 			
 			return joiner.join(validatorTexts);
 		}
 	}
 	
 	
 
 	
 	
 	public FileNameAndText createFileNameAndText(){
 		String generatorText=this.generate();
 		FileNameAndText ftext=new FileNameAndText();
 		ftext.setName(formData.getClassName()+"Validator.java");
 		ftext.setText(generatorText);
 		return ftext;
 	}
 	
 
 }
