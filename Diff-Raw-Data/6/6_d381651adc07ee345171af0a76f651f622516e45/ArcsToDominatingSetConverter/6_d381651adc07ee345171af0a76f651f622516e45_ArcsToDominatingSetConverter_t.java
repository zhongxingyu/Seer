 /*******************************************************************************
  * This file is part of DITL.                                                  *
  *                                                                             *
  * Copyright (C) 2011-2012 John Whitbeck <john@whitbeck.fr>                    *
  *                                                                             *
  * DITL is free software: you can redistribute it and/or modify                *
  * it under the terms of the GNU General Public License as published by        *
  * the Free Software Foundation, either version 3 of the License, or           *
  * (at your option) any later version.                                         *
  *                                                                             *
  * DITL is distributed in the hope that it will be useful,                     *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               *
  * GNU General Public License for more details.                                *
  *                                                                             *
  * You should have received a copy of the GNU General Public License           *
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.       *
  *******************************************************************************/
 package ditl.graphs;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
import net.sf.json.JSONObject;
 import ditl.Bus;
 import ditl.Converter;
 import ditl.Generator;
 import ditl.Listener;
 import ditl.Runner;
 import ditl.StatefulReader;
 import ditl.StatefulWriter;
 import ditl.Trace;
 
 public final class ArcsToDominatingSetConverter implements Converter,
         ArcTrace.Handler, PresenceTrace.Handler, Generator, Listener<Object> {
 
     private StatefulWriter<GroupEvent, Group> group_writer;
     private final GroupTrace dominating_set;
     private final ArcTrace _arcs;
     private final PresenceTrace _presence;
 
     private Set<Integer> ds_nodes = null;
     private final Set<Integer> present = new HashSet<Integer>();
     private final AdjacencySet.Arcs matrix = new AdjacencySet.Arcs();
     private final AdjacencySet.Arcs reverse_matrix = new AdjacencySet.Arcs();
     private final Bus<Object> update_bus = new Bus<Object>();
     private final long min_time;
     private final Integer gid = 0;
 
     public ArcsToDominatingSetConverter(GroupTrace dominatingSet,
             ArcTrace arcs, PresenceTrace presence) {
         _arcs = arcs;
         _presence = presence;
         dominating_set = dominatingSet;
         min_time = _presence.minTime();
     }
 
     @Override
     public void convert() throws IOException {
         final StatefulReader<ArcEvent, Arc> arc_reader = _arcs.getReader();
         final StatefulReader<PresenceEvent, Presence> presence_reader = _presence.getReader();
         group_writer = dominating_set.getWriter();
         update_bus.addListener(this);
 
         arc_reader.bus().addListener(this.arcEventListener());
         arc_reader.stateBus().addListener(this.arcListener());
         presence_reader.bus().addListener(this.presenceEventListener());
         presence_reader.stateBus().addListener(this.presenceListener());
 
         final Runner runner = new Runner(_arcs.maxUpdateInterval(), _presence.minTime(), _presence.maxTime());
         runner.addGenerator(arc_reader);
         runner.addGenerator(presence_reader);
         runner.addGenerator(this);
         runner.run();
 
        JSONObject labels = new JSONObject();
        labels.put("dominating set", gid);
        group_writer.setProperty(GroupTrace.labelsKey, labels);
         group_writer.setPropertiesFromTrace(_arcs);
         group_writer.close();
         arc_reader.close();
         presence_reader.close();
     }
 
     @Override
     public Listener<ArcEvent> arcEventListener() {
         return new Listener<ArcEvent>() {
             @Override
             public void handle(long time, Collection<ArcEvent> events) {
                 for (final ArcEvent aev : events) {
                     final Arc a = aev.arc();
                     if (aev.isUp()) {
                         matrix.add(a);
                         reverse_matrix.add(a.reverse());
                     } else {
                         matrix.remove(a);
                         reverse_matrix.remove(a.reverse());
                     }
                 }
                 scheduleUpdate(time);
             }
         };
     }
 
     @Override
     public Listener<Arc> arcListener() {
         return new Listener<Arc>() {
             @Override
             public void handle(long time, Collection<Arc> events) {
                 for (final Arc a : events) {
                     matrix.add(a);
                     reverse_matrix.add(a.reverse());
                 }
                 scheduleUpdate(time);
             }
         };
     }
 
     @Override
     public Listener<PresenceEvent> presenceEventListener() {
         return new Listener<PresenceEvent>() {
             @Override
             public void handle(long time, Collection<PresenceEvent> events) {
                 for (final PresenceEvent pev : events)
                     if (pev.isIn())
                         present.add(pev.id);
                     else
                         present.remove(pev.id);
                 scheduleUpdate(time);
             }
         };
     }
 
     @Override
     public Listener<Presence> presenceListener() {
         return new Listener<Presence>() {
             @Override
             public void handle(long time, Collection<Presence> events) {
                 for (final Presence p : events)
                     present.add(p.id);
                 scheduleUpdate(time);
             }
         };
     }
 
     class DSCalculator {
         TreeMap<Integer, Set<Integer>> degree_map = new TreeMap<Integer, Set<Integer>>();
         Map<Integer, Set<Integer>> remainders = new HashMap<Integer, Set<Integer>>();
         Set<Integer> covered = new HashSet<Integer>();
         Set<Integer> new_ds = new HashSet<Integer>();
 
         DSCalculator() {
             for (final Integer id : present)
                 if (reverse_matrix.getNext(id).isEmpty()) { // no incoming arcs,
                                                             // must be chosen
                     new_ds.add(id);
                     covered.add(id);
                     final Set<Integer> dests = matrix.getNext(id);
                     if (!dests.isEmpty())
                         covered.addAll(dests);
                 } else { // prepare entry in the remainder map
                     final Set<Integer> dests = matrix.getNext(id);
                     if (!dests.isEmpty()) { // nodes with incoming but no
                                             // outgoing arcs should never be in
                                             // the dominating set
                         final Set<Integer> r_dests = new HashSet<Integer>(dests);
                         r_dests.removeAll(covered);
                         remainders.put(id, r_dests);
                         setNodeDegree(id, r_dests.size());
                     }
                 }
         }
 
         Set<Integer> calculateNewDS() {
             while (!allCovered())
                 pickAlreadyInDS();
             return new_ds;
         }
 
         boolean allCovered() {
             return covered.size() >= present.size(); // covered may be greater
                                                      // than the number of
                                                      // present nodes (e.g.,
                                                      // arcs in reachability
                                                      // traces)
         }
 
         void pick(Integer node) {
             new_ds.add(node);
             final Set<Integer> newly_covered = remainders.remove(node);
             newly_covered.add(node);
             degree_map.clear();
             for (final Map.Entry<Integer, Set<Integer>> e : remainders.entrySet()) {
                 final Integer n = e.getKey();
                 final Set<Integer> dests = e.getValue();
                 dests.removeAll(newly_covered);
                 setNodeDegree(n, dests.size());
             }
             covered.addAll(newly_covered);
         }
 
         void pickAlreadyInDS() {
             Integer id = null;
             final Iterator<Integer> i = greedyChoice().iterator();
             while (i.hasNext()) {
                 id = i.next();
                 if (ds_nodes == null)
                     break;
                 if (ds_nodes.contains(id))
                     break;
             }
             pick(id);
         }
 
         Set<Integer> greedyChoice() {
             return degree_map.lastEntry().getValue();
         }
 
         void setNodeDegree(Integer node, Integer new_degree) {
             Set<Integer> degrees = degree_map.get(new_degree);
             if (degrees == null) {
                 degrees = new HashSet<Integer>();
                 degree_map.put(new_degree, degrees);
             }
             degrees.add(node);
         }
 
     }
 
     private void scheduleUpdate(long time) {
         update_bus.queue(time, Collections.<Object> emptyList());
     }
 
     @Override
     public Bus<?>[] busses() {
         return new Bus<?>[] { update_bus };
     }
 
     @Override
     public int priority() {
         return Trace.lowestPriority; // this should run with the lowest priority
     }
 
     @Override
     public void incr(long dt) throws IOException {
     }
 
     @Override
     public void seek(long time) throws IOException {
     }
 
     @Override
     public void handle(long time, Collection<Object> events) throws IOException {
         final DSCalculator calc = new DSCalculator();
         final Set<Integer> new_ds_nodes = calc.calculateNewDS();
         if (time == min_time) { // set the init state
             ds_nodes = new_ds_nodes;
             final Group grp = new Group(gid, new_ds_nodes);
             group_writer.setInitState(min_time, Collections.singleton(grp));
         } else {
             final Set<Integer> to_del = new HashSet<Integer>();
             final Set<Integer> to_add = new HashSet<Integer>();
             for (final Integer n : ds_nodes)
                 if (!new_ds_nodes.contains(n))
                     to_del.add(n);
             for (final Integer n : new_ds_nodes)
                 if (!ds_nodes.contains(n))
                     to_add.add(n);
             if (!to_del.isEmpty())
                 group_writer.append(time, new GroupEvent(gid, GroupEvent.Type.LEAVE, to_del));
             if (!to_add.isEmpty())
                 group_writer.append(time, new GroupEvent(gid, GroupEvent.Type.JOIN, to_add));
             ds_nodes = new_ds_nodes;
         }
     }
 }
