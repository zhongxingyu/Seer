 package gameView.ingame.menu;
 
 import gameView.GameWindow;
 import gameView.ingame.datatypes.ClickPosition;
 import gameView.ingame.datatypes.RelativeBoxPosition;
 import gameView.ingame.inventory.DrawableItemStack;
 import gameView.ingame.inventory.InventoryPage;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 
 import recources.ImageCache;
 import singleton.SingletonWorker;
 import utilities.ImageUtil;
 
 public class DrawableInventory extends UIItem {
 
 	private static final int sellx = 100;
 	private static final int selly = 150;
 	private int rows, cells, itemWidth, itemHeight, currentPage = 0;
 	private double relativeDistanceX, relativeDistanceY;
 	private RelativeBoxPosition relativeFirstBox;
 	private ArrayList<InventoryPage> pageList;
 	private ClickPosition backBtn;
 	private ClickPosition fwdBtn;
 
 	private ClickPosition[] equipmentBoxes = new ClickPosition[9];
 	private int selectedEntry = -1;
 	private int sellwidth;
 	private int sellheight;
 
 	public DrawableInventory(int startX, int startY, int endX, int endY, String src,
 			int rows, int cells, RelativeBoxPosition relativeFirstBox,
 			double relativeDistanceX, double relativeDistanceY,
 			RelativeBoxPosition relativeBackBtn,
 			RelativeBoxPosition relativeFwdBtn,
 			RelativeBoxPosition[] equipmentBoxes) {
 		super(startX, startY, endX, endY, src);
 		this.rows = rows;
 		this.cells = cells;
 		this.relativeDistanceX = relativeDistanceX;
 		this.relativeDistanceY = relativeDistanceY;
 		this.backBtn = new ClickPosition(relativeBackBtn);
 		this.fwdBtn = new ClickPosition(relativeFwdBtn);
 		this.relativeFirstBox = relativeFirstBox;
 
 		int currentItemCategory = 0;
 		for (RelativeBoxPosition relBox : equipmentBoxes) {
 			this.equipmentBoxes[currentItemCategory] = new ClickPosition(relBox);
 			currentItemCategory++;
 		}
 
 		itemWidth = (int) (GameWindow.getWindowWidth()
 				* relativeFirstBox.getRelativeEndX() - GameWindow
 				.getWindowWidth() * relativeFirstBox.getRelativeStartX());
 		itemHeight = (int) (GameWindow.getWindowHeight()
 				* relativeFirstBox.getRelativeEndY() - GameWindow
 				.getWindowHeight() * relativeFirstBox.getRelativeStartY());
 	}
 
 	@Override
 	public void process(long duration) {
 		if(SingletonWorker.gameData().activePlayer().getInventory().hasChanged()){
 			this.pageList = this.createInventoryPages();
 			if (this.currentPage >= this.pageList.size()) {
 				this.currentPage = 0;
 			} else if (this.currentPage < 0) {
 				this.currentPage = this.pageList.size() - 1;
 			}
 		}
 	}
 
 	@Override
 	public void draw(Graphics g) {
 		super.draw(g);
 		int mousex = SingletonWorker.gameData().activePlayer().getMouseX();
 		int mousey = SingletonWorker.gameData().activePlayer().getMouseY();
 
 		//		if (backBtn.isInRange(mousex, mousey)) {
 		//			currentPage--;
 		//			return;
 		//		} else if (fwdBtn.isInRange(mousex, mousey)) {
 		//			currentPage++;
 		//			return;
 		//		}
 
 		//		for (int category = 0; category < equipmentBoxes.length; category++) {
 		//			ClickPosition clickBox = equipmentBoxes[category];
 		//			if (clickBox.isInRange(mousex, mousey)) {
 		//				
 		//			}
 		//		}
 		int mouseoverMarker = -1;
 		for (int invPageEntry = 0; invPageEntry < pageList.get(currentPage).getItems().length; invPageEntry++) {
 			Object[] item = pageList.get(currentPage).getItems()[invPageEntry];
 			ClickPosition currentItemBox = (ClickPosition) (item[3]);
 			if (currentItemBox.isInRange(mousex, mousey)) {
 				mouseoverMarker = invPageEntry;
 			}
 		}
 
 
 		int entry = selectedEntry % 30;
 		int page = selectedEntry / 30;
 		if(page == currentPage){
 			this.pageList.get(this.currentPage).draw(g,entry,mouseoverMarker);
 			if(selectedEntry > -1){
 				String sellstring = "Sell this items for 1!";
 				if(SingletonWorker.gameProperties().getPlayerBlockY() > 2){
 					sellstring =    "Go to surface to sell!";
 				}
 				g.drawString(sellstring, sellx , selly);
 				sellwidth = g.getFontMetrics().charsWidth(sellstring.toCharArray(), 0, sellstring.length());
 				sellheight = g.getFontMetrics().getHeight()*4;
 			}
 		}else{
 			this.pageList.get(this.currentPage).draw(g,-1,mouseoverMarker);
 		}
 		this.drawEquipedItems(g);
 	}
 
 	private void drawEquipedItems(Graphics g) {
 
 		// TODO in draw oder GameControllerThread
 
 		int currentItemCategory = 0;
 		for (int item : SingletonWorker.gameData().activePlayer().getEquiped()) {
 			if (item != Integer.MAX_VALUE) {
 				Image itemImage = ImageCache.getIcon(item);
 
 				int width = (int) (equipmentBoxes[currentItemCategory]
 						.getEndX() - equipmentBoxes[currentItemCategory]
 								.getStartX());
 				int height = (int) (equipmentBoxes[currentItemCategory]
 						.getEndY() - equipmentBoxes[currentItemCategory]
 								.getStartY());
 
 				int startX = (int) (equipmentBoxes[currentItemCategory]
 						.getStartX());
 				int startY = (int) (equipmentBoxes[currentItemCategory]
 						.getStartY());
 
 				// SingletonWorker.logger().info("\r\n\r\nitemCategory: "
 				// + currentItemCategory);
 				// System.out
 				// .println("relativeXValue: "
 				// + (equipmentBoxes[currentItemCategory]
 				// .getRelativeEndX() - equipmentBoxes[currentItemCategory]
 				// .getRelativeStartX()));
 				// SingletonWorker.logger().info("startX: " + startX);
 				// SingletonWorker.logger().info("startY: " + startY);
 				// SingletonWorker.logger().info("item: " + item);
 
 				itemImage = ImageUtil.resizeImage((BufferedImage) itemImage,
 						width, height);
 
 				g.drawImage(itemImage, startX, startY, null);
 
 			}
 			currentItemCategory++;
 
 		}
 	}
 
 	public ArrayList<InventoryPage> createInventoryPages() {
 		int row = 0;
 		int cell = 0;
 		int index = 0;
 
 		ArrayList<InventoryPage> pageList = new ArrayList<InventoryPage>();
 
 		Object[][] invPage;
 		if (SingletonWorker.gameData().activePlayer()
 				.getInventory().getSize() >= (rows + 1) * (cells + 1)) {
 			invPage = new Object[(rows + 1) * (cells + 1)][6];
 		} else {
 			invPage = new Object[SingletonWorker.gameData().activePlayer().getInventory().getSize()][6];
 		}
 
 		for (DrawableItemStack is : SingletonWorker.gameData().activePlayer().getInventory().getInventoryList()) {
 
 			int startX = (int) (GameWindow.getWindowWidth()
 					* relativeFirstBox.getRelativeStartX()
 					+ GameWindow.getWindowWidth() * relativeDistanceX * cell + itemWidth
 					* cell);
 			int startY = (int) (GameWindow.getWindowHeight()
 					* relativeFirstBox.getRelativeStartY()
 					+ GameWindow.getWindowHeight() * relativeDistanceY * row + itemHeight
 					* row);
 
 			int endX = startX + itemWidth;
 			int endY = startY + itemHeight;
 
 			invPage[index][0] = startX;
 			invPage[index][1] = startY;
 
 			invPage[index][2] = ImageUtil.resizeImage(is.getImage(), itemWidth, itemHeight);
 
 			invPage[index][3] = new ClickPosition(startX, startY, endX, endY);
 
 			invPage[index][4] = is.getItemId();
 			invPage[index][5] = is.getAmount();
 
 			if (row == this.rows && cell == this.cells) {
 				if (invPage.length != 0){
 					pageList.add(new InventoryPage(invPage,itemWidth,itemHeight));
 				}
 
 				if (SingletonWorker.gameData().activePlayer().getInventory().getSize()
 						- ((pageList.size() * ((rows + 1) * (cells + 1)))) >= (rows + 1)
 						* (cells + 1)) {
 
 					if (SingletonWorker.gameData().activePlayer().getInventory().getSize()
 							- ((pageList.size() * ((rows + 1) * (cells + 1)))) > 0) {
 
 						// SingletonWorker.logger().info("Seite: " + currentPage);
 						// SingletonWorker.logger().info("Gesamt: "
 						// + SingletonWorker.gameData().getGameSessionData()
 						// .getActivePlayer().getInventory()
 						// .size());
 						// System.out
 						// .println("Bisherige: "
 						// + (pageList.size() * ((rows + 1) * (cells + 1))));
 						// System.out
 						// .println("Rest: "
 						// + (SingletonWorker.gameData()
 						// .getGameSessionData()
 						// .getActivePlayer()
 						// .getInventory().size() - ((pageList
 						// .size() * ((rows + 1) * (cells + 1))))));
 
 						invPage = new Object[(rows + 1) * (cells + 1)][6];
 					}
 
 				} else {
 
 					invPage = new Object[SingletonWorker.gameData().activePlayer()
 					                     .getInventory().getSize()
 					                     - ((pageList.size() * ((rows + 1) * (cells + 1))))][6];
 
 				}
 				row = 0;
 				cell = 0;
 				index = 0;
 			} else if (cell == this.cells) {
 				cell = 0;
 				row++;
 				index++;
 			} else {
 				cell++;
 				index++;
 			}
 		}
 		pageList.add(new InventoryPage(invPage,itemWidth,itemHeight));
 
 		return pageList;
 	}
 
 	@Override
 	public void keyPressed() {
 		// TODO Auto-generated method stub
 
 	}
 
 
 	@Override
 	public void mouseKlicked(MouseEvent e) {
 		if(selectedEntry > -1 && e.getX() > (sellx) && e.getY() > (selly-sellheight/2) && e.getX() < (sellx + sellwidth) && e.getY() < (selly)){
 			SingletonWorker.logger().info("Selling stuff");
 			SingletonWorker.networkHandlerThread().sellStuff(selectedEntry);
 			selectedEntry = -1;
 		}
 		if (backBtn.isInRange(e.getX(), e.getY())) {
			if(currentPage >= 1 ){
				currentPage--;
			}
 			return;
 		} else if (fwdBtn.isInRange(e.getX(), e.getY())) {
			if(currentPage < pageList.size()-1){
				currentPage++;
			}
 			return;
 		}
 
 		// SingletonWorker.logger().info("Mausklick bei: " + e.getX() + ":x y:" +
 		// e.getY());
 
 		for (int category = 0; category < equipmentBoxes.length; category++) {
 			ClickPosition clickBox = equipmentBoxes[category];
 			if (clickBox.isInRange(e.getX(), e.getY())) {
 
 				unEquipItem(category);
 
 			}
 		}
 		boolean rightclick = false;
 		if(e.getButton() == MouseEvent.BUTTON3){
 			rightclick = true;
 		}
 		for (int invPageEntry = 0; invPageEntry < pageList.get(currentPage).getItems().length; invPageEntry++) {
 			Object[] item = pageList.get(currentPage).getItems()[invPageEntry];
 			ClickPosition currentItemBox = (ClickPosition) (item[3]);
 			if (currentItemBox.isInRange(e.getX(), e.getY())) {
 				if(rightclick){
 					if((int)item[4]==11 || (int)item[4]==12){
 						int x = SingletonWorker.gameProperties().getPlayerBlockX();
 						int y = SingletonWorker.gameProperties().getPlayerBlockY();
 						SingletonWorker.gameData().getNetworkHandlerThread().placeLadder(x,y,(int)item[4]);
 					}else{
 						SingletonWorker.gameData().getNetworkHandlerThread().craftItem((int)item[4]);
 					}
 				}else{
 					if(selectedEntry < 0){
 						selectedEntry = currentPage*30 + invPageEntry;
 					}else{
 						change(selectedEntry,currentPage*30 + invPageEntry);
 						selectedEntry = -1;
 					}
 				}
 
 			}
 		}
 	}
 
 	private void unEquipItem(int category) {
 		SingletonWorker.gameData().activePlayer()
 		.getEquiped()[category] = Integer.MAX_VALUE;
 	}
 
 	private void change(int item1,int item2){
 		SingletonWorker.gameData().getNetworkHandlerThread().changeItemsInInventory(item1,item2);
 	}
 
 }
