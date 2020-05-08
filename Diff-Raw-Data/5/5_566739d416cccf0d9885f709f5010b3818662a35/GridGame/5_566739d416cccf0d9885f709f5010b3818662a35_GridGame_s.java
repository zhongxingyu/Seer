 /*
  * Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
  *
  * Licensed under the Academic Free License v. 3.0.
  * http://www.opensource.org/licenses/afl-3.0.php
  */
 
 /**
  * A Game stores stateful information about a running game. E.G., the grid's 
  * dimensions, the list of active Cells, and the Players involved.
  * 
  * @author awaterma@ecosur.mx
  *
  */
 
 package mx.ecosur.multigame.grid.model;
 
 import java.awt.Dimension;
 import java.util.*;
 
 import javax.persistence.*;
 
 import mx.ecosur.multigame.MessageSender;
 import mx.ecosur.multigame.exception.InvalidRegistrationException;
 import mx.ecosur.multigame.exception.InvalidSuggestionException;
 import mx.ecosur.multigame.grid.Color;
 import mx.ecosur.multigame.enums.GameState;
 import mx.ecosur.multigame.exception.InvalidMoveException;
 import mx.ecosur.multigame.grid.MoveComparator;
 import mx.ecosur.multigame.grid.comparator.PlayerComparator;
 import mx.ecosur.multigame.model.interfaces.*;
 import org.drools.KnowledgeBase;
 import org.hibernate.annotations.Sort;
 import org.hibernate.annotations.SortType;
 
 @NamedQueries( {
    @NamedQuery(name = "GridGame.GetCurrentGames",
         query = "SELECT gme FROM GridGame AS gme JOIN gme.players AS player " +
             "WHERE gme.state <> :state AND player.registrant = :registrant AND " +
             "player MEMBER OF gme.players"),
    @NamedQuery(name = "GridGame.GetAvailableGames",
         query = "SELECT DISTINCT gme FROM GridGame AS gme LEFT JOIN gme.players as player " +
             "WHERE gme.state = :state AND player.registrant <> :registrant AND " +
             "player MEMBER OF gme.players")
 })
 @Entity
 public abstract class GridGame implements Game, Cloneable {
         
     private static final long serialVersionUID = -8785194013529881730L; 
 
     protected int id;
 
     protected Date created;
 
     protected Set<GridPlayer> players;
 
     protected Set<GridMove> moves;
 
     protected GameGrid grid;
 
     protected GameState state;
         
     protected long version;
 
     private int rows, columns;
     
     private String gameType;
     
     private static KnowledgeBase kbase;
 
     private transient MessageSender messageSender;
 
     public GridGame () {
         super();
     }
 
     @Id @GeneratedValue
     public int getId() {
         return id;
     }
 
     /**
      * @param registrant
      * @return
      * @throws mx.ecosur.multigame.exception.InvalidRegistrationException
      *
      */
     public abstract GamePlayer registerPlayer(Registrant registrant) throws
             InvalidRegistrationException;
     
     /**
      * @param id the id to set
      */
     public void setId(int id) {
         this.id = id;
     }
 
     @Transient
     public Set getFacts () {
         Set ret = new HashSet();
         if (!grid.isEmpty())
             ret.addAll(grid.getCells());
         return ret;
     }
 
     /**
      * @return the players
      */
     @OneToMany (cascade={CascadeType.ALL}, fetch=FetchType.EAGER)
     @Sort(type= SortType.COMPARATOR, comparator=PlayerComparator.class)
     public Set<GridPlayer> getPlayers() {
         return players;
     }
 
     /**
      * @param players the players to set
      */
     public void setPlayers(Set<GridPlayer> players) {
         this.players = players;
     }
 
     /**
      * @return the grid
      */
     @OneToOne  (cascade={CascadeType.ALL}, fetch=FetchType.EAGER)
     public GameGrid getGrid() {
         return grid;
     }
 
     /**
      * @param grid the grid to set
      */
     public void setGrid(GameGrid grid) {
         this.grid = grid;
     }
 
     /**
      * @return the state
      */
     @Enumerated (EnumType.STRING)
     public GameState getState() {
         return state;
     }
 
     /**
      * @param state the state to set
      */
     public void setState(GameState state) {
         this.state = state;
     }
 
     /**
      * The number of rows of the grid contained by this game.
      * @return
      */
     @Column (name="nRows")
     public int getRows() {
         return rows;
     }
 
     public void setRows(int rows) {
         this.rows = rows;
     }
 
     @Version
     public long getVersion () {
         return version;
     }
 
     public void setVersion (long version) {
         this.version = version;
     }
 
     /**
      * The number of columns in the grid contained by this game.
      * @return number of columns
      */
     @Column (name="nColumns")
     public int getColumns() {
         return columns;
     }
 
     public void setColumns(int columns) {
         this.columns = columns;
     }
 
     /**
      * @return the creation date of this game
      */
     @Temporal(TemporalType.DATE)
     public Date getCreated() {
         return created;
     }
 
     /**
      * Setter for the creation date of this game
      *
      * @param created
      */
     public void setCreated(Date created) {
         this.created = created;
     }
         
     /** Non bean methods **/
     @Transient
     public Dimension getSize() {
         return new Dimension (rows, columns);
     }
 
     public void updatePlayer (GridPlayer player) {
         if (getPlayers() == null)
             setPlayers(new TreeSet<GridPlayer>(new PlayerComparator()));
 
         players.remove(player);
         players.add(player);
     }
 
     public void removePlayer(GamePlayer player) {
         this.players.remove(player);
     }
 
     /**
      * @param query
      * @return
      */
     public Query setParameters(Query query) {
         query.setParameter("game", this);
         return query;
     }
 
     /* (non-Javadoc)
      * @see mx.ecosur.multigame.model.interfaces.Game#getMoves()
      */
     @Transient
     public Set<Move> listMoves() {
         Set<Move> ret = new LinkedHashSet<Move>();
         Set<GridMove> moves = getMoves();
         for (GridMove move : moves) {
             ret.add(move);
         }
 
         return ret;
     }
 
     public abstract Suggestion suggest(Suggestion suggestion) throws InvalidSuggestionException;
 
     /**
      * @param sender
      */
     public void setMessageSender(MessageSender sender) {
         this.messageSender = sender;
     }
 
     @Transient
     public MessageSender getMessageSender() {
         return messageSender;
     }
 
     @OneToMany (cascade={CascadeType.PERSIST, CascadeType.MERGE}, fetch=FetchType.EAGER)
     @Sort(type= SortType.COMPARATOR, comparator=MoveComparator.class)
     public Set<GridMove> getMoves() {
         if (moves == null)
             moves = new TreeSet<GridMove>(new MoveComparator());
         return moves;
     }
 
     public void setMoves (Set<GridMove> moves) {
         this.moves = moves;
     }
 
     @Transient
     protected List<Color> getAvailableColors() {
         List<Color> colors = getColors();
         for (GamePlayer player : players) {
             GridPlayer ep = (GridPlayer) player;
             colors.remove(ep.getColor());
         }
 
         return colors;
     }
 
     /* (non-Javadoc)
      * @see mx.ecosur.multigame.model.interfaces.Game#listPlayers()
      */
     public List<GamePlayer> listPlayers() {
         List<GamePlayer> ret = new ArrayList<GamePlayer>();
         for (GridPlayer player : players) {
             ret.add(player);
         }
 
         return ret;
     }
 
 
     @Transient
     public List<Color> getColors() {
         return Collections.emptyList();
     }
 
     /* (non-Javadoc)
      * @see mx.ecosur.multigame.model.interfaces.Game#move(mx.ecosur.multigame.model.interfaces.Move)
      */
     public Move move(Move move) throws InvalidMoveException {
         return move;
     }
 
     public void setKbase(KnowledgeBase kbase) {
         this.kbase = kbase;
     }
 
     @Transient
     public int getMaxPlayers() {
         return 0;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public abstract Agent registerAgent(Agent implementation) throws InvalidRegistrationException;
 
     public String getGameType () {
         return gameType;
     }
 
     public void setGameType (String gameType) {
         this.gameType = gameType;
     }
     
 }
