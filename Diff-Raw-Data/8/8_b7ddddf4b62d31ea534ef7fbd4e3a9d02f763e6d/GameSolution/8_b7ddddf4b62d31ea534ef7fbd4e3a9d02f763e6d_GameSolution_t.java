 /* 
  * Project       : Bachelor Thesis - Sudoku game implementation as portlet
  * Document      : GameSolution.java
  * Author        : Ondřej Fibich <xfibic01@stud.fit.vutbr.cz>
  * Organization: : FIT VUT <http://www.fit.vutbr.cz>
  */
 
 package org.gatein.portal.examples.games.sudoku.entity;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.Date;
 import javax.persistence.*;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 /**
  * In order to play a game, a game solution entity must be created. Each solution
  * is owned by a solver who is identified by an identificator of a portal user
  * and by a full name.
  * 
  * The entity persists following attributes: a time stamp of the start, lasting
  * in seconds, field values, a counter of usages of the solution check, user's
  * rating and an indicator which denotes whether the solution is solved or not.
  *
  * @author Ondřej Fibich
  */
 @Entity
 @Table(name = "game_solutions")
 @XmlRootElement
 @NamedQueries({
     @NamedQuery(
         name = "GameSolution.findAll",
         query = "SELECT g FROM GameSolution g"
     ),
     @NamedQuery(
         name = "GameSolution.findUnfinishedOfUser",
         query = "SELECT g FROM GameSolution g "
               + "WHERE g.userId = :uid AND g.finished = 0"
     ),
     @NamedQuery(
         name = "GameSolution.count",
         query = "SELECT COUNT(g) FROM GameSolution g"
     ),
     @NamedQuery(
         name = "GameSolution.countFinished",
         query = "SELECT COUNT(g) FROM GameSolution g "
               + "WHERE g.finished > 0"
     ),
     @NamedQuery(
         name = "GameSolution.countSolver",
        query = "SELECT COUNT(DISTINCT g.userId) FROM GameSolution g "
              + "WHERE g.finished > 0 "
     )
 })
 public class GameSolution implements Serializable
 {
 
     private static final long serialVersionUID = 1L;
     
     /**
      * Defines a count for which a game solution may be checked 
      */
     public static final int MAX_CHECK_COUNT = 3;
     
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @Column(name = "id")
     private Integer id;
     
     @Column(name = "user_id")
     private String userId;
     
     @Column(name = "user_name")
     private String userName;
     
     @Basic(optional = false)
     @Lob
     @Column(name = "values_", nullable = false)
     private String values;
     
     @Basic(optional = false)
     @Column(name = "start_time", nullable = false)
     @Temporal(TemporalType.TIMESTAMP)
     private Date startTime;
     
     @Basic(optional = false)
     @Column(name = "lasting", nullable = false)
     private int lasting;
     
     @Column(name = "finished", nullable = false)
     private boolean finished;
     
     @Column(name = "check_count", nullable = false)
     private int checkCount;
     
     @Column(name = "rating")
     private Integer rating;
     
     @JoinColumn(name = "game_id", referencedColumnName = "id", nullable = false)
     @ManyToOne(optional = false)
     private Game gameId;
     
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "gameSolutionId")
     private Collection<SavedGameSolution> savedGameSolutionsCollection;
     
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "gameSolutionId")
     private Collection<LastPlayedGameSolution> lastPlayedGameSolutionCollection;
 
     /**
      * An empty contructor of the entity
      */
     public GameSolution()
     {
     }
 
     /**
      * An contructor of the entity
      * 
      * @param id        An identificator of the entity
      * @param userId    An identificator of a user who own this game solution
      * @param userName  A name of a user who own this game solution
      * @param values    Values of the game solution
      * @param startTime A datetime of the start of the game solution  
      * @param lasting   A lasting of the game solution
      * @param finished  Was the game solution finished yet?
      */
     public GameSolution(Integer id, String userId, String userName,
             String values, Date startTime, int lasting, boolean finished)
     {
         this.id = id;
         this.userId = userId;
         this.userName = userName;
         this.values = values;
         this.startTime = startTime;
         this.lasting = lasting;
         this.finished = finished;
     }
 
     /**
      * Gets the identificator 
      * 
      * @return          Identificator
      */
     public Integer getId()
     {
         return id;
     }
 
     /**
      * Sets the idetificator
      * 
      * @param id        New identificator
      */
     public void setId(Integer id)
     {
         this.id = id;
     }
 
     /**
      * Gets the identificator of a user who own this game solution
      * 
      * @return          String identificator or null for anonymous user
      */
     public String getUserId()
     {
         return userId;
     }
 
     /**
      * Sets the identificator of a user who own this game solution
      * 
      * @param userId    String identificator or null for anonymous user
      */
     public void setUserId(String userId)
     {
         this.userId = userId;
     }
 
     /**
      * Gets the name of a user who own this game solution
      * 
      * @return          String name
      */
     public String getUserName()
     {
         return userName;
     }
 
     /**
      * Sets the name of a user who own this game solution
      * 
      * @param userName  String name
      */
     public void setUserName(String userName)
     {
         this.userName = userName;
     }
 
     /**
      * Gets values of the game solution.
      * 
      * @return          Values as a string, separated by commas.
      *                  Empty values are replaced by an empty string.
      */
     public String getValues()
     {
         return values;
     }
 
     /**
      * Sets values of the game solution.
      * 
      * @param values    Values as a string, separated by commas.
      *                  Empty values are replaced by an empty string.
      */
     public void setValues(String values)
     {
         if (values == null || !values.matches("^([1-9]?,){80}[1-9]?$"))
         {
             throw new IllegalArgumentException("Wrong values: " + values);
         }
         
         this.values = values;
     }
 
     /**
      * Gets the datetime of the start of the game solution  
      * 
      * @return          Date object
      */
     public Date getStartTime()
     {
         return startTime;
     }
 
     /**
      * Sets the datetime of the start of the game solution  
      * 
      * @param startTime New date object
      */
     public void setStartTime(Date startTime)
     {
         this.startTime = startTime;
     }
 
     /**
      * Gets the lasting of the game solution 
      * 
      * @return          Lasting in seconds
      */
     public int getLasting()
     {
         return lasting;
     }
 
     /**
      * Sets the lasting of the game solution 
      * 
      * @param lasting   Lasting in seconds
      */
     public void setLasting(int lasting)
     {
         this.lasting = lasting;
     }
 
     /**
      * Gets the state of the game solution.
      * 
      * @return          Is game finished?
      */
     public boolean isFinished()
     {
         return finished;
     }
 
     /**
      * Sets the state of the game solution.
      * 
      * @param finished  An indicator of a finished game
      */
     public void setFinished(boolean finished)
     {
         this.finished = finished;
     }
 
     /**
      * Gets the parent game of the game solution
      * 
      * @return          Game entity
      */
     public Game getGameId()
     {
         return gameId;
     }
 
     /**
      * Sets the parent game of the game solution
      * 
      * @param gameId    Game entity
      */
     public void setGameId(Game gameId)
     {
         this.gameId = gameId;
     }
 
     /**
      * Gets user's rating of the game.
      * 
      * @return          A null for unfilled rating or a value from 1 to 5 (5>1). 
      */
     public Integer getRating()
     {
         return rating;
     }
 
     /**
      * Sets user's rating of the game.
      * 
      * @param rating    A null for unfilled rating or a value from 1 to 5 (5>1). 
      */
     public void setRating(Integer rating)
     {
         this.rating = rating;
     }
 
     /**
      * Gets the count of checks of the game solution by owner 
      * 
      * @return          Count
      */
     public int getCheckCount()
     {
         return checkCount;
     }
 
     /**
      * Sets the count of checks of the game solution by owner 
      * 
      * @param checkCount New count
      */
     public void setCheckCount(int checkCount)
     {
         this.checkCount = checkCount;
     }
 
     /**
      * Gets the collection of saved game solution entities of this game solution
      * 
      * @return          Collection
      */
     @JsonIgnore
     @XmlTransient
     public Collection<SavedGameSolution> getSavedGameSolutionsCollection()
     {
         return savedGameSolutionsCollection;
     }
 
     /**
      * Sets the collection of saved game solution entities of this game solution
      * 
      * @param savedGamesCollection New collection
      */
     public void setSavedGameSolutionsCollection(Collection<SavedGameSolution> savedGamesCollection)
     {
         this.savedGameSolutionsCollection = savedGamesCollection;
     }
 
     /**
      * Gets the collection of last played game solutions of this game solution
      * 
      * @return              Collection
      */
     @JsonIgnore
     @XmlTransient
     public Collection<LastPlayedGameSolution> getLastPlayedGameSolutionCollection()
     {
         return lastPlayedGameSolutionCollection;
     }
 
     /**
      * Sets the collection of last played game solutions of this game solution
      * 
      * @param lastPlayedGameSolutionCollection New collection
      */
     public void setLastPlayedGameSolutionCollection(
             Collection<LastPlayedGameSolution> lastPlayedGameSolutionCollection)
     {
         this.lastPlayedGameSolutionCollection = lastPlayedGameSolutionCollection;
     }
 
     @Override
     public int hashCode()
     {
         return id != null ? id.hashCode() : 0;
     }
 
     @Override
     public boolean equals(Object object)
     {
         if (!(object instanceof GameSolution))
         {
             return false;
         }
         
         GameSolution other = (GameSolution) object;
         
         if ((this.id == null && other.getId() != null) ||
             (this.id != null && !this.id.equals(other.getId())))
         {
             return false;
         }
         
         return true;
     }
 
     @Override
     public String toString()
     {
         return "org.gatein.portal.examples.games.entities.GameSolutions[ id=" + id + " ]";
     }
 }
