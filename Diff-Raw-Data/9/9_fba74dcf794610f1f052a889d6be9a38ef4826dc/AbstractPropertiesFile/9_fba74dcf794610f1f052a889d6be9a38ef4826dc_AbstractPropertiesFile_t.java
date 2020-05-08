 /*
  * This file is part of VIUtils.
  *
  * Copyright © 2012-2013 Visual Illusions Entertainment
  *
  * VIUtils is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  *
  * VIUtils is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with VIUtils.
  * If not, see http://www.gnu.org/licenses/lgpl.html.
  */
 package net.visualillusionsent.utils;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 /**
  * Abstract Properties File
  * 
 * @since 1.1.0
  * @version 1.0
  * @author Jason (darkdiplomat)
  */
 public abstract class AbstractPropertiesFile{
 
     protected File propsFile;
     protected String filepath;
     protected JarFile jar;
     protected Map<String, String> props;
     protected Map<String, List<String>> comments;
     protected Map<String, String> inlineCom;
     protected List<String> header;
     protected List<String> footer = new LinkedList<String>();
     protected boolean hasChanged;
 
     /**
      * Creates or loads a Properties File
      * 
      * @param filepath
      *            the path to the properties file
      * @throws UtilityException
      * <br>
      *             if there was an error with either reading or writing the properties file
      */
     public AbstractPropertiesFile(String filepath) throws UtilityException{
         if(filepath == null){
             throw new UtilityException("arg.null", "String filepath");
         }
         else if(filepath.trim().isEmpty()){
             throw new UtilityException("arg.empty", "String filepath");
         }
         this.filepath = filepath;
         propsFile = new File(filepath);
     }
 
     /**
      * Loads a PropertiesFile stored inside a Jar file
      * 
      * @param jarpath
      *            the path to the Jar file
      * @param entry
      *            the name of the file inside of the jar
      * @throws UtilityException
      * <br>
      *             if jarpath is null or empty<br>
      *             or if entry is null or empty<br>
      *             or if the Jar file is not found or unable to be read from<br>
      *             or if the Jar file does not contain the entry
      */
     public AbstractPropertiesFile(String jarpath, String entry) throws UtilityException{
         if(jarpath == null){
             throw new UtilityException("arg.null", "String jarpath");
         }
         else if(jarpath.trim().isEmpty()){
             throw new UtilityException("arg.empty", "String jarpath");
         }
         else if(entry == null){
             throw new UtilityException("arg.null", "String entry");
         }
         else if(entry.trim().isEmpty()){
             throw new UtilityException("arg.empty", "String entry");
         }
         try{
             jar = new JarFile(jarpath);
         }
         catch(IOException ioe){
             throw new UtilityException("Unable to get JarFile");
         }
         JarEntry ent = jar.getJarEntry(entry);
         if(ent == null){
             throw new UtilityException("entry.missing", entry);
         }
         filepath = entry;
     }
 
     /**
      * Loads the Properties File
      * 
      * @throws UtilityException
      * <br>
      *             if there was an error with reading the properties file
      */
     protected abstract void load(InputStream instream) throws UtilityException;
 
     /**
      * Reloads the PropertiesFile from its source
      * 
      * @throws UtilityException
      * <br>
      *             if there was an error with reading the properties file<br>
      *             or if the Jar file is not found or unable to be read from<br>
      *             or if the Jar file does not contain the entry
      */
     protected abstract void reload() throws UtilityException;
 
     /**
      * Saves the Properties File<br>
      * <b>NOTE:</b> Saving is not supported for PropertiesFiles inside of Jar Files
      * 
      * @throws UtilityException
      * <br>
      *             if there was an error with writing the properties file<br>
      *             or if save is called for a PropertiesFile inside of a Jar
      */
     protected abstract void save() throws UtilityException;
 
     /**
      * Force saves the Properties File<br>
      * <b>NOTE:</b> Saving is not supported for PropertiesFiles inside of Jar Files
      * 
      * @throws UtilityException
      * <br>
      *             if there was an error with writing the properties file<br>
      *             or if save is called for a PropertiesFile inside of a Jar
      */
     protected abstract void forceSave() throws UtilityException;
 
     /**
      * Saves the Properties File<br>
      * <b>NOTE:</b> Saving is not supported for PropertiesFiles inside of Jar Files
      * 
      * @param force
      *            {@code true} to force save the file; {@code false} to save as needed
      * @throws UtilityException
      * <br>
      *             if there was an error with writing the properties file<br>
      *             or if save is called for a PropertiesFile inside of a Jar
      */
     protected abstract void save(boolean force) throws UtilityException;
 
     /**
      * Checks if the PropertiesFile contains a key
      * 
      * @param key
      *            the key to check
      * @return {@code true} if the PropertiesFile contains the key, {@code false} otherwise
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty
      */
     protected abstract boolean containsKey(String key) throws UtilityException;
 
     /**
      * Removes a key and it's associated property and comments from the PropertiesFile
      * 
      * @param key
      *            the key to be removed
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty
      */
     protected abstract void removeKey(String key) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a {@link String}
      * 
      * @param key
      *            the key to get the property for
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      *             or if property was not found
      */
     protected abstract String getString(String key) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a {@link String} or returns the default specified<br>
      * NOTE: This will not save the properties file, it will add the key and value to the map for later saving.
      * 
      * @param key
      *            the key to get the property for
      * @param def
      *            the default value to use if key is not found
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      */
     protected abstract String getString(String key, String def) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the property to be stored
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if value is null
      */
     protected abstract void setString(String key, String value) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile with comments added
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the property to be stored
      * @param comment
      *            the comments to add
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if value is null
      */
     protected abstract void setString(String key, String value, String... comment) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a {@link String} Array<br>
      * Separates at commas ',' and trims extra whitespace from the new elements
      * 
      * @param key
      *            the key to get the property for
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      *             or if property was not found
      */
     protected abstract String[] getStringArray(String key) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a {@link String} Array or returns the default specified<br>
      * Separates at commas ',' and trims extra whitespace from the new elements<br>
      * NOTE: This will not save the properties file, it will add the key and value to the map for later saving.
      * 
      * @param key
      *            the key to get the property for
      * @param def
      *            the default value to use if key is not found
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      */
     protected abstract String[] getStringArray(String key, String[] def) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile<br>
      * Separates elements with a comma ','
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the property to be stored (elements combined using a comma as a spacer)
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setStringArray(String key, String[] value) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile with comments added<br>
      * Separates elements with a comma ','
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the property to be stored
      * @param comment
      *            the comments to add
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setStringArray(String key, String[] value, String... comment) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a {@link String} Array<br>
      * Separates at specified character(s) and trims extra whitespace from the new elements
      * 
      * @param key
      *            the key to get the property for
      * @param splitBy
      *            the character(s) to split the property value with
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      *             or if specified splitter is null or empty<br>
      *             or if property was not found
      */
     protected abstract String[] getStringArray(String key, String splitBy) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a {@link String} Array or returns the default specified<br>
      * Separates at specified character(s) and trims extra whitespace from the new elements<br>
      * NOTE: This will not save the properties file, it will add the key and value to the map for later saving.
      * 
      * @param key
      *            the key to get the property for
      * @param splitBy
      *            the character(s) to split the property value with
      * @param def
      *            the default value to use if key is not found
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      *             or if specified splitter is null or empty<br>
      */
     protected abstract String[] getStringArray(String key, String splitBy, String[] def) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile<br>
      * Separates elements with specified spacer
      * 
      * @param key
      *            the key for the property
      * @param spacer
      *            the character(s) to space the elements with
      * @param value
      *            the property to be stored (elements combined using a comma as a spacer)
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if specified spacer is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setStringArray(String key, String spacer, String[] value) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile with comments added<br>
      * Separates elements with specified spacer
      * 
      * @param key
      *            the key for the property
      * @param spacer
      *            the character(s) to space the elements with
      * @param value
      *            the property to be stored
      * @param comment
      *            the comments to add
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if specified spacer is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setStringArray(String key, String spacer, String[] value, String... comment) throws UtilityException;
 
     /**
      * Gets a byte associated with specified key
      * 
      * @param key
      *            the key to get the property for
      * @return byte associated with the property
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if the property is not a number<br>
      *             or if property is not found
      */
     protected abstract byte getByte(String key) throws UtilityException;
 
     /**
      * Gets a byte associated with specified key or returns the default specified<br>
      * NOTE: This will not save the properties file, it will add the key and value to the map for later saving.
      * 
      * @param key
      *            the key to get the property for
      * @param def
      *            the default value to use if key is not found
      * @return byte associated with the property
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if the property is not a number<br>
      */
     protected abstract byte getByte(String key, byte def) throws UtilityException;
 
     /**
      * Sets a byte as a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the byte value to be stored
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty
      */
     protected abstract void setByte(String key, byte value) throws UtilityException;
 
     /**
      * Sets a byte as a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the byte value to be stored
      * @param comment
      *            the comments to add
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty
      */
     protected abstract void setByte(String key, byte value, String... comment) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a byte Array<br>
      * Separates at commas ',' and trims extra whitespace from the new elements
      * 
      * @param key
      *            the key to get the property for
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      *             or if property was not found
      */
     protected abstract byte[] getByteArray(String key) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a byte Array or returns the default specified<br>
      * Separates at commas ',' and trims extra whitespace from the new elements<br>
      * NOTE: This will not save the properties file, it will add the key and value to the map for later saving.
      * 
      * @param key
      *            the key to get the property for
      * @param def
      *            the default value to use if key is not found
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty
      */
     protected abstract byte[] getByteArray(String key, byte[] def) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile<br>
      * Separates elements with a comma ','
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the property to be stored (elements combined using a comma as a spacer)
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setByteArray(String key, byte[] value) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile with comments added<br>
      * Separates elements with a comma ','
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the property to be stored
      * @param comment
      *            the comments to add
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setByteArray(String key, byte[] value, String... comment) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a byte array<br>
      * Separates at specified character(s) and trims extra whitespace from the new elements
      * 
      * @param key
      *            the key to get the property for
      * @param splitBy
      *            the character(s) to split the property value with
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      *             or if specified splitter is null or empty<br>
      *             or if property was not found
      */
     protected abstract byte[] getByteArray(String key, String splitBy) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a byte array or returns the default specified<br>
      * Separates at specified character(s) and trims extra whitespace from the new elements<br>
      * NOTE: This will not save the properties file, it will add the key and value to the map for later saving.
      * 
      * @param key
      *            the key to get the property for
      * @param splitBy
      *            the character(s) to split the property value with
      * @param def
      *            the default value to use if key is not found
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      *             or if specified splitter is null or empty
      */
     protected abstract byte[] getByteArray(String key, String splitBy, byte[] def) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile<br>
      * Separates elements with specified spacer
      * 
      * @param key
      *            the key for the property
      * @param spacer
      *            the character(s) to space the elements with
      * @param value
      *            the property to be stored (elements combined using a comma as a spacer)
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if specified spacer is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setByteArray(String key, String spacer, byte[] value) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile with comments added<br>
      * Separates elements with specified spacer
      * 
      * @param key
      *            the key for the property
      * @param spacer
      *            the character(s) to space the elements with
      * @param value
      *            the property to be stored
      * @param comment
      *            the comments to add
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if specified spacer is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setByteArray(String key, String spacer, byte[] value, String... comment) throws UtilityException;
 
     /**
      * Gets a short associated with specified key
      * 
      * @param key
      *            the key to get the property for
      * @return short associated with the property
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if the property is not a number<br>
      *             or if property is not found
      */
     protected abstract short getShort(String key) throws UtilityException;
 
     /**
      * Gets a short associated with specified key or returns the default specified<br>
      * NOTE: This will not save the properties file, it will add the key and value to the map for later saving.
      * 
      * @param key
      *            the key to get the property for
      * @param def
      *            the default value to use if key is not found
      * @return short associated with the property
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if the property is not a number<br>
      *             or if property is not found
      */
     protected abstract short getShort(String key, short def) throws UtilityException;
 
     /**
      * Sets a short as a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the short value to be stored
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty
      */
     protected abstract void setShort(String key, short value) throws UtilityException;
 
     /**
      * Sets a short as a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the short value to be stored
      * @param comment
      *            the comments to add
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty
      */
     protected abstract void setShort(String key, short value, String... comment) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a short Array<br>
      * Separates at commas ',' and trims extra whitespace from the new elements
      * 
      * @param key
      *            the key to get the property for
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      *             or if property was not found
      */
     protected abstract short[] getShortArray(String key) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a short Array or returns the default specified<br>
      * Separates at commas ',' and trims extra whitespace from the new elements<br>
      * NOTE: This will not save the properties file, it will add the key and value to the map for later saving.
      * 
      * @param key
      *            the key to get the property for
      * @param def
      *            the default value to use if key is not found
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      */
     protected abstract short[] getShortArray(String key, short[] def) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the property to be stored (elements combined using a comma as a spacer)
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setShortArray(String key, short[] value) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile with comments added
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the property to be stored
      * @param comment
      *            the comments to add
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setShortArray(String key, short[] value, String... comment) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a short array<br>
      * Separates at specified character(s) and trims extra whitespace from the new elements
      * 
      * @param key
      *            the key to get the property for
      * @param splitBy
      *            the character(s) to split the property value with
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      *             or if specified splitter is null or empty<br>
      *             or if property was not found
      */
     protected abstract short[] getShortArray(String key, String splitBy) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a short array or returns the default specified<br>
      * Separates at commas ',' and trims extra whitespace from the new elements<br>
      * NOTE: This will not save the properties file, it will add the key and value to the map for later saving.
      * 
      * @param key
      *            the key to get the property for
      * @param splitBy
      *            the character(s) to split the property value with
      * @param def
      *            the default value to use if key is not found
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      *             or if specified splitter is null or empty
      */
     protected abstract short[] getShortArray(String key, String splitBy, short[] def) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param spacer
      *            the character(s) to space the elements with
      * @param value
      *            the property to be stored (elements combined using a comma as a spacer)
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if specified spacer is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setShortArray(String key, String spacer, short[] value) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile with comments added
      * 
      * @param key
      *            the key for the property
      * @param spacer
      *            the character(s) to space the elements with
      * @param value
      *            the property to be stored
      * @param comment
      *            the comments to add
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if specified spacer is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setShortArray(String key, String spacer, short[] value, String... comment) throws UtilityException;
 
     /**
      * Gets an integer associated with specified key
      * 
      * @param key
      *            the key to get the property for
      * @return integer associated with the property
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if the property is not a number<br>
      *             or if property is not found
      */
     protected abstract int getInt(String key) throws UtilityException;
 
     /**
      * Gets an integer associated with specified key or returns the default specified<br>
      * NOTE: This will not save the properties file, it will add the key and value to the map for later saving.
      * 
      * @param key
      *            the key to get the property for
      * @param def
      *            the default value to use if key is not found
      * @return integer associated with the property
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if the property is not a number
      */
     protected abstract int getInt(String key, int def) throws UtilityException;
 
     /**
      * Sets an integer as a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the integer value to be stored
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty
      */
     protected abstract void setInt(String key, int value) throws UtilityException;
 
     /**
      * Sets an integer as a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the integer value to be stored
      * @param comment
      *            the comments to add
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty
      */
     protected abstract void setInt(String key, int value, String... comment) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a integer Array<br>
      * Separates at commas ',' and trims extra whitespace from the new elements
      * 
      * @param key
      *            the key to get the property for
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      *             or if property was not found
      */
     protected abstract int[] getIntArray(String key) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a integer Array or returns the default specified<br>
      * Separates at commas ',' and trims extra whitespace from the new elements<br>
      * NOTE: This will not save the properties file, it will add the key and value to the map for later saving.
      * 
      * @param key
      *            the key to get the property for
      * @param def
      *            the default value to use if key is not found
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty
      */
     protected abstract int[] getIntArray(String key, int[] def) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the property to be stored (elements combined using a comma as a spacer)
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setIntArray(String key, int[] value) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile with comments added
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the property to be stored
      * @param comment
      *            the comments to add
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setIntArray(String key, int[] value, String... comment) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a integer array<br>
      * Separates at specified character(s) and trims extra whitespace from the new elements
      * 
      * @param key
      *            the key to get the property for
      * @param splitBy
      *            the character(s) to split the property value with
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      *             or if specified splitter is null or empty<br>
      *             or if property was not found
      */
     protected abstract int[] getIntArray(String key, String splitBy) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a integer array or returns the default specified<br>
      * Separates at specified character(s) and trims extra whitespace from the new elements<br>
      * NOTE: This will not save the properties file, it will add the key and value to the map for later saving.
      * 
      * @param key
      *            the key to get the property for
      * @param splitBy
      *            the character(s) to split the property value with
      * @param def
      *            the default value to use if key is not found
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      *             or if specified splitter is null or empty
      */
     protected abstract int[] getIntArray(String key, String splitBy, int[] def) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param spacer
      *            the character(s) to space the elements with
      * @param value
      *            the property to be stored (elements combined using a comma as a spacer)
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if specified spacer is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setIntArray(String key, String spacer, int[] value) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile with comments added
      * 
      * @param key
      *            the key for the property
      * @param spacer
      *            the character(s) to space the elements with
      * @param value
      *            the property to be stored
      * @param comment
      *            the comments to add
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if specified spacer is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setIntArray(String key, String spacer, int[] value, String... comment) throws UtilityException;
 
     /**
      * Gets a long associated with specified key
      * 
      * @param key
      *            the key to get the property for
      * @return long associated with the property
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if the property is not a number<br>
      *             or if property is not found
      */
     protected abstract long getLong(String key) throws UtilityException;
 
     /**
      * Gets a long associated with specified key or returns the default specified<br>
      * NOTE: This will not save the properties file, it will add the key and value to the map for later saving.
      * 
      * @param key
      *            the key to get the property for
      * @param def
      *            the default value to use if key is not found
      * @return long associated with the property
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if the property is not a number
      */
     protected abstract long getLong(String key, long def) throws UtilityException;
 
     /**
      * Sets a long as a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the long value to be stored
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty
      */
     protected abstract void setLong(String key, long value) throws UtilityException;
 
     /**
      * Sets a long as a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the long value to be stored
      * @param comment
      *            the comments to add
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty
      */
     protected abstract void setLong(String key, long value, String... comment) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a long Array<br>
      * Separates at commas ',' and trims extra whitespace from the new elements
      * 
      * @param key
      *            the key to get the property for
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      *             or if property was not found
      */
     protected abstract long[] getLongArray(String key) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a long Array or returns the default specified<br>
      * Separates at commas ',' and trims extra whitespace from the new elements<br>
      * NOTE: This will not save the properties file, it will add the key and value to the map for later saving.
      * 
      * @param key
      *            the key to get the property for
      * @param def
      *            the default value to use if key is not found
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      *             or if property was not found
      */
     protected abstract long[] getLongArray(String key, long[] def) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the property to be stored (elements combined using a comma as a spacer)
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setLongArray(String key, long[] value) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile with comments added
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the property to be stored
      * @param comment
      *            the comments to add
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setLongArray(String key, long[] value, String... comment) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a long array<br>
      * Separates at specified character(s) and trims extra whitespace from the new elements
      * 
      * @param key
      *            the key to get the property for
      * @param splitBy
      *            the character(s) to split the property value with
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      *             or if specified splitter is null or empty<br>
      *             or if property was not found
      */
     protected abstract long[] getLongArray(String key, String splitBy) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a long array or returns the default specified<br>
      * Separates at specified character(s) and trims extra whitespace from the new elements<br>
      * NOTE: This will not save the properties file, it will add the key and value to the map for later saving.
      * 
      * @param key
      *            the key to get the property for
      * @param splitBy
      *            the character(s) to split the property value with
      * @param def
      *            the default value to use if key is not found
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      *             or if specified splitter is null or empty<br>
      *             or if property was not found
      */
     protected abstract long[] getLongArray(String key, String splitBy, long[] def) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param spacer
      *            the character(s) to space the elements with
      * @param value
      *            the property to be stored (elements combined using a comma as a spacer)
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if specified spacer is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setLongArray(String key, String spacer, long[] value) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile with comments added
      * 
      * @param key
      *            the key for the property
      * @param spacer
      *            the character(s) to space the elements with
      * @param value
      *            the property to be stored
      * @param comment
      *            the comments to add
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if specified spacer is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setLongArray(String key, String spacer, long[] value, String... comment) throws UtilityException;
 
     /**
      * Gets a float associated with specified key
      * 
      * @param key
      *            the key to get the property for
      * @return float associated with the property
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if the property is not a number<br>
      *             or if property is not found
      */
     protected abstract float getFloat(String key) throws UtilityException;
 
     /**
      * Gets a float associated with specified key or returns the default specified<br>
      * NOTE: This will not save the properties file, it will add the key and value to the map for later saving.
      * 
      * @param key
      *            the key to get the property for
      * @param def
      *            the default value to use if key is not found
      * @return float associated with the property
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if the property is not a number
      */
     protected abstract float getFloat(String key, float def) throws UtilityException;
 
     /**
      * Sets a float as a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the float value to be stored
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty
      */
     protected abstract void setFloat(String key, float value) throws UtilityException;
 
     /**
      * Sets a float as a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the float value to be stored
      * @param comment
      *            the comments to add
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty
      */
     protected abstract void setFloat(String key, float value, String... comment) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a float Array<br>
      * Separates at commas ',' and trims extra whitespace from the new elements
      * 
      * @param key
      *            the key to get the property for
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      *             or if property was not found
      */
     protected abstract float[] getFloatArray(String key) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a float Array or returns the default specified<br>
      * Separates at commas ',' and trims extra whitespace from the new elements<br>
      * NOTE: This will not save the properties file, it will add the key and value to the map for later saving.
      * 
      * @param key
      *            the key to get the property for
      * @param def
      *            the default value to use if key is not found
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      */
     protected abstract float[] getFloatArray(String key, float[] def) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the property to be stored (elements combined using a comma as a spacer)
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setFloatArray(String key, float[] value) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile with comments added
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the property to be stored
      * @param comment
      *            the comments to add
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setFloatArray(String key, float[] value, String... comment) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a float array<br>
      * Separates at specified character(s) and trims extra whitespace from the new elements
      * 
      * @param key
      *            the key to get the property for
      * @param splitBy
      *            the character(s) to split the property value with
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      *             or if specified splitter is null or empty<br>
      *             or if property was not found
      */
     protected abstract float[] getFloatArray(String key, String splitBy) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a float array or returns the default specified<br>
      * Separates at specified character(s) and trims extra whitespace from the new elements<br>
      * NOTE: This will not save the properties file, it will add the key and value to the map for later saving.
      * 
      * @param key
      *            the key to get the property for
      * @param splitBy
      *            the character(s) to split the property value with
      * @param def
      *            the default value to use if key is not found
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      *             or if specified splitter is null or empty<br>
      *             or if property was not found
      */
     protected abstract float[] getFloatArray(String key, String splitBy, float[] def) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param spacer
      *            the character(s) to space the elements with
      * @param value
      *            the property to be stored (elements combined using a comma as a spacer)
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if specified spacer is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setFloatArray(String key, String spacer, float[] value) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile with comments added
      * 
      * @param key
      *            the key for the property
      * @param spacer
      *            the character(s) to space the elements with
      * @param value
      *            the property to be stored
      * @param comment
      *            the comments to add
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if specified spacer is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setFloatArray(String key, String spacer, float[] value, String... comment) throws UtilityException;
 
     /**
      * Gets a double associated with specified key
      * 
      * @param key
      *            the key to get the property for
      * @return double associated with the property
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if the property is not a number<br>
      *             or if property is not found
      */
     protected abstract double getDouble(String key) throws UtilityException;
 
     /**
      * Gets a double associated with specified key or returns the default specified<br>
      * NOTE: This will not save the properties file, it will add the key and value to the map for later saving.
      * 
      * @param key
      *            the key to get the property for
      * @param def
      *            the default value to use if key is not found
      * @return double associated with the property
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if the property is not a number
      */
     protected abstract double getDouble(String key, double def) throws UtilityException;
 
     /**
      * Sets a double as a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the double value to be stored
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty
      */
     protected abstract void setDouble(String key, double value) throws UtilityException;
 
     /**
      * Sets a double as a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the double value to be stored
      * @param comment
      *            the comments to add
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty
      */
     protected abstract void setDouble(String key, double value, String... comment) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a double Array<br>
      * Separates at commas ',' and trims extra whitespace from the new elements
      * 
      * @param key
      *            the key to get the property for
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      *             or if property was not found
      */
     protected abstract double[] getDoubleArray(String key) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a double Array or returns the default specified<br>
      * Separates at commas ',' and trims extra whitespace from the new elements<br>
      * NOTE: This will not save the properties file, it will add the key and value to the map for later saving.
      * 
      * @param key
      *            the key to get the property for
      * @param def
      *            the default value to use if key is not found
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      */
     protected abstract double[] getDoubleArray(String key, double[] def) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the property to be stored (elements combined using a comma as a spacer)
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setDoubleArray(String key, double[] value) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile with comments added
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the property to be stored
      * @param comment
      *            the comments to add
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setDoubleArray(String key, double[] value, String... comment) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a double array<br>
      * Separates at specified character(s) and trims extra whitespace from the new elements
      * 
      * @param key
      *            the key to get the property for
      * @param splitBy
      *            the character(s) to split the property value with
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      *             or if specified splitter is null or empty<br>
      *             or if property was not found
      */
     protected abstract double[] getDoubleArray(String key, String splitBy) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a double array or returns the default specified<br>
      * Separates at specified character(s) and trims extra whitespace from the new elements<br>
      * NOTE: This will not save the properties file, it will add the key and value to the map for later saving.
      * 
      * @param key
      *            the key to get the property for
      * @param splitBy
      *            the character(s) to split the property value with
      * @param def
      *            the default value to use if key is not found
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      *             or if specified splitter is null or empty
      */
     protected abstract double[] getDoubleArray(String key, String splitBy, double[] def) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param spacer
      *            the character(s) to space the elements with
      * @param value
      *            the property to be stored (elements combined using a comma as a spacer)
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if specified spacer is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setDoubleArray(String key, String spacer, double[] value) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile with comments added
      * 
      * @param key
      *            the key for the property
      * @param spacer
      *            the character(s) to space the elements with
      * @param value
      *            the property to be stored
      * @param comment
      *            the comments to add
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if specified spacer is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setDoubleArray(String key, String spacer, double[] value, String... comment) throws UtilityException;
 
     /**
      * Gets a boolean associated with specified key
      * 
      * @param key
      *            the key to get the property for
      * @return boolean associated with the property
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if property is not found
      * @see BooleanUtils#parseBoolean(String)
      */
     protected abstract boolean getBoolean(String key) throws UtilityException;
 
     /**
      * Gets a boolean associated with specified key
      * 
      * @param key
      *            the key to get the property for
      * @param def
      *            the default value to use if key is not found
      * @return boolean associated with the property
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if property is not found
      * @see BooleanUtils#parseBoolean(String)
      */
     protected abstract boolean getBoolean(String key, boolean def) throws UtilityException;
 
     /**
      * Sets a boolean as a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the boolean value to be stored
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty
      */
     protected abstract void setBoolean(String key, boolean value) throws UtilityException;
 
     /**
      * Sets a boolean as a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the double value to be stored
      * @param comment
      *            the comments to add
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty
      */
     protected abstract void setBoolean(String key, boolean value, String... comment) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a boolean Array<br>
      * Separates at commas ',' and trims extra whitespace from the new elements
      * 
      * @param key
      *            the key to get the property for
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      *             or if property was not found
      */
     protected abstract boolean[] getBooleanArray(String key) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a boolean Array or returns the default specified<br>
      * Separates at commas ',' and trims extra whitespace from the new elements<br>
      * NOTE: This will not save the properties file, it will add the key and value to the map for later saving.
      * 
      * @param key
      *            the key to get the property for
      * @param def
      *            the default value to use if key is not found
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      */
     protected abstract boolean[] getBooleanArray(String key, boolean[] def) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the property to be stored (elements combined using a comma as a spacer)
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setBooleanArray(String key, boolean[] value) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile with comments added
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the property to be stored
      * @param comment
      *            the comments to add
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setBooleanArray(String key, boolean[] value, String... comment) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a boolean array<br>
      * Separates at specified character(s) and trims extra whitespace from the new elements
      * 
      * @param key
      *            the key to get the property for
      * @param splitBy
      *            the character(s) to split the property value with
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      *             or if specified splitter is null or empty<br>
      *             or if property was not found
      */
     protected abstract boolean[] getBooleanArray(String key, String splitBy) throws UtilityException;
 
     /**
      * Gets the property associated to the key as a boolean array or returns the default specified<br>
      * Separates at specified character(s) and trims extra whitespace from the new elements<br>
      * NOTE: This will not save the properties file, it will add the key and value to the map for later saving.
      * 
      * @param key
      *            the key to get the property for
      * @param splitBy
      *            the character(s) to split the property value with
      * @param def
      *            the default value to use if key is not found
      * @return the property associated with the key if found
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty <br>
      *             or if specified splitter is null or empty
      */
     protected abstract boolean[] getBooleanArray(String key, String splitBy, boolean[] def) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param spacer
      *            the character(s) to space the elements with
      * @param value
      *            the property to be stored (elements combined using a comma as a spacer)
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if specified spacer is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setBooleanArray(String key, String spacer, boolean[] value) throws UtilityException;
 
     /**
      * Sets a property to be saved to the PropertiesFile with comments added
      * 
      * @param key
      *            the key for the property
      * @param spacer
      *            the character(s) to space the elements with
      * @param value
      *            the property to be stored
      * @param comment
      *            the comments to add
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if specified spacer is null or empty<br>
      *             or if value is null or empty
      */
     protected abstract void setBooleanArray(String key, String spacer, boolean[] value, String... comment) throws UtilityException;
 
     /**
      * Gets a character associated with specified key
      * 
      * @param key
      *            the key to get the property for
      * @return character associated with the property
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      *             or if property is not found
      */
     protected abstract char getCharacter(String key) throws UtilityException;
 
     /**
      * Gets a character associated with specified key
      * 
      * @param key
      *            the key to get the property for
      * @param def
      *            the default value to use if key is not found
      * @return character associated with the property
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty<br>
      */
     protected abstract char getCharacter(String key, char def) throws UtilityException;
 
     /**
      * Sets a character as a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param ch
      *            the boolean value to be stored
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty
      */
     protected abstract void setCharacter(String key, char ch) throws UtilityException;
 
     /**
      * Sets a character as a property to be saved to the PropertiesFile
      * 
      * @param key
      *            the key for the property
      * @param ch
      *            the double value to be stored
      * @param comment
      *            the comments to add
      * @throws UtilityException
      * <br>
      *             if specified key is null or empty
      */
     protected abstract void setCharacter(String key, char ch, String... comment) throws UtilityException;
 
     /**
      * Gets an unmodifiableMap of all keys and properties as Strings
      * 
      * @return unmodifiable properties map
      */
     protected abstract Map<String, String> getPropertiesMap();
 
     /**
      * Method for adding comments to keys
      * 
      * @param key
      *            the key to add comments for
      * @param comment
      *            the comment(s) to be added
      */
     protected abstract void addComment(String key, String... comment);
 
     /**
      * Sets the comments for a given property.<br>
      * NOTE: If comment is null or length of 0, this will essentially remove all comments.
      * 
      * @param key
      *            the key to the property to set comments for
      * @param comment
      *            the comment(s) to set
      */
     protected abstract void setComments(String key, String... comment);
 
     /**
      * Gets all the comments attached to the property key
      * 
      * @param key
      *            the property key
      * @return comments if found, {@code null} if no comments found
      */
     protected abstract String[] getComments(String key);
 
     /**
      * Gets all the comments attached to the property key
      * 
      * @param key
      *            the property key
      * @return comments if found; {@code null} if no comments found
      */
     protected abstract List<String> getCommentsAsList(String key);
 
     /**
      * Removes a comment from a property key
      * 
      * @param key
      *            the property key
      * @param comment
      *            the comment to be removed
      */
     protected abstract void removeComment(String key, String comment);
 
     /**
      * Removes all the comments from a property key
      * 
      * @param key
      *            the property key to remove comments for
      */
     protected abstract void removeAllCommentsFromKey(String key);
 
     /**
      * Removes all the comments from all the properties in the file
      */
     protected abstract void removeAllCommentsFromFile();
 
     /**
      * Adds lines to the Header of the PropertiesFile
      * 
      * @param lines
      *            the lines to be added
      */
     protected abstract void addHeaderLines(String... lines);
 
     /**
      * Gets the lines of the Header
      * 
      * @return the header lines
      */
     protected abstract LinkedList<String> getHeaderLines();
 
     /**
      * Clears the header
      */
     protected abstract void clearHeader();
 
     /**
      * Adds lines to the Footer of the PropertiesFile
      * 
      * @param lines
      *            the lines to be added
      */
     protected abstract void addFooterLines(String... lines);
 
     /**
      * Gets the lines of the Header
      * 
      * @return the header lines
      */
     protected abstract LinkedList<String> getFooterLines();
 
     /**
      * Clears the footer
      */
     protected abstract void clearFooter();
 
     /**
      * Gets the InLine comment for a property
      * 
      * @param key
      *            the property key to get inline comment for
      * @return the inline comment or {@code null} if no comment
      */
     protected abstract String getInlineComment(String key);
 }
