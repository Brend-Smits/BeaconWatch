package net.toastynetworks.beaconwatch;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

public enum TeamColor {
	BLUE(TextColors.BLUE, "Blue"), RED(TextColors.RED, "Red"), GREEN(TextColors.GREEN, "Green"), DARK_PURPLE(TextColors.DARK_PURPLE, "Purple"), AQUA(TextColors.AQUA, "Aqua"), WHITE(TextColors.WHITE, "White"), GOLD(TextColors.GOLD, "Gold"), GRAY(TextColors.GRAY, "Gray");
	private TextColor color;
	private Text name;
	
	private TeamColor(TextColor color, String name) {
		this.color = color;
		this.name = Text.of(color, name);
	}

	public TextColor getColor() {
		return color;
	}

	public Text getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name.toPlain();
	}
}
