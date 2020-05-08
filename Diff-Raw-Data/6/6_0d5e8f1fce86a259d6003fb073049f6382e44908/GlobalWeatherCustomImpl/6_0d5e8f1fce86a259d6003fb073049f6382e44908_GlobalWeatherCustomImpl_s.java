 
 package jsr181.jaxb.globalweather;
 
 import javax.jws.WebService;
 import javax.jws.soap.SOAPBinding;
 import javax.jws.soap.SOAPBinding.ParameterStyle;
 import javax.jws.soap.SOAPBinding.Style;
 import javax.jws.soap.SOAPBinding.Use;
 
import org.codehaus.xfire.fault.XFireFault;

 @WebService(serviceName = "GlobalWeather", targetNamespace = "http://www.webserviceX.NET", endpointInterface = "jsr181.jaxb.globalweather.GlobalWeatherSoap")
 @SOAPBinding(style = Style.DOCUMENT, use = Use.LITERAL, parameterStyle = ParameterStyle.WRAPPED)
 public class GlobalWeatherCustomImpl
     implements GlobalWeatherSoap
 {
 
     public String GetCitiesByCountry(String CountryName)
        throws XFireFault
     {
         return null;
     }
 
     public String GetWeather(String CityName, String CountryName)
        throws XFireFault
     {
         return "foo";
     }
 
 
 }
