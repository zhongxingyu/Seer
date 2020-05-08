 /*  	CASi Context Awareness Simulation Software
  *   Copyright (C) 2011 2012  Moritz Bürger, Marvin Frick, Tobias Mende
  *
  *  This program is free software. It is licensed under the
  *  GNU Lesser General Public License with one clarification.
  *  
  *  You should have received a copy of the 
  *  GNU Lesser General Public License along with this program. 
  *  See the LICENSE.txt file in this projects root folder or visit
  *  <http://www.gnu.org/licenses/lgpl.html> for more details.
  */
 package de.uniluebeck.imis.casi.simulation.model.actionHandling;
 
import java.io.Serializable;
 import java.util.Collection;
 
 /**
  * This interface declares schedulers that are able to select an actions that
  * should be performed next out of lists or sets of actions.
  * 
  * @author Tobias Mende
  * 
  */
public interface IActionScheduler extends Serializable{
 
 	/**
 	 * Sets the list of actions that should be performed in every case
 	 * 
 	 * @param todoList
 	 *            the list to set
 	 */
 	public void setTodoList(Collection<AbstractAction> todoList);
 
 	/**
 	 * Sets a pool of actions that might be performed if there are gaps in the
 	 * todo list.
 	 * 
 	 * @param actionPool
 	 *            the pool to set.
 	 */
 	public void setActionPool(Collection<AbstractAction> actionPool);
 
 	/**
 	 * Adds a new action to the todo list
 	 * 
 	 * @param action
 	 *            the action to add.
 	 */
 	public void schedule(AbstractAction action);
 
 	/**
 	 * Adds an action as optional action to the todo list
 	 * 
 	 * @param action
 	 *            the action to add.
 	 */
 	public void addPoolAction(AbstractAction action);
 	
 	/**
 	 * Adds an action that should be performed next.
 	 * @param action the action to set
 	 */
 	public void addInterruptAction(AbstractAction action);
 	
 	/**
 	 * Checks whether there is an interrupt action or not
 	 * @return <code>true</code> if there is an interrupt action, <code>false</code> otherwise.
 	 */
 	public boolean isInterruptScheduled();
 
 	/**
 	 * This method selects an action that should be performed next and deletes
 	 * it from the list of available actions.
 	 * 
 	 * @return the action that should be performed next.
 	 */
 	
 	public AbstractAction getNextAction();
 	
 	
 }
