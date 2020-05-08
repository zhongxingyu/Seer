 import javax.swing.JButton;
 
 public class Pawn {
 	private final JButton imgSrc;
 	private Field location;
 	private HomeField homeLoc;
 	private int pos = 0;
 
 	/**
 	 * 
 	 * @param source
 	 * @param pos
 	 */
 	public Pawn(final JButton source, final HomeField loc) {
 		this.imgSrc = source;
 		this.homeLoc = loc;
 		moveToField(loc);
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	protected JButton getImgSrc() {
 		return imgSrc;
 	}
 
 	public final Field getField() {
 		return location;
 	}
 
 	public final boolean isAtHome() {
 		return (pos == 0 ? true : false);
 	}
 
 	public final boolean isAtBasic() {
 		return (pos == 1 ? true : false);
 	}
 
 	public final boolean isAtGoal() {
 		return (pos == 2 ? true : false);
 	}
 
 	public final void moveToHome() {
 		moveToField(homeLoc);
 	}
 
 	/**
 	 * 
 	 * @param field
 	 */
 	public final void moveToField(final Field field) {
 		if (field != homeLoc) {
 			location.setPawn(null);
 			if (field.hasPawn()) {
 				field.getPawn().moveToHome();
 			}
 		}
 		this.location = field;
 		location.setPawn(this);
 		if (field.getClass() == BasicField.class) {
 			this.pos = 1;
 		} else if (field.getClass() == GoalField.class) {
 			this.pos = 2;
 		} else if (field.getClass() == HomeField.class) {
 			this.pos = 0;
 		} else {
 			System.err.println("Pawn unable to identify current field type");
 		}
 	}
}
