 package org.romppu.translit.dictionary.impl;
 
 
 import org.romppu.translit.dictionary.TranslitDictionary;
 import org.romppu.translit.profile.TranslitProfile;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.transform.TransformerException;
 import java.io.*;
 import java.nio.charset.Charset;
 import java.util.*;
 
 /**
  * Main goal of <code>XmlTranslitDictionary</code> is an implementing of {@link org.romppu.translit.dictionary.TranslitDictionary}
  * interface which is used by {@link org.romppu.translit.document.TranslitDocument}
  * The XmlTranslitDictionary deals with xml file which is represented by {@link org.romppu.translit.profile.TranslitProfile}
  */
 public class XmlTranslitDictionary implements TranslitDictionary {
 
     private TranslitProfile translitProfile;
     private String documentPath;
     private Map<Side, Integer> longestWordLen = new Hashtable<Side, Integer>();
 
     public XmlTranslitDictionary()  {
         longestWordLen.put(Side.LEFT, 0);
         longestWordLen.put(Side.RIGHT, 0);
     }
 
     /**
      * Constructs new instance of XmlTranslitDictionary with the specified {@see documentPath} and
      * invokes {@link #load()}
      *
      * @param documentPath
      * @throws JAXBException
      */
     public XmlTranslitDictionary(String documentPath) throws Exception {
         this();
         setDocumentPath(documentPath);
         load();
     }
 
     /**
      * Returns {@see documentPath} property value.
      * The {@see documentPath} property preserve value of path to xml file.
      *
      * @return {@see documentPath} property value
      */
     public String getDocumentPath() {
         return documentPath;
     }
 
     /**
      * Sets value of {@see documentPath} property
      *
      * @param documentPath
      */
     public void setDocumentPath(String documentPath) {
         this.documentPath = documentPath;
     }
 
     /**
      * Loads dictionary data from xml file
      *
      * @throws Exception
      */
     public void load() throws Exception {
         System.out.println("Loading dictionary from " + getDocumentPath());
         InputStream stream = getClass().getResourceAsStream(getDocumentPath());
         if (stream == null) {
             stream = new FileInputStream(getDocumentPath());
         }
         load(stream);
         System.out.println("Dictionary version is " + translitProfile.getVersion() + ", done.");
     }
 
     /**
      * Saves dictionary data to xml file
      *
      * @throws IOException
      * @throws TransformerException
      */
     public void save() throws Exception {
         System.out.println("Saving dictionary " + getDocumentPath());
         save(new FileOutputStream(getDocumentPath()));
     }
 
     /**
      * Finds pairs on the specified <code>side</code> by the specified <code>value</code>
      *
      * @param side
      * @param value
      * @return
      */
     public List<String> findOpposites(String value, Side side) {
         ArrayList<String> toReturn = new ArrayList<String>();
         for (TranslitProfile.Pair pair : translitProfile.getPair()) {
             if (side == Side.LEFT && value.equals(pair.getLeft())) {
                toReturn.add(value);
            } else if (side == Side.RIGHT && value.equals(pair.getRight())) {
                toReturn.add(value);
             }
         }
         return toReturn;
     }
 
     @Override
     public int indexOf(String string, Side side) {
         for (int i = 0; i < translitProfile.getPair().size(); i++) {
             String value = getValueAt(i, side);
             if (value.equals(string)) return i;
         }
         return -1;
     }
 
     /**
      * Returns value at the specified position from the specified side
      *
      * @param idx
      * @param side
      * @return
      */
     public String getValueAt(int idx, Side side) {
         TranslitProfile.Pair pair = translitProfile.getPair().get(idx);
         return side == Side.LEFT ? pair.getLeft() : pair.getRight();
     }
 
     /**
      * Returns number of pairs
      *
      * @return number of pairs
      */
     public int getSize() {
         return translitProfile.getPair().size();
     }
 
     @Override
     public int getLongestWordLen(Side side) {
         return longestWordLen.get(side);
     }
 
     @Override
     public String getDescription() {
         return translitProfile.getName();
     }
 
     @Override
     public void setDescription(String description) {
         translitProfile.setName(description);
     }
 
     /**
      * Adds new pair to profile
      *
      * @param left
      * @param right
      */
     public void addPair(String left, String right) {
         TranslitProfile.Pair pair = new TranslitProfile.Pair();
         pair.setLeft(left);
         pair.setRight(right);
         translitProfile.getPair().add(pair);
         updateLongestWordLen();
     }
 
     @Override
     public void removeAt(int idx) {
         translitProfile.getPair().remove(idx);
     }
 
     @Override
     public List<String> getOppositeList(String value, Side side) {
         return findOpposites(value, side);
     }
 
     /**
      * Wrapper of {@link org.romppu.translit.profile.TranslitProfile#getVersion()}
      *
      * @return {@link org.romppu.translit.profile.TranslitProfile#version} property value
      */
     public String getVersion() {
         if (translitProfile == null) return null;
         return translitProfile.getVersion();
     }
 
     @Override
     public String getInitialParam() {
         return getDocumentPath();
     }
 
     @Override
     public void save(OutputStream stream) throws Exception {
         JAXBContext jc = JAXBContext.newInstance(TranslitProfile.class.getPackage().getName());
         Marshaller m = jc.createMarshaller();
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
         m.marshal(translitProfile, new OutputStreamWriter(stream, Charset.forName("UTF8")));
     }
 
     @Override
     public void load(InputStream stream) throws Exception {
         JAXBContext jc = JAXBContext.newInstance(TranslitProfile.class.getPackage().getName());
         Unmarshaller u = jc.createUnmarshaller();
         translitProfile = (TranslitProfile) u.unmarshal(stream);
         updateLongestWordLen();
     }
 
     @Override
     public String getFilenameExtension() {
         return ".xml";
     }
 
     @Override
     public String getExclusionMarker(ExclusionMarker exclusionMarker) {
         switch (exclusionMarker) {
             case START: return translitProfile.getExclusionMarkerStart();
             case END: return translitProfile.getExclusionMarkerEnd();
             default: throw new RuntimeException("Invalid exclusionMarker " + exclusionMarker);
         }
     }
 
     /**
      * Wrapper of {@link TranslitProfile#setVersion(String)}
      *
      * @param newValue
      */
     public void setVersion(String newValue) {
         translitProfile.setVersion(newValue);
     }
 
     private void updateLongestWordLen() {
         for (TranslitProfile.Pair pair: translitProfile.getPair()) {
             if (pair.getLeft().length() > longestWordLen.get(Side.LEFT))
                 longestWordLen.put(Side.LEFT, pair.getLeft().length());
             if (pair.getRight().length() > longestWordLen.get(Side.RIGHT))
                 longestWordLen.put(Side.RIGHT, pair.getRight().length());
         }
     }
 
 }
