 package com.slauson.tactics.controller;
 
 import com.slauson.tactics.ai.Move;
 import com.slauson.tactics.model.Neighbor;
 import com.slauson.tactics.model.Overworld;
 import com.slauson.tactics.model.Overworld.Phase;
 import com.slauson.tactics.model.Player;
 import com.slauson.tactics.model.Region;
 import com.slauson.tactics.model.Unit;
 import com.slauson.tactics.utils.BattleUtils;
 import com.slauson.tactics.utils.PlayerUtils;
 import com.slauson.tactics.utils.RegionUtils;
 
 /**
  * Controls the overworld.
  * @author josh
  *
  */
 public class OverworldController extends Controller {
 	
 	private static float TIME_PER_MOVE = 2;
 	
 	private Overworld overworld;
 	private Region selectedRegion;
 	
 	private Move currentMove;
 	private float currentMoveTime;
 	private boolean paused;
 	
 	public OverworldController(Overworld overworld) {
 		this.overworld = overworld;
 		
 		selectedRegion = null;
 		currentMove = new Move(Move.Type.DUMMY);
 		currentMoveTime = 0;
 		paused = false;
 	}
 	
 	@Override
 	public void update(float delta) {
 		if (delta > MAX_DELTA) {
 			delta = MAX_DELTA;
 		}
 
 		if (!paused && overworld.activePlayer().type != Player.Type.PLAYER) {
 			handleMove(delta);
 		}
 	}
 	
 	@Override
 	public boolean touchDown(float worldX, float worldY) {
 		
 		// only allow selecting regions for player controlled players
 		if (overworld.activePlayer().type != Player.Type.PLAYER) {
 			return false;
 		}
 		
 		Region region = overworld.getContainingRegion(worldX, worldY);
 		if (region != null) {
 				
 			System.out.println("touched: " + region);
 			
 			switch (overworld.phase) {
 			case ATTACK:
 				// there's a currently selected region
 				if (selectedRegion != null) {
 					
 					System.out.println("deselected: " + selectedRegion);
 					
 					// unselect previously selected region
 					selectedRegion.selected = false;
 					
 					// unmark previously selected region neighbors
 					RegionUtils.unmarkRegionNeighbors(selectedRegion);
 					
 					// if selected again, just unselect
 					if (selectedRegion == region) {
 						selectedRegion = null;
 						return true;
 					}
 					
 					// region is in selected region's neighbors
 					Neighbor.Type neighborType = selectedRegion.neighbors.getNeighborType(region);
 					if (neighborType != null) {
 						
 						// attack
 						if (selectedRegion.unit.hasAttack && !selectedRegion.player.equals(region.player) && region.unit != null &&
 								((selectedRegion.unit.type.isRanged() && neighborType.isRanged()) || (!selectedRegion.unit.type.isRanged() && !neighborType.isRanged())))
 						{
 							Region updatedAttackingRegion = BattleUtils.handleBattle(selectedRegion, region);
 							
 							// keep region selected if attacker won battle and can still move
 							if (updatedAttackingRegion != null && updatedAttackingRegion.unit.hasMove) {
 								selectedRegion = updatedAttackingRegion;
 								selectedRegion.selected = true;
 								
 								RegionUtils.markRegionNeighbors(selectedRegion);
 							} else {
 								selectedRegion = null;
 							}
 							
 							return true;
 						}
 						// move (unoccupied region)
 						else if (selectedRegion.unit.hasMove && region.unit == null && neighborType != Neighbor.Type.RANGED) {
 							// update region count if not owned by player
 							if (region.player != selectedRegion.player) { 
 								selectedRegion.player.regions++;
 							}
 							
 							region.unit = selectedRegion.unit;
 							region.player = selectedRegion.player;
 							region.unit.hasMove = false;
 							selectedRegion.unit = null;
 							
 							// keep region selected if can still attack
 							if (region.unit.hasAttack) {
 								selectedRegion = region;
 								selectedRegion.selected = true;
 							
 								RegionUtils.markRegionNeighbors(selectedRegion);
 							} else {
 								selectedRegion = null;
 							}
 							return true;
 						}
 						// move (player owned region)
 						else if (selectedRegion.unit.hasMove && selectedRegion.player == region.player && region.unit != null&& region.unit.hasMove && neighborType != Neighbor.Type.RANGED) {
 							// swap units
 							Unit temp = region.unit;
 							region.unit = selectedRegion.unit;
 							selectedRegion.unit = temp;
 							
 							region.unit.hasMove = false;
 							selectedRegion.unit.hasMove = false;
 							
 							if (region.unit.hasAttack) {
 								selectedRegion = region;
 								selectedRegion.selected = true;
 							
 								RegionUtils.markRegionNeighbors(selectedRegion);
 							} else {
 								selectedRegion = null;
 							}
 						} else {
 							selectedRegion = null;
 						}
 					}
 				}
 				
 				// only allow selecting regions with units for player who has current turn and are active
 				if (region.unit != null &&
 						region.player == overworld.activePlayer() &&
 						(region.unit.hasAttack || region.unit.hasMove))
 				{
 				
 					// select new selected region
 					selectedRegion = region;
 					selectedRegion.selected = true;
 					
 					System.out.println("selected: " + selectedRegion);
 					
 					RegionUtils.markRegionNeighbors(selectedRegion);
 				}
 				break;
 			case REINFORCE:
 				if (region.player == overworld.activePlayer()) {
 					
 					// existing unit
 					if (overworld.activePlayer().reinforcements > 0 && region.unit != null && region.unit.health < Unit.MAX_HEALTH) {
 						
 						// mark previously selected region
 						if (selectedRegion != null) {
 							selectedRegion.marked = true;
 							selectedRegion.selected = false;
 						}
 						
 						region.unit.health = Unit.MAX_HEALTH;
 						region.marked = true;
 						selectedRegion = null;
 						overworld.activePlayer().reinforcements--;
 					}
 					// new unit
 					else if (overworld.activePlayer().reinforcements > 0 && region.unit == null) {
 						
 						// mark previously selected region
 						if (selectedRegion != null) {
 							selectedRegion.marked = true;
 							selectedRegion.selected = false;
 						}
 						
 						region.unit = new Unit(Unit.Type.values()[0], Unit.MAX_HEALTH);
 						selectedRegion = region;
 						selectedRegion.selected = true;
 						overworld.activePlayer().reinforcements--;
 					}
 					// switch unit type of new unit
 					else if (region == selectedRegion) {
 						region.unit.type = region.unit.type.next();
 					}
 					
 					System.out.println("reinforced: " + region);
 				}
 				break;
 			}
 			
 			return true;
 		}
 		
 		return false;
 	}
 	
 
 	@Override
 	public boolean keyTyped(char character) {
 		switch (character) {
 		case ' ':
 			if (overworld.activePlayer().type == Player.Type.PLAYER) {
 				// unmark previously selected region
 				if (selectedRegion != null) {
 					RegionUtils.unmarkRegionNeighbors(selectedRegion);
 					selectedRegion.selected = false;
 					selectedRegion = null;
 				}
 				
 				// move to next phase
 				nextPhase();
 				
 				return true;
 			}
 		case 'p':
 			paused = !paused;
 			break;
 		}
 		
 		return false;
 	}
 	
 	private void nextPhase() {
 		overworld.nextPhase();
 		
 		// next turn
 		if (overworld.phase == Phase.ATTACK) {
 			
 			RegionUtils.unmarkAllRegions(overworld);
 			
 			// move to next turn
 			overworld.nextTurn();
 			
 			PlayerUtils.setPlayerUnitsActive(overworld, overworld.activePlayer());
 		} else {
 			// handle reinforcements
 			PlayerUtils.updatePlayerReinforcements(overworld, overworld.activePlayer());
 			PlayerUtils.markPlayerReinforcements(overworld, overworld.activePlayer());
 		}
 	}
 	
 	private void handleMove(float delta) {
 		currentMoveTime -= delta;
 		
 		if (currentMoveTime < 0) {
 			
 			currentMove.numPhases--;
 			
 			// numPhases - 1
 			switch (currentMove.numPhases) {
 			case 1:
 				switch (currentMove.type) {
 				case ATTACK:
 				case MOVE:
 					// select region
 					currentMove.region.selected = true;
 					
 					// mark neighbors
 					RegionUtils.markRegionNeighbors(currentMove.region);
 					break;
 				case REINFORCE:
 				case END_PHASE:
 				default:
 					break;
 				}
 				currentMoveTime = TIME_PER_MOVE;
 				break;
 			case 0:
 				switch (currentMove.type) {
 				case ATTACK:
 					Region updatedAttackingRegion = BattleUtils.handleBattle(currentMove.region, currentMove.otherRegion);
 					
 					// deselect region and unmark neighboring regions
 					currentMove.region.selected = false;
 					RegionUtils.unmarkRegionNeighbors(currentMove.region);
 					
 					if (updatedAttackingRegion != null) {
 						
 						// remove reference to region we're attacking if it was defeated
 						if (updatedAttackingRegion.equals(currentMove.otherRegion)) {
 							currentMove.otherRegion = null;
 						}
 						
 						currentMove.region = updatedAttackingRegion;
 						
 						// reselect and re-mark neighbors only if attacker can move
 						if (currentMove.region.unit != null && currentMove.region.unit.hasMove) {
 							currentMove.region.selected = true;
 							RegionUtils.markRegionNeighbors(currentMove.region);
 						}
 					}
 					break;
 				case MOVE:
					
					// takeover empty region
					if (currentMove.otherRegion.unit == null) {
						currentMove.otherRegion.player = currentMove.region.player;
					}
					
 					// swap units
 					Unit temp = currentMove.region.unit;
 					currentMove.region.unit = currentMove.otherRegion.unit;
 					currentMove.otherRegion.unit = temp;
 					
 					// deselect region and unmark neighboring regions
 					currentMove.region.selected = false;
 					RegionUtils.unmarkRegionNeighbors(currentMove.region);
 					
 					// unit can't move again
 					if (currentMove.region.unit != null) {
 						currentMove.region.unit.hasMove = false;
 					}
 					if (currentMove.otherRegion.unit != null) {
 						currentMove.otherRegion.unit.hasMove = false;
 						
 						// keep marked if unit can still attack
 						if (currentMove.otherRegion.unit.hasAttack) {
 							currentMove.otherRegion.selected = true;
 							RegionUtils.markRegionNeighbors(currentMove.otherRegion);
 						}
 					}
 					break;
 				case REINFORCE:
 					// mark region
 					currentMove.region.marked = true;
 					
 					// new unit
 					if (currentMove.region.unit == null) {
 						currentMove.region.unit = new Unit(currentMove.unitType, Unit.MAX_HEALTH);
 						overworld.activePlayer().reinforcements--;
 					}
 					// existing unit
 					else {
 						currentMove.region.unit.health = Unit.MAX_HEALTH;
 						overworld.activePlayer().reinforcements--;
 					}
 					break;
 				case END_PHASE:
 				default:
 					nextPhase();
 					break;
 				}
 				currentMoveTime = TIME_PER_MOVE;
 				break;
 			case -1:
 				
 				// deselect and unmark neighbors for attack moves
 				if (currentMove.type == Move.Type.ATTACK && currentMove.region != null) {
 					currentMove.region.selected = false;
 					RegionUtils.unmarkRegionNeighbors(currentMove.region);
 				}
 				
 				// deselect and unmark neighbors for move moves
 				if (currentMove.type == Move.Type.MOVE && currentMove.otherRegion != null) {
 					currentMove.otherRegion.selected = false;
 					RegionUtils.unmarkRegionNeighbors(currentMove.otherRegion);
 				}
 				
 				// get next move
 				currentMove = overworld.activePlayer().ai.getNextMove(overworld, overworld.activePlayer());
 				
 				System.out.println("next move: " + currentMove);
 				// don't reset move time here so we go directly into first phase of next move
 				break;
 			}
 		}
 	}
 }
