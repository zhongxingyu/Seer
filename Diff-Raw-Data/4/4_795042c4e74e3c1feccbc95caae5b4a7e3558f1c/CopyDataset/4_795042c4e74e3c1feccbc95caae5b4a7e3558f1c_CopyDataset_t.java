 package com.socrata.tools;
 
 import com.google.common.collect.Lists;
 import com.socrata.api.HttpLowLevel;
 import com.socrata.api.Soda2Consumer;
 import com.socrata.api.Soda2Producer;
 import com.socrata.api.SodaDdl;
 import com.socrata.builders.SoqlQueryBuilder;
 import com.socrata.exceptions.LongRunningQueryException;
 import com.socrata.exceptions.SodaError;
 import com.socrata.model.UpsertResult;
 import com.socrata.model.importer.Column;
 import com.socrata.model.importer.Dataset;
 import com.socrata.model.importer.DatasetInfo;
 import com.socrata.model.soql.OrderByClause;
 import com.socrata.model.soql.SoqlQuery;
 import com.socrata.model.soql.SortOrder;
 import com.socrata.tools.utils.ConfigurationLoader;
 import com.socrata.tools.model.SocrataConnectionInfo;
 import com.socrata.tools.utils.CliUtils;
 import com.sun.jersey.api.client.ClientResponse;
 import org.apache.commons.cli.*;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.tuple.Pair;
 
 import javax.annotation.Nonnull;
 import javax.ws.rs.core.MediaType;
 import java.io.*;
 import java.nio.charset.Charset;
 import java.util.List;
 import java.util.zip.GZIPInputStream;
 
 /**
  */
 public class CopyDataset
 {
 
    public static final Option DEST_DOMAIN   = OptionBuilder.withArgName("destUrl" )
                                          .hasArg()
                                          .withDescription(  "The url for the destination domain, e.g. https://foo.bar.baz .  This defaults to being the same domain as the source domain" )
                                          .create("d");
 
     public static final Option SOURCE_DOMAIN   = OptionBuilder.withArgName("srcDomain")
                                           .hasArg()
                                           .withDescription(  "The url for the source domain.  This defaults to what is loaded in the connection config." )
                                           .create("s");
 
     public static final Option CONFIG_FILE   = OptionBuilder.withArgName("connectionConfig")
                                           .hasArg()
                                           .withDescription(  "The configuration file to load for user source domain URL/name/password/apptoken.  Defaults to ~/.socrata/connection.json" )
                                           .create("c");
 
     public static final Option DEST_CONFIG_FILE   = OptionBuilder.withArgName("destConnectionConfig")
                                                             .hasArg()
                                                             .withDescription(  "The configuration file to load for user destination domain URL/name/password/apptoken.  Defaults to whatever is used for the source domain." )
                                                             .create("x");
 
 
     public static final Option DATA_FILE   = OptionBuilder.withArgName("dataFile")
                                            .hasArg()
                                            .withDescription("The directory to look for data files to upload to the newly created dataset.  " +
                                                                     "The tool will look for files that have the same name as the dataset id they go with.  " +
                                                                     "These can be json or csv.  In addition, the can be gzipped.")
                                            .create("f");
 
     public static final Option CREATE_ONLY   = OptionBuilder.withArgName("createOnly")
                                            .withDescription("Don't copy over data, ONLY create the new dataset.")
                                            .create("C");
 
     public static final Option COPY_DATA   = OptionBuilder.withArgName("copyDataLive")
                                                             .withDescription("Do NOT use a file to import data.  Copy it directly from the live dataset.")
                                                             .create("p");
 
 
     public static final Option CREATE_OPTIONS   = OptionBuilder.withArgName("createOptions")
                                             .hasArg()
                                             .withDescription("This adds an option that should be passed on the URL when creating the dataset.  E.g. $$testflag=true .")
                                             .create("o");
 
     public static final Option USAGE_OPTIONS   = OptionBuilder.withArgName("?")
                                                                .withDescription("Shows usage.")
                                                                .create("?");
 
 
     public static final Options OPTIONS = new Options();
 
     static {
         OPTIONS.addOption(DEST_DOMAIN);
         OPTIONS.addOption(SOURCE_DOMAIN);
         OPTIONS.addOption(CONFIG_FILE);
         OPTIONS.addOption(DATA_FILE);
         OPTIONS.addOption(CREATE_ONLY);
         OPTIONS.addOption(CREATE_OPTIONS);
         OPTIONS.addOption(COPY_DATA);
         OPTIONS.addOption(USAGE_OPTIONS);
         OPTIONS.addOption(DEST_CONFIG_FILE);
     }
 
 
     final boolean createOnly;
     final boolean copyDataLive;
     final SocrataConnectionInfo srcConnectionInfo;
     final SocrataConnectionInfo destConnectionInfo;
     final String srcDomain;
     final String destDomain;
     final File   dataFileDir;
     final List<Pair<String, String>> parsedCreateOptions;
 
     /**
      * DatasetId
      * userName
      * userPassword
      * token
      * @param args
      */
     public static void main(String[] args) throws SodaError, InterruptedException, IOException, LongRunningQueryException
     {
 
         CommandLineParser   parser = new PosixParser();
 
 
         try {
             CommandLine         cmd = parser.parse(OPTIONS, args, false);
             String configFile = cmd.getOptionValue("c", CliUtils.defaultConfigFile().getCanonicalPath());
             String destConfigFile = cmd.getOptionValue("x", configFile);
 
             if (cmd.hasOption("?")) {
                 HelpFormatter formatter = new HelpFormatter();
                 formatter.printHelp( "copydataset", OPTIONS );
                 System.exit(1);
             }
 
             try {
 
                 final File config = new File(configFile);
                 if (!config.canRead()) {
                     throw new IllegalArgumentException("Unable to load connection configuration from " + configFile + ".  Either use the -c option or setup a connection file there.");
                 }
 
                 final File destConfig = new File(destConfigFile);
                 if (!destConfig.canRead()) {
                     throw new IllegalArgumentException("Unable to load connection configuration from " + destConfig + ".  Either use the -x option or setup a connection file there.");
                 }
 
 
                 final SocrataConnectionInfo connectionInfo = ConfigurationLoader.loadSocrataConnectionConfig(config);
                 CliUtils.validateConfiguration(connectionInfo);
 
                 final SocrataConnectionInfo destConnectionInfo = ConfigurationLoader.loadSocrataConnectionConfig(destConfig);
                 CliUtils.validateConfiguration(destConnectionInfo);
 
 
                 final String srcDomain = cmd.getOptionValue("s", connectionInfo.getUrl());
                 if (StringUtils.isEmpty(srcDomain)) {
                     throw new IllegalArgumentException("No source domain specified in either the connection configuration or the commandline arguments.");
                 }
 
                 final String destDomain = cmd.getOptionValue("d", srcDomain);
                 final String createOptions = cmd.getOptionValue("o");
                 final List<Pair<String, String>> parsedCreateOptions = CliUtils.parseOptions(createOptions, Charset.defaultCharset());
                 final File dataFileDir = new File(cmd.getOptionValue("f", "."));
                 final boolean copyDataLive = cmd.hasOption("p");
                 final boolean createOnly = cmd.hasOption("C");
 
 
                 final Writer      output = new OutputStreamWriter(System.out);
                 final CopyDataset copyDataset = new CopyDataset(srcDomain, destDomain, connectionInfo, destConnectionInfo, dataFileDir, parsedCreateOptions, createOnly, copyDataLive);
                 final List<Pair<Dataset, UpsertResult>> results = copyDataset.doCopy(cmd.getArgs(), output);
                 output.flush();
 
                 for (Pair<Dataset, UpsertResult> result : results) {
                     System.out.println("Created dataset " + destDomain + "/id/" + result.getKey().getId() + ".  Created " + result.getValue().getRowsCreated());
                 }
 
             } catch (IllegalArgumentException e) {
                 System.out.println(e.getMessage());
                 HelpFormatter formatter = new HelpFormatter();
                 formatter.printHelp( "copydataset", OPTIONS );
                 System.exit(1);
             }
         } catch (ParseException e) {
             System.err.println( "Parsing failed.  Reason: " + e.getMessage() );
 
             HelpFormatter formatter = new HelpFormatter();
             formatter.printHelp( "copydataset", OPTIONS );
             System.exit(1);
        } catch (Exception e) {
            System.err.println( "Failure copying dataset: " + e.getMessage() );
            e.printStackTrace();
            System.exit(1);
         }
     }
 
     public CopyDataset(String srcDomain, String destDomain, SocrataConnectionInfo srcConnectionInfo, SocrataConnectionInfo destConnectionInfo, File dataFileDir, List<Pair<String, String>> parsedCreateOptions, boolean createOnly, boolean copyDataLive)
     {
         this.srcDomain = srcDomain;
         this.destDomain = destDomain;
         this.srcConnectionInfo = srcConnectionInfo;
         this.destConnectionInfo = destConnectionInfo;
         this.dataFileDir = dataFileDir;
         this.parsedCreateOptions = parsedCreateOptions;
         this.createOnly = createOnly;
         this.copyDataLive = copyDataLive;
     }
 
     public List<Pair<Dataset, UpsertResult>> doCopy(String[] datasetIds, Writer output) throws SodaError, InterruptedException, LongRunningQueryException, IOException
     {
         List<Pair<Dataset, UpsertResult>>   results = Lists.newArrayList();
         for (String datasetId : datasetIds) {
             results.add(doCopy(datasetId, output));
         }
         return results;
     }
 
     public Pair<Dataset, UpsertResult> doCopy(String datasetId, Writer output) throws SodaError, InterruptedException, LongRunningQueryException, IOException
     {
 
         final SodaDdl ddlSrc = SodaDdl.newDdl(srcDomain, srcConnectionInfo.getUser(), srcConnectionInfo.getPassword(), srcConnectionInfo.getToken());
         final SodaDdl ddlDest = SodaDdl.newDdl(destDomain, destConnectionInfo.getUser(), destConnectionInfo.getPassword(), destConnectionInfo.getToken());
 
 
         for (Pair<String, String> createOption : parsedCreateOptions) {
             ddlDest.getHttpLowLevel().getAdditionalParameters().put(createOption.getKey(), createOption.getValue());
         }
 
         final Dataset srcDataset = loadSourceSchema(ddlSrc, datasetId);
         final DatasetInfo destDatasetTemplate = Dataset.copy(srcDataset);
         final Dataset destDataset = createDestSchema(ddlDest, srcDataset, destDatasetTemplate, output);
 
         final Soda2Producer producerDest = Soda2Producer.newProducer(destDomain, destConnectionInfo.getUser(), destConnectionInfo.getPassword(), destConnectionInfo.getToken());
 
         UpsertResult    upsertResult = new UpsertResult(0, 0, 0, null);
 
         //Now for the data part
         if (!createOnly) {
             if (copyDataLive) {
                 upsertResult = copyDataLive(producerDest, srcDataset.getId(), destDataset.getId(), output);
             } else {
                 upsertResult = importDataFile(producerDest, destDataset.getId(), dataFileDir, output);
             }
         }
 
         return Pair.of(destDataset, upsertResult);
     }
 
 
     public static Dataset loadSourceSchema(SodaDdl ddlSrc, String datasetId) throws SodaError, InterruptedException
     {
         final DatasetInfo datasetInfo = ddlSrc.loadDatasetInfo(datasetId);
         if (!(datasetInfo instanceof Dataset)) {
             throw new SodaError("Can currently only copy datasets.");
         }
 
         return (Dataset) datasetInfo;
     }
 
     public static Dataset createDestSchema(SodaDdl ddlDest, Dataset srcDataset, DatasetInfo destDatasetTemplate, Writer output) throws SodaError, InterruptedException, IOException
     {
         destDatasetTemplate.setResourceName(null);
         Dataset newDataset = (Dataset) ddlDest.createDataset(destDatasetTemplate);
         if (output != null) {
             output.write("Created dataset " + newDataset.getName() + ".  4x4 is " + newDataset.getId() + "\n");
             output.flush();
         }
 
         for (Column column  : srcDataset.getColumns()) {
             ddlDest.addColumn(newDataset.getId(), column);
             if (output != null) {
                 output.write("Added column " + column.getName() + ".\n");
                 output.flush();
             }
         }
 
         ddlDest.publish(newDataset.getId());
         return (Dataset) ddlDest.loadDatasetInfo(newDataset.getId());
     }
 
     @Nonnull
     public static UpsertResult importDataFile(Soda2Producer producerDest, String destId, File dataFile, Writer output) throws IOException, SodaError, InterruptedException
     {
         if (!dataFile.exists()) {
             throw new SodaError(dataFile.getCanonicalPath() + " does not exist.\n");
         }
 
         String unprocecessedName = dataFile.getName();
         String extToProcess = getExtension(unprocecessedName);
         InputStream is = new FileInputStream(dataFile);
 
         if (extToProcess.equals(".gz")) {
             is = new GZIPInputStream(is);
             unprocecessedName = unprocecessedName.substring(0, unprocecessedName.length() - extToProcess.length());
             extToProcess = getExtension(unprocecessedName);
         }
 
         MediaType mediaType = HttpLowLevel.CSV_TYPE;
         if (extToProcess.equals(".json")) {
             mediaType = HttpLowLevel.JSON_TYPE;
         }
 
         if (output != null) {
             output.write("Upserting file " + dataFile.getCanonicalPath() + " as " + mediaType + "\n");
             output.flush();
         }
         return producerDest.upsertStream(destId, mediaType, is);
     }
 
     public UpsertResult copyDataLive(Soda2Producer producerDest, String srcId, String destId, Writer output) throws LongRunningQueryException, SodaError, InterruptedException, IOException
     {
 
         if (output != null) {
             output.write("Copying data live from " + srcId + ".\n");
             output.flush();
         }
         final Soda2Consumer querySource = Soda2Consumer.newConsumer(srcDomain, srcConnectionInfo.getUser(), srcConnectionInfo.getPassword(), srcConnectionInfo.getToken());
 
         SoqlQueryBuilder    builder = new SoqlQueryBuilder(SoqlQuery.SELECT_ALL)
                 .addOrderByPhrase(new OrderByClause(SortOrder.Ascending, ":id"))
                 .setLimit(1000);
 
         int         offset = 0;
         long        rowsAdded = 0;
         boolean     hasMore = true;
 
         while (hasMore) {
             ClientResponse response = querySource.query(srcId, HttpLowLevel.JSON_TYPE, builder.setOffset(offset).build());
             UpsertResult result = producerDest.upsertStream(destId, HttpLowLevel.JSON_TYPE, response.getEntityInputStream());
 
             offset+=1000;
             rowsAdded+=result.getRowsCreated();
 
             if (output != null) {
                 if ((rowsAdded % 40000) == 0) {
                     output.write('\n');
                     output.flush();
                 } else {
                     output.write('.');
                     output.flush();
                 }
             }
 
 
             if (result.getRowsCreated() == 0) {
                 hasMore = false;
             }
         }
         return new UpsertResult(rowsAdded, 0, 0, null);
     }
 
     @Nonnull
     private static String getExtension(String fileName) {
         final int lastDot = fileName.lastIndexOf('.');
         if (lastDot == -1 || lastDot==fileName.length()) {
             return "";
         }
 
         return fileName.substring(lastDot);
     }
 }
