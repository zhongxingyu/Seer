 /*
  * This source file is part of CaesarJ 
  * For the latest info, see http://caesarj.org/
  * 
  * Copyright  2003-2005 
  * Darmstadt University of Technology, Software Technology Group
  * Also see acknowledgements in readme.txt
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
 * $Id: AllTests.java,v 1.16 2005-03-02 13:15:02 gasiunas Exp $
  */
 
 package org.caesarj.test;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 /**
  * provides all Caesar tests.
  * 
  * @author Andreas Wittmann, Sven Kloppenburg
  *
  * @see CompileAndRunResultsTest
  * @see CompileAndRunResultsCITest
  */
 public class AllTests {
 	public static Test suite() {
 		TestSuite suite = new TestSuite("all Caesar tests");
 		//$JUnit-BEGIN$
         suite.addTestSuite( VirtualClassesTests.class );
         //suite.addTestSuite( CompileAndRunResultsWeaverTest.class );
         suite.addTestSuite( AspectDeploymentTests.class);
        suite.addTestSuite( CompilerErrorsTests.class);
         suite.addTestSuite( StructureModelTests.class);
         suite.addTestSuite( PackageTests.class );
         //$JUnit-END$
 		return suite;
 	}
 }
