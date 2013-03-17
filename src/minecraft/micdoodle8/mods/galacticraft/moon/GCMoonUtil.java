package micdoodle8.mods.galacticraft.moon;

import micdoodle8.mods.galacticraft.moon.blocks.GCMoonBlocks;
import micdoodle8.mods.galacticraft.moon.items.GCMoonItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import universalelectricity.components.common.BasicComponents;

/**
 * Copyright 2012-2013, micdoodle8
 *
 *  All rights reserved.
 *
 */
public class GCMoonUtil
{
	public static void addCraftingRecipes()
	{
		CraftingManager.getInstance().addRecipe(new ItemStack(GCMoonItems.cheeseBlock, 1), new Object[] {
			"YYY",
			"YXY",
			"YYY",
			'X', Item.bucketMilk,
			'Y', GCMoonItems.cheeseCurd
		});
	}

	public static void addSmeltingRecipes()
	{
		FurnaceRecipes.smelting().addSmelting(GCMoonItems.meteoricIronRaw.itemID, new ItemStack(GCMoonItems.meteoricIronIngot), 1.0F);
		FurnaceRecipes.smelting().addSmelting(GCMoonBlocks.blockMoon.blockID, 0, new ItemStack(BasicComponents.itemIngot, 1, 0), 1.0F);
		FurnaceRecipes.smelting().addSmelting(GCMoonBlocks.blockMoon.blockID, 1, new ItemStack(BasicComponents.itemIngot, 1, 1), 1.0F);
	}
}
