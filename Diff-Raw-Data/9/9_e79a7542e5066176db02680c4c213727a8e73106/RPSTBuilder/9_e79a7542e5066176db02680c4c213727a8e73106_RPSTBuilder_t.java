 /*
  * Copyright (C) 2011 Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.jamgotchian.abcd.core.ir;
 
 import fr.jamgotchian.abcd.core.graph.PostDominatorInfo;
 import fr.jamgotchian.abcd.core.graph.DominatorInfo;
 import fr.jamgotchian.abcd.core.util.Sets;
 import java.util.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Build a refined program structure tree (RPST) from control flow graph.
  *
  * A RPST is a tree build out of canonical refined regions of a function.
  *
  * This is an implementation of the algorithm described in the following paper :
  * The refined program structure tree, Calculate a fine grained PST taking
  * advantage of dominance information, Tobias Grosser, University of Passau,
  * May 19, 2010.
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 public class RPSTBuilder {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(RPSTBuilder.class);
 
     private static class RPSTRegion {
 
         private final BasicBlock entry;
 
         private final BasicBlock exit;
 
         private RPSTRegion parent;
 
         private final Set<RPSTRegion> children = new LinkedHashSet<RPSTRegion>();
 
         private RPSTRegion(BasicBlock entry, BasicBlock exit) {
             this.entry = entry;
             this.exit = exit;
         }
 
         @Override
         public String toString() {
             return "(" + entry + ", " + exit + ")";
         }
     }
 
     private final ControlFlowGraph cfg;
 
     private final Map<BasicBlock, RPSTRegion> bb2region = new HashMap<BasicBlock, RPSTRegion>();
 
     public RPSTBuilder(ControlFlowGraph cfg) {
         this.cfg = cfg;
     }
 
     private DominatorInfo<BasicBlock, Edge> getDomInfo() {
         return cfg.getDominatorInfo();
     }
 
     private PostDominatorInfo<BasicBlock, Edge> getPostDomInfo() {
         return cfg.getPostDominatorInfo();
     }
 
     private boolean isCommonDomFrontier(BasicBlock bb, BasicBlock entry, BasicBlock exit) {
         for (BasicBlock p : cfg.getPredecessorsOf(bb)) {
             if (getDomInfo().dominates(entry, p) && !getDomInfo().dominates(exit, p)) {
                 return false;
             }
         }
         return true;
     }
 
     private boolean isRegion(BasicBlock entry, BasicBlock exit) {
         // 2 cases
         //   Entry does not dominates Exit
         //   Entry dominates Exit
         if (!getDomInfo().dominates(entry, exit)) {
             // Exit has to be the only element in the dominance frontier of Entry.
             // In this case the only edges leaving the region end at Exit
             if (!Sets.isSubset(getDomInfo().getDominanceFrontierOf2(entry),
                                Collections.singleton(exit))) {
                 return false;
             }
         } else {
             // Only basic blocks that are part of the dominance frontier of Exit
             // are allowed to be element of the dominance frontier of Entry
             if (!Sets.isSubset(getDomInfo().getDominanceFrontierOf2(entry),
                                com.google.common.collect.Sets.union(getDomInfo().getDominanceFrontierOf2(exit),
                                           Collections.singleton(entry)))) {
                 return false;
             }
             // Basic blocks of the dominance frontier of Entry can only be reached
             // from Entry through a path passing Exit. To show this the isCommonDomFrontier
             // function is used. isCommonDomFrontier ( BB, Entry, Exit) checks if there exists a path
             // from Entry to BB that does not pass Exit. This is done by checking
             // for every predecessor of BB, that if it is dominated by Entry it is
             // also dominated by Exit.
             for (BasicBlock bb : getDomInfo().getDominanceFrontierOf2(entry)) {
                 if (!isCommonDomFrontier(bb, entry, exit)) {
                     return false;
                 }
             }
             // It has still to be shown that there are no edges entering the region.
             // As all basic blocks are dominated by Entry the only case where edges
             // enter the region is if Exit is dominated by Entry and has back edges
             // pointing into the region. These back edges will point to basic blocks
             // dominated by Entry but not by Exit. So the dominance frontier of Exit
             // is not allowed to contain any basic blocks that are dominated by Entry
             for (BasicBlock bb : getDomInfo().getDominanceFrontierOf2(exit)) {
                 if (getDomInfo().strictlyDominates(entry, bb)) {
                     return false;
                 }
             }
         }
         return true;
     }
 
     private void insertShortCut(BasicBlock entry, BasicBlock exit,
                                 Map<BasicBlock, BasicBlock> shortCut) {
         assert entry != null && exit != null;
         BasicBlock bb = shortCut.get(exit);
         if (bb == null) {
             shortCut.put(entry, exit);
         } else {
             shortCut.put(entry, bb);
         }
     }
 
     private BasicBlock getNextPostDom(BasicBlock bb, Map<BasicBlock, BasicBlock> shortCut) {
         BasicBlock bb2 = shortCut.get(bb);
         return getPostDomInfo().getImmediatePostDominatorOf(bb2 == null ? bb : bb2);
     }
 
     private RPSTRegion createRegion(BasicBlock entry, BasicBlock exit) {
         assert entry != null && exit != null;
         RPSTRegion newRegion = new RPSTRegion(entry, exit);
         if (!bb2region.containsKey(entry)) {
             bb2region.put(entry, newRegion);
         }
         LOGGER.debug("New Region {}", newRegion);
         return newRegion;
     }
 
    private static class DetectionContext {
 
         private RPSTRegion lastRegion;
 
         private BasicBlock lastExit;
 
         private DetectionContext(RPSTRegion lastRegion, BasicBlock lastExit) {
             this.lastRegion = lastRegion;
             this.lastExit = lastExit;
         }
 
         private DetectionContext(DetectionContext other) {
             lastRegion = other.lastRegion;
             lastExit = other.lastExit;
         }
     }
 
     private void detectRegionsWithEntry(BasicBlock entry, Map<BasicBlock, BasicBlock> shortCut) {
         assert  entry != null;
         BasicBlock exit = getNextPostDom(entry, shortCut);
         if (exit != null) {
             DetectionContext context = new DetectionContext(null, entry);
             detectRegionsWithEntry(entry, exit, shortCut, context);
             if (!context.lastExit.equals(entry)) {
                 insertShortCut(entry, context.lastExit, shortCut);
             }
         }
     }
 
     private void detectRegionsWithEntry(BasicBlock entry, BasicBlock exit, Map<BasicBlock, BasicBlock> shortCut, DetectionContext context) {
         if (isRegion(entry, exit)) {
             RPSTRegion newRegion = createRegion(entry, exit);
             if (context.lastRegion != null) {
                 context.lastRegion.parent = newRegion;
                 LOGGER.trace("Parent of region {} is {}", context.lastRegion, newRegion);
             } else {
                 LOGGER.trace("Parent of BB {} is {}", entry, newRegion);
             }
             context.lastRegion = newRegion;
             context.lastExit = exit;
         }
         if (getDomInfo().dominates(entry, exit)) {
             BasicBlock exit2 = getNextPostDom(exit, shortCut);
             if (exit2 != null) {
                 detectRegionsWithEntry(entry, exit2, shortCut, new DetectionContext(context));
             }
         }
     }
 
     private void detectRegions() {
         Map<BasicBlock, BasicBlock> shortCut = new HashMap<BasicBlock, BasicBlock>();
         for (BasicBlock bb : getDomInfo().getDominatorsTree().getNodesPostOrder()) {
             detectRegionsWithEntry(bb, shortCut);
         }
     }
 
     private RPSTRegion getTopMostParent(RPSTRegion region) {
         while (region.parent != null) {
             region = region.parent;
         }
         return region;
     }
 
     private void buildRegionTree(BasicBlock bb, RPSTRegion region) {
         while (bb.equals(region.exit)) {
             region = region.parent;
         }
         RPSTRegion newRegion = bb2region.get(bb);
         if (newRegion != null) {
             RPSTRegion topMostParent = getTopMostParent(newRegion);
             LOGGER.trace("Parent of region {} is {}", topMostParent, region);
             topMostParent.parent = region;
             region = newRegion;
         } else {
             LOGGER.trace("Parent of BB {} is {}", bb, region);
             bb2region.put(bb, region);
         }
         for (BasicBlock c : getDomInfo().getDominatorsTree().getChildren(bb)) {
             buildRegionTree(c, region);
         }
     }
 
     /**
      * Test if two canonical regions could be merge in one non canonical region.
      */
     private boolean isNonCanonicalRegion(RPST rpst, Region region1, Region region2) {
         if (!region1.getExit().equals(region2.getEntry())) {
             return false;
         }
         if (rpst.getChildCount(region2) == 0) {
             return false;
         }
         // basic blocks of merged region
         Set<BasicBlock> basicBlocks = new HashSet<BasicBlock>();
         basicBlocks.addAll(rpst.getBasicBlocks(region1));
         basicBlocks.addAll(rpst.getBasicBlocks(region2));
         basicBlocks.add(region2.getExit());
         if (!basicBlocks.containsAll(cfg.getSuccessorsOf(region1.getExit()))) {
             return false;
         }
         if (!basicBlocks.containsAll(cfg.getPredecessorsOf(region1.getExit()))) {
             return false;
         }
         return true;
     }
 
     private void findNonCanonicalRegions(RPST rpst, Region region) {
         Set<Region> childrenBefore = new HashSet<Region>(rpst.getChildren(region));
         if (rpst.getChildCount(region) > 1) {
             boolean found = true;
             while (found) {
                 found = false;
 
                 // children ordered by dominance
                 List<Region> children = new ArrayList<Region>(rpst.getChildren(region));
                 Collections.sort(children, new Comparator<Region>() {
                     @Override
                     public int compare(Region region1, Region region2) {
                         return getDomInfo().dominates(region1.getEntry(), region2.getEntry()) ? 1 : -1;
                     }
                 });
 
                 for (Region child1 : children) {
                     for (Region child2 : children) {
                         if (!child1.equals(child2)) {
                             if (isNonCanonicalRegion(rpst, child1, child2)) {
                                 Region newRegion = new RegionImpl(child1.getEntry(),
                                                               child2.getExit(),
                                                               ParentType.UNDEFINED);
                                 LOGGER.debug("New non canonical region {}", newRegion);
                                 rpst.addRegion(newRegion, region);
                                 rpst.setNewParent(child1, newRegion);
                                 rpst.setNewParent(child2, newRegion);
                                 found = true;
                                 break;
                             }
                         }
                     }
                     if (found) {
                         break;
                     }
                 }
             }
         }
         for (Region child : childrenBefore) {
             findNonCanonicalRegions(rpst, child);
         }
     }
 
     private void a(RPSTRegion r, Region r2, RPST rpst, Map<RPSTRegion, Region> mapR) {
         mapR.put(r, r2);
         for (RPSTRegion c : r.children) {
             Region c2 = new RegionImpl(c.entry, c.exit, ParentType.UNDEFINED);
             rpst.addRegion(c2, r2);
             a(c, c2, rpst, mapR);
         }
     }
 
     private void printAllRegions() {
         for (BasicBlock bb1 : cfg.getBasicBlocks()) {
             for (BasicBlock bb2 : cfg.getBasicBlocks()) {
                 if (!bb1.equals(bb2) && getDomInfo().dominates(bb1, bb2)) {
                     if (isRegion(bb1, bb2)) {
                         LOGGER.debug("REGION {},{}", bb1, bb2);
                     }
                 }
             }
         }
     }
 
     public RPST build() {
         // find canonical regions
         detectRegions();
 
         // build tree of canonical regions
         RPSTRegion root = new RPSTRegion(cfg.getEntryBlock(), null);
         buildRegionTree(cfg.getEntryBlock(), root);
 
         // build children
         for (RPSTRegion r : bb2region.values()) {
             for(RPSTRegion r2 = r; r2 != null; r2 = r2.parent) {
                 if (r2.parent != null) {
                     r2.parent.children.add(r2);
                 }
             }
         }
 
         RPST rpst = new RPST(cfg, new RegionImpl(cfg.getEntryBlock(), null, ParentType.ROOT));
         Map<RPSTRegion, Region> mapR = new HashMap<RPSTRegion, Region>();
         a(root, rpst.getRootRegion(), rpst, mapR);
         for (Map.Entry<BasicBlock, RPSTRegion> entry : bb2region.entrySet()) {
             BasicBlock bb = entry.getKey();
             RPSTRegion r = entry.getValue();
             Region r2 = mapR.get(r);
             rpst.addRegion(bb, r2);
         }
 
         // add non canonical region to the tree
         findNonCanonicalRegions(rpst, rpst.getRootRegion());
 
         return rpst;
     }
 }
