 package de.otto.jobstore.common;
 
 import javax.xml.bind.annotation.XmlRootElement;
 import java.util.List;
 
 @XmlRootElement
 public final class RemoteJobStatus {
 
     public static enum Status {
         RUNNING,
         FINISHED
     }
 
     public Status status;
    public List<String> logLines;
     public RemoteJobResult result;
     public String finishTime;
 
    public RemoteJobStatus() {}
 
     public RemoteJobStatus(Status status, List<String> logLines, RemoteJobResult result, String finishTime) {
         this.status = status;
         this.logLines = logLines;
         this.result = result;
         this.finishTime = finishTime;
     }
 
 }
