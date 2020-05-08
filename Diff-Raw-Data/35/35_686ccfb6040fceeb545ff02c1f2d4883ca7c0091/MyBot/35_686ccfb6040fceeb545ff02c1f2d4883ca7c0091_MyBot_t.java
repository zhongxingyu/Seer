 import java.util.*;
 import java.io.IOException;
 import java.util.logging.Level;
 
 public class MyBot extends Bot {
   public static Level logLevel = Level.OFF;
   
   public void logFine(String str) {
     if (logLevel.intValue() <= Level.FINE.intValue()) {
       System.err.println("FINE: " + str);
     } 
   }
 
   public void logFiner(String str) {
     if (logLevel.intValue() <= Level.FINER.intValue()) {
       System.err.println("FINER: " + str);
     } 
   }
 
   public void logFinest(String str) {
     if (logLevel.intValue() <= Level.FINEST.intValue()) {
       System.err.println("FINEST: " + str);
     } 
   }
   
   Random random;
   int maxExploreAnts = 0;
 
   Map<Tile, Tile> orders     = new HashMap<Tile, Tile>();
   Set<Tile> waterTiles       = new HashSet<Tile>();
   Set<Tile> enemyHills       = new HashSet<Tile>();
   Set<Tile> enemyDeadHills   = new HashSet<Tile>();
   int enemyHillsLastTurn     = 0;
   int enemyDeadHillsLastTurn = 0;
   
   Set<Tile> attackAnts = new HashSet<Tile>();
   Map<Tile, Set<Tile>> combatAnts = null;
   
   Map<Tile,Integer> stillAnts = new HashMap<Tile,Integer>();
   
   enum Agent {
     EXPLORE,
     DEFEND,
     ENEMY_HILLS,
     ENEMY_ANTS;
   }
 
   class AgentMap extends HashMap<Agent,Integer> {}
 
   final AgentMap debugAgents  = new AgentMap();
   {
     debugAgents.put(Agent.ENEMY_HILLS, 100);
   }
   
   final AgentMap attackAgents  = new AgentMap();
   {
     attackAgents.put(Agent.EXPLORE, 15);
     attackAgents.put(Agent.DEFEND, 100);
     attackAgents.put(Agent.ENEMY_HILLS, 50);
     attackAgents.put(Agent.ENEMY_ANTS, 25);
   }
 
   final AgentMap superAttackAgents  = new AgentMap();
   {
     superAttackAgents.put(Agent.EXPLORE, 5);
     superAttackAgents.put(Agent.DEFEND, 100);
     superAttackAgents.put(Agent.ENEMY_HILLS, 50);
     superAttackAgents.put(Agent.ENEMY_ANTS, 5);
   }
   
   final AgentMap exploreAgents = new AgentMap();
   {
     exploreAgents.put(Agent.EXPLORE, 50);
     exploreAgents.put(Agent.DEFEND, 100);
     exploreAgents.put(Agent.ENEMY_HILLS, 20);
   }  
 
   final AgentMap superExploreAgents = new AgentMap();
   {
     superExploreAgents.put(Agent.EXPLORE, 50);
     superExploreAgents.put(Agent.DEFEND, 100);
   }  
 
   class AimAttackers implements Comparable<AimAttackers> {
     public final Aim aim;
     public final Set<Tile> attackers;
     
     public AimAttackers(Aim aim, Set<Tile> attackers) {
       this.aim   = aim;
       this.attackers = attackers;
     }
     
     public String toString() {
       return aim + "(" + attackers + ")";
     }
     
     public int compareTo(AimAttackers other) {
       return ((Integer) attackers.size()).compareTo(other.attackers.size());
     }
   }
 
   class CombatValues {
     final Map<Tile, Integer> antsOwners;
     final Map<Tile, List<AimAttackers>> enemyChoices;
     final Map<Tile, Set<Tile>> myPotentialAttackers;
     
     public CombatValues() {
       this(ants.getAnts());
     }
     
     public CombatValues(Map<Tile,Integer> antsOwners) {
       this.antsOwners = antsOwners;
       this.enemyChoices = new HashMap<Tile, List<AimAttackers>>();
       this.myPotentialAttackers = new HashMap<Tile, Set<Tile>>();
       
       for (Tile antLoc: antsOwners.keySet()) {
         if (antsOwners.get(antLoc) != 0) { continue; }
         
         Set<Tile> potentialAttackers = getPotentialAttackers(antLoc, 0);
         if (potentialAttackers.size() == 0) { continue; }
                 
         for (Tile enemyLoc : potentialAttackers) {
           List<AimAttackers> choices = enemyChoices.get(enemyLoc);
           if (choices != null) { continue; }
           
           choices = getEnemyChoices(enemyLoc, antsOwners.get(enemyLoc));
           // logFinest(enemyLoc + " choices: " + choices);
           enemyChoices.put(enemyLoc, choices);
         }
         
         myPotentialAttackers.put(antLoc, potentialAttackers);
         // logFinest(antLoc + " potential attackers " + potentialAttackers);
       }
     }
     
     public boolean willDie(Tile antLoc, boolean kamikaze) {
       Set<Tile> potentialAttackers = myPotentialAttackers.get(antLoc);
       if (potentialAttackers == null || potentialAttackers.size() == 0) { return false; }
 
       for (Tile potentialAttacker: potentialAttackers) {
         int numWaysMyAntDies     = 0;
         int numWaysBothAntDie    = 0;
         int numWaysEnemyAntDies  = 0;
         int numWaysNeitherAntDie = 0;
 
         List<AimAttackers> choices = enemyChoices.get(potentialAttacker);
         for (AimAttackers choice: choices) {
           if (choice.attackers.contains(antLoc)) {
             if (choice.attackers.size() > potentialAttackers.size()) {
               numWaysEnemyAntDies++;
             } else if (choice.attackers.size() == potentialAttackers.size()) {
               numWaysBothAntDie++;
             } else {
               numWaysMyAntDies++;
             }
           } else {
             numWaysNeitherAntDie++;
           }
         }
         
         if (ants.getMyAnts().size() > maxExploreAnts * 3) {
           if (numWaysEnemyAntDies >= 1 || numWaysBothAntDie >= 1) { return false; }
         } else {
           if (numWaysMyAntDies >= 1) { return true; }
           if (numWaysBothAntDie >= 1 && numWaysEnemyAntDies == 0 && !kamikaze) { return true; }
         }
       }
 
       return false;
     }
     
     public void move(Tile fromLoc, Tile toLoc) {
       // logFinest("move " + fromLoc + " to " + toLoc);
       Set<Tile> enemiesToUpdate = new HashSet<Tile>();
 
       antsOwners.remove(fromLoc);
       antsOwners.put(toLoc, 0);
       
       Set<Tile> fromAttackers = myPotentialAttackers.remove(fromLoc);
       if (fromAttackers != null) { enemiesToUpdate.addAll(fromAttackers); }
       
       Set<Tile> toAttackers = getPotentialAttackers(toLoc, 0);
       if (toAttackers.size() > 0) { 
         enemiesToUpdate.addAll(toAttackers); 
         myPotentialAttackers.put(toLoc, toAttackers);
       }
 
       for (Tile enemyToUpdate: enemiesToUpdate) {
         enemyChoices.put(enemyToUpdate, getEnemyChoices(enemyToUpdate, antsOwners.get(enemyToUpdate)));
       }
     }
         
     private List<AimAttackers> getEnemyChoices(Tile enemyLoc, int owner) {
       List<AimAttackers> choices = new ArrayList<AimAttackers>();
       choices.add(new AimAttackers(null, getActualAttackers(enemyLoc, antsOwners.get(enemyLoc))));
       for (Aim aim: Aim.values()) {
         Tile aimLoc = ants.getTile(enemyLoc, aim);
         if (waterTiles.contains(aimLoc)) { continue; }
         choices.add(new AimAttackers(aim, getActualAttackers(aimLoc, antsOwners.get(enemyLoc))));
       }
       return choices;
     }
     
     private Set<Tile> getActualAttackers(Tile tile, int owner) {
       return getTileAttackers(tile, owner, ants.getAttackOffsets());
     }
     
     private Set<Tile> getPotentialAttackers(Tile tile, int owner) {
       return getTileAttackers(tile, owner, ants.getAggressionOffsets());
     }
     
     private Set<Tile> getTileAttackers(Tile tile, int owner, Set<Tile> offsets) {
       Set<Tile> attackers = new HashSet<Tile>();
       
       for (Tile offset : offsets) {
         Tile loc = ants.getTile(tile, offset);
         Integer locOwner = antsOwners.get(loc);
             
         if (locOwner != null && locOwner != owner) {
           attackers.add(loc);
         }
       }
       
       return attackers;
     }
         
   }
 
   class AimValue implements Comparable<AimValue> {
     public final Aim aim;
     public final double value;
     
     public AimValue(Aim aim, double value) {
       this.aim   = aim;
       this.value = value;
     }
     
     public String toString() {
       return aim + "(" + value + ")";
     }
     
     public int compareTo(AimValue other) {
       return Double.compare(value, other.value);
     }
   }
 
   class Square extends Tile {
     protected final double[] values;
     protected final boolean[] initValues;
     protected int seenOnTurn;
     
     public Square(int row, int col) {
       super(row, col);
       
       this.values     = new double[Agent.values().length];
       this.initValues = new boolean[Agent.values().length];
       this.seenOnTurn = -1;
 
       setValue(Agent.values(), 0.0);
     }
 
     public void initValue(Agent agent) {
       initValues[agent.ordinal()] = false;
       switch (agent) {
         case DEFEND:
           if (ants.getEnemyAnts().contains(this)) {
             for (Tile myHill: ants.getMyHills()) {
               if (combatAnts.get(myHill) != null && combatAnts.get(myHill).contains(this)) {
                setInitValue(agent, 100);
                 break;
               }
             }
           }
           break;
         case EXPLORE:
           if (!ants.isVisible(this)) {
             if (seenOnTurn == -1) {
               setInitValue(agent, 80);
             } else {
               setInitValue(agent, Math.min(80, 40 + ((turn - seenOnTurn) * 2)));
             }
           } else {
             if (ants.getMyHills().size() > 0) {
               if (ants.getEnemyAnts().contains(this)) {
                 setInitValue(agent, 25);
               } else if (ants.getMyAnts().contains(this)) {
                 if (!combatAnts.containsKey(this)) {
                   setInitValue(agent, 0);
                 }
               }
             } else {
               if (ants.getEnemyAnts().contains(this)) {
                 setInitValue(agent, 0);  
               }
             }
             seenOnTurn = turn;
           }
           break;
         case ENEMY_HILLS:
           if (enemyHills.contains(this)) {
             setInitValue(agent, 100);
           } else if (ants.getMyHills().contains(this)) {
             setInitValue(agent, 0);
           }
           break;
         case ENEMY_ANTS:
           if (ants.getEnemyAnts().contains(this)) {
             setInitValue(agent, 100);
           }
           break;
       }
     }
     
     public double getValue(final Agent agent) {
       return values[agent.ordinal()];
     }
     
     public double getValue(final AgentMap agentMap) {
       double sum = 0.0;
       for (Map.Entry<Agent,Integer> entry: agentMap.entrySet()) {
         Agent agent    = entry.getKey();
         Integer factor = entry.getValue();
 
         sum += (values[agent.ordinal()] * factor / 100.0);
       }
       return sum;
     }
 
     public void setValue(final Agent agent, final double value) {
       if (initValues[agent.ordinal()] == false) {
         values[agent.ordinal()] = value;
       }
     }
 
     public void setValue(final Agent[] agent, final double value) {
       if (agent == null) {
         setValue(Agent.values(), value);
       } else {
         for (Agent tmpAgent : agent) {
           if (initValues[tmpAgent.ordinal()] == false) {
             values[tmpAgent.ordinal()] = value;
           }
         }
       }
     }
 
     private void setInitValue(final Agent agent, final double value) {
       values[agent.ordinal()]     = value;
       initValues[agent.ordinal()] = true;
     }
   }
 
   Map<Tile,Set<Tile>> antsHoards; 
   
   public Set<Tile> getHoard(Tile antLoc) {
     Set<Tile> hoard = antsHoards.get(antLoc);
     if (hoard != null) { return hoard; }
         
     hoard = new HashSet<Tile>();
     List<Tile> openSet = new ArrayList<Tile>();
     openSet.add(antLoc);
     
     while (openSet.size() > 0) {
       Tile tile = openSet.remove(0);
       if (hoard.contains(tile)) { continue; }
       if (!ants.getMyAnts().contains(tile)) { continue; }
 
       hoard.add(tile);
       antsHoards.put(tile, hoard);
       
       for (Aim aim: Aim.values()) {
         Tile aimLoc = ants.getTile(tile, aim);
         openSet.add(aimLoc);
       }
     }
     
     logFinest(antLoc + " hoard " + hoard);
     return hoard;
   }
 
   class Squares {
     final int rows;
     final int cols;
     final Square[][] squares;
     final double[][] tmpValues;
     final Set<Tile> deadEnds;
     
     public Squares(int rows, int cols) {
       this.rows = rows;
       this.cols = cols;
       this.squares = new Square[this.rows][this.cols];
       for (int r = 0; r < rows; r = r + 1) {
         for (int c = 0; c < cols; c = c + 1) {
           squares[r][c] = new Square(r, c);
         }
       }
       
       this.tmpValues = new double[this.rows][this.cols];
       this.deadEnds = new HashSet<Tile>();
     }
 
     public void clear(final Agent[] agents) {
       for (int r = 0; r < rows ; r++) {
         for (int c = 0; c < cols; c++) {
           squares[r][c].setValue(agents, 0.0);
         }
       }
     }
 
     public void influence(Agent agent) {
       for (int r = 0; r < rows ; r++) {
         for (int c = 0; c < cols; c++) {
           squares[r][c].initValue(agent);
         }
       }
 
       for (int r = 0; r < rows ; r++) {
         for (int c = 0; c < cols; c++) {
           double maxValue = 0.0;
           if (shouldDiffuse(squares[r][c])) {
             if (shouldDiffuse(squares[(rows + r - 1) % rows][c])) {
               maxValue = Math.max(maxValue, squares[(rows + r - 1) % rows][c].getValue(agent));
             }
             if (shouldDiffuse(squares[(r + 1) % rows][c])) {
               maxValue = Math.max(maxValue, squares[(r + 1) % rows][c].getValue(agent));
             }
             if (shouldDiffuse(squares[r][(cols + c - 1) % cols])) {
               maxValue = Math.max(maxValue, squares[r][(cols + c - 1) % cols].getValue(agent));
             }
             if (shouldDiffuse(squares[r][(c + 1) % cols])) {            
               maxValue = Math.max(maxValue, squares[r][(c + 1) % cols].getValue(agent));
             }
 
             maxValue = maxValue * 0.975;
           }
           tmpValues[r][c] = maxValue;
         }
       }
       
       for (int r = 0; r < rows ; r++) {
         for (int c = 0; c < cols; c++) {
           squares[r][c].setValue(agent, tmpValues[r][c]);
         }
       }
     }
 
     public void diffuse(Agent agent) {
       // long t0 = System.currentTimeMillis();
 
       for (int r = 0; r < rows ; r++) {
         for (int c = 0; c < cols; c++) {
           squares[r][c].initValue(agent);
         }
       }
       
       for (int r = 0; r < rows ; r++) {
         for (int c = 0; c < cols; c++) {
           double sum = 0.0;
           double sides = 0;
           if (shouldDiffuse(squares[r][c])) {
             if (shouldDiffuse(squares[(rows + r - 1) % rows][c])) {
               sum = sum + squares[(rows + r - 1) % rows][c].getValue(agent);
               sides++;
             }
             if (shouldDiffuse(squares[(r + 1) % rows][c])) {
               sum = sum + squares[(r + 1) % rows][c].getValue(agent);
               sides++;
             }
             if (shouldDiffuse(squares[r][(cols + c - 1) % cols])) {
               sum = sum + squares[r][(cols + c - 1) % cols].getValue(agent);
               sides++;
             }
             if (shouldDiffuse(squares[r][(c + 1) % cols])) {            
               sum = sum + squares[r][(c + 1) % cols].getValue(agent);
               sides++;
             }
             if (sides <= 1) {
               deadEnds.add(squares[r][c]);
             }
             sum = sum / sides;
           }
           tmpValues[r][c] = sum;
         }
       }
       
       for (int r = 0; r < rows ; r++) {
         for (int c = 0; c < cols; c++) {
           squares[r][c].setValue(agent, tmpValues[r][c]);
         }
       }
       // System.err.println("diffusion time: " + (System.currentTimeMillis() - t0));
     }
     
     private boolean shouldDiffuse(Square square) {
       return !waterTiles.contains(square) && !deadEnds.contains(square);
     }
     
     public void print(AgentMap agentMap) {
       for (int r = 0; r < rows ; r++) {
         for (int c = 0; c < cols; c++) {
           if (waterTiles.contains(squares[r][c])) {
             System.err.print(' ');
           } else if (deadEnds.contains(squares[r][c])) {
             System.err.print('-');
           } else {
             double value = squares[r][c].getValue(agentMap);
             if (value >= 0) {
               System.err.print(value > 100 ? '+' : ((char) ((value / 100.0 * 25) + 'A')));  
             } else {
               System.err.print(-value > 100 ? '-' : ((char) ((-value / 100.0 * 25) + 'a')));  
             }
           }
         }
         System.err.println();
       }
       System.err.println();
     }
     
     public void printRaw(AgentMap agentMap) {
       for (int r = 0; r < rows; r++) {
         for (int c = 0; c < cols; c++) {
           if (waterTiles.contains(squares[r][c])) {
             System.err.print("    ");
           } else if (deadEnds.contains(squares[r][c])) {
             System.err.print("  --");
           } else {
             double value = squares[r][c].getValue(agentMap);
             System.err.print(String.format("%4.0f", value));
           }
         }
         System.err.println();
       }
       System.err.println();
     }
     
     public List<AimValue> getDirectionsFor(final Tile tile) {
       if (ants.getMyHills().size() > 0) {
         if (enemyHills.size() > 0 && ((ants.getMyAnts().size() - attackAnts.size()) > maxExploreAnts)) {
           return attackAnts.contains(tile) ? getDirections(superAttackAgents, tile, -1) : getDirections(superExploreAgents, tile, -1);
         } else {
           return attackAnts.contains(tile) ? getDirections(attackAgents, tile, -1) : getDirections(exploreAgents, tile, -1);
         }
       } else {
         return getDirections(exploreAgents, tile, -1);
       }
     }
     
     public List<AimValue> getDirections(final AgentMap agentMap, final Tile tile, final int ascending) {
       final ArrayList<AimValue> values = new ArrayList<AimValue>();
       final int r = tile.getRow();
       final int c = tile.getCol();
       
       values.add(new AimValue(Aim.NORTH, squares[(rows + r - 1) % rows][c].getValue(agentMap)));
       values.add(new AimValue(Aim.SOUTH, squares[(r + 1) % rows][c].getValue(agentMap)));
       values.add(new AimValue(Aim.WEST, squares[r][(cols + c - 1) % cols].getValue(agentMap)));
       values.add(new AimValue(Aim.EAST, squares[r][(c + 1) % cols].getValue(agentMap)));
       values.add(new AimValue(null, squares[r][c].getValue(agentMap)));
       
       if (ascending > 0) {
         Collections.sort(values);
       } else {
         Collections.sort(values, Collections.reverseOrder());
       }
       return values;
     }
 
     public boolean hasBeenSeen(final Tile tile) {
       return squares[tile.getRow()][tile.getCol()].seenOnTurn != -1;
     }
 
     // public List<AimValue> getProbabilisticDirections(final Agent agent, final Tile tile, final int ascending) {
     //   List<AimValue> values = getDirections(agent, tile, ascending);
     //   List<AimValue> result = new ArrayList<AimValue>(values);
     //   double tileValue = getValue(agent, tile);
     //   
     //   for (AimValue value : values) {
     //     if (random.nextInt(100) < 20) {
     //       // move to the end
     //       result.remove(value);
     //       result.add(value);
     //     }
     //   }
     //   
     //   return result;
     // }
     
     // public double getValue(final Agent agent, final Tile tile) {
     //   final int r = tile.getRow();
     //   final int c = tile.getCol();
     //   return squares[r][c].getValue(agent);
     // }
     // 
     // public double getValue(final Agent agent, final Tile tile, Aim direction) {
     //   return getValue(agent, direction != null ? ants.getTile(tile, direction) : tile);
     // }
     
   }
   Squares squares;
   int turn = 0;
 
   public static void main(String[] args) throws IOException {
     if (System.getProperty("logLevel") != null) {
       try {
         logLevel = Level.parse(System.getProperty("logLevel"));
       } catch (IllegalArgumentException e) {  
       } catch (NullPointerException e) {
       }
     }
     
     new MyBot().readSystemInput();
   }
   
   public void setup(int loadTime, int turnTime, int rows, int cols, int turns, int viewRadius2, int attackRadius2, int spawnRadius2) {
     super.setup(loadTime, turnTime, rows, cols, turns, viewRadius2, attackRadius2, spawnRadius2);
 
     random = new Random(1);
     squares = new Squares(rows, cols);
     
     maxExploreAnts = (int) (rows * cols / viewRadius2 / 2.6);
     logFine("maxExploreAnts: " + maxExploreAnts);
   }
   
   public void afterUpdate() {
     long t0 = System.currentTimeMillis();
     super.afterUpdate();
 
     orders.clear();
     for (Tile myHill : ants.getMyHills()) {
       orders.put(myHill, null);
     }
 
     // Remove hills that are no longer active
     for (Iterator<Tile> it = enemyHills.iterator(); it.hasNext(); ) {
       Tile enemyHill = it.next();
       if (ants.isVisible(enemyHill) && !ants.getEnemyHills().contains(enemyHill)) {
         enemyDeadHills.add(enemyHill);
         it.remove();
       }
     }
     
     waterTiles.addAll(ants.getWaterTiles());
     enemyHills.addAll(ants.getEnemyHills());
     
     combatAnts = new HashMap<Tile, Set<Tile>>();
     
     Set<Tile> locs = new HashSet<Tile>();
     locs.addAll(ants.getMyAnts());
     locs.addAll(ants.getMyHills());
     
     for (Tile antLoc : locs) {
       Set<Tile> enemySet = new HashSet<Tile>();
       
       for (Tile offset : ants.getVisionOffsets()) {
         Tile loc = ants.getTile(antLoc, offset);
         if (ants.getEnemyAnts().contains(loc) && new Router(antLoc, loc).directions(10) != null) {
           enemySet.add(loc);
         }
       }
       
       if (!enemySet.isEmpty()) {
         combatAnts.put(antLoc, enemySet);
       }
     }
 
     attackAnts.retainAll(ants.getMyAnts());
 
     stillAnts.keySet().retainAll(ants.getMyAnts());
     for (Tile antLoc : ants.getMyAnts()) {
       if (stillAnts.containsKey(antLoc)) {
         stillAnts.put(antLoc, stillAnts.get(antLoc) + 1);
       } else {
         stillAnts.put(antLoc, 0);
       }
     }
 
     antsHoards = new HashMap<Tile,Set<Tile>>();
 
     logFine("afterUpdate: " + (System.currentTimeMillis() - t0));
   }
   
   private boolean moveDirection(Tile antLoc, Aim direction) {
     return moveDirection(antLoc, direction, orders);
   }
 
   private boolean moveDirection(Tile antLoc, Aim direction, Map<Tile,Tile> orders) {
     if (direction == null) {
       if (!orders.containsKey(antLoc)) {
         orders.put(antLoc, antLoc);
         return true;
       } else {
         return false;
       }
     }
     
     Ants ants = getAnts();
     
     Tile newLoc = ants.getTile(antLoc, direction);
     if (!waterTiles.contains(newLoc) && !orders.containsKey(newLoc)) {
       orders.put(newLoc, antLoc);
       return true;
     } else {
       return false;
     }
   }
 
   public void moveAntsOnTargets(Set<Tile> targets, int maxDistance, Map<Tile,Tile> tmpOrders) {
     PriorityQueue<Route> possibleRoutes = new PriorityQueue<Route>();
     Set<Tile> antsOnTarget = new HashSet<Tile>();
     Route route = null;
 
     for (Tile foodLoc : targets) {
       for (Tile antLoc : ants.getMyAnts()) {
         if (tmpOrders.containsValue(antLoc)) { continue; }
 
         List<Aim> directions = new Router(antLoc, foodLoc).directions(maxDistance);
         if (directions != null) {
           route = new Route(antLoc, foodLoc, directions);
           possibleRoutes.offer(route);
         }
       }
     }
     
     while ((route = possibleRoutes.poll()) != null) {
       if (antsOnTarget.contains(route.getEnd())) { continue; }
       if (tmpOrders.containsValue(route.getStart())) { continue; }
 
       Aim aim = route.getDirections().size() == 1 ? null : route.getDirections().get(0);
       if (moveDirection(route.getStart(), aim, tmpOrders)) {
         antsOnTarget.add(route.getEnd());
         // logFiner(route.getStart() + " -> " + route.getEnd() + " FOOD " + aim);
       }
     }
   }
 
   int minStillTurns = 3;
 
   public boolean chargeCapableAnt(Tile antLoc, Aim aim) {
     if (!ants.getMyAnts().contains(antLoc)) { return false; }
     
     Integer stillTurns = stillAnts.get(antLoc);
     if (stillTurns == null || stillTurns < minStillTurns) { return false; }
     
     Tile aheadLoc = ants.getTile(antLoc, aim);
     if (waterTiles.contains(aheadLoc) || ants.getMyAnts().contains(aheadLoc)) { return false; }
     
     Tile behindLoc = ants.getTile(antLoc, aim.opposite());
     if (!ants.getMyAnts().contains(behindLoc)) { return false; }
     
     return true;
   }
 
   public void moveChargeAnts() {
     logFinest("stillAnts: " + stillAnts);
     
     Collection<AimAttackers> aimsAttackers = new PriorityQueue<AimAttackers>(11, Collections.reverseOrder());
     
     mainStillLoop:
     for (Map.Entry<Tile,Integer> entry: stillAnts.entrySet()) {
       Tile antLoc = entry.getKey();
       
       if (orders.containsValue(antLoc)) { continue; }
       if (entry.getValue() < minStillTurns) { continue; } // ants hasn't been still for long enough
       for (Tile myHill: ants.getMyHills()) {
         if (ants.getDistance(myHill, antLoc) <= ants.getViewRadius2()) {
           continue mainStillLoop;
         }
       }
 
       // logFiner("stillAnt: " + entry.getKey() + " for " + entry.getValue() + " turns");
       
       List<AimValue> directions = squares.getDirectionsFor(antLoc);
       Aim aim = directions.remove(0).aim;
       if (aim == null) { 
         aim = directions.remove(0).aim; 
       }
         
       if (!chargeCapableAnt(antLoc, aim)) { 
         // logFiner("-> not capable of charging towards " + aim);
         continue;
       }
 
       for (Aim primaryAim: new Aim[] { null, aim, aim.opposite() }) {
         Set<Tile> waveAnts = new HashSet<Tile>();
         waveAnts.add(antLoc);
 
         for (Aim sidewayAim: aim.sideways()) {
           Tile sidewayLoc = ants.getTile(antLoc, sidewayAim);
           if (primaryAim != null) { sidewayLoc = ants.getTile(sidewayLoc, primaryAim); }
           // logFiner(sidewayAim + " " + primaryAim + " considering " + sidewayLoc);
           while (chargeCapableAnt(sidewayLoc, aim)) {
             waveAnts.add(sidewayLoc);
             sidewayLoc = ants.getTile(sidewayLoc, sidewayAim);
             if (primaryAim != null) { sidewayLoc = ants.getTile(sidewayLoc, primaryAim); }
             // logFiner(sidewayAim + " " + primaryAim + " considering " + sidewayLoc);
           }
 
           if (primaryAim != null) { primaryAim = primaryAim.opposite(); }
         }
 
         if (waveAnts.size() < 3 && getHoard(antLoc).size() < maxExploreAnts / 3) { continue; }
                 
         aimsAttackers.add(new AimAttackers(aim, waveAnts));
         continue mainStillLoop;
       }
     }
 
     mainAttackersLoop:
     for (AimAttackers aimAttackers: aimsAttackers) {
       for (Tile waveAnt: aimAttackers.attackers) {
         if (orders.containsValue(waveAnt)) { continue mainAttackersLoop; }
       }
       
       logFiner("wavelet ants (" + aimAttackers.attackers + ") -> " + aimAttackers.aim);
       for (Tile waveAnt: aimAttackers.attackers) {
         if (!moveDirection(waveAnt, aimAttackers.aim, orders)) {
           logFiner(waveAnt + " !!! " + aimAttackers.aim);
         }
       } 
     }
   }
 
   public void moveFoodAnts(Map<Tile,Tile> tmpOrders) {
     moveAntsOnTargets(ants.getFoodTiles(), 10, tmpOrders);
   }
 
   public void moveAnts() {
     Map<Tile,Tile> tmpOrders = new HashMap<Tile,Tile>(orders);
     Map<Tile, List<AimValue>> antsDirections = new HashMap<Tile, List<AimValue>>();
     Set<Tile> kamikazeAnts = new HashSet<Tile>();
 
     for (Tile antLoc : ants.getMyHills()) {
       if (!ants.getMyAnts().contains(antLoc)) { continue; }
       
       if (((ants.getMyAnts().size() - attackAnts.size()) > maxExploreAnts) || (ants.getMyAnts().size() / 2 > attackAnts.size())) {
         attackAnts.add(antLoc);
       }
     }
 
     moveFoodAnts(tmpOrders);
     
     // Try to move ants towards highest explore goals.
     for (Tile antLoc : ants.getMyAnts()) {
       if (tmpOrders.containsValue(antLoc)) { continue; }
       
       // For ants close to my hills be more aggressive about attacking enemy ants (even if it means dying) to prevent
       // a lucky enemy ant from killing my hive.
       for (Tile myHill: ants.getMyHills()) {
         if (ants.getDistance(myHill, antLoc) <= ants.getViewRadius2()) {
           kamikazeAnts.add(antLoc);
           break;
         }
       }
       
       List<AimValue> directions = squares.getDirectionsFor(antLoc);
       antsDirections.put(antLoc, directions);
       
       // logFiner(antLoc + " " + directions + (attackAnts.contains(antLoc) ? " attack" : "") + (kamikazeAnts.contains(antLoc) ? " kamikaze" : ""));
       while (directions.size() > 0) {
         AimValue aimValue = directions.remove(0);
         if (aimValue.aim == null && directions.size() > 0) { 
           directions.add(aimValue); 
           aimValue = directions.remove(0);
         }
                 
         if (moveDirection(antLoc, aimValue.aim, tmpOrders)) {
           // logFiner(" -> " + aimValue.aim);
           break;
         }
       }
 
       // to -> from
       if (!tmpOrders.containsValue(antLoc)) { // haven't moved antLoc
         while (antLoc != null) {
           antLoc = tmpOrders.put(antLoc, antLoc); // move antLoc onto itself .. is there another ant?
         }
       }
     }
 
     Map<Tile,Integer> tmpAnts = new HashMap<Tile,Integer>(ants.getAnts());
     for (Map.Entry<Tile,Tile> entry: tmpOrders.entrySet()) {
       tmpAnts.remove(entry.getValue());
       tmpAnts.put(entry.getKey(), 0);
     }
     CombatValues combatValues = new CombatValues(tmpAnts);
 
     // Prevent ants from dying.
     while (tmpOrders.size() > 0) {
       Tile origAntLoc = null;
       Tile prevAntLoc = null;
 
       for (Iterator<Map.Entry<Tile,Tile>> it = tmpOrders.entrySet().iterator(); it.hasNext(); ) {
         Map.Entry<Tile,Tile> tmpOrder = it.next();
         Tile newLoc = tmpOrder.getKey();
         Tile oldLoc = tmpOrder.getValue();
 
         if (orders.containsValue(oldLoc)) { continue; } // skip real orders
         
         if (combatValues.willDie(newLoc, kamikazeAnts.contains(oldLoc))) {
           origAntLoc = oldLoc;
           prevAntLoc = newLoc;
           
           it.remove();
           // logFiner(oldLoc + " ->  " + newLoc + " will die");
           break;
         }
       }
 
       if (origAntLoc == null) { break; }
 
       List<AimValue> directions = antsDirections.get(origAntLoc);
       if (directions == null) {
         directions = squares.getDirectionsFor(origAntLoc);
         antsDirections.put(origAntLoc, directions);
       }
       // logFiner(origAntLoc + " " + directions);
       
       while (directions.size() > 0) {
         AimValue aimValue = directions.remove(0);
 
         if (moveDirection(origAntLoc, aimValue.aim, tmpOrders)) {
           Tile nextAntLoc = aimValue.aim == null ? origAntLoc : ants.getTile(origAntLoc, aimValue.aim);
           combatValues.move(prevAntLoc, nextAntLoc);
           
           // logFiner(" -> " + aimValue.aim);
           break;
         }
       }
       
       // to -> from
       if (!tmpOrders.containsValue(origAntLoc)) { // haven't moved antLoc
         Tile lastOrigAntLoc = null;
         while (origAntLoc != null) {
           lastOrigAntLoc = origAntLoc;
           orders.put(origAntLoc, origAntLoc);
           origAntLoc = tmpOrders.put(origAntLoc, origAntLoc); // move antLoc onto itself .. is there another ant?
         }
         combatValues.move(prevAntLoc, lastOrigAntLoc);
       }
     }
     
     orders = tmpOrders;
   }
 
   public void issueOrders() {
     Set<Tile> newAttackAnts = new HashSet<Tile>();
     
     for (Map.Entry<Tile,Tile> order : orders.entrySet()) {
       Tile newLoc = order.getKey();
       Tile oldLoc = order.getValue();
       
       if (newLoc != null && oldLoc != null && !newLoc.equals(oldLoc)) {
         List<Aim> aims = ants.getDirections(oldLoc, newLoc);
         ants.issueOrder(oldLoc, aims.get(0));
       }
       
       if (attackAnts.contains(oldLoc)) {
         newAttackAnts.add(newLoc);
       }
     }
     
     attackAnts = newAttackAnts;
   }
 
   int accelerateAttackDiffusion = 0;
   public void diffuse() {
     squares.clear(new Agent[] { Agent.ENEMY_ANTS, Agent.DEFEND });
 
     boolean hillsUnderAttack = false;
     for (Tile myHill: ants.getMyHills()) {
       if (combatAnts.containsKey(myHill)) {
         hillsUnderAttack = true;
         break;
       }
     }
     
     for (int i = 0; i < Math.min(turn, 10); i++) {
       squares.diffuse(Agent.EXPLORE);
       squares.diffuse(Agent.ENEMY_ANTS);
       if (hillsUnderAttack) {
         squares.diffuse(Agent.DEFEND);
       }
     }
 
    // boolean enemyHillDiscovered = enemyHillsLastTurn == 0 && enemyHills.size() > 0;
    // boolean enemyHillDied       = enemyDeadHillsLastTurn < enemyDeadHills.size();
    // enemyHillsLastTurn = enemyHills.size();
    // enemyDeadHillsLastTurn = enemyDeadHills.size();
    // 
    // if (enemyHillDied) {
    //   // logFiner("enemy hill died or discovered");
    //   squares.clear(new Agent[] { Agent.ENEMY_HILLS });
    //   
    //   // if (enemyHills.size() > 0) {
    //   //   accelerateAttackDiffusion = 5;
    //   // }
    // }
 
     // if (accelerateAttackDiffusion > 0) {
     //   for (int i = 0; i < 100; i++) {
     //     squares.influence(Agent.ENEMY_HILLS);
     //   }
     //   // logFiner(">>>>>> attack");
     //   // squares.printRaw(attackAgents);
     //   // 
     //   // logFiner(">>>>>> super attack");
     //   // squares.printRaw(superAttackAgents);
     //   
     //   accelerateAttackDiffusion--;
     // }
    // if (enemyHills.size() > 0 && turn > 10) {
       for (int i = 0; i < 50; i++) {
         squares.influence(Agent.ENEMY_HILLS);
       }
    // }
     
     //squares.printRaw(attackAgents);
   }
   
   public void doTurn() {
     long t0;
 
     t0 = System.currentTimeMillis();
     diffuse();
     logFine("diffusion: " + (System.currentTimeMillis() - t0));
 
     t0 = System.currentTimeMillis();
     moveChargeAnts();
     logFine("moveChargeAnts: " + (System.currentTimeMillis() - t0));
     
     t0 = System.currentTimeMillis();
     moveAnts();
     logFine("moveAnts: " + (System.currentTimeMillis() - t0));
     
     t0 = System.currentTimeMillis();
     issueOrders();
     logFine("issueOrders: " + (System.currentTimeMillis() - t0));
     
     logFine("ants died: " + ants.getMyDeadAnts().size());
     logFine("explore ants (" + maxExploreAnts + "): " + (ants.getMyAnts().size() - attackAnts.size()) + " attack ants: " + attackAnts.size() + " enemy hills: " + enemyHills.size());
     logFine("----- done ----- " + ants.getTimeRemaining());
     turn++;
   }
   
   class Router {
     Tile from;
     Tile to;
     
     Set<Tile> closedSet         = new HashSet<Tile>();
     Set<Tile> openSet           = new HashSet<Tile>();
     Map<Tile,Object[]> cameFrom = new HashMap<Tile,Object[]>();
 
     Map<Tile,Float> gScore = new HashMap<Tile,Float>();
     Map<Tile,Float> hScore = new HashMap<Tile,Float>();
     Map<Tile,Float> fScore = new HashMap<Tile,Float>();
     TreeMap<Tile,Float> fSorted = new TreeMap<Tile, Float>(new Comparator<Tile>() {
       public int compare(Tile a, Tile b) {
         return Double.compare(fScore.get(a), fScore.get(b));
       }
     });
     
     public Router(Tile from, Tile to) {
       this.from = from;
       this.to   = to;
 
       openSet.add(from);
       
       gScore.put(from, 0.0f);
       hScore.put(from, (float) ants.getManhattanDistance(from, to));
       fScore.put(from, gScore.get(from) + hScore.get(from));
     }
 
     public List<Aim> directions() {
       return directions(-1);
     }
     
     public List<Aim> directions(int maxDistance) {
       Ants ants = getAnts();
 
       while (!openSet.isEmpty()) {
         fSorted.clear();
         for (Tile x: openSet) {
           if (maxDistance == -1 || fScore.get(x).intValue() <= maxDistance) { fSorted.put(x, fScore.get(x)); }
         }
         if (fSorted.isEmpty()) {
           break;
         }
         
         Tile x = fSorted.firstKey();
 
         // System.err.println("first sorted: " + x);
         if (x.equals(to)) { 
           return reconstructPath(cameFrom, to);
         }
 
         openSet.remove(x);
         closedSet.add(x);
 
         for (Aim direction : Aim.values()) {
           Tile y = ants.getTile(x, direction);
           if (closedSet.contains(y))         { continue; }
           if (waterTiles.contains(y))        { continue; }
           if (ants.getMyHills().contains(y)) { continue; }
           if (!squares.hasBeenSeen(y))       { continue; }
 
           // System.err.println("considering: " + y);
           float distance = 1.0f;
           // if (ants.isVisible(y)) {
           //   Ilk ilk = ants.getIlk(y);
           //   if (orders.containsKey(y)) {
           //     distance = 2.0f;
           //   }
           // }
 
           float yGScore = gScore.get(x) + distance;
           if (!openSet.contains(y) || yGScore < gScore.get(y)) {
             openSet.add(y);
 
             Object[] fromDir = new Object[2];
             fromDir[0] = x;
             fromDir[1] = direction;
             cameFrom.put(y, fromDir);
             gScore.put(y, yGScore);
             hScore.put(y, (float) ants.getManhattanDistance(y, to));
             fScore.put(y, gScore.get(y) + hScore.get(y));
           }
         }
       }
       return null;
     }
 
     private List<Aim> reconstructPath(Map<Tile,Object[]> cameFrom, Tile tile) {
       Object[] from = cameFrom.get(tile);
       if (from != null) {
         List<Aim> path = reconstructPath(cameFrom, (Tile) from[0]);
         path.add((Aim) from[1]);
         return path;
       } else {
         return new ArrayList<Aim>();
       }
     }
     
   }
   
 }
