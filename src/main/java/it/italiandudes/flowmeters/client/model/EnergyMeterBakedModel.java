package it.italiandudes.flowmeters.client.model;

import com.google.common.collect.ImmutableList;
import it.italiandudes.flowmeters.block.BlockEnergyMeter;
import it.italiandudes.flowmeters.block.BlockEnergyMeter.MeterType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class EnergyMeterBakedModel implements IBakedModel {
  private final IBakedModel originalModel;

  public EnergyMeterBakedModel(IBakedModel originalModel) {
    this.originalModel = originalModel;
  }

  @Override
  public @NotNull List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
    if (side == null) {
      return ImmutableList.of();
    }

    if (state != null) {
      EnumFacing facing = state.getValue(BlockEnergyMeter.PROP_FACING);
      MeterType type = state.getValue(BlockEnergyMeter.PROP_TYPE);

      if (side == facing) {
        return ImmutableList.of(
            TexturedQuadCache.INSTANCE.getBakedQuad(side, EnergyMeterTextureLocations.getScreenTexture(type)));
      }

      if (state instanceof IExtendedBlockState) {
        IExtendedBlockState ext = (IExtendedBlockState) state;

        EnumFacing inputSide = ext.getValue(BlockEnergyMeter.PROP_INPUT);
        if (side == inputSide) {
          return ImmutableList.of(
              TexturedQuadCache.INSTANCE.getBakedQuad(side, EnergyMeterTextureLocations.getInputTexture(type)));
        }

        EnumFacing outputSide = ext.getValue(BlockEnergyMeter.PROP_OUTPUT);
        if (side == outputSide) {
          return ImmutableList.of(
              TexturedQuadCache.INSTANCE.getBakedQuad(side, EnergyMeterTextureLocations.getOutputTexture(type)));
        }
      }

      return ImmutableList.of(
          TexturedQuadCache.INSTANCE.getBakedQuad(side, EnergyMeterTextureLocations.getSideTexture(type)));
    }

    return ImmutableList.of(
        TexturedQuadCache.INSTANCE.getBakedQuad(side, EnergyMeterTextureLocations.getSideTexture(MeterType.FE_METER)));
  }

  @Override
  public boolean isAmbientOcclusion() {
    return true;
  }

  @Override
  public boolean isGui3d() {
    return false;
  }

  @Override
  public boolean isBuiltInRenderer() {
    return false;
  }

  @Override
  public @NotNull TextureAtlasSprite getParticleTexture() {
    return this.originalModel.getParticleTexture();
  }

  @Override
  public @NotNull ItemOverrideList getOverrides() {
    return null;
  }
}
