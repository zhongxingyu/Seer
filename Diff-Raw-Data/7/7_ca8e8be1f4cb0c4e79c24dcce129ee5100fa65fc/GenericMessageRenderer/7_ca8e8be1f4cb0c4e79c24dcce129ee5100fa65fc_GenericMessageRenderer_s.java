 package teachnet.view.config;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.lang.reflect.Field;
 
 import teachnet.view.MessageStatus;
 
 public class GenericMessageRenderer extends MessageRenderer {
 	private static final int RADIUS = 5;
 
 	public void paint(Graphics g, MessageStatus status)
 			throws RendererException {
 		Point position = getPosition(status);
 
 		Color color = null;
 		String caption = null;
 		Shape shape = null;
 
 		if (status.payload instanceof Boolean) {
			final boolean b = ((Boolean) status.payload).booleanValue();
			color = b ? Color.GREEN : Color.RED;
 		}

		if (status.payload instanceof Color) {
 			color = (Color) status.payload;
 		} else if (status.payload instanceof String) {
 			caption = (String) status.payload;
 		} else {
 			caption = status.payload.toString();
 			try {
 				color = (Color) getMember(status.payload, "color");
 			} catch (NoSuchFieldException ex) {
 			}
 
 			try {
 				shape = (Shape) getMember(status.payload, "shape");
 			} catch (NoSuchFieldException ex) {
 			}
 		}
 
 		if (color == null) {
 			color = Color.YELLOW;
 		}
 		if (shape == null) {
 			shape = Shape.CIRCLE;
 		}
 
 		switch (shape) {
 		case RECTANGLE:
 			g.setColor(color);
 			g.fillRect(position.x - RADIUS, position.y - RADIUS, RADIUS * 2, RADIUS * 2);
 
 			g.setColor(Color.black);
 			g.drawRect(position.x - RADIUS, position.y - RADIUS, RADIUS * 2, RADIUS * 2);
 			break;
 
 		case RHOMBUS:
 			g.setColor(color);
 			final int[] xPoints = { position.x, position.x + RADIUS, position.x, position.x - RADIUS };
 			final int[] yPoints = { position.y + RADIUS, position.y, position.y - RADIUS, position.y };
 			g.fillPolygon(xPoints, yPoints, 4);
 
 			g.setColor(Color.black);
 			g.drawPolygon(xPoints, yPoints, 4);
 			break;
 
 		default:
 			g.setColor(color);
 			g.fillOval(position.x - RADIUS, position.y - RADIUS, RADIUS * 2, RADIUS * 2);
 
 			g.setColor(Color.black);
 			g.drawOval(position.x - RADIUS, position.y - RADIUS, RADIUS * 2, RADIUS * 2);
 		}
 
 		if (caption != null)
 			g.drawString(caption, position.x + RADIUS + 2, position.y + RADIUS);
 	}
 
 	// These should be changed inside the AlgorithmInspector class.
 	public static Object getMember(Object algo, String name)
 			throws NoSuchFieldException {
 		return getMember(algo.getClass(), algo, name);
 	}
 
 	public static Object getMember(Class<?> clazz, Object algo, String name)
 			throws NoSuchFieldException {
 		try {
 			Field field = clazz.getDeclaredField(name);
 			field.setAccessible(true);
 
 			return field.get(algo);
 		} catch (IllegalAccessException ex) {
 			return null;
 		} catch (SecurityException ex) {
 		}
 		return null;
 	}
 }
