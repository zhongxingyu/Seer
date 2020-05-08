 package com.vmware.vcloud.nclient;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
import java.util.TimeZone;
 import java.util.Date;
 
 import com.rabbitmq.client.AMQP.BasicProperties;
 import com.rabbitmq.client.Channel;
 import com.rabbitmq.client.Connection;
 import com.rabbitmq.client.ConnectionFactory;
 import com.rabbitmq.client.MessageProperties;
 
 import org.codehaus.jackson.JsonEncoding;
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonGenerator;
 import org.testng.annotations.Test;
 
 import static com.vmware.vcloud.nclient.NotificationMessage.JSON_CONTENT_TYPE;
 import static com.vmware.vcloud.nclient.NotificationMessage.XML_CONTENT_TYPE;
 import static org.testng.Assert.assertEquals;
 
 @Test(groups = {"unit"})
 public class NotificationMessageTest {
 
     public static final String KEY_ID = "eventId";
     public static final String KEY_TYPE = "type";
     public static final String KEY_STATUS = "operationSuccess";
     public static final String KEY_TIMESTAMP = "timestamp";
     public static final String KEY_ENTITY = "entity";
     public static final String KEY_USER = "user";
     public static final String KEY_ORG = "org";
     public static final String KEY_TASK = "task";
     public static final String KEY_TASK_OWNER = "taskOwner";
 
     @Test
     public void testXmlParsing() throws Exception {
         String payload = getXmlPayload();
         NotificationMessage msg = NotificationMessage.createFromPayloadAndHeaders(payload, null);
         assertEquals(msg.getType(), "com/vmware/vcloud/event/blockingtask/create");
         assertEquals(msg.getEntityType(), "blockingTask");
         assertEquals(msg.getEntityName(), "vappUpdateVm");
         assertEquals(msg.getEntityHref(), "https://10.23.6.35/api/entity/urn:vcloud:blockingTask:41aaf964-7452-42e6-9b8d-772e5f8421d8");
         assertEquals(msg.getOrgName(), "Default");
         assertEquals(msg.getUserName(), "vcloud");
         Calendar cal = Calendar.getInstance();
         cal.set(2011, 5, 10, 10, 4, 49);
         cal.set(Calendar.MILLISECOND, 47);
        cal.setTimeZone(TimeZone.getTimeZone("GMT+1"));
         assertEquals(msg.getTimestamp(), cal.getTime());
     }
 
     @Test
     public void testJsonParsing() throws Exception {
         String payload = getJsonPayload();
         NotificationMessage msg = NotificationMessage.createFromPayloadAndHeaders(payload, null);
         assertEquals(msg.getType(), "com/vmware/foobar");
         assertEquals(msg.getEntityType(), "blockingTask");
         assertEquals(msg.getEntityName(), "");
         assertEquals(msg.getEntityHref(), "");
         assertEquals(msg.getOrgName(), "");
         assertEquals(msg.getUserName(), "");
         Calendar cal = Calendar.getInstance();
         cal.set(2012, 9, 8, 16, 23, 8);
         cal.set(Calendar.MILLISECOND, 842);
         assertEquals(msg.getTimestamp(), cal.getTime());
     }
 
     @Test(enabled=false)
     public void createJsonNotification() throws IOException {
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
         JsonFactory jsonFactory = new JsonFactory();
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         JsonGenerator jg = jsonFactory.createJsonGenerator(baos, JsonEncoding.UTF8);
         jg.writeStartObject();
         jg.writeStringField(KEY_ID, "1234567");
         jg.writeStringField(KEY_TYPE, "com/vmware/fira");
         jg.writeBooleanField(KEY_STATUS, true);
         String timestamp = sdf.format(new Date());
         jg.writeStringField(KEY_TIMESTAMP, timestamp);
         jg.writeStringField(KEY_USER, "urn:vcloud:user:74a39795-2307-4667-ba2a-2eddc7e52325");
         jg.writeStringField(KEY_ORG, "urn:vcloud:org:251ae0f6-9780-429d-a8cf-9495c036eef2");
         jg.writeStringField(KEY_ENTITY, "urn:vcloud:blockingTask:41aaf964-7452-42e6-9b8d-772e5f8421d8");
         jg.close();
         String json = new String(baos.toByteArray(), "UTF-8");
         System.out.println(json);
     }
 
     @Test(enabled=false)
     public void publish() throws IOException {
         ConnectionFactory factory = new ConnectionFactory();
         Connection connection = factory.newConnection();
         Channel channel = connection.createChannel();
         BasicProperties props = MessageProperties.PERSISTENT_BASIC;
         props.setContentEncoding("UTF-8");
         props.setContentType(XML_CONTENT_TYPE);
         channel.basicPublish("systemExchange", "foo", props, getXmlPayload().getBytes("UTF-8"));
         props.setContentType(JSON_CONTENT_TYPE);
         channel.basicPublish("systemExchange", "foo", props, getJsonPayload().getBytes("UTF-8"));
         channel.close();
         connection.close();
     }
 
     String getXmlPayload() {
         return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
         		"<vmext:Notification xmlns:vmext=\"http://www.vmware.com/vcloud/extension/v1.5\" type=\"com/vmware/vcloud/event/blockingtask/create\" eventId=\"55aba16e-427c-431a-9949-00db6aa78e5a\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.vmware.com/vcloud/extension/v1.5 http://10.23.6.35/api/v1.5/schema/vmwextensions.xsd\">\r\n" +
         		"    <vmext:Link rel=\"entityResolver\" href=\"https://10.23.6.35/api/entity/\"/>\r\n" +
         		"    <vmext:EntityLink rel=\"entity\" type=\"vcloud:blockingTask\" name=\"vappUpdateVm\" id=\"urn:vcloud:blockingTask:41aaf964-7452-42e6-9b8d-772e5f8421d8\"/>\r\n" +
         		"    <vmext:EntityLink rel=\"down\" type=\"vcloud:user\" name=\"vcloud\" id=\"urn:vcloud:user:74a39795-2307-4667-ba2a-2eddc7e52325\"/>\r\n" +
         		"    <vmext:EntityLink rel=\"up\" type=\"vcloud:org\" name=\"Default\" id=\"urn:vcloud:org:251ae0f6-9780-429d-a8cf-9495c036eef2\"/>\r\n" +
         		"    <vmext:EntityLink rel=\"task\" type=\"vcloud:task\" name=\"vappUpdateVm\" id=\"urn:vcloud:task:a56be2f7-2cb4-4561-a7ef-ee2fdd2d3a15\"/>\r\n" +
         		"    <vmext:EntityLink rel=\"task:owner\" type=\"vcloud:vm\" id=\"urn:vcloud:vm:26839d04-5050-4702-a602-0667be86dad6\"/>\r\n" +
        		"    <vmext:Timestamp>2011-06-10T10:04:49.047+01:00</vmext:Timestamp>\r\n" +
         		"    <vmext:OperationSuccess>true</vmext:OperationSuccess>\r\n" +
         		"</vmext:Notification>";
     }
 
     String getJsonPayload() {
         return "{\"eventId\":\"55aba16e-427c-431a-9949-00db6aa78e5b\",\"type\":\"com/vmware/foobar\",\"operationSuccess\":true,\"timestamp\":\"2012-10-08 16:23:08.842\",\"user\":\"urn:vcloud:user:74a39795-2307-4667-ba2a-2eddc7e52325\",\"org\":\"urn:vcloud:org:251ae0f6-9780-429d-a8cf-9495c036eef2\",\"entity\":\"urn:vcloud:blockingTask:41aaf964-7452-42e6-9b8d-772e5f8421d8\"}";
     }
 
 }
