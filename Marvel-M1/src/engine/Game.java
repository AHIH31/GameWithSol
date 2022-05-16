package engine;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

import exceptions.ChampionDisarmedException;
import exceptions.GameActionException;
import exceptions.InvalidTargetException;
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
		 if(getFirstPlayer().getTeam().get(0).getCondition().equals(Condition.KNOCKEDOUT) && getFirstPlayer().getTeam().get(1).getCondition().equals(Condition.KNOCKEDOUT) && getFirstPlayer().getTeam().get(2).getCondition().equals(Condition.KNOCKEDOUT)) {
			 if(getSecondPlayer().getTeam().get(0).getCondition() != (Condition.KNOCKEDOUT) ||getSecondPlayer().getTeam().get(1).getCondition() != (Condition.KNOCKEDOUT) || getSecondPlayer().getTeam().get(2).getCondition() != (Condition.KNOCKEDOUT))
				 return secondPlayer;
			 else 
				 return firstPlayer;
		 }
		 return null;
	 }
	 
	 
	 
	 
	 
	 public void move(Direction d) throws UnallowedMovementException, NotEnoughResourcesException{
		 int x = getCurrentChampion().getLocation().x;
		 int y = getCurrentChampion().getLocation().y;
		 if((getCurrentChampion().getCurrentActionPoints()>=1)){
			 if(getCurrentChampion().getCondition() == Condition.ROOTED) {
				 throw new UnallowedMovementException();
			 }
			 else {
			 switch(d) {
			 	case UP:
			 		 if((x==4)||(board[x+1][y]!=null)) 
						 throw new UnallowedMovementException();
					 
					 else {
						 //update board
						 Point newPoint = new Point();
						 newPoint.setLocation(x+1,y);
						 getCurrentChampion().setLocation(newPoint);
						 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints()-1);
					 }
			 	break;
			 	case DOWN:
			 		 if(x==0||board[x-1][y]!=null) {
			 			 throw new UnallowedMovementException();		
			 }
			 	else {
			 		Point newPoint = new Point();
			 		newPoint.setLocation(x-1,y);
			 		getCurrentChampion().setLocation(newPoint);
				 	getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints()-1);
			 }
			 	break;
			 	case LEFT:
			 		if(y==0||board[x][y-1]!=null) 
			 		 throw new UnallowedMovementException();		
			 
			 	else {
			 		Point newPoint = new Point();
			 		newPoint.setLocation(x,y-1);
			 		getCurrentChampion().setLocation(newPoint);
			 		getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints()-1);
			 }
			 break;
			 	case RIGHT:
			 		if(y==4||board[x][y+1]!=null) 
			 		throw new UnallowedMovementException();
			 
			 	else {
			 		Point newPoint = new Point();
			 		newPoint.setLocation(x,y+1);
			 		getCurrentChampion().setLocation(newPoint);
			 		getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints()-1);
			 }
			 break;
			 }
		 }
		 }
		 else 
			 throw new NotEnoughResourcesException();
	 }
	 
	 
	 
	 
	 
	 public void attack(Direction d) throws InvalidTargetException, ChampionDisarmedException, NotEnoughResourcesException {
		 if(getCurrentChampion().getCurrentActionPoints()>=2) {
				//condition same team
			Player current = currentChampPlayer(getCurrentChampion());
		if(d == Direction.UP) {
			 Damageable target = targets(getCurrentChampion().getAttackRange(),Direction.UP);
			 if(target instanceof Cover) {
			 		target.setCurrentHP(target.getCurrentHP()-getCurrentChampion().getAttackDamage());
			 		getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
			 }
		 		else if(target instanceof Champion) {
		 			if(checkDisarm(getCurrentChampion())==true){
		 				 throw new ChampionDisarmedException();
		 			 }
		 			 else if(checkShield((Champion) target)==true) {
		 				 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 			 }
		 			else if(checkDodge((Champion) target)==true) {
		 	 			int prob = (int) Math.round(Math.random());
		 	 			if (prob == 1)
		 	 				 target.setCurrentHP((int)(target.getCurrentHP()-(getCurrentChampion().getAttackDamage()))); 
		 	 			getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 			}
		 			else if(target instanceof Hero) {
		 				 System.out.print(0);
		 				 if(getCurrentChampion() instanceof AntiHero) {
		 					target.setCurrentHP((target.getCurrentHP()-(int)(getCurrentChampion().getAttackDamage()*1.5)));
		 				 	getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 				 }
		 				 if(getCurrentChampion() instanceof Villain) {
		 					target.setCurrentHP((target.getCurrentHP()-(int)(getCurrentChampion().getAttackDamage()*1.5)));
		 				 	getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 			}
		 				 if(getCurrentChampion() instanceof Hero) {
							target.setCurrentHP(target.getCurrentHP()-getCurrentChampion().getAttackDamage());
							getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 				 }
		 			 }
		 			else if(target instanceof Villain) {
		 				 if(getCurrentChampion() instanceof AntiHero) {
		 					target.setCurrentHP((target.getCurrentHP()-(int)(getCurrentChampion().getAttackDamage()*1.5)));
		 				 	getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 				 }
		 				 if(getCurrentChampion() instanceof Hero) {
		 					 target.setCurrentHP((target.getCurrentHP()-(int)(getCurrentChampion().getAttackDamage()*1.5)));
		 				 	getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 			}
		 				 if(getCurrentChampion() instanceof Villain) {
							target.setCurrentHP(target.getCurrentHP()-getCurrentChampion().getAttackDamage());
							getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 				 }
		 			 }
		 			else if(target instanceof AntiHero) {
		 				 if(getCurrentChampion() instanceof Hero) {
			 				target.setCurrentHP((target.getCurrentHP()-(int)(getCurrentChampion().getAttackDamage()*1.5)));
		 				 	getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 			}
		 				 if(getCurrentChampion() instanceof Villain) {
			 				 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 					 target.setCurrentHP((target.getCurrentHP()-(int)(getCurrentChampion().getAttackDamage()*1.5)));
		 				 }
		 				 if(getCurrentChampion() instanceof AntiHero) {
							target.setCurrentHP(target.getCurrentHP()-getCurrentChampion().getAttackDamage());
			 			 	getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
			 			 }
			 			 }
		 			}
		}
		}
		if(d == Direction.RIGHT) {
			 Damageable target = targets(getCurrentChampion().getAttackRange(),Direction.RIGHT);
			 if(target instanceof Cover) {
			 		target.setCurrentHP(target.getCurrentHP()-getCurrentChampion().getAttackDamage());
			 		getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
			 }
		 		else if(target instanceof Champion) {
		 			if(checkDisarm(getCurrentChampion())==true){
		 				 throw new ChampionDisarmedException();
		 			 }
		 			 else if(checkShield((Champion) target)==true) {
		 				 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 			 }
		 			else if(checkDodge((Champion) target)==true) {
		 	 			int prob = (int) Math.round(Math.random());
		 	 			if (prob == 1)
		 	 				 target.setCurrentHP((int)(target.getCurrentHP()-(getCurrentChampion().getAttackDamage()))); 
		 	 			getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 			}
		 			else if(target instanceof Hero) {
		 				 System.out.print(0);
		 				 if(getCurrentChampion() instanceof AntiHero) {
		 					target.setCurrentHP((target.getCurrentHP()-(int)(getCurrentChampion().getAttackDamage()*1.5)));
		 				 	getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 				 }
		 				 if(getCurrentChampion() instanceof Villain) {
		 					target.setCurrentHP((target.getCurrentHP()-(int)(getCurrentChampion().getAttackDamage()*1.5)));
		 				 	getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 			}
		 				 if(getCurrentChampion() instanceof Hero) {
							target.setCurrentHP(target.getCurrentHP()-getCurrentChampion().getAttackDamage());
							getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 				 }
		 			 }
		 			else if(target instanceof Villain) {
		 				 if(getCurrentChampion() instanceof AntiHero) {
		 					target.setCurrentHP((target.getCurrentHP()-(int)(getCurrentChampion().getAttackDamage()*1.5)));
		 				 	getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 				 }
		 				 if(getCurrentChampion() instanceof Hero) {
		 					 target.setCurrentHP((target.getCurrentHP()-(int)(getCurrentChampion().getAttackDamage()*1.5)));
		 				 	getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 			}
		 				 if(getCurrentChampion() instanceof Villain) {
							target.setCurrentHP(target.getCurrentHP()-getCurrentChampion().getAttackDamage());
							getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 				 }
		 			 }
		 			else if(target instanceof AntiHero) {

		 				 if(getCurrentChampion() instanceof Hero) {
			 				target.setCurrentHP((target.getCurrentHP()-(int)(getCurrentChampion().getAttackDamage()*1.5)));
		 				 	getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 			}
		 				 if(getCurrentChampion() instanceof Villain) {
			 				 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 					 target.setCurrentHP((target.getCurrentHP()-(int)(getCurrentChampion().getAttackDamage()*1.5)));
		 				 }
		 				 if(getCurrentChampion() instanceof AntiHero) {
							target.setCurrentHP(target.getCurrentHP()-getCurrentChampion().getAttackDamage());
			 			 	getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
			 			 }
			 			 }
		 			}
			 	
		 			 
		 	
		}	
		if(d == Direction.DOWN) {
			 Damageable target = targets(getCurrentChampion().getAttackRange(),Direction.DOWN);
			 	if(target instanceof Cover) {
			 		target.setCurrentHP(target.getCurrentHP()-getCurrentChampion().getAttackDamage());
			 		getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
			 }
		 		else if(target instanceof Champion) {
		 			if(checkDisarm(getCurrentChampion())==true){
		 				 throw new ChampionDisarmedException();
		 			 }
		 			 else if(checkShield((Champion) target)==true) {
		 				 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 			 }
		 			else if(checkDodge((Champion) target)==true) {
		 	 			int prob = (int) Math.round(Math.random());
		 	 			if (prob == 1)
		 	 				 target.setCurrentHP((int)(target.getCurrentHP()-(getCurrentChampion().getAttackDamage()))); 
		 	 			getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 			}
		 			else if(target instanceof Hero) {
		 				 System.out.print(0);
		 				 if(getCurrentChampion() instanceof AntiHero) {
		 					target.setCurrentHP((target.getCurrentHP()-(int)(getCurrentChampion().getAttackDamage()*1.5)));
		 				 	getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 				 }
		 				 if(getCurrentChampion() instanceof Villain) {
		 					target.setCurrentHP((target.getCurrentHP()-(int)(getCurrentChampion().getAttackDamage()*1.5)));
		 				 	getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 			}
		 				 if(getCurrentChampion() instanceof Hero) {
							target.setCurrentHP(target.getCurrentHP()-getCurrentChampion().getAttackDamage());
							getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 				 }
		 			 }
		 			else if(target instanceof Villain) {
		 				 if(getCurrentChampion() instanceof AntiHero) {
		 					target.setCurrentHP((target.getCurrentHP()-(int)(getCurrentChampion().getAttackDamage()*1.5)));
		 				 	getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 				 }
		 				 if(getCurrentChampion() instanceof Hero) {
		 					 target.setCurrentHP((target.getCurrentHP()-(int)(getCurrentChampion().getAttackDamage()*1.5)));
		 				 	getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 			}
		 				 if(getCurrentChampion() instanceof Villain) {
							target.setCurrentHP(target.getCurrentHP()-getCurrentChampion().getAttackDamage());
							getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 				 }
		 			 }
		 			else if(target instanceof AntiHero) {
		 				 if(getCurrentChampion() instanceof Hero) {
			 				target.setCurrentHP((target.getCurrentHP()-(int)(getCurrentChampion().getAttackDamage()*1.5)));
		 				 	getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 			}
		 				 if(getCurrentChampion() instanceof Villain) {
			 				 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 					 target.setCurrentHP((target.getCurrentHP()-(int)(getCurrentChampion().getAttackDamage()*1.5)));
		 				 }
		 				 if(getCurrentChampion() instanceof AntiHero) {
							target.setCurrentHP(target.getCurrentHP()-getCurrentChampion().getAttackDamage());
			 			 	getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
			 			 }
			 			 }
		 			}
			 	
			 			
		}
		if(d == Direction.LEFT) {
			 Damageable target = targets(getCurrentChampion().getAttackRange(),Direction.LEFT);
			 if(target instanceof Cover) {
			 		target.setCurrentHP(target.getCurrentHP()-getCurrentChampion().getAttackDamage());
			 		getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
			 }
		 		else if(target instanceof Champion) {
		 			if(checkDisarm(getCurrentChampion())==true){
		 				 throw new ChampionDisarmedException();
		 			 }
		 			 else if(checkShield((Champion) target)==true) {
		 				 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 			 }
		 			else if(checkDodge((Champion) target)==true) {
		 	 			int prob = (int) Math.round(Math.random());
		 	 			if (prob == 1)
		 	 				 target.setCurrentHP((int)(target.getCurrentHP()-(getCurrentChampion().getAttackDamage()))); 
		 	 			getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 			}
		 			else if(target instanceof Hero) {
		 				 System.out.print(0);
		 				 if(getCurrentChampion() instanceof AntiHero) {
		 					target.setCurrentHP((target.getCurrentHP()-(int)(getCurrentChampion().getAttackDamage()*1.5)));
		 				 	getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 				 }
		 				 if(getCurrentChampion() instanceof Villain) {
		 					target.setCurrentHP((target.getCurrentHP()-(int)(getCurrentChampion().getAttackDamage()*1.5)));
		 				 	getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 			}
		 				 if(getCurrentChampion() instanceof Hero) {
							target.setCurrentHP(target.getCurrentHP()-getCurrentChampion().getAttackDamage());
							getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 				 }
		 			 }
		 			else if(target instanceof Villain) {
		 				 if(getCurrentChampion() instanceof AntiHero) {
		 					target.setCurrentHP((target.getCurrentHP()-(int)(getCurrentChampion().getAttackDamage()*1.5)));
		 				 	getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 				 }
		 				 if(getCurrentChampion() instanceof Hero) {
		 					 target.setCurrentHP((target.getCurrentHP()-(int)(getCurrentChampion().getAttackDamage()*1.5)));
		 				 	getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 			}
		 				 if(getCurrentChampion() instanceof Villain) {
							target.setCurrentHP(target.getCurrentHP()-getCurrentChampion().getAttackDamage());
							getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 				 }
		 			 }
		 			else if(target instanceof AntiHero) {
		 				 if(getCurrentChampion() instanceof Hero) {
			 				target.setCurrentHP((target.getCurrentHP()-(int)(getCurrentChampion().getAttackDamage()*1.5)));
		 				 	getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 			}
		 				 if(getCurrentChampion() instanceof Villain) {
			 				 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 					 target.setCurrentHP((target.getCurrentHP()-(int)(getCurrentChampion().getAttackDamage()*1.5)));
		 				 }
		 				 if(getCurrentChampion() instanceof AntiHero) {
							target.setCurrentHP(target.getCurrentHP()-getCurrentChampion().getAttackDamage());
			 			 	getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
			 			 }
			 			 }
		 			}
			 	
		}
		 }
	
	 

	 public void castAbility(Ability a) throws CloneNotSupportedException, InvalidTargetException {
		ArrayList<Damageable> targets = abilityRange(a.getCastRange());
		ArrayList<Damageable> targets2 = new ArrayList<Damageable>();
		ArrayList<Damageable> targets3 = new ArrayList<Damageable>();
		ArrayList<Damageable> targets4 = new ArrayList<Damageable>();
		boolean f = false;
		for(int i=0;i<targets.size();i++) {
			if(targets.get(i) instanceof Cover)
				targets2.add(targets.get(i));
			else {
			f= checkFriend2((Champion) targets.get(i), currentChampPlayer(getCurrentChampion()));
			if(f==false) {
				targets2.add(targets.get(i));
				targets4.add(targets.get(i));

				}	
			else targets3.add(targets.get(i));
		}}
		
		if(a.getCastArea() == AreaOfEffect.SURROUND ) {
			if(a instanceof DamagingAbility) {
				a.execute(targets2);
			getCurrentChampion().setMana(getCurrentChampion().getMana()- a.getManaCost());
			 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -a.getRequiredActionPoints()); 
			}
			 else if(a instanceof HealingAbility) {
				a.execute(targets3);
			getCurrentChampion().setMana(getCurrentChampion().getMana()- a.getManaCost());
			 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -a.getRequiredActionPoints());
			}
			else if(a instanceof CrowdControlAbility&&((CrowdControlAbility)a).getEffect().equals(EffectType.BUFF)) {
				a.execute(targets3);
				getCurrentChampion().setMana(getCurrentChampion().getMana()- a.getManaCost());
				getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -a.getRequiredActionPoints()); 
			}
			 else {
				a.execute(targets4);
			getCurrentChampion().setMana(getCurrentChampion().getMana()- a.getManaCost());
			 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -a.getRequiredActionPoints()); 
			}

			 getCurrentChampion().setMana(getCurrentChampion().getMana()- a.getManaCost());
			 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -a.getRequiredActionPoints()); 
		}
		else if(a.getCastArea() == AreaOfEffect.SELFTARGET ) {
			ArrayList<Damageable> x = new ArrayList<Damageable>(); 
			if(a instanceof HealingAbility) {
				x.add(getCurrentChampion());
				a.execute(x);
				getCurrentChampion().setMana(getCurrentChampion().getMana()- a.getManaCost());
				getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -a.getRequiredActionPoints());
		}
			else if(a instanceof CrowdControlAbility) {
				if(((CrowdControlAbility)a).getEffect().equals(EffectType.BUFF)){
				x.add(getCurrentChampion());
				a.execute(x);
				getCurrentChampion().setMana(getCurrentChampion().getMana()- a.getManaCost());
				getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -a.getRequiredActionPoints());
		}
		}
		}
		else if(a.getCastArea() == AreaOfEffect.TEAMTARGET ) {
			ArrayList<Damageable> x = new ArrayList<Damageable>(); 
			if((a instanceof HealingAbility)||((CrowdControlAbility)a).getEffect().equals(EffectType.BUFF)){
				for(int i = 0; i < 3; i++) {
					x.add(currentChampPlayer(getCurrentChampion()).getTeam().get(i));
			}
				a.execute(x);
				getCurrentChampion().setMana(getCurrentChampion().getMana()- a.getManaCost());
				getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -a.getRequiredActionPoints());
		}
		
			else if((a instanceof DamagingAbility)||((CrowdControlAbility)a).getEffect().equals(EffectType.DEBUFF))
				for(int i = 0; i < 3; i++) {
					x.add(enemyPlayer(getCurrentChampion()).getTeam().get(i));
			}
				a.execute(x);
				getCurrentChampion().setMana(getCurrentChampion().getMana()- a.getManaCost());
				getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -a.getRequiredActionPoints());
		}
				
			}
			
		
	 
	 
	
			
	 
	 
	 
	 public void castAbility(Ability a, Direction d) {
		 if(a.getCastArea() == AreaOfEffect.DIRECTIONAL ) {
			 switch(d) {
			 case UP:
				 
				 
				 break;
			 case DOWN:
				 
				 
				 break;
			 case RIGHT:
				 
				 
				 break;
			 case LEFT:
				 
				 
				 break;
			 
			 
			 
			 }
				 
		 
		 }
	}
	 
	 public void castAbility(Ability a, int x, int y) {
		 
	 }
	 
	 public void useLeaderAbility() {
		 
		 
	 }
	 
	 public void endTurn() {
		 
		 
	 }
	 
	 private void prepareChampionTurns() {
		 
		 
	 }
	 public Damageable targets(int range, Direction d) {
		 int x = getCurrentChampion().getLocation().y;
		 int y = getCurrentChampion().getLocation().x;
		 switch (d) {
		 case UP:
			 boolean flag = false;
			 int indexUp=1;
			 for(int i=1;i<=range;i++) {
				 if((board[y+i][x]!=null) && (y<4)) { 
					flag=true;
				 	break;
			 }
				 else 
					 indexUp++;
				 
			 }
				 if(flag==true)
					 return (Damageable) board[y+indexUp][x];
				 else
					 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -1);
			 break;
		 case DOWN: 
			 boolean flagDown = false;
			 int indexDown=1;
			 for(int i=1;i<=range;i++) {
				 if((board[y-i][x]!=null) && (y>0)) {
					flagDown=true;
				 	break;
			 }
				 else
					 indexDown++;
			 }
				 if(flagDown==true)
					 return (Damageable) board[y-indexDown][x];
				 else
					 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -1);
		 	break;
		 case RIGHT:
			 boolean flagRight = false;
			 int indexRight=1;
			 for(int i=1;i<=range;i++) {
		
				 if((board[y][x+i]!=null) && (x<4)) {
					flagRight=true;
				 	break;
			 }
				 else {
					 indexRight++;
				 }
			 }
				 if(flagRight==true)
					 return ((Damageable) board[y][x+indexRight]);
				 else
					 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -1); 
			 break;
		 case LEFT:
			 boolean flagLeft = false;
			 int indexLeft=1;
			 for(int i=1;i<=range;i++) {
				 if((board[y][x-i]!=null) && (y>0)) {
					flagLeft=true;
				 	break;
			 }
				 else
					 indexLeft++;
			 }
				 if(flagLeft==true)
					 return (Damageable) board[y][x-indexLeft];
				 else
					 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -1);
			 break;
		 }
		return null;
	 }
	 
	 
	 public Player enemyPlayer(Champion currentCh) {
		 for(int i=0;i<firstPlayer.getTeam().size();i++) {
			 if(firstPlayer.getTeam().get(i)==currentCh)
				 return secondPlayer;
			 else
				 return firstPlayer;
		 }
		 
		 return null;
	 }
	 public Player currentChampPlayer(Champion currentCh) {
		 for(int i=0;i<firstPlayer.getTeam().size();i++) {
			 if(firstPlayer.getTeam().get(i)==currentCh)
				 return firstPlayer;
			 else
				 return secondPlayer;
		 }
		 return null;
	}
	 public boolean checkFriend(Player c,Point location) {
		boolean f = false;
		 for(int i=0;i<c.getTeam().size();i++) {
			 if(c.getTeam().get(i).getLocation() == location) {
				 f= true;
			 break;
		 }
		 
		 else
			 f =false;
	 }
	 return f;
	 }
	 public boolean checkFriend2(Champion c, Player p) {
		 boolean f = false;
		 for(int i=0;i<p.getTeam().size();i++) {
			 if(p.getTeam().get(i).equals(c)) {
				 f= true;
			 break;
		 }
		 
		 else
			 f =false;
	 }
	 return f;
	 }

	 public boolean checkShock(Champion currentCh) {
		boolean f = false;
		 for(int i=0;i<currentCh.getAppliedEffects().size(); i++){
			 if(currentCh.getAppliedEffects().get(i).getName().equals("Shock")) {
				 f= true;
			 break;
		 }
		 
		 else
			 f =false;
	 }
	 return f;}
	 public boolean checkDisarm(Champion currentCh) {
		boolean f = false;
		 for(int i=0;i<currentCh.getAppliedEffects().size(); i++){
			 if(currentCh.getAppliedEffects().get(i).getName().equals("Disarm")) {
				 f= true;
			 break;
		 }
		 
		 else
			 f =false;
	 }
	 return f;
	 }
	 public boolean checkShield(Champion currentCh) {
		 boolean f = false;
		 for(int i=0;i<currentCh.getAppliedEffects().size(); i++){
			 if(currentCh.getAppliedEffects().get(i).getName().equals("Shield")){
				 f= true;
			 break;
		 }
		 
		 else
			 f =false;
	 }
	 return f;
	 }
	 public boolean checkDodge(Champion currentCh) {
		boolean f = false;
		 for(int i=0;i<currentCh.getAppliedEffects().size(); i++){
			 if(currentCh.getAppliedEffects().get(i).getName().equals("Dodge"))
			 {
				 f= true;
			 break;
		 }
		 
		 else
			 f =false;
	 }
	 return f;
	 }
	 public ArrayList<Damageable> abilityRange(int range) {
		 int y = getCurrentChampion().getLocation().x;
		 int x = getCurrentChampion().getLocation().y;
		 ArrayList<Damageable> targets = new ArrayList<Damageable>();
		 for (int i = 1; i <= range; i++) {
			for(int j = -range; j <= range; j++) {
				if(board[y+i][x+j] != null)
					targets.add((Damageable) board[y+i][x+j]);
				if(board[y-i][x+j] != null)
					targets.add((Damageable) board[y-i][x+j]);
			}
		 }
		 for(int i = 1; i <= range; i++) {
			 if(board[y][x+i] != null)
					targets.add((Damageable) board[y][x+i]);
			 if(board[y][x-i] != null)
					targets.add((Damageable) board[y][x-i]);
		 }
		 return targets;
	 }
}
//hello