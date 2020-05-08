 package net.worldoftomorrow.nala.ni;
 
 import net.worldoftomorrow.nala.ni.Items.Armor;
 import net.worldoftomorrow.nala.ni.Items.Cookable;
 import net.worldoftomorrow.nala.ni.Items.TekkitTools;
 import net.worldoftomorrow.nala.ni.Items.Tools;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public enum Perms {
 	ADMIN("noitem.admin"),
 	ALLITEMS("noitem.allitems"),
 	NOCRAFT("noitem.nocraft."),
 	NOPICKUP("noitem.nopickup."),
 	NODROP("noitem.nodrop."),
 	NOBREW("noitem.nobrew."),
 	NOUSE("noitem.nouse."),
 	NOHOLD("noitem.nohold."),
 	NOWEAR("noitem.nowear."),
 	NOCOOK("noitem.nocook."),
 	NOPLACE("noitem.noplace."),
 	NOBREAK("noitem.nobreak."),
 	ONDEATH("noitem.ondeath.");
 
 	private final String perm;
 
 	private Perms(String perm) {
 		this.perm = perm;
 	}
 
 	public boolean check(Player p, String perm) {
 		Log.debug("Checking Perm: " + perm);
 		if (Vault.vaultPerms) {
 			return Vault.has(p, perm);
 		} else {
 			return p.hasPermission(perm);
 		}
 	}
 
 	public boolean has(Player p, ItemStack stack) {
 		return this.has(p, stack.getType(), stack.getDurability());
 	}
 
 	public boolean has(Player p, Block b) {
 		return this.has(p, b.getType(), b.getData());
 	}
 
 	public boolean has(Player p, Material mat, short data) {
 		if (perm.equals(ADMIN.perm) || perm.equals(ALLITEMS.perm)
 				|| perm.equals(NOBREW.perm) || perm.equals(ONDEATH.perm)) {
 			throw new UnsupportedOperationException(
 					"Incorrect checking of a permission.");
 		}
 		String namePerm;
 		String numPerm;
 		String allNamePerm = perm + this.getItemName(mat.getId()) + ".all";
 		String allNumPerm = perm + mat.getId() + ".all";
 		if (data > 0) {
 			namePerm = perm + this.getItemName(mat.getId()) + "." + data;
 			numPerm = perm + mat.getId() + "." + data;
 		} else {
 			namePerm = perm + this.getItemName(mat.getId());
 			numPerm = perm + mat.getId();
 		}
 		return this.check(p, namePerm) || this.check(p, numPerm)
 				|| this.check(p, allNamePerm) || this.check(p, allNumPerm);
 	}
 
 	public boolean has(Player p) {
		if (!perm.equals(ADMIN.perm) && !perm.equals(ALLITEMS.perm)) {
 			throw new UnsupportedOperationException(
 					"Incorrect checking of a permission.");
 		} else {
 			return this.check(p, perm);
 		}
 	}
 	
 	public boolean has(Player p, String recipe) {
 		if(perm.equals(NOBREW.perm) || perm.equals(ONDEATH.perm)) {
			return this.check(p, perm + recipe);	
 		} else {
 			throw new UnsupportedOperationException("Incorrect checking of a permission.");
 		}
 	}
 
 	private String getItemName(int id) {
 		if (Tools.isTool(id))
 			return Tools.getTool(id).getName();
 		if (Armor.isArmor(id))
 			return Armor.getArmour(id).getName();
 		if (Cookable.isCookable(id))
 			return Cookable.getItem(id).getName();
 		if (TekkitTools.isTekkitTool(id))
 			return TekkitTools.getTool(id).getName();
 		if (Material.getMaterial(id) != null)
 			return Material.getMaterial(id).name().replace("_", "")
 					.toLowerCase();
 		return Integer.toString(id);
 	}
 }
