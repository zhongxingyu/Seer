 package com.seitenbau.testing.dbunit.rule;
 
 import static org.fest.assertions.Assertions.*;
 
 import org.dbunit.dataset.DefaultDataSet;
 import org.dbunit.dataset.IDataSet;
 import org.junit.Ignore;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 import com.seitenbau.testing.dbunit.extend.DbUnitDatasetFactory;
 
 class DataSet1 implements DbUnitDatasetFactory
 {
   public IDataSet createDBUnitDataSet()
   {
     return new DefaultDataSet();
   }
 }
 
 class DataSet2 extends DataSet1
 {
   public IDataSet createDBUnitDataSet()
   {
     return new DefaultDataSet();
   }
 }
 
@Ignore("TODO change to in memory DB")
 @DatabaseSetup(prepare = DataSet1.class)
 public class DatabaseTesterRuleIntegrationTest
 {
   @Rule
   public ExpectedException exception = ExpectedException.none();
 
   @Rule
  public DatabaseTesterRule db = new DatabaseTesterRule("TODO",
      "jdbc:TODO://192.168.0.85:3308/XY", "XY", "XY")
       .setDatabaseOperationFactory(new NoneFactory());
 
   @InjectDataSet
   DataSet1 ds;
 
   int invokedBeforeDS1 = 0;
 
   int invokedBeforeDS2 = 0;
 
   int invokedBeforeExp = 0;
 
   @DatabaseBefore
   public void beforeds1(DataSet1 ds)
   {
     invokedBeforeDS1++;
   }
 
   @DatabaseBefore
   public void beforeds1(DataSet2 ds)
   {
     invokedBeforeDS2++;
   }
 
   @DatabaseBefore(id = "explicit")
   public void beforeExplicit(DataSet1 ds)
   {
     invokedBeforeExp++;
   }
 
   @Test
   @DatabaseBefore
   public void testDatasetInjection()
   {
     assertThat(ds).isNotNull();
     assertThat(ds).isInstanceOf(DataSet1.class);
     assertThat(invokedBeforeDS1).as("ds1").isEqualTo(1);
     assertThat(invokedBeforeDS2).as("ds2").isEqualTo(0);
     assertThat(invokedBeforeExp).as("explicit").isEqualTo(0);
   }
 
   @Test
   @DatabasePrepare("explicit")
   public void testDatasetInjectionExplicit()
   {
     assertThat(ds).isNotNull();
     assertThat(ds).isInstanceOf(DataSet1.class);
     assertThat(invokedBeforeDS1).as("ds1").isEqualTo(0);
     assertThat(invokedBeforeDS2).as("ds2").isEqualTo(0);
     assertThat(invokedBeforeExp).as("explicit").isEqualTo(1);
   }
 
   @Test
   @Ignore("not testable, because exception happens in rule")
   @DatabaseBefore(id = "else")
   public void testDatasetInjectionExplicit2()
   {
     exception.expectMessage("Unable to find prepare Method with id = 'else'");
   }
 }
