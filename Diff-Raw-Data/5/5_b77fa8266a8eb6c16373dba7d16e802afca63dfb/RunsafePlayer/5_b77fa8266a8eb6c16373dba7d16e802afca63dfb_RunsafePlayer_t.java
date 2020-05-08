 package no.runsafe.framework.minecraft.player;
 
 import no.runsafe.framework.api.command.ICommandExecutor;
 import no.runsafe.framework.api.hook.*;
 import no.runsafe.framework.internal.HookEngine;
 import no.runsafe.framework.internal.wrapper.player.BukkitPlayer;
 import no.runsafe.framework.minecraft.Item;
 import no.runsafe.framework.minecraft.RunsafeLocation;
 import no.runsafe.framework.minecraft.RunsafeWorld;
 import no.runsafe.framework.minecraft.Universe;
 import no.runsafe.framework.minecraft.chunk.RunsafeChunk;
 import no.runsafe.framework.minecraft.event.player.RunsafeOperatorEvent;
 import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
 import no.runsafe.framework.text.ChatColour;
 import org.bukkit.GameMode;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.joda.time.DateTime;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 public class RunsafePlayer extends BukkitPlayer implements ICommandExecutor
 {
 	public RunsafePlayer(Player toWrap)
 	{
 		super(toWrap);
 	}
 
 	public RunsafePlayer(OfflinePlayer toWrap)
 	{
 		super(toWrap);
 	}
 
 	public String getPrettyName()
 	{
 		List<IPlayerNameDecorator> decoratorHooks = HookEngine.hookContainer.getComponents(IPlayerNameDecorator.class);
 		String name = getName();
 		if (!decoratorHooks.isEmpty())
 			for (IPlayerNameDecorator decorator : decoratorHooks)
 				name = decorator.DecorateName(this, name);
 		return name;
 	}
 
 	public String getLastSeen(RunsafePlayer checker)
 	{
 		List<IPlayerSeen> seenHooks = HookEngine.hookContainer.getComponents(IPlayerSeen.class);
 		if (!seenHooks.isEmpty())
 			return seenHooks.get(0).GetLastSeen(this, checker);
 
 		return null;
 	}
 
 	public boolean isNew()
 	{
 		List<IPlayerSessionDataProvider> dataHooks = HookEngine.hookContainer.getComponents(IPlayerSessionDataProvider.class);
 		if (this.isOnline() || dataHooks.isEmpty())
 			return false;
 
 		for (IPlayerSessionDataProvider provider : dataHooks)
 			if (provider.IsFirstSession(this))
 				return true;
 
 		return false;
 	}
 
 	public void OP()
 	{
 		basePlayer.setOp(true);
 		new RunsafeOperatorEvent(this, true).Fire();
 	}
 
 	public void deOP()
 	{
 		basePlayer.setOp(false);
 		new RunsafeOperatorEvent(this, false).Fire();
 	}
 
 	public boolean isSurvivalist()
 	{
 		return player != null && player.getGameMode().equals(GameMode.SURVIVAL);
 	}
 
 	public boolean isCreative()
 	{
 		return player != null && player.getGameMode().equals(GameMode.CREATIVE);
 	}
 
 	public boolean isAdventurer()
 	{
 		return player != null && player.getGameMode().equals(GameMode.ADVENTURE);
 	}
 
 	public void teleport(RunsafeWorld world, double x, double y, double z)
 	{
 		RunsafeLocation target = new RunsafeLocation(world, x, y, z);
 		RunsafeChunk chunk = target.getChunk();
 		if (!chunk.isLoaded())
 			chunk.load();
 		teleport(target);
 	}
 
 	public HashMap<String, String> getData()
 	{
 		List<IPlayerDataProvider> dataHooks = HookEngine.hookContainer.getComponents(IPlayerDataProvider.class);
 		HashMap<String, String> results = getBasicData();
 		for (IPlayerDataProvider provider : dataHooks)
 		{
 			HashMap<String, String> data = provider.GetPlayerData(this);
 			if (data != null)
 				results.putAll(data);
 		}
 		return results;
 	}
 
 	public HashMap<String, String> getBasicData()
 	{
 		LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();
 		if (isOnline())
 		{
 			data.put("game.ip",
 				String.format("%s [%s]",
 					player.getAddress().getAddress().getHostAddress(),
 					player.getAddress().getHostName()
 				)
 			);
 			data.put("game.mode", player.getGameMode().name());
 			data.put("game.flying", player.isFlying() ? "true" : "false");
 			data.put("game.health", String.format("%d/%d", getHealth(), getMaxHealth()));
 		}
 		data.put("game.experience", String.format("%.1f", getXP()));
 		data.put("game.level", String.format("%d", getLevel()));
 		data.put("game.op", isOP() ? "true" : "false");
 		return data;
 	}
 
 	public DateTime lastLogout()
 	{
 		List<IPlayerSessionDataProvider> dataHooks = HookEngine.hookContainer.getComponents(IPlayerSessionDataProvider.class);
 		if (this.isOnline() || dataHooks.isEmpty())
 			return null;
 		DateTime logout = null;
 		for (IPlayerSessionDataProvider provider : dataHooks)
 		{
 			DateTime value = provider.GetPlayerLogout(this);
 			if (value != null && (logout == null || value.isAfter(logout)))
 				logout = value;
 		}
 		return logout;
 	}
 
 	public String getBanReason()
 	{
 		List<IPlayerSessionDataProvider> dataHooks = HookEngine.hookContainer.getComponents(IPlayerSessionDataProvider.class);
 		if (!this.isBanned() || dataHooks.isEmpty())
 			return null;
 		for (IPlayerSessionDataProvider provider : dataHooks)
 		{
 			String reason = provider.GetPlayerBanReason(this);
 			if (reason != null)
 				return reason;
 		}
 		return null;
 	}
 
 	public String getDataValue(String key)
 	{
 		HashMap<String, String> data = getData();
 		if (data.containsKey(key))
 			return data.get(key);
 		return null;
 	}
 
 	public boolean canSee(RunsafePlayer target)
 	{
 		List<IPlayerVisibility> visibilityHooks = HookEngine.hookContainer.getComponents(IPlayerVisibility.class);
 		if (visibilityHooks.isEmpty())
 			return true;
 		for (IPlayerVisibility check : visibilityHooks)
 			if (!check.canPlayerASeeB(this, target))
 				return false;
 		return true;
 	}
 
 	public boolean isVanished()
 	{
 		List<IPlayerVisibility> visibilityHooks = HookEngine.hookContainer.getComponents(IPlayerVisibility.class);
 		if (visibilityHooks.isEmpty())
 			return false;
 		for (IPlayerVisibility check : visibilityHooks)
 			if (check.isPlayerVanished(this))
 				return true;
 		return false;
 	}
 
 	public boolean isPvPFlagged()
 	{
 		List<IPlayerPvPFlag> pvpFlagHooks = HookEngine.hookContainer.getComponents(IPlayerPvPFlag.class);
 		if (pvpFlagHooks.isEmpty())
 			return true;
 		for (IPlayerPvPFlag hook : pvpFlagHooks)
 			if (!hook.isFlaggedForPvP(this))
 				return false;
 		return true;
 	}
 
 	public List<String> getGroups()
 	{
 		ArrayList<String> result = new ArrayList<String>();
 		for (IPlayerPermissions hook : HookEngine.hookContainer.getComponents(IPlayerPermissions.class))
 		{
 			List<String> groups = hook.getUserGroups(this);
 			if (groups != null)
 				result.addAll(groups);
 		}
 		if (result.size() == 0)
 			result.add("unknown");
 		return result;
 	}
 
 	public boolean setGroup(String group)
 	{
 		for (IPlayerPermissions hook : HookEngine.hookContainer.getComponents(IPlayerPermissions.class))
 		{
 			if (hook.setUserGroup(this, group))
 				return true;
 		}
 		return false;
 	}
 
 	public boolean canBuildNow()
 	{
 		List<IPlayerBuildPermission> buildPermissionHooks =
 			HookEngine.hookContainer.getComponents(IPlayerBuildPermission.class);
 		if (buildPermissionHooks.isEmpty())
 			return true;
 		for (IPlayerBuildPermission check : buildPermissionHooks)
 			if (check.blockPlayerBuilding(this, getLocation()))
 				return false;
 		return true;
 	}
 
 	public void give(RunsafeMeta... items)
 	{
 		if (items == null || items.length < 1)
 			return;
 		ItemStack[] itemStacks = new ItemStack[items.length];
 		for (int i = 0; i < items.length; ++i)
 			itemStacks[i] = items[i].getRaw();
 		player.getInventory().addItem(itemStacks);
 	}
 
 	public void removeItem(Item itemType, int amount)
 	{
 		this.getInventory().remove(itemType, amount);
 		this.updateInventory();
 	}
 
 	public void removeItem(Item itemType)
 	{
 		this.removeItem(itemType, itemType.getStackSize());
 	}
 
 	public Universe getUniverse()
 	{
		return this.getWorld().getUniverse();
 	}
 
 	public boolean isInUniverse(String universeName)
 	{
		return this.getUniverse().getName().equals(universeName);
 	}
 
 	@Override
 	public void sendColouredMessage(String message)
 	{
 		if (message != null)
 			sendMessage(ChatColour.ToMinecraft(message));
 	}
 
 	@Override
 	public void sendColouredMessage(String format, Object... params)
 	{
 		sendColouredMessage(String.format(format, params));
 	}
 }
