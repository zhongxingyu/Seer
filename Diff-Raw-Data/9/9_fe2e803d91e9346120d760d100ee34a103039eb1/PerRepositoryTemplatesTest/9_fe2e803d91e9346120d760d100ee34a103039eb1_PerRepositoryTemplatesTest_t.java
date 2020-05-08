 /*
                         QueryJ
 
     Copyright (C) 2002-today  Jose San Leandro Armendariz
                               chous@acm-sl.org
 
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU General Public
     License as published by the Free Software Foundation; either
     version 2 of the License, or any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     General Public License for more details.
 
     You should have received a copy of the GNU General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
     Thanks to ACM S.L. for distributing this library under the GPL license.
     Contact info: jose.sanleandro@acm-sl.com
 
  ******************************************************************************
  *
  * Filename: PerRepositoryTemplatesTest.java
  *
  * Author: Jose San Leandro Armendariz (chous)
  *
  * Description: Maps steps in Cucumber scenarios for testing per-repository
  *              templates.
  *
  * Date: 2013/05/05
  * Time: 17:44
  *
  */
 package cucumber.templates;
 
 /*
  * Importing project classes.
  */
 import org.acmsl.queryj.metadata.DecoratorFactory;
 import org.acmsl.queryj.metadata.vo.Table;
 import org.acmsl.queryj.templates.BasePerRepositoryTemplate;
 import org.acmsl.queryj.templates.BasePerRepositoryTemplateFactory;
 import org.acmsl.queryj.templates.BasePerRepositoryTemplateGenerator;
 import org.acmsl.queryj.templates.dao.DataAccessManagerTemplateFactory;
import org.acmsl.queryj.templates.dao.DataAccessManagerTemplateGenerator;
 import org.acmsl.queryj.tools.QueryJBuildException;
 
 /*
  * Importing ACM S.L. Commons classes.
  */
 import org.acmsl.commons.logging.UniqueLogFactory;
 
 /*
  * Importing Apache Commons Logging classes.
  */
 import org.apache.commons.logging.LogFactory;
 
 /*
  * Importing Cucumber classes.
  */
 import cucumber.api.DataTable;
 import cucumber.api.java.en.Given;
 import cucumber.api.java.en.Then;
 import cucumber.api.java.en.When;
 
 /*
  * Importing Jetbrains annotations.
  */
 import org.jetbrains.annotations.NotNull;
 
 /*
  * Importing JUnit classes.
  */
 import org.junit.Assert;
 
 /*
  * Importing JDK classes.
  */
 import java.io.File;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Maps steps in Cucumber scenarios for testing per-repository templates.
  * @author <a href="mailto:chous@acm-sl.org">Jose San Leandro</a>
  * @since 2013/05/05
  */
 @SuppressWarnings("unused")
 public class PerRepositoryTemplatesTest
     extends AbstractTemplatesTest<BasePerRepositoryTemplateGenerator, BasePerRepositoryTemplateFactory>
 {
     /**
      * The repository.
      */
     private String m__strRepository;
 
     /**
      * The table names.
      */
     private List<String> m__lTableNames;
 
     /**
      * Creates an instance.
      */
     public PerRepositoryTemplatesTest()
     {
         immutableSetTableNames(new ArrayList<String>());
 
        GENERATOR_MAPPINGS.put("DataAccessManager", new DataAccessManagerTemplateGenerator(false, 1));
         FACTORY_MAPPINGS.put("DataAccessManager", DataAccessManagerTemplateFactory.getInstance());
     }
 
     /**
      * Specifies the repository name.
      * @param name such name.
      */
     protected final void immutableSetRepositoryName(@NotNull final String name)
     {
         this.m__strRepository = name;
     }
 
     /**
      * Specifies the repository name.
      * @param name such name.
      */
     protected void setRepositoryName(@NotNull final String name)
     {
         immutableSetRepositoryName(name);
     }
 
     /**
      * Retrieves the repository name.
      * @return such name.
      */
     @NotNull
     public String getRepositoryName()
     {
         return m__strRepository;
     }
 
     /**
      * Specifies the table names.
      * @param tableNames such names.
      */
     protected final void immutableSetTableNames(@NotNull final List<String> tableNames)
     {
         m__lTableNames = tableNames;
     }
 
     /**
      * Specifies the table names.
      * @param tableNames such names.
      */
     protected void setTableNames(@NotNull final List<String> tableNames)
     {
         immutableSetTableNames(tableNames);
     }
 
     /**
      * Retrieves the table names.
      * @return such names.
      */
     protected final List<String> immutableGetTableNames()
     {
         return m__lTableNames;
     }
 
     /**
      * Retrieves the table names.
      * @return such names.
      */
     public List<String> getTableNames()
     {
         return new ArrayList<String>(immutableGetTableNames());
     }
 
     /**
      * Retrieves the {@link DecoratorFactory} instance using given generator.
      * @param generator the generator to use.
      * @return the decorator factory.
      */
     @NotNull
     @Override
     protected DecoratorFactory retrieveDecoratorFactory(@NotNull final BasePerRepositoryTemplateGenerator generator)
     {
         return generator.getDecoratorFactory();
     }
 
     /**
      * Gathers the repository information from the scenario.
      * @param repositoryTable the repository information in tabular format.
      */
     @Given("^a repository with the following information:$")
     public void defineRepository(@NotNull final DataTable repositoryTable)
     {
         @NotNull final List<Map<String,String>> entries = repositoryTable.asMaps();
 
         Assert.assertTrue("Missing repository information", entries.size() > 0);
         Assert.assertTrue("Too many repositories defined", entries.size() == 1);
 
         Map<String, String> repositoryInfo = entries.get(0);
 
         Assert.assertNotNull(repositoryInfo);
 
         String name = repositoryInfo.get("name");
         Assert.assertNotNull("Missing repository name", name);
         setRepositoryName(name);
 
         String user = repositoryInfo.get("user");
         Assert.assertNotNull("Missing repository user", user);
 
         String vendor = repositoryInfo.get("vendor");
         Assert.assertNotNull("Missing repository vendor", vendor);
     }
 
     /**
      * Gathers information about the tables defined in the repository.
      * @param dataTable the tabular data about the repository tables.
      */
     @Given("^a repository with the following tables:$")
     public void defineTables(@NotNull final DataTable dataTable)
     {
         defineTables(dataTable.asMaps(), getTableNames());
     }
 
     /**
      * Gathers information about the tables defined in the repository.
      * @param entries the tabular data about the repository tables.
      * @param tableList the table collection.
      */
     protected void defineTables(@NotNull final List<Map<String, String>> entries, List<String> tableList)
     {
         Assert.assertTrue("Missing table information", entries.size() > 0);
 
         for (@NotNull Map<String, String> entry : entries)
         {
             String tableName = entry.get("table");
             Assert.assertNotNull("Missing table name", tableName);
             tableList.add(tableName);
         }
     }
 
     /**
      * Generates a file with the information from the feature.
      * @param template the template.
      * @param engine the engine.
      */
     @SuppressWarnings("unused")
     @When("^I generate with per-repository (.*)\\.stg for (.*)$")
     public void generateFile(@NotNull final String template, @NotNull final String engine)
     {
         generateFile(template, engine, getRepositoryName(), getTableNames(), getOutputFiles());
     }
 
     /**
      * Generates a file with the information from the feature.
      * @param templateName the template.
      * @param repository the repository.
      * @param tables the tables.
      * @param outputFiles the output files.
      */
     @SuppressWarnings("unchecked")
     protected void generateFile(
         @NotNull final String templateName,
         @NotNull final String repository,
         @NotNull final String engine,
         @NotNull final List<String> tables,
         @NotNull final Map<String, File> outputFiles)
     {
         BasePerRepositoryTemplateGenerator generator =
             retrieveTemplateGenerator(templateName);
 
         Assert.assertNotNull("No template generator found for " + templateName, generator);
 
         BasePerRepositoryTemplateFactory templateFactory = retrieveTemplateFactory(templateName);
 
         Assert.assertNotNull("No template factory found for " + templateName, templateFactory);
 
         BasePerRepositoryTemplate template =
             templateFactory.createTemplate(
                 retrieveMetadataManager(engine, tables, new ArrayList<Table>(0)),
                 retrieveCustomSqlProvider(),
                 retrieveDecoratorFactory(generator),
                 "com.foo.bar.dao",
                 "com.foo.bar",
                repository,
                 "", // header
                 false, // marker
                 false, // jmx
                 tables,
                 "java:comp/env/db",
                 false, // disable generation timestamps
                 false, // disable NotNull annotations
                 true); // disable checkThread.org annotations
 
         Assert.assertNotNull("No template found for " + templateName, template);
 
         File outputDir = null;
 
         try
         {
             rootFolder.create();
             outputDir = rootFolder.newFolder("dao");
         }
         catch (@NotNull final IOException ioException)
         {
             Assert.fail(ioException.getMessage());
         }
 
 //            Assert.assertTrue("Cannot create folder: " + outputDir.getAbsolutePath(), outputDir.mkdirs());
 
         UniqueLogFactory.initializeInstance(LogFactory.getLog(PerTableTemplatesTest.class));
 
         try
         {
             generator.write(
                 template,
                 outputDir,
                 rootFolder.getRoot(),
                 Charset.defaultCharset());
         }
         catch (@NotNull final IOException ioException)
         {
             Assert.fail(ioException.getMessage());
         }
         catch (@NotNull final QueryJBuildException queryjBuildException)
         {
             Assert.fail(queryjBuildException.getMessage());
         }
 
         outputFiles.put(
             repository,
             new File(outputDir, generator.retrieveTemplateFileName(template.getTemplateContext())));
     }
 
     /**
      * Checks the generated file compiles.
      * @param outputName the name of the output file.
      */
     @SuppressWarnings("unused")
     @Then("^the generated per-repository (.*) file compiles successfully")
     public void checkGeneratedFileCompiles(@NotNull final String outputName)
     {
         checkGeneratedFilesCompile(outputName, getOutputFiles());
     }
 }
