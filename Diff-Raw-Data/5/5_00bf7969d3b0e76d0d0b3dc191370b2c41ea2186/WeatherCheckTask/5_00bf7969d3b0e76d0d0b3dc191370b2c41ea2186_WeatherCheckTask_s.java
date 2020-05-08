 package wfm.task;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.activiti.cdi.BusinessProcess;
 import org.activiti.engine.delegate.DelegateExecution;
 import org.activiti.engine.delegate.JavaDelegate;
 import org.fedy2.weather.YahooWeatherService;
 import org.fedy2.weather.data.Channel;
 import org.fedy2.weather.data.unit.DegreeUnit;
 
 import wfm.bean.User;
 import wfm.db.Course;
 
 @Named("weatherCheck")
 public class WeatherCheckTask implements JavaDelegate{
 	
 	@Inject
 	private BusinessProcess businessProcess;
 
 	@Inject
 	private Course course;
 	
 	@Inject
 	private User user;
 	
 	
 	public static String weoidVienna = "12591694";
 
 	@Override
 	public void execute(DelegateExecution execution) throws Exception {
 		
 		//http://developer.yahoo.com/weather/
 		//for more information on how the weather service works and the codes need to be interpreted
 		
 		YahooWeatherService service = new YahooWeatherService();		
 		Channel channel = service.getForecast(WeatherCheckTask.weoidVienna, DegreeUnit.CELSIUS);
 		//read the current weather condition
 		//if weather is bad... screw you guys im going home -->
 		int condition = channel.getItem().getCondition().getCode();
 		//http://developer.yahoo.com/weather/#codes
 		if (condition < 19 || condition >= 37 || condition == 35) {
 			//cancel course
			
 		}
 		
 	}
 
 }
