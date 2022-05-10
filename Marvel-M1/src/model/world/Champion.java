package model.world;

import java.awt.Point;
import java.util.ArrayList;


import model.abilities.Ability;
import model.effects.Effect;
import model.effects.Embrace;
import model.effects.Stun;

abstract public class Champion implements Comparable{
	private String name;
	private int maxHP;
	private int currentHP;
	private int mana;
	private int maxActionPointsPerTurn;
	private int currentActionPoints;
	private int attackRange;
	private int attackDamage;
	private int speed;
	private ArrayList<Ability> abilities;
	private ArrayList<Effect> appliedEffects;
	private Condition condition;
	private Point location;
	

	public Champion(String name, int maxHP, int mana, int actions, int speed, int attackRange, int attackDamage) {
		this.name = name;
		this.maxHP = maxHP;
		this.mana = mana;
		this.currentHP = this.maxHP;
		this.maxActionPointsPerTurn = actions;
		this.speed = speed;
		this.attackRange = attackRange;
		this.attackDamage = attackDamage;
		this.condition = Condition.ACTIVE;
		this.abilities = new ArrayList<Ability>();
		this.appliedEffects = new ArrayList<Effect>();
		this.currentActionPoints=maxActionPointsPerTurn;
	}

	public int getMaxHP() {
		return maxHP;
	}

	public String getName() {
		return name;
	}

	public void setCurrentHP(int hp) {

		if (hp < 0) {
			currentHP = 0;
			
		} 
		else if (hp > maxHP)
			currentHP = maxHP;
		else
			currentHP = hp;

	}

	
	public int getCurrentHP() {

		return currentHP;
	}

	public ArrayList<Effect> getAppliedEffects() {
		return appliedEffects;
	}

	public int getMana() {
		return mana;
	}

	public void setMana(int mana) {
		this.mana = mana;
	}

	public int getAttackDamage() {
		return attackDamage;
	}

	public void setAttackDamage(int attackDamage) {
		this.attackDamage = attackDamage;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int currentSpeed) {
		if (currentSpeed < 0)
			this.speed = 0;
		else
			this.speed = currentSpeed;
	}

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}

	public Point getLocation() {
		return location;
	}

	public void setLocation(Point currentLocation) {
		this.location = currentLocation;
	}

	public int getAttackRange() {
		return attackRange;
	}

	public ArrayList<Ability> getAbilities() {
		return abilities;
	}

	public int getCurrentActionPoints() {
		return currentActionPoints;
	}

	public void setCurrentActionPoints(int currentActionPoints) {
		if(currentActionPoints>maxActionPointsPerTurn)
			currentActionPoints=maxActionPointsPerTurn;
		else 
			if(currentActionPoints<0)
			currentActionPoints=0;
		this.currentActionPoints = currentActionPoints;
	}

	public int getMaxActionPointsPerTurn() {
		return maxActionPointsPerTurn;
	}

	public void setMaxActionPointsPerTurn(int maxActionPointsPerTurn) {
		this.maxActionPointsPerTurn = maxActionPointsPerTurn;
	}
	public int compareTo(Object o) {
		Champion other = (Champion) o;
		if(this.getSpeed()>other.getSpeed())
			return 1;
		if(other.getSpeed()>this.getSpeed())
			return -1;
		else
			return 0;
	}

	public ArrayList<Champion> helper(int pr, Champion other, ArrayList<Champion>targets){
		switch(pr) {
		case 1:
			targets.add(this);
		case -1:
			targets.add(other);
		case 0:
			if(this.getName().compareTo(other.getName())==-1)
				targets.add(this);
			else
				targets.add(other);
			
		}
		return targets;
	}
	
	public void useLeaderAbility(ArrayList<Champion> targets) {
		for(int i=0;i<targets.size();i++) {
			helper(this.compareTo(targets.get(i)),targets.get(i),targets);
		}
		for(int i=0;i<targets.size();i++) {
			if(targets.get(i) instanceof Hero) {
				targets.remove(targets.get(i).getAppliedEffects());
				Embrace embrace = new Embrace(2);
				embrace.apply(targets.get(i));
				
			}
			if(targets.get(i) instanceof Villain) {
				if((targets.get(i).getCurrentHP())/(targets.get(i).getMaxHP()) < 0.3)
					targets.get(i).setCondition(Condition.KNOCKEDOUT);
					
			}
			if(targets.get(i) instanceof AntiHero) {
				Stun stun = new Stun(2);
				stun.apply(targets.get(i));
			}
		}
	}
	

}
