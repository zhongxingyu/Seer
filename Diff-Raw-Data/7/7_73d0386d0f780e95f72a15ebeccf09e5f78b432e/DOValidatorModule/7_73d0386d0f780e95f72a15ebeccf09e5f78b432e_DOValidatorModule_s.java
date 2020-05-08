 package fedora.server.validation;
 
 // Fedora imports
 import fedora.server.errors.ServerException;
 import fedora.server.errors.GeneralException;
 import fedora.server.errors.ObjectValidityException;
 import fedora.server.errors.ModuleInitializationException;
 import fedora.server.errors.InitializationException;
 import fedora.server.Module;
 import fedora.server.Server;
 import fedora.server.storage.ConnectionPool;
 import fedora.server.storage.ConnectionPoolManager;
 
 // Java imports
 import java.util.Map;
 import java.io.InputStream;
 import java.io.File;
 
 /**
  *
  * <p><b>Title:</b> DOValidatorModule.java</p>
  * <p><b>Description:</b> Module Wrapper for DOValidatorImpl.java.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * <p><b>License and Copyright: </b>The contents of this file are subject to the
  * Mozilla Public License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License
  * at <a href="http://www.mozilla.org/MPL">http://www.mozilla.org/MPL/.</a></p>
  *
  * <p>Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  * the specific language governing rights and limitations under the License.</p>
  *
  * <p>The entire file consists of original code.  Copyright &copy; 2002, 2003 by The
  * Rector and Visitors of the University of Virginia and Cornell University.
  * All rights reserved.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * @author payette@cs.cornell.edu
  * @version $Id$
  */
 public class DOValidatorModule extends Module implements DOValidator
 {
 
   /**
    * An instance of the core implementation class for DOValidator.
    * The DOValidatorModule acts as a wrapper to this class.
    */
   private DOValidatorImpl dov = null;
 
   /**
    * <p>Constructs a new DOValidatorModule</p>
    *
    * @param moduleParameters The name/value pair map of module parameters.
    * @param server The server instance.
    * @param role The module role name.
    * @throws ModuleInitializationException If initialization values are
    *         invalid or initialization fails for some other reason.
    */
     public DOValidatorModule(Map moduleParameters, Server server, String role)
           throws ModuleInitializationException, ServerException
     {
         super(moduleParameters, server, role);
     }
 
   public void postInitModule() throws ModuleInitializationException
   {
     try
     {
       String tempDir = new File(getServer().getHomeDir(),
           this.getParameter("tempDir")).getPath();
       String xmlSchemaURL = this.getParameter("xmlSchemaURL");
       String schematronPreprocessorID = new File(getServer().getHomeDir(),
           this.getParameter("schematronPreprocessor")).getPath();
       String schematronSchemaID = new File(getServer().getHomeDir(),
           this.getParameter("schematronSchema")).getPath();
       ConnectionPool connectionPool=((ConnectionPoolManager)
           getServer().getModule(
           "fedora.server.storage.ConnectionPoolManager")).getPool();
       logFiner("[DOValidatorModule] tempDir set to: "
                 + tempDir);
       logFiner("[DOValidatorModule] xmlSchemaURL set to: "
                 + xmlSchemaURL);
       logFiner("[DOValidatorModule] schematronPreprocessorID set to: "
                 + schematronPreprocessorID);
       logFiner("[DOValidatorModule] schematronSchemaID set to: "
                 + schematronSchemaID);
       // instantiate the validation implementation class
       dov = new DOValidatorImpl(tempDir, xmlSchemaURL, schematronPreprocessorID,
             schematronSchemaID, connectionPool);
     }
     catch(Exception e)
     {
       System.out.println("Unable to postInitialize validation module: " + e.getMessage());
       throw new ModuleInitializationException(
           e.getMessage(),"fedora.server.validation.DOValidatorModule");
     }
   }
 
   /**
    * <p>Validates a digital object.</p>
    *
   * @param in The digital object provided as a bytestream.
    * @param validationLevel The level of validation to perform on the digital object.
    *        This is an integer from 0-3 with the following meanings:
    *        0 = do all validation levels
    *        1 = perform only XML Schema validation
    *        2 = perform only Schematron Rules validation
    *        3 = perform only referential integrity checks for the object
    * @param workFlowPhase The stage in the work flow for which the validation should be contextualized.
    *        "ingest" = the object is in the submission format for the ingest stage phase
    *        "store" = the object is in the authoritative format for the final storage phase
    * @throws ServerException If validation fails for any reason.
    */
   public void validate(InputStream objectAsStream, int validationLevel, String workFlowPhase)
     throws ServerException
   {
     dov.validate(objectAsStream, validationLevel, workFlowPhase);
     logFiner("[DOValidatorModule] Completed object validation at level: "
               + validationLevel);
   }
 
   /**
    * <p>Validates a digital object.</p>
    *
   * @param in The digital object provided as a file.
    * @param validationLevel The level of validation to perform on the digital object.
    *        This is an integer from 0-3 with the following meanings:
    *        0 = do all validation levels
    *        1 = perform only XML Schema validation
    *        2 = perform only Schematron Rules validation
    *        3 = perform only referential integrity checks for the object
    * @param workFlowPhase The stage in the work flow for which the validation should be contextualized.
    *        "ingest" = the object is in the submission format for the ingest stage phase
    *        "store" = the object is in the authoritative format for the final storage phase
    * @throws ServerException If validation fails for any reason.
    */
   public void validate(File objectAsFile, int validationLevel, String workFlowPhase)
     throws ServerException
   {
       dov.validate(objectAsFile, validationLevel, workFlowPhase);
       logFiner("[DOValidatorModule] Completed object validation at level: "
               + validationLevel);
   }
 }
