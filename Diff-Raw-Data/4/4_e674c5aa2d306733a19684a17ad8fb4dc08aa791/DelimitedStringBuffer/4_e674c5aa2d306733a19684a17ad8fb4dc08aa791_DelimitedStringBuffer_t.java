 package org.yajul.util;
 
 /**
  * Helps build delimited strings.  Clients speicify a delimiter and then repeatedly call append(str). 
  */
 public class DelimitedStringBuffer {
 	
 	private String m_delimiter;
 	private StringBuffer m_buffer;
     private boolean m_insertOnNull;
 
     /** Creates a DelimitedStringBuffer with no delimiter. */
 	public DelimitedStringBuffer() {
 	}
 
     /** Creates a DelimitedStringBuffer with no delimiter. */
 	public DelimitedStringBuffer(boolean insertOnNull) {
         m_insertOnNull = insertOnNull;
 	}
 
     /** Creates a DelimitedStringBuffer with the given delimiter. */
     public DelimitedStringBuffer(String delimiter) {
 		m_delimiter = delimiter;
 	}
 
     /** Creates a DelimitedStringBuffer with the given delimiter. */
     public DelimitedStringBuffer(String delimiter, boolean insertOnNull) {
 		m_delimiter = delimiter;
         m_insertOnNull = insertOnNull;
 	}
 
     /** appends 'str' **/
 	public DelimitedStringBuffer append(String str) {
         return insert(size(), str, m_delimiter);
     }
 
     /** appends 'str' **/
 	public DelimitedStringBuffer append(String str, String delim) {
         return insert(size(), str, delim);
 	}
     
     /* prepends 'str' */
     public DelimitedStringBuffer prepend(String str) {
         return insert(0, str, m_delimiter);
     }
 
     /* prepends 'str' */
     public DelimitedStringBuffer prepend(String str, String delim) {
         return insert(0, str, delim);
     }
 
     /*
     * inserts 'str'.
      */
     protected DelimitedStringBuffer insert(int position, String str, String delim) {
 		if (str != null || m_insertOnNull) {
 			if (m_buffer != null) {
                 if (delim != null) {
                     m_buffer.insert(position, delim);
                     if (position != 0) {
                          position += delim.length();
                     }
                 }
             }
             else {
                 m_buffer = new StringBuffer();
             }
             if (str != null) {
     			m_buffer.insert(position, str);
             }
 		}
 		return this;
     }
 
     /** Changes the delimiter to the one provided */
     public void setDelimiter(String delimiter) {
         m_delimiter = delimiter;
     }
 
     /**
 	 * Returns the delimited string
 	 **/
 	public String toString() {
         if (m_buffer == null)
             return "";
         else
     		return m_buffer.toString();
 	}
     
     /**
      * return the size of the text currently represented by this string buffer
      */
     public int size() {
         return m_buffer == null ? 0 : m_buffer.length();
     }
 }
