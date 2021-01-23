package com.untamedears.realisticbiomes.growth;

import com.untamedears.realisticbiomes.model.Plant;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.api.BlockAPI;
import vg.civcraft.mc.civmodcore.api.MaterialAPI;

public class NetherVineGrower extends VerticalGrower {

	private Material tipMaterial;

	public NetherVineGrower(int maxHeight, Material stemMaterial, Material tipMaterial, BlockFace primaryGrowthDirection) {
		super(maxHeight, stemMaterial, primaryGrowthDirection, false);

		this.tipMaterial = tipMaterial;
	}

	@Override
	public int getStage(Plant plant)
	{
		Block block = plant.getLocation().getBlock();
		if (!isPlantMaterial(block.getType()))
		{
			return -1;
		}
		Block base = getRelativeBlock(block, getPrimaryGrowthDirection().getOppositeFace());
		if (!base.getLocation().equals(block.getLocation())) {
			return -1;
		}

		return getActualHeight(block) -1;
	}

	@Override
	public void setStage(Plant plant, int stage)
	{
		int currentState = getStage(plant);
		if (stage <= currentState) {
			return;
		}

		Block block = plant.getLocation().getBlock();
		growVertically(plant, block, stage - currentState);
	}

	@Override
	protected Block growVertically(Plant plant, Block block, int howMany)
	{
		if (getMaterial() != null && tipMaterial != null && !isPlantMaterial(block.getType())) {
			if (howMany > 1) {
				block.setType(getMaterial());
			}
			else { //Growing 1 exactly
				block.setType(tipMaterial);
			}
		}

		int counter = 1;
		Block onTop = block;
		while (counter < getMaxHeight() && howMany > 0) {
			counter++;
			onTop = onTop.getRelative(getPrimaryGrowthDirection());
			Material topMaterial = onTop.getType();
			if (MaterialAPI.isAir(topMaterial)) {
				//Removed the code to check if they can be fully surrounded
				if (howMany == 1) {
					onTop.setType(tipMaterial, true);
				} else {
					onTop.setType(getMaterial(), true);
				}
				howMany--;
				continue;
			}

			if (isPlantMaterial(topMaterial))
			{
				continue;
			}

			break;
		}

		return !isPlantMaterial(onTop.getType()) ? onTop.getRelative(getPrimaryGrowthDirection().getOppositeFace()) : onTop;
	}

	@Override
	protected int getActualHeight(Block block)
	{
		Block tipBlock = null;
		Block baseBlock = null;

		if (block.getType() == tipMaterial)
		{
			tipBlock = block;
		}

		Block currentWorkingBlock = block;

		if (tipBlock == null) { //Haven't found the tip yet
			while (true) {
				Block blockInGrowthDirection = getRelativeBlock(currentWorkingBlock, getPrimaryGrowthDirection());
				if (!isPlantMaterial(blockInGrowthDirection.getType())) {
					tipBlock = currentWorkingBlock;
					break;
				}

				//Encase of a problem
				if (blockInGrowthDirection.getLocation().getBlockY() > 256 || blockInGrowthDirection.getLocation().getY() < 0) {
					break;
				}
			}
		}

		while (true) {
			Block blockInBaseDirection = getRelativeBlock(currentWorkingBlock, getPrimaryGrowthDirection().getOppositeFace());
			if (!isPlantMaterial(blockInBaseDirection.getType())) {
				baseBlock = currentWorkingBlock;
				break;
			}

			//Encase of a problem
			if (blockInBaseDirection.getLocation().getBlockY() > 256 || blockInBaseDirection.getLocation().getY() < 0) {
				break;
			}
		}

		if (tipBlock != null && baseBlock != null) {
			return Math.abs(tipBlock.getY() - baseBlock.getY()) + 1;
		}

		//Something else that's bad happened
		return -1;
	}

	private int getMaxHeight()
	{
		return this.getMaxStage() + 1;
	}

	public boolean isPlantMaterial(Material material)
	{
		return material == this.getMaterial() || material == tipMaterial;
	}
}
