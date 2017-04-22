package micdoodle8.mods.galacticraft.core.client.jei.circuitfabricator;

import com.google.common.collect.ImmutableMap;

import micdoodle8.mods.galacticraft.api.recipe.CircuitFabricatorRecipes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class CircuitFabricatorRecipeMaker
{
    public static List<CircuitFabricatorRecipeWrapper> getRecipesList()
    {
        List<CircuitFabricatorRecipeWrapper> recipes = new ArrayList<>();

        ImmutableMap<NonNullList<ItemStack>, ItemStack> recipesMap = CircuitFabricatorRecipes.getRecipes();
        for (Entry<NonNullList<ItemStack>, ItemStack> entry : recipesMap.entrySet())
        {
            CircuitFabricatorRecipeWrapper wrapper = new CircuitFabricatorRecipeWrapper(entry.getKey(), entry.getValue());
            recipes.add(wrapper);
        }

        return recipes;
    }
}
