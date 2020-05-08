 /* Copyright (c) 2009 Stanford University
  *
  * Permission to use, copy, modify, and distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 
 package org.fiz;
 import java.io.*;
 import java.util.*;
 
 /**
  * Dataset defines general-purpose mechanisms for accessing hierarchical
  * information stored in key-value form.  Datasets are used to hold a
  * variety of data in Fiz, such as query values from URLs, configuration
  * options, and data coming from data managers.  A dataset has the following
  * general properties:
  *   - A dataset consists of key-value pairs
  *   - Keys are arbitrary strings (but it's usually convenient to limit
  *     them to standard identifier characters)
  *   - Values can have two forms:
  *     - An object, such as a string, integer, or another dataset
  *     - A list of one or more objects
  *   - Datasets can be created from a variety of sources, such as
  *     XML and YAML files (see classes such as XmlDataset and
  *     YamlDataset for details).
  *   - Datasets know how to convert between types. For example, if a string
  *     is requested but there is an integer with the same key, the integer is
  *     converted into a string before being returned. See the {@code Convert}
  *     class for a full list of supported conversions.
  *   - Values in a dataset can be refered to by a key or a path. A key refers
  *     to a value in the top level of a dataset. A path refers to a value
  *     in a nested dataset and consists of keys separated by dots.  For
  *     example, the path "a.b.c" refers to the value c inside a dataset
  *     with key b inside a dataset with key a.  Many methods take either
  *     a key or a path, with the key interpretation taking preference:
  *     (e.g., if keyOrPath is "a.b.c" and there is an element
  *     in the top-level dataset whose name is "a.b.c" then that element
  *     will be used in preference to a value in a nested dataset).
  */
 
 public class Dataset implements Cloneable, Serializable {
     /**
      * For methods such as lookup and newFileInstanceFromPath where
      * there may be more than one matching result, this enum indicates
      * how many of them should be returned.
      */
     public enum Quantity {
         /**
          * Return only the first matching result.
          */
         FIRST_ONLY,
 
         /**
          * Return all matching results.
          */
         ALL
     }
 
     /**
      * Used internally to represent lists of values. This is just used as an
      * ArrayList with a different name, so we can differentiate between lists
      * of values and values that are ArrayLists.
      */
     protected static class DSArrayList<E> extends ArrayList<E> {
         protected DSArrayList() {
             super();
         }
 
         protected DSArrayList(int size) {
             super(size);
         }
 
         protected DSArrayList(Collection c) {
             super(c);
         }
     }
 
     /**
      * InvalidConversionError is thrown when methods such as {@code get}
      * cannot convert an object to the requested type (e.g., integer to
      * dataset).
      */
     public static class InvalidConversionError extends Error {
         /**
          * Construct a InvalidConversionError with a message describing the
          * conversion that failed.
          * @param value       Object we attempted to convert
          * @param className   Name of the class we tried to convert to
          */
         public InvalidConversionError(Object value, String className) {
             super("couldn't convert object of class \"" +
                   value.getClass().getName() + "\" and value \"" +
                   value.toString() + "\" to class \"" + className + "\"");
         }
 
     }
 
     /**
      * MissingValueError is thrown when methods such as {@code get}
      * cannot find the requested key in the dataset.
      */
     public static class MissingValueError extends Error {
         // Name of the dataset element that could be found:
         protected String missingKey;
 
         /**
          * Construct a MissingValueError with a message describing the
          * key that was not found.
          * @param missingKey       Name of the key that was not found;
          *                         used to generate a message in the
          *                         exception
          */
         public MissingValueError(String missingKey) {
             super("couldn't find dataset element \"" + missingKey + "\"");
             this.missingKey = missingKey;
         }
 
         /**
          * This method can be used to retrieve the name of the
          * dataset element that caused the exception.
          * @return                 Key for the dataset value that wasn't
          *                         found.
          */
         public String getMissingKey() {
             return missingKey;
         }
     }
 
     /**
      * SyntaxError is thrown when the input used to create a Dataset
      * is malformed in some way that makes the information useless.
      */
     public static class SyntaxError extends Error {
         /**
          * Construct a SyntaxError with a message that includes the name of
          * the file in which the error occurred.
          * @param fileName         If the dataset input came from a file,
          *                         this gives the file name; null means
          *                         the input didn't come from a file
          * @param message          Detailed information about the problem
          *                         (null if no details are available)
          */
         public SyntaxError(String fileName, String message) {
             super("syntax error in dataset"
                     + ((fileName != null) ? (" (file \"" + fileName + "\")")
                     : "")
                     + ((message != null) ? (": " + message) : ""));
         }
     }
 
     /**
      * UnsupportedFormat is thrown when newFileInstance is passed a
      * file name with an unrecognized extension.
      */
     public static class UnsupportedFormatError extends Error {
         /**
          * Construct an UnsupportedFormatError with a message that identifies
          * the problem file.
          * @param fileName         Name of the file whose extension was
          *                         not recognized
          */
         public UnsupportedFormatError(String fileName) {
             super("couldn't recognize format of dataset file \""
                     + fileName + "\"");
         }
     }
 
     /**
      * UnknownFileFormatError is thrown when {@code writeFile} on a
      * Dataset for which there is no on-disk representation defined
      * (such as this class).
      */
     public static class UnknownFileFormatError extends Error {
         /**
          * Construct a UnknownFileFormatError.
          * @param message          Message to use for the exception
          */
         public UnknownFileFormatError(String message) {
             super(message);
         }
     }
 
     // The following class is used to return information from the
     // lookupParent method.
     protected class ParentInfo {
         public Dataset parent;     // Dataset corresponding to the portion
                                    // of the path up to its final element,
                                    // or null if there is no such HashMap.
         public String lastName;    // The final element in the path.
 
         public ParentInfo(Dataset parent, String lastName) {
             this.parent = parent;
             this.lastName = lastName;
         }
     }
 
     // The following field holds the contents of the dataset.  Keys
     // are strings, and values can have any of the following types:
     // DSArrayList:                List of values
     // Dataset:                    A nested dataset
     // Object:                     Any other object
     // It would be better if this field could be declared as
     // HashMap<String,Object>; unfortunately that won't work, because
     // we use JYaml to read YAML datasets directly into the map and JYaml
     // declares its HashMaps without the <String,Object>.
     protected HashMap map;
 
     // If this dataset originated in a file the following variable contains
     // the name of the file; otherwise it is null.  Used for error messages.
     protected String fileName;
 
     // Used to simulate I/O errors during testing.
     protected boolean generateIoException = false;
 
     // If set to true, the dataset is sorted before being output in various
     // formats.  Used in testing to ensure reproducible results.
     protected static boolean sortOutput = false;
 
     /**
      * Set to true if the last check (or any of its variants) call was
      * successfull, false otherwise.
      */
     public boolean found;
 
     // Passed to Convert methods
     protected Convert.Success convertSuccess = new Convert.Success();
 
     /**
      * Construct an empty dataset.
      */
     public Dataset() {
         map = new HashMap();
     }
 
     /**
      * Construct a dataset from keys and values passed as arguments,
      * where each value can be any valid dataset value.
      * @param keysAndValues        An even number of argument objects;
      *                             the first argument of each pair must be
      *                             a string key and the second argument of
      *                             each pair must be a value for that key.
      *                             To create multiple nested values with the
      *                             same name, use multiple key/value pairs
      *                             with the same key.
      */
     @SuppressWarnings("unchecked")
     public Dataset(Object... keysAndValues) {
         map = new HashMap();
         int last = keysAndValues.length - 2;
         for (int i = 0; i <= last; i += 2) {
             String key = (String) keysAndValues[i];
             Object value = keysAndValues[i+1];
             add(key, value);
         }
     }
 
     /**
      * Private constructor, used by checkValue, newFileInstance, and other
      * methods.
      * @param contents             HashMap holding the contents of the
      *                             dataset.
      * @param fileName             If the dataset was read from a file this
      *                             gives the file name (if known); otherwise
      *                             this is null.
      */
     protected Dataset(HashMap contents, String fileName) {
         map = contents;
         this.fileName = fileName;
     }
 
     /**
      * Creates a Dataset from information in a file.
      * @param fileName             Name of a dataset file in a supported
      *                             format; the format is inferred from the
      *                             file's extension (e.g. ".yaml" means the
      *                             file is in YAML format).  If the name
      *                             doesn't contain an extension, this method
      *                             tries all supported extensions until it
      *                             finds a dataset file that exists.
      * @return                     New Dataset object containing contents
      *                             of {@code fileName}
      */
     public static Dataset newFileInstance(String fileName) {
         String extension = StringUtil.fileExtension(fileName);
         if (extension == null) {
             String newName = Util.findFileWithExtension(fileName,
                     ".yaml", ".yml", ".xml");
             if (newName == null) {
                 throw new FileNotFoundError(fileName, "dataset",
                         "couldn't find a file with a supported extension");
             }
             fileName = newName;
             extension = StringUtil.fileExtension(fileName);
         }
         if (extension.equals(".yml") || extension.equals(".yaml")) {
             return YamlDataset.newFileInstance(fileName);
         }
         if (extension.equals(".xml")) {
             return XmlDataset.newFileInstance(fileName);
         }
         throw new UnsupportedFormatError(fileName);
     }
 
     /**
      * Creates a Dataset from information in one or more files found by
      * searching a collection of directories.
      * @param name                 Name of the desired dataset file.  If
      *                             the name has no extension, this method
      *                             tries all of the supported extensions in
      *                             order and stops with the first file that
      *                             exists.  The search for an extension
      *                             happens separately for each directory in
      *                             {@code path} (a different extension
      *                             may be chosen for each directory).
      * @param path                 Directories to search for the dataset.
      * @param quantity             If {@code name} exists in more than
      *                             one directory in {@code path}, this
      *                             parameter indicates whether to return the
      *                             first dataset found or a CompoundDataset
      *                             containing all of them.
      * @return                     A new Dataset object.
      */
     public static Dataset newFileInstanceFromPath(String name,
             String[] path, Quantity quantity) {
         ArrayList<Dataset> datasets = null;
         for (String directory : path) {
             String fullName = directory + "/" + name;
             try {
                 Dataset current = Dataset.newFileInstance(fullName);
                 if (quantity == Quantity.FIRST_ONLY) {
                     return current;
                 }
 
                 // Build up a list of all the datasets that were found.
                 if (datasets == null) {
                     datasets = new ArrayList<Dataset>(path.length);
                 }
                 datasets.add(current);
             }
             catch (FileNotFoundError e) {
                 // There isn't a relevant dataset in this directory;
                 // skip it and go on to the next directory.
                 continue;
             }
         }
         if (datasets == null) {
             throw FileNotFoundError.newPathInstance(name, "dataset", path);
         }
         if (datasets.size() == 1) {
             return datasets.get(0);
         }
         return new CompoundDataset(datasets);
     }
 
     /**
      * Create a new dataset from a serialized dataset of the sort generated
      * by {@code serialize}.
      * @param source               Contains a serialized dataset starting at
      *                             the first character.  If the serialized
      *                             dataset ends before the end of
      *                             {@code source}, the information after
      *                             the serialized data set is ignored.
      * @return                     A dataset whose contents match the
      *                             serialized dataset in {@code source}.
      */
     public static Dataset newSerializedInstance(CharSequence source) {
         Dataset d = new Dataset();
         d.addSerializedData(source, 0);
         return d;
     }
 
     /**
      * Adds a value in the top level of the dataset with the given {@code key}.
      * If there already exist one or more top-level values by the same name then
      * the new value is added to them to form a list, with the new value at the
      * end of the list.
      *
      * @param key        Name of a value in the top-level of the dataset.
      * @param value      New value to associate with the key.
      */
     public void add(String key, Object value) {
         Object old = map.get(key);
         Object appended = appendValue(old, value);
         map.put(key, appended);
     }
 
     /**
      * Adds a value in the dataset with the given {@code path}. If there already
      * exist one or more values at the same path, then the new value is added to
      * them to form a list, with the new value at the end of the list.
      *
      * @param path       A sequence of keys separated by dots.
      *                   For example, "a.b.c" refers to a value
      *                   "c" contained in a nested dataset "b"
      *                   contained in a dataset "a" contained
      *                   in the current dataset
      * @param value      New value to associate with the key.
      */
     public void addPath(String path, Object value) {
         ParentInfo info = lookupParent(path, true);
         Object old = info.parent.check(info.lastName);
         Object appended = appendValue(old, value);
         info.parent.set(info.lastName, appended);
     }
 
     /**
      * Read a serialized dataset and add its contents to the current dataset.
      * If there are conflicts between values in the serialized dataset
      * and this dataset, the values from the serialized data set replace
      * the existing values.
      * @param source               Contains a serialized dataset, in the
      *                             syntax generated by {@code serialize}.
      */
     public void addSerializedData(CharSequence source) {
         addSerializedData(source, 0);
     }
 
     /**
      * Returns the value associated with {@code keyOrPath} if there is one.
      * Otherwise, the {@code found} instance variable is set to false.
      * @param keyOrPath                Name of the desired element (key in
      *                                 top-level dataset or multi-level path).
      * @return                         Value associated with {@code keyOrPath}.
      *                                 If there are multiple values, only the
      *                                 first one is returned. If there is no
      *                                 such value, returns null.
      */
     public Object check(String keyOrPath) {
         Object o = lookup(keyOrPath, Quantity.FIRST_ONLY);
         found = o != null;
         return o;
     }
 
     /**
      * Returns the int value associated with {@code keyOrPath} if there is one.
      * Otherwise, the {@code found} instance variable is set to false.
      * @param keyOrPath                Name of the desired element (key in
      *                                 top-level dataset or multi-level path).
      * @return                         Value associated with {@code keyOrPath}.
      *                                 If there are multiple values, only the
      *                                 first one is returned. If there is no
      *                                 such value, returns false.
      * @throws InvalidConversionError  Thrown if there is a value associated
      *                                 {@code keyOrPath}, but it cannot be
      *                                 converted to a Dataset.
      */
     public boolean checkBool(String keyOrPath) {
         Object value = lookup(keyOrPath, Quantity.FIRST_ONLY);
 
         if (value == null) {
             found = false;
             return false;
         } else {
             boolean b = Convert.toBool(value, convertSuccess);
             found = convertSuccess.succeeded();
             return b;
         }
     }
 
     /**
      * Returns the Dataset value associated with {@code keyOrPath} if there is one.
      * Otherwise, the {@code found} instance variable is set to false.
      * @param keyOrPath                Name of the desired element (key in
      *                                 top-level dataset or multi-level path).
      * @return                         Value associated with {@code keyOrPath}.
      *                                 If there are multiple values, only the
      *                                 first one is returned. If there is no
      *                                 such value, returns null.
      * @throws InvalidConversionError  Thrown if there is a value associated
      *                                 {@code keyOrPath}, but it cannot be
      *                                 converted to a Dataset.
      */
     public Dataset checkDataset(String keyOrPath) {
         Object value = lookup(keyOrPath, Quantity.FIRST_ONLY);
 
         if (value == null) {
             found = false;
             return null;
         } else {
             Dataset d = Convert.toDataset(value, convertSuccess);
             found = convertSuccess.succeeded();
             return d;
         }
     }
 
     /**
      * Returns the double associated with {@code keyOrPath} if there is one.
      * The {@code found} instance variable is set to true if keyOrPath exists,
      * and to false otherwise.
      * @param keyOrPath                Name of the desired element (key in
      *                                 top-level dataset or multi-level path).
      * @return                         Value associated with {@code keyOrPath}.
      *                                 If there are multiple values, only the
      *                                 first one is returned. If there is no
      *                                 such value, returns Double.MIN_VALUE.
      * @throws InvalidConversionError  Thrown if there is a value associated
      *                                 {@code keyOrPath}, but it cannot be
      *                                 converted to a Dataset.
      */
     public double checkDouble(String keyOrPath) {
         Object value = lookup(keyOrPath, Quantity.FIRST_ONLY);
 
         if (value == null) {
             found = false;
             return Double.MIN_VALUE;
         } else {
             double d = Convert.toDouble(value, convertSuccess);
             found = convertSuccess.succeeded();
             return d;
         }
     }
 
     /**
      * Returns the int associated with {@code keyOrPath} if there is one.
      * The {@code found} instance variable is set to true if keyOrPath exists,
      * and to false otherwise.
      * @param keyOrPath                Name of the desired element (key in
      *                                 top-level dataset or multi-level path).
      * @return                         Value associated with {@code keyOrPath}.
      *                                 If there are multiple values, only the
      *                                 first one is returned. If there is no
      *                                 such value, returns Integer.MIN_VALUE.
      * @throws InvalidConversionError  Thrown if there is a value associated
      *                                 {@code keyOrPath}, but it cannot be
      *                                 converted to a Dataset.
      */
     public int checkInt(String keyOrPath) {
         Object value = lookup(keyOrPath, Quantity.FIRST_ONLY);
 
         if (value == null) {
             found = false;
             return Integer.MIN_VALUE;
         } else {
             int i = Convert.toInt(value, convertSuccess);
             found = convertSuccess.succeeded();
             return i;
         }
     }
 
     /**
      * Returns the String value associated with {@code keyOrPath} if there is one.
      * Otherwise, the {@code found} instance variable is set to false.
      * @param keyOrPath                Name of the desired element (key in
      *                                 top-level dataset or multi-level path).
      * @return                         Value associated with {@code keyOrPath}.
      *                                 If there are multiple values, only the
      *                                 first one is returned. If there is no
      *                                 such value, returns null.
      * @throws InvalidConversionError  Thrown if there is a value associated
      *                                 {@code keyOrPath}, but it cannot be
      *                                 converted to a Dataset.
      */
     public String checkString(String keyOrPath) {
         Object value = lookup(keyOrPath, Quantity.FIRST_ONLY);
 
         if (value == null) {
             found = false;
             return null;
         } else {
             // toString always succeeds
             found = true;
             return Convert.toString(value, convertSuccess);
         }
     }
 
     /**
      * Delete all of the contents of a dataset, leaving the dataset
      * empty.
      */
     public void clear() {
         map.clear();
     }
 
     /**
      * Generates and returns a "deep copy" of the dataset, such that
      * no modification to either dataset will be visible in the other.
      * Only the datasets themselves are cloned; the values in the datasets
      * are not.
      * @return                     A copy of the current dataset.
      */
     public Dataset clone() {
        return clone(null);
     }
 
     /**
      * Clones the values in this dataset and copies them into {@code dest}, such
      * that no modification to either dataset will be visible to the other.
      * If the two datasets share any keys, the ones in {@code dest} are
      * overwritten. Only the datasets themselves are cloned; the values in the
      * datasets are not.
      * @param dest       Values from the current dataset are copied into
      *                   {@code dest}. If null, an empty dataset is created.
      * @return           A dataset with the values in the current dataset
      *                   copied into it.
      */
     public Dataset clone(Dataset dest) {
         if (dest == null) {
             dest = new Dataset();
         }
 
         dest.fileName = fileName;
 
         // Each iteration through the following loop copies one key-value
         // pair from source to dest; it will invoke this method recursively
         // to copy the contents of nested datasets.
         for (Map.Entry<String,Object> pair :
                  ((HashMap<String,Object>) map).entrySet()) {
             Object value = pair.getValue();
             if (value instanceof DSArrayList) {
                 DSArrayList sourceList = (DSArrayList) value;
                 DSArrayList destList = new DSArrayList(sourceList.size());
                 for (Object value2 : sourceList) {
                     if (value2 instanceof Dataset) {
                         destList.add(((Dataset) value2).clone());
                     } else {
                         destList.add(value2);
                     }
                 }
                 dest.set(pair.getKey(), destList);
             } else if (value instanceof Dataset) {
                 dest.set(pair.getKey(), ((Dataset) value).clone());
             } else {
                 dest.set(pair.getKey(), value);
             }
         }
         return dest;
     }
 
     /**
      * Indicates whether a key or path exists in the dataset.
      * @param key                  Name or path of the desired value.
      * @return                     True if the key or path dataset, false
      *                             otherwise.
      */
     public boolean containsKey(String key) {
         return (check(key) != null);
     }
 
     /**
      * Copies all of the top-level values from another dataset into
      * this one, replacing any existing values with the same names.
      * Nested datasets and lists are cloned before copying so that the
      * datasets remain independent.
      * @param source              Dataset whose values are to be copied
      *                            into this one.
      */
     public void copyFrom(Dataset source) {
         String tmp = fileName;
         source.clone(this);
         fileName = tmp;
     }
 
     /**
      * Removes a value from the dataset. If there is no value with the
      * given key, the key will be treated as a path. If neither of the two
      * exist, this method does nothing.
      * @param keyOrPath                Name of the desired element (key in
      *                                 top-level dataset or multi-level path).
      */
     public void delete(String keyOrPath) {
         Object old = map.remove(keyOrPath);
 
         if (old == null) {
             ParentInfo info = lookupParent(keyOrPath, false);
             if (info != null && info.parent != this) {
                 info.parent.delete(info.lastName);
             }
         }
     }
 
     /**
      * If this dataset was originally read from a file, this method
      * will provide the name of that file.
      * @return                     The name of the file from which the dataset
      *                             was originally read, or null if the dataset
      *                             was not created from a file.
      */
     public String getFileName() {
         return fileName;
     }
 
     /**
      * Returns the value associated with {@code keyOrPath}.
      *
      * @param keyOrPath            Name of the desired element (key in top-level
      *                             dataset or multi-level path).
      * @return                     Value associated with {@code keyOrPath}.
      *                             If there are multiple values, only the first
      *                             one is returned.
      * @throws MissingValueError   Thrown if {@code keyOrPath} can't be found
      */
     public Object get(String keyOrPath) {
         Object value = lookup(keyOrPath, Quantity.FIRST_ONLY);
 
         if (value == null) {
             throw new MissingValueError(keyOrPath);
         }
 
         return value;
     }
 
     /**
      * Returns the bool associated with {@code keyOrPath}.
      * @param keyOrPath                Name of the desired element (key in
      *                                 top-level dataset or multi-level path).
      * @return                         Bool associated with {@code keyOrPath}.
      *                                 If there are multiple values, only the
      *                                 first one is returned.
      * @throws MissingValueError       Thrown if {@code keyOrPath} does
      * @throws InvalidConversionError  Thrown if there is a value associated
      *                                 {@code keyOrPath}, but it cannot be
      *                                 converted to a bool.
      */
     public boolean getBool(String keyOrPath) {
         Object value = lookup(keyOrPath, Quantity.FIRST_ONLY);
 
         if (value == null) {
             throw new MissingValueError(keyOrPath);
         }
 
         boolean b = Convert.toBool(value, convertSuccess);
         if (convertSuccess.succeeded() == false) {
             throw new InvalidConversionError(value, "boolean");
         }
         return b;
     }
 
     /**
      * Returns the Dataset associated with {@code keyOrPath}.
      * @param keyOrPath                Name of the desired element (key in
      *                                 top-level dataset or multi-level path).
      * @return                         Dataset associated with {@code keyOrPath}.
      *                                 If there are multiple values, only the
      *                                 first one is returned.
      * @throws MissingValueError       Thrown if {@code keyOrPath} does
      * @throws InvalidConversionError  Thrown if there is a value associated
      *                                 {@code keyOrPath}, but it cannot be
      *                                 converted to a Dataset.
      */
     public Dataset getDataset(String keyOrPath) {
         Object value = lookup(keyOrPath, Quantity.FIRST_ONLY);
 
         if (value == null) {
             throw new MissingValueError(keyOrPath);
         }
 
         Dataset d = Convert.toDataset(value, convertSuccess);
         if (convertSuccess.succeeded() == false) {
             throw new InvalidConversionError(value, "Dataset");
         }
         return d;
     }
 
     /**
      * Returns the double associated with {@code keyOrPath}.
      * @param keyOrPath                Name of the desired element (key in
      *                                 top-level dataset or multi-level path).
      * @return                         Double associated with {@code keyOrPath}.
      *                                 If there are multiple values, only the
      *                                 first one is returned.
      * @throws MissingValueError       Thrown if {@code keyOrPath} does
      * @throws InvalidConversionError  Thrown if there is a value associated
      *                                 {@code keyOrPath}, but it cannot be
      *                                 converted to a double.
      */
     public double getDouble(String keyOrPath) {
         Object value = lookup(keyOrPath, Quantity.FIRST_ONLY);
 
         if (value == null) {
             throw new MissingValueError(keyOrPath);
         }
 
         Double d = Convert.toDouble(value, convertSuccess);
         if (convertSuccess.succeeded() == false) {
             throw new InvalidConversionError(value, "double");
         }
         return d;
     }
 
     /**
      * Returns the int associated with {@code keyOrPath}.
      * @param keyOrPath                Name of the desired element (key in
      *                                 top-level dataset or multi-level path).
      * @return                         Int associated with {@code keyOrPath}.
      *                                 If there are multiple values, only the
      *                                 first one is returned.
      * @throws MissingValueError       Thrown if {@code keyOrPath} can't be found
      * @throws InvalidConversionError  Thrown if there is a value associated
      *                                 {@code keyOrPath}, but it cannot be
      *                                 converted to an int.
      */
     public int getInt(String keyOrPath) {
         Object value = lookup(keyOrPath, Quantity.FIRST_ONLY);
 
         if (value == null) {
             throw new MissingValueError(keyOrPath);
         }
 
         int i = Convert.toInt(value, convertSuccess);
         if (convertSuccess.succeeded() == false) {
             throw new InvalidConversionError(value, "int");
         }
         return i;
     }
 
     /**
      * Returns the String associated with {@code keyOrPath}.
      * @param keyOrPath                Name of the desired element (key in
      *                                 top-level dataset or multi-level path).
      * @return                         String associated with {@code keyOrPath}.
      *                                 If there are multiple values, only the
      *                                 first one is returned.
      * @throws MissingValueError       Thrown if {@code keyOrPath} does
      * @throws InvalidConversionError  Thrown if there is a value associated
      *                                 {@code keyOrPath}, but it cannot be
      *                                 converted to a String.
      */
     public String getString(String keyOrPath) {
         Object value = lookup(keyOrPath, Quantity.FIRST_ONLY);
 
         if (value == null) {
             throw new MissingValueError(keyOrPath);
         }
 
         String s = Convert.toString(value, convertSuccess);
         if (convertSuccess.succeeded() == false) {
             throw new InvalidConversionError(value, "String");
         }
         return s;
     }
 
     /**
      * Returns all the values associated with {@code keyOrPath}.
      * @param keyOrPath                Name of the desired element (key in
      *                                 top-level dataset or multi-level path).
      * @return                         Array or values associated with
      *                                 {@code keyOrPath}.
      */
     public ArrayList<Object> getList(String keyOrPath) {
         ArrayList<Object> list = (ArrayList) lookup(keyOrPath, Quantity.ALL);
         return (ArrayList<Object>) list.clone();
     }
 
     /**
      * Returns all the bools associated with {@code keyOrPath}.
      * @param keyOrPath                Name of the desired elements (key in
      *                                 top-level dataset or multi-level path).
      * @return                         Array of bools associated with
      *                                 {@code keyOrPath}. If there are none,
      *                                 the array is empty.
      * @throws InvalidConversionError  Thrown if any of the values cannot be
      *                                 converted to a bool.
      */
     public ArrayList<Boolean> getBoolList(String keyOrPath) {
         ArrayList<Object> list = (ArrayList) lookup(keyOrPath, Quantity.ALL);
         ArrayList<Boolean> boolList = new ArrayList<Boolean>();
 
         for (Object item : list) {
             boolean b = Convert.toBool(item, convertSuccess);
             if (convertSuccess.succeeded() == false) {
                 throw new InvalidConversionError(item, "boolean");
             }
             boolList.add(b);
         }
 
         return boolList;
     }
 
     /**
      * Returns all the Datasets associated with {@code keyOrPath}.
      * @param keyOrPath                Name of the desired elements (key in
      *                                 top-level dataset or multi-level path).
      * @return                         Array of Datasets associated with
      *                                 {@code keyOrPath}. If there are none,
      *                                 the array is empty.
      * @throws InvalidConversionError  Thrown if any of the values cannot be
      *                                 converted to a Dataset.
      */
     public ArrayList<Dataset> getDatasetList(String keyOrPath) {
         ArrayList<Object> list = (ArrayList) lookup(keyOrPath, Quantity.ALL);
         ArrayList<Dataset> datasetList = new ArrayList<Dataset>();
 
         for (Object item : list) {
             Dataset d = Convert.toDataset(item, convertSuccess);
             if (convertSuccess.succeeded() == false) {
                 throw new InvalidConversionError(item, "Dataset");
             }
             datasetList.add(d);
         }
 
         return datasetList;
     }
 
     /**
      * Returns all the ints associated with {@code keyOrPath}.
      * @param keyOrPath                Name of the desired elements (key in
      *                                 top-level dataset or multi-level path).
      * @return                         Array of ints associated with
      *                                 {@code keyOrPath}. If there are none,
      *                                 the array is empty.
      * @throws InvalidConversionError  Thrown if any of the values cannot be
      *                                 converted to an int.
      */
     public ArrayList<Integer> getIntList(String keyOrPath) {
         ArrayList<Object> list = (ArrayList) lookup(keyOrPath, Quantity.ALL);
         ArrayList<Integer> intList = new ArrayList<Integer>(3);
 
         for (Object item : list) {
             int i = Convert.toInt(item, convertSuccess);
             if (convertSuccess.succeeded() == false) {
                 throw new InvalidConversionError(item, "int");
             }
             intList.add(i);
         }
 
         return intList;
     }
 
     /**
      * Returns all the doubles associated with {@code keyOrPath}.
      * @param keyOrPath                Name of the desired elements (key in
      *                                 top-level dataset or multi-level path).
      * @return                         Array of doubles associated with
      *                                 {@code keyOrPath}. If there are none,
      *                                 the array is empty.
      * @throws InvalidConversionError  Thrown if any of the values cannot be
      *                                 converted to a double.
      */
     public ArrayList<Double> getDoubleList(String keyOrPath) {
         ArrayList<Object> list = (ArrayList) lookup(keyOrPath, Quantity.ALL);
         ArrayList<Double> doubleList = new ArrayList<Double>(3);
 
         for (Object item : list) {
             double d = Convert.toDouble(item, convertSuccess);
             if (convertSuccess.succeeded() == false) {
                 throw new InvalidConversionError(item, "double");
             }
             doubleList.add(d);
         }
 
         return doubleList;
     }
 
     /**
      * Returns all the Strings associated with {@code keyOrPath}.
      * @param keyOrPath                Name of the desired elements (key in
      *                                 top-level dataset or multi-level path).
      * @return                         Array of Strings associated with
      *                                 {@code keyOrPath}. If there are none,
      *                                 the array is empty.
      * @throws InvalidConversionError  Thrown if any of the values cannot be
      *                                 converted to a String.
      */
     public ArrayList<String> getStringList(String keyOrPath) {
         ArrayList<Object> list = (ArrayList) lookup(keyOrPath, Quantity.ALL);
         ArrayList<String> stringList = new ArrayList<String>(3);
 
         for (Object item : list) {
             stringList.add(Convert.toString(item, convertSuccess));
         }
 
         return stringList;
     }
 
     /**
      * Returns a Set containing all of the top-level keys in the dataset.
      * @return                     All of the keys at the top level of
      *                             the dataset.
      */
     @SuppressWarnings("unchecked")
     public Set<String> keySet() {
         return map.keySet();
     }
 
     /**
      * Generate a compact string representation for the dataset.
      * This form is intended for efficient transmission and storage;
      * it is not intended to be human readable.  The only code that
      * should attempt to understand the structure of this information is
      * the addSerializedData method of this class.
      * @param out                  A description of the dataset is
      *                             appended here.
      */
     public void serialize(StringBuilder out) {
         // Here is the grammar for the serialized representation of
         // a dataset.  The basic idea is to use run-length encoding,
         // where a string is represented as {@code length.contents},
         // where the contents of the string are preceded by its length
         // in characters and a "." to delimit the length.
         // <dataset> = "("<element>("\n"<element)*")"
         // <element> = <name><value>
         // <name> = <encodedString>
         // encodedString> = <length>"."<contents>
         // <value> = <encodedString> | (<dataset>)+
         //
         // Here's an example of a dataset containing a string value
         // {@code age} and a list of nested datasets named {@code children},
         // each with a {@code name} element.
         // (3.age2.24
         // 8.children(4.name5.Alice)(4.name3.Bob)(4.name5.Carol))
         out.append('(');
         String prefix = "";
         Collection keySet = map.keySet();
         if (sortOutput) {
             keySet = new TreeSet<String>(keySet);
         }
         for (Object nameObject: keySet) {
             String name = (String) nameObject;
             out.append(prefix);
             out.append(name.length());
             out.append('.');
             out.append(name);
             Object value = map.get(name);
             if (value instanceof Dataset) {
                 ((Dataset) value).serialize(out);
             } else if (value instanceof DSArrayList) {
                DSArrayList<Object> list = (DSArrayList <Object>) value;
                for (Object value2 : list) {
                    if (value2 instanceof Dataset) {
                        ((Dataset) value2).serialize(out);
                    } else {
                        String s = value2.toString();
                        out.append(s.length());
                        out.append('.');
                        out.append(s);
                    }
                 }
             } else {
                 String s = value.toString();
                 out.append(s.length());
                 out.append('.');
                 out.append(s);
             }
             prefix = "\n";
         }
         out.append(')');
     }
 
     /**
      * Generate a compact string representation for the dataset.
      * This form is intended for efficient transmission and storage;
      * it is not intended to be human readable.  The only code that
      * should attempt to understand the structure of this information is
      * the addSerializedData method of this class.
      * @return                     The serialized representation of the
      *                             dataset.
      */
     public String serialize() {
         StringBuilder out = new StringBuilder();
         serialize(out);
         return out.toString();
     }
 
     /**
      * Sets a value in the top level of the dataset with the given {@code key},
      * replacing any existing value(s).
      *
      * @param key        Name of a value in the top-level of the
      *                   dataset (not a path).
      * @param value      New value to associate with the key.
      */
     public void set(String key, Object value) {
         map.put(key, value);
     }
 
     /**
      * Sets a value in the dataset with the given {@code path}, replacing any
      * existing value(s).
      *
      * @param path       A sequence of keys separated by dots.
      *                   For example, "a.b.c" refers to a value
      *                   "c" contained in a nested dataset "b"
      *                   contained in a dataset "a" contained
      *                   in the current dataset
      * @param value      New value to associate with the key.
      */
     public void setPath(String path, Object value) {
         ParentInfo info = lookupParent(path, true);
         info.parent.set(info.lastName, value);
     }
 
     /**
      * Generate a Javascript description of the database contents, in the form
      * of a Javascript Object literal enclosed in braces.
      * @param out                  The Javascript is appended to this
      *                             StringBuilder.
      */
     public void toJavascript(StringBuilder out) {
         out.append('{');
         String prefix = "";
         Collection keySet = keySet();
         if (sortOutput) {
             keySet = new TreeSet<String>(keySet);
         }
         for (Object nameObject: keySet) {
             String name = (String) nameObject;
             out.append(prefix);
             out.append(name);
             out.append(": ");
             Object value = map.get(name);
             if (value instanceof Dataset) {
                 ((Dataset) value).toJavascript(out);
             } else if (value instanceof DSArrayList) {
                 out.append('[');
                 DSArrayList<Object> list = (DSArrayList <Object>) value;
                 String listPrefix = "";
                 for (int i = 0; i < list.size(); i++) {
                     out.append(listPrefix);
                     Object value2 = list.get(i);
                     if (value2 instanceof Dataset) {
                         ((Dataset) value2).toJavascript(out);
                     } else {
                         out.append(value2.toString());
                     }
                     listPrefix = ", ";
                 }
                 out.append(']');
             } else if (value instanceof Number || value instanceof Boolean){
                 out.append(value.toString());
             } else {
                 out.append('"');
                 Html.escapeStringChars(value.toString(), out);
                 out.append('"');
             }
             prefix = ", ";
         }
         out.append('}');
     }
 
     /**
      * Generates a nicely formatted string displaying the contents
      * of the dataset (intended primarily for testing).  By default
      * the dataset is formatted using YAML syntax.
      * @return                     Pretty-printed string.
      */
     public String toString() {
         return YamlDataset.writeString(this);
     }
 
     /**
      * Writes the contents of the dataset to a file on disk.  This particular
      * class doesn't have a defined disk format, so it throws an Error
      * @param name                 Name of file to write.
      * @param comment              Optional text describing the meaning of
      *                             the file for humans who might stumble
      *                             across it.  Null means no comment.
      */
     public void writeFile(String name, String comment) {
         throw new UnknownFileFormatError("class " +
                 getClass().getSimpleName() +
                 " doesn't know how to write datasets to files");
     }
 
     /**
      * Appends a value to a list of values, for use as a dataset element. If
      * two values share the same key, they become a list of values.
      *
      * @param old        Either null, an object, or a DSArrayList of objects
      * @param value      Appended to old
      */
     protected static Object appendValue(Object old, Object value) {
         if (old != null)  {
             DSArrayList array;
             if (old instanceof DSArrayList) {
                 array = (DSArrayList) old;
             } else {
                 array = new DSArrayList(3);
                 array.add(old);
             }
             array.add(value);
             return array;
         }
         return value;
     }
 
     /**
      * Read a serialized dataset and add its contents to the current dataset.
      * If there are conflicts between values in the serialized dataset
      * and this dataset, the values from the serialized data set replaces the
      * existing values.
      * @param source               Contains a serialized dataset, in the
      *                             syntax generated by {@code serialize}.
      * @param start                Index in {@code source} of the "("
      *                             character that starts the encoded dataset.
      * @return                     Index of the character just after the
      *                             ")" that ends a serialized dataset.
      */
     protected int addSerializedData(CharSequence source, int start) {
         IntBox end = new IntBox();
         int i = start;
         int length = source.length();
 
         if ((i >= length) || (source.charAt(i) != '(')) {
             throw new SyntaxError(null,
                     "serialized dataset didn't start with \"(\"");
         }
         i++;
 
         // Each iteration through the following loop parses one top-level
         // element for the dataset, which can be either a string value
         // a single nested dataset, or a list of nested datasets.
         while (i < length) {
             // See if we have reached the end of this dataset.
             if (source.charAt(i) == ')') {
                 return i+1;
             }
 
             // Get the name of the next element.
             String name = getEncodedString(source, i, end);
             i = end.value;
 
             // See if this is a simple value or nested dataset(s).
             if (i >= length) {
                 throw new SyntaxError(null,
                         "no value for element \"" + name + "\" in " +
                         "serialized dataset");
             }
             if (source.charAt(i) != '(') {
                 // String value.
                 set(name, getEncodedString(source, i, end));
                 i = end.value;
             } else {
                 // Nested dataset(s).  At the start of each iteration
                 // {@code i} refers to the "(" that starts the next child
                 // dataset.
                 while (true) {
                     Dataset child = new Dataset();
                     i = child.addSerializedData(source, i);
                     add(name, child);
                     if ((i >= length) || (source.charAt(i) != '(')) {
                         break;
                     }
                 }
             }
             if ((i < length) && (source.charAt(i) == '\n')) {
                 i++;
             }
         }
         throw new SyntaxError(null,
                 "serialized dataset not terminated by \")\"");
     }
 
 
     /**
      * Extract a run-length encoded string from the input, and return it, used
      * to deserialize datasets.
      * @param source               Source string.  The characters starting
      *                             at {@code start} must consist of a
      *                             decimal integer followed by a "."
      *                             followed by the string, whose length is
      *                             given by the integer.
      * @param start                Index within {@code source} of the first
      *                             character in the run-length encoded
      *                             string.
      * @param end                  Used to return the index in {@code source}
      *                             of the first character after the string.
      * @return                     The extracted string.
      *
      */
     protected static String getEncodedString(CharSequence source, int start,
             IntBox end) {
         // Convert everything up to the next "." into a decimal integer.
         int i = start;
         int length = 0;
         int sourceLength = source.length();
         while (true) {
             if (i >= sourceLength) {
                 throw new SyntaxError(null,
                     "missing \".\" in serialized dataset");
             }
             char c = source.charAt(i);
             if (c == '.') {
                 i++;
                 break;
             }
             length = length*10 + (c - '0');
             i++;
         }
 
         // Use the length to extract the actual string value.
         if (length < 0) {
             throw new SyntaxError(null,
                     "serialized dataset has improper length \"" +
                     source.subSequence(start, i-1) + "\"");
         }
         if ((i + length) > sourceLength) {
             throw new SyntaxError(null,
                     "unexpected end of serialized dataset");
         }
         end.value = i + length;
         return source.subSequence(i, i + length).toString();
     }
 
     /**
      * This is a general-purpose method to find the value(s) associated
      * with a given key, intended primarily for use by other methods such
      * as {@code get} and {@code check}.  The value(s) may either be in
      * the top level of the dataset or refer to a path
      * @param keyOrPath            Name of the desired element (key in
      *                             top-level dataset or multi-level path).
      * @param quantity             Indicates whether all matching values
      *                             should be returned, or only the first
      *                             one found.
      * @return                     The return value is null if no matching
      *                             values are found.  If {@code quantity} is
      *                             {@code FIRST_ONLY} then the return
      *                             value is the first match found; otherwise
      *                             the return value is a DSArrayList, containing
      *                             all matches.
      */
     protected Object lookup(String keyOrPath, Quantity quantity) {
         Object value = map.get(keyOrPath);
 
         if (value == null) {
             value = lookupPath(keyOrPath, 0, quantity, null);
         }
 
         if (quantity == Quantity.ALL) {
             if (value == null) {
                 return new DSArrayList();
             } else if (value instanceof DSArrayList) {
                 return value;
             } else {
                 DSArrayList array = new DSArrayList();
                 array.add(value);
                 return array;
             }
         } else {
             if (value == null) {
                 return null;
             } else if (value instanceof DSArrayList) {
                 return ((DSArrayList) value).get(0);
             } else {
                 return value;
             }
         }
     }
 
 
     /**
      * Given a path, find the Dataset that contains the element named in
      * the path (if there is one) and return it along with the final
      * name in the past.  This method is used internally by several other
      * methods, such as setPath and delete.
      * @param path                 A sequence of keys separated by dots.
      * @param create               True means this method is being invoked
      *                             as part of a "create" operation: if any
      *                             of the ancestors of {@code path} don't
      *                             exist then they are created, overwriting
      *                             any non-dataset values they used to have).
      *                             False means just return a null if either
      *                             of these problems occurs.
      * @return                     ParentInfo structure with information
      *                             corresponding to {@code path}, or null
      *                             if the parent doesn't exist.
      */
     @SuppressWarnings("unchecked")
     protected ParentInfo lookupParent(String path, boolean create) {
         int startIndex = 0;
         Dataset parent = this;
         Object child;
         String key;
         int dot;
 
         // Each iteration through the following loop extracts the next
         // key from path and looks it up.
         while (true) {
             dot = path.indexOf('.', startIndex);
             if (dot == -1) {
                 break;
             }
             key = path.substring(startIndex, dot);
             child = parent.check(key);
 
             // Make sure that the current object is a nested dataset.
             if (child instanceof DSArrayList) {
                 // The child consists of a list of nested datasets; take
                 // the first one.
                 child = ((DSArrayList) child).get(0);
             }
             if (!(child instanceof Dataset)) {
                 // Child doesn't exist or isn't a Dataset
                 if (!create) {
                     return null;
                 }
                 child = new Dataset();
                 parent.set(key, child);
             }
             parent = (Dataset) child;
             startIndex = dot+1;
         }
 
         // At this point we have found the parent.
         return new ParentInfo(parent, path.substring(startIndex));
     }
 
     /**
      * This is a general-purpose method to find one or more values associated
      * with a hierarchical path, intended primarily for use by other methods
      * such as {@code lookup}. There can be
      * multiple values associated with a single path if some of the elements
      * of the path refer to nested datasets.  For example, if the element
      * {@code b} in the path {@code a.b.c} refers to 3 nested datasets
      * then there could be 3 values corresponding to {@code a.b.c}.  These
      * values need not necessarily be the same type.
      * @param path                 Dot-separated collection of element names,
      *                             indicating the desired values.
      * @param start                The caller has already processed the
      *                             portion of {@code path} up to this
      *                             index.  The next element name starts at
      *                             this index.
      * @param quantity             Indicates whether all matching values
      *                             should be returned, or only the first
      *                             one found.
      * @param results              If {@code quantity} is {@code ALL} and
      *                             this argument is non-null then the
      *                             matching values are appended to this
      *                             rather than creating a new ArrayList,
      *                             and the return value will be
      *                             {@code results}.
      * @return                     The return value is null if no matching
      *                             value is found.  If {@code quantity} is
      *                             {@code FIRST_ONLY} then the return
      *                             value is the first match found; otherwise
      *                             the return value is a DSArrayList, containing
      *                             all matches.
      */
     @SuppressWarnings("unchecked")
      protected Object lookupPath(String path, int start, Quantity quantity,
                                  DSArrayList<Object> results) {
         if (results == null && quantity == Quantity.ALL) {
             results = new DSArrayList<Object>();
         }
 
         int length = path.length();
         int dot = path.indexOf('.', start);
         if (dot == -1) {
             dot = length;
         }
         String key = path.substring(start, dot);
         Object nextObject = map.get(key);
         if (nextObject == null) {
             return null;
         }
         if (dot >= length) {
             // We've reached the end of the path; add the value(s) to
             // the result and return.
             if (quantity == Quantity.FIRST_ONLY) {
                 return nextObject;
             } else {
                 if (nextObject instanceof DSArrayList) {
                     results.addAll((DSArrayList) nextObject);
                 } else {
                     results.add(nextObject);
                 }
                 return results;
             }
         }
 
         // If we get here it means there are more path elements to look up.
         // Make a recursive call for each nested dataset in the current value.
         dot++;
         if (nextObject instanceof Dataset) {
             return ((Dataset) nextObject).lookupPath(path, dot,
                        quantity, results);
         } else if (nextObject instanceof DSArrayList) {
             Object returnValue = null;
             DSArrayList list = (DSArrayList) nextObject;
             for (Object obj : list) {
                 if (obj instanceof Dataset) {
                     Object nestedResult = ((Dataset) obj).lookupPath(path, dot,
                                           quantity, results);
                     if (nestedResult != null) {
                         if (quantity == Quantity.FIRST_ONLY) {
                             // We only need one result, and we have it.  No
                             // need to search additional nested datasets.
                             return nestedResult;
                         } else {
                             // The result is a DSArrayList; save it to use for
                             // future results (it's possible that the next call
                             // allocated it).
                             results = (DSArrayList<Object>) nestedResult;
                             returnValue = nestedResult;
                         }
                     }
                 }
             }
             return returnValue;
         } else {
             return null;
         }
     }
 }
