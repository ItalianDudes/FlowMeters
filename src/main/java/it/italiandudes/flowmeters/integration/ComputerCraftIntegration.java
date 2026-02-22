package it.italiandudes.flowmeters.integration;

import dan200.computercraft.api.ComputerCraftAPI;
import it.italiandudes.flowmeters.FlowMeters;

public class ComputerCraftIntegration {
  public static void apply() {
    ComputerCraftAPI.registerPeripheralProvider(new ComputerCraftPeripheralProvider());
    FlowMeters.LOGGER.info("Applied ComputerCraft integration");
  }
}
