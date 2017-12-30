package net.toastynetworks.ToastyWalls;

import com.flowpowered.math.vector.Vector3i;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldArchetypes;
import org.spongepowered.api.world.storage.WorldProperties;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Random;

import static com.google.common.base.Preconditions.checkNotNull;

@Plugin(id="toastywalls", name="ToastyWalls", version="1.0")
public class ToastyWalls {
    @Inject
    Game game;
    @Inject
    private Logger logger;
    @Inject
    private PluginContainer container;
    public static ToastyWalls instance;
    @Inject
    @DefaultConfig(sharedRoot=false)
    private Path defaultConfig;
    @Inject
    @DefaultConfig(sharedRoot=false)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;
    @Inject
    @ConfigDir(sharedRoot=false)
    private Path configDir;
    private ConfigurationNode config;
    public long fullGameTime;
    public long resourceTime;
    public int minPlayersGameStart;
    public int minTeams;
    public int maxTeams;
    public int minPlayersPerTeam;
    public int maxPlayersPerTeam;
    public int minDistanceBeacons;
    public int maxDistanceBeacons;
    public int resourceProtectionRadius;
    public String arenaWorldName;
    public int MinX;
    public int MinY;
    public int MinZ;
    public int MaxX;
    public int MaxY;
    public int MaxZ;

    @Listener
    public void onInit(GameInitializationEvent event) {
        instance = this;
        try {
            this.config = this.configManager.load();
            if (!this.defaultConfig.toFile().exists()) {
                this.config.getNode(new Object[]{"FullGameTime"}).setValue((Object)1200000);
                this.config.getNode(new Object[]{"ResourceTime"}).setValue((Object)600000);
                this.config.getNode(new Object[]{"MinPlayersGameStart"}).setValue((Object)8);
                this.config.getNode(new Object[]{"MinTeams"}).setValue((Object)2);
                this.config.getNode(new Object[]{"MaxTeams"}).setValue((Object)5);
                this.config.getNode(new Object[]{"MinPlayersPerTeam"}).setValue((Object)2);
                this.config.getNode(new Object[]{"MaxPlayersPerTeam"}).setValue((Object)10);
                this.config.getNode(new Object[]{"MinDistanceBeacons"}).setValue((Object)100);
                this.config.getNode(new Object[]{"MaxDistanceBeacons"}).setValue((Object)500);
                this.config.getNode(new Object[]{"ResourceProtectionRadius"}).setValue((Object)75);
                this.config.getNode(new Object[]{"ArenaWorldName"}).setValue((Object)"Arena");
                this.config.getNode(new Object[]{"MinX"}).setValue((Object)20);
                this.config.getNode(new Object[]{"MinY"}).setValue((Object) 20);
                this.config.getNode(new Object[]{"MinZ"}).setValue((Object)20);
                this.config.getNode(new Object[]{"MaxX"}).setValue((Object) 700);
                this.config.getNode(new Object[]{"MaxY"}).setValue((Object) 80);
                this.config.getNode(new Object[]{"MaxZ"}).setValue((Object) 700);
                this.configManager.save(this.config);
            }
            this.fullGameTime = this.config.getNode(new Object[]{"FullGameTime"}).getLong();
            this.resourceTime = this.config.getNode(new Object[]{"ResourceTime"}).getLong();
            this.minPlayersGameStart = this.config.getNode(new Object[]{"MinPlayersGameStart"}).getInt();
            this.minTeams = this.config.getNode(new Object[]{"MinTeams"}).getInt();
            this.maxTeams = this.config.getNode(new Object[]{"MaxTeams"}).getInt();
            this.minPlayersPerTeam = this.config.getNode(new Object[]{"MinPlayersPerTeam"}).getInt();
            this.maxPlayersPerTeam = this.config.getNode(new Object[]{"MaxPlayersPerTeam"}).getInt();
            this.minDistanceBeacons = this.config.getNode(new Object[]{"MinDistanceBeacons"}).getInt();
            this.maxDistanceBeacons = this.config.getNode(new Object[]{"MaxDistanceBeacons"}).getInt();
            this.resourceProtectionRadius = this.config.getNode(new Object[]{"ResourceProtectionRadius"}).getInt();
            this.arenaWorldName = this.config.getNode(new Object[]{"ArenaWorldName"}).getString();
            this.MinX = this.config.getNode(new Object[]{"MinX"}).getInt();
            this.MinY = this.config.getNode(new Object[]{"MinY"}).getInt();
            this.MinZ = this.config.getNode(new Object[]{"MinZ"}).getInt();
            this.MaxX = this.config.getNode(new Object[]{"MaxX"}).getInt();
            this.MaxY = this.config.getNode(new Object[]{"MaxY"}).getInt();
            this.MaxZ = this.config.getNode(new Object[]{"MaxZ"}).getInt();
            this.logger.info("Full game time is set to: " + this.fullGameTime);
            this.logger.info("Resource phase time set to: " + this.resourceTime);
            this.logger.info("Min amount of players required to start game is set to: " + this.minPlayersGameStart);
            this.logger.info("Min amount of teams is set to: " + this.minTeams);
            this.logger.info("Max amount of teams is set to: " + this.maxTeams);
            this.logger.info("Min amount of players per team is set to: " + this.minPlayersPerTeam);
            this.logger.info("Max amount of players per team is set to: " + this.maxPlayersPerTeam);
            this.logger.info("Min distance of the beacons is set to: " + this.minDistanceBeacons);
            this.logger.info("Max distance of the beacons is set to: " + this.maxDistanceBeacons);
            this.logger.info("Protection radius for resource phase is set to: " + this.resourceProtectionRadius);
            this.logger.info("Arena world is set to: " + this.arenaWorldName);
            this.logger.info("------------------------------------");
            this.logger.info("ToastyWalls was successfully loaded!");
            this.logger.info("------------------------------------");
        }
        catch (IOException e) {
            this.logger.warn("Error loading and or creating default configuration!");
            this.logger.warn("-------------------------------------------------------------");
            this.logger.warn("ToastyWalls was not loaded correctly due to a config error");
            this.logger.warn("-------------------------------------------------------------");
            e.printStackTrace();
        }
    }

    @Listener
    public void userLogin(ClientConnectionEvent.Join event) {
        Location location = ((World)Sponge.getServer().getWorld(Sponge.getServer().getDefaultWorldName()).get()).getSpawnLocation();
        Player player = event.getTargetEntity();
        player.setLocation(location);
        this.logger.info("Location of spawn is set to: " + (Object)location);
    }

    public void deleteArena() {
        Optional world = Sponge.getServer().getWorld(this.arenaWorldName);
        if (world.isPresent()) {
            this.logger.info("Deleting arena world...");
            Sponge.getServer().unloadWorld((World)world.get());
            Sponge.getServer().deleteWorld(this.getArenaWorldProperties());
        } else {
            this.logger.info("World does not exist, so it can't be removed.");
        }
    }

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
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public WorldProperties getArenaWorldProperties() {
        return (WorldProperties)Sponge.getServer().getWorldProperties(this.arenaWorldName).get();
    }

    public void placeBeacon(Location<World> blockloc) {
        blockloc.setBlockType(BlockTypes.BEACON, Cause.source(container).build());
    }

    public int calculateBeaconY(int x, int z) {
        int yMin = this.MinY;
        int yMax = this.MaxY;
        int y = yMax;
        this.logger.info("Printing debug message: - before " + "yMin: " + yMin);
        this.logger.info("Printing debug message: - before " + "yMax: " + yMax);
        this.logger.info("Printing debug message: - before " + "y: " + y);
        Optional<World> world = Sponge.getServer().getWorld(this.arenaWorldName);
        Location loc = world.get().getLocation(x, y, z);
        if (world.isPresent()) {
            while (yMin <= y) {
                this.logger.info("Printing debug message:" + "yMin: " + yMin);
                this.logger.info("Printing debug message:" + "y: " + y);
                loc = world.get().getLocation(x, y, z);
                if (loc.getExtent().isLoaded()) {
                    Vector3i chunk = loc.getChunkPosition();
                    Sponge.getServer().getWorld(this.arenaWorldName).get().loadChunk(chunk, true);
                    this.logger.info("Chunk generated on: " + chunk);
                }
                if (loc.getBlockType().equals(BlockTypes.AIR)) {
                    this.logger.info("Block type: " + loc.getBlockType().getName());
                    this.logger.info("Passed check 1 (air)");
                    y--;
                    continue;
                }
                if (loc.getBlockType().equals(BlockTypes.GRASS) || loc.getBlockType().equals(BlockTypes.STONE) || loc.getBlockType().equals(BlockTypes.SAND) || loc.getBlockType().equals(BlockTypes.COBBLESTONE)) {
                    this.logger.info("Passed check 2 (solid block)");
                    if (loc.add(0, 1, 0).getBlockType().equals(BlockTypes.AIR)) {
                        this.logger.info("Passed check 3 (Air above solid block)");
                        y = (int) loc.getY();
                        this.logger.info("Found a good location! " + "Coordinate Y: " + y);
                        break;
                    } else {
                        this.logger.info("Passed check 4 (if no air above solid block)");
                        y--;
                        continue;
                    }
                }
                //if there is no valid location, set y to -1
                y = -1;
                this.logger.info("Line 218: " + y);
            }
            if (y == -1) {
                this.logger.info("Could not find any coordinates! " + "Y coordinate is: " + loc.getY());
                this.logger.info("That sucks.." + "Y variable is set to: " + y);
            } else {
                this.logger.info("Found some juicy coordinates! " + "Y coordinate is: " + loc.getY());
                this.logger.info("Just for testing purposes, variable Y= " + y);
                this.logger.info("Just for testing purposes, variable yMax= " + yMax);
            }
        } else {
            this.logger.warn(this.arenaWorldName + " world could not be found!");
        }
        return y;
    }

    public Location<World> calculateRandomBeacon() {
        Random random = new Random();
        World world = Sponge.getServer().getWorld(this.arenaWorldName).get();
        int xMin = this.MinX;
        int zMin = this.MinZ;
        int xMax = this.MaxX;
        int zMax = this.MaxZ;

        int x = random.nextInt((Math.abs(xMax - (xMin) + 1) + (xMin)));
        int z = random.nextInt((Math.abs(zMax - (zMin) + 1) + (zMin)));

        int y = calculateBeaconY(x, z);
        if (y != -1) {
            this.logger.info("Final beacon destination: " + world.getLocation(x, y, z));
            return world.getLocation(x, y, z);
        } else if (y == -1) {
            x = random.nextInt((Math.abs(xMax - (xMin) + 1) + (xMin)));
            z = random.nextInt((Math.abs(zMax - (zMin) + 1) + (zMin)));
            this.logger.info("y = -1, recalculating...");
            y = calculateBeaconY(x, z);
            return world.getLocation(x, y, z);
        } else {
            this.logger.warn("Something didn't go as planned!");
        }
        this.logger.info("Reached final - Method calculateRandomBeacons");
        return null;
    }

    @Listener
    public void afterInit(GameStartedServerEvent event) {
        this.createArena();
        this.placeBeacon(calculateRandomBeacon());
    }

    @Listener
    public void gameStop(GameStoppingEvent event) {
        this.deleteArena();
    }
}
