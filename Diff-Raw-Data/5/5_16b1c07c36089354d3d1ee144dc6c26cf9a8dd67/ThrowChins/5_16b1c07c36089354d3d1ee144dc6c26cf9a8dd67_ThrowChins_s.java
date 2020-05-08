 package EpicsChins.util.tasks;
 
 import EpicsChins.util.Data;
 import EpicsChins.util.Method;
 import EpicsChins.util.Paint;
 import org.powerbot.concurrent.strategy.Strategy;
 import org.powerbot.game.api.methods.Game;
 import org.powerbot.game.api.methods.interactive.NPCs;
 import org.powerbot.game.api.methods.interactive.Players;
 import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.api.methods.tab.Prayer;
 import org.powerbot.game.api.methods.tab.Skills;
 import org.powerbot.game.api.util.Random;
 import org.powerbot.game.api.util.Time;
 import org.powerbot.game.api.util.Timer;
 import org.powerbot.game.api.wrappers.interactive.NPC;
 import org.powerbot.game.api.wrappers.node.Item;
 import org.powerbot.game.bot.Context;
 
 import java.util.logging.Logger;
 
 /**
  * User: Epics
  * Date: 8/28/12
  * Time: 8:24 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ThrowChins extends Strategy implements Runnable {
 		NPC monkeyZombie;
 
 	@Override
 	public void run() {
 		if (Data.logAttackCode == 0) {
 			Context.get().getActiveScript().log.info("Running attack code");
 			Data.logAttackCode++;
 		}
 		if (Inventory.getCount(Data.chinNumber) >= 1) {
 			Context.get().getActiveScript().log.info("Chins are in inventory, that must mean greegree is equipped. Equipping...");
 			Item chinNumberItem = Inventory.getItem(10034);
 			chinNumberItem.getWidgetChild().click(true);
 			final int id = 10034;
 			final int count = Inventory.getCount(id);
 			final Timer t = new Timer(2500);
 
 			while (t.isRunning() && Inventory.getCount(id) == count) {
 				Time.sleep(50);
 			}
 		}
 		int rangingFlaskData = 0;
 		for (Item y : Inventory.getItems()) {
 		for (int x : Data.FLASK_RANGING) {
 			if (y.getId() == x) {
 				rangingFlaskData++;
 			}
 		}
 		}
 
 		final int REAL_RANGE = Skills.getRealLevel(Skills.RANGE);
 		final int POTTED_RANGE = Skills.getLevel(Skills.RANGE);
 		final int RANGE_DIFFERENCE = POTTED_RANGE - REAL_RANGE;
 		final int CHIN_THROW_ID = 2779;
 
 		if (Players.getLocal().getInteracting() != null) {
 			Timer throwtimer = new Timer(5000);
 			while (Players.getLocal().getAnimation() == CHIN_THROW_ID && throwtimer.isRunning()) {
 				Time.sleep(Random.nextInt(20, 50));
 				if (Players.getLocal().getAnimation() == CHIN_THROW_ID) {
 					Data.chinNumber--;
 				}
 			}
 		}
 		Method.doAttackMonkey(monkeyZombie);
 		if (rangingFlaskData > 0 && Players.getLocal().getInteracting().equals(monkeyZombie) && Prayer.getPoints() >= 42 && !Method.isPoisoned() && Players.getLocal().getHpPercent() >= 90 && RANGE_DIFFERENCE >= 3) {
 			Context.get().getActiveScript().log.info("Killing monkeys and nothing is needed. Using antiban...");
 			Method.antiban();
 		}
 		Time.sleep(Random.nextInt(50, 75));
 		if (!Prayer.isQuickOn()) {
 			Prayer.setQuick();
 			if (Players.getLocal().getPrayerIcon() == Prayer.PRAYER_BOOK_NORMAL) {
 				if (Players.getLocal().getPrayerIcon() == 19) {
 				} else {
 					Logger.getLogger("EpicsChins").info("You didn't set up your quick prayer correctly. Shutting down...");
 				}
 			} else if (Players.getLocal().getPrayerIcon() == 9) {
 			} else {
 				Logger.getLogger("EpicsChins").info("You didn't set up your quick prayer correctly. Shutting down...");
 
 			}
 		}
 		if (Method.isPoisoned()) {
 			Method.doDrinkAntipoison();
 		} else {
 			Context.get().getActiveScript().log.info("We're out of antipoison & we're poisoned! Teleporting to safety to bank...");
 			Method.doBreakTab();
 		}
 		if (Players.getLocal().getAnimation() == CHIN_THROW_ID) {
 			Paint.chinsThrown++;
 			Time.sleep(Random.nextInt(20, 50));
 		}
		if (monkeyZombie != null && monkeyZombie.getAnimation() == Data.ID_ANIMATION_DEATH_ZOMBIE) {
 			Paint.zombieKillCount++;
 		}
 		final int VIAL_ID = 229;
 		final Item VIAL = Inventory.getItem(VIAL_ID);
 		if (Inventory.getItem() == VIAL) {
 			VIAL.getWidgetChild().interact("Drop");
 		}
 		Data.logAttackCode = 0;
 	}
 
 	@Override
 	public boolean validate() {
 		int prayPotCountData = 0;
 		for (Item y : Inventory.getItems()) {
 			for (int x : Data.POT_PRAYER) {
 				if (y.getId() == x) {
 					prayPotCountData++;
 				}
 			}
 		}
		return (monkeyZombie = NPCs.getNearest(Data.ID_NPC_MONKEY_ZOMBIE))!= null && Data.chinNumber >= 200 && prayPotCountData > 0 && Data.START_SCRIPT && Game.isLoggedIn() && !Data.runCheck && Data.atDestination;
 	}
 }
