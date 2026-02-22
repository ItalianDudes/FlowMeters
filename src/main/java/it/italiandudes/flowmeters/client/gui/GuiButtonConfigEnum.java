package it.italiandudes.flowmeters.client.gui;

import com.google.common.collect.ImmutableList;
import it.italiandudes.flowmeters.tile.config.IConfigEnum;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class GuiButtonConfigEnum<T extends Enum<T> & IConfigEnum> extends GuiIconButton implements IHasTooltip {
  private T value;
  private final List<T> possibleValues;
  private final String title;

  public GuiButtonConfigEnum(int index, String title, int x, int y, Class<T> enumClass, T value) {
    super(index, x, y, null);
    this.value = value;
    this.possibleValues = ImmutableList.copyOf(enumClass.getEnumConstants());
    this.title = title;

    if (this.value.getIcon() == null) {
      this.displayString = this.value.getDisplayName();
    }

    this.setIcon(this.value.getIcon());
  }

  public T cycle() {
    int newOrdinal = (value.ordinal() + 1) % possibleValues.size();
    this.value = possibleValues.get(newOrdinal);
    this.displayString = this.value.getIcon() == null ? this.value.getDisplayName() : "";
    this.setIcon(this.value.getIcon());
    return this.value;
  }

  @Override
  public List<String> getTooltipLines() {
    return ImmutableList.of(this.title, TextFormatting.GRAY + this.value.getDescription());
  }
}
