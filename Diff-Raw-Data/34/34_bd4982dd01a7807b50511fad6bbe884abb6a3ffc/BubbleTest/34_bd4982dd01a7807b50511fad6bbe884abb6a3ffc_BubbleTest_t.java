 package com.jrock.bulkwait.domain;
 
 import com.jrock.bulkwait.BaseTest;
 import com.jrock.bulkwait.service.BubbleService;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
 import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;
 
 import static org.hamcrest.Matchers.*;
 
 
 import static org.junit.Assert.assertThat;
 
 /**
  * @author berinle
  */
 public class BubbleTest extends BaseTest {
 
	@javax.inject.Inject
	org.hibernate.SessionFactory sessionFactory;
     
     @Test
     @SuppressWarnings({"unchecked"})
     public void test_db_insert() throws Exception {
         Bubble bubble = new Bubble();
         bubble.setName("bubble");
 
         BubbleService service = ctx.getBean(BubbleService.class);
 
         Long id = service.add(bubble);
         System.out.println(id);
         assertThat(id, is(allOf(notNullValue(), equalTo(50l))));
     }
 
     @Test
     @SuppressWarnings({"unchecked"})
     public void test_batch_insert() throws Exception {
         long count = 50;
        for(int i=0; i<150; i++){
             Bubble bubble = new Bubble();
             String name = "b"+i;
             bubble.setName(name);
             BubbleService service = ctx.getBean(BubbleService.class);
             service.add(bubble);
             
             assertThat(bubble.getId(), is(allOf(notNullValue(), equalTo(count++))));
            assertThat(bubble, hasProperty("name", equalTo(name)));			
         }

        SessionFactory sessionFactory = ctx.getBean(SessionFactory.class);
        assertThat(sessionFactory, notNullValue());
        Session s = sessionFactory.openSession();
        Long nextVal = (Long)s.createSQLQuery("select bubble_seq.nextval nextVal from dual")
                .addScalar("nextVal", Hibernate.LONG)
                .uniqueResult();
        s.close();
		assertThat(nextVal, is(4l));
     }
 }
