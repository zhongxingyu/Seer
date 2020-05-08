 package no.runsafe.framework.minecraft.player;
 
 import com.google.common.collect.ImmutableList;

 import no.runsafe.framework.api.command.ICommandExecutor;
 import no.runsafe.framework.api.hook.*;
 import no.runsafe.framework.internal.HookEngine;
 import no.runsafe.framework.internal.packets.PacketHelper;
 import no.runsafe.framework.internal.wrapper.player.BukkitPlayer;
 import no.runsafe.framework.minecraft.*;
 import no.runsafe.framework.minecraft.chunk.RunsafeChunk;
 import no.runsafe.framework.minecraft.event.player.RunsafeOperatorEvent;
 import no.runsafe.framework.minecraft.inventory.RunsafePlayerInventory;
 import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
 import no.runsafe.framework.text.ChatColour;
 import org.bukkit.GameMode;
 import org.bukkit.OfflinePlayer;
 import org.joda.time.DateTime;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 @SuppressWarnings("OverlyCoupledClass")
 public class RunsafePlayer extends BukkitPlayer implements ICommandExecutor
 {
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
 
 	@Nullable
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
 		if (isOnline() || dataHooks.isEmpty())
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
 		return player != null && player.getGameMode() == GameMode.SURVIVAL;
 	}
 
 	public boolean isCreative()
 	{
 		return player != null && player.getGameMode() == GameMode.CREATIVE;
 	}
 
 	public boolean isAdventurer()
 	{
 		return player != null && player.getGameMode() == GameMode.ADVENTURE;
 	}
 
 	public void teleport(RunsafeWorld world, double x, double y, double z)
 	{
 		RunsafeLocation target = new RunsafeLocation(world, x, y, z);
 		RunsafeChunk chunk = target.getChunk();
 		if (chunk.isUnloaded())
 			chunk.load();
 		teleport(target);
 	}
 
 	public Map<String, String> getData()
 	{
 		List<IPlayerDataProvider> dataHooks = HookEngine.hookContainer.getComponents(IPlayerDataProvider.class);
 		Map<String, String> results = getBasicData();
 		for (IPlayerDataProvider provider : dataHooks)
 		{
 			Map<String, String> data = provider.GetPlayerData(this);
 			if (data != null)
 				results.putAll(data);
 		}
 		return results;
 	}
 
 	@SuppressWarnings("HardcodedFileSeparator")
 	public Map<String, String> getBasicData()
 	{
 		Map<String, String> data = new LinkedHashMap<String, String>(7);
 		if (player != null && isOnline())
 		{
 			data.put("game.ip",
 				String.format("%s [%s]",
 					player.getAddress().getAddress().getHostAddress(),
 					player.getAddress().getHostName()
 				)
 			);
 			data.put("game.mode", player.getGameMode().name());
 			data.put("game.flying", player.isFlying() ? "true" : "false");
 			data.put("game.health", String.format("%.1f/%.1f", getHealth(), getMaxHealth()));
 		}
 		data.put("game.experience", String.format("%.1f", getXP()));
 		data.put("game.level", String.format("%d", getLevel()));
 		data.put("game.op", isOP() ? "true" : "false");
 		return data;
 	}
 
 	@Nullable
 	public DateTime lastLogout()
 	{
 		List<IPlayerSessionDataProvider> dataHooks = HookEngine.hookContainer.getComponents(IPlayerSessionDataProvider.class);
 		if (isOnline() || dataHooks.isEmpty())
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
 
 	@Nullable
 	public String getBanReason()
 	{
 		List<IPlayerSessionDataProvider> dataHooks = HookEngine.hookContainer.getComponents(IPlayerSessionDataProvider.class);
 		if (isNotBanned() || dataHooks.isEmpty())
 			return null;
 		for (IPlayerSessionDataProvider provider : dataHooks)
 		{
 			String reason = provider.GetPlayerBanReason(this);
 			if (reason != null)
 				return reason;
 		}
 		return null;
 	}
 
 	@Nullable
 	public String getDataValue(String key)
 	{
 		Map<String, String> data = getData();
 		if (data.containsKey(key))
 			return data.get(key);
 		return null;
 	}
 
 	@Deprecated
 	public boolean canSee(RunsafePlayer target)
 	{
 		return !shouldNotSee(target);
 	}
 
 	public boolean shouldNotSee(@Nonnull RunsafePlayer target)
 	{
 		//noinspection ConstantConditions
 		if (getName().equals(target.getName()))
 			return false;
 
 		List<IPlayerVisibility> visibilityHooks = HookEngine.hookContainer.getComponents(IPlayerVisibility.class);
 		if (visibilityHooks.isEmpty())
 			return false;
 
 		for (IPlayerVisibility check : visibilityHooks)
 			if (check.isPlayerHidden(this, target))
 				return true;
 		return false;
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
 			if (hook.isPvPDisabled(this))
 				return false;
 		return true;
 	}
 
 	public ImmutableList<String> getGroups()
 	{
 		List<String> result = new ArrayList<String>(5);
 		for (IPlayerPermissions hook : HookEngine.hookContainer.getComponents(IPlayerPermissions.class))
 		{
 			List<String> groups = hook.getUserGroups(this);
 			if (groups != null)
 				result.addAll(groups);
 		}
 		if (result.isEmpty())
 			result.add("unknown");
 		return ImmutableList.copyOf(result);
 	}
 
 	public boolean setGroup(String group)
 	{
 		for (IPlayerPermissions hook : HookEngine.hookContainer.getComponents(IPlayerPermissions.class))
 			if (hook.setGroup(this, group))
 				return true;
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
 		if (player == null || items == null || items.length < 1)
 			return;
 		RunsafePlayerInventory inventory = getInventory();
 		if (inventory != null)
 		{
 			inventory.addItems(items);
 			updateInventory();
 		}
 	}
 
 	public void removeItem(Item itemType, int amount)
 	{
 		RunsafePlayerInventory inventory = getInventory();
 		if (inventory != null)
 		{
 			inventory.remove(itemType, amount);
 			updateInventory();
 		}
 	}
 
 	public void removeItem(Item itemType)
 	{
 		removeItem(itemType, itemType.getStackSize());
 	}
 
 	@Nullable
 	public Universe getUniverse()
 	{
 		RunsafeWorld world = getWorld();
 		return world == null ? null : world.getUniverse();
 	}
 
 	public boolean isInUniverse(String universeName)
 	{
 		Universe universe = getUniverse();
 		return universe != null && universe.getName().equals(universeName);
 	}
 
 	public void clearInventory()
 	{
 		if (player == null)
 			return;
 
 		player.getInventory().clear();
 		updateInventory();
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
 
 	public void sendPacket(Object packet) throws Exception
 	{
 		Object entityPlayer = PacketHelper.getMethod("getHandle", player.getClass(), 0).invoke(player);
 		Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
 		PacketHelper.getMethod("sendPacket", playerConnection.getClass(), 1).invoke(playerConnection, packet);
 	}
 }
