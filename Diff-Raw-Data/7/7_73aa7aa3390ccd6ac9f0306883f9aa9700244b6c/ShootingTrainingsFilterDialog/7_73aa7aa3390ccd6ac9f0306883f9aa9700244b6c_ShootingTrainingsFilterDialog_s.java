 package com.jshooting.forms;
 
 import com.jshooting.shootingDatabase.ShootingTrainingType;
 import com.jshooting.shootingDatabase.ShootingTrainingsFilter;
 import com.jshooting.shootingDatabase.Sportsman;
 import com.jshooting.shootingDatabase.SportsmansTable;
 import com.jshooting.shootingDatabase.Team;
 import com.jshooting.shootingDatabase.TeamsTable;
 import com.jshooting.shootingDatabase.exceptions.DatabaseErrorException;
 import java.awt.Window;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListModel;
 import javax.swing.JOptionPane;
 
 /**
  * Shooting trainings filter setup dialog. Using to find which trainings need to
  * use
  *
  * @author pgalex
  */
 public class ShootingTrainingsFilterDialog extends javax.swing.JDialog
 {
 	/**
 	 * Model for teams combo box
 	 */
 	private DefaultComboBoxModel teamsComboBoxModel;
 	/**
 	 * Model for sportsmans list
 	 */
 	private DefaultListModel sportsmansListModel;
 	/**
 	 * Table of sportsmans
 	 */
 	private SportsmansTable sportsmansTable;
 	/**
 	 * Table of teams
 	 */
 	private TeamsTable teamsTable;
 	/**
 	 * Is OK button was pressed
 	 */
 	private boolean okButtonPressed;
 
 	/**
 	 * Create new dialog
 	 *
 	 * @param parentWindow parent window
 	 * @param modalityType modality type of dialog
 	 * @param teamsTable table of teams
 	 * @param sportsmansTable table of sportsmans
 	 */
 	public ShootingTrainingsFilterDialog(Window parentWindow, ModalityType modalityType,
 					TeamsTable teamsTable, SportsmansTable sportsmansTable)
 	{
 		super(parentWindow, modalityType);
 
 		if (teamsTable == null)
 		{
 			throw new IllegalArgumentException("teamsTable is null");
 		}
 		if (sportsmansTable == null)
 		{
 			throw new IllegalArgumentException("sportsmansTable is null");
 		}
 
 		okButtonPressed = false;
 		this.teamsTable = teamsTable;
 		this.sportsmansTable = sportsmansTable;
 
 		teamsComboBoxModel = new DefaultComboBoxModel();
 		sportsmansListModel = new DefaultListModel();
 
 		initComponents();
 		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0); //anything 0 - 23
 		calendar.set(Calendar.MINUTE, 0);
 		calendar.set(Calendar.SECOND, 0);
 		jDateChooserDateFrom.setDate(calendar.getTime());
 		jDateChooserDateTo.setDate(calendar.getTime());
 		setTitle("Фильтр");
 
 		fillTeamsComboBox();
 		fillSportmansListBySelectedTeam();
 	}
 
 	/**
 	 * Fill teams combo box model by teams table. Model will be empty if can no
 	 * get access to table
 	 *
 	 * @param teamsTable table of teams
 	 */
 	private void fillTeamsComboBox()
 	{
 		try
 		{
 			teamsComboBoxModel.removeAllElements();
 			List<Team> allTeams = teamsTable.getAllTeams();
 			for (Team team : allTeams)
 			{
 				teamsComboBoxModel.addElement(team);
 			}
 		}
 		catch (DatabaseErrorException ex)
 		{
 			teamsComboBoxModel.removeAllElements();
 		}
 	}
 
 	/**
 	 * Fill sportmans list model by sportmans in team, selected in teams combo
 	 * box. Empty if team not selected or can not get access to sportmans table
 	 */
 	private void fillSportmansListBySelectedTeam()
 	{
 		try
 		{
 			sportsmansListModel.removeAllElements();
 			Object selectedItem = jComboBoxTeams.getSelectedItem();
 			if (selectedItem instanceof Team)
 			{
 				Team selectedTeam = (Team) selectedItem;
 				List<Sportsman> sportsmansInSelectedTeam = sportsmansTable.getSportsmansInTeam(selectedTeam);
 				for (Sportsman sportsman : sportsmansInSelectedTeam)
 				{
 					sportsmansListModel.addElement(sportsman);
 				}
 			}
 		}
 		catch (DatabaseErrorException ex)
 		{
 			sportsmansListModel.removeAllElements();
 		}
 	}
 
 	/**
 	 * Is OK button was pressed
 	 *
 	 * @return is OK button was pressed
 	 */
 	public boolean isOKButtonPressed()
 	{
 		return okButtonPressed;
 	}
 
 	/**
 	 * Get filter with parameters choosed in dialog
 	 *
 	 * @return filter created with dialog. null if cant create filter with
 	 * parameters from dialog
 	 */
 	public ShootingTrainingsFilter getFilter()
 	{
 		if (jDateChooserDateFrom.getDate().after(jDateChooserDateTo.getDate()))
 		{
 			return null;
 		}
 
 		Object[] selectedSportsmans = jListSportsmans.getSelectedValues();
 		List<Sportsman> filteringSportsmans = new ArrayList<Sportsman>();
 		for (int i = 0; i < selectedSportsmans.length; i++)
 		{
 			filteringSportsmans.add((Sportsman) selectedSportsmans[i]);
 		}
 
 		List<ShootingTrainingType> filteringTrainingTypes = new ArrayList<ShootingTrainingType>();
 		if (jCheckBoxComplex.isSelected())
 		{
 			filteringTrainingTypes.add(ShootingTrainingType.COMPLEX);
 		}
 		if (jCheckBoxShooting.isSelected())
 		{
 			filteringTrainingTypes.add(ShootingTrainingType.SHOOTING);
 		}
 		if (jCheckBoxCompetition.isSelected())
 		{
 			filteringTrainingTypes.add(ShootingTrainingType.COMPETITION);
 		}
 
 		return new ShootingTrainingsFilter(filteringSportsmans, jDateChooserDateFrom.getDate(),
 						jDateChooserDateTo.getDate(), filteringTrainingTypes);
 	}
 
 	/**
 	 * This method is called from within the constructor to initialize the form.
 	 * WARNING: Do NOT modify this code. The content of this method is always
 	 * regenerated by the Form Editor.
 	 */
 	@SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents()
   {
 
     jPanelSportsmans = new javax.swing.JPanel();
     jLabelTeam = new javax.swing.JLabel();
     jComboBoxTeams = new javax.swing.JComboBox();
     jScrollPane1 = new javax.swing.JScrollPane();
     jListSportsmans = new javax.swing.JList();
     jPanelPeriod = new javax.swing.JPanel();
     jLabel1 = new javax.swing.JLabel();
     jDateChooserDateFrom = new com.toedter.calendar.JDateChooser();
     jLabel2 = new javax.swing.JLabel();
     jDateChooserDateTo = new com.toedter.calendar.JDateChooser();
     jPanelTrainingsType = new javax.swing.JPanel();
     jCheckBoxComplex = new javax.swing.JCheckBox();
     jCheckBoxShooting = new javax.swing.JCheckBox();
     jCheckBoxCompetition = new javax.swing.JCheckBox();
     jButtonOK = new javax.swing.JButton();
     jButtonCancel = new javax.swing.JButton();
 
     setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
     setResizable(false);
     addWindowListener(new java.awt.event.WindowAdapter()
     {
       public void windowClosed(java.awt.event.WindowEvent evt)
       {
         formWindowClosed(evt);
       }
     });
 
     jPanelSportsmans.setBorder(javax.swing.BorderFactory.createTitledBorder("Спортсмены"));
 
     jLabelTeam.setText("Команда");
 
     jComboBoxTeams.setModel(teamsComboBoxModel);
     jComboBoxTeams.addActionListener(new java.awt.event.ActionListener()
     {
       public void actionPerformed(java.awt.event.ActionEvent evt)
       {
         jComboBoxTeamsActionPerformed(evt);
       }
     });
 
     jListSportsmans.setModel(sportsmansListModel);
     jScrollPane1.setViewportView(jListSportsmans);
 
     org.jdesktop.layout.GroupLayout jPanelSportsmansLayout = new org.jdesktop.layout.GroupLayout(jPanelSportsmans);
     jPanelSportsmans.setLayout(jPanelSportsmansLayout);
     jPanelSportsmansLayout.setHorizontalGroup(
       jPanelSportsmansLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
       .add(jPanelSportsmansLayout.createSequentialGroup()
         .addContainerGap()
         .add(jPanelSportsmansLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
           .add(jScrollPane1)
           .add(jPanelSportsmansLayout.createSequentialGroup()
             .add(jLabelTeam)
             .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
             .add(jComboBoxTeams, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
         .addContainerGap())
     );
     jPanelSportsmansLayout.setVerticalGroup(
       jPanelSportsmansLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
       .add(jPanelSportsmansLayout.createSequentialGroup()
         .add(jPanelSportsmansLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
           .add(jComboBoxTeams, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
           .add(jLabelTeam))
         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
         .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
         .addContainerGap())
     );
 
     jPanelPeriod.setBorder(javax.swing.BorderFactory.createTitledBorder("Период (включительно)"));
 
     jLabel1.setText("c");
 
     jLabel2.setText("по");
 
     org.jdesktop.layout.GroupLayout jPanelPeriodLayout = new org.jdesktop.layout.GroupLayout(jPanelPeriod);
     jPanelPeriod.setLayout(jPanelPeriodLayout);
     jPanelPeriodLayout.setHorizontalGroup(
       jPanelPeriodLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
       .add(jPanelPeriodLayout.createSequentialGroup()
         .addContainerGap()
         .add(jPanelPeriodLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
           .add(jLabel1)
           .add(jLabel2))
         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
         .add(jPanelPeriodLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
           .add(jDateChooserDateTo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
           .add(jDateChooserDateFrom, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         .addContainerGap())
     );
     jPanelPeriodLayout.setVerticalGroup(
       jPanelPeriodLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
       .add(jPanelPeriodLayout.createSequentialGroup()
         .add(jPanelPeriodLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
           .add(jDateChooserDateFrom, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
           .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
         .add(jPanelPeriodLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
           .add(jDateChooserDateTo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
           .add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
     );
 
     jPanelTrainingsType.setBorder(javax.swing.BorderFactory.createTitledBorder("Тип тренировки"));
 
     jCheckBoxComplex.setSelected(true);
     jCheckBoxComplex.setText("Комплексная");
     jCheckBoxComplex.addActionListener(new java.awt.event.ActionListener()
     {
       public void actionPerformed(java.awt.event.ActionEvent evt)
       {
         jCheckBoxComplexActionPerformed(evt);
       }
     });
 
     jCheckBoxShooting.setSelected(true);
     jCheckBoxShooting.setText("Стрелковая");
     jCheckBoxShooting.addActionListener(new java.awt.event.ActionListener()
     {
       public void actionPerformed(java.awt.event.ActionEvent evt)
       {
         jCheckBoxShootingActionPerformed(evt);
       }
     });
 
     jCheckBoxCompetition.setSelected(true);
     jCheckBoxCompetition.setText("Соревнование");
 
     org.jdesktop.layout.GroupLayout jPanelTrainingsTypeLayout = new org.jdesktop.layout.GroupLayout(jPanelTrainingsType);
     jPanelTrainingsType.setLayout(jPanelTrainingsTypeLayout);
     jPanelTrainingsTypeLayout.setHorizontalGroup(
       jPanelTrainingsTypeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
       .add(jPanelTrainingsTypeLayout.createSequentialGroup()
         .add(jPanelTrainingsTypeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
           .add(jCheckBoxComplex, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
           .add(jCheckBoxShooting, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
           .add(jCheckBoxCompetition, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE))
         .addContainerGap())
     );
     jPanelTrainingsTypeLayout.setVerticalGroup(
       jPanelTrainingsTypeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
       .add(jPanelTrainingsTypeLayout.createSequentialGroup()
         .add(jCheckBoxComplex)
         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
         .add(jCheckBoxShooting)
         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
         .add(jCheckBoxCompetition)
         .addContainerGap(127, Short.MAX_VALUE))
     );
 
     jButtonOK.setText("OK");
     jButtonOK.addActionListener(new java.awt.event.ActionListener()
     {
       public void actionPerformed(java.awt.event.ActionEvent evt)
       {
         jButtonOKActionPerformed(evt);
       }
     });
 
     jButtonCancel.setText("Отмена");
     jButtonCancel.addActionListener(new java.awt.event.ActionListener()
     {
       public void actionPerformed(java.awt.event.ActionEvent evt)
       {
         jButtonCancelActionPerformed(evt);
       }
     });
 
     org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
     getContentPane().setLayout(layout);
     layout.setHorizontalGroup(
       layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
       .add(layout.createSequentialGroup()
         .addContainerGap()
         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
           .add(layout.createSequentialGroup()
             .add(jPanelSportsmans, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
             .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
             .add(jPanelPeriod, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
             .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
             .add(jPanelTrainingsType, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
           .add(layout.createSequentialGroup()
             .add(jButtonOK)
             .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
             .add(jButtonCancel)
             .add(0, 0, Short.MAX_VALUE)))
         .addContainerGap())
     );
     layout.setVerticalGroup(
       layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
       .add(layout.createSequentialGroup()
         .addContainerGap()
         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
           .add(jPanelTrainingsType, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
           .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelPeriod, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
           .add(jPanelSportsmans, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
           .add(jButtonOK)
           .add(jButtonCancel))
         .addContainerGap())
     );
 
     pack();
   }// </editor-fold>//GEN-END:initComponents
 
   private void jComboBoxTeamsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jComboBoxTeamsActionPerformed
   {//GEN-HEADEREND:event_jComboBoxTeamsActionPerformed
 		fillSportmansListBySelectedTeam();
   }//GEN-LAST:event_jComboBoxTeamsActionPerformed
 
   private void jCheckBoxComplexActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxComplexActionPerformed
   {//GEN-HEADEREND:event_jCheckBoxComplexActionPerformed
 		// TODO add your handling code here:
   }//GEN-LAST:event_jCheckBoxComplexActionPerformed
 
   private void jCheckBoxShootingActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxShootingActionPerformed
   {//GEN-HEADEREND:event_jCheckBoxShootingActionPerformed
 		// TODO add your handling code here:
   }//GEN-LAST:event_jCheckBoxShootingActionPerformed
 
   private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonOKActionPerformed
   {//GEN-HEADEREND:event_jButtonOKActionPerformed
 		if (!jDateChooserDateFrom.getDate().after(jDateChooserDateTo.getDate()))
 		{
 			okButtonPressed = true;
 			setVisible(false);
 		}
 		else
 		{
 			JOptionPane.showMessageDialog(null, "Дата 'c' периода позже даты 'по' ", "Ошибка", JOptionPane.ERROR_MESSAGE);
 		}
   }//GEN-LAST:event_jButtonOKActionPerformed
 
   private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonCancelActionPerformed
   {//GEN-HEADEREND:event_jButtonCancelActionPerformed
 		okButtonPressed = false;
 		setVisible(false);
   }//GEN-LAST:event_jButtonCancelActionPerformed
 
   private void formWindowClosed(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosed
   {//GEN-HEADEREND:event_formWindowClosed
 		okButtonPressed = false;
   }//GEN-LAST:event_formWindowClosed
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton jButtonCancel;
   private javax.swing.JButton jButtonOK;
   private javax.swing.JCheckBox jCheckBoxCompetition;
   private javax.swing.JCheckBox jCheckBoxComplex;
   private javax.swing.JCheckBox jCheckBoxShooting;
   private javax.swing.JComboBox jComboBoxTeams;
   private com.toedter.calendar.JDateChooser jDateChooserDateFrom;
   private com.toedter.calendar.JDateChooser jDateChooserDateTo;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabelTeam;
   private javax.swing.JList jListSportsmans;
   private javax.swing.JPanel jPanelPeriod;
   private javax.swing.JPanel jPanelSportsmans;
   private javax.swing.JPanel jPanelTrainingsType;
   private javax.swing.JScrollPane jScrollPane1;
   // End of variables declaration//GEN-END:variables
 }
