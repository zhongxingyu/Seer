 
 package net.sf.freecol.server.control;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Iterator;
 import java.util.Random;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 import net.sf.freecol.common.model.Building;
 import net.sf.freecol.common.model.Colony;
 import net.sf.freecol.common.model.Europe;
 import net.sf.freecol.common.model.FoundingFather;
 import net.sf.freecol.common.model.Game;
 import net.sf.freecol.common.model.Goods;
 import net.sf.freecol.common.model.GoodsContainer;
 import net.sf.freecol.common.model.IndianSettlement;
 import net.sf.freecol.common.model.Location;
 import net.sf.freecol.common.model.LostCityRumour;
 import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Market;
 import net.sf.freecol.common.model.Player;
 import net.sf.freecol.common.model.Settlement;
 import net.sf.freecol.common.model.Tile;
 import net.sf.freecol.common.model.Unit;
 import net.sf.freecol.common.model.WorkLocation;
 import net.sf.freecol.common.model.Map.Position;
 import net.sf.freecol.common.networking.Connection;
 import net.sf.freecol.common.networking.Message;
 import net.sf.freecol.common.networking.NetworkConstants;
 import net.sf.freecol.server.FreeColServer;
 import net.sf.freecol.server.ai.AIPlayer;
 import net.sf.freecol.server.model.ServerPlayer;
 
 import org.w3c.dom.Element;
 
 
 /**
 * Handles the network messages that arrives while
 * {@link FreeColServer#IN_GAME in game}.
 */
 public final class InGameInputHandler extends InputHandler implements NetworkConstants {
     private static Logger logger = Logger.getLogger(InGameInputHandler.class.getName());
 
     public static final String  COPYRIGHT = "Copyright (C) 2003-2005 The FreeCol Team";
     public static final String  LICENSE = "http://www.gnu.org/licenses/gpl.html";
     public static final String  REVISION = "$Revision$";
 
     public static Random attackCalculator;
     public static final Random random = new Random();
 
 
     /**
     * The constructor to use.
     * @param freeColServer The main server object.
     */
     public InGameInputHandler(FreeColServer freeColServer) {
         super(freeColServer);
         attackCalculator = new Random();
     }
 
 
 
     /**
     * Handles a network message.
     *
     * @param connection The <code>Connection</code> the message came from.
     * @param element The message to be processed.
     * @return The reply.
     */
     public synchronized Element handle(Connection connection, Element element) {
         Element reply = null;
         FreeColServer freeColServer = getFreeColServer();
 
         String type = element.getTagName();
 
         try {
             if (element != null) {
                 // The first messages you see here are the ones that are supported even
                 // though it is NOT the sender's turn:
                 if (type.equals("logout")) {
                     reply = logout(connection, element);
                 } else if (type.equals("createUnit")) {
                     reply = createUnit(connection, element);
                 } else if (type.equals("getRandom")) {
                     reply = getRandom(connection, element);
                 } else if (type.equals("getVacantEntryLocation")) {
                     reply = getVacantEntryLocation(connection, element);
                 } else if (type.equals("disconnect")) {
                     reply = disconnect(connection, element);
                 } else if (freeColServer.getGame().getCurrentPlayer().equals(freeColServer.getPlayer(connection))) {
                     if (type.equals("chat")) {
                         reply = chat(connection, element);
                     } else if (type.equals("move")) {
                         reply = move(connection, element);
                     } else if (type.equals("explore")) {
                         reply = explore(connection, element);
                     } else if (type.equals("askSkill")) {
                         reply = askSkill(connection, element);
                     } else if (type.equals("attack")) {
                         reply = attack(connection, element);
                     } else if (type.equals("embark")) {
                         reply = embark(connection, element);
                     } else if (type.equals("boardShip")) {
                         reply = boardShip(connection, element);
                     } else if (type.equals("learnSkillAtSettlement")) {
                         reply = learnSkillAtSettlement(connection, element);
                     } else if (type.equals("scoutIndianSettlement")) {
                         reply = scoutIndianSettlement(connection, element);
                     } else if (type.equals("missionaryAtSettlement")) {
                         reply = missionaryAtSettlement(connection, element);
                     } else if (type.equals("inciteAtSettlement")) {
                         reply = inciteAtSettlement(connection, element);
                     } else if (type.equals("leaveShip")) {
                         reply = leaveShip(connection, element);
                     } else if (type.equals("loadCargo")) {
                         reply = loadCargo(connection, element);
                     } else if (type.equals("unloadCargo")) {
                         reply = unloadCargo(connection, element);
                     } else if (type.equals("buyGoods")) {
                         reply = buyGoods(connection, element);
                     } else if (type.equals("sellGoods")) {
                         reply = sellGoods(connection, element);
                     } else if (type.equals("moveToEurope")) {
                         reply = moveToEurope(connection, element);
                     } else if (type.equals("moveToAmerica")) {
                         reply = moveToAmerica(connection, element);
                     } else if (type.equals("buildColony")) {
                         reply = buildColony(connection, element);
                     } else if (type.equals("recruitUnitInEurope")) {
                         reply = recruitUnitInEurope(connection, element);
                     } else if (type.equals("emigrateUnitInEurope")) {
                         reply = emigrateUnitInEurope(connection, element);
                     } else if (type.equals("trainUnitInEurope")) {
                         reply = trainUnitInEurope(connection, element);
                     } else if (type.equals("equipunit")) {
                         reply = equipUnit(connection, element);
                     } else if (type.equals("work")) {
                         reply = work(connection, element);
                     } else if (type.equals("changeWorkType")) {
                         reply = changeWorkType(connection, element);
                     } else if (type.equals("setCurrentlyBuilding")) {
                         reply = setCurrentlyBuilding(connection, element);
                     } else if (type.equals("changeState")) {
                         reply = changeState(connection, element);
                     } else if (type.equals("putOutsideColony")) {
                         reply = putOutsideColony(connection, element);
                     } else if (type.equals("clearSpeciality")) {
                         reply = clearSpeciality(connection, element);
                     } else if (type.equals("setNewLandName")) {
                         reply = setNewLandName(connection, element);
                     } else if (type.equals("endTurn")) {
                         reply = endTurn(connection, element);
                     } else if (type.equals("disbandUnit")) {
                         reply = disbandUnit(connection, element);
                     } else if (type.equals("cashInTreasureTrain")) {
                         reply = cashInTreasureTrain(connection, element);
                     } else if (type.equals("tradeProposition")) {
                         reply = tradeProposition(connection, element);
                     } else if (type.equals("trade")) {
                         reply = trade(connection, element);
                     } else if (type.equals("deliverGift")) {
                         reply = deliverGift(connection, element);
                     } else if (type.equals("indianDemand")) {
                         reply = indianDemand(connection, element);
                     } else if (type.equals("buyLand")) {
                         reply = buyLand(connection, element);
                     } else if (type.equals("payForBuilding")) {
                         reply = payForBuilding(connection, element);
                     } else if (type.equals("payArrears")) {
                         reply = payArrears(connection, element);
                     } else if (type.equals("toggleExports")) {
                         reply = toggleExports(connection, element);
                     } else {
                         logger.warning("Unknown request from client " + element.getTagName());
                     }
                 } else {
                     // The message we've received is probably a good one, but
                     // it was sent when it was not the sender's turn.
                     reply = Message.createNewRootElement("error");
                     reply.setAttribute("message", "Not your turn.");
 
                     logger.warning("Received message when not in turn: " + element.getTagName());
                 }
             }
         } catch (Exception e) {
             StringWriter sw = new StringWriter();
             e.printStackTrace(new PrintWriter(sw));
 
             logger.warning(sw.toString());
 
             Element reconnect = Message.createNewRootElement("reconnect");
 
             try {
                 connection.send(reconnect);
             } catch (IOException ex) {
                 logger.warning("Could not send reconnect message!");
             }
 
             return null;
         }
 
         return reply;
     }
 
 
     /**
     * Handles a "buyLand"-message from a client.
     *
     * @param connection The connection the message came from.
     * @param element The element containing the request.
     *
     */
     private Element buyLand(Connection connection, Element element) {
         Game game = getFreeColServer().getGame();
 
         ServerPlayer player = getFreeColServer().getPlayer(connection);
         Tile tile = (Tile) game.getFreeColGameObject(element.getAttribute("tile"));
         
         if (tile == null) {
             throw new NullPointerException();
         }
 
         player.buyLand(tile);
 
         return null;
     }
 
 
     /**
     * Handles a "setNewLandName"-message from a client.
     *
     * @param connection The connection the message came from.
     * @param element The element containing the request.
     *
     */
     private Element setNewLandName(Connection connection, Element element) {
         ServerPlayer player = getFreeColServer().getPlayer(connection);
         player.setNewLandName(element.getAttribute("newLandName"));
 
         // TODO: Send name to all other players.
 
         return null;
     }
 
 
     /**
     * Handles a "createUnit"-message from a client.
     *
     * @param connection The connection the message came from.
     * @param element The element containing the request.
     *
     */
     private Element createUnit(Connection connection, Element element) {
         Game game = getFreeColServer().getGame();
 
         logger.info("Receiving \"createUnit\"-request.");
 
         String taskID = element.getAttribute("taskID");
         Location location = (Location) game.getFreeColGameObject(element.getAttribute("location"));
         Player owner = (Player) game.getFreeColGameObject(element.getAttribute("owner"));
         int type = Integer.parseInt(element.getAttribute("type"));
 
         if (location == null) {
             throw new NullPointerException();
         }
         
         if (owner == null) {
             throw new NullPointerException();
         }
         
         Unit unit = getFreeColServer().getModelController().createUnit(taskID, location, owner, type, false, connection);
 
         Element reply = Message.createNewRootElement("createUnitConfirmed");
         reply.appendChild(unit.toXMLElement(owner, reply.getOwnerDocument()));
 
         return reply;
     }
 
 
     /**
     * Handles a "getRandom"-message from a client.
     *
     * @param connection The connection the message came from.
     * @param element The element containing the request.
     *
     */
     private Element getRandom(Connection connection, Element element) {
         logger.info("Receiving \"getRandom\"-request.");
 
         String taskID = element.getAttribute("taskID");
         int n = Integer.parseInt(element.getAttribute("n"));
 
         int result = getFreeColServer().getModelController().getRandom(taskID, n);
 
         Element reply = Message.createNewRootElement("getRandomConfirmed");
         reply.setAttribute("result", Integer.toString(result));
 
         logger.info("Result: " + result);
         return reply;
     }
 
 
     /**
     * Handles a "getVacantEntryLocation"-message from a client.
     *
     * @param connection The connection the message came from.
     * @param element The element containing the request.
     *
     */
     private Element getVacantEntryLocation(Connection connection, Element element) {
         Game game = getFreeColServer().getGame();
         Unit unit = (Unit) game.getFreeColGameObject(element.getAttribute("unit"));
         Player owner = unit.getOwner();
 
         if (owner != getFreeColServer().getPlayer(connection)) {
             throw new IllegalStateException();
         }
 
         Location entryLocation = getFreeColServer().getModelController().setToVacantEntryLocation(unit);
 
         Element reply = Message.createNewRootElement("getVacantEntryLocationConfirmed");
         reply.setAttribute("location", entryLocation.getID());
 
         return reply;
     }
 
 
     /**
     * Handles a "chat"-message from a client.
     *
     * @param connection The connection the message came from.
     * @param element The element containing the request.
     *
     */
     private Element chat(Connection connection, Element element) {
         // TODO: Add support for private chat.
         element.setAttribute("sender", getFreeColServer().getPlayer(connection).getID());
         getFreeColServer().getServer().sendToAll(element, connection);
         return null;
     }
 
 
     /**
     * Handles a "move"-message from a client.
     *
     * @param connection The connection the message came from.
     * @param moveElement The element containing the request.
     * @exception IllegalArgumentException If the data format of the message is invalid.
     * @exception IllegalStateException If the request is not accepted by the model.
     *
     */
     private Element move(Connection connection, Element moveElement) {
         FreeColServer freeColServer = getFreeColServer();
         Game game = freeColServer.getGame();
 
         ServerPlayer player = freeColServer.getPlayer(connection);
 
         Unit unit = (Unit) game.getFreeColGameObject(moveElement.getAttribute("unit"));
         int direction = Integer.parseInt(moveElement.getAttribute("direction"));
 
         if (unit == null) {
             throw new IllegalArgumentException("Could not find 'Unit' with specified ID: " + moveElement.getAttribute("unit"));
         }
 
         if (unit.getTile() == null) {
             throw new IllegalArgumentException("'Unit' not on map: ID: " + moveElement.getAttribute("unit") + " (" + unit.getName() + ")");
         }
 
         if (unit.getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
 
         Tile newTile = game.getMap().getNeighbourOrNull(direction, unit.getTile());
         boolean disembark = !unit.getTile().isLand() && newTile.isLand() && newTile.getSettlement() == null;        
 
         Iterator enemyPlayerIterator = game.getPlayerIterator();
         while (enemyPlayerIterator.hasNext()) {
             ServerPlayer enemyPlayer = (ServerPlayer) enemyPlayerIterator.next();
 
             if (player.equals(enemyPlayer) || enemyPlayer.getConnection() == null) {
                 continue;
             }
 
             try {
                 if (unit.isVisibleTo(enemyPlayer) && !disembark) {
                     Element opponentMoveElement = Message.createNewRootElement("opponentMove");
                     opponentMoveElement.setAttribute("direction", Integer.toString(direction));
                     opponentMoveElement.setAttribute("unit", unit.getID());
                     enemyPlayer.getConnection().send(opponentMoveElement);
                 } else if (enemyPlayer.canSee(newTile) && newTile.getSettlement() == null) {
                     Element opponentMoveElement = Message.createNewRootElement("opponentMove");
                     opponentMoveElement.setAttribute("direction", Integer.toString(direction));
                     opponentMoveElement.setAttribute("tile", newTile.getID());
                     opponentMoveElement.appendChild(unit.toXMLElement(enemyPlayer, opponentMoveElement.getOwnerDocument()));
                     enemyPlayer.getConnection().send(opponentMoveElement);
                 }
             } catch (IOException e) {
                 logger.warning("Could not send message to: " + enemyPlayer.getName() + " with connection " + enemyPlayer.getConnection());
             }
         }
 
         unit.move(direction);
 
         Element reply = Message.createNewRootElement("update");
         Vector surroundingTiles = game.getMap().getSurroundingTiles(unit.getTile(), unit.getLineOfSight());
 
         for (int i=0; i<surroundingTiles.size(); i++) {
             Tile t = (Tile) surroundingTiles.get(i);
             reply.appendChild(t.toXMLElement(player, reply.getOwnerDocument()));
         }
 
         return reply;
     }
 
 
     /**
      * Returns a type of Lost City Rumour. The type of rumour depends
      * on the exploring unit, as well as player settings.
      *
      * @param tile The <code>Tile</code> containing the lost city rumour.
      * @param unit The <code>Unit</code> exploring the lost city rumour.
      * @return A Lost City Rumour type.
      */
     public static int exploreLostCityRumour(Tile tile, Unit unit) {
         /**
          * Random numbers can be generated by this method since it is
          * only invoked by the explore method of the server's
          * InGameInputHandler.
          */
         Player player = unit.getOwner();
         int type = unit.getType();
         // difficulty is in range 0-4, dx in range 2-6
         int dx = player.getDifficulty() + 2;
 
         // seasoned scouts should be more successful
         if (type == Unit.SEASONED_SCOUT) {
             dx--;
         }
 
         // dx is now in range 1-6
         int max = 7; // maximum dx + 1
 
         /** The higher the difficulty, the more likely bad things are
          * to happen.
          */
         int[] probability = new int[LostCityRumour.NUMBER_OF_RUMOURS];
 
         probability[LostCityRumour.BURIAL_GROUND] = dx;
         probability[LostCityRumour.EXPEDITION_VANISHES] = dx * 2;
         probability[LostCityRumour.NOTHING] = dx * 5;
 
         // only these units can be promoted
         if (type == Unit.FREE_COLONIST ||
             type == Unit.INDENTURED_SERVANT ||
             type == Unit.PETTY_CRIMINAL) {
             probability[LostCityRumour.SEASONED_SCOUT] = ( max - dx ) * 3;
         } else {
             probability[LostCityRumour.SEASONED_SCOUT] = 0;
         }
 
         /** The higher the difficulty, the less likely good things are
          * to happen.
          */
         probability[LostCityRumour.TRIBAL_CHIEF] = ( max - dx ) * 3;
         probability[LostCityRumour.COLONIST] = ( max - dx ) * 2;
         probability[LostCityRumour.TREASURE_TRAIN] = ( max - dx ) * 2;
         probability[LostCityRumour.FOUNTAIN_OF_YOUTH] = ( max - dx );
 
         int start = 0;
 
         if (player.hasFather(FoundingFather.HERNANDO_DE_SOTO)) {
             // rumours are always positive
             start = 3;
         }
         
         /* 
          * It should not be possible to find an indian burial ground
          * outside indian territory:
          */          
         if (tile.getNationOwner() == Player.NO_NATION
        		|| tile.getGame().getPlayer(tile.getNationOwner()).isIndian()) {
         	probability[LostCityRumour.BURIAL_GROUND] = 0;
         }
 
         int accumulator = 0;
         for ( int i = start; i < LostCityRumour.NUMBER_OF_RUMOURS; i++ ) {
             accumulator += probability[i];
             probability[i] = accumulator;
         }
 
         int randomInt = random.nextInt(accumulator);
 
         for ( int j = start; j < LostCityRumour.NUMBER_OF_RUMOURS; j++ ) {
             if (randomInt < probability[j]) {
                 return j;
             }
         }
         return LostCityRumour.NO_SUCH_RUMOUR;
     }
     
     
     /**
     * Handles an "explore"-message from a client.
     *
     * @param connection The connection the message came from.
     * @param moveElement The element containing the request.
     * @exception IllegalArgumentException If the data format of the message is invalid.
     * @exception IllegalStateException If the request is not accepted by the model.
     *
     */
     private Element explore(Connection connection, Element exploreElement) {
         FreeColServer freeColServer = getFreeColServer();
         Game game = freeColServer.getGame();
 
         ServerPlayer player = freeColServer.getPlayer(connection);
 
         Unit unit = (Unit) game.getFreeColGameObject(exploreElement.getAttribute("unit"));
         if (unit == null) {
             throw new IllegalArgumentException("Unit is null.");
         }
 
         Tile tile = unit.getTile();
         if (tile.hasLostCityRumour()) {
             tile.setLostCityRumour(false);
         } else {
             throw new IllegalStateException("No rumour to explore.");
         }
             
         int type = exploreLostCityRumour(tile, unit);
         Element rumourElement = Message.createNewRootElement("lostCityRumour");
         rumourElement.setAttribute("type", Integer.toString(type));
         rumourElement.setAttribute("unit", unit.getID());
         Unit newUnit;
         int dx = 10 - player.getDifficulty(); // 6-10
 
         switch (type) {
         case LostCityRumour.BURIAL_GROUND:
             Player indianPlayer = game.getPlayer(unit.getTile().getNationOwner());
             indianPlayer.modifyTension(player, Player.TENSION_HATEFUL);
         case LostCityRumour.EXPEDITION_VANISHES:
             unit.dispose();
             break;
         case LostCityRumour.NOTHING:
             break;
         case LostCityRumour.SEASONED_SCOUT:
             unit.setType(Unit.SEASONED_SCOUT);
             break;
         case LostCityRumour.TRIBAL_CHIEF:
             int amount = attackCalculator.nextInt(dx * 10) + dx * 5;
             player.modifyGold(amount);
             rumourElement.setAttribute("amount", Integer.toString(amount));
             break;
         case LostCityRumour.COLONIST:
             newUnit = new Unit(game, tile, player, Unit.FREE_COLONIST, Unit.ACTIVE);
             unit.getTile().add(newUnit);
             rumourElement.appendChild(newUnit.toXMLElement(player, rumourElement.getOwnerDocument()));
             break;
         case LostCityRumour.TREASURE_TRAIN:
             int treasure = attackCalculator.nextInt(dx * 600) + dx * 300;
             newUnit = new Unit(game, tile, player, Unit.TREASURE_TRAIN, Unit.ACTIVE);
             newUnit.setTreasureAmount(treasure);
             unit.getTile().add(newUnit);
             rumourElement.setAttribute("amount", Integer.toString(treasure));
             rumourElement.appendChild(newUnit.toXMLElement(player, rumourElement.getOwnerDocument()));
             break;
         case LostCityRumour.FOUNTAIN_OF_YOUTH:
             for (int k = 0; k < dx; k++) {
                 newUnit = new Unit(game, player.getEurope(), player,
                                    player.generateRecruitable(), Unit.SENTRY);
                 player.getEurope().add(newUnit);
                 rumourElement.appendChild(newUnit.toXMLElement(player, rumourElement.getOwnerDocument()));
             }
             break;
         default:
             throw new IllegalStateException( "No such rumour." );
         }
 
         // tell everyone the rumour has been explored
         Iterator updateIterator = game.getPlayerIterator();
         while (updateIterator.hasNext()) {
             ServerPlayer updatePlayer = (ServerPlayer) updateIterator.next();
 
             if (player.equals(updatePlayer) || updatePlayer.getConnection() == null) {
                 continue;
             } else if (updatePlayer.canSee(tile)) {
                 try {
                     Element rumourUpdate = Message.createNewRootElement("update");
                     rumourUpdate.appendChild(tile.toXMLElement(updatePlayer, rumourUpdate.getOwnerDocument()));
                     updatePlayer.getConnection().send(rumourUpdate);
                 } catch (IOException e) {
                     logger.warning("Could not send update message to: " +
                                    updatePlayer.getName() + " with connection " +
                                    updatePlayer.getConnection());
                 }
             }
         }
         
         return rumourElement;
     }
         
     
 
     /**
     * Handles an "askSkill"-message from a client.
     *
     * @param connection The connection the message came from.
     * @param element The element containing the request.
     *
     * @exception IllegalArgumentException If the data format of the message is invalid.
     * @exception IllegalStateException If the request is not accepted by the model.
     */
     private Element askSkill(Connection connection, Element element) {
         FreeColServer freeColServer = getFreeColServer();
         Game game = freeColServer.getGame();
         Map map = game.getMap();
         ServerPlayer player = freeColServer.getPlayer(connection);
 
         Unit unit = (Unit) game.getFreeColGameObject(element.getAttribute("unit"));
         int direction = Integer.parseInt(element.getAttribute("direction"));
 
         if (unit == null) {
             throw new IllegalArgumentException("Could not find 'Unit' with specified ID: " + element.getAttribute("unit"));
         }
 
         if (unit.getMovesLeft() == 0) {
             throw new IllegalArgumentException("Unit has no moves left.");
         }
 
         if (unit.getTile() == null) {
             throw new IllegalArgumentException("'Unit' not on map: ID: " + element.getAttribute("unit"));
         }
 
         if (unit.getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
 
         IndianSettlement settlement = (IndianSettlement) map.getNeighbourOrNull(direction, unit.getTile()).getSettlement();
 
         if (settlement.getLearnableSkill() != IndianSettlement.UNKNOWN) {
             unit.setMovesLeft(0);
 
             if (settlement.getLearnableSkill() != IndianSettlement.NONE) {
                 // We now put the unit on the indian settlement. Normally we shouldn't have
                 // to this, but the movesLeft are set to 0 for unit and if the player decides
                 // to learn a skill with a learnSkillAtSettlement message then we have to be
                 // able to check if the unit can learn the skill.
                 unit.setLocation(settlement);
             }
 
             Element reply = Message.createNewRootElement("provideSkill");
             reply.setAttribute("skill", Integer.toString(settlement.getLearnableSkill()));
 
             // Set the Tile.PlayerExploredTile attribute.
             settlement.getTile().updateIndianSettlementSkill(player);           
 
             return reply;
         } else {
             throw new IllegalStateException("Learnable skill from Indian settlement is unknown at server.");
         }
     }
 
 
     /**
     * Handles an "attack"-message from a client.
     *
     * @param connection The connection the message came from.
     * @param attackElement The element containing the request.
     * @exception IllegalArgumentException If the data format of the message is invalid.
     * @exception IllegalStateException If the request is not accepted by the model.
     *
     */
     private Element attack(Connection connection, Element attackElement) {
         FreeColServer freeColServer = getFreeColServer();
         Game game = freeColServer.getGame();
         ServerPlayer player = freeColServer.getPlayer(connection);
 
 
         // Get parameters:
         Unit unit = (Unit) game.getFreeColGameObject(attackElement.getAttribute("unit"));
         int direction = Integer.parseInt(attackElement.getAttribute("direction"));
 
 
         // Test the parameters:
         if (unit == null) {
             throw new IllegalArgumentException("Could not find 'Unit' with specified ID: " + attackElement.getAttribute("unit"));
         }
 
         if (unit.getTile() == null) {
             throw new IllegalArgumentException("'Unit' is not on the map: " + unit.toString());
         }
 
         if (unit.getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
 
         Tile oldTile = unit.getTile();
         Tile newTile = game.getMap().getNeighbourOrNull(direction, unit.getTile());
 
         if (newTile == null) {
             throw new IllegalArgumentException("Could not find tile in direction " + direction + " from unit with ID " + attackElement.getAttribute("unit"));
         }
 
         Element dowElement = null;
         Unit defender = newTile.getDefendingUnit(unit);
         if (defender == null) {
             throw new IllegalStateException("Nothing to attack in direction " + direction + " from unit with ID " + attackElement.getAttribute("unit"));
         } else if (player.getStance(defender.getOwner()) == Player.ALLIANCE) {
             throw new IllegalArgumentException("Can not attack allied player.");
         } else if (player.getStance(defender.getOwner()) != Player.WAR) {
             player.setStance(defender.getOwner(), Player.WAR);
             defender.getOwner().warDeclaredBy(player);
             // create diplomatic message
             dowElement = Message.createNewRootElement("diplomaticMessage");
             dowElement.setAttribute("type", "declarationOfWar");
             dowElement.setAttribute("attacker", player.getID());
             dowElement.setAttribute("defender", defender.getOwner().getID());
         }
 
         int result = generateAttackResult(unit, defender);
         int plunderGold = -1;
 
         if (result == Unit.ATTACK_DONE_SETTLEMENT) {
             plunderGold = newTile.getSettlement().getOwner().getGold()/10; // 10% of their gold
         }
 
         // Inform the other players (other then the player attacking) about the attack:
         Iterator enemyPlayerIterator = game.getPlayerIterator();
         while (enemyPlayerIterator.hasNext()) {
             ServerPlayer enemyPlayer = (ServerPlayer) enemyPlayerIterator.next();
 
             if (player.equals(enemyPlayer) || enemyPlayer.getConnection() == null) {
                 continue;
             }
 
             if (dowElement != null) {
                 try {
                     enemyPlayer.getConnection().send(dowElement);
                 } catch (IOException e) {
                     logger.warning("Could not send message to: " +
                                    enemyPlayer.getName() + " with connection " +
                                    enemyPlayer.getConnection());
                 }
             }
             
             Element opponentAttackElement = Message.createNewRootElement("opponentAttack");
             if (unit.isVisibleTo(enemyPlayer) || defender.isVisibleTo(enemyPlayer)) {
                 opponentAttackElement.setAttribute("direction", Integer.toString(direction));
                 opponentAttackElement.setAttribute("result", Integer.toString(result));
                 opponentAttackElement.setAttribute("plunderGold", Integer.toString(plunderGold));
                 opponentAttackElement.setAttribute("unit", unit.getID());
                 opponentAttackElement.setAttribute("defender", defender.getID());
             
                 if (!defender.isVisibleTo(enemyPlayer)) {
                     opponentAttackElement.setAttribute("update", "defender");
                     if (!enemyPlayer.canSee(defender.getTile())) {
                         enemyPlayer.setExplored(defender.getTile());
                         opponentAttackElement.appendChild(defender.getTile().toXMLElement(enemyPlayer, opponentAttackElement.getOwnerDocument()));
                     }
                     opponentAttackElement.appendChild(defender.toXMLElement(enemyPlayer, opponentAttackElement.getOwnerDocument()));
                 } else if (!unit.isVisibleTo(enemyPlayer)) {
                     opponentAttackElement.setAttribute("update", "unit");
                     opponentAttackElement.appendChild(unit.toXMLElement(enemyPlayer, opponentAttackElement.getOwnerDocument()));
                 }
                 
                 try {
                     enemyPlayer.getConnection().send(opponentAttackElement);
                 } catch (IOException e) {
                     logger.warning("Could not send message to: " +
                                    enemyPlayer.getName() + " with connection " +
                                    enemyPlayer.getConnection());
                 }
             }
         }
 
 
         // Create the reply for the attacking player:
         Element reply = Message.createNewRootElement("attackResult");
         reply.setAttribute("result", Integer.toString(result));
         reply.setAttribute("plunderGold", Integer.toString(plunderGold));
 
         if (result == Unit.ATTACK_DONE_SETTLEMENT && newTile.getColony() != null) { // If a colony will been won, send an updated tile:
             reply.appendChild(newTile.toXMLElement(newTile.getColony().getOwner(), reply.getOwnerDocument()));
         } else if (!defender.isVisibleTo(player)) {
             reply.appendChild(defender.toXMLElement(player, reply.getOwnerDocument()));
         }
 
         unit.attack(defender, result, plunderGold);
 
         if (result >= Unit.ATTACK_EVADES && unit.getTile().equals(newTile)) { // In other words, we moved...
             Element update = reply.getOwnerDocument().createElement("update");
             Vector surroundingTiles = game.getMap().getSurroundingTiles(unit.getTile(), unit.getLineOfSight());
 
             for (int i=0; i<surroundingTiles.size(); i++) {
                 Tile t = (Tile) surroundingTiles.get(i);
                 update.appendChild(t.toXMLElement(player, update.getOwnerDocument()));
             }
 
             reply.appendChild(update);
         }
 
         return reply;
     }
 
 
     /**
     * Generates a result of an attack.
     */
     private int generateAttackResult(Unit unit, Unit defender) {
         int attackPower = unit.getOffensePower(defender);
         int totalProbability = attackPower + defender.getDefensePower(unit);
         int result;
         int r = attackCalculator.nextInt(totalProbability+1);
         if (r > attackPower) {
             result = Unit.ATTACK_LOSS;
         } else if(r == attackPower) {
             if (defender.isNaval()) {
                 result = Unit.ATTACK_EVADES;
             } else {
                 result = Unit.ATTACK_WIN;
             }
         } else { // (r < attackPower)
             result = Unit.ATTACK_WIN;
         }
 
         if (result == Unit.ATTACK_WIN) {
             int diff = defender.getDefensePower(unit)*2-attackPower;
             int r2 = attackCalculator.nextInt((diff<3) ? 3 : diff);
 
             if (r2 == 0) {
                 result = Unit.ATTACK_GREAT_WIN;
             } else {
                 result = Unit.ATTACK_WIN;
             }
         }
 
         if (result == Unit.ATTACK_LOSS) {
             int diff = attackPower*2-defender.getDefensePower(unit);
             int r2 = attackCalculator.nextInt((diff<3) ? 3 : diff);
 
             if (r2 == 0) {
                 result = Unit.ATTACK_GREAT_LOSS;
             } else {
                 result = Unit.ATTACK_LOSS;
             }
         }
 
         if ((result == Unit.ATTACK_WIN || result == Unit.ATTACK_GREAT_WIN) && (
                 defender.getTile().getSettlement() != null && defender.getTile().getSettlement() instanceof IndianSettlement
                 && ((IndianSettlement) defender.getTile().getSettlement()).getUnitCount()+defender.getTile().getUnitCount() <= 1
                 || defender.getTile().getColony() != null && !defender.isArmed() && !defender.isMounted() && defender.getType() != Unit.ARTILLERY
                 && defender.getType() != Unit.DAMAGED_ARTILLERY && !defender.isMounted())) {
             result = Unit.ATTACK_DONE_SETTLEMENT;
         }
 
         return result;
     }
 
 
     /**
     * Handles an "embark"-message from a client.
     *
     * @param connection The connection the message came from.
     * @param embarkElement The element containing the request.
     * @exception IllegalArgumentException If the data format of the message is invalid.
     */
     private Element embark(Connection connection, Element embarkElement) {
         FreeColServer freeColServer = getFreeColServer();
         Game game = freeColServer.getGame();
         ServerPlayer player = freeColServer.getPlayer(connection);
 
         Unit unit = (Unit) game.getFreeColGameObject(embarkElement.getAttribute("unit"));
         int direction = Integer.parseInt(embarkElement.getAttribute("direction"));
         Unit destinationUnit = (Unit) game.getFreeColGameObject(embarkElement.getAttribute("embarkOnto"));
 
         if (unit == null || destinationUnit == null || game.getMap().getNeighbourOrNull(direction, unit.getTile()) != destinationUnit.getTile()) {
             throw new IllegalArgumentException("Invalid data format in client message.");
         }
 
         if (unit.getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
 
         Tile oldTile = unit.getTile();
 
         unit.embark(destinationUnit);
 
         Iterator enemyPlayerIterator = game.getPlayerIterator();
         while (enemyPlayerIterator.hasNext()) {
             ServerPlayer enemyPlayer = (ServerPlayer) enemyPlayerIterator.next();
 
             if (player.equals(enemyPlayer) || enemyPlayer.getConnection() == null) {
                 continue;
             }
 
             try {
                 if (enemyPlayer.canSee(oldTile)) {
                     Element removeElement = Message.createNewRootElement("remove");
 
                     Element removeUnit = removeElement.getOwnerDocument().createElement("removeObject");
                     removeUnit.setAttribute("ID", unit.getID());
                     removeElement.appendChild(removeUnit);
 
                     enemyPlayer.getConnection().send(removeElement);
                 }
             } catch (IOException e) {
                 logger.warning("Could not send message to: " + enemyPlayer.getName() + " with connection " + enemyPlayer.getConnection());
             }
         }
 
         return null;
     }
 
 
     /**
     * Handles an "boardShip"-message from a client.
     *
     * @param connection The connection the message came from.
     * @param boardShipElement The element containing the request.
     */
     private Element boardShip(Connection connection, Element boardShipElement) {
         FreeColServer freeColServer = getFreeColServer();
         Game game = freeColServer.getGame();
         ServerPlayer player = freeColServer.getPlayer(connection);
 
         Unit unit = (Unit) game.getFreeColGameObject(boardShipElement.getAttribute("unit"));
         Unit carrier = (Unit) game.getFreeColGameObject(boardShipElement.getAttribute("carrier"));
 
         if (unit.getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
 
         Tile oldTile = unit.getTile();
 
         boolean tellEnemyPlayers = true;
         if (oldTile == null || oldTile.getSettlement() != null) {
             tellEnemyPlayers = false;
         }
 
         if (unit.isCarrier()) {
           logger.warning("Tried to load a carrier onto another carrier.");
           return null;
         }
 
         unit.boardShip(carrier);
 
         if (tellEnemyPlayers) {
             Iterator enemyPlayerIterator = game.getPlayerIterator();
             while (enemyPlayerIterator.hasNext()) {
                 ServerPlayer enemyPlayer = (ServerPlayer) enemyPlayerIterator.next();
 
                 if (player.equals(enemyPlayer) || enemyPlayer.getConnection() == null) {
                     continue;
                 }
 
                 try {
                     if (enemyPlayer.canSee(oldTile)) {
                         Element removeElement = Message.createNewRootElement("remove");
 
                         Element removeUnit = removeElement.getOwnerDocument().createElement("removeObject");
                         removeUnit.setAttribute("ID", unit.getID());
                         removeElement.appendChild(removeUnit);
 
                         enemyPlayer.getConnection().send(removeElement);
                     }
                 } catch (IOException e) {
                     logger.warning("Could not send message to: " + enemyPlayer.getName() + " with connection " + enemyPlayer.getConnection());
                 }
             }
         }
 
         return null;
     }
 
 
     /**
     * Handles a "learnSkillAtSettlement"-message from a client.
     *
     * @param connection The connection the message came from.
     * @param element The element containing the request.
     */
     private Element learnSkillAtSettlement(Connection connection, Element element) {
         FreeColServer freeColServer = getFreeColServer();
         Game game = freeColServer.getGame();
         Map map = game.getMap();
         ServerPlayer player = freeColServer.getPlayer(connection);
 
         Unit unit = (Unit) game.getFreeColGameObject(element.getAttribute("unit"));
         int direction = Integer.parseInt(element.getAttribute("direction"));
         boolean cancelAction = false;
 
         if (element.getAttribute("action").equals("cancel")) {
             cancelAction = true;
         }
 
         if (unit.getTile() == null) {
             throw new IllegalArgumentException("'Unit' not on map: ID: " + element.getAttribute("unit"));
         }
 
         if (unit.getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
 
         // The unit was relocated to the indian settlement. See askSkill for more info.
         IndianSettlement settlement = (IndianSettlement) unit.getLocation();
         Tile tile = map.getNeighbourOrNull(Map.getReverseDirection(direction), unit.getTile());
         unit.setLocation(tile);
 
         if (!cancelAction) {
             unit.setType(settlement.getLearnableSkill());
             settlement.setLearnableSkill(IndianSettlement.NONE);
 
             // Set the Tile.PlayerExploredTile attribute.
             settlement.getTile().updateIndianSettlementSkill(player);
         }
 
         return null;
     }
 
 
     /**
     * Handles a "scoutIndianSettlement"-message from a client.
     *
     * @param connection The connection the message came from.
     * @param element The element containing the request.
     */
     private Element scoutIndianSettlement(Connection connection, Element element) {
         FreeColServer freeColServer = getFreeColServer();
         Game game = freeColServer.getGame();
         Map map = game.getMap();
         ServerPlayer player = freeColServer.getPlayer(connection);
 
         Unit unit = (Unit) game.getFreeColGameObject(element.getAttribute("unit"));
 
         if (unit.getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
 
         int direction = Integer.parseInt(element.getAttribute("direction"));
         String action = element.getAttribute("action");
         IndianSettlement settlement;
 
         if (action.equals("basic")) {
             settlement = (IndianSettlement) map.getNeighbourOrNull(direction, unit.getTile()).getSettlement();
             // Move the unit onto the settlement's Tile.
             unit.setLocation(settlement.getTile());
             unit.setMovesLeft(0);
         } else {
             settlement = (IndianSettlement) unit.getTile().getSettlement();
             // Move the unit back to its original Tile.
             unit.setLocation(map.getNeighbourOrNull(Map.getReverseDirection(direction), unit.getTile()));
         }
 
         Element reply = Message.createNewRootElement("scoutIndianSettlementResult");
 
         if (action.equals("basic")) {
             // Just return the skill and wanted goods.
             reply.setAttribute("skill", Integer.toString(settlement.getLearnableSkill()));            
             settlement.updateWantedGoods();
             reply.setAttribute("highlyWantedGoods", Integer.toString(settlement.getHighlyWantedGoods()));
             reply.setAttribute("wantedGoods1", Integer.toString(settlement.getWantedGoods1()));
             reply.setAttribute("wantedGoods2", Integer.toString(settlement.getWantedGoods2()));
 
             // Set the Tile.PlayerExploredTile attribute.
             settlement.getTile().updateIndianSettlementInformation(player);
         } else if (action.equals("speak")) {
             if (!settlement.hasBeenVisited()) {
                 // This can probably be randomized, I don't think the AI needs to do anything here.
                 double random = Math.random();
                 if (random < 0.33) {
                     reply.setAttribute("result", "tales");
                     Element update = reply.getOwnerDocument().createElement("update");
 
                     Position center = new Position(settlement.getTile().getX(), settlement.getTile().getY());
 
                     Iterator circleIterator = map.getCircleIterator(center, true, 6);
                     while (circleIterator.hasNext()) {
                         Position position = (Position)circleIterator.next();
                         if ((!position.equals(center)) && map.getTile(position).isLand()) {
                             Tile t = map.getTile(position);
                             player.setExplored(t);
                             update.appendChild(t.toXMLElement(player, update.getOwnerDocument()));
                         }
                     }
 
                     reply.appendChild(update);
                 } else {
                     int beadsGold = (int) (Math.random() * (400 * settlement.getBonusMultiplier())) + 50;
                     if (unit.getType() == Unit.SEASONED_SCOUT) {
                         beadsGold = (beadsGold * 11) / 10;
                     }
                     reply.setAttribute("result", "beads");
                     reply.setAttribute("amount", Integer.toString(beadsGold));
                     player.modifyGold(beadsGold);
                 } 
                 
                 /* This should only happen if you have "Searched for a treasure in the burial mounds":
                 else {
                     reply.setAttribute("result", "die");
                     unit.dispose();
                 }
                 */
                 settlement.setVisited();
             } else {
                 reply.setAttribute("result", "nothing");
             }
         } else if (action.equals("tribute")) {
             // TODO: the AI needs to determine whether or not we want to pay and how much.
             double random = Math.random();
             if (random < 0.5 && settlement.getOwner().getGold() >= 100) {
                 reply.setAttribute("result", "agree");
                 reply.setAttribute("amount", "100");
                 settlement.getOwner().modifyGold(-100);
                 player.modifyGold(100);
             } else {
                 reply.setAttribute("result", "disagree");
             }
             settlement.getOwner().modifyTension(player, Player.TENSION_ADD_MINOR);
         } else if (action.equals("attack")) {
             // The movesLeft has been set to 0 when the scout initiated its action.
             // If it wants to attack then it can and it will need some moves to do it.
             unit.setMovesLeft(1);
             return null;
         } else if (action.equals("cancel")) {
             return null;
         }
 
         return reply;
     }
 
 
     /**
     * Handles a "missionaryAtSettlement"-message from a client.
     *
     * @param connection The connection the message came from.
     * @param element The element containing the request.
     */
     private Element missionaryAtSettlement(Connection connection, Element element) {
         FreeColServer freeColServer = getFreeColServer();
         Game game = freeColServer.getGame();
         Map map = game.getMap();
         ServerPlayer player = freeColServer.getPlayer(connection);
 
         Unit unit = (Unit) game.getFreeColGameObject(element.getAttribute("unit"));
         int direction = Integer.parseInt(element.getAttribute("direction"));
         String action = element.getAttribute("action");
         IndianSettlement settlement = (IndianSettlement) map.getNeighbourOrNull(direction, unit.getTile()).getSettlement();
 
         unit.setMovesLeft(0);
 
         if (action.equals("cancel")) {
             return null;
         } else if (action.equals("establish")) {
             sendRemoveUnitToAll(unit, player);
             settlement.setMissionary(unit);
             return null;
         } else if (action.equals("heresy")) {
             Element reply = Message.createNewRootElement("missionaryReply");
 
             sendRemoveUnitToAll(unit, player);
 
             // TODO: chance needs to depend on amount of crosses that the players who are involved have.
             double random = Math.random();
             if (settlement.getMissionary().getType() == Unit.JESUIT_MISSIONARY
                     || settlement.getMissionary().getOwner().hasFather(FoundingFather.FATHER_JEAN_DE_BREBEUF)) {
                 random += 0.2;
             }
             if (unit.getType() == Unit.JESUIT_MISSIONARY
                     || unit.getOwner().hasFather(FoundingFather.FATHER_JEAN_DE_BREBEUF)) {
                 random -= 0.2;
             }
             if (random < 0.5) {
                 reply.setAttribute("success", "true");
                 settlement.setMissionary(unit);
             } else {
                 reply.setAttribute("success", "false");
                 unit.dispose();
             }
 
 
             return reply;
         }  else if (action.equals("incite")) {
             Element reply = Message.createNewRootElement("missionaryReply");
 
             Player enemy = (Player) game.getFreeColGameObject(element.getAttribute("incite"));
 
             reply.setAttribute("amount", String.valueOf(Game.getInciteAmount(player, enemy, settlement.getOwner())));
 
             // Move the unit into the settlement while we wait for the client's response.
             unit.setLocation(settlement);
 
             return reply;
         } else {
             return null;
         }
     }
 
 
     private void sendRemoveUnitToAll(Unit unit, Player player) {
         FreeColServer freeColServer = getFreeColServer();
         Game game = freeColServer.getGame();
 
         Iterator enemyPlayerIterator = game.getPlayerIterator();
         while (enemyPlayerIterator.hasNext()) {
             ServerPlayer enemyPlayer = (ServerPlayer) enemyPlayerIterator.next();
 
             if (player.equals(enemyPlayer) || enemyPlayer.getConnection() == null) {
                 continue;
             }
 
             try {
                 if (unit.isVisibleTo(enemyPlayer)) {
                     Element removeElement = Message.createNewRootElement("remove");
 
                     Element removeUnit = removeElement.getOwnerDocument().createElement("removeObject");
                     removeUnit.setAttribute("ID", unit.getID());
                     removeElement.appendChild(removeUnit);
 
                     enemyPlayer.getConnection().send(removeElement);
                 }
             } catch (IOException e) {
                 logger.warning("Could not send message to: " + enemyPlayer.getName() + " with connection " + enemyPlayer.getConnection());
             }
         }
     }
 
 
     /**
     * Handles a "inciteAtSettlement"-message from a client.
     *
     * @param connection The connection the message came from.
     * @param element The element containing the request.
     */
     private Element inciteAtSettlement(Connection connection, Element element) {
         FreeColServer freeColServer = getFreeColServer();
         Game game = freeColServer.getGame();
         Map map = game.getMap();
         ServerPlayer player = freeColServer.getPlayer(connection);
 
         Unit unit = (Unit)game.getFreeColGameObject(element.getAttribute("unit"));
         int direction = Integer.parseInt(element.getAttribute("direction"));
         String confirmed = element.getAttribute("confirmed");
 
         IndianSettlement settlement = (IndianSettlement) unit.getTile().getSettlement();
         // Move the unit back to its original Tile.
         unit.setLocation(map.getNeighbourOrNull(Map.getReverseDirection(direction), unit.getTile()));
 
         if (confirmed.equals("true")) {
             Player enemy = (Player)game.getFreeColGameObject(element.getAttribute("enemy"));
 
             int amount = Game.getInciteAmount(player, enemy, settlement.getOwner());
 
             player.modifyGold(-amount);
 
             // Set the indian player at war with the european player (and vice versa).
             settlement.getOwner().setStance(enemy, Player.WAR);
             enemy.setStance(settlement.getOwner(), Player.WAR);
             
             // Increase tension levels:
             settlement.getOwner().modifyTension(enemy, 500);
             enemy.modifyTension(settlement.getOwner(), 500);
             enemy.modifyTension(player, 250);
         }
         // else: no need to do anything: unit's moves are already zero.
 
         return null;
     }
 
 
     /**
     * Handles a "leaveShip"-message from a client.
     *
     * @param connection The connection the message came from.
     * @param leaveShipElement The element containing the request.
     */
     private Element leaveShip(Connection connection, Element leaveShipElement) {
         Game game = getFreeColServer().getGame();
         ServerPlayer player = getFreeColServer().getPlayer(connection);
 
         Unit unit = (Unit) game.getFreeColGameObject(leaveShipElement.getAttribute("unit"));
 
         if (unit.getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
 
         unit.leaveShip();
         Tile newTile = unit.getTile();
 
         if (newTile != null) {
             sendUpdatedTileToAll(newTile, player);
         }
 
         return null;
     }
 
     /**
     * Handles a "loadCargo"-message from a client.
     *
     * @param connection The connection the message came from.
     * @param loadCargoElement The element containing the request.
     */
     private Element loadCargo(Connection connection, Element loadCargoElement) {
         Game game = getFreeColServer().getGame();
         ServerPlayer player = getFreeColServer().getPlayer(connection);
 
         Unit carrier = (Unit) game.getFreeColGameObject(loadCargoElement.getAttribute("carrier"));
         Goods goods = new Goods(game, (Element) loadCargoElement.getChildNodes().item(0));
 
         goods.loadOnto(carrier);
 
         return null;
     }
 
 
     /**
     * Handles an "unloadCargo"-message from a client.
     *
     * @param connection The connection the message came from.
     * @param unloadCargoElement The element containing the request.
     */
     private Element unloadCargo(Connection connection, Element unloadCargoElement) {
         Game game = getFreeColServer().getGame();
         ServerPlayer player = getFreeColServer().getPlayer(connection);
 
         Goods goods = new Goods(game, (Element) unloadCargoElement.getChildNodes().item(0));
 
         if (goods.getLocation() instanceof Unit && ((Unit) goods.getLocation()).getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
 
         goods.unload();
 
         return null;
     }
 
     /**
     * Handles a "buyGoods"-message from a client.
     *
     * @param connection The connection the message came from.
     * @param buyGoodsElement The element containing the request.
     */
     private Element buyGoods(Connection connection, Element buyGoodsElement) {
         Game game = getFreeColServer().getGame();
         ServerPlayer player = getFreeColServer().getPlayer(connection);
 
         Unit carrier = (Unit) game.getFreeColGameObject(buyGoodsElement.getAttribute("carrier"));
         int type = Integer.parseInt(buyGoodsElement.getAttribute("type"));
         int amount = Integer.parseInt(buyGoodsElement.getAttribute("amount"));
 
         if (carrier.getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
 
         if (carrier.getOwner() != player) {
             throw new IllegalStateException();
         }
 
         carrier.buyGoods(type, amount);
 
         return null;
     }
 
 
     /**
     * Handles a "sellGoods"-message from a client.
     *
     * @param connection The connection the message came from.
     * @param sellGoodsElement The element containing the request.
     */
     private Element sellGoods(Connection connection, Element sellGoodsElement) {
         Game game = getFreeColServer().getGame();
         ServerPlayer player = getFreeColServer().getPlayer(connection);
 
         Goods goods = new Goods(game, (Element) sellGoodsElement.getChildNodes().item(0));
 
         if (goods.getLocation() instanceof Unit && ((Unit) goods.getLocation()).getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
 
         game.getMarket().sell(goods, player);
 
         return null;
     }
 
     /**
     * Handles a "moveToEurope"-message from a client.
     *
     * @param connection The connection the message came from.
     * @param moveToEuropeElement The element containing the request.
     */
     private Element moveToEurope(Connection connection, Element moveToEuropeElement) {
         Game game = getFreeColServer().getGame();
         ServerPlayer player = getFreeColServer().getPlayer(connection);
 
         Unit unit = (Unit) game.getFreeColGameObject(moveToEuropeElement.getAttribute("unit"));
 
         if (unit.getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
 
         Tile oldTile = unit.getTile();
         unit.moveToEurope();
 
         Iterator enemyPlayerIterator = game.getPlayerIterator();
         while (enemyPlayerIterator.hasNext()) {
             ServerPlayer enemyPlayer = (ServerPlayer) enemyPlayerIterator.next();
 
             if (player.equals(enemyPlayer) || enemyPlayer.getConnection() == null) {
                 continue;
             }
 
             try {
                 if (enemyPlayer.canSee(oldTile)) {
                     Element removeElement = Message.createNewRootElement("remove");
 
                     Element removeUnit = removeElement.getOwnerDocument().createElement("removeObject");
                     removeUnit.setAttribute("ID", unit.getID());
                     removeElement.appendChild(removeUnit);
 
                     enemyPlayer.getConnection().send(removeElement);
                 }
             } catch (IOException e) {
                 logger.warning("Could not send message to: " + enemyPlayer.getName() + " with connection " + enemyPlayer.getConnection());
             }
         }
 
         return null;
     }
 
 
     /**
     * Handles a "moveToAmerica"-message from a client.
     *
     * @param connection The connection the message came from.
     * @param moveToAmericaElement The element containing the request.
     */
     private Element moveToAmerica(Connection connection, Element moveToAmericaElement) {
         Game game = getFreeColServer().getGame();
         ServerPlayer player = getFreeColServer().getPlayer(connection);
 
         Unit unit = (Unit) game.getFreeColGameObject(moveToAmericaElement.getAttribute("unit"));
 
         if (unit.getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
 
         unit.moveToAmerica();
 
         return null;
     }
 
 
     /**
     * Handles a "buildColony"-request from a client.
     *
     * @param connection The connection the message came from.
     * @param buildColonyElement The element containing the request.
     */
     private Element buildColony(Connection connection, Element buildColonyElement) {
         Game game = getFreeColServer().getGame();
         ServerPlayer player = getFreeColServer().getPlayer(connection);
 
         String name = buildColonyElement.getAttribute("name");
         Unit unit = (Unit) game.getFreeColGameObject(buildColonyElement.getAttribute("unit"));
 
         if (unit.getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
 
         if (unit.canBuildColony()) {
             Colony colony = new Colony(game, player, name, unit.getTile());
 
             Element reply = Message.createNewRootElement("buildColonyConfirmed");
             reply.appendChild(colony.toXMLElement(player, reply.getOwnerDocument()));
 
             if (colony.getLineOfSight() > unit.getLineOfSight()) {
                 Element updateElement = reply.getOwnerDocument().createElement("update");
                 Vector surroundingTiles = game.getMap().getSurroundingTiles(unit.getTile(), colony.getLineOfSight());
 
                 for (int i=0; i<surroundingTiles.size(); i++) {
                     Tile t = (Tile) surroundingTiles.get(i);
                     if (t != unit.getTile()) {
                         updateElement.appendChild(t.toXMLElement(player, reply.getOwnerDocument()));
                     }
                 }
 
                 reply.appendChild(updateElement);
             }
 
             unit.buildColony(colony);
 
             sendUpdatedTileToAll(unit.getTile(), player);
 
             return reply;
         } else {
             logger.warning("A client is requesting to build a colony, but the operation is not permitted! (unsynchronized?)");
             return null;
         }
     }
 
 
     /**
     * Handles a "recruitUnitInEurope"-request from a client.
     *
     * @param connection The connection the message came from.
     * @param recruitUnitInEuropeElement The element containing the request.
     */
     private Element recruitUnitInEurope(Connection connection, Element recruitUnitInEuropeElement) {
         Game game = getFreeColServer().getGame();
         Player player = getFreeColServer().getPlayer(connection);
         Europe europe = player.getEurope();
 
         int slot = Integer.parseInt(recruitUnitInEuropeElement.getAttribute("slot"));
         int recruitable = europe.getRecruitable(slot);
         int newRecruitable = player.generateRecruitable();
 
         Unit unit = new Unit(game, player, recruitable);
 
         Element reply = Message.createNewRootElement("recruitUnitInEuropeConfirmed");
         reply.setAttribute("newRecruitable", Integer.toString(newRecruitable));
         reply.appendChild(unit.toXMLElement(player, reply.getOwnerDocument()));
 
         europe.recruit(slot, unit, newRecruitable);
 
         return reply;
     }
 
     /**
     * Handles an "emigrateUnitInEurope"-request from a client.
     *
     * @param connection The connection the message came from.
     * @param emigrateUnitInEuropeElement The element containing the request.
     */
     private Element emigrateUnitInEurope(Connection connection, Element emigrateUnitInEuropeElement) {
         Game game = getFreeColServer().getGame();
         Player player = getFreeColServer().getPlayer(connection);
         Europe europe = player.getEurope();
 
         int slot;
         if (player.hasFather(FoundingFather.WILLIAM_BREWSTER)) {
             slot = Integer.parseInt(emigrateUnitInEuropeElement.getAttribute("slot"));
         } else {
             slot = (int) ((Math.random() * 3) + 1);
         }
 
         int recruitable = europe.getRecruitable(slot);
         int newRecruitable = player.generateRecruitable();
 
         Unit unit = new Unit(game, player, recruitable);
 
         Element reply = Message.createNewRootElement("emigrateUnitInEuropeConfirmed");
         if (!player.hasFather(FoundingFather.WILLIAM_BREWSTER)) {
             reply.setAttribute("slot", Integer.toString(slot));
         }
         reply.setAttribute("newRecruitable", Integer.toString(newRecruitable));
         reply.appendChild(unit.toXMLElement(player, reply.getOwnerDocument()));
 
         europe.emigrate(slot, unit, newRecruitable);
 
         return reply;
     }
 
     /**
     * Handles a "trainUnitInEurope"-request from a client.
     *
     * @param connection The connection the message came from.
     * @param trainUnitInEuropeElement The element containing the request.
     */
     private Element trainUnitInEurope(Connection connection, Element trainUnitInEuropeElement) {
         Game game = getFreeColServer().getGame();
         Player player = getFreeColServer().getPlayer(connection);
         Europe europe = player.getEurope();
 
         int unitType = Integer.parseInt(trainUnitInEuropeElement.getAttribute("unitType"));
 
         Unit unit = new Unit(game, player, unitType);
 
         Element reply = Message.createNewRootElement("trainUnitInEuropeConfirmed");
         reply.appendChild(unit.toXMLElement(player, reply.getOwnerDocument()));
 
         europe.train(unit);
 
         return reply;
     }
 
     /**
     * Handles a "equipunit"-request from a client.
     *
     * @param connection The connection the message came from.
     * @param workElement The element containing the request.
     */
     private Element equipUnit(Connection connection, Element workElement) {
         Game game = getFreeColServer().getGame();
         ServerPlayer player = getFreeColServer().getPlayer(connection);
 
         Unit unit = (Unit) game.getFreeColGameObject(workElement.getAttribute("unit"));
         int type = Integer.parseInt(workElement.getAttribute("type"));
         int amount = Integer.parseInt(workElement.getAttribute("amount"));
 
         if (unit.getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
 
         switch(type) {
             case Goods.CROSSES:
                 unit.setMissionary((amount > 0));
                 break;
             case Goods.MUSKETS:
                 unit.setArmed((amount > 0)); // So give them muskets if the amount we want is greater than zero.
                 break;
             case Goods.HORSES:
                 unit.setMounted((amount > 0)); // As above.
                 break;
             case Goods.TOOLS:
                 int actualAmount = amount;
                 if ((actualAmount % 20) > 0) {
                     logger.warning("Trying to set a number of tools that is not a multiple of 20.");
                     actualAmount -= (actualAmount % 20);
                 }
                 unit.setNumberOfTools(actualAmount);
                 break;
             default:
                 logger.warning("Invalid type of goods to equip.");
                 return null;
         }
 
         if (unit.getLocation() instanceof Tile) {
             sendUpdatedTileToAll(unit.getTile(), player);
         }
 
         return null;
     }
 
 
     /**
     * Handles a "work"-request from a client.
     *
     * @param connection The connection the message came from.
     * @param workElement The element containing the request.
     */
     private Element work(Connection connection, Element workElement) {
         Game game = getFreeColServer().getGame();
         ServerPlayer player = getFreeColServer().getPlayer(connection);
 
         Unit unit = (Unit) game.getFreeColGameObject(workElement.getAttribute("unit"));
         WorkLocation workLocation = (WorkLocation) game.getFreeColGameObject(workElement.getAttribute("workLocation"));
 
         if (unit.getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
 
         if (workLocation == null) {
             throw new NullPointerException();
         }
 
         // No reason to send an update to other players: this is always hidden.
 
         unit.work(workLocation);
 
         return null;
     }
 
 
     /**
     * Handles a "changeWorkType"-request from a client.
     *
     * @param connection The connection the message came from.
     * @param workElement The element containing the request.
     */
     private Element changeWorkType(Connection connection, Element workElement) {
         Game game = getFreeColServer().getGame();
         ServerPlayer player = getFreeColServer().getPlayer(connection);
 
         Unit unit = (Unit) game.getFreeColGameObject(workElement.getAttribute("unit"));
         int workType = Integer.parseInt(workElement.getAttribute("workType"));
 
         if (unit.getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
         // No reason to send an update to other players: this is always hidden.
 
         unit.setWorkType(workType);
 
         return null;
     }
 
     /**
     * Handles a "setCurrentlyBuilding"-request from a client.
     *
     * @param connection The connection the message came from.
     * @param setCurrentlyBuildingElement The element containing the request.
     */
     private Element setCurrentlyBuilding(Connection connection, Element setCurrentlyBuildingElement) {
         Game game = getFreeColServer().getGame();
         ServerPlayer player = getFreeColServer().getPlayer(connection);
 
         Colony colony = (Colony) game.getFreeColGameObject(setCurrentlyBuildingElement.getAttribute("colony"));
         int type = Integer.parseInt(setCurrentlyBuildingElement.getAttribute("type"));
 
         if (colony.getOwner() != player) {
             throw new IllegalStateException("Not your colony!");
         }
 
         colony.setCurrentlyBuilding(type);
 
         sendUpdatedTileToAll(colony.getTile(), player);
 
         return null;
     }
 
 
     /**
     * Handles a "changeState"-message from a client.
     *
     * @param connection The connection the message came from.
     * @param changeStateElement The element containing the request.
     * @exception IllegalArgumentException If the data format of the message is invalid.
     * @exception IllegalStateException If the request is not accepted by the model.
     *
     */
     private Element changeState(Connection connection, Element changeStateElement) {
         Game game = getFreeColServer().getGame();
 
         ServerPlayer player = getFreeColServer().getPlayer(connection);
 
         Unit unit = (Unit) game.getFreeColGameObject(changeStateElement.getAttribute("unit"));
         int state = Integer.parseInt(changeStateElement.getAttribute("state"));
 
         if (unit == null) {
             throw new IllegalArgumentException("Could not find 'Unit' with specified ID: " + changeStateElement.getAttribute("unit"));
         }
 
         if (unit.getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
 
         Tile oldTile = unit.getTile();
 
         if (!unit.checkSetState(state)) {
             // Oh, really, Mr. Client? I'll show YOU!
             // kickPlayer(player);
             logger.warning("Can't set state " + state + ". Possible cheating attempt?");
             return null;
         }
         unit.setState(state);
 
         sendUpdatedTileToAll(oldTile, player);
 
         return null;
     }
 
     /**
     * Handles a "putOutsideColony"-request from a client.
     *
     * @param connection The connection the message came from.
     * @param putOutsideColonyElement The element containing the request.
     */
     private Element putOutsideColony(Connection connection, Element putOutsideColonyElement) {
         Game game = getFreeColServer().getGame();
         ServerPlayer player = getFreeColServer().getPlayer(connection);
 
         Unit unit = (Unit) game.getFreeColGameObject(putOutsideColonyElement.getAttribute("unit"));
 
         if (unit.getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
 
         unit.putOutsideColony();
 
         sendUpdatedTileToAll(unit.getTile(), player);
 
         return null;
     }
 
 
     /**
     * Handles a "payForBuilding"-request from a client.
     *
     * @param connection The connection the message came from.
     * @param payForBuildingElement The element containing the request.
     */
     private Element payForBuilding(Connection connection, Element payForBuildingElement) {
         Game game = getFreeColServer().getGame();
         ServerPlayer player = getFreeColServer().getPlayer(connection);
 
         Colony colony = (Colony) game.getFreeColGameObject(payForBuildingElement.getAttribute("colony"));
         if (colony.getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
 
         colony.payForBuilding();
 
         return null;
     }
 
     /**
     * Handles a "payArrears"-request from a client.
     *
     * @param connection The connection the message came from.
     * @param payArrearsElement The element containing the request.
     */
     private Element payArrears(Connection connection, Element payArrearsElement) {
         Game game = getFreeColServer().getGame();
         ServerPlayer player = getFreeColServer().getPlayer(connection);
 
         int goods = new Integer(payArrearsElement.getAttribute("goods")).intValue();
         int arrears = player.getArrears(goods);
         
         if (player.getGold() < arrears) {
             throw new IllegalStateException("Not enough gold to pay tax arrears!");
         } else {
             player.modifyGold(-arrears);
             player.setArrears(goods, 0);
         }
 
         return null;
     }
 
     /**
      * Handles a "toggleExports"-request from a client.
      *
      * @param connection The connection the message came from.
      * @param payArrearsElement The element containing the request.
      */
     private Element toggleExports(Connection connection, Element toggleExportsElement) {
         Game game = getFreeColServer().getGame();
         ServerPlayer player = getFreeColServer().getPlayer(connection);
 
         Colony colony = (Colony) game.getFreeColGameObject(toggleExportsElement.getAttribute("colony"));
         if (colony == null) {
             throw new IllegalArgumentException("Found no colony with ID " +
                                                toggleExportsElement.getAttribute("colony"));
         } else if (colony.getOwner() != player) {
             throw new IllegalStateException("Not your colony!");
         } else if (!colony.getBuilding(Building.CUSTOM_HOUSE).isBuilt()) {
             throw new IllegalStateException("Colony has no custom house!");
         }
         
         int goods = new Integer(toggleExportsElement.getAttribute("goods")).intValue();
         colony.toggleExports(goods);
 
         return null;
     }
 
     /**
     * Handles a "clearSpeciality"-request from a client.
     *
     * @param connection The connection the message came from.
     * @param clearSpecialityElement The element containing the request.
     */
     private Element clearSpeciality(Connection connection, Element clearSpecialityElement) {
         Game game = getFreeColServer().getGame();
         ServerPlayer player = getFreeColServer().getPlayer(connection);
 
         Unit unit = (Unit) game.getFreeColGameObject(clearSpecialityElement.getAttribute("unit"));
 
         if (unit.getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
 
         unit.clearSpeciality();
 
         if (unit.getLocation() instanceof Tile) {
             sendUpdatedTileToAll(unit.getTile(), player);
         }
 
         return null;
     }
 
 
     /**
     * Handles an "endTurn" notification from a client.
     *
     * @param connection The connection the message came from.
     * @param element The element containing the request.
     */
     private Element endTurn(Connection connection, Element element) {
         ServerPlayer player = getFreeColServer().getPlayer(connection);
         getFreeColServer().getInGameController().endTurn(player);
 
         return null;
     }
 
 
     /**
      * Handles a "disbandUnit"-message.
      *
      * @param connection The <code>Connection</code> the message was received on.
      * @param element The element containing the request.
      */
     private Element disbandUnit(Connection connection, Element element) {
         Game game = getFreeColServer().getGame();
         ServerPlayer player = getFreeColServer().getPlayer(connection);
         Unit unit = (Unit) game.getFreeColGameObject(element.getAttribute("unit"));
 
         if (unit == null) {
             throw new IllegalArgumentException("Could not find 'Unit' with specified ID: " + element.getAttribute("unit"));
         }
 
         if (unit.getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
 
         Tile oldTile = unit.getTile();
 
         unit.dispose();
 
         sendUpdatedTileToAll(oldTile, player);
 
         return null;
     }
 
 
     /**
      * Handles a "cashInTreasureTrain"-message.
      *
      * @param connection The <code>Connection</code> the message was received on.
      * @param element The element containing the request.
      */
     private Element cashInTreasureTrain(Connection connection, Element element) {
         Game game = getFreeColServer().getGame();
         ServerPlayer player = getFreeColServer().getPlayer(connection);
         Unit unit = (Unit) game.getFreeColGameObject(element.getAttribute("unit"));
 
         if (unit == null) {
             throw new IllegalArgumentException("Could not find 'Unit' with specified ID: " + element.getAttribute("unit"));
         }
 
         if (unit.getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
 
         Tile oldTile = unit.getTile();
         unit.cashInTreasureTrain();
 
         sendUpdatedTileToAll(oldTile, player);
 
         return null;
     }
 
 
     /**
      * Handles a "tradeProposition"-message.
      *
      * @param connection The <code>Connection</code> the message was received on.
      * @param element The element containing the request.
      */
     private Element tradeProposition(Connection connection, Element element) {
         Game game = getFreeColServer().getGame();
         ServerPlayer player = getFreeColServer().getPlayer(connection);
 
         Unit unit = (Unit) game.getFreeColGameObject(element.getAttribute("unit"));
         Settlement settlement = (Settlement) game.getFreeColGameObject(element.getAttribute("settlement"));
         Goods goods = new Goods(game, Message.getChildElement(element, Goods.getXMLElementTagName()));
         int gold = -1;
         if (element.hasAttribute("gold")) {
              gold = Integer.parseInt(element.getAttribute("gold"));
         }
 
         if (goods.getAmount() > 100) {
             throw new IllegalArgumentException();
         }
 
         if (unit == null) {
             throw new IllegalArgumentException("Could not find 'Unit' with specified ID: " + element.getAttribute("unit"));
         }
 
         if (unit.getMovesLeft() <= 0) {
             throw new IllegalStateException("No moves left!");
         }
 
         if (settlement == null) {
             throw new IllegalArgumentException("Could not find 'Settlement' with specified ID: " + element.getAttribute("settlement"));
         }
 
         if (unit.getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
 
         if (unit.getTile().getDistanceTo(settlement.getTile()) > 1) {
             throw new IllegalStateException("Not adjacent to settlemen!");
         }
 
         int returnGold = ((AIPlayer) getFreeColServer().getAIMain().getAIObject(settlement.getOwner())).tradeProposition(unit, settlement, goods, gold);
 
         Element tpaElement = Message.createNewRootElement("tradePropositionAnswer");
         tpaElement.setAttribute("gold", Integer.toString(returnGold));
 
         return tpaElement;
     }
 
 
     /**
      * Handles a "trade"-message.
      *
      * @param connection The <code>Connection</code> the message was received on.
      * @param element The element containing the request.
      */
     private Element trade(Connection connection, Element element) {
         Game game = getFreeColServer().getGame();
         ServerPlayer player = getFreeColServer().getPlayer(connection);
 
         Unit unit = (Unit) game.getFreeColGameObject(element.getAttribute("unit"));
         Settlement settlement = (Settlement) game.getFreeColGameObject(element.getAttribute("settlement"));
         Goods goods = new Goods(game, Message.getChildElement(element, Goods.getXMLElementTagName()));
         int gold = Integer.parseInt(element.getAttribute("gold"));
 
         if (gold <= 0) {
             throw new IllegalArgumentException();
         }
 
         if (goods.getAmount() > 100) {
             throw new IllegalArgumentException();
         }
 
         if (unit == null) {
             throw new IllegalArgumentException("Could not find 'Unit' with specified ID: " + element.getAttribute("unit"));
         }
 
         if (unit.getMovesLeft() <= 0) {
             throw new IllegalStateException("No moves left!");
         }
 
         if (settlement == null) {
             throw new IllegalArgumentException("Could not find 'Settlement' with specified ID: " + element.getAttribute("settlement"));
         }
 
         if (unit.getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
 
         if (unit.getTile().getDistanceTo(settlement.getTile()) > 1) {
             throw new IllegalStateException("Not adjacent to settlemen!");
         }
 
         int returnGold = ((AIPlayer) getFreeColServer().getAIMain().getAIObject(settlement.getOwner())).tradeProposition(unit, settlement, goods, gold);
         if (returnGold != gold) {
             throw new IllegalArgumentException("This was not the price we agreed upon! Cheater?");
         }
 
         unit.trade(settlement, goods, gold);
 
         return null;
     }
 
 
     /**
      * Handles a "deliverGift"-message.
      *
      * @param connection The <code>Connection</code> the message was received on.
      * @param element The element containing the request.
      */
     private Element deliverGift(Connection connection, Element element) {
         Game game = getFreeColServer().getGame();
         ServerPlayer player = getFreeColServer().getPlayer(connection);
 
         Unit unit = (Unit) game.getFreeColGameObject(element.getAttribute("unit"));
         Settlement settlement = (Settlement) game.getFreeColGameObject(element.getAttribute("settlement"));
         Goods goods = new Goods(game, Message.getChildElement(element, Goods.getXMLElementTagName()));
 
         if (goods.getAmount() > 100) {
             throw new IllegalArgumentException();
         }
 
         if (unit == null) {
             throw new IllegalArgumentException("Could not find 'Unit' with specified ID: " + element.getAttribute("unit"));
         }
 
         if (unit.getMovesLeft() <= 0) {
             throw new IllegalStateException("No moves left!");
         }
 
         if (settlement == null) {
             throw new IllegalArgumentException("Could not find 'Settlement' with specified ID: " + element.getAttribute("settlement"));
         }
 
         if (unit.getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
 
         if (unit.getTile().getDistanceTo(settlement.getTile()) > 1) {
             throw new IllegalStateException("Not adjacent to settlemen!");
         }
 
         ServerPlayer receiver = (ServerPlayer) settlement.getOwner();
         if (!receiver.isAI() && receiver.isConnected()) {
             Element deliverGiftElement = Message.createNewRootElement("deliverGift");
 
             Element unitElement = unit.toXMLElement(receiver, deliverGiftElement.getOwnerDocument());
             Element goodsContainerElement = unit.getGoodsContainer().toXMLElement(receiver, deliverGiftElement.getOwnerDocument());
             unitElement.replaceChild(goodsContainerElement, unitElement.getElementsByTagName(GoodsContainer.getXMLElementTagName()).item(0));
             deliverGiftElement.appendChild(unitElement);
 
             deliverGiftElement.setAttribute("settlement", settlement.getID());
             deliverGiftElement.appendChild(goods.toXMLElement(receiver, deliverGiftElement.getOwnerDocument()));
 
             try {
                 receiver.getConnection().send(deliverGiftElement);
             } catch (IOException e) {
                 logger.warning("Could not send \"deliverGift\"-message!");
             }
         }
 
         unit.deliverGift(settlement, goods);
 
         return null;
     }
 
 
     /**
      * Handles an "indianDemand"-message.
      *
      * @param connection The <code>Connection</code> the message was received on.
      * @param element The element containing the request.
      */
     private Element indianDemand(Connection connection, Element element) {
         Game game = getFreeColServer().getGame();
         ServerPlayer player = getFreeColServer().getPlayer(connection);
 
         Unit unit = (Unit) game.getFreeColGameObject(element.getAttribute("unit"));
         Colony colony = (Colony) game.getFreeColGameObject(element.getAttribute("colony"));
 
         if (unit == null) {
             throw new IllegalArgumentException("Could not find 'Unit' with specified ID: " +
                                                element.getAttribute("unit"));
         }
 
         if (unit.getMovesLeft() <= 0) {
             throw new IllegalStateException("No moves left!");
         }
 
         if (colony == null) {
             throw new IllegalArgumentException("Could not find 'Colony' with specified ID: " +
                                                element.getAttribute("colony"));
         }
 
         if (unit.getOwner() != player) {
             throw new IllegalStateException("Not your unit!");
         }
 
         if (unit.getTile().getDistanceTo(colony.getTile()) > 1) {
             throw new IllegalStateException("Not adjacent to colony!");
         }
 
         ServerPlayer receiver = (ServerPlayer) colony.getOwner();
         if (receiver.isConnected()) {
             int gold = 0;
             Goods goods = null;
             Element goodsElement = Message.getChildElement(element, Goods.getXMLElementTagName());
             if (goodsElement == null) {
                 gold = Integer.parseInt(element.getAttribute("gold"));
             } else {
                 goods = new Goods(game, goodsElement);
             }
 
             try {
                 Element reply = receiver.getConnection().ask(element);
                 boolean accepted = Boolean.valueOf(reply.getAttribute("accepted")).booleanValue();
                 if (accepted) {
                     if (goods == null) {
                         receiver.modifyGold(-gold);
                     } else {
                         colony.getGoodsContainer().removeGoods(goods);
                     }
                 }
                 return reply;
             } catch (IOException e) {
                 logger.warning("Could not send \"demand\"-message!");
             }
         }
 
         return null;
     }
 
 
     
     /**
     * Handles a "logout"-message.
     *
     * @param connection The <code>Connection</code> the message was received on.
     * @param logoutElement The element (root element in a DOM-parsed XML tree) that
     *                holds all the information.
     * @return The reply.
     */
     protected Element logout(Connection connection, Element logoutElement) {
         ServerPlayer player = getFreeColServer().getPlayer(connection);
 
         logger.info("Logout by: " + connection + ((player != null) ? " (" + player.getName() + ") " : ""));
 
         if (player == null) {
             return null;
         }
         // TODO
 
         // Remove the player's units/colonies from the map and send map updates to the
         // players that can see such units or colonies.
         // SHOULDN'T THIS WAIT UNTIL THE CURRENT PLAYER HAS FINISHED HIS TURN?
 
         /*
         player.setDead(true);
 
         Element setDeadElement = Message.createNewRootElement("setDead");
         setDeadElement.setAttribute("player", player.getID());
         freeColServer.getServer().sendToAll(setDeadElement, connection);
         */
 
         /*
         TODO: Setting the player dead directly should be a server option,
             but for now - allow the player to reconnect:
         */
         player.setConnected(false);
 
 
         if (getFreeColServer().getGame().getCurrentPlayer() == player && !getFreeColServer().isSingleplayer() && isHumanPlayersLeft()) {
             getFreeColServer().getInGameController().endTurn(player);
         }
 
         getFreeColServer().updateMetaServer();
 
         return null;
     }
 
 
     private boolean isHumanPlayersLeft() {
         Iterator playerIterator = getFreeColServer().getGame().getPlayerIterator();
         while (playerIterator.hasNext()) {
             Player p = (Player) playerIterator.next();
 
             if (!p.isDead() && !p.isAI()) {
                 return true;
             }
         }
 
         return false;
     }
 
 
     private void sendUpdatedTileToAll(Tile newTile, Player player) {
         Game game = getFreeColServer().getGame();
 
         Iterator enemyPlayerIterator = game.getPlayerIterator();
         while (enemyPlayerIterator.hasNext()) {
             ServerPlayer enemyPlayer = (ServerPlayer) enemyPlayerIterator.next();
 
             if (player != null && player.equals(enemyPlayer) || enemyPlayer.getConnection() == null) {
                 continue;
             }
 
             try {
                 if (enemyPlayer.canSee(newTile)) {
                     Element updateElement = Message.createNewRootElement("update");
                     updateElement.appendChild(newTile.toXMLElement(enemyPlayer, updateElement.getOwnerDocument()));
 
                     enemyPlayer.getConnection().send(updateElement);
                 }
             } catch (IOException e) {
                 logger.warning("Could not send message to: " + enemyPlayer.getName() + " with connection " + enemyPlayer.getConnection());
             }
         }
     }
 
 
     private void sendErrorToAll(String message, Player player) {
         Game game = getFreeColServer().getGame();
 
         Iterator enemyPlayerIterator = game.getPlayerIterator();
         while (enemyPlayerIterator.hasNext()) {
             ServerPlayer enemyPlayer = (ServerPlayer) enemyPlayerIterator.next();
 
             if ((player != null) && (player.equals(enemyPlayer)) || enemyPlayer.getConnection() == null) {
                 continue;
             }
 
             try {
                 Element errorElement = Message.createNewRootElement("error");
                 errorElement.setAttribute("message", message);
 
                 enemyPlayer.getConnection().send(errorElement);
             } catch (IOException e) {
                 logger.warning("Could not send message to: " + enemyPlayer.getName() + " with connection " + enemyPlayer.getConnection());
             }
         }
     }
 
 }
