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
		board = new Object[BOARDWIDTH][BOARDHEIGHT];
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
		PriorityQueue tmp = new PriorityQueue(turnOrder.size());
		for(int i=0;i<turnOrder.size();i++) {
			tmp.insert(turnOrder.peekMin());
			return (Champion)turnOrder.remove();	
		}
		for(int i=0;i<tmp.size();i++) {
			turnOrder.insert(tmp.remove());
		}
		return null;
	}
	 public Player checkGameOver() {
		 if(getFirstPlayer().getTeam().get(0).getCondition().equals(Condition.KNOCKEDOUT) && getFirstPlayer().getTeam().get(1).equals(Condition.KNOCKEDOUT) && getFirstPlayer().getTeam().get(2).equals(Condition.KNOCKEDOUT)) {
			 if(getSecondPlayer().getTeam().get(0).getCondition() != (Condition.KNOCKEDOUT) ||getSecondPlayer().getTeam().get(1).getCondition() != (Condition.KNOCKEDOUT) || getSecondPlayer().getTeam().get(2).getCondition() != (Condition.KNOCKEDOUT))
				 return secondPlayer;
			 else 
				 return firstPlayer;
		 }
		 return null;
	 }
	 public void move(Direction d) throws UnallowedMovementException {
		 if(getCurrentChampion().getCurrentActionPoints()!=0) {
			 if(d.UP != null || getCurrentChampion().getLocation().getY()==4) {
				 throw new UnallowedMovementException();
			 }
			 else {
				 Point newPoint = new Point();
				 newPoint.setLocation(getCurrentChampion().getLocation().getX(), getCurrentChampion().getLocation().getY()+1);
				 getCurrentChampion().setLocation(newPoint);
				 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints()-1);
			 }
			 if(d.DOWN != null || getCurrentChampion().getLocation().getY()==0) {
				 throw new UnallowedMovementException();
			 }
			 else {
				 Point newPoint = new Point();
				 newPoint.setLocation(getCurrentChampion().getLocation().getX(), getCurrentChampion().getLocation().getY()-1);
				 getCurrentChampion().setLocation(newPoint);
				 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints()-1);
			 }
			 if(d.LEFT != null || getCurrentChampion().getLocation().getX()==0) {
				 throw new UnallowedMovementException();
			 }
			 else {
				 Point newPoint = new Point();
				 newPoint.setLocation(getCurrentChampion().getLocation().getX()-1, getCurrentChampion().getLocation().getY());
				 getCurrentChampion().setLocation(newPoint);
				 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints()-1);
			 }
			 if(d.RIGHT != null || getCurrentChampion().getLocation().getX()==4) {
				 throw new UnallowedMovementException();
			 }
			 else {
				 Point newPoint = new Point();
				 newPoint.setLocation(getCurrentChampion().getLocation().getX()+1, getCurrentChampion().getLocation().getY());
				 getCurrentChampion().setLocation(newPoint);
				 getCurrentChampion().setCurrentActionPoints(getCurrentChampion().getCurrentActionPoints()-1);
			 }
			
		 }
	 }
	 public void attack(Direction d) throws InvalidTargetException, ChampionDisarmedException {
		 Player currentPlayer = choosePlayer(getCurrentChampion());
		 if(check(getCurrentChampion())==true){
			 throw new ChampionDisarmedException();
		 }
		 else if(checkShield(getEnemyCh(currentPlayer))==true) {
			 getCurrentChampion().setMaxActionPointsPerTurn(getCurrentChampion().getMaxActionPointsPerTurn() -1);
		 }
		// else if(check3(getEnemyCh(currentPlayer))==true) {
 		//		int prob = (int)(Math.random() * 1);
 		//		if(prob==0)
 		//			attack(d);
 		//		else
 		//			getCurrentChampion().setMaxActionPointsPerTurn(getCurrentChampion().getMaxActionPointsPerTurn() -1);
		 else {
		 Point other = new Point();
		 switch(d) {
		 	case UP:
		 		other.setLocation(getCurrentChampion().getLocation().getX(), getCurrentChampion().getLocation().getY()+getCurrentChampion().getAttackRange());
		 		if((other != null)&&(other.getY()<=4)&&(getOtherPoint(currentPlayer) != other)) {
		 
		 			if((getEnemyCh(currentPlayer) instanceof AntiHero && getCurrentChampion() instanceof AntiHero)||(getEnemyCh(currentPlayer) instanceof Hero && getCurrentChampion() instanceof Hero) || (getEnemyCh(currentPlayer) instanceof Villain && getCurrentChampion() instanceof Villain)) {
						 getEnemyCh(currentPlayer).setCurrentHP((int)(getEnemyCh(currentPlayer).getCurrentHP()-(getCurrentChampion().getAttackDamage())));
					} 
					else {
						 getEnemyCh(currentPlayer).setCurrentHP((int)(getEnemyCh(currentPlayer).getCurrentHP()-(getCurrentChampion().getAttackDamage()*1.5)));
					}
				 }
		 		else {
		 				throw new InvalidTargetException();
		 		}
		 			
		 		
		 	case DOWN:
		 		other.setLocation(getCurrentChampion().getLocation().getX(), getCurrentChampion().getLocation().getY()-getCurrentChampion().getAttackRange());
		 		if((other != null)&&(other.getY()>=0)&&(getOtherPoint(currentPlayer) != other)) {
		 			if((getEnemyCh(currentPlayer) instanceof AntiHero && getCurrentChampion() instanceof AntiHero)||(getEnemyCh(currentPlayer) instanceof Hero && getCurrentChampion() instanceof Hero) || (getEnemyCh(currentPlayer) instanceof Villain && getCurrentChampion() instanceof Villain)) {
						 getEnemyCh(currentPlayer).setCurrentHP((int)(getEnemyCh(currentPlayer).getCurrentHP()-(getCurrentChampion().getAttackDamage())));
					} 
					else {
						 getEnemyCh(currentPlayer).setCurrentHP((int)(getEnemyCh(currentPlayer).getCurrentHP()-(getCurrentChampion().getAttackDamage()*1.5)));
					}
	 		}
		 			else {
		 					throw new InvalidTargetException();
		 				
		 				 }
		 	case RIGHT:
		 		other.setLocation(getCurrentChampion().getLocation().getX(), getCurrentChampion().getLocation().getX()+getCurrentChampion().getAttackRange());
		 		if((other != null)&&(other.getX()<=4)&&(getOtherPoint(currentPlayer) != other)) {
		 			if((getEnemyCh(currentPlayer) instanceof AntiHero && getCurrentChampion() instanceof AntiHero)||(getEnemyCh(currentPlayer) instanceof Hero && getCurrentChampion() instanceof Hero) || (getEnemyCh(currentPlayer) instanceof Villain && getCurrentChampion() instanceof Villain)) {
						 getEnemyCh(currentPlayer).setCurrentHP((int)(getEnemyCh(currentPlayer).getCurrentHP()-(getCurrentChampion().getAttackDamage())));
					} 
					else {
						 getEnemyCh(currentPlayer).setCurrentHP((int)(getEnemyCh(currentPlayer).getCurrentHP()-(getCurrentChampion().getAttackDamage()*1.5)));
					}
		 				
		 		}
		 			
		 		else {
		 			throw new InvalidTargetException();
		 				 }
		 	case LEFT:
		 		other.setLocation(getCurrentChampion().getLocation().getX(), getCurrentChampion().getLocation().getX()-getCurrentChampion().getAttackRange());
		 		if((other != null)&&(other.getX()>=0)&&(getOtherPoint(currentPlayer) != other)) {
		 			if((getEnemyCh(currentPlayer) instanceof AntiHero && getCurrentChampion() instanceof AntiHero)||(getEnemyCh(currentPlayer) instanceof Hero && getCurrentChampion() instanceof Hero) || (getEnemyCh(currentPlayer) instanceof Villain && getCurrentChampion() instanceof Villain)) {
						 getEnemyCh(currentPlayer).setCurrentHP((int)(getEnemyCh(currentPlayer).getCurrentHP()-(getCurrentChampion().getAttackDamage())));
					} 
					else {
						 getEnemyCh(currentPlayer).setCurrentHP((int)(getEnemyCh(currentPlayer).getCurrentHP()-(getCurrentChampion().getAttackDamage()*1.5)));
					}
	 				
	 		}
		 			else {
		 				throw new InvalidTargetException();
		 				
		 				 }
		 		
		 			}
		 		
		 }
		 } 
		 
	 
	 public Player choosePlayer(Champion currentCh) {
		 for(int i=0;i<firstPlayer.getTeam().size();i++) {
			 if(firstPlayer.getTeam().get(i)==currentCh)
				 return secondPlayer;
			 else
				 return firstPlayer;
		 }
		 return null;
	 }
	 public Point getOtherPoint(Player currentPlayer) {
		 for(int i=0;i<currentPlayer.getTeam().size();i++) {
			 return currentPlayer.getTeam().get(i).getLocation();
		 }
		 return null;
	 }
	 public boolean check(Champion currentCh) {
		 for(int i=0;i<currentCh.getAppliedEffects().size(); i++){
			 if(currentCh.getAppliedEffects().get(i).getName().equals("Disarm"))
				 return true;
			 else
				 return false;
		 }
		return true;
	 }
	 public boolean checkShield(Champion currentCh) {
		 for(int i=0;i<currentCh.getAppliedEffects().size(); i++){
			 if(currentCh.getAppliedEffects().get(i).getName().equals("Shield"))
				 return true;
			 else
				 return false;
		 }
		return true;
	 }
	 public boolean checkDodge(Champion currentCh) {
		 for(int i=0;i<currentCh.getAppliedEffects().size(); i++){
			 if(currentCh.getAppliedEffects().get(i).getName().equals("Dodge"))
				 return true;
			 else
				 return false;
		 }
		return true;
	 }
	 public Champion getEnemyCh(Player current) {
		 for(int i=0;i<current.getTeam().size(); i++){
			 return current.getTeam().get(i);
		 }
		 return null;
	 }
}
//hello