 package edu.uwm.cs552;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.logging.Logger;
 
 import edu.uwm.cs552.util.Pair;
 
 public class Player extends Observable implements Observer {
 
   private final RailGame game;
   private final int index;
   private final Logger log = Logger.getAnonymousLogger();
   private final Color color;
   private final TrackStatus track = new TrackStatus();
   private final TrainStatus train = new TrainStatus();
   private int time;
   private double money;
   private Good want;
   private final CardStatus card;
   private boolean autoMove;
 
   /**
    * Create a player at the given index and using the given color. Everyone
    * starts with one game tick of time.
    * 
    * @param g game, must not be null
    * @param i index of player (1 based)
    * @param c color to use for track
    * @param startupMoney initial money
    */
   public Player(RailGame g, int i, Color c, double startupMoney) {
     game = g;
     index = i;
     color = c;
     time = 1;
     money = startupMoney;
     track.addObserver(this);
     train.addObserver(this);
 
     GoalCard[] cards = new GoalCard[RailGameConstants.GOAL_CARD_COUNT];
     for (int cardIndex = 0; cardIndex < cards.length; cardIndex++) {
       cards[cardIndex] = game.generateGoalCard();
     }
     card = new CardStatus(cards);
   }
 
   /**
    * Return the index of the player (1-based)
    * 
    * @return number of player
    */
   public int getIndex() {
     return index;
   }
 
   public TrainStatus getTrainStatus() {
     return train;
   }
 
   /**
    * Get (anonymous) logger which gets errors, warnings and information
    * messages.
    * 
    * @return logger used
    */
   public Logger getLogger() {
     return log;
   }
 
   /**
    * Draw player specific information on the graphics context. We assume the
    * board is already drawn.
    * 
    * @param g graphics context
    * @param scale width of hexagons
    * @param showConfidential whether to show information private only to player
    */
   public void draw(Graphics2D g, double scale, boolean showConfidential) {
     g.setColor(color);
     track.draw(g, scale, showConfidential);
     train.draw(g, scale, showConfidential);
   }
 
   /**
    * Return how much money this player has.
    * 
    * @return how much money this player has
    */
   public double getMoney() {
     return money;
   }
 
   /**
    * Give money to this player, for example from track rental
    * 
    * @param extra money to give, must be non-negative
    */
   public void addMoney(double extra) {
     if (extra < 0)
       throw new IllegalArgumentException("adding negative money");
     money += extra;
     if (extra > 0)
       setChanged();
     notifyObservers(new Double(money));
   }
 
   /**
    * Return whether this player can still move. If they don't have time, return
    * false.
    * 
    * @return whether they have units of time
    */
   public boolean hasTime() {
     return time > 0;
   }
 
   /**
    * Return
    * 
    * @return
    */
   public int getTime() {
     return time;
   }
 
   /**
    * Give the player more time.
    * 
    * @param ticks game turns to add
    */
   public void addTime(int ticks) {
     time += ticks;
     if (ticks > 0)
       setChanged();
     notifyObservers();
   }
 
   /**
    * Return state of rail at this location. Return null if no rail being built
    * or fully built here. This does not take into account proposed construction.
    * 
    * @param e edge in question, may not be null
    * @return
    */
   public RailState getTrack(HexEdge e) {
     return track.getTrack(e);
   }
 
   /**
    * Return true if the player has a train on this coordinate.
    * 
    * @param h
    * @return
    */
   public boolean occupies(HexCoordinate h) {
     return h.equals(train.getLocation());
   }
 
   /**
    * Move the train (if possible) and create track (if possible).
    * <ul>
    * <li>
    * If the train hadn't started, the new coordinate must be some sort of city
    * (any size). Otherwise, the planned next coordinate must be adjacent to the
    * current one, must not be occupied (or we wait), must have track or be in a
    * large city, the track must be fully built (or wait), and we must have
    * sufficient money if the track is owned by others. Unless a condition is to
    * be waited for, then if no movement can be done, we discard the planned
    * coordinate and try the next one.
    * <li>Any existing track in the process of being built makes one unit of
    * progress toward completion.
    * <li>
    * We consider the next proposed construction. The cost must be bearable, and
    * there must not already be track there, and at least one endpoint of the
    * edge must already be connected to this player's network or be a suburb. In
    * order to move the train, the planned next coordinate This method uses up
    * one unit of time. Problems are logged onto the logger.
    */
   public void move() {
     if (!hasTime())
       throw new IllegalStateException("no time to move");
     --time;
     setChanged();
     // first move train, if possible
     moveTrain: for (;;) {
       HexCoordinate next = train.nextPlanned();
       if (next == null)
         break;
       HexCoordinate current = train.getLocation();
       if (current == null) {
         // train is starting: must be at a city of some kind
         Terrain t = game.getTerrain(next);
         if (t == null) {
           log.warning("planned nest hex not on map: " + next);
           continue;
         }
         switch (t) {
         default:
           log.warning("can only start train in town/city, not " + t);
           continue;
         case SMALL:
         case MEDIUM:
         case LARGE:
           log.info("starting train in " + next);
           train.move(next);
         break moveTrain;
         }
       }
       else if (current.distance(next) != 1) {
         log.warning("Planned next hex " + next + " not one unit from current "
             + current);
         continue;
       }
       else if (game.occupier(next) != null) {
         log.warning("destination currently occupied.  Waiting.");
         train.pushPlanned(next);
         break;
       }
       else {
         // moving train along track
         HexEdge edge = new HexEdge(current, next);
         Player trackOwner = game.occupier(edge);
         // several cases:
         // 1. trackOwner is us: free movement
         boolean usableTrack = trackOwner != null
             && trackOwner.getTrack(edge) == RailState.BUILT;
         if (usableTrack && trackOwner == this) {
           train.move(next);
           break moveTrain;
         }
         // 2. track owner can rent it to us
         else if (usableTrack) {
           double cost = game.getTrackRental().cost(game.getTerrain(current),
               game.getBarrier(edge), game.getTerrain(next));
           if (!pay(cost)) {
             log.warning("Insufficient fund to rent track, needed " + cost);
             continue;
           }
           trackOwner.addMoney(cost);
           train.move(next);
           break moveTrain;
         }
         // 3. we can travel for free if it's in the suburbs of a large city
         else if (inCity(edge)) {
           train.move(next);
           break moveTrain;
         }
         // 4. track will be built soon, just wait
         else if (trackOwner != null) {
           log.info("train waiting for track completion at " + next);
           train.pushPlanned(next);
           break moveTrain;
         }
         // 5. toss
         else {
           log.warning("no track to " + next + " ?");
         }
       }
     }
 
     track.progress(1);
 
     // build track
     for (;;) {
       HexEdge edge = track.nextProposed();
       if (edge == null)
         break;
       Pair<HexCoordinate, HexCoordinate> p = edge.getHexagons();
       final double cost = game.getConstructionCost()
           .cost(game.getTerrain(p.fst), game.getBarrier(edge),
               game.getTerrain(p.snd));
       if (cost == Double.POSITIVE_INFINITY) {
         log.warning("proposed track edge absolutely impossible: " + edge);
         continue;
       }
       if (!isConnected(p.fst) && !isConnected(p.snd)) {
         log.warning("proposed track doesn't connect with player's network: "
             + edge);
         continue;
       }
       if (game.occupier(edge) != null) {
         log.warning("proposed track already built: " + edge);
         continue;
       }
       if (!pay(cost)) {
         log.warning("construction too expensive: " + edge + " = $" + cost);
         continue;
       }
       track.constructTrack(edge);
       log.info("Constructed track: " + edge);
       break;
     }
     notifyObservers();
   }
 
   /**
    * Pay out the sum given, or fail to do so.
    * 
    * @param cost amoutn to pay out
    * @return whether payment succeeded
    */
   protected boolean pay(double cost) {
     if (money < cost)
       return false;
     if (cost != 0.0)
       setChanged();
     money -= cost;
     notifyObservers(new Double(cost));
     return true;
   }
 
   public void pickupWant() {
     want.grab();
     train.addGood(want);
   }
 
   public boolean autoMove() {
     return autoMove;
   }
 
   public void setAutoMove(boolean move) {
     autoMove = move;
   }
 
   public CardStatus getCardStatus() {
     return card;
   }
 
   /**
    * Return true whether this edge is in a large city (between suburbs or
    * between suburbs of a large city). Such edges can be travelled by any train
    * that
    * 
    * @param e edge of concern, not null
    * @return whether in a city
    */
   protected boolean inCity(HexEdge e) {
     Pair<HexCoordinate, HexCoordinate> p = e.getHexagons();
     return inCity(p.fst) && inCity(p.snd);
   }
 
   /**
    * Return true if cooridnate is a large city or a suburb.
    * 
    * @param h coordinate to examine, not null
    * @return true if terrain at this coordinate is a large city or a suburb.
    */
   protected boolean inCity(HexCoordinate h) {
     Terrain t = game.getTerrain(h);
     return t == Terrain.LARGE || t == Terrain.SUBURB;
   }
 
   /**
    * Return whether this player is already connected to the hex coordinate,
    * either because they already have track to the location or because it is a
    * suburb of a large city.
    * 
    * @param h
    * @return whether connected to this coordinate
    */
   protected boolean isConnected(HexCoordinate h) {
     if (game.getTerrain(h) == Terrain.SUBURB)
       return true;
     for (HexDirection d : HexDirection.values()) {
       if (game.occupier(new HexEdge(h, d)) == this)
         return true;
     }
     return false;
   }
 
   public void update(Observable source, Object arg) {
     setChanged();
     if (arg == null)
       arg = this;
     super.notifyObservers(arg);
   }
 
   /**
    * The player proposes to construct track here. If there already is a proposal
    * to construct track, this new proposal is ignored. If the remove flag is
    * given, we remove any existing plans for this edge.
    * 
    * @param e location of track, not null
    * @param remove remove proposal rather than add proposal
    */
   public void proposeConstruction(HexEdge e, boolean remove) {
     if (remove)
       track.removeProposal(e);
     else
       track.propose(e);
   }
 
   /**
    * Forget all proposed construction projects.
    */
   public void clearConstructionProposal() {
     track.clearProposed();
   }
 
   /**
    * Plan to move the train here after previous plans. If the remove flag is
    * set, remove plans instead of adding them. Adding the same coordinate twice
    * in succession has no effect, but adding the same coordinate after
    * intervening coordinates is assumed intentional: perhaps the train stops and
    * turns back.
    * 
    * @param h coordinate to move to, not null
    * @param remove whether to remove a plan for this coordinate rather than add
    */
   public void planMovement(HexCoordinate h, boolean remove) {
     if (remove)
       train.removePlanned(h);
     else
       train.addPlanned(h);
   }
 
   /**
    * Clear any plans for movement.
    */
   public void clearMovementPlan() {
     train.clearPlanned();
   }
 }
