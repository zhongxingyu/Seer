 // ========================================================================
 // Copyright (C) zeroth Project Team. All rights reserved.
 // GNU AFFERO GENERAL PUBLIC LICENSE Version 3, 19 November 2007
 // http://www.gnu.org/licenses/agpl-3.0.txt
 // ========================================================================
 package zeroth.framework.enterprise.domain;
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 import java.util.Date;
 import org.junit.Before;
 import org.junit.Test;
 /**
  * {@link AbstractRevisedObject} のユニットテスト
  * @author nilcy
  */
@SuppressWarnings("all")
 public final class AbstractRevisedObjectTest {
     private TestRevisedObject testee;
     private static final Date TODAY = new Date();
     @Before
     public void before() {
         this.testee = new TestRevisedObject();
     }
     @Test
     public void testAbstractRevisedObject() {
         assertThat(new AbstractRevisedObject() {
         }, is(not(nullValue())));
     }
     @Test
     public void testGetSetCreated() {
         assertThat(this.testee.getCreated(), is(nullValue()));
         this.testee.setCreated(TODAY);
         assertThat(this.testee.getCreated(), is(TODAY));
     }
     @Test
     public void testGetSetUpdated() {
         assertThat(this.testee.getUpdated(), is(nullValue()));
         this.testee.setUpdated(TODAY);
         assertThat(this.testee.getUpdated(), is(TODAY));
     }
     @Test
     public void testSameIdentityAs() {
         assertThat(this.testee.sameIdentityAs(null), is(false));
         final TestRevisedObject other = new TestRevisedObject();
         assertThat(this.testee.sameIdentityAs(other), is(true));
         this.testee.setId(0L);
         other.setId(0L);
         assertThat(this.testee.sameIdentityAs(other), is(true));
         this.testee.setId(0L);
         other.setId(1L);
         assertThat(this.testee.sameIdentityAs(other), is(false));
     }
 }
