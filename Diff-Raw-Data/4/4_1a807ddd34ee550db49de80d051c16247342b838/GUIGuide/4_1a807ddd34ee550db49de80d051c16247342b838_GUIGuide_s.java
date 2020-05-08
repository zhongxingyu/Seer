 /**
  * This file is part of InfoGuide.
  *
  * Copyright Dockter 2012 <mcsnetworks.com> InfoGuide is licensed under the GNU
  * General Public License.
  *
  * InfoGuide is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  *
  * As an exception, all classes which do not reference GPL licensed code are
  * hereby licensed under the GNU Lesser Public License, as described in GNU
  * General Public License.
  *
  * InfoGuide is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License, the GNU
  * Lesser Public License (for classes that fulfill the exception) and the GNU
  * General Public License along with this program. If not, see
  * <http://www.gnu.org/licenses/> for the GNU General Public License and the GNU
  * Lesser Public License.
  */
 package net.dockter.infoguide.gui;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 import net.dockter.infoguide.Main;
 import net.dockter.infoguide.guide.Guide;
 import net.dockter.infoguide.guide.GuideManager;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.getspout.spoutapi.gui.CheckBox;
 import org.getspout.spoutapi.gui.Color;
 import org.getspout.spoutapi.gui.ComboBox;
 import org.getspout.spoutapi.gui.GenericButton;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.GenericPopup;
 import org.getspout.spoutapi.gui.GenericTextField;
 import org.getspout.spoutapi.gui.GenericTexture;
 import org.getspout.spoutapi.gui.RenderPriority;
 import org.getspout.spoutapi.gui.Screen;
 import org.getspout.spoutapi.gui.WidgetAnchor;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class GUIGuide extends GenericPopup {
 
 	final GenericTextField guideField, guideInvisible;
 	final GenericLabel guideName, guideDate, pagelabel;
 	final public static HashMap<Player, Guide> map = new HashMap<Player, Guide>();
 	public int pageno = 1;
 	final GenericButton close, newb, saveb, deleteb, pd, pu;
 	final ComboBox box;
 	final CheckBox checkBox;
 	private final SpoutPlayer player;
 	public final Logger log = Logger.getLogger("Minecraft");
 
 	public GUIGuide(SpoutPlayer player) {
 		super();
 		this.player = player;
 
 		GenericLabel label = new GenericLabel();
 		label.setText(Main.getInstance().getConfig().getString("PromptTitle"));
 		label.setAnchor(WidgetAnchor.CENTER_CENTER);
 		label.shiftXPos(-35).shiftYPos(-122);
 		label.setPriority(RenderPriority.Lowest);
 		label.setWidth(-1).setHeight(-1);
 
 		guideName = new GenericLabel("TheGuideNameHere");
 		guideName.setWidth(-1).setHeight(-1);
 		guideName.setAnchor(WidgetAnchor.CENTER_CENTER);
 		guideName.shiftXPos(-200).shiftYPos(-105);
 
 		guideInvisible = new GenericTextField();
 		guideInvisible.setWidth(150).setHeight(18);
 		guideInvisible.setAnchor(WidgetAnchor.CENTER_CENTER);
 		guideInvisible.shiftXPos(-200).shiftYPos(-110);
 		guideInvisible.setMaximumCharacters(30);
 		guideInvisible.setMaximumLines(1);
 		guideInvisible.setVisible(false);
 
 		guideDate = new GenericLabel("Updated: " + new SimpleDateFormat("HH:mm dd-MM").format(Calendar.getInstance().getTime()));
 		guideDate.setWidth(-1).setHeight(-1);
 		guideDate.setAnchor(WidgetAnchor.CENTER_CENTER);
 		guideDate.shiftXPos(-200).shiftYPos(90);
 
 		box = new MyCombo(this);
 		box.setText("Guides");
 		box.setAnchor(WidgetAnchor.CENTER_CENTER);
 		box.setWidth(GenericLabel.getStringWidth("12345678901234567890123459"));
 		box.setHeight(18);
 		box.shiftXPos(25).shiftYPos(-110);
 		box.setAuto(true);
 		refreshItems();
 
 		GenericTexture border = new GenericTexture("http://www.almuramc.com/images/sguide.png");
 		border.setAnchor(WidgetAnchor.CENTER_CENTER);
 		border.setPriority(RenderPriority.High);
 		border.setWidth(626).setHeight(240);
 		border.shiftXPos(-220).shiftYPos(-128);
 
 		guideField = new GenericTextField();
 		guideField.setText("first guide goes here"); // The default text
 		guideField.setAnchor(WidgetAnchor.CENTER_CENTER);
 		guideField.setBorderColor(new Color(1.0F, 1.0F, 1.0F, 1.0F)); // White border
 		guideField.setMaximumCharacters(1000);
 		guideField.setMaximumLines(13);
 		guideField.setHeight(160).setWidth(377);
 		guideField.shiftXPos(-195).shiftYPos(-83);
 		guideField.setMargin(0);
 
 		close = new CloseButton(this);
 		close.setAuto(true);
 		close.setAnchor(WidgetAnchor.CENTER_CENTER);
 		close.setHeight(18).setWidth(40);
 		close.shiftXPos(142).shiftYPos(87);
 
 		pu = new PageUpButton(this);
 		pu.setAuto(true).setText("<<<");
 		pu.setAnchor(WidgetAnchor.CENTER_CENTER);
 		pu.setHeight(18).setWidth(40);
 		pu.shiftXPos(17).shiftYPos(87);
 
 		pagelabel = new GenericLabel();
 		pagelabel.setPriority(RenderPriority.Highest);
 		pagelabel.setText(Integer.toString(pageno));
 		pagelabel.setAnchor(WidgetAnchor.CENTER_CENTER);
 		pagelabel.shiftXPos(66).shiftYPos(92);
 		pagelabel.setPriority(RenderPriority.Lowest);
 		pagelabel.setWidth(5).setHeight(18);
 
 		pd = new PageDownButton(this);
 		pd.setAuto(true).setText(">>>");
 		pd.setAnchor(WidgetAnchor.CENTER_CENTER);
 		pd.setHeight(18).setWidth(40);
 		pd.shiftXPos(82).shiftYPos(87);
 
 		checkBox = new BypassCheckBox(player, this);
 		checkBox.setText("Bypass");
 		checkBox.setAnchor(WidgetAnchor.CENTER_CENTER);
 		checkBox.setHeight(20).setWidth(19);
 		checkBox.shiftXPos(-52).shiftYPos(87);
 		checkBox.setAuto(true);
 
 		this.setTransparent(true);
 		attachWidgets(Main.getInstance(), border, label);
 
 		this.setTransparent(true);
 		attachWidget(Main.getInstance(), label);
 		attachWidget(Main.getInstance(), border);
 		attachWidget(Main.getInstance(), guideField);
 		attachWidget(Main.getInstance(), close);
 		attachWidget(Main.getInstance(), pu);
 		attachWidget(Main.getInstance(), pagelabel);
 		attachWidget(Main.getInstance(), pd);
 		attachWidget(Main.getInstance(), guideName);
 		attachWidget(Main.getInstance(), guideInvisible);
 		attachWidget(Main.getInstance(), guideDate);
 		attachWidget(Main.getInstance(), box);
 		if (Main.getInstance().canBypass(player.getName()) || player.hasPermission("infoguide.bypass") || player.hasPermission("infoguide.admin")) {
 			attachWidget(Main.getInstance(), checkBox);
 		}
 
 		if (player.hasPermission("infoguide.edit") || player.hasPermission("infoguide.admin")) {
 			saveb = new SaveButton(this);
 			saveb.setAnchor(WidgetAnchor.CENTER_CENTER);
 			saveb.setAuto(true).setHeight(18).setWidth(40).shiftXPos(-145).shiftYPos(87);
 			attachWidget(Main.getInstance(), saveb);
 		} else {
 			saveb = null;
 		}
 
 		if (player.hasPermission("infoguide.create") || player.hasPermission("infoguide.edit") || player.hasPermission("infoguide.admin")) {
 			guideDate.setVisible(false);
 			newb = new NewButton(this);
 			newb.setAuto(true);
 			newb.setAnchor(WidgetAnchor.CENTER_CENTER);
 			newb.setAuto(true).setHeight(18).setWidth(40).shiftXPos(-190).shiftYPos(87);
 			attachWidget(Main.getInstance(), newb);
 		} else {
 			newb = null;
 			guideDate.setVisible(true);
 		}
 
 		if (player.hasPermission("infoguide.delete") || player.hasPermission("infoguide.admin")) {
 			deleteb = new DeleteButton(this);
 			deleteb.setAnchor(WidgetAnchor.CENTER_CENTER);
 			deleteb.setAuto(true).setHeight(18).setWidth(40).shiftXPos(-100).shiftYPos(87);
 			attachWidget(Main.getInstance(), deleteb);
 		} else {
 			deleteb = null;
 		}
 
 		if (player.hasPermission("infoguide.moderatorguide")) {
 			setGuide(GuideManager.getLoadedGuides().get(Main.getInstance().getConfig().getString("ModeratorGuide")));
 			return;
 		} else if (player.hasPermission("infoguide.supermemberguide")) {
 			setGuide(GuideManager.getLoadedGuides().get(Main.getInstance().getConfig().getString("SuperMemberGuide")));
 			return;
 		} else if (player.hasPermission("infoguide.memberguide")) {
 			setGuide(GuideManager.getLoadedGuides().get(Main.getInstance().getConfig().getString("MemberGuide")));
 			return;
 		} else if (player.hasPermission("infoguide.guestguide")) {
 			setGuide(GuideManager.getLoadedGuides().get(Main.getInstance().getConfig().getString("GuestGuide")));
 			return;
 		} else {
 			setGuide(GuideManager.getLoadedGuides().get(Main.getInstance().getConfig().getString("DefaultGuide")));
 		}
 	}
 	private Guide guide;
 
 	public void setGuide(Guide guide) {
 		if (guide == null) {
 			return;
 		}
 		this.guide = guide;
 		guideDate.setText("Updated: " + guide.getDate());
 		guideName.setText(guide.getName()).setWidth(-1);
 		map.put(player, guide);
 		pageno = 1;
 		pagelabel.setText(Integer.toString(pageno));
 		guideField.setText(guide.getPage(1));
 		if (pageno == guide.getPages() && player.hasPermission("infoguide.edit")) {
 			pd.setText("+");
 			pd.setDirty(true);
 		}
 	}
 
 	public void pageUp() {
 		pageno = pageno - 1;
 		if (pageno == 0) {
 			pageno = 1;
 		}
 		Guide gguide = map.get(player);
 		if (pageno == gguide.getPages() - 1) {
 			pd.setText(">>>");
 			pd.setDirty(true);
 		}
 		guideField.setText(gguide.getPage(pageno));
 		pagelabel.setText(Integer.toString(pageno));
 	}
 
 	public void pageDown() {
 		pageno++;
 		Guide gguide = map.get(player);
 		if (pageno == gguide.getPages() + 1) {
 			if (player.hasPermission("infoguide.edit")) {
 				gguide.addPage();
 				pd.setText(">>>");
 			}
 			pd.setDirty(true);
 			pageno--;
 		}
		if (pageno == gguide.getPages() && player.hasPermission("infoguide.edit")) {
 			pd.setText("+");
 			pd.setDirty(true);
 		}
 		guideField.setText(gguide.getPage(pageno));
 		pagelabel.setText(Integer.toString(pageno));
 	}
 
 	public void onNewClick() {
 		setGuide(new Guide("", "", new ArrayList<String>()));
 		guideName.setVisible(false);
 		guideInvisible.setVisible(true);
 	}
 
 	void onSaveClick(String playerName) {
 
 		guide.setPage(pageno, guideField.getText());
 
 		guide.setDate(new SimpleDateFormat("HH:mm dd-MM").format(Calendar.getInstance().getTime()));
 		if (guideInvisible.isVisible()) {
 			guide.setName(guideInvisible.getText());
 			guideName.setText(guideInvisible.getText()).setWidth(-1);
 			guideInvisible.setVisible(false);
 			guideName.setVisible(true);
 			GuideManager.addGuide(guide);
 		}
 		Bukkit.broadcastMessage(ChatColor.GOLD + playerName + ChatColor.YELLOW + " updated the guide " + ChatColor.GOLD + guide.getName() + ChatColor.YELLOW + "!");
 		guide.save();
 		refreshItems();
 	}
 
 	void onDeleteClick() {
 		if (box.getItems().size() == 1) {
 			return;
 		}
 		GuideManager.removeLoadedGuide(guideName.getText());
 		refreshItems();
 		setGuide(GuideManager.getLoadedGuides().get(box.getItems().get(0)));
 	}
 
 	void onCloseClick() {
 		Screen screen = ((SpoutPlayer) player).getMainScreen();
 		screen.removeWidget(this);
 		//player.getMainScreen().closePopup();
 		player.closeActiveWindow();
 
 	}
 
 	void onSelect(int i, String text) {
 		setGuide(GuideManager.getLoadedGuides().get(text));
 	}
 
 	private void refreshItems() {
 		List<String> items = new ArrayList<String>();
 		for (String gguide : GuideManager.getLoadedGuides().keySet()) {
 			if (player.hasPermission("infoguide.view." + gguide) || player.hasPermission("infoguide.view")) {
 				items.add(gguide);
 			}
 		}
 		Collections.sort(items, String.CASE_INSENSITIVE_ORDER);
 		box.setItems(items);
 		box.setDirty(true);
 	}
 }
