 //
 // Treasure Data Bulk-Import Tool in Java
 //
 // Copyright (C) 2012 - 2013 Muga Nishizawa
 //
 //    Licensed under the Apache License, Version 2.0 (the "License");
 //    you may not use this file except in compliance with the License.
 //    You may obtain a copy of the License at
 //
 //        http://www.apache.org/licenses/LICENSE-2.0
 //
 //    Unless required by applicable law or agreed to in writing, software
 //    distributed under the License is distributed on an "AS IS" BASIS,
 //    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //    See the License for the specific language governing permissions and
 //    limitations under the License.
 //
 package com.treasure_data.td_import.prepare;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.nio.charset.CodingErrorAction;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.logging.Logger;
 import java.util.zip.GZIPInputStream;
 
 import joptsimple.OptionSet;
 
 import com.treasure_data.td_import.Options;
 import com.treasure_data.td_import.Configuration;
 import com.treasure_data.td_import.model.ColumnType;
 import com.treasure_data.td_import.reader.ApacheFileReader;
 import com.treasure_data.td_import.reader.CSVFileReader;
 import com.treasure_data.td_import.reader.FileReader;
 import com.treasure_data.td_import.reader.JSONFileReader;
 import com.treasure_data.td_import.reader.MessagePackFileReader;
 import com.treasure_data.td_import.reader.MySQLTableReader;
 import com.treasure_data.td_import.reader.RegexFileReader;
 import com.treasure_data.td_import.reader.SyslogFileReader;
 import com.treasure_data.td_import.writer.FileWriter;
 import com.treasure_data.td_import.writer.MsgpackGZIPFileWriter;
 
 public class PrepareConfiguration extends Configuration {
 
     public static class Factory {
         protected Options options;
 
         public Factory(Properties props, boolean isUploaded) {
             options = new Options();
             if (isUploaded) {
                 options.initUploadOptionParser(props);
             } else {
                 options.initPrepareOptionParser(props);
             }
         }
 
         public Options getBulkImportOptions() {
             return options;
         }
 
         public PrepareConfiguration newPrepareConfiguration(String[] args) {
             options.setOptions(args);
             OptionSet optionSet = options.getOptions();
 
             // TODO FIXME when uploadParts is called, default format is "msgpack.gz"
             // on the other hand, when prepareParts, default format is "csv".
             String formatStr;
             if (optionSet.has(BI_PREPARE_PARTS_FORMAT)) {
                 formatStr = (String) optionSet.valueOf(BI_PREPARE_PARTS_FORMAT);
             } else {
                 formatStr = BI_PREPARE_PARTS_FORMAT_DEFAULTVALUE;
             }
 
             // lookup format enum
             Format format = Format.fromString(formatStr);
             if (format == null) {
                 throw new IllegalArgumentException(String.format(
                         "unsupported format '%s'", formatStr));
             }
             PrepareConfiguration c = format.createPrepareConfiguration();
             c.options = options;
             return c;
         }
     }
 
     public static enum Format {
         CSV("csv") {
             @Override
             public FileReader<CSVPrepareConfiguration> createFileReader(
                     PrepareConfiguration conf, FileWriter writer)
                     throws PreparePartsException {
                 return new CSVFileReader((CSVPrepareConfiguration) conf, writer);
             }
 
             @Override
             public PrepareConfiguration createPrepareConfiguration() {
                 return new CSVPrepareConfiguration();
             }
         },
         TSV("tsv") {
             @Override
             public FileReader<CSVPrepareConfiguration> createFileReader(
                     PrepareConfiguration conf, FileWriter writer)
                     throws PreparePartsException {
                 return new CSVFileReader((CSVPrepareConfiguration) conf, writer);
             }
 
             @Override
             public PrepareConfiguration createPrepareConfiguration() {
                 return new CSVPrepareConfiguration();
             }
         },
         MYSQL("mysql") {
             @Override
             public FileReader<MySQLPrepareConfiguration> createFileReader(
                     PrepareConfiguration conf, FileWriter writer)
                     throws PreparePartsException {
                 return new MySQLTableReader((MySQLPrepareConfiguration) conf,
                         writer);
             }
 
             @Override
             public PrepareConfiguration createPrepareConfiguration() {
                 return new MySQLPrepareConfiguration();
             }
         },
         JSON("json") {
             @Override
             public FileReader<JSONPrepareConfiguration> createFileReader(
                     PrepareConfiguration conf, FileWriter writer)
                     throws PreparePartsException {
                 return new JSONFileReader((JSONPrepareConfiguration) conf,
                         writer);
             }
 
             @Override
             public PrepareConfiguration createPrepareConfiguration() {
                 return new JSONPrepareConfiguration();
             }
         },
         REGEX("regex") {
             @Override
             public FileReader<RegexPrepareConfiguration> createFileReader(
                     PrepareConfiguration conf, FileWriter writer)
                     throws PreparePartsException {
                 return new RegexFileReader<RegexPrepareConfiguration>(
                         (RegexPrepareConfiguration) conf, writer);
             }
 
             @Override
             public PrepareConfiguration createPrepareConfiguration() {
                 return new RegexPrepareConfiguration();
             }
         },
         APACHE("apache") {
             @Override
             public FileReader<ApachePrepareConfiguration> createFileReader(
                     PrepareConfiguration conf, FileWriter writer)
                     throws PreparePartsException {
                 return new ApacheFileReader((ApachePrepareConfiguration) conf,
                         writer);
             }
 
             @Override
             public PrepareConfiguration createPrepareConfiguration() {
                 return new ApachePrepareConfiguration();
             }
         },
         SYSLOG("syslog") {
             @Override
             public FileReader<SyslogPrepareConfiguration> createFileReader(
                     PrepareConfiguration conf, FileWriter writer)
                     throws PreparePartsException {
                 return new SyslogFileReader((SyslogPrepareConfiguration) conf,
                         writer);
             }
 
             @Override
             public PrepareConfiguration createPrepareConfiguration() {
                 return new SyslogPrepareConfiguration();
             }
         },
         MSGPACK("msgpack") {
             @Override
             public FileReader<MessagePackPrepareConfiguration> createFileReader(
                     PrepareConfiguration conf, FileWriter writer)
                     throws PreparePartsException {
                 return new MessagePackFileReader(
                         (MessagePackPrepareConfiguration) conf, writer);
             }
 
             @Override
             public PrepareConfiguration createPrepareConfiguration() {
                 return new MessagePackPrepareConfiguration();
             }
         };
 
         private String format;
 
         Format(String format) {
             this.format = format;
         }
 
         public String format() {
             return format;
         }
 
         public abstract PrepareConfiguration createPrepareConfiguration();
 
         public FileReader<? extends PrepareConfiguration> createFileReader(
                 PrepareConfiguration conf, FileWriter writer)
                 throws PreparePartsException {
             throw new PreparePartsException(
                     new UnsupportedOperationException("format: " + this));
         }
 
         public static Format fromString(String format) {
             return StringToFormat.get(format);
         }
 
         private static class StringToFormat {
             private static final Map<String, Format> REVERSE_DICTIONARY;
 
             static {
                 Map<String, Format> map = new HashMap<String, Format>();
                 for (Format elem : Format.values()) {
                     map.put(elem.format(), elem);
                 }
                 REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
             }
 
             static Format get(String key) {
                 return REVERSE_DICTIONARY.get(key);
             }
         }
     }
 
     public static enum OutputFormat {
         MSGPACKGZ("msgpackgz") {
             @Override
             public FileWriter createFileWriter(PrepareConfiguration conf) throws PreparePartsException {
                 return new MsgpackGZIPFileWriter(conf);
             }
         },
         SYSLOGMSGPACKGZ("syslogmsgpackgz") {
             @Override
             public FileWriter createFileWriter(PrepareConfiguration conf) throws PreparePartsException {
                 return new SyslogFileReader.ExtFileWriter(conf);
             }
         };
 
         private String outputFormat;
 
         OutputFormat(String outputFormat) {
             this.outputFormat = outputFormat;
         }
 
         public String outputFormat() {
             return outputFormat;
         }
 
         public FileWriter createFileWriter(PrepareConfiguration conf) throws PreparePartsException {
             throw new PreparePartsException(
                     new UnsupportedOperationException("output format: " + this));
         }
 
         public static OutputFormat fromString(String outputFormat) {
             return StringToOutputFormat.get(outputFormat);
         }
 
         private static class StringToOutputFormat {
             private static final Map<String, OutputFormat> REVERSE_DICTIONARY;
 
             static {
                 Map<String, OutputFormat> map = new HashMap<String, OutputFormat>();
                 for (OutputFormat elem : OutputFormat.values()) {
                     map.put(elem.outputFormat(), elem);
                 }
                 REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
             }
 
             static OutputFormat get(String key) {
                 return REVERSE_DICTIONARY.get(key);
             }
         }
     }
 
     public static enum CompressionType {
         GZIP("gzip") {
             @Override
             public InputStream createInputStream(InputStream in) throws IOException {
                 return new BufferedInputStream(new GZIPInputStream(in));
             }
         }, AUTO("auto") {
             @Override
             public InputStream createInputStream(InputStream in) throws IOException {
                 throw new IOException("unsupported compress type");
             }
         }, NONE("none") {
             @Override
             public InputStream createInputStream(InputStream in) throws IOException {
                 return new BufferedInputStream(in);
             }
         };
 
         private String type;
 
         CompressionType(String type) {
             this.type = type;
         }
 
         public String type() {
             return type;
         }
 
         public abstract InputStream createInputStream(InputStream in) throws IOException;
 
         public static CompressionType fromString(String type) {
             return StringToCompressionType.get(type);
         }
 
         private static class StringToCompressionType {
             private static final Map<String, CompressionType> REVERSE_DICTIONARY;
 
             static {
                 Map<String, CompressionType> map = new HashMap<String, CompressionType>();
                 for (CompressionType elem : CompressionType.values()) {
                     map.put(elem.type(), elem);
                 }
                 REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
             }
 
             static CompressionType get(String key) {
                 return REVERSE_DICTIONARY.get(key);
             }
         }
     }
 
     public static enum ErrorRecordsHandling {
         SKIP(BI_PREPARE_PARTS_ERROR_RECORDS_HANDLING_DEFAULTVALUE) {
             @Override
             public void handleError(PreparePartsException e)
                     throws PreparePartsException {
                 // ignore
             }
         },
         ABORT("abort") {
             @Override
             public void handleError(PreparePartsException e)
                     throws PreparePartsException {
                 throw e;
             }
         };
 
         private String mode;
 
         ErrorRecordsHandling(String mode) {
             this.mode = mode;
         }
 
         public String mode() {
             return mode;
         }
 
         public abstract void handleError(PreparePartsException e)
                 throws PreparePartsException;
 
         public static ErrorRecordsHandling fromString(String mode) {
             return StringToErrorHandling.get(mode);
         }
 
         private static class StringToErrorHandling {
             private static final Map<String, ErrorRecordsHandling> REVERSE_DICTIONARY;
 
             static {
                 Map<String, ErrorRecordsHandling> map = new HashMap<String, ErrorRecordsHandling>();
                 for (ErrorRecordsHandling elem : ErrorRecordsHandling.values()) {
                     map.put(elem.mode(), elem);
                 }
                 REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
             }
 
             static ErrorRecordsHandling get(String key) {
                 return REVERSE_DICTIONARY.get(key);
             }
         }
     }
 
     private static final Logger LOG = Logger
             .getLogger(PrepareConfiguration.class.getName());
 
     // FIXME this field is also declared in td-client.Config.
     protected Properties props;
     protected Options options;
     protected OptionSet optionSet;
 
     protected Format format;
     protected OutputFormat outputFormat = OutputFormat.MSGPACKGZ;
     protected CompressionType compressionType;
     protected CharsetDecoder charsetDecoder;
     protected int numOfPrepareThreads;
     protected String aliasTimeColumn;
     protected long timeValue = -1;
     protected String timeFormat;
     protected String errorRecordOutputDirName;
     protected ErrorRecordsHandling errorRecordsHandling;
     protected boolean dryRun = false;
     protected String outputDirName;
     protected int splitSize;
     protected int sampleRowSize;
     protected String[] columnNames;
     protected ColumnType[] columnTypes;
     protected String[] excludeColumns;
     protected String[] onlyColumns;
 
     public PrepareConfiguration() {
     }
 
     public void configure(Properties props, Options options) {
         this.props = props;
         this.options = options;
         this.optionSet = options.getOptions();
 
         // format
         setFormat();
 
         // output format
         setOutputFormat();
 
         // compression type
         setCompressionType();
 
         // parallel
         setPrepareThreadNum();
 
         // encoding
         setEncoding();
 
         // alias time column
         setAliasTimeColumn();
 
         // time value
         setTimeValue();
 
         // time format
         setTimeFormat();
 
         // output DIR
         setOutputDirName();
 
         // error handling
         setErrorRecordsHandling();
 
         // exclude-columns
         setExcludeColumns();
 
         // only-columns
         setOnlyColumns();
 
         // dry-run mode
         setDryRun();
 
         // split size
         setSplitSize();
 
         // row size with sample reader
         setSampleReaderRowSize();
     }
 
     public List<String> getNonOptionArguments() {
         return (List<String>) options.getOptions().nonOptionArguments();
     }
 
     public boolean hasHelpOption() {
         return options.getOptions().has(BI_PREPARE_PARTS_HELP);
     }
 
     @Override
     public String showHelp(Properties props) {
         StringBuilder sbuf = new StringBuilder();
 
         // usage
         sbuf.append("usage:\n");
         sbuf.append(Configuration.CMD_PREPARE_USAGE);
         sbuf.append("\n");
 
         // example
         sbuf.append("example:\n");
         sbuf.append(Configuration.CMD_PREPARE_EXAMPLE);
         sbuf.append("\n");
 
         // description
         sbuf.append("description:\n");
         sbuf.append(Configuration.CMD_PREPARE_DESC);
         sbuf.append("\n");
 
         // options
         sbuf.append("options:\n");
         sbuf.append(Configuration.CMD_PREPARE_OPTIONS);
         sbuf.append("\n");
 
         return sbuf.toString();
     }
 
     public void setFormat() {
         String formatStr;
         if (!optionSet.has(BI_PREPARE_PARTS_FORMAT)) {
             formatStr = Configuration.BI_PREPARE_PARTS_FORMAT_DEFAULTVALUE;
         } else {
             formatStr = (String) optionSet.valueOf(BI_PREPARE_PARTS_FORMAT);
         }
         format = Format.fromString(formatStr);
         if (format == null) {
             throw new IllegalArgumentException(String.format(
                     "unsupported format '%s'", formatStr));
         }
     }
 
     public Format getFormat() {
         return format;
     }
 
     public void setOutputFormat() {
         if (format == null) {
             throw new IllegalStateException(
                     "this method MUST be called after invoking the setFormat()");
         }
 
         if (format.equals(Format.SYSLOG)) {
             // if format type is 'syslog', output format 
             outputFormat = OutputFormat.SYSLOGMSGPACKGZ;
         } else {
             outputFormat = OutputFormat.MSGPACKGZ;
         }
     }
 
     public OutputFormat getOutputFormat() {
         return outputFormat;
     }
 
     public void setCompressionType() {
         String type;
         if (!optionSet.has(BI_PREPARE_PARTS_COMPRESSION)) {
             type = BI_PREPARE_PARTS_COMPRESSION_DEFAULTVALUE;
         } else {
             type = (String) optionSet.valueOf(BI_PREPARE_PARTS_COMPRESSION);
         }
 
         compressionType = CompressionType.fromString(type);
         if (compressionType == null) {
             throw new IllegalArgumentException(String.format(
                     "unsupported compression type: %s", type));
         }
     }
 
     public CompressionType getCompressionType() {
         return compressionType;
     }
 
     public CompressionType checkCompressionType(String fileName) throws PreparePartsException {
         if (getCompressionType() != CompressionType.AUTO) {
             return getCompressionType();
         }
 
         CompressionType[] candidateCompressTypes = new CompressionType[] {
                 CompressionType.GZIP, CompressionType.NONE,
         };
 
         CompressionType compressionType = null;
         for (int i = 0; i < candidateCompressTypes.length; i++) {
             InputStream in = null;
             try {
                 if (candidateCompressTypes[i].equals(CompressionType.GZIP)) {
                     in = CompressionType.GZIP.createInputStream(new FileInputStream(fileName));
                 } else if (candidateCompressTypes[i].equals(CompressionType.NONE)) {
                     in = CompressionType.NONE.createInputStream(new FileInputStream(fileName));
                 } else {
                     throw new PreparePartsException("fatal error");
                 }
                 byte[] b = new byte[2];
                 in.read(b);
 
                 compressionType = candidateCompressTypes[i];
                 break;
             } catch (IOException e) {
                 LOG.fine(String.format("file %s is %s", fileName,
                         e.getMessage()));
             } finally {
                 if (in != null) {
                     try {
                         in.close();
                     } catch (IOException e) {
                         // ignore
                     }
                 }
             }
         }
 
         this.compressionType = compressionType;
         return compressionType;
     }
 
     public void setPrepareThreadNum() {
         String num;
         if (!optionSet.has(BI_PREPARE_PARTS_PARALLEL)) {
             num = BI_PREPARE_PARTS_PARALLEL_DEFAULTVALUE;
         } else {
             num = (String) optionSet.valueOf(BI_PREPARE_PARTS_PARALLEL);
         }
 
         try {
             int n = Integer.parseInt(num);
             if (n < 0) {
                 numOfPrepareThreads = 2;
             } else if (n > 9){
                 numOfPrepareThreads = 8;
             } else {
                 numOfPrepareThreads = n;
             }
         } catch (NumberFormatException e) {
             String msg = String.format(
                     "'int' value is required as '%s' option", BI_PREPARE_PARTS_PARALLEL);
             throw new IllegalArgumentException(msg, e);
         }
     }
 
     public int getNumOfPrepareThreads() {
         return numOfPrepareThreads;
     }
 
     public void setEncoding() {
         String encoding;
         if (!optionSet.has(BI_PREPARE_PARTS_ENCODING)) {
             encoding = BI_PREPARE_PARTS_ENCODING_DEFAULTVALUE;
         } else {
             encoding = (String) optionSet.valueOf(BI_PREPARE_PARTS_ENCODING);
         }
 
         try {
             createCharsetDecoder(encoding);
         } catch (Exception e) {
             throw new IllegalArgumentException(e.getMessage());
         }
     }
 
     public void createCharsetDecoder(String encoding) throws Exception {
         charsetDecoder = Charset.forName(encoding).newDecoder()
                 .onMalformedInput(CodingErrorAction.REPORT)
                 .onUnmappableCharacter(CodingErrorAction.REPORT);
     }
 
     public CharsetDecoder getCharsetDecoder() throws PreparePartsException {
         return charsetDecoder;
     }
 
     public void setAliasTimeColumn() {
         if (optionSet.has(BI_PREPARE_PARTS_TIMECOLUMN)) {
             aliasTimeColumn = (String) optionSet.valueOf(BI_PREPARE_PARTS_TIMECOLUMN);
         }
     }
 
     public String getAliasTimeColumn() {
         return aliasTimeColumn;
     }
 
     public void setTimeValue() {
         String v = null;
         if (optionSet.has(BI_PREPARE_PARTS_TIMEVALUE)) {
             v = (String) optionSet.valueOf(BI_PREPARE_PARTS_TIMEVALUE);
         }
 
         if (v != null) {
             try {
                 timeValue = Long.parseLong(v);
             } catch (NumberFormatException e) {
                 String msg = String.format(
                         "'%s' is required as long type (unix timestamp)", BI_PREPARE_PARTS_TIMEVALUE);
                 throw new IllegalArgumentException(msg, e);
             }
         }
     }
 
     public long getTimeValue() {
         return timeValue;
     }
 
     public void setTimeFormat() {
         if (optionSet.has(BI_PREPARE_PARTS_TIMEFORMAT)) {
             timeFormat = (String) optionSet.valueOf(BI_PREPARE_PARTS_TIMEFORMAT);
         }
     }
 
     public Strftime getTimeFormat() {
         return timeFormat == null ? null : new Strftime(timeFormat);
     }
 
     public Strftime getTimeFormat(String strfString) {
         return strfString == null ? null : new Strftime(strfString);
     }
 
     public void setOutputDirName() {
         if (optionSet.has("output")) {
             outputDirName = (String) optionSet.valueOf(BI_PREPARE_PARTS_OUTPUTDIR);
         }
 
         File outputDir = null;
         if (outputDirName == null || outputDirName.isEmpty()) {
             outputDir = new File(new File("."), BI_PREPARE_PARTS_OUTPUTDIR_DEFAULTVALUE);
             outputDirName = outputDir.getName();
         } else {
             outputDir = new File(outputDirName);
         }
 
         // validate output dir
         if (!outputDir.isDirectory()) {
             if (!outputDir.mkdir()) {
                 throw new IllegalArgumentException(String.format(
                         "Cannot create '%s' directory '%s'",
                         BI_PREPARE_PARTS_OUTPUTDIR, outputDirName));
             }
         }
     }
 
     public void setErrorRecordsHandling() {
         String mode;
         if (!optionSet.has(BI_PREPARE_PARTS_ERROR_RECORDS_HANDLING)) {
             mode = BI_PREPARE_PARTS_ERROR_RECORDS_HANDLING_DEFAULTVALUE;
         } else {
             mode = (String) optionSet.valueOf(BI_PREPARE_PARTS_ERROR_RECORDS_HANDLING);
         }
 
         errorRecordsHandling = ErrorRecordsHandling.fromString(mode);
         if (errorRecordsHandling == null) {
             throw new IllegalArgumentException(String.format(
                     "unsupported '%s' mode '%s'",
                     BI_PREPARE_PARTS_ERROR_RECORDS_HANDLING, mode));
         }
     }
 
     public ErrorRecordsHandling getErrorRecordsHandling() {
         return errorRecordsHandling;
     }
 
     public void setDryRun() {
         if (optionSet.has("dry-run")) {
             String drun = (String) optionSet.valueOf("dry-run");
             dryRun = drun != null && drun.equals("true");    
         }
     }
 
     public boolean dryRun() {
         return dryRun;
     }
 
     public String getOutputDirName() {
         return outputDirName;
     }
 
     public void setSplitSize() {
         String size;
         if (!optionSet.has(BI_PREPARE_PARTS_SPLIT_SIZE)) {
             size = BI_PREPARE_PARTS_SPLIT_SIZE_DEFAULTVALUE;
         } else {
             size = (String) optionSet.valueOf(BI_PREPARE_PARTS_SPLIT_SIZE);
         }
 
         try {
             splitSize = Integer.parseInt(size);
         } catch (NumberFormatException e) {
             String msg = String.format("'%s' is required as int type",
                     BI_PREPARE_PARTS_SPLIT_SIZE);
             throw new IllegalArgumentException(msg, e);
         }
     }
 
     public int getSplitSize() {
         return splitSize;
     }
 
     public void setSampleReaderRowSize() {
         String sRowSize = props.getProperty(
                 Configuration.BI_PREPARE_PARTS_SAMPLE_ROWSIZE,
                 Configuration.BI_PREPARE_PARTS_SAMPLE_ROWSIZE_DEFAULTVALUE);
         try {
             sampleRowSize = Integer.parseInt(sRowSize);
         } catch (NumberFormatException e) {
             String msg = String.format(
                     "sample row size is required as int type e.g. -D%s=%s",
                     Configuration.BI_PREPARE_PARTS_SAMPLE_ROWSIZE,
                     Configuration.BI_PREPARE_PARTS_SAMPLE_ROWSIZE_DEFAULTVALUE);
             throw new IllegalArgumentException(msg, e);
         }
     }
 
     public int getSampleRowSize() {
         return sampleRowSize;
     }
 
     public void setColumnNames() {
         if (!optionSet.has(BI_PREPARE_PARTS_COLUMNS)) {
             columnNames = new String[0];
         } else {
             columnNames = optionSet.valuesOf(BI_PREPARE_PARTS_COLUMNS).toArray(new String[0]);
         }
     }
 
     public void setColumnNames(String[] columnNames) {
         this.columnNames = columnNames;
     }
 
     public String[] getColumnNames() {
         return columnNames;
     }
 
     public void setColumnTypes() {
         if (!optionSet.has(BI_PREPARE_PARTS_COLUMNTYPES)) {
             columnTypes = new ColumnType[0];
         } else {
             String[] types = optionSet.valuesOf(BI_PREPARE_PARTS_COLUMNTYPES).toArray(new String[0]);
             columnTypes = new ColumnType[types.length];
             for (int i = 0; i < types.length; i++) {
                 columnTypes[i] = ColumnType.fromString(types[i].toLowerCase());
                if (columnTypes[i] == null) {
                    throw new IllegalArgumentException(String.format(
                            "'%s' cannot be specified as column type", types[i]));
                }
             }
         }
     }
 
     public void setColumnTypes(ColumnType[] columnTypes) {
         this.columnTypes = columnTypes;
     }
 
     public ColumnType[] getColumnTypes() {
         return columnTypes;
     }
 
     public void setExcludeColumns() {
         if (!optionSet.has(BI_PREPARE_PARTS_EXCLUDE_COLUMNS)) {
             excludeColumns = new String[0];
         } else {
             excludeColumns = optionSet.valuesOf(BI_PREPARE_PARTS_EXCLUDE_COLUMNS).toArray(new String[0]);
             for (String c : excludeColumns) {
                 if (c.equals(Configuration.BI_PREPARE_PARTS_TIMECOLUMN_DEFAULTVALUE)) {
                     throw new IllegalArgumentException(String.format(
                             "'time' column cannot be included in '%s'",
                             BI_PREPARE_PARTS_EXCLUDE_COLUMNS));
                 }
             }
         }
     }
 
     public String[] getExcludeColumns() {
         return excludeColumns;
     }
 
     public void setOnlyColumns() {
         if (!optionSet.has(BI_PREPARE_PARTS_ONLY_COLUMNS)) {
             onlyColumns = new String[0];
         } else {
             onlyColumns = optionSet.valuesOf(BI_PREPARE_PARTS_ONLY_COLUMNS).toArray(new String[0]);
             for (String oc : onlyColumns) {
                 for (String ec : excludeColumns) {
                     if (oc.equals(ec)) {
                         throw new IllegalArgumentException(String.format(
                                 "don't include '%s' in '%s'",
                                 BI_PREPARE_PARTS_EXCLUDE_COLUMNS,
                                 BI_PREPARE_PARTS_ONLY_COLUMNS));
                     }
                 }
             }
         }
     }
 
     public String[] getOnlyColumns() {
         return onlyColumns;
     }
 
 }
