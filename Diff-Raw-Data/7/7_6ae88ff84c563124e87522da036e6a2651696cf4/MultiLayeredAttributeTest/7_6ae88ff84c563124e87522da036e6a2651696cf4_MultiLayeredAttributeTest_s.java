 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.jmxdatamart.Extractor.MXBean;
 
 import java.lang.management.ManagementFactory;
 import java.util.Map;
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 import org.jmxdatamart.Extractor.Attribute;
 import org.jmxdatamart.JMXTestServer.CarBean;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import static org.hamcrest.Matchers.*;
 import org.jmxdatamart.JMXTestServer.Car;
 import org.jmxdatamart.JMXTestServer.TestBean;
 import org.jmxdatamart.common.DataType;
 
 /**
  *
  * @author Binh Tran <mynameisbinh@gmail.com>
  */
 public class MultiLayeredAttributeTest {
     
     @Test
     public void testMultiLayeredAttribute() throws Exception{
         // note: test PS MarkSweep
         MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
         ObjectName mS = new ObjectName("java.lang:name=CarBean");
         CarBean cb = new CarBean();
         mbs.registerMBean(cb, mS);
         
         MultiLayeredAttribute mla = new MultiLayeredAttribute(mbs);
         Attribute attr = new Attribute("Car.*", null, null);
         Map<Attribute, Object> result = mla.getAll(mS, attr);
         
         assertTrue ( result.size() == 4);
         
         for (Map.Entry<Attribute, Object> entry : result.entrySet()) {
             if (entry.getKey().getAlias().equals("Car_name")) {
                 assertThat(entry.getValue().toString(), equalTo(CarBean.NAME));
             } else if (entry.getKey().getAlias().equals("Car_year")) {
                 assertThat(entry.getValue().toString(), equalTo(CarBean.YEAR));
             } else if (entry.getKey().getAlias().equals("Car_engine")) {
                 assertThat(entry.getValue().toString(), 
                         equalTo((new Integer(CarBean.ENGINE)).toString()));
             } else if (entry.getKey().getAlias().equals("Car_power")) {
                 assertThat(entry.getValue().toString(),
                         equalTo((new Integer(CarBean.POWER)).toString()));
             } else {
                 fail("Unknown attribute " + entry.getKey().toString());
             }
         }
         attr = new Attribute("Map.*.*", null, null);
         result = mla.getAll(mS, attr);
         
         assertEquals(cb.getMap().size() * 4, result.size());
         mbs.unregisterMBean(mS);
     }
     
     @Test
     public void testSameLevelPattern() throws Exception{
         TestBean tb = new TestBean();
         tb.setA(new Integer(42));
         tb.setB(new Long(8));
         MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
         String mbName = "org.jmxdatamart.JMXTestServer:type=TestBean";
         ObjectName mbeanName = new ObjectName(mbName);
         mbs.registerMBean(tb, mbeanName);
         
         MultiLayeredAttribute mla = new MultiLayeredAttribute(mbs);
         Attribute attr = new Attribute("*", null, null);
         Map<Attribute, Object> result = mla.getAll(mbeanName, attr);
         
        assertEquals(2, result.size());
         for (Map.Entry<Attribute, Object> entry : result.entrySet()) {
             if (entry.getKey().getAlias().equals("A")) {
                 assertThat(entry.getValue().toString(), equalTo("42"));
             } else if (entry.getKey().getAlias().equals("B")){
                 assertThat(entry.getValue().toString(), equalTo("8"));
             } else {
                 fail("Unknown attribute " + entry.getKey());
             }
         }
         mbs.unregisterMBean(mbeanName);
     }
 }
