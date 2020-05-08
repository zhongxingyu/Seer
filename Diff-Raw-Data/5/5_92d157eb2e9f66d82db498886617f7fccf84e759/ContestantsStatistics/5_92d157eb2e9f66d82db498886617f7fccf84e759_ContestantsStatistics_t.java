 package services.competition;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import javax.persistence.Query;
 import models.Competition;
 import models.Contestant;
 import models.Task;
 import models.UserGroup;
 import play.db.jpa.JPA;
 
 /**
  * Statistics of contest sorted by place
  * Shows: position, contestant name, solved tasks
  * @author Daniel Robenek <danrob@seznam.cz>
  */
 public class ContestantsStatistics {
 
 	public boolean hasStatistics(Competition competition) {
 		return true; //competition.startDate != null && competition.stopDate != null;
 	}
 
 	public Collection<ContestantsResult> getStatistics(Competition competition, boolean forceActual) {
 		final List<ContestantsResult> result = new ArrayList<ContestantsResult>();
 
 		Date toDate = null;
		if (!forceActual && competition.stopDate != null && competition.startDate != null && competition.dontUpdateStatisticsBefore != null) {
 			Calendar calendar = Calendar.getInstance();
 			calendar.setTime(competition.stopDate);
 			calendar.add(Calendar.MINUTE, -competition.dontUpdateStatisticsBefore);
 			toDate = calendar.getTime();
 		}
 
 		for (UserGroup group : competition.groups) {
 			for (Contestant contestant : group.users) {
 				Query query = JPA.em().createQuery(
 						"SELECT s.task.id, MIN(s.created) AS colutionCreated, "
 						+ "(SELECT COUNT(s2) FROM Solution s2 JOIN s2.systemReply sr2 WHERE s2.competition = :competition AND s2.user = :user AND sr2.accepting = false AND s2.task=s.task) AS invalidSubmittionCount "
 						+ "FROM Solution s "
 						+ "JOIN s.systemReply sr "
 						+ "WHERE s.competition = :competition "
 						+ (toDate == null ? "" : "AND s.created < :toDate ")
 						+ "AND s.user = :user "
 						+ "AND sr.accepting = true "
 						+ "GROUP BY s.task").setParameter("competition", competition).setParameter("user", contestant);
 				if (toDate != null) {
 					query.setParameter("toDate", toDate);
 				}
 				List st = query.getResultList();
 				final Map<Task, Integer> solvedTasks = new TreeMap<Task, Integer>(new TaskComparator());
 				long penalization = 0;
 				for (Object solved : st) {
 					final Task task = Task.findById(((Object[]) solved)[0]);
 					final Date solutionCreated = (Date) ((Object[]) solved)[1];
 					final long invalidSubmittionCount = (Long) ((Object[]) solved)[2];
 					solvedTasks.put(task, (int) (invalidSubmittionCount));
 					// short competition
					if(competition.startDate != null && competition.stopDate != null) {
 						penalization += solutionCreated.getTime() - competition.startDate.getTime();
 						if(competition.timePenalization != null)
 							penalization += competition.timePenalization * 60000 * invalidSubmittionCount;
 					} else { // long time competition
 						penalization += invalidSubmittionCount;
 					}
 				}
 				result.add(new ContestantsResult(contestant, solvedTasks, penalization));
 			}
 		}
 		Collections.sort(result);
 		int i = 0;
 		for (ContestantsResult r : result) {
 			r.setPosition(++i);
 		}
 		return result;
 	}
 
 	public static class TaskComparator implements Comparator<Task> {
 
 		public int compare(Task t, Task t1) {
 			final int c = t.name.compareTo(t1.name);
 			return c == 0 ? t.getId().compareTo(t1.getId()) : c;
 		}
 	}
 }
