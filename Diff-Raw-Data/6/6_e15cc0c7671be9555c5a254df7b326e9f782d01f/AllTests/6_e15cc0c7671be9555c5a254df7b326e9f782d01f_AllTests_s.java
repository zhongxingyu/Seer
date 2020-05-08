 /**
  * Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package fr.imag.adele.apam.test.support;
 
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.Suite.SuiteClasses;
 
import fr.imag.adele.apam.test.messages.PullMessageTest;
 import fr.imag.adele.apam.test.obrman.OBRMANTest;
 import fr.imag.adele.apam.test.testcases.CompositeTest;
 import fr.imag.adele.apam.test.testcases.ConstraintTest;
 import fr.imag.adele.apam.test.testcases.DynamanDependentTest;
 import fr.imag.adele.apam.test.testcases.InjectionInstantiationTest;
 import fr.imag.adele.apam.test.testcases.PropertyTest;
 
 @RunWith(Suite.class)
 @SuiteClasses({ CompositeTest.class, ConstraintTest.class,
		InjectionInstantiationTest.class, PropertyTest.class,DynamanDependentTest.class, OBRMANTest.class, PullMessageTest.class })
 public class AllTests {
 
 }
