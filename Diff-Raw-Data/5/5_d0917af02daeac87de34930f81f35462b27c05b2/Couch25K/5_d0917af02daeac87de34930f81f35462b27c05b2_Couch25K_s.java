 import java.io.IOException;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.microedition.lcdui.Choice;
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.CommandListener;
 import javax.microedition.lcdui.Display;
 import javax.microedition.lcdui.Displayable;
 import javax.microedition.lcdui.Form;
 import javax.microedition.lcdui.Gauge;
 import javax.microedition.lcdui.List;
 import javax.microedition.lcdui.StringItem;
 import javax.microedition.media.Manager;
 import javax.microedition.media.MediaException;
 import javax.microedition.midlet.MIDlet;
 import javax.microedition.midlet.MIDletStateChangeException;
 
 public class Couch25K extends MIDlet implements CommandListener {
     private static final int STATE_SELECT_WEEK = 1;
     private static final int STATE_SELECT_WORKOUT = 2;
     private static final int STATE_WORKOUT_SELECTED = 3;
     private static final int STATE_WORKOUT = 4;
     private static final int STATE_WORKOUT_PAUSED = 5;
     private static final int STATE_WORKOUT_COMPLETE = 6;
 
     private int state;
     private int selectedWeek;
     private int selectedWorkout;
     private Workout workout;
     private WorkoutState workoutState;
 
     private Display display;
     private List selectWeekScreen;
     private Command selectWeekCommand = new Command("Select", Command.ITEM, 1);
     private List selectWorkoutScreen;
     private Command selectWorkoutCommand = new Command("Select", Command.ITEM, 1);
     private Form workoutScreen;
     private Command startWorkoutCommand = new Command("Start", Command.SCREEN, 1);
     private Command pauseWorkoutCommand = new Command("Pause", Command.SCREEN, 2);
     private Command resumeWorkoutCommand = new Command("Resume", Command.SCREEN, 3);
     private StringItem action;
     private Gauge stepProgress;
     private StringItem timeDisplay;
     private Gauge workoutProgress;
     private Form workoutCompleteScreen;
 
     // Workout timing
     private Timer workoutTimer;
 
     // MIDlet API --------------------------------------------------------------
 
     protected void startApp() throws MIDletStateChangeException {
         if (display == null) {
             display = Display.getDisplay(this);
             // Week selection screen setup
            selectWeekScreen = new List("Select Week", Choice.EXCLUSIVE, new String[] {
                     "Week 1", "Week 2", "Week 3", "Week 4", "Week 5", "Week 6",
                     "Week 7", "Week 8", "Week 9"
             }, null);
             selectWeekScreen.addCommand(selectWeekCommand);
             selectWeekScreen.setCommandListener(this);
 
             // Workout selection screen setup
            selectWorkoutScreen = new List("Select Workout", Choice.EXCLUSIVE, new String[] {
                     "Workout 1", "Workout 2", "Workout 3"
             }, null);
             selectWorkoutScreen.addCommand(selectWorkoutCommand);
             selectWorkoutScreen.setCommandListener(this);
 
             // Workout screen setup
             workoutScreen = new Form("");
             workoutScreen.setCommandListener(this);
             action = new StringItem(null, "");
             stepProgress = new Gauge("Step", false, -1, 0);
             timeDisplay = new StringItem(null, "");
             workoutProgress = new Gauge("Workout", false, -1, 0);
             workoutScreen.append(action);
             workoutScreen.append(stepProgress);
             workoutScreen.append(timeDisplay);
             workoutScreen.append(workoutProgress);
 
             workoutCompleteScreen = new Form("Workout Complete");
             workoutCompleteScreen.setCommandListener(this);
         }
 
         if (state == STATE_WORKOUT_PAUSED) {
             startWorkout();
         } else {
           init();
         }
     }
 
     protected void pauseApp() {
         if (state == STATE_WORKOUT) {
             pauseWorkout();
         }
     }
 
     protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
     }
 
     // CommandListener API -----------------------------------------------------
 
     public void commandAction(Command c, Displayable d) {
         switch (state) {
         case STATE_SELECT_WEEK:
             if (c == selectWeekCommand) selectWeek();
             break;
         case STATE_SELECT_WORKOUT:
             if (c == selectWorkoutCommand) selectWorkout();
             break;
         case STATE_WORKOUT_SELECTED:
             if (c == startWorkoutCommand) startWorkout();
             break;
         case STATE_WORKOUT:
             if (c == pauseWorkoutCommand) pauseWorkout();
             break;
         case STATE_WORKOUT_PAUSED:
             if (c == resumeWorkoutCommand) resumeWorkout();
             break;
         default:
             throw new IllegalStateException("Command " + c + " not expected in state " + state);
         }
     }
 
     // State transitions -------------------------------------------------------
 
     public void init() {
         display.setCurrent(selectWeekScreen);
         state = STATE_SELECT_WEEK;
     }
 
     public void selectWeek() {
         selectedWeek = selectWeekScreen.getSelectedIndex() + 1;
         display.setCurrent(selectWorkoutScreen);
         state = STATE_SELECT_WORKOUT;
     }
 
     public void selectWorkout() {
         selectedWorkout = selectWorkoutScreen.getSelectedIndex() + 1;
         workout = Workouts.getWorkout(selectedWeek, selectedWorkout);
         workoutScreen.setTitle("Week " + selectedWeek + ", Workout " + selectedWorkout);
         workoutProgress.setMaxValue(workout.totalDuration);
         workoutProgress.setValue(0);
         workoutScreen.addCommand(startWorkoutCommand);
         display.setCurrent(workoutScreen);
         workoutState = new WorkoutState(this, workout);
         state = STATE_WORKOUT_SELECTED;
     }
 
     public void startWorkout() {
         workoutScreen.removeCommand(startWorkoutCommand);
         workoutScreen.addCommand(pauseWorkoutCommand);
         trackWorkoutState(workoutState);
         state = STATE_WORKOUT;
     }
 
     public void pauseWorkout() {
         workoutTimer.cancel();
         workoutTimer = null;
         workoutScreen.removeCommand(pauseWorkoutCommand);
         workoutScreen.addCommand(resumeWorkoutCommand);
         state = STATE_WORKOUT_PAUSED;
     }
 
     public void resumeWorkout() {
         workoutScreen.removeCommand(resumeWorkoutCommand);
         workoutScreen.addCommand(pauseWorkoutCommand);
         trackWorkoutState(workoutState);
         state = STATE_WORKOUT;
     }
 
     public void finishWorkout() {
         workoutTimer.cancel();
         display.setCurrent(workoutCompleteScreen);
         playSound("finished");
         state = STATE_WORKOUT_COMPLETE;
     }
 
     // Status update API -------------------------------------------------------
 
     public void updateStep(int stepNum, WorkoutStep step) {
         action.setText(step.action + " (" + stepNum + "/" + workout.steps.length + ")");
         stepProgress.setValue(0);
         stepProgress.setMaxValue(step.duration);
         if (stepNum > 1) {
             playSound(step.action.toLowerCase());
         }
     }
 
     public void updateProgress(int progress, int totalTime) {
         stepProgress.setValue(progress);
         int minutes = totalTime / 60;
         int seconds = totalTime % 60;
         timeDisplay.setText(pad(minutes) + ":" + pad(seconds));
         workoutProgress.setValue(totalTime);
     }
 
     // Utilities ---------------------------------------------------------------
 
     private void trackWorkoutState(final WorkoutState workoutState) {
         workoutTimer = new Timer();
         workoutTimer.scheduleAtFixedRate(new TimerTask() {
             public void run() {
                 workoutState.increment();
             }
         }, 0, 1000);
     }
 
     private void playSound(String file) {
         try {
             Manager.createPlayer(
                 getClass().getResourceAsStream("/" + file + ".wav"),
                 "audio/x-wav"
             ).start();
         } catch (MediaException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private String pad(int n) {
         if (n < 10) {
             return "0" + n;
         }
         return "" + n;
     }
 }
