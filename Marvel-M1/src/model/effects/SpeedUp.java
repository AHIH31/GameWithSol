package model.effects;

import model.world.Champion;

public class SpeedUp extends Effect{

	public SpeedUp(int duration) {
		super("SpeedUp",duration,EffectType.BUFF);
	}

	public void apply(Champion c) {
	
		c.setSpeed((int)(c.getSpeed() * 1.15));
		c.setMaxActionPointsPerTurn((int)(c.getMaxActionPointsPerTurn() + 1));
		c.setCurrentActionPoints((int)(c.getCurrentActionPoints() + 1));
	}

	public void remove(Champion c) {
		c.getAppliedEffects().remove(this);
		c.setSpeed((int)(c.getSpeed() * 0.85));
		c.setMaxActionPointsPerTurn((int)(c.getMaxActionPointsPerTurn() - 1));
		c.setCurrentActionPoints((int)(c.getCurrentActionPoints() - 1));
	}

}
