 package ttProject.controller.main;
 
 import ttProject.controller.input.TTInput;
 import ttProject.controller.input.joystick.TTJoystickFactory;
 import ttProject.controller.measurements.MeasurementsManager;
 import ttProject.controller.measurements.reactions.ReactionWrapper;
 import ttProject.controller.measurements.signals.HarmonicSignalWrapper;
 import ttProject.controller.measurements.signals.Signal;
 import ttProject.view.items.Target;
 import ttProject.view.items.Tracker;
 import ttProject.view.main.MyMainFrame;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Michal
  * Date: 14.5.12
  * Time: 8:55
  * To change this template use File | Settings | File Templates.
  */
 public class MainController {
 
     private MyMainFrame myMainFrame;
     private TTInput ttInput;
     private JoystickThread joystickThread;
     private Signal signals;
 
 
     public MainController() {
        this.myMainFrame = new MyMainFrame("TT",this);
 
         this.ttInput = TTJoystickFactory.createJoystick();
 
         this.signals = MeasurementsManager.getMeasurementManager().getHarmonicSignalWrapper();
        signals.setBounds(myMainFrame.getGraphicsPanel().getWidth(),myMainFrame.getGraphicsPanel().getHeight());
 
 
         this.joystickThread = new JoystickThread(myMainFrame.getGraphicsPanel(), ttInput);
         joystickThread.setSignal(signals);
         myMainFrame.setJoystickThread(joystickThread);
         joystickThread.start();
     }
 
    public void setHarmonicSignalActive(){
         Signal signal = MeasurementsManager.getMeasurementManager().getHarmonicSignalWrapper();
         joystickThread.setSignal(signal);
        joystickThread.endMeasurement();
     }
 
    public void setRandomSignalActive(){
         Signal signal = MeasurementsManager.getMeasurementManager().getRandomSignalWrapper();
         signal.resetStates();
        signal.setBounds(myMainFrame.getGraphicsPanel().getWidth(),myMainFrame.getGraphicsPanel().getHeight());
         joystickThread.setSignal(signal);
        joystickThread.endMeasurement();
     }
 
 }
