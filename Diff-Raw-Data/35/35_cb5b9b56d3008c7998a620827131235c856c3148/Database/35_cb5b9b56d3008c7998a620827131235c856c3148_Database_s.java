 package edu.rivfader.data;
 
 import edu.rivfader.errors.ColumnLoadError;
 import edu.rivfader.errors.ColumnWriteError;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.NoSuchElementException;
 
 import java.io.File;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.EOFException;
 
 /**
  * Class which encapsulates access to the database files.
  * @author harald
  */
 public class Database {
     /**
      * contains the suffix for all table variables.
      */
     private static final String TABLE_SUFFIX = ".table";
 
     /**
      * contains the name of the catalogue file.
      */
 
     private static final String CATALOGUE_FILE = "cataloge";
     /**
      * contains the base name of the database.
      */
 
     private final File baseFolder;
     /**
      * contains the table currently open for output, if outBuffer != null.
      */
 
     private String tableOpenForOutput;
 
     /**
      * contains a stream into the currently open output table.
      */
     private ObjectOutputStream outBuffer;
 
     /**
      * contains the name of the outbuffer file.
      */
     private File outBufferFile;
 
     public Database(final String databaseFolder) {
         baseFolder = new File(databaseFolder);
     }
 
     /**
      * computes the file name for a table.
      * @param tableName the table to compute the name for.
      * @return the name of the table file.
      */
     private File tableFilename(String tableName) {
         return new File(baseFolder, tableName+TABLE_SUFFIX);
     }
 
     /**
      * loads the table and returns an iterator over the rows.
      * @param tableName the name of the table to load
      * @return a row iterator.
      */
     public Iterator<Row> loadTable(final String tableName) throws IOException {
         ObjectInputStream input = new ObjectInputStream(
                                     new FileInputStream(
                                         tableFilename(tableName)));
         return new RowReaderIterator(input);
     }
 
     /**
      * stores the catalogue.
      * @param catalogue the catalogue to store.
      */
     private void storeCatalogue(Map<String, List<String>> catalogue)
         throws IOException{
         ObjectOutputStream output = new ObjectOutputStream(
                                         new FileOutputStream(
                                             new File(baseFolder,
                                                      CATALOGUE_FILE)));
         output.writeObject(catalogue);
         output.close();
     }
 
     /**
      * reads the catalogue.
      * @return the catalogue.
      */
     private Map<String, List<String>> readCatalogue()
         throws IOException {
         Map<String, List<String>> catalogue;
         ObjectInputStream input = new ObjectInputStream(
                                     new FileInputStream(
                                         new File(baseFolder,
                                                  CATALOGUE_FILE)));
         try {
             catalogue = (Map<String, List<String>>) input.readObject();
         } catch(ClassNotFoundException e) {
             throw new IOException("catalogue file borked", e);
         }
         input.close();
         return catalogue;
     }
 
     /**
      * creates a new table.
      * @param tableName the name of the table to create.
      * @param columnNames the names of the columns, the type is varchar.
      */
     public void createTable(final String tableName,
                             final List<String> columnNames)
         throws IOException {
         if(!baseFolder.exists()) {
             if(!baseFolder.mkdir()) {
                 throw new IOException("Cannot create database folder");
             }
             Map<String, List<String>> catalogue =
                 new HashMap<String, List<String>>();
             storeCatalogue(catalogue);
         }
 
         Map<String, List<String>> catalogue = readCatalogue();
         catalogue.put(tableName, columnNames);
         storeCatalogue(catalogue);
 
         File tableFile = tableFilename(tableName);
         tableFile.createNewFile();
         ObjectOutputStream output = new ObjectOutputStream(
                                         new FileOutputStream(tableFile));
         output.close();
     }
 
     /**
      * deletes a table if it exists.
      * @param tableName the name of the table to delete.
      */
     public void dropTable(final String tableName)
         throws IOException {
         File doomedFile = tableFilename(tableName);
         if(!doomedFile.exists()) {
             return;
         }
         if(!doomedFile.delete()) {
             throw new IOException("Table " + tableName
                                    + " could not be deleted");
         }
         Map<String, List<String>> catalogue = readCatalogue();
         catalogue.remove(tableName);
         storeCatalogue(catalogue);
     }
 
     /**
      * stores a row in a table.
      * @param tableName the name of the table to store to.
      * @param row the row to store.
      */
     public void storeRow(final String tableName, final Row row)
         throws IOException{
         if(outBuffer == null) {
             throw new IllegalStateException("Buffer not open");
         }
         if(!tableName.equals(tableOpenForOutput)) {
             throw new IllegalStateException("Writing to wrong table?");
         }
 
         outBuffer.writeObject(row);
     }
 
     /**
      * opens a table for writing.
      * @param tableName the table to open
      */
     public void openTableForWriting(final String tableName)
         throws IOException {
         if(outBuffer != null) {
             throw new IllegalStateException("Buffer already open");
         }
         tableOpenForOutput = tableName;
         outBufferFile = File.createTempFile("table", "out");
         outBuffer = new ObjectOutputStream(
                         new FileOutputStream(outBufferFile));
     }
 
     /**
      * closes the table.
      * @param tableName the table to close
      */
     public void closeTable(final String tableName)
         throws IOException {
         if (outBuffer != null) {
             outBuffer.close();
             ObjectInputStream input = new ObjectInputStream(
                             new FileInputStream(outBufferFile));
             ObjectOutputStream output = new ObjectOutputStream(
                             new FileOutputStream(tableFilename(tableName)));
             while(true) {
                 try {
                     output.writeObject(input.readObject());
                 } catch(ClassNotFoundException e) {
                     throw new IOException("temp file was borked", e);
                 } catch(EOFException e) {
                     break;
                 }
             }
             input.close();
             output.close();
         }
         outBuffer = null;
     }
 
     /**
      * appends a row to a table.
      * @param tableName the table to append to
      * @param row the row to append
      */
     public void appendRow(final String tableName, final Row row)
         throws IOException{
         ObjectOutputStream output = new AppendingObjectOutputStream(
                                         new FileOutputStream(
                                             tableFilename(tableName),
                                             true));
         output.writeObject(row);
         output.close();
     }
 
     /**
      * returns the list of column names of a table.
      * @param tableName the name of the table
      */
     public List<String> getColumnNames(final String tableName)
         throws IOException {
        ObjectInputStream input = new ObjectInputStream(
                        new FileInputStream(
                            new File(baseFolder, CATALOGUE_FILE)));
         Map<String, List<String>> catalogue = readCatalogue();
         return catalogue.get(tableName);
     }
 
     /**
      * Iterates over all rows contained in an ObjectInputStream.
      */
     private static class RowReaderIterator implements Iterator<Row> {
         /**
          * contains the input stream to iterate over.
          */
         private ObjectInputStream input;
 
         private Row nextRow;
         private boolean nextRowValid;
 
         /**
          * constructs a new RowReaderIterator.
          * @param pInput the input stream to read from
          */
         public RowReaderIterator(final ObjectInputStream pInput) {
             input = pInput;
         }
 
         @Override
         public boolean hasNext() {
             if(nextRowValid) {
                 return true;
             }
 
             try {
                 nextRow = (Row) input.readObject();
             } catch(ClassNotFoundException e) {
                 throw new ColumnLoadError("Table file corrupted");
             } catch(EOFException e) {
                 return false;
             } catch(IOException e) {
                 throw new ColumnLoadError(e);
             }
             nextRowValid = true;
             return true;
         }
 
         @Override
         public Row next() {
             if(hasNext()) {
                 nextRowValid = false;
                 return nextRow;
             } else {
                 throw new NoSuchElementException();
             }
         }
 
         @Override
         public void remove() {
             throw new UnsupportedOperationException();
         }
     }
 
     /**
      * Implements an object output stream that outputs no
      * header (which makes it possible to append to another object
      * output stream that way)
      * @author harald
      */
     private static class AppendingObjectOutputStream
             extends ObjectOutputStream {
         /**
          * constructs a new appending object output stream.
          * @param es a place to write to
          */
         public AppendingObjectOutputStream(final OutputStream os)
             throws IOException {
             super(os);
         }
 
         @Override
         protected void writeStreamHeader() {
             // nothing
         }
     }
 }
