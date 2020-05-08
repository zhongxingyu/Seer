 package mvhsbandinventory;
 
 import au.com.bytecode.opencsv.CSVParser;
 import au.com.bytecode.opencsv.CSVWriter;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  *
  * @author jonathan
  */
 public class InstrumentFileStore extends InstrumentStore
 {
     private File directory;
 
     /**
      * Constructs a new InstrumentFileStore object for storing Instrument
      * information on the disk.  The path argument needs to be a path for a
      * valid folder where all of the CSV files will reside.
      * @param path
      */
     public InstrumentFileStore(String path)
     {
         directory = new File(path);
     }
 
     /**
      * Generates a File object that should reference the instrument based on
      * the specified information about that instrument.  The specified
      * information passed in via arguments must be the name, brand, and serial
      * number of the instrument.
      * @param name
      * @param brand
      * @param serial
      * @return file that should reference the specified instrument
      */
     private File getFile(String name, String brand, String serial)
     {
         String fileName = name + "_" + brand + "_" + serial + ".csv";
         return new File(directory, fileName);
     }
 
     /**
      * Generates a File object that should reference the specified instrument
      * in the data store.
      * @param instrument
      * @return file that should reference the specified instrument
      */
     private File getFile(Instrument instrument)
     {
         String name = (String) instrument.get("Name");
         String brand = (String) instrument.get("Brand");
         String serial = (String) instrument.get("Serial");
 
         return getFile(name, brand, serial);
     }
 
     public static List<String[]> prepare(Instrument instrument, 
                                          String[] attributes,
                                          boolean includeHistory)
     {
         List<String[]> rows = new ArrayList<String[]>();
 
         // Prepare all of the attributes for storage in the CSV file
         for (String attribute : attributes)
         {
             String value = instrument.get(attribute);
             String[] row = new String[2];
 
             row[0] = attribute;
             row[1] = value;
 
             rows.add(row);
         }
 
         // If requested, prepare all of the history items for storage in the
         // CSV file; we're adding "History" as a heading before that item
         if (includeHistory)
         {
             List<String> history = instrument.getHistory();
             history.add(0, "History");
             String[] row = (String[]) history.toArray();
 
             rows.add(row);
         }
 
         return rows;
     }
 
     public static List<String[]> prepare(Instrument instrument)
     {
         return prepare(instrument, Instrument.attributes, true);
     }
 
     public static Instrument parse(List<String> rows)
     {
         CSVParser parser = new CSVParser();
         Instrument instrument = new Instrument();
 
         try
         {
             for (String row : rows)
             {
                 String[] cells = parser.parseLine(row);
                 String attribute = cells[0];
 
                 if ("History".equals(attribute))
                 {
                     List<String> history = Arrays.asList(cells);
                     history.remove(0);
                     instrument.setHistory(history);
                 }
                 else
                 {
                     String value = cells[1];
                     instrument.set(attribute, value);
                 }
             }
         }
         catch (Exception e) {}
         finally
         {
             return instrument;
         }
     }
 
     /**
      * Determines whether the specified instrument is already stored in the
      * data store.
      * @param instrument
      * @return a boolean value determining whether the instrument is in the
      * data store; true indicates that the instrument is in the store and false
      * indicates that it doesn't exist in the data store
      */
     public boolean exists(Instrument instrument)
     {
         return getFile(instrument).exists();
     }
 
     /**
      * Adds a new instrument to the store; serializes all of the data and
      * writes the data to the disk.
      * @param instrument
      */
     public void add(Instrument instrument)
     {
         // With other store types, we might actually need to do something
         // different to create a new instrument item; for here, however, we
         // don't need anything extra
         update(instrument);
     }
 
     /**
      * Updates an existing instrument in the store; serializes all of the data
      * in the instrument parameter and writes the data to the disk.
      * @param instrument
      */
     public void update(Instrument instrument)
     {
         CSVWriter writer = null;
         
         try
         {
             // Prepare the data in the Instrument object for being written to
             // the disk drive
             List<String[]> table = prepare(instrument);
 
             // Use the opencsv library to write the data to the disk drive
             File file = getFile(instrument);
             writer = new CSVWriter(new FileWriter(file));
             writer.writeAll(table);
         } 
         catch (IOException ex) {}
        finally {
             // Make sure that the IO actually gets closed so that we don't have
             // any random file locks floating around
             try
             {
                 writer.close();
            } catch (IOException ex) {}
         }
     }
 
     /**
      * Deletes the instrument from the store permenantly.
      * @param instrument
      */
     public void delete(Instrument instrument) throws Exception
     {
         if (!exists(instrument))
         {
             throw new Exception("The instrument that was flagged for deletion "
                     + "does not yet exist.");
         }
 
         File file = getFile(instrument);
         file.delete();
     }
 
     /**
      * Read the instrument from the disk based on the specific information
      * passed in about it.  The specific information passed in via arguments
      * must be strings containing the name, brand, and serial of the instrument
      * to load from the disk.
      * @param name
      * @param brand
      * @param serial
      * @return a parsed instrument from the data store
      */
     public Instrument read(String name, String brand, String serial)
     {
         // Determine and read the appropriate file from the disk based on the
         // information about the instrument to read that was passed in
         File file = getFile(name, brand, serial);
         return read(file);
     }
 
     /**
      * Read and parse the instrument data contained in the file object passed
      * in via an argument.  A parsed instrument object is returned back.
      *
      * Note that this is a datastore-type-dependent method.  Do not use this
      * externally unless it is absolutely necessary to do so.  Use of this
      * method overload will make it difficult to switch to a different datastore
      * type later on.  Please use the method overload with the name, brand, and
      * serial arguments.
      * @param file
      * @return a parsed instrument from the data store
      */
     public Instrument read(File file)
     {
         BufferedReader reader = null;
         String line = "";
         ArrayList<String> lines = new ArrayList<String>();
 
         try
         {
             reader = new BufferedReader(new FileReader(file));
 
             // Load all of the file into our buffer string
             while ((line = reader.readLine()) != null)
             {
                 if (!line.equals("") && line != null) {
                     lines.add(line);
                 }
             }
         }
         catch (FileNotFoundException ex) {}
         catch (IOException ex) {}
         finally
         {
             // Make sure that the file reader is closed down properly
             try
             {
                 reader.close();
             } catch (IOException ex) {}
         }
 
         // Return the file after it has been parsed into an Instrument
         return parse(lines);
     }
 
     /**
      * Loads all of the instruments from the store.
      * @return an array of all of the parsed instruments in the store
      */
     public Instrument[] load() throws Exception
     {
         // Get a list of all of the instruments in the store
         File[] files = directory.listFiles();
         
         // If there are no files in the directory, don't try to create an array
         // of instruments of zero length
         if (files == null)
         {
             throw new FileNotFoundException("The directory location for the " +
                     "storage of instrument files is not valid.");
         }
 
         int size = files.length;
 
         // Create an array of the instruments with a size equal to the number
         // of instruments in the store
         Instrument[] instruments = new Instrument[size];
 
         // Read and unserialize all of the files into our instruments array
         for (int i = 0; i < size; i++)
         {
             instruments[i] = read(files[i]);
         }
 
         return instruments;
     }
 }
