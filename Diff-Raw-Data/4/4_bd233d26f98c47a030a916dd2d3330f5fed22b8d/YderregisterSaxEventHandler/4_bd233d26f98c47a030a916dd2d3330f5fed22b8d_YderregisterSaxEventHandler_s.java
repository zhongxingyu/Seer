 /**
  * The MIT License
  *
  * Original work sponsored and donated by National Board of e-Health (NSI), Denmark
  * (http://www.nsi.dk)
  *
  * Copyright (C) 2011 National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is furnished to do
  * so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package dk.nsi.sdm4.yder.parser;
 
 import static java.lang.String.format;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 
 import dk.nsi.sdm4.core.persistence.recordpersister.*;
 import dk.nsi.sdm4.yder.recordspecs.YderregisterRecordSpecs;
 import org.apache.commons.codec.binary.Hex;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.joda.time.Instant;
 import org.springframework.dao.IncorrectResultSizeDataAccessException;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import dk.nsi.sdm4.core.parser.ParserException;
 
 public class YderregisterSaxEventHandler extends DefaultHandler {
     private static final Logger logger = Logger.getLogger(YderregisterSaxEventHandler.class);
     private static final String SUPPORTED_INTERFACE_VERSION = "S1040025";
     private static final String EXPECTED_RECIPIENT_ID = "F053";
 
     protected final DateFormat datoFormatter = new SimpleDateFormat("yyyyMMdd");
 
     private static final String ROOT_QNAME = "etds1040025XML";
     private static final String START_QNAME = "Start";
     private static final String END_QNAME = "Slut";
     private static final String YDER_QNAME = "Yder";
     private static final String PERSON_QNAME = "Person";
 
     private long yderRecordCount = 0;
     private long personRecordCount = 0;
 
     private String versionNumber = null;
 
     RecordFetcher fetcher;
 
     RecordPersister persister;
 
     public YderregisterSaxEventHandler(RecordPersister persister, RecordFetcher fetcher) {
         this.persister = persister;
         this.fetcher = fetcher;
         yderRecordCount = 0;
         personRecordCount = 0;
         versionNumber = null;
     }
 
     public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
         if (START_QNAME.equals(qName)) {
             parseStartElement(attributes);
         } else if (YDER_QNAME.equals(qName)) {
             yderRecordCount += 1;
             parseYder(attributes);
         } else if (PERSON_QNAME.equals(qName)) {
             personRecordCount += 1;
             parsePerson(attributes);
         } else if (END_QNAME.equals(qName)) {
             parseEndRecord(attributes, yderRecordCount, personRecordCount);
         } else if (ROOT_QNAME.equals(qName)) {
             // ignore the root
         } else {
             throw new ParserException(format("Unknown xml element '%s' in yderregister.", qName));
         }
     }
 
     private void parseEndRecord(Attributes att, long yderRecordCount, long personRecordCount) {
         long expectedRecordCount = Long.parseLong(att.getValue("AntPost"));
         if (logger.isDebugEnabled()) {
             logger.debug(format("Found %s Yder records and %s Person records", yderRecordCount, personRecordCount));
         }
 
         long recordCount = yderRecordCount + personRecordCount;
         if (recordCount != expectedRecordCount) {
             throw new ParserException(format("The expected number of records '%d' did not match the actual '%d'.",
                     expectedRecordCount, recordCount));
         }
     }
 
     /**
      * Parse a person from xml
      * @param attributes xml attribues of the element
      */
     private void parsePerson(Attributes attributes) {
         // Id is a MD5 of the following fields HistIdPerson + CprNr + YdernrPerson + TilgDatoPerson, seperated by -
         String yderNrPerson = attributes.getValue("YdernrPerson");
         String cprPerson = attributes.getValue("CprNr");
         String histIdPerson = getValue(attributes, "HistIdPerson");
         String tilgDatoPerson = getValue(attributes, "TilgDatoPerson");
 
         String rawId = histIdPerson + "-" + cprPerson + "-" + yderNrPerson + "-" + tilgDatoPerson;
         String id = makeMd5Identifier(rawId);
 
         if (cprPerson == null || cprPerson.trim().equals("")) {
             return;
         }
 
     	String afgDatoPerson = getValue(attributes, "AfgDatoPerson");
 
         Instant currentValidFrom = extractDate(tilgDatoPerson);
         Instant currentValidTo = extractDate(afgDatoPerson);
 
         // Insert new record
         RecordSpecification personSpecification = YderregisterRecordSpecs.PERSON_RECORD_TYPE;
 
         Record currentRecord = new RecordBuilder(personSpecification)
                 .field("Id", id)
         		.field("HistIdPerson", histIdPerson)
         		.field("YdernrPerson", removeLeadingZeroes(yderNrPerson))
         		.field("CprNr", cprPerson)
                 .field("TilgDatoPerson", getValue(attributes, "TilgDatoPerson"))
                 .field("AfgDatoPerson", afgDatoPerson)
                 .field("PersonrolleKode", getValue(attributes, "PersonrolleKode"))
                 .field("PersonrolleTxt", getValue(attributes, "PersonrolleTxt"))
                 .build();
         processParsedRecord(currentValidFrom, currentValidTo, personSpecification, currentRecord, id);
     }
 
     /**
      * Parse a yder from xml
      * @param attributes xml attribues of the element
      */
     private void parseYder(Attributes attributes) {
         // Id is a MD5 of the following fields HistIdYder + YdernrYder + TilgDatoYder, seperated by -
         String ydernrYder = removeLeadingZeroes(attributes.getValue("YdernrYder"));
         String tilgDatoYder = getValue(attributes, "TilgDatoYder");
         String histIdYder = getValue(attributes, "HistIdYder");
         String afgDatoYder = getValue(attributes, "AfgDatoYder");
 
         String rawId = histIdYder + "-" + ydernrYder + "-" + tilgDatoYder;
         String id = makeMd5Identifier(rawId);
 
         Instant currentValidFrom = extractDate(tilgDatoYder);
         Instant currentValidTo = extractDate(afgDatoYder);
 
 
         Record currentRecord = new RecordBuilder(YderregisterRecordSpecs.YDER_RECORD_TYPE)
                 .field("Id", id)
                 .field("HistIdYder", histIdYder)
                 .field("AmtKodeYder", getValue(attributes, "AmtKodeYder"))
                 .field("AmtTxtYder", getValue(attributes, "AmtTxtYder"))
                 .field("YdernrYder", ydernrYder)
                 .field("PrakBetegn", getValue(attributes, "PrakBetegn"))
                 .field("AdrYder", getValue(attributes, "AdrYder"))
                 .field("PostnrYder", getValue(attributes, "PostnrYder"))
                 .field("PostdistYder", getValue(attributes, "PostdistYder"))
                 .field("AfgDatoYder", getValue(attributes, "AfgDatoYder"))
                 .field("TilgDatoYder", getValue(attributes, "TilgDatoYder"))
                 .field("HvdSpecKode", getValue(attributes, "HvdSpecKode"))
                 .field("HvdSpecTxt", getValue(attributes, "HvdSpecTxt"))
                 .field("HvdTlf", getValue(attributes, "HvdTlf"))
                 .field("EmailYder", getValue(attributes, "EmailYder"))
                 .field("WWW", getValue(attributes, "WWW"))
                 .build();
         processParsedRecord(currentValidFrom, currentValidTo,
                 YderregisterRecordSpecs.YDER_RECORD_TYPE, currentRecord, id);
     }
 
     /**
      * Process a parsed record in a magical way which handles ValidTo and ValidFrom and updates records correctly.
      * See documentation for the flow in the doc/ directory
      * @param currentValidFrom
      * @param currentValidTo
      * @param specification
      * @param currentRecord
      * @param keyValue
      */
     private void processParsedRecord(Instant currentValidFrom, Instant currentValidTo,
                                      RecordSpecification specification, Record currentRecord, String keyValue) {
         ////////////////////////////////////////////////////////////////////
         // NOTE !!
         // To understand the flow below please read the documentation under doc/ carefully
 
         RecordWithMetadata currentRecordWithMeta = enrichRecordWithMeta(currentRecord, currentValidFrom, currentValidTo);
         // Check if record already exists
         try {
             RecordWithMetadata fetchedRecordWithMeta = fetcher.fetchWithMetaAt(
                     keyValue,
                     currentValidFrom,
                     specification);
             if (fetchedRecordWithMeta == null) {
                 // Not found just save
                 persistAt(currentRecord, specification, currentValidFrom, currentValidTo);
             } else {
                 Record fetchedRecord = fetchedRecordWithMeta.getRecord();
                 // If records equals do nothing
                 if (!fetchedRecord.equals(currentRecord)) {
                     if (isValidAtTransactionTime(fetchedRecordWithMeta)) {
                         // The fetched record is valid
                         if (isValidAtTransactionTime(currentRecordWithMeta)) {
                             // Both is valid terminate the fetched when current begins
                             fetchedRecordWithMeta.setValidTo(currentValidFrom);
                             persister.update(fetchedRecordWithMeta, specification);
                             // The old one has been terminated when this begin so it is safe to save current
                             persistAt(currentRecord, specification, currentValidFrom, currentValidTo);
                         } else {
                             // Current is not valid update fetched
                             replaceFetchedWithCurrentRecord(currentRecordWithMeta, fetchedRecordWithMeta, specification);
                         }
                     } else {
                         // The fetched record was not valid
                         if (isValidAtTransactionTime(currentRecordWithMeta)) {
                             // Current record is valid but fetched was not, fine just insert
                             persistAt(currentRecord, specification, currentValidFrom, currentValidTo);
                         } else {
                             // Neither one of the records was valid update the existing
                             replaceFetchedWithCurrentRecord(currentRecordWithMeta, fetchedRecordWithMeta, specification);
                         }
                     }
                 }
             }
         } catch (SQLException e) {
             throw new ParserException(e);
         } catch (IncorrectResultSizeDataAccessException e) {
             throw new ParserException(e);
         }
     }
 
     private void replaceFetchedWithCurrentRecord(RecordWithMetadata currentRecordWithMeta,
                                                  RecordWithMetadata fetchedRecordWithMeta,
                                                  RecordSpecification specification) {
         RecordWithMetadata updatedRecord = new RecordWithMetadata(currentRecordWithMeta.getValidFrom(),
                 currentRecordWithMeta.getValidTo(), null, fetchedRecordWithMeta.getPid(),
                 currentRecordWithMeta.getRecord());
         persister.update(updatedRecord, specification);
     }
 
     /**
      * Check if a records is valid at transaction time
      * @param recordWithMetadata
      * @return true if valid false otherwise
      */
     private boolean isValidAtTransactionTime(RecordWithMetadata recordWithMetadata) {
         Instant transactionTime = persister.getTransactionTime();
         Instant fetchedValidFrom = recordWithMetadata.getValidFrom();
         Instant fetchedValidTo = recordWithMetadata.getValidTo();
         if ((fetchedValidFrom.isBefore(transactionTime) || fetchedValidFrom.isEqual(transactionTime)) &&
             (fetchedValidTo == null || fetchedValidTo.isAfter(transactionTime))) {
             return true;
         }
         return false;
     }
 
     /**
      * Enrich a Record with ValidTo and ValidFrom to a RecordWithMetadata
      * @param record
      * @param validFrom
      * @param validTo
      * @return
      */
     private RecordWithMetadata enrichRecordWithMeta(Record record, Instant validFrom, Instant validTo) {
         return new RecordWithMetadata(validFrom, validTo, null, null, record);
     }
 
     /**
      * Persist a record with a given validTo and validFrom
      * @param record
      * @param personSpecification
      * @param currentValidFrom
      * @param currentValidTo
      * @throws SQLException
      */
     private void persistAt(Record record, RecordSpecification personSpecification, Instant currentValidFrom,
                            Instant currentValidTo) throws SQLException {
         RecordWithMetadata recordWithMetadata = new RecordWithMetadata(currentValidFrom, currentValidTo, null, null, record);
         persister.persist(recordWithMetadata, personSpecification);
     }
 
     /**
      * Extract an instant from a string
      * @param date
      * @return instant with parsed time or null if input was empty
      */
     private Instant extractDate(String date) {
         Instant result = null;
         if(date != null && date.trim().length() > 0) {
             try {
                 result = new Instant(datoFormatter.parse(date));
             } catch (ParseException e) {
                 throw new ParserException(e);
             }
         }
         return result;
     }
 
     private String getValue(Attributes attributes, String name) {
         String value = attributes.getValue(name);
         return StringUtils.trimToNull(value);
     }
 
     private void parseStartElement(Attributes att) throws SAXException {
         String receiverId = getValue(att, "Modt");
 
         if (!EXPECTED_RECIPIENT_ID.equals(receiverId)) {
             throw new ParserException(format("The recipient id in the file '%s' did not match the expected '%s'.",
                     receiverId, EXPECTED_RECIPIENT_ID));
         }
 
         String interfaceId = getValue(att, "SnitfladeId");
 
         if (!SUPPORTED_INTERFACE_VERSION.equals(interfaceId)) {
             throw new ParserException(format("The interface id in the file '%s' did not match the expected '%s'.",
                     interfaceId, SUPPORTED_INTERFACE_VERSION));
         }
 
         // This number is used to check the sequence once the parser is done.
         //
         versionNumber = getValue(att, "OpgDato");
     }
 
     /**
      * Strips leading zeros but leaves one if the input is all zeros. E.g.
      * "0000" -> "0".
      */
     private String removeLeadingZeroes(String valueToStrip) {
         return valueToStrip.replaceFirst("^0+(?!$)", "");
     }
 
     public String GetVersionFromFileSet() {
         return versionNumber;
     }
 
     private String makeMd5Identifier(String identifier) {
         String result = "";
         try {
             MessageDigest md = MessageDigest.getInstance("MD5");
             byte[] digest = md.digest(identifier.getBytes());
             result = String.valueOf(Hex.encodeHex(digest));
         } catch (NoSuchAlgorithmException e) {
             throw new RuntimeException(e);
         }
         return result;
     }
 
 }
