package dev.zenfyr.rbip;

import dev.zenfyr.rbip.access.RecipeBookComponentWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class RecipeBookPageButton extends Button {

  private static final Identifier TEXTURE =
      Identifier.fromNamespaceAndPath("rbip", "textures/gui/recipe_book_buttons.png");

  private final boolean next;
  private final RecipeBookComponent widget;

  public RecipeBookPageButton(int x, int y, RecipeBookComponent widget, boolean next) {
    super(
        x,
        y,
        14,
        13,
        next ? Component.literal(">") : Component.literal("<"),
        button -> {},
        DEFAULT_NARRATION);
    this.widget = widget;
    this.next = next;
  }

  @Override
  protected void renderContents(GuiGraphics context, int mouseX, int mouseY, float delta) {
    this.isHovered = mouseX >= this.getX()
        && mouseY >= this.getY()
        && mouseX < this.getX() + this.width
        && mouseY < this.getY() + this.height;

    if (this.visible) {
      int u = this.active && this.isHovered() ? 28 : 0;
      int v = this.active ? 0 : 13;

      context.blit(
          RenderPipelines.GUI_TEXTURED,
          TEXTURE,
          this.getX(),
          this.getY(),
          u + (next ? 14 : 0),
          v,
          this.width,
          this.height,
          256,
          256);

      if (this.isHovered && Minecraft.getInstance().screen != null) {
        context.setTooltipForNextFrame(
            Minecraft.getInstance().font,
            Component.literal(((RecipeBookComponentWidget) widget).rbip$getPage() + 1 + "/"
                + ((RecipeBookComponentWidget) widget).rbip$getPageCount()),
            mouseX,
            mouseY);
      }
    }
  }
}
