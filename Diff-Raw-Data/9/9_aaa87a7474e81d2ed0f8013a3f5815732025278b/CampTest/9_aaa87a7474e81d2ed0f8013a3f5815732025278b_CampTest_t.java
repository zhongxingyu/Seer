 package com.github.croesch.partimana.types;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import java.util.Date;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.github.croesch.partimana.types.exceptions.RequiredFieldSetToNullException;
 
 /**
  * Provides test methods for {@link Camp}
  * 
  * @author croesch
  * @since Date: Jun 18, 2011
  */
 public class CampTest {
 
   /** object under test */
   private Camp camp;
 
   @Before
   public final void setUp() {
     final int untilDate = 100000;
     this.camp = new Camp("Testcamp", new Date(0), new Date(untilDate), "here", "100");
   }
 
   @Test
   public final void testCamp() {
     final int untilDate = 100000;
     assertThat(this.camp.getName()).isEqualTo("Testcamp");
     assertThat(this.camp.getFromDate()).isEqualTo(new Date(0));
     assertThat(this.camp.getUntilDate()).isEqualTo(new Date(untilDate));
     assertThat(this.camp.getLocation()).isEqualTo("here");
     assertThat(this.camp.getRatePerParticipant()).isEqualTo("100");
   }
 
   @Test(expected = RequiredFieldSetToNullException.class)
   public final void testSetAndGetNameRFSTNE() {
     this.camp.setName(null);
   }
 
   @Test
   public final void testSetAndGetName() {
     this.camp.setName("name");
     assertThat(this.camp.getName()).isEqualTo("name");
 
     this.camp.setName("...");
     assertThat(this.camp.getName()).isEqualTo("...");
 
     this.camp.setName("");
     assertThat(this.camp.getName()).isEqualTo("");
   }
 
   @Test(expected = RequiredFieldSetToNullException.class)
   public final void testSetAndGetFromDateRFSTNE() {
     this.camp.setFromDate(null);
   }
 
   @Test
   public final void testSetAndGetFromDate() {
     final Date d1 = new Date();
     this.camp.setFromDate(d1);
     assertThat(this.camp.getFromDate()).isEqualTo(d1);
 
     final Date d2 = new Date(17);
     this.camp.setFromDate(d2);
     assertThat(this.camp.getFromDate()).isEqualTo(d2);
 
     Date d3 = new Date(99);
     this.camp.setFromDate(d3);
     assertThat(this.camp.getFromDate()).isEqualTo(d3);
 
     d3.setTime(98);
     assertThat(this.camp.getFromDate()).isNotEqualTo(d3);
 
     d3 = this.camp.getFromDate();
     d3.setTime(98);
     assertThat(this.camp.getFromDate()).isNotEqualTo(d3);
   }
 
   @Test(expected = RequiredFieldSetToNullException.class)
   public final void testSetAndGetUntilDateRFSTNE() {
     this.camp.setUntilDate(null);
   }
 
   @Test
   public final void testSetAndGetUntilDate() {
     final Date d1 = new Date();
     this.camp.setUntilDate(d1);
     assertThat(this.camp.getUntilDate()).isEqualTo(d1);
 
     final Date d2 = new Date(17);
     this.camp.setUntilDate(d2);
     assertThat(this.camp.getUntilDate()).isEqualTo(d2);
 
     Date d3 = new Date(99);
     this.camp.setUntilDate(d3);
     assertThat(this.camp.getUntilDate()).isEqualTo(d3);
 
     d3.setTime(98);
     assertThat(this.camp.getUntilDate()).isNotEqualTo(d3);
 
     d3 = this.camp.getUntilDate();
     d3.setTime(98);
     assertThat(this.camp.getUntilDate()).isNotEqualTo(d3);
   }
 
   @Test(expected = RequiredFieldSetToNullException.class)
   public final void testSetAndGetLocationRFSTNE() {
     this.camp.setLocation(null);
   }
 
   @Test
   public final void testSetAndGetLocation() {
     this.camp.setLocation("loc");
     assertThat(this.camp.getLocation()).isEqualTo("loc");
 
     this.camp.setLocation("...");
     assertThat(this.camp.getLocation()).isEqualTo("...");
 
     this.camp.setLocation("");
     assertThat(this.camp.getLocation()).isEqualTo("");
   }
 
   @Test(expected = RequiredFieldSetToNullException.class)
   public final void testSetAndGetRatePerParticipantRFSTNE() {
     this.camp.setRatePerParticipant(null);
   }
 
   @Test
   public final void testSetAndGetRatePerParticipant() {
     this.camp.setRatePerParticipant("12");
     assertThat(this.camp.getRatePerParticipant()).isEqualTo("12");
 
     this.camp.setRatePerParticipant("...");
     assertThat(this.camp.getRatePerParticipant()).isEqualTo("...");
 
     this.camp.setRatePerParticipant("");
     assertThat(this.camp.getRatePerParticipant()).isEqualTo("");
   }
 
   @Test
   public final void testSetAndGetRatePerDayChildren() {
     this.camp.setRatePerDayChildren("12");
     assertThat(this.camp.getRatePerDayChildren()).isEqualTo("12");
 
     this.camp.setRatePerDayChildren("...");
     assertThat(this.camp.getRatePerDayChildren()).isEqualTo("...");
 
     this.camp.setRatePerDayChildren("");
     assertThat(this.camp.getRatePerDayChildren()).isEqualTo("");
   }
 
   @Test
   public final void testGetId() {
     final long id = this.camp.getId();
     this.camp = new Camp("n", new Date(), new Date(), "w", "r");
 
     assertThat(this.camp.getId()).isEqualTo(id + 1);
 
     this.camp = new Camp(id + 2, "n", new Date(), new Date(), "w", "r");
     assertThat(this.camp.getId()).isEqualTo(id + 2);
   }
 
   @Test(expected = IllegalArgumentException.class)
   public final void testGetIdIAE1() {
     final long id = this.camp.getId();
     this.camp = new Camp(id, "n", new Date(), new Date(), "w", "r");
   }
 
   @Test(expected = IllegalArgumentException.class)
   public final void testGetIdIAE2() {
     final long id = this.camp.getId();
     this.camp = new Camp(id - 1, "n", new Date(), new Date(), "w", "r");
   }
 
   @Test(expected = IllegalArgumentException.class)
   public final void testGetIdIAE3() {
     this.camp = new Camp(-1, "n", new Date(), new Date(), "w", "r");
   }
 
   @Test
   public final void testToString() {
     assertThat(this.camp.toString()).contains(String.valueOf(this.camp.getId()));
   }
 
   @Test(expected = RequiredFieldSetToNullException.class)
   public final void testCampCampRFSTNE() {
     new Camp(null);
   }
 
   @Test
   public final void testCampCamp() {
     this.camp.setRatePerDayChildren("children-rate");
 
     final Camp c = new Camp(this.camp);
     assertThat(c).isEqualTo(this.camp);
 
     c.setRatePerDayChildren("day-children-rate");
     assertThat(c).isNotEqualTo(this.camp);
   }
 
   @Test
   public final void testCampEquals() {
 
     this.camp.setRatePerDayChildren("children-rate");
 
     Camp c = new Camp(this.camp);
     assertThat(c).isEqualTo(this.camp);
     assertThat(this.camp).isEqualTo(this.camp);
     assertThat(c).isEqualTo(c);
     assertThat(c).isNotEqualTo(null);
     assertThat(c).isNotEqualTo("this.camp");
 
     c.setFromDate(new Date(987));
     assertThat(c).isNotEqualTo(this.camp);
 
     c = new Camp(c.getId() + 10,
                  this.camp.getName(),
                  this.camp.getFromDate(),
                  this.camp.getUntilDate(),
                  this.camp.getLocation(),
                  this.camp.getRatePerParticipant());
     c.setRatePerDayChildren(this.camp.getRatePerDayChildren());
     assertThat(c).isNotEqualTo(this.camp);
     c = new Camp(this.camp);
 
     assertThat(c).isEqualTo(this.camp);
     c.setLocation("nowhere");
     assertThat(c).isNotEqualTo(this.camp);
     c.setLocation(this.camp.getLocation());
 
     assertThat(c).isEqualTo(this.camp);
     c.setName("no-name");
     assertThat(c).isNotEqualTo(this.camp);
     c.setName(this.camp.getName());
 
     assertThat(c).isEqualTo(this.camp);
     c.setRatePerDayChildren(null);
     assertThat(c).isNotEqualTo(this.camp);
     c.setRatePerDayChildren(this.camp.getRatePerDayChildren());
 
     assertThat(c).isEqualTo(this.camp);
     c.setRatePerParticipant("no rate");
     assertThat(c).isNotEqualTo(this.camp);
     c.setRatePerParticipant(this.camp.getRatePerParticipant());
 
     assertThat(c).isEqualTo(this.camp);
     c.setUntilDate(new Date(987));
     assertThat(c).isNotEqualTo(this.camp);
 
   }
 
   @Test
   public final void testCampHashCode() {
 
     this.camp.setRatePerDayChildren("children-rate");
 
     Camp c = new Camp(this.camp);
     assertThat(c.hashCode()).isEqualTo(this.camp.hashCode());
     assertThat(this.camp.hashCode()).isEqualTo(this.camp.hashCode());
     assertThat(c.hashCode()).isEqualTo(c.hashCode());
     assertThat(c.hashCode()).isNotNull();
 
     c.setFromDate(new Date(987));
     assertThat(c.hashCode()).isNotEqualTo(this.camp.hashCode());
 
     c = new Camp(c.getId() + 10,
                  this.camp.getName(),
                  this.camp.getFromDate(),
                  this.camp.getUntilDate(),
                  this.camp.getLocation(),
                  this.camp.getRatePerParticipant());
     c.setRatePerDayChildren(this.camp.getRatePerDayChildren());
     assertThat(c.hashCode()).isNotEqualTo(this.camp.hashCode());
     c = new Camp(this.camp);
 
     assertThat(c.hashCode()).isEqualTo(this.camp.hashCode());
     c.setLocation("nowhere");
     assertThat(c.hashCode()).isNotEqualTo(this.camp.hashCode());
     c.setLocation(this.camp.getLocation());
 
     assertThat(c.hashCode()).isEqualTo(this.camp.hashCode());
     c.setName("no-name");
     assertThat(c.hashCode()).isNotEqualTo(this.camp.hashCode());
     c.setName(this.camp.getName());
 
     assertThat(c.hashCode()).isEqualTo(this.camp.hashCode());
     c.setRatePerDayChildren(null);
     assertThat(c.hashCode()).isNotEqualTo(this.camp.hashCode());
     c.setRatePerDayChildren(this.camp.getRatePerDayChildren());
 
     assertThat(c.hashCode()).isEqualTo(this.camp.hashCode());
     c.setRatePerParticipant("no rate");
     assertThat(c.hashCode()).isNotEqualTo(this.camp.hashCode());
     c.setRatePerParticipant(this.camp.getRatePerParticipant());
 
     assertThat(c.hashCode()).isEqualTo(this.camp.hashCode());
     c.setUntilDate(new Date(987));
     assertThat(c.hashCode()).isNotEqualTo(this.camp.hashCode());
   }
 
   @Test
   public void testGetParticipants() {
     assertThat(this.camp.getParticipants()).isEmpty();
 
     final Participant participant = new Participant("Mustermann",
                                                     "Max",
                                                     Gender.MALE,
                                                     Denomination.OTHER,
                                                     new Date(),
                                                     "Musterstrasse 12",
                                                     98667,
                                                     "Musterhausen",
                                                     CountyCouncil.OTHER);
     this.camp.getParticipants().add(new CampParticipant(participant));
     assertThat(this.camp.getParticipants()).isEmpty();
 
     this.camp.addParticipant(participant);
     assertThat(this.camp.getParticipants()).containsExactly(new CampParticipant(participant));
   }
 
   @Test
   public void testAddAndRemoveParticipant() {
     final Participant participant = new Participant("Mustermann",
                                                     "Max",
                                                     Gender.MALE,
                                                     Denomination.OTHER,
                                                     new Date(),
                                                     "Musterstrasse 12",
                                                     98667,
                                                     "Musterhausen",
                                                     CountyCouncil.OTHER);
     final Participant participant2 = new Participant("Mustermann",
                                                      "Max",
                                                      Gender.MALE,
                                                      Denomination.OTHER,
                                                      new Date(),
                                                      "Musterstrasse 12",
                                                      98667,
                                                      "Musterhausen",
                                                      CountyCouncil.OTHER);
 
     this.camp.addParticipant(participant2);
     assertThat(this.camp.getParticipants()).containsExactly(new CampParticipant(participant2));
 
     this.camp.addParticipant(participant);
    assertThat(this.camp.getParticipants()).containsExactly(new CampParticipant(participant2),
                                                            new CampParticipant(participant));
 
     participant.setBank("blub");
    assertThat(this.camp.getParticipants()).containsExactly(new CampParticipant(participant2),
                                                            new CampParticipant(participant));
     this.camp.removeParticipant(participant2);
     assertThat(this.camp.getParticipants()).containsExactly(new CampParticipant(participant));
 
     this.camp.addParticipant(null);
     assertThat(this.camp.getParticipants()).containsExactly(new CampParticipant(participant));
 
     this.camp.removeParticipant(null);
     assertThat(this.camp.getParticipants()).containsExactly(new CampParticipant(participant));
 
     this.camp.removeParticipant(participant2);
     assertThat(this.camp.getParticipants()).containsExactly(new CampParticipant(participant));
 
     this.camp.removeParticipant(participant);
     assertThat(this.camp.getParticipants()).isEmpty();
   }
 }
