 /* Copyright 2013 Kevin Seiden. All rights reserved.
 
  This works is licensed under the Creative Commons Attribution-NonCommercial 3.0
 
  You are Free to:
     to Share: to copy, distribute and transmit the work
     to Remix: to adapt the work
 
  Under the following conditions:
     Attribution: You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
     Non-commercial: You may not use this work for commercial purposes.
 
  With the understanding that:
     Waiver: Any of the above conditions can be waived if you get permission from the copyright holder.
     Public Domain: Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
     Other Rights: In no way are any of the following rights affected by the license:
         Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
         The author's moral rights;
         Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.
 
  Notice: For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
  http://creativecommons.org/licenses/by-nc/3.0/
  */
 
 package io.github.alshain01.Flags.area;
 
 import io.github.alshain01.Flags.Flags;
 import me.ryanhamshire.GriefPrevention.Claim;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 
 public class GriefPreventionClaim78 extends GriefPreventionClaim implements	Subdivision {
 
 	/**
 	 * Creates an instance of GriefPreventionClaim78 based on a Bukkit Location
 	 * 
 	 * @param location
 	 *            The Bukkit location
 	 */
 	public GriefPreventionClaim78(Location location) {
 		super(location);
 	}
 
 	/**
 	 * Creates an instance of GriefPreventionClaim78 based on a claim ID
 	 * 
 	 * @param ID
 	 *            The claim ID
 	 */
 	public GriefPreventionClaim78(long ID) {
 		super(ID);
 	}
 
 	/**
 	 * Creates an instance of GriefPreventionClaim78 based on a claim ID and
 	 * sub-claimID
 	 * 
 	 * @param ID
 	 *            The claim ID
 	 * @param subID
 	 *            The sub-claim ID
 	 */
 	public GriefPreventionClaim78(long ID, long subID) {
 		super(ID);
 		this.claim = (claim == null) ? null : claim.getSubClaim(subID);
 	}
 
 	/**
 	 * 0 if the the claims are the same 
 	 * -1 if the claim is a subdivision of the provided claim. 
 	 * 1 if the claim is a parent of the provided claim.
 	 * 2 if they are "sister" subdivisions. 3 if they are completely unrelated.
 	 * 
 	 * @return The value of the comparison.
 	 */
 	@Override
 	public int compareTo(Area a) {
 		if (!(a instanceof GriefPreventionClaim78)) {
 			return 3;
 		}
 		
 		Claim testClaim = ((GriefPreventionClaim78)a).getClaim();
 		if(claim.equals(testClaim)) {
 			return 0;
		} else if (claim.parent != null && claim.parent.equals(testClaim)) {
 			return -1;
		} else if (testClaim.parent != null && testClaim.parent.equals(claim)) {
 			return 1;
 		} else if (claim.parent != null && claim.parent.equals(testClaim.parent)) {
 			return 2;
 		}
 		return 3;
 	}
 
 	@Override
 	public String getSystemSubID() {
 		return claim != null && claim.parent != null ? String.valueOf(claim.getSubClaimID()) : null;
 	}
 
 	@Override
 	public org.bukkit.World getWorld() {
 		return Bukkit.getServer().getWorld(claim.getClaimWorldName());
 	}
 
 	@Override
 	public boolean isInherited() {
         return claim != null && claim.parent != null && Flags.getDataStore().readInheritance(this);
 	}
 
 	@Override
 	public boolean isSubdivision() {
 		return claim != null && claim.parent != null;
 	}
 
 	@Override
 	public void setInherited(Boolean value) {
 		if (claim == null || claim.parent == null) {
 			return;
 		}
 
 		Flags.getDataStore().writeInheritance(this, value);
 	}
 
 	@Override
 	public boolean isParent(Area area) {
         return area instanceof GriefPreventionClaim78 && claim.parent != null
             && claim.parent.equals(((GriefPreventionClaim78)area).getClaim());
 	}
 
 	@Override
 	public Area getParent() {
 		if(claim.parent == null) { return null; }
 		return new GriefPreventionClaim78(claim.getID());
 	}
 }
