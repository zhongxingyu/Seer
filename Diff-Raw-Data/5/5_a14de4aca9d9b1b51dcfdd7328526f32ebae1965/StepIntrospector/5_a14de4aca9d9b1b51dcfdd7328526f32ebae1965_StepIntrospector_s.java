 /*===========================================================================
   Copyright (C) 2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.common.pipeline.annotations;
 
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.exceptions.OkapiBadStepInputException;
 import net.sf.okapi.common.pipeline.IPipelineStep;
 
 public final class StepIntrospector {
 
 	public static List<ConfigurationParameter> getStepParameters(
 			IPipelineStep step) {
 		List<ConfigurationParameter> parameters = new LinkedList<ConfigurationParameter>();
 
 		// get all the declared fields (skipping any inherited ones) of the
 		// object including protected and private
 		Method[] methods = step.getClass().getDeclaredMethods();
 
 		// iterate over the fields and pull out the ones that have
 		// StepConfigurationParameter or StepExternalParameter annotations
 		for (Method m : methods) {
 			if (m.isAnnotationPresent(StepParameterMapping.class)) {
 				StepParameterMapping a = m
 						.getAnnotation(StepParameterMapping.class);
 				parameters.add(new ConfigurationParameter(m, a.parameterType(),
 						step));
 			}
 		}
 		return parameters;
 	}
 
 	public static List<String> getStepEventHandlers(IPipelineStep step) {
 		List<String> eventHandlers = new LinkedList<String>();
 		Method[] methods = step.getClass().getDeclaredMethods();
 		for (Method m : methods) {
 			// only look at methods with a single Event
 			// parameter and return type
			if (m.getName().startsWith("handle")
 					&& m.getParameterTypes().length == 1
 					&& Arrays.asList(m.getParameterTypes()).contains(
 							Event.class)) {
 				eventHandlers.add(m.getName());
 			}
 		}
 
 		if (eventHandlers.size() <= 0) {
 			throw new OkapiBadStepInputException(
					"Steps must implement at least one handler method");
 		}
 
 		return eventHandlers;
 	}
 }
