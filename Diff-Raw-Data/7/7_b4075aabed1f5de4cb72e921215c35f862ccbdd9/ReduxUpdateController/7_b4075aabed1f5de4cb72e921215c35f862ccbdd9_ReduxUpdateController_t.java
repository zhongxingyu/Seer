 package org.atlasapi.remotesite.redux;
 
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.remotesite.redux.ReduxDayUpdateTask.Builder;
 import org.atlasapi.remotesite.redux.model.FullReduxProgramme;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.google.common.util.concurrent.ThreadFactoryBuilder;
 import com.metabroadcast.common.base.Maybe;
 
 @Controller
 public class ReduxUpdateController {
 
     private final ReduxProgrammeHandler handler;
     private final ReduxClient client;
 
     private final Builder taskBuilder;
     private final DateTimeFormatter isoParser;
     private final ExecutorService executor;
     
     public ReduxUpdateController(ReduxClient client, ReduxProgrammeHandler handler, AdapterLog log) {
         this.client = client;
         this.handler = handler;
         this.taskBuilder = ReduxDayUpdateTask.dayUpdateTaskBuilder(client, handler, log);
         this.isoParser = DateTimeFormat.forPattern("yyyyMMdd");
         this.executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Redux Manual Updater %s").build());
     }
     
     @RequestMapping(value="/system/update/redux/day/{date}", method=RequestMethod.POST)
    public void updateDay(HttpServletResponse response, @PathVariable("date") String date){
         
         executor.submit(taskBuilder.updateFor(isoParser.parseDateTime(date).toLocalDate()));
         
     }
     
    @RequestMapping(value="/system/update/redux/diskref/{dr}", method=RequestMethod.POST)
    public void updateDiskref(HttpServletResponse response, @PathVariable("dr") final String diskRef) {
         
         executor.submit(new Runnable() {
             @Override
             public void run() {
                 Maybe<FullReduxProgramme> possibleProgramme = client.programmeFor(diskRef);
                 if(possibleProgramme.hasValue()) {
                     handler.handle(possibleProgramme.requireValue());
                 }
             }
         });
         
     }
     
 }
