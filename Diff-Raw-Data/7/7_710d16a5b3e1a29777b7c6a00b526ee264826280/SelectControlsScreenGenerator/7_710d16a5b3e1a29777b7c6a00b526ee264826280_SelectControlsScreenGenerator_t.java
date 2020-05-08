 package com.navid.trafalgar.mod.common;
 
 import com.google.common.collect.HashMultimap;
 import com.navid.trafalgar.input.Command;
 import com.navid.trafalgar.input.CommandGenerator;
 import com.navid.trafalgar.input.GeneratorBuilder;
 import com.navid.trafalgar.model.AShipModelTwo;
 import com.navid.trafalgar.model.GameConfiguration;
 import com.navid.trafalgar.screenflow.ScreenGenerator;
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.builder.LayerBuilder;
 import de.lessvoid.nifty.builder.PanelBuilder;
 import de.lessvoid.nifty.builder.ScreenBuilder;
 import de.lessvoid.nifty.builder.TextBuilder;
 import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
 import de.lessvoid.nifty.controls.radiobutton.builder.RadioButtonBuilder;
 import de.lessvoid.nifty.controls.radiobutton.builder.RadioGroupBuilder;
 import de.lessvoid.nifty.screen.Screen;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import org.springframework.beans.factory.annotation.Autowired;
 
 /**
  *
  * @author alberto
  */
 public class SelectControlsScreenGenerator implements ScreenGenerator {
 
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
     
     @Autowired
     private SelectControlsScreenController screenControlScreenController;
     
     @Autowired
     private Nifty nifty;
     
     private Map<String, PanelBuilder> panels;
 
     @Override
     public void buildScreen() {
         panels = new HashMap<String, PanelBuilder>();
 
         AShipModelTwo ship = gameConfiguration.getPreGameModel().getSingleByType(AShipModelTwo.class);
 
         Set<Command> commands = ship.getCommands();
 
         HashMultimap<Command, CommandGenerator> gens = generatorBuilder.getGeneratorsFor(commands);
 
 
 
         final PanelBuilder outerPanelBuilder = new PanelBuilder("Panel_ID") {
             {
                 height("80%");
                 childLayoutHorizontal();
             }
         };
 
         final PanelBuilder commandNamePanelBuilder = new PanelBuilder("CommandNamePanel") {
             {
                 childLayoutVertical();
                 text(new TextBuilder("text") {
                     {
                         text("Command");
                         style("nifty-label");
                         alignCenter();
                         valignCenter();
                         height("10%");
                     }
                 });
             }
         };
 
         outerPanelBuilder.panel(commandNamePanelBuilder);
 
 
         for (final Command currentCommand : gens.keySet()) {
 
             commandNamePanelBuilder.text(new TextBuilder("text") {
                 {
                     text(currentCommand.toString());
                     style("nifty-label");
                     alignCenter();
                     valignCenter();
                     height("10%");
 
                 }
             });
 
             commandNamePanelBuilder.control(new RadioGroupBuilder(currentCommand.toString())); // the RadioGroup id is used to link radiobuttons logical together so that only one of them can be active at a certain time
 
             for (final CommandGenerator commandGenerator : gens.get(currentCommand)) {
 
                 PanelBuilder commandGeneratorPanel = getPanel(commandGenerator.toString(), outerPanelBuilder);
 
                 commandGeneratorPanel.control(new RadioButtonBuilder(commandGenerator.toString()) {
                     {
                         group(currentCommand.toString());
                         height("10%");
                     }
                 });
 
 
             }
         }
 
 
         Screen screen = new ScreenBuilder("selectControl") {
             {
                 controller(screenControlScreenController); // Screen properties       
 
                 // <layer>
                 layer(new LayerBuilder("Layer_ID") {
                     {
                         childLayoutVertical(); // layer properties, add more...
 
                         // <panel>
                         panel(outerPanelBuilder);
                         // </panel>
                         panel(new PanelBuilder("Panel_ID") {
                             {
                                 height("20%");
                                 childLayoutHorizontal();
                                 control(new ButtonBuilder("PreviousButton", "Back") {
                                     {
                                         alignCenter();
                                         valignCenter();
                                         interactOnClick("previous()");
                                     }
                                 });
 
                                 control(new ButtonBuilder("NextButton", "Next") {
                                     {
                                         alignCenter();
                                         valignCenter();
                                         interactOnClick("next()");
                                     }
                                 });
                             }
                         });
 
                     }
                 });
                 // </layer>
             }
         }.build(nifty);
         // <screen>
     }
 
     private PanelBuilder createPanel(String panelName) {
         return new PanelBuilder(panelName) {
             {
                 childLayoutVertical();
             }
         };
     }
 
     private PanelBuilder getPanel(final String panelName, PanelBuilder parent) {
         if (!panels.containsKey(panelName)) {
             PanelBuilder panel = createPanel(panelName);
             parent.panel(panel);
             panel.text(new TextBuilder("text") {
                 {
                     text(panelName);
                     style("nifty-label");
                     alignCenter();
                     valignCenter();
                     height("10%");
                 }
             });
             panels.put(panelName, panel);
         }
 
         return panels.get(panelName);
     }
 
     /**
      * @param nifty the nifty to set
      */
     public void setNifty(Nifty nifty) {
         this.nifty = nifty;
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
 
     /**
      * @param screenControlScreenController the screenControlScreenController to
      * set
      */
     public void setScreenControlScreenController(SelectControlsScreenController screenControlScreenController) {
         this.screenControlScreenController = screenControlScreenController;
     }
 }
