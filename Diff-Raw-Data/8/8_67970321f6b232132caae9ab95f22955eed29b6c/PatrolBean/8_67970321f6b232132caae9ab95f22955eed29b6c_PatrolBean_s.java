 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.ikano.hedwig.controller;
 
 import java.io.Serializable;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.util.ArrayList;
 import java.util.List;
 
import javax.ejb.Singleton;
 import javax.ejb.Stateless;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.management.ObjectName;
 import javax.management.remote.rmi.RMIConnection;
 import javax.management.remote.rmi.RMIServer;
 import javax.persistence.EntityManager;
 
 import org.jboss.solder.logging.Logger;
 import org.jboss.solder.logging.TypedCategory;
 
 import de.ikano.hedwig.model.PatrolReport;
 import de.ikano.hedwig.model.Target;
 import de.ikano.hedwig.model.TargetStatus;
 import de.ikano.hedwig.util.JMXUtils;
 
 /**
  *
  * @author lutz
  */
 @Named("patrolBean")
 @Stateless
 public class PatrolBean implements Serializable {
 
     private static final long serialVersionUID = 1L;
     @Inject
     private EntityManager em;    
 
 	@Inject @TypedCategory(PatrolBean.class)
     private Logger log;
 
     public List<PatrolReport> startPatrolling() {
     	log.info("Starting Patrol...");
     	@SuppressWarnings("unchecked")
 		List<Target> targets = em.createQuery("select t from Target t").getResultList();
         List<PatrolReport> reports = new ArrayList<PatrolReport>(targets.size());
         for (Target target : targets) {
             PatrolReport report = checkSingleTarget(target);
             //save
             em.merge(target);
             //done
             reports.add(report);
         }
         log.infof("...finished patrolling %s Target(s).", targets.size());
         return reports;
     }
 
     public PatrolReport checkSingleTarget(Target target) {
     	log.infof("Now serving Target %s", target.getName());
         PatrolReport report = new PatrolReport(target);
         RMIConnection conn = null;
         try {
             //access jmx bean
             Registry registry = LocateRegistry.getRegistry(target.getHost(), target.getPort());
             RMIServer remote = (RMIServer) registry.lookup(target.getRegistryName());
             conn = remote.newClient(null);
             
             Boolean success;
             if (target.isAccessViaMethod()) {
                 success = (Boolean) conn.invoke(
                         new ObjectName(target.getObjectName()), target.getAccessMethod(), null, null, null);
             } else {
                 success = (Boolean) conn.getAttribute(new ObjectName(target.getObjectName()),
                         target.getAccessProperty(), null);
             }
             target.setStatus(success ? TargetStatus.OK : TargetStatus.NOT_OK);            
         }
         catch (Exception e) {
             //TODO Loggin and proper status / exception handling
             target.setStatus(TargetStatus.UNREACHABLE);
         }
         finally {
             JMXUtils.closeQuietly(conn);
         }
         //set report status
         report.setStatus(target.getStatus());
 
         return report;
     }
     
 }
