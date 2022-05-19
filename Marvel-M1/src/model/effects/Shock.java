package model.effects;

import model.world.Champion;

public class Shock extends Effect {

	public Shock(int duration) {
		super("Shock", duration, EffectType.DEBUFF);
		
	}

	public void apply(Champion c) {
		c.setSpeed((int)(c.getSpeed() * 0.9));
		c.setAttackDamage((int)(c.getAttackDamage() * 0.9));
		c.setMaxActionPointsPerTurn((int)(c.getMaxActionPointsPerTurn() - 1));
		c.setCurrentActionPoints((int)(c.getCurrentActionPoints() - 1));
	}
	public void remove(Champion c) {
		c.setSpeed((int)(c.getSpeed() / 0.9));
		c.setAttackDamage((int)(c.getAttackDamage()/ 0.9));
		c.setMaxActionPointsPerTurn((int)(c.getMaxActionPointsPerTurn() + 1));
		c.setCurrentActionPoints((int)(c.getCurrentActionPoints() + 1));
	}

}
