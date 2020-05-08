 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: NavigationControllerServletOptions.java,v 1.5 2003-10-25 19:18:37 shahid.shah Exp $
  */
 
 package com.netspective.sparx.navigate;
 
 import java.util.Properties;
 import java.util.Map;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.io.StringWriter;
 import java.io.PrintWriter;
 import javax.servlet.ServletConfig;
 
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.PosixParser;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.discovery.tools.DiscoverClass;
 
 import com.netspective.commons.text.TextUtils;
 import com.netspective.commons.RuntimeEnvironmentFlags;
 import com.netspective.commons.xdm.XmlDataModelSchema;
 import com.netspective.sparx.ProjectComponent;
 
 public class NavigationControllerServletOptions
 {
     private static final Log log = LogFactory.getLog(NavigationControllerServlet.class);
     private static DiscoverClass discoverClass = new DiscoverClass();
 
     public static final String INITPARAMNAME_SERVLET_OPTIONS = "com.netspective.sparx.navigate.CONTROLLER_SERVLET_OPTIONS";
     public static final Class PROJECT_COMPONENT_CLASS = discoverClass.find(ProjectComponent.class, ProjectComponent.class.getName());
     public static final Class RUNTIME_ENVIRONMENT_FLAGS_CLASS = discoverClass.find(RuntimeEnvironmentFlags.class, RuntimeEnvironmentFlags.class.getName());
 
     public static final String DEFAULT_PROJECT_FILE_NAME = "/WEB-INF/sparx/project.xml";
     public static final String DEFAULT_EXEC_PROPS_FILE_NAME = "/WEB-INF/sparx/conf/execution.properties";
     public static final String DEFAULT_RUNTIME_FLAGS = "DEVELOPMENT|FRAMEWORK_DEVELOPMENT";
     public static final String DEFAULT_LOGOUT_REQ_PARAM = "_logout";
     public static final String DEFAULT_SPARX_RESOURCES_LOCATOR = "/resources/sparx,/sparx";
     public static final String DEFAULT_INIT_SUCCESS = "END_INIT";
     public static final String DEFAULT_DATA_SOURCE_ID = "jdbc/default";
 
     private CommandLineParser parser = new PosixParser();
     private Options servletOptions = new Options();
     private CommandLine commandLine;
 
     public NavigationControllerServletOptions(ServletConfig servletConfig)
     {
         initOptions();
         try
         {
             String optionsParamValue = servletConfig.getInitParameter(INITPARAMNAME_SERVLET_OPTIONS);
            log.debug("Using servlet init param " + INITPARAMNAME_SERVLET_OPTIONS + ":\n  " + optionsParamValue);
             commandLine = parser.parse(servletOptions, optionsParamValue != null ? TextUtils.split(optionsParamValue, " ", false) : new String[0]);
         }
         catch (ParseException pe)
         {
             log.error("Unable to parse servlet options using CLI", pe);
 
             printHelp();
             try
             {
                 commandLine = parser.parse(servletOptions, new String[0]);
             }
             catch (ParseException pe2)
             {
                 throw new RuntimeException("This should never happen!");
             }
         }
         log.debug(this);
     }
 
     public void printHelp()
     {
         HelpFormatter formatter = new HelpFormatter();
         formatter.defaultWidth = 120;
         formatter.printHelp(getClass().getName(), servletOptions);
     }
 
     public String getHelp()
     {
         StringWriter stringWriter = new StringWriter();
         PrintWriter printWriter = new PrintWriter(stringWriter);
         HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp(printWriter, 90, "com.netspective.sparx.navigate.CONTROLLER_SERVLET_OPTIONS Init Parameter", "Class: " + getClass().getName(), servletOptions, 3, 0, "");
         return stringWriter.getBuffer().toString();
     }
 
     public void initOptions()
     {
         servletOptions.addOption(OptionBuilder.withLongOpt("help")
                                               .withDescription("Show options")
                                               .create('?'));
 
         servletOptions.addOption(OptionBuilder.withLongOpt("project")
                                               .hasArg().withArgName("file")
                                               .withDescription("The project file to use. The default is " + DEFAULT_PROJECT_FILE_NAME)
                                               .create('p'));
 
         servletOptions.addOption(OptionBuilder.withLongOpt("project-component-class")
                                               .hasArg().withArgName("name")
                                               .withDescription("The name of the class used for the ProjectComponent instance. Default is " + PROJECT_COMPONENT_CLASS.getName() + ".")
                                               .create('P'));
 
         servletOptions.addOption(OptionBuilder.withLongOpt("runtime-environment")
                                               .hasArg().withArgName("flags")
                                               .withDescription("The runtime environment flags to use. The default is "+ DEFAULT_RUNTIME_FLAGS +".")
                                               .create('e'));
 
         servletOptions.addOption(OptionBuilder.withLongOpt("runtime-environment-class")
                                               .hasArg().withArgName("name")
                                               .withDescription("The class used for the RuntimeEnvironmentFlags instance. The default is " + RUNTIME_ENVIRONMENT_FLAGS_CLASS.getName() + ".")
                                               .create('E'));
 
         servletOptions.addOption(OptionBuilder.withLongOpt("navigation-tree")
                                               .hasArg().withArgName("name")
                                               .withDescription("The name of navigation tree (defined in <project>) to use for navigation. The default is specified in the project file as <navigation-tree default=\"yes\">.")
                                               .create('n'));
 
         servletOptions.addOption(OptionBuilder.withLongOpt("theme")
                                               .hasArg().withArgName("name")
                                               .withDescription("The name of theme (defined in <project>) to use for presentation. The default is specified in the project file as <theme default=\"yes\">.")
                                               .create('t'));
 
         servletOptions.addOption(OptionBuilder.withLongOpt("sparx-resource-locators")
                                               .hasArg().withArgName("locators")
                                               .withDescription("A set of comma-separated locators for finding Sparx web resources. Default is " + DEFAULT_SPARX_RESOURCES_LOCATOR)
                                               .create('s'));
 
         servletOptions.addOption(OptionBuilder.withLongOpt("login-manager")
                                               .hasArg().withArgName("name")
                                               .withDescription("The name of the login manager (defined in <project>) to use for security. There is no security by default.")
                                               .create('l'));
 
         servletOptions.addOption(OptionBuilder.withLongOpt("logout-request-param-name")
                                               .hasArg().withArgName("name")
                                               .withDescription("The name of the servlet request parameter that will be set if when a users wants to logout. The default is \""+ DEFAULT_LOGOUT_REQ_PARAM +"\".")
                                               .create('L'));
 
         servletOptions.addOption(OptionBuilder.withLongOpt("init-first-time-using-ant")
                                               .hasArg().withArgName("file:target")
                                               .withDescription("Initialize the servlet using an Ant build file and a given target the first time the servlet is initialized.")
                                               .create('I'));
 
         servletOptions.addOption(OptionBuilder.withLongOpt("init-using-ant")
                                               .hasArg().withArgName("file:target")
                                               .withDescription("Initialize the servlet using an Ant build file and a given target. This build file and target will always be executed each time the servlet is initialized (and if a init-first-time-using-ant option is provided, it will be run after that file:target too).")
                                               .create('i'));
 
         servletOptions.addOption(OptionBuilder.withLongOpt("init-success")
                                               .hasArg().withArgName("type")
                                               .withDescription("Determine when the initialization will be considered successful (to increment the init count). Options are END_INIT which means at the end of the Servlet init() method or FIRST_GET_POST which means at the end of the first successful GET/POST. Default is FIRST_GET_POST")
                                               .create('c'));
 
         servletOptions.addOption(OptionBuilder.withLongOpt("servlet-exec-properties")
                                               .hasArg().withArgName("file")
                                               .withDescription("The name of the file that stores the persistent servlet execution properties like initialization count. The default is " + DEFAULT_EXEC_PROPS_FILE_NAME + ".")
                                               .create('x'));
 
         servletOptions.addOption(OptionBuilder.withLongOpt("listener-class")
                                               .hasArg().withArgName("name")
                                               .withDescription("The name of a project lifecycle listener class to add to project after all other initialization has occurred. More than one may be provided.")
                                               .create('r'));
 
         servletOptions.addOption(OptionBuilder.withLongOpt("debug-options")
                                               .withDescription("Dump the option values to STDOUT.")
                                               .create('g'));
 
         servletOptions.addOption(OptionBuilder.withLongOpt("default-data-source")
                                               .withDescription("The identifier of the default data source.")
                                               .create('d'));
     }
 
     public boolean isHelpRequested()
     {
         return commandLine.hasOption("?");
     }
 
     public boolean isDebugOptionsRequested()
     {
         return commandLine.hasOption("g");
     }
 
     public String getLoginManagerName()
     {
         return commandLine.getOptionValue("l");
     }
 
     public String getLogoutActionReqParamName()
     {
         return commandLine.getOptionValue("L", DEFAULT_LOGOUT_REQ_PARAM);
     }
 
     public String getNavigationTreeName()
     {
         return commandLine.getOptionValue("n");
     }
 
     public String getProjectFileName()
     {
         return commandLine.getOptionValue("p", DEFAULT_PROJECT_FILE_NAME);
     }
 
     public String getProjectComponentClassName()
     {
         return commandLine.getOptionValue("P", PROJECT_COMPONENT_CLASS.getName());
     }
 
     public String getRuntimeEnvFlags()
     {
         return commandLine.getOptionValue("e", DEFAULT_RUNTIME_FLAGS);
     }
 
     public String getRuntimeEnvClassName()
     {
         return commandLine.getOptionValue("E", RUNTIME_ENVIRONMENT_FLAGS_CLASS.getName());
     }
 
     public String getThemeName()
     {
         return commandLine.getOptionValue("t");
     }
 
     public String getSparxResourceLocators()
     {
         return commandLine.getOptionValue("s", DEFAULT_SPARX_RESOURCES_LOCATOR);
     }
 
     public String getInitUsingAnt()
     {
         return commandLine.getOptionValue("i");
     }
 
     public String getInitFirstTimeUsingAnt()
     {
         return commandLine.getOptionValue("I");
     }
 
     public String getServletExecutionPropertiesFileName()
     {
         return commandLine.getOptionValue("x", DEFAULT_EXEC_PROPS_FILE_NAME);
     }
 
     public String getInitSuccessType()
     {
         return commandLine.getOptionValue("c", DEFAULT_INIT_SUCCESS);
     }
 
     public String[] getProjectLifecycleListenerClassNames()
     {
         return commandLine.getOptionValues('r');
     }
 
     public String getDefaultDataSourceId(String defaultDataSourceId)
     {
         return commandLine.getOptionValue('d', defaultDataSourceId);
     }
 
     public Properties setProperties(Properties properties, String propNamesPrefix, boolean setNulls)
     {
         XmlDataModelSchema schema = XmlDataModelSchema.getSchema(getClass());
         Map attributeAccessors = schema.getAttributeAccessors();
         Object[] attrNames = attributeAccessors.keySet().toArray();
         Arrays.sort(attrNames);
 
         Map propertyNames = schema.getPropertyNames();
         for(int an = 0; an < attrNames.length; an++)
         {
             String attrName = (String) attrNames[an];
             if(attrName.equals("name") || attrName.equals("class"))
                 continue;
 
             XmlDataModelSchema.PropertyNames propNames = (XmlDataModelSchema.PropertyNames) propertyNames.get(attrName);
             if(propNames != null && propNames.isPrimaryName(attrName))
             {
                 XmlDataModelSchema.AttributeAccessor accessor = (XmlDataModelSchema.AttributeAccessor) attributeAccessors.get(attrName);
                 if(accessor != null)
                 {
                     String propName = propNames.getPrimaryName();
                     if(propNamesPrefix != null)
                         propName = propNamesPrefix + '.' + propName;
                     try
                     {
                         Object propValue = accessor.get(null, this);
                         if(propValue != null)
                             properties.setProperty(propName, propValue.toString());
                         else if(setNulls)
                             properties.setProperty(propName, "NULL");
                     }
                     catch (Exception e)
                     {
                         log.error("Unable to set property " + propName, e);
                         properties.setProperty(propName, e.getMessage());
                     }
                 }
             }
         }
 
         return properties;
     }
 
     public String toString()
     {
         StringBuffer result = new StringBuffer();
         Properties props = setProperties(new Properties(), null, true);
         for(Iterator i = props.keySet().iterator(); i.hasNext(); )
         {
             String propName = (String) i.next();
             String propValue = props.getProperty(propName);
             result.append(propName + " = " + propValue + "\n");
         }
         return result.toString();
     }
 }
