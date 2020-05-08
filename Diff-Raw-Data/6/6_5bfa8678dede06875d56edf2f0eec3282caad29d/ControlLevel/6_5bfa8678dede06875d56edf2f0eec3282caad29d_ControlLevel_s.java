 package gui;
 
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import org.xml.sax.InputSource;
 import controller.Robot;
 import controller.LevelHandler;
 import controller.IllegalDirectionException;
 import controller.LabyrinthField;
 import controller.TuringMachine;
 import controller.InstructionList;
 import controller.Instruction;
 import controller.SqlCommunicator;
 import controller.LabyrinthXpertMode;
 import controller.IntStringMap;
 
 public class ControlLevel {
     ControllerGuiCommunicator controller_gui_communicator_;
     ControlSimulation control_simulation_;
 
     private StatisticsPanel statistics_panel_;
     private PrintLabyrinth print_labyrinth_;
     private SpeedPanel speed_panel_;
     private JButton level_button_;
     private StatusPanel status_message_;
     private LevelHandler level_handler_;
     private Robot robot_;
     private static LabyrinthField labyrinth_field_;
     private TuringMachine turing_machine_;
     private InstructionList instruction_list_;
     private RunLab run_lab_;
     private Thread th_;
     private boolean wait_, wait_step_;
     private ControlLevel controllevel_;
     private LabyrinthXpertMode xpert_mode_;
     private int level_;
     private String source_;
 
     public ControlLevel(PrintLabyrinth print_labyrinth,
             StatisticsPanel statistics_panel, SpeedPanel speed_panel,
             JButton level_button, StatusPanel status_message,
             LevelHandler level_handler, IntStringMap mark_array) {
         statistics_panel_ = statistics_panel;
         print_labyrinth_ = print_labyrinth;
         speed_panel_ = speed_panel;
         speed_panel_.setControl(this);
         level_button_ = level_button;
         status_message_ = status_message;
         level_handler_ = level_handler;
         instruction_list_ = new InstructionList();
         wait_step_ = false;
         controllevel_ = this;
         xpert_mode_ = new LabyrinthXpertMode(mark_array);
         level_ = 0;
         source_ = new String("");
     }
 
     public void setCommunicationElements(
             ControllerGuiCommunicator controller_gui_communicator,
             ControlSimulation control_simulation) {
         controller_gui_communicator_ = controller_gui_communicator;
         control_simulation_ = control_simulation;
     }
 
     public void changedSpeed() {
         if (speed_panel_.getDelay() == 0) {
             SqlCommunicator.add_log("Schrittmodus aktiviert", 16);
             if (turing_machine_.checkStartPoint() == true)
                 startButton();
             else
                 stepButton();
             wait_ = false;
             wait_step_ = true;
         } else {
             SqlCommunicator.add_log("Geschwindigkeit geändert zu: "
                     + speed_panel_.getDelay(), 16);
             if (turing_machine_.checkStartPoint() == true)
                 startButton();
             else
                 stopButton();
             wait_ = false;
             wait_step_ = false;
         }
     }
 
     private void startButton() {
         ActionListener[] tmp = level_button_.getActionListeners();
         if (tmp.length > 0)
             level_button_.removeActionListener(tmp[0]);
         level_button_.setText("Neustart !");
         level_button_.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 labyrinth_field_ = level_handler_.getLabyrinthField();
                 robot_ = new Robot();
                 robot_.setPos(labyrinth_field_.getStartpointRow(),
                         labyrinth_field_.getStartpointCol());
                 try {
                     robot_.setStartDirection(labyrinth_field_
                             .getStartDirection());
                 } catch (IllegalDirectionException ex) {
                     System.err.println(ex.getMessage());
                 }
                 print_labyrinth_.setLabyrinthField(labyrinth_field_);
                 print_labyrinth_.setRobot(robot_);
                 print_labyrinth_.repaint();
                 statistics_panel_.setFieldcountDiamondcount(
                         labyrinth_field_.getNumWayCells() - 1,
                         labyrinth_field_.getNumDiamonds());
                 statistics_panel_.resetPanel();
                 status_message_.clear();
                 status_message_
                         .setNewFirstLine("   Marvin-10 programmieren...");
                 instruction_list_ = controller_gui_communicator_
                         .getInstructionListFromRuleTree();
                 turing_machine_ = new TuringMachine(print_labyrinth_,
                         labyrinth_field_, robot_);
                 turing_machine_.setInstructionList(instruction_list_);
                 InstructionList possible = turing_machine_
                         .getPossibleInstructions();
                 controller_gui_communicator_.newActiveInstructionList(possible);
                 SqlCommunicator.flush_log();
                 SqlCommunicator.add_log("Start Knopf getrückt", 4);
                 SqlCommunicator.add_log("Regelsatz neu eingelesen", 32);
                 String current_program = "Aktuelles Programm:\n";
                 for (int i = 0; i < instruction_list_.size(); i++) {
                     Instruction dummy = instruction_list_
                             .getInstructionAtPos(i);
                     current_program += "                          "
                             + dummy.toString();
                 }
                 SqlCommunicator.add_log(current_program, 64);
                 if (speed_panel_.getDelay() == 0)
                     stepButton();
                 else
                     stopButton();
 
                 wait_ = false;
 
                 if (th_ != null) {
                     run_lab_.finish();
                     th_.interrupt();
                     while (!th_.isInterrupted()) {
                         try {
                             th_.sleep(10);
                         } catch (InterruptedException e) {
                             th_.interrupt();
                             break;
                         } catch (Exception e) {
                             e.printStackTrace();
                         }
                     }
                 }
                 run_lab_ = new RunLab(controllevel_);
                 th_ = new Thread(run_lab_, "RUN LAB");
                 th_.start();
             }
         });
     }
 
     private void stopButton() {
         ActionListener[] tmp = level_button_.getActionListeners();
         if (tmp.length > 0)
             level_button_.removeActionListener(tmp[0]);
         level_button_.setText("Stopp ");
         level_button_.repaint();
         level_button_.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 startButton();
                 SqlCommunicator.add_log("Stopp Knopf getrückt", 4);
                 xpert_mode_.setLabyrinthField(labyrinth_field_);
                 String current = "";
                 if (level_ == 0)
                     current = xpert_mode_.getCurrentLabyrinth(source_);
                 else
                     current = xpert_mode_.getCurrentLabyrinth(level_);
                 SqlCommunicator.add_log(current, 1024);
                 wait_ = true;
             }
         });
     }
 
     private void stepButton() {
         ActionListener[] tmp = level_button_.getActionListeners();
         if (tmp.length > 0)
             level_button_.removeActionListener(tmp[0]);
         level_button_.setText("Schritt ");
         status_message_.setNewSecondLine("   warte auf nächsten Schritt...");
         level_button_.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 instruction_list_ = controller_gui_communicator_
                         .getInstructionListFromRuleTree();
                 turing_machine_.setInstructionList(instruction_list_);
                 InstructionList possible = turing_machine_
                         .getPossibleInstructions();
                 controller_gui_communicator_.newActiveInstructionList(possible);
                 wait_step_ = false;
                 wait_ = false;
                 SqlCommunicator.add_log("Schritt Knopf getrückt", 4);
                 SqlCommunicator.add_log("Regelsatz neu eingelesen", 32);
                 String current_program = "Aktuelles Programm:\n";
                 for (int i = 0; i < instruction_list_.size(); i++) {
                     Instruction dummy = instruction_list_
                             .getInstructionAtPos(i);
                     current_program += "                          "
                             + dummy.toString();
                 }
                 SqlCommunicator.add_log(current_program, 64);
                 current_program = "Mögliche Regeln:\n";
                 for (int i = 0; i < possible.size(); i++) {
                     Instruction dummy = possible.getInstructionAtPos(i);
                     current_program += "                          "
                             + dummy.toString();
                 }
                 SqlCommunicator.add_log(current_program, 2048);
 
                 // th_.notify();
             }
         });
     }
 
     public void drawNewLabyrinth(int level) {
         level_ = level;
         level_handler_.loadLevel(level);
         labyrinth_field_ = level_handler_.getLabyrinthField();
         SqlCommunicator.setLevel(labyrinth_field_.getName());
         SqlCommunicator.add_log(
                 "Lade neues Labyrinth: " + labyrinth_field_.getName(), 2);
 
         xpert_mode_.setLabyrinthField(level_handler_.getLabyrinthField());
         String current = xpert_mode_.getCurrentLabyrinth(level_);
         SqlCommunicator.add_log(current, 128);
 
         robot_ = new Robot();
         robot_.setPos(labyrinth_field_.getStartpointRow(),
                 labyrinth_field_.getStartpointCol());
         try {
             robot_.setStartDirection(labyrinth_field_.getStartDirection());
         } catch (IllegalDirectionException ex) {
             System.err.println(ex.getMessage());
         }
         print_labyrinth_.setLabyrinthField(labyrinth_field_);
         print_labyrinth_.setRobot(robot_);
         print_labyrinth_.repaint();
         // _______________________________Achtung ! ________-1 Anzahl der
         // Startpunkte____________________
         statistics_panel_.setFieldcountDiamondcount(
                 labyrinth_field_.getNumWayCells() - 1,
                 labyrinth_field_.getNumDiamonds());
         statistics_panel_.resetPanel();
         status_message_.clear();
         status_message_.setNewFirstLine("   Marvin-10 programmieren...");
         turing_machine_ = new TuringMachine(print_labyrinth_, labyrinth_field_,
                 robot_);
         // if (th_!=null)
         // {
         // run_lab_.finish();
         // th_.stop();
         // }
         // run_lab_= new RunLab(this);
         // th_ = new Thread(run_lab_,"RUN LAB");
         startButton();
     }
 
     public void drawNewLabyrinth(String source) {
         level_ = 0;
         source_ = source;
         level_handler_.loadLevelFromString(source);
         labyrinth_field_ = level_handler_.getLabyrinthField();
         SqlCommunicator.setLevel("Expertenmodus");
         SqlCommunicator.add_log("Lade neues Labyrinth: Expertenmodus", 2);
 
         xpert_mode_.setLabyrinthField(level_handler_.getLabyrinthField());
         String current = xpert_mode_.getCurrentLabyrinth(source);
         SqlCommunicator.add_log("Xpert Labyrinth \n" + current, 128);
 
         robot_ = new Robot();
         robot_.setPos(labyrinth_field_.getStartpointRow(),
                 labyrinth_field_.getStartpointCol());
         try {
             robot_.setStartDirection(labyrinth_field_.getStartDirection());
         } catch (IllegalDirectionException ex) {
             System.err.println(ex.getMessage());
         }
         print_labyrinth_.setLabyrinthField(labyrinth_field_);
         print_labyrinth_.setRobot(robot_);
         print_labyrinth_.repaint();
         // _______________________________Achtung ! ________-1 Anzahl der
         // Startpunkte____________________
         statistics_panel_.setFieldcountDiamondcount(
                 labyrinth_field_.getNumWayCells() - 1,
                 labyrinth_field_.getNumDiamonds());
         statistics_panel_.resetPanel();
         status_message_.clear();
         status_message_.setNewFirstLine("   Marvin-10 programmieren...");
         turing_machine_ = new TuringMachine(print_labyrinth_, labyrinth_field_,
                 robot_);
         // if (th_!=null)
         // {
         // run_lab_.finish();
         // th_.stop();
         // }
         // run_lab_= new RunLab(this);
         // th_ = new Thread(run_lab_,"RUN LAB");
         startButton();
     }
 
     public void setInsructionList(InstructionList instructions) {
         instruction_list_ = instructions;
     }
 
     public void runLab() {
         try {
             turing_machine_.newStep();
             InstructionList possible = turing_machine_
                     .getPossibleInstructions();
             status_message_.setNewFirstLine(turing_machine_.getMessage());
             controller_gui_communicator_.newActiveInstructionList(possible);
 
             if (speed_panel_.getDelay() > 0) {
                 while (wait_ == true) {
                     th_.sleep(100);
                 }
                 th_.sleep(speed_panel_.getDelay());
             } else {
                 wait_step_ = true;
                 while (wait_step_)
                     th_.sleep(100);
                 /*
                  * try{ th_.wait(); } catch(InterruptedException e) {}
                  */
             }
 
             boolean step_ok = turing_machine_
                     .makeSingleStepWithRandInstruction();
 
             if (step_ok == false) {
                 // status_message_.setNewSecondLine("   Oje, der Roboter ist in die Wand gekracht!");
                 status_message_.setNewSecondLine(turing_machine_.getMessage());
                 // SqlCommunicator.add_log(turing_machine_.getMessage(), 8);
             } else if (step_ok == true && speed_panel_.getDelay() == 0)
                 status_message_
                         .setNewSecondLine("   warte auf nächsten Schritt...");
             else if (wait_ == true)
                 status_message_.setNewSecondLine("   Programm angehalten...");
             else
                 status_message_.setNewSecondLine("   Programm läuft...");
 
             if (step_ok == false) {
                 if (turing_machine_.getMessage() == "   ")
                     SqlCommunicator
                             .add_log(
                                     "Keine Regel gefunden, das Programm ist nicht vollständig!",
                                     8);
                 else
                     SqlCommunicator.add_log(turing_machine_.getMessage(), 8);
                 xpert_mode_.setLabyrinthField(labyrinth_field_);
                 String current = "";
                 if (level_ == 0)
                     current = xpert_mode_.getCurrentLabyrinth(source_);
                 else
                     current = xpert_mode_.getCurrentLabyrinth(level_);
                 // System.out.println("Laby: \n"+current);
                 SqlCommunicator.add_log(current, 1024);
                 // SqlCommunicator.flush_log();
                 wait_ = true;
                 if (speed_panel_.getDelay() != 0)
                     startButton();
             } else {
                 statistics_panel_.refreshPanel(robot_.getNumSteps(),
                         robot_.getNumTakenThings(),
                         turing_machine_.getNumVisited());
             }
         } catch (InterruptedException ex) {
             System.err.println(ex.getMessage());
         }
 
         if (turing_machine_.checkReturnedToStartPoint() == true) {
             if (!labyrinth_field_.getStartDirection().equals(
                     robot_.getCurrentDirection())) {
                 if (labyrinth_field_.wereAllDiamondsFound() == true) {
                     status_message_
                             .setNewFirstLine("   Der Roboter hat das Labyrinth verlassen!");
                     status_message_
                            .setNewSecondLine("   Gratuliere du hast es geschafft!");
                     SqlCommunicator
                             .add_log(
                                     "Der Roboter hat das Labyrinth verlassen!\nVollständig",
                                     8);
                     xpert_mode_.setLabyrinthField(labyrinth_field_);
                     String current = "";
                     if (level_ == 0)
                         current = xpert_mode_.getCurrentLabyrinth(source_);
                     else
                         current = xpert_mode_.getCurrentLabyrinth(level_);
                     SqlCommunicator.add_log(current, 1024);
                     SqlCommunicator.flush_log();
                     control_simulation_.levelFinished();
                     System.out.println("Finish, OK");
                 } else {
                     robot_.setPos(labyrinth_field_.getStartpointRow(),
                             labyrinth_field_.getStartpointCol());
                     try {
                         robot_.setStartDirection(labyrinth_field_
                                 .getStartDirection());
                     } catch (IllegalDirectionException ex) {
                         System.err.println(ex.getMessage());
                     }
                     instruction_list_ = controller_gui_communicator_
                             .getInstructionListFromRuleTree();
                     turing_machine_.setInstructionList(instruction_list_);
                     InstructionList possible = turing_machine_
                             .getPossibleInstructions();
                     controller_gui_communicator_
                             .newActiveInstructionList(possible);
                     wait_ = true;
                     status_message_
                             .setNewFirstLine("   Der Roboter hat das Labyrinth verlassen!");
                     status_message_
                             .setNewSecondLine("   Es sind noch Diamanten übrig!");
                     SqlCommunicator
                             .add_log(
                                     "Der Roboter hat das Labyrinth verlassen!\nUnvollständig",
                                     8);
                     xpert_mode_.setLabyrinthField(labyrinth_field_);
                     String current = "";
                     if (level_ == 0)
                         current = xpert_mode_.getCurrentLabyrinth(source_);
                     else
                         current = xpert_mode_.getCurrentLabyrinth(level_);
                     SqlCommunicator.add_log(current, 1024);
                     SqlCommunicator.flush_log();
                     System.out.println("Finish, !OK");
                 }
 
                 startButton();
                 run_lab_.finish();
             }
         }
     }
 
}
