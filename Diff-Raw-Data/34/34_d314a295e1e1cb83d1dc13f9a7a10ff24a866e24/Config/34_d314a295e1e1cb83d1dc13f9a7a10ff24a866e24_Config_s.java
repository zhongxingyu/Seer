 package no.uio.master.autoscale.config;
 
 import me.prettyprint.hector.api.Cluster;
 
 public class Config {
 	public Integer intervall_timer = 1; 
 	public Integer threshold_breach_limit = 10;
 	public Integer min_number_of_nodes = 1;
 	public Double min_memory_usage = 10.0;
 	public Double max_memory_usage = 90.0;
 	public Long min_free_disk_space = 60L;
 	public Long max_free_disk_space = 20000L;
 	public Cluster cluster = null;
 }
