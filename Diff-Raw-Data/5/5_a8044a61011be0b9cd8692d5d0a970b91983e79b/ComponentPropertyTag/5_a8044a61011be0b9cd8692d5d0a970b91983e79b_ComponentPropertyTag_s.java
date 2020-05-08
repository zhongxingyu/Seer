 package org.imirsel.nema.webapp.taglib;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.tagext.DynamicAttributes;
 import javax.servlet.jsp.tagext.SimpleTagSupport;
 
 import org.imirsel.nema.annotatons.parser.beans.BooleanDataTypeBean;
 import org.imirsel.nema.annotatons.parser.beans.DataTypeBean;
 import org.imirsel.nema.annotatons.parser.beans.DoubleDataTypeBean;
 import org.imirsel.nema.annotatons.parser.beans.IntegerDataTypeBean;
 import org.imirsel.nema.annotatons.parser.beans.StringDataTypeBean;
 import org.imirsel.nema.model.Property;
 
 /**This tag renders the component property
  *  -Added support for readonly and hidden
  * @author Amit Kumar
  *
  */
 public class ComponentPropertyTag  extends SimpleTagSupport implements DynamicAttributes{
 	private ArrayList<String> keys = new ArrayList<String>();
 	private ArrayList<String> values = new ArrayList<String>();
 	private Property property;
 	private String component;
 	private String DEFAULT_ROLE ="ROLE_USER";
 	private String[] roles = new String[]{DEFAULT_ROLE};
 	boolean canEdit = true;
 
 
 	// class and other attributes are dynamic
 	public void setDynamicAttribute(String uri, String localName, Object value)
 	throws JspException {
 		if(value==null|| localName==null){
 			return;
 		}
 		keys.add( localName );
 		values.add(value.toString());
 	}
 
 	// setter for the value attribute
 	public void setValue(Property property){
 		this.property=property;
 	}
 	
 	// the component instance uri
 	public void setComponent(String component) {
 		this.component = component;
 	}
 
 	public String getComponent() {
 		return component;
 	}
 	
 
 	public String[] getRoles() {
 		return roles;
 	}
 
 	public void setRoles(String[] roles) {
 		this.roles = roles;
 	}
 
 	// start of the tag
 	public void doTag() throws JspException {
 		JspWriter out = getJspContext().getOut();
 		StringWriter writer = new StringWriter();
 		StringWriter cssWriter = new StringWriter();
 		for( int i = 0; i < keys.size(); i++ ) {
 			String key = (String)keys.get( i );
 			Object value = values.get(i);
 			cssWriter.append(key+"='"+value+"' ");
 		}
 		String editRole= DEFAULT_ROLE;
 		List<DataTypeBean> ltb =this.property.getDataTypeBeanList();
 		String htmlWidget=null;
 		if(ltb.isEmpty()){
 			htmlWidget=createInputBox(property.getName(),"text",cssWriter.toString(),property.getDefaultValue(),property.getValue(),false);
 		}else{
 			DataTypeBean dataTypeBean = ltb.get(0);
 			editRole=dataTypeBean.getEditRole();
 			canEdit = allowedToEdit(editRole);
 			if(dataTypeBean instanceof StringDataTypeBean){
 				StringDataTypeBean sd = (StringDataTypeBean) dataTypeBean;
 				if(sd.getRenderer()==null){
 					if(sd.getValueList()!=null){
 						if(sd.getValueList().length>0){
 							htmlWidget=createSelectBox(property.getName(),cssWriter.toString(),property.getDefaultValue(),sd.getValueList(),sd.getValueList(),property.getValue(), sd.isHidden());
 						}else{
 							htmlWidget=createInputBox(property.getName(),"text",cssWriter.toString(),property.getDefaultValue(),property.getValue(), sd.isHidden());
 						}
 					}else{
 						htmlWidget=createInputBox(property.getName(),"text",cssWriter.toString(),property.getDefaultValue(),property.getValue(), sd.isHidden());
 					}
 				}else if(sd.getRenderer().endsWith("FileRenderer")){
 					htmlWidget=createInputBox(property.getName(),"file",cssWriter.toString(),property.getDefaultValue(),property.getValue(), sd.isHidden());
 				}else if(sd.getRenderer().endsWith("CollectionRenderer")){
 					// this is a collection renderer...
 					if(property.getValueList().length>0){
 						htmlWidget=createSelectBox(property.getName(),cssWriter.toString(),property.getDefaultValue(),property.getLabelList(),property.getValueList(),property.getValue(), sd.isHidden());
 					}else{
 						// value list size is 0, let's display the input box...
 						htmlWidget=createInputBox(property.getName(),"text",cssWriter.toString(),property.getDefaultValue(),property.getValue(), sd.isHidden());
 					}
 			
 				}else{ // create input box for all others....
 					htmlWidget=createInputBox(property.getName(),"text",cssWriter.toString(),property.getDefaultValue(),property.getValue(), sd.isHidden());
 				}
 
 
 			}else if(dataTypeBean instanceof BooleanDataTypeBean){
 				BooleanDataTypeBean sd = (BooleanDataTypeBean) dataTypeBean;
 				htmlWidget=createRadioBox(property.getName(),cssWriter.toString(),property.getDefaultValue(),property.getValue(), sd.isHidden());
 			}else if(dataTypeBean instanceof DoubleDataTypeBean){
 				DoubleDataTypeBean sd = (DoubleDataTypeBean)dataTypeBean;
 				if(sd.getValueList()!=null){
 					if(sd.getValueList().length>0){
 						htmlWidget=createSelectBox(property.getName(),cssWriter.toString(),property.getDefaultValue(), sd.getValueList(),property.getValue(), sd.isHidden());
 					}else{
 						htmlWidget=createInputBox(property.getName(),"text",cssWriter.toString(),property.getDefaultValue(),property.getValue(), sd.isHidden());
 					}
 				}else{
 				htmlWidget=createInputBox(property.getName(),"text",cssWriter.toString(),property.getDefaultValue(),property.getValue(), sd.isHidden());
 				}
 			}else if(dataTypeBean instanceof IntegerDataTypeBean){
 				IntegerDataTypeBean sd = (IntegerDataTypeBean)dataTypeBean;
 				if(sd.getValueList()!=null){
 					if(sd.getValueList().length>0){
 					htmlWidget=createSelectBox(property.getName(),cssWriter.toString(),property.getDefaultValue(), sd.getValueList(),property.getValue(), sd.isHidden());
 					}else{
 					htmlWidget=createInputBox(property.getName(),"text",cssWriter.toString(),property.getDefaultValue(),property.getValue(), sd.isHidden());
 					}
 				}else{
 					htmlWidget=createInputBox(property.getName(),"text",cssWriter.toString(),property.getDefaultValue(),property.getValue(), sd.isHidden());
 				}
 			}else{
 				htmlWidget=createInputBox(property.getName(),"text",cssWriter.toString(),property.getDefaultValue(),property.getValue(), false);
 			}
 		}
 		
 		writer.append(htmlWidget);
 
 
 		try {
 			out.println(writer.toString());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 
 	}
 
 	private boolean allowedToEdit(String editRole) {
 		if(this.roles==null){
 			return false;
 		}
 		for(String role:this.roles){
 			if(role.equalsIgnoreCase(editRole)){
 				return true;
 			}
 		}
 		// if this is an administrator -show them whatever
 		for(String role:this.roles){
 			if(role.equalsIgnoreCase("ROLE_ADMIN")){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private String createSelectBox(String name, String styleString,
 			Object defaultValue, String[] labelList,Object[] valueList, String value, boolean hidden) {
 		if(hidden){
 			return " ";
 		}
 		
 		StringWriter swriter = new StringWriter();
 		if(value!=null){
 			if(value.length()>0){
 				defaultValue = value;
 			}
 		}
 		swriter.append("<select name='"+getName(name)+"' "+styleString);
 		if(!canEdit){
 			swriter.append(" disabled=\'disabled\'");
 		}
 		swriter.append(">");
 
 		for(int i=0;i<valueList.length;i++){
 			if(defaultValue==null){
 				swriter.append("<option value='"+valueList[i]+"'>"+valueList[i]+"</option>");
 			}else{
 				if(defaultValue.toString().equalsIgnoreCase(valueList[i].toString())){
 					swriter.append("<option selected='selected' value='"+valueList[i]+"'>"+labelList[i]+"</option>");
 				}else{
 					swriter.append("<option value='"+valueList[i]+"'>"+labelList[i]+"</option>");
 				}
 			}
 		}
 		swriter.append("</select>");
 		return swriter.toString();
 	}
 	
 
 	private String createSelectBox(String name, String styleString,
 			Object defaultValue, double[] valueList, String value, boolean hidden) {
 		if(hidden){
 			return " ";
 		}
 	
 		StringWriter swriter = new StringWriter();
 		if(value!=null){
 			if(value.length()>0){
 				defaultValue = value;
 			}
 		}
 		swriter.append("<select name='"+getName(name)+"' "+styleString);
 		if(!canEdit){
 			swriter.append(" disabled=\'disabled\'");
 		}
 		swriter.append(">");
 		for(int i=0;i<valueList.length;i++){
 			if(defaultValue==null){
 				swriter.append("<option value='"+valueList[i]+"'>"+valueList[i]+"</option>");
 			}else{
 				if(Double.parseDouble(defaultValue.toString()) == valueList[i]){
 					swriter.append("<option selected='selected' value='"+valueList[i]+"'>"+valueList[i]+"</option>");
 				}else{
 					swriter.append("<option value='"+valueList[i]+"'>"+valueList[i]+"</option>");
 				}
 			}
 				
 		}
 		swriter.append("</select>");
 		return swriter.toString();
 	}
 	
 	private String createSelectBox(String name, String styleString,
 			Object defaultValue, int[] valueList, String value, boolean hidden) {
 		if(hidden){
 			return " ";
 		}
 	
 		StringWriter swriter = new StringWriter();
 		if(value!=null){
 			if(value.length()>0){
 				defaultValue = value;
 			}
 		}
 		swriter.append("<select name='"+getName(name)+"' "+styleString);
 		if(!canEdit){
 			swriter.append(" disabled=\'disabled\'");
 		}
 		swriter.append(">");
 
 		for(int i=0;i<valueList.length;i++){
 			if(defaultValue==null){
 				swriter.append("<option value='"+valueList[i]+"'>"+valueList[i]+"</option>");
 			}else {
 				if(Integer.parseInt(defaultValue.toString()) == valueList[i]){
 					swriter.append("<option selected='selected' value='"+valueList[i]+"'>"+valueList[i]+"</option>");
 				}else{
 					swriter.append("<option value='"+valueList[i]+"'>"+valueList[i]+"</option>");
 				}
 			}
 		}
 		swriter.append("</select>");
 		return swriter.toString();
 	}
 	
 
 	private String createRadioBox(String name, String styleString,	Object defaultValue, String value, boolean hidden) {
 		if(hidden){
 			return " ";
 		}
 	
 		StringWriter swriter = new StringWriter();
 		if(value!=null){
 			if(value.length()>0){
 				defaultValue = value;
 			}
 		}
 		swriter.append("<input type='radio' name='"+getName(name)+"' "+ styleString);
 		if(defaultValue!=null){
 			if(defaultValue.toString().equals("true")){
 				swriter.append(" checked='checked' ");
 			}
 		}
 		if(!canEdit){
 			swriter.append(" disabled=\'disabled\'");
 		}
 		
 		swriter.append(" value='true'>true</input>");
 		swriter.append("<input type='radio' name='"+getName(name)+"' "+ styleString);
 		if(defaultValue!=null){
 			if(defaultValue.toString().equals("false")){
 				swriter.append(" checked='checked' ");
 			}
 		}
 		if(!canEdit){
 			swriter.append(" disabled=\'disabled\'");
 		}
 		swriter.append(" value='false'>false</input>");
 		return swriter.toString();
 	}
 
 	private String createInputBox(String name, String type, String styleString,
 			Object defaultValue, String value, boolean hidden) {
 		
 		if(hidden){
 			return " ";
 		}
 		
 		if(value!=null){
 			if(value.length()>0){
 				defaultValue = value;
 			}
 		}
 		StringWriter swriter = new StringWriter();
 		swriter.append("<input type='"+type+"' name='"+getName(name)+"' "+ styleString);
 		if(defaultValue!=null){
 			swriter.append(" value='"+defaultValue.toString()+"' ");
 		}
 		if(!canEdit){
 			swriter.append(" disabled=\'disabled\'");
 		}
 		swriter.append("/>");
 		return swriter.toString();
 	}
 
 
 	private String getName(String propertyName){
 		if(this.component==null){
 			return propertyName;
 		}
 		int index = this.component.lastIndexOf("/");
 		if(index==-1){
 			return this.component+"_"+propertyName;
 		}
 		int second = this.component.substring(0, index).lastIndexOf("/");
 		String cname=this.component.substring(second+1,index);
 		String count = this.component.substring(index+1);
 		return cname+"_"+count+"_"+propertyName;
 	}
 
 
 }
