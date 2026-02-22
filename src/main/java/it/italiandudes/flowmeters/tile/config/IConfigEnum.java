package it.italiandudes.flowmeters.tile.config;

import it.italiandudes.flowmeters.client.Sprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public interface IConfigEnum {

  @Nullable
  String getDisplayName();

  @Nullable
  String getDescription();

  Enum<? extends IConfigEnum> getDefault();

  @SideOnly(Side.CLIENT)
  @Nullable
  default Sprite getIcon() {
    return null;
  }

  default boolean isAvailable() {
    return true;
  }
}
