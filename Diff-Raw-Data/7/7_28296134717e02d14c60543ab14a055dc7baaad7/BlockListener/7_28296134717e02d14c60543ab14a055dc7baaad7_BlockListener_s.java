 package ch.minepvp.spout.kingdoms.listener;
 
 import ch.minepvp.spout.kingdoms.Kingdoms;
 import ch.minepvp.spout.kingdoms.component.KingdomsComponent;
 import ch.minepvp.spout.kingdoms.database.table.Kingdom;
 import ch.minepvp.spout.kingdoms.database.table.Plot;
 import ch.minepvp.spout.kingdoms.database.table.Zone;
 import ch.minepvp.spout.kingdoms.entity.KingdomRank;
 import ch.minepvp.spout.kingdoms.manager.KingdomManager;
 import ch.minepvp.spout.kingdoms.manager.PlotManager;
 import ch.minepvp.spout.kingdoms.manager.ZoneManager;
 import com.alta189.simplesave.Database;
 import org.spout.api.chat.ChatArguments;
 import org.spout.api.entity.Player;
 import org.spout.api.event.EventHandler;
 import org.spout.api.event.Listener;
 import org.spout.api.event.Order;
 import org.spout.api.event.block.BlockChangeEvent;
 import org.spout.api.lang.Translation;
 import org.spout.vanilla.event.block.SignUpdateEvent;
 import org.spout.vanilla.event.cause.PlayerBreakCause;
 import org.spout.vanilla.event.cause.PlayerPlacementCause;
 
 public class BlockListener implements Listener {
 
     private Kingdoms plugin;
     private Database db;
 
     private KingdomManager kingdomManager;
     private PlotManager plotManager;
     private ZoneManager zoneManager;
 
 
     public BlockListener() {
 
         plugin = Kingdoms.getInstance();
         db = plugin.getDatabase();
 
         kingdomManager = plugin.getKingdomManager();
         plotManager = plugin.getPlotManager();
         zoneManager = plugin.getZoneManager();
 
     }
 
     @EventHandler (order = Order.EARLIEST)
     public void onBlockChangeEventProtection( BlockChangeEvent event ) {
 
         Player player;
 
         Kingdom kingdom = kingdomManager.getKingdomByPoint( event.getBlock().getPosition() );
         Plot plot = plotManager.getPlotByPoint( event.getBlock().getPosition() );
         Zone zone = zoneManager.getZoneByPoint( event.getBlock().getPosition() );
 
         if ( kingdom == null && plot == null && zone == null ) {
             return;
         }
 
         if ( event.getCause() instanceof PlayerBreakCause) {
 
             player = ((PlayerBreakCause) event.getCause()).getSource();
 
             if ( kingdom != null ) {
 
                 if ( plot != null ) {
 
                     // Plot
                     if ( !plot.getOwner().equalsIgnoreCase( player.getName() ) ) {
                         event.setCancelled(true);
                         player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You cant Build in a others Player Plot!", player) ) );
                         return;
                     }
 
                 } else {
 
                     // Kingdom
                     if ( player.get(KingdomsComponent.class).getKingdom() != null ) {
 
                         if ( kingdom.getId() != player.get(KingdomsComponent.class).getKingdom().getId() ) {
                             event.setCancelled(true);
                             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You cant Build in a other Kingdom!", player) ) );
                             return;
                         }
 
                         if ( player.get(KingdomsComponent.class).getMember().getRank().equals( KingdomRank.NOVICE ) ) {
                             event.setCancelled(true);
                             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You need to be Member in the Kingdom to Build!", player) ) );
                             return;
                         }
 
                     } else {
                         event.setCancelled(true);
                         player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You cant Build in this Kingdom!", player) ) );
                         return;
                     }
 
                 }
 
             }
 
             if ( zone!= null ) {
 
                if ( player.hasPermission("kingdoms.zone.build.bypass") ) {
 
                     if ( !zone.isBuild() ) {
                         event.setCancelled(true);
                         player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You cant Build in this Zone!", player) ) );
                         return;
                     }
 
                     if ( zone.getKingdom() == null ) {
                         event.setCancelled(true);
                         player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}No Kingdom has this Zone!", player) ) );
                         return;
                     }
 
                     if ( !zone.isAttack() ) {
                         event.setCancelled(true);
                         player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}This Zone is not attacked!", player) ) );
                         return;
                     }
 
                     if ( player.get(KingdomsComponent.class).getKingdom() == null ) {
                         event.setCancelled(true);
                         player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You cant Build in this Zone!", player) ) );
                         return;
                     }
 
                     if ( !player.get(KingdomsComponent.class).getKingdom().getName().equalsIgnoreCase( zone.getKingdom().getName() ) &&
                             !player.get(KingdomsComponent.class).getKingdom().getName().equalsIgnoreCase( zone.getAttacker().getName() ) ) {
 
                         event.setCancelled(true);
                         player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You cant Build in this Zone!", player) ) );
                         return;
                     }
 
                 }
 
             }
 
         } else if ( event.getCause() instanceof PlayerPlacementCause) {
 
             player = ((PlayerPlacementCause) event.getCause()).getSource();
 
             if ( kingdom != null ) {
 
                 if ( plot != null ) {
 
                     // Plot
                     if ( !plot.getOwner().equalsIgnoreCase( player.getName() ) ) {
                         event.setCancelled(true);
                         player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You cant Build in a others Player Plot!", player) ) );
                         return;
                     }
 
                 } else {
 
                     // Kingdom
                     if ( player.get(KingdomsComponent.class).getKingdom() != null ) {
 
                         if ( kingdom.getId() != player.get(KingdomsComponent.class).getKingdom().getId() ) {
                             event.setCancelled(true);
                             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You cant Build in a other Kingdom!", player) ) );
                             return;
                         }
 
                         if ( player.get(KingdomsComponent.class).getMember().getRank().equals( KingdomRank.NOVICE ) ) {
                             event.setCancelled(true);
                             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You need to be Member in the Kingdom to Build!", player) ) );
                             return;
                         }
 
                     } else {
                         event.setCancelled(true);
                         player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You cant Build in this Kingdom!", player) ) );
                         return;
                     }
 
                 }
 
             }
 
             if ( zone!= null ) {
 
                if ( player.hasPermission("kingdoms.zone.build.bypass") ) {
 
                     if ( !zone.isBuild() ) {
                         event.setCancelled(true);
                         player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You cant Build in this Zone!", player) ) );
                         return;
                     }
 
                     if ( zone.getKingdom() == null ) {
                         event.setCancelled(true);
                         player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}No Kingdom has this Zone!", player) ) );
                         return;
                     }
 
                     if ( !zone.isAttack() ) {
                         event.setCancelled(true);
                         player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}This Zone is not attacked!", player) ) );
                         return;
                     }
 
                     if ( player.get(KingdomsComponent.class).getKingdom() == null ) {
                         event.setCancelled(true);
                         player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You cant Build in this Zone!", player) ) );
                         return;
                     }
 
                     if ( !player.get(KingdomsComponent.class).getKingdom().getName().equalsIgnoreCase( zone.getKingdom().getName() ) &&
                             !player.get(KingdomsComponent.class).getKingdom().getName().equalsIgnoreCase( zone.getAttacker().getName() ) ) {
 
                         event.setCancelled(true);
                         player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You cant Build in this Zone!", player) ) );
                         return;
                     }
 
                 }
 
             }
 
         }
 
     }
 
     @EventHandler (order = Order.MONITOR)
     public void onBlockChangeEventStats( BlockChangeEvent event ) {
 
         if ( event.isCancelled() ) {
             return;
         }
 
         Player player;
 
         // Stats
         if ( event.getCause() instanceof PlayerBreakCause ) {
 
             player = ((PlayerBreakCause) event.getCause()).getSource();
             player.get(KingdomsComponent.class).getMember().addBlockBreak();
 
             if ( player.get(KingdomsComponent.class).getKingdom() != null ) {
                 player.get(KingdomsComponent.class).getKingdom().addBlockBreak();
             }
 
         } else if ( event.getCause() instanceof PlayerPlacementCause ) {
 
             player = ((PlayerPlacementCause) event.getCause()).getSource();
             player.get(KingdomsComponent.class).getMember().addBlockPlace();
 
             if ( player.get(KingdomsComponent.class).getKingdom() != null ) {
                 player.get(KingdomsComponent.class).getKingdom().addBlockPlace();
             }
 
         }
 
     }
 
 
     @EventHandler (order = Order.MONITOR)
     public void onSignUpdateEventSellPlot( SignUpdateEvent event ) {
 
         if ( !event.getLines()[0].equalsIgnoreCase("[Sell]") ) {
             return;
         }
 
         // TODO Implement Sell Sign for Plots
 
     }
 
 
 }
