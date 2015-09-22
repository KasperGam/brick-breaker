package base;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class BrickBreaker extends JComponent implements ComponentListener,
		MouseListener, MouseMotionListener, KeyListener {
	
	/////////////// CONSTANTS ///////////////
	//serial version UID. Just to get rid of that Eclipse warning
	private static final long serialVersionUID = 1L;

	//Configuration loader.
	public static final ConfigLoader config = new ConfigLoader();
	
	//ball speed. Just set higher for greater velocity, lower for lesser.
	public static int BALL_SPEED_MULTIPLIER = (int) config.getNumValue("BALL_SPEED_MULTIPLIER");
	public static final int BALL_SPEED_STEP = (int) config.getNumValue("BALL_SPEED_STEP");
	public static final int BALL_SPEED_STEP_POINT = (int) config.getNumValue("BALL_SPEED_STEP_POINT");
	
	//play states, do not mess with this. Seriously, don't change these.
	public static final String STATE_PLAYING = "playing";
	public static final String STATE_FREE_PLAY = "freePlay";
	public static final String STATE_MAIN = "main";
	public static final String STATE_INFO = "info";
	public static final String STATE_PAUSED = "pause";
	
	//Hit points amount is awarded after each brick hit, but only if the brick does not break
	private static final int HIT_POINTS = (int)config.getNumValue("HIT_POINTS");
	//Destroy points are awarded if the brick is destroyed by that ball hit. Note that hit points
	//and destroy points are not awarded for a hit destroying a brick, only destroy points are awarded
	private static final int DESTROY_POINTS = (int)config.getNumValue("DESTROY_POINTS");
	//Points awarded with the completion of each level.
	private static final int LEVEL_POINTS = (int)config.getNumValue("LEVEL_POINTS");
	//Percentage increase in level points from level to level. Leave as a decimal
	private static final double LEVEL_MULTIPLIER = config.getNumValue("LEVEL_MULTIPLIER");
	
	private static final int FREE_PLAY_START_ROUND = 1;
	
	//If the broken bricks will spawn perks or not.
	private static final boolean SPAWN_GOOD_PERKS = config.getBoolValue("SPAWN_GOOD_PERKS");
	private static final boolean SPAWN_BAD_PERKS = config.getBoolValue("SPAWN_BAD_PERKS");
	
	//Amount of milliseconds to hide the ball from view
	private static final int CONFUSION_HIDDEN_TIME = 300;
	
	//Amount of milliseconds to show the ball as a different color form the background
	private static final int CONFUSION_SHOWING_TIME = 400;
	
	//the chance is one in this number. So 1/PERK_SPAWN_CHANCE
	private static final int PERK_SPAWN_CHANCE = (int)config.getNumValue("PERK_SPAWN_CHANCE");
	
	//amount of spacing between heart icons
	private static final int HEART_SPACING = 10;
	
	private static final int TOTAL_INFO_SCREENS = 3;
	
	//if the user is invincible or not. If true, balls bounce off of the bottom,
	//so therefor it is impossible to loose.
	private static final boolean INVINCIBLE = config.getBoolValue("INVINCIBLE");

	/////////////// GLOBAL VARIABLES ///////////////
	
	private Random rand = new Random();
	
	private String currentLevel = config.getStartLevel();
	private Level level = null;
	
	private int Round = 1;
	
	// info image array
	
	private ArrayList<Image> informationScreens = new ArrayList<Image>();
	private int infoScreenSelection = 0;
	
	// main buttons

	private Button playButton;
	private Button quickPlayButton;
	private Button infoButton;
	private Button exitButton;
	
	// pause buttons
	
	private Button continueButton;
	private Button mainButton;	
	private Image pauseMenu;
	
	// load/save buttons
	
	private Button loadButton;
	private Button saveButton;
	
	// back and delete button
	
	private Button backButton;
	private Button deleteButton;
	
	// information page buttons
	
	private Button infoBackButton;
	private Button infoNextButton;
	
	// save button field
	
	private JTextField saveField = new JTextField(20);
	
	// load file picker
	
	private JComboBox loadFilePicker = new JComboBox();

	// active game variables and containers
	private Image heartImage; 
	
	private int lives = 3;
	private int levelLives = lives;
	
	private JLabel info = new JLabel("");
	private int infoTimer = -1000;
	
	public ArrayList<Brick> bricks = new ArrayList<Brick>();
	public ArrayList<Ball> balls = new ArrayList<Ball>();
	public ArrayList<Perk> perks = new ArrayList<Perk>();
	public ArrayList<Laser> lasers = new ArrayList<Laser>();
	public ArrayList<EvilBall> evilBalls = new ArrayList<EvilBall>();
	
	private boolean inFreePlay = false;
	private int freePlayRound = 1;
	
	private double minBrickY = 1;
	private double maxBrickY = 0;
	
	private Color foregroundColor = Color.black;
	private Color ballColor = foregroundColor;
	
	private Image[] damageImages = new Image[10];

	private int invincibleBallsCounter = 0;
	private int shortPaddleCounter = 0;
	private int longPaddleCounter = 0;
	private int laserCounter = 0;
	private int fastBallCounter = 0;
	private int confusionCounter = 0;
	
	private long confusionStartTime = 0;
	
	private int levelLaserCount = laserCounter;
	
	private int[] beginColor = new int[3];
	private int[] endColor = new int[3];
	
	private int numLevels = 1;
	
	private int points = 0;
	private int levelPoints = 0;

	private boolean levelReady = true;

	private Rectangle paddle = new Rectangle(getWidth() / 2, getHeight() - 30, getWidth() / 10, 30);

	private Ball ballMain;
	
	private LevelReader reader;
	private SaveFileLoader saveLoader;

	private GameThread gameThread;

	private String gameState = "main";	

/////////////// APPLET METHODS ///////////////
	
	/**
	 * Safely initializes the applet.
	 */
	public void init() {
		
		try {
			EventQueue.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					setup();
				}
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	/**
	 * Sets up the applet. Initializes the images and variables.
	 * Sets up the main screen after loading the resources.
	 */
	private void setup() {
		Dimension d = new Dimension(400, 680);
		setSize(d);
		setBackground(Color.blue);
		
		saveLoader = new SaveFileLoader();
		
		saveField.setEditable(true);
		loadFilePicker.setBounds((getWidth()-loadFilePicker.getWidth())/2, getHeight()/2, getWidth()/8, loadFilePicker.getHeight());
		loadFilePicker.setEditable(false);
		
		Image play = null;
		Image quickPlay = null;
		Image exit = null;
		Image cont = null;
		Image pause = null;
		Image main = null;
		Image save = null;
		Image load = null;
		Image back = null;
		Image delete = null;
		Image infoImage = null;
		Image infoBack = null;
		Image infoNext = null;
		
		Image playPressed = null;
		Image quickPlayPressed = null;
		Image exitPressed = null;
		Image contPressed = null;
		Image mainPressed = null;
		Image savePressed = null;
		Image loadPressed = null;
		Image backPressed = null;
		Image deletePressed = null;
		Image infoImagePressed = null;
		Image infoBackPressed = null;
		Image infoNextPressed = null;
		
		String sep = System.getProperty("file.separator");

		String mainDir = "resources"+sep+"mainImages"+sep;
		String playDir = "resources"+sep+"playImages"+sep;
		String pauseDir = "resources"+sep+"pauseImages"+sep;
		String infoDir = "resources"+sep+"infoImages"+sep;
		ClassLoader loader = getClass().getClassLoader();
		try {
			play = ImageIO.read(loader.getResource(mainDir+"playButton.png"));
			playPressed = ImageIO.read(loader.getResource(mainDir+"playButtonPressed.png"));
			
			quickPlay = ImageIO.read(loader.getResource(mainDir+"quickPlayButton.png"));
			quickPlayPressed = ImageIO.read(loader.getResource(mainDir+"quickPlayButtonPressed.png"));

			exit = ImageIO.read(loader.getResource(mainDir+"quitButton.png"));
			exitPressed = ImageIO.read(loader.getResource(mainDir+"quitButtonPressed.png"));

			cont = ImageIO.read(loader.getResource(pauseDir+"continueButton.png"));
			contPressed = ImageIO.read(loader.getResource(pauseDir+"continueButtonPressed.png"));

			pause =  ImageIO.read(loader.getResource(pauseDir+"pauseMenu.png"));

			main = ImageIO.read(loader.getResource(pauseDir+"mainButton.png"));
			mainPressed = ImageIO.read(loader.getResource(pauseDir+"mainButtonPressed.png"));

			save = ImageIO.read(loader.getResource(mainDir+"saveButton.png"));
			savePressed = ImageIO.read(loader.getResource(mainDir+"saveButtonPressed.png"));

			load = ImageIO.read(loader.getResource(mainDir+"loadButton.png"));
			loadPressed = ImageIO.read(loader.getResource(mainDir+"loadButtonPressed.png"));

			back = ImageIO.read(loader.getResource(mainDir+"backButton.png"));
			backPressed = ImageIO.read(loader.getResource(mainDir+"backButtonPressed.png"));
			
			delete = ImageIO.read(loader.getResource(mainDir+"deleteButton.png"));
			deletePressed = ImageIO.read(loader.getResource(mainDir+"deleteButtonPressed.png"));
			
			infoImage = ImageIO.read(loader.getResource(mainDir+"infoButton.png"));
			infoImagePressed = ImageIO.read(loader.getResource(mainDir+"infoButtonPressed.png"));
			
			infoBack = ImageIO.read(loader.getResource(infoDir+"backArrowButton.png"));
			infoBackPressed = ImageIO.read(loader.getResource(infoDir+"backArrowButtonPressed.png"));
			
			infoNext = ImageIO.read(loader.getResource(infoDir+"nextButton.png"));
			infoNextPressed = ImageIO.read(loader.getResource(infoDir+"nextButtonPressed.png"));
			
			heartImage = ImageIO.read(loader.getResource(playDir+"heart.png"));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Initializes the buttons
		playButton = new Button(play, playPressed, 0, (int)(getHeight()*.4));
		exitButton = new Button(exit, exitPressed, 0, playButton.getY()+playButton.getHeight()+35);
		quickPlayButton = new Button(quickPlay, quickPlayPressed, 0, playButton.getY()+playButton.getHeight()+35);
		playButton.setX((getWidth()-playButton.getWidth())/2);
		quickPlayButton.setX((getWidth()-quickPlayButton.getWidth())/2);
		exitButton.setX((getWidth()-exitButton.getWidth())/2);
		
		
		continueButton = new Button(cont, contPressed, 0, (int)(getHeight()*.4));
		mainButton = new Button(main, mainPressed, 0, continueButton.getY()+continueButton.getHeight()+35);
		continueButton.setX((getWidth()-continueButton.getWidth())/2);
		mainButton.setX((getWidth()-mainButton.getWidth())/2);
		
		saveButton = new Button(save, savePressed, 0, mainButton.getY()+mainButton.getHeight()+35);
		loadButton = new Button(load, loadPressed, 0, quickPlayButton.getY()+quickPlayButton.getHeight()+35);
		saveButton.setX((getWidth()-saveButton.getWidth())/2);
		loadButton.setX((getWidth()-loadButton.getWidth())/2);
		
		infoButton = new Button(infoImage, infoImagePressed, 0, loadButton.getY()+loadButton.getHeight()+35);
		infoButton.setX((getWidth()-infoButton.getWidth())/2);
		
		exitButton.setY(infoButton.getY()+infoButton.getHeight()+35);
		
		backButton = new Button(back, backPressed, 0, 0);
		deleteButton = new Button(delete, deletePressed, 0, 0);
		
		infoBackButton = new Button(infoBack, infoBackPressed, 15, 0);
		infoNextButton = new Button(infoNext, infoNextPressed, getWidth()-15-infoNext.getWidth(this), 0);
		infoBackButton.setY(getHeight()-infoBackButton.getHeight()-20);
		infoNextButton.setY(getHeight()-infoNextButton.getHeight()-20);
		
		//loads the damage images
		for (int i=1; i<=10; i++){
			damageImages[i-1] = getDamageImage(i);
		}
		
		//loads the information images
		for (int i=0; i<TOTAL_INFO_SCREENS; i++){
			try {
				
				informationScreens.add(ImageIO.read(loader.getResource(infoDir+"infoScreen_"+i+".png")));
			} catch (IOException e) {
				System.out.println("Error reading info screen: "+"infoScreen_"+i+".png");
				e.printStackTrace();
			}
		}
		
		pauseMenu = pause;
		
		info.setFont(new Font("SANS_SERIF", 0, 30));
		
		addComponentListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		repaint();

	}
	
	/**
	 * Goes through the steps to return to the main screen.
	 */
	private void setUpMain(){
		setBackground(Color.blue);
		currentLevel = config.getStartLevel();
		quickPlayButton.setY(playButton.getY()+playButton.getHeight()+35);
		loadButton.setY(quickPlayButton.getY()+quickPlayButton.getHeight()+35);
		infoButton.setY(loadButton.getY()+loadButton.getHeight()+35);
		exitButton.setY(infoButton.getY()+infoButton.getHeight()+35);
	}

	
	/**
	 * Starts the game, dependant on the currentLevel. 
	 */
	private void startGame() {
		gameState = STATE_PLAYING;
		
		BALL_SPEED_MULTIPLIER = (int)config.getNumValue("BALL_SPEED_MULTIPLIER");

		info.setText("Click to Start");
		
		bricks.clear();
		balls.clear();
		perks.clear();
		lasers.clear();
		evilBalls.clear();
		
		reader = new LevelReader(currentLevel);
		level = reader.getLevel();
		bricks = level.getBricks();
		evilBalls.clear();
		
		minBrickY = 1;
		maxBrickY = 0;

		for (int i=0; i<bricks.size(); i++){
			Brick currBrick = bricks.get(i);
			if (currBrick.getY()<minBrickY){
				minBrickY = currBrick.getY();
			}
			if (currBrick.getY()>maxBrickY){
				maxBrickY = currBrick.getY();
			}
		}
		
		// Color stuff with bricks
		Color begin = level.getTopColor();
		Color end = level.getBottomColor();
		beginColor[0] = begin.getRed();
		beginColor[1] = begin.getGreen();
		beginColor[2] = begin.getBlue();
		endColor[0] = end.getRed();
		endColor[1] = end.getGreen();
		endColor[2] = end.getBlue();

		// Ball code and initialization
		ballMain = new Ball(getWidth() / 2, getHeight() / 2, 10, 10);
		ballMain.setYVelocity(BALL_SPEED_MULTIPLIER);
		ballMain.setXVelocity(((rand.nextDouble() * 2) - 1) * BALL_SPEED_MULTIPLIER);
		balls.add(ballMain);
		
		//sizes ball appropriately (radius according to width)
		for (int i = 0; i < balls.size(); i++) {
			Ball curBall = balls.get(i);
			if (2.0 * getWidth() / 100.0 >= 14) {
				curBall.setFrame(curBall.getX(), curBall.getY(), (2.0 * getWidth() / 100.0), (2.0 * getWidth() / 100.0));
			} else {
				break;
			}
		}

		setBackground(level.getBackground());
		foregroundColor = level.getForeground();
		ballColor = foregroundColor;
		
		gameThread = new GameThread();
	}

	/**
	 * Sets up the next free play round.
	 * @param round
	 */
	private void setUpFreePlayRound(int round){
		
		levelPoints = points;
		levelLives = lives;
		levelReady = false;
		
		gameState = STATE_FREE_PLAY;
		if(round==1){
			BALL_SPEED_MULTIPLIER = (int)config.getNumValue("BALL_SPEED_MULTIPLIER");
		}
		
		inFreePlay = true;
		
		shortPaddleCounter = 0;
		fastBallCounter = 0;
		confusionCounter = 0;
		if(round<=1){
			laserCounter = 0;
		}
		
		if(laserCounter<20){
			laserCounter = 20;
		}
		levelLaserCount = laserCounter;

		
		resetGameThread();
		
		info.setText("Round "+round);
		
		bricks.clear();
		balls.clear();
		perks.clear();
		lasers.clear();
		evilBalls.clear();
		
		Level curLevel = new Level("Round "+round);
		curLevel.setBackground(getFreePlayBackground(round));
		curLevel.setForeground(getFreePlayForeground(round));
		int numStartBricks = 10+round*2;
		double w = .1;
		double h = .06;
		for(int i=0; i<numStartBricks; i++){
			double x = rand.nextDouble();
			while(x>1-w){
				x = rand.nextDouble();
			}
			double y = rand.nextDouble();
			while(y>1-h-.2){
				y = rand.nextDouble();
			}
			Brick brick = new Brick(x, y, w, h);
			if(round/(100.0*rand.nextDouble())>=0.5){
				int damage = rand.nextInt((round/5)+1)+1;
				brick.setStrength(damage);
			}
			bricks.add(brick);
		}
		
		if(round>8){
			if(rand.nextInt(100/round)==0){
				//has bad rain.
				int period = rand.nextInt(10000)+25000/round;
				
				curLevel.setAttribute("PERK_RAIN_BAD", Integer.toString(period));
			}
		}
		
		if(round>14){
			if(rand.nextInt(100/round)==0){
				//has good rain.
				int period = rand.nextInt(10000)+25000/round;
				
				curLevel.setAttribute("PERK_RAIN_GOOD", Integer.toString(period));
			}
		}
		
		if(round>18){
			if(rand.nextInt(100/round)==0){
				//has evil balls.
				int period = rand.nextInt(30000)+25000/round;
				System.out.println(period);
				String color1 = " 255 0 0";
				String color2 = " 255 255 0";
				String switchTime = " 300";
				
				curLevel.setAttribute("EVIL_BALL_SPAWN", Integer.toString(period)+color1+color2+switchTime);
			}
		}
		
		minBrickY = 1;
		maxBrickY = 0;

		for (int i=0; i<bricks.size(); i++){
			Brick currBrick = bricks.get(i);
			if (currBrick.getY()<minBrickY){
				minBrickY = currBrick.getY();
			}
			if (currBrick.getY()>maxBrickY){
				maxBrickY = currBrick.getY();
			}
		}
		
		curLevel.setTopColor(new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
		curLevel.setBottomColor(new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));

		level = curLevel;
		// Color stuff with bricks
		Color begin = level.getTopColor();
		Color end = level.getBottomColor();
		beginColor[0] = begin.getRed();
		beginColor[1] = begin.getGreen();
		beginColor[2] = begin.getBlue();
		endColor[0] = end.getRed();
		endColor[1] = end.getGreen();
		endColor[2] = end.getBlue();

		// Ball code and initialization
		int ballR = (int)(.02*getWidth());
		if(ballR<14){ ballR=14; }
		ballMain = new Ball(getWidth() / 2, getHeight() / 2, ballR, ballR);
		ballMain.setYVelocity(BALL_SPEED_MULTIPLIER);
		ballMain.setXVelocity(((rand.nextDouble() * 2) - 1) * BALL_SPEED_MULTIPLIER);
		balls.add(ballMain);
		
		setBackground(level.getBackground());
		foregroundColor = level.getForeground();
		ballColor = foregroundColor;
		levelReady = true;
		gameThread = new GameThread();
	}
	
	private Color getFreePlayBackground(int round){
		round = round%50;
		if(round<6){
			return Color.white;
		} else if (round<11){
			return Color.black;
		} else if (round<16){
			return new Color(255, 50, 255);
		} else if (round<21){
			return new Color(255, 255, 0);
		} else if (round<26){
			return Color.black;
		} else if (round<31){
			return new Color(0, 150, 0);
		} else if (round<36){
			return Color.cyan;
		} else if (round<41){
			return Color.orange;
		} else if (round<46){
			return Color.blue;
		} else if (round<=49){
			return Color.red;
		}
		return Color.white;
	}
	
	private Color getFreePlayForeground(int round){
		round = round%50;
		
		if(round<6){
			return Color.black;
		} else if (round<11){
			return Color.green;
		} else if (round<16){
			return Color.black;
		} else if (round<21){
			return Color.black;
		} else if (round<26){
			return Color.red;
		} else if (round<31){
			return Color.white;
		} else if (round<36){
			return Color.black;
		} else if (round<41){
			return Color.blue;
		} else if (round<46){
			return Color.white;
		} else if (round<=49){
			return Color.black;
		}
		return Color.black;

	}
	/**
	 * Finishes the current level and sets up the next one. Similar to 
	 * the startGame() method.
	 * @throws InterruptedException
	 */
	private void doEndLevel() throws InterruptedException {
		
		//ends the current thread
		gameThread.setTerminate();
		
		//updates variables
		levelLives = lives;
		levelPoints = points;
		levelLaserCount = laserCounter;
		
		info.setText("Click to Start");

		//gets the next level information.
		reader.getNextLevel();
		currentLevel = level.getLevelName();
		level = reader.getLevel();
				
		minBrickY = 1;
		maxBrickY = 0;
		
		//sets up the next level bricks and balls. 
		bricks = level.getBricks();
		
		for (int i=0; i<bricks.size(); i++){
			Brick currBrick = bricks.get(i);
			if (currBrick.getY()<minBrickY){
				minBrickY = currBrick.getY();
			}
			if (currBrick.getY()>maxBrickY){
				maxBrickY = currBrick.getY();
			}
		}
		
		
		

		ballMain = new Ball(getWidth() / 2, getHeight() / 2, (int)(.02*getWidth()), (int)(.02*getWidth()));
		ballMain.setYVelocity(BALL_SPEED_MULTIPLIER);
		ballMain.setXVelocity(((rand.nextDouble() * 2) - 1) * BALL_SPEED_MULTIPLIER);
		balls.add(ballMain);
		
		//this insures there will be only one ball at the beginning of each level.
		while(balls.size()>1){
			balls.remove(0);
		}
		
		//Clears extras on screen. Each new life is a fresh start.
		perks.clear();
		lasers.clear();
		evilBalls.clear();
		
		//resets perk timers
		invincibleBallsCounter = 0;
		longPaddleCounter = 0;
		shortPaddleCounter = 0;
		fastBallCounter = 0;
		confusionCounter = 0;
		
		//Appropriately sizes the balls.
		for (int i = 0; i < balls.size(); i++) {
			Ball curBall = balls.get(i);
			if (2.0 * getWidth() / 100.0 >= 14) {
				curBall.setFrame(curBall.getX(), curBall.getY(), (2.0 * getWidth() / 100.0), (2.0 * getWidth() / 100.0));
			} else {
				break;
			}
		}
		
		//Gets the colors for the next level.
		Color begin = level.getTopColor();
		Color end = level.getBottomColor();
		beginColor[0] = begin.getRed();
		beginColor[1] = begin.getGreen();
		beginColor[2] = begin.getBlue();
		endColor[0] = end.getRed();
		endColor[1] = end.getGreen();
		endColor[2] = end.getBlue();
		setBackground(level.getBackground());
		foregroundColor = level.getForeground();
		ballColor = foregroundColor;
		
		//starts the thread again.
		gameThread = new GameThread();
		levelReady = true;
	}
	
	/**
	 * Resets the game thread. Sets game info to click to start
	 */
	private void resetGameThread(){
		if(gameThread!=null){
			if (gameThread.isAlive()){
				gameThread.setTerminate();
			}
		}
		if (lives<=0){
			gameState = STATE_MAIN;
			inFreePlay = false;
			setUpMain();
			repaint();
			try {
				Thread.sleep(1);
				repaint();
			} catch (InterruptedException e) {
				System.out.println("Thread sleep error.");
			}
		} else {
			if (balls.size()<=0){
				ballMain = new Ball(getWidth() / 2, getHeight() / 2, 10, 10);
				ballMain.setYVelocity(BALL_SPEED_MULTIPLIER);
				ballMain.setXVelocity(((rand.nextDouble() * 2) - 1) * BALL_SPEED_MULTIPLIER);
				balls.add(ballMain);
			}
			
			//Appropriately sizes the balls.
			for (int i = 0; i < balls.size(); i++) {
				Ball curBall = balls.get(i);
				if (2.0 * getWidth() / 100.0 >= 14) {
					curBall.setFrame(curBall.getX(), curBall.getY(), (2.0 * getWidth() / 100.0), (2.0 * getWidth() / 100.0));
				} else {
					break;
				}
			}
			gameThread = new GameThread();
		}
	}

	/**
	 * Paint method. Depends on gameState. Will paint the appropriate screen based
	 * on the gameState.
	 */
	public void paint(Graphics G) {
		Graphics2D g2 = (Graphics2D) G;
		
		//Playing screen.
		if (gameState.equals(STATE_PLAYING) || gameState.equals(STATE_FREE_PLAY) || gameState.equals(STATE_PAUSED)) {
			g2.clearRect(0, 0, getWidth(), getHeight());
			

			// Paints the bricks
			for (int i = 0; i < bricks.size(); i++) {
				Brick currBrick = bricks.get(i);
				
				int x = (int) (currBrick.getX() * getWidth());
				int y = (int) (currBrick.getY() * getHeight());
				int width = (int) (currBrick.getWidth() * getWidth());
				int height = (int) (currBrick.getHeight() * getHeight());
				double Y = currBrick.getY();
				Y = (Y-minBrickY)/(maxBrickY-minBrickY);
				if (currBrick.getHasSpecialColor()) {
					g2.setColor(currBrick.getColor());
				} else {
					int red = (int) (beginColor[0] + (((endColor[0] - beginColor[0]) * Y)));
					int green = (int) (beginColor[1] + (((endColor[1] - beginColor[1]) * Y)));
					int blue = (int) (beginColor[2] + (((endColor[2] - beginColor[2]) * Y)));
					g2.setColor(new Color(red, green, blue));
				}
				if(confusionCounter>0){
					x = x+(int)(.01*getWidth()*(Math.sin((Math.PI*((System.currentTimeMillis()-confusionStartTime)/1000.0)))));
				}
				RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, width, height, 8, 8);
				
				g2.fill(rect);
				double damage = currBrick.getPercentDamaged();
				if (damage > 0) {
					int index = (int) (Math.round(damage * 10));
					Image damageImage = damageImages[index-1];
					Image scaled = damageImage.getScaledInstance(width, height, Image.SCALE_FAST);
					g2.drawImage(scaled, x, y, this);
				}
			}
			
			// paints the perks
			for (int i=0; i<perks.size(); i++){
				Perk currPerk = perks.get(i);
				g2.setColor(currPerk.getColor());
				int rad = (int)(.01*getWidth());
				g2.fillOval(currPerk.getX(), currPerk.getY(), rad, rad);
			}
			
			//paints the active lasers
			
			for (int i=0; i<lasers.size(); i++){
				Laser currLaser = lasers.get(i);
				g2.setColor(currLaser.getColor());
				g2.fill(currLaser.getBounds());
				g2.setColor(level.getForeground());
				g2.draw(currLaser.getBounds());
			}
			
			g2.setColor(foregroundColor);
			
			if(inFreePlay){
				g2.drawString("Round "+Integer.toString(freePlayRound), 5, 15);

			} else {
				g2.drawString(currentLevel, 5, 15);

			}
			int offset = (int)((Math.log(points)));
			offset*=8;
			offset+=12;
			g2.drawString(Integer.toString(points), getWidth()-offset, 15);
			
			int heartSpace = lives*heartImage.getHeight(this) + (lives-1)*HEART_SPACING;
			int heartStart = (getHeight()-heartSpace)/2;
			for (int i=0; i<lives; i++){
				g2.drawImage(heartImage, 5, heartStart+i*(heartImage.getHeight(this)+HEART_SPACING), this);
			}
			
			g2.setColor(Color.blue);
			Rectangle laserIcon = new Rectangle(20, heartStart+(lives-1)*(heartImage.getHeight(this)+HEART_SPACING)+heartImage.getHeight(this)+10, 8, 20);
			g2.fill(laserIcon);
			g2.setColor(level.getForeground());
			g2.draw(laserIcon);
			g2.drawString(Integer.toString(laserCounter), 20, (int)(laserIcon.getY()+laserIcon.getHeight()+14));
			
			//draws the paddle
			g2.setColor(foregroundColor);
			paddle.setBounds((int) paddle.getX(), getHeight() - 30, getWidth() /10 , 10);
			if (longPaddleCounter>0){
				paddle.width = getWidth()/5;
			} else if (shortPaddleCounter>0){
				paddle.width = getWidth()/20;
			}
			g2.fill(paddle);
			
			// paints the balls #lol
			for (int i = 0; i < balls.size(); i++) {
				Ball currBall = balls.get(i);
				g2.setColor(ballColor);
				g2.fill(currBall.ball);
				if(invincibleBallsCounter>0){
					g2.setColor(foregroundColor);
					g2.draw(currBall.ball);
				} 
				currBall.setRelativeX(currBall.getX()/getWidth());
				currBall.setRelativeY(currBall.getY()/getHeight());
			}
			
			// paints the evil balls
			for (int i=0; i<evilBalls.size(); i++){
				EvilBall currBall = evilBalls.get(i);
				g2.setColor(currBall.getCurrentColor());
				g2.fill(currBall.ball);
				currBall.setRelativeX(currBall.getX()/getWidth());
				currBall.setRelativeY(currBall.getY()/getHeight());
			}
			
			// draws the info label
			g2.setColor(foregroundColor);
			if (info.getText().equals("Fire Ball")){
				g2.setColor(Color.red);
			}
			g2.setFont(info.getFont());
			g2.drawString(info.getText(), (getWidth()-info.getWidth())/2-80, getHeight()/2);
			
			// paints the pause screen if required
			if (gameState.equals(STATE_PAUSED)){
				int width = (int)(getWidth()*.8);
				int height = (int)(getHeight()*.8);
				Image scaled = pauseMenu.getScaledInstance(width, height, Image.SCALE_FAST);
				g2.drawImage(scaled, (getWidth()-scaled.getWidth(this))/2, (getHeight()-scaled.getHeight(this))/2, this);
				g2.drawImage(continueButton.getButtonImage(), continueButton.getX(), continueButton.getY(), this);
				g2.drawImage(mainButton.getButtonImage(), mainButton.getX(), mainButton.getY(), this);
				g2.drawImage(saveButton.getButtonImage(), saveButton.getX(), saveButton.getY(), this);
				g2.drawImage(loadButton.getButtonImage(), loadButton.getX(), loadButton.getY(), this);
			}
			
		} else if (gameState.equals(STATE_MAIN)) {
			g2.drawImage(playButton.getButtonImage(), playButton.getX(), playButton.getY(), this);
			g2.drawImage(quickPlayButton.getButtonImage(), quickPlayButton.getX(), quickPlayButton.getY(), this);
			g2.drawImage(loadButton.getButtonImage(), loadButton.getX(), loadButton.getY(), this);
			g2.drawImage(infoButton.getButtonImage(), infoButton.getX(), infoButton.getY(), this);
			g2.drawImage(exitButton.getButtonImage(), exitButton.getX(), exitButton.getY(), this);
		
		} else if (gameState.equals(STATE_INFO)) {
			if (infoScreenSelection==0){
				Image screen = informationScreens.get(0);
				g2.drawImage(screen, (int)((getWidth()-screen.getWidth(this))/2), (int)(getHeight()*.1), this);
			} else {
				Image screen = informationScreens.get(infoScreenSelection).getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
				g2.drawImage( screen, 0, 0, this);
			}
			if (infoScreenSelection==0){
				g2.drawImage(mainButton.getButtonImage(), mainButton.getX(), mainButton.getY(), this);
			} else {
				g2.drawImage(infoBackButton.getButtonImage(), infoBackButton.getX(), infoBackButton.getY(), this);
			}
			if (infoScreenSelection!=informationScreens.size()-1){
				g2.drawImage(infoNextButton.getButtonImage(), infoNextButton.getX(), infoNextButton.getY(), this);
			}
		}
	}

	/**
	 * Gets the damage image for blocks with damage. 
	 * @param index The index 1-10 of the damage. 1 is least damaged, 10 is about to break.
	 * @return Returns the image specified. 
	 */
	private Image getDamageImage(int index) {
		String sep = System.getProperty("file.separator");
		String dir = "resources"+sep+"breakImages"+sep+"break_"+Integer.toString(index)+".png";
		Image img = null;
		try {
			img = ImageIO.read(getClass().getClassLoader().getResource(dir));
		} catch (IOException e) {
			System.out.println("Error reading image file: ");
			e.printStackTrace();
		}
		
		return img;

	}
	
	/////////////// LISTENER METHODS ///////////////

	@Override
	public void componentHidden(ComponentEvent e) {

	}

	@Override
	public void componentMoved(ComponentEvent e) {

	}

	@Override
	public void componentResized(ComponentEvent e) {
		//resizes the buttons.
		continueButton.setX((getWidth()-continueButton.getWidth())/2);
		mainButton.setX((getWidth()-mainButton.getWidth())/2);
		saveButton.setX((getWidth()-saveButton.getWidth())/2);
		loadButton.setX((getWidth()-loadButton.getWidth())/2);
		playButton.setX((getWidth()-playButton.getWidth())/2);
		quickPlayButton.setX((getWidth()-quickPlayButton.getWidth())/2);
		infoButton.setX((getWidth()-infoButton.getWidth())/2);
		exitButton.setX((getWidth()-exitButton.getWidth())/2);
		
		continueButton.setY((int)(getHeight()*.4));
		playButton.setY(continueButton.getY());
		mainButton.setY(continueButton.getY()+continueButton.getHeight()+35);

		if (gameState.equals(STATE_PLAYING) || gameState.equals(STATE_FREE_PLAY) || gameState.equals(STATE_PAUSED)){
			//updates the balls
			for (int i = 0; i < balls.size(); i++) {
				Ball curBall = balls.get(i);
				double d = .02*getWidth();
				if (2.0 * d < 14) {
					d = 14;
				}
				curBall.setFrame(curBall.getRelativeX()*getWidth(), curBall.getRelativeY()*getHeight(), d, d);
			}
			
			//updates the evil balls
			for (int i=0; i<evilBalls.size(); i++){
				EvilBall curBall = evilBalls.get(i);
				double d = .02*getWidth();
				if (2.0 * d < 14) {
					d = 14;
				}
				curBall.setFrame(curBall.getRelativeX()*getWidth(), curBall.getRelativeY()*getHeight(), d, d);
			}
			
			for (int i=0; i<lasers.size(); i++){
				lasers.get(i).resize(getWidth(), getHeight());
			}
			
			if (gameState.equals(STATE_PAUSED)){
				saveButton.setY(mainButton.getY()+mainButton.getHeight()+35);
				loadButton.setY(saveButton.getY()+saveButton.getHeight()+35);
			}
		} else if (gameState.equals(STATE_MAIN)){
			quickPlayButton.setY(playButton.getY()+playButton.getHeight()+35);
			loadButton.setY(quickPlayButton.getY()+quickPlayButton.getHeight()+35);
			infoButton.setY(loadButton.getY()+loadButton.getHeight()+35);
			exitButton.setY(infoButton.getY()+infoButton.getHeight()+35);
		} else if (gameState.equals(STATE_INFO)){
			mainButton.setX(15);
			mainButton.setY(getHeight()-mainButton.getHeight()-20);
			infoBackButton.setX(15);
			infoBackButton.setY(getHeight()-infoBackButton.getHeight()-20);
			infoNextButton.setX(getWidth()-infoNextButton.getWidth()-15);
			infoNextButton.setY(getHeight()-infoNextButton.getHeight()-20);
			
		}
		repaint();
	}

	@Override
	public void componentShown(ComponentEvent e) {

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		Point2D mouse = new Point2D.Double(arg0.getX(), arg0.getY());
		if (gameState.equals(STATE_MAIN)){
			if (playButton.getBounds().contains(mouse)){
				playButton.setPressed(true);
			} else if (quickPlayButton.getBounds().contains(mouse)){
				quickPlayButton.setPressed(true);
			} else if (loadButton.getBounds().contains(mouse)){
				loadButton.setPressed(true);
			} else if (infoButton.getBounds().contains(mouse)){
				infoButton.setPressed(true);
			} else if (exitButton.getBounds().contains(mouse)){
				exitButton.setPressed(true);
			} else {
				
			}
		} else if (gameState.equals(STATE_PAUSED)){
			if (continueButton.getBounds().contains(mouse)){
				continueButton.setPressed(true);
			} else if (saveButton.getBounds().contains(mouse)){
				saveButton.setPressed(true);
			} else if (loadButton.getBounds().contains(mouse)){
				loadButton.setPressed(true);
			} else if (mainButton.getBounds().contains(mouse)){
				mainButton.setPressed(true);
			} else {
				
			}
		} else if (gameState.equals(STATE_INFO)){
			System.out.println(infoNextButton.getBounds());
			System.out.println(mouse);
			if (mainButton.getBounds().contains(mouse)){
				mainButton.setPressed(true);
			}
			if (infoBackButton.getBounds().contains(mouse)){
				infoBackButton.setPressed(true);
			} else if (infoNextButton.getBounds().contains(mouse)){
				infoNextButton.setPressed(true);
			}
			
		}
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		Point2D mouse = new Point2D.Double(arg0.getX(), arg0.getY());
		playButton.setPressed(false);
		quickPlayButton.setPressed(false);
		saveButton.setPressed(false);
		loadButton.setPressed(false);
		infoButton.setPressed(false);
		exitButton.setPressed(false);
		continueButton.setPressed(false);
		mainButton.setPressed(false);
		infoBackButton.setPressed(false);
		infoNextButton.setPressed(false);
		
		//continues game thread if necessary. - meaning does the 'click' in click to start.
		if (gameState.equals(STATE_PLAYING) || gameState.equals(STATE_FREE_PLAY)) {
			if (levelReady && !gameThread.isAlive()) {
				info.setText("");
				gameThread.start();
			}
		} else if (gameState.equals(STATE_MAIN)) {
			
			//play button action.
			if (playButton.getBounds().contains(mouse)){
				lives = 3;
				points = 0;
				invincibleBallsCounter = 0;
				shortPaddleCounter = 0;
				longPaddleCounter = 0;
				laserCounter = 0;
				fastBallCounter = 0;
				confusionCounter = 0;
				startGame();
				
			//quick play button action.
			} else if (quickPlayButton.getBounds().contains(mouse)){
				lives = 3;
				points = 0;
				invincibleBallsCounter = 0;
				shortPaddleCounter = 0;
				longPaddleCounter = 0;
				laserCounter = 0;
				fastBallCounter = 0;
				confusionCounter = 0;
				inFreePlay = true;
				freePlayRound = FREE_PLAY_START_ROUND;
				setUpFreePlayRound(freePlayRound);
			
			//info button action.
			} else if (infoButton.getBounds().contains(mouse)){
				gameState = STATE_INFO;
				mainButton.setX(15);
				mainButton.setY(getHeight()-mainButton.getHeight()-20);
				infoBackButton.setX(15);
				infoBackButton.setY(getHeight()-infoBackButton.getHeight()-20);
				infoNextButton.setX(getWidth()-infoNextButton.getWidth()-15);
				infoNextButton.setY(getHeight()-infoNextButton.getHeight()-20);
				
			//exit button action.
			} else if (exitButton.getBounds().contains(mouse)){
				System.exit(0);
				
			//load button action.
			} else if (loadButton.getBounds().contains(mouse)){
				LoadSaveFrame frame = new LoadSaveFrame(0, 0, getWidth(), getHeight(), LoadSaveFrame.LOAD);
				frame.setLocation((getWidth()-frame.getWidth())/2, (getHeight()-frame.getHeight())/2);
			}
		} else if (gameState.equals(STATE_PAUSED)){
			//continue button action.
			if (continueButton.getBounds().contains(mouse)){
				if(inFreePlay){
					gameState = STATE_FREE_PLAY;
					info.setText("Click to continue");
					resetGameThread();
				} else {
					gameState = STATE_PLAYING;
					info.setText("Click to continue");
					resetGameThread();
				}
			
			//main button action.
			} else if (mainButton.getBounds().contains(mouse)){
				gameState = STATE_MAIN;
				inFreePlay = false;
				playButton.setY((int)(getHeight()*.4));
				loadButton.setY(playButton.getY()+playButton.getHeight()+35);
				exitButton.setY(loadButton.getY()+loadButton.getHeight()+35);
				setUpMain();
				repaint();
			
			//save button action.
			} else if (saveButton.getBounds().contains(mouse)){
				LoadSaveFrame frame = new LoadSaveFrame(0, 0, getWidth(), getHeight(), LoadSaveFrame.SAVE);
				frame.setLocation((getWidth()-frame.getWidth())/2, (getHeight()-frame.getHeight())/2);
			//load button action.
			} else if (loadButton.getBounds().contains(mouse)){
				LoadSaveFrame frame = new LoadSaveFrame(0, 0, getWidth(), getHeight(), LoadSaveFrame.LOAD);
				frame.setLocation((getWidth()-frame.getWidth())/2, (getHeight()-frame.getHeight())/2);
			} 
		} else if (gameState.equals(STATE_INFO)){
			if (mainButton.getBounds().contains(mouse) && infoScreenSelection==0){
				gameState = STATE_MAIN;
			} else if (infoBackButton.getBounds().contains(mouse) && infoScreenSelection>0){
				infoScreenSelection--;
			} else if (infoNextButton.getBounds().contains(mouse) && infoScreenSelection<informationScreens.size()-1){
				infoScreenSelection++;
			}
		}
		repaint();
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {

	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		if (gameState.equals(STATE_PLAYING) || gameState.equals(STATE_FREE_PLAY)) {
			if (arg0.getX() + paddle.getWidth() / 2 > getWidth()) {
				paddle.setLocation((int) (getWidth() - paddle.getWidth()), (int) paddle.getY());
			} else if (arg0.getX() - paddle.getWidth() / 2 < 0) {
				paddle.setLocation(0, (int) paddle.getY());
			} else {
				paddle.setLocation((int) (arg0.getX() - paddle.getWidth() / 2), (int) paddle.getY());
			}
			repaint();
		}
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		int code = arg0.getKeyCode();

		if (gameState.equals(STATE_PLAYING) || gameState.equals(STATE_FREE_PLAY)){
			if (code == KeyEvent.VK_P || code == KeyEvent.VK_ESCAPE) {
				gameState = STATE_PAUSED;
				continueButton.setY((int)(getHeight()*.4));
				mainButton.setY(continueButton.getY()+continueButton.getHeight()+35);
				mainButton.setX((getWidth()-mainButton.getWidth())/2);
				saveButton.setY(mainButton.getY()+mainButton.getHeight()+35);
				loadButton.setY(saveButton.getY()+saveButton.getHeight()+35);
				repaint();
			//fires a laser
			} else if (code == KeyEvent.VK_SPACE && gameThread.isAlive()){
				if (laserCounter>0){
					laserCounter--;
					Laser newLaser = new Laser((int)paddle.getX(), (int)paddle.getY(), 1, 1);
					newLaser.resize(getWidth(), getHeight());
					newLaser.setX(paddle.getX()+(paddle.getWidth()-newLaser.getWidth())/2);
					newLaser.setY((int)(paddle.getY()-newLaser.getHeight()));
					lasers.add(newLaser);
				}
			} else if(code == KeyEvent.VK_C && gameThread.isAlive()){
				if(confusionCounter<=0){
					confusionStartTime = System.currentTimeMillis();
				}
				confusionCounter+=20000;
			}
		} else if (gameState.equals(STATE_PAUSED)){
			if (code == KeyEvent.VK_P || code == KeyEvent.VK_ESCAPE){
				if(inFreePlay){
					gameState = STATE_FREE_PLAY;
					info.setText("Click to continue");
					resetGameThread();
					repaint();
				} else {
					gameState = STATE_PLAYING;
					info.setText("Click to continue");
					resetGameThread();
					repaint();
				}
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {

	}

	/////////////// Game Thread ///////////////

	class GameThread extends Thread {

		public static final int MAX_PAUSE_TIME = 10;
		private boolean shouldExit = false;
		private int smallConfusionCounter = 0;
		private int ballConfusionCounter = 0;

		public void run() {

			long time = System.currentTimeMillis()-1;

			while (true) {

				//checks if the game is at the main screen. If it is, breaks out and ends the thread.
				if (gameState.equals(STATE_MAIN)) {
					setBackground(Color.blue);
					break;
				}
				
				//checks if there are no more balls in play. If so, removes a life and continues.
				if (balls.size()<=0){
					if(inFreePlay){
						lives--;
						points-=50*(Math.pow(LEVEL_MULTIPLIER, freePlayRound));
						info.setText("Click to start");
						invincibleBallsCounter = 0;
						shortPaddleCounter = 0;
						longPaddleCounter = 0;
						fastBallCounter = 0;
						confusionCounter = 0;
						resetGameThread();
					} else {
						lives--;
						points-=50*(Math.pow(LEVEL_MULTIPLIER, numLevels));
						info.setText("Click to start");
						invincibleBallsCounter = 0;
						shortPaddleCounter = 0;
						longPaddleCounter = 0;
						fastBallCounter = 0;
						confusionCounter = 0;
						resetGameThread();
					}
				}

				for (int i = 0; i < balls.size(); i++) {
					Ball currBall = balls.get(i);
					double multiplier = 1;
					if (fastBallCounter>0){
						multiplier = 2;
					}
					double newX = currBall.getX() + (currBall.getXVelocity()*multiplier / (double) (System.currentTimeMillis() - time));
					double newY = currBall.getY() + (currBall.getYVelocity()*multiplier / (double) (System.currentTimeMillis() - time));

					//Checks for collisions with the walls of the applet
					//left
					if (newX <= 0) {
						currBall.setXVelocity(Math.abs(currBall.getXVelocity()));
						newX = currBall.getX() + (currBall.getXVelocity()*multiplier / (System.currentTimeMillis() - time));
					//right
					} else if (newX + currBall.getWidth() >= getWidth()) {
						currBall.setXVelocity(Math.abs(currBall.getXVelocity())*-1);
						newX = currBall.getX() + (currBall.getXVelocity()*multiplier / (System.currentTimeMillis() - time));
					//up
					}
					if (newY <= 0) {
						currBall.setYVelocity(Math.abs(currBall.getYVelocity()));
						newY = currBall.getY() + (currBall.getYVelocity()*multiplier / (System.currentTimeMillis() - time));
					//down
					} else if (newY + currBall.getHeight() >= getHeight()) {
						if (!INVINCIBLE){
							balls.remove(i);
						} else {
							currBall.setYVelocity(Math.abs(currBall.getYVelocity())*-1);
							newY = currBall.getY() + (currBall.getYVelocity()*multiplier / (System.currentTimeMillis() - time));
						}
					}

					Rectangle ballRect = currBall.ball.getBounds();

					// Checks for collisions with the bricks
					for (int j = 0; j < bricks.size(); j++) {
						Brick currBrick = bricks.get(j);
						int x = (int) (currBrick.getX() * getWidth());
						int y = (int) (currBrick.getY() * getHeight());
						int width = (int) (currBrick.getWidth() * getWidth());
						int height = (int) (currBrick.getHeight() * getHeight());
						Rectangle rect = new Rectangle(x, y, width, height);
						if (ballRect.intersects(rect)) {
							points+=HIT_POINTS;
							currBrick.setStrength(currBrick.getStrength() - 1);
							if (currBrick.getStrength() <= 0) {
								bricks.remove(j);
								points+=DESTROY_POINTS-HIT_POINTS;
							}

							//Checks and sets new ball velocity for direction of impact
							//with the brick and the ball.
							if (invincibleBallsCounter<=0){
								double upDistance = Math.abs((y+height)-currBall.getY());
								double downDistance = Math.abs(y-(currBall.getY()+currBall.getHeight()));
								double leftDistance = Math.abs(x-(currBall.getX()+currBall.getWidth()));
								double rightDistance = Math.abs((x+width)-currBall.getX());
								if (upDistance < downDistance && upDistance<leftDistance && upDistance<rightDistance){
									currBall.setYVelocity(Math.abs(currBall.getYVelocity()));
									newY = y+height;
								} else if (downDistance < leftDistance && downDistance<rightDistance){
									currBall.setYVelocity(Math.abs(currBall.getYVelocity())*-1);
									newY = y-currBall.getHeight();
								} else if (leftDistance<rightDistance){
									currBall.setXVelocity(Math.abs(currBall.getXVelocity())*-1);
									newX = x-currBall.getWidth();
								} else {
									currBall.setXVelocity(Math.abs(currBall.getXVelocity()));
									newX = x+width;
								}
							}
							//code for creation of perks.
							if (rand.nextInt(PERK_SPAWN_CHANCE)==0 && currBrick.getStrength()<=0){
								int perkr = (int)(.01*getWidth());
								if (perkr<8){ perkr = 8; }
								if (rand.nextInt(2)==0){
									if (SPAWN_GOOD_PERKS){
										perks.add(new Perk(((width-perkr)/2)+x, ((height-perkr)/2)+y, 20, true));
									}
								} else {
									if (SPAWN_BAD_PERKS){
										perks.add(new Perk(((width-perkr)/2)+x, ((height-perkr)/2)+y, 20, false));
									}
								}
							}
						}
					}
					currBall.setFrame(newX, newY, currBall.getWidth(), currBall.getHeight());

					//paddle and ball intersection code
					if (ballRect.intersects(paddle)) {
						currBall.setFrame(currBall.getX(), paddle.getY()-currBall.getHeight(), currBall.getWidth(), currBall.getHeight());
						double vx = currBall.getXVelocity();
						double vy = currBall.getYVelocity();
						double percentOfPaddle = ((currBall.getX()+currBall.getWidth())-paddle.getX())/(paddle.getWidth()+currBall.getWidth());
						percentOfPaddle*=100;
						double angle = percentOfPaddle*(-9.0/10.0) + 135;
						double angleInRad = (angle*Math.PI)/180.0;
						double hyp = Math.sqrt(vx*vx+vy*vy);
						currBall.setXVelocity(hyp*Math.cos(angleInRad));
						currBall.setYVelocity(Math.abs(hyp*Math.sin(angleInRad))*-1);
					}
					
					//ball and evil ball collision code
					for(int j=0; j<evilBalls.size(); j++){
						EvilBall ball = evilBalls.get(j);
						if(ballRect.intersects(ball.ball.getBounds())){
							evilBalls.remove(j);
							if(inFreePlay){
								points+=50*(Math.pow(LEVEL_MULTIPLIER, freePlayRound));
							} else {
								points+=50*(Math.pow(LEVEL_MULTIPLIER, numLevels));
							}
							j--;
						}
					}
				}
				
				//Updates the evil balls
				for (int i=0; i<evilBalls.size(); i++){
					EvilBall currBall = evilBalls.get(i);
					currBall.update((int)(System.currentTimeMillis()-time));
				
					Rectangle ballRect = currBall.ball.getBounds();
					
					double newX = currBall.getX() + (currBall.getXVelocity() / (double) (System.currentTimeMillis() - time));
					double newY = currBall.getY() + (currBall.getYVelocity() / (double) (System.currentTimeMillis() - time));

					//Checks for collisions with the walls of the applet
					//left
					if (newX <= 0) {
						currBall.setXVelocity(Math.abs(currBall.getXVelocity()));
						newX = currBall.getX() + (currBall.getXVelocity() / (System.currentTimeMillis() - time));
					//right
					} else if (newX + currBall.getWidth() >= getWidth()) {
						currBall.setXVelocity(Math.abs(currBall.getXVelocity())*-1);
						newX = currBall.getX() + (currBall.getXVelocity() / (System.currentTimeMillis() - time));
					//up
					}
					if (newY <= 0) {
						currBall.setYVelocity(Math.abs(currBall.getYVelocity()));
						newY = currBall.getY() + (currBall.getYVelocity() / (System.currentTimeMillis() - time));
					//down
					} else if (newY + currBall.getHeight() >= getHeight()) {
						currBall.setYVelocity(Math.abs(currBall.getYVelocity())*-1);
						newY = currBall.getY() + (currBall.getYVelocity() / (System.currentTimeMillis() - time));
					}
					
					currBall.setFrame(newX, newY, currBall.getWidth(), currBall.getHeight());

					// Checks for collisions with the bricks
					for (int j = 0; j < bricks.size(); j++) {
						Brick currBrick = bricks.get(j);
						int x = (int) (currBrick.getX() * getWidth());
						int y = (int) (currBrick.getY() * getHeight());
						int width = (int) (currBrick.getWidth() * getWidth());
						int height = (int) (currBrick.getHeight() * getHeight());
						Rectangle rect = new Rectangle(x, y, width, height);
						if (ballRect.intersects(rect)) {
							if(currBrick.getPercentDamaged()>0){
								currBrick.setStrength(currBrick.getStrength()+1);
							}
						}
					}
					if(ballRect.intersects(paddle)){
						if(!INVINCIBLE){
							balls.clear();
							evilBalls.clear();
							lives--;
							points-=50*(Math.pow(LEVEL_MULTIPLIER, numLevels));
							info.setText("Click to start");
							invincibleBallsCounter = 0;
							shortPaddleCounter = 0;
							longPaddleCounter = 0;
							fastBallCounter = 0;
							confusionCounter = 0;
							resetGameThread();
						}
					}
				}
				
				//Updates the visible perks
				
				for (int i=0; i<perks.size(); i++){
					Perk currPerk = perks.get(i);
					int y = currPerk.getY();
					currPerk.setY((int)(y+currPerk.getVelocity()/(double)(System.currentTimeMillis()-time)));
					if (currPerk.getY()>getHeight()){
						perks.remove(i);
					} else {
						int rad = (int)(.01*getWidth());
						Rectangle rect = new Rectangle(currPerk.getX(), y, rad, rad);
						if (rect.intersects(paddle)){
							perks.remove(i);
							//good perks
							if (currPerk.isGood()){
								double next = rand.nextDouble();
								//five percent chance of extra life
								if (next<.05){
									lives++;
									info.setText("Extra Life");
								//five percent chance of lasers
								} else if (next<.1){
									laserCounter+=15;
									info.setText("Lasers");		//Lupe Fiasco lol
								//twenty percent chance of invincible balls
								} else if (next<.3){
									invincibleBallsCounter += 10000;
									ballColor = Color.red;
									info.setText("Fire Ball");	//not the whiskey you alcoholics
								//thirty percent chance of long paddle
								} else if (next<.6){
									shortPaddleCounter = 0;
									longPaddleCounter +=15000;
									info.setText("Long Paddle");//Long and narrow, good for hitting those pesky balls
								//Forty percent chance of extra balls
								} else {
									Ball newBall = new Ball(getWidth() / 2, getHeight() / 2, (int)(.02*getWidth()), (int)(.02*getWidth()));
									newBall.setYVelocity(BALL_SPEED_MULTIPLIER);
									newBall.setXVelocity(((rand.nextDouble() * 2) - 1) * BALL_SPEED_MULTIPLIER);
									balls.add(newBall);
									newBall = new Ball(getWidth() / 2, getHeight() / 2, (int)(.02*getWidth()), (int)(.02*getWidth()));									
									newBall.setYVelocity(BALL_SPEED_MULTIPLIER);
									newBall.setXVelocity(((rand.nextDouble() * 2) - 1) * BALL_SPEED_MULTIPLIER);
									balls.add(newBall);
									info.setText("Multi-Ball");
								}
							//bad perks
							} else {
								double next = rand.nextDouble();
								//thirty percent chance of short paddle
								if (next<=.3){
									longPaddleCounter = 0;
									shortPaddleCounter+=15000;
									info.setText("Short Paddle");//Having problems? Try brickbreakagra! Or just play better.
								//thirty percent chance of confusion
								} else if (next<=.6){
									if(confusionCounter<=0){
										confusionStartTime = System.currentTimeMillis();
									}
									confusionCounter+=10000;
									info.setText("Confusion");	//Super trippy
								//Forty percent chance of fast balls
								} else {
									fastBallCounter +=10000;
									info.setText("Fast Balls");	//Scarry indeed
								}
							}
							infoTimer = 2500;
						}
					}
				}
				
				//Updates the visible lasers
				for (int i=0; i<lasers.size(); i++){
					Laser currLaser = lasers.get(i);
					currLaser.setY((int)(currLaser.getY()-(currLaser.getVelocity()*(System.currentTimeMillis()-(double)time))));
					for (int j=0; j<bricks.size(); j++){
						Brick currBrick = bricks.get(j);
						int x = (int) (currBrick.getX() * getWidth());
						int y = (int) (currBrick.getY() * getHeight());
						int width = (int) (currBrick.getWidth() * getWidth());
						int height = (int) (currBrick.getHeight() * getHeight());
						Rectangle rect = new Rectangle(x, y, width, height);
						if (currLaser.getBounds().intersects(rect)){
							currBrick.setStrength(currBrick.getStrength()-1);
							if (currBrick.getStrength()<=0){
								bricks.remove(j);
							}
							if(i<=lasers.size()-1){
								lasers.remove(i);
							}
						}
					}
					if (currLaser.getY()+currLaser.getHeight()<0){
						lasers.remove(i);
					}
					for(int j=0; j<evilBalls.size(); j++){
						EvilBall ball = evilBalls.get(j);
						if(currLaser.getBounds().intersects(ball.ball.getBounds())){
							evilBalls.remove(j);
							if(inFreePlay){
								points+=50*(Math.pow(LEVEL_MULTIPLIER, freePlayRound));
							} else {
								points+=50*(Math.pow(LEVEL_MULTIPLIER, numLevels));
							}
							j--;
							lasers.remove(i);
							i--;
						}
					}
				}
				

				//checks for if there are no more bricks. If so, advances to 
				//the next level.
				if (bricks.size() <= 0) {
					levelReady = false;
					if(inFreePlay){
						points+=(int)(Math.round(Math.pow(LEVEL_MULTIPLIER, freePlayRound)*LEVEL_POINTS));
						freePlayRound++;
						if(freePlayRound%BALL_SPEED_STEP_POINT==0){
							BALL_SPEED_MULTIPLIER+=BALL_SPEED_STEP;
						}
						setUpFreePlayRound(freePlayRound);
					} else {
						points+=(int)(Math.round(Math.pow(LEVEL_MULTIPLIER, numLevels)*LEVEL_POINTS));
						numLevels++;
						if (numLevels%BALL_SPEED_STEP_POINT==0){
							BALL_SPEED_MULTIPLIER+=BALL_SPEED_STEP;
						}
						try {
							doEndLevel();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
				}
				
				//Updates the level attributes
				
				//good perk spawn
				int goodSpawn = level.spawnPerk(true, (int)(System.currentTimeMillis()-time));
				for (int i=0; i<goodSpawn; i++){
					int perkr = (int)(.01*getWidth());
					if (perkr<8){ perkr = 8; }
					int xPos = (int)(rand.nextDouble()*getWidth());
					if (xPos>getWidth()-perkr){xPos = getWidth()-perkr;}
					perks.add(new Perk(xPos, 0, 20, true));
				}
				
				//bad perk spawn
				int badSpawn = level.spawnPerk(false, (int)(System.currentTimeMillis()-time));
				for (int i=0; i<badSpawn; i++){
					int perkr = (int)(.01*getWidth());
					if (perkr<8){ perkr = 8; }
					int xPos = (int)(rand.nextDouble()*getWidth());
					if (xPos>getWidth()-perkr){xPos = getWidth()-perkr;}
					perks.add(new Perk(xPos, 0, 20, false));
				}
				
				int evilSpawn = level.spawnEvilBall((int)(System.currentTimeMillis()-time));
				for(int i=0; i<evilSpawn; i++){
					int r = (int)(.02*getWidth());
					if(r<14){ r=14;}
					int x = (int)(rand.nextDouble()*getWidth());
					if(x>getWidth()-r){x = getWidth()-r;}
					EvilBall newEvilBall = level.getEvilBall(x, 0, r);
					newEvilBall.setYVelocity(BALL_SPEED_MULTIPLIER);
					newEvilBall.setXVelocity(((rand.nextDouble() * 2) - 1) * BALL_SPEED_MULTIPLIER);
					evilBalls.add(newEvilBall);
				}
				
				//Updates the perk timers
				if (longPaddleCounter>0){
					longPaddleCounter-=(System.currentTimeMillis()-time);
				} else if (shortPaddleCounter>0){
					shortPaddleCounter-=(System.currentTimeMillis()-time);
				}
				
				if (invincibleBallsCounter>0){
					invincibleBallsCounter-=(System.currentTimeMillis()-time);
				} else {
					if (confusionCounter<=0){
						ballColor = foregroundColor;
					}
				}
				
				if (fastBallCounter>0){
					fastBallCounter-=(System.currentTimeMillis()-time);
				}
				
				//confusion color update
				if (confusionCounter>0){
					boolean ballColorDif = false;
					if(ballConfusionCounter>0){
						ballColorDif = false;	
						ballConfusionCounter-=(System.currentTimeMillis()-time);
					} else {
						ballConfusionCounter-=(System.currentTimeMillis()-time);
						if(ballConfusionCounter<=-CONFUSION_SHOWING_TIME){
							ballConfusionCounter=  CONFUSION_HIDDEN_TIME;
						}
						ballColorDif = true;
					}
					if (smallConfusionCounter>0){
						smallConfusionCounter-=(System.currentTimeMillis()-time);
					} else {
						smallConfusionCounter = 200;
						Color clr = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
						setBackground(clr);
						if (ballColorDif){
							ballColor = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
						} else {
							ballColor = clr;
						}
						
					}
					confusionCounter-=(System.currentTimeMillis()-time);
				} else {
					smallConfusionCounter = 0;
					if (gameState.equals(STATE_MAIN)){
						setBackground(Color.blue);
					} else {
						setBackground(level.getBackground());
					}
					if (invincibleBallsCounter<=0){
						ballColor = foregroundColor;
					} else {
						ballColor = Color.red;
					}
				}
				
				//Label timer
				if (infoTimer>0){
					infoTimer-=(System.currentTimeMillis()-time);
				} else if (infoTimer!=-1000){
					infoTimer = -1000;
					info.setText("");
				}
				
				
				
				//If this thread should break.
				if (shouldExit) {
					break;
				}
				
				time = System.currentTimeMillis();
				
				//loops while the game is paused.
				while(gameState.equals(STATE_PAUSED)){
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						System.out.println("Sleep interupted in gameThread.");
					}
				}
				
				try {
					Thread.sleep(MAX_PAUSE_TIME);
				} catch (InterruptedException e) {
					System.out.println("Sleep interupted in gameThread.");
				}
				

				repaint();
				
			}
			//code executes once the thread escapes from its loop
			
		}
		public void setTerminate() {
			shouldExit = true;
		}
	}
	
	/////////////// Load/Save Frame ///////////////
	
	class LoadSaveFrame extends JFrame {
		
		private static final long serialVersionUID = 1L;
		
		private JLabel label = new JLabel();
		public static final String LOAD = "load";
		public static final String SAVE = "save";
		private String State;
		private JPanel buttonPanel = new JPanel();
		private JPanel backButtonPanel = new JPanel();
		private JPanel deleteButtonPanel = new JPanel();
		LoadSaveFrame frameRef = this;
		
		public LoadSaveFrame (int x, int y, int width, int height, String state){
			super();
			State = state;
			setLocation(x, y);
			setSize(width, height);
			setBackground(Color.blue);
			GridLayout layout = new GridLayout(5, 1);
			layout.setHgap(1);
			layout.setVgap(10);
			setLayout(layout);
			
			//adds the label
			if (state.equalsIgnoreCase(LOAD)){
				label.setText("load");
				JPanel panel = new JPanel();
				panel.add(label);
				add(panel);
			} else if (state.equalsIgnoreCase(SAVE)) {
				label.setText("save");
				JPanel panel = new JPanel();
				panel.add(label);
				add(panel);
			}
			
			// adds the text field or file picker
			if (state.equalsIgnoreCase(LOAD)){
				JPanel panel = new JPanel();
				loadFilePicker = new JComboBox(saveLoader.getAllSaves());
				panel.add(loadFilePicker);
				add(panel);
			} else if (state.equalsIgnoreCase(SAVE)) {
				JPanel panel = new JPanel();
				panel.add(saveField);
				add(panel);
			}
			
			//adds the save or load buttons and back button
			add(buttonPanel);
			if (state.equalsIgnoreCase(LOAD)){
				add(deleteButtonPanel);
				deleteButtonPanel.addMouseListener(new DeleteButtonListener());
			}
			add(backButtonPanel);
			buttonPanel.addMouseListener(new LoadSaveButtonListener());
			backButtonPanel.addMouseListener(new BackListener());
			pack();
			setVisible(true);
			validate();
			repaint();
		}
		
		public void paint(Graphics G){
			Graphics gp = buttonPanel.getGraphics();
			Graphics2D g2 = (Graphics2D)gp;
			if (State.equalsIgnoreCase(LOAD)){
				g2.drawImage(loadButton.getButtonImage(), (buttonPanel.getWidth()-loadButton.getWidth())/2, 0, buttonPanel);
			} else if (State.equalsIgnoreCase(SAVE)){
				g2.drawImage(saveButton.getButtonImage(), (buttonPanel.getWidth()-saveButton.getWidth())/2, 0, buttonPanel);
			}
			gp = backButtonPanel.getGraphics();
			g2 = (Graphics2D)gp;
			g2.drawImage(backButton.getButtonImage(), (backButtonPanel.getWidth()-backButton.getWidth())/2, 0, backButtonPanel);
			gp = deleteButtonPanel.getGraphics();
			g2 = (Graphics2D)gp;
			g2.drawImage(deleteButton.getButtonImage(), (deleteButtonPanel.getWidth()-deleteButton.getWidth())/2, 0, deleteButtonPanel);
			validate();
		}
		
		 class LoadSaveButtonListener implements MouseListener{

			@Override
			public void mouseClicked(MouseEvent e) {
				
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				Point2D mouse = new Point2D.Double(e.getX(), e.getY());
				if (State.equalsIgnoreCase(LOAD)){
					Rectangle loadBox = new Rectangle((buttonPanel.getWidth()-loadButton.getWidth())/2, 0, loadButton.getWidth(), loadButton.getHeight());
					if (loadBox.contains(mouse)){
						Object selection = loadFilePicker.getSelectedItem();
						String fileName = ((SaveFile)selection).getFileName();
						SaveFile currentSave = saveLoader.getSave(fileName);
						invincibleBallsCounter = 0;
						shortPaddleCounter = 0;
						longPaddleCounter = 0;
						fastBallCounter = 0;
						confusionCounter = 0;
						if(currentSave.inNormalPlay()){
							currentLevel = currentSave.getLevel();
							numLevels = currentSave.getNumLevels();
							lives = currentSave.getLives();
							points = currentSave.getPoints();
							laserCounter = currentSave.getLasers();
							levelLives = lives;
							levelPoints = points;
							levelLaserCount = laserCounter;
							BALL_SPEED_MULTIPLIER+=(BALL_SPEED_STEP*(BALL_SPEED_STEP_POINT/numLevels));
							frameRef.dispose();
							startGame();
						} else {
							gameState = STATE_FREE_PLAY;
							freePlayRound = currentSave.getRound();
							lives = currentSave.getLives();
							points = currentSave.getPoints();
							laserCounter = currentSave.getLasers();
							inFreePlay = true;
							levelLives = lives;
							levelPoints = points;
							levelLaserCount = laserCounter;
							frameRef.dispose();
							setUpFreePlayRound(freePlayRound);
						}
					}
				} else if (State.equalsIgnoreCase(SAVE)){
					Rectangle saveBox = new Rectangle((buttonPanel.getWidth()-saveButton.getWidth())/2, 0, saveButton.getWidth(), saveButton.getHeight());
					if (saveBox.contains(mouse)){
						String fileName = saveField.getText().toString();
						if(inFreePlay){
							saveLoader.saveFreePlay(fileName, Round, levelLives, levelPoints, levelLaserCount);
						} else {
							saveLoader.SaveFile(fileName, currentLevel, levelLives, levelPoints, levelLaserCount, numLevels);
						}
						frameRef.dispose();
					}
				}
			}
			 
		 }
		 
		 class BackListener implements MouseListener {
			 @Override
			 public void mouseReleased(MouseEvent e){
				 Point2D mouse = new Point2D.Double(e.getX(), e.getY());
				 Rectangle backBox = new Rectangle((backButtonPanel.getWidth()-backButton.getWidth())/2, 0, backButton.getWidth(), backButton.getHeight());
				 if (backBox.contains(mouse)){
						frameRef.dispose();
				 }
			 }

			@Override
			public void mouseClicked(MouseEvent arg0) {
				
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				
			}
		 }
		 
		 class DeleteButtonListener implements MouseListener {
			 
			@Override
			public void mouseReleased(MouseEvent e) {
				 Point2D mouse = new Point2D.Double(e.getX(), e.getY());
				 Rectangle deleteBox = new Rectangle((deleteButtonPanel.getWidth()-deleteButton.getWidth())/2, 0, deleteButton.getWidth(), deleteButton.getHeight());
				 if (deleteBox.contains(mouse)){
					 Object selection = loadFilePicker.getSelectedItem();
					String fileName = ((SaveFile)selection).getFileName();
					 saveLoader.deleateSave(fileName);
					 if (selection!=null){
						 loadFilePicker.removeItem(selection);
					 }
				 }
			}
			 
			@Override
			public void mouseClicked(MouseEvent arg0) {
				
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				
			}

		 }
	}
}