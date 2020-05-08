 /**
  * 
  */
 package org.arachna.netweaver.javadoc;
 
 import java.util.HashSet;
 
 import org.apache.velocity.app.VelocityEngine;
 import org.arachna.ant.AntHelper;
 import org.arachna.netweaver.dc.types.DevelopmentComponentFactory;
 import org.arachna.netweaver.dc.types.DevelopmentConfiguration;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 
 /**
  * Unittests for {@link BuildFileGenerator}.
  * 
 * @author Dirk Weigenand
  */
 public class BuildFileGeneratorTest {
     private BuildFileGenerator generator;
 
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp() throws Exception {
         DevelopmentConfiguration developmentConfiguration = new DevelopmentConfiguration("DI1");
         DevelopmentComponentFactory dcFactory = new DevelopmentComponentFactory();
         AntHelper antHelper = new AntHelper("", dcFactory);
         this.generator =
             new BuildFileGenerator(developmentConfiguration, antHelper, dcFactory, new HashSet<String>(),
                 new VelocityEngine(), false);
     }
 
     /**
      * @throws java.lang.Exception
      */
     @After
     public void tearDown() throws Exception {
     }
 
     @Test
     public final void test() {
         assertThat(this.generator.normalize("c:\\temp"), equalTo("c:/temp"));
     }
 }
