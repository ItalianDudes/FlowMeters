package it.italiandudes.flowmeters.energy;

import it.italiandudes.flowmeters.integration.ModIDs;
import net.minecraftforge.fml.common.Loader;

public class EnergyTypeMJ extends EnergyType {
  EnergyTypeMJ() {
    super("MJ", "Minecraft Joule");
  }

  @Override
  public boolean isAvailable() {
    return Loader.isModLoaded(ModIDs.BUILDCRAFT);
  }

  @Override
  public boolean isLimitable() {
    return true;
  }
}
