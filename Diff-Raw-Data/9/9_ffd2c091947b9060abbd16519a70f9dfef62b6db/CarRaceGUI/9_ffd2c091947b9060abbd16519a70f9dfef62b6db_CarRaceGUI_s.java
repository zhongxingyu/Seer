 package test_cr;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.PrintStream;
 import java.util.ResourceBundle;
 import javax.swing.*;
 import javax.swing.border.TitledBorder;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.vecmath.Vector3d;
 
 import jp.sourceforge.acerola3d.a3.*;
 import com.github.hiuprocon.pve.car.*;
 import com.github.hiuprocon.pve.ui.*;
 
 class CarRaceGUI extends JFrame implements ActionListener {
     private static final long serialVersionUID = 1L;
     CarRaceImpl impl;
     SimpleIDE ide;
     A3Canvas mainCanvas;
     A3CSController a3csController;
     A3SubCanvas carCanvas;
     double cameraDist = 6.7;
     JTextField carClassTF;
     JLabel loadFromL;
     JButton changeCPB;
     JButton confB;
     JButton ideB;
     JLabel generalInfoL;
     JCheckBox fastForwardCB;
     JButton startB;
     JButton pauseB;
     JButton stopB;
     //JLabel carEnergyL;
     JTextArea stdOutTA;
     JTextAreaOutputStream out;
     //i18n
     ResourceBundle messages;
 
     String i18n(String s) {
         return messages.getString(s);
     }
     
     CarRaceGUI(CarRaceImpl i,String carClass) {
         super("CarRace");
         PropertiesControl pc = new PropertiesControl("UTF-8");
        messages = ResourceBundle.getBundle("Messages",pc);
         System.out.println("i18n: test="+i18n("test"));
 
         impl = i;
         ide = new SimpleIDE(this);
 
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         this.setLayout(new BorderLayout());
         VBox baseBox = new VBox();
         this.add(baseBox,BorderLayout.CENTER);
 
         HBox controlBox = new HBox();
         baseBox.myAdd(controlBox,0);
         HBox classNameBox = new HBox();
         classNameBox.setBorder(new TitledBorder(i18n("main.carClassname")));
         controlBox.myAdd(classNameBox,1);
         carClassTF = new JTextField(carClass,20);
         classNameBox.myAdd(carClassTF,1);
         HBox loadFromBox = new HBox();
         loadFromBox.setBorder(new TitledBorder(i18n("main.loadFrom")));
         controlBox.myAdd(loadFromBox,0);
         loadFromL = new JLabel("loadFrom???");
         loadFromBox.myAdd(loadFromL,0);
 //changeCPB = new JButton(new String[]{"システムのみ","作業フォルダ","JARファイル"});
         changeCPB = new JButton(i18n("main.change"));
         changeCPB.addActionListener(this);
         loadFromBox.myAdd(changeCPB,0);
         VBox controlBox1 = new VBox();
         controlBox.myAdd(controlBox1,0);
         confB = new JButton(i18n("main.settings"));
         confB.addActionListener(this);
         controlBox1.myAdd(confB,0);
         ideB = new JButton(i18n("main.programming"));
         ideB.addActionListener(this);
         controlBox1.myAdd(ideB,0);
         VBox controlBox2 = new VBox();
         controlBox.myAdd(controlBox2,0);
         HBox generalInfoBox = new HBox();
         controlBox2.myAdd(generalInfoBox,0);
         fastForwardCB = new JCheckBox(i18n("main.fastForward"));
         fastForwardCB.addActionListener(this);
         generalInfoBox.myAdd(fastForwardCB,0);
         generalInfoL = new JLabel(i18n("main.time"));
         generalInfoBox.myAdd(generalInfoL,1);
         HBox mainButtonsBox = new HBox();
         controlBox2.myAdd(mainButtonsBox,0);
         startB = new JButton(i18n("main.start"));
         startB.addActionListener(this);
         mainButtonsBox.myAdd(startB,1);
         pauseB = new JButton(i18n("main.pause"));
         pauseB.addActionListener(this);
         mainButtonsBox.myAdd(pauseB,1);
         stopB = new JButton(i18n("main.stop"));
         stopB.addActionListener(this);
         mainButtonsBox.myAdd(stopB,1);
 
         HBox displayBox = new HBox();
         baseBox.myAdd(displayBox,1);
         mainCanvas = A3Canvas.createA3Canvas(400,400);
         mainCanvas.setCameraLocImmediately(0.0,150.0,0.0);
         mainCanvas.setCameraLookAtPointImmediately(-50.0,0.0,1.0);
         a3csController = new A3CSController(150.0);
         mainCanvas.setA3Controller(a3csController);
         //mainCanvas.setNavigationMode(A3CanvasInterface.NaviMode.SIMPLE,150.0);
         displayBox.myAdd(mainCanvas,1);
         VBox subBox = new VBox();
         displayBox.myAdd(subBox,1);
         HBox carBox = new HBox();
         subBox.myAdd(carBox,1);
         carCanvas = A3SubCanvas.createA3SubCanvas(200,200);
         carBox.myAdd(carCanvas,1);
         //VBox carInfoBox = new VBox();
         //carBox.myAdd(carInfoBox,0);
         //carInfoBox.myAdd(new JLabel("CAR:"),0);
         //carEnergyL = new JLabel("Energy: 000");
         //carInfoBox.myAdd(carEnergyL,0);
         
         stdOutTA = new JTextArea(10,40);
         stdOutTA.setEditable(false);
         JScrollPane sp = new JScrollPane(stdOutTA);
         sp.setMinimumSize(new Dimension(100,100));
         baseBox.myAdd(sp,0);
         out = new JTextAreaOutputStream(stdOutTA,System.out);
         PrintStream ps = new PrintStream(out,true);
         System.setOut(ps);
         System.setErr(ps);
         //this.pack();
         //this.setVisible(true);
 
         updateLoadFrom();
     }
 
     void setCar(CarBase c) {
         carCanvas.setAvatar(c.car.a3);
         Vector3d lookAt = new Vector3d(0.0,0.0,6.0);
         Vector3d camera = new Vector3d(0.0,3.0,-6.0);
         camera.normalize();
         camera.scale(cameraDist);
         Vector3d up = new Vector3d(0.0,1.0,0.0);
         carCanvas.setNavigationMode(A3CanvasInterface.NaviMode.CHASE,lookAt,camera,up,10.0);
     }
 
     void updateCarInfo(CarBase c) {
         //carEnergyL.setText("Energy: "+c.energy);
     }
     void updateTime(double t) {
         generalInfoL.setText(i18n("main.time")+String.format("%4.2f",t));
     }
     @Override
     public void actionPerformed(ActionEvent ae) {
         Object s = ae.getSource();
         if (s==startB) {
             impl.startBattle();
         } else if (s==pauseB) {
             impl.pauseBattle();
         } else if (s==stopB) {
             impl.stopBattle();
         } else if (s==changeCPB) {
             changeCP();
         } else if (s==confB) {
             conf();
         } else if (s==ideB) {
             ide();
         } else if (s==fastForwardCB) {
             impl.fastForward(fastForwardCB.isSelected());
         }
     }
     void changeCP() {
         Object[] possibleValues = {
             i18n("ccp.onlySystem"),
             i18n("ccp.workingFolder"),
             i18n("ccp.jarFile") };
         Object iniVal;
         if (impl.carClasspath.equals("System"))
             iniVal = possibleValues[0];
         else if (impl.carClasspath.equals("IDE"))
             iniVal = possibleValues[1];
         else
             iniVal = possibleValues[2];
         String selectedValue = (String)JOptionPane.showInputDialog(this,
             i18n("ccp.whereLoadFrom"), "Input", JOptionPane.INFORMATION_MESSAGE,
                 null, possibleValues, iniVal);
         if (selectedValue==null)
             return;
         if (selectedValue.equals(possibleValues[0]))
             impl.changeCP("System");
         else if (selectedValue.equals(possibleValues[1]))
             if (impl.workDir==null) {
                 JOptionPane.showMessageDialog(this,i18n("ccp.warning1"));
             } else {
                 impl.changeCP("IDE");
             }
         else {
             JFileChooser chooser = new JFileChooser();
             FileNameExtensionFilter filter = new FileNameExtensionFilter(
                 "JAR File", "jar");
             chooser.setFileFilter(filter);
             int returnVal = chooser.showOpenDialog(this);
             if(returnVal == JFileChooser.APPROVE_OPTION) {
                 File f = chooser.getSelectedFile();
                 String url = f.toURI().toString();
                 url = "jar:"+url+"!/";
                 impl.changeCP(url);
             }
         }
         updateLoadFrom();
     }
     void updateLoadFrom() {
         if (impl.carClasspath.equals("System"))
             loadFromL.setText(i18n("ccp.onlySystem"));
         else if (impl.carClasspath.equals("IDE"))
             loadFromL.setText(i18n("ccp.workingFolder"));
         else
             loadFromL.setText(i18n("ccp.jarFile"));
     }
     void setParamEditable(boolean b) {
         carClassTF.setEditable(b);
         changeCPB.setEnabled(b);
         confB.setEnabled(b);
         ideB.setEnabled(b);
     }
     void conf() {
         Object[] possibleValues = {
             i18n("conf.workingFolder"), i18n("conf.cameraTracking") };
         String selectedValue = (String)JOptionPane.showInputDialog(this,
                 i18n("conf.whichOption"), "Input",
                 JOptionPane.INFORMATION_MESSAGE, null,
                 possibleValues, possibleValues[0]);
         if (selectedValue==null)
             return;
         if (selectedValue.equals(possibleValues[0])) {
             File iniF = null;
             if (impl.workDir!=null) {
                 iniF = new File(impl.workDir);
             }
             JFileChooser chooser = new JFileChooser();
             chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
             if (iniF!=null)
                 chooser.setSelectedFile(iniF);
             int returnVal = chooser.showOpenDialog(this);
             if(returnVal == JFileChooser.APPROVE_OPTION) {
                 File f = chooser.getSelectedFile();
                 impl.setWorkDir(f.getAbsolutePath());
                 impl.setWorkDirURL(f.toURI().toString());
             } else {
                 //変更しないことにした
                 //impl.setWorkDir(null);
                 //impl.setWorkDirURL(null);
             }
         } else if (selectedValue.equals(possibleValues[1])) {
             try {
                 String val = JOptionPane.showInputDialog(this,i18n("conf.distance"),"6.7");
                 if (val==null) return;
                 cameraDist = Double.parseDouble(val);
                 Vector3d lookAt = new Vector3d(0.0,0.0,6.0);
                 Vector3d camera = new Vector3d(0.0,3.0,-6.0);
                 camera.normalize();camera.scale(cameraDist);
                 Vector3d up = new Vector3d(0.0,1.0,0.0);
                 carCanvas.setNavigationMode(A3CanvasInterface.NaviMode.CHASE,lookAt,camera,up,10.0);
                 val = JOptionPane.showInputDialog(this,i18n("conf.interpolation"),"0.1");
                 if (val==null) return;
                 double dVal = Double.parseDouble(val);
                 carCanvas.setCameraInterpolateRatio(dVal);
             } catch(Exception e) {
                 e.printStackTrace();
             }
         }
     }
     void ide() {
         ide.popup(impl.workDir);
     }
     void clearTA() {
         stdOutTA.setText("");
     }
     void clearCamera() {
         mainCanvas.setCameraLocImmediately(0.0,150.0,0.0);
         mainCanvas.setCameraLookAtPointImmediately(-50.0,0.0,1.0);
         carCanvas.setCameraLocImmediately(-10,10,0);
         carCanvas.setCameraLookAtPointImmediately(0,0,0);
     }
 }
