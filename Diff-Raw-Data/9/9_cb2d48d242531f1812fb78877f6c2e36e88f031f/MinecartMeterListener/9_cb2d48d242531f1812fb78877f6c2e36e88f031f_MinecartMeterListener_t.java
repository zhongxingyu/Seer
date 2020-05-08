 package no.openshell.oddstr13.minecartmeter;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.event.vehicle.*;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Vehicle;
 import org.bukkit.entity.Minecart;
 import org.bukkit.entity.Player;
 
 /**
  * Handle vehicle events
  * @author Oddstr13
  */
 public class MinecartMeterListener extends VehicleListener {
     private final MinecartMeter plugin;
 
     public MinecartMeterListener(MinecartMeter instance) {
         plugin = instance;
     }
 
     @Override
     public void onVehicleEnter(VehicleEnterEvent event) {
         Entity entity = event.getEntered();
         Vehicle vehicle = event.getVehicle();
         if (vehicle instanceof Minecart) {
             Minecart cart = (Minecart) vehicle;
             if (entity instanceof Player) {
                 Entity passenger = cart.getPassenger();
                 if (passenger instanceof Player) {
                     Player p = (Player) passenger;
                     System.out.println("[DEBUG]: Passenger of minecart " + cart.getEntityId() + " is " + p.getName());
                     System.out.println("[DEBUG]: This is most likly a ghost event.");
                 } else {
                     Player player = (Player) entity;
                     Location l = cart.getLocation();
                     String msg = "entered minecart with entityId " + cart.getEntityId() + 
                       " at location " + l.getWorld().getName() + "," + l.getX() + "," + l.getY() + "," + l.getZ();
                     player.sendMessage("[DEBUG]: You " + msg);
                     System.out.println("[DEBUG]: " + player.getDisplayName() + "(" + player.getName() + ") " + msg);
                     plugin.setStartLocation(player, l);
                     plugin.resetDistanceCounter(player);
                     World world = l.getWorld();
                     long world_time = world.getTime();
                    long world_hh = world_time/1000;
                    long world_mm = (world_time - world_hh)*60/1000;
                     System.out.println("[DEBUG]: full time of " + world.getName() + ": " + world.getFullTime());
                     System.out.println("[DEBUG]: time of " + world.getName() + ": " + world.getTime()+ " " + world_hh +":"+world_mm);
                     player.sendMessage("[DEBUG]: full time of " + world.getName() + ": " + world.getFullTime());
                     player.sendMessage("[DEBUG]: time of " + world.getName() + ": " + world.getTime()+ " " + world_hh +":"+world_mm);
                 }
             }
         }
     }
 
     @Override
     public void onVehicleExit(VehicleExitEvent event) {
         Entity entity = event.getExited();
         Vehicle vehicle = event.getVehicle();
         handleExitVehicle(vehicle, entity);
     }
 
     @Override
     public void onVehicleDestroy(VehicleDestroyEvent event) {
         Vehicle vehicle = event.getVehicle();
         if (vehicle instanceof Minecart) {
             Minecart cart = (Minecart) vehicle;
             Location l = cart.getLocation();
             if (!cart.isEmpty()) {
                 Entity entity = cart.getPassenger();
                 if (entity instanceof Player) {
                     Player player = (Player) entity;
                     System.out.println("[DEBUG]: minecart " + cart.getEntityId() + " destroyed, passenger " + player.getName() + " ejected.");
                     
                 }
             }
         }
     }
 
     @Override
     public void onVehicleMove(VehicleMoveEvent event) {
         Vehicle vehicle = event.getVehicle();
         Location from = event.getFrom();
         Location to = event.getTo();
 
         /* from craftbook's MinecartManager.java */
         boolean crossesBlockBoundary =
                from.getBlockX() == to.getBlockX()
             && from.getBlockY() == to.getBlockY()
             && from.getBlockZ() == to.getBlockZ();
 
         if (!(crossesBlockBoundary)) {
             if (vehicle instanceof Minecart) {
                 Minecart cart = (Minecart)vehicle;
                 if (!(cart.isEmpty())) {
                     Entity passenger = cart.getPassenger();
                     if (passenger instanceof Player) {
                         Player player = (Player)passenger;
                         plugin.increaseDistanceCounter(player);
                     }
                 }
             }
         }
     }
 
     public void handleExitVehicle(Vehicle vehicle, Entity entity) {
         if (vehicle instanceof Minecart) {
             Minecart cart = (Minecart) vehicle;
             if (entity instanceof Player) {
                 Player player = (Player) entity;
                 Location l = cart.getLocation();
                 String msg = "exited minecart with entityId " + cart.getEntityId() +
                   " at location " + l.getWorld().getName() + "," + l.getX() + "," + l.getY() + "," + l.getZ();
                 player.sendMessage("[DEBUG]: You " + msg);
                 System.out.println("[DEBUG]: " + player.getDisplayName() + "(" + player.getName() + ") " + msg);
                 Entity passenger = cart.getPassenger();
                 if (passenger instanceof Player) {
                     Player p = (Player) passenger;
                     System.out.println("[DEBUG]: Passenger of minecart " + cart.getEntityId() + " is " + p.getName());
                 }
                 Location startlocation = plugin.getStartLocation(player);
                 Double distance = l.distance(startlocation);
                 System.out.println("[DEBUG]: player " + player.getName() + " have traveled " + plugin.doubleMetersToString(distance) + " meters in direct line by railroad.");
                 player.sendMessage("You have traveled " + plugin.doubleMetersToString(distance) + " meters in direct line by railroad.");
                 player.sendMessage("You have traveled " + plugin.getDistanceCounter(player) + " meters.");
                 System.out.println("[DEBUG]: player " + player.getName() + "have traveled " + plugin.getDistanceCounter(player) + " meters.");
                 World world = l.getWorld();
                 long world_time = world.getTime();
                long world_hh = world_time/1000;
                long world_mm = (world_time - world_hh)*60/1000;
 
                 System.out.println("[DEBUG]: full time of " + world.getName() + ": " + world.getFullTime());
                 System.out.println("[DEBUG]: time of " + world.getName() + ": " + world.getTime()+ " " + world_hh +":"+world_mm);
                 player.sendMessage("[DEBUG]: full time of " + world.getName() + ": " + world.getFullTime());
                 player.sendMessage("[DEBUG]: time of " + world.getName() + ": " + world.getTime()+ " " + world_hh +":"+world_mm);
             }
         }
 
     }
 }
