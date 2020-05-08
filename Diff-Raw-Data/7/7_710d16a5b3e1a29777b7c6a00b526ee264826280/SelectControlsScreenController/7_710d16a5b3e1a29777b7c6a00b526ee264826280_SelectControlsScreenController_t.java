 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.navid.trafalgar.mod.common;
 
 import com.google.common.base.Function;
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Maps;
 import com.navid.trafalgar.input.Command;
 import com.navid.trafalgar.input.CommandGenerator;
 import com.navid.trafalgar.input.CommandStateListener;
 import com.navid.trafalgar.input.GeneratorBuilder;
 import com.navid.trafalgar.model.AShipModelTwo;
 import com.navid.trafalgar.model.GameConfiguration;
 import com.navid.trafalgar.screenflow.ScreenFlowManager;
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.controls.RadioButtonStateChangedEvent;
 import de.lessvoid.nifty.controls.radiobutton.RadioButtonControl;
 import de.lessvoid.nifty.screen.Screen;
 import de.lessvoid.nifty.screen.ScreenController;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import org.bushe.swing.event.EventTopicSubscriber;
 import org.springframework.beans.factory.annotation.Autowired;
 
 /**
  *
  * @author alberto
  */
 public class SelectControlsScreenController implements ScreenController {
 
     /**
      * From bind
      */
     private Nifty nifty;
     /**
      * From bind
      */
     private Screen screen;
     /**
      * Singleton
      */
     @Autowired
     private GameConfiguration gameConfiguration;
     /**
      *
      */
     @Autowired
     private GeneratorBuilder generatorBuilder;
     /**
      *
      */
     @Autowired
     private ScreenFlowManager screenFlowManager;
 
     @Override
     public void bind(Nifty nifty, Screen screen) {
         this.nifty = nifty;
         this.screen = screen;
     }
     private EventTopicSubscriber<RadioButtonStateChangedEvent> eventHandler;
     private Map<String, String> generated = new HashMap<String, String>();
     private Map<String, Command> commandsMap;
     private Map<String, CommandGenerator> generatorMap;
 
     @Override
     public void onStartScreen() {
 
         eventHandler = new EventTopicSubscriber<RadioButtonStateChangedEvent>() {
             @Override
             public void onEvent(String string, RadioButtonStateChangedEvent t) {
                 if (t.isSelected()) {
                     generated.put(t.getRadioButton().getGroup().getId(), string);
                 }
             }
         };
 
         AShipModelTwo ship = gameConfiguration.getPreGameModel().getSingleByType(AShipModelTwo.class);
         Set<Command> commands = ship.getCommands();
 
         commandsMap = Maps.uniqueIndex(commands, new Function<Command, String>() {
             @Override
             public String apply(Command input) {
                 return input.toString();
             }
         });
 
         generatorMap = generatorBuilder.getGenerators();
 
         HashMultimap<Command, CommandGenerator> gens = generatorBuilder.getGeneratorsFor(commands);
 
         for (final Command currentCommand : gens.keySet()) {
             for (final CommandGenerator currentGenerator : gens.get(currentCommand)) {
                 nifty.subscribe(screen, currentGenerator.toString(), RadioButtonStateChangedEvent.class, eventHandler);
 
                 RadioButtonControl radioControl = screen.findControl(currentGenerator.toString(), RadioButtonControl.class);
                 if (radioControl.isActivated()) {
                     generated.put(currentCommand.toString(), currentGenerator.toString());
                 }
             }
         }
     }
 
     @Override
     public void onEndScreen() {
     }
 
     public void previous() {
     }
 
     public void goTo(String nextScreen) {
         Map<Command, CommandGenerator> assignments = new HashMap<Command, CommandGenerator>();
 
         for (Map.Entry<String, String> entry : generated.entrySet()) {
             assignments.put(commandsMap.get(entry.getKey()), generatorMap.get(entry.getValue()));
         }
 
         Set<CommandStateListener> listeners = generatorBuilder.generateControllers(assignments);
 
         gameConfiguration.getPreGameModel().addToModel(listeners);
 
         nifty.gotoScreen(nextScreen);
     }
 
     public void next() {
         screenFlowManager.changeNextScreen();
         goTo("redirector");
     }
 
     /**
      * @param screenFlowManager the screenFlowManager to set
      */
     public void setScreenFlowManager(ScreenFlowManager screenFlowManager) {
         this.screenFlowManager = screenFlowManager;
     }
 
     /**
      * @param gameConfiguration the gameConfiguration to set
      */
     public void setGameConfiguration(GameConfiguration gameConfiguration) {
         this.gameConfiguration = gameConfiguration;
     }
 
     /**
      * @param generatorBuilder the generatorBuilder to set
      */
     public void setGeneratorBuilder(GeneratorBuilder generatorBuilder) {
         this.generatorBuilder = generatorBuilder;
     }
 }
