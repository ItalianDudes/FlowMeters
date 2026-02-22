package it.italiandudes.flowmeters.network;

import io.netty.buffer.ByteBuf;
import it.italiandudes.flowmeters.FlowMeters;
import it.italiandudes.flowmeters.tile.TileEntityEnergyMeterBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketEnergyTransferRate implements IMessage {
  public BlockPos pos;
  private float rate;
  private long totalEnergyTransferred;

  public PacketEnergyTransferRate() {}

  public PacketEnergyTransferRate(BlockPos meterPos, float rate, long totalEnergyTransferred) {
    this.pos = meterPos;
    this.rate = rate;
    this.totalEnergyTransferred = totalEnergyTransferred;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.pos = BufferUtil.readBlockPos(buf);
    this.rate = buf.readFloat();
    this.totalEnergyTransferred = buf.readLong();
  }

  @Override
  public void toBytes(ByteBuf buf) {
    BufferUtil.writeBlockPos(buf, this.pos);
    buf.writeFloat(this.rate);
    buf.writeLong(this.totalEnergyTransferred);
  }

  public static class Handler implements IMessageHandler<PacketEnergyTransferRate, IMessage> {

    @Override
    public IMessage onMessage(PacketEnergyTransferRate message, MessageContext ctx) {
      final float rate = message.rate;
      final long totalEnergyTransferred = message.totalEnergyTransferred;
      final BlockPos pos = message.pos;

      Minecraft.getMinecraft().addScheduledTask(() -> {
        WorldClient world = Minecraft.getMinecraft().world;
        if (!world.isBlockLoaded(pos)) {
          FlowMeters.LOGGER.error(
              "Received PacketEnergyTransferRate for unloaded position {}", pos);
          return;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityEnergyMeterBase) {
          TileEntityEnergyMeterBase energyMeterTile = (TileEntityEnergyMeterBase) tile;
          energyMeterTile.setTransferRate(rate);
          energyMeterTile.setTotalEnergyTransferred(totalEnergyTransferred);
        } else {
          FlowMeters.LOGGER.error(
              "Received PacketEnergyTransferRate for position with no TE: {}", pos);
        }
      });

      return null;
    }
  }
}
