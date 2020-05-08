 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of FifthElement.
  * 
  * FifthElement is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * FifthElement is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with FifthElement.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.FifthElement.manager;
 
 import java.util.Map;
 
 import org.bukkit.entity.Player;
 
 import de.minestar.FifthElement.core.Core;
 import de.minestar.FifthElement.data.Bank;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 
 public class BankManager {
 
     private Map<String, Bank> bankMap;
 
     public BankManager() {
         loadBanks();
     }
 
     // **************************
     // LOADING AND INITIALIZATION
     // **************************
 
     private void loadBanks() {
         bankMap = Core.dbHandler.loadBanks();
         ConsoleUtils.printInfo(Core.NAME, "Loaded " + bankMap.size() + " Banks.");
     }
 
     public Bank getBank(String playerName) {
         return bankMap.get(playerName.toLowerCase());
     }
 
     // **************************
     // MANIPULATE CURRENT BANKS
     // **************************
 
     public void createBank(Player creator, String bankOwner) {
         Bank bank = new Bank(creator.getLocation(), bankOwner);
        bankMap.put(bankOwner.toLowerCase(), bank);
         Core.dbHandler.addBank(bank);
     }
 
     public void moveBank(Player player, Bank bank) {
         bank.updateLocation(player.getLocation());
         Core.dbHandler.updateBankLocation(bank);
     }
 }
