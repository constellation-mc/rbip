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
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.recipebook.RecipeGroupButtonWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeGroupButtonWidget.class)
public abstract class RecipeGroupButtonMixin extends ToggleButtonWidget
    implements RecipeGroupButtonWidgetDuck, PaginatedRecipeGroupButtonWidget {

  public RecipeGroupButtonMixin(int x, int y, int width, int height, boolean toggled) {
    super(x, y, width, height, toggled);
  }

  @Unique private int rbip$page = -1;

  @Unique private ItemGroup rbip$realGroup;

  @Override
  public int rbip$getPage() {
    return rbip$page;
  }

  @Override
  public void rbip$setPage(int page) {
    this.rbip$page = page;
  }

  @Override
  public void rbip$setRealItemGroup(ItemGroup group) {
    this.rbip$realGroup = group;
  }

  @Override
  public ItemGroup rbip$getRealItemGroup() {
    return this.rbip$realGroup;
  }

  @ModifyExpressionValue(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/recipebook/ClientRecipeBook;getResultsForGroup(Lnet/minecraft/client/recipebook/RecipeBookGroup;)Ljava/util/List;"),
      method = "checkForNewRecipes")
  private List<RecipeResultCollection> checkForNewRecipes(
      List<RecipeResultCollection> original, @Local ClientRecipeBook recipeBook) {
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
                  "Lnet/minecraft/client/recipebook/ClientRecipeBook;getResultsForGroup(Lnet/minecraft/client/recipebook/RecipeBookGroup;)Ljava/util/List;"),
      method = "hasKnownRecipes")
  private List<RecipeResultCollection> hasKnownRecipes(
      List<RecipeResultCollection> original, @Local(argsOnly = true) ClientRecipeBook recipeBook) {
    if (this.rbip$realGroup != null) {
      return ((ClientRecipeBookDuck) recipeBook).rbip$getResultsForGroup(this.rbip$realGroup);
    }
    return original;
  }

  @Inject(at = @At("HEAD"), method = "renderIcons", cancellable = true)
  private void rbip$render(DrawContext context, ItemRenderer itemRenderer, CallbackInfo ci) {
    if (this.rbip$realGroup == null) return;

    int i = this.toggled ? -2 : 0;

    if (RecipeBookIsPain.isOwOLoaded) {
      if (OwOCompat.render(context, i, (RecipeGroupButtonWidget) (Object) this, rbip$realGroup)) {
        ci.cancel();
        return;
      }
    }

    if (RecipeBookIsPain.isPulsarLoaded) {
      if (PulsarCompat.render(
          context, i, (RecipeGroupButtonWidget) (Object) this, rbip$realGroup)) {
        ci.cancel();
        return;
      }
    }

    ItemStack icon = this.rbip$realGroup.getIcon();
    if (!icon.isEmpty()) context.drawItemWithoutEntity(icon, this.getX() + 9 + i, this.getY() + 5);
    ci.cancel();
  }
}
