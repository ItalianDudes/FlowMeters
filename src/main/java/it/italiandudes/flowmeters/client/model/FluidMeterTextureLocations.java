package it.italiandudes.flowmeters.client.model;

import it.italiandudes.flowmeters.FlowMeters;
import net.minecraft.util.ResourceLocation;

public class FluidMeterTextureLocations {

  public static final ResourceLocation SIDE = new ResourceLocation(FlowMeters.MODID, "blocks/fluid_meter");
  public static final ResourceLocation INPUT = new ResourceLocation(FlowMeters.MODID, "blocks/fluid_meter_input");
  public static final ResourceLocation OUTPUT = new ResourceLocation(FlowMeters.MODID, "blocks/fluid_meter_output");
  public static final ResourceLocation SCREEN = new ResourceLocation(FlowMeters.MODID, "blocks/fluid_meter_screen");

  public static ResourceLocation getGuiResource(ResourceLocation location) {
    String path = location.getPath();
    return new ResourceLocation(location.getNamespace(), String.format("textures/%s.png", path));
  }
}
