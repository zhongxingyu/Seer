 package com.github.croesch.partimana.view;
 
 import static com.github.croesch.partimana.view.CampParticipantListViewGUITest.requireParticipant;
 import static com.github.croesch.partimana.view.ParticipantListViewGUITest.requireParticipant;
 import static org.fest.assertions.Assertions.assertThat;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import org.fest.swing.core.MouseClickInfo;
 import org.fest.swing.data.TableCell;
 import org.fest.swing.edt.GuiActionRunner;
 import org.fest.swing.edt.GuiQuery;
 import org.fest.swing.edt.GuiTask;
 import org.fest.swing.fixture.Containers;
 import org.fest.swing.fixture.FrameFixture;
 import org.fest.swing.fixture.JLabelFixture;
 import org.fest.swing.fixture.JPanelFixture;
 import org.fest.swing.fixture.JTextComponentFixture;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.github.croesch.partimana.PartiManaDefaultGUITestCase;
 import com.github.croesch.partimana.types.Camp;
 import com.github.croesch.partimana.types.CampParticipant;
 import com.github.croesch.partimana.types.CountyCouncil;
 import com.github.croesch.partimana.types.Denomination;
 import com.github.croesch.partimana.types.Gender;
 import com.github.croesch.partimana.types.Participant;
 import com.github.croesch.partimana.view.api.ICampEditView;
 
 /**
  * Provides gui tests for {@link CampEditView}
  * 
  * @author croesch
  * @since Date: Sep 13, 2012
  */
 public class CampEditViewGUITest extends PartiManaDefaultGUITestCase {
 
   private ICampEditView editView;
 
   private JPanelFixture testView;
 
   private Camp camp;
 
   private FrameFixture window;
 
   private Participant participant;
 
   @BeforeClass
   public static void first() {
     Locale.setDefault(Locale.GERMAN);
   }
 
   @Override
   protected void before() {
     final CampEditView view = GuiActionRunner.execute(new GuiQuery<CampEditView>() {
       @Override
       protected CampEditView executeInEDT() throws Throwable {
         return new CampEditView();
       }
     });
 
     this.camp = new Camp("OFZ", new Date(500000), new Date(1000000), "Raversbeuren", "1,50€");
     this.camp.setRatePerDayChildren("0,5€");
 
     this.participant = new Participant("Schmidt",
                                        "Hans",
                                        Gender.MALE,
                                        Denomination.NONE,
                                        new Date(1200),
                                        "Strasse 4",
                                        56789,
                                        "Stadt",
                                        CountyCouncil.CITY_NEUSTADT);
     this.camp.addParticipant(new CampParticipant(this.participant));
 
     GuiActionRunner.execute(new GuiTask() {
 
       @Override
       protected void executeInEDT() throws Throwable {
         view.setCamp(CampEditViewGUITest.this.camp);
       }
     });
     view.setName("editView");
     this.editView = view;
 
     this.window = new FrameFixture(robot(), Containers.frameFor(view));
     this.window.show();
     this.testView = this.window.panel("editView");
   }
 
   @Override
   protected void after() {
     this.window.cleanUp();
     cleanUp();
   }
 
   @Test
   public void testClear() {
     clear();
     this.testView.textBox("nameTF").requireEmpty();
     this.testView.textBox("locationTF").requireEmpty();
     this.testView.textBox("fromTF").requireEmpty();
     this.testView.textBox("untilTF").requireEmpty();
     this.testView.textBox("ratePerParticipantTF").requireEmpty();
     this.testView.textBox("ratePerDayTF").requireEmpty();
     this.testView.label("idLbl").requireText((String) null);
     this.testView.table("campParticipants").requireRowCount(0);
 
     assertThat(this.editView.getNameOfCamp()).isEmpty();
     assertThat(this.editView.getLocationOfCamp()).isEmpty();
     assertThat(this.editView.getId()).isEqualTo(0);
     assertThat(this.editView.getFrom()).isNull();
     assertThat(this.editView.getUntil()).isNull();
     assertThat(this.editView.getRatePerParticipant()).isEmpty();
     assertThat(this.editView.getRatePerDay()).isEmpty();
     assertThat(this.editView.getCampParticipants()).isEmpty();
   }
 
   private void clear() {
     GuiActionRunner.execute(new GuiTask() {
       @Override
       protected void executeInEDT() throws Throwable {
         CampEditViewGUITest.this.editView.clear();
       }
     });
   }
 
   @Test
   public void testSetCamp() {
     GuiActionRunner.execute(new GuiTask() {
       @Override
       protected void executeInEDT() throws Throwable {
         CampEditViewGUITest.this.editView.setCamp(null);
       }
     });
     this.testView.textBox("nameTF").requireEmpty();
     this.testView.textBox("locationTF").requireEmpty();
     this.testView.textBox("fromTF").requireEmpty();
     this.testView.textBox("untilTF").requireEmpty();
     this.testView.textBox("ratePerParticipantTF").requireEmpty();
     this.testView.textBox("ratePerDayTF").requireEmpty();
     this.testView.label("idLbl").requireText((String) null);
 
     assertThat(this.editView.getNameOfCamp()).isEmpty();
     assertThat(this.editView.getLocationOfCamp()).isEmpty();
     assertThat(this.editView.getId()).isEqualTo(0);
     assertThat(this.editView.getFrom()).isNull();
     assertThat(this.editView.getUntil()).isNull();
     assertThat(this.editView.getRatePerParticipant()).isEmpty();
     assertThat(this.editView.getRatePerDay()).isEmpty();
   }
 
   @Test
   public void testGetName() {
     final JTextComponentFixture testObj = this.testView.textBox("nameTF");
 
     testObj.requireText(this.camp.getName());
     testObj.deleteText();
     testObj.enterText("OFZ");
     assertThat(this.editView.getNameOfCamp()).isEqualTo("OFZ");
   }
 
   @Test
   public void testGetLocation() {
     final JTextComponentFixture testObj = this.testView.textBox("locationTF");
 
     testObj.requireText(this.camp.getLocation());
     testObj.deleteText();
     testObj.enterText("Raversbeuren");
     assertThat(this.editView.getLocationOfCamp()).isEqualTo("Raversbeuren");
   }
 
   @Test
   public final void testGetFrom() throws ParseException {
     final JTextComponentFixture testObj = this.testView.textBox("fromTF");
     final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
 
     testObj.requireText(sdf.format(this.camp.getFromDate()));
     testObj.deleteText();
     testObj.enterText("24.03.2002");
     assertThat(this.editView.getFrom()).isEqualTo(sdf.parse("24.03.2002"));
   }
 
   @Test
   public final void testGetTo() throws ParseException {
     final JTextComponentFixture testObj = this.testView.textBox("untilTF");
     final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
 
     testObj.requireText(sdf.format(this.camp.getUntilDate()));
     testObj.deleteText();
     testObj.enterText("14.04.2002");
     assertThat(this.editView.getUntil()).isEqualTo(sdf.parse("14.04.2002"));
   }
 
   @Test
   public final void testGetRatePerParticipant() {
     final JTextComponentFixture testObj = this.testView.textBox("ratePerParticipantTF");
 
     testObj.requireText(this.camp.getRatePerParticipant());
     testObj.deleteText();
     testObj.enterText("1,50 EUR");
     assertThat(this.editView.getRatePerParticipant()).isEqualTo("1,50 EUR");
   }
 
   @Test
   public final void testGetRatePerDay() {
     final JTextComponentFixture testObj = this.testView.textBox("ratePerDayTF");
 
     testObj.requireText(this.camp.getRatePerDayChildren());
     testObj.deleteText();
     testObj.enterText("2,50 EUR");
     assertThat(this.editView.getRatePerDay()).isEqualTo("2,50 EUR");
   }
 
   @Test
   public final void testGetID() {
     final JLabelFixture testObj = this.testView.label("idLbl");
     testObj.requireText(String.valueOf(this.camp.getId()));
     assertThat(this.editView.getId()).isEqualTo(this.camp.getId());
   }
 
   @Test
   public final void testParticipantList() {
 
     final Participant participant1 = new Participant("Mustermann",
                                                      "Max",
                                                      Gender.MALE,
                                                      Denomination.OTHER,
                                                      new Date(),
                                                      "Musterstrasse 12",
                                                      12345,
                                                      "Musterhausen",
                                                      CountyCouncil.OTHER);
 
     final Date d1 = new Date(123);
 
     participant1.setBank("bank");
     participant1.setBankAccountNumber(1);
     participant1.setBankCodeNumber(12);
     participant1.setComment("comment");
     participant1.setDateSinceInDataBase(d1);
     participant1.setDateUpToInSystem(d1);
     participant1.setDenomination(Denomination.JEWISH);
     participant1.setFax("fax");
     participant1.setMailAddress("mail");
     participant1.setMobilePhone("mobile");
     participant1.setPhone("phone");
     participant1.setPhoneOfParents("phone");
     participant1.setPossibleAGE(true);
     participant1.setPossibleBoard(true);
     participant1.setPossibleExtendedBoard(true);
     participant1.setPossibleKitchen(true);
     participant1.setPossibleMAK(true);
     participant1.setPossibleMisc(true);
     participant1.setPossibleParticipant(true);
     participant1.setPossibleSeminar(true);
     participant1.setPossibleStaff(true);
     participant1.setPossibleStaffYouth(true);
     participant1.setCityPostal("city");
     participant1.setPostCodePostal(3124);
     participant1.setStreetPostal("street");
 
     final Participant participant2 = new Participant("Schmidt",
                                                      "Hans",
                                                      Gender.MALE,
                                                      Denomination.NONE,
                                                      new Date(1200),
                                                      "Strasse 4",
                                                      56789,
                                                      "Stadt",
                                                      CountyCouncil.CITY_NEUSTADT);
 
     final Participant participant3 = new Participant("Müller",
                                                      "Jasmin",
                                                      Gender.FEMALE,
                                                      Denomination.CATHOLIC,
                                                      new Date(6789543),
                                                      "Mittelgasse 3",
                                                      54321,
                                                      "Schimmelhausen",
                                                      CountyCouncil.CITY_ZWEIBRUECKEN);
 
     final Participant participant4 = new Participant("Mauer",
                                                      "Jaqueline",
                                                      Gender.FEMALE,
                                                      Denomination.JEWISH,
                                                      new Date(1297579),
                                                      "Hinterweg 12",
                                                      99384,
                                                      "Hilgen",
                                                      CountyCouncil.UNKNOWN);
 
     final Participant participant5 = new Participant("Bauer",
                                                      "Andreas",
                                                      Gender.MALE,
                                                      Denomination.EVANGELIC,
                                                      new Date(9876543),
                                                      "Julgenweg 76",
                                                      21228,
                                                      "Mildeningen",
                                                      CountyCouncil.CITY_NEUSTADT);
 
     final List<Participant> participants = Arrays.asList(participant1, participant2, participant3, participant4,
                                                          participant5);
     update(participants);
 
     requireParticipant(this.testView.table("participants"), 0, participant1);
     requireParticipant(this.testView.table("participants"), 1, participant2);
     requireParticipant(this.testView.table("participants"), 2, participant3);
     requireParticipant(this.testView.table("participants"), 3, participant4);
     requireParticipant(this.testView.table("participants"), 4, participant5);
   }
 
   @Test
   public final void testParticipantAdding() {
 
     final Participant participant1 = new Participant("Mustermann",
                                                      "Max",
                                                      Gender.MALE,
                                                      Denomination.OTHER,
                                                      new Date(),
                                                      "Musterstrasse 12",
                                                      12345,
                                                      "Musterhausen",
                                                      CountyCouncil.OTHER);
 
     final Date d1 = new Date(123);
 
     participant1.setBank("bank");
     participant1.setBankAccountNumber(1);
     participant1.setBankCodeNumber(12);
     participant1.setComment("comment");
     participant1.setDateSinceInDataBase(d1);
     participant1.setDateUpToInSystem(d1);
     participant1.setDenomination(Denomination.JEWISH);
     participant1.setFax("fax");
     participant1.setMailAddress("mail");
     participant1.setMobilePhone("mobile");
     participant1.setPhone("phone");
     participant1.setPhoneOfParents("phone");
     participant1.setPossibleAGE(true);
     participant1.setPossibleBoard(true);
     participant1.setPossibleExtendedBoard(true);
     participant1.setPossibleKitchen(true);
     participant1.setPossibleMAK(true);
     participant1.setPossibleMisc(true);
     participant1.setPossibleParticipant(true);
     participant1.setPossibleSeminar(true);
     participant1.setPossibleStaff(true);
     participant1.setPossibleStaffYouth(true);
     participant1.setCityPostal("city");
     participant1.setPostCodePostal(3124);
     participant1.setStreetPostal("street");
 
     final Participant participant2 = new Participant("Schmidt",
                                                      "Hans",
                                                      Gender.MALE,
                                                      Denomination.NONE,
                                                      new Date(1200),
                                                      "Strasse 4",
                                                      56789,
                                                      "Stadt",
                                                      CountyCouncil.CITY_NEUSTADT);
 
     final Participant participant3 = new Participant("Müller",
                                                      "Jasmin",
                                                      Gender.FEMALE,
                                                      Denomination.CATHOLIC,
                                                      new Date(6789543),
                                                      "Mittelgasse 3",
                                                      54321,
                                                      "Schimmelhausen",
                                                      CountyCouncil.CITY_ZWEIBRUECKEN);
 
     final Participant participant4 = new Participant("Mauer",
                                                      "Jaqueline",
                                                      Gender.FEMALE,
                                                      Denomination.JEWISH,
                                                      new Date(1297579),
                                                      "Hinterweg 12",
                                                      99384,
                                                      "Hilgen",
                                                      CountyCouncil.UNKNOWN);
 
     final Participant participant5 = new Participant("Bauer",
                                                      "Andreas",
                                                      Gender.MALE,
                                                      Denomination.EVANGELIC,
                                                      new Date(9876543),
                                                      "Julgenweg 76",
                                                      21228,
                                                      "Mildeningen",
                                                      CountyCouncil.CITY_NEUSTADT);
 
     final List<Participant> participants = Arrays.asList(participant1, participant2, participant3, participant4,
                                                          participant5);
     update(participants);
 
     requireParticipant(this.testView.table("participants"), 0, participant1);
     requireParticipant(this.testView.table("participants"), 1, participant2);
     requireParticipant(this.testView.table("participants"), 2, participant3);
     requireParticipant(this.testView.table("participants"), 3, participant4);
     requireParticipant(this.testView.table("participants"), 4, participant5);
     requireParticipant(this.testView.table("campParticipants"), 0, new CampParticipant(this.participant));
     assertThat(this.editView.getCampParticipants()).isEqualTo(Arrays.asList(new CampParticipant(this.participant)));
 
     this.testView.table("participants").click(TableCell.row(2).column(2), MouseClickInfo.leftButton().times(2));
     requireParticipant(this.testView.table("participants"), 0, participant1);
     requireParticipant(this.testView.table("participants"), 1, participant2);
     requireParticipant(this.testView.table("participants"), 2, participant4);
     requireParticipant(this.testView.table("participants"), 3, participant5);
     requireParticipant(this.testView.table("campParticipants"), 0, new CampParticipant(this.participant));
     requireParticipant(this.testView.table("campParticipants"), 1, new CampParticipant(participant3));
     assertThat(this.editView.getCampParticipants()).isEqualTo(Arrays.asList(new CampParticipant(this.participant),
                                                                             new CampParticipant(participant3)));
 
    this.testView.table("participants").click(TableCell.row(2).column(0), MouseClickInfo.leftButton().times(2));
     requireParticipant(this.testView.table("participants"), 0, participant1);
     requireParticipant(this.testView.table("participants"), 1, participant2);
     requireParticipant(this.testView.table("participants"), 2, participant5);
     requireParticipant(this.testView.table("campParticipants"), 0, new CampParticipant(this.participant));
     requireParticipant(this.testView.table("campParticipants"), 1, new CampParticipant(participant3));
     requireParticipant(this.testView.table("campParticipants"), 2, new CampParticipant(participant4));
     assertThat(this.editView.getCampParticipants()).isEqualTo(Arrays.asList(new CampParticipant(this.participant),
                                                                             new CampParticipant(participant3),
                                                                             new CampParticipant(participant4)));
 
    this.testView.table("participants").click(TableCell.row(2).column(1), MouseClickInfo.leftButton().times(2));
     requireParticipant(this.testView.table("participants"), 0, participant1);
     requireParticipant(this.testView.table("participants"), 1, participant2);
     requireParticipant(this.testView.table("campParticipants"), 0, new CampParticipant(this.participant));
     requireParticipant(this.testView.table("campParticipants"), 1, new CampParticipant(participant3));
     requireParticipant(this.testView.table("campParticipants"), 2, new CampParticipant(participant4));
     requireParticipant(this.testView.table("campParticipants"), 3, new CampParticipant(participant5));
     assertThat(this.editView.getCampParticipants()).isEqualTo(Arrays.asList(new CampParticipant(this.participant),
                                                                             new CampParticipant(participant3),
                                                                             new CampParticipant(participant4),
                                                                             new CampParticipant(participant5)));
   }
 
   @Test
   public final void testCampParticipantList() {
 
     final Participant participant1 = new Participant("Mustermann",
                                                      "Max",
                                                      Gender.MALE,
                                                      Denomination.OTHER,
                                                      new Date(),
                                                      "Musterstrasse 12",
                                                      12345,
                                                      "Musterhausen",
                                                      CountyCouncil.OTHER);
 
     final Date d1 = new Date(123);
 
     participant1.setBank("bank");
     participant1.setBankAccountNumber(1);
     participant1.setBankCodeNumber(12);
     participant1.setComment("comment");
     participant1.setDateSinceInDataBase(d1);
     participant1.setDateUpToInSystem(d1);
     participant1.setDenomination(Denomination.JEWISH);
     participant1.setFax("fax");
     participant1.setMailAddress("mail");
     participant1.setMobilePhone("mobile");
     participant1.setPhone("phone");
     participant1.setPhoneOfParents("phone");
     participant1.setPossibleAGE(true);
     participant1.setPossibleBoard(true);
     participant1.setPossibleExtendedBoard(true);
     participant1.setPossibleKitchen(true);
     participant1.setPossibleMAK(true);
     participant1.setPossibleMisc(true);
     participant1.setPossibleParticipant(true);
     participant1.setPossibleSeminar(true);
     participant1.setPossibleStaff(true);
     participant1.setPossibleStaffYouth(true);
     participant1.setCityPostal("city");
     participant1.setPostCodePostal(3124);
     participant1.setStreetPostal("street");
 
     final Participant participant2 = new Participant("Schmidt",
                                                      "Hans",
                                                      Gender.MALE,
                                                      Denomination.NONE,
                                                      new Date(1200),
                                                      "Strasse 4",
                                                      56789,
                                                      "Stadt",
                                                      CountyCouncil.CITY_NEUSTADT);
 
     final Participant participant3 = new Participant("Müller",
                                                      "Jasmin",
                                                      Gender.FEMALE,
                                                      Denomination.CATHOLIC,
                                                      new Date(6789543),
                                                      "Mittelgasse 3",
                                                      54321,
                                                      "Schimmelhausen",
                                                      CountyCouncil.CITY_ZWEIBRUECKEN);
 
     final Participant participant4 = new Participant("Mauer",
                                                      "Jaqueline",
                                                      Gender.FEMALE,
                                                      Denomination.JEWISH,
                                                      new Date(1297579),
                                                      "Hinterweg 12",
                                                      99384,
                                                      "Hilgen",
                                                      CountyCouncil.UNKNOWN);
 
     final Participant participant5 = new Participant("Bauer",
                                                      "Andreas",
                                                      Gender.MALE,
                                                      Denomination.EVANGELIC,
                                                      new Date(9876543),
                                                      "Julgenweg 76",
                                                      21228,
                                                      "Mildeningen",
                                                      CountyCouncil.CITY_NEUSTADT);
 
     final CampParticipant campParticipant = new CampParticipant(participant1);
     campParticipant.setAGE(true);
     this.camp.addParticipant(campParticipant);
     this.camp.addParticipant(new CampParticipant(participant2));
     this.camp.addParticipant(new CampParticipant(participant3));
     this.camp.addParticipant(new CampParticipant(participant4));
     this.camp.addParticipant(new CampParticipant(participant5));
 
     setCamp();
 
     requireParticipant(this.testView.table("campParticipants"), 0, new CampParticipant(this.participant));
     requireParticipant(this.testView.table("campParticipants"), 1, campParticipant);
     requireParticipant(this.testView.table("campParticipants"), 2, new CampParticipant(participant2));
     requireParticipant(this.testView.table("campParticipants"), 3, new CampParticipant(participant3));
     requireParticipant(this.testView.table("campParticipants"), 4, new CampParticipant(participant4));
     requireParticipant(this.testView.table("campParticipants"), 5, new CampParticipant(participant5));
 
     final CampParticipant cParticipant = new CampParticipant(participant1);
     cParticipant.setAGE(false);
     this.camp.removeParticipant(cParticipant);
     setCamp();
     requireParticipant(this.testView.table("campParticipants"), 0, new CampParticipant(this.participant));
     requireParticipant(this.testView.table("campParticipants"), 1, new CampParticipant(participant2));
     requireParticipant(this.testView.table("campParticipants"), 2, new CampParticipant(participant3));
     requireParticipant(this.testView.table("campParticipants"), 3, new CampParticipant(participant4));
     requireParticipant(this.testView.table("campParticipants"), 4, new CampParticipant(participant5));
 
     assertThat(this.editView.getCampParticipants()).isEqualTo(this.camp.getParticipants());
   }
 
   private void setCamp() {
     GuiActionRunner.execute(new GuiTask() {
       @Override
       protected void executeInEDT() throws Throwable {
         CampEditViewGUITest.this.editView.setCamp(CampEditViewGUITest.this.camp);
       }
     });
   }
 
   private void update(final List<Participant> participants) {
     GuiActionRunner.execute(new GuiTask() {
       @Override
       protected void executeInEDT() throws Throwable {
         CampEditViewGUITest.this.editView.update(participants);
       }
     });
   }
 }
