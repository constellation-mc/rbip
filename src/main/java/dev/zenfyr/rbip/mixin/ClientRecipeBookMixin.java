package dev.zenfyr.rbip.mixin;

import dev.zenfyr.rbip.access.ClientRecipeBookDuck;
import java.util.*;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClientRecipeBook.class, priority = 999)
public class ClientRecipeBookMixin implements ClientRecipeBookDuck {

  @Shadow
  private List<RecipeCollection> allCollections;

  @Unique private final Map<CreativeModeTab, List<RecipeCollection>> rbip$groupedCollections =
      new HashMap<>();

  @Inject(at = @At("TAIL"), method = "setupCollections")
  private void rbip$reload(Iterable<Recipe<?>> recipes, RegistryAccess manager, CallbackInfo ci) {
    CreativeModeTabs.tryRebuildTabContents(FeatureFlags.REGISTRY.allFlags(), false, manager);

    Map<Item, CreativeModeTab> tabs = new HashMap<>();
    BuiltInRegistries.CREATIVE_MODE_TAB.stream()
        .filter(itemGroup -> !itemGroup.isAlignedRight())
        .forEach(itemGroup -> {
          for (ItemStack stack : itemGroup.getSearchTabDisplayItems()) {
            if (tabs.containsKey(stack.getItem())) continue;
            tabs.put(stack.getItem(), itemGroup);
          }
        });

    Map<CreativeModeTab, List<RecipeCollection>> collectionsByTab = new HashMap<>();

    this.allCollections.forEach(collection -> {
      for (Recipe<?> recipe : collection.getRecipes()) {
        if (!RecipeType.CRAFTING.equals(recipe.getType())) return;
        if (recipe.isSpecial() || recipe.isIncomplete()) return;

        ItemStack output = recipe.getResultItem(manager);
        if (output.isEmpty()) continue;

        CreativeModeTab itemGroup =
            tabs.getOrDefault(output.getItem(), CreativeModeTabs.getDefaultTab());
        collectionsByTab.computeIfAbsent(itemGroup, g -> new ArrayList<>()).add(collection);
        return;
      }
    });

    this.rbip$groupedCollections.clear();
    this.rbip$groupedCollections.putAll(collectionsByTab);
  }

  @Inject(at = @At("HEAD"), method = "getCategory", cancellable = true)
  private static void rbip$getGroupForRecipe(
      Recipe<?> recipe, CallbackInfoReturnable<RecipeBookCategories> cir) {
    if (RecipeType.CRAFTING.equals(recipe.getType())) {
      cir.setReturnValue(RecipeBookCategories.CRAFTING_MISC);
    }
  }

  @Override
  public List<RecipeCollection> rbip$getCollectionForTab(CreativeModeTab tab) {
    return this.rbip$groupedCollections.getOrDefault(tab, List.of());
  }
}
