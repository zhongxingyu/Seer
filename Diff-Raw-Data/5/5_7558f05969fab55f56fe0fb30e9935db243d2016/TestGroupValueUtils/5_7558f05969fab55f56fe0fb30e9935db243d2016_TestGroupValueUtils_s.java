 package org.selfbus.sbtools.knxcom.application.value;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 import org.selfbus.sbtools.common.HexString;
 
 public class TestGroupValueUtils
 {
    @Test
    public void testIntToFromBytes()
    {
       byte[] raw;
 
       raw = GroupValueUtils.intToBytes(255, 1);
       assertEquals(255, GroupValueUtils.bytesToInt(raw, 0, 1));
 
       raw = GroupValueUtils.intToBytes(255, 1);
       assertEquals(255, GroupValueUtils.bytesToInt(raw, 0, 1));
 
       raw = GroupValueUtils.intToBytes(424242, 4);
       assertEquals(424242, GroupValueUtils.bytesToInt(raw, 0, 4));
    }
 
    @Test
    public void testFromBytes_ShortFloat()
    {
       Object o;
 
      o = GroupValueUtils.fromBytes(HexString.valueOf("0C FB"), DataPointType.SHORT_FLOAT);
       assertEquals(25.5f, (float) o, 0.001);
 
      o = GroupValueUtils.fromBytes(HexString.valueOf("03 7C"), DataPointType.SHORT_FLOAT);
       assertEquals(8.92f, (float) o, 0.001);
    }
 
    @Test
    public void testToBytes_ShortFloat()
    {
       assertArrayEquals(HexString.valueOf("00 00"), GroupValueUtils.toBytes(0f, DataPointType.SHORT_FLOAT));
       assertArrayEquals(HexString.valueOf("00 01"), GroupValueUtils.toBytes(0.01f, DataPointType.SHORT_FLOAT));
 
       // might be wrong
       assertArrayEquals(HexString.valueOf("87 FF"), GroupValueUtils.toBytes(-0.01f, DataPointType.SHORT_FLOAT));
 
       assertArrayEquals(HexString.valueOf("00 64"), GroupValueUtils.toBytes(1f, DataPointType.SHORT_FLOAT));
       assertArrayEquals(HexString.valueOf("0C FB"), GroupValueUtils.toBytes(25.5f, DataPointType.SHORT_FLOAT));
    }
 }
