 /*
  * Copyright (c) 2013 The Interedition Development Group.
  *
  * This file is part of CollateX.
  *
  * CollateX is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CollateX is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package eu.interedition.collatex.dekker.matrix;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Sets;
 
 /**
  * 
  * @author Ronald Haentjens Dekker
  * @author Bram Buitendijk
  * @author Meindert Kroese
  */
 public class IslandConflictResolver {
   private static final int MINIMUM_OUTLIER_DISTANCE_FACTOR = 5;
   Logger LOG = Logger.getLogger(IslandConflictResolver.class.getName());
   private final MatchTable table;
   private final int outlierTranspositionsSizeLimit;
 
   public IslandConflictResolver(MatchTable table, int outlierTranspositionsSizeLimit) {
     this.table = table;
     this.outlierTranspositionsSizeLimit = outlierTranspositionsSizeLimit;
   }
 
   /*
    * Create a non-conflicting version by simply taken all the islands that do
    * not conflict with each other, largest first. This presuming that
    * Archipelago will have a high value if it contains the largest possible
    * islands
    */
   public Archipelago createNonConflictingVersion(Set<Island> islands) {
     Archipelago result = new Archipelago();
     Multimap<Integer, Island> islandMultimap = ArrayListMultimap.create();
     for (Island isl : islands) {
       islandMultimap.put(isl.size(), isl);
     }
     List<Integer> keySet = Lists.newArrayList(islandMultimap.keySet());
     Collections.sort(keySet);
     List<Integer> decreasingIslandSizes = Lists.reverse(keySet);
     for (Integer islandSize : decreasingIslandSizes) {
       // if (islandSize > 0) { // limitation to prevent false transpositions
       List<Island> possibleIslands = possibleIslands(islandMultimap.get(islandSize));
       if (possibleIslands.size() == 1) {
         addIslandToResult(possibleIslands.get(0), result);
       } else if (possibleIslands.size() > 1) {
         Multimap<IslandCompetition, Island> analysis = analyze(result, possibleIslands);
         resolveConflictsBySelectingPreferredIslands(result, analysis);
       }
     }
     return result;
   }
 
   /*
    * This method analyzes the relationship between all the islands of the same
    * size that have yet to be selected. They can compete with one another
    * (choosing one locks out the other), some of them can be on the ideal line.
    * 
    * Parameters: fixedIslands: the already committed islands possibleIslands:
    * the islands to select the next islands to commit from. They all have the
    * same size
    */
   private Multimap<IslandCompetition, Island> analyze(Archipelago fixedIslands, List<Island> possibleIslands) {
     Multimap<IslandCompetition, Island> conflictMap = ArrayListMultimap.create();
     Set<Island> competingIslands = getCompetingIslands(possibleIslands, fixedIslands);
     for (Island island : competingIslands) {
       Coordinate leftEnd = island.getLeftEnd();
       if (fixedIslands.getIslandVectors().contains(leftEnd.row - leftEnd.column)) {
         conflictMap.put(IslandCompetition.CompetingIslandAndOnIdealIine, island);
       } else {
         conflictMap.put(IslandCompetition.CompetingIsland, island);
       }
     }
     for (Island island : getNonCompetingIslands(possibleIslands, competingIslands)) {
       conflictMap.put(IslandCompetition.NonCompetingIsland, island);
     }
     return conflictMap;
   }
 
   /*
    * The preferred Islands are directly added to the result Archipelago 
    * If we want to
    * re-factor this into a pull construction rather then a push construction
    * we have to move this code out of this method and move it to the caller
    * class
    */
   private void resolveConflictsBySelectingPreferredIslands(Archipelago archipelago, Multimap<IslandCompetition, Island> islandConflictMap) {
     // First select competing islands that are on the ideal line
     Multimap<Double, Island> distanceMap1 = makeDistanceMap(islandConflictMap.get(IslandCompetition.CompetingIslandAndOnIdealIine), archipelago);
     LOG.fine("addBestOfCompeting with competingIslandsOnIdealLine");
     addBestOfCompeting(archipelago, distanceMap1);
     
     // Second select other competing islands
     Multimap<Double, Island> distanceMap2 = makeDistanceMap(islandConflictMap.get(IslandCompetition.CompetingIsland), archipelago);
     LOG.fine("addBestOfCompeting with otherCompetingIslands");
     addBestOfCompeting(archipelago, distanceMap2);
 
     // Third select non competing islands
     LOG.fine("add non competing islands");
     for (Island i : islandConflictMap.get(IslandCompetition.NonCompetingIsland)) {
       addIslandToResult(i, archipelago);
     }
   }
 
    // TODO: find a better way to determine the best choice of island
   private void addBestOfCompeting(Archipelago archipelago, Multimap<Double, Island> distanceMap1) {
     for (Double d : shortestToLongestDistances(distanceMap1)) {
       for (Island ci : distanceMap1.get(d)) {
         if (table.isIslandPossibleCandidate(ci)) {
           addIslandToResult(ci, archipelago);
         }
       }
     }
   }
 
   private Multimap<Double, Island> makeDistanceMap(Collection<Island> competingIslands, Archipelago archipelago) {
     Multimap<Double, Island> distanceMap = ArrayListMultimap.create();
     for (Island isl : competingIslands) {
       distanceMap.put(archipelago.smallestDistance(isl), isl);
     }
     return distanceMap;
   }
 
   private List<Double> shortestToLongestDistances(Multimap<Double, Island> distanceMap) {
     List<Double> distances = Lists.newArrayList(distanceMap.keySet());
     Collections.sort(distances);
     return distances;
   }
 
   private Set<Island> getNonCompetingIslands(List<Island> islands, Set<Island> competingIslands) {
     Set<Island> nonCompetingIslands = Sets.newHashSet(islands);
     nonCompetingIslands.removeAll(competingIslands);
     return nonCompetingIslands;
   }
 
   private Set<Island> getCompetingIslands(List<Island> islands, Archipelago result) {
     Set<Island> competingIslands = Sets.newHashSet();
     for (int i = 0; i < islands.size(); i++) {
       Island i1 = islands.get(i);
       for (int j = 1; j < islands.size() - i; j++) {
         Island i2 = islands.get(i + j);
         if (result.islandsCompete(i1, i2)) {
           competingIslands.add(i1);
           competingIslands.add(i2);
         }
       }
     }
     return competingIslands;
   }
 
   private List<Island> possibleIslands(Collection<Island> islandsOfSize) {
     List<Island> islands = Lists.newArrayList();
     for (Island island : islandsOfSize) {
       if (table.isIslandPossibleCandidate(island)) {
         islands.add(island);
       } else {
         removeConflictingEndCoordinates(island);
         if (island.size() > 1) {
           islands.add(island);
         }
       }
     }
     return islands;
   }
 
   private void removeConflictingEndCoordinates(Island island) {
     boolean goOn = true;
     while (goOn) {
       Coordinate leftEnd = island.getLeftEnd();
       if (table.doesCoordinateOverlapWithCommittedCoordinate(leftEnd)) {
         island.removeCoordinate(leftEnd);
         if (island.size() == 0) {
           return;
         }
       } else {
         goOn = false;
       }
     }
     goOn = true;
     while (goOn) {
       Coordinate rightEnd = island.getRightEnd();
       if (table.doesCoordinateOverlapWithCommittedCoordinate(rightEnd)) {
         island.removeCoordinate(rightEnd);
         if (island.size() == 0) {
           return;
         }
       } else {
         goOn = false;
       }
     }
   }
 
   private void addIslandToResult(Island isl, Archipelago result) {
     if (islandIsNoOutlier(result, isl)) {
       if (LOG.isLoggable(Level.FINE)) {
         LOG.log(Level.FINE, "adding island: '{0}'", isl);
       }
       table.commitIsland(isl);
       result.add(isl);
     } else {
       if (LOG.isLoggable(Level.FINE)) {
         LOG.log(Level.FINE, "island: '{0}' is an outlier, not added", isl);
       }
     }
   }
 
   private boolean islandIsNoOutlier(Archipelago a, Island isl) {
     double smallestDistance = a.smallestDistanceToIdealLine(isl);
     if (LOG.isLoggable(Level.FINE)) {
       LOG.log(Level.FINE, "island {0}, distance={1}", new Object[] { isl, smallestDistance });
     }
     int islandSize = isl.size();
     return (!(a.size() > 0 && islandSize <= outlierTranspositionsSizeLimit && smallestDistance >= islandSize * MINIMUM_OUTLIER_DISTANCE_FACTOR));
   }
 }
