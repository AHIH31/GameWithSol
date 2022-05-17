package model.effects;

import model.abilities.DamagingAbility;
import model.abilities.HealingAbility;
import model.world.Champion;

public class PowerUp extends Effect {
	

	public PowerUp(int duration) {
		super("PowerUp", duration, EffectType.BUFF);
		
	}
	public void apply(Champion c) {

		for(int i = 0; i<c.getAbilities().size(); i++) {
			
			if(c.getAbilities().get(i) instanceof DamagingAbility) {
				double x = ((((DamagingAbility) c.getAbilities().get(i))).getDamageAmount() * 1.20);
				((DamagingAbility) c.getAbilities().get(i)).setDamageAmount((int)x);
				}
			if(c.getAbilities().get(i) instanceof HealingAbility) { 
				double y =((((HealingAbility) c.getAbilities().get(i))).getHealAmount() * 1.20);
				((HealingAbility) c.getAbilities().get(i)).setHealAmount((int) y);
				}
		}
	
	}
	public void remove(Champion c) {
		c.getAppliedEffects().remove(this);
		for(int i = 0; i<c.getAbilities().size(); i++) {
			if(c.getAbilities().get(i) instanceof DamagingAbility) {
				double x =((((DamagingAbility) c.getAbilities().get(i))).getDamageAmount() / 1.2);
				((DamagingAbility) c.getAbilities().get(i)).setDamageAmount((int) x);
			}
			if(c.getAbilities().get(i) instanceof HealingAbility) {
				double y = ((((HealingAbility) c.getAbilities().get(i))).getHealAmount() / 1.2);
				((HealingAbility) c.getAbilities().get(i)).setHealAmount((int)y);
			}
		}
	}
}