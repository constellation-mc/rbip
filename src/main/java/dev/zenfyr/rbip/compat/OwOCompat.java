package dev.zenfyr.rbip.compat;

import dev.zenfyr.rbip.RecipeBookIsPain;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.world.item.CreativeModeTab;

public class OwOCompat {

  private static Class<?> groupClass;
  private static MethodHandle iconHandle;
  private static MethodHandle renderHandle;

  @SneakyThrows
  public static boolean render(
      GuiGraphics context, int i, RecipeBookTabButton widget, CreativeModeTab group) {
    if (groupClass == null || iconHandle == null || renderHandle == null) return false;
    if (!groupClass.isInstance(group)) return false;

    Minecraft client = Minecraft.getInstance();
    double e = client.mouseHandler.xpos()
        * client.getWindow().getGuiScaledWidth()
        / client.getWindow().getWidth();
    double f = client.mouseHandler.ypos()
        * client.getWindow().getScreenHeight()
        / client.getWindow().getHeight();
    var icon = iconHandle.invoke(group);
    renderHandle.invoke(
        icon,
        context,
        widget.getX() + 9 + i,
        widget.getY() + 5,
        (int) e,
        (int) f,
        client.getDeltaFrameTime());
    return true;
  }

  public static void init(MethodHandles.Lookup lookup) {
    try {
      groupClass = Class.forName("io.wispforest.owo.itemgroup.OwoItemGroup");
      var iconClass = Class.forName("io.wispforest.owo.itemgroup.Icon");

      iconHandle = lookup.findVirtual(groupClass, "icon", MethodType.methodType(iconClass));
      renderHandle = lookup.findVirtual(
          iconClass,
          "render",
          MethodType.methodType(
              void.class,
              GuiGraphics.class,
              int.class,
              int.class,
              int.class,
              int.class,
              float.class));
    } catch (Exception e) {
      RecipeBookIsPain.LOGGER.error("Failed to prepare OwO compat.", e);
    }
  }
}
