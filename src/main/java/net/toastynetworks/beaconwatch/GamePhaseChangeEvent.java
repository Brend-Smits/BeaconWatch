package net.toastynetworks.beaconwatch;

import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

public abstract class GamePhaseChangeEvent extends AbstractEvent {
	private GamePhase phase;
	private Cause cause;
	
	public GamePhaseChangeEvent(GamePhase phase, Cause cause) {
		this.phase = phase;
		this.cause = cause;
	}

	public GamePhase getPhase() {
		return phase;
	}

	@Override
	public Cause getCause() {
		return cause;
	}
	//'super' refers to the constructor of the GamePhaseChangeEvent
	public static class Resource extends GamePhaseChangeEvent {
		public Resource(Cause cause) {
			super(GamePhase.RESOURCE, cause);
		}
	}
	
	public static class PvP extends GamePhaseChangeEvent {
		public PvP(Cause cause) {
			super(GamePhase.PVP, cause);
		}
	}
	
	public static class EndGame extends GamePhaseChangeEvent {
		private Team winningTeam;
		
		public EndGame(Team winningTeam, Cause cause) {
			super(GamePhase.ENDGAME, cause);
			this.winningTeam = winningTeam;
		}

		public Team getWinningTeam() {
			return winningTeam;
		}
	}
}
