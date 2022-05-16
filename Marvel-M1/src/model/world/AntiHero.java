package model.world;

import java.util.ArrayList;

import model.effects.Stun;

public class AntiHero extends Champion {

	public AntiHero(String name, int maxHP, int maxMana, int actions, int speed, int attackRange, int attackDamage) {
		super(name, maxHP, maxMana, actions, speed, attackRange, attackDamage);

	}
	public void useLeaderAbility(ArrayList<Champion> targets) {
		for(int i=0;i<targets.size();i++) {
			Stun stun = new Stun(2);
			try {
				stun.clone();
			} catch (CloneNotSupportedException e) {
				
				e.printStackTrace();
			}
			stun.apply(targets.get(i));
		}
	}
	public String toString() {
		return "AntiHero";
	}

	

}
