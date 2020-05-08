 package com.objectpartners.sbdemo.service;
 
 import com.objectpartners.sbdemo.dao.GameDao;
 import com.objectpartners.sbdemo.dao.TeamDao;
 import com.objectpartners.sbdemo.persistant.Game;
 import com.objectpartners.sbdemo.persistant.Team;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * Implementation of SbdemoService
  */
 @Transactional
 public class SbdemoServiceImpl implements SbdemoService {
 
     private GameDao gameDao;
     private TeamDao teamDao;
 
     @Override
     public void saveGames(List<? extends Game> games) {
         for (Game game : games) {
             gameDao.saveGame(game);
         }
     }
 
     @Override
     public void saveTeams(Collection<? extends Team> teams) {
         for (Team team : teams) {
             teamDao.saveTeam(team);
         }
     }
 
     @Override
     public void resetStandings() {
         List<Team> teams = teamDao.findAll();
         for (Team team : teams) {
             team.setWinPercentage(0.0);
             team.setWins(0);
             team.setLosses(0);
         }
         saveTeams(teams);
     }
 
     @Override
     public List<Team> updateTeamStandings(Game game) {
 
         // Reload the teams. The win/loss totals may have changed during processing
         Team home = teamDao.load(game.getHome().getId());
         Team visitor = teamDao.load(game.getVisitor().getId());
 
         if(game.getHomeScore() > game.getVisitorScore()) {
             home.setWins(home.getWins() + 1);
             visitor.setLosses(visitor.getLosses() + 1);
         } else {
             home.setLosses(home.getLosses() + 1);
             visitor.setWins(visitor.getWins() + 1);
         }
 
        Double homeWinPercentage = (new Double(home.getWins()))/(new Double(home.getWins() + home.getLosses()));
        Double visitorWinPercentage = (new Double(visitor.getWins()))/(new Double(visitor.getWins() + visitor.getLosses()));
 
        home.setWinPercentage(homeWinPercentage);
        visitor.setWinPercentage(visitorWinPercentage);
 
         List<Team> teams = new ArrayList<Team>();
 
         teams.add(home);
         teams.add(visitor);
 
         return teams;
     }
 
     @Override
     public Team findTeamByName(String name, String nickName) {
         return teamDao.find(name, nickName);
     }
 
     public void setTeamDao(TeamDao teamDao) {
         this.teamDao = teamDao;
     }
 
     public void setGameDao(GameDao gameDao) {
         this.gameDao = gameDao;
     }
 }
