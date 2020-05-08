 package com.maienm.FlooNetwork;
 
 import com.m0pt0pmatt.menuservice.api.ContainerAttribute;
 import com.m0pt0pmatt.menuservice.api.Menu;
 import com.m0pt0pmatt.menuservice.api.MenuComponent;
 import com.m0pt0pmatt.menuservice.api.MenuInstance;
 import com.m0pt0pmatt.menuservice.api.MenuService;
 import com.m0pt0pmatt.menuservice.api.Renderer;
 import com.maienm.FlooNetwork.Fireplace;
 import com.maienm.FlooNetwork.FlooNetwork;
 import java.util.Arrays;
 import java.util.HashMap;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 class PlayerMenu extends MenuComponent
 {
     /**
      * Whether the menu is valid.
      */
     public boolean isValid = false;
 
     /**
      * The player for which this menu has been created.
      */
     private Player player;
 
     /**
      * Menu Service (for... well, menu's).
      */
     private static MenuService menuService;
 
     /**
      * The renderer.
      */
     private static Renderer renderer;
 
     /**
      * Floo Network plugin.
      */
     private static FlooNetwork floonetwork;
 
     /**
      * The next available ID for a tag.
 
     /**
      * Initialize this class.
      *
      * Called in the onEnable method of the plugin.
      */
     public static void init()
     {
         // Get the FlooNetwork plugin instance.
         floonetwork = (FlooNetwork) Bukkit.getPluginManager().getPlugin("FlooNetwork");
 
         // Get the MenuService.
         menuService = Bukkit.getServicesManager().getRegistration(MenuService.class).getProvider();
 
         // Find the renderer.
         renderer = menuService.getRenderer("inventory");
     }
 
     public PlayerMenu(Player player)
     {
         super();
         this.player = player;
 
         // Set title.
         addAttribute("title", "Pick a fireplace to travel to:");
         addAttribute("plugin", floonetwork.getName());
         addAttribute("tag", player.getName() + "-menu");
 
         // Add items.
         for (Fireplace fp : floonetwork.getAllFireplaces())
         {
             if (fp.hasAccess(player))
             {
                 isValid = true;
                 addFireplace(fp);
             }
         }
 
         // Add the renderer.
         addRenderer(renderer);
     }
 
     /**
      * Add a fireplace to the menu.
      */
     private void addFireplace(Fireplace fp)
     {
         // Create a new component.
         MenuComponent component = new MenuComponent();
 
         // Set attributes.
         component.addAttribute("type", "button");
         component.addAttribute("tag", String.format("%s-%s", fp.owner.getName(), fp.name));
         component.addAttribute("text", ChatColor.RESET + fp.name);
         component.addAttribute("lore", Arrays.asList(ChatColor.GOLD + fp.owner.getName()));
         component.addAttribute("item", fp.item);
 
         // Add click handler.
         HashMap<String, Object> actionMap = new HashMap<String, Object>();
         HashMap<String, Object> leftClickMap = new HashMap<String, Object>();
         leftClickMap.put("tags", Arrays.asList(fp.id));
         actionMap.put("leftClick", new ContainerAttribute("leftClick", leftClickMap));
         component.addAttribute("actions", new ContainerAttribute("actions", actionMap));
 
         // Add to menu.
         addComponent(component);
     }
 
     /**
      * Show this menu.
      */
     public void show()
     {
         String instanceName = player.getName() + "-instance";
 
         // If an old version of the menu for this player still exists, remove it.
         Menu oldMenu = menuService.getMenu(floonetwork, getAttribute("tag").toString());
         if (oldMenu != null)
         {
            menuService.removeMenu(floonetwork, oldMenu);
         }
 
         // Register the menu.
         menuService.addMenu(floonetwork, this);
 
         // Create an instance of the menu.
         MenuInstance instance = menuService.createMenuInstance(this, instanceName);
 
         // Add the action listener.
         instance.addActionListener(floonetwork);
 
         // Show the menu.
         menuService.openMenuInstance(instance, player.getName());
     }
 }
