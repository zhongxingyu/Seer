 package de.bedit.gaming.wormstats.controller;
 
 import de.bedit.gaming.wormstats.dao.CompetitorDao;
 import de.bedit.gaming.wormstats.dao.LeageDao;
 import de.bedit.gaming.wormstats.model.Competitor;
 import de.bedit.gaming.wormstats.model.CompetitorMatchStatistic;
 import de.bedit.gaming.wormstats.model.Leage;
 import de.bedit.gaming.wormstats.model.MatchGame;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.model.SelectItem;
 import javax.validation.constraints.NotNull;
 
 /**
  *
  * @author benjamin
  */
 @ManagedBean(name = "matchCreate")
 @ViewScoped
 public class MatchCreateController {
 
     @EJB
     private LeageDao leageDao;
     @EJB
     private CompetitorDao competitorDao;
     private MatchGame match = new MatchGame();
     private Leage leage;
     private List<Competitor> competitors = new ArrayList<Competitor>();
     private List<CompetitorMatchStatistic> statistics = new ArrayList<CompetitorMatchStatistic>();
     private List<SelectItem> competitorsWinList = new ArrayList<SelectItem>();
     private boolean toTable = true;
     @NotNull(message = "And the winner is??")
     private long winner;
 
     @PostConstruct
     public void init() {
         LeagesController leageController = (LeagesController) FacesContext
                 .getCurrentInstance().getExternalContext().getSessionMap().get(
                         "leages");
         leage = leageController.getCurrentLeage();
         for (Competitor comp : leage.getCompetitors()) {
             if (comp.isActive()) {
                 competitors.add(comp);
             }
         }
 
        competitorsWinList.add(new SelectItem(null, "-- Bitte wählen --"));
 
         for (Competitor competitor : competitors) {
             CompetitorMatchStatistic cms = new CompetitorMatchStatistic();
             competitorsWinList.add(new SelectItem(competitor.getId(),
                     competitor.getName()));
             cms.setWorms(6);
             cms.setKills(0);
             cms.setCompetitor(competitor);
             statistics.add(cms);
         }
     }
 
     public String save() {
         String result = "leages";
 
         if (toTable) {
             result = "simpleTable";
         } else {
             FacesContext.getCurrentInstance().getExternalContext()
                     .getSessionMap().remove("leages");
         }
 
         for (CompetitorMatchStatistic cms : statistics) {
             if (cms.isPlays()) {
                 match.getCompetitorMatchStatistics().add(cms);
             }
         }
 
         match.setWinner(competitorDao.getCompetitorById(winner));
         match.setDate(new Date());
         leage.getMatches().add(match);
         leageDao.updateLeage(leage);
 
         return result;
     }
 
     public List<Competitor> getCompetitors() {
         return competitors;
     }
 
     public void setCompetitors(List<Competitor> competitors) {
         this.competitors = competitors;
     }
 
     public List<SelectItem> getCompetitorsWinList() {
         return competitorsWinList;
     }
 
     public void setCompetitorsWinList(List<SelectItem> competitorsWinList) {
         this.competitorsWinList = competitorsWinList;
     }
 
     public long getWinner() {
         return winner;
     }
 
     public void setWinner(long winner) {
         this.winner = winner;
     }
 
     public void removeStats(CompetitorMatchStatistic competitorMatchStatistic) {
         statistics.remove(competitorMatchStatistic);
     }
 
     public boolean isToTable() {
         return toTable;
     }
 
     public void setToTable(boolean toTable) {
         this.toTable = toTable;
     }
 
     public Leage getLeage() {
         return leage;
     }
 
     public void setLeage(Leage leage) {
         this.leage = leage;
     }
 
     public MatchGame getMatch() {
         return match;
     }
 
     public void setMatch(MatchGame match) {
         this.match = match;
     }
 
     public List<CompetitorMatchStatistic> getStatistics() {
         return statistics;
     }
 
     public void setStatistics(List<CompetitorMatchStatistic> statistics) {
         this.statistics = statistics;
     }
 }
