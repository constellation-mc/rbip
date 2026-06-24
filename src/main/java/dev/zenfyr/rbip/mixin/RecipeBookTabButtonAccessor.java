package dev.zenfyr.rbip.mixin;

import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeBookTabButton.class)
public interface RecipeBookTabButtonAccessor {

  @Accessor("selected")
  boolean rbip$isSelected();
}
