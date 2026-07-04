package dev.zenfyr.rbip;

import dev.zenfyr.rbip.compat.OwOCompat;
import dev.zenfyr.rbip.compat.PulsarCompat;
import java.lang.invoke.MethodHandles;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("rbip")
public class RecipeBookIsPain {

  public static final Logger LOGGER = LogManager.getLogger("RBIP");

  public static final boolean isOwOLoaded = ModList.get().isLoaded("owo");
  public static final boolean isPulsarLoaded = ModList.get().isLoaded("pulsar");

  public RecipeBookIsPain(IEventBus bus) {
    bus.addListener(this::onInitializeClient);
  }

  public void onInitializeClient(FMLClientSetupEvent event) {
    var lookup = MethodHandles.lookup();

    if (isOwOLoaded) {
      OwOCompat.init(lookup);
    }

    if (isPulsarLoaded) {
      PulsarCompat.init(lookup);
    }
  }
}
