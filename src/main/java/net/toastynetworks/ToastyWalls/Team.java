package net.toastynetworks.ToastyWalls;

import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

public class Team {
    private TextColor color;
    private List<TeamMember> players;
    private Location<World> beacon;

    public Team(TextColor color, List<TeamMember> players, Location<World> beacon) {
        this.color = color;
        this.players = players;
        this.beacon = beacon;
    }

    public TextColor getColor() {
        return color;
    }

    public List<TeamMember> getPlayers() {
        return players;
    }

    public Location<World> getBeacon() {
        return beacon;
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
