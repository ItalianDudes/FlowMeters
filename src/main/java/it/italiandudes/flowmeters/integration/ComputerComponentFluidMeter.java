package it.italiandudes.flowmeters.integration;

import dan200.computercraft.api.lua.ArgumentHelper;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import it.italiandudes.flowmeters.FlowMeters;
import it.italiandudes.flowmeters.tile.TileEntityEnergyMeterBase;
import it.italiandudes.flowmeters.tile.TileEntityFluidMeterBase;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Optional.Interface(modid = ModIDs.COMPUTERCRAFT, iface = "dan200.computercraft.api.peripheral.IPeripheral")
public class ComputerComponentFluidMeter implements IPeripheral {
  public static final String COMPONENT_NAME = "fluid_meter";

  private final TileEntityFluidMeterBase meter;

  public ComputerComponentFluidMeter(TileEntityFluidMeterBase meter) {
    this.meter = meter;
  }

  public Object[] getTransferRate()  {
    return new Object[] { this.meter.getTransferRate() / this.meter.getFluidScale() };
  }

  public Object[] getTotalFluidTransferred() {
    return new Object[] { this.meter.getTotalFluidTransferred() / this.meter.getFluidScale() };
  }

  public Object[] getStatus() {
    String statusString = "active";
    if (!this.meter.isFullyConnected()) {
      statusString = "not connected";
    } else if (this.meter.isDisabled()) {
      statusString = "disabled";
    }
    return new Object[] { statusString };
  }

  public Object[] getRedstoneControlState() {
    return new Object[] { this.meter.getRedstoneControlState().getDescription().toLowerCase()};
  }

  public Object[] getTransferRateLimit() {
    return new Object[] { this.meter.getRateLimit() };
  }

  public Object[] setTransferRateLimit(int limit) {
    boolean rateValid = limit >= 0 || limit == TileEntityEnergyMeterBase.UNLIMITED_RATE;
    if (rateValid) {
      this.meter.handleRateLimitChangeRequest(limit);
    }

    return new Object[] { rateValid };
  }

  @Optional.Method(modid = ModIDs.COMPUTERCRAFT)
  @Nonnull
  @Override
  public String getType() {
    return COMPONENT_NAME;
  }

  @Optional.Method(modid = ModIDs.COMPUTERCRAFT)
  @Nonnull
  @Override
  public String[] getMethodNames() {
    return new String[] {
        "getTransferRate",
        "getTotalEnergyTransferred",
        "getStatus",
        "getRedstoneControlState",
        "getEnergyType",
        "getEnergyTypeAlias",
        "getTransferRateLimit",
        "setTransferRateLimit"
    };
  }

  @Optional.Method(modid = ModIDs.COMPUTERCRAFT)
  @Nullable
  @Override
  public Object[] callMethod(@Nonnull IComputerAccess computer, @Nonnull ILuaContext context, int method, @Nonnull Object[] args) throws LuaException, InterruptedException {
    switch (method) {
      case 0:
        return this.getTransferRate();
      case 1:
        return this.getTotalFluidTransferred();
      case 2:
        return this.getStatus();
      case 3:
        return this.getRedstoneControlState();
      case 4:
        return this.getTransferRateLimit();
      case 5: {
        return this.setTransferRateLimit(ArgumentHelper.getInt(args, 0));
      }
      default:
        FlowMeters.LOGGER.error("Attempted to call unknownComputerCraft method {}", method);
        return null;
    }
  }

  @Optional.Method(modid = ModIDs.COMPUTERCRAFT)
  @Override
  public boolean equals(@Nullable IPeripheral other) {
    if (other == this) {
      return true;
    }

    if (other instanceof ComputerComponentFluidMeter) {
      ComputerComponentFluidMeter otherComponent = (ComputerComponentFluidMeter) other;
      return this.meter.getPos().equals(otherComponent.meter.getPos())
          && this.meter.getWorld().provider.getDimension() == otherComponent.meter.getWorld().provider.getDimension();
    }

    return false;
  }
}
