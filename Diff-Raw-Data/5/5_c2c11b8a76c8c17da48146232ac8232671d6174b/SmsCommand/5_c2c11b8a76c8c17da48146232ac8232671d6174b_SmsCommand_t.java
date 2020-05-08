 package net.robbytu.banjoserver.bungee.donate;
 
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.CommandSender;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.plugin.Command;
 import net.robbytu.banjoserver.bungee.Main;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 
 public class SmsCommand extends Command {
     public SmsCommand() {
         super("sms");
     }
 
     @Override
     public void execute(final CommandSender sender, final String[] args) {
         if(args.length == 0) {
             sender.sendMessage(ChatColor.RED + "Kies een tag om te doneren.");
             return;
         }
 
         if(args.length == 1) {
             Donation[] donations = DonateUtil.getDonationsForServer(Main.instance.getProxy().getPlayer(sender.getName()).getServer().getInfo().getName());
             Donation donation = null;
 
             for(Donation d : donations) if(d.tag.equalsIgnoreCase(args[0])) donation = d;
 
             if(donation == null) {
                 sender.sendMessage(ChatColor.RED + "Er is geen donatie in deze server mogelijk met de tag " + args[0]);
                 return;
             }
 
             sender.sendMessage("");
             sender.sendMessage(ChatColor.BOLD + "" + ChatColor.AQUA + donation.title + " - " + Main.instance.getProxy().getPlayer(sender.getName()).getServer().getInfo().getName() + " server");
            sender.sendMessage(ChatColor.GRAY + "Sms " + ChatColor.BOLD + ChatColor.WHITE + "BETAAL " + donation.code + ChatColor.RESET + ChatColor.GRAY + " naar " + ChatColor.BOLD + ChatColor.WHITE + "3010" + ChatColor.RESET + ChatColor.GRAY + ". Dit kost eenmalig €" + DonateUtil.prices.get(donation.code) + ". Voer de ontvangen code vervolgens in met " + ChatColor.RESET + ChatColor.WHITE + "/sms " + donation.tag + " [code]" + ChatColor.RESET + ChatColor.GRAY + " om je donatie te voltooien.");
             sender.sendMessage("");
         }
 
         if(args.length == 2) {
             Donation[] donations = DonateUtil.getDonationsForServer(Main.instance.getProxy().getPlayer(sender.getName()).getServer().getInfo().getName());
             Donation donation = null;
 
             for(Donation d : donations) if(d.tag.equalsIgnoreCase(args[0])) donation = d;
 
             if(donation == null) {
                 sender.sendMessage(ChatColor.RED + "Er is geen donatie in deze server mogelijk met de tag " + args[0]);
                 return;
             }
 
             sender.sendMessage(ChatColor.GRAY + "De code wordt gecontroleerd...");
 
             final Donation finalDonation = donation;
 
             Main.instance.getProxy().getScheduler().runAsync(Main.instance, new Runnable() {
                 public void run() {
                     try {
                        URLConnection connection = new URL("http://www.targetpay.com/api/sms-pincode?rtlo=" + Main.config.targetpay_rtlo + "&keyword=BETAAL+" + finalDonation.code + "&code=" + args[1] + "&shortcode=3010&country=31").openConnection();
                         BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 
                         String[] response = in.readLine().split(" ");
 
                         // All response codes can be found at https://www.targetpay.com/info/premium-sms-docu
                         if(response[0].equals("000")) {
                             // Succesful
                             DonateUtil.processSuccesfulDonation(sender.getName(), finalDonation);
 
                             for(ProxiedPlayer player : Main.instance.getProxy().getPlayers()) {
                                 if(!player.getName().equalsIgnoreCase(sender.getName())) {
                                     player.sendMessage(ChatColor.YELLOW + sender.getName() + " heeft gedoneerd voor " + finalDonation.title + " in de " + Main.instance.getProxy().getPlayer(sender.getName()).getServer().getInfo().getName() + " server");
                                     player.sendMessage(ChatColor.GRAY + "Je kan zelf ook doneren met het /doneer commando.");
                                 }
                             }
 
                             sender.sendMessage(ChatColor.GREEN + "De transactie is voltooid en je donatie wordt binnen enkele minuten verwerkt. Bedankt!");
                         }
                         else if(response[0].equals("103")) {
                             // No pincode specified
                             sender.sendMessage(ChatColor.RED + "Je hebt geen pincode opgegeven.");
                         }
                         else if(response[0].equals("104")) {
                             // Pincode length incorrect
                             sender.sendMessage(ChatColor.RED + "De pincode is niet lang genoeg: hij zou 6 cijfers moeten zijn.");
                         }
                         else if(response[0].equals("106")) {
                             // Pincode already checked or not paid
                             sender.sendMessage(ChatColor.RED + "De pincode die je opgaf is al eens gebruikt of is ongeldig.");
                         }
                         else {
                             // Targetpay internal errors
                             sender.sendMessage(ChatColor.RED + "Er is een fout opgetreden tijdens het verwerken van de transactie. Probeer het nog eens of neem contact op met een admin.");
                             Main.instance.getLogger().warning("Error while processing donation. Got error code " + response[0] + " from TargetPay.");
                         }
 
                     }
                     catch (Exception e) {
                         e.printStackTrace();
                         sender.sendMessage(ChatColor.RED + "Er is een fout opgetreden tijdens het controleren van je code. Neem contact op met een admin.");
                     }
                 }
             });
         }
     }
 }
