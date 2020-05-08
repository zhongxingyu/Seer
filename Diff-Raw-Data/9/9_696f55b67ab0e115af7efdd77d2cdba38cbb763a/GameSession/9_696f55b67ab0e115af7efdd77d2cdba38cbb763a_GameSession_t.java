 /**
  * Copyright (c) 2011-2012 Henning Funke.
  * 
  * This file is part of Battlepath.
  *
  * Battlepath is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * Battlepath is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package game;
 
 import interaction.KeyBindings;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import main.Battlepath;
 
 import util.Rectangle2D;
 import util.SafeList;
 import util.Vector2D;
 import engine.GlobalInfo;
 import engine.Pathplanner;
 import entities.Entity;
 import entities.Unit;
 
 /**
  * @author henning
  *
  */
 public class GameSession implements Session {
 	public Pathplanner pathPlanner;
 	public SafeList<Swarm> swarms = new SafeList<Swarm>();
 	public Swarm swarm = null;
 	public ArrayList<Team> teams = new ArrayList<Team>();
 	public Team playerteam = null;
 	public GameMode mode = GameMode.STRATEGY;
 	public boolean found = false;
 	double lastShot = 0;
 	
 	public GameSession(Vector2D startpos, ArrayList<Team> teams, int team) {
 		this.teams = teams;
 		this.playerteam = teams.get(team);
 		Core.entitySystem.add(new Unit(startpos, playerteam));
 	}
 
 
 	@Override
 	public void initialize() {
 		Core.useSelectionRect = true;
 		
 		
 		ArrayList<Entity> testResult;
 		
 		Core.entitySystem.add(new Unit(new Vector2D(30,30),new Team("bla",1)));
 		Core.entitySystem.arrange();
 		testResult = Core.entitySystem.entitiesInRange(new Vector2D(28,30), 3);
 		
 		System.out.println(testResult.size());
 	}
 
 
 	@Override
 	public void step(double dt) {
 		
 		if(swarm == null) {
 			this.setMode(GameMode.STRATEGY);
 		}
 		if(mode==GameMode.ACTION) {
 			Core.view.follow(swarm.getLeader());
 		}
 		
 		for(Entity e : Core.entitySystem.entities) {
 			if(e instanceof Unit) {
 				if(Core.selectionRect != null) {
 					if(Core.selectionRect.inside(e.pos) && e.team == playerteam) {
 						((Unit) e).isSelected = true;
 					}
 					else {
 						((Unit) e).isSelected = false;
 					}
 				}
 			}
 		}
 		
 		
 		for(Swarm s : swarms) {
 			s.process();
 			if(!s.alive) swarms.remove(s);
 		}
 		swarms.applyChanges();
 		
 	}
 	
 	/**
 	 * Processes all user inputs, mouse and keyboard. Gets its information about that from Input class.
 	 * @param dt time step
 	 */
 	public void processInputState(double dt) {
 		Unit controlledU = null;
 		if(swarm != null) controlledU = swarm.getLeader();
 		ArrayList<Unit> selected = Core.entitySystem.selected();
 		
 		//Mouse
 		if(mode == GameMode.STRATEGY) {
 			if(Core.input.mouseButtonPressed[0]  && !Core.input.lastMouseState[0]) {
 				found = false;
 				for(Entity e : Core.entitySystem.entities) {
 					if(!(e instanceof Unit)) continue;
 					Unit u = (Unit)e;
 					if(Core.input.getCursorPos().distance(e.pos) < e.getRadius()) {
 						if(!u.isSelected && u.team == playerteam) {
 							((Unit) e).isSelected = true;
 						}
 						found = true;
 					}
 				}
 			}
 			
			if(Core.input.mouseButtonPressed[2] && !Core.input.lastMouseState[2] && selected.size() != 0) {
 				for(Entity e : selected) {
 					Unit u = (Unit)e;
 					u.moveTo(Core.input.getCursorPos());
 				}
 			}
 		}
 		
 		if(mode == GameMode.ACTION) {
 			if(Core.input.mouseButtonPressed[0]) {
 				swarm.shootAt(Core.input.getCursorPos());
 			}
 		}
 		
 			
 		//Keyboard part one (input.isPressed)
 		if(selected.size() != 0 & mode == GameMode.ACTION) {
 			
 			Vector2D direction = GlobalInfo.nullVector.copy();
 			
 			if(Core.input.isPressed(KeyBindings.MOVE_LEFT)) direction.x = 1;
 			else if(Core.input.isPressed(KeyBindings.MOVE_RIGHT)) direction.x = -1;
 			else direction.x = 0.0;
 			
 			if(Core.input.isPressed(KeyBindings.MOVE_DOWN)) direction.y = -1;
 			else if(Core.input.isPressed(KeyBindings.MOVE_UP)) direction.y = 1;
 			else direction.y = 0.0;
 			
 			controlledU.setDirection(direction);
 		}
 	}
 	
 	public void processKey(int key) {
 		switch(key) {
 		/*DEBUGGING KEYS*/
 		case 't':
 			toggleMode();
 			break;
 		case 'r':
 			// random team
 			java.util.Random random = new Random();
 			int team = random.nextInt(teams.size());
 			Core.entitySystem.add(new Unit(Battlepath.findStartPos(Core.field), teams.get(team)));
 			break;
 		case 'e':
 			swarmify();
 			break;
 		case 'q':
 			if(Core.entitySystem.selectedUnits.size() > 0)
 				if(!Core.entitySystem.selectedUnits.get(0).isMenuVisible())
 					Core.entitySystem.selectedUnits.get(0).openMenu();
 				else
 					Core.entitySystem.selectedUnits.get(0).closeMenu();
 			break;
 		}
 	}
 
 	
 	/**
 	 * Sets the game mode. See GameMode.java for more info
 	 * @param gm new mode
 	 */
 	public void setMode(GameMode gm) {
 		switch(gm) {
 		case ACTION:
 			if(swarms.size() == 0) {
 				swarmify();
 			}
 			if(swarm != null) {
 				mode = gm;
 				Core.view.follow(swarm.getLeader());
 			}
 			Core.useSelectionRect = false;
 			break;
 		case STRATEGY:
 			mode = gm;
 			Core.view.unfollow();
 			if(Core.entitySystem.selected().size() != 0){
 				Core.entitySystem.selected().get(0).velocity = new Vector2D(0,0);
 			}
 			Core.useSelectionRect = true;
 			break;
 		}
 	}
 	
 	/**
 	 * Switch between ACTION and STRATEGY mode.
 	 */
 	public void toggleMode() {
 		if(mode == GameMode.ACTION) {
 			setMode(GameMode.STRATEGY);
 		}
 		else if(mode == GameMode.STRATEGY) {
 			setMode(GameMode.ACTION);
 		}
 	}
 	
 
 	
 	public void swarmify() {
 		if(Core.entitySystem.selected().size() > 0) {
 			swarm = new Swarm(Core.entitySystem.selected());
 			swarms.add(swarm);
 		}
 	}
 	
 }
