 package edu.first3729.frc2012;
 
 /**
  * @file Teleoperated.java
  */
 
import edu.wpi.first.wpilibj.*;
 
 /**
  * 
  * @class Teleoperated
  * @brief Class that runs robot during Teleoperated period
  * 
  * Manages instances of Drive, Input, and Manipulator classes.  Gets appropriate
  * input based on controller layout mode and sets outputs accordingly.
  */
 public class Teleoperated
 {
     private Input _input_manager;
     private Drive _drive;
     private Manipulator _manip;
     private DigitalInput intake_limit;
     private double x = 0.0, y = 0.0, z = 0.0, left = 0.0, right = 0.0, scale_factor = 0.0;
     private boolean intake_continue = false, net_down = false, net_up = false, shoot = false, lift = false, intake = false, bridge_down = false, bridge_up = false;
     private boolean shoot_old = false, lift_old = false;
     private int input_left = 0, input_right = 0, input_controller = 0;
     
     /**
      * @param imanager instance of Input class passed from MainRobotClass
      * @param drv instance of Drive class passed from MainRobotClass
      * @param manip instance of Manipulator class passed from MainRobotClass
      * @brief Constructor - takes subsystem classes passed from MainRobotClass
      */
     public Teleoperated(Input imanager, Drive drv, Manipulator manip)
     {
         this._input_manager = imanager;
         this._manip = manip;
         this._drive = drv;
         this.intake_limit = new DigitalInput(Params.intake_limit_digin_port);
     }
     
     /**
      * @brief Initializes manipulator, locks drive, locks input
      */
     public void init()
     {
         this._input_manager.setMode(Input.locked);
         this._drive.lock_motors();
         this._manip.init();
     }
     
     /**
      * @brief Runs Teleoperated period
      * Runs Teleoperated period
      */
     public void run()
     {
         // Update input fields
         this.getInput();
         // Drive robot based on drive mode
         switch (this._input_manager.getMode()) {
         case Input.mecanum:
             this._drive.drive_mecanum(this.x, this.y, this.z);
         case Input.arcade_joy:
         case Input.arcade_controller:
             this._drive.drive_arcade(this.x, this.y);
             break;
         case Input.tank:
             this._drive.drive_tank(this.left, this.right);
             break;
         case Input.locked:
             this._drive.lock_motors();
         }
         // If intake limit switch is hit, turn off intake relay
         if (!this.intake_limit.get()) {
             this._manip.intake(Relay.Value.kOff);
         }
         // Intake control
         if (this.intake) {
             this._manip.intake(Relay.Value.kForward);
         }
         // Gist of the above 2 'if' statements: when intake button pressed, 
         // keep relay running until limit switch is pressed.
         // Toggle elevator
         if (lift && lift != lift_old) { this._manip.lift(lift); lift_old = lift; }
         // Toggle shooter
         if (shoot && shoot != shoot_old) { this._manip.shoot(shoot); shoot_old = shoot; }
         // Bridge relay control
         if (bridge_up) { this._manip.bridge(Relay.Value.kForward); }
         else if (bridge_down) { this._manip.bridge(Relay.Value.kReverse); }
         else { this._manip.bridge(Relay.Value.kOff); }
         // If up and down both requested, do nothing
         if (bridge_up && bridge_down) { this._manip.bridge(Relay.Value.kOff); }
         // Ditto for net
         if (net_up) { this._manip.lift_net(Relay.Value.kForward); }
         else if (net_down) { this._manip.lift_net(Relay.Value.kReverse); }
         else { this._manip.lift_net(Relay.Value.kOff); }
         if (net_up && net_down) { this._manip.lift_net(Relay.Value.kOff); }
     }
     
     /**
      * @brief Updates local input fields with values read from input devices
      */
     public void getInput()
     {
         int mode = this._input_manager.getMode();
         if (this._input_manager.getTwist() > 0) {
             this.scale_factor = Params.drive_creep_scale_factor;
         }
         else
             this.scale_factor = 1.0;
         this.x = this._input_manager.getX() * scale_factor;
         this.y = this._input_manager.getY() * scale_factor;
         this.z = this._input_manager.getZ() * scale_factor;
         this.input_left = this._input_manager.getBooleanButtonInputs(0);
         this.input_right = this._input_manager.getBooleanButtonInputs(1);
         this.input_controller = this._input_manager.getBooleanButtonInputs(2);
         switch (mode) {
         default:
         case Input.arcade_joy:  // Arcade drive, two joysticks
         case Input.arcade_controller:  // Arcade drive w/ flight controller and joystick - buttons same
             this.intake = this._input_manager.checkButton(0, 1);
             this.shoot = this._input_manager.checkButton(0, 2);
             this.bridge_down = this._input_manager.checkButton(0, 4);
             this.lift = this._input_manager.checkButton(0, 3);
             this.bridge_up = this._input_manager.checkButton(0, 5);
             this.net_up = this._input_manager.checkButton(0, 7);
             this.net_down = this._input_manager.checkButton(0, 8);
             break;
         case Input.tank:  // Tank drive and
         case Input.mecanum:  // Mecanum drive - both input from joystick 3
             this.intake = this._input_manager.checkButton(2, 1);
             this.shoot = this._input_manager.checkButton(2, 2);
             this.bridge_down = this._input_manager.checkButton(2, 4);
             this.lift = this._input_manager.checkButton(2, 3);
             this.bridge_up = this._input_manager.checkButton(2, 5);
             this.net_up = this._input_manager.checkButton(0, 7);
             this.net_down = this._input_manager.checkButton(0, 8);
             break;
         case Input.locked:  // Locked controls, nothing to do here
             this.intake = this.shoot = this.bridge_up = this.bridge_down = this.lift = this.net_up = this.net_down = false;
             break;
         }
         // Button 6, arcade drive 2 joysticks
         if (this._input_manager.checkButton(1, 6)) {
             this._input_manager.setMode(Input.arcade_joy);
         }
         // Button 7, arcade drive 1 joystick 1 controller
         if (this._input_manager.checkButton(1, 7)) 
         {
             this._input_manager.setMode(Input.arcade_controller);
         }
         // Button 10, tank drive 3 joysticks
         if (this._input_manager.checkButton(1, 10)) {
             this._input_manager.setMode(Input.tank);
         }
         // Button 11, lock all controls
         if (this._input_manager.checkButton(1, 11)) {
             this._input_manager.setMode(Input.locked);
         }
         System.out.println("Left twist: " + this._input_manager.getTwist(0));
         System.out.println("Right twist: " + this._input_manager.getTwist(1));
         System.out.println("Mode: " + this._input_manager.getMode());
     }
     
 }
