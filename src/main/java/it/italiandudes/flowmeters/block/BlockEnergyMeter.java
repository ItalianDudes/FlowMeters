package it.italiandudes.flowmeters.block;

import it.italiandudes.flowmeters.FlowMeters;
import it.italiandudes.flowmeters.energy.EnergyType;
import it.italiandudes.flowmeters.energy.EnergyTypes;
import it.italiandudes.flowmeters.integration.ModIDs;
import it.italiandudes.flowmeters.properties.UnlistedPropertyBoolean;
import it.italiandudes.flowmeters.properties.UnlistedPropertyFacing;
import it.italiandudes.flowmeters.tile.TileEntityEnergyMeterBase;
import it.italiandudes.flowmeters.tile.TileEntityEnergyMeterEU;
import it.italiandudes.flowmeters.tile.TileEntityEnergyMeterFE;
import it.italiandudes.flowmeters.tile.TileEntityEnergyMeterMJ;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;

public class BlockEnergyMeter extends BlockBase {

  public static final String NAME = "energy_meter";
  public static final PropertyDirection PROP_FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
  public static final PropertyEnum<MeterType> PROP_TYPE = PropertyEnum.create("type", MeterType.class);

  public static final UnlistedPropertyFacing PROP_INPUT = UnlistedPropertyFacing.create("input");
  public static final UnlistedPropertyFacing PROP_OUTPUT = UnlistedPropertyFacing.create("output");
  public static final UnlistedPropertyBoolean PROP_CONNECTED = UnlistedPropertyBoolean.create("connected");

  public BlockEnergyMeter() {
    super(Material.IRON, NAME);

    this.setSoundType(SoundType.METAL);

    this.setDefaultState(blockState.getBaseState()
        .withProperty(PROP_FACING, EnumFacing.NORTH)
        .withProperty(PROP_TYPE, MeterType.FE_METER));

    this.setHarvestLevel("pickaxe", 1);
  }

  @Override
  public TileEntity createTileEntity(@NotNull World world, IBlockState state) {
    MeterType type = state.getValue(PROP_TYPE);

    switch (type) {
      case FE_METER:
        return new TileEntityEnergyMeterFE();
      case MJ_METER:
        return new TileEntityEnergyMeterMJ();
      case EU_METER:
        return new TileEntityEnergyMeterEU();
      default:
        FlowMeters.LOGGER.error("Attempted to create tile entity for invalid type {}", type.getName());
    }

    return null;
  }

  @Override
  public boolean hasTileEntity(@NotNull IBlockState state) {
    return true;
  }

  @Override
  public void neighborChanged(@NotNull IBlockState state, @NotNull World world, @NotNull BlockPos pos, @NotNull Block blockIn,
                              @NotNull BlockPos fromPos) {
    super.neighborChanged(state, world, pos, blockIn, fromPos);

    TileEntityEnergyMeterBase tile = (TileEntityEnergyMeterBase) world.getTileEntity(pos);
    if (tile != null) {
      tile.onNeighborChanged(fromPos, state);
    }
  }

  @Override
  public @NotNull IBlockState getStateForPlacement(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                                   float hitX, float hitY, float hitZ, int meta, @NotNull EntityLivingBase placer) {

    MeterType meterType = MeterType.values()[meta];
    return this.getDefaultState().withProperty(PROP_TYPE, meterType);
  }

  @Override
  public @NotNull IBlockState getStateForPlacement(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing, float hitX,
                                                   float hitY, float hitZ, int meta, @NotNull EntityLivingBase placer, @NotNull EnumHand hand) {

    /*
     * Terrible, hacky fix for when using Better Builder's Wands. It appears that the mod incorrectly
     * calls 'getStateForPlacement' using world metadata instead of itemstack metadata. The easiest
     * (and ugliest) workaround is to check the stack trace and call `getStateFromMeta` is the the
     * caller is 'portablejim.bbw.core.WandWorker'.
     */
    if (Loader.isModLoaded(ModIDs.BETTER_BUILDERS_WANDS)) {
      StackTraceElement[] stack = Thread.currentThread().getStackTrace();
      if (stack.length > 2 && stack[2].getClassName().equals("portablejim.bbw.core.WandWorker")) {
          return this.getStateFromMeta(meta);
      }
    }

    return this.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer);
  }

  @Override
  public @NotNull IBlockState getStateFromMeta(int meta) {
    int facingIndex = meta & 0b11;
    int typeIndex = (meta & 0b1100) >> 2;
    return getDefaultState()
        .withProperty(PROP_FACING, EnumFacing.byHorizontalIndex(facingIndex))
        .withProperty(PROP_TYPE, MeterType.values()[typeIndex]);
  }

  @Override
  public int getMetaFromState(IBlockState state) {
    int facingIndex = state.getValue(PROP_FACING).getHorizontalIndex();
    int typeIndex = state.getValue(PROP_TYPE).getIndex();
    return facingIndex | (typeIndex << 2);
  }

  @Override
  public boolean onBlockActivated(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state,
                                  @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
    return FlowMeters.PROXY.handleEnergyBlockActivation(world, pos, player);
  }

  @Override
  protected @NotNull BlockStateContainer createBlockState() {
    return new ExtendedBlockState(
        this,
        new IProperty[] { PROP_FACING, PROP_TYPE },
        new IUnlistedProperty[] { PROP_INPUT, PROP_OUTPUT, PROP_CONNECTED });
  }

  @Override
  public @NotNull IExtendedBlockState getExtendedState(@NotNull IBlockState state, IBlockAccess world, @NotNull BlockPos pos) {
    IExtendedBlockState ext = (IExtendedBlockState) state;
    TileEntityEnergyMeterBase tile = (TileEntityEnergyMeterBase) world.getTileEntity(pos);

    if (tile != null) {
      ext = ext.withProperty(PROP_INPUT, tile.getInputSide())
          .withProperty(PROP_OUTPUT, tile.getOutputSide())
          .withProperty(PROP_CONNECTED, tile.isFullyConnected());
    }

    return ext;
  }

  @Override
  public void onBlockPlacedBy(World world, @NotNull BlockPos pos, IBlockState state, @NotNull EntityLivingBase placer, @NotNull ItemStack stack) {
    world.setBlockState(pos, state.withProperty(PROP_FACING, getFacingFromEntity(pos, placer)), 2);
  }

  public EnumFacing getFacingFromEntity(BlockPos clickedBlock, EntityLivingBase entity) {
    EnumFacing facing =  EnumFacing.getFacingFromVector(
        (float) (entity.posX - clickedBlock.getX()),
        (float) (entity.posY - clickedBlock.getY()),
        (float) (entity.posZ - clickedBlock.getZ()));
    if (facing.getAxis() == EnumFacing.Axis.Y) {
      facing = EnumFacing.NORTH;
    }
    return facing;
  }

  @Override
  public boolean canConnectRedstone(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @Nullable EnumFacing side) {
    return side != null && side != state.getValue(PROP_FACING).getOpposite();
  }

  @Override
  public void getSubBlocks(@NotNull CreativeTabs tabs, @NotNull NonNullList<ItemStack> items) {
    for (MeterType type : MeterType.values()) {
      if (type.getEnergyType().isAvailable()) {
        items.add(new ItemStack(this, 1, type.getIndex()));
      }
    }
  }

  @Override
  public int damageDropped(IBlockState state) {
    MeterType type = state.getValue(PROP_TYPE);
    return type.getIndex();
  }

  public @NotNull ItemStack getPickBlock(IBlockState state, @NotNull RayTraceResult target, @NotNull World world, @NotNull BlockPos pos, @NotNull EntityPlayer player) {
    MeterType type = state.getValue(PROP_TYPE);
    return new ItemStack(Item.getItemFromBlock(this), 1, type.getIndex());
  }

  @SideOnly(Side.CLIENT)
  public void registerItemModel(Item item) {
    for (MeterType type : MeterType.values()) {
      ModelLoader.setCustomModelResourceLocation(item, type.getIndex(),
          new ModelResourceLocation(
                  Objects.requireNonNull(getRegistryName()), "inventory_" + type.getName()));
    }
  }

  public enum MeterType implements IStringSerializable  {
    FE_METER(0, EnergyTypes.FE),
    MJ_METER(1, EnergyTypes.MJ),
    EU_METER(2, EnergyTypes.EU);

    private final int index;
    private final EnergyType type;

    MeterType(int index, EnergyType type) {
      this.index = index;
      this.type = type;
    }

    public int getIndex() {
      return this.index;
    }

    public EnergyType getEnergyType() {
      return this.type;
    }

    @Override
    public @NotNull String getName() {
      return this.type.getName().toLowerCase();
    }
  }
}
