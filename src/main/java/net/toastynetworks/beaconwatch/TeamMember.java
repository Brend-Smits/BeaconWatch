package net.toastynetworks.beaconwatch;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;
import java.util.UUID;

public class TeamMember {
    private UUID uuid;
    private int kills, deaths, blocksPlaced, blocksBroken, brokenBeacons;
    
	public TeamMember(UUID uuid) {
        this.uuid = uuid;
    }

    public int getKills() {
		return kills;
	}

	public void setKills(int kills) {
		this.kills = kills;
	}
	
	public void incrementKills() {
		this.kills++;
	}

	public int getDeaths() {
		return deaths;
	}

	public void setDeaths(int deaths) {
		this.deaths = deaths;
	}
	
	/**
	 * Increases this TeamMember deaths statistics by one
	 */
	public void incrementDeaths() {
		this.deaths++;
	}

	public int getBlocksPlaced() {
		return blocksPlaced;
	}

	public void setBlocksPlaced(int blocksPlaced) {
		this.blocksPlaced = blocksPlaced;
	}
	
	public void incrementBlocksPlaced() {
		this.blocksPlaced++;
	}

	public int getBlocksBroken() {
		return blocksBroken;
	}

	public void setBlocksBroken(int blocksBroken) {
		this.blocksBroken = blocksBroken;
	}
	
	public void incrementBlocksBroken() {
		this.blocksBroken++;
	}

	public int getBrokenBeacons() {
		return brokenBeacons;
	}

	public void setBrokenBeacons(int brokenBeacons) {
		this.brokenBeacons = brokenBeacons;
	}
	
	public void incrementBrokenBeacons() {
		this.brokenBeacons++;
	}

	public UUID getPlayerId() {
		return uuid;
	}

    /**
     * @return player of team
     */
    public Optional<Player> getPlayer() {
        return Sponge.getServer().getPlayer(uuid);
    }

	@Override
	public String toString() {
		return "TeamMember [uuid=" + uuid + ", kills=" + kills + ", deaths=" + deaths + ", blocksPlaced=" + blocksPlaced
				+ ", blocksBroken=" + blocksBroken + "]";
	}

   
}
