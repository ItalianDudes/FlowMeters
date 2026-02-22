package it.italiandudes.flowmeters.block;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockFluidMeter extends ItemBlock {
  public ItemBlockFluidMeter(BlockFluidMeter block) {
    super(block);
    this.setHasSubtypes(true);
    this.setMaxDamage(0);
  }

  @Override
  public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, List<String> tooltip,
                             @NotNull ITooltipFlag flagIn) {
    tooltip.add("Supports Fluids");
  }

  @Override
  public @NotNull EnumActionResult onItemUse(@NotNull EntityPlayer player, @NotNull World worldIn, @NotNull BlockPos pos, @NotNull EnumHand hand,
                                             @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
    return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
  }
}
