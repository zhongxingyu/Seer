 package com.schmal.domain;
 
 import com.fasterxml.jackson.annotation.JsonIgnore;
 import java.util.List;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.GeneratedValue;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 import lombok.Getter;
 import lombok.NoArgsConstructor;
 import lombok.NonNull;
 import lombok.RequiredArgsConstructor;
 import lombok.Setter;
 import org.hibernate.annotations.GenericGenerator;
 
 @NoArgsConstructor
 @RequiredArgsConstructor
 @Entity
 @Table(name = "result")
 @NamedQueries({
     @NamedQuery(
         name = "maxScoringPeriod",
         query = "select max(scoringPeriod.periodID) from Result " +
                 "where player.league.id = :leagueID"
     )
 })
 public class Result
 {
     @Id
     @Getter @Setter
     @GeneratedValue(generator = "result-id-gen")
     @GenericGenerator(name = "result-id-gen", strategy = "increment")
     @Column(name = "id", nullable = false)
     private long ID;
 
     @NonNull @Getter @Setter
     @JsonIgnore
     @OneToOne
     private Player player;
 
     @NonNull @Getter @Setter
     @OneToOne
     private ScoringPeriod scoringPeriod;
 
     @Getter @Setter
     private float score;
 
     @Getter @Setter
     @OneToMany(mappedBy = "result", orphanRemoval = true, cascade = CascadeType.ALL)
     private List<Stat> stats;
 
     public void calculateScore()
     {
         float score = 0f;
 
         for (Stat stat : getStats())
         {
             if ("IP".equals(stat.getCategory().getCategory()))
             {
                 float ip = stat.getValue() * 10;
                 int outs = Math.round(ip % 10) + Math.round(ip / 10) * 3;
                score += outs * Math.round(stat.getCategory().getPoints() / 3);
                 continue;
             }
 
             score += stat.getValue() * stat.getCategory().getPoints();
         }
 
         this.score = score;
     }
 }
