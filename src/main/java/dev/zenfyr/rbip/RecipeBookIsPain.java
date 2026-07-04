package dev.zenfyr.rbip;

import dev.zenfyr.rbip.compat.OwOCompat;
import dev.zenfyr.rbip.compat.PulsarCompat;
import java.lang.invoke.MethodHandles;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("rbip")
public class RecipeBookIsPain {

  public static final Logger LOGGER = LogManager.getLogger("RBIP");

  public static final boolean isOwOLoaded = ModList.get().isLoaded("owo");
  public static final boolean isPulsarLoaded = ModList.get().isLoaded("pulsar");

  public RecipeBookIsPain(FMLJavaModLoadingContext context) {
    context.getModEventBus().addListener(this::onInitializeClient);
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
