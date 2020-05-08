 package org.cognitor.server.hibernate.type;
 
 import junit.framework.Assert;
 import org.hibernate.HibernateException;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Types;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertFalse;
 import static junit.framework.Assert.assertTrue;
 import static org.mockito.Mockito.*;
 
 /**
  * @author Patrick Kranz
  */
 @RunWith(MockitoJUnitRunner.class)
 public class StringLongTypeTest {
     private StringLongType stringLongType;
 
     @Before
     public void setUp() {
         stringLongType = new StringLongType();
     }
 
     @Test
     public void shouldMapStringToBigInt() {
         assertEquals(1, stringLongType.sqlTypes().length);
         assertEquals(Types.BIGINT, stringLongType.sqlTypes()[0]);
     }
 
     @Test
     public void shouldMapBigIntToString() {
         assertEquals(String.class, stringLongType.returnedClass());
     }
 
     @Test
     public void shouldReturnNotEqualWhenBothArgumentsNullGiven() {
         assertFalse(stringLongType.equals(null, null));
     }
 
     @Test
     public void shouldReturnNotEqualWhenOnlyFirstArgumentNullGiven() {
         assertFalse(stringLongType.equals(null, new Object()));
     }
 
     @Test
     public void shouldReturnNotEqualWhenOnlySecondArgumentNullGiven() {
         assertFalse(stringLongType.equals(new Object(), null));
     }
 
     @Test
     public void shouldReturnEqualWhenBothArgumentsEqualGiven() {
         assertTrue(stringLongType.equals("3", "3"));
     }
 
     @Test
     public void shouldReturnNotEqualWhenDifferentArgumentTypesGiven() {
         assertFalse(stringLongType.equals("3", 3L));
     }
 
     @Test(expected = HibernateException.class)
     public void shouldThrowExceptionWhenNullForHashCodeGiven() {
         stringLongType.hashCode(null);
     }
 
     @Test
     public void shouldReturnHashCodeOfArgumentWhenArgumentForHashCodeGiven() {
         int hash = "3".hashCode();
         assertEquals(hash, stringLongType.hashCode("3"));
     }
 
     @Test
     public void shouldReturnStringWhenResultSetGiven() throws Exception {
         ResultSet resultSet = mock(ResultSet.class);
         when(resultSet.getLong("name")).thenReturn(1L);
         Object result = stringLongType.nullSafeGet(resultSet, new String[]{"name"}, null, null);
         assertTrue(result instanceof String);
         assertEquals("1", (String) result);
     }
 
     @Test
     public void shouldSetLongValueOnStatementWhenLongStringGiven() throws Exception {
         PreparedStatement statement = mock(PreparedStatement.class);
         stringLongType.nullSafeSet(statement, "3", 0, null);
         verify(statement, atLeastOnce()).setLong(0, 3L);
     }
 
     @Test(expected = HibernateException.class)
     public void shouldThrowExceptionWhenNonParsableStringValueGiven() throws Exception {
         stringLongType.nullSafeSet(mock(PreparedStatement.class), "bla", 0, null);
     }
 
     @Test(expected = HibernateException.class)
     public void shouldThrowExceptionWhenNullValueGiven() throws Exception {
         stringLongType.nullSafeSet(mock(PreparedStatement.class), null, 0, null);
     }
 
     @Test
     public void shouldReturnValueWhenValueForDeepCopyGiven() {
         String value = "3";
         assertEquals(value, stringLongType.deepCopy(value));
     }
 
     @Test
     public void shouldNotBeMutable() {
         assertFalse(stringLongType.isMutable());
     }
 
     @Test
    public void shouldReturnValueWhenDisassambleCalled() {
         String value = "3";
         assertEquals(value, stringLongType.disassemble(value));
     }
 
     @Test
    public void shouldReturnValueWhenAssambleCalled() {
         String value = "3";
         assertEquals(value, stringLongType.assemble(value, null));
     }
 
     @Test
     public void shouldReturnDetachedValueWhenReplaceWhenDetachedAndCachedValueGiven() {
         assertEquals("3", stringLongType.replace("3", "4", null));
     }
 }
