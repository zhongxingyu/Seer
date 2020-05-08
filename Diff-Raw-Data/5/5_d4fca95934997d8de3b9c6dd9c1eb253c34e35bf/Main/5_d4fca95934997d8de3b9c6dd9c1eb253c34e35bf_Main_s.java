 /*
  * Copyright (c) 2011, Sho SHIMIZU
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package org.galibier.benchmark;
 
 import org.galibier.netty.OpenFlowDecoder;
 import org.galibier.netty.OpenFlowEncoder;
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.channel.*;
 import org.jboss.netty.channel.group.ChannelGroup;
 import org.jboss.netty.channel.group.ChannelGroupFuture;
 import org.jboss.netty.channel.group.DefaultChannelGroup;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import static org.galibier.openflow.Constants.*;
 
 public class Main {
     @Option(name = "-s", aliases = "--switch", usage = "Number of switches")
     private int switches = 16;
 
     @Option(name = "-p", aliases = "--port", usage = "Port number of the controller")
     private int port = 6633;
 
     @Option(name = "-l", aliases = "--loops", usage = "Number of loops")
     private int loops = 10;
 
     @Option(name = "-d", aliases = "--duration", usage = "Duration of a loop in milli sec")
     private int duration = 1000;
 
     @Option(name = "-m", aliases = "--message", usage = "Bytes of the payload of a packet in")
     private int messageLength = 128;
 
     @Option(name = "-h", aliases = "--help", usage = "Print this help")
     private boolean help = false;
 
     @Argument(index = 0, metaVar = "host", required = true, usage = "Host name of the controller")
     private String host;
 
     private List<FakeSwitch> fakeSwitches;
     private List<ClientBootstrap> bootstraps;
     private long[] previousSentMessages;
     private long[] previousReceivedMessages;
     private final ExecutorService executor = Executors.newCachedThreadPool();
     private final ChannelFactory factory = new NioClientSocketChannelFactory(executor, executor);
     private final ChannelGroup channels = new DefaultChannelGroup("emulated-channels");
 
     public void doMain(String[] args) {
         CmdLineParser parser = new CmdLineParser(this);
         parser.setUsageWidth(80);
 
         try {
             parser.parseArgument(args);
         } catch (CmdLineException e) {
             help = true;
         }
 
         if (help) {
             System.err.println("java Main [option] host");
             parser.printUsage(System.err);
            System.exit(0);
         }
 
         initialize();
 
         long connectionStartTime = System.nanoTime();
         connect();
         long connectionEndTime = System.nanoTime();
         long connectionCompletionTime = connectionEndTime - connectionStartTime;
         double acceptanceRate = (double)switches / (double)connectionCompletionTime * 1.0e9;
         System.out.println(String.format("Acceptance rate :%.5f connections/sec", acceptanceRate));
 
         long benchmarkStartTime = System.nanoTime();
         start(benchmarkStartTime);
 
         reportPeriodically();
 
         close();
 
         long benchmarkEndTime = System.nanoTime();
         long benchmarkDuration = benchmarkEndTime - connectionEndTime;
         long totalMessages = totalReceivedMessages();
         double throughput = (double)totalMessages / (double)benchmarkDuration * 1.0e9;
         System.out.println(String.format("Overall throughput: %.5f requests/sec", throughput));
     }
 
     private void reportPeriodically() {
         for (int i = 0; i < loops; i++) {
             try {
                 Thread.sleep(duration);
                 outputReport();
             } catch (InterruptedException e) {
                 //  ignore
             }
         }
     }
 
     private void start(long benchmarkStartTime) {
         for (Channel channel: channels) {
             OpenFlowBenchmarkHandler handler = (OpenFlowBenchmarkHandler)channel.getPipeline().getLast();
             handler.start(benchmarkStartTime, duration * loops);
         }
     }
 
     private void connect() {
         List<ChannelFuture> futures = new ArrayList<ChannelFuture>(switches);
         for (ClientBootstrap bootstrap: bootstraps) {
             futures.add(bootstrap.connect(new InetSocketAddress(host, port)));
         }
 
         for (ChannelFuture f: futures) {
             f.awaitUninterruptibly();
             if (!f.isSuccess()) {
                 System.err.println("Connection failed: " + f.getCause());
                 System.err.println("Abort");
                System.exit(0);
             }
 
             channels.add(f.getChannel());
         }
     }
 
     private void close() {
         ChannelGroupFuture closeFutures = channels.close();
         closeFutures.awaitUninterruptibly();
         for (ClientBootstrap bootstrap: bootstraps) {
             bootstrap.releaseExternalResources();
         }
     }
 
     private void initialize() {
         fakeSwitches = new ArrayList<FakeSwitch>(switches);
         bootstraps = new ArrayList<ClientBootstrap>(switches);
         previousSentMessages = new long[switches];
         Arrays.fill(previousSentMessages, 0);
         previousReceivedMessages = new long[switches];
         Arrays.fill(previousReceivedMessages, 0);
 
         for (int i = 0; i < switches; i++) {
             //  Datapath ID must be non-zero for NOX
             final FakeSwitch fakeSwitch = new FakeSwitch(i + 1, messageLength);
             fakeSwitches.add(fakeSwitch);
 
             ClientBootstrap bootstrap = new ClientBootstrap(factory);
             bootstraps.add(bootstrap);
 
             bootstrap.setOption("tcpNoDelay", true);
             bootstrap.setOption("keepAlive", true);
             bootstrap.setOption("reuseAddress", true);
             bootstrap.setOption("connectTimeoutMillis", 1000);
 
             bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
                 public ChannelPipeline getPipeline() throws Exception {
                     ChannelPipeline pipeline = Channels.pipeline();
                     pipeline.addLast("framer", new LengthFieldBasedFrameDecoder(
                             MAXIMUM_PACKET_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, -4, 0));
                     pipeline.addLast("decoder", new OpenFlowDecoder());
                     pipeline.addLast("encoder", new OpenFlowEncoder());
                     pipeline.addLast("handler", new OpenFlowBenchmarkHandler(fakeSwitch));
 
                     return pipeline;
                 }
             });
         }
     }
 
     private long totalReceivedMessages() {
         long total = 0;
         for (FakeSwitch fakeSwitch: fakeSwitches) {
             total += fakeSwitch.getReceivedMessages();
         }
 
         return total;
     }
 
     private void outputReport() {
         System.out.print(String.format("%s switches: ", switches));
         long totalReceivedMessage = 0;
         for (int i = 0; i < switches; i++) {
             FakeSwitch fs = fakeSwitches.get(i);
             long currentSentMessages = fs.getSentPacketIns();
             long currentReceivedMessages = fs.getReceivedMessages();
             long sentMessageDifference = currentSentMessages - previousSentMessages[i];
             long receivedMessageDifference = currentReceivedMessages - previousReceivedMessages[i];
             totalReceivedMessage += receivedMessageDifference;
             previousSentMessages[i] = currentSentMessages;
             previousReceivedMessages[i] = currentReceivedMessages;
 
             System.out.print(String.format("%d/%d  ", receivedMessageDifference, sentMessageDifference));
         }
 
         System.out.println();
         System.out.println("Total: " + totalReceivedMessage);
     }
 
     private long totalSentPacketIns() {
         long total = 0;
         for (FakeSwitch sw: fakeSwitches) {
             total += sw.getSentPacketIns();
         }
 
         return total;
     }
 
     public static void main(String[] args) {
         new Main().doMain(args);
     }
 }
