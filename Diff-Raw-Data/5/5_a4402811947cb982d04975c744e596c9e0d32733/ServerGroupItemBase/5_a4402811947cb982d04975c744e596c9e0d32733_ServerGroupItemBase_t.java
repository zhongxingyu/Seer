 /*
  * Copyright (c) 2006-2014 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.serverlists;
 
 import com.dmdirc.config.profiles.Profile;
 import com.dmdirc.config.profiles.ProfileManager;
 import com.dmdirc.interfaces.config.ConfigProvider;
 
import com.google.common.collect.Iterables;

 /**
  * Abstract base class for {@link ServerGroupItem}s.
  *
  * @since 0.6.4
  */
 public abstract class ServerGroupItemBase implements ServerGroupItem {
 
     /** Manager to get profiles from */
     private final ProfileManager profileManager;
     /** Whether or not this item has been modified. */
     private boolean modified;
     /** The name of the item. */
     private String name;
     /** The name of the profile to use. */
     private String profile;
 
     public ServerGroupItemBase(final ProfileManager profileManager) {
         this.profileManager = profileManager;
     }
 
     @Override
     public boolean isModified() {
         return modified;
     }
 
     @Override
     public void setModified(final boolean isModified) {
         this.modified = isModified;
     }
 
     @Override
     public String getName() {
         return name;
     }
 
     @Override
     public void setName(final String name) {
         setModified(true);
         this.name = name;
     }
 
     @Override
     public String getPath() {
         if (getParent() != null) {
             return getParent().getPath() + " → " + getName();
         }
         return getName();
     }
 
     @Override
     public String getProfile() {
         return profile;
     }
 
     @Override
     public void setProfile(final String profile) {
         setModified(true);
         this.profile = profile;
     }
 
     /**
      * Returns the parent group of this item, or <code>null</code> if the item is a root group.
      *
      * @return This item's parent group
      */
     protected abstract ServerGroup getParent();
 
     /**
      * Returns the {@link ConfigProvider} which corresponds to this server's desired profile.
      *
      * @return This server's profile identity
      */
     protected Profile getProfileIdentity() {
         if (profile != null) {
             for (Profile identity : profileManager.getProfiles()) {
                 if (profile.equals(identity.getName())) {
                     return identity;
                 }
             }
         }
 
         if (getParent() == null) {
            return profileManager.getDefault();
         } else {
             return getParent().getProfileIdentity();
         }
     }
 
 }
