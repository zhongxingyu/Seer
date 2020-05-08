 /*
  * Copyright (c) 2013, LankyLord
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * * Redistributions of source code must retain the above copyright notice, this
  *   list of conditions and the following disclaimer.
  * * Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the documentation
  *   and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package net.lankylord.booksave.commands;
 
 import java.util.List;
 import net.lankylord.booksave.BookSave;
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.PermissionDefault;
 
 /**
  *
  * @author LankyLord
  */
 public class GiveCommand extends BookSaveCommand {
 
     public GiveCommand(BookSave plugin) {
         super(plugin);
         this.setName("BookSave: Give");
         this.setCommandUsage("/book give [PlayerName] <BookName>");
         this.setArgRange(1, 2);
         this.addKey("booksave give");
         this.addKey("bs give");
         this.addKey("book give");
         this.setPermission("booksave.give", "Allows this user to retrieve saved books", PermissionDefault.OP);
     }
 
     @Override
     public void runCommand(CommandSender sender, List<String> args) {
         Player receiver = null;
         String title = null;
         if (args.size() == 2) {
             receiver = Bukkit.getPlayer(args.get(0));
             title = args.get(1);
         } else if (sender instanceof Player && args.size() != 2) {
             receiver = (Player) sender;
             title = args.get(0);
         }
        if (plugin.getManager().giveBookToPlayer(receiver, title))
             sender.sendMessage(colour1 + "You have given " + colour2 + receiver.getName() + colour1 + " the book " + colour2
                     + title);
     }
 }
