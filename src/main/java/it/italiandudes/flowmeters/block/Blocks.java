package it.italiandudes.flowmeters.block;

import it.italiandudes.flowmeters.FlowMeters;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

import java.util.Objects;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber
@ObjectHolder(FlowMeters.MODID)
public final class Blocks {

  @ObjectHolder(BlockEnergyMeter.NAME)
  public static BlockEnergyMeter ENERGY_METER;

  @ObjectHolder(BlockFluidMeter.NAME)
  public static BlockFluidMeter FLUID_METER;

  @SubscribeEvent
  public static void onRegisterBlockEvent(RegistryEvent.Register<Block> event) {
    event.getRegistry().register(new BlockEnergyMeter());
    event.getRegistry().register(new BlockFluidMeter());
  }

  @SubscribeEvent
  public static void onRegisterItemsEvent(RegistryEvent.Register<Item> event) {
    event.getRegistry().register(
        new ItemBlockEnergyMeter(ENERGY_METER)
            .setRegistryName(Objects.requireNonNull(ENERGY_METER.getRegistryName())));
    event.getRegistry().register(
        new ItemBlockFluidMeter(FLUID_METER)
                .setRegistryName(Objects.requireNonNull(FLUID_METER.getRegistryName())));
  }

  private Blocks() {}
}
