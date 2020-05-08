 package com.sas.comp.resource;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Comparator;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 
 import com.sas.comp.hibernate.Hibernate;
 import com.sas.comp.models.Season;
 import com.sas.comp.models.Team;
 
 @Path("seasons")
 @Produces("application/json")
 public class SeasonResource {
 
 	@GET
 	public Collection<Season> findAll() {
 		Collection<Season> seasons = Hibernate
 				.getInstance()
 				.createQuery("SELECT DISTINCT s FROM seasons s JOIN FETCH s.teams t JOIN FETCH s.games g ORDER BY s.name DESC",
 						Season.class).getResultList();
 		for(Season s : seasons) {
 			Collection<Team> teams = s.getTeams().values();
 			for(Team t : teams) {
 				t.processGames();
 			}
 			Team[] sortedTeams = teams.toArray(new Team[0]);
 			Arrays.sort(sortedTeams, new Comparator<Team>() {
 				@Override
 				public int compare(Team o1, Team o2) {
					int pc = o2.getPoints().compareTo(o1.getPoints());
					if (pc == 0)
						pc = o2.getGoalDifferential().compareTo(o1.getGoalDifferential());
					if (pc == 0)
						pc = o2.getGoalsFor().compareTo(o1.getGoalsFor());
					if (pc == 0)
						pc = o1.getGoalsAgainst().compareTo(o2.getGoalsAgainst());
					return pc;
 				}
 			});
 			int i = 1;
 			for (Team t : sortedTeams){
 				t.setRank(i++);
 			}
 		}
 		return seasons;
 	}
 
 	@GET
 	@Path("last")
 	public Season findLast() {
 		return Hibernate.getInstance()
 				.createQuery("SELECT DISTINCT s FROM seasons s JOIN FETCH s.teams t JOIN FETCH s.games g ORDER BY s.id DESC", Season.class)
 				.setMaxResults(1).getSingleResult();
 	}
 
 	@GET
 	@Path("{id}")
 	public Season find(@PathParam("id") final Integer id) {
 		return Hibernate.getInstance().find(Season.class, id);
 	}
 }
