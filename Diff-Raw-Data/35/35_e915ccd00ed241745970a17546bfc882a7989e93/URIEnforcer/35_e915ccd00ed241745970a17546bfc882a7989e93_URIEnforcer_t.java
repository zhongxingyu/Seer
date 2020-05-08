/*
 * Copyright (c) 2013 cedeel.
 * All rights reserved.
 * 
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS''
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 package be.darnell.urienforcer;
 
 import java.io.IOException;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mcstats.MetricsLite;
 
 public class URIEnforcer extends JavaPlugin implements Listener {
     
     private static final int DEFAULT_PORT = 25565;
     private String ip;
     
     @Override
     public void onEnable() {
         getServer().getPluginManager().registerEvents(this, this);
         
         saveDefaultConfig();
         FileConfiguration config = getConfig();
         ip = config.getString("ip", "localhost");
         if (config.getBoolean("mcstats", true))
            setupMetrics();
     }
     
     @EventHandler
     public void onLogin(PlayerLoginEvent e) {
         String desiredname = ip + ":" + getServer().getPort();
         if (!e.getHostname().equalsIgnoreCase(desiredname)) {
             if (getServer().getPort() != DEFAULT_PORT)
                 e.disallow(Result.KICK_OTHER, "\u00A77Please log in using \u00A7F" + desiredname);
             else
                 e.disallow(Result.KICK_OTHER, "\u00A77Please log in using \u00A7F" + ip);
            getLogger().info(e.getPlayer().getName() + " tried to log in using " + e.getHostname());
         }
     }
     
    private void setupMetrics() {
         try {
             MetricsLite metrics = new MetricsLite(this);
             metrics.start();
         } catch (IOException e) {
             System.out.println("Failed to submit stats.");
         }
     }
 }
