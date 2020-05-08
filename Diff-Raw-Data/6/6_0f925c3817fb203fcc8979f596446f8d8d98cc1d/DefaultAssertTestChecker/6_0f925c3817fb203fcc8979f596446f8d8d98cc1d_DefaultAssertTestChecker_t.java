 package au.net.netstorm.boost.retire.reflect;
 
 import junit.framework.Assert;
 
 public final class DefaultAssertTestChecker implements AssertTestChecker {
 
     // SUGGEST Refactor duplication, if possible.
     public void checkEquals(Object[] o1, Object[] o2) {
         basicCheck(o1, o2);
         for (int i = 0; i < o2.length; i++) Assert.assertEquals("" + i, o1[i], o2[i]);
     }
 
     public void checkBagEquals(Object[] o1, Object[] o2) {
         basicCheck(o1, o2);
         boolean[] used = usedFlags(o1);
        for (int i = 0; i < o2.length; i++) {
            Object ref = o2[i];
            if (!find(used, o1, ref)) Assert.fail("Not in bag " + ref);
         }
     }
 
     public void checkEquals(byte[] value1, byte[] value2) {
         Assert.assertNotNull(value1);
         Assert.assertNotNull(value2);
         Assert.assertEquals(value1.length, value2.length);
         for (int i = 0; i < value2.length; i++) Assert.assertEquals("" + i, value1[i], value2[i]);
     }
 
     public void checkEquals(int[] value1, int[] value2) {
         Assert.assertEquals(value1.length, value2.length);
         for (int i = 0; i < value1.length; i++) Assert.assertEquals(value1[i], value2[i]);
     }
 
     public void checkNotEquals(byte[] value1, byte[] value2) {
         if (value1.length != value2.length) return;
         for (int i = 0; i < value1.length; i++)
             if (value1[i] != value2[i]) return;
         Assert.fail();
     }
 
     private boolean find(boolean[] used, Object[] array, Object toFind) {
         for (int i = 0; i < array.length; i++) {
             if (used[i]) continue;
             if (toFind.equals(array[i])) return true;
         }
         return false;
     }
 
     private boolean[] usedFlags(Object[] o1) {
         boolean[] used = new boolean[o1.length];
         for (int i = 0; i < used.length; i++) used[i] = false;
         return used;
     }
 
     private void basicCheck(Object[] o1, Object[] o2) {
         Assert.assertNotNull(o1);
         Assert.assertNotNull(o2);
         Assert.assertEquals("Array lengths do not match.", o1.length, o2.length);
     }
 }
