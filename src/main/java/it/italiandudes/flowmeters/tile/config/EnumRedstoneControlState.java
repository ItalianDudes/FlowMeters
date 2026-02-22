package it.italiandudes.flowmeters.tile.config;

import it.italiandudes.flowmeters.client.Sprite;
import it.italiandudes.flowmeters.client.Sprites;

import javax.annotation.Nullable;
import java.util.function.Function;

public enum EnumRedstoneControlState implements IConfigEnum {
  ACTIVE("Active", powered -> !powered),
  INVERTED("Inverted", Function.identity()),
  IGNORED("Ignored", powered -> true);

  private final String title;
  private final Function<Boolean, Boolean> checkEnabledFunction;

  EnumRedstoneControlState(String title, Function<Boolean, Boolean> checkEnabledFunction) {
    this.title = title;
    this.checkEnabledFunction = checkEnabledFunction;
  }

  @Override
  public String getDisplayName() {
    return null;
  }

  @Nullable
  @Override
  public String getDescription() {
    return this.title;
  }

  @Override
  public EnumRedstoneControlState getDefault() {
    return ACTIVE;
  }

  @Override
  public Sprite getIcon() {
    switch(ordinal()) {
      case 0: return Sprites.REDSTONE_ACTIVE;
      case 1: return Sprites.REDSTONE_INVERTED;
      case 2: return Sprites.REDSTONE_DISABLED;
      default:
        throw new RuntimeException(
            "EnumRedstoneControl attempted to get icon for invalid ordinal " + ordinal());
    }
  }

  public boolean isMachineEnabled(boolean powered) {
    return this.checkEnabledFunction.apply(powered);
  }
}
