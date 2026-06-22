package dev.zenfyr.rbip.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import dev.zenfyr.rbip.RecipeBookPageButton;
import dev.zenfyr.rbip.access.ClientRecipeBookDuck;
import dev.zenfyr.rbip.access.PaginatedRecipeBookTabButton;
import dev.zenfyr.rbip.access.RecipeBookComponentWidget;
import dev.zenfyr.rbip.access.RecipeBookTabButtonDuck;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.jetbrains.annotations.Nullable;
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
  protected RecipeBookMenu<?> menu;

  @Shadow
  private int width;

  @Shadow
  private int height;

  @Shadow
  private int xOffset;

  @Shadow
  public abstract boolean isVisible();

  @Shadow
  @Final
  public static int IMAGE_WIDTH;

  @Shadow
  @Final
  public static int IMAGE_HEIGHT;

  @Shadow
  @Nullable private RecipeBookTabButton selectedTab;

  @Shadow
  private ClientRecipeBook book;

  @Unique private int rbip$page = 0;

  @Unique private int rbip$pages;

  @Unique private RecipeBookPageButton rbip$nextPageButton;

  @Unique private RecipeBookPageButton rbip$prevPageButton;

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookTabButton;setStateTriggered(Z)V",
              shift = At.Shift.BEFORE),
      method = "initVisuals")
  private void dark_matter$reset(CallbackInfo ci) {
    int a = (this.width - rbip$horizontalOffset()) / 2 - this.xOffset;
    int s = (this.height - rbip$verticalOffset()) / 2;
    this.rbip$nextPageButton =
        new RecipeBookPageButton(a + 18, s - 13, (RecipeBookComponent) (Object) this, true);
    this.rbip$prevPageButton =
        new RecipeBookPageButton(a + 3, s - 13, (RecipeBookComponent) (Object) this, false);
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V",
              shift = At.Shift.BEFORE),
      method = "render")
  private void dark_matter$render(
      GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
    this.rbip$prevPageButton.render(context, mouseX, mouseY, delta);
    this.rbip$nextPageButton.render(context, mouseX, mouseY, delta);
  }

  @Inject(at = @At("HEAD"), method = "mouseClicked", cancellable = true)
  private void dark_matter$mouseClicked(
      double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
    if (this.minecraft.player != null)
      if (this.isVisible() && !this.minecraft.player.isSpectator()) {
        if (this.rbip$nextPageButton.mouseClicked(mouseX, mouseY, button)) {
          this.rbip$incrementPage();
          cir.setReturnValue(true);
        } else if (this.rbip$prevPageButton.mouseClicked(mouseX, mouseY, button)) {
          this.rbip$decrementPage();
          cir.setReturnValue(true);
        }
      }
  }

  @Inject(at = @At("TAIL"), method = "updateCollections")
  private void dark_matter$refreshResults(boolean resetCurrentPage, CallbackInfo ci) {
    if (resetCurrentPage && this.selectedTab != null) {
      if (this.rbip$getPage() != ((PaginatedRecipeBookTabButton) this.selectedTab).rbip$getPage()) {
        this.rbip$setPage(
            Math.max(((PaginatedRecipeBookTabButton) this.selectedTab).rbip$getPage(), 0));
      }
    }
  }

  @ModifyExpressionValue(
      at =
          @At(
              value = "FIELD",
              target =
                  "Lnet/minecraft/client/RecipeBookCategories;CRAFTING_SEARCH:Lnet/minecraft/client/RecipeBookCategories;"),
      method = "updateTabs",
      require = 0)
  private RecipeBookCategories dark_matter$refresh$correctGroup(
      RecipeBookCategories group, @Local RecipeBookTabButton widget) {
    return RecipeBookCategories.AGGREGATE_CATEGORIES.containsKey(widget.getCategory())
        ? widget.getCategory()
        : group;
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

  @Unique private static int rbip$horizontalOffset() {
    return IMAGE_WIDTH;
  }

  @Unique private static int rbip$verticalOffset() {
    return IMAGE_HEIGHT;
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
      at = @At(value = "INVOKE", target = "Ljava/util/List;clear()V", shift = At.Shift.AFTER),
      method = "initVisuals")
  private void init(CallbackInfo ci) {
    if (this.menu.getRecipeBookType() != RecipeBookType.CRAFTING) return;

    var search = RecipeBookCategories.getCategories(this.menu.getRecipeBookType()).stream()
        .filter(RecipeBookCategories.AGGREGATE_CATEGORIES::containsKey)
        .findFirst();
    search.ifPresent(
        recipeBookGroup -> this.tabButtons.add(new RecipeBookTabButton(recipeBookGroup)));

    BuiltInRegistries.CREATIVE_MODE_TAB.stream()
        .filter(itemGroup -> !itemGroup.isAlignedRight())
        .forEach(itemGroup -> {
          var widget = new RecipeBookTabButton(RecipeBookCategories.CRAFTING_MISC);
          ((RecipeBookTabButtonDuck) widget).rbip$setCreativeTab(itemGroup);
          this.tabButtons.add(widget);
        });
  }

  @WrapOperation(
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/client/RecipeBookCategories;equals(Ljava/lang/Object;)Z"),
      method = "method_2582")
  private boolean checkTabInEquals(
      RecipeBookCategories instance,
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
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/RecipeBookCategories;getCategories(Lnet/minecraft/world/inventory/RecipeBookType;)Ljava/util/List;"),
      method = "initVisuals")
  private List<RecipeBookCategories> skipRealButtons(List<RecipeBookCategories> original) {
    return this.menu.getRecipeBookType() == RecipeBookType.CRAFTING ? List.of() : original;
  }

  @ModifyExpressionValue(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/ClientRecipeBook;getCollection(Lnet/minecraft/client/RecipeBookCategories;)Ljava/util/List;"),
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
              target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V",
              shift = At.Shift.BEFORE),
      method = "render")
  private void rbip$renderTooltip(
      GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
    if (minecraft.screen == null) return;
    if (this.menu.getRecipeBookType() != RecipeBookType.CRAFTING) return;

    this.tabButtons.stream()
        .filter(widget -> widget.visible && widget.isHovered())
        .forEach(widget -> {
          if (RecipeBookCategories.AGGREGATE_CATEGORIES.containsKey(widget.getCategory())) {
            context.renderTooltip(
                minecraft.font, CreativeModeTabs.searchTab().getDisplayName(), mouseX, mouseY);
          } else {
            Optional.ofNullable(((RecipeBookTabButtonDuck) widget).rbip$getCreativeTab())
                .map(CreativeModeTab::getDisplayName)
                .ifPresent(text -> context.renderTooltip(minecraft.font, text, mouseX, mouseY));
          }
        });
  }
}
