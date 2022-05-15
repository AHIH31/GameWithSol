package model.effects;

import model.world.Champion;
import model.world.Condition;

public class Stun extends Effect {

	public Stun(int duration) {
		super("Stun", duration, EffectType.DEBUFF);
	}

	public void apply(Champion c) {	
		c.setCondition(Condition.INACTIVE);
	}

	public void remove(Champion c) {
		c.getAppliedEffects().remove(this);
		
		if(c.getCondition()!=Condition.ROOTED)
			c.setCondition(Condition.ACTIVE);
	}


}
