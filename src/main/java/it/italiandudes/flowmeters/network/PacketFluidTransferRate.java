package it.italiandudes.flowmeters.network;

import io.netty.buffer.ByteBuf;
import it.italiandudes.flowmeters.FlowMeters;
import it.italiandudes.flowmeters.tile.TileEntityFluidMeter;
import it.italiandudes.flowmeters.tile.TileEntityFluidMeterBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketFluidTransferRate implements IMessage {
  public BlockPos pos;
  private float rate;
  private long totalFluidTransferred;

  public PacketFluidTransferRate() {}

  public PacketFluidTransferRate(BlockPos meterPos, float rate, long totalFluidTransferred) {
    this.pos = meterPos;
    this.rate = rate;
    this.totalFluidTransferred = totalFluidTransferred;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.pos = BufferUtil.readBlockPos(buf);
    this.rate = buf.readFloat();
    this.totalFluidTransferred = buf.readLong();
  }

  @Override
  public void toBytes(ByteBuf buf) {
    BufferUtil.writeBlockPos(buf, this.pos);
    buf.writeFloat(this.rate);
    buf.writeLong(this.totalFluidTransferred);
  }

  public static class Handler implements IMessageHandler<PacketFluidTransferRate, IMessage> {

    @Override
    public IMessage onMessage(PacketFluidTransferRate message, MessageContext ctx) {
      final float rate = message.rate;
      final long totalEnergyTransferred = message.totalFluidTransferred;
      final BlockPos pos = message.pos;

      Minecraft.getMinecraft().addScheduledTask(() -> {
        WorldClient world = Minecraft.getMinecraft().world;
        if (!world.isBlockLoaded(pos)) {
          FlowMeters.LOGGER.error(
              "Received PacketFluidTransferRate for unloaded position {}", pos);
          return;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityFluidMeterBase) {
          TileEntityFluidMeterBase fluidMeterTile = (TileEntityFluidMeter) tile;
          fluidMeterTile.setTransferRate(rate);
          fluidMeterTile.setTotalFluidTransferred(totalEnergyTransferred);
        } else {
          FlowMeters.LOGGER.error(
              "Received PacketFluidTransferRate for position with no TE: {}", pos);
        }
      });

      return null;
    }
  }
}
