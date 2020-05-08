 package org.giavacms.twizz.service.timer;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.logging.Logger;
 
 import javax.annotation.Resource;
 import javax.ejb.LocalBean;
 import javax.ejb.Stateless;
 import javax.ejb.Timeout;
 import javax.ejb.Timer;
 import javax.ejb.TimerConfig;
 import javax.ejb.TimerService;
 import javax.inject.Inject;
 
 import org.giavacms.twizz.model.pojo.CallToMake;
 import org.twiliofaces.cdi.doers.Caller;
 
 @Stateless
 @LocalBean
 public class CallerTimerService
 {
 
    @Inject
    Caller twilioCaller;
 
    @Resource
    TimerService timerService;
 
    Logger logger = Logger.getLogger(getClass().getName());
 
    public void createTimer(CallToMake callToMake)
    {
       logger.info(callToMake.toString());
       Calendar calendar = Calendar.getInstance();
       calendar.setTime(callToMake.getWhen());
      timerService.createSingleActionTimer(callToMake.getWhen(), new TimerConfig(callToMake, true));
    }
 
    @Timeout
    public void timeout(Timer timer)
    {
       CallToMake callToMake = (CallToMake) timer.getInfo();
       logger.info(getClass().getName() + ": " + new Date() + " " + callToMake);
       try
       {
          String callId = twilioCaller
 
                   .setTo(callToMake.getTo())
                   .url(callToMake.getUrl()).call();
          logger.info("call id: " + callId);
       }
       catch (Throwable e)
       {
          e.printStackTrace();
       }
 
    }
 
 }
