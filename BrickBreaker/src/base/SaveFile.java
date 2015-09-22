package base;

public class SaveFile {
	
	private String Name;
	private boolean freePlay = false;
	private String level = "Level001";
	private int round = 1;
	private int Lives = 3;
	private int Points = 0;
	private int Lasers = 0;
	private int numLevels = 1;

	public SaveFile(String name, boolean freeplay){
		Name = name;
		freePlay = freeplay;
	}
	
	public String getFileName(){
		return Name;
	}
	
	public boolean inFreePlay(){
		return freePlay;
	}
	
	public boolean inNormalPlay(){
		return !freePlay;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public int getLives() {
		return Lives;
	}

	public void setLives(int lives) {
		Lives = lives;
	}

	public int getPoints() {
		return Points;
	}

	public void setPoints(int points) {
		Points = points;
	}

	public int getLasers() {
		return Lasers;
	}

	public void setLasers(int lasers) {
		Lasers = lasers;
	}

	public int getNumLevels() {
		return numLevels;
	}

	public void setNumLevels(int numLevels) {
		this.numLevels = numLevels;
	}
	
	@Override
	public String toString(){
		return Name;
	}
	
}
