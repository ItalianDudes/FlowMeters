package it.italiandudes.flowmeters.client.gui;

public enum RelativeBlockSide {
  FRONT("Front"),
  BACK("Back"),
  TOP("Top"),
  BOTTOM("Bottom"),
  LEFT("Left"),
  RIGHT("Right");

  private final String label;

  RelativeBlockSide(String label) {
    this.label = label;
  }

  public String getLabel() {
    return this.label;
  }
}


