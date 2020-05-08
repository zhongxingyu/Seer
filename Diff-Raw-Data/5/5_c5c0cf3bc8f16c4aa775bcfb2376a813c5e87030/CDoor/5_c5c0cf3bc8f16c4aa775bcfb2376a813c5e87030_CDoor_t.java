 package org.percepta.mgrankvi.floorplanner.gwt.client.item.door;
 
 import org.percepta.mgrankvi.floorplanner.gwt.client.VisualItem;
 import org.percepta.mgrankvi.floorplanner.gwt.client.geometry.GeometryUtil;
 import org.percepta.mgrankvi.floorplanner.gwt.client.geometry.Point;
 import org.percepta.mgrankvi.floorplanner.gwt.client.item.door.DoorState.Direction;
 
 import com.google.gwt.canvas.dom.client.Context2d;
 
 public class CDoor extends VisualItem {
 
 	private int size = 20;
 	private int orgSize = 20;
 	private Direction opening = Direction.UP_LEFT;
 
 	public CDoor(final int size, final Direction opening) {
 		this.size = size;
 		orgSize = size;
 		this.opening = opening;
 	}
 
 	public CDoor(final int size, final Point position) {
 		this.size = size;
 		orgSize = size;
 		this.position = position;
 	}
 
 	@Override
 	public void setPosition(final Point position) {
 		this.position = position;
 	}
 
 	public void setSize(final int size) {
 		this.size = size;
 		orgSize = size;
 	}
 
 	public int getSize() {
 		return size;
 	}
 
 	@Override
 	public void scale(final double scale) {
		// super.scale(scale);
		if (size == orgSize) {
			orgPosition = new Point(position.getX(), position.getY());
		}
 		size = (int) Math.ceil(size * scale);
 	}
 
 	@Override
 	public void reset() {
 		size = orgSize;
 		super.reset();
 	}
 
 	@Override
 	public void paint(final Context2d context) {
 		context.beginPath();
 
 		switch (opening) {
 		case UP_LEFT:
 			context.arc(position.getX(), position.getY(), size, Math.PI, Math.PI * 1.5, false);
 			context.moveTo(position.getX(), position.getY() - size);
 			context.lineTo(position.getX(), position.getY());
 			context.lineTo(position.getX() - size, position.getY());
 			context.moveTo(position.getX(), position.getY());
 			break;
 		case UP_RIGHT:
 			context.arc(position.getX(), position.getY(), size, Math.PI * 1.5, 0, false);
 			context.moveTo(position.getX(), position.getY() - size);
 			context.lineTo(position.getX(), position.getY());
 			context.lineTo(position.getX() + size, position.getY());
 			context.moveTo(position.getX(), position.getY());
 			break;
 		case DOWN_LEFT:
 			context.arc(position.getX(), position.getY(), size, Math.PI * 0.5, Math.PI, false);
 			context.moveTo(position.getX(), position.getY() + size);
 			context.lineTo(position.getX(), position.getY());
 			context.lineTo(position.getX() - size, position.getY());
 			context.moveTo(position.getX(), position.getY());
 			break;
 		case DOWN_RIGHT:
 			context.arc(position.getX(), position.getY(), size, 0, Math.PI * 0.5, false);
 			context.moveTo(position.getX(), position.getY() + size);
 			context.lineTo(position.getX(), position.getY());
 			context.lineTo(position.getX() + size, position.getY());
 			context.moveTo(position.getX(), position.getY());
 			break;
 		}
 
 		context.closePath();
 		context.stroke();
 	}
 
 	@Override
 	public void paint(final Context2d context, final Point offset) {
 		final Point drawPosition = GeometryUtil.combine(position, offset);
 
 		context.beginPath();
 
 		switch (opening) {
 		case UP_LEFT:
 			context.arc(drawPosition.getX(), drawPosition.getY(), size, Math.PI, Math.PI * 1.5, false);
 			context.moveTo(drawPosition.getX(), drawPosition.getY() - size);
 			context.lineTo(drawPosition.getX(), drawPosition.getY());
 			context.lineTo(drawPosition.getX() - size, drawPosition.getY());
 			context.moveTo(drawPosition.getX(), drawPosition.getY());
 			break;
 		case UP_RIGHT:
 			context.arc(drawPosition.getX(), drawPosition.getY(), size, Math.PI * 1.5, 0, false);
 			context.moveTo(drawPosition.getX(), drawPosition.getY() - size);
 			context.lineTo(drawPosition.getX(), drawPosition.getY());
 			context.lineTo(drawPosition.getX() + size, drawPosition.getY());
 			context.moveTo(drawPosition.getX(), drawPosition.getY());
 			break;
 		case DOWN_LEFT:
 			context.arc(drawPosition.getX(), drawPosition.getY(), size, Math.PI * 0.5, Math.PI, false);
 			context.moveTo(drawPosition.getX(), drawPosition.getY() + size);
 			context.lineTo(drawPosition.getX(), drawPosition.getY());
 			context.lineTo(drawPosition.getX() - size, drawPosition.getY());
 			context.moveTo(drawPosition.getX(), drawPosition.getY());
 			break;
 		case DOWN_RIGHT:
 			context.arc(drawPosition.getX(), drawPosition.getY(), size, 0, Math.PI * 0.5, false);
 			context.moveTo(drawPosition.getX(), drawPosition.getY() + size);
 			context.lineTo(drawPosition.getX(), drawPosition.getY());
 			context.lineTo(drawPosition.getX() + size, drawPosition.getY());
 			context.moveTo(drawPosition.getX(), drawPosition.getY());
 			break;
 		}
 
 		context.closePath();
 		context.stroke();
 	}
 
 	@Override
 	public boolean pointInObject(final double x, final double y) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
