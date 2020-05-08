 package ip.industrialProcessing.gui;
 
 import ip.industrialProcessing.gui.components.GuiLayoutPanel;
 import ip.industrialProcessing.gui.components.GuiLayoutPanelType;
 import ip.industrialProcessing.gui.components.GuiLayoutPanelTypeClass;
 import ip.industrialProcessing.gui.container.ContainerIP;
 import ip.industrialProcessing.gui.container.slot.layout.SlotLayoutType;
 import ip.industrialProcessing.gui.container.slot.layout.components.SlotLayoutInventory;
 import ip.industrialProcessing.recipes.Recipe;
 
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.gui.Gui;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.Slot;
 import net.minecraft.tileentity.TileEntity;
 
 public class GuiLayout {
 
 	ArrayList<GuiLayoutPanel> layoutPanels;
 	int slotIndex = 0;
 
 	public int multipleLines = 1;
 	public boolean useMargin = true;
 	public boolean drawTitel = true;
 	public boolean drawInventoryTitel = true;
 	GuiLayoutPanelType[] sortingOrder = new GuiLayoutPanelType[] { GuiLayoutPanelType.heat, GuiLayoutPanelType.simpleHeat, GuiLayoutPanelType.simpleTankInput, GuiLayoutPanelType.tankInput, GuiLayoutPanelType.slotsInput, GuiLayoutPanelType.worker, GuiLayoutPanelType.slotsOutput, GuiLayoutPanelType.tankOutput, GuiLayoutPanelType.simpleTankOutput };
 
 	public GuiLayout() {
 		layoutPanels = new ArrayList<GuiLayoutPanel>();
 		addLayoutPanel(GuiLayoutPanelType.slotsInventory);
 		slotIndex = 0;
 	}
 
 	public GuiLayoutPanel addLayoutPanel(GuiLayoutPanelType type, int... location) {
 		GuiLayoutPanel panel = null;
 		int x = 0;
 		int y = 0;
 		int offsetX = 0;
 		int offsetY = 0;
 		boolean fixed = false;
 		if (location.length == 2) {
 			fixed = true;
 		}
 		for (int i = 0; i < layoutPanels.size(); i++) {
 			if (layoutPanels.get(i).type == type) {
 				offsetX += 2 + layoutPanels.get(i).getWidth();
 				offsetY += 2 + layoutPanels.get(i).getHeight();
 			}
 		}
 
 		switch (type) {
 		case slotsInput:
 			if (location.length == 2) {
 				x = location[0];
 				y = location[2];
 			} else {
 				x = 47 + offsetX;
 				y = 40;
 			}
 			panel = new GuiLayoutPanel(this, type, x, y, 52, 52, fixed);
 			// SlotLayout set by Block
 			// GuiContainerLayout set in above function
 			layoutPanels.add(panel);
 			break;
 		case slotsOutput:
 			if (location.length == 2) {
 				x = location[0];
 				y = location[2];
 			} else {
 			x = 135 + offsetX;
 			y = 40;
 			}
 			panel = new GuiLayoutPanel(this, type, x, y, 52, 52, fixed);
 			// SlotLayout set by Block
 			// GuiContainerLayout set in above function
 			layoutPanels.add(panel);
 			break;
 		case slotsSide:
 			break;
 		case tankInput:
 		case tankOutput:
 			if (location.length == 2) {
 				x = location[0];
 				y = location[2];
 			} else {
 			x = 0;
 			y = 40;
 			}
 			panel = new GuiLayoutPanel(this, type, x, y, 40, 52, fixed);
 			panel.setSlotLayout(SlotLayoutType.fixed, 2);
 			panel.setGuiContainerLayout(type);
 			layoutPanels.add(panel);
 			break;
 		case simpleTankInput:
 		case simpleTankOutput:
 			if (location.length == 2) {
 				x = location[0];
 				y = location[2];
 			} else {
 			x = 0;
 			y = 40;
 			}
 			panel = new GuiLayoutPanel(this, type, x, y, 18, 52, fixed);
 			panel.setGuiContainerLayout(type);
 			// no draw required
 			layoutPanels.add(panel);
 			break;
 		case slotsInventory:
 			if (location.length == 2) {
 				x = location[0];
 				y = location[2];
 			} else {
 			x = 8;
 			y = 116;
 			}
 			panel = new GuiLayoutPanel(this, type, x, y, 222, 76, fixed);
 			panel.setSlotLayout(SlotLayoutType.inventory, 0);
 			// no draw required
 			layoutPanels.add(panel);
 			break;
 		case worker:
 			if (location.length == 2) {
 				x = location[0];
 				y = location[2];
 			} else {
 			x = 106;
 			y = 59 + offsetY;
 			}
 			panel = new GuiLayoutPanel(this, type, x, y, 24, 16, fixed);
 			// No slotLayout
 			panel.setGuiContainerLayout(type);
 			layoutPanels.add(panel);
 			break;
 		case power:
 			if (location.length == 2) {
 				x = location[0];
 				y = location[2];
 			} else {
 			x = 150;
 			y = 9 + offsetY;
 			}
 			panel = new GuiLayoutPanel(this, type, x, y, 74, 18, fixed);
 			panel.setSlotLayout(SlotLayoutType.fixed, 1);
 			panel.setGuiContainerLayout(type);
 			layoutPanels.add(panel);
 			break;
 		case heat:
 			if (location.length == 2) {
 				x = location[0];
 				y = location[2];
 			} else {
 			x = 0;
 			y = 39 + offsetY;
 			}
 			panel = new GuiLayoutPanel(this, type, x, y, 35, 54,fixed, 15, 54);
 			panel.setSlotLayout(SlotLayoutType.fixed, 2);
 			panel.setGuiContainerLayout(type);
 			layoutPanels.add(panel);
 			break;
 		case simpleHeat:
 			if (location.length == 2) {
 				x = location[0];
 				y = location[2];
 			} else {
 			x = 8;
 			y = 39 + offsetY;
 			}
 			panel = new GuiLayoutPanel(this, type, x, y, 15, 54, fixed);
 			// no slotLayout
 			panel.setGuiContainerLayout(type);
 			layoutPanels.add(panel);
 			break;
 		case lifespan:
 			if (location.length == 2) {
 				x = location[0];
 				y = location[2];
 			} else {
 			x = 8;
 			y = 16 + offsetY;
 			}
 			panel = new GuiLayoutPanel(this, type, x, y, 50, 8, fixed);
 			// no slotLayout
 			panel.setGuiContainerLayout(type);
 			layoutPanels.add(panel);
 			break;
 		default:
 			break;
 
 		}
 		reDoLayout();
 		return panel;
 	}
 
 	public int getSlotIndex() {
 		return slotIndex;
 	}
 
 	public void setSlotIndex(int val) {
 		slotIndex = val;
 	}
 
 	public Iterator<Slot> getSlotsMachine(IInventory iinventory) {
 		ArrayList<Slot> slots = new ArrayList<Slot>();
 		Iterator<GuiLayoutPanel> it = layoutPanels.iterator();
 		while (it.hasNext()) {
 			GuiLayoutPanel container = it.next();
 			if (container.getSlotLayout() != null && !(container.getSlotLayout() instanceof SlotLayoutInventory)) {
 				Slot[] containerSlots = container.getSlotLayout().getGuiContainerSlots(iinventory);
 				for (int i = 0; i < containerSlots.length; i++)
 					slots.add(containerSlots[i]);
 			}
 		}
 		return slots.iterator();
 	}
 
 	public Iterator<Slot> getSlotsInventory(IInventory iinventory) {
 		ArrayList<Slot> slots = new ArrayList<Slot>();
 		Iterator<GuiLayoutPanel> it = layoutPanels.iterator();
 		while (it.hasNext()) {
 			GuiLayoutPanel container = it.next();
 			if (container.getSlotLayout() != null && (container.getSlotLayout() instanceof SlotLayoutInventory)) {
 				Slot[] containerSlots = container.getSlotLayout().getGuiContainerSlots(iinventory);
 				for (int i = 0; i < containerSlots.length; i++)
 					slots.add(containerSlots[i]);
 			}
 		}
 		return slots.iterator();
 	}
 
 	public void reDoLayout() {
 		int widthScreen = 236;
 		for (int j = 0; j < multipleLines; j++) {
 			ArrayList<GuiLayoutPanel> panels = getOrganizedPanels(j + 1);
 			int totalWidth = 0;
 			for (int i = 0; i < panels.size(); i++) {
 				totalWidth += panels.get(i).getWidth();
 			}
 			int margin = (widthScreen - totalWidth) / (panels.size() + 1);
 			if (useMargin) {
 				totalWidth = 0;
 				for (int i = 0; i < panels.size(); i++) {
 					panels.get(i).setX(totalWidth + (i + 1) * margin);
 					totalWidth += panels.get(i).getWidth();
 					if (multipleLines > 1) {
 						panels.get(i).setY(j * 56 + 5);
 					}
 				}
 			} else {
 				totalWidth = (widthScreen - totalWidth) / 2;
 				for (int i = 0; i < panels.size(); i++) {
 					panels.get(i).setX(totalWidth);
 					totalWidth += panels.get(i).getWidth() + 2;
 					if (multipleLines > 1) {
 						panels.get(i).setY(j * 56 + 5);
 					}
 				}
 			}
 		}
 	}
 	
 	public void setSortingOrder(GuiLayoutPanelType... types){
 		sortingOrder = new GuiLayoutPanelType[types.length];
 		for(int i = 0;i<types.length;i++){
 			sortingOrder[i] = types[i];
 		}
 	}
 
 	private ArrayList<GuiLayoutPanel> getOrganizedPanels(int line) {
 		ArrayList<GuiLayoutPanel> panels = new ArrayList<GuiLayoutPanel>();
 		for (GuiLayoutPanelType type : sortingOrder) {
 			Iterator<GuiLayoutPanel> it = layoutPanels.iterator();
 			while (it.hasNext()) {
 				GuiLayoutPanel panel = it.next();
 				if (panel.type == type && panel.line == line && !panel.isFixed()) {
 					panels.add(panel);
 				}
 			}
 		}
 		return panels;
 	}
 
 	public void draw(Gui gui, Container container, int offsetX, int offsetY, int mouseX, int mouseY) {
 		int typeIndex = 0;
 		GuiLayoutPanelType type = null;
 		for (int i = 0; i < layoutPanels.size(); i++) {
 			GuiLayoutPanel panel = layoutPanels.get(i);
 			GuiLayoutPanelType panelType = panel.type;
 			if (type == null)
 				type = panel.type;
 
 			if (type != panelType) {
 				if (isSameTypeClass(type,panelType)) {
 
 				} else {
 					typeIndex = 0;
 					type = panelType;
 				}
 			}
 
 			panel.draw(gui, container, typeIndex, offsetX, offsetY, mouseX, mouseY);
 
 			typeIndex++;
 		}
 	}
 
 	private boolean isSameTypeClass(GuiLayoutPanelType type, GuiLayoutPanelType panelType) {
 		GuiLayoutPanelTypeClass typeClass1 = getTypeClass(type);
		GuiLayoutPanelTypeClass typeClass2 = getTypeClass(type);
 		return typeClass1 == typeClass2;
 	}
 
 	private GuiLayoutPanelTypeClass getTypeClass(GuiLayoutPanelType type) {
 		switch (type){
 		case heat:
 			return GuiLayoutPanelTypeClass.heat;
 		case lifespan:
 			return GuiLayoutPanelTypeClass.live;
 		case power:
 			return GuiLayoutPanelTypeClass.power;
 		case simpleHeat:
 			return GuiLayoutPanelTypeClass.heat;
 		case simpleTankInput:
 			return GuiLayoutPanelTypeClass.tank;
 		case simpleTankOutput:
 			return GuiLayoutPanelTypeClass.tank;
 		case slotsInput:
 			return GuiLayoutPanelTypeClass.slots;
 		case slotsInventory:
 			return GuiLayoutPanelTypeClass.slots;
 		case slotsOutput:
 			return GuiLayoutPanelTypeClass.slots;
 		case slotsSide:
 			return GuiLayoutPanelTypeClass.slots;
 		case tankInput:
 			return GuiLayoutPanelTypeClass.tank;
 		case tankOutput:
 			return GuiLayoutPanelTypeClass.tank;
 		case worker:
 			return GuiLayoutPanelTypeClass.worker;
 		default:
 			return GuiLayoutPanelTypeClass.other;
 		}
 	}
 
 	public void addSyncingHandlersToContainer(ContainerIP container, TileEntity tileEntity) {
 		int typeIndex = 0;
 		GuiLayoutPanelType type = null;
 		for (int i = 0; i < layoutPanels.size(); i++) {
 			GuiLayoutPanel panel = layoutPanels.get(i);
 			GuiLayoutPanelType panelType = panel.type;
 
 			if (type == null)
 				type = panel.type;
 
 			if (type != panelType) {
 
				if ((type.isTankInput() && panelType.isTankOutput()) || (type.isTankOutput() && panelType.isTankInput())) {
 				} else {
 					typeIndex = 0;
 					type = panelType;
 				}
 			}

 			container.addHandlerToContainer(panelType, typeIndex);
 
 			typeIndex++;
 		}
 	}
 
 	public void drawFilledPanels(Rectangle rectangle, Gui gui, int mouseX, int mouseY, Recipe recipe, Block craftingBlock) {
 		ArrayList<GuiLayoutPanel> panels = getOrganizedPanels(1);
 
 		int typeIndex = 0;
 		GuiLayoutPanelType type = null;
 		int offset = 3;
 		int x = rectangle.x + offset;
 		int y = rectangle.y + offset;
 		int ymax = 0;
 		for (int i = 0; i < panels.size(); i++) {
 
 			GuiLayoutPanel panel = panels.get(i);
 			GuiLayoutPanelType panelType = panel.type;
 
 			if (type == null)
 				type = panel.type;
 
 			if (type != panelType) {
 
 				if ((type.isTankInput() && panelType.isTankOutput()) || (type.isTankOutput() && panelType.isTankInput())) {
 				} else {
 					typeIndex = 0;
 					type = panelType;
 				}
 			}
 
 			if (panel.getHeight() > ymax)
 				ymax = panel.getHeight();
 			if (panel.getWidthReduced() + x > rectangle.x + rectangle.width) {
 				x = rectangle.x + offset;
 				y = y += ymax + offset;
 				ymax = 0;
 			}
 			panel.drawFilledPanel(gui, typeIndex, x, y, recipe, mouseX, mouseY, craftingBlock);
 			x += panel.getWidthReduced() + offset;
 
 			typeIndex++;
 		}
 	}
 }
