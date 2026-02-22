package it.italiandudes.flowmeters.block;

import it.italiandudes.flowmeters.FlowMeters;
import it.italiandudes.flowmeters.integration.ModIDs;
import it.italiandudes.flowmeters.properties.UnlistedPropertyBoolean;
import it.italiandudes.flowmeters.properties.UnlistedPropertyFacing;
import it.italiandudes.flowmeters.tile.TileEntityFluidMeter;
import it.italiandudes.flowmeters.tile.TileEntityFluidMeterBase;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
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

public class BlockFluidMeter extends BlockBase {

  public static final String NAME = "fluid_meter";
  public static final PropertyDirection PROP_FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

  public static final UnlistedPropertyFacing PROP_INPUT = UnlistedPropertyFacing.create("input");
  public static final UnlistedPropertyFacing PROP_OUTPUT = UnlistedPropertyFacing.create("output");
  public static final UnlistedPropertyBoolean PROP_CONNECTED = UnlistedPropertyBoolean.create("connected");

  public BlockFluidMeter() {
    super(Material.IRON, NAME);

    this.setSoundType(SoundType.METAL);

    this.setDefaultState(blockState.getBaseState()
        .withProperty(PROP_FACING, EnumFacing.NORTH));

    this.setHarvestLevel("pickaxe", 1);
  }

  @Override
  public TileEntity createTileEntity(@NotNull World world, @NotNull IBlockState state) {
    return new TileEntityFluidMeter();
  }

  @Override
  public boolean hasTileEntity(@NotNull IBlockState state) {
    return true;
  }

  @Override
  public void neighborChanged(@NotNull IBlockState state, @NotNull World world, @NotNull BlockPos pos, @NotNull Block blockIn,
                              @NotNull BlockPos fromPos) {
    super.neighborChanged(state, world, pos, blockIn, fromPos);

    TileEntityFluidMeterBase tile = (TileEntityFluidMeterBase) world.getTileEntity(pos);
    if (tile != null) {
      tile.onNeighborChanged(fromPos, state);
    }
  }

  @Override
  public @NotNull IBlockState getStateForPlacement(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                                   float hitX, float hitY, float hitZ, int meta, @NotNull EntityLivingBase placer) {
    return this.getDefaultState();
  }

  @Override
  public @NotNull IBlockState getStateForPlacement(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing, float hitX,
                                                   float hitY, float hitZ, int meta, @NotNull EntityLivingBase placer, @NotNull EnumHand hand) {
    /*
     * Terrible, hacky fix for when using Better Builder's Wands. It appears that the mod incorrectly
     * calls 'getStateForPlacement' using world metadata instead of itemstack metadata. The easiest
     * (and ugliest) workaround is to check the stack trace and call `getStateFromMeta` is the
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
    return getDefaultState().withProperty(PROP_FACING, EnumFacing.byHorizontalIndex(facingIndex));
  }

  @Override
  public int getMetaFromState(IBlockState state) {
    return state.getValue(PROP_FACING).getHorizontalIndex();
  }

  @Override
  public boolean onBlockActivated(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state,
                                  @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
    return FlowMeters.PROXY.handleFluidBlockActivation(world, pos, player);
  }

  @Override
  protected @NotNull BlockStateContainer createBlockState() {
    return new ExtendedBlockState(
        this,
        new IProperty[] { PROP_FACING },
        new IUnlistedProperty[] { PROP_INPUT, PROP_OUTPUT, PROP_CONNECTED });
  }

  @Override
  public @NotNull IExtendedBlockState getExtendedState(@NotNull IBlockState state, IBlockAccess world, @NotNull BlockPos pos) {
    IExtendedBlockState ext = (IExtendedBlockState) state;
    TileEntityFluidMeterBase tile = (TileEntityFluidMeterBase) world.getTileEntity(pos);

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
  public void getSubBlocks(@NotNull CreativeTabs tabs, NonNullList<ItemStack> items) {
    items.add(new ItemStack(this, 1));
  }

  @SideOnly(Side.CLIENT)
  public void registerItemModel(Item item) {
    ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "inventory"));
  }

  public @NotNull ItemStack getPickBlock(@NotNull IBlockState state, @NotNull RayTraceResult target, @NotNull World world, @NotNull BlockPos pos, @NotNull EntityPlayer player) {
    return new ItemStack(Item.getItemFromBlock(this), 1);
  }
}
