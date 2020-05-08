 
 package org.cluenet.cluebot.reviewinterface.server;
 
import java.util.ArrayList;
 import java.util.List;
 
 import org.cluenet.cluebot.reviewinterface.shared.Classification;
 
 import com.google.appengine.api.datastore.Key;
 
 public class Statistics {
 	
 	public static String getStats() {
 		String stats = "";
 
 		stats += "{{/EditGroupHeader}}\n";
 		for( EditGroup eg : EditGroup.list() ) {
 			org.cluenet.cluebot.reviewinterface.shared.EditGroup egc = eg.getLightClientClass();
 			stats += "{{/EditGroup\n";
 			stats += "|name=" + egc.name + "\n";
 			stats += "|weight=" + egc.weight.toString() + "\n";
 			stats += "|notdone=" + egc.countLeft.toString() + "\n";
 			stats += "|partial=" + egc.countReviewed.toString() + "\n";
 			stats += "|done=" + egc.countDone.toString() + "\n";
 			stats += "}}\n";
 		}
 		stats += "{{/EditGroupFooter}}\n\n";
 		
 		stats += "{{/UserHeader}}\n";
		
		for( User u : new ArrayList< User >( User.list() ) ) {
 			Key start = null;
 			Integer requested = 500;
 			Integer got;
 			Integer total = 0;
 			Integer correct = 0;
 			do {
 				got = 0;
 				List< EditClassification > ecs = EditClassification.findByUser( u, start, requested );
 				for( EditClassification ec : ecs ) {
 					got++;
 					start = ec.getKey();
 					
 					Classification majority = ec.getEdit().calculateClassification();
 					Classification user = ec.getClassification();
 					if(
 							majority.equals( Classification.UNKNOWN )
 							|| majority.equals( Classification.SKIPPED )
 							|| user.equals( Classification.SKIPPED )
 					)
 						continue;
 					if( majority.equals( user ) )
 						correct++;
 					total++;
 				}
 			} while( got == requested );
 			stats += "{{/User\n";
 			stats += "|nick=" + u.getNick() + "\n";
 			stats += "|admin=" + u.isAdmin().toString() + "\n";
 			stats += "|count=" + u.getClassifications() + "\n";
 			stats += "|accuracy=" + new Double( ( (double) correct ) / ( (double) total ) * 100.0 ).toString() + "\n";
 			stats += "|accuracyedits=" + total.toString() + "\n";
 			stats += "}}\n";
 		}
 		stats += "{{/UserFooter}}\n\n";
 		
 		return stats;
 	}
 }
