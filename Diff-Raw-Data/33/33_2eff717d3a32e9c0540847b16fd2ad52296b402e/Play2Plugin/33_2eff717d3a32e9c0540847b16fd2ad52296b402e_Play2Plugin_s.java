 package com.alvazan.play2;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.alvazan.orm.api.base.Bootstrap;
 import com.alvazan.orm.api.base.NoSqlEntityManager;
 import com.alvazan.orm.api.base.NoSqlEntityManagerFactory;
 import com.alvazan.orm.api.base.anno.NoSqlEntity;
 
 import play.Application;
 import play.Play;
 import play.Plugin;
 
 public class Play2Plugin extends Plugin {
 	
     private final Application application;
     
    private final Map<String, NoSqlEntityManagerFactory> emfs = new HashMap<String, NoSqlEntityManagerFactory>();
 
     public Play2Plugin(Application application) {
         this.application = application;
     }
    
    private boolean isPluginDisabled() {
        String status =  application.configuration().getString("playormplugin");
        return status != null && status.equals("disabled");
    } 
    
 	@Override
 	public boolean enabled() {
		return isPluginDisabled() == false;		
 	}
 
 	@Override
 	public void onStop() {
	    if (emfs.get("emf") != null) {
	    	NoSqlEntityManagerFactory factory = emfs.get("emf");
	    	factory.close();
	        return;
 	    }
 	}
 
 	@Override
 	public void onStart() {
 	// check if nosql.Persistence is here
 	try {
 		Class.forName("nosql.Persistence", true, Play.application().classloader());
 	} catch (ClassNotFoundException e) {
 				return;
 	}
 
 	if (NoSqlForPlay2.getEntityManagerFactory() != null) {
         	return;
         }
         Map<String, Object> props = new HashMap<String, Object>();
         props.put(Bootstrap.AUTO_CREATE_KEY, "create");
         props.put("nosql.nosqltype", Play.application().configuration().getString("nosql.nosqltype"));
        	props.put("nosql.cassandra.clusterName", Play.application().configuration().getString("nosql.cassandra.clusterName"));
        	props.put("nosql.cassandra.keyspace", Play.application().configuration().getString("nosql.cassandra.keyspace"));
        	props.put("nosql.cassandra.seeds", Play.application().configuration().getString("nosql.cassandra.seeds"));	
         NoSqlEntityManagerFactory factory = Bootstrap.create(props, Play.application().classloader());
         NoSqlForPlay2.setEntityManagerFactory(factory);
        emfs.put("emf", factory);
 	}
 }
