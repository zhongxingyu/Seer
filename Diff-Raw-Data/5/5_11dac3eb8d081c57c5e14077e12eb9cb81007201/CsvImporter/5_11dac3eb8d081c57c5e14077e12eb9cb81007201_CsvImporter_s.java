 package org.motechproject.ananya.referencedata.csv;
 
 
 import org.apache.commons.io.FileUtils;
 import org.motechproject.ananya.referencedata.csv.exception.FileReadException;
 import org.motechproject.ananya.referencedata.csv.exception.InvalidArgumentException;
 import org.motechproject.ananya.referencedata.csv.exception.WrongNumberArgsException;
 import org.motechproject.importer.CSVDataImporter;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.stereotype.Component;
 import org.springframework.web.multipart.commons.CommonsMultipartFile;
 
 import java.io.File;
 import java.io.IOException;
 
 @Component
 public class CsvImporter {
 
     private static final String APPLICATION_CONTEXT_XML = "applicationContext-csv-importer.xml";
 
     public byte[] importLocation(CommonsMultipartFile file) throws IOException {
         File tempLocationCsvFile = createTempLocationCsvFile(file);
         importFile(tempLocationCsvFile.getPath(), ImportType.Location);
         File errorFile = getErrorFile();
         byte[] errors = readError(errorFile);
         clearTempFiles(tempLocationCsvFile, errorFile);
         return errors;
     }
 
     public static void main(String args[]) throws Exception {
         try {
             validateArguments(args);
             String entityType = args[0];
             String filePath = args[1];
             ImportType importType = validateAndSetImportType(entityType);
             validateImportFile(filePath);
 
             importFile(filePath, importType);
         } catch (Exception exception) {
             throw exception;
         }
     }
 
 
     private void clearTempFiles(File locationCsv, File errorFile) {
         FileUtils.deleteQuietly(locationCsv);
         FileUtils.deleteQuietly(errorFile);
     }
 
     private File createTempLocationCsvFile(CommonsMultipartFile file) throws IOException {
        String tempLocationCsvFilePath = FileUtils.getTempDirectoryPath() + "location.csv";
         File locationCsv = new File(tempLocationCsvFilePath);
         FileUtils.writeByteArrayToFile(locationCsv, file.getBytes());
         return locationCsv;
     }
 
     private byte[] readError(File errorFile) {
         try {
             return FileUtils.readFileToByteArray(errorFile);
         } catch (IOException e) {
             return new byte[0];
         }
     }
 
     private File getErrorFile() throws IOException {
        String errorCsvFilePath = FileUtils.getTempDirectoryPath() + "errors.csv";
         File errorsCsv = new File(errorCsvFilePath);
         return errorsCsv;
     }
 
     private static void importFile(String filePath, ImportType importType) {
         ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(APPLICATION_CONTEXT_XML);
         CSVDataImporter csvDataImporter = (CSVDataImporter) context.getBean("csvDataImporter");
         importType.performAction(filePath, csvDataImporter);
     }
 
     private static void validateArguments(String[] args) throws WrongNumberArgsException {
         if (args.length != 2)
             throw new WrongNumberArgsException("Wrong number of arguments. Arguments expected in order : <entity_type> <file_name>");
     }
 
     private static ImportType validateAndSetImportType(String entity) throws Exception {
         if (ImportType.isInValid(entity))
             throw new InvalidArgumentException("Invalid entity. Valid entities are : FrontLineWorker, Location");
         return ImportType.findFor(entity);
     }
 
     private static void validateImportFile(String importFile) throws FileReadException {
         if (!new File(importFile).canRead()) {
             throw new FileReadException("Cannot read import file " + importFile);
         }
     }
 }
 
