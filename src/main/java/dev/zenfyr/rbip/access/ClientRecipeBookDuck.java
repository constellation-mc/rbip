package dev.zenfyr.rbip.access;

import java.util.List;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.item.CreativeModeTab;

public interface ClientRecipeBookDuck {

  List<RecipeCollection> rbip$getResultsForGroup(CreativeModeTab group);
}
