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
 
 import java.util.Collections;
 import java.util.Map;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 
 import uk.ac.imperial.presage2.core.db.persistent.PersistentAgent;
 import uk.ac.imperial.presage2.core.db.persistent.TransientAgentState;
 
 public class AgentState implements TransientAgentState {
 
 	final static String agentStateCollection = "agentstate";
 	final DB db;
 	final DBCollection states;
 	final MongoObject object;
 	final Agent agent;
 
 	AgentState(Agent agent, int time, DB db) {
 		super();
 		this.db = db;
 		this.states = db.getCollection(agentStateCollection);
 		this.agent = agent;
 
 		// indices
 		DBObject index = new BasicDBObject();
 		index.put("time", 1);
 		index.put("aid", 1);
 		states.ensureIndex(index, "simID", true);
 
 		DBObject query = new BasicDBObject();
 		query.put("time", time);
 		query.put("aid", agent.getID());
 		DBObject state = this.states.findOne(query);
 
 		if (state == null) {
 			// create new
 			this.object = new MongoObject();
 			object.putInt("time", time);
 			object.put("aid", agent.getID());
 			object.put("properties", new MongoObject());
 			states.insert(object);
 		} else {
 			// use existing
 			this.object = new MongoObject(state);
 		}
 	}
 
 	@Override
 	public int getTime() {
 		return object.getInt("time");
 	}
 
 	@Override
 	public PersistentAgent getAgent() {
 		return this.agent;
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
 		states.save(object);
 	}
 
	@SuppressWarnings("unchecked")
 	@Override
 	public Map<String, String> getProperties() {
		return Collections
				.unmodifiableMap(((DBObject) object.get("properties")).toMap());
 	}
 
 }
