 package StaffDrops.republica.devteam;
 
 import net.minecraft.server.ItemStack;
 import net.minecraft.server.NBTTagCompound;
 import net.minecraft.server.NBTTagList;
 import net.minecraft.server.NBTTagString;
 
 import org.bukkit.Material;
 import org.bukkit.craftbukkit.inventory.CraftItemStack;
 
 public class ILessThanThreeMeiskam {
 	/* A class just to make a book about Meiskam, because
 	 * fuck the police, I do what I want, and Meiskam 
 	 * basically made half this plugin, so I love him.
 	 */
 	private String title = "Ode to Meiskam";
 	private String author = "xXProdigalXx";
 	
	private String pI = "\t This is a book just for meiskam, " +
 			"because he dealt with my shit in the BukkitDev IRC, " +
 			"even though he didn't have to. He wrote a good half " +
 			"of this plugin, and in the time it took me to make this " +
 			"he wrote an entire functioning plugin just to achieve " +
			"the same thing I was going for, and provide " +
			"documentation for this aspect of minecraft.";
	private String pII = "\t He will always have a special " +
 			"place in my heart. He made me a better plugin " +
 			"developer, and sat through constant pestering " +
 			"by some random kid (me). And so, that is why " +
 			"I decided to write this book,";
 	private String pIII = "in honor of him. #End";
 	
 	private ItemStack bi;
 	
 	public ILessThanThreeMeiskam(){
 		CraftItemStack book = new CraftItemStack(Material.WRITTEN_BOOK);
 		bi = book.getHandle();
 		NBTTagCompound biNBT = new NBTTagCompound();
 		
 		biNBT.setString("title", title);
 		biNBT.setString("author", author);
 		
 		NBTTagList bookPages = new NBTTagList();
 			bookPages.add(new NBTTagString("page1", pI));
 			bookPages.add(new NBTTagString("page2", pII));
 			bookPages.add(new NBTTagString("page3", pIII));
 			
 		biNBT.set("pages", bookPages);
 		
 		bi.setTag(biNBT);
 	}
 	
 	public CraftItemStack getItemStack(){
 		CraftItemStack bir = new CraftItemStack(bi);
 		return bir;
 	}
 }
