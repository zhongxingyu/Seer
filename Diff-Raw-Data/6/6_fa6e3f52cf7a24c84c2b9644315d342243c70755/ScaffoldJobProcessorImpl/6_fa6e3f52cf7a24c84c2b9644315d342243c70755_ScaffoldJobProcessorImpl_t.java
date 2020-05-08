 /********************************************************************************
  * Copyright (c) 2009 Regents of the University of Minnesota
  *
  * This Software was written at the Minnesota Supercomputing Institute
  * http://msi.umn.edu
  *
  * All rights reserved. The following statement of license applies
  * only to this file, and and not to the other files distributed with it
  * or derived therefrom.  This file is made available under the terms of
  * the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Minnesota Supercomputing Institute - initial API and implementation
  *******************************************************************************/
 
 package edu.umn.msi.tropix.proteomics.scaffold.impl;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.io.FilenameUtils;
 
 import com.google.common.base.Function;
 import com.google.common.base.Functions;
 import com.google.common.base.Supplier;
 import com.google.common.collect.Iterables;
 
 import edu.umn.msi.tropix.common.io.Directories;
 import edu.umn.msi.tropix.common.io.FileUtils;
 import edu.umn.msi.tropix.common.io.FileUtilsFactory;
 import edu.umn.msi.tropix.common.io.IOUtils;
 import edu.umn.msi.tropix.common.io.IOUtilsFactory;
 import edu.umn.msi.tropix.common.io.InputContext;
 import edu.umn.msi.tropix.common.io.InputContexts;
 import edu.umn.msi.tropix.common.io.OutputContext;
 import edu.umn.msi.tropix.common.io.TempFileSuppliers;
 import edu.umn.msi.tropix.common.io.ZipUtils;
 import edu.umn.msi.tropix.common.io.ZipUtilsFactory;
 import edu.umn.msi.tropix.common.jobqueue.jobprocessors.BaseExecutableJobProcessorImpl;
 import edu.umn.msi.tropix.common.jobqueue.progress.LineProcessingFileProgressTracker;
 import edu.umn.msi.tropix.common.jobqueue.progress.ProgressTrackable;
 import edu.umn.msi.tropix.common.jobqueue.progress.ProgressTracker;
import edu.umn.msi.tropix.common.xml.FormattedXmlUtility;
 import edu.umn.msi.tropix.proteomics.scaffold.input.BiologicalSample;
 import edu.umn.msi.tropix.proteomics.scaffold.input.Experiment;
 import edu.umn.msi.tropix.proteomics.scaffold.input.Export;
 import edu.umn.msi.tropix.proteomics.scaffold.input.FastaDatabase;
 import edu.umn.msi.tropix.proteomics.scaffold.input.Scaffold;
 import edu.umn.msi.tropix.proteomics.xml.ScaffoldUtility;
 
 class ScaffoldJobProcessorImpl extends BaseExecutableJobProcessorImpl implements ProgressTrackable {
   private static final Supplier<File> TEMP_FILE_SUPPLIER = TempFileSuppliers.getDefaultTempFileSupplier();
   private static final FileUtils FILE_UTILS = FileUtilsFactory.getInstance();
   private static final IOUtils IO_UTILS = IOUtilsFactory.getInstance();
   private static final ZipUtils ZIP_UTILS = ZipUtilsFactory.getInstance();
   private static final ScaffoldUtility SCAFFOLD_XML_UTILITY = new ScaffoldUtility();
  private static final FormattedXmlUtility<Scaffold> SCAFFOLD_WRITER = new FormattedXmlUtility<Scaffold>(Scaffold.class);
   private static final String XML_DRIVER_PATH = "input.xml", INPUT_PATH = "input", OUTPUT_PATH = "output", DB_PATH = "db";
 
   private boolean quiteMode = false;
   private String keyPath = null;
   private LineProcessingFileProgressTracker fileProgressTracker = null;
   private Scaffold scaffoldInput;
   private Iterable<InputContext> downloadContexts;
   private int version = 2;
 
   @Override
   protected void initialize() {
     scaffoldInput = SCAFFOLD_XML_UTILITY.deserialize(getStagingDirectory().getInputContext(XML_DRIVER_PATH));
     initializeOutputTracker();
   }
 
   public void setFileProgressTracker(final LineProcessingFileProgressTracker fileProgressTracker) {
     this.fileProgressTracker = fileProgressTracker;
   }
 
   private List<String> getInputPaths(final Scaffold scaffold) {
     final List<String> contexts = new LinkedList<String>();
 
     final List<FastaDatabase> databases = scaffoldInput.getExperiment().getFastaDatabase();
     for(final FastaDatabase database : databases) {
       final String path = database.getPath();
       contexts.add(getRelativePath(path)); // stagingDirectory.getOutputContext(getRelativePath(path)));
     }
 
     final List<BiologicalSample> samples = scaffold.getExperiment().getBiologicalSample();
     for(final BiologicalSample sample : samples) {
       for(final String path : sample.getInputFile()) {
         contexts.add(getRelativePath(path)); // stagingDirectory.getOutputContext(getRelativePath(path)));
       }
     }
 
     return contexts;
   }
 
   private void addFullPathToScaffoldInputFiles(final String inputDirectory, final String databaseDirectory, final Scaffold scaffold) {
     final Experiment experiment = scaffoldInput.getExperiment();
     final List<FastaDatabase> databases = experiment.getFastaDatabase();
     for(final FastaDatabase database : databases) {
       final String path = database.getPath();
       final String sanitizedPath = FilenameUtils.getName(path);
       database.setPath(databaseDirectory + getStagingDirectory().getSep() + sanitizedPath);
     }
 
     final List<BiologicalSample> samples = scaffold.getExperiment().getBiologicalSample();
     for(final BiologicalSample sample : samples) {
       for(int i = 0; i < sample.getInputFile().size(); i++) {
         final String relativePath = sample.getInputFile().get(i);
         final String sanitizedPath = FilenameUtils.getName(relativePath);
         sample.getInputFile().set(i, inputDirectory + getStagingDirectory().getSep() + sanitizedPath);
       }
     }
   }
 
   private void addFullPathToScaffoldOutputFiles(final String outputDirectory, final Scaffold scaffold) {
     final List<Export> exports = scaffold.getExperiment().getExport();
     for(final Export export : exports) {
       final String relativePath = export.getPath();
       final String sanitizedPath = FilenameUtils.getName(relativePath);
       final String fullPath = outputDirectory + getStagingDirectory().getSep() + sanitizedPath;
       export.setPath(fullPath);
     }
   }
 
   private String getRelativePath(final String absolutePath) {
     return absolutePath.substring(getStagingDirectory().getAbsolutePath().length() + getStagingDirectory().getSep().length());
   }
 
   private Iterable<String> getOutputFiles(final Scaffold scaffold) {
     final List<Export> exports = scaffold.getExperiment().getExport();
     return Iterables.transform(exports, new Function<Export, String>() {
       public String apply(final Export export) {
         return getRelativePath(export.getPath());
       }
     });
   }
 
   private void initializeOutputTracker() {
     if(fileProgressTracker != null) {
       final File stdoutFile = new File(getStagingDirectory().getAbsolutePath(), "output_log");
       // Don't try to create a remote log...
       final File parent = stdoutFile.getParentFile();
       if(parent != null && parent.exists()) {
         this.getJobDescription().getJobDescriptionType().setStdout(stdoutFile.getAbsolutePath());
         FILE_UTILS.touch(stdoutFile);
         fileProgressTracker.setTrackedFile(stdoutFile);
         fileProgressTracker.setLineCallback(new ScaffoldLineCallbackImpl());
       }
     }
   }
 
   @Override
   protected void doPreprocessing() {
     getStagingDirectory().makeDirectory(INPUT_PATH);
     getStagingDirectory().makeDirectory(OUTPUT_PATH);
     getStagingDirectory().makeDirectory(DB_PATH);
 
     final String inputAbsPath = Directories.buildAbsolutePath(getStagingDirectory(), INPUT_PATH);
     final String dbAbsPath = Directories.buildAbsolutePath(getStagingDirectory(), DB_PATH);
     addFullPathToScaffoldInputFiles(inputAbsPath, dbAbsPath, scaffoldInput);
     addFullPathToScaffoldOutputFiles(Directories.buildAbsolutePath(getStagingDirectory(), OUTPUT_PATH), scaffoldInput);
     // InputContexts.putIntoOutputContexts(downloadContexts, getInputContexts(scaffoldInput));
     // Iterator<OutputContext> iIter = getInputContexts(scaffoldInput).iterator();
     final Iterator<InputContext> oIter = downloadContexts.iterator();
     for(final String inputPath : getInputPaths(scaffoldInput)) {
       final InputContext inputContext = oIter.next();
       final OutputContext outputContext = getStagingDirectory().getOutputContext(inputPath);
       if(inputPath.endsWith(".omx")) {
         FileInputStream tempFileInputStream = null;
         final File tempFile = TEMP_FILE_SUPPLIER.get();
         try {
           inputContext.get(tempFile);
           tempFileInputStream = FILE_UTILS.getFileInputStream(tempFile);
           if(IO_UTILS.isZippedStream(tempFileInputStream)) {
             ZIP_UTILS.unzipToContexts(tempFile, Functions.constant(outputContext));
           } else {
             InputContexts.forFile(tempFile).get(outputContext);
           }
         } finally {
           FILE_UTILS.deleteQuietly(tempFile);
           IO_UTILS.closeQuietly(tempFileInputStream);
         }
       } else {
         inputContext.get(outputContext);
       }
     }
 
    SCAFFOLD_WRITER.serialize(scaffoldInput, getStagingDirectory().getOutputContext(XML_DRIVER_PATH));
 
     final List<String> arguments = new LinkedList<String>();
     arguments.add("-f");
     if(quiteMode) {
       arguments.add("-q");
     }
     if(keyPath != null) {
       arguments.add("-keypath");
       arguments.add(keyPath);
     }
     arguments.add(Directories.buildAbsolutePath(getStagingDirectory(), XML_DRIVER_PATH));
     this.getJobDescription().getJobDescriptionType().setArgument(arguments.toArray(new String[] {}));
     initializeOutputTracker();
   }
 
   @Override
   public void doPostprocessing() {
     if(wasCompletedNormally()) {
       for(final String file : getOutputFiles(scaffoldInput)) {
         getResourceTracker().add(getStagingDirectory().getInputContext(file));
       }
     }
   }
 
   /**
    * Should reference the absolute path to the file.
    * 
    * @param keyPath
    */
   public void setKeyPath(final String keyPath) {
     this.keyPath = keyPath;
   }
 
   public void setScaffoldInput(final Scaffold scaffoldInput) {
     this.scaffoldInput = scaffoldInput;
   }
 
   public void setQuiteMode(final boolean quiteMode) {
     this.quiteMode = quiteMode;
   }
 
   public ProgressTracker getProgressTracker() {
     return fileProgressTracker;
   }
 
   public void setDownloadContexts(final Iterable<InputContext> downloadContexts) {
     this.downloadContexts = downloadContexts;
   }
 }
