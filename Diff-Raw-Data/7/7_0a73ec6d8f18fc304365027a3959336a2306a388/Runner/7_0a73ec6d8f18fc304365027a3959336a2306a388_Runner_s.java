 /*
  * Copyright (c) 2011 Sergey Prilukin
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package jstreamserver;
 
 
 import anhttpserver.DefaultSimpleHttpServer;
 import anhttpserver.SimpleHttpServer;
 import jstreamserver.ftp.FtpServerWrapper;
 import jstreamserver.http.DispatcherHandler;
 import jstreamserver.utils.Config;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * entry point for <b>jstreamserver</b>
  *
  * @author Sergey Prilukin
  */
 public final class Runner {
     public static final String SYNTAX = "java -jar jstreamserver.jar";
     public static final String HEADER = "Jstreamserver simple static content HTTP server, version 0.1";
     public static final String FOOTER = "--END--";
 
     private static final Object monitor = new Object();
 
     private static Options options;
     static {
 
         //Server port
         Option port = new Option("p", "port", true, "Server port. By default: 8888");
         port.setArgs(1);
         port.setOptionalArg(false);
         port.setArgName("port");
         port.setRequired(false);
 
         //Server host
         Option host = new Option("h", "host", true, "Server host. By default: 0.0.0.0");
         host.setArgs(1);
         host.setOptionalArg(false);
         host.setArgName("host");
         host.setRequired(false);
 
         //Max threads count
         Option threads = new Option("t", "threads", true, "Max threads count. By default: 10");
         threads.setArgs(1);
         threads.setOptionalArg(false);
         threads.setArgName("threadsCount");
         threads.setRequired(false);
 
         //Path to mime config
         Option mime = new Option("m", "mime", true, "Path to MIME properties config file. Internal config will be used if not specified");
         mime.setArgs(1);
         mime.setOptionalArg(false);
         mime.setArgName("pathToFile");
         mime.setRequired(false);
 
         //Path to mime config
         Option resources = new Option("r", "resources", true, "Path to folder with static content");
         resources.setArgs(1);
         resources.setOptionalArg(false);
         resources.setArgName("folderName");
         resources.setRequired(false);
 
         //FFmppeg executble location
         Option ffmpegLocation = new Option("ff", "ffmpeg", true, "Full path to ffmpeg executable. By default not set");
         ffmpegLocation.setArgs(1);
         ffmpegLocation.setOptionalArg(false);
         ffmpegLocation.setArgName("ffmpegLocation");
         ffmpegLocation.setRequired(false);
 
         //FFmppeg executble location
         Option ffmpegParams = new Option("fp", "ffmpegp", true, "Params for ffmpeg.");
         ffmpegParams.setArgs(1);
         ffmpegParams.setOptionalArg(false);
         ffmpegParams.setArgName("ffmpegParams");
         ffmpegParams.setRequired(false);
 
         //FFmppeg executble location
         Option segmenterocation = new Option("se", "segmenter", true, "Full path to segmenter executable. By default not set");
         segmenterocation.setArgs(1);
         segmenterocation.setOptionalArg(false);
         segmenterocation.setArgName("segmenterLocation");
         segmenterocation.setRequired(false);
 
         //FFmppeg executble location
         Option segmenterDuration = new Option("sd", "sduration", true, "Duration of segment for HTTP Live streaming (in seconds)");
         segmenterDuration.setArgs(1);
         segmenterDuration.setOptionalArg(false);
         segmenterDuration.setArgName("segmentDuration");
         segmenterDuration.setRequired(false);
 
         //FFmppeg executble location
         Option segmenterWindowSize = new Option("sw", "swindow", true, "Window size of the segmenter for HTTP Live streaming");
         segmenterWindowSize.setArgs(1);
         segmenterWindowSize.setOptionalArg(false);
         segmenterWindowSize.setArgName("windowSize");
         segmenterWindowSize.setRequired(false);
 
         //FFmppeg executble location
         Option segmenterSearchKillFile = new Option("sk", "skillfile", true, "Search kill file for HTTP Live streaming");
         segmenterSearchKillFile.setArgs(1);
         segmenterSearchKillFile.setOptionalArg(false);
         segmenterSearchKillFile.setArgName("searchKillFile");
         segmenterSearchKillFile.setRequired(false);
 
         //FFmppeg executble location
         Option segmenterMaxTimeout = new Option("dt", "timeout", true, "Max segmenter timeout in msec for HTTP Live streaming");
         segmenterMaxTimeout.setArgs(1);
         segmenterMaxTimeout.setOptionalArg(false);
         segmenterMaxTimeout.setArgName("timeout");
         segmenterMaxTimeout.setRequired(false);
 
         Option defaultCharset = new Option("c", "charset", true, "Default text files encoding");
         defaultCharset.setArgs(1);
         defaultCharset.setOptionalArg(false);
         defaultCharset.setArgName("charset");
         defaultCharset.setRequired(false);
 
        Option ftpPort = new Option("fp", "ftpport", true, "Port for built-in FTP server");
         ftpPort.setArgs(1);
         ftpPort.setOptionalArg(false);
         ftpPort.setArgName("port");
         ftpPort.setRequired(false);
 
         //Root locations
         Option locations = new Option("f", "folders", true, "List of folders which will be shown in root.");
         locations.setArgs(Option.UNLIMITED_VALUES);
         locations.setOptionalArg(false);
         locations.setArgName("\"path1=label1\" \"path2=label2\" ...");
         locations.setRequired(true);
 
         //Create options
         options = new Options();
         options.addOption(port);
         options.addOption(host);
         options.addOption(threads);
         options.addOption(mime);
         options.addOption(resources);
         options.addOption(ffmpegLocation);
         options.addOption(ffmpegParams);
         options.addOption(segmenterocation);
         options.addOption(segmenterDuration);
         options.addOption(segmenterWindowSize);
         options.addOption(segmenterSearchKillFile);
         options.addOption(segmenterMaxTimeout);
         options.addOption(locations);
         options.addOption(ftpPort);
         options.addOption(defaultCharset);
     }
 
     private void start(Config config) throws IOException {
         System.out.print(config.toString());
 
         Properties mapping = new Properties();
         mapping.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(DispatcherHandler.MAPPING));
 
         DispatcherHandler dispatcherHandler = new DispatcherHandler();
         dispatcherHandler.setConfig(config);
         dispatcherHandler.setMapping(mapping);
 
         SimpleHttpServer server = new DefaultSimpleHttpServer();
         server.setMaxThreads(config.getMaxThreads());
         server.setHost(config.getHost());
         server.setPort(config.getPort());
         server.addHandler("/", dispatcherHandler);
         server.start();
     }
 
     /**
      * Create {@link Config} instance from passed params
      *
      * @param args command-line parameters
      * @return instance of {@link Config}
      */
     private static Config getConfig(String[] args) throws ParseException {
         CommandLine commandLine = parseCommandLine(options, args);
         Config config = new Config();
         if (commandLine.hasOption("p")) {
             config.setPort(Integer.parseInt(commandLine.getOptionValue("p")));
         }
 
         if (commandLine.hasOption("h")) {
             config.setHost(commandLine.getOptionValue("h"));
         }
 
         if (commandLine.hasOption("t")) {
             config.setMaxThreads(Integer.parseInt(commandLine.getOptionValue("t")));
         }
 
         if (commandLine.hasOption("m")) {
             config.setMimeProperties(commandLine.getOptionValue("m"));
         }
 
         if (commandLine.hasOption("r")) {
             config.setResourcesFolder(commandLine.getOptionValue("r"));
         }
 
         if (commandLine.hasOption("c")) {
             config.setDefaultTextCharset(commandLine.getOptionValue("charset"));
         }
 
         if (commandLine.hasOption("ff")) {
             config.setFfmpegLocation(commandLine.getOptionValue("ff"));
             if (commandLine.hasOption("fp")) {
                 config.setFfmpegParams(commandLine.getOptionValue("fp"));
             }
         }
 
        if (commandLine.hasOption("fp")) {
            config.setFtpPort(Integer.parseInt(commandLine.getOptionValue("fp")));
         }
 
         if (commandLine.hasOption("se")) {
             config.setSegmenterLocation(commandLine.getOptionValue("se"));
             if (commandLine.hasOption("sd")) {
                 config.setSegmentDurationInSec(Integer.parseInt(commandLine.getOptionValue("sd")));
             }
             if (commandLine.hasOption("sw")) {
                 config.setSegmentWindowSize(Integer.parseInt(commandLine.getOptionValue("sw")));
             }
             if (commandLine.hasOption("sk")) {
                 config.setSegmenterSearchKillFile(Integer.parseInt(commandLine.getOptionValue("sk")));
             }
             if (commandLine.hasOption("dt")) {
                 config.setSegmenterMaxtimeout(Integer.parseInt(commandLine.getOptionValue("dt")));
             }
         }
 
         if (commandLine.hasOption("f")) {
             Map<String, String> rotDirs = new LinkedHashMap<String, String>();
 
             try {
                 for (String path: commandLine.getOptionValues("f")) {
                     String[] location = path.split("=");
                     rotDirs.put(location[1], location[0]);
                 }
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
 
             config.setRootDirs(rotDirs);
         }
 
         return config;
     }
 
     private static CommandLine parseCommandLine(Options options, String[] arguments) throws ParseException {
         CommandLineParser parser = new PosixParser();
         return parser.parse(options, arguments);
     }
 
     private static void printHelp() {
         PrintWriter writer = new PrintWriter(System.out);
         HelpFormatter helpFormatter = new HelpFormatter();
         helpFormatter.printHelp(writer, 80, SYNTAX, HEADER,
                 options, 3, 5, FOOTER, true);
         writer.flush();
     }
 
     public static void main(String[] args) throws Exception {
         try {
             Config config = getConfig(args);
 
             Runner runner = new Runner();
             runner.start(config);
 
             FtpServerWrapper ftpServer = new FtpServerWrapper();
             ftpServer.start(config);
 
             synchronized (monitor) {
                 monitor.wait();
             }
         } catch (ParseException e) {
             printHelp();
         }
     }
 }
