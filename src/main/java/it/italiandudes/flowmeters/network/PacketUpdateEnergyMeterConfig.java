package it.italiandudes.flowmeters.network;

import io.netty.buffer.ByteBuf;
import it.italiandudes.flowmeters.FlowMeters;
import it.italiandudes.flowmeters.tile.TileEntityEnergyMeterBase;
import it.italiandudes.flowmeters.tile.config.EnumRedstoneControlState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketUpdateEnergyMeterConfig implements IMessage {
  private BlockPos pos;
  private EnumRedstoneControlState redstoneControlState;
  private int energyAliasIndex;

  public PacketUpdateEnergyMeterConfig() {}

  public PacketUpdateEnergyMeterConfig(BlockPos pos, EnumRedstoneControlState redstoneControlState, int energyAliasIndex) {
    this.pos = pos;
    this.redstoneControlState = redstoneControlState;
    this.energyAliasIndex = energyAliasIndex;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.pos = BufferUtil.readBlockPos(buf);
    this.redstoneControlState = EnumRedstoneControlState.values()[buf.readInt()];
    this.energyAliasIndex = buf.readInt();
  }

  @Override
  public void toBytes(ByteBuf buf) {
    BufferUtil.writeBlockPos(buf, this.pos);
    buf.writeInt(this.redstoneControlState.ordinal());
    buf.writeInt(this.energyAliasIndex);
  }

  public static class Handler implements IMessageHandler<PacketUpdateEnergyMeterConfig, IMessage> {

    @Override
    public IMessage onMessage(PacketUpdateEnergyMeterConfig message, MessageContext ctx) {
      BlockPos pos = message.pos;
      WorldServer world =  ctx.getServerHandler().player.getServerWorld();

      world.addScheduledTask(() -> {
        if (!world.isBlockLoaded(pos)) {
          FlowMeters.LOGGER.error(
              "Received PacketUpdateEnergyMeterSides for unloaded position {}", pos);
          return;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityEnergyMeterBase) {
          ((TileEntityEnergyMeterBase) tile).handleConfigUpdateRequest(
              message.redstoneControlState, message.energyAliasIndex);
          FlowMeters.LOGGER.info(
              "Received PacketUpdateEnergyMeterConfig for {}", pos);
        } else {
          FlowMeters.LOGGER.error(
              "Received PacketUpdateEnergyMeterConfig for position with no TE: {}", pos);
        }
      });

      return null;
    }
  }
}
