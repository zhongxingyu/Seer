 package com.crystalclash.renders.helpers.ui;
 
 import java.util.Enumeration;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.utils.Align;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.crystalclash.controllers.GameController;
 import com.crystalclash.renders.helpers.ResourceHelper;
 
 public class UnitThumbsList extends Group {
 	private Table table;
 	private Image imgTableBg;
 	private ScrollPane scrollPane;
 	private Label lblUnitsCount;
	private Label lblMessage;
 	private UnitThumb selectedThumb;
 
 	public UnitThumbsList(final UnitListSelectListener unitThumbListener, final UnitItemSplashListener unitSplashListener) {
 		lblUnitsCount = new Label("", new LabelStyle(ResourceHelper.getBigFont(), Color.WHITE));
		lblMessage = new Label("Units placed", new LabelStyle(ResourceHelper.getBigFont(), Color.WHITE));
 
 		imgTableBg = new Image(ResourceHelper.getTexture("in_game/first_turn/list_background"));
 		addActor(imgTableBg);
 
 		table = new Table();
 		scrollPane = new ScrollPane(table);
 		scrollPane.setPosition(10, 155);
		lblMessage.setPosition(230, 85);
		lblUnitsCount.setPosition(275, 50);
 		scrollPane.setScrollingDisabled(true, false);
 		scrollPane.setOverscroll(false, true);
 		scrollPane.setSmoothScrolling(true);
 		scrollPane.setForceScroll(false, true);
 		scrollPane.setSize(623, 685);
 		scrollPane.invalidate();
 		addActor(scrollPane);
 
 		table.align(Align.top | Align.left);
 		table.defaults().width(198).height(252).padLeft(6).padTop(6);
 		// List items
 		Enumeration<String> unit_names = GameController.getUnitNames();
 		String unit_name;
 		int i = 0;
 		ClickListener unitItemClickListener = new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				UnitThumb item = (UnitThumb) event.getListenerActor().getParent();
 				if (item != null) {
 					if (item == selectedThumb) {
 						item.desselect();
 						unitThumbListener.select(item.getUnitName(), false, x, y);
 						selectedThumb = null;
 					} else {
 						if (selectedThumb != null)
 							selectedThumb.desselect();
 						item.select();
 						selectedThumb = item;
 						unitThumbListener.select(item.getUnitName(), true, x, y);
 					}
 				}
 			}
 		};
 		ClickListener unitItemSplashListener = new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				UnitThumb item = (UnitThumb) event.getListenerActor().getParent();
 				unitSplashListener.openSplash(item.getUnitName());
 			}
 		};
 		while (unit_names.hasMoreElements()) {
 			if (i == 3) {
 				table.row();
 				i = 0;
 			}
 			i++;
 			unit_name = unit_names.nextElement();
 			UnitThumb item = new UnitThumb(unit_name, unitItemClickListener, unitItemSplashListener);
 			table.add(item);
 		}
 
 		addActor(lblUnitsCount);
		addActor(lblMessage);
 	}
 
 	public void setUnitCountText(String str) {
 		lblUnitsCount.setText(str);
 	}
 
 	public void desSelect() {
 		if (selectedThumb != null) {
 			selectedThumb.desselect();
 			selectedThumb = null;
 		}
 	}
 }
