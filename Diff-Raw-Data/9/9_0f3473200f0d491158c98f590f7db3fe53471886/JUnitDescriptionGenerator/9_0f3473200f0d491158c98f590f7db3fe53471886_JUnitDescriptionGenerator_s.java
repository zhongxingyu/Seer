 /*
  * Copyright 2009 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.spockframework.runtime;
 
 import org.junit.runner.Description;
 
 import org.spockframework.runtime.model.*;
 
 /**
  * Generates and attaches JUnit Description's to a SpecInfo's nodes.
  *
  * @author Peter Niederwieser
  */
 public class JUnitDescriptionGenerator {
   private final SpecInfo spec;
   private final SpecInfo initialRequestor;
 
   public JUnitDescriptionGenerator(SpecInfo spec) {
     this(spec, spec);
   }
 
   private JUnitDescriptionGenerator(SpecInfo spec, SpecInfo initialRequestor) {
     this.spec = spec;
     this.initialRequestor = initialRequestor;
   }
 
   public void generate() {
     Description desc = Description.createSuiteDescription(spec.getReflection());
     spec.setMetadata(desc);
 
     SpecInfo superSpec = spec.getSuperSpec();
     if (superSpec != null) {
       new JUnitDescriptionGenerator(superSpec, initialRequestor).generate();
       for (FeatureInfo feature : superSpec.getFeatures())
         desc.addChild((Description) feature.getMetadata());
     }
 
     for (FeatureInfo feature : spec.getFeatures())
       desc.addChild(describeFeature(feature));
 
     describeMethod(spec.getSetupMethod());
     describeMethod(spec.getCleanupMethod());
     describeMethod(spec.getSetupSpecMethod());
     describeMethod(spec.getCleanupSpecMethod());
   }
 
   private Description describeFeature(FeatureInfo feature) {
     Description desc = describeMethod(feature.getFeatureMethod());
     feature.setMetadata(desc);
     if (feature.getDataProcessorMethod() != null)
       feature.getDataProcessorMethod().setMetadata(desc);
     for (DataProviderInfo prov : feature.getDataProviders())
       prov.getDataProviderMethod().setMetadata(desc);
     return desc;
   }
 
   private Description describeMethod(MethodInfo method) {
     Description desc = Description.createTestDescription(initialRequestor.getReflection(), method.getName());
     method.setMetadata(desc);
     return desc;
   }
 }
