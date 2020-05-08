 /* Copyright 2011-2012 Netherlands Forensic Institute and
                        Centrum Wiskunde & Informatica
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 
 package org.dimitri_lang.validator.test;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 import org.dimitri_lang.*;
 import org.dimitri_lang.validator.*;
 import org.dimitri_lang.validator.generated.*;
 
 public class TestGeneratedValidators {
 
 	public final static String TEST_DIRECTORY = "testdata";
 	public final static String TEST_FILE = "280px-PNG_transparency_demonstration_1.png";
 	private String _name;
 	
 	public TestGeneratedValidators() {
 		_name = TEST_FILE;
 	}
 
 	@Test
 	public void testGeneratedValidator() {
 		Validator validator = new PNGValidator();
 		ValidatorInputStream stream = ValidatorInputStreamFactory.create(TEST_DIRECTORY + "/" + _name);
 		validator.setStream(stream);
 		ParseResult result = validator.tryParse();
 		Assert.assertTrue("Parsing failed. " + validator.getClass() + " on " + _name + ". Last read: " + result.getLastRead() + "; Last location: " + result.getLastLocation() + "; Last symbol: " + result.getSymbol() + "; Sequence: " + result.getSequence(), result.isSuccess());
 	}
 
 }
