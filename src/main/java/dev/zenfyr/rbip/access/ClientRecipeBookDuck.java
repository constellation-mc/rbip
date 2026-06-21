package dev.zenfyr.rbip.access;

import java.util.List;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.item.ItemGroup;

public interface ClientRecipeBookDuck {

  List<RecipeResultCollection> rbip$getResultsForGroup(ItemGroup group);
}
