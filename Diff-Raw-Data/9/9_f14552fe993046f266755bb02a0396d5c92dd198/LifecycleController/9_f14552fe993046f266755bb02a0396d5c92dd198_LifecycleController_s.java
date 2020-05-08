 package ar.edu.unicen.ringo.mockapp.ui;
 
 import java.util.concurrent.ScheduledFuture;
 
 import javax.annotation.PostConstruct;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.TaskScheduler;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
 
 import ar.edu.unicen.ringo.mockapp.core.CallGeneratorTask;
 import ar.edu.unicen.ringo.mockapp.core.Configuration;
 
 /**
  * Starts/stops the invocation sampler
  */
 @Controller("lifecycle")
 @RequestMapping("/generator")
 public class LifecycleController {
 
 	@Autowired
 	private TaskScheduler scheduler;
 
 	@Autowired
 	private CallGeneratorTask generator;
 
 	@Autowired
 	private Configuration configuration;
 
 	private ScheduledFuture<?> future;
 
 	private MappingJackson2JsonView view = new MappingJackson2JsonView();
 
 	@PostConstruct
 	public void init() {
 		this.view.setExtractValueFromSingleKeyModel(true);
 	}
 
 	@ResponseBody
 	@RequestMapping("start")
 	public String start() {
 		if (future != null) {
 			throw new AlreadyInRequestedState();
 		}
 		future = scheduler.scheduleAtFixedRate(generator,
 				configuration.getSampleInterval());
 		return "Sampler successfully started";
 	}
 
 	@ResponseBody
 	@RequestMapping("stop")
 	public String stop() {
 		if (future == null) {
 			throw new AlreadyInRequestedState();
 		}
 		future.cancel(false);
 		return "Sampler successfully stopped";
 	}
 
 	@RequestMapping(value = "status", consumes = "*/*", produces = "application/jspn", method = RequestMethod.GET)
 	public ModelAndView status() {
 		ModelAndView mav = new ModelAndView(view, "config", configuration);
 		return mav;
 	}
 }
