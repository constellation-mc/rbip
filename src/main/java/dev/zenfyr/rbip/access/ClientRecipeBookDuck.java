package dev.zenfyr.rbip.access;

import java.util.Collection;
import java.util.List;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.item.CreativeModeTab;

public interface ClientRecipeBookDuck {

  List<RecipeCollection> rbip$getCollectionForTab(CreativeModeTab tab);

  Collection<RecipeBookComponent.TabInfo> rbip$allInfos();

  CreativeModeTab rbip$tabForInfo(RecipeBookComponent.TabInfo tab);
}
