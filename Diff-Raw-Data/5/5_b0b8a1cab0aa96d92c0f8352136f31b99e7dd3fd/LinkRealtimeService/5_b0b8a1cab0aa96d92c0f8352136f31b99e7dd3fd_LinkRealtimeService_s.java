 package org.onebusaway.sound_transit.realtime.link;
 
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.Credentials;
 import org.apache.http.auth.NTCredentials;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.onebusaway.collections.FactoryMap;
 import org.onebusaway.collections.Min;
 import org.onebusaway.collections.tuple.Pair;
 import org.onebusaway.collections.tuple.Tuples;
 import org.onebusaway.gtfs.csv.CsvEntityReader;
 import org.onebusaway.gtfs.csv.EntityHandler;
 import org.onebusaway.gtfs.model.AgencyAndId;
 import org.onebusaway.realtime.api.EVehiclePhase;
 import org.onebusaway.realtime.api.TimepointPredictionRecord;
 import org.onebusaway.realtime.api.VehicleLocationListener;
 import org.onebusaway.realtime.api.VehicleLocationRecord;
 import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
 import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
 import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
 import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
 import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocationService;
 import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
 import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
 import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
 import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
 import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
 import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 public class LinkRealtimeService {
 
   private static Logger _log = LoggerFactory.getLogger(LinkRealtimeService.class);
 
   private static SimpleDateFormat _format = new SimpleDateFormat("HH:mm:ss");
 
   private VehicleLocationListener _vehicleLocationListener;
 
   private BlockCalendarService _blockCalendarService;
 
   private ScheduledBlockLocationService _scheduledBlockLocationService;
 
   private ScheduledExecutorService _executor;
 
   private Map<DirectionDestinationKey, List<VehicleInstance>> _vehiclesByDirectionDestination = new HashMap<DirectionDestinationKey, List<VehicleInstance>>();
 
   private int _nextVehicleId = 1;
 
   private int _refreshInterval = 60;
 
   private String _url;
 
   private String _username;
 
   private String _password;
 
   private boolean _useNtlm = false;
 
   private String _domain;
 
   private double _scheduleDeviationFactor = 0.25;
 
   private Map<Pair<AgencyAndId>, Double> _segmetScheduleDeviationFactors = new HashMap<Pair<AgencyAndId>, Double>();
 
   @Autowired
   public void setVehicleLocationListener(
       VehicleLocationListener vehicleLocationListener) {
     _vehicleLocationListener = vehicleLocationListener;
   }
 
   @Autowired
   public void setBlockCalendarService(BlockCalendarService blockCalendarService) {
     _blockCalendarService = blockCalendarService;
   }
 
   @Autowired
   public void setScheduledBlockLocationService(
       ScheduledBlockLocationService scheduledBlockLocationService) {
     _scheduledBlockLocationService = scheduledBlockLocationService;
   }
 
   public void setUrl(String url) {
     _url = url;
   }
 
   public void setUsername(String username) {
     _username = username;
   }
 
   public void setPassword(String password) {
     _password = password;
   }
 
   public void setDomain(String domain) {
     _domain = domain;
   }
 
   public void setUseNtlm(boolean useNtlm) {
     _useNtlm = useNtlm;
   }
 
   public void setRefreshInterval(int refreshInterval) {
     _refreshInterval = refreshInterval;
   }
 
   public void setScheduleDeviationFactor(double scheduleDeviationFactor) {
     _scheduleDeviationFactor = scheduleDeviationFactor;
   }
 
   /**
    * 
    * @param factors
    */
   public void setSegmetScheduleDeviationFactors(Map<String, Double> factors) {
     for (Map.Entry<String, Double> entry : factors.entrySet()) {
       String key = entry.getKey();
       String[] tokens = key.split(",");
       AgencyAndId fromStopId = AgencyAndIdLibrary.convertFromString(tokens[0]);
       AgencyAndId toStopId = AgencyAndIdLibrary.convertFromString(tokens[1]);
       Pair<AgencyAndId> pair = Tuples.pair(fromStopId, toStopId);
       _segmetScheduleDeviationFactors.put(pair, entry.getValue());
     }
   }
 
   @PostConstruct
   public void start() {
     _executor = Executors.newSingleThreadScheduledExecutor();
     _executor.scheduleAtFixedRate(new RefreshTask(), 0, _refreshInterval,
         TimeUnit.SECONDS);
   }
 
   @PreDestroy
   public void stop() {
     _executor.shutdownNow();
   }
 
   /****
    * Private Methods
    ****/
 
   private void handleRecords(List<Record> records) {
 
     if (records.isEmpty())
       return;
 
     System.out.println("====== CYCLE ======");
 
     List<VehicleLocationRecord> results = new ArrayList<VehicleLocationRecord>();
 
     Map<DirectionDestinationKey, List<Record>> recordsByKey = groupRecordsByDirectionAndDestination(records);
 
     for (Map.Entry<DirectionDestinationKey, List<Record>> entry : recordsByKey.entrySet()) {
 
       DirectionDestinationKey key = entry.getKey();
 
       System.out.println("group=" + key.getDirection() + " dest="
           + key.getDestinationStopId());
 
       BlockInstance blockInstance = getBestBlockInstance(key);
 
       if (blockInstance == null) {
         _log.warn("no block instance found for group: key=" + key);
         continue;
       }
 
       Map<AgencyAndId, List<Record>> recordsByStop = groupRecordsByStop(entry.getValue());
 
       List<VehicleRecords> allVehicleRecords = linkRecordsAlongBlock(
           blockInstance, recordsByStop);
 
       List<VehicleState> states = getLinkedVehicleRecordsAsVehicleStates(
           blockInstance, allVehicleRecords);
 
       Collections.sort(states, VehicleStateDescendingComparator.INSTANCE);
 
       List<VehicleInstance> currentInstances = new ArrayList<VehicleInstance>();
 
       List<VehicleInstance> prevVehicles = _vehiclesByDirectionDestination.get(key);
       if (prevVehicles == null)
         prevVehicles = Collections.emptyList();
 
       for (VehicleState state : states) {
         VehicleInstance prevInstance = getBestPreviousVehicleInstance(
             prevVehicles, state);
 
         VehicleInstance currentInstance = null;
 
         if (prevInstance != null) {
           prevVehicles.remove(prevInstance);
           currentInstance = new VehicleInstance(prevInstance.getVehicleId(),
               state);
         } else {
           AgencyAndId vehicleId = new AgencyAndId("40",
               Integer.toString(_nextVehicleId++));
           currentInstance = new VehicleInstance(vehicleId, state);
         }
 
         currentInstances.add(currentInstance);
       }
 
       Collections.sort(currentInstances,
           VehicleInstanceDescendingComparator.INSTANCE);
 
       for (VehicleInstance instance : currentInstances) {
 
         VehicleLocationRecord vlr = getVehicleInstanceAsVehicleLocationRecord(instance);
         results.add(vlr);
 
         dumpVehicleInstance(instance);
 
       }
 
       _vehiclesByDirectionDestination.put(key, currentInstances);
     }
 
     if (!results.isEmpty())
       _vehicleLocationListener.handleVehicleLocationRecords(results);
   }
 
   private Map<DirectionDestinationKey, List<Record>> groupRecordsByDirectionAndDestination(
       List<Record> records) {
 
     Map<DirectionDestinationKey, List<Record>> recordsByKey = new FactoryMap<DirectionDestinationKey, List<Record>>(
         new ArrayList<Record>());
 
     for (Record record : records) {
       AgencyAndId routeId = new AgencyAndId("40", record.getRouteId());
       AgencyAndId destinationStopId = new AgencyAndId("1",
           record.getDestinationTimepointId());
       DirectionDestinationKey key = new DirectionDestinationKey(routeId,
           destinationStopId, record.getDirection());
       recordsByKey.get(key).add(record);
     }
 
     return recordsByKey;
   }
 
   private BlockInstance getBestBlockInstance(DirectionDestinationKey key) {
 
     AgencyAndId routeId = key.getRouteId();
     long t = System.currentTimeMillis();
 
     List<BlockInstance> instances = _blockCalendarService.getActiveBlocksForRouteInTimeRange(
         routeId, t - 30 * 60 * 1000, t + 30 * 60 * 1000);
 
     List<BlockInstance> matchesDestination = new ArrayList<BlockInstance>();
 
     for (BlockInstance instance : instances) {
 
       // TODO - How do we handle a mixture of frequency and regular trips?
       if (instance.getFrequency() == null)
         continue;
 
       BlockConfigurationEntry blockConfig = instance.getBlock();
       List<BlockStopTimeEntry> stopTimes = blockConfig.getStopTimes();
       BlockStopTimeEntry lastBlockStopTime = stopTimes.get(stopTimes.size() - 1);
       StopTimeEntry lastStopTime = lastBlockStopTime.getStopTime();
       StopEntry lastStop = lastStopTime.getStop();
       if (lastStop.getId().equals(key.getDestinationStopId()))
         matchesDestination.add(instance);
     }
 
     if (matchesDestination.isEmpty())
       return null;
 
     Min<BlockInstance> m = new Min<BlockInstance>();
 
     for (BlockInstance instance : matchesDestination) {
       int scheduleTime = (int) ((t - instance.getServiceDate()) / 1000);
       FrequencyEntry frequency = instance.getFrequency();
       if (frequency.getStartTime() <= scheduleTime
           && scheduleTime <= frequency.getEndTime()) {
         m.add(0, instance);
       } else {
         int a = Math.abs(scheduleTime - frequency.getStartTime());
         int b = Math.abs(scheduleTime - frequency.getEndTime());
         m.add(Math.min(a, b), instance);
       }
     }
 
     return m.getMinElement();
   }
 
   private Map<AgencyAndId, List<Record>> groupRecordsByStop(List<Record> records) {
 
     Map<AgencyAndId, List<Record>> recordsByStop = new FactoryMap<AgencyAndId, List<Record>>(
         new ArrayList<Record>());
 
     for (Record record : records) {
       AgencyAndId stopId = new AgencyAndId("1", record.getTimepointId());
       recordsByStop.get(stopId).add(record);
     }
 
     for (List<Record> recordsForStop : recordsByStop.values())
       Collections.sort(recordsForStop,
           RecordDescendingTimepointArrivalTimeComparator.INSTANCE);
 
     return recordsByStop;
   }
 
   private List<VehicleRecords> linkRecordsAlongBlock(
       BlockInstance blockInstance, Map<AgencyAndId, List<Record>> recordsByStop) {
     List<VehicleRecords> allVehicleRecords = new ArrayList<VehicleRecords>();
 
     for (BlockStopTimeEntry blockStopTime : blockInstance.getBlock().getStopTimes()) {
 
       StopTimeEntry stopTime = blockStopTime.getStopTime();
       StopEntry stop = stopTime.getStop();
       AgencyAndId stopId = stop.getId();
 
       List<Record> recordsForStop = recordsByStop.get(stopId);
 
       for (Record recordForStop : recordsForStop) {
 
         VehicleRecords vehicleRecords = getBestVehicleTripForRecord(
             allVehicleRecords, blockStopTime, recordForStop);
 
         if (vehicleRecords == null) {
           vehicleRecords = new VehicleRecords(recordForStop, blockStopTime);
           allVehicleRecords.add(vehicleRecords);
         } else {
           vehicleRecords.addRecord(recordForStop, blockStopTime);
         }
       }
     }
     return allVehicleRecords;
   }
 
   private VehicleRecords getBestVehicleTripForRecord(
       List<VehicleRecords> vehicles, BlockStopTimeEntry blockStopTime,
       Record recordForStop) {
 
     for (VehicleRecords vehicle : vehicles) {
 
       BlockStopTimeEntry lastBlockStopTime = vehicle.getLastBlockStopTime();
 
       if (lastBlockStopTime.getBlockSequence() < blockStopTime.getBlockSequence()) {
 
         Record lastRecord = vehicle.getLastRecord();
 
         // The vehicle can't travel backwards in time
         if (recordForStop.getTimepointTime() < lastRecord.getTimepointTime())
           continue;
 
         double deviationFactor = _scheduleDeviationFactor;
 
         Pair<AgencyAndId> segment = Tuples.pair(
             lastBlockStopTime.getStopTime().getStop().getId(),
             blockStopTime.getStopTime().getStop().getId());
 
         Double v = _segmetScheduleDeviationFactors.get(segment);
 
         if (v != null)
           deviationFactor = v;
 
         int expectedTimeDelta = blockStopTime.getStopTime().getArrivalTime()
             - lastBlockStopTime.getStopTime().getDepartureTime();
         int fudge = (int) (expectedTimeDelta * deviationFactor);
         int expectedTimeDeltaMin = expectedTimeDelta - fudge;
         int expectedTimeDeltaMax = expectedTimeDelta + fudge;
 
         int actualTimeDelta = (int) ((recordForStop.getTimepointTime() - lastRecord.getTimepointTime()) / 1000);
         if (expectedTimeDeltaMin <= actualTimeDelta
             && actualTimeDelta <= expectedTimeDeltaMax) {
           return vehicle;
         }
       }
     }
 
     return null;
   }
 
   private List<VehicleState> getLinkedVehicleRecordsAsVehicleStates(
       BlockInstance blockInstance, List<VehicleRecords> allVehicleRecords) {
 
     List<VehicleState> states = new ArrayList<VehicleState>();
     for (VehicleRecords vehicleRecords : allVehicleRecords) {
       VehicleState state = getVehicleRecordsAsVehicleState(blockInstance,
           vehicleRecords);
       states.add(state);
     }
     return states;
 
   }
 
   private VehicleState getVehicleRecordsAsVehicleState(
       BlockInstance blockInstance, VehicleRecords vehicleRecords) {
 
     BlockConfigurationEntry blockConfig = blockInstance.getBlock();
     List<BlockStopTimeEntry> stopTimes = blockConfig.getStopTimes();
 
     Record nextRecord = vehicleRecords.getFirstRecord();
     BlockStopTimeEntry nextStopTime = vehicleRecords.getFirstBlockStopTime();
 
     int timeToFirstStop = (int) ((nextRecord.getTimepointTime() - nextRecord.getTime()) / 1000);
     int scheduleTime = nextStopTime.getStopTime().getArrivalTime()
         - timeToFirstStop;
 
     BlockStopTimeEntry firstStopTime = stopTimes.get(0);
     int firstArrival = firstStopTime.getStopTime().getArrivalTime();
 
     if (scheduleTime < firstArrival) {
       ScheduledBlockLocation scheduledBlockLocation = _scheduledBlockLocationService.getScheduledBlockLocationFromScheduledTime(
          stopTimes, firstArrival);
       return new VehicleState(blockInstance, scheduledBlockLocation,
           scheduleTime, EVehiclePhase.LAYOVER_BEFORE, nextRecord.getTime(),
           vehicleRecords);
     }
 
     ScheduledBlockLocation scheduledBlockLocation = _scheduledBlockLocationService.getScheduledBlockLocationFromScheduledTime(
        stopTimes, scheduleTime);
     return new VehicleState(blockInstance, scheduledBlockLocation,
         scheduleTime, EVehiclePhase.IN_PROGRESS, nextRecord.getTime(),
         vehicleRecords);
   }
 
   private VehicleInstance getBestPreviousVehicleInstance(
       List<VehicleInstance> prevVehicles, VehicleState state) {
 
     for (VehicleInstance prevVehicle : prevVehicles) {
 
       VehicleState prevState = prevVehicle.getState();
 
       int effectiveDelta = state.getEffectiveScheduleTime()
           - prevState.getEffectiveScheduleTime();
 
       // The train can't backup... well, maybe just a little bit
       if (effectiveDelta < -90) {
         continue;
       }
 
       int actualDelta = (int) ((state.getUpdateTime() - prevState.getUpdateTime()) / 1000);
 
       double effectiveDrift = Math.abs(effectiveDelta - actualDelta);
       double maxDrift = actualDelta * .25 + 3 * 60;
 
       if (effectiveDrift < maxDrift) {
         return prevVehicle;
       }
     }
 
     return null;
   }
 
   private VehicleLocationRecord getVehicleInstanceAsVehicleLocationRecord(
       VehicleInstance instance) {
 
     VehicleState state = instance.getState();
     BlockInstance blockInstance = state.getBlockInstance();
     BlockConfigurationEntry blockConfig = blockInstance.getBlock();
     BlockEntry block = blockConfig.getBlock();
     ScheduledBlockLocation blockLocation = state.getBlockLocation();
 
     VehicleRecords vehicleRecords = state.getRecords();
     List<TimepointPredictionRecord> timepointPredictions = getRecordsAsTimepointPredictions(vehicleRecords);
 
     VehicleLocationRecord r = new VehicleLocationRecord();
     r.setBlockId(block.getId());
     r.setPhase(state.getPhase());
     r.setServiceDate(blockInstance.getServiceDate());
     r.setDistanceAlongBlock(blockLocation.getDistanceAlongBlock());
     r.setTimeOfRecord(state.getUpdateTime());
     r.setTimepointPredictions(timepointPredictions);
     r.setVehicleId(instance.getVehicleId());
 
     int scheduleTime = (int) ((state.getUpdateTime() - blockInstance.getServiceDate()) / 1000);
     int scheduleDeviation = (int) (scheduleTime - state.getEffectiveScheduleTime());
     r.setScheduleDeviation(scheduleDeviation);
 
     return r;
   }
 
   private List<TimepointPredictionRecord> getRecordsAsTimepointPredictions(
       VehicleRecords vehicleRecords) {
 
     List<Record> records = vehicleRecords.getRecords();
     List<BlockStopTimeEntry> blockStopTimes = vehicleRecords.getBlockStopTimes();
 
     int n = records.size();
     List<TimepointPredictionRecord> results = new ArrayList<TimepointPredictionRecord>(
         n);
 
     for (int i = 0; i < n; i++) {
       Record record = records.get(i);
       BlockStopTimeEntry blockStopTime = blockStopTimes.get(i);
       TimepointPredictionRecord tpr = new TimepointPredictionRecord();
       tpr.setTimepointId(blockStopTime.getStopTime().getStop().getId());
       tpr.setTimepointPredictedTime(record.getTimepointTime());
       results.add(tpr);
     }
 
     return results;
   }
 
   private void dumpVehicleInstance(VehicleInstance instance) {
     AgencyAndId vehicleId = instance.getVehicleId();
     System.out.println("== vehicle=" + vehicleId + " ==");
     VehicleState state = instance.getState();
     System.out.println("  effectiveScheduleTime="
         + state.getEffectiveScheduleTime());
     System.out.println("  block="
         + state.getBlockInstance().getBlock().getBlock().getId());
     System.out.println("  location=" + state.getBlockLocation().getLocation());
     System.out.println("  phase=" + state.getPhase());
     VehicleRecords vehicleRecords = state.getRecords();
 
     for (Record record : vehicleRecords.getRecords())
       System.out.println("  " + record.getNextSign() + " "
           + _format.format(new Date(record.getTimepointTime())));
   }
 
   /****
    * 
    ****/
 
   private class RefreshTask implements Runnable, EntityHandler {
 
     private DefaultHttpClient _client = new DefaultHttpClient();
 
     private CsvEntityReader _reader = new CsvEntityReader();
 
     private List<Record> _records = new ArrayList<Record>();
 
     public RefreshTask() {
 
       if (_username != null) {
 
         Credentials credentials = null;
 
         if (_useNtlm) {
           _client.getAuthSchemes().register("ntlm", new NTLMSchemeFactory());
           credentials = new NTCredentials(_username, _password, "MYSERVER",
               _domain);
         } else {
           credentials = new UsernamePasswordCredentials(_username, _password);
         }
 
         _client.getCredentialsProvider().setCredentials(AuthScope.ANY,
             credentials);
       }
 
       _reader.addEntityHandler(this);
     }
 
     @Override
     public void run() {
 
       try {
 
         _records.clear();
 
         HttpGet get = new HttpGet(_url);
         HttpResponse response = _client.execute(get);
         HttpEntity entity = response.getEntity();
 
         if (entity != null) {
           InputStream in = entity.getContent();
           _reader.readEntities(Record.class, in);
           in.close();
         }
 
         handleRecords(_records);
 
         _records.clear();
 
       } catch (Exception ex) {
         _log.warn("error querying realtime data", ex);
       }
     }
 
     @Override
     public void handleEntity(Object obj) {
       _records.add((Record) obj);
     }
   }
 
 }
