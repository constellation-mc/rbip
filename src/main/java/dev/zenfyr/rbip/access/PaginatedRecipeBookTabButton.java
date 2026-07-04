package dev.zenfyr.rbip.access;

public interface PaginatedRecipeBookTabButton {

  default int rbip$getPage() {
    throw new IllegalStateException("Interface not implemented");
  }

  default void rbip$setPage(int page) {
    throw new IllegalStateException("Interface not implemented");
  }
}
