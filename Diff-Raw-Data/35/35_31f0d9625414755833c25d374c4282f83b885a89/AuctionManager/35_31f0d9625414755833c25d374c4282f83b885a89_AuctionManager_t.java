 package de.paralleluniverse.Faithcaio.AuctionHouse;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Stack;
 import org.bukkit.inventory.ItemStack;
 
 
 /**
  * Manages all Auctions
  *
  * @author Faithcaio
  */
 public class AuctionManager
 {
     private static AuctionManager instance = null;
 
     public final List<Auction> auctions;
     public final Stack<Integer> freeIds;
     private static final AuctionHouse plugin = AuctionHouse.getInstance();
     private static final AuctionHouseConfiguration config = plugin.getConfigurations();
     
     private AuctionManager()
     {
        int maxAuctions = config.auction_maxAuctions;
        if (!(maxAuctions > 0 )) maxAuctions = 1;
         this.auctions = new ArrayList<Auction>();
         this.freeIds = new Stack<Integer>();
        for (int i = maxAuctions; i > 0; --i)
         {
            this.freeIds.push(i); 
         }
     }
 
     public static AuctionManager getInstance()
     {
         if (instance == null)
         {
             instance = new AuctionManager();
         }
         return instance;
     }
     
     public Auction getAuction(int id) //Get Auction with ID
     {
         return this.auctions.get(id);
     }
 
     public List<Auction> getAuctionItems(ItemStack item) //Get all Auctions with item
     {
         ArrayList<Auction> auctionlist = new ArrayList<Auction>() {};
         for (int i = 1;i == this.auctions.size();i++)
         {
             if (this.auctions.get(i).item == item)
             { auctionlist.add( this.getAuction(i) ); }
         } 
         return auctionlist;    
     }
     
     public List<Auction> getEndingAuctions(int min) //Get soon Ending Auctions
     {
         ArrayList<Auction> auctionlist = new ArrayList<Auction>() {};
         for (int i = 1;i == this.auctions.size();i++)
         {
             if (this.auctions.get(i).auctionEnd - System.currentTimeMillis() <= 1000 * 60 * min)
     
             { auctionlist.add( this.getAuction(i) ); }
         }
         Collections.sort(auctionlist, new Comparator()
            {   
                 public int compare(Object a1,Object a2){
                 if (((Auction)a1).auctionEnd <= ((Auction)a2).auctionEnd) return 1;
                 //else
                 return -1;
             } 
         }); 
         return auctionlist;
     }
     
         public boolean cancelAuction(Auction auction)
     {
         //TODO
         this.freeIds.push(auction.id);        
         auction.abortAuction();
         return true;   
     }
     
     public boolean addAuction(Auction auction)
     {
         //Rechte zum Starten ?
         //return false;
         //TODO
         /*
         if (this.auctions.size() >= config.auction_maxAuctions) {return false;}
         Random generator = new Random();
         do { id = generator.nextInt(config.auction_maxAuctions); }    
         while (this.auctions.get(id)!= null);
         */
         auction.id = this.freeIds.peek();
         this.freeIds.pop();
         this.auctions.add(auction);
         return true;   
     }
     public boolean addMultiAuction(Auction auction, int quantity)
     {  
         //Rechte zum Starten ?
         //return false; 
         for (int i=0; i < quantity; ++i)
         {
           this.addAuction(auction);    
         }
         return true;   
     }
 }
