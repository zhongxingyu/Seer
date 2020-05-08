 //    Mimamememu is launcher for M.A.M.E and other emulators.
 //    Copyright (C) 2013 Adrián Romero Corchado.
 //    https://github.com/adrianromero/mimamememu
 //
 //    This file is part of Mimamememu
 //
 //    Mimamememu is free software: you can redistribute it and/or modify
 //    it under the terms of the GNU General Public License as published by
 //    the Free Software Foundation, either version 3 of the License, or
 //    (at your option) any later version.
 //
 //    Mimamememu is distributed in the hope that it will be useful,
 //    but WITHOUT ANY WARRANTY; without even the implied warranty of
 //    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //    GNU General Public License for more details.
 //
 //    You should have received a copy of the GNU General Public License
 //    along with Mimamememu.  If not, see <http://www.gnu.org/licenses/>.
 
 package com.adr.mmmmm.platform;
 
 import com.adr.mmmmm.GamesItem;
 import com.adr.mmmmm.Platform;
 import java.awt.image.BufferedImage;
 import java.util.Collections;
 import java.util.List;
 
 /**
  *
  * @author adrian
  */
 public class SNESCommand implements Platform {
 
     @Override
     public String getPlatformName() {
         return "SNES";
     }
 
     @Override
     public String getPlatformTitle() {
         return "Super Nintendo Entertainment System";
     }
 
     @Override
     public String[] getCommand(GamesItem item) {
        return new String[]{"zsnes", "-m", "-v", "18", "/home/adrian/.mimamememu/_GENERAL/" + item.getName() + ".smc"};
     }
 
     @Override
     public BufferedImage getDefaultImage() {
         return null;
     }
 
     @Override
     public BufferedImage getDefaultCabinet() {
         return null;
     }
 
     @Override
     public List<GamesItem> getGames() {
         return Collections.EMPTY_LIST;
     }   
 }
