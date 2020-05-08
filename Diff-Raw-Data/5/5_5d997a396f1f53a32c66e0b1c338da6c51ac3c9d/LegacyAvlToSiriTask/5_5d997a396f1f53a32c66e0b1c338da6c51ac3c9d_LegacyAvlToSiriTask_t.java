 package org.onebusaway.king_county_metro.legacy_avl_to_siri;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Future;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.inject.Inject;
 import javax.inject.Singleton;
 
 import org.krakenapps.pcap.PcapInputStream;
 import org.krakenapps.pcap.Protocol;
 import org.krakenapps.pcap.decoder.ethernet.EthernetDecoder;
 import org.krakenapps.pcap.decoder.ethernet.EthernetType;
 import org.krakenapps.pcap.decoder.ip.InternetProtocol;
 import org.krakenapps.pcap.decoder.ip.IpDecoder;
 import org.krakenapps.pcap.decoder.udp.UdpDecoder;
 import org.krakenapps.pcap.decoder.udp.UdpPacket;
 import org.krakenapps.pcap.decoder.udp.UdpPortProtocolMapper;
 import org.krakenapps.pcap.decoder.udp.UdpProcessor;
 import org.krakenapps.pcap.packet.PcapPacket;
 import org.krakenapps.pcap.util.Buffer;
 import org.onebusaway.siri.core.SiriServer;
 import org.onebusaway.siri.core.SiriTypeFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.org.siri.siri.FramedVehicleJourneyRefStructure;
 import uk.org.siri.siri.ServiceDelivery;
 import uk.org.siri.siri.VehicleActivityStructure;
 import uk.org.siri.siri.VehicleActivityStructure.MonitoredVehicleJourney;
 import uk.org.siri.siri.VehicleMonitoringDeliveryStructure;
 
 @Singleton
 public class LegacyAvlToSiriTask implements Runnable {
 
   private static final Logger _log = LoggerFactory.getLogger(LegacyAvlToSiriTask.class);
 
   private static final DateFormat _serviceDateFormat = new SimpleDateFormat(
       "yyyy-MM-dd");
 
   private List<Packet> packets = new ArrayList<Packet>();
 
   private Map<String, VehicleServiceDateRecord> _serviceDateRecordsByVehicleId = new HashMap<String, LegacyAvlToSiriTask.VehicleServiceDateRecord>();
 
   private SiriServer _server;
 
   private ExecutorService _executor;
 
   private Future<?> _task;
 
   private InputStream _source = System.in;
 
   private long _pauseBetwenPackets;
 
   /**
    * Default service date expiration after an hour
    */
   private int _vehicleServiceDateExpiration = 60 * 60;
 
   @Inject
   public void setSiriServer(SiriServer server) {
     _server = server;
   }
 
   @Inject
   public void setExecutor(ExecutorService executor) {
     _executor = executor;
   }
 
   public void setSource(InputStream source) {
     _source = source;
   }
 
   public void setPauseBetweenPackets(long pauseBetweenPackets) {
     _pauseBetwenPackets = pauseBetweenPackets;
   }
 
   /**
    * When we initially hear from a vehicle, we assign it a service date based on
    * the current date. If we haven't heard from that vehicle the specified
    * number of seconds, we forget that service date.
    * 
    * @param vehicleServiceDateExpiration time, in seconds
    */
   public void setVehicleServiceDateExpiration(int vehicleServiceDateExpiration) {
     _vehicleServiceDateExpiration = vehicleServiceDateExpiration;
   }
 
   @PostConstruct
   public void start() {
     _task = _executor.submit(this);
   }
 
   @PreDestroy
   public void stop() {
     if (_task != null) {
       _task.cancel(true);
       _task = null;
     }
   }
 
   /****
    * {@link Runnable} Interface
    ****/
 
   @Override
   public void run() {
     try {
       runInternal();
     } catch (Throwable ex) {
       _log.error("error in LegacyAvlToSiriTask", ex);
       System.exit(-1);
     }
   }
 
   private void runInternal() throws IOException {
 
     EthernetDecoder eth = new EthernetDecoder();
     IpDecoder ip = new IpDecoder();
 
     UdpPortProtocolMapper protocolMapper = new UdpPortProtocolMapper();
 
     /**
      * Since the Protocol enum doesn't allow us to add our own Protocol entry,
      * we just reuse an existing protocol.
      */
     protocolMapper.register(5000, Protocol.WHOIS);
     protocolMapper.register(Protocol.WHOIS, new UdpHandler());
 
     UdpDecoder udp = new UdpDecoder(protocolMapper);
 
     eth.register(EthernetType.IPV4, ip);
     ip.register(InternetProtocol.UDP, udp);
 
     PcapInputStream is = new PcapGenericInputStream(_source);
 
     try {
       while (true) {
         PcapPacket packet = is.getPacket();
         eth.decode(packet);
       }
     } finally {
       try {
         is.close();
       } catch (IOException ex) {
 
       }
     }
   }
 
   private void publishPacketsAsSiri() {
 
     VehicleMonitoringDeliveryStructure vm = new VehicleMonitoringDeliveryStructure();
 
     for (Packet packet : packets) {
 
       if (packet.isNoTrip() || packet.getTrip() == 0)
         continue;
 
       VehicleActivityStructure activity = new VehicleActivityStructure();
       long t = packet.getTime() * 1000L;
       activity.setRecordedAtTime(new Date(t));
       activity.setValidUntilTime(new Date(t + 10 * 60 * 1000L));
 
       MonitoredVehicleJourney mvj = new MonitoredVehicleJourney();
       activity.setMonitoredVehicleJourney(mvj);
 
       String line = Integer.toString(packet.getRoute());
       mvj.setLineRef(SiriTypeFactory.lineRef(line));
       mvj.setPublishedLineName(SiriTypeFactory.nls(line));
 
       mvj.setJourneyPatternRef(SiriTypeFactory.journeyPatternRef(packet.getPattern()));
 
       String operatorId = Integer.toString(packet.getOperatorId());
       mvj.setOperatorRef(SiriTypeFactory.operatorRef(operatorId));
 
       mvj.setDelay(SiriTypeFactory.duration(packet.getScheduleDeviation() * 1000L));
 
       String vehicleId = Integer.toString(packet.getVehicleId());
       mvj.setVehicleRef(SiriTypeFactory.vehicleRef(vehicleId));
 
       FramedVehicleJourneyRefStructure fvjRef = new FramedVehicleJourneyRefStructure();
       mvj.setFramedVehicleJourneyRef(fvjRef);
 
       String tripId = Integer.toString(packet.getTrip());
       fvjRef.setDatedVehicleJourneyRef(tripId);
       String serviceDate = getServiceDateForVehicleId(vehicleId);
       fvjRef.setDataFrameRef(SiriTypeFactory.dataFrameRef(serviceDate));
 
       vm.getVehicleActivity().add(activity);
     }
 
     /**
      * On the off chance that none of the packets were good
      */
     if (vm.getVehicleActivity().isEmpty())
       return;
 
    _log.debug("publishing vehicles={}", vm.getVehicleActivity().size());
 
     ServiceDelivery serviceDelivery = new ServiceDelivery();
     serviceDelivery.getVehicleMonitoringDelivery().add(vm);
 
     _server.publish(serviceDelivery);
   }
 
   private String getServiceDateForVehicleId(String vehicleId) {
 
     VehicleServiceDateRecord record = _serviceDateRecordsByVehicleId.get(vehicleId);
 
     if (record == null
         || record.getLastUpdate() + _vehicleServiceDateExpiration * 1000 < System.currentTimeMillis()) {
 
       String serviceDate = _serviceDateFormat.format(new Date());
       record = new VehicleServiceDateRecord(serviceDate);
       _serviceDateRecordsByVehicleId.put(vehicleId, record);
     }
 
     record.update();
 
     return record.getServiceDate();
   }
 
   private class UdpHandler implements UdpProcessor {
 
     private byte[] rawBuffer = new byte[1024];
     ByteBuffer buffer = ByteBuffer.wrap(rawBuffer);
 
     @Override
     public void process(UdpPacket p) {
 
      _log.info("UDP Packet: {}", p);
 
       Buffer data = p.getData();
       int dataLength = Math.min(data.readableBytes(), rawBuffer.length);
 
       data.gets(rawBuffer, 0, dataLength);
 
       packets.clear();
       buffer.rewind();
 
       PacketIO.parsePacket(buffer, dataLength, packets);
 
       if (packets.isEmpty())
         return;
 
       publishPacketsAsSiri();
 
       if (_pauseBetwenPackets > 0) {
         try {
           Thread.sleep(_pauseBetwenPackets);
         } catch (InterruptedException e) {
           return;
         }
       }
     }
 
   }
 
   private static class VehicleServiceDateRecord {
 
     private final String serviceDate;
 
     private long lastUpdate = System.currentTimeMillis();
 
     public VehicleServiceDateRecord(String serviceDate) {
       this.serviceDate = serviceDate;
     }
 
     public void update() {
       lastUpdate = System.currentTimeMillis();
     }
 
     public String getServiceDate() {
       return serviceDate;
     }
 
     public long getLastUpdate() {
       return lastUpdate;
     }
   }
 }
