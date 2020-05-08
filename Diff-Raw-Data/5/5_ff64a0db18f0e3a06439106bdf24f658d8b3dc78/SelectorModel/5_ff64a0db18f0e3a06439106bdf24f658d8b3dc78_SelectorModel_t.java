 //==============================================================================
 // file :       $Id:SelectorModel.java 2023 2006-10-17 05:18:45Z jcai $
 // project:     corner
 //
 // last change: date:       $Date:2006-10-17 05:18:45Z $
 //              by:         $Author:jcai $
 //              revision:   $Revision:2023 $
 //------------------------------------------------------------------------------
 //copyright:	Beijing Maxinfo Technology Ltd. http://www.bjmaxinfo.com
 //License:      the Apache License, Version 2.0 (the "License")
 //==============================================================================
 
 package corner.orm.tapestry.component.select;
 
 import java.util.Map;
 
 import org.apache.tapestry.IComponent;
 import org.apache.tapestry.services.DataSqueezer;
 
 import corner.service.EntityService;
 import corner.util.BeanUtils;
 
 /**
  * 实现一个po的自动完成模型.
  * @author <a href="mailto:jun.tsai@bjmaxinfo.com">Jun Tsai</a>
  * @author ghostbb
  * @version $Revision:2023 $
  * @since 2.2.1
  */
 public class SelectorModel implements IPoSelectorModel {
 	
 	Class poClass;
 	String field;
 	EntityService entityService;
 	private boolean saveObj;
 	DataSqueezer squeezer;
 	ISelectFilter filter;
 	
 	String[] returnValueFileds;
 	String [] updateFields;
 	private IComponent nestComp;
 	
 	/**
 	 * @param field The field to set.
 	 */
 	public void setLabelField(String field) {
 		this.field = field;
 	}
 	public void setEntityService(EntityService entityService){
 		this.entityService=entityService;
 	}
 	public void setDataSqueezer(DataSqueezer squeezer){
 		this.squeezer=squeezer;
 	}
 	
 	public void setReturnValueFields(String ...strings){
 		this.returnValueFileds=strings;
 	}
 	public String[] getReturnValueFields(){
 		return this.returnValueFileds;
 	}
 	/**
 	 * @param poClass The poClass to set.
 	 */
 	public void setPoClass(Class poClass) {
 		this.poClass = poClass;
 	}
 	public void setSelectFilter(ISelectFilter filter){
 		this.filter=filter;
 	}
 
 	/**
 	 * 
 	 * @see org.apache.tapestry.dojo.form.IAutocompleteModel#filterValues(java.lang.String)
 	 */
 	public Map filterValues(final String match) {
 		if(this.filter==null){
 			filter=new DefaultSelectFilter();
 		}
 		if(this.getReturnValueFields()==null&&this.getLabelField()!=null){
 			this.setReturnValueFields(this.getLabelField());
 		}
 		return filter.query(match,this);
 	}
 	
 
 	/**
 	 * 
 	 * @see org.apache.tapestry.dojo.form.IAutocompleteModel#getLabelFor(java.lang.Object)
 	 */
 	public String getLabelFor(Object value) {
 		if(this.entityService.isPersistent(value)){
 			boolean fieldIsNull=(field==null);
 			boolean filterFieldIsNull=false;
 			if(filter!=null)
 				filterFieldIsNull=(filter.getLabelField()==null);
 			
 			
 			if(fieldIsNull){
 				if(!filterFieldIsNull){
					return BeanUtils.getProperty(value, filter.getLabelField())!=null?BeanUtils.getProperty(value, filter.getLabelField()).toString():null;
 				}
 				
 				return null;
 				
 			}
 			
			return BeanUtils.getProperty(value, field)!=null?BeanUtils.getProperty(value, field).toString():null;
 		}
 		return value.toString();
 	}
 
 	/**
 	 * 
 	 * @see org.apache.tapestry.components.IPrimaryKeyConverter#getPrimaryKey(java.lang.Object)
 	 */
 	public Object getPrimaryKey(Object obj) {
 		return obj;
 	}
 	/**
 	 * 
 	 * @see org.apache.tapestry.components.IPrimaryKeyConverter#getValue(java.lang.Object)
 	 */
 	public Object getValue(Object key) {
 		return key;
 	}
 	/**
 	 * @see corner.orm.tapestry.component.select.IPoSelectorModel#isSaveObj()
 	 */
 	public boolean isSaveObj() {
 		return saveObj;
 	}
 	/**
 	 * @param saveObj The saveObj to set.
 	 */
 	public void setSaveObj(boolean saveObj) {
 		this.saveObj = saveObj;
 	}
 	/**
 	 * @see corner.orm.tapestry.component.select.IPoSelectorModel#getSqueezer()
 	 */
 	public DataSqueezer getSqueezer() {
 		return squeezer;
 	}
 	/**
 	 * @param squeezer The squeezer to set.
 	 */
 	public void setSqueezer(DataSqueezer squeezer) {
 		this.squeezer = squeezer;
 	}
 	/**
 	 * @see corner.orm.tapestry.component.select.IPoSelectorModel#getEntityService()
 	 */
 	public EntityService getEntityService() {
 		return entityService;
 	}
 	
 	/**
 	 * @see corner.orm.tapestry.component.select.IPoSelectorModel#getLabelField()
 	 */
 	public String getLabelField() {
 		return field;
 	}
 	/**
 	 * @see corner.orm.tapestry.component.select.IPoSelectorModel#getPoClass()
 	 */
 	public Class getPoClass() {
 		return poClass;
 	}
 	public IComponent getComponent() {
 		return this.nestComp;
 	}
 	public void setComponent(IComponent c){
 		this.nestComp=c;
 	}
 	public String[] getUpdateFields() {
 		return updateFields;
 	}
 	public void setUpdateFields(String[] updateFields) {
 		this.updateFields = updateFields;
 	}
 	
 	
 }
