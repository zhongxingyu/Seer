 /*
  * Copyright (C) 2009
  *
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  *
  * The interactive user interfaces in modified source and object code versions
  * of this program must display Appropriate Legal Notices, as required under
  * Section 5 of the GNU Affero General Public License version 3.
  *
  * In accordance with Section 7(b) of the GNU Affero General Public License
  * version 3, these Appropriate Legal Notices must retain the display of the
  * "Derived from Travian world" logo. If the display of the logo is not
  * reasonably feasible for technical reasons, the Appropriate Legal Notices must
  * display the words "Derived from Travian world".
  */
 
 package com.funambol.lanciadelta;
 
 import bsh.Interpreter;
 import com.funambol.lanciadelta.rally.LanciaDeltaService;
 import com.rallydev.webservice.v1_10.domain.Workspace;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Properties;
 import org.apache.commons.lang.StringUtils;
 
 
 /**
  * This is the command line main class that creates, initializes and launches
  * the BeanShell interpreter.
  *
  * @author ste
  */
 public class LanciaDeltaShell
 implements Constants {
     // ------------------------------------------------------------ Private data
 
     private Interpreter interpreter;
 
     private LanciaDeltaService service;
 
     private Workspace workspace;
 
 
     // ------------------------------------------------------------ Constructors
 
     public LanciaDeltaShell() throws Exception {
         checkProperties();
 
         String rallyUrl = "https://"
                         + System.getProperty(PROPERTY_HOST)
                         + RALLY_URL
                         ;
 
         service = LanciaDeltaService.getInstance();
 
         workspace = new Workspace();
         workspace.setRef(rallyUrl + '/' + WORKSPACE + '/' + OID_FUNAMBOL_WORKSPACE);
         service.read(workspace);
 
         interpreter = new Interpreter( 
             new InputStreamReader(System.in),
             System.out,
             System.err,
             true
         );
         interpreter.set(RALLY_SERVICE_OBJECT, service);
         interpreter.set(RALLY_WORKSPACE_OBJECT, workspace);
         interpreter.setShowResults(true);
 
         interpreter.eval("importCommands(\"/ext\");");
         interpreter.eval("initialize();");
     }
     
     // ---------------------------------------------------------- Public methods
     public Interpreter getInterpreter() {
         return interpreter;
     }
 
     // -------------------------------------------------------------------- main
 
     public static void main(String[] args) throws Exception {
         LanciaDeltaShell shell = null;
 
         //
         // We read the project artifactid, groupid and version and set them
         // in the System.properties for further use
         //
         //readProjectProperties();
         
         try {
             shell = new LanciaDeltaShell();
         } catch (Exception e) {
             System.err.println(e.getMessage());
             System.exit(1);
         }
 
         while (true) {
             try {
                 shell.getInterpreter().run();
             } catch (Exception e) {
                 System.err.println("ERR: " + e.getMessage());
                 System.err.println(">>>");
                 e.printStackTrace();
                 System.err.println("<<<");
             }
         }
  
      }
 
     // --------------------------------------------------------- Private methods
 
     /**
      * Checks if the needed system properties has been defined
      * 
      * @throws java.lang.RuntimeException
      */
     private void checkProperties() throws RuntimeException {
         String value = System.getProperty(PROPERTY_USERNAME);
 
         if (StringUtils.isEmpty(value)) {
             throw new RuntimeException(getMissingPropertyMessage(PROPERTY_USERNAME));
         }
 
         value = System.getProperty(PROPERTY_PASSWORD);
         if (StringUtils.isEmpty(value)) {
             throw new RuntimeException(getMissingPropertyMessage(PROPERTY_PASSWORD));
         }
 
         value = System.getProperty(PROPERTY_HOST);
         if (StringUtils.isEmpty(value)) {
             System.setProperty(PROPERTY_HOST, RALLY_HOST_COMMUNITY);
         }
     }
 
     private String getMissingPropertyMessage(String p) {
         return "Missing " + p + ". Use -D" + p + "=<value>";
     }
 
     /**
      * Reads maven project properties from META-INF/com.funambol/lanciadelta/pom.properties
      * and set artifactid, groupid and version as system properties.
      */
     private static void readProjectProperties() {
         InputStream is =
             LanciaDeltaShell.class.getResourceAsStream("META-INF/com.funambol/lanciadelta/pom.properties");
 
         if (is != null) {
             Properties projectProperties = new Properties();
 
             try {
                 projectProperties.load(is);
             } catch (Exception e) {
                 System.err.println("Error reading properties file: " + e);
             }
 
            System.out.println("project properties: " + projectProperties);
             System.getProperties().putAll(projectProperties);
         }
     }
 
 }
