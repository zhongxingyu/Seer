 package com.akjava.lib.common.tag;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.common.collect.Lists;
 
 public class LabelAndValue {
 public LabelAndValue(String value){
 	this(null,value,false);
 }
 public LabelAndValue(String label,String value){
 	this(label,value,false);
 }
 public LabelAndValue(String label, String value,boolean selected) {
 	this.label=label;
 	this.value=value;
 	this.selected=selected;
 }
 private boolean selected;
 public boolean isSelected() {
 	return selected;
 }
 public void setSelected(boolean selected) {
 	this.selected = selected;
 }
 private String label;
 private String value;
 public String getLabel() {
 	return label;
 }
 public void setLabel(String label) {
 	this.label = label;
 }
 public String getValue() {
 	return value;
 }
 public void setValue(String value) {
 	this.value = value;
 }
 
 public String getPrintValue(){
 	if(label==null){
 		return value;
 	}else{
 		return label;
 	}
 }
 
 public String toString(){
 	String out="";
 	if(label!=null){
 		out=label+":"+value;
 	}else{
 		out=value;
 	}
 	if(selected){
 		out+=":"+true;
 	}
 	return out;
 }
 
 public static void selectValues(List<LabelAndValue> lvalues,String value,String splitValue){
 	if(value==null){
 		return;
 	}
 	List<String> multipleValue=null;
 	if(splitValue==null){
 		multipleValue=new ArrayList<String>();
 		multipleValue.add(value);
 	}else{
 		multipleValue= Lists.newArrayList(value.split(splitValue));
 	}
 
 	for (LabelAndValue lv : lvalues) {
 		for (String v : multipleValue) {
 			if (lv.getValue().equals(v)) {
 				lv.setSelected(true);
 			}
 		}
 	}
 }
 
 public String toOption(){
 	String option="<option";
 	String outValue=value;
 	String selectValue="";
 	if(selected){
 		selectValue=" selected";
 	}
 	if(outValue.indexOf('"')!=-1){
 		outValue=outValue.replace("\"", "&quot;");
 	}
 	if(label!=null){
 		option+=" value=\""+outValue+"\"";
 		option+=selectValue+">";
 		option+=label;
 	}else{
 		option+=selectValue+">"+value;
 	}
 
 	return option;
 }
 
 //for edit
 public LabelAndValue clone(){
 	return new LabelAndValue(label,value,selected);
 }
 
 }
