 package pk.ip.weather.web.controller;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.format.annotation.DateTimeFormat;
 import org.springframework.format.annotation.DateTimeFormat.ISO;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;
 import pk.ip.weather.core.DateInterval;
 import pk.ip.weather.domain.City;
 import pk.ip.weather.domain.Forecast;
 import pk.ip.weather.service.Period;
 import pk.ip.weather.service.StatisticsType;
 import pk.ip.weather.service.WeatherService;
 import pk.ip.weather.task.DataCollectorTask;
 
 @Controller()
 public class WeatherController implements ApplicationContextAware
 {
     DataCollectorTask task;
     WeatherService weaterService;
     ApplicationContext context;
     
     public WeatherController(DataCollectorTask task, WeatherService weaterService)
     {
         this.task = task;
         this.weaterService = weaterService;
     }
 
    @RequestMapping("/history/{dateStart}/{dateEnd}/{cityId}/{type}/{period}")
    public ModelAndView findHistoricalStatistics(@PathVariable("dateStart") @DateTimeFormat(iso=ISO.DATE) Date dateStart, @PathVariable("dateEnd") @DateTimeFormat(iso=ISO.DATE) Date dateEnd, @PathVariable("cityId") Long cityId, @PathVariable("type") StatisticsType type, @PathVariable("period") Period period) throws NoSuchRequestHandlingMethodException
     {
        City city = weaterService.findCity(cityId);

         Map<String, Float> results = weaterService.findHistoricalData(city, new DateInterval(dateStart, dateEnd), type, period);
         
         return createModelAndView(results);
     }
     
     private ModelAndView createModelAndView(Object results)
     {
         ModelAndView mav = new ModelAndView();
         mav.setView(context.getBean("jsonView", View.class));
         mav.addObject("responseCode", 200);
         mav.addObject("results", results);
         return mav;
     }
     
     @RequestMapping("/cities")
     public ModelAndView findCities()
     {
         List<City> cities = weaterService.findCities();
         
         return createModelAndView(cities.toArray());
     }
     
    @RequestMapping("/forecast/{cityId}")
    public ModelAndView findForecast(@PathVariable("cityId") Long cityId) throws NoSuchRequestHandlingMethodException
     {
        City city = weaterService.findCity(cityId);
        
         List<Forecast> forecasts = weaterService.findForecast(city);
 
         return createModelAndView(forecasts.toArray());
     }
 
     @Override
     public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
     {
         context = applicationContext;
     }
     
 //    @RequestMapping("/test/{date}")
 //    public void test(@PathVariable("date") @DateTimeFormat(iso=ISO.DATE) Date date)
 //    {
 //        task.collectWeaterHistory(date);
 //    }
 }
