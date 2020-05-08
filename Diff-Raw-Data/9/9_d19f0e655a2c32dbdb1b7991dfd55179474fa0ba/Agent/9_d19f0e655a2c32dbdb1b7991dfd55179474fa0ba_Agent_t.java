 /**
  * 	Copyright (C) 2011 Sam Macbeth <sm1106 [at] imperial [dot] ac [dot] uk>
  *
  * 	This file is part of Presage2.
  *
  *     Presage2 is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU Lesser Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     Presage2 is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU Lesser Public License for more details.
  *
  *     You should have received a copy of the GNU Lesser Public License
  *     along with Presage2.  If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.ac.imperial.presage2.db.mongodb;
 
 import java.lang.ref.WeakReference;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import uk.ac.imperial.presage2.core.db.persistent.PersistentAgent;
 import uk.ac.imperial.presage2.core.db.persistent.TransientAgentState;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 
 public class Agent implements PersistentAgent {
 
 	final static String agentCollection = "agents";
 	final DB db;
 	final DBCollection agents;
 	final MongoObject object;
 	final Map<Integer, WeakReference<AgentState>> stateCache = new HashMap<Integer, WeakReference<AgentState>>();
 
 	Agent(UUID id, String name, Simulation sim, DB db) {
 		super();
 		this.db = db;
 		this.agents = db.getCollection(agentCollection);
 		this.object = new MongoObject();
 		// get indices
 		DBObject index = new BasicDBObject();
 		index.put("simID", 1);
 		index.put("aid", 1);
 		agents.ensureIndex(index, "simID", true);
 
 		// set object attributes
 		object.putLong("simID", sim.getID());
 		object.put("aid", id);
 		object.putString("name", name);
 		object.put("properties", new MongoObject());
 		object.putInt("registeredAt", 0);
 		object.putInt("deregisteredAt", 0);
 
 		this.agents.insert(object);
 	}
 
 	Agent(UUID aid, Simulation sim, DB db) {
 		super();
 		this.db = db;
 		this.agents = db.getCollection(agentCollection);
 		DBObject query = new BasicDBObject();
 		query.put("simID", sim.getID());
 		query.put("aid", aid);
 		this.object = new MongoObject(this.agents.findOne(query));
 	}
 
 	Agent(DBObject object, DB db) {
 		super();
 		this.db = db;
 		this.agents = db.getCollection(agentCollection);
 		this.object = new MongoObject(object);
 	}
 
 	@Override
 	public UUID getID() {
 		return (UUID) object.get("aid");
 	}
 
 	@Override
 	public String getName() {
 		return object.getString("name");
 	}
 
 	@Override
 	public void setRegisteredAt(int time) {
 		object.putInt("registeredAt", time);
 		this.agents.save(object);
 	}
 
 	@Override
 	public void setDeRegisteredAt(int time) {
 		object.putInt("deregisteredAt", time);
 		this.agents.save(object);
 	}
 
 	@Override
 	public String getProperty(String key) {
 		DBObject props = (DBObject) object.get("properties");
 		try {
 			return props.get(key).toString();
 		} catch (NullPointerException e) {
 			return null;
 		}
 	}
 
 	@Override
 	public void setProperty(String key, String value) {
 		DBObject props = (DBObject) object.get("properties");
 		props.put(key, value);
 		this.agents.save(object);
 	}
 
 	@Override
 	public TransientAgentState getState(int time) {
 		AgentState t;
 		synchronized (stateCache) {
 			if (stateCache.containsKey(time)) {
 				t = stateCache.get(time).get();
 				if (t != null)
 					return t;
 				// tidy map
				for (Integer key : stateCache.keySet()) {
					if (stateCache.get(key) == null) {
						stateCache.remove(key);
 					}
 				}
 
 			}
 			t = new AgentState(this, time, db);
 			stateCache.put(time, new WeakReference<AgentState>(t));
 		}
 		return t;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Map<String, String> getProperties() {
 		return Collections
 				.unmodifiableMap(((DBObject) object.get("properties")).toMap());
 	}
 
 }
