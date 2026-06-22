package dev.zenfyr.rbip;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.zenfyr.rbip.access.PaginatedRecipeBookWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class RecipeBookPageButton extends Button {

  private static final ResourceLocation TEXTURE =
      new ResourceLocation("rbip", "textures/gui/recipe_book_buttons.png");

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
  public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
    this.isHovered = mouseX >= this.getX()
        && mouseY >= this.getY()
        && mouseX < this.getX() + this.width
        && mouseY < this.getY() + this.height;

    if (this.visible) {
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, TEXTURE);
      int u = this.active && this.isHovered() ? 28 : 0;
      int v = this.active ? 0 : 13;

      RenderSystem.enableDepthTest();
      this.renderTexture(
          context,
          TEXTURE,
          this.getX(),
          this.getY(),
          u + (next ? 14 : 0),
          v,
          0,
          this.width,
          this.height,
          256,
          256);
      if (this.isHovered && Minecraft.getInstance().screen != null) {
        context.renderTooltip(
            Minecraft.getInstance().font,
            Component.literal(((PaginatedRecipeBookWidget) widget).rbip$getPage() + 1 + "/"
                + ((PaginatedRecipeBookWidget) widget).rbip$getPageCount()),
            mouseX,
            mouseY);
      }
    }
  }
}
