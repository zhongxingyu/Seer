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
 package ditl;
 
 import java.io.*;
 import java.util.*;
 
 public class StatefulWriter<E, S> extends Writer<E> {
 
 	private final static int n_event_trigger = 1000; // trigger a snapshot every 1000 events
 	
 	long last_snap;
	long next_snap_trigger = Trace.INFINITY;
 	long state_max_interval;
 	long state_min_interval;
 	int event_count = 0;
 	BufferedWriter snap_writer;
 	StateUpdater<E,S> _updater;
 	
 	public StatefulWriter(Store store, String name, StateUpdater<E,S> updater, PersistentMap info) throws IOException {
 		super(store, name, info);
 		last_snap = -Trace.INFINITY;
 		_updater = updater;
 		snap_writer = new BufferedWriter( new OutputStreamWriter(_store.getOutputStream(_store.snapshotsFile(_name))));
 		state_min_interval = Trace.INFINITY;
 		state_max_interval = -Trace.INFINITY;
 	}
 	
 	public Set<S> states(){
 		return _updater.states();
 	}
 	
 	@Override
 	public void write(long time, E event) throws IOException {
 		super.write(time, event);
 		event_count++;
 		_updater.handleEvent(time, event);
 	}
 	
 	@Override
 	void updateTime(long time) throws IOException {
 		super.updateTime(time);
 		if ( last_snap == -Trace.INFINITY ){ // no snap has yet been made
 			last_snap = last_time;
 			write_snapshot (last_snap);
 		} else if ( event_count > n_event_trigger ){
			next_snap_trigger = time;
		} else if ( time > next_snap_trigger ){
 			write_snapshot(time);
 			long dt = time - last_snap;
 			if ( dt > state_max_interval ) state_max_interval = dt;
			if ( dt < state_min_interval ) state_min_interval = dt;
			next_snap_trigger = Trace.INFINITY;
 			last_snap = time;
 			event_count = 0;
 		}
 	}
 	
 	@Override
 	void setRemainingInfo(){
 		super.setRemainingInfo();
 		if ( state_max_interval < 0 ){ // single event trace
 			state_max_interval = Long.parseLong(_info.get(Trace.maxTimeKey)) - Long.parseLong(_info.get(Trace.minTimeKey));
 			state_min_interval = state_max_interval;
 		}
 		_info.setIfUnset(Trace.stateMaxUpdateIntervalKey, state_max_interval);
 		_info.setIfUnset(Trace.stateMinUpdateIntervalKey, state_min_interval);
 	}
 	
 	@Override
 	public void close() throws IOException {
 		snap_writer.close();
 		super.close();
 	}
 	
 	public void setInitState(long time, Collection<S> states) throws IOException {
 		_updater.setState(states);
 		write_snapshot(time);
 		last_snap = time;
 		min_time = time;
 	}
 	
 	private void write_snapshot(long time) throws IOException {
 		snap_writer.write(time+"\n");
 		for ( S state : _updater.states() )
 			snap_writer.write(state+"\n");
 	}
 }
