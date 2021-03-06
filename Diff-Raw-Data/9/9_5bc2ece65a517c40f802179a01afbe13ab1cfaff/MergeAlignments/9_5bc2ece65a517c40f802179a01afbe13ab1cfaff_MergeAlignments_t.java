 // Copyright (C) 2011 CRS4.
 // 
 // This file is part of Seal.
 // 
 // Seal is free software: you can redistribute it and/or modify it
 // under the terms of the GNU General Public License as published by the Free
 // Software Foundation, either version 3 of the License, or (at your option)
 // any later version.
 // 
 // Seal is distributed in the hope that it will be useful, but
 // WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 // or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 // for more details.
 // 
 // You should have received a copy of the GNU General Public License along
 // with Seal.  If not, see <http://www.gnu.org/licenses/>.
 
 package it.crs4.seal.read_sort;
 
 import it.crs4.seal.read_sort.BwaRefAnnotation;
 import it.crs4.seal.common.SealToolRunner;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.InputStream;
 import java.io.Writer;
 import java.io.OutputStreamWriter;
 import java.io.BufferedWriter;
 import java.util.Map;
 import java.util.HashMap;
 
 
 import org.apache.commons.cli.*;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.FileUtil;
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IOUtils;
 import org.apache.hadoop.util.Tool;
 
 
 public class MergeAlignments extends Configured implements Tool
 {
 	private String userInput;
 	private String userOutput;
 	private String userAnnotation;
 	private String sortOrder = "coordinate";
 
 	private BwaRefAnnotation refAnnotation;
 
 	private Path annotationPath;
 	private Path[] inputPaths;
 	private Path outputPath;
 
 	private Map<String, String> readGroupFields;
 
 	private static final Log log = LogFactory.getLog(MergeAlignments.class); // log must not go to standard output.
 
 	private Path getQualifiedPath(String simplePath) throws IOException
 	{
 		Path path = new Path(simplePath);
 		return path.makeQualified(path.getFileSystem(getConf()));
 	}
 
 	private Map<String, Option> defineRGOptions()
 	{
  		Map<String, Option> readGroupOptions = new HashMap<String, Option>();
 
 		readGroupOptions.put("ID", 
 			              OptionBuilder
 			              .withDescription("Read group id")
 			              .hasArg()
 			              .withArgName("ID")
 			              .withLongOpt("rg-id")
 			              .create("rgid"));
 
 		readGroupOptions.put("SM",
 			              OptionBuilder
 			              .withDescription("Read group sample")
 			              .hasArg()
 			              .withArgName("sample")
 			              .withLongOpt("rg-sm")
 			              .create("rgsm"));
 
 		readGroupOptions.put("LB",
 			              OptionBuilder
 			              .withDescription("Read group library")
 			              .hasArg()
 			              .withArgName("library")
 			              .withLongOpt("rg-lb")
 			              .create("rglb"));
 
 		readGroupOptions.put("PU",
 			              OptionBuilder
 			              .withDescription("Read group platform unit")
 			              .hasArg()
 			              .withArgName("pu")
 			              .withLongOpt("rg-pu")
 			              .create("rgpu"));
 
 		readGroupOptions.put("CN",
 			              OptionBuilder
 			              .withDescription("Read group center")
 			              .hasArg()
 			              .withArgName("center")
 			              .withLongOpt("rg-cn")
 			              .create("rgcn"));
 
 		readGroupOptions.put("DT",
 			              OptionBuilder
 			              .withDescription("Read group date")
 			              .hasArg()
 			              .withArgName("date")
 			              .withLongOpt("rg-dt")
 			              .create("rgdt"));
 
 		readGroupOptions.put("PL",
 			              OptionBuilder
 			              .withDescription("Read group platform")
 			              .hasArg()
 			              .withArgName("platform")
 			              .withLongOpt("rg-pl")
 			              .create("rgpl"));
 
 		return readGroupOptions;
 	}
 
 	private Map<String, String> parseReadGroupOptions(Map<String, Option> readGroupOptions, CommandLine args) throws ParseException
 	{
 		HashMap<String, String> fields = new HashMap<String, String>();
 
 		for (Map.Entry<String, Option> pair: readGroupOptions.entrySet())
 		{
 			String fieldName = pair.getKey();
 			Option opt = pair.getValue();
 			if (args.hasOption(opt.getOpt()))
 			{
 				fields.put(fieldName, args.getOptionValue( opt.getOpt() ));
 			}
 		}
 
 		if (!fields.isEmpty())
 		{
 			if (!fields.containsKey("ID") || !fields.containsKey("SM"))
 				throw new ParseException("If you specify read group tags (RG) you must specify at least id and sample");
 		}
 		return fields;
 	}
 
 	/**
 	 * Scan command line and set configuration values appropriately.
 	 * Calls System.exit in case of a command line error.
 	 */
 	private void scanOptions(String[] args)
 	{
 		Configuration conf = getConf();
 		Options options = new Options();
 
 		Option ann = OptionBuilder
 			              .withDescription("annotation file (.ann) of the BWA reference used to create the SAM data")
 			              .hasArg()
 			              .withArgName("ref.ann")
 			              .withLongOpt("annotations")
 			              .create("ann");
 		options.addOption(ann);
 
 		Option optSortOrder = OptionBuilder
 			              .withDescription("Sort order.  Can be one of: unsorted, queryname, coordinate.  Default:  coordinate")
 			              .hasArg()
 			              .withArgName("sort order")
 			              .withLongOpt("sort-order")
 			              .create("so");
 		options.addOption(optSortOrder);
 
 		// read group options
 		Map<String, Option> readGroupOptions = defineRGOptions();
 		for (Option opt: readGroupOptions.values())
 			options.addOption(opt);
 
 		CommandLineParser parser = new GnuParser();
 
 		try 
 		{
 			CommandLine line = parser.parse( options, args );
 
 			if (line.hasOption(ann.getOpt()))
 				userAnnotation = line.getOptionValue(ann.getOpt());
 			else
 				throw new ParseException("You must provide the path to the reference annotation file (<ref>.ann)");
 
 			if (line.hasOption(optSortOrder.getOpt()))
 			{
 				String value = line.getOptionValue(optSortOrder.getOpt());
 				if ( "unordered".equals(value) || "queryname".equals(value) || "coordinate".equals(value) )
 					sortOrder = value;
 				else
 					throw new ParseException("Invalid sort order.  Sort order must be one of: unsorted, queryname, coordinate.");
 			}
 
 			// remaining args
 			String[] otherArgs = line.getArgs();
 			if (otherArgs.length == 1 || otherArgs.length == 2) 
 			{
 				userInput = otherArgs[0];
 				if (otherArgs.length == 1)
 					userOutput = null;
 				else
 					userOutput = otherArgs[1];
 			}
 			else
 				throw new ParseException("You must provide an HDFS input path and, optionally, an output path.");
 			readGroupFields = parseReadGroupOptions(readGroupOptions, line);
 		}
 		catch( ParseException e ) 
 		{
 			System.err.println("Usage error: " + e.getMessage());
 			// XXX: redirect System.out to System.err since the simple version of 
 			// HelpFormatter.printHelp prints to System.out, and we're on a way to
 			// a fatal exit.
 			System.setOut(System.err);
 			HelpFormatter formatter = new HelpFormatter();
 			formatter.printHelp( "MergeAlignments [options] -ann <ref>.ann <in> [<out>]", options);
 			System.exit(1);
 		}
 	}
 
 	private void loadAnnotations() throws IOException
 	{
 		Path annPath = getQualifiedPath(userAnnotation);
 		FileSystem fs = annPath.getFileSystem(getConf());
 		log.info("using annotation file " + annPath);
 		try
 		{
 			FSDataInputStream in = fs.open(annPath);
 			refAnnotation = new BwaRefAnnotation(new InputStreamReader(in));
 		}
 		catch (IOException e)
 		{
 			log.fatal("Can't read annotation file " + annPath + " on filesystem " + fs.getUri());
 			throw e;
 		}
 	}
 
 	private static class SourcePathFilter implements org.apache.hadoop.fs.PathFilter
 	{
 		public boolean accept(Path p)
 		{
 			boolean decision = true;
 			if (p.getName().toString().startsWith("_"))
 				decision = false;
 
 			return decision;
 		}
 	}
 
 	private Path[] getSourcePaths() throws Exception
 	{
 		Path srcPath = new Path(userInput);
     FileSystem srcFs = srcPath.getFileSystem(getConf());
 		if (srcFs.exists(srcPath))
 		{
 			FileStatus stat = srcFs.getFileStatus(srcPath);
 			if (stat.isDir())
 			{
 				String msg = "source path " + srcPath + " is a directory.  Globbing with ";
 				srcPath = new Path(srcPath, "*");
 				log.info(msg + srcPath);
 			}
 		}
 
 		// Glob source path.  The returned paths are already sorted.  We filter out paths starting 
 		// with '_' (see SourcePathFilter).
 		// If the path doesn't contain a glob patter, and it doesn't exist, the function will return null.
		Path[] sources = FileUtil.stat2Paths(srcFs.globStatus(srcPath, new SourcePathFilter()));
 		if (sources == null)
			throw new IllegalArgumentException("Source path " + srcPath.makeQualified(srcFs) + " doesn't exist");
 
 		if (log.isDebugEnabled())
 		{
 			log.debug("Sources:");
 			for (int i = 0; i < sources.length; ++i)
 				log.debug(sources[i]);
 		}
 
 		if (sources.length == 0)
 			throw new IllegalArgumentException("no source files selected");

		log.info("Merging " + sources.length + " files.");

 		return sources;
 	}
 
 	private void writeSamHeader(OutputStream rawOut) throws IOException
 	{
 		Writer out = 
 			new BufferedWriter(
 					new OutputStreamWriter(rawOut));
 		out.write("@HD\tVN:1.0\tSO:" + sortOrder + "\n");
 
 		for (BwaRefAnnotation.Contig c: refAnnotation)
 			out.write( String.format("@SQ\tSN:%s\tLN:%d\n", c.getName(), c.getLength()) );
 
 		// @PG:  Seal name and version
 		String version = "not available";
 		Package pkg = this.getClass().getPackage();
 		if (pkg != null && pkg.getImplementationVersion() != null)
 			version = pkg.getImplementationVersion();
 		else
 			log.warn("Could not get package version");
 
 		out.write("@PG\tID:seal\tVN:" +  version + "\n");
 
 		// @RG, if provided
 		if (!readGroupFields.isEmpty())
 		{
 			StringBuilder rgBuilder = new StringBuilder("@RG");
 			for (Map.Entry<String, String> pair: readGroupFields.entrySet())
 				rgBuilder.append("\t").append(pair.getKey()).append(":").append(pair.getValue());
 			rgBuilder.append("\n");
 
 			out.write( rgBuilder.toString() );
 		}
 
 		out.flush();
 	}
 
 	private void copyMerge(Path[] sources, OutputStream out) throws IOException 
 	{
 		Configuration conf = getConf();
     
 		for (int i = 0; i < sources.length; ++i)
 		{
 			FileSystem fs = sources[i].getFileSystem(conf);
 			InputStream in = fs.open(sources[i]);
 			try 
 			{
 				IOUtils.copyBytes(in, out, conf, false);
 			}
 			finally {
 				in.close();
 			} 
 		}
 	}
 
 
 	/**
 	 * Make an OutputStream to write to the user selected userOutput.
 	 */
 	private OutputStream getOutputStream() throws IOException
 	{
 		OutputStream out;
 		if (userOutput == null) // Write to stdout
 		{
 			log.info("writing to standard out");
 			out = System.out;
 		}
 		else
 		{
 			Path destPath = getQualifiedPath(userOutput);
 			FileSystem destFs = destPath.getFileSystem(getConf());
 			/*
 			if (destFs.getUri().equals(FileSystem.getLocal().getUri()))
 			{
 				// We're wring to the local file system
 			}*/
 			if (destFs.exists(destPath))
 				throw new RuntimeException("Output destination " + destPath + " exists.  Please remove it or change the output path.");
 
 			log.info("Merging sources to " + destPath);
 			out = destFs.create(destPath);
 		}
 		return out;
 	}
 
 	public int run(String[] args) throws Exception
 	{
 		scanOptions(args);
 		Configuration conf = getConf();
 
 		Path[] sources = getSourcePaths();
 
 		loadAnnotations();
 		log.info("Annotations read");
 
 		OutputStream destFile = getOutputStream();
 		try
 		{
 			writeSamHeader(destFile);
 			copyMerge(sources, destFile);
 		}
 		finally {
 			destFile.close();
 		}
 		log.info("Finished");
 
 		return 0;
 	}
 
 	public static void main(String[] args)
 	{
 		int res = 0;
 		try
 		{
 			res = new SealToolRunner().run(new MergeAlignments(), args);
 		}
 		catch (Exception e)
 		{
 			System.err.println("Error executing MergeAlignments: " + e.getMessage());
 			res = 1;
 		}
 		System.exit(res);
 	}
 }
