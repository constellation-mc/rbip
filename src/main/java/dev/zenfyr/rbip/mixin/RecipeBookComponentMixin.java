package dev.zenfyr.rbip.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.zenfyr.rbip.RecipeBookPageButton;
import dev.zenfyr.rbip.access.ClientRecipeBookDuck;
import dev.zenfyr.rbip.access.PaginatedRecipeBookTabButton;
import dev.zenfyr.rbip.access.RecipeBookComponentWidget;
import dev.zenfyr.rbip.access.RecipeBookTabButtonDuck;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.crafting.ExtendedRecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RecipeBookComponent.class, priority = 1001)
public abstract class RecipeBookComponentMixin implements RecipeBookComponentWidget {

  @Shadow
  protected Minecraft minecraft;

  @Shadow
  @Final
  private List<RecipeBookTabButton> tabButtons;

  @Shadow
  protected RecipeBookMenu menu;

  @Shadow
  public abstract boolean isVisible();

  @Shadow
  @Nullable private RecipeBookTabButton selectedTab;

  @Shadow
  private ClientRecipeBook book;

  @Shadow
  @Final
  private List<RecipeBookComponent.TabInfo> tabInfos;

  @Shadow
  protected abstract void onTabButtonPress(Button button);

  @Unique private int rbip$page = 0;

  @Unique private int rbip$pages;

  @Unique private RecipeBookPageButton rbip$nextPageButton;

  @Unique private RecipeBookPageButton rbip$prevPageButton;

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/inventory/RecipeBookMenu;fillCraftSlotsStackedContents(Lnet/minecraft/world/entity/player/StackedItemContents;)V"),
      method = "initVisuals")
  private void dark_matter$reset(
      CallbackInfo ci, @Local(index = 2) int xo, @Local(index = 3) int yo) {
    this.rbip$nextPageButton =
        new RecipeBookPageButton(xo + 18, yo - 13, (RecipeBookComponent) (Object) this, true);
    this.rbip$prevPageButton =
        new RecipeBookPageButton(xo + 3, yo - 13, (RecipeBookComponent) (Object) this, false);
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookPage;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIF)V",
              shift = At.Shift.AFTER),
      method = "extractRenderState")
  private void dark_matter$render(
      GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
    this.rbip$prevPageButton.extractRenderState(graphics, mouseX, mouseY, delta);
    this.rbip$nextPageButton.extractRenderState(graphics, mouseX, mouseY, delta);
  }

  @Inject(at = @At("HEAD"), method = "mouseClicked", cancellable = true)
  private void dark_matter$mouseClicked(
      MouseButtonEvent mouseButtonEvent, boolean bl, CallbackInfoReturnable<Boolean> cir) {
    if (this.minecraft.player != null)
      if (this.isVisible() && !this.minecraft.player.isSpectator()) {
        if (this.rbip$nextPageButton.mouseClicked(mouseButtonEvent, bl)) {
          this.rbip$incrementPage();
          cir.setReturnValue(true);
        } else if (this.rbip$prevPageButton.mouseClicked(mouseButtonEvent, bl)) {
          this.rbip$decrementPage();
          cir.setReturnValue(true);
        }
      }
  }

  @Inject(at = @At("TAIL"), method = "updateCollections")
  private void dark_matter$refreshResults(
      boolean resetCurrentPage, boolean isFiltering, CallbackInfo ci) {
    if (resetCurrentPage && this.selectedTab != null) {
      if (this.rbip$getPage() != ((PaginatedRecipeBookTabButton) this.selectedTab).rbip$getPage()) {
        this.rbip$setPage(
            Math.max(((PaginatedRecipeBookTabButton) this.selectedTab).rbip$getPage(), 0));
      }
    }
  }

  @ModifyArg(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookTabButton;setPosition(II)V"),
      method = "updateTabs",
      index = 1)
  private int dark_matter$refresh$setPos(
      int y,
      @Local RecipeBookTabButton widget,
      @Local(ordinal = 1) int j,
      @Share("index") LocalIntRef index) {
    int pos = j + widget.getHeight() * index.get();
    index.set(index.get() + 1);
    return pos;
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookTabButton;setPosition(II)V",
              shift = At.Shift.AFTER),
      method = "updateTabs")
  private void dark_matter$refresh$setPos(
      CallbackInfo ci,
      @Local RecipeBookTabButton widget,
      @Share("index") LocalIntRef index,
      @Share("wc") LocalIntRef wc) {
    ((PaginatedRecipeBookTabButton) widget).rbip$setPage((int) Math.floor(wc.get() / 6f));
    if (index.get() == 6) index.set(0);
    wc.set(wc.get() + 1);
  }

  @Inject(at = @At("TAIL"), method = "updateTabs")
  private void dark_matter$refresh$tail(CallbackInfo ci, @Share("wc") LocalIntRef wc) {
    this.rbip$pages = (int) Math.ceil(wc.get() / 6f);
    this.rbip$updatePages();
    this.rbip$updatePageSwitchButtons();
  }

  @Unique @Override
  public void rbip$updatePages() {
    for (RecipeBookTabButton widget : this.tabButtons) {
      widget.visible = ((PaginatedRecipeBookTabButton) widget).rbip$getPage() == this.rbip$page;
    }
  }

  @Unique @Override
  public void rbip$updatePageSwitchButtons() {
    if (this.rbip$nextPageButton != null) {
      this.rbip$nextPageButton.visible = this.rbip$getPageCount() > 1;
      this.rbip$nextPageButton.active = this.rbip$getPage() < (this.rbip$getPageCount() - 1);
    }
    if (this.rbip$prevPageButton != null) {
      this.rbip$prevPageButton.visible = this.rbip$getPageCount() > 1;
      this.rbip$prevPageButton.active = this.rbip$getPage() > 0;
    }
  }

  @Unique @Override
  public int rbip$getPage() {
    return this.rbip$page;
  }

  @Unique @Override
  public void rbip$setPage(int page) {
    if (page < 0) page = 0;
    if (page > rbip$pages - 1) page = rbip$pages - 1;

    this.rbip$page = page;
    rbip$updatePages();
    rbip$updatePageSwitchButtons();
  }

  @Unique @Override
  public int rbip$getPageCount() {
    return this.rbip$pages;
  }

  //
  // The Hacky part is here!
  //

  @Inject(
      at = @At("CTOR_HEAD"),
      method = "<init>(Lnet/minecraft/world/inventory/RecipeBookMenu;Ljava/util/List;)V")
  private void init(
      RecipeBookMenu recipeBookMenu,
      List<RecipeBookComponent.TabInfo> _l,
      CallbackInfo ci,
      @Local(argsOnly = true) LocalRef<List<RecipeBookComponent.TabInfo>> list) {
    if (recipeBookMenu.getRecipeBookType() == RecipeBookType.CRAFTING) {
      var minecraft = Minecraft.getInstance();
      List<RecipeBookComponent.TabInfo> tabInfos1 = new ArrayList<>();
      tabInfos1.add(new RecipeBookComponent.TabInfo(SearchRecipeBookCategory.CRAFTING));
      tabInfos1.addAll(((ClientRecipeBookDuck) minecraft.player.getRecipeBook()).rbip$allInfos());
      list.set(tabInfos1);
    }
  }

  @Inject(
      at = @At(value = "INVOKE", target = "Ljava/util/List;clear()V", shift = At.Shift.AFTER),
      method = "initVisuals")
  private void init(CallbackInfo ci) {
    if (this.menu.getRecipeBookType() != RecipeBookType.CRAFTING) return;

    for (RecipeBookComponent.TabInfo tabInfo : this.tabInfos) {
      var button = new RecipeBookTabButton(0, 0, tabInfo, this::onTabButtonPress);
      var tab = ((ClientRecipeBookDuck) minecraft.player.getRecipeBook()).rbip$tabForInfo(tabInfo);
      ((RecipeBookTabButtonDuck) button).rbip$setCreativeTab(tab);
      this.tabButtons.add(button);
    }
  }

  @WrapOperation(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/item/crafting/ExtendedRecipeBookCategory;equals(Ljava/lang/Object;)Z"),
      method = "lambda$initVisuals$3")
  private boolean checkTabInEquals(
      ExtendedRecipeBookCategory instance,
      Object o,
      Operation<Boolean> original,
      @Local(argsOnly = true) RecipeBookTabButton widget) {
    if (((RecipeBookTabButtonDuck) widget).rbip$getCreativeTab() != null
        && this.selectedTab != null) {
      return Objects.equals(
          ((RecipeBookTabButtonDuck) widget).rbip$getCreativeTab(),
          ((RecipeBookTabButtonDuck) this.selectedTab).rbip$getCreativeTab());
    }
    return original.call(instance, o);
  }

  @ModifyExpressionValue(
      at =
          @At(
              value = "FIELD",
              target =
                  "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;tabInfos:Ljava/util/List;",
              opcode = Opcodes.GETFIELD),
      method = "initVisuals")
  private List<RecipeBookCategories> skipRealButtons(List<RecipeBookCategories> original) {
    return this.menu.getRecipeBookType() == RecipeBookType.CRAFTING ? List.of() : original;
  }

  @ModifyExpressionValue(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/ClientRecipeBook;getCollection(Lnet/minecraft/world/item/crafting/ExtendedRecipeBookCategory;)Ljava/util/List;"),
      method = "updateCollections")
  private List<RecipeCollection> refreshResults(List<RecipeCollection> original) {
    var real = ((RecipeBookTabButtonDuck) this.selectedTab).rbip$getCreativeTab();
    if (real != null) {
      return ((ClientRecipeBookDuck) this.book).rbip$getCollectionForTab(real);
    }
    return original;
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/gui/screens/recipebook/GhostSlots;extractTooltip(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/Minecraft;IILnet/minecraft/world/inventory/Slot;)V",
              shift = At.Shift.AFTER),
      method = "extractTooltip")
  private void rbip$renderTooltip(
      GuiGraphicsExtractor context, int i, int j, Slot slot, CallbackInfo ci) {
    if (minecraft.screen == null) return;
    if (this.menu.getRecipeBookType() != RecipeBookType.CRAFTING) return;

    this.tabButtons.stream()
        .filter(widget -> widget.visible && widget.isHovered())
        .forEach(widget -> {
          if (widget.getCategory() instanceof SearchRecipeBookCategory) {
            context.setTooltipForNextFrame(
                minecraft.font, CreativeModeTabs.searchTab().getDisplayName(), i, j);
          } else {
            Optional.ofNullable(((RecipeBookTabButtonDuck) widget).rbip$getCreativeTab())
                .map(CreativeModeTab::getDisplayName)
                .ifPresent(text -> context.setTooltipForNextFrame(minecraft.font, text, i, j));
          }
        });
  }
}
