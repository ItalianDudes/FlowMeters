package it.italiandudes.flowmeters;

import it.italiandudes.flowmeters.block.Blocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FlowMetersCreativeTab extends CreativeTabs {
  public FlowMetersCreativeTab() {
    super(FlowMeters.MODID);
  }

  @Override
  public @NotNull ItemStack createIcon() {
    return new ItemStack(Item.getItemFromBlock(Blocks.ENERGY_METER));
  }
}
