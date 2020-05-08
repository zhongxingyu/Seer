 package mvhsbandinventory;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author jonathan
  */
 public class InstrumentFileStore extends InstrumentStore
 {
 
     private File directory;
     public static String horizontalSeparator = ",";
     public static String verticalSeparator = System.getProperty("line.separator");
 
     /**
      * Constructs a new InstrumentFileStore object for storing Instrument
     * information on the disk.
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
         String name = (String) instrument.get("name");
         String brand = (String) instrument.get("brand");
         String serial = (String) instrument.get("serial");
 
         return getFile(name, brand, serial);
     }
 
     /**
      * Serializes the data contained in the instrument into a CSV string that
      * can be written to a file.  The data in the instrument will be written
      * such that there will be one attribute of the instrument per column, with
      * the first row of each column containing the key name.  The second row
      * will contain the key value.  If the key value is an ArrayList (rather
      * than a string, the array list will be written to the string, one item per
      * row, starting at the second row.
      *
      * The attributes parameter allows you to choose a subset of the attributes
      * specified as valid in the Instrument.attributes static array.
      *
      * If the omitHeadings parameter is set to true, then there will be no
      * headings printed at the top of the CSV output.  This is useful if one is
      * serializing multiple Instrument objects into a large list.
      * @param instrument
      * @param attributes
      * @param omitHeadings
      * @return csv-serialized instrument string
      */
     public static String serialize(Instrument instrument, String[] attributes,
             boolean omitHeadings)
     {
         int height = 1;
         int width = attributes.length;
         int headingStart = 0;
         int dataStart = 0;
 
         // Determine if there (a) are any ArrayLists in the Instrument and (b)
         // what the size of the largest ArrayList in the Instrument is
         for (String key : attributes)
         {
             Object value = instrument.get(key);
 
             if (value instanceof ArrayList)
             {
                 ArrayList<String> list = (ArrayList<String>) value;
                 height = (list.size() > height) ? list.size() : height;
             }
         }
 
         // If we're adding headings to the CSV file, we'll increase the set
         // height by one to accomodate the header and change the dataStart
         // variable to one to allow the data to be below the header
         if (omitHeadings == false)
         {
             height += 1;
             dataStart = 1;
         }
 
         // Generate a two-dimensional array to be turned into our CSV file
         String[][] table = new String[width][height];
 
         // Insert our data into the two-dimensional array so that we have a
         // table that we will generate a CSV file out of
         for (int c = 0; c < width; c++)
         {
             String key = attributes[c];
             Object value = instrument.get(key);
 
             if (omitHeadings == false)
             {
                 table[c][headingStart] = key;
             }
 
             if (value instanceof ArrayList)
             {
                 ArrayList<String> list = (ArrayList<String>) value;
                 int length = list.size();
                 for (int r = dataStart; r <= length; r++)
                 {
                     table[c][r] = list.get(r - 1);
                 }
             } else
             {
                 table[c][dataStart] = (String) value;
             }
         }
 
         // Serialize this table into CSV format so that we can later write the
         // data to file
         String buffer = "";
 
         for (int r = 0; r < height; r++)
         {
             String rowBuffer = "";
 
             for (int c = 0; c < width; c++)
             {
                 String cell = table[r][c];
                 rowBuffer += (c != width - 1)
                         ? cell + horizontalSeparator : cell + verticalSeparator;
             }
         }
 
         return buffer;
     }
 
     /**
      * A convenience version of the InstrumentFileStore.serialize function that
      * exports every valid property specified in the Instrument.attributes
      * static array.  Note that with this function, headers will not be omitted.
      * @param instrument
      * @return csv-serialized instrument string
      */
     public static String serialize(Instrument instrument)
     {
         return serialize(instrument, Instrument.attributes, false);
     }
 
     /**
      * Parse the data serialized in CSV format (presumably with the
      * InstrumentFileStore.serialize method) into an Instrument object.  See the
      * documentation for the InstrumentFileStore.serialize method for more
      * details on the serialization format for the CSV file.
      *
      * Note that this function will only correctly unserialize data created with
      * the serialize function if the serialize function is set up to not omit
      * headings.
      * @param csv-serialized instrument
      * @return instrument object
      */
     public static Instrument unserialize(String csv)
     {
         // Create a two-dimensional array that will hold our CSV data
         String[][] table = null;
 
         // Start by splitting the data into separate rows
         List<String> rows = Arrays.asList(csv.split(verticalSeparator));
 
         // Now, we're going to further split the rows into individual cells and
         // determine the width of the table specified in the serialized data
         int height = rows.size();
         int width = 0;
 
         for (int r = 0; r < height; r++)
         {
             String row = rows.get(r);
             String[] cells = row.split(horizontalSeparator);
 
             if (width == 0)
             {
                 width = cells.length;
                 table = new String[width][height];
             }
 
             for (int c = 0; c < width; c++)
             {
                 table[c][r] = cells[c];
             }
         }
 
         // Create an Instrument object and fill in the data from the two-
         // dimensional array
         Instrument instrument = new Instrument();
 
         for (int c = 0; c < width; c++)
         {
             String attribute = table[c][0];
 
             // Infer the data type from the arrangement of data in the cells
            if (!table[c][2].equals(""))
             {
                 // There is content in the third row, meaning that there is an
                 // array of items; we need to extract an arraylist of items
                 ArrayList<String> value = new ArrayList<String>();
                 for (int r = 1; r < height; r++)
                 {
                     value.add(table[c][r]);
                 }
 
                 try
                 {
                     instrument.set(attribute, value);
                 } catch (Exception ex)
                 {
                     Logger.getLogger(InstrumentFileStore.class.getName()).log(Level.SEVERE, null, ex);
                 }
             } else
             {
                 // There's only one value for the field--there is just a string
                 // for this field
                 String value = table[c][1];
                 try
                 {
                     instrument.set(attribute, value);
                 } catch (Exception ex)
                 {
                     Logger.getLogger(InstrumentFileStore.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
 
         return instrument;
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
         try
         {
             // Serialize the file
             String csv = serialize(instrument);
             // Write the serialized instrument to the appropriate file on disk
             File file = getFile(instrument);
             Writer pointer = new BufferedWriter(new FileWriter(file));
             pointer.write(csv);
             pointer.close();
         } catch (IOException ex)
         {
             Logger.getLogger(InstrumentFileStore.class.getName()).log(Level.SEVERE, null, ex);
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
         String raw = "";
 
         try
         {
             reader = new BufferedReader(new FileReader(file));
 
             // Load all of the file into our buffer string
             while ((line = reader.readLine()) != null)
             {
                 raw += line;
             }
         } catch (FileNotFoundException ex)
         {
             Logger.getLogger(InstrumentFileStore.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex)
         {
             Logger.getLogger(InstrumentFileStore.class.getName()).log(Level.SEVERE, null, ex);
         } finally
         {
             try
             {
                 reader.close();
             } catch (IOException ex)
             {
                 Logger.getLogger(InstrumentFileStore.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         // Return the file after it has been parsed into an Instrument
         return unserialize(raw);
     }
 
     /**
      * Loads all of the instruments from the store.
      * @return an array of all of the parsed instruments in the store
      */
     public Instrument[] load()
     {
         // Get a list of all of the instruments in the store and count them
         File[] files = directory.listFiles();
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
