 /**
    This file is part of GoldenGate Project (named also GoldenGate or GG).
 
    Copyright 2009, Frederic Bregier, and individual contributors by the @author
    tags. See the COPYRIGHT.txt in the distribution for a full listing of
    individual contributors.
 
    All GoldenGate Project is free software: you can redistribute it and/or 
    modify it under the terms of the GNU General Public License as published 
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
 
    GoldenGate is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with GoldenGate .  If not, see <http://www.gnu.org/licenses/>.
  */
 package goldengate.commandexec.server;
 
 import static org.jboss.netty.channel.Channels.*;
 import goldengate.commandexec.utils.LocalExecDefaultResult;
 
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.group.ChannelGroup;
 import org.jboss.netty.channel.group.DefaultChannelGroup;
 import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
 import org.jboss.netty.handler.codec.frame.Delimiters;
 import org.jboss.netty.handler.codec.string.StringDecoder;
 import org.jboss.netty.handler.codec.string.StringEncoder;
 
 /**
  * Creates a newly configured {@link ChannelPipeline} for a new channel for LocalExecServer.
  *
  *
  */
 public class LocalExecServerPipelineFactory implements ChannelPipelineFactory {
 
     private long delay = LocalExecDefaultResult.MAXWAITPROCESS;
    private ChannelGroup channelGroup = new DefaultChannelGroup("LocalExecServer");
 
     /**
      * Constructor with default delay
      * 
      */
     public LocalExecServerPipelineFactory() {
         // Default delay
     }
 
     /**
      * Constructor with a specific default delay
      * @param newdelay
      */
     public LocalExecServerPipelineFactory(long newdelay) {
         delay = newdelay;
     }
 
 
     public ChannelPipeline getPipeline() throws Exception {
         // Create a default pipeline implementation.
         ChannelPipeline pipeline = pipeline();
 
         // Add the text line codec combination first,
         pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192,
                 Delimiters.lineDelimiter()));
         pipeline.addLast("decoder", new StringDecoder());
         pipeline.addLast("encoder", new StringEncoder());
 
         // and then business logic.
         // Could change it with a new fixed delay if necessary at construction
         pipeline.addLast("handler", new LocalExecServerHandler(this, delay));
 
         return pipeline;
     }
     /**
      * Add a channel to the ExecClient Group
      * @param channel
      */
     public void addChannel(Channel channel) {
         channelGroup.add(channel);
     }
     /**
      * remove a channel to the ExecClient Group
      * @param channel
      */
     public void removeChannel(Channel channel) {
         channelGroup.remove(channel);
     }
     /**
      * Release internal resources
      */
     public void releaseResources() {
         channelGroup.close();
     }
 }
