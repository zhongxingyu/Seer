 package se.cygni.expenses.jdbi;
 
 import org.h2.jdbcx.JdbcConnectionPool;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.skife.jdbi.v2.DBI;
 import org.skife.jdbi.v2.Handle;
 import se.cygni.expenses.api.Event;
 import se.cygni.expenses.api.Expense;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 public class ExpensesRepositoryTest {
 
     private static DBI dbi;
     private static JdbcConnectionPool ds;
 
     private static Expense dinner = new Expense(3, "Dinner at O Learys", "Kalle", new Date(342432432584L), 670, 2);
     private static Expense taxi = new Expense(4, "Taxi home", "Filip", new Date(342432732584L), 345, 2);
 
 
     @BeforeClass
     public static void initDB() {
 
        ds = JdbcConnectionPool.create("jdbc:h2:mem:test3", "sa", "");
         dbi = new DBI(ds);
         createTables(dbi);
     }
 
     @AfterClass
     public static void closeDB() {
 
         ds.dispose();
     }
 
     @Before
     public void clearTables() {
 
         Handle handle = dbi.open();
         handle.execute("delete from " + ExpensesRepository.TABLE_NAME);
         handle.execute("delete from " + EventsRepository.TABLE_NAME);
         handle.close();
     }
 
     private void initBaseData() {
 
         insertEvent(dbi, new Event(1, "Cancun", new Date(0)));
         insertEvent(dbi, new Event(2, "New Delhi", new Date(1554)));
 
         insertExpense(dbi, dinner);
         insertExpense(dbi, taxi);
     }
 
     @Test
     public void shouldListExpensesForEvent() {
 
         //given
         initBaseData();
 
         ExpensesRepository target = dbi.open(ExpensesRepository.class);
 
         //when
         List<Expense> result = target.findExpensesForEventId(2);
 
         //then
         assertThat("result contains two elements", result.size(), is(2));
     }
 
     @Test
     public void shouldCreateNewExpense() {
 
         // given
         initBaseData();
 
         ExpensesRepository target = dbi.open(ExpensesRepository.class);
 
         target.add(new Expense(5, "Night cap", "Pelle", new Date(342432487584L), 225, 2));
 
         //when
         List<Expense> result = target.findExpensesForEventId(2);
 
         //then
         assertThat("result contains two elements", result.size(), is(3));
     }
 
     @Test
     public void shouldRetreiveOneExpense() {
 
         // given
         initBaseData();
 
         ExpensesRepository target = dbi.open(ExpensesRepository.class);
 
         // when
         Expense expense = target.findById(3);
 
         // then
         assertThat("Correct expense was fetched", expense.hashCode(), is(dinner.hashCode()));
     }
 
     private void insertEvent(DBI dbi, Event event) {
 
         Handle handle = dbi.open();
 
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
 
         handle.execute("insert into event " +
                 "(id, name, date) " +
                 "values " +
                 "(" + event.getId() + "," +
                 "'" + event.getName() + "'," +
                 "'" + sdf.format(event.getDate()) + "')");
     }
 
     private void insertExpense(DBI dbi, Expense expense) {
 
         Handle handle = dbi.open();
 
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
 
         handle.execute("insert into expense " +
                 "(id, description, person, date, amount, eventId) " +
                 "values " +
                 "(" + expense.getId() + "," +
                 "'" + expense.getDescription() + "'," +
                 "'" + expense.getPerson() + "'," +
                 "'" + sdf.format(expense.getDate()) + "', " +
                 expense.getAmount() + "," +
                 expense.getEventId() + "," +
                 ")");
 
         handle.close();
     }
 
     private static void createTables(DBI dbi) {
 
         Handle handle = dbi.open();
         handle.execute(EventsRepository.CREATE_TABLE_STATEMENT);
         handle.execute(ExpensesRepository.CREATE_TABLE_STATEMENT);
         handle.close();
     }
 
 }
