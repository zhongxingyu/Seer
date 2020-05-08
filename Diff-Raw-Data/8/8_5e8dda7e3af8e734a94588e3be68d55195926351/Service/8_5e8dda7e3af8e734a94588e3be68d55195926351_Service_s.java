 /**
  * 
  */
 package uk.org.vacuumtube.rrd4j;
 
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  * @author clivem
  *
  */
 public class Service {
 	
 	public final static long KILO_MULT = 1000;
 	
 	public final static String XL30_3 = "XL_30_3";
 	public final static String XXL50_5 = "XXL_50_5";
 	public final static String XL60_6 = "XL_60_6";
 	public final static String XL100_5 = "XL_100_5";
 	public final static String XL100_10 = "XL_100_10";
 
 	private final static Map<String, Service> SERVICES = new TreeMap<String, Service>();
 
 	private String serviceName;
 	private int connectionSpeedDownMbps;
 	private int connectionSpeedUpMbps;
 	private Map<String, ServicePeriod> serviceStmPeriodMap;
 
     static {
     	/*
 		 * XL_30_3
 		 */
     	new Service(XL30_3, 30, 3,
     			ServicePeriod.createMap(new ServicePeriod[] {
     					new ServicePeriod("DAY", 0, 24,
     			    			StmProfile.createMap(new StmProfile[] {
     			    					new StmProfile(Direction.DOWN, -1, 0, 0), 
     			    					new StmProfile(Direction.UP, -1, 0, 0)
     			    			})),
     					new ServicePeriod("DOWN AM", 10, 5, 
     							StmProfile.createMap(new StmProfile[] {
     									new StmProfile(Direction.DOWN, 7000, 50, 5)
     							})),
     					new ServicePeriod("DOWN PM", 16, 5, 
     							StmProfile.createMap(new StmProfile[] {
     									new StmProfile(Direction.DOWN, 3500, 50, 5)
     							})),
     					new ServicePeriod("UP PM", 15, 5, 
     							StmProfile.createMap(new StmProfile[] {
     									new StmProfile(Direction.UP, 4200, 75, 5)
    								}))
     					}));
 
     	/*
     	 * XXL50_5
     	 */
     	new Service(XXL50_5, 50, 5,
     			ServicePeriod.createMap(new ServicePeriod[] {
     					new ServicePeriod("DAY", 0, 24,
     			    			StmProfile.createMap(new StmProfile[] {
     			    					new StmProfile(Direction.DOWN, -1, 0, 0), 
     			    					new StmProfile(Direction.UP, -1, 0, 0)
     			    			})),
     					new ServicePeriod("DOWN AM", 10, 5, 
     							StmProfile.createMap(new StmProfile[] {
     									new StmProfile(Direction.DOWN, 10000, 50, 5)
     							})),
     					new ServicePeriod("DOWN PM", 16, 5, 
     							StmProfile.createMap(new StmProfile[] {
     									new StmProfile(Direction.DOWN, 5000, 50, 5)
     							})),
     					new ServicePeriod("UP PM", 15, 5, 
     							StmProfile.createMap(new StmProfile[] {
     									new StmProfile(Direction.UP, 6000, 65, 5)
    								}))
     					}));
 
 		/*
 		 * XL60_6
 		 */
    	new Service(XL60_6, 50, 5,
     			ServicePeriod.createMap(new ServicePeriod[] {
     					new ServicePeriod("DAY", 0, 24,
     			    			StmProfile.createMap(new StmProfile[] {
     			    					new StmProfile(Direction.DOWN, -1, 0, 0), 
     			    					new StmProfile(Direction.UP, -1, 0, 0)
     			    			})),
     					new ServicePeriod("DOWN AM", 10, 5, 
     							StmProfile.createMap(new StmProfile[] {
     									new StmProfile(Direction.DOWN, 10000, 50, 5)
     							})),
     					new ServicePeriod("DOWN PM", 16, 5, 
     							StmProfile.createMap(new StmProfile[] {
     									new StmProfile(Direction.DOWN, 5000, 50, 5)
     							})),
     					new ServicePeriod("UP PM", 15, 5, 
     							StmProfile.createMap(new StmProfile[] {
     									new StmProfile(Direction.UP, 7000, 75, 5)
    								}))
     					}));
 
 		/*
 		 * XL100_5
 		 */
    	new Service(XL100_5, 50, 5,
     			ServicePeriod.createMap(new ServicePeriod[] {
     					new ServicePeriod("DAY", 0, 24,
     			    			StmProfile.createMap(new StmProfile[] {
     			    					new StmProfile(Direction.DOWN, -1, 0, 0), 
     			    					new StmProfile(Direction.UP, -1, 0, 0)
     			    			})),
     					new ServicePeriod("DOWN AM", 10, 5, 
     							StmProfile.createMap(new StmProfile[] {
     									new StmProfile(Direction.DOWN, 20000, 50, 5)
     							})),
     					new ServicePeriod("DOWN PM", 16, 5, 
     							StmProfile.createMap(new StmProfile[] {
     									new StmProfile(Direction.DOWN, 10000, 50, 5)
     							})),
     					new ServicePeriod("UP PM", 15, 5, 
     							StmProfile.createMap(new StmProfile[] {
     									new StmProfile(Direction.UP, 6000, 65, 5)
    								}))
     					}));
 
 		/*
 		 * XL100_10
 		 */
    	new Service(XL100_10, 50, 5,
     			ServicePeriod.createMap(new ServicePeriod[] {
     					new ServicePeriod("DAY", 0, 24,
     			    			StmProfile.createMap(new StmProfile[] {
     			    					new StmProfile(Direction.DOWN, -1, 0, 0), 
     			    					new StmProfile(Direction.UP, -1, 0, 0)
     			    			})),
     					new ServicePeriod("DOWN AM", 10, 5, 
     							StmProfile.createMap(new StmProfile[] {
     									new StmProfile(Direction.DOWN, 20000, 50, 5)
     							})),
     					new ServicePeriod("DOWN PM", 16, 5, 
     							StmProfile.createMap(new StmProfile[] {
     									new StmProfile(Direction.DOWN, 10000, 50, 5)
     							})),
     					new ServicePeriod("UP PM", 15, 5, 
     							StmProfile.createMap(new StmProfile[] {
     									new StmProfile(Direction.UP, 12000, 75, 5)
    								}))
     					}));
 
     }
     
     /**
      * @param serviceName
      * @param connectionSpeedDownMbps
      * @param connectionSpeedUpMbps
      * @param serviceStmPeriodMap
      */
     private Service(String serviceName, int connectionSpeedDownMbps, int connectionSpeedUpMbps, 
     		Map<String, ServicePeriod> serviceStmPeriodMap) {
     	this.serviceName = serviceName;
     	this.connectionSpeedDownMbps = connectionSpeedDownMbps;
     	this.connectionSpeedUpMbps = connectionSpeedUpMbps;
 		this.serviceStmPeriodMap = serviceStmPeriodMap;
 		SERVICES.put(serviceName, this);
 	}
     
 	/**
 	 * @return the service profile name
 	 */
 	public String getServiceName() {
 		return serviceName;
 	}
 
 	/**
 	 * @return the DOWN connection speed in Mbps
 	 */
 	public int getConnectionSpeedDownMbps() {
 		return connectionSpeedDownMbps;
 	}
 
 	/**
 	 * @return the the DOWN connection speed in bps
 	 */
 	public long getConnectionSpeedDownBps() {
 		return connectionSpeedDownMbps * KILO_MULT * KILO_MULT;
 	}
 
 	/**
 	 * @return the UP connection speed in Mbps
 	 */
 	public int getConnectionSpeedUpMbps() {
 		return connectionSpeedUpMbps;
 	}
 
 	/**
 	 * @return the UP connection speed in bps 
 	 */
 	public long getConnectionSpeedUpBps() {
 		return connectionSpeedUpMbps * KILO_MULT * KILO_MULT;
 	}
 
     /**
      * @return a list of ServicePeriod contained in this service
      */
     public ServicePeriod[] getStmPeriodList() {
     	return serviceStmPeriodMap.values().toArray(new ServicePeriod[serviceStmPeriodMap.size()]);
     }
     
     /**
      * @return a list of valid service names
      */
     public static String[] getServiceNames() {
     	return SERVICES.keySet().toArray(new String[SERVICES.size()]);
     }
 
     /**
      * @param serviceName
      * @return the Service for the given serviceName
      */
     public static Service getService(String serviceName) {
     	return SERVICES.get(serviceName);
     }
     
 	/**
 	 * @return
 	 */
 	public String getServiceDescription() {
 		return("Service: " + getServiceName() + 
 				". Down Speed: " + getConnectionSpeedDownMbps() + "Mbps" + 
 				". Up Speed: " + getConnectionSpeedUpMbps() + "Mbps.");
 	}
         
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		StringBuffer buf = new StringBuffer();
 		buf.append("Service[");
 	    buf.append("serviceName=");
 	    buf.append(serviceName);
 	    buf.append(", connectionSpeedDownMbps=");
 	    buf.append(connectionSpeedDownMbps);
 	    buf.append(", connectionSpeedUpMbps=");
 	    buf.append(connectionSpeedUpMbps);
 	    buf.append(", serviceStmPeriodMap=");
 	    buf.append(serviceStmPeriodMap);
 		buf.append("]");
 		return buf.toString();
 	}
 }
