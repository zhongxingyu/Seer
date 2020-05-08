 package org.percepta.mgrankvi.floorplanner.gwt.client.floorgrid;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.percepta.mgrankvi.floorplanner.gwt.client.CommandObject;
 import org.percepta.mgrankvi.floorplanner.gwt.client.InfoButton;
 import org.percepta.mgrankvi.floorplanner.gwt.client.geometry.Point;
 import org.percepta.mgrankvi.floorplanner.gwt.client.paint.GridUtils;
 import org.percepta.mgrankvi.floorplanner.gwt.client.room.CRoom;
 import org.percepta.mgrankvi.floorplanner.gwt.client.room.RoomState;
 
 import com.google.gwt.canvas.client.Canvas;
 import com.google.gwt.canvas.dom.client.Context2d;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.NativeEvent;
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.dom.client.Style.Position;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.ContextMenuEvent;
 import com.google.gwt.event.dom.client.ContextMenuHandler;
 import com.google.gwt.event.dom.client.MouseDownEvent;
 import com.google.gwt.event.dom.client.MouseDownHandler;
 import com.google.gwt.event.dom.client.MouseMoveEvent;
 import com.google.gwt.event.dom.client.MouseMoveHandler;
 import com.google.gwt.event.dom.client.MouseUpEvent;
 import com.google.gwt.event.dom.client.MouseUpHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.MenuBar;
 import com.google.gwt.user.client.ui.MenuItem;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 public class CFloorGrid extends Widget implements ClickHandler, MouseDownHandler, MouseUpHandler, MouseMoveHandler, ContextMenuHandler, ChangeHandler {
 
 	private static final String CLASSNAME = "c-floorgrid";
 
 	private final Canvas canvas;
 	private final List<CRoom> rooms = new LinkedList<CRoom>();
 	private final Map<String, CRoom> roomMap = new HashMap<String, CRoom>();
 
 	private final TextBox typeAndEdit = new TextBox();
 
 	private PopupPanel contextMenu;
 
 	private InfoButton hoverElement = null;
 
 	private final int gridSize = 50;
 	private int offsetX = 0;
 	private int offsetY = 0;
 	private final Point origo = new Point(0, 0);
 
 	private boolean mouseDown = false;
 	private boolean mouseMoved = true;
 	private int downX = 0;
 	private int downY = 0;
 	CRoom selected = null;
 	Point targetPoint = null;
 
 	public CFloorGrid() {
 		setElement(Document.get().createDivElement());
 		setStyleName(CLASSNAME);
 
 		addDomHandler(this, MouseDownEvent.getType());
 		addDomHandler(this, MouseMoveEvent.getType());
 		addDomHandler(this, MouseUpEvent.getType());
 		addDomHandler(this, ClickEvent.getType());
 		addDomHandler(this, ContextMenuEvent.getType());
 		addDomHandler(this, ChangeEvent.getType());
 
 		canvas = Canvas.createIfSupported();
 		if (canvas != null) {
 			getElement().appendChild(canvas.getElement());
 			clearCanvas();
 			paint();
 		} else {
 			getElement().setInnerHTML("Canvas not supported");
 		}
 
 		getElement().appendChild(typeAndEdit.getElement());
 
 		final Style editStyle = typeAndEdit.getElement().getStyle();
 		typeAndEdit.addChangeHandler(this);
 		editStyle.setPosition(Position.RELATIVE);
 		editStyle.setLeft(0.0, Style.Unit.PX);
 		typeAndEdit.setWidth(Window.getClientWidth() + "px");
 
 	}
 
 	private void clearCanvas() {
 		canvas.setCoordinateSpaceWidth(Window.getClientWidth());
 		canvas.setCoordinateSpaceHeight(Window.getClientHeight() - typeAndEdit.getOffsetHeight() - 4);
 	}
 
 	public void addRoom(final RoomState roomState) {
 		if (roomState.id != null && !roomMap.containsKey(roomState.id)) {
 			final CRoom room = new CRoom(roomState.id, roomState.getPoints(), roomState.getPosition());
 			rooms.add(room);
 			roomMap.put(roomState.id, room);
 			room.paint(canvas.getContext2d());
 		}
 	}
 
 	public void paintRooms() {
 		for (final CRoom room : rooms) {
 			room.paint(canvas.getContext2d());
 		}
 	}
 
 	private void paint() {
 		final Context2d context = canvas.getContext2d();
 
 		GridUtils.paintGrid(context, new Point(offsetX, offsetY), gridSize, origo);
 		paintRooms();
 
 		if (hoverElement != null) {
 			hoverElement.paint(canvas.getContext2d());
 		}
 	}
 
 	private void repaint() {
 		clearCanvas();
 		paint();
 	}
 
 	@Override
 	public void onClick(final ClickEvent event) {
 		if (typeAndEdit.getElement().equals(event.getNativeEvent().getEventTarget())) {
 			return;
 		}
 		if (event.getNativeButton() == NativeEvent.BUTTON_RIGHT) {
 		} else {
 			if (mouseMoved) {
 				mouseMoved = false;
 				return;
 			}
 			if (hoverElement != null && hoverElement.pointInObject(event.getClientX(), event.getClientY())) {
 				fireEvent(new MenuEvent(MenuEvent.MenuEventType.OPEN_ROOM_INFO, hoverElement.getRoom().getId()));
 			}
 			boolean selected = false;
 			for (final CRoom room : rooms) {
 				if (room.pointInObject(event.getClientX(), event.getClientY()) && !selected) {
 					room.setSelection(true);
 					selected = true;
 				} else {
 					room.setSelection(false);
 				}
 			}
 			repaint();
 		}
 	}
 
 	@Override
 	public void onMouseDown(final MouseDownEvent event) {
 		downX = event.getClientX();
 		downY = event.getClientY();
 		mouseDown = true;
 		if (contextMenu != null) {
 			contextMenu.hide();
 			contextMenu = null;
 		}
 		for (final CRoom room : rooms) {
 			if (room.isSelected()) {
 				targetPoint = room.selectedPoint(downX, downY);
 				if (targetPoint != null || room.pointInObject(downX, downY)) {
 					selected = room;
 					break;
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onMouseUp(final MouseUpEvent event) {
 		mouseDown = false;
 		selected = null;
 	}
 
 	@Override
 	public void onMouseMove(final MouseMoveEvent event) {
 		if (mouseDown) {
 			mouseMoved = true;
 
 			if (selected == null) {
 				pan(event);
 			} else {
 				moveRoom(selected, event);
 			}
 
 			downX = event.getClientX();
 			downY = event.getClientY();
 
 			repaint();
 		} else if (hoverElement == null) {
 			for (final CRoom room : rooms) {
 				if (room.pointInObject(event.getClientX(), event.getClientY())) {
 					hoverElement = new InfoButton(room);
 					break;
 				}
 			}
 		} else {
 			if (!hoverElement.getRoom().pointInObject(event.getClientX(), event.getClientY())) {
 				hoverElement = null;
 			}
 		}
 		repaint();
 	}
 
 	private void pan(final MouseMoveEvent event) {
 		offsetY += event.getClientY() - downY;
 		offsetX += event.getClientX() - downX;
 
 		offsetY = offsetY % 50;
 		offsetX = offsetX % 50;
 
 		origo.move(event.getClientX() - downX, event.getClientY() - downY);
 		for (final CRoom room : rooms) {
 			room.movePosition(event.getClientX() - downX, event.getClientY() - downY);
 		}
 	}
 
 	private void moveRoom(final CRoom room, final MouseMoveEvent event) {
 		if (targetPoint == null) {
 			if (event.isAltKeyDown()) {
 			}
 			room.movePosition(event.getClientX() - downX, event.getClientY() - downY);
 		} else {
 			targetPoint.move(event.getClientX() - downX, event.getClientY() - downY);
 		}
 	}
 
 	@Override
 	public void onChange(final ChangeEvent event) {
 		final String value = typeAndEdit.getValue();
 		if (value != null && !value.isEmpty()) {
 			for (final CRoom room : rooms) {
 				if (room.isSelected()) {
 					final CommandObject cmd = new CommandObject(value);
 					switch (cmd.getCommand()) {
 					case MOVE_TO:
						room.setPosition(cmd.getPosition());
 						typeAndEdit.setValue("");
 						break;
 					case MOVE_BY:
 						room.movePosition(cmd.getX(), cmd.getY());
 						typeAndEdit.setValue("");
 						break;
 					case INVALID_STRING:
 						Window.alert("Command String was invalid");
 						break;
 					case PARSE_FAILED:
 						Window.alert("Parsing coordinates failed");
 						break;
 					}
 					repaint();
 					break;
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onContextMenu(final ContextMenuEvent event) {
 		event.preventDefault();
 		mouseDown = false;
 		selected = null;
 
 		final int x = event.getNativeEvent().getClientX();
 		final int y = event.getNativeEvent().getClientY();
 
 		final MenuBar rootMenu = new MenuBar(true);
 		final MenuBar roomMenu = new MenuBar(true);
 
 		final MenuItem triangle = new MenuItem("3 corners", new AddCommand(3, x, y));
 		final MenuItem square = new MenuItem("4 corners", new AddCommand(4, x, y));
 		final MenuItem fivePoints = new MenuItem("5 corners", new AddCommand(5, x, y));
 		final MenuItem LShape = new MenuItem("L shaped 6 corners", new AddCommand(6, x, y));
 
 		roomMenu.addItem(triangle);
 		roomMenu.addItem(square);
 		roomMenu.addItem(fivePoints);
 		roomMenu.addItem(LShape);
 
 		final MenuItem newRoom = new MenuItem("Add new room", roomMenu);
 		rootMenu.addItem(newRoom);
 
 		rootMenu.setVisible(true);
 		contextMenu = new PopupPanel();
 		contextMenu.add(rootMenu);
 
 		Style style = rootMenu.getElement().getStyle();
 		style.setProperty("backgroundColor", "gray");
 		style.setProperty("borderColor", "gray");
 		style.setProperty("borderWidth", "1px 3px 3px 1px");
 		style.setProperty("borderStyle", "solid");
 
 		style = roomMenu.getElement().getStyle();
 		style.setProperty("backgroundColor", "gray");
 		style.setProperty("borderColor", "gray");
 		style.setProperty("borderWidth", "1px 3px 3px 1px");
 		style.setProperty("borderStyle", "solid");
 
 		chekcForRemoveAndAddItem(rootMenu);
 
 		contextMenu.setPopupPosition(x, y);
 		contextMenu.show();
 	}
 
 	private void chekcForRemoveAndAddItem(final MenuBar rootMenu) {
 		CRoom selectedRoom = null;
 		for (final CRoom room : rooms) {
 			if (room.isSelected()) {
 				selectedRoom = room;
 				break;
 			}
 		}
 		if (selectedRoom != null) {
 			final MenuItem remove = new MenuItem("Remove selected", new RemoveCommand(selectedRoom));
 			rootMenu.addItem(remove);
 		}
 	}
 
 	public HandlerRegistration addMenuEventHandler(final MenuEventHandler menuEventHandler) {
 		return addHandler(menuEventHandler, MenuEvent.getType());
 	}
 
 	private class AddCommand implements Command {
 
 		private final int corners, x, y;
 
 		public AddCommand(final int corners, final int x, final int y) {
 			this.corners = corners;
 			this.x = x;
 			this.y = y;
 		}
 
 		@Override
 		public void execute() {
 			fireEvent(new MenuEvent(MenuEvent.MenuEventType.ADD_ROOM, corners, x, y));
 			contextMenu.hide();
 			contextMenu = null;
 		}
 	}
 
 	private class RemoveCommand implements Command {
 		private final CRoom selectedRoom;
 
 		public RemoveCommand(final CRoom selectedRoom) {
 			this.selectedRoom = selectedRoom;
 		}
 
 		@Override
 		public void execute() {
 			rooms.remove(selectedRoom);
 			fireEvent(new MenuEvent(MenuEvent.MenuEventType.REMOVE_ROOM, selectedRoom.getId()));
 			contextMenu.hide();
 			contextMenu = null;
 			repaint();
 		}
 	}
 }
