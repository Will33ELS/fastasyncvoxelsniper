package com.thevoxelbox.voxelsniper.brush;

import java.util.List;
import com.thevoxelbox.voxelsniper.Message;
import com.thevoxelbox.voxelsniper.sniper.SnipeData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.Nullable;

/**
 * http://www.voxelwiki.com/minecraft/Voxelsniper#The_Jockey_Brush
 *
 * @author Voxel
 * @author Monofraps
 */
public class JockeyBrush extends AbstractBrush {

	private static final int ENTITY_STACK_LIMIT = 50;
	private JockeyType jockeyType = JockeyType.NORMAL_ALL_ENTITIES;
	@Nullable
	private Entity jockeyedEntity;


	public JockeyBrush() {
		super("Jockey");
	}

	private void sitOn(SnipeData snipeData) {
		Chunk targetChunk = this.getWorld()
			.getChunkAt(this.getTargetBlock()
				.getLocation());
		int targetChunkX = targetChunk.getX();
		int targetChunkZ = targetChunk.getZ();
		double range = Double.MAX_VALUE;
		Entity closest = null;
		for (int x = targetChunkX - 1; x <= targetChunkX + 1; x++) {
			for (int y = targetChunkZ - 1; y <= targetChunkZ + 1; y++) {
				for (Entity entity : this.getWorld()
					.getChunkAt(x, y)
					.getEntities()) {
					if (entity.getEntityId() == snipeData.getOwner()
						.getPlayer()
						.getEntityId()) {
						continue;
					}
					if (this.jockeyType == JockeyType.NORMAL_PLAYER_ONLY || this.jockeyType == JockeyType.INVERSE_PLAYER_ONLY) {
						if (!(entity instanceof Player)) {
							continue;
						}
					}
					Location entityLocation = entity.getLocation();
					double entityDistance = entityLocation.distance(snipeData.getOwner()
						.getPlayer()
						.getLocation());
					if (entityDistance < range) {
						range = entityDistance;
						closest = entity;
					}
				}
			}
		}
		if (closest != null) {
			Player player = snipeData.getOwner()
				.getPlayer();
			PlayerTeleportEvent playerTeleportEvent = new PlayerTeleportEvent(player, player.getLocation(), closest.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
			Bukkit.getPluginManager()
				.callEvent(playerTeleportEvent);
			if (!playerTeleportEvent.isCancelled()) {
				if (this.jockeyType == JockeyType.INVERSE_PLAYER_ONLY || this.jockeyType == JockeyType.INVERSE_ALL_ENTITIES) {
					player.setPassenger(closest);
				} else {
					closest.setPassenger(player);
					this.jockeyedEntity = closest;
				}
				snipeData.sendMessage(ChatColor.GREEN + "You are now saddles on entity: " + closest.getEntityId());
			}
		} else {
			snipeData.sendMessage(ChatColor.RED + "Could not find any entities");
		}
	}

	private void stack(SnipeData snipeData) {
		int brushSizeDoubled = snipeData.getBrushSize() * 2;
		List<Entity> nearbyEntities = snipeData.getOwner()
			.getPlayer()
			.getNearbyEntities(brushSizeDoubled, brushSizeDoubled, brushSizeDoubled);
		Entity lastEntity = snipeData.getOwner()
			.getPlayer();
		int stackHeight = 0;
		for (Entity entity : nearbyEntities) {
			if (!(stackHeight >= ENTITY_STACK_LIMIT)) {
				if (this.jockeyType == JockeyType.STACK_ALL_ENTITIES) {
					lastEntity.setPassenger(entity);
					lastEntity = entity;
					stackHeight++;
				} else if (this.jockeyType == JockeyType.STACK_PLAYER_ONLY) {
					if (entity instanceof Player) {
						lastEntity.setPassenger(entity);
						lastEntity = entity;
						stackHeight++;
					}
				} else {
					snipeData.getOwner()
						.getPlayer()
						.sendMessage("You broke stack! :O");
				}
			} else {
				return;
			}
		}
	}

	@Override
	public final void arrow(SnipeData snipeData) {
		if (this.jockeyType == JockeyType.STACK_ALL_ENTITIES || this.jockeyType == JockeyType.STACK_PLAYER_ONLY) {
			stack(snipeData);
		} else {
			this.sitOn(snipeData);
		}
	}

	@Override
	public final void powder(SnipeData snipeData) {
		if (this.jockeyType == JockeyType.INVERSE_PLAYER_ONLY || this.jockeyType == JockeyType.INVERSE_ALL_ENTITIES) {
			snipeData.getOwner()
				.getPlayer()
				.eject();
			snipeData.getOwner()
				.getPlayer()
				.sendMessage(ChatColor.GOLD + "The guy on top of you has been ejected!");
		} else {
			if (this.jockeyedEntity != null) {
				this.jockeyedEntity.eject();
				this.jockeyedEntity = null;
				snipeData.getOwner()
					.getPlayer()
					.sendMessage(ChatColor.GOLD + "You have been ejected!");
			}
		}
	}

	@Override
	public final void info(Message message) {
		message.brushName(this.getName());
		message.custom("Current jockey mode: " + ChatColor.GREEN + this.jockeyType);
		message.custom(ChatColor.GREEN + "Help: " + ChatColor.AQUA + "http://www.voxelwiki.com/minecraft/Voxelsniper#The_Jockey_Brush");
	}

	@Override
	public final void parameters(String[] parameters, SnipeData snipeData) {
		try {
			boolean stack = false;
			boolean playerOnly = false;
			boolean inverse = false;
			for (String parameter : parameters) {
				if (parameter.startsWith("-i:")) {
					inverse = parameter.endsWith("y");
				}
				if (parameter.startsWith("-po:")) {
					playerOnly = parameter.endsWith("y");
				}
				if (parameter.startsWith("-s:")) {
					stack = parameter.endsWith("y");
				}
			}
			if (inverse) {
				this.jockeyType = playerOnly ? JockeyType.INVERSE_PLAYER_ONLY : JockeyType.INVERSE_ALL_ENTITIES;
			} else if (stack) {
				this.jockeyType = playerOnly ? JockeyType.STACK_PLAYER_ONLY : JockeyType.STACK_ALL_ENTITIES;
			} else {
				this.jockeyType = playerOnly ? JockeyType.NORMAL_PLAYER_ONLY : JockeyType.NORMAL_ALL_ENTITIES;
			}
			snipeData.sendMessage("Current jockey mode: " + ChatColor.GREEN + this.jockeyType);
		} catch (RuntimeException exception) {
			snipeData.sendMessage("Error while parsing your arguments.");
			exception.printStackTrace();
		}
	}

	/**
	 * Available types of jockey modes.
	 */
	private enum JockeyType {
		NORMAL_ALL_ENTITIES("Normal (All)"),
		NORMAL_PLAYER_ONLY("Normal (Player only)"),
		INVERSE_ALL_ENTITIES("Inverse (All)"),
		INVERSE_PLAYER_ONLY("Inverse (Player only)"),
		STACK_ALL_ENTITIES("Stack (All)"),
		STACK_PLAYER_ONLY("Stack (Player only)");
		private String name;

		JockeyType(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}

	@Override
	public String getPermissionNode() {
		return "voxelsniper.brush.jockey";
	}
}
