package model.effects;

import model.world.Champion;

public class Embrace extends Effect {
	

	public Embrace(int duration) {
		super("Embrace", duration, EffectType.BUFF);
	}

	public void apply(Champion c) {
		
		c.setCurrentHP(c.getCurrentHP() + (int)(c.getMaxHP() * 0.20));
		c.setMana((int) (c.getMana() * 1.2));
		c.setSpeed((int) (c.getSpeed() *1.2));
		c.setAttackDamage((int)(c.getAttackDamage() * 1.20));
	}
	public void remove(Champion c) {
		c.getAppliedEffects().remove(this);
		c.setSpeed((int) (c.getSpeed()*0.84));
		c.setAttackDamage((int)(c.getAttackDamage() * 0.8));
		
	}

}
