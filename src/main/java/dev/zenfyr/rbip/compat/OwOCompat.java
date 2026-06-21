package dev.zenfyr.rbip.compat;

import dev.zenfyr.rbip.RecipeBookIsPain;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import lombok.SneakyThrows;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.recipebook.RecipeGroupButtonWidget;
import net.minecraft.item.ItemGroup;

public class OwOCompat {

  private static Class<?> groupClass;
  private static MethodHandle iconHandle;
  private static MethodHandle renderHandle;

  @SneakyThrows
  public static boolean render(
      DrawContext context, int i, RecipeGroupButtonWidget widget, ItemGroup group) {
    if (groupClass == null || iconHandle == null || renderHandle == null) return false;
    if (!groupClass.isInstance(group)) return false;

    MinecraftClient client = MinecraftClient.getInstance();
    double e = client.mouse.getX()
        * client.getWindow().getScaledWidth()
        / client.getWindow().getWidth();
    double f = client.mouse.getY()
        * client.getWindow().getScaledHeight()
        / client.getWindow().getHeight();
    var icon = iconHandle.invoke(group);
    renderHandle.invoke(
        icon,
        context,
        widget.getX() + 9 + i,
        widget.getY() + 5,
        (int) e,
        (int) f,
        client.getTickDelta());
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
              DrawContext.class,
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
