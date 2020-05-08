 package org.apache.archiva.redback.components.modello.jpox;
 
 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 import org.codehaus.modello.ModelloParameterConstants;
 import org.codehaus.modello.core.ModelloCore;
 import org.codehaus.modello.model.Model;
 import org.codehaus.plexus.util.ReaderFactory;
 import org.dom4j.Document;
 import org.dom4j.io.SAXReader;
 
 import java.io.File;
 import java.util.Properties;
 
 
 /**
  * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
  * @version $Id: JPoxJdoMappingModelloGeneratorTest.java 840 2007-07-17 18:50:39Z hboutemy $
  */
 public class JPoxJdoMappingModelloGeneratorTest
     extends AbstractJpoxGeneratorTestCase
 {
     public JPoxJdoMappingModelloGeneratorTest()
     {
         super( "jpox-jdo-mapping" );
     }
 
     public void testSimpleInvocation()
         throws Exception
     {
         ModelloCore core = (ModelloCore) lookup( ModelloCore.ROLE );
 
         Model model =
             core.loadModel( ReaderFactory.newXmlReader( getTestFile( "src/test/resources/mergere-tissue.mdo" ) ) );
 
         // ----------------------------------------------------------------------
         // Generate the code
         // ----------------------------------------------------------------------
 
         Properties parameters = new Properties();
 
         parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, getOutputDirectory().getAbsolutePath() );
 
         parameters.setProperty( ModelloParameterConstants.VERSION, "1.0.0" );
 
         parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.FALSE.toString() );
 
         core.generate( model, "jpox-jdo-mapping", parameters );
 
         // ----------------------------------------------------------------------
         // Assert
         // ----------------------------------------------------------------------
 
         assertTrue( new File( getOutputDirectory(), "package.jdo" ).exists() );
 
         //assertGeneratedFileExists( "package.jdo" );
 
         SAXReader reader = new SAXReader();
         reader.setEntityResolver( new JdoEntityResolver() );
        Document jdoDocument = reader.read( new File( "target/" + getName() + "/package.jdo" ) );
 
         assertNotNull( jdoDocument );
 
         // Tree should consist of only elements with attributes. NO TEXT.
         assertNoTextNodes( jdoDocument, "//jdo", true );
 
         assertAttributeEquals( jdoDocument, "//class[@name='TissueModelloMetadata']/field[@name='modelVersion']/column",
                                "default-value", "1.0.0" );
 
         assertAttributeEquals( jdoDocument, "//class[@name='Issue']/field[@name='summary']", "persistence-modifier",
                                "none" );
 
         // -----------------------------------------------------------------------
         // Association Tests.
 
         //   mdo/association/jpox.dependent-element == false (only on association with "*" multiplicity (default type)
         assertAttributeEquals( jdoDocument, "//class[@name='Issue']/field[@name='friends']/collection",
                                "dependent-element", "false" );
 
         //   mdo/association (default type) with "1" multiplicity.
         assertElementNotExists( jdoDocument, "//class[@name='Issue']/field[@name='assignee']/collection" );
 
         //   mdo/association (map) with "*" multiplicity.
         assertElementExists( jdoDocument, "//class[@name='Issue']/field[@name='configuration']/map" );
 
         // -----------------------------------------------------------------------
         // Fetch Group Tests
         assertAttributeMissing( jdoDocument, "//class[@name='Issue']/field[@name='reporter']", "default-fetch-group" );
         assertAttributeEquals( jdoDocument, "//class[@name='Issue']/field[@name='configuration']",
                                "default-fetch-group", "true" );
         assertAttributeEquals( jdoDocument, "//class[@name='Issue']/field[@name='assignee']", "default-fetch-group",
                                "false" );
 
         // -----------------------------------------------------------------------
         // Value Strategy Tests
         //   defaulted
         assertAttributeEquals( jdoDocument, "//class[@name='ComplexIdentity']/field[@name='id']", "value-strategy",
                                "native" );
         assertAttributeEquals( jdoDocument, "//class[@name='Issue']/field[@name='accountId']", "value-strategy",
                                "native" );
         //   intentionally unset
         assertAttributeMissing( jdoDocument, "//class[@name='User']/field[@name='id']", "value-strategy" );
 
         // -----------------------------------------------------------------------
         // Superclass Tests
         assertAttributeEquals( jdoDocument, "//class[@name='User']", "persistence-capable-superclass",
                                "org.mergere.user.AbstractUser" );
 
         // -----------------------------------------------------------------------
         // Primary Key Tests
         assertAttributeEquals( jdoDocument, "//class[@name='Issue']/field[@name='accountId']", "primary-key", "true" );
         assertAttributeEquals( jdoDocument, "//class[@name='Issue']/field[@name='summary']", "primary-key", "false" );
 
         assertAttributeEquals( jdoDocument, "//class[@name='ComplexIdentity']/field[@name='id']", "primary-key",
                                "true" );
         assertAttributeEquals( jdoDocument, "//class[@name='ComplexIdentity']/field[@name='username']", "primary-key",
                                "false" );
         assertAttributeEquals( jdoDocument, "//class[@name='ComplexIdentity']/field[@name='fullName']", "primary-key",
                                "false" );
         assertAttributeEquals( jdoDocument, "//class[@name='ComplexIdentity']/field[@name='email']", "primary-key",
                                "false" );
         assertAttributeEquals( jdoDocument, "//class[@name='ComplexIdentity']/field[@name='locked']", "primary-key",
                                "false" );
         assertAttributeEquals( jdoDocument, "//class[@name='ComplexIdentity']/field[@name='lastLoginDate']",
                                "primary-key", "false" );
 
         // -----------------------------------------------------------------------
         // Alternate Table and Column Names Tests.
         assertAttributeEquals( jdoDocument, "//class[@name='DifferentTable']", "table", "MyTable" );
         assertAttributeEquals( jdoDocument, "//class[@name='Issue']/field[@name='accountId']/column", "name", "id" );
     }
 }
