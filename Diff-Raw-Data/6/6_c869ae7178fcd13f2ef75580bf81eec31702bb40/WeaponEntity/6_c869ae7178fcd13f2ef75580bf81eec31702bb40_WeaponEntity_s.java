 import java.awt.Image;
 
 public class WeaponEntity extends Entity {
 	private int weaponType;
 	private Image itemIcon;
 	private Image itemRenderPicture;
 	
 	private boolean gotOwner;
 	
 	public WeaponEntity(double xPosition, double yPosition, double zPosition, Equipment weapon) {
 		super(xPosition, yPosition, zPosition);
 		
 		this.weaponType = weapon.getStat_Type();
 		this.itemIcon = weapon.getItemIcon();
 		this.itemRenderPicture = weapon.getItemRenderPicture();
 		// TODO Auto-generated constructor stub
 	}
 	
 	private void createWeaponHitbox() { };
 
 	@Override
	protected void updateHitboxes() {
		// TODO Auto-generated method stub
		
	}

	@Override
 	protected void updateSpeed() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean checkForCollision(Entity e) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean checkForCollision(Hitbox h) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
