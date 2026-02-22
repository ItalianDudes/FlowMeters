package it.italiandudes.flowmeters.tile;

import it.italiandudes.flowmeters.FlowMeters;
import it.italiandudes.flowmeters.MovingAverage;
import it.italiandudes.flowmeters.Util;
import it.italiandudes.flowmeters.block.BlockFluidMeter;
import it.italiandudes.flowmeters.block.Blocks;
import it.italiandudes.flowmeters.fluid.IFluidMeter;
import it.italiandudes.flowmeters.integration.ComputerComponentFluidMeter;
import it.italiandudes.flowmeters.integration.ModIDs;
import it.italiandudes.flowmeters.network.BufferUtil;
import it.italiandudes.flowmeters.network.PacketFluidTransferRate;
import it.italiandudes.flowmeters.tile.config.EnumRedstoneControlState;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@Optional.Interface(modid = ModIDs.OPENCOMPUTERS, iface = "li.cil.oc.api.network.SimpleComponent")
@SuppressWarnings("unused")
public abstract class TileEntityFluidMeterBase extends TileEntity implements ITickable, IFluidMeter, SimpleComponent {

  /**
   * The maximum distance in blocks from this tile entity that a player has to be to receive
   * an update packet.
    */
  protected static final double PACKET_RANGE = 32;

  /**
   * The maximum interval in ticks between {@link PacketFluidTransferRate} packets that tile entity
   * sends out.
   */
  protected static final double UPDATE_PACKET_MAX_TICK_INTERVAL = 120;

  /**
   * The minimum interval in ticks between {@link PacketFluidTransferRate} packets that tile entity
   * sends out. For example, if this value is {@code 4}, this tile entity will wait at least
   * {@code 4} ticks between packets.
   */
  protected static final double UPDATE_PACKET_MIN_TICK_INTERVAL = 4;

  /** The name of this component as it appears to mods like OpenComputers and ComputerCraft. */
  protected static final String COMPUTER_COMPONENT_NAME = "fluid-meter";

  public static final int UNLIMITED_RATE = -1;

  protected static final String NBT_CONNECTED_KEY = "connected";
  protected static final String NBT_POWERED_KEY = "powered";
  protected static final String NBT_INPUT_SIDE_KEY = "input-side";
  protected static final String NBT_OUTPUT_SIDE_KEY = "output-side";
  protected static final String NBT_TOTAL_FLUID_TRANSFERRED_KEY = "total-fluid-transferred";
  protected static final String NBT_REDSTONE_CONTROL_STATE = "redstone-control-state";
  protected static final String NBT_RATE_LIMIT_KEY = "rate-limit";

  protected static final String NBT_OWNER_UUID = "owner-uuid";
  protected static final String NBT_OWNER_USERNAME = "owner-username";

  protected int ticks = 0;
  protected long totalFluidTransferred = 0;
  protected long totalFluidTransferredLastTick = 0;
  protected float transferRate = 0;
  protected float lastTransferRateSent = 0;
  protected int ticksSinceLastTransferRatePacket = 0;

  protected boolean saved = false;

  /**
   * True if this meter is fully connected, meaning that it has a valid {@link IFluidHandler}
   * connected to the input and output sides.
   */
  protected boolean fullyConnected = false;

  /** True if this tile entity's block is currently receiving redstone power */
  protected boolean powered = false;

  protected MovingAverage transferRateMovingAverage = new MovingAverage(10);

  protected EnumFacing screenSide = EnumFacing.NORTH;

  @Nullable
  protected EnumFacing inputSide;

  @Nullable
  protected EnumFacing outputSide;

  protected EnumRedstoneControlState redstoneControlState = EnumRedstoneControlState.ACTIVE;

  protected int rateLimit = UNLIMITED_RATE;

  protected TargetPoint packetTargetPoint;

  private final ComputerComponentFluidMeter computerComponentFluidMeter;

  public TileEntityFluidMeterBase() {
    this.computerComponentFluidMeter = new ComputerComponentFluidMeter(this);
  }

  @Nullable
  public ComputerComponentFluidMeter getComputerComponent() {
    return this.computerComponentFluidMeter;
  }

  @Override
  public void readFromNBT(@NotNull NBTTagCompound tag) {
    super.readFromNBT(tag);
    this.inputSide = BufferUtil.decodeNullableFace(tag.getByte(NBT_INPUT_SIDE_KEY));
    this.outputSide = BufferUtil.decodeNullableFace(tag.getByte(NBT_OUTPUT_SIDE_KEY));
    this.totalFluidTransferred = tag.getLong(NBT_TOTAL_FLUID_TRANSFERRED_KEY);
    this.redstoneControlState = EnumRedstoneControlState.values()[tag.getInteger(NBT_REDSTONE_CONTROL_STATE)];
    this.rateLimit = tag.getInteger(NBT_RATE_LIMIT_KEY);
    this.totalFluidTransferredLastTick = this.totalFluidTransferred;
    this.saved = true;
  }

  @Override
  public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
    NBTTagCompound tag = super.writeToNBT(compound);
    tag.setByte(NBT_INPUT_SIDE_KEY, BufferUtil.encodeNullableFace(this.inputSide));
    tag.setByte(NBT_OUTPUT_SIDE_KEY, BufferUtil.encodeNullableFace(this.outputSide));
    tag.setLong(NBT_TOTAL_FLUID_TRANSFERRED_KEY, this.totalFluidTransferred);
    tag.setInteger(NBT_REDSTONE_CONTROL_STATE, this.redstoneControlState.ordinal());
    tag.setInteger(NBT_RATE_LIMIT_KEY, this.rateLimit);
    return tag;
  }

  @Override
  public @NotNull NBTTagCompound getUpdateTag() {
    NBTTagCompound tag = super.getUpdateTag();
    tag.setBoolean(NBT_CONNECTED_KEY, this.fullyConnected);
    tag.setBoolean(NBT_POWERED_KEY, this.powered);
    tag.setByte(NBT_INPUT_SIDE_KEY, BufferUtil.encodeNullableFace(this.inputSide));
    tag.setByte(NBT_OUTPUT_SIDE_KEY, BufferUtil.encodeNullableFace(this.outputSide));
    tag.setLong(NBT_TOTAL_FLUID_TRANSFERRED_KEY, this.totalFluidTransferred);
    tag.setInteger(NBT_REDSTONE_CONTROL_STATE, this.redstoneControlState.ordinal());
    tag.setInteger(NBT_RATE_LIMIT_KEY, this.rateLimit);
    return tag;
  }

  @Override
  public void handleUpdateTag(NBTTagCompound tag) {
    this.fullyConnected = tag.getBoolean(NBT_CONNECTED_KEY);
    this.powered = tag.getBoolean(NBT_POWERED_KEY);
    this.inputSide = BufferUtil.decodeNullableFace(tag.getByte(NBT_INPUT_SIDE_KEY));
    this.outputSide = BufferUtil.decodeNullableFace(tag.getByte(NBT_OUTPUT_SIDE_KEY));
    this.totalFluidTransferred = tag.getLong(NBT_TOTAL_FLUID_TRANSFERRED_KEY);
    this.redstoneControlState = EnumRedstoneControlState.values()[tag.getInteger(NBT_REDSTONE_CONTROL_STATE)];
    this.rateLimit = tag.getInteger(NBT_RATE_LIMIT_KEY);
  }

  /**
   * Returns the {@link SPacketUpdateTileEntity} for this tile to send to clients. This is called
   * automatically on the server whenever a client loads the chunk or when a block update is
   * triggered on the server. The packet contains <i>state</i> update info for clients, such as if
   * the meter is fully connected or powered by redstone.
   * <br>
   * This is not to be confused with the {@link PacketFluidTransferRate} packets, which are sent
   * out every few ticks if the transfer rate changes.
   */
  @Override
  public SPacketUpdateTileEntity getUpdatePacket() {
    return new SPacketUpdateTileEntity(this.pos, 1, this.getUpdateTag());
  }

  /**
   * Called on the client when it receives a {@link SPacketUpdateTileEntity} from the server.
   */
  @Override
  public void onDataPacket(@NotNull NetworkManager net, SPacketUpdateTileEntity packet){
    this.handleUpdateTag(packet.getNbtCompound());
    this.getWorld().markBlockRangeForRenderUpdate(this.pos, this.pos);
  }

  /**
   * Called after this tile entity is loaded into the world and is ready to go. The internal fields
   * like {@link #pos} and {@link #world} are populated by this point. Use this method as an
   * alternative to a constructor for initializing a tile entity.
   */
  @Override
  public void onLoad() {
    IBlockState state = this.world.getBlockState(pos);
    this.screenSide = state.getValue(BlockFluidMeter.PROP_FACING);

    if (!saved) {
      this.inputSide = Util.getLeftFace(this.screenSide);
      this.outputSide = Util.getRightFace(this.screenSide);
    }

    if (!this.world.isRemote) {
      this.packetTargetPoint = new TargetPoint(
          this.world.provider.getDimension(),
          this.pos.getX(),
          this.pos.getY(),
          this.pos.getZ(), PACKET_RANGE);

      this.checkConnections();
      this.checkRedstone();
    }
  }

  @Override
  public void update() {
    if (this.world.isRemote) {
      return;
    }

    // Compute the FE transfer in this tick by taking the difference between total transfer this
    // tick and the total transfer last tick
    long transferredThisTick = Math.abs(this.totalFluidTransferred - this.totalFluidTransferredLastTick);

    // Add FE transfer this tick to moving average
    this.transferRateMovingAverage.add(transferredThisTick);
    this.transferRate = this.transferRateMovingAverage.getAverage(); // compute average FE/t
    this.totalFluidTransferredLastTick = this.totalFluidTransferred;

    if (transferredThisTick > 0) {
      this.markDirty();
    }

    // Send update packet to all nearby players if required (if the transfer rate changed or enough
    // ticks have passed since the last packet)
    if (((this.transferRate != this.lastTransferRateSent || this.transferRate > 0)
        && this.ticksSinceLastTransferRatePacket > UPDATE_PACKET_MIN_TICK_INTERVAL)
        || this.ticksSinceLastTransferRatePacket >= UPDATE_PACKET_MAX_TICK_INTERVAL) {

      this.lastTransferRateSent = this.transferRate;
      this.ticksSinceLastTransferRatePacket = 0;

      FlowMeters.NETWORK.sendToAllTracking(
          new PacketFluidTransferRate(this.pos, this.transferRate, this.totalFluidTransferred),
          this.packetTargetPoint);
    } else {
      this.ticksSinceLastTransferRatePacket++;
    }

    if (this.ticks % 10 == 0) {
      this.checkConnections();
    }

    this.ticks = ++this.ticks % 20;
  }

  public EnumRedstoneControlState getRedstoneControlState() {
    return this.redstoneControlState;
  }

  public void setRedstoneControlState(EnumRedstoneControlState redstoneControlState) {
    this.redstoneControlState = redstoneControlState;
  }

  /**
   * Returns true is this meter is currently disabled. A disabled meter acts like a breaker and
   * will not transmit power. Whether this meter is disable depends on if it is powered, and
   * it's redstone control state as defined in {@link #redstoneControlState}.
   */
  public boolean isDisabled() {
    return !this.redstoneControlState.isMachineEnabled(this.powered);
  }

  protected void notifyUpdate() {
    IBlockState currentBlockState = this.world.getBlockState(this.pos);
    this.world.notifyBlockUpdate(this.pos, currentBlockState, currentBlockState, 3);
    this.world.notifyNeighborsOfStateChange(this.pos, Blocks.FLUID_METER, false);
  }

  public abstract int getFluidScale();

  protected abstract void checkConnections();

  /**
   * Checks if this tile entity's block is powered. If the powered state has changed, then a block
   * update is triggered.
   */
  protected void checkRedstone() {
    boolean newPowered = this.world.isBlockPowered(this.pos);
    if (newPowered != this.powered) {
      this.powered = newPowered;
      this.notifyUpdate();
    }
  }

  public void onNeighborChanged(BlockPos neighborPos, IBlockState newState) {
    if (this.world.isRemote) {
      return;
    }

    this.checkConnections();
    this.checkRedstone();
  }

  /** Sets the current FE transfer rate for this meter. This method should only be used client-side
   * to set the transfer rate for rendering when an update packet
   * {@link PacketFluidTransferRate} is received. On the server, the transfer rate is calculated in
   * the {@link #update} method.
   */
  public void setTransferRate(float rate) {
    this.transferRate = rate;
  }

  public float getTransferRate() {
    return this.transferRate;
  }

  public void setTotalFluidTransferred(long totalFluidTransferred) {
    this.totalFluidTransferred = totalFluidTransferred;
  }

  public long getTotalFluidTransferred() {
    return this.totalFluidTransferred;
  }

  public EnumFacing getScreenSide() {
    return this.screenSide;
  }

  @Nullable
  public EnumFacing getInputSide() {
    return this.inputSide;
  }

  @Nullable
  public EnumFacing getOutputSide() {
    return this.outputSide;
  }

  public void setInputSide(@Nullable EnumFacing side) {
    if (side == this.screenSide) {
      throw new IllegalArgumentException("Cannot set input side to screen side");
    }

    this.inputSide = side;
  }

  public void setOutputSide(@Nullable EnumFacing side) {
    if (side == this.screenSide) {
      throw new IllegalArgumentException("Cannot set output side to screen side");
    }

    this.outputSide = side;
  }

  public int getRateLimit() {
    return this.rateLimit;
  }

  public void setRateLimit(int limit) {
    this.rateLimit = limit;
  }

  public void handleSideUpdateRequest(@Nullable EnumFacing inputSide, @Nullable EnumFacing outputSide) {
    if (this.world.isRemote) {
      throw new IllegalStateException("Should not have received side update packet on the client");
    }

    this.setInputSide(inputSide);
    this.setOutputSide(outputSide);

    this.checkConnections();
    this.markDirty();

    IBlockState state = this.world.getBlockState(this.pos);
    this.world.notifyBlockUpdate(pos, state, state, 3);
    this.world.notifyNeighborsOfStateChange(this.pos, Blocks.FLUID_METER, false);
  }

  public void handleConfigUpdateRequest(EnumRedstoneControlState redstoneControlState) {
    if (this.world.isRemote) {
      throw new IllegalStateException("Should not have received config update packet on the client");
    }
    this.redstoneControlState = redstoneControlState;
    this.markDirty();

    IBlockState state = this.world.getBlockState(this.pos);
    this.world.notifyBlockUpdate(pos, state, state, 3);
    this.world.notifyNeighborsOfStateChange(this.pos, Blocks.FLUID_METER, false);
  }

  public void handleRateLimitChangeRequest(int newRateLimit) {
    if (this.world.isRemote) {
      throw new IllegalStateException("Should not have received rate limit update packet on the client");
    }

    if (newRateLimit < 0 && newRateLimit != UNLIMITED_RATE) {
      newRateLimit = UNLIMITED_RATE;
    }

    this.rateLimit = newRateLimit;

    this.checkConnections();
    this.markDirty();

    IBlockState state = this.world.getBlockState(this.pos);
    this.world.notifyBlockUpdate(pos, state, state, 3);
    this.world.notifyNeighborsOfStateChange(this.pos, Blocks.FLUID_METER, false);
  }

  public boolean isFullyConnected() {
    return this.fullyConnected;
  }


  public boolean canReceive(EnumFacing side) {
    return side == this.inputSide && this.isFullyConnected() && !this.isDisabled();
  }

  protected boolean doesSideAcceptConnection(EnumFacing facing) {
    return facing == this.inputSide || facing == this.outputSide;
  }

  // IMeter implementation

  @Override
  public BlockPos getPosition() {
    return this.pos;
  }

  @Override
  public World getWorldObj() {
    return this.world;
  }

  // End IMeter implementation

  // OpenComputers SimpleComponent implementation
  @Optional.Method(modid = ModIDs.OPENCOMPUTERS)
  @Override
  public String getComponentName() {
    return ComputerComponentFluidMeter.COMPONENT_NAME;
  }

  @Optional.Method(modid = ModIDs.OPENCOMPUTERS)
  @Callback(doc = "function():number -- gets the current average fluid transfer rate per tick")
  public Object[] getTransferRate(final Context context, final Arguments args) {
    return this.computerComponentFluidMeter.getTransferRate();
  }

  @Optional.Method(modid = ModIDs.OPENCOMPUTERS)
  @Callback(doc = "function():number -- gets the total fluid transferred though this meter")
  public Object[] getTotalFluidTransferred(final Context context, final Arguments args) {
    return this.computerComponentFluidMeter.getTotalFluidTransferred();
  }

  @Optional.Method(modid = ModIDs.OPENCOMPUTERS)
  @Callback(doc = "function():string -- returns the status of this meter, either \"active\", \"not_connected\", or \"disabled\"")
  public Object[] getStatus(final Context context, final Arguments args) {
    return this.computerComponentFluidMeter.getStatus();
  }

  @Optional.Method(modid = ModIDs.OPENCOMPUTERS)
  @Callback(doc = "function():string -- returns the current redstone control state")
  public Object[] getRedstoneControlState(final Context context, final Arguments args) {
    return this.computerComponentFluidMeter.getRedstoneControlState();
  }

  @Optional.Method(modid = ModIDs.OPENCOMPUTERS)
  @Callback(doc = "function():number -- returns the current transfer rate limit or -1 if it is unlimited")
  public Object[] getTransferRateLimit(final Context context, final Arguments args) {
    return this.computerComponentFluidMeter.getTransferRateLimit();
  }

  @Optional.Method(modid = ModIDs.OPENCOMPUTERS)
  @Callback(doc = "function(limit:number):bool -- attempts to set the transfer rate limit for this meter. Pass -1 to set it to unlimited. Returns a boolean indicating success.")
  public Object[] setTransferRateLimit(final Context context, final Arguments args) {
    return this.computerComponentFluidMeter.setTransferRateLimit(args.checkInteger(0)) ;
  }
  // End OpenComputers SimpleComponent implementation
}
