 package de.paralleluniverse.Faithcaio.AuctionHouse;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author Faithcaio
  */
 public class Bidder {
    public final Player player;
    public final ArrayList<Auction> activeBids;
    
    private static final Map<Player, Bidder> bidderInstances = new HashMap<Player, Bidder>();
    
    public Bidder(Player player)
    {
        this.player = player;  
        this.activeBids = new ArrayList<Auction>();
    }
    
      public static Bidder getInstance(Player player)
     {
         Bidder instance;
        if (bidderInstances.isEmpty()) instance = null;
        else instance = bidderInstances.get(player);
         if (instance == null)
          bidderInstances.put(player, new Bidder(player));
        instance = bidderInstances.get(player);
         return instance;
     }
     
     public List<Auction> getLeadingAuctions(Player player)
     {
         List<Auction> auctionlist = new ArrayList<Auction>();
         final int length = this.activeBids.size();
         for (int i = 0; i < length; ++i)
         {
             if (this.activeBids.get(i).bids.peek().getBidder() == player)
             {
                 auctionlist.add(this.activeBids.get(i));
             }
         } 
         return auctionlist;
     }
     
     
     public List<Auction> getAuctions() //Get all Auctions with player involved
     {
         return activeBids;
     }
     
     public List<Auction> getAuctions(Player player) //Get all Auctions started by player
     {
         ArrayList<Auction> auctionlist = new ArrayList<Auction>() {};  
         for (int i = 1;i == this.activeBids.size();i++)
         {
             if (this.activeBids.get(i).owner == player)
             { auctionlist.add( this.activeBids.get(i) ); }     
         }
         return auctionlist;
     }
   
     public Bidder addAuction(Auction auction)
     {
         this.activeBids.add(auction);
         return this;
     }
    
 }
