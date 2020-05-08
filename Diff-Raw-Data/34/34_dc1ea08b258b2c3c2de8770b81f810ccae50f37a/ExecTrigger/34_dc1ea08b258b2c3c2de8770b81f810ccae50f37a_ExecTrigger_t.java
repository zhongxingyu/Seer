 package org.graylog2.execalarmcallback.callback;
 
import java.io.File;
 import java.io.IOException;
 import java.lang.Process;
 import java.lang.ProcessBuilder;
 import java.lang.ProcessBuilder.Redirect;
 import java.util.Map;
 
 import org.graylog2.plugin.alarms.Alarm;
 import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
 
 /**
  * @author Harm Geerts <hgeerts@osso.nl>
  */
 public class ExecTrigger {
     private String cmd;
 
     public ExecTrigger(String cmd) {
         this.cmd = cmd;
     }
 
     public void trigger(Alarm alarm) throws AlarmCallbackException {
        String[] cmd = new String[]{"/bin/sh", "-c", this.cmd};
        ProcessBuilder pb = new ProcessBuilder(cmd);
        Map<String, String> env = pb.environment();
 
        // send stdin/err to the graylog logs
         pb.redirectOutput(Redirect.INHERIT).redirectError(Redirect.INHERIT);
        // change workdir to /tmp
        pb.directory(new File("/tmp"));
 
         env.put("GL2_TOPIC", alarm.getTopic());
         env.put("GL2_DESCRIPTION", alarm.getDescription());
        // not supported by our graylog server
        //env.put("GL2_MESSAGE_COUNT", Integer.toString(alarm.getMessageCount()));
 
         try {
            Process p = pb.start();
         } catch (IOException e) {
             throw new AlarmCallbackException(String.format("Could not start alarm process: %s", e));
         }
 
        // 
        //int status = p.waitFor();
        //if (status != 0) {
        //    throw new AlarmCallbackException(String.format("Command terminated with status %d", status));
        //}
     }
 }
 // vim: set ts=8 sw=4 sts=4 et ai:
