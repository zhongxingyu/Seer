 /*
  * This file is part of CBCJVM.
  * CBCJVM is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * CBCJVM is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with CBCJVM.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package cbccore.events;
 
 /**
  * Every event should subclass this. Event types may be dynamic, and you can
  * decide how to deal with this, or they can be static (most common). Look at
  * the example code in the "tests" folder included in the CBCJVM distribution.
  * This class also contains a public "data" variable of type "E" for easy ad-hoc
  * expandablity.
  * 
  * @author Braden McDorman, Benjamin Woodruff
  * @see    EventManager
  * @see    EventType
  */
 
 public class Event<E> extends java.util.EventObject {
 	private static final long serialVersionUID = 146392288658724975L;
 	private EventType handle;
 	public E data;
 	@SuppressWarnings("unused")
 	private EventManager manager;
 	
 	public Event(EventType handle) {
		this(handle, null);
 	}
 	
 	public Event(EventType handle, Object source) {
		super(source);
 		this.handle = handle;
 	}
 	public EventType getType() {
 		return handle;
 	}
 	public void emit() {
 		EventManager.get().__emit(this);
 	}
 	//this function would mess up static event types, by removing all of a type
 	//suspending until we find a cleaner way of doing this
 	/*public void dispose() {
 		EventManager.get().__dispose(this);
 		handle = -1;
 	}*/
 }
