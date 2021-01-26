package com.untamedears.realisticbiomes.utils;

import com.untamedears.realisticbiomes.RealisticBiomes;
import com.untamedears.realisticbiomes.model.Plant;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.playersettings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.playersettings.impl.BooleanSetting;

public class AutoReplant implements Listener {

	private BooleanSetting toggleAutoReplant;

	public AutoReplant() {
		initSettings();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();
		PlayerInventory inventory = player.getInventory();

		Material seed = getSeed(block);
		if (seed == null) {
			return;
		}
		if (!isFullyGrown(block)) {
			return;
		}
		if (!playerHasSeeds(inventory, seed)) {
			return;
		}

		removeSeedFromPlayerInv(inventory, seed);
		replantCrop(block, seed);
	}

	/**
	 * Takes a block and checks if its a crop, returns seeds if it is, null if it isn't.
	 * @return Seed Material
	 */
	public Material getSeed(Block block) {
		switch (block.getType()) {
			case WHEAT:
				return Material.WHEAT_SEEDS;
			case CARROTS:
				return Material.CARROT;
			case POTATOES:
				return Material.POTATO;
			case BEETROOTS:
				return Material.BEETROOT;
			case NETHER_WART:
				return Material.NETHER_WART;
			default:
				return null;
		}
	}

	/**
	 * Checks a players inventory for a given seed
	 * @param inventory PlayerInventory
	 * @param seeds Seed we are looking for
	 * @return true if found
	 */
	public boolean playerHasSeeds(PlayerInventory inventory, Material seeds) {
		ItemStack[] items = inventory.getContents();
		for (ItemStack item : items) {
			if (item == null) {
				continue;
			}
			if (item.getType() == seeds) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Runnable that replants the crop 5 ticks later, also updates PlantLogicManager to handle plant creation
	 * @param block Replant location
	 * @param seed Seed to replant
	 */
	public void replantCrop(Block block, Material seed) {
		Bukkit.getScheduler().runTaskLater(RealisticBiomes.getInstance(), () -> {
			block.setType(seed);
			RealisticBiomes.getInstance().getPlantLogicManager().handlePlantCreation(block, new ItemStack(seed));
		},5L);
	}

	public void removeSeedFromPlayerInv(PlayerInventory inventory, Material seed) {
		ItemStack[] items = inventory.getContents();
		for (ItemStack item : items) {
			if (item == null) {
				continue;
			}
			if (item.getType() == seed) {
				item.setAmount(item.getAmount() - 1);
			}
		}
	}

	public boolean isFullyGrown(Block block) {
		Ageable crop = (Ageable) block.getBlockData();
		return crop.getAge() == crop.getMaximumAge();
	}

	public void initSettings() {
		MenuSection rbMenu = PlayerSettingAPI.getMainMenu()
				.createMenuSection("RealisticBiomes", "Auto replant setting", new ItemStack(
						Material.WHEAT_SEEDS));
		toggleAutoReplant = new BooleanSetting(RealisticBiomes.getInstance(), true, "Use auto replant?", "autoReplant",
				"Will automatically take seeds from your inventory and replant crops");
		PlayerSettingAPI.registerSetting(toggleAutoReplant, rbMenu);
	}

	public boolean getToggleAutoReplant(UUID uuid) {
		return toggleAutoReplant.getValue(uuid);
	}
}
