package dev.zenfyr.rbip.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.zenfyr.rbip.RecipeBookIsPain;
import dev.zenfyr.rbip.access.ClientRecipeBookDuck;
import dev.zenfyr.rbip.access.PaginatedRecipeGroupButtonWidget;
import dev.zenfyr.rbip.access.RecipeGroupButtonWidgetDuck;
import dev.zenfyr.rbip.compat.OwOCompat;
import dev.zenfyr.rbip.compat.PulsarCompat;
import java.util.List;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeBookTabButton.class)
public abstract class RecipeGroupButtonMixin extends StateSwitchingButton
    implements RecipeGroupButtonWidgetDuck, PaginatedRecipeGroupButtonWidget {

  public RecipeGroupButtonMixin(int x, int y, int width, int height, boolean toggled) {
    super(x, y, width, height, toggled);
  }

  @Unique private int rbip$page = -1;

  @Unique private CreativeModeTab rbip$realGroup;

  @Override
  public int rbip$getPage() {
    return rbip$page;
  }

  @Override
  public void rbip$setPage(int page) {
    this.rbip$page = page;
  }

  @Override
  public void rbip$setRealItemGroup(CreativeModeTab group) {
    this.rbip$realGroup = group;
  }

  @Override
  public CreativeModeTab rbip$getRealItemGroup() {
    return this.rbip$realGroup;
  }

  @ModifyExpressionValue(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/ClientRecipeBook;getCollection(Lnet/minecraft/client/RecipeBookCategories;)Ljava/util/List;"),
      method = "startAnimation")
  private List<RecipeCollection> checkForNewRecipes(
      List<RecipeCollection> original, @Local ClientRecipeBook recipeBook) {
    if (this.rbip$realGroup != null) {
      return ((ClientRecipeBookDuck) recipeBook).rbip$getResultsForGroup(this.rbip$realGroup);
    }
    return original;
  }

  @ModifyExpressionValue(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/ClientRecipeBook;getCollection(Lnet/minecraft/client/RecipeBookCategories;)Ljava/util/List;"),
      method = "updateVisibility")
  private List<RecipeCollection> hasKnownRecipes(
      List<RecipeCollection> original, @Local(argsOnly = true) ClientRecipeBook recipeBook) {
    if (this.rbip$realGroup != null) {
      return ((ClientRecipeBookDuck) recipeBook).rbip$getResultsForGroup(this.rbip$realGroup);
    }
    return original;
  }

  @Inject(at = @At("HEAD"), method = "renderIcon", cancellable = true)
  private void rbip$render(GuiGraphics context, ItemRenderer itemRenderer, CallbackInfo ci) {
    if (this.rbip$realGroup == null) return;

    int i = this.isStateTriggered ? -2 : 0;

    if (RecipeBookIsPain.isOwOLoaded) {
      if (OwOCompat.render(context, i, (RecipeBookTabButton) (Object) this, rbip$realGroup)) {
        ci.cancel();
        return;
      }
    }

    if (RecipeBookIsPain.isPulsarLoaded) {
      if (PulsarCompat.render(context, i, (RecipeBookTabButton) (Object) this, rbip$realGroup)) {
        ci.cancel();
        return;
      }
    }

    ItemStack icon = this.rbip$realGroup.getIconItem();
    if (!icon.isEmpty()) context.renderFakeItem(icon, this.getX() + 9 + i, this.getY() + 5);
    ci.cancel();
  }
}
