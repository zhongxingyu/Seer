 /*
  * Copyright 2012 EMBL - European Bioinformatics Institute
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or impl
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.ebi.fg.annotare2.magetabcheck.checker;
 
 import org.junit.Test;
 import uk.ac.ebi.fg.annotare2.magetabcheck.checker.annotation.MageTabCheck;
 
 import java.lang.reflect.Method;
 
import static org.junit.Assert.*;
 import static org.junit.Assert.assertFalse;
import static uk.ac.ebi.fg.annotare2.magetabcheck.checker.CheckApplicationType.ANY;
import static uk.ac.ebi.fg.annotare2.magetabcheck.checker.CheckApplicationType.HTS_ONLY;
import static uk.ac.ebi.fg.annotare2.magetabcheck.checker.CheckApplicationType.MICRO_ARRAY_ONLY;
 import static uk.ac.ebi.fg.annotare2.magetabcheck.checker.ClassInstanceProvider.DEFAULT_CLASS_INSTANCE_PROVIDER;
 import static uk.ac.ebi.fg.annotare2.magetabcheck.checker.ExperimentType.HTS;
 import static uk.ac.ebi.fg.annotare2.magetabcheck.checker.ExperimentType.MICRO_ARRAY;
 
 /**
  * @author Olga Melnichuk
  */
 public class MethodBasedCheckDefinitionTest {
 
     private static class MethodBasedChecks {
         @MageTabCheck(value = "check 1", application = MICRO_ARRAY_ONLY)
         public void check1(String s) {
         }
 
         @MageTabCheck(value = "check 2", application = HTS_ONLY)
         public void check2(Integer i) {
         }
 
         @MageTabCheck(value = "check 3", application = ANY)
         public void check3(Double d) {
         }
     }
 
     @Test
     public void test() {
         Method[] methods = MethodBasedChecks.class.getDeclaredMethods();
 
         MethodBasedCheckDefinition def1 = new MethodBasedCheckDefinition(methods[0], DEFAULT_CLASS_INSTANCE_PROVIDER);
         assertTrue(def1.isSubjectTypeAssignableFrom(String.class));
         assertFalse(def1.isSubjectTypeAssignableFrom(Integer.class));
         assertTrue(def1.isApplicable(String.class, MICRO_ARRAY));
         assertFalse(def1.isApplicable(Integer.class, MICRO_ARRAY));
         assertFalse(def1.isApplicable(String.class, HTS));
 
         MethodBasedCheckDefinition def2 = new MethodBasedCheckDefinition(methods[1], DEFAULT_CLASS_INSTANCE_PROVIDER);
         assertTrue(def2.isSubjectTypeAssignableFrom(Integer.class));
         assertFalse(def2.isSubjectTypeAssignableFrom(String.class));
         assertTrue(def2.isApplicable(Integer.class, HTS));
         assertFalse(def2.isApplicable(String.class, HTS));
         assertFalse(def2.isApplicable(Integer.class, MICRO_ARRAY));
 
         MethodBasedCheckDefinition def3 = new MethodBasedCheckDefinition(methods[2], DEFAULT_CLASS_INSTANCE_PROVIDER);
         assertTrue(def3.isSubjectTypeAssignableFrom(Double.class));
         assertFalse(def3.isSubjectTypeAssignableFrom(String.class));
         assertTrue(def3.isApplicable(Double.class, HTS));
         assertTrue(def3.isApplicable(Double.class, MICRO_ARRAY));
         assertFalse(def3.isApplicable(Integer.class, MICRO_ARRAY));
     }
 }
