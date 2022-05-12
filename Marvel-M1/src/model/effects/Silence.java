package model.effects;

import model.world.Champion;

public class Silence extends Effect {

	public Silence( int duration) {
		super("Silence", duration, EffectType.DEBUFF);
		
	}

	public void apply(Champion c) {
	
		c.setMaxActionPointsPerTurn((int)(c.getMaxActionPointsPerTurn() + 2));
		c.setCurrentActionPoints((int)(c.getCurrentActionPoints() + 2));
	}
	public void remove(Champion c) {
		c.getAppliedEffects().remove(this);
		c.setMaxActionPointsPerTurn((int)(c.getMaxActionPointsPerTurn() - 2));
		c.setCurrentActionPoints((int)(c.getCurrentActionPoints() - 2));
	}

}
