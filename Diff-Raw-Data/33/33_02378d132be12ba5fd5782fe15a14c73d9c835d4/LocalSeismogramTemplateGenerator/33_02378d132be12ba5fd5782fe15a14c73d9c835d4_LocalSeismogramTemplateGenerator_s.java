 /**
  * LocalSeismogramTemplateGenerator.java
  * 
  * @author Created by Philip Oliver-Paull
  */
 package edu.sc.seis.sod.process.waveform;
 
 import java.io.File;
 import java.util.Properties;
 import org.apache.log4j.Logger;
 import org.apache.velocity.app.VelocityEngine;
 import org.apache.velocity.runtime.RuntimeConstants;
 import org.w3c.dom.Element;
 import edu.iris.Fissures.IfEvent.EventAccessOperations;
 import edu.iris.Fissures.IfNetwork.Channel;
 import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
 import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
 import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
 import edu.sc.seis.sod.CookieJar;
 import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.EventFormatter;
 import edu.sc.seis.sod.status.FileWritingTemplate;
import edu.sc.seis.sod.status.StationFormatter;
 import edu.sc.seis.sod.status.StringTreeLeaf;
 import edu.sc.seis.sod.status.TemplateFileLoader;
 import edu.sc.seis.sod.status.waveformArm.LocalSeismogramTemplate;
 
 public class LocalSeismogramTemplateGenerator implements WaveformProcess {
 
     private LocalSeismogramTemplate template;
 
     private SeismogramImageProcess seismoImageProcess;
 
    private String fileDir = FileWritingTemplate.getBaseDirectoryName();
 
     private String fileName;
 
    private EventFormatter eventFormatter;

    private StationFormatter stationFormatter;

     private static Logger logger = Logger.getLogger(LocalSeismogramTemplateGenerator.class);
 
     public LocalSeismogramTemplateGenerator(Element el) throws Exception {
         Element waveformSeismogramConfig = TemplateFileLoader.getTemplate(el);
         Element outputLocEl = SodUtil.getElement(waveformSeismogramConfig,
                                                  "outputLocation");
        SeismogramImageOutputLocator out = SeismogramImageOutputLocator.createForLocalSeismogramTemplate(outputLocEl);
         waveformSeismogramConfig.removeChild(outputLocEl);
         seismoImageProcess = new SeismogramImageProcess(out);
         Element fileEl = SodUtil.getElement(waveformSeismogramConfig,
                                             "filename");
         if(fileEl != null) {
             fileName = fileEl.getFirstChild().getNodeValue();
             waveformSeismogramConfig.removeChild(fileEl);
         }
         if(waveformSeismogramConfig == null) { throw new IllegalArgumentException("The configuration requires a template"); }
         if(fileName != null) {
             template = new LocalSeismogramTemplate(waveformSeismogramConfig,
                                                   fileDir + "/");
         }
     }
 
     public static VelocityEngine getVelocity() {
         return velocity;
     }
 
     static VelocityEngine velocity = null;
     static {
         try {
             velocity = new VelocityEngine();
             String loggerName = "Velocity";
             Properties props = new Properties();
             props.put("resource.loader", "class");
             props.put("class.resource.loader.description",
                       "Velocity Classpath Resource Loader");
             props.put("class.resource.loader.class",
                       "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
             props.put(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                       "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
             props.put("runtime.log.logsystem.log4j.category", loggerName);
             props.put("velocimacro.library", "");
             velocity.init(props);
         } catch(Throwable t) {
             GlobalExceptionHandler.handle("Problem initializing Velocity", t);
         }
     }
 
     public WaveformResult process(EventAccessOperations event,
                                   Channel channel,
                                   RequestFilter[] original,
                                   RequestFilter[] available,
                                   LocalSeismogramImpl[] seismograms,
                                   CookieJar cookieJar) throws Exception {
         logger.debug("process() called");
         if(seismoImageProcess != null) {
             seismoImageProcess.process(event,
                                        channel,
                                        original,
                                        available,
                                        seismograms,
                                        cookieJar);
         } else {
             logger.debug("There was no picname in config.  I am not generating pictures.");
         }
         if(fileName != null) {
            template.update(getOutputLocation(event, channel), cookieJar);
         } else {
             logger.debug("There was no fileName in config. I am not generating html pages.");
         }
         return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
     }
 
     public File getOutputFile(EventAccessOperations event, Channel chan) {
        return new File(new File(fileDir), getOutputLocation(event, chan));
     }
 
     /** this is relative to the status directory */
    public String getOutputLocation(EventAccessOperations event, Channel chan) {
        return eventFormatter.getResult(event) + '/'
                + stationFormatter.getResult(chan.my_site.my_station) + '/'
                + fileName;
     }
 
     public SeismogramImageProcess getSeismogramImageProcess() {
         return seismoImageProcess;
     }
 }
