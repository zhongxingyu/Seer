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
 package com.treasure_data.bulk_import;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import joptsimple.HelpFormatter;
 import joptsimple.OptionDescriptor;
 import joptsimple.OptionParser;
 import joptsimple.OptionSet;
 
 
 public class BulkImportOptions {
     private static final Logger LOG = Logger.getLogger(BulkImportOptions.class.getName());
 
     private static class SimpleHelpFormatter implements HelpFormatter {
         private boolean isPrepare(Map<String, ? extends OptionDescriptor> options) {
             for (OptionDescriptor desc : options.values()) {
                 if (desc.options().contains("parallel")) {
                     return false;
                 }
             }
             return true;
         }
 
         public String format(Map<String, ? extends OptionDescriptor> options) {
             boolean isPrepare = isPrepare(options);
             System.out.println("command: " + isPrepare);
 
             StringBuilder sbuf = new StringBuilder();
 
             // usage
             sbuf.append("usage:\n");
             if (isPrepare) {
                 sbuf.append(Configuration.CMD_PREPARE_USAGE);
             } else {
                 sbuf.append(Configuration.CMD_UPLOAD_USAGE);
             }
             sbuf.append("\n");
 
             // example
             sbuf.append("example:\n");
             if (isPrepare) {
                 sbuf.append(Configuration.CMD_PREPARE_EXAMPLE);
             } else {
                 sbuf.append(Configuration.CMD_UPLOAD_EXAMPLE);
             }
             sbuf.append("\n");
 
             // description
             sbuf.append("description:\n");
             if (isPrepare) {
                 sbuf.append(Configuration.CMD_PREPARE_DESC);
             } else {
                 sbuf.append(Configuration.CMD_UPLOAD_DESC);
             }
             sbuf.append("\n");
 
             // options
             sbuf.append("options:\n");
             if (isPrepare) {
                 sbuf.append(Configuration.CMD_PREPARE_OPTIONS);
             } else {
                 sbuf.append(Configuration.CMD_UPLOAD_OPTIONS);
             }
             sbuf.append("\n");
 
             return sbuf.toString();
         }
     }
 
     protected OptionParser op;
     protected OptionSet options;
 
     public BulkImportOptions() {
         op = new OptionParser();
     }
 
     public void initPrepareOptionParser(Properties props) {
         op.formatHelpWith(new SimpleHelpFormatter());
         op.acceptsAll(Arrays.asList("h",
                 Configuration.BI_PREPARE_PARTS_HELP),
                 Configuration.BI_PREPARE_PARTS_HELP_DESC);
         op.acceptsAll(Arrays.asList("f",
                 Configuration.BI_PREPARE_PARTS_FORMAT),
                 Configuration.BI_PREPARE_PARTS_FORMAT_DESC)
                 .withRequiredArg()
                 .describedAs("FORMAT")
                 .ofType(String.class);
         op.acceptsAll(Arrays.asList("C",
                 Configuration.BI_PREPARE_PARTS_COMPRESSION),
                 Configuration.BI_PREPARE_PARTS_COMPRESSION_DESC)
                 .withRequiredArg()
                 .describedAs("TYPE")
                 .ofType(String.class);
         op.acceptsAll(Arrays.asList("e",
                 Configuration.BI_PREPARE_PARTS_ENCODING),
                 Configuration.BI_PREPARE_PARTS_ENCODING_DESC)
                 .withRequiredArg()
                 .describedAs("TYPE")
                 .ofType(String.class);
         op.acceptsAll(Arrays.asList("t",
                 Configuration.BI_PREPARE_PARTS_TIMECOLUMN),
                 Configuration.BI_PREPARE_PARTS_TIMECOLUMN_DESC)
                 .withRequiredArg()
                 .describedAs("NAME")
                 .ofType(String.class);
         op.acceptsAll(Arrays.asList("T",
                 Configuration.BI_PREPARE_PARTS_TIMEFORMAT),
                 Configuration.BI_PREPARE_PARTS_TIMEFORMAT_DESC)
                 .withRequiredArg()
                 .withValuesSeparatedBy(",")
                 .describedAs("FORMAT")
                 .ofType(String.class);
         op.acceptsAll(Arrays.asList(
                 Configuration.BI_PREPARE_PARTS_TIMEVALUE),
                 Configuration.BI_PREPARE_PARTS_TIMEVALUE_DESC)
                 .withRequiredArg()
                 .describedAs("TIME")
                 .ofType(String.class);
         op.acceptsAll(Arrays.asList("o",
                 Configuration.BI_PREPARE_PARTS_OUTPUTDIR),
                 Configuration.BI_PREPARE_PARTS_OUTPUTDIR_DESC)
                 .withRequiredArg()
                 .describedAs("DIR")
                 .ofType(String.class);
         op.acceptsAll(Arrays.asList("s",
                 Configuration.BI_PREPARE_PARTS_SPLIT_SIZE),
                 Configuration.BI_PREPARE_PARTS_SPLIT_SIZE_DESC )
                 .withRequiredArg()
                 .describedAs("SIZE_IN_KB")
                 .ofType(String.class);
         op.acceptsAll(Arrays.asList(
                 Configuration.BI_PREPARE_PARTS_ERROR_RECORDS_HANDLING),
                 Configuration.BI_PREPARE_PARTS_ERROR_RECORDS_HANDLING_DESC)
                 .withRequiredArg()
                 .describedAs("MODE")
                 .ofType(String.class);
         op.acceptsAll(Arrays.asList(
                 Configuration.BI_PREPARE_PARTS_DELIMITER),
                 Configuration.BI_PREPARE_PARTS_DELIMITER_DESC)
                 .withRequiredArg()
                 .describedAs("CHAR")
                 .ofType(String.class);
         op.acceptsAll(Arrays.asList(
                 Configuration.BI_PREPARE_PARTS_QUOTE),
                 Configuration.BI_PREPARE_PARTS_QUOTE_DESC)
                 .withRequiredArg()
                 .describedAs("CHAR")
                 .ofType(String.class);
         op.acceptsAll(Arrays.asList(
                 Configuration.BI_PREPARE_PARTS_NEWLINE),
                 Configuration.BI_PREPARE_PARTS_NEWLINE_DESC)
                 .withRequiredArg()
                 .describedAs("TYPE")
                 .ofType(String.class);
         op.acceptsAll(Arrays.asList(
                 Configuration.BI_PREPARE_PARTS_COLUMNHEADER),
                 Configuration.BI_PREPARE_PARTS_COLUMNHEADER_DESC);
         op.acceptsAll(Arrays.asList(
                 Configuration.BI_PREPARE_PARTS_COLUMNS),
                 Configuration.BI_PREPARE_PARTS_COLUMNS_DESC)
                 .withRequiredArg()
                 .describedAs("NAME,NAME,...")
                 .ofType(String.class)
                 .withValuesSeparatedBy(",");
         op.acceptsAll(Arrays.asList(
                 Configuration.BI_PREPARE_PARTS_COLUMNTYPES),
                 Configuration.BI_PREPARE_PARTS_COLUMNTYPES_DESC)
                 .withRequiredArg()
                 .describedAs("TYPE,TYPE,...")
                 .ofType(String.class)
                 .withValuesSeparatedBy(",");
         op.acceptsAll(Arrays.asList(
                 Configuration.BI_PREPARE_PARTS_EXCLUDE_COLUMNS),
                 Configuration.BI_PREPARE_PARTS_EXCLUDE_COLUMNS_DESC)
                 .withRequiredArg()
                 .describedAs("NAME,NAME,...")
                 .ofType(String.class)
                 .withValuesSeparatedBy(",");
         op.acceptsAll(Arrays.asList(
                 Configuration.BI_PREPARE_PARTS_ONLY_COLUMNS),
                 Configuration.BI_PREPARE_PARTS_ONLY_COLUMNS_DESC)
                 .withRequiredArg()
                 .describedAs("NAME,NAME,...")
                 .ofType(String.class)
                 .withValuesSeparatedBy(",");
        op.acceptsAll(Arrays.asList(
                Configuration.BI_PREPARE_PARTS_PARALLEL),
                Configuration.BI_PREPARE_PARTS_PARALLEL_DESC)
                .withRequiredArg()
                .describedAs("NUM")
                .ofType(String.class);
 
         // mysql
         op.acceptsAll(Arrays.asList(
                 Configuration.BI_PREPARE_PARTS_JDBC_CONNECTION_URL),
                 Configuration.BI_PREPARE_PARTS_JDBC_CONNECTION_URL_DESC)
                 .withRequiredArg()
                 .describedAs("URL")
                 .ofType(String.class);
         op.acceptsAll(Arrays.asList(
                 Configuration.BI_PREPARE_PARTS_JDBC_USER),
                 Configuration.BI_PREPARE_PARTS_JDBC_USER_DESC)
                 .withRequiredArg()
                 .describedAs("NAME")
                 .ofType(String.class);
         op.acceptsAll(Arrays.asList(
                 Configuration.BI_PREPARE_PARTS_JDBC_PASSWORD),
                 Configuration.BI_PREPARE_PARTS_JDBC_PASSWORD_DESC)
                 .withRequiredArg()
                 .describedAs("NAME")
                 .ofType(String.class);
     }
 
     public void initUploadOptionParser(Properties props) {
         this.initPrepareOptionParser(props);
         op.acceptsAll(Arrays.asList(
                 Configuration.BI_UPLOAD_PARTS_AUTO_CREATE),
                 Configuration.BI_UPLOAD_PARTS_AUTO_CREATE_DESC)
                 .withRequiredArg()
                 .describedAs("DATABASE.TABLE")
                 .ofType(String.class)
                 .withValuesSeparatedBy(".");
         op.acceptsAll(Arrays.asList(
                 Configuration.BI_UPLOAD_PARTS_AUTO_PERFORM),
                 Configuration.BI_UPLOAD_PARTS_AUTO_PERFORM_DESC);
         op.acceptsAll(Arrays.asList(
                 Configuration.BI_UPLOAD_PARTS_AUTO_COMMIT),
                 Configuration.BI_UPLOAD_PARTS_AUTO_COMMIT_DESC);
         op.acceptsAll(Arrays.asList(
                 Configuration.BI_UPLOAD_PARTS_PARALLEL),
                 Configuration.BI_UPLOAD_PARTS_PARALLEL_DESC)
                 .withRequiredArg()
                 .describedAs("NUM")
                 .ofType(String.class);
         op.acceptsAll(Arrays.asList(
                 Configuration.BI_UPLOAD_PARTS_AUTO_DELETE),
                 Configuration.BI_UPLOAD_PARTS_AUTO_DELETE_DESC);
     }
 
     public void showHelp() throws IOException {
         // this method should be called after invoking initXXXOptionParser(..)
         op.printHelpOn(System.out);
     }
 
     public void setOptions(final String[] args) {
         options = op.parse(args);
     }
 
     public OptionSet getOptions() {
         return options;
     }
 }
