 /*
  * This file is part of mmoChatPirate <http://github.com/mmoMinecraftDev/mmoChatPirate>.
  *
  * mmoChatPirate is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package mmo.Chat;
 
 import java.util.regex.Pattern;
 
 import mmo.Core.ChatAPI.MMOChatEvent;
import mmo.Core.MMOListener;
 import mmo.Core.MMOPlugin;
 import mmo.Core.util.EnumBitSet;
 
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
 
public class MMOChatPirate extends MMOPlugin {
 	static private String[][] replace = {{"\\bmy\\b", "me"},
 		{"\\bboss\\b|\\bmanager\\b", "admiral"},
 		{"\\bcaptain\\b", "Cap'n"},
 		{"\\bmyself\\b", "meself"},
 		{"\\byou(r?)\\b", "ye$1"},
 		{"\\bfriend\\b", "matey"},
 		{"\\bfriends\\b", "maties"},
 		{"\\bco[-]?worker(s?)\\b", "shipmate$1"},
 		{"\\bearlier\\b", "afore"},
 		{"\\bbold\\b", "auld"},
 		{"\\bthe\\b", "th'"},
 		{"\\bof\\b", "o'"},
 		{"\\bdon'?t\\b|\\bdo not\\b", "dern't"},
 		{"\\b(ne|e|o)ver\\b", "$1'er"},
 		{"\\byes\\b", "aye"},
 		{"\\bno\\b", "nay"},
 		{"\\bdon't know\\b", "dinna"},
 		{"\\b(ha|di)dn'?t\\b", "$1'nae"},
 		{"\\bwasn'?t\\b", "weren't"},
 		{"\\bhaven'?t\\b", "ha'nae"},
 		{"\\bfor\\b", "fer"},
 		{"\\bbetween\\b", "betwixt"},
 		{"\\baround\\b", "aroun'"},
 		{"\\bto\\b", "t'"},
 		{"\\bit's?\\b", "tis"},
 		{"\\bwoman\\b|\\blady\\b", "wench"},
 		{"\\bwife\\b", "lady"},
 		{"\\bgirl\\b", "lass"},
 		{"\\bgirls\\b", "lassies"},
 		{"\\bguy\\b|\\bman\\b|\\bfellow\\b|\\bdude\\b", "lubber"},
 		{"\\bboy\\b", "lad"},
 		{"\\bboys\\b", "laddies"},
 		{"\\bchildren\\b", "little sandcrabs"},
 		{"\\bkids\\b", "minnows"},
 		{"\\bhim\\b", "that scurvey dog"},
 		{"\\bher\\b", "that comely wench"},
 		{"\\bhim\\.\\b", "that drunken sailor"},
 		{"\\bhe\\b", "the ornery cuss"},
 		{"\\bshe\\b", "the winsome lass"},
 		{"\\b(s?)he's\\b", "$1he be"},
 		{"\\bwas\\b", "were bein'"},
 		{"\\bhey\\b", "avast"},
 		{"\\bher\\.\\b", "that lovely lass"},
 		{"\\bfood\\b", "chow"},
 		{"\\broad(s?)\\b", "sea$1"},
 		{"\\bstreet(s?)\\b", "river$1"},
 		{"\\bhighway(s?)\\b", "ocean$1"},
 		{"\\bcar(s?)\\b", "boat$1"},
 		{"\\btruck(s?)\\b", "schooner$1"},
 		{"\\bsuv\\b|\\bcoach\\b", "ship"},
 		{"\\bairplane\\b|\\bjet\\b", "flying machine"},
 		{"\\bmachine\\b", "contraption"},
 		{"\\bdriving\\b", "sailing"},
 		{"\\bdrive\\b", "sail"},
 		{"ing\\b", "in'"},
 		{"ings\\b", "in's"},
 		{"([^\\.!?])$", "$1."}};
 
 	static private String[] flavour = {
 		", avast$1",
 		"$1 Ahoy!",
 		", and a bottle of rum!",
 		", by Blackbeard's sword$1",
 		", by Davy Jones' locker$1",
 		"$1 Walk the plank!",
 		"$1 Aarrr!",
 		"$1 Yaaarrrrr!",
 		", pass the grog!",
 		", and dinna spare the whip!",
 		", with a chest full of booty$1",
 		", and a bucket o' chum$1",
 		", we'll keel-haul ye!",
 		"$1 Shiver me timbers!",
 		"$1 And hoist the mainsail!",
 		"$1 And swab the deck!",
 		", ye scurvey dog$1",
 		"$1 Fire the cannons!",
 		", to be sure$1",
		", I'll warrant ye$1"};
 
 	@Override
 	public EnumBitSet mmoSupport(EnumBitSet support) {
 		support.set(Support.MMO_NO_CONFIG);
 		return support;
 	}

 	@Override
 	public void onEnable() {
 		super.onEnable();
		pm.registerEvent(Type.CUSTOM_EVENT,
			new MMOListener() {
				@Override
				public void onMMOChat(MMOChatEvent event) {
					if (event.hasFilter("Pirate") && event.getPlayer().hasPermission("mmo.chat.pirate")) {
						event.setMessage(translate(event.getMessage()));
					}
				}
			}, Priority.Lowest, this);
 	}
 
 	static public String translate(String text) {
 		for (String[] words : replace) {
 			text = Pattern.compile(words[0], Pattern.CASE_INSENSITIVE).matcher(text).replaceAll(words[1]);
 		}
 		if (Math.random() > 0.8f) {
 			text = text.replaceFirst("([.!?])$", flavour[(int) (Math.random() * flavour.length)]);
 		}
 		return text;
 	}
 }
