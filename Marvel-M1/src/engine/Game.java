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
		 if(getCurrentChampion().getCurrentActionPoints()>=1) {
			 switch(d) {
			 	case UP:
			 		 if(x==4 ||board[x+1][y]!=null) 
						 throw new UnallowedMovementException();
					 
					 else {
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
		 else 
			 throw new NotEnoughResourcesException();
	 }
	 
	 
	 
	 
	 
	 public void attack(Direction d) throws InvalidTargetException, ChampionDisarmedException, NotEnoughResourcesException {
		 Player friendPlayer = currentChampPlayer(getCurrentChampion());
		 ArrayList<Damageable> target = new ArrayList<Damageable>();
		 if(getCurrentChampion().getCurrentActionPoints()>=2) {
		 switch(d) {
		 	case UP:
		 		if(getCurrentChampion().getLocation().y == 4)
		 			throw new InvalidTargetException();
		 		else {
		 			int y = getCurrentChampion().getLocation().x;
		 			int x = getCurrentChampion().getLocation().y;
			 		for(int i=1; i<=(int) getCurrentChampion().getAttackRange();i++) {
			 			if(y+i<=4&&board[y+1][x]== null)
			 			target.add((Damageable) board[y+i][x]);
			 			else 
			 				target.add((Damageable) board[y+i][x]);
			 				break;
			 			
			 		}
		 		
			 		for(int i = 0; i<target.size();i++) {
			 		if(target.get(i) == null)
			 			getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -1);
			 		else {

		 			if(target.get(i) instanceof Cover)
		 				 target.get(i).setCurrentHP(target.get(i).getCurrentHP() - getCurrentChampion().getAttackDamage());
		 			else {
		 		if((target.get(i).getLocation().y<=4)&&(checkFriend(friendPlayer,target.get(i).getLocation())) == false) {
		 			
		 			 if(checkDisarm(getCurrentChampion())==true){
		 				 throw new ChampionDisarmedException();
		 			 }
		 			 else if(checkShield((Champion) target.get(i))==true) {
		 				 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -1);
		 			 }
		 			else {if(checkDodge((Champion) target.get(i))==true) {
		 	 			int prob = (int) Math.round(Math.random());
		 	 			if (prob == 1)
		 	 				 target.get(i).setCurrentHP((int)(target.get(i).getCurrentHP()-(getCurrentChampion().getAttackDamage())));
		 	 				 
		 	 			getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -1);
		 			}}
	
		 			if((target.get(i) instanceof AntiHero && getCurrentChampion() instanceof AntiHero)||(target.get(i) instanceof Hero && getCurrentChampion() instanceof Hero) || (target.get(i) instanceof Villain && getCurrentChampion() instanceof Villain)) {
		 				target.get(i).setCurrentHP((int)(target.get(i).getCurrentHP()-(getCurrentChampion().getAttackDamage())));
					} 
					else {
						target.get(i).setCurrentHP((int)(target.get(i).getCurrentHP()-(getCurrentChampion().getAttackDamage()*1.5)));
					}
				 }
		 		else {		 	 			
		 			getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -1);
		 			}
		 		}}
		 		}}
		 		break;
		 	case DOWN:
		 		if(getCurrentChampion().getLocation().y == 0)
		 			throw new InvalidTargetException();
		 		else {
		 			int y = getCurrentChampion().getLocation().y;
		 			int x = getCurrentChampion().getLocation().x;
			 		for(int i=1; i<=getCurrentChampion().getAttackRange();i++) {
			 			if(y-i>=0&&board[y-i][x]==null)
			 			target.add((Damageable) board[y-i][x]);
			 			else 
			 				target.add((Damageable) board[y-i][x]);
			 				break;
			 		}
			 	for(int i = 0; i<target.size();i++) {
		 		if(target.get(i) == null)
		 			getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -1);
		 		else {
		 			if(target.get(i) instanceof Cover)
		 				target.get(i).setCurrentHP(target.get(i).getCurrentHP() - getCurrentChampion().getAttackDamage());
		 			else {
		 		if((target.get(i).getLocation().getX()<=0)&&(checkFriend(friendPlayer,target.get(i).getLocation())) == false) {
		 			
		 			 if(checkDisarm(getCurrentChampion())==true){
		 				 throw new ChampionDisarmedException();
		 			 }
		 			 else if(checkShield((Champion) target.get(i))==true) {
		 				 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -1);
		 			 }
		 			else {if(checkDodge((Champion) target.get(i))==true) {
		 	 			int prob = (int) Math.round(Math.random());
		 	 			if (prob == 1)
		 	 				 target.get(i).setCurrentHP((int)(target.get(i).getCurrentHP()-(getCurrentChampion().getAttackDamage())));
		 	 				 
		 	 			getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -1);
		 			}}
		 			if((target.get(i) instanceof AntiHero && getCurrentChampion() instanceof AntiHero)||(target.get(i) instanceof Hero && getCurrentChampion() instanceof Hero) || (target.get(i) instanceof Villain && getCurrentChampion() instanceof Villain)) {
		 				target.get(i).setCurrentHP((int)(target.get(i).getCurrentHP()-(getCurrentChampion().getAttackDamage())));
					} 
					else {
						target.get(i).setCurrentHP((int)(target.get(i).getCurrentHP()-(getCurrentChampion().getAttackDamage()*1.5)));
					}
				 }
		 		else {
		 			getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -1);
		 			}
		 		
		 		}}}
		 }
		 		break;
		 	case RIGHT:
		 		if(getCurrentChampion().getLocation().x == 4)
		 			throw new InvalidTargetException();
		 		else {
		 			int y = getCurrentChampion().getLocation().y;
		 			int x = getCurrentChampion().getLocation().x;
			 		for(int i=1; i<=getCurrentChampion().getAttackRange();i++) {
			 			if(x+i<=4&& board[y][x+i] == null)
			 			target.add((Damageable) board[y][x+i]);
			 			else 
			 				target.add((Damageable) board[y][x+i]);
			 				break;
			 		
			 		}
		 		
			 		for(int i = 0; i<target.size();i++) {
			 		if(target.get(i) == null)
			 			getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -1);
			 		else {

		 			if(target.get(i) instanceof Cover)
		 				target.get(i).setCurrentHP(target.get(i).getCurrentHP() - getCurrentChampion().getAttackDamage());
		 			else {
		 		if((target.get(i).getLocation().getY()<=4)&&(checkFriend(friendPlayer,target.get(i).getLocation())) == false) {
		 			
		 			 if(checkDisarm(getCurrentChampion())==true){
		 				 throw new ChampionDisarmedException();
		 			 }
		 			 else if(checkShield((Champion) target.get(i))==true) {
		 				 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -1);
		 				// getCurrentChampion().getAppliedEffects().remove(target.get(i));
		 			 }
		 			else {if(checkDodge((Champion) target.get(i))==true) {
		 	 			int prob = (int) Math.round(Math.random());
		 	 			if (prob == 1)
		 	 				 target.get(i).setCurrentHP((int)(target.get(i).getCurrentHP()-(getCurrentChampion().getAttackDamage())));
		 	 				 
		 	 			getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -1);
		 			}}
		 			if((target.get(i) instanceof AntiHero && getCurrentChampion() instanceof AntiHero)||(target.get(i) instanceof Hero && getCurrentChampion() instanceof Hero) || (target.get(i) instanceof Villain && getCurrentChampion() instanceof Villain)) {
		 				target.get(i).setCurrentHP((int)(target.get(i).getCurrentHP()-(getCurrentChampion().getAttackDamage())));
					} 
					else {
						target.get(i).setCurrentHP((int)(target.get(i).getCurrentHP()-(getCurrentChampion().getAttackDamage()*1.5)));
					}
				 }
		 		else {
	 	 			getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -1);
		 			}
		 		}}
		 		}}
		 		break;
		 	case LEFT:	
		 		if(getCurrentChampion().getLocation().x == 0)
	 			throw new InvalidTargetException();
	 		else {
	 			int y = getCurrentChampion().getLocation().y;
	 			int x = getCurrentChampion().getLocation().x;
		 		for(int i=1; i<=getCurrentChampion().getAttackRange();i++) {
		 			if(x-i>=0&&board[y][x-i]==null)
		 			target.add((Damageable) board[y][x-i]);
		 			else 
		 				target.add((Damageable) board[y][x-i]);
		 				break;
		 		}
		 		
		 		for(int i = 0; i<target.size();i++) {
		 		if(target.get(i) == null)
		 			getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -1);
		 		else {

		 			if(target.get(i) instanceof Cover)
		 				target.get(i).setCurrentHP(target.get(i).getCurrentHP() - getCurrentChampion().getAttackDamage());
		 			else {
		 		if((target.get(i).getLocation().getY()<=0)&&(checkFriend(friendPlayer,target.get(i).getLocation())) == false) {
		 			
		 			 if(checkDisarm(getCurrentChampion())==true){
		 				 throw new ChampionDisarmedException();
		 			 }
		 			 else if(checkShield((Champion) target.get(i))==true) {
		 				 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -1);
		 			 }
		 			else {if(checkDodge((Champion) target.get(i))==true) {
		 	 			int prob = (int) Math.round(Math.random());
		 	 			if (prob == 1)
		 	 				 target.get(i).setCurrentHP((int)(target.get(i).getCurrentHP()-(getCurrentChampion().getAttackDamage())));
		 	 				 
		 	 			getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -1);
		 			}}
		 			if((target.get(i) instanceof AntiHero && getCurrentChampion() instanceof AntiHero)||(target.get(i) instanceof Hero && getCurrentChampion() instanceof Hero) || (target.get(i) instanceof Villain && getCurrentChampion() instanceof Villain)) {
		 				target.get(i).setCurrentHP((int)(target.get(i).getCurrentHP()-(getCurrentChampion().getAttackDamage())));
					} 
					else {
						target.get(i).setCurrentHP((int)(target.get(i).getCurrentHP()-(getCurrentChampion().getAttackDamage()*1.5)));
					}
				 }
		 		else {
	 	 			getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints() -1);
		 			}
		 		}
		 		}}}
		 		break;
		 }	
	 }
	else 
	 			throw new NotEnoughResourcesException();
		 
} 
	 public void castAbility(Ability a) {
		 
		 
	 }
	 
	 public void castAbility(Ability a, Direction d) {
		 
		 
	 }
	 
	 public void castAbility(Ability a, int x, int y) {
		 
	 }
	 
	 public void useLeaderAbility() {
		 
		 
	 }
	 
	 public void endTurn() {
		 
		 
	 }
	 
	 private void prepareChampionTurns() {
		 
		 
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
		 for(int i=0;i<c.getTeam().size();i++) {
			 if(c.getTeam().get(i).getLocation() == location)
				 return true;
			 else
				 return false;
		 }
		 return false;
	 }
	 public boolean checkShock(Champion currentCh) {
		 for(int i=0;i<currentCh.getAppliedEffects().size(); i++){
			 if(currentCh.getAppliedEffects().get(i).getName().equals("Shock"))
				 return true;
			 else
				 return false;
		 }
		return false;
	 }
	 public boolean checkDisarm(Champion currentCh) {
		 for(int i=0;i<currentCh.getAppliedEffects().size(); i++){
			 if(currentCh.getAppliedEffects().get(i).getName().equals("Disarm"))
				 return true;
			 else
				 return false;
		 }
		return false;
	 }
	 public boolean checkShield(Champion currentCh) {
		 for(int i=0;i<currentCh.getAppliedEffects().size(); i++){
			 if(currentCh.getAppliedEffects().get(i).getName().equals("Shield"))
				 return true;
			 else
				 return false;
		 }
		return false;
	 }
	 public boolean checkDodge(Champion currentCh) {
		 for(int i=0;i<currentCh.getAppliedEffects().size(); i++){
			 if(currentCh.getAppliedEffects().get(i).getName().equals("Dodge"))
				 return true;
			 else
				 return false;
		 }
		return false;
	 }
	
}
//hello