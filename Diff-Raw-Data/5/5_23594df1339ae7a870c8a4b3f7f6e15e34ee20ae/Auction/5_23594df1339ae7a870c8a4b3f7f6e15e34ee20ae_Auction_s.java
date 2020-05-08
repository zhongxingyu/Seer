 package de.paralleluniverse.Faithcaio.AuctionHouse;
 
 import java.util.Stack;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * Represents an auction
  *
  * @author Anselm
  */
 public class Auction
 {
     public final int id;
     public final ItemStack item;
     public final Player owner;
     public final long auctionEnd;
     public final Stack<Bid> bids;
     
     public Auction(int id, ItemStack item, Player owner, long auctionEnd)
     {
         this.id = id;
         this.item = item;
         this.owner = owner;
         this.auctionEnd = auctionEnd;
         this.bids = new Stack<Bid>();
     }
     
    public boolean bid(Player bidder,double amount)
     {
        if (amount<this.bids.peek().amount)
         {
             return false;
         }
         this.bids.push(new Bid(bidder, amount));
         return true; 
     }
 }
