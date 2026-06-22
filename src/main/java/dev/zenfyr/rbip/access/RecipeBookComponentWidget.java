package dev.zenfyr.rbip.access;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface RecipeBookComponentWidget {

  default void rbip$updatePages() {
    throw new IllegalStateException("Interface not implemented");
  }

  default void rbip$updatePageSwitchButtons() {
    throw new IllegalStateException("Interface not implemented");
  }

  default int rbip$getPage() {
    throw new IllegalStateException("Interface not implemented");
  }

  default void rbip$setPage(int page) {
    throw new IllegalStateException("Interface not implemented");
  }

  default void rbip$incrementPage() {
    this.rbip$setPage(this.rbip$getPage() + 1);
  }

  default void rbip$decrementPage() {
    this.rbip$setPage(this.rbip$getPage() - 1);
  }

  default int rbip$getPageCount() {
    throw new IllegalStateException("Interface not implemented");
  }
}
