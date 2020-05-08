 package org.omidbiz.component.html;
 
 import javax.el.ELException;
 import javax.el.ValueExpression;
 import javax.faces.FacesException;
 import javax.faces.context.FacesContext;
 import org.omidbiz.component.UIDatePicker;
 
 public class HtmlDatePicker extends UIDatePicker{
 
static final public  String COMPONENT_FAMILY = "org.omidbiz.DatePicker";
 
static final public  String COMPONENT_TYPE = "org.omidbiz.DatePicker";
 
 /*
 * Default is : %Y/%m/%d
 			other options : 
 			%Y-%m-%d
 			%Y-%m-%d %I:%M %P
 			%Y-%m-%d %H:%M
 			%A, %e %B %Y
 			%A, %e %B %Y  %H:%M
 */
 private  String _datePattern = null;
 
 /*
 * defines date type including : 
 					jalali
 					gregorian
 					default is jalali
 */
 private  String _dateType = null;
 
 /*
 * true or false
 			Default is : false
 */
 private  Boolean _showOthers = null;
 
 /*
 * true or false
 			Default is : false
 			show weeknumbers
 */
 private  Boolean _weekNumbers = null;
 
 
 public HtmlDatePicker(){
 setRendererType("org.omidbiz.DatePickerRenderer");
 }
 
 public String getDatePattern(){
 	if (this._datePattern != null) {
 		return this._datePattern;
 	}
 	ValueExpression ve = getValueExpression("datePattern");
 	if (ve != null) {
 	    String value = null;
 	    
 	    try {
 			value = (String) ve.getValue(getFacesContext().getELContext());
 	    } catch (ELException e) {
 			throw new FacesException(e);
 	    }
 	    
 	    return value;
 	} 
 
     return "%Y/%m/%d";
 	
 
 }
 
 public void setDatePattern(String _datePattern){
 this._datePattern = _datePattern;
 }
 
 public String getDateType(){
 	if (this._dateType != null) {
 		return this._dateType;
 	}
 	ValueExpression ve = getValueExpression("dateType");
 	if (ve != null) {
 	    String value = null;
 	    
 	    try {
 			value = (String) ve.getValue(getFacesContext().getELContext());
 	    } catch (ELException e) {
 			throw new FacesException(e);
 	    }
 	    
 	    return value;
 	} 
 
     return "jalali";
 	
 
 }
 
 public void setDateType(String _dateType){
 this._dateType = _dateType;
 }
 
 public Boolean getShowOthers(){
 	if (this._showOthers != null) {
 		return this._showOthers;
 	}
 	ValueExpression ve = getValueExpression("showOthers");
 	if (ve != null) {
 	    Boolean value = null;
 	    
 	    try {
 			value = (Boolean) ve.getValue(getFacesContext().getELContext());
 	    } catch (ELException e) {
 			throw new FacesException(e);
 	    }
 	    
 	    return value;
 	} 
 
     return false;
 	
 
 }
 
 public void setShowOthers(Boolean _showOthers){
 this._showOthers = _showOthers;
 }
 
 public Boolean getWeekNumbers(){
 	if (this._weekNumbers != null) {
 		return this._weekNumbers;
 	}
 	ValueExpression ve = getValueExpression("weekNumbers");
 	if (ve != null) {
 	    Boolean value = null;
 	    
 	    try {
 			value = (Boolean) ve.getValue(getFacesContext().getELContext());
 	    } catch (ELException e) {
 			throw new FacesException(e);
 	    }
 	    
 	    return value;
 	} 
 
     return false;
 	
 
 }
 
 public void setWeekNumbers(Boolean _weekNumbers){
 this._weekNumbers = _weekNumbers;
 }
 
 public String getFamily(){
 return COMPONENT_FAMILY;
 }
 
 @Override
 public Object saveState(FacesContext context){
 Object [] state = new Object[5];
 state[0] = super.saveState(context);
 state[1] = _datePattern;
 state[2] = _dateType;
 state[3] = _showOthers;
 state[4] = _weekNumbers;
 return state;
 }
 
 @Override
 public void restoreState(FacesContext context, Object state){
 Object[] states = (Object[]) state;
 super.restoreState(context, states[0]);
 	_datePattern = (String)states[1];;
 		_dateType = (String)states[2];;
 		_showOthers = (Boolean)states[3];;
 		_weekNumbers = (Boolean)states[4];;
 	
 }
 
 }
