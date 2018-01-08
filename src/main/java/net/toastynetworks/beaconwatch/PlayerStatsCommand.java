package net.toastynetworks.beaconwatch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;


public class PlayerStatsCommand implements CommandExecutor{
	private BeaconWatch instance;
	
	public PlayerStatsCommand(BeaconWatch instance) {
		this.instance = instance;
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (src instanceof Player) {
			Player p = (Player) src;
			try {
				ResultSet rs = instance.getStatements().selectPlayerStats.executeQuery(p.getUniqueId().toString());
				if (rs.next()) {
					int timePlayed = rs.getInt(1);
					Timestamp firstLogin = rs.getTimestamp(2);
					Timestamp lastSeen = rs.getTimestamp(3);
					int beaconsDestroyed = rs.getInt(4);
					int kills = rs.getInt(5);
					int deaths = rs.getInt(6);
					int blocksBroken = rs.getInt(7);
					int blocksPlaced = rs.getInt(8);
					p.sendMessage(Text.of(TextColors.DARK_GREEN, timePlayed, " ", firstLogin, " ", lastSeen, " ", beaconsDestroyed, " ", kills, " ", deaths, " ", blocksBroken, " ", blocksPlaced));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				instance.getStatements().connection.close();
			}
		}
		return CommandResult.success();
	}
	

}
