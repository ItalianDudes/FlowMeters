package it.italiandudes.flowmeters.proxy;

import it.italiandudes.flowmeters.block.BlockEnergyMeter;
import it.italiandudes.flowmeters.block.BlockEnergyMeter.MeterType;
import it.italiandudes.flowmeters.block.Blocks;
import it.italiandudes.flowmeters.client.EnergyMeterScreenRenderer;
import it.italiandudes.flowmeters.client.FluidMeterScreenRenderer;
import it.italiandudes.flowmeters.client.gui.GuiEnergyMeter;
import it.italiandudes.flowmeters.client.gui.GuiFluidMeter;
import it.italiandudes.flowmeters.client.model.EnergyMeterBakedModel;
import it.italiandudes.flowmeters.client.model.FluidMeterBakedModel;
import it.italiandudes.flowmeters.client.model.TexturedQuadCache;
import it.italiandudes.flowmeters.tile.TileEntityEnergyMeterBase;
import it.italiandudes.flowmeters.tile.TileEntityFluidMeterBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

  @Override
  public void preInit(FMLPreInitializationEvent event) {
    super.preInit(event);
    MinecraftForge.EVENT_BUS.register(this);
  }

  @Override
  public void init(FMLInitializationEvent event) {
    super.init(event);
    ClientRegistry.bindTileEntitySpecialRenderer(
        TileEntityEnergyMeterBase.class, new EnergyMeterScreenRenderer());
    ClientRegistry.bindTileEntitySpecialRenderer(
        TileEntityFluidMeterBase.class, new FluidMeterScreenRenderer());
  }

  @Override
  public void postInit(FMLPostInitializationEvent event) {
    super.postInit(event);
  }

  @Override
  public boolean handleEnergyBlockActivation(World world, BlockPos pos, EntityPlayer player) {
    if (!world.isRemote) {
      return true;
    }

    TileEntity tile = world.getTileEntity(pos);
    if (tile instanceof TileEntityEnergyMeterBase) {
      Minecraft.getMinecraft().displayGuiScreen(new GuiEnergyMeter((TileEntityEnergyMeterBase) tile));
      return true;
    }

    return false;
  }
  @Override
  public boolean handleFluidBlockActivation(World world, BlockPos pos, EntityPlayer player) {
    if (!world.isRemote) {
      return true;
    }

    TileEntity tile = world.getTileEntity(pos);
    if (tile instanceof TileEntityFluidMeterBase) {
      Minecraft.getMinecraft().displayGuiScreen(new GuiFluidMeter((TileEntityFluidMeterBase) tile));
      return true;
    }

    return false;
  }

  /**
   * Listener for the {@link ModelRegistryEvent} client-side event. All block item models are
   * registered here.
   */
  @SubscribeEvent
  public void onRegisterModels(ModelRegistryEvent event) {
    Blocks.ENERGY_METER.registerItemModel(Item.getItemFromBlock(Blocks.ENERGY_METER));
    Blocks.FLUID_METER.registerItemModel(Item.getItemFromBlock(Blocks.FLUID_METER));
  }

  @SubscribeEvent
  public void onModelBaking(ModelBakeEvent event) {
    Map<IBlockState, ModelResourceLocation> energyVariants =
            event.getModelManager().getBlockModelShapes()
                    .getBlockStateMapper()
                    .getVariants(Blocks.ENERGY_METER);

    Map<MeterType, IBlockState> meterTypeToStateMap = new HashMap<>();
    for (IBlockState state : energyVariants.keySet()) {
      MeterType type = state.getValue(BlockEnergyMeter.PROP_TYPE);
      meterTypeToStateMap.putIfAbsent(type, state);
    }

    Map<EnumFacing, BakedQuad> cubeQuadMap = new HashMap<>();
    Map<ResourceLocation, TextureAtlasSprite> spriteMap = new HashMap<>();
    IBakedModel originalEnergyModel = null;

    for (Map.Entry<MeterType, IBlockState> entry : meterTypeToStateMap.entrySet()) {
      IBlockState state = entry.getValue();
      ModelResourceLocation loc = energyVariants.get(state);
      IBakedModel model = event.getModelRegistry().getObject(loc);

      if (originalEnergyModel == null) {
        originalEnergyModel = model;
      }

      for (EnumFacing side : EnumFacing.values()) {
        List<BakedQuad> quads = model.getQuads(state, side, 0);
        if (!quads.isEmpty()) {
          BakedQuad quad = quads.get(0);
          cubeQuadMap.put(side, quad);

          ResourceLocation spriteLoc = new ResourceLocation(quad.getSprite().getIconName());
          spriteMap.putIfAbsent(spriteLoc, quad.getSprite());
        }
      }
    }
    TextureAtlasSprite spriteScreen  = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("flowmeters:blocks/fluid_meter_screen");
    TextureAtlasSprite spriteInput   = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("flowmeters:blocks/fluid_meter_input");
    TextureAtlasSprite spriteOutput  = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("flowmeters:blocks/fluid_meter_output");
    TextureAtlasSprite spriteSide    = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("flowmeters:blocks/fluid_meter");
    spriteMap.put(new ResourceLocation("flowmeters:blocks/fluid_meter_screen"), spriteScreen);
    spriteMap.put(new ResourceLocation("flowmeters:blocks/fluid_meter_input"),  spriteInput);
    spriteMap.put(new ResourceLocation("flowmeters:blocks/fluid_meter_output"), spriteOutput);
    spriteMap.put(new ResourceLocation("flowmeters:blocks/fluid_meter"),       spriteSide);

    TexturedQuadCache.INSTANCE.setCubeQuadMap(cubeQuadMap);
    TexturedQuadCache.INSTANCE.setTextureMap(spriteMap);

    for (Map.Entry<IBlockState, ModelResourceLocation> entry : energyVariants.entrySet()) {
      event.getModelRegistry().putObject(entry.getValue(), new EnergyMeterBakedModel(originalEnergyModel));
    }
    Map<IBlockState, ModelResourceLocation> fluidVariants =
            event.getModelManager().getBlockModelShapes()
                    .getBlockStateMapper()
                    .getVariants(Blocks.FLUID_METER);

    IBakedModel originalFluidModel = null;

    for (Map.Entry<IBlockState, ModelResourceLocation> entry : fluidVariants.entrySet()) {
      if (originalFluidModel == null) {
        originalFluidModel = event.getModelRegistry().getObject(entry.getValue());
      }
    }

    for (Map.Entry<IBlockState, ModelResourceLocation> entry : fluidVariants.entrySet()) {
      event.getModelRegistry().putObject(entry.getValue(), new FluidMeterBakedModel(originalFluidModel));
    }
  }
}
