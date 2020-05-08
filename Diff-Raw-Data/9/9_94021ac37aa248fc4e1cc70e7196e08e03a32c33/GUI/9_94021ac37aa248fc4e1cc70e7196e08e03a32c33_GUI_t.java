 /**
 This file is part of Like.
 
 Like is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
Like is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with Like.  If not, see http://www.gnu.org/licenses/.
 **/
 
 package me.endorphin8er.Rater;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.getspout.commons.ChatColor;
 import org.getspout.spoutapi.gui.Button;
 import org.getspout.spoutapi.gui.GenericButton;
 import org.getspout.spoutapi.gui.GenericContainer;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.GenericPopup;
 import org.getspout.spoutapi.gui.WidgetAnchor;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class GUI {
 	
 	GenericPopup popup;
 	GenericContainer box;
 	GenericLabel title;
 	GenericLabel likes;
 	GenericLabel dislikes;
 	GenericLabel agrees;
 	GenericLabel disagrees;
 	GenericLabel facepalms;
 	
 	GenericButton like;
 	GenericButton dislike;
 	GenericButton agree;
 	GenericButton disagree;
 	GenericButton facepalm;
 	
 	// Define the Rate object
 	Rate rate;
 	
 	Player target;
 
 	public GUI(SpoutPlayer sp, Player target_player, Plugin plugin, Rate rate_class) {
 		
 		rate = rate_class;
 		target = target_player;
 		
 		/*
 		 * Create all labels
 		 */
 		title = new GenericLabel(ChatColor.GREEN + target.getName());
 		title.setAnchor(WidgetAnchor.TOP_CENTER);
 		title.doResize();
 		
 		likes = new GenericLabel(ChatColor.AQUA + "Likes: " + ChatColor.WHITE + rate.Likes(target));
 		likes.setAnchor(WidgetAnchor.CENTER_CENTER);
 		
 		dislikes = new GenericLabel(ChatColor.GREEN + "Dislikes: " + ChatColor.WHITE + rate.DisLikes(target));
 		dislikes.setAnchor(WidgetAnchor.CENTER_CENTER);
 		
 		agrees = new GenericLabel(ChatColor.AQUA + "Agrees: " + ChatColor.WHITE + rate.Agrees(target));
 		agrees.setAnchor(WidgetAnchor.CENTER_CENTER);
 		
 		disagrees = new GenericLabel(ChatColor.GREEN + "Disagrees: " + ChatColor.WHITE + rate.DisAgrees(target));
 		disagrees.setAnchor(WidgetAnchor.CENTER_CENTER);
 		
 		facepalms = new GenericLabel(ChatColor.AQUA + "Facepalms: " + ChatColor.WHITE + rate.Facepalms(target));
 		facepalms.setAnchor(WidgetAnchor.CENTER_CENTER);
 		
 		/*
 		 * Create all buttons
 		 */
 		
 		like = new GenericButton("Like");
 		dislike = new GenericButton("DisLike");
 		agree = new GenericButton("Agree");
 		disagree = new GenericButton("DisAgree");
 		facepalm = new GenericButton("FacePalm");
 		
 		box = new GenericContainer();
 		
 		box.addChildren(title, likes, like, dislikes, dislike, agrees, agree, disagrees, disagree, facepalms, facepalm);
 		box.setHeight(250);
 		box.setWidth(200);
 		box.setX(115);
 		box.setY(0);
 		
 		box.setDirty(true);
 		
 		popup = new GenericPopup(); 
 		popup.attachWidget(plugin, box);
 		sp.getMainScreen().attachPopupScreen(popup);
 		
 	}
 	
 	public boolean isLikeButton(Button button) {
 		if (button.getText().equals("Like") && button.getPlugin() == rate) {
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	public boolean isDisLikeButton(Button button) {
 		if (button.getText().equals("DisLike") && button.getPlugin() == rate) {
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	public boolean isAgree(Button button) {
 		if (button.getText().equals("Agree") && button.getPlugin() == rate) {
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	public boolean isDisAgree(Button button) {
 		if (button.getText().equals("DisAgree") && button.getPlugin() == rate) {
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	public boolean isFacePalm(Button button) {
 		if (button.getText().equals("FacePalm") && button.getPlugin() == rate) {
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	public Player isTargetPlayer() {
 		return target;
 	}
 }
