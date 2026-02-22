package it.italiandudes.flowmeters.fluid;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public interface IFluidMeter {

  /**
   * Returns whether the given side can receive fluid.
   */
  boolean canReceiveFluid(EnumFacing side);

  /**
   * Returns whether the given side can send fluid.
   */
  boolean canEmitFluid(EnumFacing side);

  /**
   * Handles the receiving of fluid on the given side.
   * @return The amount of fluid that was accepted. Must be at most the amount provided and
   * non-negative.
   */
  long receiveFluid(FluidStack fluidStack, boolean simulate, EnumFacing side);

  /**
   * Returns the amount of fluid requested on the given side. Not all implementations will need
   * to override this, since not all energy systems have the concept of "requesting power".
   */
  default FluidStack getRequestedFluid(EnumFacing side) {
    return null;
  }

  BlockPos getPosition();

  World getWorldObj();
}
