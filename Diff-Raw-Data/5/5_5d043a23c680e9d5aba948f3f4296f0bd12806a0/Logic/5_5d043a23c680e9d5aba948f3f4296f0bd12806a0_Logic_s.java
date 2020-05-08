 package com.group7.dragonwars.engine;
 
 import java.util.*;
 import java.lang.Math;
 import android.util.Log;
 
 /* Class containing things like damage calculation and path finding. */
 public class Logic {
 
     private final static String TAG = "Logic";
 
     public List<Position> findPath(GameMap map, Unit unit, Position destination) {
         return AStar(map, unit, destination);
     }
 
     public Integer calculateMovementCost(GameMap map, Unit unit, List<Position> path) {
         Double totalCost = 0.0;
         for (Position pos : path) {
             if (pos.equals(unit.getPosition())) {
                 continue;
             }
             totalCost += getMovementCost(map, unit, pos);
         }
 
         totalCost = Math.ceil(totalCost);
 
 
         return totalCost.intValue();
     }
 
     public List<Position> destinations(GameMap map, Unit unit) {
         Set<Position> checked = new HashSet<Position>();
         Set<Position> reachable = new HashSet<Position>();
 
         Position unitPosition = unit.getPosition();
         List<Node> start
             = new ArrayList<Node>();
         start.add(new Node(unitPosition, 0.0, 0.0));
 
         checked.add(unitPosition);
         reachable.add(unitPosition);
 
         List<Node> next = nextPositions(map, start);
         while (next.size() != 0) {
             List<Node> newNext = new ArrayList<Node>();
             for (Node n : next) {
                 checked.add(n.getPosition());
                 if (unit.getRemainingMovement() < n.getG()) {
                     continue;
                 }
 
                 if (map.getField(n.getPosition()).doesAcceptUnit(unit)) {
                     if (map.getField(n.getPosition()).hostsUnit()) {
                         Player op = map.getField(n.getPosition())
                             .getUnit().getOwner();
 
                         if (!op.equals(unit.getOwner())) {
                             continue;
                         }
                     }
                     reachable.add(n.getPosition());
                     List<Node> thisNext = new ArrayList<Node>(5);
                     thisNext.add(n);
                     thisNext = nextPositions(map, thisNext);
 
                     for (Node thisNode : thisNext) {
                         if (!checked.contains(thisNode.getPosition())) {
                             newNext.add(thisNode);
                         }
                     }
                 }
             }
             next = newNext;
         }
 
         List<Position> shown = new ArrayList<Position>();
         for (Position p : reachable) {
             if (map.getField(p).canBeStoppedOn()) {
                 shown.add(p);
             }
         }
 
         return shown;
     }
 
     public List<Node>
         nextPositions(GameMap map, List<Node> toCheck) {
 
         List<Node> result = new ArrayList<Node>();
 
         for (Node n : toCheck) {
             Double costSoFar = n.getG();
             Position currentPosition = n.getPosition();
             List<Position> adj = getValidNeighbours(map, currentPosition);
 
             for (Position pos : adj) {
                 GameField cField = map.getField(pos);
                 Double newCost = costSoFar + cField.getMovementModifier();
                 result.add(new Node(pos, newCost, 0.0));
             }
         }
 
         return result;
     }
 
 
 
     public Pair<Double, Double> calculateDamage(GameMap map, Unit attacker,
             Unit defender) {
         return new Pair<Double, Double>
                (calculateRawDamage(map, attacker, defender),
                 calculateCounterDamage(map, attacker, defender));
     }
 
     public Double calculateRawDamage(GameMap map, Unit attacker, Unit defender) {
         GameField defenderField = map.getField(defender.getPosition());
 
         Double fieldDefense = defenderField.getDefenseModifier() - 1;
         Double unitDefense = attacker.isRanged() ? defender.getRangeDefense() : defender.getMeleeDefense() - 1;
 
         Double damage = attacker.getAttack() +
                         (2 * attacker.getAttack() *
                         (attacker.getHealth()/attacker.getMaxHealth()));
 
         Double finalDamage = damage - (((fieldDefense * damage) / 2) + ((unitDefense * damage) / 2));
         Log.v(null, "finalDamage: " + finalDamage + " damage: " + damage + " unitDefense: " + unitDefense + " fieldDefense: " + fieldDefense);
         return (attacker.getHealth() > 0.0 ? finalDamage : 0.0);
 
         /*Double attackerMod = attackerField.getAttackModifier();
         Double defenderMod = defenderField.getDefenseModifier();
 
         Double defense = defender.getHealth() * (defenderMod / 100);
         defense *= DEFENDER_DISADVANTAGE;
 
         Double rawDamage = attacker.getHealth() * (attackerMod / 100);
 
         Double damage = rawDamage - defense;
 
         return (defense < 0) ? 0 : damage;*/
     }
 
     public Double calculateCounterDamage(GameMap map, Unit attacker, Unit defender) {
         Double initialDamage = calculateRawDamage(map, attacker, defender);
         Double defenderHealth = defender.getHealth() - initialDamage;
         defenderHealth = (defenderHealth < 0) ? 0 : defenderHealth;
 
         return calculateTheoreticalCounterDamage(map, defender, attacker,
                 defenderHealth);
     }
 
     private Double calculateTheoreticalCounterDamage(GameMap map, Unit attacker,
             Unit defender, Double atkHealth) {
         GameField defenderField = map.getField(defender.getPosition());
 
         double fieldDefense = defenderField.getDefenseModifier() - 1;
         double unitDefense = attacker.isRanged() ? defender.getRangeDefense() : defender.getMeleeDefense() - 1;
 
         double damage = attacker.getAttack() +
                         (2 * attacker.getAttack() *
                         (atkHealth/attacker.getMaxHealth()));
 
         double finalDamage = damage - (((fieldDefense * damage) / 2) + ((unitDefense * damage) / 2));
         Log.v(null, "finalDamage: " + finalDamage + " damage: " + damage + " unitDefense: " + unitDefense + " fieldDefense: " + fieldDefense);
 
         return attacker.getHealth() > 0 ? finalDamage : 0;
 
         /*
         // No defense disadvantage on a counter.
         Double attackerMod = attackerField.getAttackModifier();
         Double defenderMod = defenderField.getDefenseModifier();
 
         Double defense = defender.getHealth() * (defenderMod / 100);
 
         Double rawDamage = atkHealth * (attackerMod / 100);
 
         Double damage = rawDamage - defense;
 
         return (defense < 0) ? 0 : damage;
         */
     }
 
     private List<Position> AStar(GameMap map, Unit unit, Position destination) {
         if (!map.isValidField(destination))
             return new ArrayList<Position>(0);
 
         PriorityQueue<Node> openSet
             = new PriorityQueue<Node>(10, new AStarComparator());
         Set<Node> closedSet = new HashSet<Node>();
 
         Node root = new Node(unit.getPosition(), 0.0,
                              1.0 * getManhattanDistance(
                                  unit.getPosition(), destination));
         openSet.add(root);
 
         while (openSet.size() != 0) {
             Node current = openSet.poll();
 
             if (current.getPosition().equals(destination)) {
                 return reconstructPath(current);
             }
 
             closedSet.add(current);
 
             for (Position n : getValidNeighbours(map, current.getPosition())) {
                 GameField gf = map.getField(n);
                 Node neigh = new Node(n, gf.getMovementModifier(),
                                       1.0 * getManhattanDistance(
                                           unit.getPosition(), destination));
                //neigh.setParent(current);
 
                 Double tentG = current.getG() + neigh.getG();
 
                 if (closedSet.contains(neigh)) {
                     if (tentG >= neigh.getG()) {
                         continue;
                     }
                 }
 
                 if ((!openSet.contains(neigh)) || tentG < neigh.getG()) {
                     neigh.setParent(current);
                     if (!openSet.contains(neigh)) {
                         openSet.add(neigh);
                     }
                 }
             }
         }
 
         return new ArrayList<Position>(0); /* Search failed */
 
     }
 
     private List<Position> getValidNeighbours(GameMap map, Position pos) {
         List<Position> positions = new ArrayList<Position>(4);
         positions.add(new Position(pos.getX(), pos.getY() + 1));
         positions.add(new Position(pos.getX(), pos.getY() - 1));
         positions.add(new Position(pos.getX() + 1, pos.getY()));
         positions.add(new Position(pos.getX() - 1, pos.getY()));
         List<Position> validPositions = new ArrayList<Position>(4);
         for (Position p : positions) {
             if (map.isValidField(p)) {
                 validPositions.add(p);
             }
         }
 
         return validPositions;
     }
 
     private List<Position> getAdjacentPositions(Position pos) {
         List<Position> positions = new ArrayList<Position>();
         positions.add(new Position(pos.getX(), pos.getY() + 1));
         positions.add(new Position(pos.getX(), pos.getY() - 1));
         positions.add(new Position(pos.getX() + 1, pos.getY()));
         positions.add(new Position(pos.getX() - 1, pos.getY()));
         return positions;
     }
 
     private class AStarComparator implements
         Comparator<Node> {
         public int compare(Node a, Node b) {
             Double t = a.getF() - b.getF();
             if (t > 0) {
                 return 1;
             }
 
             if (t < 0) {
                 return -1;
             }
 
             return 0;
         }
     }
 
     private List<Position> reconstructPath(Node node) {
         List<Position> path = new ArrayList<Position>();
         path.add(node.getPosition());
         Node parent = node.getParent();
         while (parent != null) {
             path.add(parent.getPosition());
             parent = parent.getParent();
         }
         return path;
     }
 
     private class Node {
         private Node parent;
         private Position p;
         private Double g, h;
 
         public Node(Position p, Double g, Double h) {
             this.p = p;
             this.g = g;
             this.h = h;
         }
 
         public Node getParent() {
             return parent;
         }
 
         public void setParent(Node parent) {
             this.parent = parent;
             this.g = this.g + parent.getG();
         }
 
         public Double getH() {
             return h;
         }
 
         public Double getG() {
             return g;
         }
 
         public Double getF() {
             return h + g;
         }
 
         public Position getPosition() {
             return p;
         }
 
         @Override
         public boolean equals(Object other) {
             if (this == other) {
                 return true;
             }
 
             if (!(other instanceof Node)) {
                 return false;
             }
 
             Node that = (Node) other;
             return p.equals(that.getPosition());
         }
 
         @Override
         public int hashCode() {
             return p.hashCode();
         }
     }
 
     private Double getMovementCost(GameMap map, Unit unit, Position origin) {
         /* g(x) for search */
         // flying units ignore this; always 1
         if (unit.isFlying())
             return 1.0;
 
         return map.getField(origin).getMovementModifier();
     }
 
     public Set<Position> getAttackableUnitPositions(GameMap map, Unit unit, Position position) {
     	Set<Position> atkFields = getAttackableFields(map, unit, position);
         Set<Position> atkUnits = new HashSet<Position>();
 
         for (Position p : atkFields) {
             if (map.isValidField(p)) {
                 if (map.getField(p).hostsUnit()) {
                     Player uOwner = map.getField(p).getUnit().getOwner();
 
                     if (!uOwner.equals(unit.getOwner())) {
                         atkUnits.add(p);
                     }
                 }
             }
         }
         return atkUnits;
     }
 
     public Set<Position> getAttackableUnitPositions(GameMap map, Unit unit) {
         /*Set<Position> atkFields = getAttackableFields(map, unit);
         Set<Position> atkUnits = new HashSet<Position>();
 
         for (Position p : atkFields) {
             if (map.getField(p).hostsUnit()) {
                 Player uOwner = map.getField(p).getUnit().getOwner();
 
                 if (!uOwner.equals(unit.getOwner()))
                     atkUnits.add(p);
             }
         }
 
         return atkUnits;*/
     	return getAttackableUnitPositions(map, unit, unit.getPosition());
     }
 
     private Set<Position> getAttackableFields(GameMap map, Unit unit, Position position) {
         if (!unit.isRanged()) {
             return new HashSet<Position>(getAdjacentPositions(position));
         }
 
         RangedUnit ru = (RangedUnit) unit;
         return getPositionsInRange(map, position, ru.getMinRange(),
                                    ru.getMaxRange());
     }
 
     public Set<Position> getAttackableFields(GameMap map, Unit unit) {
         /*if (!unit.isRanged())
             return getPositionsInRange(map, unit.getPosition(), 1.0);
 
         RangedUnit ru = (RangedUnit) unit;
         return getPositionsInRange(map, ru.getPosition(), ru.getMinRange(),
                                    ru.getMaxRange());*/
     	return getAttackableFields(map, unit, unit.getPosition());
     }
 
     private Set<Position> getPositionsInRange(GameMap map, Position origin,
             Double range) {
         Set<Position> positions = new HashSet<Position>();
         Double maxr = Math.ceil(range);
 
         for (Integer x = 0; x < maxr * 2; x++) {
             for (Integer y = 0; y < maxr * 2; y++) {
                 Position newP = new Position(x, y);
 
                 // Pair<Integer, Integer> dist = getManhattanDistance(origin,
                 // newP);
                 if (x < maxr)
                     newP = new Position(newP.getX() - x, newP.getY());
                 else if (x > maxr)
                     newP = new Position(newP.getX() + x, newP.getY());
 
                 if (y < maxr)
                     newP = new Position(newP.getX(), newP.getY() - x);
                 else if (y > maxr)
                     newP = new Position(newP.getX(), newP.getY() + y);
 
                 if (newP.equals(origin) || !map.isValidField(newP))
                     continue;
 
                 positions.add(newP);
 
             }
         }
 
         return positions;
     }
 
     private Set<Position> getPositionsInRange(GameMap map, Position origin,
             Double minRange, Double maxRange) {
         Set<Position> positions = getPositionsInRange(map, origin, maxRange);
         Set<Position> filtered = new HashSet<Position>();
 
         for (Position p : positions) {
             Pair<Integer, Integer> dist;
             dist = getDistanceAway(origin, p);
 
             if (Math.hypot(dist.getLeft(), dist.getRight()) < minRange)
                 continue;
 
             filtered.add(p);
         }
 
         return filtered;
     }
 
     /* Used as a heuristic for A* */
     private Integer getManhattanDistance(Position origin, Position destination) {
         /* h(x) */
         Pair<Integer, Integer> distance = getDistanceAway(origin, destination);
 
         return distance.getLeft() + distance.getRight();
     }
 
     private Pair<Integer, Integer> getDistanceAway(Position origin,
             Position destination) {
         Integer x = Math.abs(origin.getX() - destination.getX());
         Integer y = Math.abs(origin.getY() - destination.getY());
         return new Pair<Integer, Integer>(x, y);
     }
 
 }
