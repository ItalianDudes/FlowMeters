package it.italiandudes.flowmeters.network;

import io.netty.buffer.ByteBuf;
import it.italiandudes.flowmeters.FlowMeters;
import it.italiandudes.flowmeters.tile.TileEntityFluidMeterBase;
import it.italiandudes.flowmeters.tile.config.EnumRedstoneControlState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketUpdateFluidMeterConfig implements IMessage {
  private BlockPos pos;
  private EnumRedstoneControlState redstoneControlState;

  public PacketUpdateFluidMeterConfig() {}

  public PacketUpdateFluidMeterConfig(BlockPos pos, EnumRedstoneControlState redstoneControlState) {
    this.pos = pos;
    this.redstoneControlState = redstoneControlState;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.pos = BufferUtil.readBlockPos(buf);
    this.redstoneControlState = EnumRedstoneControlState.values()[buf.readInt()];
  }

  @Override
  public void toBytes(ByteBuf buf) {
    BufferUtil.writeBlockPos(buf, this.pos);
    buf.writeInt(this.redstoneControlState.ordinal());
  }

  public static class Handler implements IMessageHandler<PacketUpdateFluidMeterConfig, IMessage> {

    @Override
    public IMessage onMessage(PacketUpdateFluidMeterConfig message, MessageContext ctx) {
      BlockPos pos = message.pos;
      WorldServer world =  ctx.getServerHandler().player.getServerWorld();

      world.addScheduledTask(() -> {
        if (!world.isBlockLoaded(pos)) {
          FlowMeters.LOGGER.error(
              "Received PacketUpdateFluidMeterSides for unloaded position {}", pos);
          return;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityFluidMeterBase) {
          ((TileEntityFluidMeterBase) tile).handleConfigUpdateRequest(message.redstoneControlState);
          FlowMeters.LOGGER.info(
              "Received PacketUpdateFluidMeterConfig for {}", pos);
        } else {
          FlowMeters.LOGGER.error(
              "Received PacketUpdateFluidMeterConfig for position with no TE: {}", pos);
        }
      });

      return null;
    }
  }
}
