 /*
  *  The MIT License
  *
  *  Copyright 2011 Sony Ericsson Mobile Communications. All rights reserved.
  *
  *  Permission is hereby granted, free of charge, to any person obtaining a copy
  *  of this software and associated documentation files (the "Software"), to deal
  *  in the Software without restriction, including without limitation the rights
  *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  *  copies of the Software, and to permit persons to whom the Software is
  *  furnished to do so, subject to the following conditions:
  *
  *  The above copyright notice and this permission notice shall be included in
  *  all copies or substantial portions of the Software.
  *
  *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  *  THE SOFTWARE.
  */
 package com.sonyericsson.jenkins.plugins.externalresource.dispatcher.data;
 
 import com.sonyericsson.hudson.plugins.metadata.model.JsonUtils;
 import com.sonyericsson.hudson.plugins.metadata.model.MetadataJobProperty;
 import com.sonyericsson.hudson.plugins.metadata.model.MetadataNodeProperty;
 import com.sonyericsson.hudson.plugins.metadata.model.values.MetadataValue;
 import com.sonyericsson.hudson.plugins.metadata.model.values.TreeStructureUtil;
 import com.sonyericsson.jenkins.plugins.externalresource.dispatcher.Constants;
 import com.sonyericsson.jenkins.plugins.externalresource.dispatcher.MockUtils;
 import hudson.model.Hudson;
 import net.sf.json.JSONObject;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.LinkedList;
 import java.util.TimeZone;
 import java.util.concurrent.TimeUnit;
 
 import static com.sonyericsson.jenkins.plugins.externalresource.dispatcher.Constants.JSON_ATTR_ENABLED;
 import static com.sonyericsson.jenkins.plugins.externalresource.dispatcher.Constants.JSON_ATTR_ID;
 import static com.sonyericsson.jenkins.plugins.externalresource.dispatcher.Constants.JSON_ATTR_LOCKED;
 import static com.sonyericsson.jenkins.plugins.externalresource.dispatcher.Constants.JSON_ATTR_RESERVED;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNotSame;
 import static org.junit.Assert.assertTrue;
 
 //CS IGNORE MagicNumber FOR NEXT 200 LINES. REASON: Test data.
 
 /**
  * Tests for {@link ExternalResource} and it's descriptor.
  *
  * @author Robert Sandell &lt;robert.sandell@sonyericsson.com&gt;
  */
 @RunWith(PowerMockRunner.class)
 public class ExternalResourceTest {
 
     //CS IGNORE LineLength FOR NEXT 4 LINES. REASON: JavaDoc
 
     /**
      * Tests {@link com.sonyericsson.jenkins.plugins.externalresource.dispatcher.data.ExternalResource.ExternalResourceDescriptor#appliesTo(hudson.model.Descriptor)}
      * that it doesn't apply to a Job.
      */
     @Test
     public void testAppliesToJob() {
         ExternalResource.ExternalResourceDescriptor descriptor = new ExternalResource.ExternalResourceDescriptor();
         assertFalse(descriptor.appliesTo(new MetadataJobProperty.MetaDataJobPropertyDescriptor()));
     }
 
     //CS IGNORE LineLength FOR NEXT 4 LINES. REASON: JavaDoc
 
     /**
      * Tests {@link com.sonyericsson.jenkins.plugins.externalresource.dispatcher.data.ExternalResource.ExternalResourceDescriptor#appliesTo(hudson.model.Descriptor)}
      * that it does apply to a node.
      */
     @Test
     public void testAppliesToBuild() {
         ExternalResource.ExternalResourceDescriptor descriptor = new ExternalResource.ExternalResourceDescriptor();
         assertTrue(descriptor.appliesTo(new MetadataNodeProperty.MetadataNodePropertyDescriptor()));
     }
 
     /**
      * Tests {@link Lease#createInstance(long, int, String)}. When the slave timeZone is in Japan and the local server
      * is here.
      */
     @Test
     public void testLeaseCreateInstanceJapan() {
         // Given a time of 10am in Japan, get the server time
         Calendar japanCal = new GregorianCalendar(TimeZone.getTimeZone("Japan"));
 
         japanCal.set(Calendar.HOUR_OF_DAY, 10);            // 0..23
         japanCal.set(Calendar.MINUTE, 0);
         japanCal.set(Calendar.SECOND, 0);
 
        int seconds = (int)TimeUnit.MILLISECONDS.toSeconds(japanCal.getTimeZone().getRawOffset()
                                                            + japanCal.getTimeZone().getDSTSavings());
 
         Lease lease = Lease.createInstance(japanCal.getTimeInMillis(), seconds, "Japan RuleZ");
 
         Calendar local = new GregorianCalendar();
        int localOffset = (int)TimeUnit.MILLISECONDS.toHours(local.getTimeZone().getRawOffset()
                                                                + local.getTimeZone().getDSTSavings());
         int japanOffset = (int)TimeUnit.SECONDS.toHours(seconds);
 
         assertEquals(10 - japanOffset + localOffset, lease.getServerTime().get(Calendar.HOUR_OF_DAY));
     }
 
     /**
      * Tests {@link Lease#createInstance(long, int, String)}. When the slave timeZone is PST and the local server is
      * here.
      */
     @Test
     public void testLeaseCreateInstanceSf() {
         // Given a time of 9am in San Francisco, get the server time
         Calendar sfCal = new GregorianCalendar(TimeZone.getTimeZone("PST"));
 
         sfCal.set(Calendar.HOUR_OF_DAY, 9);            // 0..23
         sfCal.set(Calendar.MINUTE, 0);
         sfCal.set(Calendar.SECOND, 0);
 
         int seconds = (int)TimeUnit.MILLISECONDS.toSeconds(sfCal.getTimeZone().getRawOffset());
 
         Lease lease = Lease.createInstance(sfCal.getTimeInMillis(), seconds, "SF RuleZ");
 
         Calendar local = new GregorianCalendar();
         int localOffset = (int)TimeUnit.MILLISECONDS.toHours(local.getTimeZone().getRawOffset());
         int sfOffset = (int)TimeUnit.SECONDS.toHours(seconds);
 
         assertEquals(9 - sfOffset + localOffset, lease.getServerTime().get(Calendar.HOUR_OF_DAY));
     }
 
     /**
      * Tests {@link ExternalResource#toJson()}.
      */
     @PrepareForTest(Hudson.class)
     @Test
     public void testToJson() {
         Hudson hudson = MockUtils.mockHudson();
         MockUtils.mockMetadataValueDescriptors(hudson);
 
         String name = "name";
         String description = "description";
         String id = "id";
         ExternalResource resource = new ExternalResource(name, description, id,
                 true, new LinkedList<MetadataValue>());
         String me = "me";
         resource.setReserved(
                 new StashInfo(StashInfo.StashType.INTERNAL, me, new Lease(Calendar.getInstance(), "iso"), "key"));
         TreeStructureUtil.addValue(resource, "value", "descript", "some", "path");
 
         JSONObject json = resource.toJson();
         assertEquals(name, json.getString(JsonUtils.NAME));
         assertEquals(id, json.getString(JSON_ATTR_ID));
         assertTrue(json.getBoolean(JSON_ATTR_ENABLED));
         assertTrue(json.getJSONObject(JSON_ATTR_LOCKED).isNullObject());
         JSONObject reserved = json.getJSONObject(JSON_ATTR_RESERVED);
         assertNotNull(reserved);
         assertEquals(StashInfo.StashType.INTERNAL.name(), reserved.getString(Constants.JSON_ATTR_TYPE));
         assertEquals(me, reserved.getString(Constants.JSON_ATTR_STASHED_BY));
         assertEquals(1, json.getJSONArray(JsonUtils.CHILDREN).size());
     }
 
     /**
      * Tests {@link ExternalResource#clone()}.
      *
      * @throws CloneNotSupportedException if it fails.
      */
     @Test
     public void testClone() throws CloneNotSupportedException {
         String name = "name";
         String description = "description";
         String id = "id";
         ExternalResource resource = new ExternalResource(name, description, id,
                 true, new LinkedList<MetadataValue>());
         String me = "me";
         resource.setReserved(
                 new StashInfo(StashInfo.StashType.INTERNAL, me, new Lease(Calendar.getInstance(), "iso"), "key"));
         TreeStructureUtil.addValue(resource, "value", "descript", "some", "path");
 
         ExternalResource other = resource.clone();
 
         assertEquals(name, other.getName());
         assertEquals(id, other.getId());
         assertNotNull(other.getReserved());
         assertNotSame(resource.getReserved(), other.getReserved());
         assertEquals(resource.getReserved().getStashedBy(), other.getReserved().getStashedBy());
         assertNotSame(resource.getReserved().getLease(), other.getReserved().getLease());
         assertEquals(resource.getReserved().getLease().getSlaveIsoTime(),
                 other.getReserved().getLease().getSlaveIsoTime());
         assertNotSame(TreeStructureUtil.getPath(resource, "some", "path"),
                 TreeStructureUtil.getPath(other, "some", "path"));
         assertEquals(TreeStructureUtil.getPath(resource, "some", "path").getValue(),
                 TreeStructureUtil.getPath(other, "some", "path").getValue());
         assertTrue(other.isEnabled());
 
     }
 
     /**
      * Tests {@link ExternalResource#isEnabled()} when enables is set to null (not set).
      */
     @Test
     public void testIsEnabledNotSet() {
         ExternalResource resource = new ExternalResource("name", "description", "id", null, null);
         assertTrue(resource.isEnabled());
     }
 
     /**
      * Tests {@link ExternalResource#isEnabled()} when enables is set to false.
      */
     @Test
     public void testIsEnabledFalse() {
         ExternalResource resource = new ExternalResource("name", "description", "id", null, null);
         resource.setEnabled(false);
         assertFalse(resource.isEnabled());
     }
 
     /**
      * Tests {@link ExternalResource#isEnabled()} when enables is set to true.
      */
     @Test
     public void testIsEnabledTrue() {
         ExternalResource resource = new ExternalResource("name", "description", "id", null, null);
         resource.setEnabled(true);
         assertTrue(resource.isEnabled());
     }
 }
