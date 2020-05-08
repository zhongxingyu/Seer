 package elxris.SpiceCraft.Commands;
 
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.MemoryConfiguration;
 import org.bukkit.entity.Player;
 
 import elxris.SpiceCraft.Objects.Warp;
 import elxris.SpiceCraft.Utils.Chat;
 import elxris.SpiceCraft.Utils.Econ;
 import elxris.SpiceCraft.Utils.Strings;
 
 public class WarpCommand extends Comando{
     private Configuration cache;
     private Econ econ;
     public WarpCommand() {
         this.cache = new MemoryConfiguration();
         this.econ = new Econ();
     }
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         Player jugador;
         if(sender instanceof Player){
             jugador = (Player) sender;
         }else{
             return true;
         }
         if(!jugador.hasPermission("spicecraft.tw")){
             mensaje(jugador, "alert.permission");
             return true;
         }
         // Crea un registro en el cache, por si se va a usar.
         if(!cache.isSet(getPath("p.%s.set", jugador))){
             cache.set(getPath("p.%s.set", jugador), false);
         }
         
         // Si no hay argumentos. Shortcut, o info.
         if(args.length < 1){
             // Busca si hay un warp personal para el jugador.
             // Si no, muestra la info.
             if(cache.getBoolean(getPath("p.%s.set", jugador))){
                 teleport(jugador, "p.%s.warp");
             }else{
                 chatInfo(jugador);
             }
         }else
         // Si hay un argumento. Busca el warp indicado. O si llam a destruir, destruye.
         if(args.length == 1){
             // Si existe el warp teleporta.
             if(cache.isSet(String.format("g.%s.set", args[0]))){
                 teleport(jugador, String.format("g.%s.warp", args[0]));
                 return true;
             }else if(isCommand("comm.tw.destroy", args[0])){
                 borrarWarp(jugador, getPath("p.%s", jugador));
                 return true;
             }else if(isCommand("comm.tw.list", args[0])){
                 mensaje(jugador, "tw.listHeader", getlistaWarps());
                 return true;
             }
             Chat.mensaje(jugador, "tw.noExist");
         } else
         
         // Si son dos argumentos crea el warp personal.
         if(args.length == 2){
             if(isCommand("comm.tw.new", args[0])){
                String warpName = getPath("p.%s", jugador);
                 validarNewWarp(warpName, args[1], "", jugador);
                 return true;
             }else if(isCommand("comm.tw.destroy", args[0])){
                 borrarWarp(jugador, "g."+args[1]);
                 return true;
             }
             Chat.mensaje(jugador, "alert.error");
             return true;
         } else
             
         // Si son tres argumentos crea el warp general.
         if (args.length == 3){
             if(isCommand("comm.tw.new", args[0])){
                 String warpName = String.format("g.%s", args[1]);
                 validarNewWarp(warpName, args[2], args[1], jugador);
                 return true;
             }
             Chat.mensaje(jugador, "alert.error");
         }
         return true;
     }
     private void teleport(Player jugador, String path){
         double precio = (double)getValue("tw.usePrice");
         if(!econ.cobrar(jugador, precio)){
             mensaje(jugador, "tw.noMoney");
         }else{
             jugador.teleport((Location)cache.get(getPath(path, jugador)));
             mensaje(jugador, "tw.teleported");
         }
     }
     private String getPath(String path, Player jugador) {
         return String.format(path, getPlayerName(jugador));
     }
     private String getPlayerName(Player jugador){
         return jugador.getName();
     }
     private void chatInfo(Player jugador){
         Chat.mensaje(jugador, "tw.info", econ.getPrecio((double)getValue("tw.price")));
     }
     private boolean crearWarp(Player jugador, int tiempo, String path){
         double precio = (double)getValue("tw.price")*tiempo;
         econ.setJugador(jugador);
         if(!econ.cobrar(jugador, precio)){
             mensaje(jugador, "tw.noMoney");
             return false;
         }
         Warp w = new Warp(jugador.getLocation(), jugador, tiempo, cache, path);
         Thread t = new Thread(w);
         t.start();
         cache.set(path+".warp", jugador.getLocation());
         cache.set(path+".owner", jugador.getName());
         cache.set(path+".date", System.currentTimeMillis());
         cache.set(path+".time", tiempo*60*1000);
         return true;
     }
     private void validarNewWarp(String path, String tiempo, String nombreWarp, Player jugador){
         // Si es entero.
         if(!isInteger(tiempo)){
             Chat.mensaje(jugador, "alert.noInteger");
             return;
         }
         // Si es ya existe.
         if(cache.isSet(path+".warp")){
             mensaje(jugador, "tw.exist");
             return;
         }
         int minutos = Integer.parseInt(tiempo);
         //Si est fuera de rango.
         if(!(minutos >= (int)getValue("tw.minTime") && minutos <= (int)getValue("tw.maxTime"))){
             if(!jugador.hasPermission("spicecraft.tw.noMaxTime")){
                 mensaje(jugador, "tw.timeLimit", (int)getValue("tw.minTime"), (int)getValue("tw.maxTime"));
                 return;                
             }
         }
         // Asegura que exista el numero de warps.
         if(!cache.isSet("user."+getPlayerName(jugador))){
             cache.set("user."+getPlayerName(jugador), 0);
         }
         // Si excede a los mximos por usuario.
         if((int)getValue("tw.maxPerUser") <= cache.getInt("user."+getPlayerName(jugador))){
             if(!jugador.hasPermission("spicecraft.tw.noWarpLimit")){
                 mensaje(jugador, "tw.warpLimit");
                 return;                
             }
         }
         if(crearWarp(jugador, minutos, path)){            
             mensaje(jugador, "tw.created", minutos, nombreWarp);
             // Aadir el warp a la lista de warps
             if(nombreWarp != ""){
                 addListaWarps(nombreWarp);                
             }
         }
     }
     private void borrarWarp(Player jugador, String path){
        if(cache.isSet(path+".owner")){
             if(cache.getString(path+".owner").contentEquals(jugador.getName())
                     || jugador.hasPermission("spicecraft.tw.master")){
                 cache.set(path, null);
                 cache.set("user."+jugador.getName(), cache.getInt("user."+jugador.getName())-1);
                 Chat.mensaje(jugador, "tw.destroyed");
                 return;
             }else{
                 mensaje(jugador, "tw.noOwner");
                 return;
             }
         }
         mensaje(jugador, "tw.noExist");
     }
     
     private void addListaWarps(String s){
         List<String> l = cache.getStringList("list");
         l.add(s);
         cache.set("list", l);
     }
     private String getlistaWarps(){
         String r = "";
         for(String s: cache.getStringList("list")){
             if(cache.isSet("g."+s)){
                 r += String.format(
                         Strings.getString("tw.listItem")+"\n", 
                         s,
                         cache.getString("g."+s+".owner"),
                         (cache.getLong("g."+s+".date")+cache.getLong("g."+s+".time")-System.currentTimeMillis())/1000
                         );
             }
         }
         return r;
     }
 
 }
