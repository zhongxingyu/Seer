 package cn.poe.group1.db;
 
 import cn.poe.group1.api.MeasurementBackend;
 import cn.poe.group1.entity.Measurement;
 import cn.poe.group1.entity.Port;
 import cn.poe.group1.entity.PortStatus;
 import cn.poe.group1.entity.Switch;
 import java.util.Calendar;
 import java.util.List;
 import javax.persistence.Persistence;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 
 /**
  * This is a JUnit test class for the measurement database implementation.
  */
 public class MeasurementBackendTest {
     private static final String FACTORY_NAME = "poe-snmp-tool-test";
     private static MeasurementBackend backend;
     
     @BeforeClass
     public static void init() {
         backend = new MeasurementDatabase(Persistence.createEntityManagerFactory(FACTORY_NAME).createEntityManager());
     }
 
     @Test
     public void testPersistSwitch_shouldInsertSwitchs() throws Exception {
         int size_before = backend.retrieveAllSwitches().size();
        backend.persistSwitch(new Switch("test1/id", "testIp", "testtype", 20, "", "public"));
        backend.persistSwitch(new Switch("test1/id2", "testIp2", "testtype2", 30, "", "public"));
         
         int size_after = backend.retrieveAllSwitches().size();
         assertThat(size_after, is(size_before + 2));
     }
     
     @Test
     public void testUpdateSwitch_shouldUpdateSwitch() throws Exception {
        Switch sw = new Switch("test2/id", "testIp", "testtype", 20, "", "public");
         backend.persistSwitch(sw);
         sw = backend.getSwitchById("test2/id");
         assertThat(sw, notNullValue());
         assertThat(sw.getIdentifier(), is("test2/id"));
         assertThat(sw.getType(), is("testtype"));
         
         sw.setType("otherType");
         backend.persistSwitch(sw);
         sw = backend.getSwitchById("test2/id");
         assertThat(sw, notNullValue());
         assertThat(sw.getIdentifier(), is("test2/id"));
         assertThat(sw.getType(), is("otherType"));        
     }
     
     @Test
     public void testPersistPort_shouldInsertPorts() throws Exception {
        Switch sw = new Switch("test3/id", "testIp", "testtype", 4, "", "public");
         backend.persistSwitch(sw);
         int size_before = backend.retrieveAllPorts(sw).size();
         backend.persistPort(new Port(sw, 1, ""));
         backend.persistPort(new Port(sw, 2, ""));
         int size_after = backend.retrieveAllPorts(sw).size();
         assertThat(size_after, is(size_before + 2));
     }
     
     @Test
     public void testUpdatePort_shouldUpdatePort() throws Exception {
        Switch sw = new Switch("test4/id", "testIp", "testtype", 4, "", "public");
         backend.persistSwitch(sw);
         Port port = new Port(sw, 1, "testComment");
         backend.persistPort(port);
         port = backend.getPortById(port.getId());
         assertThat(port, notNullValue());
         assertThat(port.getComment(), is("testComment"));
         port.setComment("newTestComment");
         backend.persistPort(port);
         port = backend.getPortById(port.getId());
         assertThat(port.getComment(), is("newTestComment"));
     }
     
     @Test
     public void testLoadMeasurementsWithIncorrectSwitchId_shouldReturnNoResult() throws Exception {
        Switch sw = new Switch("test5/id", "testIp", "testtype", 20, "", "public");
         backend.persistSwitch(sw);
         Port port1 = new Port(sw, 1, "");
         Port port2 = new Port(sw, 2, "");
         backend.persistPort(port1);
         backend.persistPort(port2);
         
         Calendar begin = getBeginTime();
         Calendar end = getEndTime();
         
         insertTestMeasurements(port1, port2);
        sw = new Switch("nonsenseid", "testIp", "testtype", 20, "", "public");
         List<Measurement> results = backend.queryMeasurementsBySwitch(sw,  
                 begin.getTime(), end.getTime());
         
         assertThat(results.size(), is(0));
     }
     
     @Test
     public void testLoadMeasurementsWithIncorrectTimes_shouldReturnNoResult() throws Exception {
        Switch sw = new Switch("test6/id", "testIp", "testtype", 20, "", "public");
         backend.persistSwitch(sw);
         Port port1 = new Port(sw, 1, "");
         Port port2 = new Port(sw, 2, "");
         backend.persistPort(port1);
         backend.persistPort(port2);
         
         Calendar begin = Calendar.getInstance();
         Calendar end = Calendar.getInstance();
         end.add(Calendar.MINUTE, 1);
         
         insertTestMeasurements(port1, port2);
         
         List<Measurement> results = backend.queryMeasurementsBySwitch(sw, 
                 begin.getTime(), end.getTime());
         assertThat(results.size(), is(0));
     }
     
     @Test
     public void testLoadMeasurementsByPort_shouldLoadResult() throws Exception {
        Switch sw = new Switch("test7/id", "testIp", "testtype", 20, "", "public");
         backend.persistSwitch(sw);
         Port port1 = new Port(sw, 1, "");
         Port port2 = new Port(sw, 2, "");
         backend.persistPort(port1);
         backend.persistPort(port2);
         
         Calendar begin = getBeginTime();
         Calendar end = getEndTime();
         
         insertTestMeasurements(port1, port2);
         
         List<Measurement> results = backend.queryMeasurementsByPort(port1, 
                 begin.getTime(), end.getTime());
         assertThat(results.size(), is(3));
         
         results = backend.queryMeasurementsByPort(port2, begin.getTime(), 
                 end.getTime());
         assertThat(results.size(), is(3));
         
         begin.add(Calendar.MINUTE, 1);
         begin.add(Calendar.SECOND, 30);
         results = backend.queryMeasurementsByPort(port2, begin.getTime(), 
                 end.getTime());
         assertThat(results.size(), is(2));
     }
     
     @Test
     public void testLoadMeasurementsBySwitch_shouldLoadResult() throws Exception {
        Switch sw = new Switch("test8/id", "testIp", "testtype", 20, "", "public");
         backend.persistSwitch(sw);
         Port port1 = new Port(sw, 1, "");
         Port port2 = new Port(sw, 2, "");
         backend.persistPort(port1);
         backend.persistPort(port2);
         
         Calendar begin = getBeginTime();
         Calendar end = getEndTime();
         
         insertTestMeasurements(port1, port2);
         
         List<Measurement> results = backend.queryMeasurementsBySwitch(sw, 
                 begin.getTime(), end.getTime());
         assertThat(results.size(), is(6));
         
         begin.add(Calendar.MINUTE, 1);
         begin.add(Calendar.SECOND, 30);
         results = backend.queryMeasurementsBySwitch(sw, begin.getTime(), 
                 end.getTime());
         assertThat(results.size(), is(4));
     }
     
     @Test
     public void testMeasurementSavingAndRetrievingWorks_shouldWork() throws Exception {
        Switch sw = new Switch("test9/id", "testIp", "testtype", 20, "", "public");
         backend.persistSwitch(sw);
         Port port = new Port(sw, 1, "");
         backend.persistPort(port);
         Calendar begin = Calendar.getInstance();
         Measurement measurement = new Measurement(port, begin.getTime());
         measurement.setCpeExtPsePortDeviceDetected(true);
         measurement.setCpeExtPsePortEnable(PortStatus.LIMIT);
         measurement.setCpeExtPsePortMaxPwrDrawn(1500);
         measurement.setCpeExtPsePortPwrAllocated(1300);
         measurement.setCpeExtPsePortPwrAvailable(1400);
         measurement.setCpeExtPsePortPwrConsumption(1000);
         measurement.setCpeExtPsePortPwrMax(1500);
         backend.saveMeasurement(measurement);
         
         Measurement m = backend.queryMeasurementsByPort(port).get(0);
         assertThat(m, notNullValue());
         assertThat(m.getCpeExtPsePortDeviceDetected(), is(measurement.getCpeExtPsePortDeviceDetected()));
         assertThat(m.getCpeExtPsePortEnable(), is(measurement.getCpeExtPsePortEnable()));
         assertThat(m.getCpeExtPsePortMaxPwrDrawn(), is(measurement.getCpeExtPsePortMaxPwrDrawn()));
         assertThat(m.getCpeExtPsePortPwrAllocated(), is(measurement.getCpeExtPsePortPwrAllocated()));
         assertThat(m.getCpeExtPsePortPwrAvailable(), is(measurement.getCpeExtPsePortPwrAvailable()));
         assertThat(m.getCpeExtPsePortPwrConsumption(), is(measurement.getCpeExtPsePortPwrConsumption()));
         assertThat(m.getCpeExtPsePortPwrMax(), is(measurement.getCpeExtPsePortPwrMax()));
     }
     
     @Test
     public void testPersistSwitchAndPorts_shouldWork() throws Exception {
        Switch sw = new Switch("test10/id", "testIp", "testtype", 20, "", "public");
         Port port1 = new Port(sw, 1, "testComment");
         Port port2 = new Port(sw, 2, "testComment");
         sw.addPort(port1);
         sw.addPort(port2);
         int size_before = backend.retrieveAllPorts(sw).size();
         backend.persistSwitch(sw);        
         int size_after = backend.retrieveAllPorts(sw).size();
         
         Switch sw1 = backend.getSwitchById("test10/id");
         assertThat(sw1, notNullValue());
         List<Port> sw1Ports = sw1.getPorts();
         assertThat(sw1Ports, notNullValue());
         assertEquals(2, sw1Ports.size());
         assertThat(size_after, is(size_before + 2));
     }
     
     private Calendar getBeginTime() {
         Calendar begin = Calendar.getInstance();
         begin.set(2013, 5, 8, 10, 29, 0);
         return begin;
     }
     
     private Calendar getEndTime() {
         Calendar end = Calendar.getInstance();
         end.set(2013, 5, 8, 10, 33, 0);
         return end;
     }
     
     private void insertTestMeasurements(Port port1, Port port2) throws Exception {
         Calendar c = getBeginTime();
         c.set(Calendar.MINUTE, 30);
         backend.saveMeasurement(new Measurement(port1, c.getTime()));
         backend.saveMeasurement(new Measurement(port2, c.getTime()));
         
         c.set(Calendar.MINUTE, 31);
         backend.saveMeasurement(new Measurement(port1, c.getTime()));
         backend.saveMeasurement(new Measurement(port2, c.getTime()));
         
         c.set(Calendar.MINUTE, 32);
         backend.saveMeasurement(new Measurement(port1, c.getTime()));
         backend.saveMeasurement(new Measurement(port2, c.getTime()));
     }
 }
