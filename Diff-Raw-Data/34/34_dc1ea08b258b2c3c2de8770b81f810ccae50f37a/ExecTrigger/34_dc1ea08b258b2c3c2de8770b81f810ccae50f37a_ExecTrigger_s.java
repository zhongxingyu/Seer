 package org.graylog2.execalarmcallback.callback;
 
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
        ProcessBuilder pb;
        Map<String, String> env;
        int status;
 
        pb = new ProcessBuilder(this.cmd);
        // send stdin/err to the greylog logs
         pb.redirectOutput(Redirect.INHERIT).redirectError(Redirect.INHERIT);
 
        env = pb.environment();
         env.put("GL2_TOPIC", alarm.getTopic());
         env.put("GL2_DESCRIPTION", alarm.getDescription());
        env.put("GL2_MESSAGE_COUNT", alarm.getMessageCount().toString());
 
         try {
            Process process = pb.start();
         } catch (IOException e) {
             throw new AlarmCallbackException(String.format("Could not start alarm process: %s", e));
         }
 
        status = process.waitFor();
        if (status != 0) {
            throw new AlarmCallbackException(String.format("Command terminated with status %d", status));
        }
     }
 }
 // vim: set ts=8 sw=4 sts=4 et ai:
