 package com.schmal.domain;
 
 import com.fasterxml.jackson.annotation.JsonIgnore;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.GeneratedValue;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderBy;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import lombok.EqualsAndHashCode;
 import lombok.Getter;
 import lombok.NoArgsConstructor;
 import lombok.NonNull;
 import lombok.RequiredArgsConstructor;
 import lombok.Setter;
 import org.hibernate.annotations.GenericGenerator;
 
 @NoArgsConstructor
 @RequiredArgsConstructor
 @EqualsAndHashCode(of = {"fantasyID", "year"})
 @Entity
 @Table(name = "league")
 @NamedQueries({
     @NamedQuery(
         name = "get",
         query = "select L from League L where L.fantasyID = :fantasyID and L.year = :year"
     ),
     @NamedQuery(
         name = "getByService",
         query = "select L from League L where L.service = :service"
     ),
     @NamedQuery(
         name = "getByFantasyID",
         query = "select L from League L where L.fantasyID = :fantasyID"
     ),
     @NamedQuery(
         name = "getByID",
         query = "select L from League L where L.ID = :leagueID"
     )
 })
 public class League
 {
     @Id
     @Getter @Setter
     @GeneratedValue(generator = "league-id-gen")
     @GenericGenerator(name = "league-id-gen", strategy = "increment")
     @Column(name = "id", nullable = false)
     private long ID;
 
     @NonNull @Getter @Setter
     @Column(name = "fantasy_id", nullable = false)
     private long fantasyID;
 
     @NonNull @Getter @Setter
     @Column(name = "year", nullable = false)
     private int year;
 
     @NonNull @Getter @Setter
     @Column(name = "name", nullable = false)
     private String name;
 
     @NonNull @Getter @Setter
     @Column(name = "service", nullable = false)
     private String service;
 
     @Getter @Setter
     @OneToMany(mappedBy = "league", orphanRemoval = true, cascade = CascadeType.ALL)
     private List<Team> teams;
 
     @Getter @Setter
     @OneToMany(mappedBy = "league", orphanRemoval = true, cascade = CascadeType.ALL)
    @OrderBy("startPeriod ASC")
     private List<Week> weeks;
 
     @Getter @Setter
     @OneToMany(mappedBy = "league", orphanRemoval = true, cascade = CascadeType.ALL)
     private List<Category> categories;
 
     @Getter @Setter
     @OneToMany(mappedBy = "league", orphanRemoval = true, cascade = CascadeType.ALL)
     private List<Player> players;
 
     @Getter @Setter
     @OneToMany(mappedBy = "league", orphanRemoval = true, cascade = CascadeType.ALL)
     @OrderBy("periodID ASC")
     private List<ScoringPeriod> scoringPeriods;
 
     @Transient @JsonIgnore
     private Map<Integer,Team> teamMap;
 
     @Transient @JsonIgnore
     private Map<Long,Player> playerMap;
 
     @Transient @JsonIgnore
     private Map<String,Category> categoryMap;
 
     @Transient @JsonIgnore
     private Map<Integer,ScoringPeriod> scoringPeriodMap;
 
     public Map<Integer,Team> getTeamMap()
     {
         if (teamMap == null)
         {
             createTeamMap();
         }
 
         return teamMap;
     }
 
     private void createTeamMap()
     {
         teamMap = new HashMap<Integer,Team>();
 
         for (Team team : getTeams())
         {
             teamMap.put(team.getFantasyTeamID(), team);
         }
     }
 
     public Map<Long,Player> getPlayerMap()
     {
         if (playerMap == null)
         {
             createPlayerMap();
         }
 
         return playerMap;
     }
 
     private void createPlayerMap()
     {
         playerMap = new HashMap<Long,Player>();
 
         for (Player player : getPlayers())
         {
             playerMap.put(player.getFantasyID(), player);
         }
     }
 
     public Category getCategory(char type, String category)
     {
         if (categoryMap == null)
         {
             createCategoryMap();
         }
 
         return categoryMap.get(type + ":" + category);
     }
 
     private void createCategoryMap()
     {
         categoryMap = new HashMap<String,Category>();
 
         for (Category category : getCategories())
         {
             categoryMap.put(category.getCategoryType() + ":" + category.getCategory(), category);
         }
     }
 
     public Map<Integer,ScoringPeriod> getScoringPeriodMap()
     {
         if (scoringPeriodMap == null)
         {
             createScoringPeriodMap();
         }
 
         return scoringPeriodMap;
     }
 
     private void createScoringPeriodMap()
     {
         scoringPeriodMap = new HashMap<Integer,ScoringPeriod>();
 
         for (ScoringPeriod scoringPeriod : getScoringPeriods())
         {
             scoringPeriodMap.put(scoringPeriod.getPeriodID(), scoringPeriod);
         }
     }
 }
