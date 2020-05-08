 package elxris.SpiceCraft.Objects;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.inventory.ClickType;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryView;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.inventory.meta.SkullMeta;
 
 import elxris.SpiceCraft.SpiceCraft;
 import elxris.SpiceCraft.Utils.Chat;
 import elxris.SpiceCraft.Utils.Econ;
 import elxris.SpiceCraft.Utils.Strings;
 
 public class FactoryGui
 {
     private static Factory f;
     private static ItemStack itemNext;
     private static ItemStack itemPrevious;
     private static ItemStack itemReturn;
     public static final int SIZE = 27;
     private Player p;
     // 0 - 53 slots.
     public FactoryGui(Factory f){
         FactoryGui.f = f;
     }
     public FactoryGui(Player p){
         this.p = p;
     }
     public void updateInventory(Inventory inv){
         inv.clear();
         List<ItemStack> items;
         int itemsSize;
         // Si existe una lista de objetos.
         if(isRelativeSet("list")){
             itemsSize = getItemListSize();
         // Si existe un submenu
         }else if(isRelativeSet("sub")){
             itemsSize = getItemMenuSize();
         // Si es una tienda de usuario.
         }else if(isUserShop()){
             itemsSize = getItemUserSize();
         }else{
             return;
         }
         int itemsPerPage = SIZE;
         int page = getPage();
         // Si es la tienda del servidor, y no es una tienda particular, incluye un return;
         if(!isRelativeSet("return") && !isUserShop()){
             inv.addItem(getReturn());
             itemsPerPage--;
         }
         // Si hay ms objetos de los que caben en una pgina.
         if(itemsSize > itemsPerPage){
             itemsPerPage -= 2;
         }else if(itemsSize <= itemsPerPage){
             page = 1;
         }
         if(itemsSize > itemsPerPage){
             // Si la pgina es mayor a 1
             if(page > 1){
                 inv.setItem((SIZE-2)-itemsPerPage, getPrev());
             }
             if(itemsSize-(page*itemsPerPage)>=1){
                 inv.setItem((SIZE-1)-itemsPerPage, getNext());
             }
         }
         if(isRelativeSet("list")){
             // Si existe una lista de objetos.
             items = getItemList(getPath(), itemsPerPage, page);
         }else if(isRelativeSet("sub")){
             // Si existe un submenu
             items = getItemMenu(getPath("sub"));
         }else if(isUserShop()){
             items = getItemUser(getPath("items"), itemsPerPage, page);
         }else{
             return;
         }
         if(items == null || items.size() == 0){
             return;
         }
         int count = 0;
         for(int i = SIZE-itemsPerPage; i < SIZE; i++){
             inv.setItem(i, items.get(count));
             if(++count >= items.size()){
                 break;
             }
         }
         f.save();
     }
     public List<ItemStack> getItemList(String path, int itemsPerPage, int page){
         List<ItemStack> list = new ArrayList<ItemStack>();
         Econ econ = new Econ();
         // Inicia algunas variables que se usarn en el loop.
         ItemStack i;
         ItemMeta meta;
         String id;
         double precioBuy, precioSell;
         int limitUP = (page)*itemsPerPage;
         int limitDOWN = (--page)*itemsPerPage;
         int count = 0;
         for(String item : getConfig().getStringList(path+".list")){
             if(count < limitDOWN){
                 count++;
                 continue;
             }else if(count >= limitUP){
                 break;
             }
             count++;
             i = f.createItem(p.getName(), item, 1);
             meta = i.getItemMeta();
             id = i.getTypeId()+((i.getDurability() > 0)?":"+i.getDurability():"");
             precioBuy = f.getPrice(item);
             precioSell = precioBuy*f.SELLRATE/f.MULTIPLIER;
             if(!f.getUserBuy(p, item)){
                 precioBuy = 0d;
             }
             if(!f.getUserSell(p, item)){
                 precioSell = 0d;
             }
             meta.setLore(Strings.getStringList("shop.itemLore",
                     econ.getPrecio(precioBuy), econ.getPrecio(precioSell),
                     f.getProduction(item), id));
             i.setItemMeta(meta);
             list.add(i);
         }
         return list;
     }
     public List<ItemStack> getItemMenu(String path){
         List<ItemStack> list = new ArrayList<ItemStack>();
         for(String item : getConfig().getConfigurationSection(path).getKeys(false)){
             String name = getConfig().getString(path+"."+item+".name");
             ItemStack i = f.createItem(p.getName(), item, 1);
             ItemMeta m = i.getItemMeta();
             m.setDisplayName(name);
             i.setItemMeta(m);
             list.add(i);
         }
         return list;
     }
     public List<ItemStack> getItemUser(String path, int itemsPerPage, int page){
         if(getItemUserSize() == 0){
             return null;
         }
         List<ItemStack> list = new ArrayList<ItemStack>();
         ItemStack i;
         ItemMeta meta;
         String id, production;
         int acc, stock;
         double precio;
         int limitUP = (page)*itemsPerPage;
         int limitDOWN = (--page)*itemsPerPage;
         int count = 0;
         ConfigurationSection config = getUserConfig().getConfigurationSection(path);
         for(String item : config.getKeys(false)){
             if(count < limitDOWN){
                 count++;
                 continue;
             }else if(count >= limitUP){
                 break;
             }
             stock = getItemStock(item);
             if(stock <= 0){
                 if(isOwnShop()){
                     if(getUserConfig().isSet(getPath("items."+item+".amount"))){
                         Chat.mensaje(p, "shop.outOfStock", item);
                         getUserConfig().set(getPath("items."+item+".amount"), null);
                     }
                 }
                 continue;
             }
             i = f.createItem(getUserShopName(), item, 1);
             meta = i.getItemMeta();
             production = f.getProduction(item)+"";
             acc = getItemVel(item);
             if(acc < 0){
                 production += String.format(Strings.getString("shop.negativeProduction"), acc);
             }else if(acc > 0){
                 production += String.format(Strings.getString("shop.positiveProduction"), "+"+acc);
             }
             id = i.getTypeId()+((i.getDurability() > 0)?":"+i.getDurability():"");
             precio = (f.getPrice(item, acc)/f.MULTIPLIER)*f.USERMULTIPLIER;
             meta.setLore(
                     Strings.getStringList("shop.userItemLore",
                     new Econ().getPrecio(precio),
                     stock, production, id));
             i.setItemMeta(meta);
             if(stock > i.getMaxStackSize()){
                 //stock = i.getMaxStackSize();
             }
             i.setAmount(stock);
             list.add(i);
         }
         return list;
     }
     public int getItemListSize(){
         String path = getPath();
         return getConfig().getStringList(path+".list").size();
     }
     public int getItemMenuSize(){
         String path = getPath("sub");
         return getConfig().getConfigurationSection(path).getKeys(false).size();
     }
     public int getItemUserSize(){
         String path = getPath("items");
         if(getUserConfig().isSet(path)){
             int count = 0;
             for(String item : getUserConfig().getConfigurationSection(path).getKeys(false)){
                 if(getItemStock(item) > 0){
                     count++;
                 }
             }
             return count;
         }
            return 0;
     }
     public ItemStack getNext(){
         if(itemNext == null){
             itemNext = getItemMenu("shop.next").get(0);
         }
         return itemNext;
     }
     public ItemStack getPrev(){
         if(itemPrevious == null){
             itemPrevious = getItemMenu("shop.previous").get(0); 
         }
         return itemPrevious;
     }
     public ItemStack getReturn(){
         if(itemReturn == null){
             itemReturn = getItemMenu("shop.return").get(0);
         }
         return itemReturn;
     }
     public String getUserShopName() {
         String user = getPath().substring("userShop.".length());
         user = user.substring(0, user.length()-1);
         return user;
     }
     /*
     click[Top/Bot][Cursor][Slot]
     */
     public boolean click(final InventoryClickEvent e){
         boolean cancelled = false;
         InventoryView view = e.getView();
         ClickType click = e.getClick();
         int currentItem = e.getRawSlot();
         // Si el click es dentro del inventario de la tienda.
         if(currentItem < 0){
             return cancelled;
         }
         if(e.getRawSlot() < e.getInventory().getSize()){
             cancelled = clickTopCursorSlot(view, click, currentItem);
         // Si el click es fuera del inventario de la tienda.
         }else{
             // Cancela lo que no sea un clic derecho, izquierdo o doble click.
             cancelled = clickBotCursorSlot(view, click, currentItem);
         }
         return cancelled;
     }
     // Click en la tienda con algo en mano y algo en el slot.
     public boolean clickTopCursorSlot(InventoryView view, ClickType click, int currentItem){
         boolean cancelled = false;
         // Si tiene el cursor est vacio.
         if(view.getCursor().getTypeId() == 0){
             clickTopSlot(view, click, currentItem);
         // Si el cursor tiene objeto.
         }else{
             clickTopCursor(view, click, currentItem);
         }
         playSound();
         cancelled = true;
         return cancelled;
     }
     // Click en la tienda con algo en mano.
     public void clickTopCursor(InventoryView view, ClickType click, int currentItem){
         // Si es una tienda de usuario.
         if(isUserShop()){
             // Si es dueo
             if(isOwnShop()){
                 addCursorToShop(view, click);
                 updateInventory(view.getTopInventory());
             // Si no es dueo, no puede modificar tienda.
             }else{
                 return;
             }
         }else{
             ItemStack cursor = view.getCursor();
             int amount = cursor.getAmount();
             // Si es izquierdo, vende el stack.
             if(click == ClickType.LEFT){
                 f.sellItem(p, cursor);
                 cursor.setAmount(0);
                 // Si es derecho, vende uno.
             }else if(click == ClickType.RIGHT){
                 cursor.setAmount(1);
                 f.sellItem(p, cursor);
                 cursor.setAmount(--amount);
             }
             view.setCursor(cursor);
         }
     }
     // Click en la tienda con algo en el slot.
     public void clickTopSlot(InventoryView view, ClickType click, int currentItem){
         ItemStack current = view.getItem(currentItem);
         // Cancela si el slot clickado no tiene un objeto.
         if(current.getTypeId() == 0){
             return;
         }
         // Si tiene nombre seguro es un men.
         if(current.getItemMeta().hasDisplayName()){
             String itemName = current.getItemMeta().getDisplayName();
             if(itemName.contentEquals(getReturn().getItemMeta().getDisplayName())){
                 close();
             }else if(itemName.contentEquals(getNext().getItemMeta().getDisplayName())){
                 addPage(1);
             }else if(itemName.contentEquals(getPrev().getItemMeta().getDisplayName())){
                 addPage(-1);
             }else{
                 // Si es un men.
                 addPath(f.getItemName(current));
                 setPage(1);
             }
             updateInventory(view.getTopInventory());
         // Si no es un objeto-men, es un item de la tienda.
         }else{
             String item = f.getItemName(current);
             ItemStack currentStack = f.createItem(p.getName(), item, 1);
             FileConfiguration c = getUserConfig();
             // Si es una tienda
             if(isUserShop()){
                 // Si es su tienda.
                 if(isOwnShop()){
                     // Si es con shift regresa todo al inventario.
                     if(click.isShiftClick()){
                         int amount = 0;
                         for(ItemStack k: view.getBottomInventory().getContents()){
                             if(k == null){
                                 amount += currentStack.getMaxStackSize();
                             }else if(k.isSimilar(currentStack)){
                                 amount += currentStack.getMaxStackSize()-k.getAmount();
                             }
                         }
                         if(amount > getItemStock(item)){
                             amount = getItemStock(item);
                             c.set(getPath("items."+item), null);
                         }else{
                             addItemStock(item, -amount);
                         }
                         double cost = 0;
                         List<ItemStack> items = f.createItems(p.getName(), item, amount);
                         for(ItemStack i: items){
                             cost += ((double)i.getAmount()/(double)i.getMaxStackSize())*f.TAXPERREMOVEDSTACK;
                         }
                         new Econ().cobrar(p, cost);
                         new Econ().getLogg().logg("Shop", p, "tax for remove items", item, amount, cost);
                         f.addItemsToInventory(p, items);
                     // Si es izquierdo, ponlo ms caro.
                     }else if(click.isLeftClick()){
                         addItemVel(item, +1);
                     // Si es derecho, ponlo ms barato.
                     }else if(click.isRightClick()){
                         addItemVel(item, -1);
                     }
                     updateInventory(view.getTopInventory());
                 // Si no es su tienda, compra.
                 }else{
                     if(!p.hasPermission("spicecraft.shop.private.buy")){
                         Chat.mensaje(p, "alert.permission");
                         return;
                     }
                     int stock = getItemStock(item);
                     int amount;
                     // Si es con shift o derecho compra un stack.
                     if(click.isShiftClick() && click.isLeftClick()){
                         amount = current.getMaxStackSize();
                     // Si es izquierdo, compra uno.
                     }else if(!click.isShiftClick() && click.isLeftClick()){
                         amount = 1;
                     }else{
                         return;
                     }
                     // Si no hay suficiente para un stack, haz un stack pequeo. Y elimina.
                     if(amount > stock){
                         amount = stock;
                     }
                     String user = getUserShopName();
                     if(f.shopUser(p, item, amount, getItemVel(item))){
                         Chat.mensaje(SpiceCraft.getOnlineExactPlayer(user), "shop.userBuyUser",
                                 p.getName(), amount, item);
                         addMoney(f.getPrecio(item, amount, getItemVel(item))/f.MULTIPLIER*f.USERMULTIPLIER);
                         addItemStock(item, -amount);
                     }
                     pay(user);
                     updateInventory(view.getTopInventory());
                 }
             // Si es la tienda del servidor
             }else{
                 // Se compra uno.
                 if(!click.isShiftClick() && click.isLeftClick()){
                     f.shop(p, f.getItemName(current), 1);
                     // Se compra un stack.
                 }else if(click.isShiftClick() && click.isLeftClick()){
                     f.shop(p, f.getItemName(current), current.getMaxStackSize());
                 }
             }
         }
     }
     // Click fuera de la tienda con algo en el cursor y en el slot.
     public boolean clickBotCursorSlot(InventoryView view, ClickType click, int currentItem){
         boolean cancelled = false;
         if(!(click.isRightClick()
                 || click.isLeftClick()
                 || click == ClickType.DOUBLE_CLICK)){
             cancelled = true;
         }
         cancelled = clickBotSlot(view, click, currentItem);
         return cancelled;
     }
     // Click fuera de la tienda con algo en el cursor.
     public void clickBotCursor(InventoryView view, ClickType click, int currentItem){
         
     }
     // Click fuera de la tienda con algo en el slot.
     public boolean clickBotSlot(InventoryView view, ClickType click, int currentItem){
         boolean cancelled = false;
         ItemStack current = view.getItem(currentItem);
         ItemStack cursor = view.getCursor();
         if(click.isShiftClick()){
             if(current.getTypeId() == 0){
                 return cancelled;
             }
         }
         if(isUserShop()){
             // Si es propia.
             if(isOwnShop()){
                 if(click.isShiftClick()){
                     // Pon en la tienda como si el objeto estuviera en tu mano.
                     if(cursor.isSimilar(current)){
                         current.setAmount(current.getAmount()+cursor.getAmount());
                         view.setItem(currentItem, null);
                     }else{
                         view.setItem(currentItem, cursor);
                         cancelled = true;
                     }
                     view.setCursor(current);
                     addCursorToShop(view, click);
                     updateInventory(view.getTopInventory());
                 }
             }else{
                 if(click.isShiftClick()){
                     cancelled = true;
                 }
             }
         }else{
             // Si se hace shift click fuera del inventario de la tienda, se vende el stack.
             if(click.isShiftClick()){
                 view.setItem(currentItem, null);
                 f.sellItem(p, current);
                 cancelled = true;
             }
         }
         return cancelled;
     }
     private void addCursorToShop(InventoryView view, ClickType click){
         if(!p.hasPermission("spicecraft.shop.private.sell")){
             Chat.mensaje(p, "alert.permission");
             return;
         }
         ItemStack cursor = view.getCursor();
         String item = f.getItemName(cursor);
         if(item == null){
             return;
         }
         int amount = cursor.getAmount();
         FileConfiguration c = getUserConfig();
         if(cursor.getItemMeta().hasEnchants()){
             return;
         }
         if(cursor.getDurability()>0){
             if(cursor.getType().getMaxDurability() > 0){
                 return;
             }
         }
         if(cursor.getType() == Material.SKULL_ITEM){
             if(cursor.getDurability() == 3){
                 if(!((SkullMeta) cursor.getItemMeta()).getOwner().contentEquals(view.getPlayer().getName())){
                     return;
                 }
             }
         }
         if(cursor.getType() == Material.ENCHANTED_BOOK){
             return;
         }
         if(click.isRightClick()){
             amount = 1;
         }
         // Si no existe, crealo.
         if(!c.isSet(getPath("items."+item))){
             if(!c.isSet(getPath("items"))
                    ||(c.getConfigurationSection(getPath("items")).getKeys(false).size() < 
                     SpiceCraft.plugin().getConfig().getInt("shop.userShopSize"))){
                 setItemStock(item, amount);
                 setItemVel(item, 0);
             }else{
                 return;
             }
         // Si existe, aade ms stock.
         }else{
             addItemStock(item, amount);
         }
         cursor.setAmount(cursor.getAmount() - amount);
         view.setCursor(cursor);
     }
     public void setItemStock(String item, int amount){
         getUserConfig().set(getPath("items."+item+".amount"), amount);
     }
     public int getItemStock(String item){
         return getUserConfig().getInt(getPath("items."+item+".amount"), 0);
     }
     public void addItemStock(String item, int amount){
         setItemStock(item, getItemStock(item)+amount);
     }
     public void setItemVel(String item, int vel){
         getUserConfig().set(getPath("items."+item+".vel"), vel);
     }
     public int getItemVel(String item){
         return getUserConfig().getInt(getPath("items."+item+".vel"));
     }
     public void addItemVel(String item, int vel){
         setItemVel(item, getItemVel(item)+vel);
         int limit = SpiceCraft.plugin().getConfig().getInt("shop.userVariation");
         if(getItemVel(item) > limit){
             setItemVel(item, limit);
         }else if(getItemVel(item) < -limit){
             setItemVel(item, -limit);
         }
     }
     public void setMoney(double money){
         getUserConfig().set(getPath("money"), money);
     }
     public double getMoney(){
         return getUserConfig().getDouble(getPath("money"));
     }
     public void addMoney(double money){
         setMoney(getMoney()+money);
     }
     public void pay(Player p){
         String path = getPath();
         if(p == null){
             return;
         }
         setPath("userShop."+p.getName()+".");
         Econ e = new Econ();
         if(e.pagar(p, getMoney())){
             e.getLogg().logg("Shop", p, "receive", "for sell some objects", 0, getMoney());
             setMoney(0);
         }
         setPath(path);
     }
     public void pay(String player){
         pay(SpiceCraft.getOnlineExactPlayer(player));
     }
     public String getPath(){
         if(!getCache().isSet(p.getName()+".set") || !getCache().getBoolean(p.getName()+".set")){
             getCache().set(p.getName()+".set", true);
             setPath("shop.");
         }
         return getCache().getString(p.getName()+".path");
     }
     public boolean isRelativeSet(String s){
         return getConfig().isSet(getPath(s));
     }
     public String getPath(String s){
         return getPath()+s;
     }
     public void setPath(String s){
         // Nos aseguramos que est inicializada la tienda.
         getPath();
         getCache().set(p.getName()+".path", s);
     }
     public void addPath(String s){
         setPath(getPath()+"sub."+s+".");
     }
     public int getPage(){
         return getCache().getInt(p.getName()+".page", 1);
     }
     public void setPage(int i){
         getCache().set(p.getName()+".page", i);
     }
     public void addPage(int i){
         setPage(getPage()+i);
     }
     public void close(){
         getCache().set(p.getName(), null);
     }
     public void playSound(){
         Random rndm = new Random();
         p.getWorld().playSound(p.getLocation(), Sound.CLICK, 0.2f, (rndm.nextFloat()/5f)+0.8f);
     }
     public boolean isUserShop(){
         return getPath().startsWith("userShop.");
     }
     public boolean isOwnShop(){
         if(isUserShop()){
             return getPath().contentEquals("userShop."+p.getName()+".");
         }
         return false;
     }
     public FileConfiguration getCache(){
         return Factory.getVolatilCache();
     }
     public FileConfiguration getConfig(){
         return Factory.getCache();
     }
     public FileConfiguration getUserConfig(){
         return Factory.getUserCache();
     }
 }
