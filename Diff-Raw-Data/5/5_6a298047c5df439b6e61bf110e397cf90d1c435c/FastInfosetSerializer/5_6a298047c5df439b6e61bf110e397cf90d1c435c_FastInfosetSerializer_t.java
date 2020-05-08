 /*
  * Fast Infoset ver. 0.1 software ("Software")
  * 
  * Copyright, 2004-2005 Sun Microsystems, Inc. All Rights Reserved. 
  * 
  * Software is licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License. You may
  * obtain a copy of the License at:
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  *    Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations.
  * 
  *    Sun supports and benefits from the global community of open source
  * developers, and thanks the community for its important contributions and
  * open standards-based technology, which Sun has adopted into many of its
  * products.
  * 
  *    Please note that portions of Software may be provided with notices and
  * open source licenses from such communities and third parties that govern the
  * use of those portions, and any licenses granted hereunder do not alter any
  * rights and obligations you may have under such open source licenses,
  * however, the disclaimer of warranty and limitation of liability provisions
  * in this License will apply to all Software in this distribution.
  * 
  *    You acknowledge that the Software is not designed, licensed or intended
  * for use in the design, construction, operation or maintenance of any nuclear
  * facility.
  *
  * Apache License
  * Version 2.0, January 2004
  * http://www.apache.org/licenses/
  *
  */
 package org.jvnet.fastinfoset;
 
 import java.io.OutputStream;
 import java.util.Map;
 
 /**
  * A general interface for serializers of fast infoset documents.
  *
  * <p>
  * This interface contains common methods that are not specific to any
  * API associated with the serialization of XML Infoset to fast infoset
  * documents.
  * 
  * @author Paul.Sandoz@Sun.Com
  */
 public interface FastInfosetSerializer {
     /**
      * The feature to ignore the document type declaration and the 
      * internal subset.
      * <p>
      * The default value is false. If true a serializer shall ignore document
      * type declaration and the internal subset.
      */
     public static final String IGNORE_DTD_FEATURE = 
         "http://jvnet.org/fastinfoset/serializer/feature/ignore/DTD";
     
     /**
      * The feature to ignore comments.
      * <p>
      * The default value is false. If true a serializer shall ignore comments
      * and shall not serialize them.
      */
     public static final String IGNORE_COMMENTS_FEATURE = 
         "http://jvnet.org/fastinfoset/serializer/feature/ignore/comments";
 
     /**
      * The feature to ignore processing instructions.
      * <p>
      * The default value is false. If true a serializer shall ignore processing
      * instructions and shall not serialize them.
      */
     public static final String IGNORE_PROCESSING_INSTRUCTIONS_FEATURE = 
         "http://jvnet.org/fastinfoset/serializer/feature/ignore/processingInstructions";
     
     /**
      * The feature to ignore text content that consists completely of white
      * space characters.
      * <p>
      * The default value is false. If true a serializer shall ignore text
      * content that consists completely of white space characters.
      */
     public static final String IGNORE_WHITE_SPACE_TEXT_CONTENT_FEATURE = 
         "http://jvnet.org/fastinfoset/serializer/feature/ignore/whiteSpaceTextContent";
     
     /**
      * The property name to be used for getting and setting the buffer size
      * of a parser.
      */
     public static final String BUFFER_SIZE_PROPERTY = 
         "http://jvnet.org/fastinfoset/parser/properties/buffer-size";
 
     /**
      * The property name to be used for getting and setting the 
      * Map containing encoding algorithms.
      *
      */    
     public static final String REGISTERED_ENCODING_ALGORITHMS_PROPERTY =
         "http://jvnet.org/fastinfoset/parser/properties/registered-encoding-algorithms";
     
    /**
      * The property name to be used for getting and setting the 
      * Map containing external vocabularies.
      *
      */    
     public static final String EXTERNAL_VOCABULARIES_PROPERTY =
         "http://jvnet.org/fastinfoset/parser/properties/external-vocabularies";
     
     /**
      * The default value for the limit on the size of character content chunks
      * that will be indexed.
      */
    public final static int CHARACTER_CONTENT_CHUNK_SIZE_CONSTRAINT = 32;
     
     /**
      * The default value for limit on the size of indexed Map for attribute values
      * Limit is measured in bytes not in number of entries
      */
     public static final int CHARACTER_CONTENT_CHUNK_MAP_MEMORY_CONSTRAINT = Integer.MAX_VALUE;
 
     /**
      * The default value for the limit on the size of attribute values
      * that will be indexed.
      */
    public final static int ATTRIBUTE_VALUE_SIZE_CONSTRAINT = 32;
     
     /**
      * The default value for limit on the size of indexed Map for character content chunks
      * Limit is measured in bytes not in number of entries
      */
     public static final int ATTRIBUTE_VALUE_MAP_MEMORY_CONSTRAINT = Integer.MAX_VALUE;
     
     /**
      * The character encoding scheme string for UTF-8.
      */
     public static final String UTF_8 = "UTF-8";
     
     /**
      * The character encoding scheme string for UTF-16BE.
      */
     public static final String UTF_16BE = "UTF-16BE";
 
     /**
      * Set the {@link #IGNORE_DTD_FEATURE}.
      * @param ignoreDTD true if the feature shall be ignored.
      */
     public void setIgnoreDTD(boolean ignoreDTD);
     
     /**
      * Get the {@link #IGNORE_DTD_FEATURE}.
      * @return true if the feature is ignored, false otherwise.
      */
     public boolean getIgnoreDTD();
     
     /**
      * Set the {@link #IGNORE_COMMENTS_FEATURE}.
      * @param ignoreComments true if the feature shall be ignored.
      */
     public void setIgnoreComments(boolean ignoreComments);
     
     /**
      * Get the {@link #IGNORE_COMMENTS_FEATURE}.
      * @return true if the feature is ignored, false otherwise.
      */
     public boolean getIgnoreComments();
     
     /**
      * Set the {@link #IGNORE_PROCESSING_INSTRUCTIONS_FEATURE}.
      * @param ignoreProcesingInstructions true if the feature shall be ignored.
      */
     public void setIgnoreProcesingInstructions(boolean ignoreProcesingInstructions);
     
     /**
      * Get the {@link #IGNORE_PROCESSING_INSTRUCTIONS_FEATURE}.
      * @return true if the feature is ignored, false otherwise.
      */
     public boolean getIgnoreProcesingInstructions();
     
     /**
      * Set the {@link #IGNORE_WHITE_SPACE_TEXT_CONTENT_FEATURE}.
      * @param ignoreWhiteSpaceTextContent true if the feature shall be ignored.
      */
     public void setIgnoreWhiteSpaceTextContent(boolean ignoreWhiteSpaceTextContent);
     
     /**
      * Get the {@link #IGNORE_WHITE_SPACE_TEXT_CONTENT_FEATURE}.
      * @return true if the feature is ignored, false otherwise.
      */
     public boolean getIgnoreWhiteSpaceTextContent();
     
     /**
      * Sets the character encoding scheme.
      *
      * The character encoding can be either UTF-8 or UTF-16BE for the
      * the encoding of chunks of CIIs, the [normalized value]
      * property of attribute information items, comment information
      * items and processing instruction information items.
      *
      * @param characterEncodingScheme The set of registered algorithms.
      */
     public void setCharacterEncodingScheme(String characterEncodingScheme);
     
     /**
      * Gets the character encoding scheme.
      *
      * @return The character encoding scheme.
      */
     public String getCharacterEncodingScheme();
     
     /**
      * Sets the set of registered encoding algorithms.
      *
      * @param algorithms The set of registered algorithms.
      */
     public void setRegisteredEncodingAlgorithms(Map algorithms);
     
     /**
      * Gets the set of registered encoding algorithms.
      *
      * @return The set of registered algorithms.
      */
     public Map getRegisteredEncodingAlgorithms();
     
     /**
      * Sets the limit on the size of character content chunks
      * that will be indexed.
      *
      * @param size The character content chunk size limit. Any chunk less
      * that a length of size limit will be indexed.
      */
     public void setCharacterContentChunkSizeLimit(int size);
     
     /**
      * Gets the limit on the size of character content chunks
      * that will be indexed.
      *
      * @return The character content chunk size limit.
      */
     public int getCharacterContentChunkSizeLimit();
 
     /**
      * Sets the limit on the memory size of Map of attribute values
      * that will be indexed.
      *
      * @param size The attribute value size limit. Any value less
      * that a length of size limit will be indexed.
      */
     public void setCharacterContentChunkMapMemoryLimit(int size);
     
     /**
      * Gets the limit on the memory size of Map of attribute values
      * that will be indexed.
      *
      * @return The attribute value size limit.
      */
     public int getCharacterContentChunkMapMemoryLimit();
     
     /**
      * Sets the limit on the size of attribute values
      * that will be indexed.
      *
      * @param size The attribute value size limit. Any value less
      * that a length of size limit will be indexed.
      */
     public void setAttributeValueSizeLimit(int size);
     
     /**
      * Gets the limit on the size of attribute values
      * that will be indexed.
      *
      * @return The attribute value size limit.
      */
     public int getAttributeValueSizeLimit();
 
     /**
      * Sets the limit on the memory size of Map of attribute values
      * that will be indexed.
      *
      * @param size The attribute value size limit. Any value less
      * that a length of size limit will be indexed.
      */
     public void setAttributeValueMapMemoryLimit(int size);
     
     /**
      * Gets the limit on the memory size of Map of attribute values
      * that will be indexed.
      *
      * @return The attribute value size limit.
      */
     public int getAttributeValueMapMemoryLimit();
 
     /**
      * Set the external vocabulary that shall be used when serializing.
      * 
      * @param v the vocabulary. 
      */
     public void setExternalVocabulary(ExternalVocabulary v);
     
     /**
      * Set the application data to be associated with the serializer vocabulary.
      * 
      * @param data the application data. 
      */
     public void setVocabularyApplicationData(VocabularyApplicationData data);
     
     /**
      * Get the application data associated with the serializer vocabulary.
      * 
      * @return the application data. 
      */
     public VocabularyApplicationData getVocabularyApplicationData();
     
     /**
      * Reset the serializer for reuse serializing another XML infoset.
      */
     public void reset();
         
     /**
      * Set the OutputStream to serialize the XML infoset to a 
      * fast infoset document.
      *
      * @param s the OutputStream where the fast infoset document is written to.
      */
     public void setOutputStream(OutputStream s);
 }
