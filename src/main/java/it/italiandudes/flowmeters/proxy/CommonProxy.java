package it.italiandudes.flowmeters.proxy;

import it.italiandudes.flowmeters.FlowMeters;
import it.italiandudes.flowmeters.integration.ComputerCraftIntegration;
import it.italiandudes.flowmeters.integration.ModIDs;
import it.italiandudes.flowmeters.network.Packets;
import it.italiandudes.flowmeters.tile.TileEntityEnergyMeterEU;
import it.italiandudes.flowmeters.tile.TileEntityEnergyMeterFE;
import it.italiandudes.flowmeters.tile.TileEntityEnergyMeterMJ;
import it.italiandudes.flowmeters.tile.TileEntityFluidMeter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy {
  public void preInit(FMLPreInitializationEvent event) { }

  public void init(FMLInitializationEvent event) {
    // Register tile entities
    GameRegistry.registerTileEntity(
        TileEntityEnergyMeterFE.class,
        new ResourceLocation(FlowMeters.MODID, "te_energy_meter_fe"));
    GameRegistry.registerTileEntity(
        TileEntityEnergyMeterMJ.class,
        new ResourceLocation(FlowMeters.MODID, "te_energy_meter_mj"));
    GameRegistry.registerTileEntity(
        TileEntityEnergyMeterEU.class,
        new ResourceLocation(FlowMeters.MODID, "te_energy_meter_eu"));
    GameRegistry.registerTileEntity(
            TileEntityFluidMeter.class,
            new ResourceLocation(FlowMeters.MODID, "te_fluid_meter"));

    // Register network packets
    Packets.register();
  }

  public void postInit(FMLPostInitializationEvent event) {
    if (Loader.isModLoaded(ModIDs.COMPUTERCRAFT)) {
      ComputerCraftIntegration.apply();
    }
  }

  public boolean handleEnergyBlockActivation(World world, BlockPos pos, EntityPlayer player) {
    return true;
  }
  public boolean handleFluidBlockActivation(World world, BlockPos pos, EntityPlayer player) {
    return true;
  }
}
