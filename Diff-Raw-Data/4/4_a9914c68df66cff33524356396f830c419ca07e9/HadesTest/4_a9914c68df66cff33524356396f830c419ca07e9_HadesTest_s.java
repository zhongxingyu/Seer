 package com.freeroom.persistence.proxy;
 
 import com.freeroom.persistence.beans.Book;
 import com.freeroom.persistence.beans.Order;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import java.sql.SQLException;
 import java.util.List;
 
 import static com.freeroom.persistence.DBFixture.getDbProperties;
 import static com.freeroom.persistence.DBFixture.prepareDB;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 public class HadesTest
 {
     private static Hades hades;
 
     @BeforeClass
     public static void setUp() throws SQLException
     {
         prepareDB();
         hades = new Hades(getDbProperties());
     }
 
     @Test
     public void should_get_simple_record()
     {
         final Book book = (Book)hades.create(Book.class, 1L);
 
         assertThat(book.getIsbn(), is(123L));
         assertThat(book.getName(), is("JBoss Seam"));
         assertThat(book.getPrice(), is(18.39));
         assertThat(book.getPublishDate(), is(1234567890L));
         assertThat(book.getTags().get(0), is(101L));
         assertThat(book.getTags().get(1), is(102L));
     }
 
     @Test
     public void should_know_simple_record_has_been_changed()
     {
         final Book book = (Book)hades.create(Book.class, 1L);
         book.setName("Learning Node");
 
         assertThat(hades.isDirty(book), is(true));
     }
 
     @Test
     public void should_load_records()
     {
        final List<Object> orders = hades.createList(Order.class, "SELECT id FROM order WHERE book_id=?", 1L);
         assertThat(orders.size(), is(1));
         assertThat(((Order)orders.get(0)).getAmount(), is(8));
         assertThat(((Order)orders.get(0)).getMemo(), is("Deliver at work time"));
     }
 }
