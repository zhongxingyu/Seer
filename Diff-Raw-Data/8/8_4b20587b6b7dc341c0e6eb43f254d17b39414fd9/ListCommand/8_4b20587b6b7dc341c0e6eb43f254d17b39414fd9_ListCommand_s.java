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
 
 import net.lankylord.booksave.BookSave;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.permissions.PermissionDefault;
 
 import java.util.List;
 
 /** @author LankyLord */
 public class ListCommand extends BookSaveCommand {
 
     public ListCommand(BookSave plugin) {
         super(plugin);
         this.setName("BookSave: List");
         this.setCommandUsage("/book list");
         this.setArgRange(0, 0);
         this.addKey("booksave list");
         this.addKey("bs list");
         this.addKey("book list");
         this.setPermission("booksave.list", "Allows this user to list books that are stored on the system", PermissionDefault.OP);
     }
 
     @Override
     public void runCommand(CommandSender sender, List<String> args) {
         if (!manager.getBookList().isEmpty()) {
             sender.sendMessage(colour1.toString() + ChatColor.UNDERLINE + "List of Books");
             sender.sendMessage("");
            for (String bookname : manager.getBookList()) {
                String booktitle = manager.getBookTitle(bookname);
                sender.sendMessage(colour1 + "Name: " + colour2 + bookname);
                sender.sendMessage(colour1 + "Title: " + colour2 + booktitle);
                 sender.sendMessage(colour2 + "-----");
             }
         } else
             sender.sendMessage(colour3 + "There are no books to list.");
     }
 }
