 package org.aimas.craftingquest.core;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.aimas.craftingquest.state.BasicUnit;
 import org.aimas.craftingquest.state.Blueprint;
 import org.aimas.craftingquest.state.CellState;
 import org.aimas.craftingquest.state.CraftedObject;
 import org.aimas.craftingquest.state.GameState;
 import org.aimas.craftingquest.state.Merchant;
 import org.aimas.craftingquest.state.PlayerState;
 import org.aimas.craftingquest.state.Point2i;
 import org.aimas.craftingquest.state.ResourceAttributes;
 import org.aimas.craftingquest.state.Tower;
 import org.aimas.craftingquest.state.Transition;
 import org.aimas.craftingquest.state.TransitionResult;
 import org.aimas.craftingquest.state.UnitState;
 import org.aimas.craftingquest.state.CellState.CellType;
 import org.aimas.craftingquest.state.CraftedObject.BasicResourceType;
 import org.aimas.craftingquest.state.Transition.ActionType;
 
 /**
  * 
  * @author Razvan
  */
 public class ActionEngine {
 
 	GameState game;
 
 	public ActionEngine(GameState gameState) {
 		game = gameState;
 	}
 
 	@SuppressWarnings("unchecked")
 	public TransitionResult process(PlayerState player, Transition transition) {
 		refresh(player);
 		
 		// first check if the operator is of Nothing or RequestState type
 		// these are just for filling up and synchronization - so return the OK
 		if(transition.operator == ActionType.Nothing || transition.operator == ActionType.RequestState) {
 			TransitionResult res = new TransitionResult(transition.id);
 			res.errorType = TransitionResult.TransitionError.NoError;
 			return res;
 		}
 		
 		Integer unitID = (Integer)transition.operands[0];
 		
 		UnitState playerUnit = null;
 		for (UnitState u : player.units) {
 			if (u.id == unitID && u.playerID == player.id) {
 				playerUnit = u;
 				break;
 			}
 		}
 		
 		switch (transition.operator) {
 		case Move:
 		{
 			// check allowed distance
 			Point2i toPos = (Point2i)transition.operands[1];
 			Point2i fromPos = playerUnit.pos;
 			if (Math.abs(toPos.x - fromPos.x) > 1 || Math.abs(toPos.y - fromPos.y) > 1) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.MoveError;
 				res.errorReason = "Move allowed only to neighboring cells";
 				return res;
 			}
 			
 			// check position bounds
 			if (toPos.x < 0 || toPos.y < 0 || toPos.x >= game.map.cells.length || toPos.y >= game.map.cells.length) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.MoveError;
 				res.errorReason = "Move allowed only within map bounds";
 				return res;
 			}
 			
 			// check allowed terrain type
 			CellType toCellType = game.map.cells[toPos.y][toPos.x].type;
 			if ( !GamePolicy.terrainMovePossibilities.get(toCellType).contains(playerUnit.type) ) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.TerrainError;
 				res.errorReason = "Move allowed only on appropriate terrain type";
 				return res;
 			}
 			
 			// check no object is there
 			if ( game.map.cells[toPos.y][toPos.x].strategicResource != null ) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.ObstacleError;
 				res.errorReason = "Move not allowed to cells containing strategic structures.";
 				return res;
 			}
 			
 			// check enough energy for action
 			int carriedResourcesAmount = 0;
 			for (Integer quant : playerUnit.carriedResources.values()) {
 				carriedResourcesAmount += quant;
 			}
 			for (Integer quant : playerUnit.carriedObjects.values()) {
 				carriedResourcesAmount += quant;
 			}
 			int requiredEnergy = (int)(GamePolicy.moveBase * (1.0 + GamePolicy.movePenalty.get(toCellType)) 
 					+ GamePolicy.resourceMoveCost * carriedResourcesAmount / 2);
 			
 			if ( playerUnit.energy < requiredEnergy) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.NoEnergyError;
 				res.errorReason = "Not enough energy points left for move";
 				return res;
 			}
 			
 			// all ok..then move - update unit position, sightMatrix and energy points
 			playerUnit.pos = toPos;
 			playerUnit.energy -= requiredEnergy;
 			updateUnitSight(playerUnit, toPos);
 			
 			// update cells with new unit position
 			Iterator<BasicUnit> it = game.map.cells[fromPos.y][fromPos.x].cellUnits.iterator();
 			while(it.hasNext()) {
 				BasicUnit u = it.next();
 				if (u.playerID == playerUnit.playerID && u.type == playerUnit.type) {
 					it.remove();
 					break;
 				}
 			}
 			game.map.cells[toPos.y][toPos.x].cellUnits.add(playerUnit.getOpponentPerspective());
 			
 			TransitionResult moveres = new TransitionResult(transition.id);
 			moveres.errorType = TransitionResult.TransitionError.NoError;
 			return moveres;
 		}
 		
 		case Dig:
 		{
 			
 			// check enough energy points
 			if (playerUnit.energy < GamePolicy.digCost) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.NoEnergyError;
 				res.errorReason = "Not enough energy points left for digging";
 				return res;
 			}
 			
 			// all ok - dig - return list of existing resources in this cell and subtract energy points
 			playerUnit.currentCellResources.clear();
 			CellState currentCell = game.map.cells[playerUnit.pos.y][playerUnit.pos.x];
 			Iterator<BasicResourceType> resIterator = currentCell.resources.keySet().iterator();
 			while(resIterator.hasNext()) {
 				BasicResourceType res = resIterator.next();
 				if (currentCell.resources.get(res) > 0) {
 					playerUnit.currentCellResources.put(res, currentCell.resources.get(res));
 				}
 			}
 			playerUnit.energy -= GamePolicy.digCost;
 			
 			TransitionResult digres = new TransitionResult(transition.id);
 			digres.errorType = TransitionResult.TransitionError.NoError;
 			return digres;
 		}
 		
 		case ScanLand:
 		{	
 			// check enough energy points
 			if (playerUnit.energy < GamePolicy.scanCost) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.NoEnergyError;
 				res.errorReason = "Not enough energy points left for scanning";
 				return res;
 			}
 			
 			// all ok - scan - return attributes of surrounding cells and subtract energy points
 			CellState scanCell = game.map.cells[playerUnit.pos.y][playerUnit.pos.x];
 			playerUnit.scannedResourceAttributes = getScannedResourceAttributes(scanCell.pos);
 			playerUnit.energy -= GamePolicy.scanCost;
 			TransitionResult scanres = new TransitionResult(transition.id);
 			scanres.errorType = TransitionResult.TransitionError.NoError;
 			return scanres;
 		}	
 		
 		case PickupResources:
 		{
 			HashMap<BasicResourceType, Integer> requiredResources = (HashMap<BasicResourceType, Integer>)transition.operands[1];
 			
 			// check for valid operand
 			if (requiredResources == null) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.OperandError;
 				res.errorReason = "Submitted operand is not valid (null or wrong type).";
 				return res;
 			}
 			
 			// check that enough energy points are available for this operation
 			if (playerUnit.energy < GamePolicy.pickupCost) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.NoEnergyError;
 				res.errorReason = "Not enough energy points left for picking up resources";
 				return res;
 			}
 			
 			// check that no opponent tower is guarding the resources
 			boolean resourcesGuarded = false;
 			for (Integer pId : game.getPlayerIds()) {
 				if (pId != player.id) {
 					List<Tower> opponentTowers = game.playerTowers.get(pId);
 					for (Tower t : opponentTowers) {
 						if (Math.abs(t.getPosition().x - playerUnit.pos.x) <= GamePolicy.towerCutoffRadius && 
 							Math.abs(t.getPosition().y - playerUnit.pos.y) <= GamePolicy.towerCutoffRadius) {
 							resourcesGuarded = true;
 							break;
 						}
 					}
 				}
 				
 				if (resourcesGuarded) {
 					break;
 				}
 			}
 			
 			if (resourcesGuarded) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.GuardError;
 				res.errorReason = "Cannot pickup resources from guarded area.";
 				return res;
 			}
 			
 			// check that the desired (res, quantity) pairs can be satisfied by the current cell
 			// and do the pickup where conditions are met
 			CellState miningCell = game.map.cells[playerUnit.pos.y][playerUnit.pos.x];
 			
 			HashMap<BasicResourceType, Integer> cellResources = miningCell.resources;
 			HashMap<BasicResourceType, Integer> visibleCellResources = miningCell.visibleResources;
 			HashMap<BasicResourceType, Integer> carriedResources = playerUnit.carriedResources;
 			
 			Iterator<BasicResourceType> it = requiredResources.keySet().iterator();
 			while (it.hasNext()) {
 				BasicResourceType res = it.next();
 				Integer required = requiredResources.get(res);
 				Integer available = cellResources.get(res);
 				Integer availableVisible = visibleCellResources.get(res);
 				Integer total = new Integer(0); 
 				if (available != null) {
 					total += available;
 				}
 				
 				if (availableVisible != null) {
 					total += availableVisible;
 				}
 				
 				if (required <= total) {
 					Integer carried = carriedResources.get(res);
 					if (carried == null) {
 						carriedResources.put(res, required);
 					}
 					else {
 						carriedResources.put(res, carried + required);
 					}
 					
 					if (availableVisible != null) {				// first try and take all resources from
 						if (required <= availableVisible) {		// the visible ones
 							visibleCellResources.put(res, availableVisible - required);
 						}
 						else {
 							visibleCellResources.put(res, 0);  	// consume all visible resources
 							cellResources.put(res, available - (required - availableVisible));
 						}
 					}
 					else {									// otherwise take them from the ones in the soil 
 						cellResources.put(res, available - required);
 					}
 				}
 			}
 			
 			playerUnit.energy -= GamePolicy.pickupCost;		// update energy levels
 			TransitionResult pickupres = new TransitionResult(transition.id);
 			pickupres.errorType = TransitionResult.TransitionError.NoError;
 			return pickupres;
 		}
 		
 		case PickupObjects:
 		{
 			HashMap<CraftedObject, Integer> requiredObjects = (HashMap<CraftedObject, Integer>)transition.operands[1];
 			
 			// check for valid operand
 			if (requiredObjects == null) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.OperandError;
 				res.errorReason = "Submitted operand is not valid (null or wrong type).";
 				return res;
 			}
 			
 			// check that enough energy points are available for this operation
 			if (playerUnit.energy < GamePolicy.pickupCost) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.NoEnergyError;
 				res.errorReason = "Not enough energy points left for picking up resources";
 				return res;
 			}
 			
 			// check that no opponent tower is guarding the resources
 			boolean objectsGuarded = false;
 			for (Integer pId : game.getPlayerIds()) {
 				if (pId != player.id) {
 					List<Tower> opponentTowers = game.playerTowers.get(pId);
 					for (Tower t : opponentTowers) {
 						if (Math.abs(t.getPosition().x - playerUnit.pos.x) <= GamePolicy.towerCutoffRadius && 
 							Math.abs(t.getPosition().y - playerUnit.pos.y) <= GamePolicy.towerCutoffRadius) {
 							objectsGuarded = true;
 							break;
 						}
 					}
 				}
 				
 				if (objectsGuarded) {
 					break;
 				}
 			}
 			
 			if (objectsGuarded) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.GuardError;
 				res.errorReason = "Cannot pickup objects from guarded area.";
 				return res;
 			}
 			
 			// check that the desired (res, quantity) pairs can be satisfied by the current cell
 			// and do the pickup where conditions are met
 			CellState miningCell = game.map.cells[playerUnit.pos.y][playerUnit.pos.x];
 			HashMap<CraftedObject, Integer> cellObjects = miningCell.craftedObjects;
 			HashMap<CraftedObject, Integer> carriedObjects = playerUnit.carriedObjects;
 			
 			Iterator<CraftedObject> it = requiredObjects.keySet().iterator();
 			while (it.hasNext()) {
 				CraftedObject res = it.next();
 				Integer required = requiredObjects.get(res);
 				Integer available = cellObjects.get(res);
 				
 				if (available != null && required <= available) {
 					Integer carried = carriedObjects.get(res);
 					if (carried == null) {
 						carriedObjects.put(res, required);
 					}
 					else {
 						carriedObjects.put(res, carried + required);
 					}
 					
 					carriedObjects.put(res, available - required);
 				}
 			}
 			
 			playerUnit.energy -= GamePolicy.pickupCost;		// update energy levels
 			TransitionResult pickupobjs = new TransitionResult(transition.id);
 			pickupobjs.errorType = TransitionResult.TransitionError.NoError;
 			return pickupobjs;
 		}
 		
 		case DropResources:
 		{
 			HashMap<BasicResourceType, Integer> unwantedResources = (HashMap<BasicResourceType, Integer>)transition.operands[1];
 			
 			// check for valid operand
 			if (unwantedResources == null) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.OperandError;
 				res.errorReason = "Submitted operand is not valid (null or wrong type).";
 				return res;
 			}
 			
 			// check for enough energy points
 			if (playerUnit.energy < GamePolicy.dropCost) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.NoEnergyError;
 				res.errorReason = "Not enough energy points left for dropping resources";
 				return res;
 			}
 			
 			HashMap<BasicResourceType, Integer> visibleCellResources = game.map.cells[playerUnit.pos.y][playerUnit.pos.x].visibleResources;  
 			HashMap<BasicResourceType, Integer> carriedResources = playerUnit.carriedResources;
 			
 			Iterator<BasicResourceType> it = unwantedResources.keySet().iterator();
 			while (it.hasNext()) {
 				BasicResourceType res = it.next();
 				Integer dropped = unwantedResources.get(res);
 				Integer existing = visibleCellResources.get(res);
 				Integer carried = carriedResources.get(res);
 				
 				if (carried != null && dropped <= carried) {
 					if (existing == null) {
 						visibleCellResources.put(res, dropped);
 					}
 					else {
 						visibleCellResources.put(res, existing + dropped);
 					}
 					
 					carriedResources.put(res, carried - dropped);
 				}
 			}
 			
 			playerUnit.energy -= GamePolicy.dropCost;		// update energy levels
 			TransitionResult dropres = new TransitionResult(transition.id);
 			dropres.errorType = TransitionResult.TransitionError.NoError;
 			return dropres;
 		}	
 		
 		case DropObjects:
 		{
 			HashMap<CraftedObject, Integer> unwantedObjects = (HashMap<CraftedObject, Integer>)transition.operands[1];
 			
 			// check for valid operand
 			if (unwantedObjects == null) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.OperandError;
 				res.errorReason = "Submitted operand is not valid (null or wrong type).";
 				return res;
 			}
 			
 			// check for enough energy points
 			if (playerUnit.energy < GamePolicy.dropCost) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.NoEnergyError;
 				res.errorReason = "Not enough energy points left for dropping resources";
 				return res;
 			}
 			
 			HashMap<CraftedObject, Integer> cellObjects = game.map.cells[playerUnit.pos.y][playerUnit.pos.x].craftedObjects;  
 			HashMap<CraftedObject, Integer> carriedObjects = playerUnit.carriedObjects;
 			
 			Iterator<CraftedObject> it = unwantedObjects.keySet().iterator();
 			while (it.hasNext()) {
 				CraftedObject res = it.next();
 				Integer dropped = unwantedObjects.get(res);
 				Integer existing = cellObjects.get(res);
 				Integer carried = carriedObjects.get(res);
 				
 				if (carried != null && dropped <= carried) {
 					if (existing == null) {
 						cellObjects.put(res, dropped);
 					}
 					else {
 						cellObjects.put(res, existing + dropped);
 					}
 					
 					carriedObjects.put(res, carried - dropped);
 				}
 			}
 			
 			playerUnit.energy -= GamePolicy.dropCost;		// update energy levels
 			TransitionResult dropres = new TransitionResult(transition.id);
 			dropres.errorType = TransitionResult.TransitionError.NoError;
 			return dropres;
 		}	
 		
 		case CraftObject:
 		{
 			CraftedObject target = (CraftedObject)transition.operands[1];
 			HashMap<CraftedObject, Integer> usedObjects = (HashMap<CraftedObject, Integer>)transition.operands[2];
 			HashMap<BasicResourceType, Integer> usedResources = (HashMap<BasicResourceType, Integer>)transition.operands[3];
 			
 			// check for valid operand; usedObjects and usedResources are tested in checkCraftingRequirements
 			if (target == null) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.OperandError;
 				res.errorReason = "Submitted operands are not valid (null or wrong type).";
 				return res;
 			}
 			
 			// check for enough energy points
 			if (playerUnit.energy < GamePolicy.buildCost) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.NoEnergyError;
 				res.errorReason = "Not enough energy points left for building an object";
 				return res;
 			}
 			
 			// check that player holds corresponding blueprint
 			boolean foundBlueprint = false;
 			for (Blueprint bp : player.boughtBlueprints) {
 				if (bp.getDescribedObject().getType() == target.getType()) {
 					foundBlueprint = true;
 				}
 			}
 			
 			if (!foundBlueprint) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.CraftingError;
 				res.errorReason = "Object crafting requirements are not met. Missing required blueprint.";
 				return res;
 			}
 			
 			// check that the unit has the required resources/objects required for making the object
 			if ( !checkCraftingRequirements(playerUnit, target, usedObjects, usedResources) ) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.CraftingError;
 				res.errorReason = "Object crafting requirements are not met.";
 				return res;
 			}
 			
 			playerUnit.energy -= GamePolicy.buildCost;		// update energy levels
 			
 			Integer targetObjectCount = playerUnit.carriedObjects.get(target);
 			if (targetObjectCount == null) {				// add new crafted object to the list
 				playerUnit.carriedObjects.put(target, 1);
 			}
 			else {
 				playerUnit.carriedObjects.put(target, targetObjectCount + 1);
 			}
 			
 			TransitionResult craftres = new TransitionResult(transition.id);
 			craftres.errorType = TransitionResult.TransitionError.NoError;
 			return craftres;
 		}
 		
 		case SellObject:
 		{
 			CraftedObject obj = (CraftedObject)transition.operands[1];
 			Integer quantity = (Integer)transition.operands[2];
 			
 			// check for valid operand
 			if (obj == null || quantity == null) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.OperandError;
 				res.errorReason = "Submitted operands are not valid (null or wrong type).";
 				return res;
 			}
 			
 			HashMap<CraftedObject, Integer> carriedObjects = playerUnit.carriedObjects;
 			
 			Integer carried = carriedObjects.get(obj);
 			if (carried != null && quantity <= carried) {
 				player.credit += obj.getValue() * quantity;				// update team score
 				carriedObjects.put(obj, carried - quantity);			// update amount of carried objects
 			}
 			else {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.SellRequestError;
 				res.errorReason = "No (or not enough) crafted objects of given type available to sell";
 				return res;
 			}
 			
 			TransitionResult sellres = new TransitionResult(transition.id);
 			sellres.errorType = TransitionResult.TransitionError.NoError;
 			return sellres;
 		}
 		
 		case PlaceTower:
 		{
 			// check for enough energy points
 			if (playerUnit.energy < GamePolicy.buildCost) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.NoEnergyError;
 				res.errorReason = "Not enough energy points left for constructing a tower";
 				return res;
 			}
 			
 			// check for enough credit
 			if (player.credit < GamePolicy.towerBuildCost) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.NoCreditError;
 				res.errorReason = "Not enough credit left for constructing a tower";
 				return res;
 			}
 			
 			// check to see if any towers are already in the cell
 			CellState unitCell = game.map.cells[playerUnit.pos.y][playerUnit.pos.x];
 			if (unitCell.strategicResource != null) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.BuildError;
 				res.errorReason = "Cannot build a tower in a cell that already contains a strategic resource";
 				return res;
 			}
 			
 			// check to see if any resources are left in the cell
 			boolean emptyCell = true;
 			for (BasicResourceType restype : unitCell.resources.keySet()) {		// first soil resources
 				if (unitCell.resources.get(restype) > 0) {
 					emptyCell = false;
 					break;
 				}
 			}
 			
 			if (emptyCell) {		// if still empty then check for visible resources
 				for (BasicResourceType restype : unitCell.visibleResources.keySet()) {	
 					if (unitCell.visibleResources.get(restype) > 0) {
 						emptyCell = false;
 						break;
 					}
 				}
 			}
 			
 			if (emptyCell) {       // if still empty then check for crafted objects
 				for (CraftedObject obj : unitCell.craftedObjects.keySet()) {
 					if (unitCell.craftedObjects.get(obj) > 0) {
 						emptyCell = false;
 						break;
 					}
 				}
 			}
 			
 			if (!emptyCell) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.BuildError;
 				res.errorReason = "Cannot build a tower in a cell that still contains unmined resources";
 				return res;
 			}
 			
 			// all is ok - build tower and update energy and credit levels
 			Tower tower = new Tower(player.id, playerUnit.pos);
 			unitCell.strategicResource = tower;								// place tower in cell
 			
 			List<Tower> playerTowers = game.playerTowers.get(player.id);	// add in global list of towers
 			if (playerTowers == null) {
 				playerTowers = new ArrayList<Tower>();
 				playerTowers.add(tower);
 				game.playerTowers.put(player.id, playerTowers);
 				player.availableTowers.put(tower, true);		// this tower is newly available
 			}
 			else {
 				playerTowers.add(tower);
 			}
 			
 			player.credit -= GamePolicy.towerBuildCost;			// subtract cost from player credit
 			
 			TransitionResult towerres = new TransitionResult(transition.id);
 			towerres.errorType = TransitionResult.TransitionError.NoError;
 			return towerres;
 		}
 		
 		case BuyBlueprint:
 		{
 			Blueprint blueprint = (Blueprint)transition.operands[1];
 			
 			// check for valid operand
 			if (blueprint == null) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.OperandError;
 				res.errorReason = "Submitted operand is not valid (null or wrong type).";
 				return res;
 			}
 			
 			// check if the unit is near a merchant
 			Merchant nearMerchant = null;
 			for (Merchant m : game.merchantList) {
 				if (Math.abs(m.getPosition().x - playerUnit.pos.x) <= 1 && 
 					Math.abs(m.getPosition().y - playerUnit.pos.y) <= 1 ) {
 					nearMerchant = m;
 					break;
 				}
 			}
 			
 			if (nearMerchant == null) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.BuyRequestError;
 				res.errorReason = "Not near a merchant camp. Cannot request to buy a blueprint.";
 				return res;
 			}
 			
 			// check that the merchant holds the desired blueprint
 			boolean hasBlueprint = false;
 			for (Blueprint bp : nearMerchant.getBlueprints()) {
 				if (bp.getDescribedObject().getType() == blueprint.getDescribedObject().getType()) {
 					hasBlueprint = true;
 				}
 			}
 			
 			if (!hasBlueprint) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.BuyRequestError;
 				res.errorReason = "Merchant does not hold requested blueprint.";
 				return res;
 			}
 			
 			// check for enough credit
 			if (player.credit < blueprint.getValue()) {
 				TransitionResult res = new TransitionResult(transition.id);
 				res.errorType = TransitionResult.TransitionError.NoCreditError;
 				res.errorReason = "Not enough credit to buy the requested blueprint.";
 				return res;
 			}
 			
 			// all is ok - subtract credit, add blueprint to knownBlueprints
 			player.boughtBlueprints.add(blueprint);
 			player.credit -= blueprint.getValue();
 			
 			TransitionResult blueprintres = new TransitionResult(transition.id);
 			blueprintres.errorType = TransitionResult.TransitionError.NoError;
 			return blueprintres;
 		}
 		
 		
 		}
 		
 		TransitionResult res = new TransitionResult(transition.id);
 		return res;
 	}
 	
 	
 	/* =========================================================================================== */
 	
 	
 	// clears transient fields (currentCellResources, scannedAttributes) of each of the players units
 	private void refresh(PlayerState player) {
 		player.response = null;							// reset player transition response
 		
 		for (UnitState unit : player.units) {			// reset each unit's dig and scan results
 			unit.currentCellResources.clear();
 			unit.scannedResourceAttributes = null;
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	private List<int[]>[][] getScannedResourceAttributes(Point2i pos) {
 		int radius = GamePolicy.scanRadius;
 		int dim = radius * 2 + 1;
 		
 		List<int[]>[][] resAttr = new List[dim][dim];
 		for (int y = pos.y - radius, i = 0; y <= pos.y + radius; y++, i++) {
 			for (int x = pos.x - radius, j = 0; x <= pos.x + radius; x++, j++) {
 				resAttr[i][j] = null;
 				if (x >= 0 && x < GamePolicy.mapsize.x && y >= 0 && y < GamePolicy.mapsize.y) {
 					resAttr[i][j] = new ArrayList<int[]>();
 					
 					Iterator<BasicResourceType> resIt = game.map.cells[y][x].scanAttributes.keySet().iterator();
 					while (resIt.hasNext()) {
 						BasicResourceType resType = resIt.next();
 						Integer existing = game.map.cells[y][x].resources.get(resType);
 						if (existing != null && existing > 0) {
 							ResourceAttributes ra = game.map.cells[y][x].scanAttributes.get(resType);
 							resAttr[i][j].add(ra.attributeValues);
 						}
 					}
 				}
 			}
 		}
 		
 		return resAttr;
 	}
 	
 	private void updateUnitSight(UnitState playerUnit, Point2i pos) {
 		int sightRadius = GamePolicy.sightRadius;
 		
 		CellState[][] unitSight = playerUnit.sight;
 		for (int i = 0, y = pos.y - sightRadius; y <= pos.y + sightRadius; y++, i++) {
 			for (int j = 0, x = pos.x - sightRadius; x <= pos.x + sightRadius; x++, j++) {
 				unitSight[i][j] = null;
 				if (x >= 0 && x < GamePolicy.mapsize.x && y >= 0 && y < GamePolicy.mapsize.y) {
 					unitSight[i][j] = game.map.cells[y][x];
 				}
 			}
 		}
 	}
 	
 	private boolean checkCraftingRequirements(UnitState playerUnit, CraftedObject target, 
 			HashMap<CraftedObject, Integer> usedObjects, HashMap<BasicResourceType, Integer> usedResources) {
 		
 		HashMap<CraftedObject, Integer> carriedObjects = playerUnit.carriedObjects;
 		HashMap<BasicResourceType, Integer> carriedResources = playerUnit.carriedResources;
 		
 		if (target.getRequiredObjects() != null) {		// it is an object made out of sub-objects
 			if (usedObjects == null) {
 				return false;
 			}
 			
 			boolean requirementsMet = false;
 			
 			for (HashMap<CraftedObject, Integer> craftingOption : target.getRequiredObjects()) {
 				boolean alternativeOk = true;
 				
 				Iterator<CraftedObject> objIt = craftingOption.keySet().iterator();
 				while(objIt.hasNext()) {
 					CraftedObject obj = objIt.next();
 					Integer required = craftingOption.get(obj);
 					Integer available = usedObjects.get(obj);
 					Integer carried = carriedObjects.get(obj);
 					
					if(available == null || carried == null || required > available || required > carried || available > carried) {
 						alternativeOk = false;
 						break;
 					}
 				}
 				
 				if(alternativeOk) {
 					requirementsMet = true;
 					break;
 				}
 			}
 			
 			if (requirementsMet) {		// if requirements met update carriedObjects with the quantity that remains 
 				Iterator<CraftedObject> it = usedObjects.keySet().iterator();	
 				while (it.hasNext()) { 
 					CraftedObject obj = it.next();
 					Integer used = usedObjects.get(obj);
 					Integer existing = carriedObjects.get(obj);
 					
 					carriedObjects.put(obj, existing - used);
 				}
 				
 				return true;
 			}
 		}
 		else {												// it is an object made only out of basic resources
 			if (usedResources == null) {
 				return false;
 			}
 			
 			boolean requirementsMet = false;
 			
 			for (HashMap<BasicResourceType, Integer> resourceOption : target.getRequiredResources()) {
 				boolean alternativeOk = true;
 				
 				Iterator<BasicResourceType> resIt = resourceOption.keySet().iterator();
 				while(resIt.hasNext()) {
 					BasicResourceType res = resIt.next();
 					Integer required = resourceOption.get(res);
 					Integer available = usedResources.get(res);
 					Integer carried = carriedResources.get(res);
 					
					if(available == null || carried == null || required > available || required > carried || available > carried) {
 						alternativeOk = false;
 						break;
 					}
 				}
 				
 				if(alternativeOk) {
 					requirementsMet = true;
 					break;
 				}
 			}
 			
 			if (requirementsMet) {		// if requirements met update carriedResources with the quantity that remains
 				Iterator<BasicResourceType> it = usedResources.keySet().iterator();
 				while (it.hasNext()) { 
 					BasicResourceType res = it.next();
 					Integer used = usedResources.get(res);
 					Integer existing = carriedResources.get(res);
 					
 					carriedResources.put(res, existing - used);
 				}
 				
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	protected void doTowerDrain(GameState state, Integer playerID) {
 		PlayerState playerState = state.playerStates.get(playerID);
 		
 		for (UnitState unit : playerState.units) {
 			List<Tower> opponentTowers = state.getOpponentTowers(playerID);
 			
 			for (Tower oppTower : opponentTowers) {
 				if (Math.abs(oppTower.getPosition().x - unit.pos.x) <= GamePolicy.towerCutoffRadius && 
 					Math.abs(oppTower.getPosition().y - unit.pos.y) <= GamePolicy.towerCutoffRadius) {
 					
 					int distance = Math.min( Math.abs(oppTower.getPosition().x - unit.pos.x), Math.abs(oppTower.getPosition().y - unit.pos.y) );
 					if (distance == 0) {	// can happen if a player constructs a tower in a cell
 						distance = 1;		// that contains an opponents unit
 					}
 					int drainAmount = GamePolicy.towerDrainBase / distance;
 					
 					unit.energy -= drainAmount;						// drain unit energy
 					oppTower.weakenTower(drainAmount);				// and also weaken tower with the same amount
 					
 					if (oppTower.getRemainingStrength() <= 0) {		// if the tower has been weakened enough => destroy it
 						List<Integer> playerIds = state.getPlayerIds();
 						for (Integer pId : playerIds) {
 							boolean foundTower = false;
 							
 							if (pId != playerID) {
 								List<Tower> pTowers = state.playerTowers.get(pId);	// get opponent tower list
 								
 								for (Tower t : pTowers) {			// see if it contains 
 									if (t.getPosition().isEqual(oppTower.getPosition())) {
 										PlayerState opponentState = state.playerStates.get(pId);
 										opponentState.availableTowers.put(t, false);	// tower is no longer available
 										pTowers.remove(t);			// the weakened tower and remove it
 										foundTower = true;
 										break;
 									}
 								}
 							}
 							
 							if (foundTower) {		// there can't be more than one tower in that position
 								break;
 							}
 						}
 					}
 					
 				}
 			}
 				
 		}
 	}
 	
 	protected void replenishEnergy() {
 		for (PlayerState pState : game.playerStates.values()) {
 			for (UnitState unit : pState.units) {
 				unit.energy = GamePolicy.unitEnergy;
 			}
 		}
 	}
 }
