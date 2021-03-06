 package org.yogpstop.qp;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import net.minecraft.enchantment.Enchantment;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.world.World;
 
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.fluids.Fluid;
 import net.minecraftforge.fluids.FluidContainerRegistry;
 import net.minecraftforge.fluids.FluidStack;
 import net.minecraftforge.fluids.FluidTankInfo;
 import net.minecraftforge.fluids.IFluidHandler;
 
 import buildcraft.api.power.IPowerReceptor;
 import buildcraft.api.power.PowerHandler;
 import buildcraft.api.power.PowerHandler.PowerReceiver;
 import buildcraft.api.power.PowerHandler.Type;
 import buildcraft.api.recipes.RefineryRecipes;
 import buildcraft.api.recipes.RefineryRecipes.Recipe;
 
 import com.google.common.io.ByteArrayDataInput;
 
 import cpw.mods.fml.common.network.PacketDispatcher;
 
 public class TileRefinery extends APacketTile implements IFluidHandler, IPowerReceptor {
 	public FluidStack src1, src2, res;
 	private PowerHandler pp = new PowerHandler(this, Type.MACHINE);
 	private int ticks;
 
 	public float animationSpeed = 1;
 	private int animationStage = 0;
 
 	protected byte unbreaking;
 	protected byte fortune;
 	protected boolean silktouch;
 	protected byte efficiency;
 
 	public int buf;
 
 	void G_init(NBTTagList nbttl) {
 		if (nbttl != null) for (int i = 0; i < nbttl.tagCount(); i++) {
 			short id = ((NBTTagCompound) nbttl.tagAt(i)).getShort("id");
 			short lvl = ((NBTTagCompound) nbttl.tagAt(i)).getShort("lvl");
 			if (id == 32) this.efficiency = (byte) lvl;
 			if (id == 33) this.silktouch = true;
 			if (id == 34) this.unbreaking = (byte) lvl;
 			if (id == 35) this.fortune = (byte) lvl;
 		}
 		G_reinit();
 	}
 
 	protected void G_reinit() {
 		PowerManager.configureR(this.pp, this.efficiency, this.unbreaking);
 		this.buf = (int) (FluidContainerRegistry.BUCKET_VOLUME * 4 * Math.pow(1.3, this.fortune));
 	}
 
 	public Collection<String> C_getEnchantments() {
 		ArrayList<String> als = new ArrayList<String>();
 		if (this.efficiency > 0) als.add(Enchantment.enchantmentsList[32].getTranslatedName(this.efficiency));
 		if (this.silktouch) als.add(Enchantment.enchantmentsList[33].getTranslatedName(1));
 		if (this.unbreaking > 0) als.add(Enchantment.enchantmentsList[34].getTranslatedName(this.unbreaking));
 		if (this.fortune > 0) als.add(Enchantment.enchantmentsList[35].getTranslatedName(this.fortune));
 		return als;
 	}
 
 	void S_setEnchantment(ItemStack is) {
 		if (this.efficiency > 0) is.addEnchantment(Enchantment.enchantmentsList[32], this.efficiency);
 		if (this.silktouch) is.addEnchantment(Enchantment.enchantmentsList[33], 1);
 		if (this.unbreaking > 0) is.addEnchantment(Enchantment.enchantmentsList[34], this.unbreaking);
 		if (this.fortune > 0) is.addEnchantment(Enchantment.enchantmentsList[35], this.fortune);
 	}
 
 	@Override
 	public void readFromNBT(NBTTagCompound nbttc) {
 		super.readFromNBT(nbttc);
 		this.silktouch = nbttc.getBoolean("silktouch");
 		this.fortune = nbttc.getByte("fortune");
 		this.efficiency = nbttc.getByte("efficiency");
 		this.unbreaking = nbttc.getByte("unbreaking");
 		this.pp.readFromNBT(nbttc);
 		this.src1 = FluidStack.loadFluidStackFromNBT(nbttc.getCompoundTag("src1"));
 		this.src2 = FluidStack.loadFluidStackFromNBT(nbttc.getCompoundTag("src2"));
 		this.res = FluidStack.loadFluidStackFromNBT(nbttc.getCompoundTag("res"));
 		this.animationSpeed = nbttc.getFloat("animationSpeed");
 		this.animationStage = nbttc.getInteger("animationStage");
 		this.buf = (int) (FluidContainerRegistry.BUCKET_VOLUME * 4 * Math.pow(1.3, this.fortune));
 	}
 
 	@Override
 	public void writeToNBT(NBTTagCompound nbttc) {
 		super.writeToNBT(nbttc);
 		nbttc.setBoolean("silktouch", this.silktouch);
 		nbttc.setByte("fortune", this.fortune);
 		nbttc.setByte("efficiency", this.efficiency);
 		nbttc.setByte("unbreaking", this.unbreaking);
 		this.pp.writeToNBT(nbttc);
 		if (this.src1 != null) nbttc.setCompoundTag("src1", this.src1.writeToNBT(new NBTTagCompound()));
 		if (this.src2 != null) nbttc.setCompoundTag("src2", this.src2.writeToNBT(new NBTTagCompound()));
 		if (this.res != null) nbttc.setCompoundTag("res", this.res.writeToNBT(new NBTTagCompound()));
 		nbttc.setFloat("animationSpeed", this.animationSpeed);
 		nbttc.setInteger("animationStage", this.animationStage);
 	}
 
 	@Override
 	public void updateEntity() {
 		if (this.worldObj.isRemote) {
 			simpleAnimationIterate();
 			return;
 		}
 		if (this.worldObj.getWorldTime() % 20 == 7) {
 			PacketDispatcher.sendPacketToAllPlayers(PacketHandler.getPacketFromNBT(this));
 		}
 		this.ticks++;
 		for (int i = this.efficiency + 1; i > 0; i--) {
 			Recipe r = RefineryRecipes.findRefineryRecipe(this.src1, this.src2);
 			if (r == null) {
 				decreaseAnimation();
 				this.ticks = 0;
 				return;
 			}
 			if (this.res != null && r.result.amount > (this.buf - this.res.amount)) {
 				decreaseAnimation();
 				return;
 			}
 			if (r.delay > this.ticks) return;
 			if (i == 1) this.ticks = 0;
 			if (!PowerManager.useEnergyR(this.pp, r.energy, this.unbreaking)) {
 				decreaseAnimation();
 				return;
 			}
 			increaseAnimation();
 			if (r.ingredient1.isFluidEqual(this.src1)) this.src1.amount -= r.ingredient1.amount;
 			else this.src2.amount -= r.ingredient1.amount;
 			if (r.ingredient2 != null) {
 				if (r.ingredient2.isFluidEqual(this.src2)) this.src2.amount -= r.ingredient2.amount;
 				else this.src1.amount -= r.ingredient2.amount;
 			}
 			if (this.src1 != null && this.src1.amount == 0) this.src1 = null;
 			if (this.src2 != null && this.src2.amount == 0) this.src2 = null;
 			if (this.res == null) this.res = r.result.copy();
 			else this.res.amount += r.result.amount;
 		}
 	}
 
 	public int getAnimationStage() {
 		return this.animationStage;
 	}
 
 	private void simpleAnimationIterate() {
 		if (this.animationSpeed > 1) {
 			this.animationStage += this.animationSpeed;
 
 			if (this.animationStage > 300) {
 				this.animationStage = 100;
 			}
 		} else if (this.animationStage > 0) {
 			this.animationStage--;
 		}
 	}
 
 	private void increaseAnimation() {
 		if (this.animationSpeed < 2) {
 			this.animationSpeed = 2;
 		} else if (this.animationSpeed <= 5) {
 			this.animationSpeed += 0.1;
 		}
 
 		this.animationStage += this.animationSpeed;
 
 		if (this.animationStage > 300) {
 			this.animationStage = 100;
 		}
 	}
 
 	private void decreaseAnimation() {
 		if (this.animationSpeed >= 1) {
 			this.animationSpeed -= 0.1;
 
 			this.animationStage += this.animationSpeed;
 
 			if (this.animationStage > 300) {
 				this.animationStage = 100;
 			}
 		} else {
 			if (this.animationStage > 0) {
 				this.animationStage--;
 			}
 		}
 	}
 
 	@Override
 	void S_recievePacket(byte pattern, ByteArrayDataInput data, EntityPlayer ep) {
 
 	}
 
 	@Override
 	void C_recievePacket(byte pattern, ByteArrayDataInput data) {
 
 	}
 
 	@Override
 	public PowerReceiver getPowerReceiver(ForgeDirection side) {
 		return this.pp.getPowerReceiver();
 	}
 
 	@Override
 	public void doWork(PowerHandler workProvider) {}
 
 	@Override
 	public World getWorld() {
 		return this.worldObj;
 	}
 
 	@Override
 	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
 		if (resource.isFluidEqual(this.src1)) {
 			int ret = Math.min(this.buf - this.src1.amount, resource.amount);
 			if (doFill) this.src1.amount += ret;
 			return ret;
 		} else if (resource.isFluidEqual(this.src2)) {
 			int ret = Math.min(this.buf - this.src2.amount, resource.amount);
 			if (doFill) this.src2.amount += ret;
 			return ret;
 		} else if (this.src1 == null) {
 			int ret = Math.min(this.buf, resource.amount);
 			if (doFill) {
 				this.src1 = resource.copy();
 				this.src1.amount = ret;
 			}
 			return ret;
 		} else if (this.src2 == null) {
 			int ret = Math.min(this.buf, resource.amount);
 			if (doFill) {
 				this.src2 = resource.copy();
 				this.src2.amount = ret;
 			}
 			return ret;
 		}
 		return 0;
 	}
 
 	@Override
 	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
 		// if (resource == null) return null;
 		if (resource.equals(this.res)) return drain(from, resource.amount, doDrain);
 		if (resource.equals(this.src1)) {
 			FluidStack ret = this.src1.copy();
 			ret.amount = Math.min(resource.amount, ret.amount);
 			if (doDrain) {
 				this.src1.amount -= ret.amount;
 				if (this.src1.amount == 0) this.src1 = null;
 			}
 			return ret;
 		}
 		if (resource.equals(this.src2)) {
 			FluidStack ret = this.src2.copy();
 			ret.amount = Math.min(resource.amount, ret.amount);
 			if (doDrain) {
 				this.src2.amount -= ret.amount;
 				if (this.src2.amount == 0) this.src2 = null;
 			}
 			return ret;
 		}
 		return null;
 	}
 
 	@Override
 	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
 		if (this.res == null) return null;
 		FluidStack ret = this.res.copy();
 		ret.amount = Math.min(maxDrain, ret.amount);
 		if (doDrain) {
 			this.res.amount -= ret.amount;
 			if (this.res.amount == 0) this.res = null;
 		}
 		return ret;
 	}
 
 	@Override
 	public boolean canFill(ForgeDirection from, Fluid fluid) {
 		return true;
 	}
 
 	@Override
 	public boolean canDrain(ForgeDirection from, Fluid fluid) {
 		return true;
 	}
 
 	@Override
 	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
 		return new FluidTankInfo[] { new FluidTankInfo(this.src1, this.buf), new FluidTankInfo(this.src2, this.buf), new FluidTankInfo(this.res, this.buf) };
 	}
 }
