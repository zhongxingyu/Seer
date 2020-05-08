 
 package com.wartricks.logic;
 
 import java.util.NoSuchElementException;
 import java.util.Observable;
 import java.util.Observer;
 
 import bsh.EvalError;
 
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.World;
 import com.artemis.annotations.Mapper;
 import com.artemis.managers.GroupManager;
 import com.artemis.managers.TagManager;
 import com.artemis.utils.ImmutableBag;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
 import com.badlogic.gdx.utils.Array;
 import com.wartricks.components.Action;
 import com.wartricks.components.ActionSequence;
 import com.wartricks.components.Cooldown;
 import com.wartricks.components.Cost;
 import com.wartricks.components.EnergyBar;
 import com.wartricks.components.EnergyRegen;
 import com.wartricks.components.Initiative;
 import com.wartricks.components.MapPosition;
 import com.wartricks.components.Owner;
 import com.wartricks.components.Range;
 import com.wartricks.components.ScriptExecutable;
 import com.wartricks.components.SkillSet;
 import com.wartricks.custom.Pair;
 import com.wartricks.custom.PositionArray;
 import com.wartricks.input.CreatureSelectInput;
 import com.wartricks.input.GeneralInput;
 import com.wartricks.input.TargetSelectInput;
 import com.wartricks.logic.StateMachine.GameState;
 import com.wartricks.systems.OnBeginTurnSystem;
 import com.wartricks.systems.OnEndTurnSystem;
 import com.wartricks.systems.OnExecuteTurnSystem;
 import com.wartricks.ui.FramedDialog;
 import com.wartricks.ui.FramedMenu;
 import com.wartricks.utils.Constants.Groups;
 import com.wartricks.utils.Constants.Players;
 import com.wartricks.utils.EntityFactory;
 
 public class VersusGame implements Observer {
     private OrthographicCamera camera;
 
     private Stage stage;
 
     public GameMap map;
 
     public World world;
 
     public ActionExecutor executor;
 
     public StateMachine state;
 
     public InputMultiplexer inputSystem;
 
     public Api api;
 
     private Skin skin;
 
     private FramedMenu playerMenu;
 
     private FramedMenu creatureMenu;
 
     private FramedDialog confirm;
 
     private TargetSelectInput targetSelectInput;
 
     private CreatureSelectInput creatureSelectInput;
 
     private GeneralInput generalInput;
 
     private OnBeginTurnSystem onBeginTurnSystem;
 
     private OnEndTurnSystem onEndTurnSystem;
 
     private OnExecuteTurnSystem onExecuteTurnSystem;
 
     @Mapper
     private ComponentMapper<Range> rm;
 
     @Mapper
     private ComponentMapper<MapPosition> mm;
 
     @Mapper
     private ComponentMapper<EnergyBar> ebm;
 
     @Mapper
     private ComponentMapper<Cost> com;
 
     @Mapper
     private ComponentMapper<ActionSequence> asm;
 
     @Mapper
     private ComponentMapper<Initiative> im;
 
     @Mapper
     private ComponentMapper<Owner> om;
 
     @Mapper
     private ComponentMapper<EnergyRegen> erm;
 
     @Mapper
     private ComponentMapper<Cooldown> cdm;
 
     @Mapper
     private ComponentMapper<ScriptExecutable> ocm;
 
     @Mapper
     private ComponentMapper<SkillSet> ssm;
 
     public VersusGame(GameMap gameMap, World gameWorld, OrthographicCamera camera, Stage stage) {
         super();
         map = gameMap;
         world = gameWorld;
         this.camera = camera;
         this.stage = stage;
         final FileHandle skinFile = Gdx.files.internal("resources/uiskin/uiskin.json");
         skin = new Skin(skinFile);
         playerMenu = new FramedMenu(skin, 250, 800);
         creatureMenu = new FramedMenu(skin, 250, 800);
         confirm = new FramedDialog(skin, "", "Are you sure?", 300, 120);
         api = new Api(this);
         executor = new ActionExecutor(this);
         rm = world.getMapper(Range.class);
         mm = world.getMapper(MapPosition.class);
         ebm = world.getMapper(EnergyBar.class);
         com = world.getMapper(Cost.class);
         om = world.getMapper(Owner.class);
         erm = world.getMapper(EnergyRegen.class);
         asm = world.getMapper(ActionSequence.class);
         im = world.getMapper(Initiative.class);
         cdm = world.getMapper(Cooldown.class);
         ocm = world.getMapper(ScriptExecutable.class);
         ssm = world.getMapper(SkillSet.class);
         onExecuteTurnSystem = gameWorld.setSystem(new OnExecuteTurnSystem(this), true);
         onBeginTurnSystem = gameWorld.setSystem(new OnBeginTurnSystem(this), true);
         onEndTurnSystem = gameWorld.setSystem(new OnEndTurnSystem(this), true);
         // playerInputSystem = gameWorld.setSystem(new PlayerInputSystem(camera, gameMap), false);
         inputSystem = new InputMultiplexer();
         generalInput = new GeneralInput(camera, this);
         creatureSelectInput = new CreatureSelectInput(camera, this);
         targetSelectInput = new TargetSelectInput(camera, this);
         state = new StateMachine();
         state.addObserver(this);
     }
 
     public boolean startLogic() {
         // TODO creating players
         EntityFactory.createPlayer(world, map, Players.ONE, 10);
         EntityFactory.createPlayer(world, map, Players.TWO, 10);
         final Array<String> characters = new Array<String>(new String[] {
                "apple", "dash"
         });
         final Array<Integer> creatures = api.loadCreatures(characters);
         api.assignCreatureToPlayer(creatures.get(0), Players.ONE, 0, 0);
        api.assignCreatureToPlayer(creatures.get(1), Players.TWO, 9, 6);
         // //////////////////////////////////////////////////
         inputSystem.addProcessor(stage);
         inputSystem.addProcessor(creatureSelectInput);
         inputSystem.addProcessor(targetSelectInput);
         inputSystem.addProcessor(generalInput);
         Gdx.input.setInputProcessor(inputSystem);
         state.setCurrentState(GameState.BEGIN_TURN);
         return true;
     }
 
     @Override
     public void update(Observable obs, Object currentState) {
         if (currentState instanceof GameState) {
             final GameState gameState = (GameState)currentState;
             switch (gameState) {
                 case BEGIN_TURN:
                     onBeginTurnSystem.process();
                     this.onBeginTurn();
                     state.setActivePlayer(Players.ONE);
                     state.setCurrentState(GameState.CHOOSING_CHARACTER);
                 case CHOOSING_CHARACTER:
                     this.onChoosingCharacter();
                     break;
                 case CHOOSING_SKILL:
                     this.onSkillSelect();
                     break;
                 case CHOOSING_TARGET:
                     break;
                 case CHOOSING_CONFIRM:
                     this.onConfirm();
                     break;
                 case PLAYER_FINISHED:
                     state.clearSelection();
                     map.clearHighlights();
                     state.clearSelectedIds();
                     if (Players.ONE == state.getActivePlayer()) {
                         state.setActivePlayer(Players.TWO);
                         state.setCurrentState(GameState.CHOOSING_CHARACTER);
                     } else if (Players.TWO == state.getActivePlayer()) {
                         onExecuteTurnSystem.process();
                         state.setCurrentState(GameState.END_TURN);
                     }
                     break;
                 case END_TURN:
                     this.onEndTurn();
                     onEndTurnSystem.process();
                     state.setCurrentState(GameState.BEGIN_TURN);
                     break;
             }
         }
     }
 
     private boolean onBeginTurn() {
         this.checkForVictory();
         this.restoreEnergy(Players.ONE);
         this.restoreEnergy(Players.TWO);
         this.refreshSkills();
         return false;
     }
 
     private void onChoosingCharacter() {
         map.clearHighlights();
         state.clearSelection();
         playerMenu.clear();
         creatureMenu.clear();
         playerMenu.addButton("End Turn", new ChangeListener() {
             @Override
             public void changed(ChangeEvent event, Actor actor) {
                 state.setCurrentState(GameState.PLAYER_FINISHED);
             }
         }, true);
         playerMenu.addToStage(stage, 30, Gdx.graphics.getHeight() - 30);
     }
 
     private void onSkillSelect() {
         playerMenu.addButton("Deselect", new ChangeListener() {
             @Override
             public void changed(ChangeEvent event, Actor actor) {
                 state.getSelectedIds().removeValue(state.getSelectedCreature(), true);
                 state.setCurrentState(GameState.CHOOSING_CHARACTER);
             }
         }, true);
         final SkillSet skillSet = ssm.getSafe(world.getEntity(state.getSelectedCreature()));
         final Entity player = world.getManager(TagManager.class).getEntity(
                 state.getActivePlayer().toString());
         final EnergyBar bar = ebm.getSafe(player);
         for (final Integer skillId : skillSet.skillSet) {
             final Entity skill = world.getEntity(skillId);
             final Cost cost = com.getSafe(skill);
             final String skillName = ocm.getSafe(skill).name;
             final boolean isActive = (bar.getCurrentEnergy() - cost.getCostAfterModifiers()) >= 0;
             creatureMenu.addButton(skillName, new ChangeListener() {
                 @Override
                 public void changed(ChangeEvent event, Actor actor) {
                     VersusGame.this.selectSkill(skillId);
                 }
             }, isActive);
         }
         playerMenu.addToStage(stage, 30, Gdx.graphics.getHeight() - 30);
         creatureMenu.addToStage(stage, 30, Gdx.graphics.getHeight() - 90);
     }
 
     private void onConfirm() {
         confirm = new FramedDialog(skin, "", "Are you sure?", 250, 100);
         confirm.addButton("Yes", new ChangeListener() {
             @Override
             public void changed(ChangeEvent event, Actor actor) {
                 VersusGame.this.confirmAction();
             }
         });
         confirm.addButton("No", new ChangeListener() {
             @Override
             public void changed(ChangeEvent event, Actor actor) {
                 map.clearHighlights();
                 state.setCurrentState(GameState.CHOOSING_CHARACTER);
             }
         });
         confirm.addToStage(stage, 30, stage.getHeight() - 400);
     }
 
     private boolean onEndTurn() {
         return false;
     }
 
     public boolean selectCreature(int creatureId) {
         if ((creatureId > -1) && !state.getSelectedIds().contains(creatureId, false)) {
             final Entity e = world.getEntity(creatureId);
             final Owner owner = om.getSafe(e);
             if (owner.getOwner() == state.getActivePlayer()) {
                 state.setSelectedCreature(creatureId);
                 state.setCurrentState(GameState.CHOOSING_SKILL);
             }
             return true;
         }
         return false;
     }
 
     public boolean selectSkill(int skillId) {
         if (skillId > -1) {
             final Entity skill = world.getEntity(skillId);
             final Entity player = world.getManager(TagManager.class).getEntity(
                     state.getActivePlayer().toString());
             final Cost cost = com.getSafe(skill);
             final Cooldown cooldown = cdm.getSafe(skill);
             final EnergyBar bar = ebm.getSafe(player);
             if (cooldown.isReady()
                     && ((bar.getCurrentEnergy() - cost.getCostAfterModifiers()) >= 0)) {
                 final MapPosition origin = mm.getSafe(world.getEntity(state.getSelectedCreature()));
                 final Range range = rm.getSafe(skill);
                 map.addHighlights(map.tools.getCircularRange(origin.getPosition(),
                         range.getMinRangeAfterModifiers(), range.getMaxRangeAfterModifiers()));
                 state.setSelectedSkill(skillId);
                 state.setCurrentState(GameState.CHOOSING_TARGET);
                 return true;
             } else {
                 // TODO SKILL TOO EXPENSIVE!
             }
         }
         return false;
     }
 
     public boolean selectHexagon(Pair coords) {
         if (map.highlighted.contains(coords, false)) {
             state.setSelectedHex(coords);
             final Entity skill = world.getEntity(state.getSelectedSkill());
             final MapPosition origin = mm.getSafe(world.getEntity(state.getSelectedCreature()));
             map.clearHighlights();
             final ScriptExecutable cast = ocm.getSafe(skill);
             try {
                 cast.interpreter.set("game", api);
                 cast.interpreter.set("caster", state.getSelectedCreature());
                 cast.interpreter.set("origin", origin.getPosition());
                 cast.interpreter.set("target", state.getSelectedHex());
                 final PositionArray highlights = (PositionArray)cast.interpreter.eval("affected()");
                 map.addHighlights(highlights);
             } catch (final EvalError e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             state.setCurrentState(GameState.CHOOSING_CONFIRM);
             return true;
         }
         return false;
     }
 
     public boolean confirmAction() {
         if (state.getSelectedCreature() > -1) {
             final Entity creature = world.getEntity(state.getSelectedCreature());
             if (null != creature) {
                 state.getSelectedIds().add(state.getSelectedCreature());
                 final MapPosition position = mm.get(creature);
                 final ActionSequence sequence = asm.get(creature);
                 sequence.onCastActions.add(new Action(state.getSelectedCreature(), state
                         .getSelectedSkill(), position.getPosition(), state.getSelectedHex()));
                 creature.changedInWorld();
                 final Entity player = world.getManager(TagManager.class).getEntity(
                         state.getActivePlayer().toString());
                 final Entity skill = world.getEntity(state.getSelectedSkill());
                 final Cost cost = com.getSafe(skill);
                 final EnergyBar bar = ebm.getSafe(player);
                 bar.modifyCurrentEnergyBy(-cost.getCostAfterModifiers());
                 player.changedInWorld();
                 final Cooldown cooldown = cdm.getSafe(skill);
                 cooldown.setCurrentCooldown(0);
                 skill.changedInWorld();
                 state.getSelectedIds().add(state.getSelectedCreature());
                 // TODO removed autoturns
                 String group;
                 if (state.getActivePlayer() == Players.ONE) {
                     group = Groups.PLAYER_ONE_CREATURE;
                 } else {
                     group = Groups.PLAYER_TWO_CREATURE;
                 }
                 if (state.getSelectedIds().size >= world.getManager(GroupManager.class)
                         .getEntities(group).size()) {
                     state.setCurrentState(GameState.PLAYER_FINISHED);
                 } else {
                     state.setCurrentState(GameState.CHOOSING_CHARACTER);
                 }
                 return true;
             }
         }
         return false;
     }
 
     // TODO unused ATM
     // TODO remove player from state.selectedIds
     public boolean undoLatestAction(int creatureId) {
         if (creatureId > -1) {
             try {
                 final Action removed = world.getEntity(state.getSelectedCreature()).getComponent(
                         ActionSequence.class).onCastActions.removeLast();
                 final Entity player = world.getManager(TagManager.class).getEntity(
                         state.getActivePlayer().toString());
                 final Entity skill = world.getEntity(removed.skillId);
                 final Cost cost = com.getSafe(skill);
                 final EnergyBar bar = ebm.getSafe(player);
                 bar.modifyCurrentEnergyBy(cost.getCostAfterModifiers());
                 player.changedInWorld();
             } catch (final NoSuchElementException e) {
                 e.printStackTrace();
             }
         }
         return false;
     }
 
     private void restoreEnergy(Players player) {
         final Entity playerEntity = world.getManager(TagManager.class).getEntity(player.toString());
         final EnergyBar bar = ebm.getSafe(playerEntity);
         final String creatureGroup = player.equals(Players.ONE) ? Groups.PLAYER_ONE_CREATURE
                 : Groups.PLAYER_TWO_CREATURE;
         final ImmutableBag<Entity> creatures = world.getManager(GroupManager.class).getEntities(
                 creatureGroup);
         for (int i = 0; i < creatures.size(); i++) {
             final EnergyRegen regen = erm.getSafe(creatures.get(i));
             bar.modifyCurrentEnergyBy(regen.getEnergyRegenAfterModifiers());
         }
         playerEntity.changedInWorld();
     }
 
     private void refreshSkills() {
         final ImmutableBag<Entity> skills = world.getManager(GroupManager.class).getEntities(
                 Groups.PLAYER_SKILL);
         for (int i = 0; i < skills.size(); i++) {
             final Cooldown cooldown = cdm.getSafe(skills.get(i));
             cooldown.refreshOnce();
         }
     }
 
     private void checkForVictory() {
         String group = Groups.PLAYER_ONE_CREATURE;
         final int creaturesP1 = world.getManager(GroupManager.class).getEntities(group).size();
         group = Groups.PLAYER_TWO_CREATURE;
         final int creaturesP2 = world.getManager(GroupManager.class).getEntities(group).size();
         if ((creaturesP1 == 0) || (creaturesP2 == 0)) {
             final FileHandle skinFile = Gdx.files.internal("resources/uiskin/uiskin.json");
             final Skin skin = new Skin(skinFile);
             final int winner = (creaturesP1 == 0) ? 2 : 1;
             new FramedDialog(skin, "", "VICTORY FOR PLAYER " + winner, 300, 120).addToStage(stage,
                     400, 250);
         }
     }
 }
