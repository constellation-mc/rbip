package dev.zenfyr.rbip.mixin;

import dev.zenfyr.rbip.access.ClientRecipeBookDuck;
import java.util.*;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientRecipeBook.class, priority = 999)
public class ClientRecipeBookMixin implements ClientRecipeBookDuck {

  @Shadow
  private List<RecipeCollection> allCollections;

  @Unique private Map<RecipeBookComponent.TabInfo, CreativeModeTab> rbip$tabInfoMap;

  @Unique private Map<CreativeModeTab, List<RecipeCollection>> rbip$groupedCollections;

  @Unique private List<RecipeBookComponent.TabInfo> rbip$sortedInfos;

  @Inject(at = @At("TAIL"), method = "rebuildCollections")
  private void rbip$reload(CallbackInfo ci) {
    var minecraft = Minecraft.getInstance();
    var conn = minecraft.getConnection();
    var level = minecraft.level;
    if (conn == null || level == null) return;

    CreativeModeTabs.tryRebuildTabContents(
        FeatureFlags.REGISTRY.allFlags(), false, conn.registryAccess());

    Map<Item, CreativeModeTab> tabs = new HashMap<>();
    BuiltInRegistries.CREATIVE_MODE_TAB.stream()
        .filter(itemGroup -> !itemGroup.isAlignedRight())
        .forEach(itemGroup -> {
          for (ItemStack stack : itemGroup.getSearchTabDisplayItems()) {
            tabs.putIfAbsent(stack.getItem(), itemGroup);
          }
        });

    List<RecipeBookComponent.TabInfo> sorted = new ArrayList<>();
    Map<RecipeBookComponent.TabInfo, CreativeModeTab> tabInfoMap = new IdentityHashMap<>();

    BuiltInRegistries.CREATIVE_MODE_TAB.stream()
        .filter(itemGroup -> !itemGroup.isAlignedRight())
        .forEach(itemGroup -> {
          var info = new RecipeBookComponent.TabInfo(
              itemGroup.getIconItem(), Optional.empty(), RecipeBookCategories.CRAFTING_MISC);
          sorted.add(info);
          tabInfoMap.put(info, itemGroup);
        });

    Map<CreativeModeTab, List<RecipeCollection>> collectionsByTab = new HashMap<>();
    var context = SlotDisplayContext.fromLevel(minecraft.level);
    this.allCollections.forEach(collection -> {
      for (RecipeDisplayEntry recipe : collection.getRecipes()) {
        if (recipe.display().type() != ShapedCraftingRecipeDisplay.TYPE
            && recipe.display().type() != ShapelessCraftingRecipeDisplay.TYPE) return;

        List<ItemStack> output = recipe.resultItems(context);
        if (output.isEmpty() || output.get(0).isEmpty()) continue;

        CreativeModeTab itemGroup =
            tabs.getOrDefault(output.get(0).getItem(), CreativeModeTabs.getDefaultTab());
        collectionsByTab.computeIfAbsent(itemGroup, g -> new ArrayList<>()).add(collection);
        return;
      }
    });

    this.rbip$groupedCollections = collectionsByTab;
    this.rbip$tabInfoMap = tabInfoMap;
    this.rbip$sortedInfos = sorted;
  }

  @Override
  public List<RecipeCollection> rbip$getCollectionForTab(CreativeModeTab tab) {
    return this.rbip$groupedCollections.getOrDefault(tab, List.of());
  }

  @Override
  public Collection<RecipeBookComponent.TabInfo> rbip$allInfos() {
    return this.rbip$sortedInfos;
  }

  @Override
  public CreativeModeTab rbip$tabForInfo(RecipeBookComponent.TabInfo tab) {
    return this.rbip$tabInfoMap.get(tab);
  }
}
