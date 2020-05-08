 package base.models;
 
 public class BoardPiece<E extends Entity> {
 
 	private final Position position;
 	private E entity;
 
 	public BoardPiece(Position position) {
 		this.position = position;
 	}
 
 	public E getEntity() {
 		return entity;
 	}
 
 	public void setEntity(E entity) {
 		this.entity = entity;
 	}
 
 	public Position getPosition() {
 		return position;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public BoardPiece<E> clonePiece(){
 		BoardPiece<E> clone = new BoardPiece<E>(this.position);
		E eClone = (E) this.entity.clone();
		clone.setEntity(eClone);
 		return clone;
 	}
 	
 }
