 package fedora.server.validation;
 
 /**
  * <p>Title: DOValidatorImpl.java </p>
  * <p>Description: . </p>
  * <p>Copyright: Copyright (c) 2002</p>
  * <p>Company: </p>
  * @author Sandy Payette, payette@cs.cornell.edu
  * @version 1.0
  */
 
 // Fedora imports
 import fedora.server.errors.ServerException;
 import fedora.server.errors.GeneralException;
 import fedora.server.errors.ObjectValidityException;
 
 // Java imports
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Hashtable;
 
 // SAX imports
 import org.xml.sax.SAXException;
 
 // TrAX classes
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 
 
 public class DOValidatorImpl implements DOValidator
 {
   /**
    * Configuration variables for the Digital Object Validator.
    * (These will ultimately be set via Module initialization, but this
    * class can be tested stand-alone via main(), in which cas the pre-set
    * values for the configuration variables will be used as defaults.)
    *
    */
     protected static String tempDir = "temp/";
     protected static String xmlSchemaURL = "http://www.cs.cornell.edu/payette/mellon/fedora/mets-fedora.xsd";
    protected static String xmlSchemaLocalPath = "c:/mellon-test/work/xsd/mets-fedora.xsd";
     protected static String schematronPreprocessorID = "schematron/preprocessor.xslt";
     protected static String schematronSchemaID = "schematron/fedoraRules.xml";
     protected static String schematronValidatingXslID = "schematron/fedoraValidator.xslt";
 
     // TEMPORARY: static variables to support testing via main()
     protected static boolean debug = true;
 
     public static void main(String[] args)
     {
       if (args.length < 2)
       {
         System.err.println("usage: java DOValidatorImpl objectLocation validationLevel workFlowPhase" + "\n" +
           "  objectLocation: the file path of the object to be validated" + "\n" +
           "  validationLevel: {0|1|2|3} where" + "\n" +
           "      0=all validation, 1=xmlSchema only, 2=schematron only, 3=referential integrity only" + "\n" +
           "  workFlowPhase: {ingest|store} the phase of the object lifecycle to which validation pertains");
         System.exit(1);
       }
 
       try
       {
         File objectAsFile = new File((String)args[0]);
         DOValidatorImpl dov = new DOValidatorImpl();
         dov.validate(new FileInputStream(objectAsFile), new Integer(args[1]).intValue(), args[2]);
       }
       catch (GeneralException e)
       {
         System.out.println("DOValidatorImpl caught GeneralException in Constructor.");
         System.out.println("Suppressing message since not attached to Server.");
       }
       catch (ObjectValidityException e)
       {
         System.out.println("DOValidatorImpl caught ObjectValidityException in Constructor.");
         System.out.println("Suppressing message since not attached to Server.");
       }
       catch (ServerException e)
       {
         System.out.println("DOValidatorImpl caught ServerException in Constructor.");
         System.out.println("Suppressing message since not attached to Server.");
       }
       catch (Exception e)
       {
         System.out.println("ERROR in DOValidatorImpl: " + e.getMessage());
       }
       System.exit(1);
     }
 
   /**
    * <p>Constructs a new DOValidatorImpl to support all forms of
    * digital object validation.</p>
    *
    * @throws ServerException If construction fails for any reason.
    */
     public DOValidatorImpl() throws ServerException
     {
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
       //throws ObjectValidityException, GeneralException
       throws ServerException
     {
       // ISSUE!!!: We need to use the object Inputstream twice, once for XML Schema validation
       // and once for Schematron validation.  We may want to consider implementing some form
       // of a rewindable InputStream. For now, I will just write the object InputStream to
       // disk so I can read it multiple times.
       try
       {
         System.out.println("TEMP FILE DIR:" + tempDir);
         File objectAsFile = streamtoFile(tempDir, objectAsStream);
         //validate(objectAsFile, "2", workFlowPhase);
         validate(objectAsFile, validationLevel, workFlowPhase);
       }
       catch (ServerException e)
       {
         System.out.println("DOValidatorImpl says caught ServerException. Re-throwing as ServerException: ");
         throw e;
       }
       catch (Exception e)
       {
         System.out.println("DOValidatorImpl says caught Exception. Re-throwing as GeneralException: " + e.getMessage());
         throw new GeneralException(e.getMessage());
       }
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
       //throws ObjectValidityException, GeneralException
       throws ServerException
     {
       switch ( validationLevel )
       {
       // Default: ALL forms of validation
       case 0:
         validateL1(objectAsFile, xmlSchemaLocalPath);
         validateL2(objectAsFile, schematronSchemaID, schematronPreprocessorID,
                    schematronValidatingXslID, workFlowPhase);
         validateL3(objectAsFile);
         break;
       // XML Schema Validation only
       case 1:
         validateL1(objectAsFile, xmlSchemaLocalPath);
         break;
       // Schematron Rules Validation only
       case 2:
         validateL2(objectAsFile, schematronSchemaID, schematronPreprocessorID,
                    schematronValidatingXslID, workFlowPhase);
         break;
       // Referential Integrity Checks only
       case 3:
         validateL3(objectAsFile);
         break;
       // Default: ALL forms of validation
       default:
         System.out.println("DOValidatorImpl says invalid validationLevel specified.");
         cleanUp(objectAsFile);
         throw new GeneralException("DOValidatorImpl.validate has invalid validationLevel: " + validationLevel);
       }
       cleanUp(objectAsFile);
     }
 
     private void streamCopy(InputStream in, OutputStream out)
       throws IOException
     {
       int bufferSize = 512;
       byte[] buffer= new byte[bufferSize];
       int bytesRead = 0;
       while ((bytesRead = in.read(buffer, 0, bufferSize)) != -1)
       {
               out.write(buffer,0,bytesRead);
       }
       in.close();
       out.close();
     }
 
     private File streamtoFile(String dirname, InputStream objectAsStream)
         throws IOException
     {
         File objectAsFile = null;
         try
         {
           File tempDir = new File(dirname);
           String fileLocation = null;
 
           if ( tempDir.exists() || tempDir.mkdirs() )
           {
               fileLocation = tempDir.toString() + File.separator + System.currentTimeMillis() + ".tmp";
               FileOutputStream fos = new FileOutputStream(fileLocation);
               streamCopy(objectAsStream, fos);
               objectAsFile = new File(fileLocation);
           }
         }
         catch (IOException e)
         {
           if (objectAsFile.exists())
           {
             objectAsFile.delete();
           }
           throw e;
         }
         return(objectAsFile);
     }
 
     private void validateL1(File objectAsFile, String xmlSchemaLocalPath)
       throws ServerException
     {
       // Level 1: METS rules validation using XML Schema
       System.out.println("Validating object to Level 1 (METS/XML SCHEMA) ...");
       try
       {
        //DOValidatorXMLSchema xsv = new DOValidatorXMLSchema(xmlSchemaLocalPath);
        DOValidatorXMLSchema xsv = new DOValidatorXMLSchema(xmlSchemaURL);
         xsv.validate(objectAsFile);
       }
       catch (SAXException e)
       {
         System.out.println("DOValidatorImpl says caught SAXException. Re-throwing as ObjectValidityException: " + e.getMessage());
         cleanUp(objectAsFile);
         throw new ObjectValidityException(e.getMessage());
       }
       catch (Exception e)
       {
         System.out.println("DOValidatorImpl says caught Exception. Re-throwing as GeneralException: " + e.getMessage());
         cleanUp(objectAsFile);
         throw new GeneralException(e.getMessage());
       }
       System.out.println("Finished Level 1 Validation (METS/XMLSchema).");
     }
 
     private void validateL2(File objectAsFile, String schemaID, String preprocessorID, String validatingXslID, String phase)
       throws ServerException
     {
       // Level 2: Fedora rules validation using Schematron
       System.out.println("Validating object to Level 2 (FEDORA/SCHEMATRON) ...");
       try
       {
         DOValidatorSchematron validator =
           new DOValidatorSchematron(schemaID, preprocessorID, validatingXslID, phase);
         validator.validate(objectAsFile);
       }
       catch (ServerException e)
       {
         System.out.println("DOValidatorImpl says caught ServerException. Re-throwing as ServerException: ");
         cleanUp(objectAsFile);
         throw e;
       }
       catch (Exception e)
       {
         System.out.println("DOValidatorImpl says caught Exception. Re-throwing as GeneralException: " + e.getMessage());
         cleanUp(objectAsFile);
         throw new GeneralException(e.getMessage());
       }
       System.out.println("Finished Level 2 Validation (FEDORA/SCHEMATRON).");
     }
 
     private void validateL3(File objectAsFile)
       throws ServerException
     {
       // Placeholder for future integrity checks.  Still working on
       // requirements here.
       return;
     }
 
     // FIXIT!!  This is a hack for now.  I want to distinguish temporary object files
     // from real object files that were passed in for validation.  This is a bit
     // ugly as it stands, but it should only blow away files in the temp directory.
     private void cleanUp(File f)
     {
       if (f.getParentFile() != null)
       {
         if (((new File(tempDir)).getAbsolutePath()).equalsIgnoreCase(((f.getParentFile()).getAbsolutePath())))
         {
           System.out.println("Deleting temporary file: " + f.getAbsolutePath());
           f.delete();
         }
       }
     }
 }
