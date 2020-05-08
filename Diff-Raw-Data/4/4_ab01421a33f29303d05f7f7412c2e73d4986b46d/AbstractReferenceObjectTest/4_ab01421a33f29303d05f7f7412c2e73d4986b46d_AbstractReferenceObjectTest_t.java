 // ========================================================================
 // Copyright (C) zeroth Project Team. All rights reserved.
 // GNU AFFERO GENERAL PUBLIC LICENSE Version 3, 19 November 2007
 // http://www.gnu.org/licenses/agpl-3.0.txt
 // ========================================================================
 package zeroth.framework.enterprise.domain;
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 /**
  * {@link AbstractReferenceObject} のユニットテスト
  * @author nilcy
  */
 @SuppressWarnings("all")
 public final class AbstractReferenceObjectTest {
     private AbstractReferenceObject testee;
     @Before
     public void before() {
         this.testee = new AbstractReferenceObject() {
         };
     }
     @Test
     public void testAbstractReferenceObject() {
         assertThat(this.testee, is(not(nullValue())));
     }
     @Test
     public void testGetSetId() {
         assertThat(this.testee.getId(), is(nullValue()));
        this.testee.setId(0L);
        assertThat(this.testee.getId(), is(0L));
     }
     @Test
     public void testSameIdentityAs() {
         assertThat(this.testee.sameIdentityAs(null), is(false));
         final AbstractReferenceObject other = new AbstractReferenceObject() {
         };
         assertThat(this.testee.sameIdentityAs(other), is(true));
         this.testee.setId(0L);
         other.setId(0L);
         assertThat(this.testee.sameIdentityAs(other), is(true));
         this.testee.setId(0L);
         other.setId(1L);
         assertThat(this.testee.sameIdentityAs(other), is(false));
     }
     @Test
     public void testIdentity() {
         assertThat(this.testee.identity(), is(nullValue()));
         this.testee.setId(0L);
         assertThat(this.testee.identity(), is(0L));
     }
 }
