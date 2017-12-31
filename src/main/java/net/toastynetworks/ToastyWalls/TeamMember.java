package net.toastynetworks.ToastyWalls;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;
import java.util.UUID;

public class TeamMember {
//- destroy enemy beacon (Method) - Event
//- Kill enemies (Method) - Event
//- Build (Method) - Event
//- UUID of player (property)

    private UUID uuid;

    public TeamMember(UUID uuid) {
        this.uuid = uuid;
    }

    public Optional<Player> getPlayer() {
        return Sponge.getServer().getPlayer(uuid);
    }

    @Override
    public String toString() {
        return "TeamMember{" +
                "uuid=" + uuid +
                '}';
    }
}
