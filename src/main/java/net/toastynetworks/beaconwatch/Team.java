package net.toastynetworks.beaconwatch;

import java.util.Map;
import java.util.UUID;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class Team {
    private TeamColor color;
    private Map<UUID, TeamMember> players;
    private Location<World> beacon;
    private boolean defeated;

    public Team(TeamColor color, Map<UUID, TeamMember> players, Location<World> beacon) {
        this.color = color;
        this.players = players;
        this.beacon = beacon;
    }

    /**
     * @return color of the specified Team
     */
    public TeamColor getColor() {
        return color;
    }

    /**
     * @return all players of a specified team
     */
    public Map<UUID, TeamMember> getPlayers() {
        return players;
    }

    /**
     * @return location of the beacon from the specified team
     */
    public Location<World> getBeacon() {
        return beacon;
    }
    
    /**
     * @return false if team is not defeated or true if team has been defeated.
     */
    public boolean isDefeated() {
		return defeated;
	}

	/**
	 * Set the boolean defeated to true ot false if the team has lost or won.
	 * @param defeated
	 */
	public void setDefeated(boolean defeated) {
		this.defeated = defeated;
	}
	
	/**
	 * Set the gamemode of a player in a specific team.
	 * @param gameMode
	 */
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
