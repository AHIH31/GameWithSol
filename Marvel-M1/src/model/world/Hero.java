package model.world;

import java.util.ArrayList;

import model.effects.EffectType;
import model.effects.Embrace;

public class Hero extends Champion {

	public Hero(String name, int maxHP, int maxMana, int actions, int speed, int attackRange, int attackDamage) {
		super(name, maxHP, maxMana, actions, speed, attackRange, attackDamage);

	}




	public void useLeaderAbility(ArrayList<Champion> targets) {
		for(int i=0;i<targets.size();i++) {
			for(int j=0;j<targets.get(i).getAppliedEffects().size();j++) {
				if(targets.get(i).getAppliedEffects().get(j).getType().equals(EffectType.DEBUFF))
					targets.remove(targets.get(i).getAppliedEffects().get(j));
			}
			
			Embrace embrace = new Embrace(2);
			try {
				embrace.clone();
			} catch (CloneNotSupportedException e) {
				
				e.printStackTrace();
			}
			embrace.apply(targets.get(i));
		}
		
	}

	
}
