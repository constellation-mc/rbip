package dev.zenfyr.rbip.access;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface PaginatedRecipeGroupButtonWidget {

  default int rbip$getPage() {
    throw new IllegalStateException("Interface not implemented");
  }

  default void rbip$setPage(int page) {
    throw new IllegalStateException("Interface not implemented");
  }
}
