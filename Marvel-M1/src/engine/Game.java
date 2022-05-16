package engine;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


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
						 Point newPoint = new Point();
						 newPoint.setLocation(x+1,y);
						 getCurrentChampion().setLocation(newPoint);
						 board[x+1][y] = getCurrentChampion();
						 board[x][y] = null;
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
			 		 board[x-1][y] = getCurrentChampion();
					 board[x][y] = null;
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
			 		 board[x][y-1] = getCurrentChampion();
					 board[x][y] = null;
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
			 		 board[x][y+1] = getCurrentChampion();
					 board[x][y] = null;
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
			//Player current = currentChampPlayer(getCurrentChampion());
		if(d == Direction.UP) {
			 Damageable target = targets(getCurrentChampion().getAttackRange(),Direction.UP);
			 if(target==null) {
				 	System.out.print(1);
			 		getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);

			 }
			 else {
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
		 	 			if (prob == 1) {
		 	 				 target.setCurrentHP((int)(target.getCurrentHP()-(getCurrentChampion().getAttackDamage())));
		 	 				getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 	 			}
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
			 if(target==null) {
			 		getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);

			 }
			 else {
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
		 	 			if (prob == 1) {
		 	 				 target.setCurrentHP((int)(target.getCurrentHP()-(getCurrentChampion().getAttackDamage())));
		 	 				getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 	 			}
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
		if(d == Direction.DOWN) {
			 Damageable target = targets(getCurrentChampion().getAttackRange(),Direction.DOWN);
			 System.out.print(target);
			 if(target==null) {
			 		getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
			 }
			 else {
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
		 	 			if (prob == 1) {
		 	 				 target.setCurrentHP((int)(target.getCurrentHP()-(getCurrentChampion().getAttackDamage())));
		 	 				getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 	 			}
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
		if(d == Direction.LEFT) {
			 Damageable target = targets(getCurrentChampion().getAttackRange(),Direction.LEFT);
			 if(target==null) {
			 		getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);

			 }
			 else {
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
		 	 			if (prob == 1) {
		 	 				 target.setCurrentHP((int)(target.getCurrentHP()-(getCurrentChampion().getAttackDamage())));
		 	 				getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 	 			}
		 	 			getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -2);
		 			}
		 			else if(target instanceof Hero) {		 				 if(getCurrentChampion() instanceof AntiHero) {
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
		 				 System.out.print(0);

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
		 }
		 else
			 throw new NotEnoughResourcesException();
	 }

	 public void castAbility(Ability a) throws CloneNotSupportedException, InvalidTargetException, AbilityUseException {
		ArrayList<Damageable> targets = abilityRange(a.getCastRange());
		ArrayList<Damageable> targets2 = new ArrayList<Damageable>();
		ArrayList<Damageable> targets3 = new ArrayList<Damageable>();
		ArrayList<Damageable> targets4 = new ArrayList<Damageable>();
		ArrayList<Damageable> targetsEnemySh = new ArrayList<Damageable>();
		ArrayList<Damageable> targetsEnemyhWSH = new ArrayList<Damageable>();
		ArrayList<Damageable> targetsCover = new ArrayList<Damageable>();

		boolean f = false;
		boolean f1 = false;
		boolean f6=false;
		for(int i=0; i<getCurrentChampion().getAppliedEffects().size();i++){
			if(getCurrentChampion().getAppliedEffects().get(i).getName().equals("Stun")) {
				f6=true;
				break;
			}	
		}
		if(f6==true) {
			for(int i=0; i<getCurrentChampion().getAppliedEffects().size();i++){
				if(getCurrentChampion().getAppliedEffects().get(i).getName().equals("Stun")) {
					if(getCurrentChampion().getAppliedEffects().get(i).getDuration()>1)
						getCurrentChampion().getAppliedEffects().get(i).setDuration(getCurrentChampion().getAppliedEffects().get(i).getDuration()-1);
					else
						getCurrentChampion().getAppliedEffects().remove(i);
				}
			}
		}
		else {

		for(int i=0; i<getCurrentChampion().getAppliedEffects().size();i++){
			if(getCurrentChampion().getAppliedEffects().get(i).getName().equals("Silence")) {
				f1=true;
				break;
			}	
		}
		if(f1==true) {
			for(int i=0; i<getCurrentChampion().getAppliedEffects().size();i++){
				if(getCurrentChampion().getAppliedEffects().get(i).getName().equals("Silence")) {
					getCurrentChampion().getAppliedEffects().remove(i);
				}
			}
			throw new AbilityUseException();
		}
		else {
		for(int i=0;i<targets.size();i++) {
			if(targets.get(i) instanceof Cover) {
				targets2.add(targets.get(i));
				targetsCover.add(targets.get(i));
			}
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
				boolean f2 = false;
				for(int i=0;i<targets4.size();i++) {
					for(int j = 0; j<((Champion)targets4.get(i)).getAppliedEffects().size();j++) {
						if(((Champion)targets4.get(i)).getAppliedEffects().get(j).getName().equals("Shield")) {
							f2 = true;
							break;
						}
						else
							f2= false;
					}
					if(f2 == true)
						targetsEnemyhWSH.add(targets4.get(i));
					else 
						targetsEnemySh.add(targets4.get(i));		
				}
				for(int i=0;i<targetsEnemyhWSH.size();i++) {
					for(int j=0;j<((Champion)targets4.get(i)).getAppliedEffects().size();j++) {
					if(((Champion)targetsEnemyhWSH.get(i)).getAppliedEffects().get(j).getName().equals(("Shield"))) {
						((Champion)targetsEnemyhWSH.get(i)).getAppliedEffects().remove(j);
					}
					}
				}
				a.execute(targetsEnemySh);
				a.execute(targetsCover);
			getCurrentChampion().setMana(getCurrentChampion().getMana()- a.getManaCost());
			 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -a.getRequiredActionPoints()); 
			}
			 else if(a instanceof HealingAbility) {
				a.execute(targets3);
			getCurrentChampion().setMana(getCurrentChampion().getMana()- a.getManaCost());
			 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -a.getRequiredActionPoints());
			}
			else if(a instanceof CrowdControlAbility&&((CrowdControlAbility)a).getEffect().getType().equals(EffectType.BUFF)) {
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
				if(((CrowdControlAbility)a).getEffect().getType().equals(EffectType.BUFF)){
				x.add(getCurrentChampion());
				a.execute(x);
				getCurrentChampion().setMana(getCurrentChampion().getMana()- a.getManaCost());
				getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -a.getRequiredActionPoints());
		}
		}
			else
				throw new AbilityUseException();
		}
		else if(a.getCastArea() == AreaOfEffect.TEAMTARGET ) {
			ArrayList<Damageable> x = new ArrayList<Damageable>(); 
			if((a instanceof HealingAbility)||((CrowdControlAbility)a).getEffect().getType() == EffectType.BUFF){
				for(int i = 0; i < 3; i++) {
					x.add(currentChampPlayer(getCurrentChampion()).getTeam().get(i));
			}
				a.execute(x);
				getCurrentChampion().setMana(getCurrentChampion().getMana()- a.getManaCost());
				getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -a.getRequiredActionPoints());
		}
		
			else if(((CrowdControlAbility)a).getEffect().getType().equals(EffectType.DEBUFF)) {
				for(int i = 0; i < 3; i++) {
					x.add(enemyPlayer(getCurrentChampion()).getTeam().get(i));
			}
				a.execute(x);
				getCurrentChampion().setMana(getCurrentChampion().getMana()- a.getManaCost());
				getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -a.getRequiredActionPoints());
		}
			else if((a instanceof DamagingAbility)) {
				boolean f4 = false;
				for(int i = 0; i < 3; i++) {
					x.add(enemyPlayer(getCurrentChampion()).getTeam().get(i));
			}
				for(int i=0;i<x.size();i++) {
					for(int j = 0; j<((Champion)x.get(i)).getAppliedEffects().size();j++) {
						if(((Champion)x.get(i)).getAppliedEffects().get(j).getName().equals("Shield")) {
							f4 = true;
							break;
						}
						else
							f4= false;
					}
					if(f4 == true)
						targetsEnemyhWSH.add(x.get(i));
					else 
						targetsEnemySh.add(x.get(i));		
				}
				for(int i=0;i<targetsEnemyhWSH.size();i++) {
					for(int j=0;j<((Champion)x.get(i)).getAppliedEffects().size();j++) {
					if(((Champion)targetsEnemyhWSH.get(i)).getAppliedEffects().get(j).getName().equals(("Shield"))) {
						((Champion)targetsEnemyhWSH.get(i)).getAppliedEffects().remove(j);
					}
					}
				}
				a.execute(targetsEnemySh);
			}
			}
		}
		}	
		
	 }
		
	 
	 //healing team target
	
			
	 
	 
	 
	 public void castAbility(Ability a, Direction d) throws CloneNotSupportedException, AbilityUseException {
		 ArrayList<Damageable> targets = targetsCast(getCurrentChampion().getAttackRange(),d);
		 ArrayList<Damageable> targetsCover = new ArrayList<Damageable>();
		 ArrayList<Damageable> targetsEnemy = new ArrayList<Damageable>();
		 ArrayList<Damageable> targetsFriend = new ArrayList<Damageable>();
		 ArrayList<Damageable> targetsEnemySh = new ArrayList<Damageable>();
		 ArrayList<Damageable> targetsEnemyhWSH = new ArrayList<Damageable>();

		 if(a.getCastArea() == AreaOfEffect.DIRECTIONAL ) {
			 boolean f1 = false;
			 boolean f6=false;
				for(int i=0; i<getCurrentChampion().getAppliedEffects().size();i++){
					if(getCurrentChampion().getAppliedEffects().get(i).getName().equals("Stun")) {
						f6=true;
						break;
					}	
				}
				
				if(f6==true) {
					for(int i=0; i<getCurrentChampion().getAppliedEffects().size();i++){
						if(getCurrentChampion().getAppliedEffects().get(i).getName().equals("Stun")) {
							if(getCurrentChampion().getAppliedEffects().get(i).getDuration()>1)
								getCurrentChampion().getAppliedEffects().get(i).setDuration(getCurrentChampion().getAppliedEffects().get(i).getDuration()-1);
							else
								getCurrentChampion().getAppliedEffects().remove(i);
						}
					}
				}
				else {
				for(int i=0; i<getCurrentChampion().getAppliedEffects().size();i++){
					if(getCurrentChampion().getAppliedEffects().get(i).getName().equals("Silence")) {
						f1=true;
						break;
					}	
				}
				if(f1==true) {
					for(int i=0; i<getCurrentChampion().getAppliedEffects().size();i++){
						if(getCurrentChampion().getAppliedEffects().get(i).getName().equals("Silence")) {
							getCurrentChampion().getAppliedEffects().remove(i);
						}
					}
					throw new AbilityUseException();
				}
				else {
			 for(int i=0;i<targets.size();i++) {
					if(targets.get(i) instanceof Cover)
						targetsCover.add(targets.get(i));
					else if(checkFriend2((Champion)targets.get(i), currentChampPlayer(getCurrentChampion()))==true)
						targetsFriend.add(targets.get(i));
					else if(checkFriend2((Champion)targets.get(i), currentChampPlayer(getCurrentChampion()))==false)
						targetsEnemy.add(targets.get(i));	
				}
			if(a instanceof HealingAbility) {
				a.execute(targetsFriend);
				getCurrentChampion().setMana(getCurrentChampion().getMana()- a.getManaCost());
				getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -a.getRequiredActionPoints());
			}
			else if(a instanceof DamagingAbility) {
				boolean f = false;
				for(int i=0;i<targetsEnemy.size();i++) {
					for(int j = 0; j<((Champion)targetsEnemy.get(i)).getAppliedEffects().size();j++) {
						if(((Champion)targetsEnemy.get(i)).getAppliedEffects().get(j).getName().equals("Shield")) {
							f = true;
							break;
						}
						else
							f= false;
					}
					if(f == true)
						targetsEnemyhWSH.add(targetsEnemy.get(i));
					else 
						targetsEnemySh.add(targetsEnemy.get(i));		
				}
				for(int i=0;i<targetsEnemyhWSH.size();i++) {
					for(int j=0;j<((Champion)targetsEnemy.get(i)).getAppliedEffects().size();j++) {
					if(((Champion)targetsEnemyhWSH.get(i)).getAppliedEffects().get(j).getName().equals(("Shield"))) {
						((Champion)targetsEnemyhWSH.get(i)).getAppliedEffects().remove(j);
					}
					}
				}
				a.execute(targetsEnemySh);
				a.execute(targetsCover);
				getCurrentChampion().setMana(getCurrentChampion().getMana()- a.getManaCost());
				getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -a.getRequiredActionPoints());
			}
			else if(a instanceof CrowdControlAbility) {
				if(((CrowdControlAbility) a).getEffect().getType().equals(EffectType.DEBUFF)) {
					a.execute(targetsEnemy);
					getCurrentChampion().setMana(getCurrentChampion().getMana()- a.getManaCost());
					getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -a.getRequiredActionPoints());
				}
				else {
					a.execute(targetsFriend);
					getCurrentChampion().setMana(getCurrentChampion().getMana()- a.getManaCost());
					getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -a.getRequiredActionPoints());
				}	
			}
		 
		 }
		 }
	}
	 }
	 
	 public void castAbility(Ability a, int x, int y) throws AbilityUseException, CloneNotSupportedException, InvalidTargetException {
		 int xChamp= getCurrentChampion().getLocation().y;
		 int yChamp= getCurrentChampion().getLocation().x;
		 ArrayList<Damageable> targets = new ArrayList<Damageable>();
		 int distance = Math.abs(yChamp-x) + Math.abs(xChamp-y);
		 boolean f1 = false;
		 boolean f6=false;
		 if(a.getCastArea() == AreaOfEffect.SINGLETARGET) {
			 if(board[x][y]!=null) {
			for(int i=0; i<getCurrentChampion().getAppliedEffects().size();i++){
				if(getCurrentChampion().getAppliedEffects().get(i).getName().equals("Stun")) {
					f6=true;
					break;
				}	
			}
			if(f6==true) {
				for(int i=0; i<getCurrentChampion().getAppliedEffects().size();i++){
					if(getCurrentChampion().getAppliedEffects().get(i).getName().equals("Stun")) {
						if(getCurrentChampion().getAppliedEffects().get(i).getDuration()>1)
							getCurrentChampion().getAppliedEffects().get(i).setDuration(getCurrentChampion().getAppliedEffects().get(i).getDuration()-1);
						else
							getCurrentChampion().getAppliedEffects().remove(i);
					}
				}
			}
			else {
			for(int i=0; i<getCurrentChampion().getAppliedEffects().size();i++){
				if(getCurrentChampion().getAppliedEffects().get(i).getName().equals("Silence")) {
					f1=true;
					break;
				}	
			}
			if(f1==true) {
				for(int i=0; i<getCurrentChampion().getAppliedEffects().size();i++){
					if(getCurrentChampion().getAppliedEffects().get(i).getName().equals("Silence")) {
						getCurrentChampion().getAppliedEffects().remove(i);
					}
				}
				throw new AbilityUseException();
			}
			else {
		 if(distance>=a.getCastRange()) {
			 throw new AbilityUseException();
		 }
		 else {
			 targets.add((Damageable) board[x][y]);
		 }
		 if(a instanceof DamagingAbility) {
			 boolean f=false;
			 if(targets.get(0) instanceof Cover) {
				 a.execute(targets);
				 getCurrentChampion().setMana(getCurrentChampion().getMana()- a.getManaCost());
					getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -a.getRequiredActionPoints());
			 }
			 
			 else if(checkFriend2((Champion) targets.get(0),currentChampPlayer(getCurrentChampion()))==false) {
				for(int i=0;i<((Champion)targets.get(0)).getAppliedEffects().size();i++) {
					if(((Champion)targets.get(0)).getAppliedEffects().get(i).getName().equals("Shield")) {
						((Champion)targets.get(0)).getAppliedEffects().remove(i);
						f=true;
					}
				}
				if(f==false) {
					a.execute(targets);
					getCurrentChampion().setMana(getCurrentChampion().getMana()- a.getManaCost());
					getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -a.getRequiredActionPoints());
				}
			 }
				
		 }
		 else if(a instanceof CrowdControlAbility) {
			 if(((CrowdControlAbility) a).getEffect().getType().equals(EffectType.DEBUFF)&& (checkFriend2((Champion) targets.get(0),currentChampPlayer(getCurrentChampion()))==false)) {
					a.execute(targets);
					getCurrentChampion().setMana(getCurrentChampion().getMana()- a.getManaCost());
					getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -a.getRequiredActionPoints());
			 }
			 else if(((CrowdControlAbility) a).getEffect().getType().equals(EffectType.BUFF)&& (checkFriend2((Champion) targets.get(0),currentChampPlayer(getCurrentChampion()))==true)) {
				 a.execute(targets);
				 getCurrentChampion().setMana(getCurrentChampion().getMana()- a.getManaCost());
					getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -a.getRequiredActionPoints());
			 } 
			 else
				 throw new AbilityUseException();
				
		 }
		 else if(a instanceof HealingAbility) {
			 if(checkFriend2((Champion) targets.get(0),currentChampPlayer(getCurrentChampion()))==true) {
				 a.execute(targets);
				 getCurrentChampion().setMana(getCurrentChampion().getMana()- a.getManaCost());
					getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -a.getRequiredActionPoints());
			 }
			 else
				 throw new AbilityUseException();
				 
		 }
		 
			}
			}
			 }
			 else {
				 getCurrentChampion().setMana(getCurrentChampion().getMana()- a.getManaCost());
				getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -a.getRequiredActionPoints());
			 }
			}
	 }
	 
	 public void useLeaderAbility() throws LeaderNotCurrentException, LeaderAbilityAlreadyUsedException { 
		ArrayList<Champion> leaderTeam = new ArrayList<Champion>();
		if(currentChampPlayer(getCurrentChampion())==firstPlayer){
			if(firstPlayer.getLeader() != getCurrentChampion())
				throw new LeaderNotCurrentException();
			else if(isFirstLeaderAbilityUsed()==false) {
			for(int i=0;i<3;i++) {
				leaderTeam.add(firstPlayer.getTeam().get(i));
			}
			getCurrentChampion().useLeaderAbility(leaderTeam);
			firstLeaderAbilityUsed = true;
			}
			else if(isFirstLeaderAbilityUsed()==true)
				throw new LeaderAbilityAlreadyUsedException();
		}
		else if(currentChampPlayer(getCurrentChampion())==secondPlayer){
			if(secondPlayer.getLeader() != getCurrentChampion())
				throw new LeaderNotCurrentException();
			else if(isSecondLeaderAbilityUsed()==false) {
			for(int i=0;i<3;i++) {
				leaderTeam.add(secondPlayer.getTeam().get(i));
			}
			getCurrentChampion().useLeaderAbility(leaderTeam);
			secondLeaderAbilityUsed = true;;
			}
			else if(isSecondLeaderAbilityUsed()==true)
				throw new LeaderAbilityAlreadyUsedException();
		} 
	 }
	 
	 public void endTurn() {
		 turnOrder.remove();
		 if(turnOrder.isEmpty()) {
			 prepareChampionTurns();
		 }
		 else {
			 for(int i=0;i<turnOrder.size();i++) {
				 if(getCurrentChampion().getCondition()==Condition.INACTIVE) {
					 turnOrder.remove();
				 }
			 else
				 break;
			 }
			 for(int i = 0; i< getCurrentChampion().getAppliedEffects().size();i++) {
				 getCurrentChampion().getAppliedEffects().get(i).setDuration(getCurrentChampion().getAppliedEffects().get(i).getDuration()-1);
			 }
			 for(int i = 0; i< getCurrentChampion().getAbilities().size();i++) {
				 getCurrentChampion().getAbilities().get(i).setCurrentCooldown( getCurrentChampion().getAbilities().get(i).getCurrentCooldown()-1);
			 }

			 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getMaxActionPointsPerTurn());;
			 prepareChampionTurns();
		 }
	 }
	
	 
	 
	 private void prepareChampionTurns() {
		 for(int i =0; i<firstPlayer.getTeam().size();i++) {
			 if(firstPlayer.getTeam().get(i).getCondition()!=Condition.KNOCKEDOUT)
				 turnOrder.insert(firstPlayer.getTeam().get(i));  
		 }
		 for(int i =0; i<secondPlayer.getTeam().size();i++) {
			 if(secondPlayer.getTeam().get(i).getCondition()!=Condition.KNOCKEDOUT)
				 turnOrder.insert(secondPlayer.getTeam().get(i));
		 } 
	 }
	 
	 
	 
	 
	 
	 
	 
	 public Damageable targets(int range, Direction d) {
		 int x = getCurrentChampion().getLocation().y;
		 int y = getCurrentChampion().getLocation().x;
		 Damageable damg =  null;
		 if(d == Direction.UP) {
			 System.out.print("hello");
			 boolean flag = false;
			 int indexUp=1;
			 System.out.print(range);
			 for(int i=1;i<=range;i++) {
				 if(y+i>=5)
					 break;
				 else if((y+i<=4)&&(board[y+i][x]!=null)) { 
					flag=true;
				 	break;
			 }
				 else 
					 indexUp++;
				 
			 }
			 
				 if(flag==true) {
					 System.out.print(board[y-indexUp][x]);

					 return (Damageable) board[y+indexUp][x];}
				 else
					 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -1);
		 }
		 else if(d == Direction.DOWN) {
			 boolean flagDown = false;
			 int indexDown=1;
			 for(int i=1;i<=range;i++) {
				 if((y-i>=0)&&(board[y-i][x]!=null)) {
					
					 flagDown=true;
				 	break;
			 }
				 else
					 indexDown++;
			 }
			 
				 if(flagDown==true) {
					 System.out.print(board[y-indexDown][x]);
					 return (Damageable) board[y-indexDown][x];}
				 else
					 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -1);
		 }
		 else if(d == Direction.RIGHT) {
			 boolean flagRight = false;
			 int indexRight=1;
			 for(int i=1;i<=range;i++) {
				 if((x+i<=4)&&(board[y][x+i]!=null)) {
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
		 }
		 else if(d == Direction.LEFT) {
			 boolean flagLeft = false;
			 int indexLeft=1;
			 for(int i=1;i<=range;i++) {
				 if((x-i>=0)&&(board[y][x-i]!=null)) {
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
		 }
		return null;
		 
		 }
		
	
	 
	 
	 
	 public ArrayList<Damageable> targetsCast(int range, Direction d) {
		 int x = getCurrentChampion().getLocation().y;
		 int y = getCurrentChampion().getLocation().x;
		 ArrayList<Damageable> damg =  new ArrayList<Damageable>();
		 if(d == Direction.UP) {
			 for(int i=1;i<=range;i++) {
				 if((board[y+i][x]!=null)&&y<4) { 
					damg.add((Damageable)board[y+i][x]);
			 }
		 }
		 }
		 else if(d== Direction.DOWN) {
			 for(int i=1;i<=range;i++) {
				 if((board[y-i][x]!=null)&&y>0) {
					damg.add((Damageable)board[y-i][x]);
			 }
		 }
		 }
		 else if(d==Direction.RIGHT) {
			 for(int i=1;i<=range;i++) {
				 if((board[y][x+i]!=null) && x<4) {
					damg.add((Damageable)board[y][x+i]);
			 }
		 }
		 }
		 else if(d==Direction.LEFT) {
			 for(int i=1;i<=range;i++) {
				 if((board[y][x-i]!=null)&&x>0) {
					damg.add((Damageable)board[y][x-i]);
			 }
			 }
		 }
		 return damg;
	 
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
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 public boolean checkFriend2(Champion c, Player p) {
		 boolean f = false;
		 for(int i=0;i<p.getTeam().size();i++) {
			 if(p.getTeam().get(i).getName().equals(c.getName())) {
				 f= true;
				 break;
		 }
		 
		 else
			 f =false;
	 }
	 return f;
	 }

	 
	 
	 
	
	 public boolean checkDisarm(Champion currentCh) {
		boolean f = false;
		 for(int i=0;i<currentCh.getAppliedEffects().size(); i++){
			 if(currentCh.getAppliedEffects().get(i).getName().equals("Disarm")) {
				 f= true;
				 currentCh.getAppliedEffects().remove(currentCh.getAppliedEffects().get(i));
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
				 currentCh.getAppliedEffects().remove(currentCh.getAppliedEffects().get(i));
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
				 currentCh.getAppliedEffects().remove(currentCh.getAppliedEffects().get(i));
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