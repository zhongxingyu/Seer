 package com.xonami.javaBells;
 
 import java.io.IOException;
 import java.net.DatagramSocket;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension;
 import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleIQ;
 import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ParameterPacketExtension;
 import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.PayloadTypePacketExtension;
 import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.RtpDescriptionPacketExtension;
 import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension.CreatorEnum;
 import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension.SendersEnum;
 
 import org.ice4j.TransportAddress;
 import org.ice4j.ice.CandidatePair;
 import org.ice4j.ice.Component;
 import org.ice4j.ice.IceMediaStream;
 import org.jitsi.service.libjitsi.LibJitsi;
 import org.jitsi.service.neomedia.DefaultStreamConnector;
 import org.jitsi.service.neomedia.MediaDirection;
 import org.jitsi.service.neomedia.MediaService;
 import org.jitsi.service.neomedia.MediaStream;
 import org.jitsi.service.neomedia.MediaStreamTarget;
 import org.jitsi.service.neomedia.MediaType;
 import org.jitsi.service.neomedia.MediaUseCase;
 import org.jitsi.service.neomedia.StreamConnector;
 import org.jitsi.service.neomedia.device.MediaDevice;
 import org.jitsi.service.neomedia.format.AudioMediaFormat;
 import org.jitsi.service.neomedia.format.MediaFormat;
 import org.jitsi.service.neomedia.format.MediaFormatFactory;
 
 public class JingleStreamManager {
 	private static final DynamicPayloadTypeRegistry dynamicPayloadTypes = new DynamicPayloadTypeRegistry();
 	
 	private final CreatorEnum creator;
 	
 	private final TreeMap<String,MediaDevice> devices = new TreeMap<String,MediaDevice>();
 	private final TreeMap<String,JingleStream> jingleStreams = new TreeMap<String,JingleStream>();
 	
 	public JingleStreamManager(CreatorEnum creator) {
 		this.creator = creator;
 	}
 	
 	public boolean addDefaultMedia( MediaType mediaType, String name ) {
 		MediaService mediaService = LibJitsi.getMediaService();
 		MediaDevice dev = mediaService.getDefaultDevice(mediaType, MediaUseCase.CALL);
 		
 		if( dev == null )
 			return false;
 		
 		devices.put(name, dev);
 		return true;
 	}
 	
 	public List<ContentPacketExtension> createContentList(SendersEnum senders) {
 		List<ContentPacketExtension> contentList = new ArrayList<ContentPacketExtension>();
 		for( Map.Entry<String,MediaDevice> e : devices.entrySet() ) {
 			String name = e.getKey();
 			MediaDevice dev = e.getValue();
 
 			List<MediaFormat> formats = dev.getSupportedFormats();
 			ContentPacketExtension content = new ContentPacketExtension();
 	        RtpDescriptionPacketExtension description = new RtpDescriptionPacketExtension();
 
 	        // fill in the basic content:
 	        content.setCreator(creator);
 	        content.setName(name);
 	        if(senders != null && senders != SendersEnum.both)
 	            content.setSenders(senders);
 
 	        //RTP description
 	        content.addChildExtension(description);
 	        description.setMedia(formats.get(0).getMediaType().toString());
 
 	        //now fill in the RTP description
 	        for(MediaFormat fmt : formats)
 	            description.addPayloadType( formatToPayloadType(fmt, dynamicPayloadTypes));
 	        
 	        contentList.add(content);
 		}
 		return contentList;
 	}
 	
 	public JingleStream startStream(String name, IceAgent iceAgent) throws IOException {
         IceMediaStream stream = iceAgent.getAgent().getStream(name);
         if( stream == null )
         	throw new IOException("Stream not found.");
         Component rtpComponent = stream.getComponent(org.ice4j.ice.Component.RTP);
         Component rtcpComponent = stream.getComponent(org.ice4j.ice.Component.RTCP);
         
         if( rtpComponent == null )
         	throw new IOException("RTP component not found.");
         if( rtcpComponent == null )
         	throw new IOException("RTCP Component not found.");
 
         CandidatePair rtpPair = rtpComponent.getSelectedPair();
         CandidatePair rtcpPair = rtcpComponent.getSelectedPair();
         
        
        
        System.out.println( "RTP : L " + rtpPair.getLocalCandidate().getDatagramSocket().getLocalPort() + " <-> " + rtpPair.getRemoteCandidate().getHostAddress() + " R " );
        System.out.println( "RTCP: L " + rtcpPair.getLocalCandidate().getDatagramSocket().getLocalPort() + " <-> " + rtcpPair.getRemoteCandidate().getHostAddress() + " R " );
         
         return startStream( name,
         		rtpPair.getRemoteCandidate().getHostAddress(),
         		rtcpPair.getRemoteCandidate().getHostAddress(),
         		rtpPair.getLocalCandidate().getDatagramSocket(),
         		rtcpPair.getLocalCandidate().getDatagramSocket());
 	}
 	
 	public JingleStream startStream( String name, TransportAddress remoteRtpAddress, TransportAddress remoteRtcpAddress, DatagramSocket rtpDatagramSocket, DatagramSocket rtcpDatagramSocket ) throws IOException {
 		MediaDevice dev = devices.get(name);
 		
 		MediaService mediaService = LibJitsi.getMediaService();
 		
         MediaStream mediaStream = mediaService.createMediaStream(dev);
 
         mediaStream.setDirection(MediaDirection.SENDRECV);
 
         // format
         String encoding;
         double clockRate;
         /*
          * The AVTransmit2 and AVReceive2 examples use the H.264 video
          * codec. Its RTP transmission has no static RTP payload type number
          * assigned.   
          */
         byte dynamicRTPPayloadType;
 
         //FIXME: this should be passed as an argument or something
         switch (dev.getMediaType())
         {
         case AUDIO:
             encoding = "PCMU";
             clockRate = 8000;
             /* PCMU has a static RTP payload type number assigned. */
             dynamicRTPPayloadType = -1;
             break;
         case VIDEO:
             encoding = "H264";
             clockRate = MediaFormatFactory.CLOCK_RATE_NOT_SPECIFIED;
             /*
              * The dymanic RTP payload type numbers are usually negotiated
              * in the signaling functionality.
              */
             dynamicRTPPayloadType = 99;
             break;
         default:
             encoding = null;
             clockRate = MediaFormatFactory.CLOCK_RATE_NOT_SPECIFIED;
             dynamicRTPPayloadType = -1;
         }
 
         if (encoding != null)
         {
             MediaFormat format
                 = mediaService.getFormatFactory().createMediaFormat(
                         encoding,
                         clockRate);
 
             /*
              * The MediaFormat instances which do not have a static RTP
              * payload type number association must be explicitly assigned
              * a dynamic RTP payload type number.
              */
             if (dynamicRTPPayloadType != -1)
             {
                 mediaStream.addDynamicRTPPayloadType(
                         dynamicRTPPayloadType,
                         format);
             }
 
             mediaStream.setFormat(format);
         }
 
         StreamConnector connector = new DefaultStreamConnector( rtpDatagramSocket, rtcpDatagramSocket );
 
         mediaStream.setConnector(connector);
 
         mediaStream.setTarget( new MediaStreamTarget(
         		new InetSocketAddress( remoteRtpAddress.getAddress(), remoteRtpAddress.getPort() ),
         		new InetSocketAddress( remoteRtcpAddress.getAddress(), remoteRtcpAddress.getPort() ) ) );
 
         mediaStream.setName(name);
         
         mediaStream.start();
         
         JingleStream js = new JingleStream( name, mediaStream, this );
         jingleStreams.put( name, js );
         return js;
 	}
 	
     public static PayloadTypePacketExtension formatToPayloadType(
             MediaFormat format,
             DynamicPayloadTypeRegistry ptRegistry)
     {
         PayloadTypePacketExtension ptExt = new PayloadTypePacketExtension();
 
         int payloadType = format.getRTPPayloadType();
 
         if (payloadType == MediaFormat.RTP_PAYLOAD_TYPE_UNKNOWN)
                 payloadType = ptRegistry.obtainPayloadTypeNumber(format);
 
         ptExt.setId(payloadType);
         ptExt.setName(format.getEncoding());
 
         if(format instanceof AudioMediaFormat)
             ptExt.setChannels(((AudioMediaFormat)format).getChannels());
 
         ptExt.setClockrate((int)format.getClockRate());
 
         /*
          * Add the format parameters and the advanced attributes (as parameter
          * packet extensions).
          */
         for(Map.Entry<String, String> entry :
             format.getFormatParameters().entrySet())
         {
             ParameterPacketExtension ext = new ParameterPacketExtension();
             ext.setName(entry.getKey());
             ext.setValue(entry.getValue());
             ptExt.addParameter(ext);
         }
         for(Map.Entry<String, String> entry :
             format.getAdvancedAttributes().entrySet())
         {
             ParameterPacketExtension ext = new ParameterPacketExtension();
             ext.setName(entry.getKey());
             ext.setValue(entry.getValue());
             ptExt.addParameter(ext);
         }
 
         return ptExt;
     }
     
     /** Checks the content packet of the jingle iq and returns its name. If there
      * is a problem with the formatting of the content packet or jingle IQ an
      * IOException is thrown.
      * 
      * @param jiq
      * @return the name of the contentpacket
      * @throws IOException if the name cannot be found
      */
 	public static String getContentPacketName(JingleIQ jiq) throws IOException {
 		String name = null;
 		List<ContentPacketExtension> cpes = jiq.getContentList();
 		for( ContentPacketExtension cpe : cpes ) {
 			if( name != null )
 				throw new IOException();
 			name = cpe.getName();
 		}
 		if( name == null )
 			throw new IOException();
 		return name;
 	}
 }
