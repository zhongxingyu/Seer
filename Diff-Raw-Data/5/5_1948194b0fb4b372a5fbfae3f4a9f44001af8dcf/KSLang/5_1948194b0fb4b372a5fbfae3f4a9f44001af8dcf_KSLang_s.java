 package de.bdh.ks;
 
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class KSLang 
 {
 	HashMap<String,HashMap<String,String>> lng = new HashMap<String,HashMap<String,String>>();;
 	String lang = "";
 	public KSLang()
 	{
 		HashMap<String,String> en = new HashMap<String,String>();
 		HashMap<String,String> de = new HashMap<String,String>();
 		
 		en.put("usage", "USAGE: /auction SELL/BUY/REQUEST/DETAIL/LIST/SIGN/LISTREQUESTS/COLLECT/ABORT/ABORTREQUEST/OVERVIEW/OVERVIEWREQUESTS");
 		en.put("usage_abort","USAGE: /auction abort ID - you can get the id by using list");
 		en.put("usage_request", "USAGE: /auction request (Item) MAXPRICE AMOUNT");
 		en.put("usage_buy","USAGE: /auction buy (Item (MAXPRICE)) AMOUNT");
 		en.put("usage_detail", "USAGE: /auction detail Item");
 		en.put("usage_sign", "USAGE: /auction sign REQUEST/OFFER ID - you can get the id by using list/listrequest");
 		en.put("usage_overview", "USAGE: /auction overview PAGE");
 		en.put("usage_sell","USAGE: /auction sell Item PRICEPERBLOCK (AMOUNT) OR /auction sell PRICE_EACH for Item in Hand");
 		en.put("usage_abortrequest","USAGE: /auction abortrequest ID - you can get the id by using list");
 		en.put("err_num","$1 must be numeric");
 		en.put("rem_success","Your auction has been cancelled. You can pick it up at the auction house");
 		en.put("err_invalid_id", "This ID was invalid or you dont have the permissions to do that");
		en.put("rem_rec_suc","Your request has been cancelled. You can pick it up at the auction house");
 		en.put("err_nosale","Nothing for sale");
 		en.put("noqsell","Unable to quicksell item - no default price");
 		en.put("header_list","You've $1 $2. Page: $3 of $4");
 		en.put("err_noreq","Nothing requested");
 		en.put("err_noperm","You're not allowed to do this");
 		en.put("err_to_ah","You've to go to an auction house to do this");
 		en.put("err_toohigh","$1 is too high");
 		en.put("err_nodeliver", "There is nothing for delivery");
 		en.put("err_block_404","Item not found");
 		en.put("err_block","Item is invalid");
 		en.put("err_full_inv", "Your inventory is full");
 		en.put("err_notrade", "Cannot be traded");
 		en.put("err","Something went wrong");
 		en.put("err_nomoney","You don't have enough money");
 		en.put("err_nooffer","There is no offer which fulfills your options");
 		en.put("err_nomoney_fee", "You cannot afford the fee of $1 $a");
 		en.put("suc_offer", "Success. You're offering $1 Items for $2 $a");
 		en.put("suc_fee_paid","You've paid an auction-fee of $1 $a");
 		en.put("suc_bought","You've bought the amount you wanted");
 		en.put("suc_bought_part","You've bought $1 of $2");
 		en.put("suc_req", "You've requested $1 items for $2 $a");
 		en.put("suc_rec_item","You've received $1 items");
 		en.put("suc_rec_money","You've received $1 $a");
 		en.put("suc_req_part","You've only enough money for $1 items for $2 $a");
 		en.put("suc_sign","Now just destroy the sign you want to use");
 		en.put("info", "Auction details about $1");
 		en.put("goto_ah", "You can collect some items in the auction house");
 		en.put("collect", "You can collect some items by entering /auction collect");
 		en.put("amount_sale", "Amount for sale: $1");
 		en.put("default_price", "Suggested retail price: $1");
 		en.put("average_price", "Average price: $1");
 		en.put("offer","Offer: $1 for $2 $a each");
 		en.put("request","Request: $1 for $2 $a each");
 		en.put("err_noitem","You dont own this item");
 		en.put("welcome","Welcome to KrimSale - worldofminecraft.de");
 		en.put("suc_sign_com","Success. The sign has been created");
 		en.put("req_info","Your request is valid for 14 days. If noone offers this item for your price, you'll get your money back");
 		
 		de.put("usage", "NUTZUNG: /auction SELL/BUY/REQUEST/DETAIL/LIST/SIGN/LISTREQUESTS/COLLECT/ABORT/ABORTREQUEST/OVERVIEW/OVERVIEWREQUESTS");
 		de.put("usage_abort","NUTZUNG: /auction abort ID - die ID erhaelst du mittels List");
 		de.put("usage_request", "NUTZUNG: /auction request (Item) MAXPREIS MENGE");
 		de.put("usage_buy","NUTZUNG: /auction buy (Item (MAXPREIS)) MENGE");
 		de.put("usage_detail", "NUTZUNG: /auction detail Itemname");
 		de.put("usage_sign", "NUTZUNG: /auction sign REQUEST/OFFER ID - die ID erhaelst du mittels List/Listrequests");
 		de.put("usage_overview", "NUTZUNG: /auction overview [SEITE]");
 		de.put("usage_sell","NUTZUNG: /auction sell Item PREISPROBLOCK (MENGE) oder /auction sell PREIS_PRO fuer Gegenstand in der Hand");
 		de.put("usage_abortrequest","NUTZUNG: /auction abortrequest ID - die ID erhaelst du mittels Listrequest");
 		de.put("err_num","$1 muss eine Nummer sein");
 		en.put("noqsell","Kann nicht qsell'en. Es ist kein Standardpreis bekannt");
 		de.put("rem_success","Deine Auktion wurde abgebrochen. Du kannst deine Gegenstaende im Auktionshaus abholen");
 		de.put("err_invalid_id", "Diese ID ist ungueltig oder du hast keine Berechtigung dies zu tun");
		de.put("rem_rec_suc","Deine Anfrage wurde abgebrochen. Du kannst dein Geld im Auktionshaus abholen");
 		de.put("err_nosale","Es wurden keine Gegenstaende zum Verkauf gefunden");
 		de.put("header_list","Du hast $1 $2. Seite: $3 von $4");
 		de.put("err_noreq","Es wurden keine Gegenstaende zum Ankauf gefunden");
 		de.put("err_noperm","Du darfst dies nicht tun");
 		de.put("err_to_ah","Du musst in ein Auktionshaus gehen");
 		de.put("default_price", "Preisempfehlung: $1");
 		de.put("err_toohigh","$1 ist zu hoch");
 		de.put("suc_req_part","Dein Geld hat nur fuer $1 Items fuer $2 $a gereicht");
 		de.put("err_nodeliver", "Dein Postfach ist leer");
 		de.put("err_block_404","Item nicht gefunden");
 		de.put("err_block","Item ist ungueltig");
 		de.put("err_full_inv", "Dein Inventar ist voll");
 		de.put("err_notrade", "Kann nicht verkauft werden");
 		de.put("err","Etwas ist schiefgelaufen");
 		de.put("err_noitem","Du besitzt diesen Gegenstand nicht");
 		de.put("err_nomoney","Du hast nicht genug Geld");
 		de.put("err_nooffer","Es gibt keine Angebote die deinen Anforderungen entsprechen");
 		de.put("err_nomoney_fee", "Du kannst dir die Gebuehren von $1 $a nicht leisten");
 		de.put("suc_offer", "Erfolgreich eingestellt: $1 Items fuer $2 $a");
 		de.put("suc_fee_paid","Du hast $1 $a an Gebuehren bezahlt");
 		de.put("suc_bought","Du hast alles Gekauft, was du haben wolltest");
 		de.put("suc_bought_part","Du hast $1 von $2 gekauft");
 		de.put("suc_req", "Du hast $1 Gegenstaende zum Ankauf von $2 $a eingetragen");
 		de.put("suc_rec_item","Du hast $1 Items erhalten");
 		de.put("suc_rec_money","Du hast $1 $a erhalten");
 		de.put("suc_sign","Jetzt zerstoere das Schild, welches du nutzen willst");
 		de.put("suc_sign_com","Erfolgreich. Das Schild wurde angelegt");
 		de.put("info", "Auktionsinformationen ueber $1");
 		de.put("average_price", "Durchschnittspreis: $1");
 		de.put("goto_ah", "Einige Waren koennen im Auktionshaus abgeholt werden");
 		de.put("collect", "Du kannst einige Waren via /auction collect empfangen");
 		de.put("amount_sale", "Menge zum Verkauf: $1");
 		de.put("offer","Angebot: $1 fuer je $2 $a");
 		de.put("request","Anfrage: $1 fuer je $2 $a");
 		de.put("welcome","Willkommen bei KrimSale - worldofminecraft.de");
 		de.put("req_info","Deine Anfrage ist fuer 14 Tage gueltig. Wenn dies nicht erfolgreich ist bekommst du dein Geld wieder");
 		
 		this.lng.put("en", en);
 		this.lng.put("de", de);
 		
 		if(this.lng.get(configManager.lang) == null)
 			this.lang = "en";
 		else
 			this.lang = configManager.lang;
 	}
 	
 
 	public void msg(Player p, String el)
 	{
 		this.msg((Player)p, el, new Object[]{});
 	}
 	
 	public void msg(CommandSender p, String el)
 	{
 		this.msg((Player)p, el, new Object[]{});
 	}
 	
 	public void msg(CommandSender p, String el, Object[] args)
 	{
 		this.msg((Player)p, el, args);
 	}
 	
 	public void msg(Player p,String el, Object[] args)
 	{
 		String str = el;
 		if(this.lng.get(this.lang).get(el) != null)
 			str = this.lng.get(this.lang).get(el);
 		
 		str = str.replace("$a", Main.econ.currencyNamePlural());
 		
 		if(args != null && args.length > 0)
 		{
 			for (int i = 1; i < args.length+1; i++) 
 			{
 				String tmp = "";
 				if(args[i-1] instanceof String)
 					tmp = (String)args[i-1];
 				else if(args[i-1] instanceof Integer)
 					tmp = new Integer((Integer)args[i-1]).toString();
 				
 				str = str.replace("$" + i, tmp);
 			}
 			str = str.replace("$$", "$");
 		}
 		
 		p.sendMessage(ChatColor.GOLD + str);
 	}
 }
