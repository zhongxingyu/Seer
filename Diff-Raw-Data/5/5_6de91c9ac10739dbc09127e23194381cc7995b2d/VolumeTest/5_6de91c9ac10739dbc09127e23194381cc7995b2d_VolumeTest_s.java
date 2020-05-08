 package com.bt.pi.app.common.entities;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 import com.bt.pi.core.parser.KoalaJsonParser;
 
 public class VolumeTest {
 
     @Test
     public void shouldConstructVolume() {
         // setup
         // act
         Volume volume = new Volume();
         volume.setAttachTime(0l);
         volume.setDevice("device");
         volume.setInstanceId("inst");
         volume.setOwnerId("owner");
         volume.setStatus(VolumeState.CREATING);
         volume.setVolumeId("vol");
         volume.setAvailabilityZone("zone");
         volume.setCreateTime(0l);
         // assert
         assertEquals(0l, volume.getCreateTime());
         assertEquals("zone", volume.getAvailabilityZone());
         assertEquals("device", volume.getDevice());
         assertEquals("inst", volume.getInstanceId());
         assertEquals("owner", volume.getOwnerId());
         assertEquals(VolumeState.CREATING, volume.getStatus());
         assertEquals("vol", volume.getVolumeId());
     }
 
     @Test
     public void shouldConstructVolumeUsingFields() {
         // setup
 
         // act
         Volume volume = new Volume("owner", "vol", "inst", "device", VolumeState.CREATING, 0l);
 
         // assert
         assertEquals("device", volume.getDevice());
         assertEquals("inst", volume.getInstanceId());
         assertEquals("owner", volume.getOwnerId());
         assertEquals(VolumeState.CREATING, volume.getStatus());
         assertEquals("vol", volume.getVolumeId());
     }
 
     @Test
     public void shouldBeEqual() {
         // setup
         Volume volume1 = new Volume("owner", "vol", "inst", "device", VolumeState.CREATING, 0l);
         Volume volume2 = new Volume("owner", "vol", "inst", "device", VolumeState.CREATING, 0l);
         Volume volume3 = new Volume("owner", "vol", "inst", "device", VolumeState.CREATING, 0l);
 
         // assert
         assertTrue(volume1.equals(volume2));
         assertTrue(volume1.equals(volume3));
         assertTrue(volume2.equals(volume3));
         assertTrue(volume2.equals(volume1));
         assertTrue(volume3.equals(volume1));
         assertTrue(volume3.equals(volume2));
     }
 
     @Test
     public void shouldHaveSameHashCode() {
         // setup
         Volume volume1 = new Volume("owner", "vol", "inst", "device", VolumeState.CREATING, 0l);
         long aStatusTimestamp = System.currentTimeMillis();
         volume1.setStatusTimestamp(aStatusTimestamp);
         Volume volume2 = new Volume("owner", "vol", "inst", "device", VolumeState.CREATING, 0l);
         volume2.setStatusTimestamp(aStatusTimestamp);
         Volume volume3 = new Volume("owner", "vol", "inst", "device", VolumeState.CREATING, 0l);
         volume3.setStatusTimestamp(aStatusTimestamp);
 
         // assert
         assertEquals(volume1.hashCode(), volume2.hashCode());
         assertEquals(volume2.hashCode(), volume3.hashCode());
         assertEquals(volume3.hashCode(), volume1.hashCode());
     }
 
     @Test
     public void settingStatusShouldAlsoSetStatusTimestamp() throws Exception {
         // setup
         Volume volume1 = new Volume("owner", "vol", "inst", "device", VolumeState.CREATING, 0l);
         // assert
         assertTime(volume1.getStatusTimestamp());
         Thread.sleep(20);
 
         // act
         volume1.setStatus(VolumeState.DELETED);
 
         // assert
         assertTime(volume1.getStatusTimestamp());
     }
 
     private void assertTime(long statusTimestamp) {
         assertTrue(Math.abs(System.currentTimeMillis() - statusTimestamp) < 10);
     }
 
     @Test
     public void testToString() {
         // setup
         Volume volume1 = new Volume("owner", "vol", "inst", "device", VolumeState.CREATING, 0l);
 
         // assert
         assertTrue(volume1.toString().contains("owner"));
     }
 
     @Test
     public void shouldJsonRoundTrip() {
         // setup
         KoalaJsonParser koalaJsonParser = new KoalaJsonParser();
         Volume volume1 = new Volume("owner", "vol", "inst", "device", VolumeState.CREATING, 0l);
 
         // act
         String json = koalaJsonParser.getJson(volume1);
         Volume reverse = (Volume) koalaJsonParser.getObject(json, Volume.class);
 
         // assert
         assertEquals(volume1, reverse);
     }
 
     @Test
     public void shouldConvertOldAttachedToInuse() {
         // setup
         KoalaJsonParser koalaJsonParser = new KoalaJsonParser();
         String json = "{ \"status\" : \"ATTACHED\",  \"type\" : \"Volume\",  \"device\" : \"device\",  \"instanceId\" : \"inst\",  \"attachedStatus\" : null,  \"attachTime\" : 0,  \"sizeInGigaBytes\" : 0,  \"url\" : \"vol:vol\",  \"createTime\" : 0,  \"availabilityZone\" : null,  \"ownerId\" : \"owner\",  \"volumeId\" : \"vol\",  \"statusTimestamp\" : 1271436743946,  \"snapshotId\" : null,  \"version\" : 0}";
 
         // act
         Volume reverse = (Volume) koalaJsonParser.getObject(json, Volume.class);
 
         // assert
         assertEquals(VolumeState.IN_USE, reverse.getStatus());
     }
 
     @Test
     public void shouldConvertOldCreatedToAvailable() {
         // setup
         KoalaJsonParser koalaJsonParser = new KoalaJsonParser();
         String json = "{ \"status\" : \"CREATED\",  \"type\" : \"Volume\",  \"device\" : \"device\",  \"instanceId\" : \"inst\",  \"attachedStatus\" : null,  \"attachTime\" : 0,  \"sizeInGigaBytes\" : 0,  \"url\" : \"vol:vol\",  \"createTime\" : 0,  \"availabilityZone\" : null,  \"ownerId\" : \"owner\",  \"volumeId\" : \"vol\",  \"statusTimestamp\" : 1271436743946,  \"snapshotId\" : null,  \"version\" : 0}";
 
         // act
         Volume reverse = (Volume) koalaJsonParser.getObject(json, Volume.class);
 
         // assert
         assertEquals(VolumeState.AVAILABLE, reverse.getStatus());
     }
 
     @Test
     public void shouldConvertOldPendingToCreating() {
         // setup
         KoalaJsonParser koalaJsonParser = new KoalaJsonParser();
         String json = "{ \"status\" : \"PENDING\",  \"type\" : \"Volume\",  \"device\" : \"device\",  \"instanceId\" : \"inst\",  \"attachedStatus\" : null,  \"attachTime\" : 0,  \"sizeInGigaBytes\" : 0,  \"url\" : \"vol:vol\",  \"createTime\" : 0,  \"availabilityZone\" : null,  \"ownerId\" : \"owner\",  \"volumeId\" : \"vol\",  \"statusTimestamp\" : 1271436743946,  \"snapshotId\" : null,  \"version\" : 0}";
 
         // act
         Volume reverse = (Volume) koalaJsonParser.getObject(json, Volume.class);
 
         // assert
         assertEquals(VolumeState.CREATING, reverse.getStatus());
     }
 
     @Test
     public void testIsDeletedTrue() {
         // setup
         Volume volume = new Volume();
 
         // act
         volume.setStatus(VolumeState.BURIED);
 
         // assert
         assertTrue(volume.isDeleted());
     }
 
     @Test
     public void testThatIsDeletedReturnsFalseForRecentlyDeletedVolumes() throws Exception {
         // setup
         Volume volume = new Volume();
         volume.setStatus(VolumeState.DELETED);
         volume.setStatusTimestamp(System.currentTimeMillis() - (10 * Volume.BURIED_TIME - 1));
 
         // act
         boolean result = volume.isDeleted();
 
         // assert
         assertFalse(result);
     }
 
     @Test
     public void testThatIsDeletedReturnsTrueForVeryOldVolumes() throws Exception {
         // setup
         Volume volume = new Volume();
         volume.setStatus(VolumeState.DELETED);
         volume.setStatusTimestamp(System.currentTimeMillis() - (10 * Volume.BURIED_TIME + 1));
 
         // act
         boolean result = volume.isDeleted();
 
         // assert
         assertTrue(result);
     }
 
     @Test
     public void testIsDeletedFalse() {
         // setup
         Volume volume = new Volume();
 
         // act
         volume.setStatus(VolumeState.DELETING);
 
         // assert
         assertFalse(volume.isDeleted());
     }
 
     @Test
     public void testsetDeletedTrue() {
         // setup
         Volume volume = new Volume();
 
         // act
         volume.setDeleted(true);
 
         // assert
         assertTrue(volume.isDeleted());
         assertEquals(VolumeState.BURIED, volume.getStatus());
     }
 
     @Test
     public void testsetDeletedFalse() {
         // setup
         Volume volume = new Volume();
         volume.setStatus(VolumeState.CREATING);
 
         // act
         volume.setDeleted(false);
 
         // assert
         assertFalse(volume.isDeleted());
         assertEquals(VolumeState.CREATING, volume.getStatus());
     }
 }
