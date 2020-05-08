 /*
  * Copyright 2011-2013 Jeroen Meetsma - IJsberg
  *
  * This file is part of Iglu.
  *
  * Iglu is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Iglu is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with Iglu.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.ijsberg.iglu.mvc.mapping;
 
 import org.ijsberg.iglu.mvc.RequestDispatcher;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Properties;
 
 /**
  * Mapping definition of a process or series of actions,
  * possibly related to a form such as 'account data'.
  */
 public class Process extends MapElement {
 	private ArrayList elements = new ArrayList();
 
 	/**
 	 * @param argument
 	 * @param depth
 	 * @param lineNr
 	 */
 	public Process(String argument, int depth, int lineNr) {
 		super(argument, depth, lineNr);
 	}
 
 	/**
 	 * @param dispatcher
 	 * @return
 	 */
 	public String check(RequestDispatcher dispatcher) {
 		if (argument == null || "".equals(argument)) {
 			return "missing label";
 		}
 
 		if ("error".equals(argument)) {
 			boolean terminated = false;
 			Iterator i = elements.iterator();
 			while (i.hasNext()) {
 				MapElement fe = (MapElement) i.next();
 				//we can't process events here
 				if (fe instanceof ResponseWriter) {
 					terminated = true;
 				}
 			}
 			if (!terminated) {
 				return "Error handling must result in a response!";
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * @param fe
 	 * @return
 	 */
 	public boolean addFlowElement(MapElement fe) {
 		//we swallow tasks, redirects, Events
 		if (terminated) {
 			//we cannot add dead code
 			return false;
 		}
 
 		if (fe instanceof ExceptionHandler) {
 			//TODO only 1 allowed
 			exceptionHandler = (ExceptionHandler) fe;
 			return true;
 		} else if ((fe instanceof Invocation) ||
 				(fe instanceof Process) ||
 				(fe instanceof ResponseWriter)) {
 			if (fe instanceof ResponseWriter) {
 				terminated = true;
 			}
 			elements.add(fe);
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * @param processArray
 	 * @param requestProperties
 	 * @param frh
 	 * @return
 	 * @throws Throwable
 	 */
 	public boolean processRequest(String[] processArray, Properties requestProperties, RequestDispatcher frh) throws Exception {
 		timesProcessed++;
 		//success is defined by a successful redirect
 

 		Iterator i = elements.iterator();
 		while (i.hasNext()) {
 			MapElement fe = (MapElement) i.next();
 			//we can't process events here
 			try {
 				if (!(fe instanceof Process)) {
 					//we process tasks and redirects
 					if (fe.processRequest(processArray, requestProperties, frh)) {
 						//					System.out.println("redirect in element " + fe.getLabel());
 						return true;
 					}
 				} else {
 					if (processArray != null && processArray.length > 0 && processArray[0].equals(fe.getArgument())) {
 						String[] subProcessArray = new String[processArray.length - 1];
 						System.arraycopy(processArray, 1, subProcessArray, 0, subProcessArray.length);
 						if (fe.processRequest(subProcessArray, requestProperties, frh)) {
 							return true;
 						}
 					}
 				}
 			} catch (Exception e) {
 				return handleException(processArray, requestProperties, frh, e);
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @return
 	 */
 	public String toString() {
 		StringBuffer result = new StringBuffer(indent() + getArgument() + " -> " + timesProcessed + "\n");
 		if (elements != null) {
 			Iterator i = elements.iterator();
 			while (i.hasNext()) {
 				MapElement fe = (MapElement) i.next();
 				result.append(fe.toString());
 			}
 		}
 		return result.toString();
 	}
 }
