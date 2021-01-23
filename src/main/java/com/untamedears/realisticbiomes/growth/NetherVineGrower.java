package com.untamedears.realisticbiomes.growth;

import com.untamedears.realisticbiomes.model.Plant;
import com.untamedears.realisticbiomes.utils.RBUtils;
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

	public static Block getBaseBlock(Block block) {
		Material mat = block.getType();
		BlockFace face = BlockFace.DOWN;
		if (mat == Material.WEEPING_VINES || mat == Material.WEEPING_VINES_PLANT) {
			face = BlockFace.UP;
		} else if (mat == Material.TWISTING_VINES || mat == Material.TWISTING_VINES_PLANT) {
			face = BlockFace.DOWN;
		}
		
		Block bottomBlock = block;
		// not actually using this variable, but just having it here as a fail safe
		for (int i = 0; i < 257; i++) {
			Block towardsBase = bottomBlock.getRelative(face);
			if (!RBUtils.isNetherVine(towardsBase.getType())) {
				break;
			}
			bottomBlock = towardsBase;
		}
		return bottomBlock;
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
		System.out.println("Starting Height Check");
		Block tipBlock = null;
		Block baseBlock = null;

		if (block.getType() == tipMaterial)
		{
			tipBlock = block;
			System.out.println("Found Tip Block at: " + block.toString());
		}

		Block currentWorkingBlock = block;

		if (tipBlock == null) { //Haven't found the tip yet
			System.out.println("Not found Tip, so searching");
			for (int i = 0; i < 257; i++) {
				Block blockInGrowthDirection = getRelativeBlock(currentWorkingBlock, getPrimaryGrowthDirection());
				System.out.println("Searching for tip block at: " + blockInGrowthDirection.toString());
				if (blockInGrowthDirection.getType() == tipMaterial)
				{
					tipBlock = blockInGrowthDirection;
					System.out.println("Found tip block at: " + blockInGrowthDirection.toString());
					break;
				}

				//Encase of a problem
				if (blockInGrowthDirection.getLocation().getBlockY() > 256 || blockInGrowthDirection.getLocation().getY() < 0) {
					break;
				}

				currentWorkingBlock = blockInGrowthDirection;
			}
		}

		for (int i = 0; i < 257; i++) {
			Block blockInBaseDirection = getRelativeBlock(currentWorkingBlock, getPrimaryGrowthDirection().getOppositeFace());
			System.out.println("Searching for lack of base block at: " + blockInBaseDirection.toString());
			if (!isPlantMaterial(blockInBaseDirection.getType())) {
				baseBlock = currentWorkingBlock;
				System.out.println("Found base block at: " + currentWorkingBlock.toString());
				break;
			}

			//Encase of a problem
			if (blockInBaseDirection.getLocation().getBlockY() > 256 || blockInBaseDirection.getLocation().getY() < 0) {
				break;
			}
			currentWorkingBlock = blockInBaseDirection;
		}

		if (baseBlock == null && tipBlock != null) {
			baseBlock = tipBlock;
		}

		if (tipBlock != null) {
			System.out.println("Height is: Math.abs(tipBlock.getY() - baseBlock.getY()) + 1");
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
