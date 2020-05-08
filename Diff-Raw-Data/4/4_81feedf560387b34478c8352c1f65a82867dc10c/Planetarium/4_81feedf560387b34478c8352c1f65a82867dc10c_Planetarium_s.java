 /**
  * Copyright (c) 2005, 2010, Werner Keil, Ikayzo and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Werner Keil, Ikayzo and others - initial API and implementation
  */
 package org.eclipse.uomo.examples.units.console;
 
 import static org.eclipse.uomo.core.impl.OutputHelper.CONSOLE_OUTPUT;
 import static org.eclipse.uomo.core.impl.OutputHelper.println;
 
 import org.eclipse.uomo.examples.units.types.Planet;
import org.junit.Test;
 
 /**
  * @author Werner Keil
  * @version 1.4 ($Revision: 90 $), $Date: 2011-01-11 18:39:26 +0100 (Tu, 11 Jan
  *          2011) $
  */
 public class Planetarium {
 	private static final Planetarium solarSystem = new Planetarium();
 
 	private Planetarium() {
 		System.setProperty(CONSOLE_OUTPUT, "true");
 	}
 	
 	public static void main(String[] argv) {
 		solarSystem.testPlanets();
 	}
 
	@Test
 	public void testPlanets() {
 		Planet[] solarSystem = Planet.values();
 
 		for (Planet planet : solarSystem) {
 			println(planet);
 		}
 	}
 }
