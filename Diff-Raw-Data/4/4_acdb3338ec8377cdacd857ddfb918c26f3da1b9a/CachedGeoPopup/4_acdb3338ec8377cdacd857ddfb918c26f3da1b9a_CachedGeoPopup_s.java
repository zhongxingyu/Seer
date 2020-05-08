 /*
  * This file is part of Aqualock.
  *
  * Copyright (c) 2012, AlmuraDev <http://www.almuramc.com/>
  * Aqualock is licensed under the Almura Development License.
  *
  * Aqualock is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Aqualock is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License. If not,
  * see <http://www.gnu.org/licenses/> for the GNU General Public License.
  */
 package com.almuramc.aqualock.bukkit.display;
 
 import org.getspout.spoutapi.gui.GenericPopup;
 
 import org.bukkit.Location;
 
 public abstract class CachedGeoPopup extends GenericPopup {
 	public abstract boolean isOpen();
 
 	public abstract void setOpen(boolean open);
 
 	public abstract Location getLocation();
 
 	public abstract void setLocation(Location location);
 
 	public void onClose() {
		getPlayer().getMainScreen().removeWidget(this);
		getPlayer().closeActiveWindow();
 	}
 }
