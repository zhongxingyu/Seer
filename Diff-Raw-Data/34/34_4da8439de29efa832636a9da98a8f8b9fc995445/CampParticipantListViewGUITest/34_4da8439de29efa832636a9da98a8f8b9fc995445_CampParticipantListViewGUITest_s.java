 package com.github.croesch.partimana.view;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import java.awt.Dimension;
 import java.awt.event.KeyEvent;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Date;
 
 import javax.swing.SwingUtilities;
 
 import org.fest.swing.core.MouseClickInfo;
 import org.fest.swing.data.TableCell;
 import org.fest.swing.edt.GuiActionRunner;
 import org.fest.swing.edt.GuiQuery;
 import org.fest.swing.fixture.Containers;
 import org.fest.swing.fixture.FrameFixture;
 import org.fest.swing.fixture.JPanelFixture;
 import org.fest.swing.fixture.JTableFixture;
 import org.junit.Test;
 
 import com.github.croesch.partimana.PartiManaDefaultGUITestCase;
 import com.github.croesch.partimana.actions.UserAction;
 import com.github.croesch.partimana.types.CampParticipant;
 import com.github.croesch.partimana.types.CountyCouncil;
 import com.github.croesch.partimana.types.Denomination;
 import com.github.croesch.partimana.types.Gender;
 import com.github.croesch.partimana.types.Participant;
 
 /**
  * Provides gui tests for {@link CampParticipantListView}
  * 
  * @author croesch
  * @since Date: Sep 16, 2012
  */
 public class CampParticipantListViewGUITest extends PartiManaDefaultGUITestCase {
 
   private CampParticipantListView listView;
 
   private JPanelFixture testView;
 
   private CampParticipant campParticipant1;
 
   private CampParticipant campParticipant2;
 
   private CampParticipant campParticipant3;
 
   private CampParticipant campParticipant4;
 
   private CampParticipant campParticipant5;
 
   @Override
   protected void before() {
     final CampParticipantListView view = GuiActionRunner.execute(new GuiQuery<CampParticipantListView>() {
       @Override
       protected CampParticipantListView executeInEDT() throws Throwable {
         return new CampParticipantListView(CampParticipantListViewGUITest.this);
       }
     });
 
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
     selectAllPossibilities(participant1);
     participant1.setCityPostal("city");
     participant1.setPostCodePostal(3124);
     participant1.setStreetPostal("street");
 
     final Participant p2 = new Participant("Schmidt",
                                            "Hans",
                                            Gender.MALE,
                                            Denomination.NONE,
                                            new Date(1200),
                                            "Strasse 4",
                                            56789,
                                            "Stadt",
                                            CountyCouncil.CITY_NEUSTADT);
     selectAllPossibilities(p2);
     this.campParticipant2 = new CampParticipant(p2);
 
     final Participant p3 = new Participant("Müller",
                                            "Jasmin",
                                            Gender.FEMALE,
                                            Denomination.CATHOLIC,
                                            new Date(6789543),
                                            "Mittelgasse 3",
                                            54321,
                                            "Schimmelhausen",
                                            CountyCouncil.CITY_ZWEIBRUECKEN);
     selectAllPossibilities(p3);
     this.campParticipant3 = new CampParticipant(p3);
 
     final Participant p4 = new Participant("Mauer",
                                            "Jaqueline",
                                            Gender.FEMALE,
                                            Denomination.JEWISH,
                                            new Date(1297579),
                                            "Hinterweg 12",
                                            99384,
                                            "Hilgen",
                                            CountyCouncil.UNKNOWN);
     selectAllPossibilities(p4);
     this.campParticipant4 = new CampParticipant(p4);
 
     final Participant p5 = new Participant("Bauer",
                                            "Andreas",
                                            Gender.MALE,
                                            Denomination.EVANGELIC,
                                            new Date(9876543),
                                            "Julgenweg 76",
                                            21228,
                                            "Mildeningen",
                                            CountyCouncil.CITY_NEUSTADT);
     selectAllPossibilities(p5);
     this.campParticipant5 = new CampParticipant(p5);
 
     this.campParticipant1 = new CampParticipant(participant1);
 
     view.setName("listView");
     this.listView = view;
 
     final FrameFixture window = new FrameFixture(robot(), Containers.frameFor(view));
     window.target.setPreferredSize(new Dimension(800, 400));
     window.show();
     this.testView = window.panel("listView");
   }
 
   private void selectAllPossibilities(final Participant participant) {
     participant.setPossibleAGE(true);
     participant.setPossibleBoard(true);
     participant.setPossibleExtendedBoard(true);
     participant.setPossibleKitchen(true);
     participant.setPossibleMAK(true);
     participant.setPossibleMisc(true);
     participant.setPossibleParticipant(true);
     participant.setPossibleSeminar(true);
     participant.setPossibleStaff(true);
     participant.setPossibleStaffYouth(true);
   }
 
   @Override
   protected void after() {
     cleanUp();
   }
 
   @Test
   public final void testUpdateListOfParticipant() throws InterruptedException, InvocationTargetException {
     final ArrayList<CampParticipant> list = new ArrayList<CampParticipant>();
     list.add(this.campParticipant1);
     update(list);
     requireParticipant(this.testView.table(), 0, this.campParticipant1);
   }
 
   private void update(final ArrayList<CampParticipant> list) throws InterruptedException, InvocationTargetException {
     SwingUtilities.invokeAndWait(new Runnable() {
       @Override
       public void run() {
         CampParticipantListViewGUITest.this.listView.update(list);
       }
     });
   }
 
   @Test
   public final void testTableEditable() throws InterruptedException, InvocationTargetException {
     final ArrayList<CampParticipant> list = new ArrayList<CampParticipant>();
     list.add(this.campParticipant1);
     update(list);
     this.testView.table().requireNotEditable(TableCell.row(0).column(0));
     this.testView.table().requireNotEditable(TableCell.row(0).column(1));
     this.testView.table().requireNotEditable(TableCell.row(0).column(2));
   }
 
   @Test
   public final void testSelection() throws InterruptedException, InvocationTargetException {
     final ArrayList<CampParticipant> list = new ArrayList<CampParticipant>();
     list.add(this.campParticipant1);
     update(list);
     this.testView.table().selectRows(0);
     this.testView.table().requireSelectedRows(0);
     assertThat(this.listView.getSelectedParticipantId()).isEqualTo(this.campParticipant1.getId());
 
     this.testView.table().selectCell(TableCell.row(0).column(0));
     assertThat(this.testView.table().component().isCellSelected(0, 1)).isTrue();
   }
 
   @Test
   public final void testSorting_Id() throws InterruptedException, InvocationTargetException {
     final int id = pIds.poll();
     final Participant p1 = new Participant(id,
                                            "Schmidt",
                                            "Hans",
                                            Gender.MALE,
                                            Denomination.NONE,
                                            new Date(1200),
                                            "Strasse 4",
                                            56789,
                                            "Stadt",
                                            CountyCouncil.CITY_NEUSTADT);
     final Participant p2 = new Participant(id + 1,
                                            "Müller",
                                            "Jasmin",
                                            Gender.FEMALE,
                                            Denomination.CATHOLIC,
                                            new Date(6789543),
                                            "Mittelgasse 3",
                                            54321,
                                            "Schimmelhausen",
                                            CountyCouncil.CITY_ZWEIBRUECKEN);
     final Participant p3 = new Participant(id + 2,
                                            "Mauer",
                                            "Jaqueline",
                                            Gender.FEMALE,
                                            Denomination.JEWISH,
                                            new Date(1297579),
                                            "Hinterweg 12",
                                            99384,
                                            "Hilgen",
                                            CountyCouncil.UNKNOWN);
     p1.setPossibleAGE(false);
     p1.setPossibleBoard(true);
     p1.setPossibleExtendedBoard(false);
     p1.setPossibleKitchen(true);
     p1.setPossibleMAK(false);
     p1.setPossibleMisc(true);
     p1.setPossibleParticipant(false);
     p1.setPossibleSeminar(true);
     p1.setPossibleStaff(false);
     p1.setPossibleStaffYouth(true);
     p2.setPossibleAGE(true);
     p2.setPossibleBoard(false);
     p2.setPossibleExtendedBoard(true);
     p2.setPossibleKitchen(false);
     p2.setPossibleMAK(true);
     p2.setPossibleMisc(false);
     p2.setPossibleParticipant(true);
     p2.setPossibleSeminar(false);
     p2.setPossibleStaff(true);
     p2.setPossibleStaffYouth(false);
     selectAllPossibilities(p3);
 
     final CampParticipant cp1 = new CampParticipant(p1);
     final CampParticipant cp2 = new CampParticipant(p2);
     final CampParticipant cp3 = new CampParticipant(p3);
 
     final ArrayList<CampParticipant> list = new ArrayList<CampParticipant>();
     list.add(cp1);
     list.add(cp2);
     list.add(cp3);
     update(list);
 
     this.testView.table().requireRowCount(3);
     requireParticipant(this.testView.table(), 0, cp1);
     requireParticipant(this.testView.table(), 1, cp2);
     requireParticipant(this.testView.table(), 2, cp3);
 
     this.testView.table().tableHeader().clickColumn(0);
     requireParticipant(this.testView.table(), 0, cp1);
     requireParticipant(this.testView.table(), 1, cp2);
     requireParticipant(this.testView.table(), 2, cp3);
 
     this.testView.table().click();
     this.testView.table().tableHeader().clickColumn(0);
     requireParticipant(this.testView.table(), 0, cp3);
     requireParticipant(this.testView.table(), 1, cp2);
     requireParticipant(this.testView.table(), 2, cp1);
   }
 
   @Test
   public final void testSorting_FirstName() throws InterruptedException, InvocationTargetException {
     final ArrayList<CampParticipant> list = new ArrayList<CampParticipant>();
     list.add(this.campParticipant1);
     list.add(this.campParticipant2);
     list.add(this.campParticipant3);
     list.add(this.campParticipant4);
     list.add(this.campParticipant5);
     update(list);
 
     this.testView.table().requireRowCount(5);
     requireParticipant(this.testView.table(), 0, this.campParticipant1);
     requireParticipant(this.testView.table(), 1, this.campParticipant2);
     requireParticipant(this.testView.table(), 2, this.campParticipant3);
     requireParticipant(this.testView.table(), 3, this.campParticipant4);
     requireParticipant(this.testView.table(), 4, this.campParticipant5);
 
     this.testView.table().tableHeader().clickColumn(1);
     requireParticipant(this.testView.table(), 4, this.campParticipant1);
     requireParticipant(this.testView.table(), 1, this.campParticipant2);
     requireParticipant(this.testView.table(), 3, this.campParticipant3);
     requireParticipant(this.testView.table(), 2, this.campParticipant4);
     requireParticipant(this.testView.table(), 0, this.campParticipant5);
 
     this.testView.table().click();
     this.testView.table().tableHeader().clickColumn(1);
     requireParticipant(this.testView.table(), 0, this.campParticipant1);
     requireParticipant(this.testView.table(), 3, this.campParticipant2);
     requireParticipant(this.testView.table(), 1, this.campParticipant3);
     requireParticipant(this.testView.table(), 2, this.campParticipant4);
     requireParticipant(this.testView.table(), 4, this.campParticipant5);
   }
 
   @Test
   public final void testSorting_LastName() throws InterruptedException, InvocationTargetException {
     final ArrayList<CampParticipant> list = new ArrayList<CampParticipant>();
     list.add(this.campParticipant1);
     list.add(this.campParticipant2);
     list.add(this.campParticipant3);
     list.add(this.campParticipant4);
     list.add(this.campParticipant5);
     update(list);
 
     this.testView.table().requireRowCount(5);
     requireParticipant(this.testView.table(), 0, this.campParticipant1);
     requireParticipant(this.testView.table(), 1, this.campParticipant2);
     requireParticipant(this.testView.table(), 2, this.campParticipant3);
     requireParticipant(this.testView.table(), 3, this.campParticipant4);
     requireParticipant(this.testView.table(), 4, this.campParticipant5);
 
     this.testView.table().tableHeader().clickColumn(2);
     requireParticipant(this.testView.table(), 3, this.campParticipant1);
     requireParticipant(this.testView.table(), 4, this.campParticipant2);
     requireParticipant(this.testView.table(), 2, this.campParticipant3);
     requireParticipant(this.testView.table(), 1, this.campParticipant4);
     requireParticipant(this.testView.table(), 0, this.campParticipant5);
 
     this.testView.table().click();
     this.testView.table().tableHeader().clickColumn(2);
     requireParticipant(this.testView.table(), 1, this.campParticipant1);
     requireParticipant(this.testView.table(), 0, this.campParticipant2);
     requireParticipant(this.testView.table(), 2, this.campParticipant3);
     requireParticipant(this.testView.table(), 3, this.campParticipant4);
     requireParticipant(this.testView.table(), 4, this.campParticipant5);
   }
 
   @Test
   public final void testValueChanging() throws InterruptedException, InvocationTargetException {
     final ArrayList<CampParticipant> list = new ArrayList<CampParticipant>();
     list.add(this.campParticipant1);
     list.add(this.campParticipant2);
     list.add(this.campParticipant3);
     list.add(this.campParticipant4);
     list.add(this.campParticipant5);
     update(list);
 
     this.testView.table().requireRowCount(5);
     slct(this.testView.table(), 0, this.campParticipant1);
     slct(this.testView.table(), 1, this.campParticipant2);
     slct(this.testView.table(), 2, this.campParticipant3);
     slct(this.testView.table(), 3, this.campParticipant4);
     slct(this.testView.table(), 4, this.campParticipant5);
 
     this.testView.table().tableHeader().clickColumn(1);
     slct(this.testView.table(), 4, this.campParticipant1);
     slct(this.testView.table(), 1, this.campParticipant2);
     slct(this.testView.table(), 3, this.campParticipant3);
     slct(this.testView.table(), 2, this.campParticipant4);
     slct(this.testView.table(), 0, this.campParticipant5);
 
     this.testView.table().click();
     this.testView.table().tableHeader().clickColumn(1);
     slct(this.testView.table(), 0, this.campParticipant1);
     slct(this.testView.table(), 3, this.campParticipant2);
     slct(this.testView.table(), 1, this.campParticipant3);
     slct(this.testView.table(), 2, this.campParticipant4);
     slct(this.testView.table(), 4, this.campParticipant5);
   }
 
   private void slct(final JTableFixture table, final int row, final CampParticipant cp) {
     requireParticipant(table, row, cp);
 
     table.enterValue(TableCell.row(row).column(3), Boolean.TRUE.toString());
     assertThat(cp.isAGE()).isTrue();
     table.enterValue(TableCell.row(row).column(4), Boolean.TRUE.toString());
     assertThat(cp.isBoard()).isTrue();
     table.enterValue(TableCell.row(row).column(5), Boolean.TRUE.toString());
     assertThat(cp.isExtendedBoard()).isTrue();
     table.enterValue(TableCell.row(row).column(6), Boolean.TRUE.toString());
     assertThat(cp.isKitchen()).isTrue();
     table.enterValue(TableCell.row(row).column(7), Boolean.TRUE.toString());
     assertThat(cp.isMAK()).isTrue();
     table.enterValue(TableCell.row(row).column(8), Boolean.TRUE.toString());
     assertThat(cp.isMisc()).isTrue();
     table.enterValue(TableCell.row(row).column(9), Boolean.TRUE.toString());
     assertThat(cp.isParticipant()).isTrue();
     table.enterValue(TableCell.row(row).column(10), Boolean.TRUE.toString());
     assertThat(cp.isSeminar()).isTrue();
     table.enterValue(TableCell.row(row).column(11), Boolean.TRUE.toString());
     assertThat(cp.isStaff()).isTrue();
     table.enterValue(TableCell.row(row).column(12), Boolean.TRUE.toString());
     assertThat(cp.isStaffYouth()).isTrue();
 
     table.enterValue(TableCell.row(row).column(3), Boolean.TRUE.toString());
     assertThat(cp.isAGE()).isTrue();
     table.enterValue(TableCell.row(row).column(4), Boolean.FALSE.toString());
     assertThat(cp.isBoard()).isFalse();
     table.enterValue(TableCell.row(row).column(5), Boolean.TRUE.toString());
     assertThat(cp.isExtendedBoard()).isTrue();
     table.enterValue(TableCell.row(row).column(6), Boolean.FALSE.toString());
     assertThat(cp.isKitchen()).isFalse();
     table.enterValue(TableCell.row(row).column(7), Boolean.TRUE.toString());
     assertThat(cp.isMAK()).isTrue();
     table.enterValue(TableCell.row(row).column(8), Boolean.FALSE.toString());
     assertThat(cp.isMisc()).isFalse();
     table.enterValue(TableCell.row(row).column(9), Boolean.TRUE.toString());
     assertThat(cp.isParticipant()).isTrue();
     table.enterValue(TableCell.row(row).column(10), Boolean.FALSE.toString());
     assertThat(cp.isSeminar()).isFalse();
     table.enterValue(TableCell.row(row).column(11), Boolean.TRUE.toString());
     assertThat(cp.isStaff()).isTrue();
     table.enterValue(TableCell.row(row).column(12), Boolean.FALSE.toString());
     assertThat(cp.isStaffYouth()).isFalse();
 
     table.enterValue(TableCell.row(row).column(3), Boolean.FALSE.toString());
     assertThat(cp.isAGE()).isFalse();
     table.enterValue(TableCell.row(row).column(4), Boolean.TRUE.toString());
     assertThat(cp.isBoard()).isTrue();
     table.enterValue(TableCell.row(row).column(5), Boolean.FALSE.toString());
     assertThat(cp.isExtendedBoard()).isFalse();
     table.enterValue(TableCell.row(row).column(6), Boolean.TRUE.toString());
     assertThat(cp.isKitchen()).isTrue();
     table.enterValue(TableCell.row(row).column(7), Boolean.FALSE.toString());
     assertThat(cp.isMAK()).isFalse();
     table.enterValue(TableCell.row(row).column(8), Boolean.TRUE.toString());
     assertThat(cp.isMisc()).isTrue();
     table.enterValue(TableCell.row(row).column(9), Boolean.FALSE.toString());
     assertThat(cp.isParticipant()).isFalse();
     table.enterValue(TableCell.row(row).column(10), Boolean.TRUE.toString());
     assertThat(cp.isSeminar()).isTrue();
     table.enterValue(TableCell.row(row).column(11), Boolean.FALSE.toString());
     assertThat(cp.isStaff()).isFalse();
     table.enterValue(TableCell.row(row).column(12), Boolean.TRUE.toString());
     assertThat(cp.isStaffYouth()).isTrue();
   }
 
   @Test
   public final void testSelection_CountOfRows() throws InterruptedException, InvocationTargetException {
     final ArrayList<CampParticipant> list = new ArrayList<CampParticipant>();
     list.add(this.campParticipant1);
     list.add(this.campParticipant2);
     list.add(this.campParticipant3);
     list.add(this.campParticipant4);
     list.add(this.campParticipant5);
     update(list);
 
     this.testView.table().selectRows(1, 2, 3);
     this.testView.table().requireSelectedRows(3);
     assertThat(this.testView.table().target.getSelectedRowCount()).isEqualTo(1);
     assertThat(this.listView.getSelectedParticipantId()).isEqualTo(this.campParticipant4.getId());
   }
 
   @Test
   public final void testSelection_MultipleUpdate() throws InterruptedException, InvocationTargetException {
     final ArrayList<CampParticipant> list = new ArrayList<CampParticipant>();
     list.add(this.campParticipant1);
     list.add(this.campParticipant2);
     list.add(this.campParticipant3);
     list.add(this.campParticipant4);
     list.add(this.campParticipant5);
     update(list);
 
     this.testView.table().requireRowCount(5);
     requireParticipant(this.testView.table(), 0, this.campParticipant1);
     requireParticipant(this.testView.table(), 1, this.campParticipant2);
     requireParticipant(this.testView.table(), 2, this.campParticipant3);
     requireParticipant(this.testView.table(), 3, this.campParticipant4);
     requireParticipant(this.testView.table(), 4, this.campParticipant5);
 
     list.remove(this.campParticipant4);
     list.remove(this.campParticipant3);
     update(list);
 
     this.testView.table().requireRowCount(3);
     requireParticipant(this.testView.table(), 0, this.campParticipant1);
     requireParticipant(this.testView.table(), 1, this.campParticipant2);
     requireParticipant(this.testView.table(), 2, this.campParticipant5);
 
     list.remove(this.campParticipant1);
     list.remove(this.campParticipant5);
     update(list);
 
     this.testView.table().requireRowCount(1);
     requireParticipant(this.testView.table(), 0, this.campParticipant2);
 
     list.add(this.campParticipant1);
     list.add(this.campParticipant3);
     list.add(this.campParticipant4);
     list.add(this.campParticipant5);
     update(list);
 
     this.testView.table().requireRowCount(5);
     requireParticipant(this.testView.table(), 1, this.campParticipant1);
     requireParticipant(this.testView.table(), 0, this.campParticipant2);
     requireParticipant(this.testView.table(), 2, this.campParticipant3);
     requireParticipant(this.testView.table(), 3, this.campParticipant4);
     requireParticipant(this.testView.table(), 4, this.campParticipant5);
   }
 
   @Test
   public final void testGetSelectedParticipantId() throws InterruptedException, InvocationTargetException {
     final ArrayList<CampParticipant> list = new ArrayList<CampParticipant>();
     list.add(this.campParticipant1);
     list.add(this.campParticipant2);
     list.add(this.campParticipant3);
     list.add(this.campParticipant4);
     list.add(this.campParticipant5);
     update(list);
 
     this.testView.table().selectRows(1);
     assertThat(this.listView.getSelectedParticipantId()).isEqualTo(this.campParticipant2.getId());
 
     this.testView.table().selectRows(2);
     assertThat(this.listView.getSelectedParticipantId()).isEqualTo(this.campParticipant3.getId());
 
     this.testView.table().selectRows(4);
     assertThat(this.listView.getSelectedParticipantId()).isEqualTo(this.campParticipant5.getId());
 
     this.testView.table()
                  .pressKey(KeyEvent.VK_CONTROL)
                  .pressAndReleaseKeys(KeyEvent.VK_SPACE)
                  .releaseKey(KeyEvent.VK_CONTROL);
     this.testView.table().requireNoSelection();
     assertThat(this.listView.getSelectedParticipantId()).isZero();
   }
 
   @Test
   public final void testSelection_EventAfterDoubleClick() throws InterruptedException, InvocationTargetException {
     final ArrayList<CampParticipant> list = new ArrayList<CampParticipant>();
     list.add(this.campParticipant1);
     list.add(this.campParticipant2);
     list.add(this.campParticipant3);
     list.add(this.campParticipant4);
     list.add(this.campParticipant5);
     update(list);
 
     assertNoActionPerformed();
     this.testView.table().cell(TableCell.row(2).column(1)).doubleClick();
    assertThat(poll()).isEqualTo(UserAction.PARTICIPANT_SELECTED);
 
     assertNoActionPerformed();
     this.testView.table().cell(TableCell.row(0).column(0)).click(MouseClickInfo.middleButton().times(1));
 
     assertNoActionPerformed();
     this.testView.table().cell(TableCell.row(0).column(1)).click(MouseClickInfo.middleButton().times(2));
 
     assertNoActionPerformed();
     this.testView.table().cell(TableCell.row(0).column(2)).click(MouseClickInfo.middleButton().times(3));
 
     assertNoActionPerformed();
     this.testView.table().cell(TableCell.row(1).column(0)).click(MouseClickInfo.rightButton().times(1));
 
     assertNoActionPerformed();
     this.testView.table().cell(TableCell.row(1).column(1)).click(MouseClickInfo.rightButton().times(2));
 
     assertNoActionPerformed();
     this.testView.table().cell(TableCell.row(1).column(2)).click(MouseClickInfo.rightButton().times(3));
 
     assertNoActionPerformed();
     this.testView.table().cell(TableCell.row(2).column(0)).click(MouseClickInfo.leftButton().times(1));
 
     assertNoActionPerformed();
     this.testView.table().cell(TableCell.row(2).column(1)).click(MouseClickInfo.leftButton().times(2));
    assertThat(poll()).isEqualTo(UserAction.PARTICIPANT_SELECTED);
 
     assertNoActionPerformed();
     this.testView.table().cell(TableCell.row(2).column(2)).click(MouseClickInfo.leftButton().times(3));
    assertThat(poll()).isEqualTo(UserAction.PARTICIPANT_SELECTED);
 
     assertNoActionPerformed();
     this.testView.table().cell(TableCell.row(2).column(1)).click(MouseClickInfo.leftButton().times(16));
    assertThat(poll()).isEqualTo(UserAction.PARTICIPANT_SELECTED);
 
     assertNoActionPerformed();
     this.testView.table().cell(TableCell.row(4).column(0)).click(MouseClickInfo.leftButton().times(2));
    assertThat(poll()).isEqualTo(UserAction.PARTICIPANT_SELECTED);
 
     assertNoActionPerformed();
     this.testView.table().cell(TableCell.row(1).column(2)).click(MouseClickInfo.leftButton().times(2));
    assertThat(poll()).isEqualTo(UserAction.PARTICIPANT_SELECTED);
   }
 
   public static void requireParticipant(final JTableFixture table, final int row, final CampParticipant p) {
     table.requireCellValue(TableCell.row(row).column(0), String.valueOf(p.getId()));
     table.requireCellValue(TableCell.row(row).column(1), p.getForeName());
     table.requireCellValue(TableCell.row(row).column(2), p.getLastName());
     table.requireCellValue(TableCell.row(row).column(3), String.valueOf(p.isAGE()));
     table.requireCellValue(TableCell.row(row).column(4), String.valueOf(p.isBoard()));
     table.requireCellValue(TableCell.row(row).column(5), String.valueOf(p.isExtendedBoard()));
     table.requireCellValue(TableCell.row(row).column(6), String.valueOf(p.isKitchen()));
     table.requireCellValue(TableCell.row(row).column(7), String.valueOf(p.isMAK()));
     table.requireCellValue(TableCell.row(row).column(8), String.valueOf(p.isMisc()));
     table.requireCellValue(TableCell.row(row).column(9), String.valueOf(p.isParticipant()));
     table.requireCellValue(TableCell.row(row).column(10), String.valueOf(p.isSeminar()));
     table.requireCellValue(TableCell.row(row).column(11), String.valueOf(p.isStaff()));
     table.requireCellValue(TableCell.row(row).column(12), String.valueOf(p.isStaffYouth()));
 
     requireEditable(table, row, 3, p.isPossibleAGE());
     requireEditable(table, row, 4, p.isPossibleBoard());
     requireEditable(table, row, 5, p.isPossibleExtendedBoard());
     requireEditable(table, row, 6, p.isPossibleKitchen());
     requireEditable(table, row, 7, p.isPossibleMAK());
     requireEditable(table, row, 8, p.isPossibleMisc());
     requireEditable(table, row, 9, p.isPossibleParticipant());
     requireEditable(table, row, 10, p.isPossibleSeminar());
     requireEditable(table, row, 11, p.isPossibleStaff());
     requireEditable(table, row, 12, p.isPossibleStaffYouth());
   }
 
   private static void requireEditable(final JTableFixture table, final int row, final int column, final boolean editable) {
     if (editable) {
       table.requireEditable(TableCell.row(row).column(column));
     } else {
       table.requireNotEditable(TableCell.row(row).column(column));
     }
   }
 }
