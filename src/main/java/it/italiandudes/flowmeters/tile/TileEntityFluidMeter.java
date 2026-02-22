package it.italiandudes.flowmeters.tile;

import it.italiandudes.flowmeters.Util;
import it.italiandudes.flowmeters.fluid.FluidMeterHandler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class TileEntityFluidMeter extends TileEntityFluidMeterBase {
  private FluidMeterHandler inputFluidHandler;
  private FluidMeterHandler outputFluidHandler;

  public TileEntityFluidMeter() {
    super();
  }

  @Override
  public void onLoad() {
    super.onLoad();

    if (!this.world.isRemote) {
      this.inputFluidHandler = new FluidMeterHandler(this, inputSide);
      this.outputFluidHandler = new FluidMeterHandler(this, outputSide);
    }
  }

  @Override
  public int getFluidScale() {
    return 1;
  }

  @Override
  public long receiveFluid(FluidStack stack, boolean simulate, EnumFacing side) {
    if (!isFullyConnected() || side != this.inputSide || this.isDisabled()) {
      return 0;
    }

    BlockPos outputPos = this.pos.offset(this.outputSide);
    IFluidHandler adjacent = Util.getFluidHandler(this.world, outputPos, this.outputSide.getOpposite());

    if (adjacent == null) {
      return 0;
    }

    if (stack == null || stack.amount <= 0) return 0;
    int amountToSend = this.rateLimit == UNLIMITED_RATE ? stack.amount : Math.min(stack.amount, this.rateLimit);
    FluidStack toSend = new FluidStack(stack, amountToSend);

    int filled = adjacent.fill(toSend, simulate);

    if (!simulate && filled > 0) {
      this.totalFluidTransferred += filled;
    }

    return filled;
  }

  @Override
  public boolean canReceiveFluid(EnumFacing side) {
    return side == this.inputSide;
  }

  @Override
  public boolean canEmitFluid(EnumFacing side) {
    return side == this.outputSide;
  }

  @Override
  protected void checkConnections() {
    boolean connected = false;

    if (this.inputSide != null && this.outputSide != null) {
      BlockPos inputNeighbor = this.pos.offset(this.inputSide);
      BlockPos outputNeighbor = this.pos.offset(this.outputSide);

      IFluidHandler input = Util.getFluidHandler(world, inputNeighbor, this.inputSide.getOpposite());
      IFluidHandler output = Util.getFluidHandler(world, outputNeighbor, this.outputSide.getOpposite());

      connected = input != null && output != null;
    }

    if (connected != this.fullyConnected) {
      this.fullyConnected = connected;
      this.notifyUpdate();
    }
  }

  @Override
  public void handleSideUpdateRequest(@Nullable EnumFacing inputSide, @Nullable EnumFacing outputSide) {
    this.inputFluidHandler = new FluidMeterHandler(this, inputSide);
    this.outputFluidHandler = new FluidMeterHandler(this, outputSide);
    super.handleSideUpdateRequest(inputSide, outputSide);
  }

  @Override
  public boolean hasCapability(@NotNull Capability<?> capability, EnumFacing facing) {
    if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null && doesSideAcceptConnection(facing)) {
      return true;
    }
    return super.hasCapability(capability, facing);
  }

  @Override
  public <T> T getCapability(@NotNull Capability<T> capability, EnumFacing facing) {
    if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null && doesSideAcceptConnection(facing)) {
      if (facing == inputSide) {
        return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this.inputFluidHandler);
      } else if (facing == outputSide) {
        return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this.outputFluidHandler);
      }
      throw new RuntimeException("Attempted to get fluid handler capability for invalid side: " + facing);
    }
    return super.getCapability(capability, facing);
  }
}
