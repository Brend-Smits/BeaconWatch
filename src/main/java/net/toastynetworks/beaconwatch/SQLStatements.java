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
		this.createTablePlayers = connection.prepareCachedStatement("CREATE TABLE IF NOT EXISTS `Players`( `UUID` VARCHAR(255) NOT NULL , `Name` VARCHAR(255) NOT NULL , `First login` TIME NOT NULL , `Last login` TIME NOT NULL , `Timeplayed` INT NOT NULL , PRIMARY KEY (`UUID`))");
		this.createTableGameStats = connection.prepareCachedStatement("CREATE TABLE IF NOT EXISTS `GameStats`( `GameID` INT NOT NULL , `Time game starts` TIME NOT NULL , `Time game ends` TIME NOT NULL , `Winning team` VARCHAR(255) NOT NULL , PRIMARY KEY (`GameID`))");
		this.createTablePlayerStatistics = connection.prepareCachedStatement("CREATE TABLE IF NOT EXISTS `PlayerStatistics`( `GameID` INT NOT NULL , `UUID` VARCHAR(255) NOT NULL , `Beacons Destroyed` INT NOT NULL , `Kills` INT NOT NULL , `Deaths` INT NOT NULL , `Blocks Broken` INT NOT NULL , `Blocks Placed` INT NOT NULL , `Team` VARCHAR(255) NOT NULL , PRIMARY KEY (`GameID`, `UUID`))");
		this.createTableOnlinePlayers = connection.prepareCachedStatement("CREATE TABLE IF NOT EXISTS `OnlinePlayers`( `GameID` INT NOT NULL , `UUID` VARCHAR(255) NOT NULL , `Beacon destroyed` INT NOT NULL , `Team` VARCHAR(255) NOT NULL , PRIMARY KEY (`GameID`))");
		this.insertPlayer = connection.prepareCachedStatement("INSERT IGNORE Players(UUID, Name) VALUE(?)", Types.VARCHAR);
	}

}
