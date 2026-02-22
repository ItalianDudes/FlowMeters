package it.italiandudes.flowmeters.network;

import it.italiandudes.flowmeters.FlowMeters;
import net.minecraftforge.fml.relauncher.Side;

public class Packets {
  private static int packetId = 0;

  public static void register() {
    FlowMeters.NETWORK.registerMessage(
        PacketEnergyTransferRate.Handler.class,
        PacketEnergyTransferRate.class,
        packetId++,
        Side.CLIENT);

    FlowMeters.NETWORK.registerMessage(
        PacketFluidTransferRate.Handler.class,
        PacketFluidTransferRate.class,
        packetId++,
        Side.CLIENT
    );

    FlowMeters.NETWORK.registerMessage(
        PacketUpdateMeterSides.Handler.class,
        PacketUpdateMeterSides.class,
        packetId++,
        Side.SERVER);

    FlowMeters.NETWORK.registerMessage(
        PacketUpdateEnergyMeterConfig.Handler.class,
        PacketUpdateEnergyMeterConfig.class,
        packetId++,
        Side.SERVER);

    FlowMeters.NETWORK.registerMessage(
        PacketUpdateFluidMeterConfig.Handler.class,
        PacketUpdateFluidMeterConfig.class,
        packetId++,
        Side.SERVER);

    FlowMeters.NETWORK.registerMessage(
        PacketUpdateRateLimit.Handler.class,
        PacketUpdateRateLimit.class,
        packetId++,
        Side.SERVER);
  }

  private Packets() {}
}
