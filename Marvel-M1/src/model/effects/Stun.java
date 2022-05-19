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
		boolean f = false;
		for(int i=0;i<c.getAppliedEffects().size();i++) {
			if(c.getAppliedEffects().get(i).getName().equals("Stun")) {
				f = true;
				break;
			}
			
		}
		if(f==true) {
			if(c.getCondition()!=Condition.ROOTED) {
				c.setCondition(Condition.INACTIVE);
				
			}
			else {
				c.setCondition(Condition.ACTIVE);
			}
	}
		else {
			if(c.getCondition()!=Condition.ROOTED) {
				c.setCondition(Condition.ACTIVE);
				
			}
		}


}
}
