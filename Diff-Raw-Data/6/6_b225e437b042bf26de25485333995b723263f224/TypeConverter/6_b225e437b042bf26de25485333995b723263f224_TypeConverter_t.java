 package com.camilolopes.readerweb.converters;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.camilolopes.readerweb.model.bean.Type;
 import com.camilolopes.readerweb.services.interfaces.TypeServiceImpl;
 

 @Component("typeConverter")
 public class TypeConverter implements Converter {
 	@Autowired
 	private TypeServiceImpl typeServiceImpl;
 	@Override
 	public Object getAsObject(FacesContext context, UIComponent component, String value) {

 		Type type = typeServiceImpl.searchById(Long.valueOf(value));
 		return type;
 	}
 
 	@Override
 	public String getAsString(FacesContext arg0, UIComponent arg1, Object value) {
 			if(value!=null){
 				return ((Type)value).getId().toString();
 			}
 		return null;
 	}
 
 	public TypeServiceImpl getTypeServiceImpl() {
 		return typeServiceImpl;
 	}
 
 	public void setTypeServiceImpl(TypeServiceImpl typeServiceImpl) {
 		this.typeServiceImpl = typeServiceImpl;
 	}
 
 
 }
