 package org.omidbiz.renderkit;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Map;
 import java.util.TimeZone;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.faces.convert.ConverterException;
 import javax.faces.convert.DateTimeConverter;
 
 import org.ajax4jsf.renderkit.HeaderResourcesRendererBase;
 import org.omidbiz.component.UIDatePicker;
 
 
 public abstract class DatePickerRendererBase extends HeaderResourcesRendererBase {
 	
 	PersianCalendar pc= new PersianCalendar();
 	SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
 	SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");
 	SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	
 	public void decode(FacesContext context, UIComponent component) {
 		ExternalContext external = context.getExternalContext();
 		Map requestParams = external.getRequestParameterMap();
 		UIDatePicker inputDate = (UIDatePicker) component;
 		String clientId = inputDate.getClientId(context);
 		String submittedValue = (String) requestParams.get(clientId);
 		
 		if (submittedValue != null) {
 			//1390/6/30
 			//inputDate.setSubmittedValue(submittedValue);
			//change submitted value to date			
			
 			String gregorianDate = pc.SolarToGregorian(submittedValue);			
 			try {
				inputDate.setSubmittedValue(dateFormat.parse(gregorianDate));				
 			} catch (ParseException e) {
 				try {
					inputDate.setSubmittedValue(sdf.parse(gregorianDate));
 				} catch (ParseException e1) {
 					try {
 						inputDate.setSubmittedValue(dateTimeFormat.parse(gregorianDate));
 					} catch (ParseException e2) {
 						e2.printStackTrace();
 					}
 				}				
 			}
 		}
 	}
 
 	protected String getSolarValueAsString(FacesContext context,
 			UIComponent component) throws IOException {
 
 		UIDatePicker inputDate = (UIDatePicker) component;
 		String valueString = (String) inputDate.getSubmittedValue();
 
 		if (valueString == null) {
 			Object value = inputDate.getValue();
 			if (value != null) {				
 				String gDate = value.toString();				
 				try {
					valueString = pc.GregorianToSolar(sdf.format(dateFormat.parse(gDate)));
 				} catch (ParseException e) {
 					try {
						valueString = pc.GregorianToSolar(sdf.format(sdf.parse(gDate)));
 					} catch (ParseException e1) {
 						try {
							valueString = pc.GregorianToSolar(sdf.format(dateTimeFormat.parse(gDate)));
 						} catch (ParseException e2) {
 							e2.printStackTrace();
 						}
 					}
 					
 				}
 			}
 		}
 		return valueString;
 	}
 	
 	protected String getGregorianValueAsString(FacesContext context,
 			UIComponent component) throws IOException {
 
 		UIDatePicker inputDate = (UIDatePicker) component;
 		String valueString = (String) inputDate.getSubmittedValue();
 
 		if (valueString == null) {
 			Object value = inputDate.getValue();
 			if (value != null) {
 				Converter converter = getConverter(context, inputDate);
 				String convertedValue = converter.getAsString(context, component, value);
 				valueString = pc.SolarToGregorian(convertedValue);
 			}
 		}
 		return valueString;
 	}
 
 	//TODO needs to improve for cutomConvertor
 	public Object getConvertedValue(FacesContext context,
 			UIComponent component, Object submittedValue)
 			throws ConverterException {
 //		UIDatePicker inputDate = (UIDatePicker) component;
 //		Converter converter = getConverter(context, inputDate);
 //		String valueString = (String) submittedValue;
 //		return converter.getAsObject(context, component, valueString);
 		return super.getConvertedValue(context, component, submittedValue);
 	}
 
 	//TODO needs to improve
 	private Converter getConverter(FacesContext context, UIDatePicker inputDate) {
 		Converter converter = inputDate.getConverter();
 //		if (converter == null) {
 			DateTimeConverter datetime = new DateTimeConverter();
 			datetime.setLocale(context.getViewRoot().getLocale());
 			datetime.setTimeZone(TimeZone.getDefault());
 //			datetime.setType("date");
 //			datetime.setDateStyle("medium");
 			datetime.setPattern("yyyy/MM/dd");
 			converter = datetime;
 //		}
 		return converter;
 	}
 	
 	
 
 
 }
