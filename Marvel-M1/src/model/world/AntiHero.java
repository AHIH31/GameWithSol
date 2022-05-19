package model.world;

import java.util.ArrayList;

import model.effects.Effect;
import model.effects.Stun;

public class AntiHero extends Champion {

	public AntiHero(String name, int maxHP, int maxMana, int actions, int speed, int attackRange, int attackDamage) {
		super(name, maxHP, maxMana, actions, speed, attackRange, attackDamage);

	}
	public void useLeaderAbility(ArrayList<Champion> targets) {
		Stun a =  new Stun(2);
		for(int i=0;i<targets.size();i++) {
			a.apply(targets.get(i));
			targets.get(i).getAppliedEffects().add(a);
		}
	}

	

}
