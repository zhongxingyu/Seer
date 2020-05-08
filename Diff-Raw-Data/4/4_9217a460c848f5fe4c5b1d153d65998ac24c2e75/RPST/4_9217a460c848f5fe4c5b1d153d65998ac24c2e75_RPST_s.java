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
 import fr.jamgotchian.abcd.core.graph.GraphvizUtil;
 import fr.jamgotchian.abcd.core.util.Sets;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
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
 public class RPST {
 
     private static final Logger logger = Logger.getLogger(RPST.class.getName());
 
     private static final EdgeGraphvizRenderer EDGE_GRAPHVIZ_RENDERER
             = new EdgeGraphvizRenderer(true);
 
     private static final RangeGraphvizRenderer RANGE_GRAPHIZ_RENDERER
             = new RangeGraphvizRenderer();
 
     private final ControlFlowGraph cfg;
 
     private final DominatorInfo<BasicBlock, Edge> domInfo;
 
     private final PostDominatorInfo<BasicBlock, Edge> postDomInfo;
 
     private final Map<BasicBlock, Region> bb2region = new HashMap<BasicBlock, Region>();
 
     private Region rootRegion;
 
     public RPST(ControlFlowGraph cfg) {
         this.cfg = cfg;
         domInfo = cfg.getDominatorInfo();
         postDomInfo = cfg.getPostDominatorInfo();
         build();
     }
 
     public ControlFlowGraph getCFG() {
         return cfg;
     }
 
     public Region getRootRegion() {
         return rootRegion;
     }
 
     private void visitRegionPostOrder(Region region, List<Region> regionsPostOrder) {
         for (Region child : region.getChildren()) {
             visitRegionPostOrder(child, regionsPostOrder);
         }
         regionsPostOrder.add(region);
     }
 
     public List<Region> getRegionsPostOrder() {
         List<Region> regionsPostOrder = new ArrayList<Region>();
         visitRegionPostOrder(rootRegion, regionsPostOrder);
         return regionsPostOrder;
     }
 
     private boolean isCommonDomFrontier(BasicBlock bb, BasicBlock entry, BasicBlock exit) {
         for (BasicBlock p : cfg.getPredecessorsOf(bb)) {
             if (domInfo.dominates(entry, p) && !domInfo.dominates(exit, p)) {
                 return false;
             }
         }
         return true;
     }
 
     private boolean isRegion(BasicBlock entry, BasicBlock exit) {
         if (!domInfo.dominates(entry, exit)) {
             if (!Sets.isSubset(domInfo.getDominanceFrontierOf2(entry),
                                Collections.singleton(exit))) {
                 return false;
             }
         } else {
             if (!Sets.isSubset(domInfo.getDominanceFrontierOf2(entry),
                                com.google.common.collect.Sets.union(domInfo.getDominanceFrontierOf2(exit),
                                           Collections.singleton(entry)))) {
                 return false;
             }
             for (BasicBlock bb : domInfo.getDominanceFrontierOf2(entry)) {
                 if (!isCommonDomFrontier(bb, entry, exit)) {
                     return false;
                 }
             }
             for (BasicBlock bb : domInfo.getDominanceFrontierOf2(exit)) {
                 if (domInfo.strictlyDominates(entry, bb)) {
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
         if (bb2 == null) {
             return postDomInfo.getImmediatePostDominatorOf(bb);
         } else {
             return postDomInfo.getImmediatePostDominatorOf(bb2);
         }
     }
 
     private Region createRegion(BasicBlock entry, BasicBlock exit) {
         assert entry != null && exit != null;
         Region newRegion = new Region(entry, exit, ParentType.UNDEFINED);
        bb2region.put(entry, newRegion);
         logger.log(Level.FINER, "New Region {0}", newRegion);
         return newRegion;
     }
 
     private void detectRegionsWithEntry(BasicBlock entry, Map<BasicBlock, BasicBlock> shortCut) {
         assert  entry != null;
         BasicBlock exit = entry;
         Region lastRegion = null;
         BasicBlock lastExit = entry;
         while ((exit = getNextPostDom(exit, shortCut)) != null) {
             if (isRegion(entry, exit)) {
                 Region newRegion = createRegion(entry, exit);
                 if (lastRegion != null) {
                     lastRegion.setParent(newRegion);
                      logger.log(Level.FINEST, "Parent of region {0} is {1}",
                             new Object[] {lastRegion, newRegion});
                 } else {
                     entry.setParent(newRegion);
                     logger.log(Level.FINEST, "Parent of BB {0} is {1}",
                             new Object[] {entry, newRegion});
                 }
                 lastRegion = newRegion;
                 lastExit = exit;
             }
             if (!domInfo.dominates(entry, exit)) {
                 break;
             }
         }
 
         if (!lastExit.equals(entry)) {
             insertShortCut(entry, lastExit, shortCut);
         }
     }
 
     private void detectRegions(Map<BasicBlock, BasicBlock> shortCut) {
         for (BasicBlock bb : domInfo.getDominatorsTree().getNodesPostOrder()) {
             detectRegionsWithEntry(bb, shortCut);
         }
     }
 
     private Region getTopMostParent(Region region) {
         while (region.getParent() != null) {
             region = region.getParent();
         }
         return region;
     }
 
     private void buildRegionTree(BasicBlock bb, Region region) {
         while (bb.equals(region.getExit())) {
             region = region.getParent();
         }
         Region newRegion = bb2region.get(bb);
         if (newRegion != null) {
             Region topMostParent = getTopMostParent(newRegion);
             logger.log(Level.FINEST, "Parent of region {0} is {1}",
                     new Object[] {topMostParent, region});
             topMostParent.setParent(region);
             region = newRegion;
         } else {
             logger.log(Level.FINEST, "Parent of BB {0} is {1}",
                     new Object[] {bb, newRegion});
             bb.setParent(region);
             bb2region.put(bb, region);
         }
         for (BasicBlock c : domInfo.getDominatorsTree().getChildren(bb)) {
             buildRegionTree(c, region);
         }
     }
 
     /**
      * Test if two canonical regions could be merge in one non canonical region.
      */
     private boolean isNonCanonicalRegion(Region region1, Region region2) {
         if (!region1.getExit().equals(region2.getEntry())) {
             return false;
         }
         if (region2.getChildCount() == 0) {
             return false;
         }
         // basic blocks of merged region
         Set<BasicBlock> basicBlocks = new HashSet<BasicBlock>();
         basicBlocks.addAll(region1.getBasicBlocks());
         basicBlocks.addAll(region2.getBasicBlocks());
         basicBlocks.add(region2.getExit());
         if (!basicBlocks.containsAll(cfg.getSuccessorsOf(region1.getExit()))) {
             return false;
         }
         if (!basicBlocks.containsAll(cfg.getPredecessorsOf(region1.getExit()))) {
             return false;
         }
         return true;
     }
 
     private void findNonCanonicalRegions(Region region) {
         Set<Region> childrenBefore = new HashSet<Region>(region.getChildren());
         if (region.getChildCount() > 1) {
             boolean found = true;
             while (found) {
                 found = false;
 
                 // children ordered by dominance
                 List<Region> children = new ArrayList<Region>(region.getChildren());
                 Collections.sort(children, new Comparator<Region>() {
                     public int compare(Region region1, Region region2) {
                         return domInfo.dominates(region1.getEntry(), region2.getEntry()) ? 1 : -1;
                     }
                 });
 
                 for (Region child1 : children) {
                     for (Region child2 : children) {
                         if (!child1.equals(child2)) {
                             if (isNonCanonicalRegion(child1, child2)) {
                                 Region newRegion = new Region(child1.getEntry(),
                                                               child2.getExit(),
                                                               ParentType.UNDEFINED);
                                 logger.log(Level.FINER, "New non canonical region {0}", newRegion);
                                 child1.setParent(newRegion);
                                 child2.setParent(newRegion);
                                 newRegion.setParent(region);
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
             findNonCanonicalRegions(child);
         }
     }
 
     private void build() {
         // reset basic blocks parent
         for (BasicBlock bb : cfg.getBasicBlocks()) {
             bb.setParent(null);
         }
 
         // find canonical regions
         Map<BasicBlock, BasicBlock> shortCut = new HashMap<BasicBlock, BasicBlock>();
         detectRegions(shortCut);
 
         // build tree of canonical regions
         rootRegion = new Region(cfg.getEntryBlock(), null, ParentType.ROOT);
         buildRegionTree(cfg.getEntryBlock(), rootRegion);
 
         // insert dummy region for each basic block
         for (BasicBlock bb : cfg.getBasicBlocks()) {
             Region oldParent = bb.getParent();
             Region newParent = new Region(bb, bb, ParentType.BASIC_BLOCK);
             bb.setParent(newParent);
             newParent.setParent(oldParent);
         }
 
         // add non canonical region to the tree
         findNonCanonicalRegions(rootRegion);
     }
 
     public void print(Appendable out) {
         try {
             print(out, rootRegion, 0);
         } catch (IOException e) {
             logger.log(Level.SEVERE, e.toString(), e);
         }
     }
 
     private void printSpace(Appendable out, int indentLevel) throws IOException {
         for (int i = 0 ; i < indentLevel; i++) {
             out.append("    ");
         }
     }
 
     public void print(Appendable out, Region region, int indentLevel) throws IOException {
         printSpace(out, indentLevel);
         out.append(region.getChildType().toString()).append("\n");
         printSpace(out, indentLevel+1);
         out.append("+").append(region.getParentType().toString()).append(" ")
                 .append(region.toString()).append("\n");
         for (Region child : region.getChildren()) {
             print(out, child, indentLevel+2);
         }
     }
 
     private void writeSpace(Writer writer, int indentLevel) throws IOException {
         for (int i = 0 ; i < indentLevel; i++) {
             writer.append("  ");
         }
     }
 
     private void exportRegion(Writer writer, Region region, int indentLevel) throws IOException {
         if (region.isBasicBlock()) {
             BasicBlock bb = region.getEntry();
             writeSpace(writer, indentLevel);
             writer.append("  ")
                     .append(Integer.toString(System.identityHashCode(bb)))
                     .append(" ");
             Map<String, String> attrs = RANGE_GRAPHIZ_RENDERER.getAttributes(bb);
             GraphvizUtil.writeAttributes(writer, attrs);
             writeSpace(writer, indentLevel);
             writer.append("\n");
         } else {
             String clusterName = "cluster_" + Integer.toString(System.identityHashCode(region));
             writeSpace(writer, indentLevel);
             writer.append("subgraph ").append(clusterName).append(" {\n");
             writeSpace(writer, indentLevel+1);
             writer.append("fontsize=\"10\";\n");
             writeSpace(writer, indentLevel+1);
             writer.append("labeljust=\"left\";\n");
             if (region.getParentType() != null) {
                 writeSpace(writer, indentLevel+1);
                 writer.append("label=\"").append(region.getParentType().toString())
                         .append(" ").append(region.toString()).append("\";\n");
             }
             for (Region child : region.getChildren()) {
                 exportRegion(writer, child, indentLevel+1);
             }
             writeSpace(writer, indentLevel);
             writer.append("}\n");
         }
     }
 
     public void export(Writer writer) throws IOException {
         writer.append("digraph ").append("RPST").append(" {\n");
         exportRegion(writer, rootRegion, 1);
         for (Edge edge : cfg.getEdges()) {
             BasicBlock source = cfg.getEdgeSource(edge);
             BasicBlock target = cfg.getEdgeTarget(edge);
             writer.append("  ")
                     .append(Integer.toString(System.identityHashCode(source)))
                     .append(" -> ")
                     .append(Integer.toString(System.identityHashCode(target)));
             GraphvizUtil.writeAttributes(writer, EDGE_GRAPHVIZ_RENDERER.getAttributes(edge));
             writer.append("\n");
         }
         writer.append("}\n");
     }
 
     public void export(String fileName) {
         try {
             Writer writer = new FileWriter(fileName);
             export(writer);
             writer.close();
         } catch (IOException e) {
             logger.log(Level.SEVERE, e.toString(), e);
         }
     }
 }
