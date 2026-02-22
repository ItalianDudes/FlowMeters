package it.italiandudes.flowmeters.block;

import it.italiandudes.flowmeters.FlowMeters;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public abstract class BlockBase extends Block {
  public BlockBase(Material material, String name) {
    super(material);

    this.setRegistryName(name);
    this.setTranslationKey(String.format("%s.%s", FlowMeters.MODID, name));
    this.setHardness(1F);
    this.setCreativeTab(FlowMeters.CREATIVE_TAB);
  }
}
