 package hoplugins.flagsplugin;
 /**
  * FlagUpdater.java
  *
  * @author Daniel González Fisher
  */
 
 import java.io.*;
 import plugins.*;
 import org.w3c.dom.*;
 
 import java.awt.*;
 import javax.swing.*;
 import java.util.HashMap;
 import java.util.TreeSet;
 import java.util.Iterator;
 import java.util.Collection;
 import hoplugins.FlagsPlugin;
 import hoplugins.commons.utils.Debug;
 
 public class FlagUpdater {
     private plugins.IHOMiniModel hoModel;
     private HashMap<Integer,Integer> teams;
     private FlagCollection fcAway;
     private FlagCollection fcHosted;
 
     public FlagUpdater(IHOMiniModel hom, HashMap<Integer,Integer> t, FlagCollection away, FlagCollection hosted) {
         hoModel = hom;
         teams = t;
         fcAway = away;
         fcHosted = hosted;
     }
 
     public boolean updateFlags() {
         hoModel.getGUI().getInfoPanel().clearProgressbar();
 
         int myTeamId = hoModel.getBasics().getTeamId();
         IMatchKurzInfo [] partidos = hoModel.getMatchesKurzInfo(myTeamId, ISpielePanel.NUR_EIGENE_FREUNDSCHAFTSSPIELE, true);
         TreeSet<FlagObject> awayFlags = new TreeSet<FlagObject>();
         TreeSet<FlagObject> hostedFlags = new TreeSet<FlagObject>();
 
         for (int i=0; i<partidos.length; i++) {
             //int matchId = partidos[i].getMatchID();
             if (partidos[i].getMatchStatus() != IMatchKurzInfo.FINISHED) continue;
 
             boolean national = false;
             switch (partidos[i].getMatchTyp()) {
             case 4: // amistoso, normal
             case 5: // amistoso, reglas copa
                 if (!FlagsPlugin.OWN_FLAG) continue;
                 national = true;
             case 8: // amistoso internacional, normal
             case 9: // amistoso internacional, reglas copa
                 int oppoId = 0; //(partidos[i].getGastID()==myTeamId) ? partidos[i].getHeimID() : partidos[i].getGastID();
                 boolean visited = true;
                 if (partidos[i].getHeimID()==myTeamId) {
                     oppoId = partidos[i].getGastID();
                     visited = false;
                 } else oppoId = partidos[i].getHeimID();
                 hoModel.getGUI().getInfoPanel().changeProgressbarValue((i+1)*100 / partidos.length);
                 int countryId = 0;
                 /* already have that id? */
                 if (national) countryId = hoModel.getBasics().getLand();
                 else {
                     Integer oppoIdInt = new Integer(oppoId);
                     if (teams.containsKey(oppoIdInt)) {
                         countryId = ((Integer)teams.get(oppoIdInt)).intValue();
                     } else {
                         countryId = getCountryIdFromHT(oppoId);  // ****
                         if (countryId == 0) continue;
                         teams.put(oppoIdInt, new Integer(countryId));
                     }
                 }
                 /* add to list */
                 if (visited) awayFlags.add(new FlagObject(countryId));
                 else hostedFlags.add(new FlagObject(countryId));
                 break;
             default:
                 // 6 Not currently in use, but reserved for international competition matches with normal rules
                 // 7 Not currently in use, but reserved for international competition matches with cup rules
                 continue;
             }
             //debug.append("¬ "+matchId+"&nbsp;&nbsp; ST:"+partidos[i].getMatchStatus() + "&nbsp;&nbsp; Type:" + partidos[i].getMatchTyp() + (visited?"AWAY":"HOSTED") +":("+partidos[i].getHeimID() +"/"+ partidos[i].getGastID()+") @:" + countryId +"," + FlagsPlugin.getCountryName(countryId)); //DEBUG!!!
         }
 
         /* get new flags */
         Collection<FlagObject> newAway = fcAway.getMissing(awayFlags);
         Collection<FlagObject> newHosted = fcHosted.getMissing(hostedFlags);
         Collection<FlagObject> surplusAway = fcAway.getSurplus(awayFlags);
         Collection<FlagObject> surplusHosted = fcHosted.getSurplus(hostedFlags);
 
         /* show new flags obtained */
         JCheckBox chbx1 = new JCheckBox("add these", true);
         JCheckBox chbx2 = new JCheckBox("add these", true);
         JCheckBox chbx3 = new JCheckBox("remove these", false);
         JCheckBox chbx4 = new JCheckBox("remove these", false);
 
         JPanel cp = new JPanel(new GridLayout(2,2,4,4));
         cp.add(createFlagPanel(newAway, "New visited countries", chbx1));
         cp.add(createFlagPanel(newHosted, "New hosted countries", chbx2));
         cp.add(createFlagPanel(surplusAway, "Countries not visited yet", chbx3));
         cp.add(createFlagPanel(surplusHosted, "Countries not hosted yet", chbx4));
 
      /* public static int showOptionDialog(Component parentComponent,
                                            Object message,
                                            String title,
                                            int optionType,
                                            int messageType,
                                            Icon icon,
                                            Object[] options,
                                            Object initialValue) */
         int opcion = JOptionPane.showOptionDialog(hoModel.getGUI().getOwner4Dialog(), cp , "New Flags",
                                                   JOptionPane.OK_CANCEL_OPTION,
                                                   JOptionPane.PLAIN_MESSAGE, null, null, null);
         boolean updated = false;
         if (opcion == JOptionPane.OK_OPTION) {
             /* add flags */
             if (chbx1.isSelected()) updated = fcAway.addAll(newAway) || updated;
             if (chbx2.isSelected()) updated = fcHosted.addAll(newHosted) || updated;
             /* remove flags */
             if (chbx3.isSelected()) updated = fcAway.removeAll(surplusAway) || updated;
             if (chbx4.isSelected()) updated = fcHosted.removeAll(surplusHosted) || updated;
         }
         return updated;
     }
 
     protected JComponent createFlagPanel(Collection<FlagObject> flags, String title, JComponent jc) {
         JPanel p = new JPanel(new GridLayout(0,FlagsPlugin.FLAGS_PER_ROW,3,6));
         Iterator<FlagObject> it = flags.iterator();
         while (it.hasNext()) {
             FlagObject fo = it.next();
             p.add(FlagCollection.createFlag(fo));
         }
 
         GridBagLayout gbl = new GridBagLayout();
         GridBagConstraints gc = new GridBagConstraints();
         JPanel pq = new JPanel(gbl);
 
         gc.anchor = GridBagConstraints.NORTH;
         gc.weightx = 0.0;
         gc.weighty = 0.0;
         gc.gridwidth = GridBagConstraints.REMAINDER;
         gc.gridheight = 1;
         gc.fill = GridBagConstraints.NONE;
         gbl.setConstraints(p, gc);
         pq.add(p);
 
         gc.anchor = GridBagConstraints.CENTER;
         gc.weightx = 1.0;
         gc.weighty = 1.0;
         gc.gridheight = GridBagConstraints.RELATIVE;
         gc.fill = GridBagConstraints.VERTICAL;
         Component glue = Box.createVerticalGlue();
         gbl.setConstraints(glue, gc);
         pq.add(glue);
 
         gc.weightx = 0.0;
         gc.weighty = 0.0;
         gc.gridheight = GridBagConstraints.REMAINDER;
         gc.fill = GridBagConstraints.NONE;
         gbl.setConstraints(jc, gc);
         pq.add(jc);
         pq.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK,1), title));
         return pq;
     }
 
     protected int getCountryIdFromHT(int id) {
         JWindow pbar = hoModel.getGUI().createWaitDialog(null);
         pbar.setVisible(true);
 		String htpage = "/common/chppxml.axd?file=team&teamID=" + id;
         String xmlstr = "";
         try {
             xmlstr = hoModel.getDownloadHelper().getHattrickXMLFile(htpage);
         } catch (IOException e) {
             pbar.setVisible(false);
             pbar.dispose();
             Debug.log("Flags: Error 1 getting country for team " + id + " : " + e);
             return 0;
         } catch (NullPointerException e) {
             pbar.setVisible(false);
             pbar.dispose();
             Debug.log("Flags: Error 2 getting country for team " + id + " : " + e);
             return 0;
         }
 
         try {
 			Document doc = hoModel.getXMLParser().parseString(xmlstr);
 			String leagueId = doc.getElementsByTagName("LeagueID").item(0).getFirstChild().getNodeValue();
			pbar.setVisible(false);
			pbar.dispose();
 			return FlagsPlugin.getCountryIdFromLeague(leagueId);
 		} catch (Exception e) {
 			Debug.log("Flags: Error 3 getting country for team " + id + " : " + e);
 			return 0;
 		}
     }
 
 
 //     private void test() {
 //         Document doc = null;
 //         doc = hoModel.getXMLParser().parseFile(new File("test/teamDetails.xml"));
 
 //         NodeList no1 = doc.getElementsByTagName("League");
 //         NodeList no2 = doc.getElementsByTagName("LeagueID");
 
 //         jta.append(Integer.toString(no1.getLength()) + "\n");
 //         jta.append(Integer.toString(no2.getLength()) + "\n");
 
 //         Node node = no2.item(0);
 //         jta.append(node.getNodeName() + "\n");
 //         jta.append("type: " + Short.toString(node.getNodeType()) + "\n");
 //         String str = node.getNodeValue();
 //         if (str == null) jta.append("NULL\n");
 //         else jta.append(str + "*\n");
 
 //         if (node.hasAttributes()) jta.append("ATTR\n");
 //         if (node.hasChildNodes()) jta.append("CHILD\n");
 
 //         NodeList no3 = node.getChildNodes();
 //         jta.append(Integer.toString(no3.getLength()) + "!\n");
 
 //         Node x = no3.item(0);
 //         jta.append(x.getNodeName() + "\n");
 //         jta.append("type: " + Short.toString(x.getNodeType()) + "\n");
 //         str = x.getNodeValue();
 //         if (str == null) jta.append("NULL\n");
 //         else jta.append("Valor: " + str + ".\n");
 
 
 // //         jta.append(ele.getTagName() + "\n");
 // //         jta.append(ele.getNodeValue() + "\n");
 
 //     }
 
 }
