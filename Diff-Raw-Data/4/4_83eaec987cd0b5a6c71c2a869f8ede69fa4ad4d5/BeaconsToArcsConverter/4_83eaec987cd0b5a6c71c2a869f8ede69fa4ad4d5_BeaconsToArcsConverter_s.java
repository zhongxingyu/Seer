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
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.TreeMap;
 
 import ditl.Converter;
 import ditl.Incrementable;
 import ditl.Listener;
 import ditl.Reader;
 import ditl.Runner;
 import ditl.StatefulWriter;
 
 public final class BeaconsToArcsConverter implements Incrementable, Converter {
 
     private final long _period;
     private int _tol = 0;
     private long cur_time;
     private final TreeMap<Long, Set<Arc>> arcsBuffer = new TreeMap<Long, Set<Arc>>();
     private final Map<Arc, Long> lastArcs = new AdjacencyMap.Arcs<Long>();
     private final Random rng = new Random();
     private final double _expansion;
     private StatefulWriter<ArcEvent, Arc> arc_writer;
     private Reader<Arc> beacon_reader;
 
     private final ArcTrace _arcs;
     private final BeaconTrace _beacons;
 
     public BeaconsToArcsConverter(ArcTrace arcs, BeaconTrace beacons,
             int tol, double expansion) {
         _arcs = arcs;
         _beacons = beacons;
         _period = beacons.beaconningPeriod();
         _tol = tol;
         _expansion = expansion;
     }
 
     public Listener<Arc> detectedListener() {
         return new Listener<Arc>() {
             @Override
             public void handle(long time, Collection<Arc> events) {
                 for (final Arc a : events) {
                     if (!lastArcs.containsKey(a)) { // edge comes up
                         final long start_time = time - (long) (_expansion * rand());
                         arc_writer.queue(start_time, new ArcEvent(a, ArcEvent.Type.UP));
                     }
                     appendLastArc(time, a);
                 }
             }
         };
     }
 
     private void appendLastArc(long time, Arc arc) {
         if (lastArcs.containsKey(arc)) { // remove from beaconBuffer
             final Long prevTime = lastArcs.get(arc);
             final Set<Arc> arcs = arcsBuffer.get(prevTime);
             arcs.remove(arc);
             if (arcs.isEmpty())
                 arcsBuffer.remove(prevTime);
         }
         if (!arcsBuffer.containsKey(time))
             arcsBuffer.put(time, new AdjacencySet.Arcs());
         arcsBuffer.get(time).add(arc);
         lastArcs.put(arc, time);
     }
 
     private void expireBeacons() {
         while (!arcsBuffer.isEmpty() &&
                 arcsBuffer.firstKey() <= cur_time - (_tol + 1) * _period) {
             final Map.Entry<Long, Set<Arc>> e = arcsBuffer.pollFirstEntry();
             final long time = e.getKey();
             for (final Arc arc : e.getValue()) {
                 final long end_time = time + (long) (_expansion * rand());
                 arc_writer.queue(end_time, new ArcEvent(arc, ArcEvent.Type.DOWN));
                 lastArcs.remove(arc);
             }
         }
     }
 
     private long rand() {
         return Math.abs(rng.nextLong()) % _period;
     }
 
     @Override
     public void incr(long dt) throws IOException {
         expireBeacons();
         arc_writer.flush(cur_time - (_tol + 1) * _period);
         cur_time += dt;
     }
 
     @Override
     public void seek(long time) {
         cur_time = time;
     }
 
     @Override
     public void convert() throws IOException {
         arc_writer = _arcs.getWriter();
         beacon_reader = _beacons.getReader();
 
         beacon_reader.bus().addListener(detectedListener());
 
         final Runner runner = new Runner(_period, _beacons.minTime(), _beacons.maxTime());
         runner.addGenerator(beacon_reader);
         runner.add(this);
 
         runner.run();
 
         arc_writer.flush(cur_time + (_tol + 1) * _period);
 
         arc_writer.setPropertiesFromTrace(_beacons);
 
         arc_writer.close();
         beacon_reader.close();
     }
 }
