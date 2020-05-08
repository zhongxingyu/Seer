 package hunternif.mc.dota2items.effect;
 
 import hunternif.mc.dota2items.Dota2Items;
 import hunternif.mc.dota2items.core.EntityStats;
 import hunternif.mc.dota2items.core.buff.Buff;
 import hunternif.mc.dota2items.entity.EntityWrapper;
 import net.minecraft.entity.Entity;
 import net.minecraft.world.World;
 
 import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public abstract class ContinuousEffect extends EntityWrapper {
 	public static final BiMap<Buff, Class<? extends ContinuousEffect>> buffMap;
 	
 	static {
		//TODO create a registry to initialize the map:
		ImmutableBiMap.Builder<Buff, Class<? extends ContinuousEffect>> builder = ImmutableBiMap.builder();
 		
		builder.put(Buff.tango, EffectTango.class);
		builder.put(Buff.clarity, EffectClarity.class);
		builder.put(Buff.phase, EffectPhase.class);
		
		buffMap = builder.build();
 	}
 	
 	public static ContinuousEffect construct(Buff buff, Entity entity) {
 		try {
 			Class<? extends ContinuousEffect> clazz = buffMap.get(buff);
 			return clazz.getConstructor(Entity.class).newInstance(entity);
 		} catch (Exception e) {
 			Dota2Items.logger.severe("No effect exists for buff " + buff.toString());
 			return null;
 		}
 	}
 	public static boolean buffHasEffect(Buff buff) {
 		return buffMap.containsKey(buff);
 	}
 	
 	public ContinuousEffect(World world) {
 		super(world);
 	}
 	public ContinuousEffect(Entity entity) {
 		super(entity);
 	}
 
 	@Override
 	public void onEntityUpdate() {
 		super.onEntityUpdate();
 		Buff effectProducingBuff = buffMap.inverse().get(getClass());
		if (entity != null && effectProducingBuff != null) {
 			EntityStats stats = Dota2Items.stats.getEntityStats(entity);
 			if (stats == null || !stats.hasBuff(effectProducingBuff)) {
 				onEffectEnded();
 				setDead();
 			}
 		}
 		if (entity != null && entity.worldObj.isRemote) {
 			perform();
 		}
 	}
 	
 	@SideOnly(Side.CLIENT)
 	protected abstract void perform();
 	
 	protected void onEffectEnded() {}
 }
