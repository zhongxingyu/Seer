 package pms;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import javax.swing.*;
 
 /**
  * GUI for Patient Monitoring System.
  *
  * @author Anthony
  * @version 0.1
  */
 public class PMSGUI extends JFrame implements Display {
 	
 	private static final Color ECG_COLOR = Color.GREEN;
 	private static final Color NIBP_COLOR = new Color(0, 153, 255);
 	private static final Color GLU_COLOR = Color.YELLOW;
 	private static final Color TEMP_COLOR = new Color(255, 153, 51);
 	private static final Color WARNING_COLOR = Color.RED;
 	
 	private javax.swing.JLabel ageLabel;
 	private javax.swing.JLabel ageTitleLabel;
 	private javax.swing.JPanel currentStatePanel;
 	private javax.swing.JScrollPane currentStateScrollPane;
 	private javax.swing.JTextArea currentStateTextArea;
 	private javax.swing.JLabel currentStateTitleLabel;
 	private javax.swing.JPanel ecgPanel;
 	private javax.swing.JLabel ecgTitleLabel;
 	private javax.swing.JLabel ecgValueLabel;
 	private javax.swing.JLabel genderLabel;
 	private javax.swing.JLabel genderTitleLabel;
 	private javax.swing.JPanel generalInfoPanel;
 	private javax.swing.JPanel gluPanel;
 	private javax.swing.JLabel gluTitleLabel;
 	private javax.swing.JLabel gluValueLabel;
 	private javax.swing.JLabel heightLabel;
 	private javax.swing.JLabel heightTitleLabel;
 	private javax.swing.JScrollPane medicalConditionScrollPane;
 	private javax.swing.JTextArea medicalConditionTextArea;
 	private javax.swing.JLabel medicalCondtionTitleLabel;
 	private javax.swing.JLabel nameLabel;
 	private javax.swing.JLabel nameTitleLabel;
 	private javax.swing.JPanel nibpPanel;
 	private javax.swing.JLabel nibpTitleLabel;
 	private javax.swing.JLabel nibpValueLabel;
 	private javax.swing.JLabel patientNumberLabel;
 	private javax.swing.JLabel patientNumberTitleLabel;
 	private javax.swing.JPanel pmsInnerPanel;
 	private javax.swing.JPanel pmsPanel;
 	private javax.swing.JLabel tempTitleLabel;
 	private javax.swing.JLabel tempValueLabel;
 	private javax.swing.JPanel temperaturePanel;
 	private javax.swing.JLabel weightLabel;
 	private javax.swing.JLabel weightTitleLabel;
 	
 	/**
 	 * Constructs a PMSGUI.
 	 * 
 	 * @param patient patient information to show
 	 */
 	public PMSGUI(Patient patient) {
 		initComponents();
 		
 		nameLabel.setText(patient.getName());
 		patientNumberLabel.setText(patient.getPatientNumber());
 		ageLabel.setText(Integer.toString(patient.getAge()));
 		genderLabel.setText(patient.getGender().toString());
 		heightLabel.setText(Double.toString(patient.getHeight()));
 		weightLabel.setText(Double.toString(patient.getWeight()));
 		medicalConditionTextArea.setText(patient.getSpecificCondition());
 	}
 	
 	/**
 	 * initial the components of PMSGUI.
 	 */
 	private void initComponents() {
 		
         pmsPanel = new javax.swing.JPanel();
         pmsInnerPanel = new javax.swing.JPanel();
         generalInfoPanel = new javax.swing.JPanel();
         nameTitleLabel = new javax.swing.JLabel();
         patientNumberTitleLabel = new javax.swing.JLabel();
         ageTitleLabel = new javax.swing.JLabel();
         genderTitleLabel = new javax.swing.JLabel();
         weightTitleLabel = new javax.swing.JLabel();
         heightTitleLabel = new javax.swing.JLabel();
         medicalCondtionTitleLabel = new javax.swing.JLabel();
         nameLabel = new javax.swing.JLabel();
         patientNumberLabel = new javax.swing.JLabel();
         heightLabel = new javax.swing.JLabel();
         ageLabel = new javax.swing.JLabel();
         genderLabel = new javax.swing.JLabel();
         weightLabel = new javax.swing.JLabel();
         medicalConditionScrollPane = new javax.swing.JScrollPane();
         medicalConditionTextArea = new javax.swing.JTextArea();
         ecgPanel = new javax.swing.JPanel();
         ecgTitleLabel = new javax.swing.JLabel();
         ecgValueLabel = new javax.swing.JLabel();
         gluPanel = new javax.swing.JPanel();
         gluTitleLabel = new javax.swing.JLabel();
         gluValueLabel = new javax.swing.JLabel();
         currentStatePanel = new javax.swing.JPanel();
         currentStateTitleLabel = new javax.swing.JLabel();
         currentStateScrollPane = new javax.swing.JScrollPane();
         currentStateTextArea = new javax.swing.JTextArea();
         temperaturePanel = new javax.swing.JPanel();
         tempTitleLabel = new javax.swing.JLabel();
         tempValueLabel = new javax.swing.JLabel();
         nibpPanel = new javax.swing.JPanel();
         nibpTitleLabel = new javax.swing.JLabel();
         nibpValueLabel = new javax.swing.JLabel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setBackground(new java.awt.Color(0, 0, 0));
         setMaximumSize(new java.awt.Dimension(640, 480));
         setMinimumSize(new java.awt.Dimension(640, 480));
         setResizable(false);
 
         pmsPanel.setBackground(java.awt.Color.black);
         pmsPanel.setForeground(new java.awt.Color(255, 255, 255));
         pmsPanel.setMinimumSize(new java.awt.Dimension(640, 480));
         pmsPanel.setPreferredSize(new java.awt.Dimension(640, 480));
 
         pmsInnerPanel.setBackground(new java.awt.Color(255, 255, 255));
         pmsInnerPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 153, 153)));
         pmsInnerPanel.setMinimumSize(new java.awt.Dimension(580, 420));
         pmsInnerPanel.setOpaque(false);
         pmsInnerPanel.setPreferredSize(new java.awt.Dimension(580, 420));
 
         generalInfoPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 153, 102)));
         generalInfoPanel.setMinimumSize(new java.awt.Dimension(400, 160));
         generalInfoPanel.setOpaque(false);
         generalInfoPanel.setPreferredSize(new java.awt.Dimension(400, 300));
 
         nameTitleLabel.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
         nameTitleLabel.setForeground(new java.awt.Color(255, 255, 255));
         nameTitleLabel.setText("Name:");
 
         patientNumberTitleLabel.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
         patientNumberTitleLabel.setForeground(new java.awt.Color(255, 255, 255));
         patientNumberTitleLabel.setText("PatientNumber:");
 
         ageTitleLabel.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
         ageTitleLabel.setForeground(new java.awt.Color(255, 255, 255));
         ageTitleLabel.setText("Age:");
 
         genderTitleLabel.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
         genderTitleLabel.setForeground(new java.awt.Color(255, 255, 255));
         genderTitleLabel.setText("Gender:");
 
         weightTitleLabel.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
         weightTitleLabel.setForeground(new java.awt.Color(255, 255, 255));
         weightTitleLabel.setText("Weight:");
 
         heightTitleLabel.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
         heightTitleLabel.setForeground(new java.awt.Color(255, 255, 255));
         heightTitleLabel.setText("Height:");
 
         medicalCondtionTitleLabel.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
         medicalCondtionTitleLabel.setForeground(new java.awt.Color(255, 255, 255));
         medicalCondtionTitleLabel.setText("Specific Medical Condition:");
 
         nameLabel.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
         nameLabel.setForeground(new java.awt.Color(255, 255, 255));
 
         patientNumberLabel.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
         patientNumberLabel.setForeground(new java.awt.Color(255, 255, 255));
 
         heightLabel.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
         heightLabel.setForeground(new java.awt.Color(255, 255, 255));
 
         ageLabel.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
         ageLabel.setForeground(new java.awt.Color(255, 255, 255));
 
         genderLabel.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
         genderLabel.setForeground(new java.awt.Color(255, 255, 255));
 
         weightLabel.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
         weightLabel.setForeground(new java.awt.Color(255, 255, 255));
 
         medicalConditionScrollPane.setBackground(new java.awt.Color(0, 0, 0));
         medicalConditionScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         medicalConditionScrollPane.setForeground(new java.awt.Color(255, 255, 255));
         medicalConditionScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         medicalConditionScrollPane.setOpaque(false);
 
         medicalConditionTextArea.setBackground(new java.awt.Color(0, 0, 0));
         medicalConditionTextArea.setColumns(20);
         medicalConditionTextArea.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
         medicalConditionTextArea.setForeground(new java.awt.Color(255, 255, 255));
         medicalConditionTextArea.setRows(5);
         medicalConditionTextArea.setCaretColor(new java.awt.Color(255, 255, 255));
         medicalConditionScrollPane.setViewportView(medicalConditionTextArea);
 
         javax.swing.GroupLayout generalInfoPanelLayout = new javax.swing.GroupLayout(generalInfoPanel);
         generalInfoPanel.setLayout(generalInfoPanelLayout);
         generalInfoPanelLayout.setHorizontalGroup(
             generalInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(generalInfoPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(generalInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(medicalConditionScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, generalInfoPanelLayout.createSequentialGroup()
                         .addGroup(generalInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(generalInfoPanelLayout.createSequentialGroup()
                                 .addComponent(weightTitleLabel)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(weightLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                             .addGroup(generalInfoPanelLayout.createSequentialGroup()
                                 .addComponent(ageTitleLabel)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(ageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(generalInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(heightTitleLabel)
                             .addComponent(genderTitleLabel))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(generalInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(genderLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                             .addComponent(heightLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                     .addGroup(generalInfoPanelLayout.createSequentialGroup()
                         .addComponent(nameTitleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(nameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                     .addGroup(generalInfoPanelLayout.createSequentialGroup()
                         .addComponent(patientNumberTitleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(patientNumberLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                     .addGroup(generalInfoPanelLayout.createSequentialGroup()
                         .addComponent(medicalCondtionTitleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 0, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         generalInfoPanelLayout.setVerticalGroup(
             generalInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(generalInfoPanelLayout.createSequentialGroup()
                 .addGap(32, 32, 32)
                 .addGroup(generalInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(nameTitleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(nameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(generalInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(patientNumberTitleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(patientNumberLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(generalInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(genderLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(generalInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(ageTitleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(genderTitleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(ageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(generalInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(generalInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(heightTitleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(weightTitleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(heightLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(weightLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(medicalCondtionTitleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(medicalConditionScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         ecgPanel.setBorder(javax.swing.BorderFactory.createLineBorder(ECG_COLOR));
         ecgPanel.setOpaque(false);
         ecgPanel.setPreferredSize(new java.awt.Dimension(240, 100));
 
         ecgTitleLabel.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
         ecgTitleLabel.setForeground(ECG_COLOR);
         ecgTitleLabel.setText("ECG/Min");
 
         ecgValueLabel.setBackground(new java.awt.Color(255, 0, 0));
         ecgValueLabel.setFont(new java.awt.Font("Arial", 1, 48)); // NOI18N
         ecgValueLabel.setForeground(ECG_COLOR);
         ecgValueLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
 
         javax.swing.GroupLayout ecgPanelLayout = new javax.swing.GroupLayout(ecgPanel);
         ecgPanel.setLayout(ecgPanelLayout);
         ecgPanelLayout.setHorizontalGroup(
             ecgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(ecgPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(ecgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(ecgPanelLayout.createSequentialGroup()
                         .addComponent(ecgTitleLabel)
                         .addGap(0, 0, Short.MAX_VALUE))
                     .addComponent(ecgValueLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         ecgPanelLayout.setVerticalGroup(
             ecgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(ecgPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(ecgTitleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(ecgValueLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         gluPanel.setBorder(javax.swing.BorderFactory.createLineBorder(GLU_COLOR));
         gluPanel.setOpaque(false);
         gluPanel.setPreferredSize(new java.awt.Dimension(240, 100));
 
         gluTitleLabel.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
         gluTitleLabel.setForeground(GLU_COLOR);
         gluTitleLabel.setText("GLU/mM");
 
         gluValueLabel.setFont(new java.awt.Font("Arial", 1, 48)); // NOI18N
         gluValueLabel.setForeground(GLU_COLOR);
         gluValueLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
 
         javax.swing.GroupLayout gluPanelLayout = new javax.swing.GroupLayout(gluPanel);
         gluPanel.setLayout(gluPanelLayout);
         gluPanelLayout.setHorizontalGroup(
             gluPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(gluPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(gluPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(gluPanelLayout.createSequentialGroup()
                         .addComponent(gluTitleLabel)
                         .addGap(0, 115, Short.MAX_VALUE))
                     .addComponent(gluValueLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         gluPanelLayout.setVerticalGroup(
             gluPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(gluPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(gluTitleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(gluValueLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGap(6, 6, 6))
         );
 
         currentStatePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 204, 153)));
         currentStatePanel.setOpaque(false);
 
         currentStateTitleLabel.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
         currentStateTitleLabel.setForeground(new java.awt.Color(255, 255, 255));
         currentStateTitleLabel.setText("Current State:");
 
         currentStateScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         currentStateScrollPane.setForeground(new java.awt.Color(255, 255, 255));
         currentStateScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         currentStateScrollPane.setOpaque(false);
 
         currentStateTextArea.setBackground(new java.awt.Color(0, 0, 0));
         currentStateTextArea.setColumns(20);
         currentStateTextArea.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
         currentStateTextArea.setForeground(new java.awt.Color(255, 255, 255));
         currentStateTextArea.setLineWrap(true);
         currentStateTextArea.setRows(3);
         currentStateTextArea.setCaretColor(new java.awt.Color(255, 255, 255));
         currentStateScrollPane.setViewportView(currentStateTextArea);
 
         javax.swing.GroupLayout currentStatePanelLayout = new javax.swing.GroupLayout(currentStatePanel);
         currentStatePanel.setLayout(currentStatePanelLayout);
         currentStatePanelLayout.setHorizontalGroup(
             currentStatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(currentStatePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(currentStatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(currentStatePanelLayout.createSequentialGroup()
                         .addComponent(currentStateTitleLabel)
                         .addGap(0, 0, Short.MAX_VALUE))
                     .addComponent(currentStateScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                 .addContainerGap())
         );
         currentStatePanelLayout.setVerticalGroup(
             currentStatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(currentStatePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(currentStateTitleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(currentStateScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         temperaturePanel.setBorder(javax.swing.BorderFactory.createLineBorder(TEMP_COLOR));
         temperaturePanel.setOpaque(false);
         temperaturePanel.setPreferredSize(new java.awt.Dimension(240, 120));
 
         tempTitleLabel.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
         tempTitleLabel.setForeground(TEMP_COLOR);
         tempTitleLabel.setText("TEMP/Celsius");
 
         tempValueLabel.setFont(new java.awt.Font("Arial", 1, 48)); // NOI18N
         tempValueLabel.setForeground(TEMP_COLOR);
         tempValueLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
 
         javax.swing.GroupLayout temperaturePanelLayout = new javax.swing.GroupLayout(temperaturePanel);
         temperaturePanel.setLayout(temperaturePanelLayout);
         temperaturePanelLayout.setHorizontalGroup(
             temperaturePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(temperaturePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(temperaturePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(temperaturePanelLayout.createSequentialGroup()
                         .addComponent(tempTitleLabel)
                         .addGap(0, 70, Short.MAX_VALUE))
                     .addComponent(tempValueLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         temperaturePanelLayout.setVerticalGroup(
             temperaturePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(temperaturePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(tempTitleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(13, 13, 13)
                 .addComponent(tempValueLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         nibpPanel.setBorder(javax.swing.BorderFactory.createLineBorder(NIBP_COLOR));
         nibpPanel.setOpaque(false);
         nibpPanel.setPreferredSize(new java.awt.Dimension(240, 100));
 
         nibpTitleLabel.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
         nibpTitleLabel.setForeground(NIBP_COLOR);
         nibpTitleLabel.setText("NIBP/mmHg");
 
         nibpValueLabel.setFont(new java.awt.Font("Arial", 1, 48)); // NOI18N
         nibpValueLabel.setForeground(NIBP_COLOR);
         nibpValueLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
 
         javax.swing.GroupLayout nibpPanelLayout = new javax.swing.GroupLayout(nibpPanel);
         nibpPanel.setLayout(nibpPanelLayout);
         nibpPanelLayout.setHorizontalGroup(
             nibpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(nibpPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(nibpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(nibpPanelLayout.createSequentialGroup()
                         .addComponent(nibpTitleLabel)
                         .addGap(0, 0, Short.MAX_VALUE))
                     .addComponent(nibpValueLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))
                 .addContainerGap())
         );
         nibpPanelLayout.setVerticalGroup(
             nibpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(nibpPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(nibpTitleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(nibpValueLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         javax.swing.GroupLayout pmsInnerPanelLayout = new javax.swing.GroupLayout(pmsInnerPanel);
         pmsInnerPanel.setLayout(pmsInnerPanelLayout);
         pmsInnerPanelLayout.setHorizontalGroup(
             pmsInnerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pmsInnerPanelLayout.createSequentialGroup()
                 .addGroup(pmsInnerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(generalInfoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(currentStatePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pmsInnerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(gluPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
                     .addComponent(ecgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
                     .addComponent(nibpPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
                     .addComponent(temperaturePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)))
         );
         pmsInnerPanelLayout.setVerticalGroup(
             pmsInnerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pmsInnerPanelLayout.createSequentialGroup()
                 .addGroup(pmsInnerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addGroup(pmsInnerPanelLayout.createSequentialGroup()
                         .addComponent(ecgPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(nibpPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(gluPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE))
                     .addComponent(generalInfoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pmsInnerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(currentStatePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(temperaturePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)))
         );
 
         javax.swing.GroupLayout pmsPanelLayout = new javax.swing.GroupLayout(pmsPanel);
         pmsPanel.setLayout(pmsPanelLayout);
         pmsPanelLayout.setHorizontalGroup(
             pmsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pmsPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(pmsInnerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                 .addContainerGap())
         );
         pmsPanelLayout.setVerticalGroup(
             pmsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pmsPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(pmsInnerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(pmsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(pmsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
 	
 		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
 		int X = (screen.width / 2) - (getWidth() / 2); // Center horizontally.
 		int Y = (screen.height / 2) - (getHeight() / 2); // Center vertically.
 
 		setLocation(X, Y);
 
         pack();
 	}
 
 	@Override
 	public void displayHeartRate(double heartRate, boolean warning) {
 		ecgValueLabel.setText(Integer.toString((int)heartRate));
 		if(warning) {
 			ecgPanel.setBorder(javax.swing.BorderFactory.createLineBorder(WARNING_COLOR, 5));
 		}
 		else {
 			ecgPanel.setBorder(javax.swing.BorderFactory.createLineBorder(ECG_COLOR));
 		}
 	}
 
 	@Override
 	public void displayTemperature(double temperature, boolean warning) {
		tempValueLabel.setText(Integer.toString((int)temperature));
 		if(warning) {
 			temperaturePanel.setBorder(javax.swing.BorderFactory.createLineBorder(WARNING_COLOR, 5));
 		}
 		else {
 			temperaturePanel.setBorder(javax.swing.BorderFactory.createLineBorder(TEMP_COLOR));
 		}
 	}
 
 	@Override
 	public void displayBloodPressure(double highPressure, double lowPressure, boolean warning) {
 		nibpValueLabel.setText(Integer.toString((int)highPressure) + "/" + Integer.toString((int)lowPressure));
 		if(warning) {
 			nibpPanel.setBorder(javax.swing.BorderFactory.createLineBorder(WARNING_COLOR, 5));
 		}
 		else {
 			nibpPanel.setBorder(javax.swing.BorderFactory.createLineBorder(NIBP_COLOR));
 		}
 	}
 
 	@Override
 	public void displayBloodGlucoseLevel(double bloodGlucoseLevel, boolean warning) {
		gluValueLabel.setText(Integer.toString((int)bloodGlucoseLevel));
 		if(warning) {
 			gluPanel.setBorder(javax.swing.BorderFactory.createLineBorder(WARNING_COLOR, 5));
 		}
 		else {
 			gluPanel.setBorder(javax.swing.BorderFactory.createLineBorder(GLU_COLOR));
 		}
 	}
 
 	@Override
 	public void displayInfo(String stateInfo) {
 		currentStateTextArea.setText(stateInfo);
 	}
 }
