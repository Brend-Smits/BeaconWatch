package net.toastynetworks.beaconwatch;

import java.sql.Types;

import net.kaikk.mc.kaiscommons.sql.SQLConnection;

public class SQLStatements {
	SQLConnection.PreparedCachedStatement createTablePlayers;
	SQLConnection.PreparedCachedStatement createTableGameStats;
	SQLConnection.PreparedCachedStatement createTablePlayerStatistics;
	SQLConnection.PreparedCachedStatement createTableOnlinePlayers;
	SQLConnection.PreparedCachedStatement insertPlayer;

	
	public SQLConnection connection;
	public SQLStatements(SQLConnection connection) {
		this.connection = connection;
		this.createTablePlayers = connection.prepareCachedStatement("CREATE TABLE IF NOT EXISTS players (uuid CHAR(36) NOT NULL, name CHAR(16) NOT NULL, first_login TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, last_seen TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, timeplayed INT NOT NULL DEFAULT 0, PRIMARY KEY (uuid))");
		this.createTableGameStats = connection.prepareCachedStatement("CREATE TABLE IF NOT EXISTS gamestats (gameid INT NOT NULL, time_game_starts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, time_game_ends TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, winning_team TINYINT(3) UNSIGNED NOT NULL, PRIMARY KEY (gameid))");
		this.createTablePlayerStatistics = connection.prepareCachedStatement("CREATE TABLE IF NOT EXISTS playerstatistics (gameid INT NOT NULL, uuid CHAR(36) NOT NULL, beacons_destroyed INT NOT NULL, kills INT NOT NULL, deaths INT NOT NULL, blocks_broken INT NOT NULL, blocks_placed INT NOT NULL, team TINYINT(3) UNSIGNED NOT NULL, PRIMARY KEY (gameid, uuid))");
		this.createTableOnlinePlayers = connection.prepareCachedStatement("CREATE TABLE IF NOT EXISTS onlineplayers (gameid INT NOT NULL, uuid CHAR(36) NOT NULL, beacons_destroyed INT NOT NULL, team TINYINT(3) UNSIGNED NOT NULL, PRIMARY KEY (gameid))");
		this.insertPlayer = connection.prepareCachedStatement("INSERT IGNORE players (uuid, name) VALUES (?, ?)", Types.CHAR, Types.CHAR);
	}
}
