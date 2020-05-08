 package gui.processing;
 
 import java.awt.Dimension;
 import java.awt.image.BufferedImage;
 
 import javax.swing.SwingWorker;
 
 
 import controller.MainController;
 
 
 import dk.dtu.imm.c02343.grp4.dto.interfaces.ILocations;
 import dk.dtu.imm.c02343.grp4.imageprocessing.testimageprocessing.ImagePanel;
 import gui.FramePlaceHolder;
 
 /**
  *
  * @author Morten Hulvej
  */
 public class ProcessingFrame extends javax.swing.JInternalFrame {
 		
 	
     /** Creates new form ImageFrame */
     public ProcessingFrame() {
         initComponents();
         FramePlaceHolder.setProcessingFrame(this);
     }
     @Deprecated
     private void initListeners() {
 		
     	stateChangeListener = new RobotColorChangeListener();
 		// TODO init. sliders p� en smartere m�de..
 
 		// min-sliders
 		minRedSlider.addChangeListener(stateChangeListener);
 		minRedSlider1.addChangeListener(stateChangeListener);
 		minRedSlider2.addChangeListener(stateChangeListener);
 		minRedSlider3.addChangeListener(stateChangeListener);
 		
 		minGreenSlider.addChangeListener(stateChangeListener);
 		minGreenSlider1.addChangeListener(stateChangeListener);
 		minGreenSlider2.addChangeListener(stateChangeListener);
 		minGreenSlider3.addChangeListener(stateChangeListener);
 		
 		minBlueSlider.addChangeListener(stateChangeListener);
 		minBlueSlider1.addChangeListener(stateChangeListener);
 		minBlueSlider2.addChangeListener(stateChangeListener);
 		minBlueSlider3.addChangeListener(stateChangeListener);
 		
 		// max-sliders
 		maxRedSlider.addChangeListener(stateChangeListener);
 		maxRedSlider1.addChangeListener(stateChangeListener);
 		maxRedSlider2.addChangeListener(stateChangeListener);
 		maxRedSlider3.addChangeListener(stateChangeListener);
 		
 		maxGreenSlider.addChangeListener(stateChangeListener);
 		maxGreenSlider1.addChangeListener(stateChangeListener);
 		maxGreenSlider2.addChangeListener(stateChangeListener);
 		maxGreenSlider3.addChangeListener(stateChangeListener);
 		
 		maxBlueSlider.addChangeListener(stateChangeListener);
 		maxBlueSlider1.addChangeListener(stateChangeListener);
 		maxBlueSlider2.addChangeListener(stateChangeListener);
 		maxBlueSlider3.addChangeListener(stateChangeListener);
 		
 		bufzoneSlider.addChangeListener(stateChangeListener);
 		
 		
 		updateIntervalSpinner.addChangeListener(stateChangeListener);
 		
 	}
 
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
         bindingGroup = new org.jdesktop.beansbinding.BindingGroup();
 
         robot1SettingsPanel = new javax.swing.JTabbedPane();
         robotFrontThresholdPanel = new javax.swing.JPanel();
         minRGBpanel = new javax.swing.JPanel();
         minRedSlider = new javax.swing.JSlider();
         minGreenSlider = new javax.swing.JSlider();
         minBlueSlider = new javax.swing.JSlider();
         minRVal = new javax.swing.JLabel();
         minGVal = new javax.swing.JLabel();
         minBVal = new javax.swing.JLabel();
         maxRGBpanel = new javax.swing.JPanel();
         maxRedSlider = new javax.swing.JSlider();
         maxBlueSlider = new javax.swing.JSlider();
         maxGreenSlider = new javax.swing.JSlider();
         maxRVal = new javax.swing.JLabel();
         maxGVal = new javax.swing.JLabel();
         maxBVal = new javax.swing.JLabel();
         robotBackThresholdPanel = new javax.swing.JPanel();
         minRGBpanel1 = new javax.swing.JPanel();
         minRedSlider1 = new javax.swing.JSlider();
         minGreenSlider1 = new javax.swing.JSlider();
         minBlueSlider1 = new javax.swing.JSlider();
         minRVal1 = new javax.swing.JLabel();
         minGVal1 = new javax.swing.JLabel();
         minBVal1 = new javax.swing.JLabel();
         maxRGBpanel1 = new javax.swing.JPanel();
         maxRedSlider1 = new javax.swing.JSlider();
         maxBlueSlider1 = new javax.swing.JSlider();
         maxGreenSlider1 = new javax.swing.JSlider();
         maxRVal1 = new javax.swing.JLabel();
         maxGVal1 = new javax.swing.JLabel();
         maxBVal1 = new javax.swing.JLabel();
         cakesThresholdPanel = new javax.swing.JPanel();
         minRGBpanel2 = new javax.swing.JPanel();
         minRedSlider2 = new javax.swing.JSlider();
         minGreenSlider2 = new javax.swing.JSlider();
         minBlueSlider2 = new javax.swing.JSlider();
         minRVal2 = new javax.swing.JLabel();
         minGVal2 = new javax.swing.JLabel();
         minBVal2 = new javax.swing.JLabel();
         maxRGBpanel2 = new javax.swing.JPanel();
         maxRedSlider2 = new javax.swing.JSlider();
         maxBlueSlider2 = new javax.swing.JSlider();
         maxGreenSlider2 = new javax.swing.JSlider();
         maxRVal2 = new javax.swing.JLabel();
         maxGVal2 = new javax.swing.JLabel();
         maxBVal2 = new javax.swing.JLabel();
         obstacleThresholdPanel = new javax.swing.JPanel();
         minRGBpanel3 = new javax.swing.JPanel();
         minRedSlider3 = new javax.swing.JSlider();
         minGreenSlider3 = new javax.swing.JSlider();
         minBlueSlider3 = new javax.swing.JSlider();
         minRVal3 = new javax.swing.JLabel();
         minGVal3 = new javax.swing.JLabel();
         minBVal3 = new javax.swing.JLabel();
         maxRGBpanel3 = new javax.swing.JPanel();
         maxRedSlider3 = new javax.swing.JSlider();
         maxBlueSlider3 = new javax.swing.JSlider();
         maxGreenSlider3 = new javax.swing.JSlider();
         maxRVal3 = new javax.swing.JLabel();
         maxGVal3 = new javax.swing.JLabel();
         maxBVal3 = new javax.swing.JLabel();
         miscPanel = new javax.swing.JPanel();
         bufferzonePanel = new javax.swing.JPanel();
         bufzoneSlider = new javax.swing.JSlider();
         imageProcessPanel = new javax.swing.JPanel();
         imagePanel = new javax.swing.JPanel();
         processedImagePanel = new javax.swing.JPanel();
         imageToolbar = new javax.swing.JToolBar();
         webcamBtn = new javax.swing.JButton();
         pauseTBtn = new javax.swing.JToggleButton();
         updateIntvlabel = new javax.swing.JLabel();
         updateIntervalSpinner = new javax.swing.JSpinner();
         jSeparator1 = new javax.swing.JToolBar.Separator();
 
         robot1SettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Threshold Settings"));
 
         minRGBpanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Min RGB"));
 
         minRedSlider.setBackground(new java.awt.Color(255, 0, 0));
         minRedSlider.setMaximum(255);
         minRedSlider.setMinorTickSpacing(1);
         minRedSlider.setValue(0);
 
         minGreenSlider.setBackground(new java.awt.Color(0, 255, 0));
         minGreenSlider.setMaximum(255);
         minGreenSlider.setMinorTickSpacing(1);
         minGreenSlider.setValue(70);
 
         minBlueSlider.setBackground(new java.awt.Color(0, 0, 255));
         minBlueSlider.setMaximum(255);
         minBlueSlider.setMinorTickSpacing(1);
         minBlueSlider.setValue(0);
 
         org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, minRedSlider, org.jdesktop.beansbinding.ELProperty.create("${value}"), minRVal, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, minGreenSlider, org.jdesktop.beansbinding.ELProperty.create("${value}"), minGVal, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, minBlueSlider, org.jdesktop.beansbinding.ELProperty.create("${value}"), minBVal, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         javax.swing.GroupLayout minRGBpanelLayout = new javax.swing.GroupLayout(minRGBpanel);
         minRGBpanel.setLayout(minRGBpanelLayout);
         minRGBpanelLayout.setHorizontalGroup(
             minRGBpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(minRGBpanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(minRGBpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(minBlueSlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(minGreenSlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(minRedSlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(minRGBpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(minRVal)
                     .addComponent(minGVal)
                     .addComponent(minBVal))
                 .addContainerGap(34, Short.MAX_VALUE))
         );
         minRGBpanelLayout.setVerticalGroup(
             minRGBpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(minRGBpanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(minRGBpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(minRedSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(minRVal))
                 .addGroup(minRGBpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(minGreenSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(minGVal))
                 .addGroup(minRGBpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(minBVal)
                     .addComponent(minBlueSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(17, Short.MAX_VALUE))
         );
 
         minRGBpanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {minBlueSlider, minGreenSlider, minRedSlider});
 
         maxRGBpanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Max RGB"));
 
         maxRedSlider.setBackground(new java.awt.Color(255, 0, 0));
         maxRedSlider.setMaximum(255);
         maxRedSlider.setMinorTickSpacing(1);
         maxRedSlider.setValue(70);
 
         maxBlueSlider.setBackground(new java.awt.Color(0, 0, 255));
         maxBlueSlider.setMaximum(255);
         maxBlueSlider.setMinorTickSpacing(1);
         maxBlueSlider.setValue(90);
 
         maxGreenSlider.setBackground(new java.awt.Color(0, 255, 0));
         maxGreenSlider.setMaximum(255);
         maxGreenSlider.setMinorTickSpacing(1);
         maxGreenSlider.setValue(255);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, maxRedSlider, org.jdesktop.beansbinding.ELProperty.create("${value}"), maxRVal, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, maxGreenSlider, org.jdesktop.beansbinding.ELProperty.create("${value}"), maxGVal, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, maxBlueSlider, org.jdesktop.beansbinding.ELProperty.create("${value}"), maxBVal, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         javax.swing.GroupLayout maxRGBpanelLayout = new javax.swing.GroupLayout(maxRGBpanel);
         maxRGBpanel.setLayout(maxRGBpanelLayout);
         maxRGBpanelLayout.setHorizontalGroup(
             maxRGBpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(maxRGBpanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(maxRGBpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(maxBlueSlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(maxGreenSlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(maxRedSlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(maxRGBpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(maxRVal)
                     .addComponent(maxGVal)
                     .addComponent(maxBVal))
                 .addContainerGap(28, Short.MAX_VALUE))
         );
         maxRGBpanelLayout.setVerticalGroup(
             maxRGBpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(maxRGBpanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(maxRGBpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(maxRedSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(maxRVal))
                 .addGroup(maxRGBpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(maxGreenSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(maxGVal))
                 .addGroup(maxRGBpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(maxBVal)
                     .addComponent(maxBlueSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(22, 22, 22))
         );
 
         javax.swing.GroupLayout robotFrontThresholdPanelLayout = new javax.swing.GroupLayout(robotFrontThresholdPanel);
         robotFrontThresholdPanel.setLayout(robotFrontThresholdPanelLayout);
         robotFrontThresholdPanelLayout.setHorizontalGroup(
             robotFrontThresholdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(robotFrontThresholdPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(robotFrontThresholdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                     .addComponent(maxRGBpanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(minRGBpanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap(35, Short.MAX_VALUE))
         );
         robotFrontThresholdPanelLayout.setVerticalGroup(
             robotFrontThresholdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(robotFrontThresholdPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(minRGBpanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(maxRGBpanel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(116, Short.MAX_VALUE))
         );
 
         robot1SettingsPanel.addTab("Front", robotFrontThresholdPanel);
 
         minRGBpanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Min RGB"));
 
         minRedSlider1.setBackground(new java.awt.Color(255, 0, 0));
         minRedSlider1.setMaximum(255);
         minRedSlider1.setMinorTickSpacing(1);
         minRedSlider1.setValue(0);
 
         minGreenSlider1.setBackground(new java.awt.Color(0, 255, 0));
         minGreenSlider1.setMaximum(255);
         minGreenSlider1.setMinorTickSpacing(1);
         minGreenSlider1.setValue(0);
 
         minBlueSlider1.setBackground(new java.awt.Color(0, 0, 255));
         minBlueSlider1.setMaximum(255);
         minBlueSlider1.setMinorTickSpacing(1);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, minRedSlider1, org.jdesktop.beansbinding.ELProperty.create("${value}"), minRVal1, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, minGreenSlider1, org.jdesktop.beansbinding.ELProperty.create("${value}"), minGVal1, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, minBlueSlider1, org.jdesktop.beansbinding.ELProperty.create("${value}"), minBVal1, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         javax.swing.GroupLayout minRGBpanel1Layout = new javax.swing.GroupLayout(minRGBpanel1);
         minRGBpanel1.setLayout(minRGBpanel1Layout);
         minRGBpanel1Layout.setHorizontalGroup(
             minRGBpanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(minRGBpanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(minRGBpanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(minBlueSlider1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(minGreenSlider1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(minRedSlider1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(minRGBpanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(minRVal1)
                     .addComponent(minGVal1)
                     .addComponent(minBVal1))
                 .addContainerGap(64, Short.MAX_VALUE))
         );
         minRGBpanel1Layout.setVerticalGroup(
             minRGBpanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(minRGBpanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(minRGBpanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(minRedSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(minRVal1))
                 .addGroup(minRGBpanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(minGreenSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(minGVal1))
                 .addGroup(minRGBpanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(minBVal1)
                     .addComponent(minBlueSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(17, Short.MAX_VALUE))
         );
 
         maxRGBpanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Max RGB"));
 
         maxRedSlider1.setBackground(new java.awt.Color(255, 0, 0));
         maxRedSlider1.setMaximum(255);
         maxRedSlider1.setMinorTickSpacing(1);
         maxRedSlider1.setValue(20);
 
         maxBlueSlider1.setBackground(new java.awt.Color(0, 0, 255));
         maxBlueSlider1.setMaximum(255);
         maxBlueSlider1.setMinorTickSpacing(1);
         maxBlueSlider1.setValue(255);
 
         maxGreenSlider1.setBackground(new java.awt.Color(0, 255, 0));
         maxGreenSlider1.setMaximum(255);
         maxGreenSlider1.setMinorTickSpacing(1);
         maxGreenSlider1.setValue(20);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, maxRedSlider1, org.jdesktop.beansbinding.ELProperty.create("${value}"), maxRVal1, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, maxGreenSlider1, org.jdesktop.beansbinding.ELProperty.create("${value}"), maxGVal1, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, maxBlueSlider1, org.jdesktop.beansbinding.ELProperty.create("${value}"), maxBVal1, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         javax.swing.GroupLayout maxRGBpanel1Layout = new javax.swing.GroupLayout(maxRGBpanel1);
         maxRGBpanel1.setLayout(maxRGBpanel1Layout);
         maxRGBpanel1Layout.setHorizontalGroup(
             maxRGBpanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(maxRGBpanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(maxRGBpanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(maxBlueSlider1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(maxGreenSlider1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(maxRedSlider1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(maxRGBpanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(maxRVal1)
                     .addComponent(maxGVal1)
                     .addComponent(maxBVal1))
                 .addContainerGap(58, Short.MAX_VALUE))
         );
         maxRGBpanel1Layout.setVerticalGroup(
             maxRGBpanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(maxRGBpanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(maxRGBpanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(maxRedSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(maxRVal1))
                 .addGroup(maxRGBpanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(maxGreenSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(maxGVal1))
                 .addGroup(maxRGBpanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(maxBVal1)
                     .addComponent(maxBlueSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(22, 22, 22))
         );
 
         javax.swing.GroupLayout robotBackThresholdPanelLayout = new javax.swing.GroupLayout(robotBackThresholdPanel);
         robotBackThresholdPanel.setLayout(robotBackThresholdPanelLayout);
         robotBackThresholdPanelLayout.setHorizontalGroup(
             robotBackThresholdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(robotBackThresholdPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(robotBackThresholdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                     .addComponent(minRGBpanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(maxRGBpanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         robotBackThresholdPanelLayout.setVerticalGroup(
             robotBackThresholdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(robotBackThresholdPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(minRGBpanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(maxRGBpanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(116, Short.MAX_VALUE))
         );
 
         robot1SettingsPanel.addTab("Back", robotBackThresholdPanel);
 
         minRGBpanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Min RGB"));
 
         minRedSlider2.setBackground(new java.awt.Color(255, 0, 0));
         minRedSlider2.setMaximum(255);
         minRedSlider2.setMinorTickSpacing(1);
         minRedSlider2.setValue(100);
 
         minGreenSlider2.setBackground(new java.awt.Color(0, 255, 0));
         minGreenSlider2.setMaximum(255);
         minGreenSlider2.setMinorTickSpacing(1);
         minGreenSlider2.setValue(0);
 
         minBlueSlider2.setBackground(new java.awt.Color(0, 0, 255));
         minBlueSlider2.setMaximum(255);
         minBlueSlider2.setMinorTickSpacing(1);
         minBlueSlider2.setValue(0);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, minRedSlider2, org.jdesktop.beansbinding.ELProperty.create("${value}"), minRVal2, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, minGreenSlider2, org.jdesktop.beansbinding.ELProperty.create("${value}"), minGVal2, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, minBlueSlider2, org.jdesktop.beansbinding.ELProperty.create("${value}"), minBVal2, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         javax.swing.GroupLayout minRGBpanel2Layout = new javax.swing.GroupLayout(minRGBpanel2);
         minRGBpanel2.setLayout(minRGBpanel2Layout);
         minRGBpanel2Layout.setHorizontalGroup(
             minRGBpanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(minRGBpanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(minRGBpanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(minBlueSlider2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(minGreenSlider2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(minRedSlider2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(minRGBpanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(minRVal2)
                     .addComponent(minGVal2)
                     .addComponent(minBVal2))
                 .addContainerGap(50, Short.MAX_VALUE))
         );
         minRGBpanel2Layout.setVerticalGroup(
             minRGBpanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(minRGBpanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(minRGBpanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(minRedSlider2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(minRVal2))
                 .addGroup(minRGBpanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(minGreenSlider2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(minGVal2))
                 .addGroup(minRGBpanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(minBVal2)
                     .addComponent(minBlueSlider2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(17, Short.MAX_VALUE))
         );
 
         maxRGBpanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Max RGB"));
 
         maxRedSlider2.setBackground(new java.awt.Color(255, 0, 0));
         maxRedSlider2.setMaximum(255);
         maxRedSlider2.setMinorTickSpacing(1);
         maxRedSlider2.setValue(255);
 
         maxBlueSlider2.setBackground(new java.awt.Color(0, 0, 255));
         maxBlueSlider2.setMaximum(255);
         maxBlueSlider2.setMinorTickSpacing(1);
         maxBlueSlider2.setValue(25);
 
         maxGreenSlider2.setBackground(new java.awt.Color(0, 255, 0));
         maxGreenSlider2.setMaximum(255);
         maxGreenSlider2.setMinorTickSpacing(1);
         maxGreenSlider2.setValue(25);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, maxRedSlider2, org.jdesktop.beansbinding.ELProperty.create("${value}"), maxRVal2, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, maxGreenSlider2, org.jdesktop.beansbinding.ELProperty.create("${value}"), maxGVal2, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, maxBlueSlider2, org.jdesktop.beansbinding.ELProperty.create("${value}"), maxBVal2, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         javax.swing.GroupLayout maxRGBpanel2Layout = new javax.swing.GroupLayout(maxRGBpanel2);
         maxRGBpanel2.setLayout(maxRGBpanel2Layout);
         maxRGBpanel2Layout.setHorizontalGroup(
             maxRGBpanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(maxRGBpanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(maxRGBpanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(maxBlueSlider2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(maxGreenSlider2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(maxRedSlider2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(maxRGBpanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(maxRVal2)
                     .addComponent(maxGVal2)
                     .addComponent(maxBVal2))
                 .addContainerGap(50, Short.MAX_VALUE))
         );
         maxRGBpanel2Layout.setVerticalGroup(
             maxRGBpanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(maxRGBpanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(maxRGBpanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(maxRedSlider2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(maxRVal2))
                 .addGroup(maxRGBpanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(maxGreenSlider2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(maxGVal2))
                 .addGroup(maxRGBpanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(maxBVal2)
                     .addComponent(maxBlueSlider2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(22, 22, 22))
         );
 
         javax.swing.GroupLayout cakesThresholdPanelLayout = new javax.swing.GroupLayout(cakesThresholdPanel);
         cakesThresholdPanel.setLayout(cakesThresholdPanelLayout);
         cakesThresholdPanelLayout.setHorizontalGroup(
             cakesThresholdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(cakesThresholdPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(cakesThresholdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                     .addComponent(minRGBpanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(maxRGBpanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap(13, Short.MAX_VALUE))
         );
         cakesThresholdPanelLayout.setVerticalGroup(
             cakesThresholdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(cakesThresholdPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(minRGBpanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(maxRGBpanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(116, Short.MAX_VALUE))
         );
 
         robot1SettingsPanel.addTab("Cakes", cakesThresholdPanel);
 
         minRGBpanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Min RGB"));
 
         minRedSlider3.setBackground(new java.awt.Color(255, 0, 0));
         minRedSlider3.setMaximum(255);
         minRedSlider3.setMinorTickSpacing(1);
         minRedSlider3.setValue(150);
 
         minGreenSlider3.setBackground(new java.awt.Color(0, 255, 0));
         minGreenSlider3.setMaximum(255);
         minGreenSlider3.setMinorTickSpacing(1);
         minGreenSlider3.setValue(150);
 
         minBlueSlider3.setBackground(new java.awt.Color(0, 0, 255));
         minBlueSlider3.setMaximum(255);
         minBlueSlider3.setMinorTickSpacing(1);
         minBlueSlider3.setValue(150);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, minRedSlider3, org.jdesktop.beansbinding.ELProperty.create("${value}"), minRVal3, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, minGreenSlider3, org.jdesktop.beansbinding.ELProperty.create("${value}"), minGVal3, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, minBlueSlider3, org.jdesktop.beansbinding.ELProperty.create("${value}"), minBVal3, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         javax.swing.GroupLayout minRGBpanel3Layout = new javax.swing.GroupLayout(minRGBpanel3);
         minRGBpanel3.setLayout(minRGBpanel3Layout);
         minRGBpanel3Layout.setHorizontalGroup(
             minRGBpanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(minRGBpanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(minRGBpanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(minBlueSlider3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(minGreenSlider3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(minRedSlider3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(minRGBpanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(minRVal3)
                     .addComponent(minGVal3)
                     .addComponent(minBVal3))
                 .addContainerGap(50, Short.MAX_VALUE))
         );
         minRGBpanel3Layout.setVerticalGroup(
             minRGBpanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(minRGBpanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(minRGBpanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(minRedSlider3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(minRVal3))
                 .addGroup(minRGBpanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(minGreenSlider3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(minGVal3))
                 .addGroup(minRGBpanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(minBVal3)
                     .addComponent(minBlueSlider3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(17, Short.MAX_VALUE))
         );
 
         maxRGBpanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Max RGB"));
 
         maxRedSlider3.setBackground(new java.awt.Color(255, 0, 0));
         maxRedSlider3.setMaximum(255);
         maxRedSlider3.setMinorTickSpacing(1);
         maxRedSlider3.setValue(255);
 
         maxBlueSlider3.setBackground(new java.awt.Color(0, 0, 255));
         maxBlueSlider3.setMaximum(255);
         maxBlueSlider3.setMinorTickSpacing(1);
         maxBlueSlider3.setValue(255);
 
         maxGreenSlider3.setBackground(new java.awt.Color(0, 255, 0));
         maxGreenSlider3.setMaximum(255);
         maxGreenSlider3.setMinorTickSpacing(1);
         maxGreenSlider3.setValue(255);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, maxRedSlider3, org.jdesktop.beansbinding.ELProperty.create("${value}"), maxRVal3, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, maxGreenSlider3, org.jdesktop.beansbinding.ELProperty.create("${value}"), maxGVal3, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, maxBlueSlider3, org.jdesktop.beansbinding.ELProperty.create("${value}"), maxBVal3, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         javax.swing.GroupLayout maxRGBpanel3Layout = new javax.swing.GroupLayout(maxRGBpanel3);
         maxRGBpanel3.setLayout(maxRGBpanel3Layout);
         maxRGBpanel3Layout.setHorizontalGroup(
             maxRGBpanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(maxRGBpanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(maxRGBpanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(maxBlueSlider3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(maxGreenSlider3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(maxRedSlider3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(maxRGBpanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(maxRVal3)
                     .addComponent(maxGVal3)
                     .addComponent(maxBVal3))
                 .addContainerGap(50, Short.MAX_VALUE))
         );
         maxRGBpanel3Layout.setVerticalGroup(
             maxRGBpanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(maxRGBpanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(maxRGBpanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(maxRedSlider3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(maxRVal3))
                 .addGroup(maxRGBpanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(maxGreenSlider3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(maxGVal3))
                 .addGroup(maxRGBpanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(maxBVal3)
                     .addComponent(maxBlueSlider3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(22, 22, 22))
         );
 
         javax.swing.GroupLayout obstacleThresholdPanelLayout = new javax.swing.GroupLayout(obstacleThresholdPanel);
         obstacleThresholdPanel.setLayout(obstacleThresholdPanelLayout);
         obstacleThresholdPanelLayout.setHorizontalGroup(
             obstacleThresholdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(obstacleThresholdPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(obstacleThresholdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                     .addComponent(minRGBpanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(maxRGBpanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap(13, Short.MAX_VALUE))
         );
         obstacleThresholdPanelLayout.setVerticalGroup(
             obstacleThresholdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(obstacleThresholdPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(minRGBpanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(maxRGBpanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(116, Short.MAX_VALUE))
         );
 
         robot1SettingsPanel.addTab("Obstacles", obstacleThresholdPanel);
 
         bufferzonePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Bufferzone"));
 
         bufzoneSlider.setMaximum(50);
         bufzoneSlider.setValue(30);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, bufzoneSlider, org.jdesktop.beansbinding.ELProperty.create("${value}"), bufzoneSlider, org.jdesktop.beansbinding.BeanProperty.create("toolTipText"));
         bindingGroup.addBinding(binding);
 
         javax.swing.GroupLayout bufferzonePanelLayout = new javax.swing.GroupLayout(bufferzonePanel);
         bufferzonePanel.setLayout(bufferzonePanelLayout);
         bufferzonePanelLayout.setHorizontalGroup(
             bufferzonePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, bufferzonePanelLayout.createSequentialGroup()
                 .addContainerGap(59, Short.MAX_VALUE)
                 .addComponent(bufzoneSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(32, 32, 32))
         );
         bufferzonePanelLayout.setVerticalGroup(
             bufferzonePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(bufferzonePanelLayout.createSequentialGroup()
                 .addGap(19, 19, 19)
                 .addComponent(bufzoneSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(23, Short.MAX_VALUE))
         );
 
         bufzoneSlider.getAccessibleContext().setAccessibleName("bufferzone");
 
         javax.swing.GroupLayout miscPanelLayout = new javax.swing.GroupLayout(miscPanel);
         miscPanel.setLayout(miscPanelLayout);
         miscPanelLayout.setHorizontalGroup(
             miscPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(miscPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(bufferzonePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
         miscPanelLayout.setVerticalGroup(
             miscPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(miscPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(bufferzonePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(286, Short.MAX_VALUE))
         );
 
         robot1SettingsPanel.addTab("Misc", miscPanel);
 
         setIconifiable(true);
         setResizable(true);
         setTitle("Processing");
         setNormalBounds(new java.awt.Rectangle(490, 10, 650, 528));
 
         imageProcessPanel.setLayout(new java.awt.GridBagLayout());
 
         imagePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Raw Image"));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         imageProcessPanel.add(imagePanel, gridBagConstraints);
 
         processedImagePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Processed Image"));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         imageProcessPanel.add(processedImagePanel, gridBagConstraints);
 
         imageToolbar.setRollover(true);
 
         webcamBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/webcam_icon.png"))); // NOI18N
         webcamBtn.setText("Start webcam & info feed");
         webcamBtn.setToolTipText("Start Webcam");
         webcamBtn.setFocusable(false);
         webcamBtn.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
         webcamBtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 webcamBtnActionPerformed(evt);
             }
         });
         imageToolbar.add(webcamBtn);
 
         pauseTBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/pause.png"))); // NOI18N
         pauseTBtn.setText("Pause Webcam");
         pauseTBtn.setToolTipText("Pause Webcam");
         pauseTBtn.setEnabled(false);
         pauseTBtn.setFocusable(false);
         pauseTBtn.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
         imageToolbar.add(pauseTBtn);
 
         updateIntvlabel.setLabelFor(updateIntervalSpinner);
         updateIntvlabel.setText("Update interval:");
         updateIntvlabel.setEnabled(false);
         imageToolbar.add(updateIntvlabel);
 
         updateIntervalSpinner.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.100000024f), Float.valueOf(0.05f), Float.valueOf(10.0f), Float.valueOf(0.1f)));
         updateIntervalSpinner.setMaximumSize(new java.awt.Dimension(50, 20));
         updateIntervalSpinner.setMinimumSize(new java.awt.Dimension(30, 20));
         updateIntervalSpinner.setPreferredSize(new java.awt.Dimension(50, 20));
         imageToolbar.add(updateIntervalSpinner);
         imageToolbar.add(jSeparator1);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(imageToolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 638, Short.MAX_VALUE)
             .addComponent(imageProcessPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 638, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(imageToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(imageProcessPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE))
         );
 
         bindingGroup.bind();
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void webcamBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webcamBtnActionPerformed
     	
     	new UpdateTask().execute();
    	new UpdateInfoTask().execute();
     	webcamBtn.setEnabled(false);
     	
     }//GEN-LAST:event_webcamBtnActionPerformed
     
     public void updateImagePanel() {
     	
     	imagePanel.removeAll();
         processedImagePanel.removeAll();
         
     	ILocations locations = MainController.getInstance().getInformation();
     	
     	// kilde billede
     	BufferedImage sourceImg = locations.getSourceImage();
     	
     	// behandlet billede
     	BufferedImage tileImg = locations.getTileImage();
 		
     	// Opret JFrame samt panel til input-billede
         srcImgPanel = new ImagePanel(sourceImg);
         srcImgPanel.setMinimumSize(new Dimension(sourceImg.getWidth(), sourceImg.getHeight()));
         imagePanel.add(srcImgPanel);
 
         // Opret JFrame samt panel til tile-billede
         tileImgPanel = new ImagePanel(tileImg);
         tileImgPanel.setMinimumSize(new Dimension(tileImg.getWidth(), tileImg.getHeight()));
         processedImagePanel.add(tileImgPanel);
 
         
         imagePanel.repaint();
         imagePanel.validate();
         processedImagePanel.repaint();
         processedImagePanel.validate();
         
 	}
         
     private ImagePanel srcImgPanel;
     private ImagePanel tileImgPanel;
     
     private RobotColorChangeListener stateChangeListener;
     
     boolean webcamRunning = true;
     boolean webcamFeedPaused = false;
     float time_slice = 0.1f;
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     javax.swing.JPanel bufferzonePanel;
     javax.swing.JSlider bufzoneSlider;
     javax.swing.JPanel cakesThresholdPanel;
     javax.swing.JPanel imagePanel;
     private javax.swing.JPanel imageProcessPanel;
     private javax.swing.JToolBar imageToolbar;
     private javax.swing.JToolBar.Separator jSeparator1;
     private javax.swing.JLabel maxBVal;
     private javax.swing.JLabel maxBVal1;
     private javax.swing.JLabel maxBVal2;
     private javax.swing.JLabel maxBVal3;
     javax.swing.JSlider maxBlueSlider;
     javax.swing.JSlider maxBlueSlider1;
     javax.swing.JSlider maxBlueSlider2;
     javax.swing.JSlider maxBlueSlider3;
     private javax.swing.JLabel maxGVal;
     private javax.swing.JLabel maxGVal1;
     private javax.swing.JLabel maxGVal2;
     private javax.swing.JLabel maxGVal3;
     javax.swing.JSlider maxGreenSlider;
     javax.swing.JSlider maxGreenSlider1;
     javax.swing.JSlider maxGreenSlider2;
     javax.swing.JSlider maxGreenSlider3;
     javax.swing.JPanel maxRGBpanel;
     javax.swing.JPanel maxRGBpanel1;
     javax.swing.JPanel maxRGBpanel2;
     javax.swing.JPanel maxRGBpanel3;
     private javax.swing.JLabel maxRVal;
     private javax.swing.JLabel maxRVal1;
     private javax.swing.JLabel maxRVal2;
     private javax.swing.JLabel maxRVal3;
     javax.swing.JSlider maxRedSlider;
     javax.swing.JSlider maxRedSlider1;
     javax.swing.JSlider maxRedSlider2;
     javax.swing.JSlider maxRedSlider3;
     private javax.swing.JLabel minBVal;
     private javax.swing.JLabel minBVal1;
     private javax.swing.JLabel minBVal2;
     private javax.swing.JLabel minBVal3;
     javax.swing.JSlider minBlueSlider;
     javax.swing.JSlider minBlueSlider1;
     javax.swing.JSlider minBlueSlider2;
     javax.swing.JSlider minBlueSlider3;
     private javax.swing.JLabel minGVal;
     private javax.swing.JLabel minGVal1;
     private javax.swing.JLabel minGVal2;
     private javax.swing.JLabel minGVal3;
     javax.swing.JSlider minGreenSlider;
     javax.swing.JSlider minGreenSlider1;
     javax.swing.JSlider minGreenSlider2;
     javax.swing.JSlider minGreenSlider3;
     javax.swing.JPanel minRGBpanel;
     javax.swing.JPanel minRGBpanel1;
     javax.swing.JPanel minRGBpanel2;
     javax.swing.JPanel minRGBpanel3;
     private javax.swing.JLabel minRVal;
     private javax.swing.JLabel minRVal1;
     private javax.swing.JLabel minRVal2;
     private javax.swing.JLabel minRVal3;
     javax.swing.JSlider minRedSlider;
     javax.swing.JSlider minRedSlider1;
     javax.swing.JSlider minRedSlider2;
     javax.swing.JSlider minRedSlider3;
     javax.swing.JPanel miscPanel;
     javax.swing.JPanel obstacleThresholdPanel;
     private javax.swing.JToggleButton pauseTBtn;
     private javax.swing.JPanel processedImagePanel;
     javax.swing.JTabbedPane robot1SettingsPanel;
     javax.swing.JPanel robotBackThresholdPanel;
     javax.swing.JPanel robotFrontThresholdPanel;
     private javax.swing.JSpinner updateIntervalSpinner;
     private javax.swing.JLabel updateIntvlabel;
     private javax.swing.JButton webcamBtn;
     private org.jdesktop.beansbinding.BindingGroup bindingGroup;
     // End of variables declaration//GEN-END:variables
 
     
     // Swing worker classes (for background work)
     
     class UpdateTask extends SwingWorker<Void, Void>{
 
 		@Override
 		protected Void doInBackground() throws Exception
 		{
 			try
 			{
 				while(true){
 					Thread.sleep(100);
 					updateImagePanel();
 				}
 			} catch (InterruptedException e)
 			{
 				e.printStackTrace();
 			}
 			
 			return null;
 		}
     	
     }
     class UpdateInfoTask extends SwingWorker<Void, Void>{
 
 		@Override
 		protected Void doInBackground() throws Exception
 		{
 			while(true){
 				FramePlaceHolder.getMinInfoFrame().updateAllInfo();
 			}
 		}
     	
     }
     
 }
