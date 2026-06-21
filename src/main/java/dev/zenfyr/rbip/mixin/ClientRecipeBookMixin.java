package dev.zenfyr.rbip.mixin;

import com.google.common.collect.*;
import dev.zenfyr.rbip.access.ClientRecipeBookDuck;
import java.util.*;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.recipebook.RecipeBookGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.resource.featuretoggle.FeatureFlags;
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
  private List<RecipeResultCollection> orderedResults;

  @Unique private final Map<ItemGroup, List<RecipeResultCollection>> rbip$groupedRecipes = new HashMap<>();

  @Inject(at = @At("TAIL"), method = "reload")
  private void rbip$reload(
      Iterable<Recipe<?>> recipes, DynamicRegistryManager manager, CallbackInfo ci) {
    ItemGroups.updateDisplayContext(FeatureFlags.FEATURE_MANAGER.getFeatureSet(), false, manager);

    Map<Item, ItemGroup> groups = new HashMap<>();
    Registries.ITEM_GROUP.stream()
        .filter(itemGroup -> !itemGroup.isSpecial())
        .forEach(itemGroup -> {
          for (ItemStack stack : itemGroup.getSearchTabStacks()) {
            if (groups.containsKey(stack.getItem())) continue;
            groups.put(stack.getItem(), itemGroup);
          }
        });

    Map<ItemGroup, List<RecipeResultCollection>> map2 = new HashMap<>();

    this.orderedResults.forEach(collection -> {
      for (Recipe<?> recipe : collection.getAllRecipes()) {
        if (!RecipeType.CRAFTING.equals(recipe.getType())) return;
        if (recipe.isIgnoredInRecipeBook() || recipe.isEmpty()) return;

        ItemStack output = recipe.getOutput(manager);
        if (output.isEmpty()) continue;

        ItemGroup itemGroup = groups.getOrDefault(output.getItem(), ItemGroups.getDefaultTab());
        map2.computeIfAbsent(itemGroup, g -> new ArrayList<>()).add(collection);
        return;
      }
    });

    this.rbip$groupedRecipes.clear();
    this.rbip$groupedRecipes.putAll(map2);
  }

  @Inject(at = @At("HEAD"), method = "getGroupForRecipe", cancellable = true)
  private static void rbip$getGroupForRecipe(
      Recipe<?> recipe, CallbackInfoReturnable<RecipeBookGroup> cir) {
    if (RecipeType.CRAFTING.equals(recipe.getType())) {
      cir.setReturnValue(RecipeBookGroup.CRAFTING_MISC);
    }
  }

  @Override
  public List<RecipeResultCollection> rbip$getResultsForGroup(ItemGroup group) {
    return this.rbip$groupedRecipes.getOrDefault(group, List.of());
  }
}
