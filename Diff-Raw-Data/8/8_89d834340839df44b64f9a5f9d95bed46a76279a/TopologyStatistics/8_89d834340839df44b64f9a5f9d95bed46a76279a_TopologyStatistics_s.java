 /*
  *  Copyright (c) 2011 Andrea Zito
  *
  *  This is free software; see lgpl-2.1.txt
  */
 package jgrapes.test.statistics;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.UnknownHostException;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 
 import jgrapes.JGrapesException;
 import jgrapes.NodeID;
 import jgrapes.test.TopologyNode;
 
 
 public class TopologyStatistics {
   NodeID addr = null;
   int maxNodes = 0;
   int timeout = 0;
   String nhConf = null;
   String psConf = null;
   String chConf = null;
   String basedir = null;
   boolean monitor = false;
   String growth_func = null;
   int wait = 0;
   int iterations = 1;
 
 
   TopologyNode localTNodes[] = null;
   CloudMonitorNode monitorNode = null;
 
   PrintStream out = null;
   /**
    * Print help message
    */
   public static void printHelp(Options opts) {
     HelpFormatter formatter = new HelpFormatter();
     formatter.printHelp("TopologyTest", opts, true);
   }
 
   /**
    * Parse the command line
    */
   public void parseCommandLine(String args[]) {
     CommandLineParser cmdParser = new GnuParser();
     Options opts = new Options();
 
     // Declaring supported options
     opts.addOption(null, "help", false, "print this help message");
     opts.addOption("h", true, "specify the local address to use (ip:port)");
     opts.addOption(null, "nh-conf", true, "specify the net_helper configuration");
     opts.addOption(null, "ps-conf", true, "specify the peer_sampler configuration");
     opts.addOption(null, "ch-conf", true, "specify the cloud_helper configuration. Causes the cloud helper to be initialized.");
     opts.addOption("n", true, "max number of peer to initialize");
     opts.addOption("b", true, "base directory where to store log files");
     opts.addOption("t", true, "how much time to run in minutes");
     opts.addOption("m", false, "activate cloud monitor");
     opts.addOption("w", true, "wait for the specified time before starting nodes");
     opts.addOption("g", true, "specify growth function: sin, const (const)");
     opts.addOption("N", true, "number of iterations (default 1)");
 
     // Parse the command line
     try {
       CommandLine line = cmdParser.parse(opts, args);
 
       if (line.hasOption("help")) printHelp(opts);
 
       if (line.hasOption('h')) {
         try {
           addr = new NodeID(line.getOptionValue('h'));
         } catch (UnknownHostException e) {
           throw new ParseException("Local node address is not valid");
         }
       } else throw new ParseException("Missing mandatory option -h");
 
       if (line.hasOption('b')) basedir = line.getOptionValue('b');
       else throw new ParseException("Missing mandatory option -b");
 
       if (line.hasOption('n')) {
         try {
           maxNodes = Integer.parseInt(line.getOptionValue('n'));
         } catch (NumberFormatException e) {
           throw new ParseException("Illegal argument for option -n");
         }
       } else throw new ParseException("Missing mandatory option -n");
 
       if (line.hasOption('t')) {
         try {
           timeout = Integer.parseInt(line.getOptionValue('t'));
         } catch (NumberFormatException e) {
           throw new ParseException("Illegal argument for option -t");
         }
       } else throw new ParseException("Missing mandatory option -t");
 
       if (line.hasOption('g')) {
         growth_func = line.getOptionValue('g').toLowerCase().trim();
         if (!growth_func.equals("const") && !growth_func.equals("sin"))
             throw new ParseException("Unknown growth function");
       } else growth_func = "const";
 
       if(line.hasOption("nh-conf")) nhConf = line.getOptionValue("nh-conf");
 
       if(line.hasOption("ps-conf")) psConf = line.getOptionValue("ps-conf");
       else throw new ParseException("Missing mandatory option --ps-conf");
 
       if(line.hasOption("ch-conf")) chConf = line.getOptionValue("ch-conf");
       else throw new ParseException("Missing mandatory option --ch-conf");
 
       if(line.hasOption("m")) monitor = true;
 
       if (line.hasOption('w')) wait = Integer.parseInt(line.getOptionValue('w'));
 
       if (line.hasOption('N')) iterations = Integer.parseInt(line.getOptionValue('N'));
 
     } catch (ParseException e) {
       System.err.println(e.getMessage());
       printHelp(opts);
       System.exit(1);
     } catch (Exception e) {
       System.err.println(e.getMessage());
       System.exit(1);
     }
   }
 
   private void waitFor() {
     while (wait > 0) {
       System.out.format("Starting in %d seconds...\n", wait--);
       try {
         Thread.sleep(1000);
       } catch (InterruptedException e) {}
     }
   }
 
   private void setupNodes() throws JGrapesException, UnknownHostException {
     if (maxNodes > 0) {
       System.out.format("Setup %d local peers\n", maxNodes);
       localTNodes = new TopologyNode[maxNodes];
 
       for (int i=0; i<localTNodes.length; i++) {
         NodeID localTNode;
         localTNode = new NodeID(String.format("%s:%d", addr.getIpAddress(), (addr.getPort()+i)));
         try {
           String outpath = String.format("out-%s_%d.log", addr.getIpAddress(), (addr.getPort()+i));
           File outfile = new File(basedir + "/" + outpath);
           outfile.createNewFile();
 
           PrintStream pstream = new PrintStream(outfile);
           localTNodes[i] = new StatisticsTopologyNode(localTNode, nhConf, psConf, chConf, 1, pstream);
         } catch (Exception e) {
           e.printStackTrace();
         }
       }
     }
   }
 
   private void setupMonitorNode() {
     if (monitor) {
       System.out.println("Starting the cloud monitor");
       String outpath = String.format("%s/out-monitor.log", basedir);
       try {
         File outfile = new File(outpath);
         outfile.createNewFile();
         PrintStream pstream = new PrintStream(outfile);
         monitorNode = new CloudMonitorNode (chConf, pstream, 1);
       } catch (Exception e) {
         e.printStackTrace();
         System.exit(1);
       }
       monitorNode.start();
     }
   }
 
   private void print(String line) {
     System.out.println(line);
    if (this.out != null) this.out.println(line);
   }
 
 
   private int growNodesSin(long currentTime, long length) {
       double x;
       x = (((double) currentTime) / length) * 180;
       return (int)Math.round((Math.sin(Math.toRadians(x)) * maxNodes));
   }
 
   private int growNodesConst(long currentTime, long length) {
     long base = 5000;
     int nodes;
 
     if (length < base * maxNodes) {
       base = length/maxNodes;
     }
 
     nodes = (int) (currentTime/base + 1);
     if (nodes > maxNodes) nodes = maxNodes;
     return nodes;
   }
 
   private int growNodes(long currentTime, long length) {
     if (growth_func.equals("const")) {
       return growNodesConst(currentTime, length);
     } else {
       return growNodesSin(currentTime, length);
     }
   }
 
   private void runThreads() {
     for (TopologyNode localTNode: localTNodes) {
       localTNode.setActiveState(false);
       localTNode.start();
     }
 
     try {
       int count = 0;
       while (count++ < iterations) {
         System.out.println("# Starting iteration " + count + " of " + iterations);
         int nodesToAddRemove;
         long now = 0;
         long startTime =  System.currentTimeMillis();
         long endTime = startTime + (timeout * 60000);
         long lengthTime = endTime - startTime;
         int currentNodes = 0;
         int progress = 0;
 
         while ((now = System.currentTimeMillis()) < endTime) {
           long normTime = now - startTime;
           int tmp;
 
           nodesToAddRemove = growNodes(normTime, lengthTime) - currentNodes;
 
           tmp = (int)(((double)(normTime) / lengthTime) * 100);
           if ((nodesToAddRemove > 0) || (tmp > progress)) {
             progress = tmp;
             print(String.format("@ time=%d progress=%d%% current_nodes=%d, after_nodes=%d (%d)",
                                 (System.currentTimeMillis() / 1000),
                                 progress,
                                 currentNodes,
                                 (currentNodes + nodesToAddRemove), nodesToAddRemove));
           }
           if (nodesToAddRemove > 0) {
             for (int i=0; i < nodesToAddRemove; i++){
               TopologyNode n = localTNodes[currentNodes++];
               print("# activating node: " + n.getNodeID());
               n.setActiveState(true);
               n.addNode(new NodeID(String.format("0.0.0.0:0")));
             }
           } else {
             for (int i=0; i < -nodesToAddRemove; i++) {
               TopologyNode n = localTNodes[--currentNodes];
               print("# deactivating node: " + n.getNodeID());
               n.setActiveState(false);
             }
           }
           Thread.sleep(1000);
         }
 
         for (TopologyNode localTNode: localTNodes) {
           localTNode.setActiveState(false);
         }
       }
 
       print("@ end");
       print("# killing threads");
 
       for (TopologyNode n: localTNodes) n.terminate();
       for (TopologyNode n: localTNodes) {
         n.join();
       }
       if (monitor) {
         monitorNode.terminate();
         monitorNode.join();
       }
 
     } catch (Exception e) {
       e.printStackTrace();
     }
   }
 
   private void run() {
     try {
       File outfile = new File(basedir + "/out-" + addr.getIpAddress() + "-controller.log");
       outfile.createNewFile();
       out = new PrintStream(outfile);
     } catch (Exception e) {
       e.printStackTrace();
       System.exit(1);
     }
     waitFor();
     runThreads();
   }
 
   /**
    * Topology test thread entry point
    */
   public static void main(String args[]) throws JGrapesException, UnknownHostException {
     TopologyStatistics topologyStatistics = new TopologyStatistics();
 
     topologyStatistics.parseCommandLine(args);
 
     topologyStatistics.setupNodes();
     topologyStatistics.setupMonitorNode();
     topologyStatistics.run();
   }
 }
