 /*
  * Sonar, open source software quality management tool.
  * Written (W) 2011 Andrew Tereskin
  * Copyright (C) 2008-2011 SonarSource
  * mailto:contact AT sonarsource DOT com
  *
  * Sonar is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 package org.sonar.duplications.algorithm;
 
 import org.sonar.duplications.api.codeunit.block.Block;
 import org.sonar.duplications.api.index.CloneIndexBackend;
 
 import java.util.*;
 
 public class CloneReporter {
 
   /**
   * Use this comparator in TreeSet to intersect only by fileName
    */
   private static class ResourceIdBlockComparator implements Comparator<Block> {
 
     public int compare(Block o1, Block o2) {
       return o1.getResourceId().compareTo(o2.getResourceId());
     }
 
     public boolean equals(Object obj) {
       return obj instanceof ResourceIdBlockComparator;
     }
   }
 
 
   public static List<Clone> reportClones(String filename, CloneIndexBackend index) {
     SortedSet<Block> resourceSet = index.getByResourceId(filename);
 
     int totalSequences = resourceSet.size();
     ArrayList<Set<Block>> tuplesC = new ArrayList<Set<Block>>(totalSequences);
     ArrayList<Block> resourceBlocks = new ArrayList<Block>(totalSequences);
 
     ArrayList<Clone> clones = new ArrayList<Clone>();
 
     prepareSets(resourceSet, tuplesC, resourceBlocks, index);
 
     for (int i = 0; i < totalSequences; i++) {
       boolean containsInPrev = i > 0 && tuplesC.get(i - 1).containsAll(tuplesC.get(i));
       if (tuplesC.get(i).size() < 2 || containsInPrev) {
         continue;
       }
 
       Set<Block> current = createEmptySet();
       current.addAll(tuplesC.get(i));
       for (int j = i + 1; j < totalSequences + 1; j++) {
         Set<Block> intersected = createEmptySet();
         intersected.addAll(current);
         //do intersection
         intersected.retainAll(tuplesC.get(j));
 
         //if intersection size is smaller than original
         if (intersected.size() < current.size()) {
           //report clones from tuplesC[i] to current
           int cloneLength = j - i;
           Set<Block> beginSet = tuplesC.get(i);
           Set<Block> endSet = tuplesC.get(j - 1);
           Set<Block> prebeginSet = createEmptySet();
           if (i > 0) {
             prebeginSet = tuplesC.get(i - 1);
           }
           reportClone(resourceBlocks.get(i), resourceBlocks.get(j - 1), beginSet, endSet,
               prebeginSet, intersected, cloneLength, clones);
         }
 
         current = intersected;
         boolean inPrev = i > 0 && tuplesC.get(i - 1).containsAll(current);
         if (current.size() < 2 || inPrev) {
           break;
         }
       }
     }
     return clones;
   }
 
   private static Set<Block> createEmptySet() {
     return new TreeSet<Block>(new ResourceIdBlockComparator());
   }
 
   private static void prepareSets(SortedSet<Block> fileSet, List<Set<Block>> tuplesC,
                                   List<Block> fileBlocks, CloneIndexBackend index) {
     for (Block block : fileSet) {
       Set<Block> set = index.getBySequenceHash(block.getBlockHash());
       Set<Block> wrapSet = createEmptySet();
       wrapSet.addAll(set);
       fileBlocks.add(block);
       tuplesC.add(wrapSet);
     }
     //to fix last element bug
     tuplesC.add(createEmptySet());
   }
 
   private static void reportClone(Block beginTuple, Block endTuple, Set<Block> beginSet, Set<Block> endSet,
                                   Set<Block> prebeginSet, Set<Block> intersected, int cloneLength, List<Clone> clones) {
     String firstFile = beginTuple.getResourceId();
     int firstUnitIndex = beginTuple.getFirstUnitIndex();
     int firstLineStart = beginTuple.getFirstLineNumber();
     int firstLineEnd = endTuple.getLastLineNumber();
     TreeMap<String, Block> map = new TreeMap<String, Block>();
     for (Block block : endSet) {
       map.put(block.getResourceId(), block);
     }
 
     //cycle in filenames in clone start position
     for (Block secondStartBlock : beginSet) {
       if (!firstFile.equals(secondStartBlock.getResourceId()) && !intersected.contains(secondStartBlock)
           && !prebeginSet.contains(secondStartBlock)) {
         String secondFile = secondStartBlock.getResourceId();
         int secondUnitIndex = secondStartBlock.getFirstUnitIndex();
         int secondLineStart = secondStartBlock.getFirstLineNumber();
         Block secondEndBlock = map.get(secondStartBlock.getResourceId());
         int secondLineEnd = secondEndBlock.getLastLineNumber();
 
         Clone item = new Clone();
         item.setFirstResourceId(firstFile);
         item.setFirstUnitStart(firstUnitIndex);
         item.setFirstLineStart(firstLineStart);
         item.setFirstLineEnd(firstLineEnd);
 
         item.setSecondResourceId(secondFile);
         item.setSecondUnitStart(secondUnitIndex);
         item.setSecondLineStart(secondLineStart);
         item.setSecondLineEnd(secondLineEnd);
         item.setCloneLength(cloneLength);
         clones.add(item);
       }
     }
   }
 }
