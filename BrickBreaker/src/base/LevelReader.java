package base;
 
import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
 
public class LevelReader {
 
	private Level currentLevel;
    private String levelName;
    private BufferedReader levelReader;
         
    int rows;
    int columns;
         
// Constructor //
     
    /**
     * Creates a new LevelReader that will read the level files.
     * @param startLevel
     */
    public LevelReader(String levelFileName){
    	
        levelName = levelFileName;
        currentLevel = new Level(levelName);
        String sep = System.getProperty("file.separator");
        InputStream in = getClass().getClassLoader().getResourceAsStream("levels"+sep+levelFileName);
		levelReader = new BufferedReader(new InputStreamReader(in));
         
		try {
			while(levelReader.ready()){
				handleInput(getNextLine());
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

    }
    
    public void getNextLevel(){
    	levelName = currentLevel.getNext();
        currentLevel.reset(levelName);
    	String sep = System.getProperty("file.separator");
    	InputStream in = getClass().getClassLoader().getResourceAsStream("levels"+sep+levelName);
		levelReader = new BufferedReader(new InputStreamReader(in));
         
		try {
			while(levelReader.ready()){
				handleInput(getNextLine());
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
    }
     
// Accessory methods //
    
    public Level getLevel(){
    	return currentLevel;
    }
     
// Helper methods //
     
    /**
     * Handles the input given. Mostly delegates to other methods.
     * @param line The line of input.
     */
    private void handleInput(String line){
    	if (line==null){
    		return;
    	}
    	if (line.startsWith("*")){
    		ArrayList<String> args = new ArrayList<String>();
    		while(!line.startsWith("*END")){
    			args.add(line);
    			line = getNextLine();
    		}
    		handleBlock(args.toArray(new String[args.size()]));
    	} else {
    		String[] args = line.split("=");
    		for (int i=0; i<args.length; i++){
    			args[i] = args[i].trim();
    		}
    		if (args.length!=2){
    			return;
    		} else {
    			handleStatement(args[0], args[1]);
    		}
    	}
    }
    
    /**
     * Handles assignment statements (ex: PROPERTY = VALUE).
     * @param variable The property of the level.
     * @param value The value of that property.
     */
    private void handleStatement(String variable, String value){
    	variable = variable.toUpperCase();
    	if (variable.equals("TOP_COLOR")){
    		currentLevel.setTopColor(getColor(value));
    	} else if (variable.equals("BOTTOM_COLOR")){
    		currentLevel.setBottomColor(getColor(value));
    	} else if (variable.equals("BACKGROUND_COLOR")){
    		currentLevel.setBackground(getColor(value));
    	} else if (variable.equals("FOREGROUND_COLOR")){
    		currentLevel.setForeground(getColor(value));
    	} else if (variable.equals("NEXT_LEVEL")){
    		currentLevel.setNext(value);
    	} else if (variable.equals("BASE_BLOCK_STRENGTH")){
    		currentLevel.setBaseBlockStrength(Integer.parseInt(value));
    	}
    }
    
    /**
     * Handles group statements. These are statements defining
     * characteristics that take up multiple lines of the level
     * file. They are started with '*'s surrounding the type of
     * group statement. They are ended with the same statement 
     * with the string END: before the group type. The group types
     * are BLOCK so far. 
     * @param args An array of all the lines of the block.
     */
    private void handleBlock(String[] args){
    	String type = args[0].toUpperCase();
    	type = type.trim().substring(1, type.length()-1);
    	if (type.equals("BLOCKS")){
    		if (args[1].split("=").length<2){
    			return;
    		}
    		String struct = args[1].split("=")[1].toUpperCase().trim();
    		if (struct.equals("GRID")){
    			setUpGrid(args[2]);
    			for (int i=3; i<args.length; i++){
    				editBrick(args[i]);
    			}
    		} else {
    			for (int i=3; i<args.length; i++){
    				currentLevel.addBrick(getBrick(args[i]));
        		}
    		}
    	} else if (type.equals("ATTRIBUTES")) {
    		for(int i=1; i<args.length; i++){
    			String[] val=  args[i].split("=");
    			if (val.length>=2){
        			currentLevel.setAttribute(val[0].trim().toUpperCase(), val[1].trim());
    			}
    		}
    	}
    }
     
    /**
     * Gets the next valid line (does not return comments or blank lines);
     * @return
     */
    private String getNextLine(){
        while(true){
            try {
            	if (!levelReader.ready()){
            		return null;
            	}
                String line = levelReader.readLine();
                if (line==null){
                    return null;
                }
                line = line.trim();
                if (line.length()!=0){
                    if (line.length()>1){
                        if (line.substring(0,2).equals("//")){
                            //nothing
                        } else {
                            return line;
                        }
                    } else {
                    	return line;
                    }
                    //nothing
                }
            } catch (IOException e) {
                System.out.println("Error: Level format exeption: "+levelName);
                e.printStackTrace();
                return null;
            }
        }
    }
     
    /**
     * Gets the color from a specified line.
     * @param line The line containing at least three int values, the red, green, and blue.
     * @return Returns the color specified by the line. If the line does not contain
     * three values, will return black.
     */
    private Color getColor(String line){
        String clr1 = line;
        String rgb[] = clr1.split(" ");
        if (rgb.length<3){
        	return Color.black;
        }
        int red = 0;
        int green = 0;
        int blue = 0;
        try{
        	red = Integer.parseInt(rgb[0]);
        	green = Integer.parseInt(rgb[1]);
        	blue = Integer.parseInt(rgb[2]);
        } catch(Exception e){
        	System.out.println("Color format error: "+line);
        	System.out.println("Returning Black.");
        	return Color.black;
        }
        if (red<0) red = 0;
        if (green<0) green = 0;
        if (blue<0) blue = 0;
        if (red>255) red = 255;
        if (green>255) green = 255;
        if (blue>255) blue = 255;
        return new Color(red, green, blue);
    }
     
    /**
     * Gets the brick specified by the line provided. Assumes correct line format.
     * @param line The line specifying the brick.
     * @return The new brick. Returns null if not properly specified by the 
     * line input.
     */
    private Brick getBrick(String line){
        String nums[] = line.split(" ");
        if (nums.length<4){
        	System.out.println(line);
        	return null;
        }
        double x = Double.parseDouble(nums[0]);
        double y = Double.parseDouble(nums[1]);
        double w = Double.parseDouble(nums[2]);
        double h = Double.parseDouble(nums[3]);
        Brick currBrick = new Brick(x,y,w,h);
         currBrick.setStrength(currentLevel.getBaseBlockStrength());
        if (nums.length>4){
            currBrick.setStrength(Integer.parseInt(nums[4]));
        }
        if (nums.length>5){
            currBrick.setSpecialColor(true);
            int red = Integer.parseInt(nums[5]);
            int green = Integer.parseInt(nums[6]);
            int blue = Integer.parseInt(nums[7]);
            currBrick.setColor(new Color(red, green, blue));
        }
        return currBrick;
    }
    
    /**
     * Sets up the bricks ArrayList as a grid of bricks specified in the level data.
     * @param line
     */
    private void setUpGrid(String line){
    	String[] data = line.split(" ");
    	double[] nums = new double[data.length];
    	for (int i=0; i<data.length; i++){
    		nums[i] = Double.parseDouble(data[i]);
    	}
    	int column = (int) nums[0];
    	int row = (int) nums[1];
    	double height = nums[2];
    	double spacing = nums[3];
    	double offset = nums[4];
    	double width = (1-((column+1)*spacing))/column;
    	
    	rows = row;
    	columns = column;
    	for (int i=0; i<row; i++){
    		double y = offset+i*(height+spacing);
    		for (int j=0; j<column; j++){
    			double x = spacing+j*(width+spacing);
    			Brick brick = new Brick(x, y, width, height);
    			brick.setStrength(currentLevel.getBaseBlockStrength());
    			currentLevel.addBrick(brick);
    		}
    	}
    }
    
    /**
     * Edits the brick denoted in the line. The line
     * needs to contain the column, row, health, and 
     * can possibly contain a specific color in red,
     * green, blue values. All separated by spaces.
     * @param line The line specifying how to edit the brick.
     */
    private void editBrick(String line){
    	String[] data = line.split(" ");
    	int[] nums = new int[data.length];
    	for (int i=0; i<data.length; i++){
    		nums[i] = Integer.parseInt(data[i]);
    	}
    	
    	int column = nums[0];
    	int row = nums[1];
    	int newStrength = nums[2];
    	int index = (columns)*(row-1);
    	index+=column-1;
    	Brick currBrick = currentLevel.getBrick(index);
    	currBrick.setStrength(newStrength);
    	if (nums.length>3){
    		currBrick.setSpecialColor(true);
    		int red = nums[3];
    		int green = nums[4];
    		int blue = nums[5];
    		currBrick.setColor(new Color(red, green, blue));
    	}
    }
}

