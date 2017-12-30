package net.toastynetworks.ToastyWalls;

import com.flowpowered.math.vector.Vector3d;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldArchetype;
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
                this.config.getNode(new Object[]{"MinY"}).setValue((Object)0);
                this.config.getNode(new Object[]{"MinZ"}).setValue((Object)20);
                this.config.getNode(new Object[]{"MaxX"}).setValue((Object)100);
                this.config.getNode(new Object[]{"MaxY"}).setValue((Object)120);
                this.config.getNode(new Object[]{"MaxZ"}).setValue((Object)100);
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
            this.MinY = this.config.getNode(new Object[]{"MaxY"}).getInt();
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

    public Location<World> getRandomLocation(World world) {
        Random random = new Random();
        int xMin = this.MinX;
        int yMin = this.MinY;
        int zMin = this.MinZ;
        int xMax = this.MaxX;
        int yMax = this.MaxY;
        int zMax = this.MaxZ;

        int x = random.nextInt((Math.abs(xMax - (xMin) + 1) + (xMin)));
        int y = random.nextInt((Math.abs(yMax - (yMin) + 1) + (yMin)));
        int z = random.nextInt((Math.abs(zMax - (zMin) + 1) + (zMin)));

//        int x = (int)Math.random();
//        int y = (int)Math.random();
//        int z = (int)Math.random();
        return world.getLocation(x,y,z);
    }

    public Location<World> getRandomBeaconLocation(World world) {

    }

    @Listener
    public void afterInit(GameStartedServerEvent event) {
        this.createArena();
//        Optional<World> world = Sponge.getServer().getWorld(this.arenaWorldName);
//        if (world.isPresent()) {
//            Location location = getRandomLocation(world.get());
//            if (location.getBlockType() == BlockTypes.AIR) {
//                this.placeBeacon(location);
//                this.logger.info("Location: " + location);
//                this.logger.info("Beacons placed at: " + (Object)location);
//            } else {
//                this.logger.error("Not location could be found to place beacons ");
//                Location location2 = getRandomLocation(world.get());
//                this.placeBeacon(location2);
//            }
//        } else {
//            this.logger.error("Beacons have not been placed due to world not being available");
//        }
//
        int maxAttempts = 1000;
        int attempts = 0;
        Location<World> location = null;
        Optional<World> world = Sponge.getServer().getWorld(this.arenaWorldName);
        if (world.isPresent()) {
            while (location == null && attempts < maxAttempts) {
                attempts++;
                Location randomLocation = getRandomLocation(world.get());
                if (randomLocation.getBlockType().equals(BlockTypes.AIR) && randomLocation.add(0,1,0).getBlockType().equals(BlockTypes.AIR) && randomLocation.add(1,0,0).getBlockType().equals(BlockTypes.AIR) && randomLocation.add(0,0,1).getBlockType().equals(BlockTypes.AIR)) {
                    this.placeBeacon(randomLocation);
                    location = randomLocation;
                    this.logger.info("Beacons successfully placed at: " + location + " after " + attempts + " Attempts!");
                } else {
                    String item = randomLocation.getBlockType().getName();
                    this.logger.info("Random location already taken at: " + randomLocation + " with block name: " + item);
                }
            }
            if (location == null) {
                this.logger.error("Could not find suitable location for beacons after: " + attempts + " attempts!");
            }
        }

    }

    @Listener
    public void gameStop(GameStoppingEvent event) {
        this.deleteArena();
    }
}
