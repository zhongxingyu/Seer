 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.fofo.dao;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.*;
 import org.fofo.entity.Club;
 import org.fofo.entity.Competition;
 import org.fofo.entity.Team;
 
 /**
  *
  * @author josepma
  */
 public class TeamDAOImpl implements TeamDAO{
 
    EntityManager em;
    
     /**
      *
      */
     public TeamDAOImpl(){
        
 
        
    }
    
     /**
      *
      * @param em
      */
     public void setEM(EntityManager em){
        this.em = em;
    }
    
     /**
      *
      * @return
      */
     public EntityManager getEM(){
        return this.em;
    }
 
 
     /**
      *
      * @param team
      * @throws PersistException
      * @throws IncorrectTeamException
      */
     @Override
     public void addTeam(Team team) throws PersistException, 
                                           IncorrectTeamException{
     
 
        try{
           em.getTransaction().begin();
 
          //if (team.getClub() == null || team.getClub().getName() == null) 
          //                                  throw new IncorrectTeamException();
           
           Club club = (Club) em.find(Club.class, team.getClub().getName());
           if (club == null) throw new IncorrectTeamException();
           
           em.persist(team);          
           
           club.getTeams().add(team);
          
           em.getTransaction().commit();
           
        }
        catch (EntityExistsException e){
 	  throw new PersistException();
        }
        finally{
           if (em.getTransaction().isActive()) em.getTransaction().rollback();
        }
     }
     
     /**
      *
      * @param name
      */
     public void removeTeam(String name){
     
     }
     
    
     /**
      *
      * @return
      */
     public List<Team> getTeams(){
         
         List<Team> teams = new ArrayList<Team>();
         em.getTransaction().begin();
         Query pp = em.createQuery("SELECT t FROM Team t");
         teams = pp.getResultList();
         em.getTransaction().commit();
         return teams;      
         
     }
     
     
     /**
      *
      * @param name
      * @return
      * @throws PersistException
      */
     @Override
     public Team findTeamByName(String name) throws PersistException{
        Team team = null; 
        try{
           em.getTransaction().begin();
           
           team = (Team) em.find (Team.class,name);
           em.getTransaction().commit();
           
        }
        catch (PersistenceException e){
            e.printStackTrace();
 	  throw new PersistException();
        }
 //       finally{
 //          if (em.isOpen()) em.close();
 //       }
        return team;        
         
     }
     
     /**
      *
      * @param name
      * @return
      */
     public List<Team> findTeamByClub(String name){
         
         return null;
     }
 
 //CAL TREURE-LA!!!!!
     /**
      *
      * @param team
      * @return
      */
     public boolean findTeam(Team team) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
     
     
 }
