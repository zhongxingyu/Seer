 package ccm.compression.item.block;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.minecraft.block.Block;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemBlockWithMetadata;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.StatCollector;
 
 import ccm.compression.api.CompressedData;
 import ccm.compression.block.CompressedType;
 import ccm.nucleum.omnium.utils.lib.Properties;
 
 public class CompressedItemBlock extends ItemBlockWithMetadata
 {
     public CompressedItemBlock(int id, Block block)
     {
         super(id, block);
     }
 
     @Override
     public String getItemDisplayName(ItemStack item)
     {
         return getCompressedName(item);
     }
 
     @Override
     public void addInformation(ItemStack item, EntityPlayer player, List list, boolean par4)
     {
         list.add("A single Block of this type contains: " + ((long) Math.pow(9, (item.getItemDamage() + 1))));
         list.add(getCompressedName(item));
         if (Properties.DEBUG)
         {
             CompressedData data = new CompressedData();
             data.readFromNBT(item.getTagCompound());
             list.add("The Orginal Block has an ID of: " + data.getID());
             list.add("The Orginal Block has an Metadata of: " + data.getMeta());
         }
         super.addInformation(item, player, list, par4);
     }
 
     private String getCompressedName(ItemStack item)
     {
         if (item != null)
         {
             CompressedData data = new CompressedData();
             data.readFromNBT(item.getTagCompound());
             StringBuilder sb = new StringBuilder();
             sb.append(StatCollector.translateToLocalFormatted(CompressedType.values()[item.getItemDamage()].toString()));
             sb.append(" ");
             if (data.getBlock() == null)
             {
                sb.append(StatCollector.translateToLocalFormatted("compressed.name", "ERROR"));
             } else
             {
                 List<ItemStack> list = new ArrayList<ItemStack>();
                 data.getBlock().getSubBlocks(data.getID(), null, list);
                 ItemStack stack = null;
                 for (ItemStack i : list)
                 {
                     if (i.getItemDamage() == data.getMeta())
                     {
                         stack = i;
                         break;
                     }
                 }
                 if (stack == null)
                 {
                     stack = new ItemStack(data.getBlock());
                 }
                sb.append(StatCollector.translateToLocalFormatted("compressed.name", stack.getDisplayName()));
             }
             return sb.toString();
         }
         return "ITEM IS NULL";
     }
 }
