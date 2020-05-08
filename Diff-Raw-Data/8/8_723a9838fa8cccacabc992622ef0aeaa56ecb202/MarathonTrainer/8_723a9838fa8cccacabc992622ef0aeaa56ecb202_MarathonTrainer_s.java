 package com.selesse.marathontrainer;
 
 import com.selesse.marathontrainer.model.DateUtils;
 import com.selesse.marathontrainer.model.Settings;
 import com.selesse.marathontrainer.resource.files.InvalidTrainingFileException;
 import com.selesse.marathontrainer.resource.files.TrainingPlanLoader;
 import com.selesse.marathontrainer.resource.language.Language;
 import com.selesse.marathontrainer.resource.language.LanguageResource;
 import com.selesse.marathontrainer.resource.language.LanguageResourceFactory;
 import com.selesse.marathontrainer.training.TrainingActivity;
 import com.selesse.marathontrainer.training.TrainingPlan;
 import org.jdesktop.swingx.JXLabel;
 import org.jdesktop.swingx.JXMonthView;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.*;
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 
 // no pun intended...
 public class MarathonTrainer implements Runnable {
     private LanguageResource resources;
     private JFrame mainFrame;
     private Settings settings;
     private TrainingPlan trainingPlan;
     private JXMonthView monthView;
     private JLabel tomorrowsActivityLabel;
     private JLabel activityLabel;
     private JLabel daysUntilMarathonLabel;
     private Date runDate;
 
     @Override
     public void run() {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 settings = loadOrInitializeSettings();
                 createAndShowGUI(settings.getLanguage());
                 runDate = new Date();
             }
         });
     }
 
     private Settings loadOrInitializeSettings() {
         try {
            File settingsFile = new File(Settings.getSettingsLocation());
             ObjectInputStream ois = new ObjectInputStream(new FileInputStream(settingsFile));
 
             settings = (Settings) ois.readObject();
 
             ois.close();
 
             return settings;
         }
         catch (FileNotFoundException e) {
             // cool, do nothing
         }
         catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame, resources.getLoadErrorMessage(), resources.getLoadErrorTitle(),
                    JOptionPane.ERROR_MESSAGE);
         }
 
         return new Settings(Language.ENGLISH);
     }
 
     private void createAndShowGUI(Language language) {
         resources = LanguageResourceFactory.createResource(language);
 
         setSystemLookAndFeel();
 
         mainFrame = new JFrame(getProgramTitle());
         mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 
         mainFrame.setJMenuBar(createMenuBar());
         mainFrame.setPreferredSize(new Dimension(1000, 500));
 
         mainFrame.addWindowListener(new WindowAdapter() {
             @Override
             public void windowActivated(WindowEvent e) {
                 // whenever we get window activation (i.e. window focus AND resuming from sleep), see if it's a new day
                 Date rightNow = new Date();
 
                 if (DateUtils.getNumberOfDaysBetween(runDate, rightNow) > 1) {
                     runDate = new Date();
                     monthView.setSelectionDate(runDate);
                     updateMarathonLabels(runDate);
                 }
             }
         });
 
         mainFrame.pack();
         mainFrame.setLocationRelativeTo(null);
         mainFrame.setVisible(true);
 
         Runtime.getRuntime().addShutdownHook(new Thread() {
             @Override
             public void run() {
                 save();
             }
         });
 
         showFirstScreen();
     }
 
     private void save() {
         if (settings != null) {
             try {
                 // Make sure the settings directory exists
                 File settingsDirectory = new File(Settings.getSettingsDirectory());
                 if (!settingsDirectory.exists()) {
                     settingsDirectory.mkdir();
                 }
 
                 String settingsPath = Settings.getSettingsLocation();
                 File settingsFile = new File(settingsPath);
 
                 if (!settingsFile.createNewFile()) {
                     // ignore if the delete fails for now
                     settingsFile.delete();
                     settingsFile.createNewFile();
                 }
                 ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(settingsFile));
                 oos.writeObject(settings);
                 oos.flush();
                 oos.close();
             } catch (FileNotFoundException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     private void loadTrainingPlan() {
         try {
             trainingPlan = TrainingPlanLoader.loadPlan(settings);
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (InvalidTrainingFileException e) {
             e.printStackTrace();
         }
     }
 
     private void showFirstScreen() {
         if (settings.getMarathonDate() == null) {
             showFileNewHint();
         }
         else {
             showMarathonTrainer();
         }
     }
 
     private void showFileNewHint() {
         JPanel pane = new JPanel(new GridLayout(0, 1));
         JLabel startHintText = new JLabel(resources.getStartHintText());
         startHintText.setHorizontalAlignment(SwingConstants.CENTER);
 
         pane.add(startHintText);
 
         mainFrame.getContentPane().add(pane, BorderLayout.CENTER);
     }
 
     private void showMarathonTrainer() {
         mainFrame.getContentPane().removeAll();
         mainFrame.getContentPane().repaint();
         // panel that will contain all the components
         JPanel innerMarathonPanel = new JPanel(new GridBagLayout());
 
         loadTrainingPlan();
 
         mainFrame.setTitle(getProgramTitle());
 
         // set up the month view
         monthView = new JXMonthView(resources.getLanguage().getLocale());
         monthView.setFirstDayOfWeek(Calendar.SUNDAY);
         monthView.setTraversable(true);
         monthView.setBoxPaddingX(5);
         monthView.setBoxPaddingY(10);
         monthView.setBackground(innerMarathonPanel.getBackground());
 
         // make sure the date isn't null, it's used later
         if (monthView.getSelectionDate() == null) {
             monthView.setSelectionDate(new Date());
         }
 
         // initialize all the labels
         activityLabel = new JLabel();
         tomorrowsActivityLabel = new JLabel();
         daysUntilMarathonLabel = new JLabel();
         final JXLabel referenceSpeedsLabel = new JXLabel(trainingPlan.getReferenceString(resources));
 
         activityLabel.setHorizontalAlignment(SwingConstants.CENTER);
         tomorrowsActivityLabel.setHorizontalAlignment(SwingConstants.CENTER);
         daysUntilMarathonLabel.setHorizontalAlignment(SwingConstants.CENTER);
         referenceSpeedsLabel.setHorizontalAlignment(SwingConstants.CENTER);
         referenceSpeedsLabel.setLineWrap(true);
 
         monthView.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 updateMarathonLabels(monthView.getSelectionDate());
             }
         });
 
         updateMarathonLabels(monthView.getSelectionDate());
 
         // set the constraints for the all the components and add them to the panel
         // in this case, we're handling the calendar view
         GridBagConstraints constraints = new GridBagConstraints();
         constraints.fill = GridBagConstraints.HORIZONTAL;
         constraints.gridwidth = 3;
         constraints.gridx = 0;
         constraints.gridy = 0;
         constraints.insets = new Insets(20, 0, 0, 0);
 
         innerMarathonPanel.add(monthView, constraints);
 
         // set the constraints for the "days until marathon" label (1/3)
         constraints.weightx = 0.5;
         constraints.gridwidth = 1;
         constraints.gridx = 0;
         constraints.gridy = 1;
         constraints.insets = new Insets(0, 0, 0, 0);
 
         innerMarathonPanel.add(daysUntilMarathonLabel, constraints);
 
         // set the constraints for today's activity label (2/3)
         constraints.weightx = 0.5;
         constraints.gridwidth = 1;
         constraints.gridx = 1;
         constraints.gridy = 1;
 
         innerMarathonPanel.add(activityLabel, constraints);
 
         // set the constraints for tomorrow's activity label (3/3)
         constraints.weightx = 0.5;
         constraints.gridwidth = 1;
         constraints.gridx = 2;
         constraints.gridy = 1;
 
         innerMarathonPanel.add(tomorrowsActivityLabel, constraints);
 
         // set the constraints for the reference speeds label
         constraints.gridwidth = 3;
         constraints.weightx = 0;
         constraints.weighty = 1.0;
         constraints.gridx = 0;
         constraints.gridy = 2;
         constraints.insets = new Insets(0, 20, 20, 0);
 
         innerMarathonPanel.add(referenceSpeedsLabel, constraints);
 
         mainFrame.getContentPane().add(innerMarathonPanel);
         mainFrame.setVisible(true);
     }
 
     private String getProgramTitle() {
         StringBuilder stringBuilder = new StringBuilder();
         stringBuilder.append(resources.getProgramName());
         if (settings.getTargetTime() != null) {
             stringBuilder.append(" - ").append(settings.getTargetTime());
         }
 
         return stringBuilder.toString();
     }
 
     private void updateMarathonLabels(Date date) {
         updateActivityInfoLabels(date, activityLabel, tomorrowsActivityLabel);
         int daysUntilMarathon = DateUtils.getNumberOfDaysBetween(date, settings.getMarathonDate());
         daysUntilMarathonLabel.setText(getDaysUntilMarathonLabel(daysUntilMarathon));
     }
 
     private void updateActivityInfoLabels(Date selectedDate, JLabel activityLabel, JLabel tomorrowsActivityLabel) {
         Date dayAfterSelectedDate = DateUtils.getNextDay(selectedDate);
         TrainingActivity trainingActivity = trainingPlan.getActivityForDate(selectedDate);
         TrainingActivity tomorrowsActivity = trainingPlan.getActivityForDate(dayAfterSelectedDate);
 
         String activityString, tomorrowActivityString;
 
         // if it's today, say "Today:", otherwise say "Jun 24, 2013" (or whatever locale string)
         if (DateUtils.isToday(selectedDate)) {
             activityString = getTodayActivityLabel(trainingActivity.toLanguageString(resources));
         }
         else {
             Locale currentLocale = settings.getLanguage().getLocale();
             DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, currentLocale);
             activityString = getDayActivityLabel(dateFormat.format(selectedDate),
                     trainingActivity.toLanguageString(resources));
         }
 
         tomorrowActivityString = getTomorrowActivityLabel(tomorrowsActivity.toLanguageString(resources));
 
         activityLabel.setText(activityString);
         tomorrowsActivityLabel.setText(tomorrowActivityString);
     }
 
     /**
      * The two line template string, i.e. bold header, newline, text
      */
     private String getTwoLineTemplateString() {
         return "<html> <p align=\"center\"> <b> %s: </b> <br/> <p align=\"center\"> %s </p> </p> </html>";
     }
 
     private String getDayActivityLabel(String day, String activityText) {
         return String.format(getTwoLineTemplateString(), day, activityText);
     }
 
     private String getTodayActivityLabel(String activityText) {
         return String.format(getTwoLineTemplateString(), resources.getTodayString(), activityText);
     }
 
     private String getTomorrowActivityLabel(String activityText) {
         return String.format(getTwoLineTemplateString(), resources.getNextDayString(), activityText);
     }
 
     private String getDaysUntilMarathonLabel(int days) {
         return String.format(getTwoLineTemplateString(), resources.getDaysUntilMarathonString(), "" + days);
     }
 
     private void setSystemLookAndFeel() {
         final String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
         try {
             UIManager.setLookAndFeel(lookAndFeel);
         } catch (Exception e) {
             // highly unlikely this will happen, we're just setting the defaults
             e.printStackTrace();
         }
 
         JFrame.setDefaultLookAndFeelDecorated(true);
     }
 
     private JMenuBar createMenuBar() {
         JMenuBar menuBar = new JMenuBar();
         menuBar.add(createFileMenu());
         menuBar.add(createSettingsMenu());
 
         return menuBar;
     }
 
     private JMenu createFileMenu() {
         JMenu fileMenu = new JMenu(resources.getMenuFileName());
         fileMenu.setMnemonic(resources.getMenuFileMnemonic());
 
         JMenuItem newMarathonMenuItem = new JMenuItem(resources.getNewMarathonName());
         newMarathonMenuItem.setMnemonic(resources.getNewMarathonMnemonic());
 
         newMarathonMenuItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 createNewMarathonDialog();
             }
         });
 
         fileMenu.add(newMarathonMenuItem);
         fileMenu.addSeparator();
 
         JMenuItem exitMenuItem = new JMenuItem(resources.getMenuExitName());
         exitMenuItem.setMnemonic(resources.getMenuExitMnemonic());
         fileMenu.add(exitMenuItem);
 
         exitMenuItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 System.exit(0);
             }
         });
 
         return fileMenu;
     }
 
     private void createNewMarathonDialog() {
         final JDialog dialog = new JDialog(mainFrame, resources.getNewMarathonName(), true);
         final NewMarathonDialog marathonDialog = new NewMarathonDialog(resources, settings);
 
         dialog.add(marathonDialog);
         dialog.setPreferredSize(new Dimension(250, 250));
 
         marathonDialog.addPropertyChangeListener("finished", new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt) {
                 dialog.dispose();
                 showMarathonTrainer();
             }
         });
 
         dialog.pack();
         dialog.setVisible(true);
     }
 
     private JMenu createSettingsMenu() {
         JMenu settingsMenu = new JMenu(resources.getMenuSettingsName());
         settingsMenu.setMnemonic(resources.getMenuSettingsMnemonic());
 
         JMenuItem languageMenuItem = new JMenuItem(resources.getMenuLanguageName());
         languageMenuItem.setMnemonic(resources.getMenuLanguageMnemonic());
 
         settingsMenu.add(languageMenuItem);
 
         languageMenuItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 Object[] supportedLanguages = Language.getSupportedLanguages();
 
                 Language currentLanguage = resources.getLanguage();
 
                 String chosenLanguage = (String) JOptionPane.showInputDialog(mainFrame,
                         resources.getLanguageChooserText(),
                         resources.getLanguageChooserTitle(),
                         JOptionPane.PLAIN_MESSAGE,
                         null,
                         supportedLanguages,
                         supportedLanguages[0]);
 
                 if (chosenLanguage != null) {
                     Language newLanguage = Language.getLanguageFromLocaleString(chosenLanguage);
                     if (currentLanguage != newLanguage) {
                         settings.setLanguage(newLanguage);
 
                         mainFrame.setVisible(false);
                         mainFrame.dispose();
                         mainFrame = null;
                         createAndShowGUI(newLanguage);
                     }
                 }
             }
         });
 
         return settingsMenu;
     }
 }
