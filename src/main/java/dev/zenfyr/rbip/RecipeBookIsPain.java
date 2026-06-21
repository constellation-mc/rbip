package dev.zenfyr.rbip;

import dev.zenfyr.rbip.compat.OwOCompat;
import dev.zenfyr.rbip.compat.PulsarCompat;
import java.lang.invoke.MethodHandles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RecipeBookIsPain implements ClientModInitializer {

  public static final Logger LOGGER = LogManager.getLogger("RBIP");

  public static final boolean isOwOLoaded = FabricLoader.getInstance().isModLoaded("owo");
  public static final boolean isPulsarLoaded = FabricLoader.getInstance().isModLoaded("pulsar");

  @Override
  public void onInitializeClient() {
    var lookup = MethodHandles.lookup();

    if (isOwOLoaded) {
      OwOCompat.init(lookup);
    }

    if (isPulsarLoaded) {
      PulsarCompat.init(lookup);
    }
  }
}
