package net.toastynetworks.beaconwatch;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldArchetypes;
import org.spongepowered.api.world.storage.WorldProperties;

import com.flowpowered.math.vector.Vector3i;

import net.kaikk.mc.kaiscommons.CommonUtils;
import net.kaikk.mc.kaiscommons.sql.SQLConnection;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;



@Plugin(id = "beaconwatch", name = "BeaconWatch", version = "1.1")
public class BeaconWatch {

	@Inject
	Game game;
	@Inject
	private Logger logger;
	@Inject
	private PluginContainer container;
	public static BeaconWatch instance;
	@Inject
	@DefaultConfig(sharedRoot = false)
	private Path defaultConfig;
	@Inject
	@DefaultConfig(sharedRoot = false)
	private ConfigurationLoader<CommentedConfigurationNode> configManager;
	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;
	private ConfigurationNode config;
	public long fullGameTime, resourceTime;
	public String arenaWorldName, databaseURL;
	public int minX, minY, minZ, maxX, maxY, maxZ, playersPerTeam, teamCount, minDistance, resourceProtectionRadius, minPlayersGameStart;
	
	SQLStatements statements;
	Random random = new Random();
	GamePhase phase = GamePhase.PREGAME;
	Map<TeamColor, Team> teams = new HashMap<>();
	Map<UUID, TeamMember> teamMembers = new HashMap<>();
	Map<UUID, Team> playersToTeam = new HashMap<>();
	Map<UUID, Long> loginTimes = new HashMap<>();
	int gameid = 0;
	
	@Listener
	public void onInit(GameInitializationEvent event) {
		instance = this;
		try {
			this.config = this.configManager.load();
			if (!this.defaultConfig.toFile().exists()) {
				this.config.getNode("FullGameTime").setValue(1200000);
				this.config.getNode("ResourceTime").setValue(3200);
				this.config.getNode("MinPlayersGameStart").setValue(2);
				this.config.getNode("TeamCount").setValue(2);
				this.config.getNode("PlayersPerTeam").setValue(1);
				this.config.getNode("MinDistance").setValue(100);
				this.config.getNode("ResourceProtectionRadius").setValue(75);
				this.config.getNode("ArenaWorldName").setValue("Arena");
				this.config.getNode("MinX").setValue(20);
				this.config.getNode("MinY").setValue(64);
				this.config.getNode("MinZ").setValue(20);
				this.config.getNode("MaxX").setValue(700);
				this.config.getNode("MaxY").setValue(81);
				this.config.getNode("MaxZ").setValue(700);
				this.config.getNode("database").setValue("jdbc:mysql://localhost/databasehere?user=root&password=root");
				this.configManager.save(this.config);
			}
			this.fullGameTime = this.config.getNode("FullGameTime").getLong();
			this.resourceTime = this.config.getNode("ResourceTime").getLong();
			this.minPlayersGameStart = this.config.getNode("MinPlayersGameStart").getInt();
			this.resourceProtectionRadius = (int) Math.pow(this.config.getNode("ResourceProtectionRadius").getInt(), 2);
			this.arenaWorldName = this.config.getNode("ArenaWorldName").getString();
			this.teamCount = this.config.getNode("TeamCount").getInt();
			this.playersPerTeam = this.config.getNode("PlayersPerTeam").getInt();
			this.minDistance = this.config.getNode("MinDistance").getInt();
			this.minX = this.config.getNode("MinX").getInt();
			this.minY = this.config.getNode("MinY").getInt();
			this.minZ = this.config.getNode("MinZ").getInt();
			this.maxX = this.config.getNode("MaxX").getInt();
			this.maxY = this.config.getNode("MaxY").getInt();
			this.maxZ = this.config.getNode("MaxZ").getInt();
			this.databaseURL = this.config.getNode("database").getString();
			this.logger.info("Full game time is set to: " + this.fullGameTime);
			this.logger.info("Resource phase time set to: " + this.resourceTime);
			this.logger.info("Min amount of players required to start game is set to: " + this.minPlayersGameStart);
			this.logger.info("Protection radius for resource phase is set to: " + this.resourceProtectionRadius);
			this.logger.info("Arena world is set to: " + this.arenaWorldName);
			this.logger.info("------------------------------------");
			this.logger.info("BeaconWatch was successfully loaded!");
			this.logger.info("------------------------------------");
		} catch (IOException e) {
			this.logger.warn("Error loading and or creating default configuration!");
			this.logger.warn("-------------------------------------------------------------");
			this.logger.warn("BeaconWatch was not loaded correctly due to a config error");
			this.logger.warn("-------------------------------------------------------------");
			e.printStackTrace();
		}
		try {
            DataSource dataSource = Sponge.getServiceManager().provide(SqlService.class).get().getDataSource(databaseURL);
            SQLConnection connection = new SQLConnection(dataSource);
            this.statements = new SQLStatements(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			statements.createTablePlayers.executeUpdate();
			statements.createTableGame.executeUpdate();
			statements.createTablePlayerStatistics.executeUpdate();
			statements.createTableOnlinePlayers.executeUpdate();
			//In case of crash or unexpected shutdown, we clear all contents of the table
			statements.truncateOnlinePlayer.executeUpdate();
			this.logger.info("All tables have been succesfully created in the Database!");
		} catch (SQLException e) {
			e.printStackTrace();
			this.logger.warn("Some Tables could not be created. Check your configuration!");
		} finally {
			statements.connection.close();
		}
		
        CommandSpec playerstats = CommandSpec.builder()
                .description(Text.of("Show statistics for player"))
                .executor(new PlayerStatsCommand(this))
                .build();
        game.getCommandManager().register(this, playerstats, "playerstats", "pstats", "pstat");
	}

	@Listener
	public void onUserLogin(ClientConnectionEvent.Join event) {
		Player player = event.getTargetEntity();
		if (this.phase == GamePhase.PREGAME) {
			Location<World> spawnLocation = Sponge.getServer().getWorld(Sponge.getServer().getDefaultWorldName()).get().getSpawnLocation();
			player.offer(Keys.GAME_MODE, GameModes.SURVIVAL);
			player.setLocation(spawnLocation);
			healPlayer(player);
			this.logger.info("Location of spawn is set to: " + spawnLocation);
			player.getInventory().clear();
			if (getSurvivalPlayerCount() >= this.minPlayersGameStart) {
				this.phase = GamePhase.RESOURCE;
				healPlayer(player);
				GamePhaseChangeEvent changeEvent = new GamePhaseChangeEvent.Resource(Cause.source(this).build());
				Sponge.getEventManager().post(changeEvent);
			}
		} else {
			Team team = this.getTeam(player.getUniqueId());
			if (team == null || team.isDefeated()) {
				player.offer(Keys.GAME_MODE, GameModes.SPECTATOR);
			} else {
				player.offer(Keys.GAME_MODE, GameModes.SURVIVAL);
			}
		}
		//Get Login time and put it in loginTimes map
		loginTimes.put(player.getUniqueId(), System.currentTimeMillis());
		try {
			statements.insertPlayer.executeUpdate(player.getUniqueId().toString(), player.getName());
			statements.insertOnlinePlayer.executeUpdate(player.getUniqueId().toString());
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			statements.connection.close();
		}
	}
	
	@Listener
	public void onUserDisconnect(ClientConnectionEvent.Disconnect event) {
		Player player = event.getTargetEntity();
		long playtime = (System.currentTimeMillis() - loginTimes.get(player.getUniqueId())) / 1000;
		try {
			statements.updatePlayer.executeUpdate(playtime, player.getUniqueId().toString());
			statements.deleteOnlinePlayer.executeUpdate(player.getUniqueId().toString());
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			statements.connection.close();
		}
	}
	
	/**
	 * Sets the user to max health and max hunger level
	 * @param player
	 */
	public void healPlayer(Player player) {
		int maxHunger = 20;
		player.offer(Keys.FOOD_LEVEL, maxHunger);
		player.offer(Keys.HEALTH, player.get(Keys.MAX_HEALTH).get());
	}
	
	/**
	 * Calculates new beacon coordinates with an extra check if minimum distance requirement is met and keeps track by counting attempts. Making sure it does not exceed 1000 attempts.
	 * 
	 * @return beaconlocation if valid beacon location meeting requirements can be found within 1000 attempts or else it returns null.
	 */
	public Location<World> calculateNewTeamBeaconLocation() {
		for (int attempts = 0; attempts < 10000; attempts++) {
			Location<World> beaconLocation = this.calculateRandomBeacon();
			if (doesBeaconLocationKeepsMinDistance(beaconLocation.getBlockPosition())) {
				return beaconLocation;
			}
		}
		return null;
	}
	
	/**
	 * Checks if a beacon is too close to other beacons according to minimum distance specified in config.
	 * 
	 * @param loc
	 * @return false if Beacon does not meet the minimum distance required.
	 * @return true if Beacon meets the minimum distance required.
	 */
	public boolean doesBeaconLocationKeepsMinDistance(Vector3i loc) {
		for (Team team : this.teams.values()) {
			if (team.getBeacon().getBlockPosition().distanceSquared(loc) < this.minDistance) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Get all players that have survival gamemode
	 * 
	 * @return Survival-mode players count
	 */
	public int getSurvivalPlayerCount() {
		int counter = 0;
		for (Player player : Sponge.getServer().getOnlinePlayers()) {
			if (player.gameMode().get().equals(GameModes.SURVIVAL)) {
				counter++;
			}
		}
		return counter;
	}

	/**
	 * Removes the Arena world if it exists
	 */
	public void deleteArena() {
		Optional<World> world = Sponge.getServer().getWorld(this.arenaWorldName);
		if (world.isPresent()) {
			this.logger.info("Deleting arena world...");
			Sponge.getServer().unloadWorld(world.get());
			Sponge.getServer().deleteWorld(this.getArenaWorldProperties());
		} else {
			this.logger.info("World does not exist, so it can't be removed.");
		}
	}

	/**
	 * Crates the Arena world if it does not exist yet.
	 * @throws IOException if for some reason it can not crate one.
	 */
	public void createArena() {
		try {
			this.logger.info("Creating arena world...");
			WorldProperties properties = Sponge.getServer().createWorldProperties(this.arenaWorldName, WorldArchetypes.OVERWORLD);
			if (Sponge.getServer().getWorld(this.arenaWorldName).isPresent()) {
				this.logger.info("Seems like " + this.arenaWorldName + " is loaded!");
			} else {
				this.logger.info(this.arenaWorldName + " was not loaded.....");
				this.logger.info("Loading now...");
				Sponge.getServer().loadWorld(properties);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the WorldProperties of the arena world.
	 */
	public WorldProperties getArenaWorldProperties() {
		return Sponge.getServer().getWorldProperties(this.arenaWorldName).get();
	}

	/**
	 * Places a beacon on the specified Location that we get as a parameter. 
	 * @param blockloc
	 */
	public void placeBeacon(Location<World> blockloc) {
		blockloc.setBlockType(BlockTypes.BEACON, Cause.source(container).build());
	}

	/**
	 * Calculates the Y coordinate for the beacon. Only places them on specific locations where blocktype is: Grass, Stone, Sand, Cobblestone.
	 * @param x coordinate of calculateRandomBeacon method
	 * @param z coordinate of calculateRandomBeacon method
	 * @return y coordinate if valid location was found, if not, returns -1
	 */
	public int calculateBeaconY(int x, int z) {
		World world = getArenaWorld();
		for (int y = this.maxY; y >= this.minY; y--) {
			Location<World> loc = world.getLocation(x, y, z);
			if (loc.getBlockType().equals(BlockTypes.AIR)) {
			} else if (loc.getBlockType().equals(BlockTypes.GRASS) || loc.getBlockType().equals(BlockTypes.STONE) || loc.getBlockType().equals(BlockTypes.SAND) || loc.getBlockType().equals(BlockTypes.COBBLESTONE)) {
				if (loc.add(0, 1, 0).getBlockType().equals(BlockTypes.AIR)) {
					return y + 1;
				} 
			} else {
				return -1;
			}
		}
		return -1;
	}

	/**
	 * Calculates random beacon location
	 * @return valid location of beacon, null if not found
	 */
	public Location<World> calculateRandomBeacon() {
		World world = getArenaWorld();
		for (int attempts = 0; attempts < 10000; attempts++) {
			int x = this.random.nextInt(this.maxX - this.minX) + this.minX;
			int z = this.random.nextInt(this.maxZ - this.minZ) + this.minZ;

			int y = calculateBeaconY(x, z);
			if (y != -1) {
				this.logger.info("Final beacon destination: " + world.getLocation(x, y, z));
				return world.getLocation(x, y, z);
			} else {
				this.logger.info("y = -1, recalculating...");
			}
		}
		this.logger.info("We fucked up");
		return null;
	}

	/**
	 * Get the team for the specified player's UUID
	 * 
	 * @param uuid the UUID for the player
	 * @return the Team for the player, or null if not found
	 */
	public Team getTeam(UUID uuid) {
		return this.playersToTeam.get(uuid);
	}
	
	public Team getTeam(TeamColor color) {
		return this.teams.get(color);
	}
	
	/**
	 *  
	 * @return the amount of undefeated teams
	 */
	public int getAliveTeamCount() {
		int counter = 0;
		for (Team team : this.teams.values()) {
			if (!team.isDefeated()) {
				counter++;
			}
		}
		return counter;
	}
	
	/**
	 * @return the arena world
	 */
	public World getArenaWorld() {
		return Sponge.getServer().getWorld(this.arenaWorldName).get();
	}

	@Listener
	public void afterInit(GameStartedServerEvent event) {
		this.createArena();
	}

	@Listener
	public void gameStop(GameStoppingEvent event) {
		this.deleteArena();
	}

	@Listener
	public void onDeathEvent(DestructEntityEvent.Death event, @First Player killer) {
		if (event.getTargetEntity() instanceof Player) {
			Player killedPlayer = (Player) event.getTargetEntity();
			TeamMember killerMember = this.teamMembers.get(killer.getUniqueId());
			if (killerMember != null) {
				killerMember.incrementKills();
			}
			TeamMember killedMember = this.teamMembers.get(killedPlayer.getUniqueId());
			if (killedMember != null) {
				killedMember.incrementDeaths();
			}
		}
	}
	
	@Listener
	public void onAttackEntity(AttackEntityEvent event, @First EntityDamageSource damageSource) {
		if (damageSource.getSource() instanceof Player) {
			Player source = (Player) damageSource.getSource();
			if (this.phase == GamePhase.RESOURCE) {
				if (event.getTargetEntity() instanceof Player) {
					event.setCancelled(true);
					source.sendMessage(Text.of(TextColors.RED, "You can not attack anyone in this phase."));
				}
			} else if (this.phase == GamePhase.PVP) {
				if (event.getTargetEntity() instanceof Player) {
					Player targetplayer = (Player) event.getTargetEntity();
					//Find out if the source of the damage is in the same team of the target(player)
					Team sourceTeam = this.getTeam(source.getUniqueId());
					if (sourceTeam.getPlayers().containsKey(targetplayer.getUniqueId())) {
						event.setCancelled(true);
						source.sendMessage(Text.of(TextColors.RED, "Fight the enemy, not your team, fool!"));
					}
				}
			}
		}
	}

	@Listener
	public void onBlockBreak(ChangeBlockEvent.Break event, @First Player source) {
		if (this.phase == GamePhase.RESOURCE || this.phase == GamePhase.PVP) {
			
			if (this.phase == GamePhase.RESOURCE) {
				for (Transaction<BlockSnapshot> trans : event.getTransactions()) {
					BlockSnapshot blockSnapshot = trans.getOriginal();
					BlockType blockType = blockSnapshot.getState().getType();
					if (blockType == BlockTypes.BEACON) {
						event.setCancelled(true);
						trans.setValid(false);
						source.sendMessage(Text.of(TextColors.RED, "Stop trying to destroy your own beacon.."));
					}
				}
			} else {
				for (Transaction<BlockSnapshot> trans : event.getTransactions()) {
					BlockSnapshot blockSnapshot = trans.getOriginal();
					if (blockSnapshot.getLocation().isPresent()) {
						BlockType blockType = blockSnapshot.getState().getType();
						if (blockType == BlockTypes.BEACON) {
							//Find out if the beacon that source is breaking is from their own team
							Team sourceTeam = this.getTeam(source.getUniqueId());
							if (sourceTeam.getBeacon().equals(blockSnapshot.getLocation().get())) {
								event.setCancelled(true);
								trans.setValid(false);
								source.sendMessage(Text.of(TextColors.RED, "Stop trying to destroy your own beacon.."));
							} else {
								for (Team team : this.teams.values()) {
									if (team.getBeacon().equals(blockSnapshot.getLocation().get())) {
										team.setDefeated(true);
										team.setGameMode(GameModes.SPECTATOR);
										Sponge.getServer().getBroadcastChannel().send(Text.of(team.getColor().getName(), TextColors.GOLD, " beacon has been destroyed!"));
										this.logger.info(team.getColor().getName().toPlain()+" beacon has been destroyed!");
										
										TeamMember beaconDestroyer = this.teamMembers.get(source.getUniqueId());
										if (beaconDestroyer != null) {
											beaconDestroyer.incrementBrokenBeacons();
										}
										
										if (getAliveTeamCount() <= 1) {
											this.phase = GamePhase.ENDGAME;
											GamePhaseChangeEvent changeEvent = new GamePhaseChangeEvent.EndGame(sourceTeam, Cause.source(this).build());
											Sponge.getEventManager().post(changeEvent);
										}
									}
								}
							}
						}
					}
				}
			}
			
			if (!event.isCancelled()) {
				TeamMember member = this.teamMembers.get(source.getUniqueId());
				if (member != null) {
					member.incrementBlocksBroken();
				}
			}
		}
	}
	//TODO: Fix beacon drop
	@Listener
	public void onItemDrop(DropItemEvent.Pre event) {
		event.getDroppedItems().removeIf(item -> item.getType() == ItemTypes.BEACON);
	}

	@Listener
	public void onBlockPlace(ChangeBlockEvent.Place event, @First Player source) {
		if (this.phase == GamePhase.RESOURCE || this.phase == GamePhase.PVP) {
			if (this.phase == GamePhase.RESOURCE) {
				for (Transaction<BlockSnapshot> trans : event.getTransactions()) {
					BlockSnapshot blockSnapshot = trans.getOriginal();
					if (blockSnapshot.getLocation().isPresent()) {
						for (Team team : this.teams.values()) {
							if (!team.getPlayers().containsKey(source.getUniqueId())) {
								if (source.getLocation().getPosition().distanceSquared(team.getBeacon().getPosition()) < this.resourceProtectionRadius) {							
									event.setCancelled(true);
									trans.setValid(false);
									source.sendMessage(Text.of(TextColors.RED, "You can't place blocks near enemies beacon in this phase."));
								}
							}
						}
					}
				}
			}

			for (Transaction<BlockSnapshot> trans : event.getTransactions()) {
				BlockSnapshot blockSnapshot = trans.getOriginal();
				if (blockSnapshot.getLocation().isPresent()) {
					for (Team team : this.teams.values()) {
						Location<World> blockLoc = blockSnapshot.getLocation().get();
						if (team.getBeacon().getBlockX() == blockLoc.getBlockX() && team.getBeacon().getBlockZ() == blockLoc.getBlockZ() && team.getBeacon().getBlockY() < blockLoc.getBlockY()) {
							event.setCancelled(true);
							trans.setValid(false);
							source.sendMessage(Text.of(TextColors.RED, "You can't place blocks above the beacon!"));
						}
					}
				}
			}
			
			if (!event.isCancelled()) {
				TeamMember member = this.teamMembers.get(source.getUniqueId());
				if (member != null) {
					member.incrementBlocksPlaced();
				}
			}
		}
	}

	@Listener
	public void onPlayerRespawn(RespawnPlayerEvent event) {
		Player player = event.getOriginalPlayer();
		for (Team team : this.teams.values()) {
			if (team.getPlayers().containsKey(player.getUniqueId())) {
				event.setToTransform(event.getToTransform().setLocation(team.getBeacon()));
				break;
			}
		}
	}
	
	@Listener
	public void onResourcePhase(GamePhaseChangeEvent.Resource event) {
		try {
			this.gameid = statements.insertGame.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			statements.connection.close();
		}
		
		Sponge.getServer().getBroadcastChannel().send(Text.of(TextColors.GOLD, "The Game has started!"));
		Sponge.getServer().getBroadcastChannel().send(Text.of(TextColors.GOLD, "Phase 1: Gather resources to defend your beacon."));
		Sponge.getServer().getBroadcastChannel().send(Text.of(TextColors.RED, "Phase 1: Ends in: " + CommonUtils.timeToString((int)(this.resourceTime / 1000))));
		getArenaWorldProperties().setWorldTime(0);
		Map<UUID, TeamMember> members = new HashMap<>();
		try {
			//Loop through getOnlinePlayers Collection (list)
			for (Player p : Sponge.getServer().getOnlinePlayers()) {
				healPlayer(p);
				if (p.gameMode().get().equals(GameModes.SURVIVAL)) {
					members.put(p.getUniqueId(), new TeamMember(p.getUniqueId()));
					//Using addBatch instead of executeUpdate to stack up the players that we want to insert. This will improve performance.
					statements.insertPlayerStatistics.addBatch(p.getUniqueId().toString(), this.gameid, this.teams.size());
					
					if (members.size() == this.playersPerTeam) {
						// We get the teamcolor based on how many teams there are already on the teams map
						// So when there isn't any team on the teams map, we get a size of 0, and we use that as the
						// index of the TeamColor.values() array that contains all the TeamColors, and then we add the team to the map, so the map size increases to 1
						// Next time we have to add another team, the teams map size is 1, so we get the second element of the TeamColor.values() array... and so on.
						TeamColor teamColor = TeamColor.values()[this.teams.size()];
						Team team = new Team(teamColor, members, this.calculateNewTeamBeaconLocation());
						this.teams.put(teamColor, team);
						this.teamMembers.putAll(members);
						this.placeBeacon(team.getBeacon());
						this.logger.info("New team: " + team.toString());
						for (TeamMember member : members.values()) {
							Player teamPlayer = member.getPlayer().get();
							teamPlayer.setLocation(team.getBeacon());
							this.playersToTeam.put(member.getPlayerId(), team);
						}
						members = new HashMap<>();
					}
				}
			}
			//This will actually insert all the players that were queued up from before.
			statements.insertPlayerStatistics.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			statements.connection.close();
		}
		
		Task.builder().execute(() -> {
			this.phase = GamePhase.PVP;
			GamePhaseChangeEvent changeEvent2 = new GamePhaseChangeEvent.PvP(Cause.source(this).build());
			Sponge.getEventManager().post(changeEvent2);
		}).delay(resourceTime, TimeUnit.MILLISECONDS).submit(this);

		Task.builder().execute(task -> {
			if (this.phase == GamePhase.RESOURCE) {
				for (Player p : Sponge.getServer().getOnlinePlayers()) {
					if (p.gameMode().get().equals(GameModes.SURVIVAL)) {
						for (Team team : this.teams.values()) {
							if (!team.getPlayers().containsKey(p.getUniqueId())) {
								if (p.getLocation().getPosition().distanceSquared(team.getBeacon().getPosition()) < this.resourceProtectionRadius) {
									PotionEffect blindness = PotionEffect.builder().potionType(PotionEffectTypes.BLINDNESS).duration(100).build();
									PotionEffect wither = PotionEffect.builder().potionType(PotionEffectTypes.WITHER).duration(100).amplifier(1).build();
									PotionEffect mining = PotionEffect.builder().potionType(PotionEffectTypes.MINING_FATIGUE).duration(100).amplifier(10).build();
									PotionEffectData effects = p.getOrCreate(PotionEffectData.class).get();
									effects.addElement(blindness);
									effects.addElement(wither);
									effects.addElement(mining);
									p.offer(effects);
									break;
								}
							}
						}
					}
				}
			} else {
				task.cancel();
			}
		}).intervalTicks(20).submit(this);
	}
	
	@Listener
	public void onPvPPhase(GamePhaseChangeEvent.PvP event) {
		Sponge.getServer().getBroadcastChannel().send(Text.of(TextColors.GOLD, "PVP has been enabled!"));
		Sponge.getServer().getBroadcastChannel().send(Text.of(TextColors.GOLD, "Phase 2: Find and destroy enemy beacons!"));
	}
	
	@Listener
	public void onEndGamePhase(GamePhaseChangeEvent.EndGame event) {
		Sponge.getServer().getBroadcastChannel().send(Text.of(TextColors.GOLD, "Team: ", event.getWinningTeam().getColor().getName(), " is victorious!" ));
		Sponge.getServer().getBroadcastChannel().send(Text.of(TextColors.GOLD, "Phase 3: Go home and get drunk, celebrate your victory or drink away your loss!"));
		Sponge.getServer().getBroadcastChannel().send(Text.of(TextColors.RED, "Phase 3: Server is resetting in 30 seconds."));
		try {
			for (TeamMember member : this.teamMembers.values()) {
				statements.updatePlayerStatistics.addBatch(member.getBrokenBeacons(), member.getKills(), member.getDeaths(), member.getBlocksBroken(), member.getBlocksPlaced(), this.gameid, member.getPlayerId().toString());
			}
			statements.updatePlayerStatistics.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			statements.connection.close();
		}
		Task.builder().execute(task ->{
			Sponge.getServer().shutdown(Text.of(TextColors.RED,"BeaconWatch is restarting, join back in 30 seconds!"));
		}).delay(30, TimeUnit.SECONDS).submit(this);
		try {
			statements.updateGame.executeUpdate(event.getWinningTeam().getColor().ordinal(), this.gameid);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			statements.connection.close();
		}
	}
}
