 package ruby.bamboo.entity;
 
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.Entity;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 
 public class EntitySakuraPetal extends Entity {
     public float rx, ry, rz;
     boolean xflg = true;
     boolean yflg = true;
     boolean zflg = true;
     public boolean stopFall = false;
     float rad = 0.001F;
     protected int particleAge = 0;
     protected int particleMaxAge = 0;
 
     protected float particleRed;
     protected float particleGreen;
     protected float particleBlue;
    private String texPath = "textures/entitys/leaf.png";
 
     public String getTexPath() {
         return texPath;
     }
 
     public EntitySakuraPetal setCustomPetal(String path) {
        texPath = "textures/entitys/" + path + ".png";
         return this;
     }
 
     public float getRx() {
         if (xflg) {
             rx += rad;
 
             if (rx > 1) {
                 xflg = !xflg;
             }
         } else {
             rx -= rad;
 
             if (rx < -1) {
                 xflg = !xflg;
             }
         }
 
         return rx;
     }
 
     public float getRy() {
         if (yflg) {
             ry += rad;
 
             if (ry > 1) {
                 yflg = !yflg;
             }
         } else {
             ry -= rad;
 
             if (ry < -1) {
                 yflg = !yflg;
             }
         }
 
         return ry;
     }
 
     public float getRz() {
         if (zflg) {
             rz += rad;
 
             if (rz > 1) {
                 zflg = !zflg;
             }
         } else {
             rz -= rad;
 
             if (rz < -1) {
                 zflg = !zflg;
             }
         }
 
         return rz;
     }
 
     public EntitySakuraPetal(World world, double d, double d1, double d2, double d3, double d4, double d5, int color) {
         super(world);
         this.setSize(0.2F, 0.2F);
         this.yOffset = this.height / 2.0F;
         this.setPosition(d, d1, d2);
         this.particleRed = this.particleGreen = this.particleBlue = 1.0F;
         motionX = d3;
         motionY = d4;
         motionZ = d5;
         float var14 = (float) (Math.random() + Math.random() + 1.0D) * 0.15F;
         float var15 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
         this.motionX = this.motionX / var15 * var14;
         this.motionY = this.motionY / var15;
         this.motionZ = this.motionZ / var15 * var14;
 
         if (d3 == 0 && d4 == 0 && d5 == 0) {
             motionX = (rand.nextFloat() - 0.5) * 0.1;
             motionY = -0.01D;
             motionZ = (rand.nextFloat() - 0.5) * 0.1;
         }
 
         particleMaxAge = rand.nextInt(60) + 60;
         rx = world.rand.nextFloat();
         ry = world.rand.nextFloat();
         rz = world.rand.nextFloat();
         float r = (color >> 16) / 255F;
         float g = ((color >> 8) & 0xff) / 255F;
         float b = (color & 0xff) / 255F;
         setRBGColorF(r, g, b);
     }
 
     public void setRBGColorF(float par1, float par2, float par3) {
         this.particleRed = par1;
         this.particleGreen = par2;
         this.particleBlue = par3;
     }
 
     public float getRedColorF() {
         return this.particleRed;
     }
 
     public float getGreenColorF() {
         return this.particleGreen;
     }
 
     public float getBlueColorF() {
         return this.particleBlue;
     }
 
     public void setStopFall() {
         stopFall = true;
         particleAge = 0;
     }
 
     public boolean isStopFall() {
         return stopFall;
     }
 
     @Override
     public void onUpdate() {
         prevPosX = posX;
         prevPosY = posY;
         prevPosZ = posZ;
 
         if (particleAge++ >= particleMaxAge) {
             setDead();
         }
 
         // particleTextureIndex = Block.blockLapis.blockIndexInTexture ;
         if (!stopFall) {
             motionY -= 0.004D;
         } else {
             if (motionY != 0) {
                 motionY /= 1.2;
             }
         }
 
         moveEntity(motionX, motionY, motionZ);
         motionX *= 0.9D;
         motionY *= 0.9D;
         motionZ *= 0.9D;
 
         if (Material.water == worldObj.getBlockMaterial((int) (posX + 0.5), (int) posY, (int) (posZ + 0.5))) {
             if (!isStopFall()) {
                 setStopFall();
             }
         }
 
         /*
          * if (onGround) { setDead(); motionX *= 0.69999998807907104D; motionZ
          * *= 0.69999998807907104D; }
          */
     }
 
     @Override
     protected void entityInit() {
     }
 
     @Override
     protected void readEntityFromNBT(NBTTagCompound var1) {
     }
 
     @Override
     protected void writeEntityToNBT(NBTTagCompound var1) {
     }
 }
