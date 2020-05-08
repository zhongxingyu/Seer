 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 public class Preferences extends JFrame {
     private static final String TITLE = "Preferences | NPC World";
 
     private LayoutManager layout;
     private Container     container;
 
     private GUI gui;
 
     private JPanel panel;
 
     private static final int gapH = 3;
     private static final int gapV = 3;
     private static final int pad  = 3;
 
     private JSpinner _runDelay;
 
     private JSpinner _deathChance;
     private JSpinner _deathChanceMax;
     private JSpinner _deathChanceChange;
 
     private JSpinner _mutationChance;
     private JSpinner _crossoverChance;
 
     private JSpinner _populationSize;
     private JSpinner _maxSize;
 
     private JSpinner _oldAge;
     private JSpinner _youngAge;
 
     private JSpinner _eatingCapacity;
     private JSpinner _sleepingCapacity;
 
     private JSpinner _maxHunger;
     private JSpinner _maxSleepiness;
 
     private JSpinner _hungerChange;
     private JSpinner _sleepinessChange;
 
     private JSpinner _healthinessPercent;
 
     private JSpinner _matingFrequency;
 
     public Preferences(GUI gui) {
         this.gui = gui;
 
         _runDelay = new JSpinner();
 
         _deathChance        = new JSpinner();
         _deathChanceMax     = new JSpinner();
         _deathChanceChange  = new JSpinner();
 
         _mutationChance     = new JSpinner();
         _crossoverChance    = new JSpinner();
 
         _populationSize = new JSpinner();
         _maxSize        = new JSpinner();
 
         _oldAge   = new JSpinner();
         _youngAge = new JSpinner();
 
         _eatingCapacity   = new JSpinner();
         _sleepingCapacity = new JSpinner();
 
         _maxHunger     = new JSpinner();
         _maxSleepiness = new JSpinner();
 
         _hungerChange     = new JSpinner();
         _sleepinessChange = new JSpinner();
 
         _healthinessPercent = new JSpinner();
 
         _matingFrequency = new JSpinner();
 
         setTitle(TITLE);
 
         container = getContentPane();
         panel     = new JPanel();
         //container.setLayout(new GridLayout(0, 1, gapH, gapV));
         //container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
         container.add(panel);
         panel.setLayout(new GridLayout(0, 4, gapH, gapV));
         panel.setBorder(Util.makeBorder(pad));
 
         addPair("Run delay:", _runDelay);
 
         addPair("Death chance:", _deathChance);
         addPair("Death chance max:", _deathChanceMax);
         addPair("Death chance change:", _deathChanceChange);
 
         addPair("Mutation chance:", _mutationChance);
         addPair("Crossover chance:", _crossoverChance);
 
         addPair("Population size:", _populationSize);
         addPair("Max size:", _maxSize);
 
         addPair("Old age:", _oldAge);
         addPair("Young age:", _youngAge);
 
         addPair("Eating capacity:", _eatingCapacity);
         addPair("Sleeping capacity:", _sleepingCapacity);
 
         addPair("Max hunger:", _maxHunger);
         addPair("Max sleepiness:", _maxSleepiness);
 
         addPair("Hunger change:", _hungerChange);
         addPair("Sleepiness change:", _sleepinessChange);
 
         addPair("Healthiness percent:", _healthinessPercent);
 
         addPair("Mating frequency:", _matingFrequency);
         addPair("", new ZeroPaddingComponent());
 
         panel.add(new ZeroPaddingComponent());
         panel.add(Util.clickableButton("OK",     new OkHandler()));
         panel.add(Util.clickableButton("Cancel", new CancelHandler()));
         panel.add(new ZeroPaddingComponent());
 
         setResizable(false);
 
         pack();
 
         loadCurrentValues();
 
         setVisible(true);
     }
 
     private void addPair(String label, JComponent component) {
         panel.add(Util.rightLabel(label));
         panel.add(component);
     }
 
     private void loadCurrentValues() {
         _runDelay.setValue(Settings.runDelay);
 
         _deathChance.setValue((int) (Settings.deathChance * 100));
         _deathChanceMax.setValue((int) (Settings.deathChanceMax * 100));
        _deathChanceChange.setValue((int) (Settings.deathChanceMax * 100));
 
         _mutationChance.setValue((int) (Settings.mutationChance * 100));
         _crossoverChance.setValue((int) (Settings.crossoverChance * 100));
 
         _populationSize.setValue(Settings.populationSize);
         _maxSize.setValue(Settings.maxSize);
 
         _oldAge.setValue(Settings.oldAge);
         _youngAge.setValue(Settings.youngAge);
 
         _eatingCapacity.setValue(Settings.eatingCapacity);
         _sleepingCapacity.setValue(Settings.sleepingCapacity);
 
         _maxHunger.setValue(Settings.maxHunger);
         _maxSleepiness.setValue(Settings.maxSleepiness);
 
         _hungerChange.setValue(Settings.hungerChange);
         _sleepinessChange.setValue(Settings.sleepinessChange);
 
         _healthinessPercent.setValue(Settings.healthinessPercent);
 
         _matingFrequency.setValue(Settings.matingFrequency);
     }
 
     private void saveCurrentValues() {
         Settings.runDelay = Util.intFromSpinner(_runDelay);
 
         Settings.deathChance = Util.intFromSpinner(_deathChance) / 100.0;
         Settings.deathChanceMax = Util.intFromSpinner(_deathChanceMax) / 100.0;
        Settings.deathChanceChange = Util.intFromSpinner(_deathChanceMax) / 100.0;
 
         Settings.mutationChance = Util.intFromSpinner(_mutationChance) / 100.0;
         Settings.crossoverChance = Util.intFromSpinner(_crossoverChance) / 100.0;
 
         Settings.populationSize = Util.intFromSpinner(_populationSize);
         Settings.maxSize = Util.intFromSpinner(_maxSize);
 
         Settings.oldAge = Util.intFromSpinner(_oldAge);
         Settings.youngAge = Util.intFromSpinner(_youngAge);
 
         Settings.eatingCapacity = Util.intFromSpinner(_eatingCapacity);
         Settings.sleepingCapacity = Util.intFromSpinner(_sleepingCapacity);
 
         Settings.maxHunger = Util.intFromSpinner(_maxHunger);
         Settings.maxSleepiness = Util.intFromSpinner(_maxSleepiness);
 
         Settings.hungerChange = Util.intFromSpinner(_hungerChange);
         Settings.sleepinessChange = Util.intFromSpinner(_sleepinessChange);
 
         Settings.healthinessPercent = Util.intFromSpinner(_healthinessPercent);
 
         Settings.matingFrequency = Util.intFromSpinner(_matingFrequency);
     }
 
     private class OkHandler implements ActionListener {
         public void actionPerformed(ActionEvent e) {
             saveCurrentValues();
             Debug.echo("ok...");
             dispose();
         }
     }
 
     private class CancelHandler implements ActionListener {
         public void actionPerformed(ActionEvent e) {
             Debug.echo("cancel...");
             dispose();
         }
     }
 }
