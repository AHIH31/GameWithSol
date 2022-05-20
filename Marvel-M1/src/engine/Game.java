package engine;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import exceptions.AbilityUseException;
import exceptions.ChampionDisarmedException;
import exceptions.InvalidTargetException;
import exceptions.LeaderAbilityAlreadyUsedException;
import exceptions.LeaderNotCurrentException;
import exceptions.NotEnoughResourcesException;
import exceptions.UnallowedMovementException;
import model.abilities.Ability;
import model.abilities.AreaOfEffect;
import model.abilities.CrowdControlAbility;
import model.abilities.DamagingAbility;
import model.abilities.HealingAbility;
import model.effects.Disarm;
import model.effects.Dodge;
import model.effects.Effect;
import model.effects.EffectType;
import model.effects.Embrace;
import model.effects.PowerUp;
import model.effects.Root;
import model.effects.Shield;
import model.effects.Shock;
import model.effects.Silence;
import model.effects.SpeedUp;
import model.effects.Stun;
import model.world.AntiHero;
import model.world.Champion;
import model.world.Condition;
import model.world.Cover;
import model.world.Damageable;
import model.world.Direction;
import model.world.Hero;
import model.world.Villain;

public class Game {
	private static ArrayList<Champion> availableChampions;
	private static ArrayList<Ability> availableAbilities;
	private Player firstPlayer;
	private Player secondPlayer;
	private Object[][] board;
	private PriorityQueue turnOrder;
	private boolean firstLeaderAbilityUsed;
	private boolean secondLeaderAbilityUsed;
	private final static int BOARDWIDTH = 5;
	private final static int BOARDHEIGHT = 5;

	public Game(Player first, Player second) {
		firstPlayer = first;
		secondPlayer = second;
		availableChampions = new ArrayList<Champion>();
		availableAbilities = new ArrayList<Ability>();
		board = new Object[BOARDHEIGHT][BOARDWIDTH];
		turnOrder = new PriorityQueue(6);
		placeChampions();
		placeCovers();
		prepareChampionTurns();
	}

	public static void loadAbilities(String filePath) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		String line = br.readLine();
		while (line != null) {
			String[] content = line.split(",");
			Ability a = null;
			AreaOfEffect ar = null;
			switch (content[5]) {
			case "SINGLETARGET":
				ar = AreaOfEffect.SINGLETARGET;
				break;
			case "TEAMTARGET":
				ar = AreaOfEffect.TEAMTARGET;
				break;
			case "SURROUND":
				ar = AreaOfEffect.SURROUND;
				break;
			case "DIRECTIONAL":
				ar = AreaOfEffect.DIRECTIONAL;
				break;
			case "SELFTARGET":
				ar = AreaOfEffect.SELFTARGET;
				break;

			}
			Effect e = null;
			if (content[0].equals("CC")) {
				switch (content[7]) {
				case "Disarm":
					e = new Disarm(Integer.parseInt(content[8]));
					break;
				case "Dodge":
					e = new Dodge(Integer.parseInt(content[8]));
					break;
				case "Embrace":
					e = new Embrace(Integer.parseInt(content[8]));
					break;
				case "PowerUp":
					e = new PowerUp(Integer.parseInt(content[8]));
					break;
				case "Root":
					e = new Root(Integer.parseInt(content[8]));
					break;
				case "Shield":
					e = new Shield(Integer.parseInt(content[8]));
					break;
				case "Shock":
					e = new Shock(Integer.parseInt(content[8]));
					break;
				case "Silence":
					e = new Silence(Integer.parseInt(content[8]));
					break;
				case "SpeedUp":
					e = new SpeedUp(Integer.parseInt(content[8]));
					break;
				case "Stun":
					e = new Stun(Integer.parseInt(content[8]));
					break;
				}
			}
			switch (content[0]) {
			case "CC":
				a = new CrowdControlAbility(content[1], Integer.parseInt(content[2]), Integer.parseInt(content[4]),
						Integer.parseInt(content[3]), ar, Integer.parseInt(content[6]), e);
				break;
			case "DMG":
				a = new DamagingAbility(content[1], Integer.parseInt(content[2]), Integer.parseInt(content[4]),
						Integer.parseInt(content[3]), ar, Integer.parseInt(content[6]), Integer.parseInt(content[7]));
				break;
			case "HEL":
				a = new HealingAbility(content[1], Integer.parseInt(content[2]), Integer.parseInt(content[4]),
						Integer.parseInt(content[3]), ar, Integer.parseInt(content[6]), Integer.parseInt(content[7]));
				break;
			}
			availableAbilities.add(a);
			line = br.readLine();
		}
		br.close();
	}

	public static void loadChampions(String filePath) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		String line = br.readLine();
		while (line != null) {
			String[] content = line.split(",");
			Champion c = null;
			switch (content[0]) {
			case "A":
				c = new AntiHero(content[1], Integer.parseInt(content[2]), Integer.parseInt(content[3]),
						Integer.parseInt(content[4]), Integer.parseInt(content[5]), Integer.parseInt(content[6]),
						Integer.parseInt(content[7]));
				break;

			case "H":
				c = new Hero(content[1], Integer.parseInt(content[2]), Integer.parseInt(content[3]),
						Integer.parseInt(content[4]), Integer.parseInt(content[5]), Integer.parseInt(content[6]),
						Integer.parseInt(content[7]));
				break;
			case "V":
				c = new Villain(content[1], Integer.parseInt(content[2]), Integer.parseInt(content[3]),
						Integer.parseInt(content[4]), Integer.parseInt(content[5]), Integer.parseInt(content[6]),
						Integer.parseInt(content[7]));
				break;
			}

			c.getAbilities().add(findAbilityByName(content[8]));
			c.getAbilities().add(findAbilityByName(content[9]));
			c.getAbilities().add(findAbilityByName(content[10]));
			availableChampions.add(c);
			line = br.readLine();
		}
		br.close();
	}

	private static Ability findAbilityByName(String name) {
		for (Ability a : availableAbilities) {
			if (a.getName().equals(name))
				return a;
		}
		return null;
	}

	public void placeCovers() {
		int i = 0;
		while (i < 5) {
			int x = ((int) (Math.random() * (BOARDWIDTH - 2))) + 1;
			int y = (int) (Math.random() * BOARDHEIGHT);

			if (board[x][y] == null) {
				board[x][y] = new Cover(x, y);
				i++;
			}
		}

	}

	public void placeChampions() {
		int i = 1;
		for (Champion c : firstPlayer.getTeam()) {
			board[0][i] = c;
			c.setLocation(new Point(0, i));
			i++;
		}
		i = 1;
		for (Champion c : secondPlayer.getTeam()) {
			board[BOARDHEIGHT - 1][i] = c;
			c.setLocation(new Point(BOARDHEIGHT - 1, i));
			i++;
		}

	}

	public static ArrayList<Champion> getAvailableChampions() {
		return availableChampions;
	}

	public static ArrayList<Ability> getAvailableAbilities() {
		return availableAbilities;
	}

	public Player getFirstPlayer() {
		return firstPlayer;
	}

	public Player getSecondPlayer() {
		return secondPlayer;
	}

	public Object[][] getBoard() {
		return board;
	}

	public PriorityQueue getTurnOrder() {
		return turnOrder;
	}

	public boolean isFirstLeaderAbilityUsed() {
		return firstLeaderAbilityUsed;
	}

	public boolean isSecondLeaderAbilityUsed() {
		return secondLeaderAbilityUsed;
	}

	public static int getBoardwidth() {
		return BOARDWIDTH;
	}

	public static int getBoardheight() {
		return BOARDHEIGHT;
	}

	public Champion getCurrentChampion() {
		return (Champion) turnOrder.peekMin();
	}

	public Player checkGameOver() {
	if(firstPlayer.getTeam().isEmpty())
		return secondPlayer;
	else if(secondPlayer.getTeam().isEmpty())
		return firstPlayer;
	else return null;
	}

	public void move(Direction d) throws UnallowedMovementException, NotEnoughResourcesException {
		int x = getCurrentChampion().getLocation().x;
		int y = getCurrentChampion().getLocation().y;
		if ((getCurrentChampion().getCurrentActionPoints() >= 1)) {
			if (getCurrentChampion().getCondition() == Condition.ROOTED) {
				throw new UnallowedMovementException();
			} else {
				switch (d) {
				case UP:
					if ((x == 4) || (board[x + 1][y] != null))
						throw new UnallowedMovementException();

					else {
						Point newPoint = new Point();
						newPoint.setLocation(x + 1, y);
						getCurrentChampion().setLocation(newPoint);
						board[x + 1][y] = getCurrentChampion();
						board[x][y] = null;
						getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - 1);
					}
					break;
				case DOWN:
					if (x == 0 || board[x - 1][y] != null) {
						throw new UnallowedMovementException();
					} else {
						Point newPoint = new Point();
						newPoint.setLocation(x - 1, y);
						getCurrentChampion().setLocation(newPoint);
						board[x - 1][y] = getCurrentChampion();
						board[x][y] = null;
						getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - 1);
					}
					break;
				case LEFT:
					if (y == 0 || board[x][y - 1] != null)
						throw new UnallowedMovementException();

					else {
						Point newPoint = new Point();
						newPoint.setLocation(x, y - 1);
						getCurrentChampion().setLocation(newPoint);
						board[x][y - 1] = getCurrentChampion();
						board[x][y] = null;
						getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - 1);
					}
					break;
				case RIGHT:
					if (y == 4 || board[x][y + 1] != null)
						throw new UnallowedMovementException();

					else {
						Point newPoint = new Point();
						newPoint.setLocation(x, y + 1);
						getCurrentChampion().setLocation(newPoint);
						board[x][y + 1] = getCurrentChampion();
						board[x][y] = null;
						getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - 1);
					}
					break;
				}
			}
		} else
			throw new NotEnoughResourcesException();
	}

	public void attack(Direction d)
			throws InvalidTargetException, ChampionDisarmedException, NotEnoughResourcesException {
		
		Damageable target = targets(getCurrentChampion().getAttackRange(), d);
		if (getCurrentChampion().getCurrentActionPoints() >= 2) {			
				target = targets(getCurrentChampion().getAttackRange(), d);
				if (target == null) {
					getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - 2);
				}
				else {
					if (target instanceof Cover) {
						((Cover)target).setCurrentHP(((Cover)target).getCurrentHP() - getCurrentChampion().getAttackDamage());
						getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - 2);
						removeDead(((Cover)target));
					} 
					else if (target instanceof Champion) {
						if(checkFriend2((Champion) target,currentChampPlayer(getCurrentChampion())) == true) {
							 throw new InvalidTargetException();
						}
						else {
						if (checkDisarm(getCurrentChampion()) == true) {
							throw new ChampionDisarmedException();
						} 
						else if (checkShield((Champion) target) == true) {
							
							getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - 2);
						}
							
					 else { if (checkDodge((Champion) target) == true) {
							getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - 2);
						} 
							else {if (target instanceof Hero) {
							if (getCurrentChampion() instanceof AntiHero) {
								((Champion)target).setCurrentHP((((Champion)target).getCurrentHP() - (int) (getCurrentChampion().getAttackDamage() * 1.5)));
								getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - 2);
								removeDead(((Champion)target));
								
							}
							if (getCurrentChampion() instanceof Villain) {
								((Champion)target).setCurrentHP(
										(((Champion)target).getCurrentHP() - (int) (getCurrentChampion().getAttackDamage() * 1.5)));
								getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - 2);
								removeDead(((Champion)target));
							}
							if (getCurrentChampion() instanceof Hero) {
								((Champion)target).setCurrentHP(((Champion)target).getCurrentHP() - getCurrentChampion().getAttackDamage());
								getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - 2);
								removeDead(((Champion)target));
							}
							
						}
							else if (target instanceof Villain) {
							if (getCurrentChampion() instanceof AntiHero) {
								((Champion)target).setCurrentHP((((Champion)target).getCurrentHP() - (int) (getCurrentChampion().getAttackDamage() * 1.5)));
								getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - 2);
								removeDead(((Champion)target));
							}
							if (getCurrentChampion() instanceof Hero) {
								((Champion)target).setCurrentHP((((Champion)target).getCurrentHP() - (int) (getCurrentChampion().getAttackDamage() * 1.5)));
								getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - 2);
								removeDead(((Champion)target));
							}
							if (getCurrentChampion() instanceof Villain) {
								((Champion)target).setCurrentHP(((Champion)target).getCurrentHP() - getCurrentChampion().getAttackDamage());
								getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - 2);
								removeDead(((Champion)target));
							}
						
						}
							else if (target instanceof AntiHero) {
							if (getCurrentChampion() instanceof Hero) {
								((Champion)target).setCurrentHP((((Champion)target).getCurrentHP() - (int) (getCurrentChampion().getAttackDamage() * 1.5)));
								getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - 2);
								removeDead(((Champion)target));
							}
							if (getCurrentChampion() instanceof Villain) {
								getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - 2);
								((Champion)target).setCurrentHP((((Champion)target).getCurrentHP() - (int) (getCurrentChampion().getAttackDamage() * 1.5)));
								removeDead(((Champion)target));
							}
							if (getCurrentChampion() instanceof AntiHero) {
								((Champion)target).setCurrentHP(((Champion)target).getCurrentHP() - getCurrentChampion().getAttackDamage());
								getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - 2);
								removeDead(((Champion)target));
							}
					
						}

					}
					 }
						}
				}
					}
	
				}
		else
			throw new NotEnoughResourcesException();
	}


	public void castAbility(Ability a) throws CloneNotSupportedException, InvalidTargetException, AbilityUseException, NotEnoughResourcesException {
		ArrayList<Damageable> targets = abilityRange(a.getCastRange());
		ArrayList<Damageable> targetsEnemy = new ArrayList<Damageable>();
		ArrayList<Damageable> targetsFriend = new ArrayList<Damageable>();
		ArrayList<Damageable> targetsTeam = new ArrayList<Damageable>();
		ArrayList<Damageable> targetsEnemyNoSh = new ArrayList<Damageable>();
		ArrayList<Damageable> targetsCover = new ArrayList<Damageable>();
		
		
		if(getCurrentChampion().getMana() < a.getManaCost() || getCurrentChampion().getCurrentActionPoints()<a.getRequiredActionPoints() )
			throw new NotEnoughResourcesException();
		if(a.getCurrentCooldown()!=0)
			throw new AbilityUseException();
		if (checkStun(getCurrentChampion()) == true) {
			for (int i = 0; i < getCurrentChampion().getAppliedEffects().size(); i++) {
				if (getCurrentChampion().getAppliedEffects().get(i).getName().equals("Stun")) {
					if (getCurrentChampion().getAppliedEffects().get(i).getDuration() > 1)
						getCurrentChampion().getAppliedEffects().get(i).setDuration(getCurrentChampion().getAppliedEffects().get(i).getDuration() - 1);
					else
						getCurrentChampion().getAppliedEffects().remove(i);
				}
			}
		} else {
			if (checkSilence(getCurrentChampion())== true) {
				
				for (int i = 0; i < getCurrentChampion().getAppliedEffects().size(); i++) {
					if (getCurrentChampion().getAppliedEffects().get(i).getName().equals("Silence")) {
						if (getCurrentChampion().getAppliedEffects().get(i).getDuration() > 1)
							getCurrentChampion().getAppliedEffects().get(i).setDuration(getCurrentChampion().getAppliedEffects().get(i).getDuration() - 1);
						else
							getCurrentChampion().getAppliedEffects().remove(i);
					}
				}
				throw new AbilityUseException();
			} 
			else {
				for (int i = 0; i < targets.size(); i++) {
					
					if (targets.get(i) instanceof Cover) {
						targetsCover.add(targets.get(i));
					}
					else {
						if (checkFriend2((Champion) targets.get(i), currentChampPlayer(getCurrentChampion())) == false) {
							targetsEnemy.add(targets.get(i));
						} 
						else
							targetsFriend.add(targets.get(i));
					}
				}

				if (a.getCastArea() == AreaOfEffect.SURROUND) {
					if (a instanceof DamagingAbility) {
						for (int i = 0; i < targetsEnemy.size(); i++) {
							if (checkShield((Champion) targetsEnemy.get(i)) == false)
								targetsEnemyNoSh.add(targetsEnemy.get(i));
						}
						a.execute(targetsEnemyNoSh);
						removeDeadAb(targetsEnemyNoSh);
						a.execute(targetsCover);
						removeDeadAb(targetsCover);
						getCurrentChampion().setMana(getCurrentChampion().getMana() - a.getManaCost());
						getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - a.getRequiredActionPoints());
					} else if (a instanceof HealingAbility) {
						a.execute(targetsFriend);
						getCurrentChampion().setMana(getCurrentChampion().getMana() - a.getManaCost());
						getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - a.getRequiredActionPoints());
					} else if (a instanceof CrowdControlAbility && ((CrowdControlAbility) a).getEffect().getType().equals(EffectType.BUFF)) {
						a.execute(targetsFriend);
						getCurrentChampion().setMana(getCurrentChampion().getMana() - a.getManaCost());
						getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - a.getRequiredActionPoints());
					} else if(a instanceof CrowdControlAbility && ((CrowdControlAbility) a).getEffect().getType().equals(EffectType.DEBUFF)){
						a.execute(targetsEnemy);
						removeDeadAb(targetsEnemy);
						getCurrentChampion().setMana(getCurrentChampion().getMana() - a.getManaCost());
						getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - a.getRequiredActionPoints());
					}

				} 
				else if (a.getCastArea() == AreaOfEffect.SELFTARGET) {
					ArrayList<Damageable> x = new ArrayList<Damageable>();

					if (a instanceof HealingAbility) {
						x.add(getCurrentChampion());
						a.execute(x);
						removeDeadAb(x);
						getCurrentChampion().setMana(getCurrentChampion().getMana() - a.getManaCost());
						getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - a.getRequiredActionPoints());
					}

					else if (a instanceof CrowdControlAbility) {
						if (((CrowdControlAbility) a).getEffect().getType().equals(EffectType.BUFF)) {
							x.add(getCurrentChampion());
							a.execute(x);
							removeDeadAb(x);
							getCurrentChampion().setMana(getCurrentChampion().getMana() - a.getManaCost());
							getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - a.getRequiredActionPoints());
						}
					} else
						throw new AbilityUseException();
				} 
				else if (a.getCastArea() == AreaOfEffect.TEAMTARGET) {				

					if ((a instanceof HealingAbility) || (a instanceof CrowdControlAbility&&((CrowdControlAbility) a).getEffect().getType() == EffectType.BUFF)) {
						int xChamp = getCurrentChampion().getLocation().y;
						int yChamp = getCurrentChampion().getLocation().x;
						for (int i = 0; i < currentChampPlayer(getCurrentChampion()).getTeam().size(); i++) {
							int x1 = currentChampPlayer(getCurrentChampion()).getTeam().get(i).getLocation().y;
							int y1 = currentChampPlayer(getCurrentChampion()).getTeam().get(i).getLocation().x;
							int distance = Math.abs(yChamp - y1) + Math.abs(xChamp - x1);
							if(distance<=a.getCastRange())
								targetsTeam.add(currentChampPlayer(getCurrentChampion()).getTeam().get(i));
						}
						a.execute(targetsTeam);
						getCurrentChampion().setMana(getCurrentChampion().getMana() - a.getManaCost());
						getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - a.getRequiredActionPoints());
					}

					else if (a instanceof CrowdControlAbility && ((CrowdControlAbility) a).getEffect().getType().equals(EffectType.DEBUFF)) {
						int xChamp = getCurrentChampion().getLocation().y;
						int yChamp = getCurrentChampion().getLocation().x;
						for (int i = 0; i < 3; i++) {
							int x1 =  enemyPlayer(getCurrentChampion()).getTeam().get(i).getLocation().y;
							int y1 =  enemyPlayer(getCurrentChampion()).getTeam().get(i).getLocation().x;
							int distance = Math.abs(yChamp - y1) + Math.abs(xChamp - x1);
							if(distance<=a.getCastRange())
								targetsTeam.add(enemyPlayer(getCurrentChampion()).getTeam().get(i));
						}
						a.execute(targetsTeam);
						removeDeadAb(targetsTeam);
						getCurrentChampion().setMana(getCurrentChampion().getMana() - a.getManaCost());
						getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - a.getRequiredActionPoints());
					} 
					else if (a instanceof DamagingAbility) {
						int xChamp = getCurrentChampion().getLocation().y;
						int yChamp = getCurrentChampion().getLocation().x;
						for (int i = 0; i < 3; i++) {
							int x1 = enemyPlayer(getCurrentChampion()).getTeam().get(i).getLocation().y;
							int y1 = enemyPlayer(getCurrentChampion()).getTeam().get(i).getLocation().x;
							int distance = Math.abs(yChamp - y1) + Math.abs(xChamp - x1);
							if(distance<=a.getCastRange())
								targetsTeam.add(enemyPlayer(getCurrentChampion()).getTeam().get(i));
						}
						for (int i = 0; i < targetsTeam.size(); i++) {
							if(checkShield((Champion)targetsTeam.get(i)) == false) 
								targetsEnemyNoSh.add(targetsTeam.get(i));
						}
						a.execute(targetsEnemyNoSh);
						removeDeadAb(targetsEnemyNoSh);
						getCurrentChampion().setMana(getCurrentChampion().getMana() - a.getManaCost());
						getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - a.getRequiredActionPoints());
					}
				}
			}
		}

	}


	public void castAbility(Ability a, Direction d) throws CloneNotSupportedException, AbilityUseException, NotEnoughResourcesException {
		ArrayList<Damageable> targets = targetsCast(getCurrentChampion().getAttackRange(), d);
		ArrayList<Damageable> targetsCover = new ArrayList<Damageable>();
		ArrayList<Damageable> targetsEnemy = new ArrayList<Damageable>();
		ArrayList<Damageable> targetsFriend = new ArrayList<Damageable>();
		ArrayList<Damageable> targetsEnemyNoSh = new ArrayList<Damageable>();
		ArrayList<Damageable> targetsEnemyWSh = new ArrayList<Damageable>();

		if(getCurrentChampion().getMana() < a.getManaCost() || getCurrentChampion().getCurrentActionPoints()<a.getRequiredActionPoints() )
			throw new NotEnoughResourcesException();

		if (a.getCastArea() == AreaOfEffect.DIRECTIONAL) {
			if (checkStun(getCurrentChampion()) == true) {
				for (int i = 0; i < getCurrentChampion().getAppliedEffects().size(); i++) {
					if (getCurrentChampion().getAppliedEffects().get(i).getName().equals("Stun")) {
						if (getCurrentChampion().getAppliedEffects().get(i).getDuration() > 1)
							getCurrentChampion().getAppliedEffects().get(i).setDuration(getCurrentChampion().getAppliedEffects().get(i).getDuration() - 1);
						else
							getCurrentChampion().getAppliedEffects().remove(i);
					}
				}
			} else {
				if (checkSilence(getCurrentChampion())== true) {
					
					for (int i = 0; i < getCurrentChampion().getAppliedEffects().size(); i++) {
						if (getCurrentChampion().getAppliedEffects().get(i).getName().equals("Silence")) {
							if (getCurrentChampion().getAppliedEffects().get(i).getDuration() > 1)
								getCurrentChampion().getAppliedEffects().get(i).setDuration(getCurrentChampion().getAppliedEffects().get(i).getDuration() - 1);
							else
								getCurrentChampion().getAppliedEffects().remove(i);
						}
					}
					throw new AbilityUseException();
				} 
				else {
					
					for (int i = 0; i < targets.size(); i++) {
						if  ( targets.get(i) !=null && targets.get(i) instanceof Cover) {
							targetsCover.add(targets.get(i));
							
						}
						else if (checkFriend2((Champion) targets.get(i),currentChampPlayer(getCurrentChampion())) == true) {
							targetsFriend.add(targets.get(i));
							}
						else if (checkFriend2((Champion) targets.get(i),currentChampPlayer(getCurrentChampion())) == false) {
							targetsEnemy.add(targets.get(i));
							}
					}
					
					if (a instanceof HealingAbility) {
						a.execute(targetsFriend);
						getCurrentChampion().setMana(getCurrentChampion().getMana() - a.getManaCost());
						getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - a.getRequiredActionPoints());
					} 
					
					else if (a instanceof DamagingAbility) {
						for (int i = 0; i < targetsEnemy.size(); i++) {
							if(checkShield((Champion)targetsEnemy.get(i))==true )
								targetsEnemyWSh.add(targetsEnemy.get(i));
							else
								targetsEnemyNoSh.add(targetsEnemy.get(i));
						}
						a.execute(targetsEnemyNoSh);
						removeDeadAb(targetsEnemyNoSh);
						a.execute(targetsCover);
						removeDeadAb(targetsCover);
						getCurrentChampion().setMana(getCurrentChampion().getMana() - a.getManaCost());
						getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - a.getRequiredActionPoints());
					} else if (a instanceof CrowdControlAbility) {
						if (((CrowdControlAbility) a).getEffect().getType().equals(EffectType.DEBUFF)) {
							a.execute(targetsEnemy);
							removeDeadAb(targetsEnemy);
							getCurrentChampion().setMana(getCurrentChampion().getMana() - a.getManaCost());
							getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - a.getRequiredActionPoints());
						} else {
							a.execute(targetsFriend);
							getCurrentChampion().setMana(getCurrentChampion().getMana() - a.getManaCost());
							getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - a.getRequiredActionPoints());
						}
					}

				}
			}
		}
	}

	public void castAbility(Ability a, int x, int y)throws AbilityUseException, CloneNotSupportedException, InvalidTargetException, NotEnoughResourcesException {
		int xChamp = getCurrentChampion().getLocation().y;
		int yChamp = getCurrentChampion().getLocation().x;
		int distance = Math.abs(yChamp - x) + Math.abs(xChamp - y);
		ArrayList<Damageable> targets = new ArrayList<Damageable>();
		if(getCurrentChampion().getMana() < a.getManaCost() || getCurrentChampion().getCurrentActionPoints()<a.getRequiredActionPoints() )
			throw new NotEnoughResourcesException();
		if(a.getCurrentCooldown()!=0)
			throw new AbilityUseException();
		if (checkStun(getCurrentChampion()) == true) {
			for (int i = 0; i < getCurrentChampion().getAppliedEffects().size(); i++) {
				if (getCurrentChampion().getAppliedEffects().get(i).getName().equals("Stun")) {
					if (getCurrentChampion().getAppliedEffects().get(i).getDuration() > 1)
						getCurrentChampion().getAppliedEffects().get(i).setDuration(getCurrentChampion().getAppliedEffects().get(i).getDuration() - 1);
					else
						getCurrentChampion().getAppliedEffects().remove(i);
				}
			}
		} 
		else {
			if (checkSilence(getCurrentChampion())== true) {
				for (int i = 0; i < getCurrentChampion().getAppliedEffects().size(); i++) {
					if (getCurrentChampion().getAppliedEffects().get(i).getName().equals("Silence")) {
						if (getCurrentChampion().getAppliedEffects().get(i).getDuration() > 1)
							getCurrentChampion().getAppliedEffects().get(i).setDuration(getCurrentChampion().getAppliedEffects().get(i).getDuration() - 1);
						else
							getCurrentChampion().getAppliedEffects().remove(i);
					}
				}
				throw new AbilityUseException();
			} 
		}
		
		if (a.getCastArea() == AreaOfEffect.SINGLETARGET) {
			if (board[x][y] != null) {
				if (distance > a.getCastRange()) {
					throw new AbilityUseException();
				} 
				else
					targets.add((Damageable) board[x][y]);

				if (a instanceof DamagingAbility && board[x][y] != getCurrentChampion()) {
					if (targets.get(0) instanceof Cover) {
						a.execute(targets);
						removeDeadAb(targets);
						getCurrentChampion().setMana(getCurrentChampion().getMana() - a.getManaCost());
						getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - a.getRequiredActionPoints());
					}

					else if (checkFriend2((Champion) targets.get(0),currentChampPlayer(getCurrentChampion())) == false) {
						if (checkShield((Champion) targets.get(0)) == false) {
							a.execute(targets);
							removeDeadAb(targets);
							getCurrentChampion().setMana(getCurrentChampion().getMana() - a.getManaCost());
							getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints()- a.getRequiredActionPoints());
						}
						else{
							getCurrentChampion().setMana(getCurrentChampion().getMana() - a.getManaCost());
							getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints()- a.getRequiredActionPoints());
						}
					}
				} 
				else if (a instanceof DamagingAbility && board[x][y] == getCurrentChampion()) {
					throw new InvalidTargetException();
				}
				
				else if (a instanceof CrowdControlAbility&&targets.get(0) instanceof Champion) {
					if (((CrowdControlAbility) a).getEffect().getType().equals(EffectType.DEBUFF) && (checkFriend2((Champion) targets.get(0),currentChampPlayer(getCurrentChampion())) == false)
							&& board[x][y] != getCurrentChampion()){
						a.execute(targets);
						removeDeadAb(targets);
						getCurrentChampion().setMana(getCurrentChampion().getMana() - a.getManaCost());
						getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - a.getRequiredActionPoints());
					} 
					else if(((CrowdControlAbility) a).getEffect().getType().equals(EffectType.DEBUFF)
							&& ((checkFriend2((Champion) targets.get(0),currentChampPlayer(getCurrentChampion())) == true)|| board[x][y] == getCurrentChampion())){
								throw new InvalidTargetException();
							}
					
					else if (((CrowdControlAbility) a).getEffect().getType().equals(EffectType.BUFF)
							&& (checkFriend2((Champion) targets.get(0),currentChampPlayer(getCurrentChampion())) == true)) {
						a.execute(targets);
						getCurrentChampion().setMana(getCurrentChampion().getMana() - a.getManaCost());
						getCurrentChampion().setCurrentActionPoints(
								getCurrentChampion().getCurrentActionPoints() - a.getRequiredActionPoints());
					}
					else if(((CrowdControlAbility) a).getEffect().getType().equals(EffectType.BUFF)
							&& (checkFriend2((Champion) targets.get(0),currentChampPlayer(getCurrentChampion())) == false)) {
						throw new InvalidTargetException();
					}
				}
				else if(a instanceof CrowdControlAbility && targets.get(0) instanceof Cover)
						throw new InvalidTargetException();
				else if (a instanceof HealingAbility && targets.get(0) instanceof Champion) {
					if (checkFriend2((Champion) targets.get(0),currentChampPlayer(getCurrentChampion())) == true) {
						a.execute(targets);
						getCurrentChampion().setMana(getCurrentChampion().getMana() - a.getManaCost());
						getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() - a.getRequiredActionPoints());
					} 
					else
						throw new InvalidTargetException();

				}
				else if (a instanceof HealingAbility && targets.get(0) instanceof Cover) {
					throw new InvalidTargetException();
				}
			}
			else {
				throw new InvalidTargetException();
		}
			}
		}


	public void useLeaderAbility() throws LeaderNotCurrentException, LeaderAbilityAlreadyUsedException {
		ArrayList<Champion> leaderTeam = new ArrayList<Champion>();
		if(getCurrentChampion() instanceof Hero) {
			if (currentChampPlayer(getCurrentChampion()) == firstPlayer) {
				if (firstPlayer.getLeader() != getCurrentChampion())
					throw new LeaderNotCurrentException();
			else if (isFirstLeaderAbilityUsed() == false) {
				for (int i = 0; i < currentChampPlayer(getCurrentChampion()).getTeam().size(); i++) {
						leaderTeam.add(firstPlayer.getTeam().get(i));
						}
				getCurrentChampion().useLeaderAbility(leaderTeam);
				firstLeaderAbilityUsed = true;
				} 
			else if (isFirstLeaderAbilityUsed() == true)
				throw new LeaderAbilityAlreadyUsedException();
		} 
		else if (currentChampPlayer(getCurrentChampion()) == secondPlayer) {
			if (secondPlayer.getLeader() != getCurrentChampion())
				throw new LeaderNotCurrentException();
			else if (isSecondLeaderAbilityUsed() == false) {
				for (int i = 0; i < currentChampPlayer(getCurrentChampion()).getTeam().size(); i++) {
						leaderTeam.add(secondPlayer.getTeam().get(i));
						}
				getCurrentChampion().useLeaderAbility(leaderTeam);
				secondLeaderAbilityUsed = true;
			} 
			else if (isSecondLeaderAbilityUsed() == true)
				throw new LeaderAbilityAlreadyUsedException();
			}
			}
		if(getCurrentChampion() instanceof Villain) {
			if (enemyPlayer(getCurrentChampion()) == firstPlayer) {
				if (firstPlayer.getLeader() == getCurrentChampion())
					throw new LeaderNotCurrentException();
				else if (isFirstLeaderAbilityUsed() == false) {
					for (int i = 0; i < currentChampPlayer(getCurrentChampion()).getTeam().size(); i++) {
							leaderTeam.add(firstPlayer.getTeam().get(i));
							}
					getCurrentChampion().useLeaderAbility(leaderTeam);
					firstLeaderAbilityUsed = true;
				} 
				else if (isFirstLeaderAbilityUsed() == true)
					throw new LeaderAbilityAlreadyUsedException();
			}
			else if (enemyPlayer(getCurrentChampion()) == secondPlayer) {
				if (secondPlayer.getLeader() == getCurrentChampion())
					throw new LeaderNotCurrentException();
				else if (isSecondLeaderAbilityUsed() == false) {
					for (int i = 0; i < currentChampPlayer(getCurrentChampion()).getTeam().size(); i++) {
						leaderTeam.add(secondPlayer.getTeam().get(i));
					}
					getCurrentChampion().useLeaderAbility(leaderTeam);
					secondLeaderAbilityUsed = true;
				} 
				else if (isSecondLeaderAbilityUsed() == true)
					throw new LeaderAbilityAlreadyUsedException();
				}
			}
		if(getCurrentChampion() instanceof AntiHero) {
			if (currentChampPlayer(getCurrentChampion()) == firstPlayer) {
				if (firstPlayer.getLeader() != getCurrentChampion())
					throw new LeaderNotCurrentException();
				else if ((isFirstLeaderAbilityUsed() == true) ) {
					throw new LeaderAbilityAlreadyUsedException();
				}
				else {
					for (int i = 0; i < currentChampPlayer(getCurrentChampion()).getTeam().size(); i++) {
						if(firstPlayer.getTeam().get(i) != firstPlayer.getLeader())
							leaderTeam.add(firstPlayer.getTeam().get(i));
						if(secondPlayer.getTeam().get(i) != secondPlayer.getLeader())
							leaderTeam.add(secondPlayer.getTeam().get(i));
							}	
					getCurrentChampion().useLeaderAbility(leaderTeam);
					firstLeaderAbilityUsed = true;
					}
				}
				else if (currentChampPlayer(getCurrentChampion()) == secondPlayer) {
					if (secondPlayer.getLeader() != getCurrentChampion())
						throw new LeaderNotCurrentException();
					else if ((isSecondLeaderAbilityUsed() == true) ) {
						throw new LeaderAbilityAlreadyUsedException();
					}
					else {
						for (int i = 0; i < currentChampPlayer(getCurrentChampion()).getTeam().size(); i++) {
							if(secondPlayer.getTeam().get(i) != secondPlayer.getLeader())
								leaderTeam.add(secondPlayer.getTeam().get(i));
							if(firstPlayer.getTeam().get(i) != firstPlayer.getLeader())
								leaderTeam.add(firstPlayer.getTeam().get(i));
								}	
						getCurrentChampion().useLeaderAbility(leaderTeam);
						secondLeaderAbilityUsed = true;
						}
					}
			} 
		}

	public void endTurn() {
		turnOrder.remove();
		if(turnOrder.isEmpty()) {
			prepareChampionTurns();
		}
		else {
			for (int i = 0; i < turnOrder.size(); i++) {
				if (getCurrentChampion().getCondition() == Condition.INACTIVE) {
					for (int j = 0; j < getCurrentChampion().getAbilities().size(); j++) {
							getCurrentChampion().getAbilities().get(j).setCurrentCooldown(getCurrentChampion().getAbilities().get(j).getCurrentCooldown()-1);
					}
					for (int k = 0; k < getCurrentChampion().getAppliedEffects().size(); k++) {
						getCurrentChampion().getAppliedEffects().get(k).setDuration(getCurrentChampion().getAppliedEffects().get(k).getDuration() - 1);
						if(getCurrentChampion().getAppliedEffects().get(k).getDuration() == 0) {
							getCurrentChampion().getAppliedEffects().get(k).remove(getCurrentChampion());
							getCurrentChampion().getAppliedEffects().remove(k);
							k--;
						}
					}
					turnOrder.remove();	
				}
				else
					break;
			}
			if (turnOrder.isEmpty()) {
				prepareChampionTurns();
				}
			for (int j = 0; j < getCurrentChampion().getAbilities().size(); j++) {
				getCurrentChampion().getAbilities().get(j).setCurrentCooldown(getCurrentChampion().getAbilities().get(j).getCurrentCooldown()-1);
		}
		for (int k = 0; k < getCurrentChampion().getAppliedEffects().size(); k++) {
			getCurrentChampion().getAppliedEffects().get(k).setDuration(getCurrentChampion().getAppliedEffects().get(k).getDuration() - 1);

			if(getCurrentChampion().getAppliedEffects().get(k).getDuration() == 0) {
				getCurrentChampion().getAppliedEffects().get(k).remove(getCurrentChampion());
				getCurrentChampion().getAppliedEffects().remove(k);
				k--;
			}
		
		}
			getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getMaxActionPointsPerTurn());
			
		}
	}

	private void prepareChampionTurns() {
		for (int i = 0; i < firstPlayer.getTeam().size(); i++) {
			if (firstPlayer.getTeam().get(i).getCondition() != Condition.KNOCKEDOUT)
				turnOrder.insert(firstPlayer.getTeam().get(i));
			else
				removeDead(firstPlayer.getTeam().get(i));
		}
		for (int i = 0; i < secondPlayer.getTeam().size(); i++) {
			if (secondPlayer.getTeam().get(i).getCondition() != Condition.KNOCKEDOUT)
				turnOrder.insert(secondPlayer.getTeam().get(i));
			else
				removeDead(secondPlayer.getTeam().get(i));
		}
	}

	public Damageable targets(int range, Direction d) {
		int x = getCurrentChampion().getLocation().y;
		int y = getCurrentChampion().getLocation().x;
		if (d == Direction.UP) {
			boolean flagUp = false;
			int indexUp= 1;
			for (int i = 1; i <= range; i++) {
				if ((y+ i <= 4) && (board[y+i][x] != null)) {
					flagUp = true;
					break;
				}
				else {
					indexUp++;
				}
			}
			if (flagUp == true) {
				return (Damageable) board[y + indexUp][x];
			} 
		}
		 else if (d == Direction.DOWN) {
			 boolean flagDown = false;
				int indexDown= 1;
				for (int i = 1; i <= range; i++) {
					if ((y-i >=0) && (board[y-i][x] != null)) {
						flagDown = true;
						break;
					} 
					else {
						indexDown++;
					}
				}
				if (flagDown == true) {
					return (Damageable) board[y-indexDown][x];
				} 
				}
		 else if (d == Direction.RIGHT) {
			boolean flagRight = false;
			int indexRight = 1;
			for (int i = 1; i <= range; i++) {
				if ((x + i <= 4) && (board[y][x + i] != null)) {
					flagRight = true;
					break;
				} else {
					indexRight++;
				}
			}
			if (flagRight == true)
				return ((Damageable) board[y][x + indexRight]);
		} 
		 else if (d == Direction.LEFT) {
			boolean flagLeft = false;
			int indexLeft = 1;
			for (int i = 1; i <= range ; i++) {
				
				if ((x - i >= 0) && (board[y][x - i] != null)) {
					flagLeft = true;
					break;
				} else
					indexLeft++;
			}
			if (flagLeft == true) {
				return (Damageable) board[y][x - indexLeft];
			}
		 }
		return null;
	}

	public ArrayList<Damageable> targetsCast(int range, Direction d) {
		int x = getCurrentChampion().getLocation().y;
		int y = getCurrentChampion().getLocation().x;
		ArrayList<Damageable> damg = new ArrayList<Damageable>();
		if (d == Direction.UP) {
			for (int i = 1; i <= range; i++) {
				if (y + i<=4 && (board[y + i][x] != null)) {
					damg.add((Damageable) board[y + i][x]);
				}
			}
		} 
		else if (d == Direction.DOWN) {
			for (int i = 1; i <= range; i++) {
				if (y - i>=0&&(board[y - i][x] != null)) {
					damg.add((Damageable) board[y - i][x]);
				}
			}
		} 
		else if (d == Direction.RIGHT) {
			for (int i = 1; i <= range; i++) {
				if (x +i<= 4&&(board[y][x + i] != null)) {
					damg.add((Damageable) board[y][x + i]);
				}
			}
		} 
		else if (d == Direction.LEFT) {
			for (int i = 1; i <= range; i++) {
				if (x - i>=0&&(board[y][x - i] != null)) {
					damg.add((Damageable) board[y][x - i]);
				}
			}
		}
		return damg;
	}

	public Player enemyPlayer(Champion currentCh) {
		for (int i = 0; i < firstPlayer.getTeam().size(); i++) {
			if (firstPlayer.getTeam().get(i) == currentCh)
				return secondPlayer;
		}
		return firstPlayer;
	}

	public Player currentChampPlayer(Champion currentCh) {
		for (int i = 0; i < firstPlayer.getTeam().size(); i++) {
			if (firstPlayer.getTeam().get(i) == currentCh)
				return firstPlayer;
		}
		return secondPlayer;
	}

	public boolean checkFriend2(Champion c, Player p) {
		boolean f = false;
		for (int i = 0; i < p.getTeam().size(); i++) {
			if (p.getTeam().get(i) == c) {
				f = true;
				break;
			}
			else
				f = false;
		}
		return f;
	}
	
	public boolean checkDisarm(Champion currentCh) {
		boolean f = false;
		for (int i = 0; i < currentCh.getAppliedEffects().size(); i++) {
			if (currentCh.getAppliedEffects().get(i).getName().equals("Disarm")) {
				f = true;
				currentCh.getAppliedEffects().get(i).remove(currentCh);
				break;
				}
			}
		return f;
	}
	public boolean checkSilence(Champion currentCh) {
		
		boolean f = false;
		for (int i = 0; i < currentCh.getAppliedEffects().size(); i++) {
			if (currentCh.getAppliedEffects().get(i) instanceof Silence) {
				f = true;
				break;
			}
		}
		return f;
	}
	
	public boolean checkStun(Champion currentCh) {
		boolean f = false;
		for (int i = 0; i < currentCh.getAppliedEffects().size(); i++) {
			if (currentCh.getAppliedEffects().get(i) instanceof Stun) {
				f = true;
				break;
			}
		}
		return f;
	}

	public boolean checkShield(Champion currentCh) {
		boolean f = false;
		for (int i = 0; i < currentCh.getAppliedEffects().size(); i++) {
			if (currentCh.getAppliedEffects().get(i).getName().equals("Shield")) {
				f = true;
				currentCh.getAppliedEffects().get(i).remove(currentCh);
				currentCh.getAppliedEffects().remove(i);
				break;
			}
		}
		return f;
	}

	public boolean checkDodge(Champion currentCh) {
		boolean f = false;
		int i;
		for (i = 0; i < currentCh.getAppliedEffects().size(); i++) {
			if (currentCh.getAppliedEffects().get(i).getName().equals("Dodge")) {
				f = true;
				break;
			}
		}
		if(f) {
			Random rand = new Random();
			if(rand.nextInt(2) == 0) {
				currentCh.getAppliedEffects().get(i).remove(currentCh);
				return true;}
			else 
				return false;
		}
		return f;
	}

	public ArrayList<Damageable> abilityRange(int range) {
		int y = getCurrentChampion().getLocation().x;
		int x = getCurrentChampion().getLocation().y;
		ArrayList<Damageable> targets = new ArrayList<Damageable>();
		if( y+1<=4  &&  board[y+1][x] != null)
			targets.add( (Damageable) board[y+1][x]);
		if( x+1<=4  &&  board[y][x+1] != null)
			targets.add( (Damageable) board[y][x+1]);
		if( y+1<=4 && x+1<=4 &&  board[y+1][x+1] != null)
			targets.add( (Damageable) board[y+1][x+1]);
		if( y-1>=0  &&  board[y-1][x] != null)
			targets.add( (Damageable) board[y-1][x]);
		if( x-1>=0  &&  board[y][x-1] != null)
			targets.add( (Damageable) board[y][x-1]);
		if( y-1>-1 && x-1>-1  &&  board[y-1][x-1] != null)
			targets.add( (Damageable) board[y-1][x-1]);
		if( y+1<=4 && x-1>=0 &&  board[y+1][x-1] != null)
			targets.add( (Damageable) board[y+1][x-1]);
		if( y-1>=0 && x+1<=4   &&  board[y-1][x+1] != null)
			targets.add( (Damageable) board[y-1][x+1]);
		return targets;
	}
	
	public void removeDead(Damageable d) {
		if(d instanceof Cover) {
			int x = d.getLocation().y;
			int y = d.getLocation().x;
			if(d.getCurrentHP() == 0) {
				board[y][x] = null;
			}
		}
		else if(d instanceof Champion) {
			if(d.getCurrentHP() == 0 && !turnOrder.isEmpty()){
				int x = d.getLocation().y;
				int y = d.getLocation().x;
				board[y][x] = null;
				((Champion) d).setCondition(Condition.KNOCKEDOUT);
			if(currentChampPlayer((Champion) d) == firstPlayer)
				firstPlayer.getTeam().remove(d);
			else
				secondPlayer.getTeam().remove(d);
			
			PriorityQueue tmp = new PriorityQueue(turnOrder.size());
			while(!turnOrder.isEmpty()){
				if(turnOrder.peekMin() == d ) {
					turnOrder.remove();break;
				}
				else 
					tmp.insert(turnOrder.remove());
				}
			while(!tmp.isEmpty()){
				turnOrder.insert(tmp.remove());
				}
			}
			}
		}
	public void removeDeadAb(ArrayList<Damageable> d) {
		for(int i =0; i<d.size();i++) {
		int x = d.get(i).getLocation().y;
		int y = d.get(i).getLocation().x;
		if(d.get(i) instanceof Cover) {
			if(d.get(i).getCurrentHP()== 0) 
				board[y][x] = null;
		}
		else if(d.get(i) instanceof Champion) {
			if(d.get(i).getCurrentHP()== 0) {
			board[y][x] = null;
			((Champion) d.get(i)).setCondition(Condition.KNOCKEDOUT);
			if(currentChampPlayer((Champion) d.get(i)) == firstPlayer)
				firstPlayer.getTeam().remove(d.get(i));
			else
				secondPlayer.getTeam().remove(d.get(i));
			PriorityQueue tmp = new PriorityQueue(turnOrder.size());
			for(int j  = 0; j<turnOrder.size();j++) {
				if(turnOrder.peekMin() == d.get(i))
					turnOrder.remove();
				else {
					tmp.insert(turnOrder.remove());
				}	
				}
			while(!tmp.isEmpty()){
				turnOrder.insert(tmp.remove());
			}
			}
			}
		}
		}
}