package it.italiandudes.flowmeters.tile;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.NodeStats;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyTile;
import it.italiandudes.flowmeters.FlowMeters;
import it.italiandudes.flowmeters.energy.EnergyTypes;
import it.italiandudes.flowmeters.energy.storage.EUStorage;
import it.italiandudes.flowmeters.tile.config.EnumRedstoneControlState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;

public class TileEntityEnergyMeterEU extends TileEntityEnergyMeterBase {

  private EUStorage storage;
  private boolean addedToIC2EnergyNet = false;

  public TileEntityEnergyMeterEU() {
    super(EnergyTypes.EU);
  }

  @Override
  public void onLoad() {
    this.storage = new EUStorage(this);

    super.onLoad();

    if (!this.world.isRemote) {
      this.addToEnergyNet();
    }
  }

  @Override
  public void update() {
    if (!this.world.isRemote) {
      NodeStats stats = EnergyNet.instance.getNodeStats(this.storage);
      if (stats != null) {
        this.totalEnergyTransferred += (long) stats.getEnergyIn();
      }
    }

    super.update();
  }

  @Override
  public int getEnergyScale() {
    return 1;
  }

  @Override
  public void onChunkUnload() {
    super.onChunkUnload();
    this.removeFromEnergyNet();
  }

  @Override
  public void invalidate() {
    super.invalidate();
    this.removeFromEnergyNet();
  }

  private void removeFromEnergyNet() {
    if (!this.world.isRemote && this.addedToIC2EnergyNet) {
      MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this.storage));
      this.addedToIC2EnergyNet = false;
      FlowMeters.LOGGER.debug("Remove EU meter {} from energy net", this.pos);
    }
  }

  private void addToEnergyNet() {
    if (!this.world.isRemote && !this.addedToIC2EnergyNet) {
      MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this.storage));
      this.addedToIC2EnergyNet = true;
      FlowMeters.LOGGER.debug("Added EU meter {} from energy net", this.pos);
    }
  }

  @Override
  public void handleSideUpdateRequest(@Nullable EnumFacing inputSide,
      @Nullable EnumFacing outputSide) {

    if (!this.world.isRemote) {
      this.removeFromEnergyNet();
      this.addToEnergyNet();
    }

    super.handleSideUpdateRequest(inputSide, outputSide);
  }

  @Override
  protected void checkConnections() {
    boolean connected = false;

    if (this.inputSide != null && this.outputSide != null) {
      BlockPos inputNeighbor = this.pos.offset(this.inputSide);
      BlockPos outputNeightbor = this.pos.offset(this.outputSide);

      IEnergyTile inputEnergyTile = EnergyNet.instance.getTile(world, inputNeighbor);
      IEnergyTile outputEnergyTile = EnergyNet.instance.getTile(world, outputNeightbor);
      connected = inputEnergyTile != null
          && outputEnergyTile != null;
    }

    if (connected != this.fullyConnected) {
      this.fullyConnected = connected;
      this.notifyUpdate();
    }
  }

  @Override
  public void handleConfigUpdateRequest(EnumRedstoneControlState redstoneControlState,
      int energyAliasIndex) {
    super.handleConfigUpdateRequest(redstoneControlState, energyAliasIndex);
    this.checkRedstone();
  }


  @Override
  protected void checkRedstone() {
    super.checkRedstone();

    if (this.isDisabled()) {
      this.removeFromEnergyNet();
    } else {
      this.addToEnergyNet();
    }
  }

  @Override
  public boolean canReceiveEnergy(EnumFacing side) {
    return side == this.inputSide;
  }

  @Override
  public boolean canEmitEnergy(EnumFacing side) {
    return side == this.outputSide;
  }

  /**
   * Unused in EU meters since the calculation is done in {@link #update()} instead.
   */
  @Override
  public long receiveEnergy(long amount, boolean simulate, EnumFacing side) {
    return 0;
  }
}
