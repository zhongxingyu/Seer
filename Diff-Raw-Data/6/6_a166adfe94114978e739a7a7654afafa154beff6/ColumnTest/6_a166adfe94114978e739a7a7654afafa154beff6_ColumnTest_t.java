 package com.aneeshpu.dpdeppop.schema;
 
 import com.aneeshpu.dpdeppop.datatypes.DataType;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import java.sql.Connection;
 import java.util.HashMap;
 import java.util.Map;
 
 import static org.hamcrest.core.Is.is;
 import static org.hamcrest.core.IsEqual.equalTo;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.*;
 
 @RunWith(MockitoJUnitRunner.class)
 public class ColumnTest {
 
     private Connection connection = new ConnectionFactory().getConnection();
 
     @Mock
     private Record record;
 
 
     @Test
     public void considers_two_columns_from_the_same_table_with_same_names_are_equal() {
 
         final Column idColumn = Column.buildColumn().withName("id").withTable(new RecordBuilder().setName("payment").withPreassignedValues(new HashMap<String, Map<String, Object>>()).setColumnCreationStrategy(new AutoIncrementBasedCreation()).createRecord()).create();
         final Column anotherIdColumn = Column.buildColumn().withName("id").withTable(new RecordBuilder().setName("payment").withPreassignedValues(new HashMap<String, Map<String, Object>>()).setColumnCreationStrategy(new AutoIncrementBasedCreation()).createRecord()).create();
 
         assertThat(idColumn, is(equalTo(anotherIdColumn)));
     }
 
     @Test
     public void prints_to_string_with_column_name() {
         assertThat(Column.buildColumn().withName("id").create().toString(), is(equalTo("id")));
     }
 
     @Test
     public void generates_a_name_value_pair_to_be_inserted_into_sql_query() {
        when(record.tableName()).thenReturn("payment");
 
         final DataType<Float> dataType = mock(DataType.class);
         when(dataType.generateDefaultValue()).thenReturn(10f);
 
         final Column amount = Column.buildColumn().withName("amount").withDataType(dataType).withTable(record).create();
         final NameValue nameValue = amount.nameValue(new java.util.HashMap<String, java.util.Map<String, Object>>());
 
         assertThat(nameValue, is(equalTo(new NameValue("amount", 10f))));
 
         verify(dataType).generateDefaultValue();
     }
 
     @Test
     public void a_foreign_key_column_uses_the_default_value_generated_by_the_referenced_column() {
        when(record.tableName()).thenReturn("payment");
 
         final DataType<Float> dataType = mock(DataType.class);
         when(dataType.generateDefaultValue()).thenReturn(321f);
 
         final Column idColumnInInvoiceTable = mock(Column.class);
         final int primaryKeyInInvoiceTable = 123;
 
         final HashMap<String, Map<String, Object>> preassignedValues = new HashMap<String, Map<String, Object>>();
         when(idColumnInInvoiceTable.nameValue(preassignedValues)).thenReturn(new NameValue("id", primaryKeyInInvoiceTable));
 
         final Column invoiceIdColumn = Column.buildColumn().withName("invoice_id").withReferencingColumn(idColumnInInvoiceTable).withDataType(dataType).withTable(record).create();
         assertThat(invoiceIdColumn.nameValue(preassignedValues), is(equalTo(new NameValue("invoice_id", primaryKeyInInvoiceTable))));
 
         verify(dataType, never()).generateDefaultValue();
         verify(idColumnInInvoiceTable).nameValue(preassignedValues);
     }
 
     @Test
     public void creates_a_formatted_query_string() {
 
        when(record.tableName()).thenReturn("payment");

         final DataType<String> dataType = mock(DataType.class);
         when(dataType.generateDefaultValue()).thenReturn("'c'");
 
         final Column amount = Column.buildColumn().withName("amount").withDataType(dataType).withTable(record).create();
         final NameValue nameValue = amount.nameValue(new java.util.HashMap<String, java.util.Map<String, Object>>());
 
         assertThat(nameValue.formattedValue(), is(equalTo("'c',")));
 
         verify(dataType).generateDefaultValue();
 
     }
 
     @Test
     public void uses_a_preassigned_value_if_passed_in() {
         final DataType<Float> dataType = mock(DataType.class);
         when(dataType.generateDefaultValue()).thenReturn(10f);
 
         final Record record = mock(Record.class);
         when(record.tableName()).thenReturn("payment");
 
         final Column status = Column.buildColumn().withTable(record).withName("status").withDataType(dataType).create();
         final Map<String, Map<String, Object>> preassignedValues = new HashMap<String, Map<String, Object>>();
         final Map<String, Object> values = new HashMap<String, Object>();
         values.put("status", 2);
 
         preassignedValues.put("payment", values);
 
         final NameValue nameValue = status.nameValue(preassignedValues);
 
         assertThat(nameValue, is(equalTo(new NameValue("status", 2))));
 
         verify(record, atLeastOnce()).tableName();
         verify(dataType, never()).generateDefaultValue();
 
     }
 
     @Test
     public void a_column_with_nameValue_set_is_considered_assigned(){
 
         final DataType<Float> dataType = mock(DataType.class);
 
         final Record record = mock(Record.class);
         when(record.tableName()).thenReturn("payment");
 
         final Column status = Column.buildColumn().withTable(record).withName("status").withDataType(dataType).create();
         final Map<String, Map<String, Object>> preassignedValues = new HashMap<String, Map<String, Object>>();
         preassignedValues.put("payment", new HashMap<String, Object>(){{
             put("status", 1);
         }});
 
         status.nameValue(preassignedValues);
 
         assertTrue(status.isAssigned());
 
         verify(record, atLeastOnce()).tableName();
     }
 
     @Test
     public void a_column_with_null_for_nameValue_is_unassigned(){
         final DataType<Float> dataType = mock(DataType.class);
 
         final Record record = mock(Record.class);
 
         final Column status = Column.buildColumn().withTable(record).withName("status").withDataType(dataType).create();
 
         assertFalse(status.isAssigned());
     }
 }
