 package com.isocraft.entity;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.world.World;
 import net.minecraftforge.common.util.ForgeDirection;
 
 public class EntityEridiumFluxShell extends Entity {
 
 	public int angle = 0;
 	public ForgeDirection dir;
 	
 	public double len = 0;
 	public double newLen = 0;
 	public double rate = 0.2;	
 	
 	public int x;
 	public int y;
 	public int z;
 
 	public EntityEridiumFluxShell(World world) {
 		super(world);
 	}
 
 	public EntityEridiumFluxShell(World par1World, int pX, int pY, int pZ, ForgeDirection direction) {
 		super(par1World);
 		
 		if (direction == ForgeDirection.NORTH || direction == ForgeDirection.WEST){
			this.setSize( -30, 1);
 			this.setPositionAndRotation((double)pX + 0.5, (double)pY + 0.5, (double)pZ + 0.5, 0, 0);
 		}
 		else if (direction == ForgeDirection.SOUTH || direction == ForgeDirection.EAST){
			this.setSize( 30, 1);
 			this.setPositionAndRotation((double)pX + 0.5, (double)pY + 0.5, (double)pZ + 0.5, 0, 0);
 		}
 		else if (direction == ForgeDirection.UP){
			this.setSize(1, 30);
			this.setPositionAndRotation((double)pX + 0.5, (double)pY + 0.05, (double)pZ + 0.5, 0, 0);
 		}
 		else if (direction == ForgeDirection.DOWN){
			this.setSize(1, -30);
			this.setPositionAndRotation((double)pX + 0.5, (double)pY + 0.05, (double)pZ + 0.5, 0, 0);
 		} 
 		
 		this.x = pX;
 		this.y = pY;
 		this.z = pZ;
 		
 		this.dir = direction;
 		this.preventEntitySpawning = false;
 		this.noClip = true;
 		this.isImmuneToFire = true;
 	}
 
 	@Override
 	public void onUpdate() {
 		super.onUpdate();
 		
 		if (this.worldObj.isAirBlock(this.x, this.y, this.z)){
 			this.worldObj.removeEntity(this);
 			this.isDead = true;
 		}
 		
 		boolean change = false;
 		
 		if (this.len != this.newLen){
 			change = true;
 		}
 		
 		if (change){				
 			if (this.len < this.newLen){
 				if ((this.newLen - this.len) > this.rate){
 					this.len = this.len + this.rate;
 				}
 				else {
 					this.len = this.len + (this.newLen - this.len);
 				}
 			}
 			else if (this.len > this.newLen){
 				if ((this.len - this.newLen) > this.rate){
 					this.len = this.len - this.rate;
 				}
 				else {
 					this.len = this.len - (this.len - this.newLen);
 				}
 			}
 		}
 		
 		this.angle = this.angle + 2;
 
 		if (this.angle >= 360) {
 			this.angle = 0;
 		}
 	}
 
 	public void newLength(int length){
 		if (dir == ForgeDirection.UP || dir == ForgeDirection.DOWN){
			this.newLen = length;
 		}
 		else {
 			this.newLen = length;			
 		}	
 	}
 	
 	public double getLength(){
 		return this.len;
 	}
 
 	@Override
 	protected void entityInit() {
 	}
 	
 	@Override
 	public int getBrightnessForRender(float par1) {
 		return 210;
 	}
 
 	@Override
     protected void readEntityFromNBT(NBTTagCompound var1) {   
     }
 
 	@Override
     protected void writeEntityToNBT(NBTTagCompound var1) {  
     }
 }
