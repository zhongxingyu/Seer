 /*
  * This file is part of TaskMan
  *
  * Copyright (C) 2012 Jed Barlow, Mark Galloway, Taylor Lloyd, Braeden Petruk
  *
  * TaskMan is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * TaskMan is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with TaskMan.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ca.cmput301.team13.taskman.model;
 
 import java.util.ArrayList;
 
import ca.cmput301.team13.taskman.model.Requirement.contentType;

 import android.content.Context;
 import android.util.Log;
 
 public class VirtualRepository {
 	private LocalRepository local;
 
 	public VirtualRepository(Context context) {
 		local = new LocalRepository(context, this);
 	}
 	
 	/**
 	 * Obtains a list of Tasks for display
 	 * 
 	 * @param filter The TaskFilter with which to obtain Tasks
 	 * @return an ArrayList of compatible Tasks
 	 */
 	public ArrayList<Task> getTasksForFilter(TaskFilter tf) {
 		return local.loadTasks(tf);
 	}
 	
	/**
	 * Creates a Requirement and links it to a Task
	 * @param creator The User creating the requirement (null to use Task's creator)
	 * @param t The Task to add the Requirement to
	 * @param content The content type specified by the Requirement
	 * @return
	 */
	public Requirement addRequirementToTask(User creator, Task t, contentType content) {
		if(creator == null) {
			creator = t.getCreator();
		}
		Requirement r = local.createRequirement(creator, t, content);
		t.addRequirement(r);
		return r;
	}
	
	/**
	 * Creates a Fulfillment and links it to a Requirement
	 * @param creator The User creating the fulfillment (null to use Requirement's creator)
	 * @param r The Requirement to add the Fulfillment to
	 * @return
	 */
	public Fulfillment addFulfillmentToRequirement(User creator, Requirement r) {
		if(creator == null) {
			creator = r.getCreator();
		}
		Fulfillment f = local.createFulfillment(creator, r);
		r.addFulfillment(f);
		return f;
	}
	
 
 	/**
 	 * Saves any changes to the notifying object to the permanent store
 	 * @param backedObject - the object with changes
 	 */
 	boolean saveUpdate(BackedObject backedObject) {
 		if(backedObject instanceof Task) {
 			local.updateTask((Task)backedObject);
 			return true;
 		} else if(backedObject instanceof Requirement) {
 			local.updateRequirement((Requirement)backedObject);
 			return true;
 		} else if(backedObject instanceof Fulfillment) {
 			local.updateFulfillment((Fulfillment)backedObject);
 			return true;
 		}
 		//If we're here, then we didn't detect a type. Perhaps a new feature isn't fully implemented?
 		Log.w("VirtualRepository", "Attempted to save changes to unknown object: "+backedObject.getClass().toString());
 		return false;
 	}
 
 }
