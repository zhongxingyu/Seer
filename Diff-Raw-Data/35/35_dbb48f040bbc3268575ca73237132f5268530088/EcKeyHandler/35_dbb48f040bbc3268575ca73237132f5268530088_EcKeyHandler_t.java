 package ak.EnchantChanger.Client;
 
 import java.util.EnumSet;
 
 import net.minecraft.client.settings.KeyBinding;
 import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
 import cpw.mods.fml.common.TickType;
 
 public class EcKeyHandler extends KeyHandler
 {
 	public static boolean MagicKeyDown = false;
	public static boolean MagicKeyUp = true;
 	public EcKeyHandler(KeyBinding[] keyBindings, boolean[] repeatings)
 	{
 		super(keyBindings, repeatings);
 	}
 
 	@Override
 	public String getLabel()
 	{
 		return "Enchant Changer : " + this.getClass().getSimpleName();
 	}
 
 	@Override
 	public void keyDown(EnumSet<TickType> types, KeyBinding kb,boolean tickEnd, boolean isRepeat)
 	{
 		if (types.equals(EnumSet.of(TickType.CLIENT)))
 		{
 			if(kb == ClientProxy.MagicKey)
 			{
 				this.MagicKeyDown = true;
 			}
 		}
 	}
 	@Override
 	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd)
 	{
 		if (types.equals(EnumSet.of(TickType.CLIENT)))
 		{
 			if(kb == ClientProxy.MagicKey)
 			{
 				this.MagicKeyDown = false;
				this.MagicKeyUp = true;
 			}
 		}
 	}
 
 	@Override
 	public EnumSet<TickType> ticks()
 	{
 		return EnumSet.of(TickType.CLIENT);
 	}
 
 }
