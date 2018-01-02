package net.toastynetworks.beaconwatch;

import java.util.Map;
import java.util.UUID;

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class Team {
    private TextColor color;
    private Map<UUID, TeamMember> players;
    private Location<World> beacon;
    private boolean defeated;

    public Team(TextColor color, Map<UUID, TeamMember> players, Location<World> beacon) {
        this.color = color;
        this.players = players;
        this.beacon = beacon;
    }

    public TextColor getColor() {
        return color;
    }

    public Map<UUID, TeamMember> getPlayers() {
        return players;
    }

    public Location<World> getBeacon() {
        return beacon;
    }
    
    public boolean isDefeated() {
		return defeated;
	}

	public void setDefeated(boolean defeated) {
		this.defeated = defeated;
	}
	
	public void setGameMode(GameMode gameMode) {
		for (TeamMember member : players.values()) {
			if (member.getPlayer().isPresent()) {
				member.getPlayer().get().offer(Keys.GAME_MODE, gameMode);
			}
		}
	}

	@Override
    public String toString() {
        return "Team{" +
                "color=" + color +
                ", players=" + players +
                ", beacon=" + beacon +
                '}';
    }
}
