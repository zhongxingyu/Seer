 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.gda;
 
 /**
  * A class which holds information about the testing framework state.
  */
 public class ClientManager {
 
 	
 	private static boolean testingMode = false;
 	/**
 	 * @return Returns the testingMode.
 	 */
 	public static boolean isTestingMode() {
 		return testingMode;
 	}
 	/**
 	 * @param testingMode The testingMode to set.
 	 */
 	public static void setTestingMode(boolean testingMode) {
 		ClientManager.testingMode = testingMode;
 	}
 	
 	/**
 	 * @return In testing mode this returns false and in gda client mode, true
 	 */
 	public static boolean isClient() {
 		// TODO Make test more appropriate
<<<<<<< HEAD
=======
		
		// FIXME have removed test for gda.root, but this looks flakey. Needs fixing.
>>>>>>> branch '1.1' of ssh://dascgitolite@dasc-git.diamond.ac.uk/gda/gda-common.git
 		if (System.getProperty("gda.config")==null) {
 			return false;
 		}
 		return true;
 	}
 
 }
