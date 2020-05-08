 package com.jwxicc.cricket.records;
 
 import java.math.BigInteger;
 import java.util.List;
 
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import com.jwxicc.cricket.interfaces.PlayerManager;
 
 @Stateless
 public abstract class RecordsManager<T, U> {
 
 	protected static final String JWXI_TEAM_SQL = "p.teamid = :jwxi ";
 
 	@PersistenceContext(unitName = "Jwxicc_JPA")
 	EntityManager em;
 
 	@EJB
 	PlayerManager playerManager;
 
 	protected static final String MATCHES_PLAYED_SQL = "select count(*) as matches from BATTING b where b.playerid = :player";
 
	protected static final String COMPETITION_QUALIFIER_END_SQL = "inner join INNINGS i natural join GAME g "
			+ "on b.inningsid = i.inningsid "
			+ "where g.competitionid = :comp ";
 
 	public abstract List<T> getInningsBest();
 
 	public abstract List<U> getByAggregate();
 
 	public abstract List<U> getByAverage();
 
 	public abstract List<U> getBySeason(int competitionId);
 
 	public abstract U getPlayerCareerRecord(int playerId);
 
 	public int objToInt(Object o) {
 		if (o != null) {
 			return Integer.valueOf(o.toString()).intValue();
 		} else {
 			return 0;
 		}
 	}
 
 	public int getMatchesPlayed(int playerId) {
 		Query query = em.createNativeQuery(MATCHES_PLAYED_SQL);
 		query.setParameter("player", playerId);
 		int matches = ((BigInteger) query.getSingleResult()).intValue();
 		return matches;
 	}
 
 	public int getMatchesPlayedInSeason(int playerId, int competitionId) {
 		Query query = em
 				.createNativeQuery(MATCHES_PLAYED_SQL
 						+ " and b.inningsId in (select i.inningsId from INNINGS i natural join GAME g where g.competitionId = :comp)");
 		query.setParameter("player", playerId);
 		query.setParameter("comp", competitionId);
 		int matches = ((BigInteger) query.getSingleResult()).intValue();
 		return matches;
 	}
 
 }
