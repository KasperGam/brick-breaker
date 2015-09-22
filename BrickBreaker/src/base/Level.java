package base;

import java.awt.Color;
import java.util.ArrayList;

public class Level {

	private ArrayList<Brick> bricks = new ArrayList<Brick>();
	
	private Color background = null;
	private Color foreground=  null;
	private Color top = null;
	private Color bottom = null;
	
	private int baseHealth = 1;
	
	private int perkRainBad = -1;
	private int perkRainGood = -1;
	
	private int spawnEvilBalls = -1;
	private Color evilColor1 = Color.red;
	private Color evilColor2 = Color.yellow;
	private int cycleTime = 500;
	
	private int badPerkCounter = 0;
	private int goodPerkCounter = 0;
	private int evilBallsCounter = 0;
	
	private String levelName = "Level001";
	private String nextLevel = "Level002";
	
	public Level(String name){
		levelName = name;
	}
	
	public void addBrick(Brick b){
		bricks.add(b);
	}
	
	/**
	 * Sets the attribute traits. Current attributes:
	 * PERK_RAIN_BAD, PERK_RAIN_GOOD, EVIL_BALL_SPAWN
	 * @param att The name of the attribute
	 * @param val The value(s) associated with that attribute.
	 */
	public void setAttribute(String att, String val){
		att=att.toUpperCase();
		if (att.equals("PERK_RAIN_BAD")){
			perkRainBad = Integer.parseInt(val);
		} else if (att.equals("PERK_RAIN_GOOD")){
			perkRainGood = Integer.parseInt(val);
		} else if (att.equals("EVIL_BALL_SPAWN")){
			String[]args = val.split(" ");
			if(args.length>=8){
				spawnEvilBalls = Integer.parseInt(args[0]);
				int red = Integer.parseInt(args[1]);
				int green = Integer.parseInt(args[2]);
				int blue = Integer.parseInt(args[3]);
				evilColor1 = new Color(red, green, blue);
				red =  Integer.parseInt(args[4]);
				green = Integer.parseInt(args[5]);
				blue = Integer.parseInt(args[6]);
				evilColor2 = new Color(red, green, blue);
				cycleTime = Integer.parseInt(args[7]);
			} else if (args.length>=2){
				spawnEvilBalls = Integer.parseInt(args[0]);
				cycleTime = Integer.parseInt(args[1]);
			}
		}
	}
	
	/**
	 * Returns how many of the specified perk should be spawned.
	 * @param good Whether the perk is good or bad.
	 * @param timePassed The time passed.
	 * @return Returns an int, the number of perks to spawn.
	 */
	public int spawnPerk(boolean good, int timePassed){
		if(good){
			if(perkRainGood<0){
				return 0;
			} else {
				int total = timePassed/perkRainGood;
				int takeoff = timePassed%perkRainGood;
				if(takeoff>goodPerkCounter){
					total++;
					goodPerkCounter = perkRainGood-(takeoff-goodPerkCounter);
				} else {
					goodPerkCounter-=takeoff;
				}
				return total;
			}
		} else {
			if(perkRainBad<0){
				return 0;
			} else {
				int total = timePassed/perkRainBad;
				int takeoff = timePassed%perkRainBad;
				if(takeoff>badPerkCounter){
					total++;
					badPerkCounter = perkRainBad-(takeoff-badPerkCounter);
				} else {
					badPerkCounter-=takeoff;
				}
				return total;
			}
		}
	}
	
	/**
	 * Gets how many evil balls should spawn.
	 * @param timePassed The time passed (in milliseconds).
	 * @return
	 */
	public int spawnEvilBall(int timePassed){
		if(spawnEvilBalls<0){
			return 0;
		} else {
			int total = timePassed/spawnEvilBalls;
			int takeoff = timePassed%spawnEvilBalls;
			if(takeoff>evilBallsCounter){
				total++;
				evilBallsCounter = spawnEvilBalls-(takeoff-evilBallsCounter);
			} else {
				evilBallsCounter-=takeoff;
			}
			return total;
		}
	}
	
	/**
	 * Gets this level's evil balls.
	 * @param w The width
	 * @param h The height
	 * @param r The radius
	 * @return Returns the evil ball for this level. If this level has no
	 * specific evil ball, will return the default one (red to yellow, cycle time
	 * of 500 milliseconds).
	 */
	public EvilBall getEvilBall(int w, int h, int r){
		EvilBall curBall = new EvilBall(w, h, r, cycleTime);
		curBall.setFirstColor(evilColor1);
		curBall.setSecondColor(evilColor2);
		return curBall;
	}
	
	/**
	 * Resets the level to move to the next level's characteristics.
	 * @param newLevelName
	 */
	public void reset(String newLevelName){
		levelName = newLevelName;
		bricks.clear();
		perkRainBad = -1;
		perkRainGood = -1;
		spawnEvilBalls = -1;
		evilColor1 = Color.red;
		evilColor2 = Color.yellow;
		cycleTime = 500;
	}
	
	/////////////// GETTERS AND SETTERS ////////////////
	
	
	public String getLevelName(){
		return levelName;
	}
	
	public ArrayList<Brick> getBricks(){
		return bricks;
	}
	
	public Brick getBrick(int index){
		return bricks.get(index);
	}
	
	public void setNext(String next){
		nextLevel = next;
	}
	
	public String getNext(){
		return nextLevel;
	}
	
	public void setBaseBlockStrength(int str){
		baseHealth = str;
	}
	
	public int getBaseBlockStrength(){
		return baseHealth;
	}
	
	public void setBackground(Color clr){
		background = clr;
	}
	
	public void setForeground(Color clr){
		foreground = clr;
	}
	
	public void setTopColor(Color clr){
		top = clr;
	}
	
	public void setBottomColor(Color clr){
		bottom = clr;
	}
	
	public Color getBackground(){
		return background;
	}
	
	public Color getForeground(){
		return foreground;
	}
	
	public Color getTopColor(){
		return top;
	}
	
	public Color getBottomColor(){
		return bottom;
	}
}
