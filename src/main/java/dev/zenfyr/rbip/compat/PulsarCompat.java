package dev.zenfyr.rbip.compat;

import dev.zenfyr.rbip.RecipeBookIsPain;
import dev.zenfyr.rbip.mixin.RecipeBookTabButtonAccessor;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Optional;
import lombok.SneakyThrows;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.world.item.CreativeModeTab;

public class PulsarCompat {

  private static MethodHandle getIconAnimation;
  private static MethodHandle animateIcon;

  @SneakyThrows
  public static boolean render(
      GuiGraphics context, int i, RecipeBookTabButton widget, CreativeModeTab group) {
    if (getIconAnimation == null || animateIcon == null) return false;
    Optional<?> opt = (Optional<?>) getIconAnimation.invoke(group);
    if (opt.isEmpty()) return false;

    animateIcon.invoke(
        opt.get(),
        group,
        context,
        widget.getX() + 9 + i,
        widget.getY() + 5,
        ((RecipeBookTabButtonAccessor) widget).rbip$isSelected(),
        false);
    return true;
  }

  public static void init(MethodHandles.Lookup lookup) {
    try {
      var iconClass = Class.forName("dev.zenfyr.pulsar.creativetab.CreativeModeTabAnimaton");

      getIconAnimation = lookup.findStatic(
          iconClass,
          "getIconAnimation",
          MethodType.methodType(Optional.class, CreativeModeTab.class));
      animateIcon = lookup.findVirtual(
          iconClass,
          "animateIcon",
          MethodType.methodType(
              void.class,
              CreativeModeTab.class,
              GuiGraphics.class,
              int.class,
              int.class,
              boolean.class,
              boolean.class));
    } catch (Exception e) {
      RecipeBookIsPain.LOGGER.error("Failed to prepare Pulsar compat.", e);
    }
  }
}
