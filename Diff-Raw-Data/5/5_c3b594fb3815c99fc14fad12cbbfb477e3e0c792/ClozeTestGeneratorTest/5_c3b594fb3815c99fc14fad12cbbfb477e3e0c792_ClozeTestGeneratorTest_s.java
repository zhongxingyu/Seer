 /*
  * Copyright (C) 2012 Fabian Hirschmann <fabian@hirschm.net>
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package com.github.fhirschmann.clozegen.lib;
 
 import com.github.fhirschmann.clozegen.lib.adapters.CollocationAdapter;
 import com.github.fhirschmann.clozegen.lib.adapters.GenericSingleTokenInputAdapter;
 import com.github.fhirschmann.clozegen.lib.components.GapAnnotator;
 import com.github.fhirschmann.clozegen.lib.constraints.resources.PrepositionConstraintResource;
 import com.github.fhirschmann.clozegen.lib.constraints.resources.TypeConstraintResource;
 import com.github.fhirschmann.clozegen.lib.register.AnnotatorRegisterEntry;
 import com.github.fhirschmann.clozegen.lib.util.Resources2;
 import com.google.common.base.Charsets;
 import com.google.common.collect.Sets;
 import com.google.common.io.Files;
 import com.google.common.io.Resources;
 import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ART;
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import org.apache.uima.UIMAException;
 import org.apache.uima.analysis_engine.AnalysisEngineDescription;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.junit.AfterClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 import org.junit.Before;
 import static org.junit.matchers.JUnitMatchers.*;
 import org.junit.BeforeClass;
 import static org.uimafit.factory.ExternalResourceFactory.createExternalResourceDescription;
 
 /**
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class ClozeTestGeneratorTest {
     private ClozeTestGenerator gen;
     private AnnotatorRegisterEntry entry;
 
     @Before
     public void setUp() throws ResourceInitializationException {
         gen = new ClozeTestGenerator();
         entry = new AnnotatorRegisterEntry("test",
                 GapAnnotator.class,
                 GapAnnotator.CONSTRAINT_KEY,
                 createExternalResourceDescription(
                     TypeConstraintResource.class,
                     TypeConstraintResource.PARAM_TYPE, ART.class.getName()),
                 GapAnnotator.ADAPTER_KEY,
                 createExternalResourceDescription(
                 GenericSingleTokenInputAdapter.class,
                 GenericSingleTokenInputAdapter.PARAM_GENERATOR_CLASS,
                 "com.github.fhirschmann.clozegen.lib.generators.DummyGapGenerator"));
         entry.setSupportedLanguages(Sets.newHashSet("en"));
        gen.getAnnotatorRegister().add(entry);
     }
 
     @Test
     public void testRun1() throws UIMAException, IOException {
         gen.activate("test", 1);
         File outFile = File.createTempFile("ctgt", ".clz");
         gen.run(Resources.getResource("clozetest/input.txt"),
                 outFile.toURI().toURL(), "en");
         List<String> actual = Files.readLines(outFile, Charsets.UTF_8);
         List<String> expected = Resources.readLines(
                 Resources.getResource("clozetest/output.clz"), Charsets.UTF_8);
         for (int i = 0; i < actual.size(); i++) {
             assertThat(actual.get(i), is(expected.get(i)));
         }
     }
 
     @Test
     public void testRun2() throws ResourceInitializationException, IOException,
             UIMAException {
         gen.activate("test", 1);
         List<String> expected = Resources.readLines(
                 Resources.getResource("clozetest/output.clz"), Charsets.UTF_8);
         String result = gen.run(Resources.getResource("clozetest/input.txt"), "en");
         int i = 0;
         for (String line : result.split(System.getProperty("line.separator"))) {
             assertThat(line, is(expected.get(i++)));
         }
     }
 }
