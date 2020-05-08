 package org.tcrun.slickij.api.data;
 
 import com.google.code.morphia.annotations.Entity;
 import com.google.code.morphia.annotations.Id;
 import com.google.code.morphia.annotations.Property;
 import com.google.code.morphia.annotations.Reference;
 import java.io.Serializable;
 import java.util.Date;
 
 /**
  *
  * @author jcorbett
  */
 @Entity("hoststatus")
 public class HostStatus implements Serializable
 {
 	@Id
 	private String hostname;
 
 	@Property
 	private Date lastCheckin;
 
 	@Reference
 	private Result currentWork;
 
 	public Result getCurrentWork()
 	{
 		return currentWork;
 	}
 
 	public void setCurrentWork(Result currentWork)
 	{
 		this.currentWork = currentWork;
 	}
 
 	public String getHostname()
 	{
 		return hostname;
 	}
 
 	public void setHostname(String hostname)
 	{
 		this.hostname = hostname;
 	}
 
 	public Date getLastCheckin()
 	{
 		return lastCheckin;
 	}
 
 	public void setLastCheckin(Date lastCheckin)
 	{
 		this.lastCheckin = lastCheckin;
 	}
 }
