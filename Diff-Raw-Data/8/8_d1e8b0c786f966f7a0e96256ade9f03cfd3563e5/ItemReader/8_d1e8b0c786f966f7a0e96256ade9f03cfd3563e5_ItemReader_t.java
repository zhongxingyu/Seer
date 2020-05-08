 package boq.cctags.tag;
 
 import java.lang.ref.WeakReference;
 import java.util.List;
 import java.util.Random;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagInt;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.*;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import boq.cctags.CCTags;
 import boq.cctags.cc.TileEntityScanner;
 import boq.cctags.tag.EntityTagsListener.TagProperty;
 import boq.cctags.tag.access.EntityAccess.IPositionProvider;
 import boq.cctags.tag.access.*;
 import boq.utils.misc.PlayerOrientation;
 import boq.utils.misc.Utils;
 
 import com.google.common.base.Preconditions;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ItemReader extends Item {
 
     private static final String IS_PAIRED_TAG = "IsPaired";
     private static final String UID_TAG_NAME = "UID";
     private static final int READER_NORMAL = 0;
     public static final int READER_ADVANCED = 1;
 
     private final static Random RANDOM = new Random();
 
     public static class PlayerPositionProvider implements IPositionProvider {
 
         private final WeakReference<EntityPlayer> player;
 
         public PlayerPositionProvider(EntityPlayer player) {
             this.player = new WeakReference<EntityPlayer>(player);
         }
 
         private EntityPlayer get() {
             EntityPlayer p = player.get();
             Preconditions.checkNotNull(p, "Trying to access unloaded player");
             return p;
         }
 
         @Override
         public Vec3 getPosition() {
            EntityPlayer player = get();
            return Vec3.createVectorHelper(player.posX, player.posY, player.posZ);
         }
 
         @Override
         public ForgeDirection getOrientation() {
             return PlayerOrientation.getEntityOrientation(get()).toDirection();
         }
 
         @Override
         public World getWorld() {
             return get().worldObj;
         }
 
         @Override
         public boolean isValid() {
             return player.get() != null;
         }
 
     }
 
     private Icon advancedIcon;
 
     public ItemReader(int id) {
         super(id);
         setMaxDamage(0);
         setHasSubtypes(true);
         setCreativeTab(CCTags.instance.tabTags);
         setUnlocalizedName("tag-reader");
     }
 
     public static boolean isAdvanced(ItemStack stack) {
         return stack.getItemDamage() == READER_ADVANCED;
     }
 
     public static boolean isPaired(ItemStack stack) {
         return stack.hasTagCompound() && stack.stackTagCompound.getBoolean(IS_PAIRED_TAG);
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     @Override
     @SideOnly(Side.CLIENT)
     public void addInformation(ItemStack stack, EntityPlayer player, List description, boolean extended) {
         if (isAdvanced(stack) && stack.hasTagCompound()) {
             NBTTagCompound data = stack.stackTagCompound;
             if (data.getBoolean(IS_PAIRED_TAG)) {
                 int x = data.getInteger("PairedX");
                 int y = data.getInteger("PairedY");
                 int z = data.getInteger("PairedZ");
                 description.add(StatCollector.translateToLocalFormatted("handheld.paired.short", x, y, z));
             }
         }
 
     }
 
     public static int getReaderSerial(ItemStack stack) {
         NBTTagCompound data = stack.stackTagCompound;
         if (data == null) {
             data = new NBTTagCompound("tag");
             stack.stackTagCompound = data;
         }
 
         NBTTagInt uid = Utils.getTag(data, UID_TAG_NAME);
         if (uid != null)
             return uid.data;
 
         int serial = Math.abs(RANDOM.nextInt());
         data.setInteger(UID_TAG_NAME, serial);
         return serial;
     }
 
     private static TileEntityScanner getPairedPeripheral(World world, ItemStack stack) {
         if (!stack.hasTagCompound())
             return null;
 
         NBTTagCompound data = stack.stackTagCompound;
 
         if (!data.getBoolean(IS_PAIRED_TAG))
             return null;
 
         int x = data.getInteger("PairedX");
         int y = data.getInteger("PairedY");
         int z = data.getInteger("PairedZ");
 
         TileEntity e = world.getBlockTileEntity(x, y, z);
 
         if (e instanceof TileEntityScanner)
             return (TileEntityScanner)e;
 
         data.setBoolean(IS_PAIRED_TAG, false);
         return null;
     }
 
     public static void signalTagRead(IPositionProvider reader, ItemStack stack, ITagAccess access) {
         World world = reader.getWorld();
         TileEntityScanner scanner = getPairedPeripheral(world, stack);
 
         if (scanner != null) {
             ITagAccess wrappedAccess = new PositionProxyAccess(reader, scanner.provider, access);
             int serial = getReaderSerial(stack);
             scanner.signalTagRead(wrappedAccess, serial);
         }
     }
 
     public static void signalTagRead(EntityPlayer player, ItemStack stack, EntityLivingBase living) {
         IPositionProvider reader = new PlayerPositionProvider(player);
         ITagAccess access = new EntityLivingAccess(living, reader);
         signalTagRead(reader, stack, access);
     }
 
     public static void signalTagRead(EntityPlayer player, ItemStack stack, EntityTag tag) {
         IPositionProvider reader = new PlayerPositionProvider(player);
         ITagAccess access = new EntityTagAccess(tag, reader);
         signalTagRead(reader, stack, access);
     }
 
     public static void pairWithScanner(TileEntityScanner scanner, ItemStack stack) {
         NBTTagCompound tag = Utils.getItemTag(stack);
 
         tag.setBoolean(IS_PAIRED_TAG, true);
         tag.setInteger("PairedX", scanner.xCoord);
         tag.setInteger("PairedY", scanner.yCoord);
         tag.setInteger("PairedZ", scanner.zCoord);
     }
 
     @Override
     public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
         if (world.isRemote)
             return false;
 
         if (isAdvanced(stack) && player.isSneaking()) {
             TileEntity te = world.getBlockTileEntity(x, y, z);
             if (te instanceof TileEntityScanner) {
                 pairWithScanner((TileEntityScanner)te, stack);
                 player.sendChatToPlayer(ChatMessageComponent.func_111082_b("handheld.paired", x, y, z));
                 return true;
             }
         }
 
         return false;
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister registry) {
         itemIcon = registry.registerIcon("cctags:handheld");
         advancedIcon = registry.registerIcon("cctags:handheld-advanced");
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public Icon getIconFromDamage(int damage) {
         return (damage == READER_ADVANCED) ? advancedIcon : itemIcon;
     }
 
     @Override
     public String getUnlocalizedName(ItemStack stack) {
         return isAdvanced(stack) ? "item.tag-reader.advanced" : "item.tag-reader";
     }
 
     @SuppressWarnings({ "unchecked", "rawtypes" })
     @Override
     @SideOnly(Side.CLIENT)
     public void getSubItems(int id, CreativeTabs tab, List result) {
         result.add(new ItemStack(id, 1, READER_NORMAL));
         result.add(new ItemStack(id, 1, READER_ADVANCED));
     }
 
     @Override
     public boolean func_111207_a(ItemStack stack, EntityPlayer player, EntityLivingBase entity) {
        if (!CCTags.proxy.isServer())
             return false;
 
         TagProperty prop = EntityTagsListener.getProperty(entity);
 
         if (prop != null && prop.tagData != null) {
             if (!player.worldObj.isRemote) {
                 Object[] params = prop.tagData.tagDescription(entity);
                 player.sendChatToPlayer(ChatMessageComponent.func_111082_b("handheld.desc", params));
 
                 if (isAdvanced(stack) && isPaired(stack))
                     signalTagRead(player, stack, entity);
             }
             return true;
         }
 
         return false;
     }
 
     public static void trySignalTagRead(ItemStack stack, EntityPlayer player, EntityTag tag) {
         if (stack.getItem() instanceof ItemReader && isAdvanced(stack) && isPaired(stack))
             signalTagRead(player, stack, tag);
     }
 }
