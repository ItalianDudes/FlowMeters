package it.italiandudes.flowmeters.energy.storage;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import it.italiandudes.flowmeters.energy.IEnergyMeter;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

public class MJStorage implements IMjReceiver {

  private final IEnergyMeter meter;
  private final EnumFacing side;
  private final boolean connectToReceiversOnly;

  public MJStorage(IEnergyMeter meter, EnumFacing side, boolean connectToReceiversOnly) {
    this.meter = meter;
    this.side = side;
    this.connectToReceiversOnly = connectToReceiversOnly;
  }

  @Override
  public long getPowerRequested() {
    return this.meter.getRequestedEnergy(this.side);
  }

  @Override
  public long receivePower(long microJoules, boolean simulate) {
    return microJoules - this.meter.receiveEnergy(microJoules, simulate, this.side);
  }

  @Override
  public boolean canConnect(@Nonnull IMjConnector other) {
    if (other instanceof MJStorage) {
      return false; // Don't connect to adjacent meters
    }

    if (connectToReceiversOnly) {
      return other instanceof IMjReceiver && ((IMjReceiver) other).canReceive();
    }

    return true;
  }
}
