package dev.zenfyr.rbip.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import dev.zenfyr.rbip.RecipeBookPageButton;
import dev.zenfyr.rbip.access.ClientRecipeBookDuck;
import dev.zenfyr.rbip.access.PaginatedRecipeBookWidget;
import dev.zenfyr.rbip.access.PaginatedRecipeGroupButtonWidget;
import dev.zenfyr.rbip.access.RecipeGroupButtonWidgetDuck;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeGroupButtonWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.recipebook.RecipeBookGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.registry.Registries;
import net.minecraft.screen.AbstractRecipeScreenHandler;
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

@Mixin(value = RecipeBookWidget.class, priority = 1001)
public abstract class RecipeBookWidgetMixin implements PaginatedRecipeBookWidget {
  @Shadow
  protected MinecraftClient client;

  @Shadow
  @Final
  private List<RecipeGroupButtonWidget> tabButtons;

  @Shadow
  protected AbstractRecipeScreenHandler<?> craftingScreenHandler;

  @Shadow
  private int parentWidth;

  @Shadow
  private int parentHeight;

  @Shadow
  private int leftOffset;

  @Shadow
  public abstract boolean isOpen();

  @Shadow
  @Final
  public static int field_32408;

  @Shadow
  @Final
  public static int field_32409;

  @Shadow
  @Nullable private RecipeGroupButtonWidget currentTab;

  @Shadow
  private ClientRecipeBook recipeBook;

  @Unique private int rbip$page = 0;

  @Unique private int rbip$pages;

  @Unique private RecipeBookPageButton rbip$nextPageButton;

  @Unique private RecipeBookPageButton rbip$prevPageButton;

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/gui/screen/recipebook/RecipeGroupButtonWidget;setToggled(Z)V",
              shift = At.Shift.BEFORE),
      method = "reset")
  private void dark_matter$reset(CallbackInfo ci) {
    int a = (this.parentWidth - rbip$horizontalOffset()) / 2 - this.leftOffset;
    int s = (this.parentHeight - rbip$verticalOffset()) / 2;
    this.rbip$nextPageButton =
        new RecipeBookPageButton(a + 18, s - 13, (RecipeBookWidget) (Object) this, true);
    this.rbip$prevPageButton =
        new RecipeBookPageButton(a + 3, s - 13, (RecipeBookWidget) (Object) this, false);
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V",
              shift = At.Shift.BEFORE),
      method = "render")
  private void dark_matter$render(
      DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
    this.rbip$prevPageButton.render(context, mouseX, mouseY, delta);
    this.rbip$nextPageButton.render(context, mouseX, mouseY, delta);
  }

  @Inject(at = @At("HEAD"), method = "mouseClicked", cancellable = true)
  private void dark_matter$mouseClicked(
      double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
    if (this.client.player != null)
      if (this.isOpen() && !this.client.player.isSpectator()) {
        if (this.rbip$nextPageButton.mouseClicked(mouseX, mouseY, button)) {
          this.rbip$incrementPage();
          cir.setReturnValue(true);
        } else if (this.rbip$prevPageButton.mouseClicked(mouseX, mouseY, button)) {
          this.rbip$decrementPage();
          cir.setReturnValue(true);
        }
      }
  }

  @Inject(at = @At("TAIL"), method = "refreshResults")
  private void dark_matter$refreshResults(boolean resetCurrentPage, CallbackInfo ci) {
    if (resetCurrentPage && this.currentTab != null) {
      if (this.rbip$getPage()
          != ((PaginatedRecipeGroupButtonWidget) this.currentTab).rbip$getPage()) {
        this.rbip$setPage(
            Math.max(((PaginatedRecipeGroupButtonWidget) this.currentTab).rbip$getPage(), 0));
      }
    }
  }

  @ModifyExpressionValue(
      at =
          @At(
              value = "FIELD",
              target =
                  "Lnet/minecraft/client/recipebook/RecipeBookGroup;CRAFTING_SEARCH:Lnet/minecraft/client/recipebook/RecipeBookGroup;"),
      method = "refreshTabButtons",
      require = 0)
  private RecipeBookGroup dark_matter$refresh$correctGroup(
      RecipeBookGroup group, @Local RecipeGroupButtonWidget widget) {
    return RecipeBookGroup.SEARCH_MAP.containsKey(widget.getCategory())
        ? widget.getCategory()
        : group;
  }

  @ModifyArg(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/gui/screen/recipebook/RecipeGroupButtonWidget;setPosition(II)V"),
      method = "refreshTabButtons",
      index = 1)
  private int dark_matter$refresh$setPos(
      int y,
      @Local RecipeGroupButtonWidget widget,
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
                  "Lnet/minecraft/client/gui/screen/recipebook/RecipeGroupButtonWidget;setPosition(II)V",
              shift = At.Shift.AFTER),
      method = "refreshTabButtons")
  private void dark_matter$refresh$setPos(
      CallbackInfo ci,
      @Local RecipeGroupButtonWidget widget,
      @Share("index") LocalIntRef index,
      @Share("wc") LocalIntRef wc) {
    ((PaginatedRecipeGroupButtonWidget) widget).rbip$setPage((int) Math.floor(wc.get() / 6f));
    if (index.get() == 6) index.set(0);
    wc.set(wc.get() + 1);
  }

  @Inject(at = @At("TAIL"), method = "refreshTabButtons")
  private void dark_matter$refresh$tail(CallbackInfo ci, @Share("wc") LocalIntRef wc) {
    this.rbip$pages = (int) Math.ceil(wc.get() / 6f);
    this.rbip$updatePages();
    this.rbip$updatePageSwitchButtons();
  }

  @Unique private static int rbip$horizontalOffset() {
    return field_32408;
  }

  @Unique private static int rbip$verticalOffset() {
    return field_32409;
  }

  @Unique @Override
  public void rbip$updatePages() {
    for (RecipeGroupButtonWidget widget : this.tabButtons) {
      widget.visible = ((PaginatedRecipeGroupButtonWidget) widget).rbip$getPage() == this.rbip$page;
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
      method = "reset")
  private void init(CallbackInfo ci) {
    if (this.craftingScreenHandler.getCategory() != RecipeBookCategory.CRAFTING) return;

    var search = RecipeBookGroup.getGroups(this.craftingScreenHandler.getCategory()).stream()
        .filter(RecipeBookGroup.SEARCH_MAP::containsKey)
        .findFirst();
    search.ifPresent(
        recipeBookGroup -> this.tabButtons.add(new RecipeGroupButtonWidget(recipeBookGroup)));

    Registries.ITEM_GROUP.stream()
        .filter(itemGroup -> !itemGroup.isSpecial())
        .forEach(itemGroup -> {
          var widget = new RecipeGroupButtonWidget(RecipeBookGroup.CRAFTING_MISC);
          ((RecipeGroupButtonWidgetDuck) widget).rbip$setRealItemGroup(itemGroup);
          this.tabButtons.add(widget);
        });
  }

  @WrapOperation(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/recipebook/RecipeBookGroup;equals(Ljava/lang/Object;)Z"),
      method = "method_2582")
  private boolean checkTabInEquals(
      RecipeBookGroup instance,
      Object o,
      Operation<Boolean> original,
      @Local(argsOnly = true) RecipeGroupButtonWidget widget) {
    if (((RecipeGroupButtonWidgetDuck) widget).rbip$getRealItemGroup() != null
        && this.currentTab != null) {
      return Objects.equals(
          ((RecipeGroupButtonWidgetDuck) widget).rbip$getRealItemGroup(),
          ((RecipeGroupButtonWidgetDuck) this.currentTab).rbip$getRealItemGroup());
    }
    return original.call(instance, o);
  }

  @ModifyExpressionValue(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/recipebook/RecipeBookGroup;getGroups(Lnet/minecraft/recipe/book/RecipeBookCategory;)Ljava/util/List;"),
      method = "reset")
  private List<RecipeBookGroup> skipRealButtons(List<RecipeBookGroup> original) {
    return this.craftingScreenHandler.getCategory() == RecipeBookCategory.CRAFTING
        ? List.of()
        : original;
  }

  @ModifyExpressionValue(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/recipebook/ClientRecipeBook;getResultsForGroup(Lnet/minecraft/client/recipebook/RecipeBookGroup;)Ljava/util/List;"),
      method = "refreshResults")
  private List<RecipeResultCollection> refreshResults(List<RecipeResultCollection> original) {
    var real = ((RecipeGroupButtonWidgetDuck) this.currentTab).rbip$getRealItemGroup();
    if (real != null) {
      return ((ClientRecipeBookDuck) this.recipeBook).rbip$getResultsForGroup(real);
    }
    return original;
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V",
              shift = At.Shift.BEFORE),
      method = "render")
  private void rbip$renderTooltip(
      DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
    if (client.currentScreen == null) return;
    if (this.craftingScreenHandler.getCategory() != RecipeBookCategory.CRAFTING) return;

    this.tabButtons.stream()
        .filter(widget -> widget.visible && widget.isHovered())
        .forEach(widget -> {
          if (RecipeBookGroup.SEARCH_MAP.containsKey(widget.getCategory())) {
            context.drawTooltip(
                client.textRenderer, ItemGroups.getSearchGroup().getDisplayName(), mouseX, mouseY);
          } else {
            Optional.ofNullable(((RecipeGroupButtonWidgetDuck) widget).rbip$getRealItemGroup())
                .map(ItemGroup::getDisplayName)
                .ifPresent(text -> context.drawTooltip(client.textRenderer, text, mouseX, mouseY));
          }
        });
  }
}
