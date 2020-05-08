 package net.minecraft.src.redstoneExtended;
 
 import net.minecraft.src.Block;
 import net.minecraft.src.NBTTagCompound;
 
 public class LaserMode implements Cloneable {
     public float width;
 
     public boolean collision;
 
     public short damage;
 
     public byte texture;
 
     public byte colorR;
     public byte colorG;
     public byte colorB;
 
     public LaserMode() {
         width = 1f;
         collision = false;
         damage = 0;
         texture = (byte)Block.blockSnow.blockIndexInTexture;
         colorR = (byte)255;
         colorG = (byte)255;
         colorB = (byte)255;
     }
 
    public LaserMode(float width, boolean collision, short damage, byte texture, byte colorR, byte colorB, byte colorG) {
         this.width = width;
         this.collision = collision;
         this.damage = damage;
         this.texture = texture;
         this.colorR = colorR;
         this.colorG = colorG;
         this.colorB = colorB;
     }
 
     public static LaserMode readFromNBT(NBTTagCompound nbtTagCompound) {
         float width = nbtTagCompound.getFloat("Width");
         boolean collision = nbtTagCompound.getBoolean("Collision");
         short damage = nbtTagCompound.getShort("Damage");
         byte texture = nbtTagCompound.getByte("Texture");
         NBTTagCompound colorTag = nbtTagCompound.getCompoundTag("Color");
         byte colorR = colorTag.getByte("R");
         byte colorG = colorTag.getByte("G");
         byte colorB = colorTag.getByte("B");
         return new LaserMode(width, collision, damage, texture, colorR, colorB, colorG);
     }
 
     public static void writeToNBT(NBTTagCompound nbtTagCompound, LaserMode laserMode) {
         nbtTagCompound.setFloat("Width", laserMode.width);
         nbtTagCompound.setBoolean("Collision", laserMode.collision);
         nbtTagCompound.setShort("Damage", laserMode.damage);
         nbtTagCompound.setByte("Texture", laserMode.texture);
         NBTTagCompound colorTag = new NBTTagCompound();
         colorTag.setByte("R", laserMode.colorR);
         colorTag.setByte("G", laserMode.colorG);
         colorTag.setByte("B", laserMode.colorB);
         nbtTagCompound.setCompoundTag("Color", colorTag);
     }
 
     public Object copy() {
         try {
             return clone();
         } catch (CloneNotSupportedException e) {
             return new LaserMode();
         }
     }
 
     @Override
     protected Object clone() throws CloneNotSupportedException {
         return super.clone();
     }
 
     public boolean equals(LaserMode laserMode) {
         return ((width == laserMode.width) &&
                 (collision == laserMode.collision) &&
                 (damage == laserMode.damage) &&
                 (texture == laserMode.texture) &&
                 (colorR == laserMode.colorR) &&
                 (colorG == laserMode.colorG) &&
                 (colorB == laserMode.colorB));
     }
 }
