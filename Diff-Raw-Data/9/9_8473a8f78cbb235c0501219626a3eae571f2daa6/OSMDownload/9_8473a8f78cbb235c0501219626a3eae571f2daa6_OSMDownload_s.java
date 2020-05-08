 /* Copyright (c) 2013 OpenPlans. All rights reserved.
  * This code is licensed under the BSD New License, available at the root
  * application directory.
  */
 
 package org.geogit.osm.cli.commands;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.base.Preconditions.checkState;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 import org.geogit.api.GeoGIT;
 import org.geogit.api.Ref;
 import org.geogit.api.RevCommit;
 import org.geogit.api.SymRef;
 import org.geogit.api.plumbing.RefParse;
 import org.geogit.api.porcelain.BranchCreateOp;
 import org.geogit.api.porcelain.CheckoutOp;
 import org.geogit.api.porcelain.LogOp;
 import org.geogit.cli.AbstractCommand;
 import org.geogit.cli.CLICommand;
 import org.geogit.cli.GeogitCLI;
 import org.geogit.osm.internal.AddOSMLogEntry;
 import org.geogit.osm.internal.EmptyOSMDownloadException;
 import org.geogit.osm.internal.Mapping;
 import org.geogit.osm.internal.OSMDownloadReport;
 import org.geogit.osm.internal.OSMImportOp;
 import org.geogit.osm.internal.OSMLogEntry;
 import org.geogit.osm.internal.ReadOSMFilterFile;
 import org.geogit.osm.internal.ReadOSMLogEntries;
 import org.geogit.osm.internal.WriteOSMFilterFile;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.google.common.base.Charsets;
 import com.google.common.base.Joiner;
 import com.google.common.base.Optional;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Lists;
 import com.google.common.io.Files;
 
 /**
  * Imports data from OSM using the Overpass API
  * 
  * Data is filtered using an Overpass QL filter.
  * 
  * If the --update modifier is used, it updates previously imported OSM data, connecting again to
  * the Overpass API
  * 
  * It reuses the filter from the last import that can be reached from the the current branch
  * 
  * WARNING: The current update mechanism is not smart, since it downloads all data, not just the
  * elements modified/added since the last import (which is stored on the OSM log file)
  * 
  */
 @Parameters(commandNames = "download", commandDescription = "Download OpenStreetMap data")
 public class OSMDownload extends AbstractCommand implements CLICommand {
 
     private static final String OSM_FETCH_BRANCH = "OSM_FETCH";
 
     private static final String DEFAULT_API_ENDPOINT = "http://overpass-api.de/api/interpreter";
 
     private static final String FR_API_ENDPOINT = "http://api.openstreetmap.fr/oapi/interpreter/";
 
     private static final String RU_API_ENDPOINT = "http://overpass.osm.rambler.ru/";
 
     @Parameter(names = { "--filter", "-f" }, description = "The filter file to use.")
     private File filterFile;
 
     @Parameter(arity = 1, description = "<OSM Overpass api URL. eg: http://api.openstreetmap.org/api>", required = false)
     public List<String> apiUrl = Lists.newArrayList();
 
     @Parameter(names = { "--bbox", "-b" }, description = "The bounding box to use as filter (S W N E).", arity = 4)
     private List<String> bbox;
 
     @Parameter(names = "--saveto", description = "Directory where to save the dowloaded OSM data files.")
     public File saveFile;
 
     @Parameter(names = { "--keep-files", "-k" }, description = "If specified, downloaded files are kept in the --saveto folder")
     public boolean keepFiles = false;
 
     @Parameter(names = { "--update", "-u" }, description = "Update the OSM data currently in the geogit repository")
     public boolean update = false;
 
     @Parameter(names = { "--rebase" }, description = "Use rebase instead of merge when updating")
     public boolean rebase = false;
 
     @Parameter(names = { "--mapping" }, description = "The file that contains the data mapping to use")
     public File mappingFile;
 
     private String osmAPIUrl;
 
     private GeogitCLI cli;
 
     @Override
     protected void runInternal(GeogitCLI cli) throws Exception {
         checkState(cli.getGeogit() != null, "Not a geogit repository: " + cli.getPlatform().pwd());
         checkArgument(filterFile != null ^ bbox != null || update,
                 "You must specify a filter file or a bounding box");
         checkArgument((filterFile != null || bbox != null) ^ update,
                 "Filters cannot be used when updating");
         checkState(cli.getGeogit().getRepository().getIndex().countStaged(null)
                 + cli.getGeogit().getRepository().getWorkingTree().countUnstaged(null) == 0,
                 "Working tree and index are not clean");
         checkArgument(!rebase || update, "--rebase switch can only be used when updating");
         checkArgument(filterFile == null || filterFile.exists(),
                 "The specified filter file does not exist");
         checkArgument(bbox == null || bbox.size() == 4, "The specified bounding box is not correct");
 
         osmAPIUrl = resolveAPIURL();
 
         this.cli = cli;
         if (update) {
             update();
         } else {
             download();
         }
     }
 
     private void download() throws Exception {
 
         Mapping mapping = null;
         if (mappingFile != null) {
             mapping = Mapping.fromFile(mappingFile.getAbsolutePath());
         }
 
         OSMImportOp op = cli.getGeogit().command(OSMImportOp.class).setDataSource(osmAPIUrl)
                 .setDownloadFile(saveFile).setMapping(mapping).setKeepFile(keepFiles);
 
         String filter = null;
         if (filterFile != null) {
             try {
                 filter = readFile(filterFile);
             } catch (IOException e) {
                 throw new IllegalArgumentException("Error reading filter file:" + e.getMessage(), e);
             }
         } else if (bbox != null) {
             filter = "way(" + bbox.get(0) + "," + bbox.get(1) + "," + bbox.get(2) + ","
                     + bbox.get(3) + ");\n(._;>;);\nout meta;";
         }
 
         try {
             Optional<OSMDownloadReport> report = op.setFilter(filter)
                     .setProgressListener(cli.getProgressListener()).call();
 
             if (!report.isPresent()) {
                 return;
             }
 
             if (report.get().getUnpprocessedCount() > 0) {
                 cli.getConsole().println(
                         "Some elements returned by the specified filter could not be processed.\nProcessed entities: "
                                 + report.get().getCount() + "\nWrong or uncomplete elements: "
                                 + report.get().getUnpprocessedCount());
             }
 
             cli.execute("add");
             String message = "Updated OSM data";
             cli.execute("commit", "-m", message);
             OSMLogEntry entry = new OSMLogEntry(cli.getGeogit().getRepository().getWorkingTree()
                     .getTree().getId(), report.get().getLatestChangeset(), report.get()
                     .getLatestTimestamp());
             cli.getGeogit().command(AddOSMLogEntry.class).setEntry(entry).call();
             cli.getGeogit().command(WriteOSMFilterFile.class).setEntry(entry).setFilterCode(filter)
                     .call();
 
         } catch (EmptyOSMDownloadException e) {
             throw new IllegalArgumentException("The specified filter did not return any element.\n"
                     + "No changes were made to the repository.\n"
                     + "To check the downloaded elements, use the --saveto and"
                     + " --keep-files options and verify the intermediate file.");
 
         } catch (RuntimeException e) {
             throw new IllegalStateException("Error importing OSM data: " + e.getMessage(), e);
         }
 
     }
 
     private void update() throws Exception {
         GeoGIT geogit = cli.getGeogit();
         final Optional<Ref> currHead = geogit.command(RefParse.class).setName(Ref.HEAD).call();
         Preconditions.checkState(currHead.isPresent(), "Repository has no HEAD, can't update.");
         Preconditions.checkState(currHead.get() instanceof SymRef,
                 "Can't update from detached HEAD");
 
         List<OSMLogEntry> entries = geogit.command(ReadOSMLogEntries.class).call();
         checkArgument(!entries.isEmpty(), "Not in a geogit repository with OSM data");
 
         Iterator<RevCommit> log = geogit.command(LogOp.class).setFirstParentOnly(false)
                 .setTopoOrder(false).call();
 
         RevCommit lastCommit = null;
         OSMLogEntry lastEntry = null;
         while (log.hasNext()) {
             RevCommit commit = log.next();
             for (OSMLogEntry entry : entries) {
                 if (entry.getId().equals(commit.getTreeId())) {
                     lastCommit = commit;
                     lastEntry = entry;
                     break;
                 }
             }
         }
         checkNotNull(lastCommit, "The current branch does not contain OSM data");
 
         geogit.command(BranchCreateOp.class).setSource(lastCommit.getId().toString())
                 .setName(OSM_FETCH_BRANCH).setAutoCheckout(true).setForce(true).call();
 
         Optional<String> filter = geogit.command(ReadOSMFilterFile.class).setEntry(lastEntry)
                 .call();
 
         Preconditions.checkState(filter.isPresent(), "Filter file not found");
 
         Optional<OSMDownloadReport> report = geogit.command(OSMImportOp.class)
                 .setFilter(filter.get()).setDataSource(resolveAPIURL())
                 .setProgressListener(cli.getProgressListener()).call();
 
         OSMLogEntry entry = new OSMLogEntry(cli.getGeogit().getRepository().getWorkingTree()
                 .getTree().getId(), report.get().getLatestChangeset(), report.get()
                 .getLatestTimestamp());
 
         if (!report.isPresent()) {
             return;
         }
 
         cli.getConsole().println();
         if (cli.getGeogit().getRepository().getWorkingTree().countUnstaged(null) != 0) {
             cli.execute("add");
             String message = "Updated OSM data";
             cli.execute("commit", "-m", message);
             cli.getGeogit().command(AddOSMLogEntry.class).setEntry(entry).call();
             cli.getGeogit().command(WriteOSMFilterFile.class).setEntry(entry)
                     .setFilterCode(filter.get()).call();
         } else {
             // no changes, so we exit and do not continue with the merge
             cli.getConsole().println("No changes found");
             return;
         }
 
        geogit.command(CheckoutOp.class).setSource(currHead.get().getName()).call();
 
         if (rebase) {
             cli.execute("rebase", OSM_FETCH_BRANCH);
         } else {
             cli.execute("merge", OSM_FETCH_BRANCH);
         }
 
     }
 
     private String readFile(File file) throws IOException {
         List<String> lines = Files.readLines(file, Charsets.UTF_8);
         return Joiner.on("\n").join(lines);
     }
 
     private String resolveAPIURL() {
         String osmAPIUrl;
         if (apiUrl.isEmpty()) {
             osmAPIUrl = DEFAULT_API_ENDPOINT;
         } else {
             osmAPIUrl = apiUrl.get(0);
         }
         return osmAPIUrl;
     }
 
 }
