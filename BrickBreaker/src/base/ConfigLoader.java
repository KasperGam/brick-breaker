package base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConfigLoader {

	public static final String CONFIG_FILE = "Config.config";
	
	private static int ballSpeed = 30;
	private static int ballSpeedStep = 2;
	private static int ballSpeedStepPoint = 5;
	private static int hitPoints = 1;
	private static int destroyPoints = 5;
	private static int levelPoints = 100;
	private static double levelMultiplier = 1.08;
	private static boolean spawnGood = true;
	private static boolean spawnBad = true;
	private static int spawnChance = 15;
	private static boolean invincible = false;
	private static String startLevel = "Level001";
	
	public ConfigLoader(){
		InputStream in = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE);
		BufferedReader levelReader = new BufferedReader(new InputStreamReader(in));
		
		while(true){
			String input = getNextLine(levelReader);
			if (input==null){
				break;
			}
			String[] data = input.split("=");
			for (int i=0; i<data.length; i++){
				data[i] = data[i].trim();
			}
			String var = data[0];
			String val = data[1];
			if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")){
				Boolean value = val.equalsIgnoreCase("true");
				setBooleanValue(var, value);
			} else if (var.equals("STARTING_LEVEL")) {
				startLevel = val;
			} else {
				double value = Double.parseDouble(val);
				setNumValue(var, value);
			}
		}
		
	}
	
	/**
	 * Sets boolean values.
	 * @param var
	 * @param val
	 */
	private static void setBooleanValue(String var, boolean val){
		if (var.equals("SPAWN_GOOD_PERKS")){
			spawnGood = val;
		} else if (var.equals("SPAWN_BAD_PERKS")){
			spawnBad = val;
		} else if (var.equals("INVINCIBLE")){
			invincible = val;
		}
	}
	
	/**
	 * Sets numerical values.
	 * @param var
	 * @param val
	 */
	private static void setNumValue(String var, double val){
		if (var.equals("BALL_SPEED_MULTIPLIER")){
			ballSpeed = (int)val;
		} else if (var.equals("HIT_POINTS")){
			hitPoints = (int)val;
		} else if (var.equals("DESTROY_POINTS")){
			destroyPoints = (int)val;
		} else if (var.equals("LEVEL_POINTS")){
			levelPoints = (int)val;
		} else if (var.equals("LEVEL_MULTIPLIER")){
			levelMultiplier = val;
		} else if (var.equals("PERK_SPAWN_CHANCE")){
			spawnChance = (int) val;
		} else if (var.equals("BALL_SPEED_STEP")){
			ballSpeedStep = (int)val;
		} else if (var.equals("BALL_SPEED_STEP_POINT")){
			ballSpeedStepPoint = (int)val;
		}
				
	}
	
	/**
	 * Returns the value of the specified variable. 
	 * @param configVarName
	 * @return The boolean value for the specified variable. Returns false if no variable name was found.
	 */
	public boolean getBoolValue(String configVarName){
		if (configVarName.equals("SPAWN_GOOD_PERKS")){
			return spawnGood;
		} else if (configVarName.equals("SPAWN_BAD_PERKS")){
			return spawnBad;
		} else if (configVarName.equals("INVINCIBLE")){
			return invincible;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns the value of the specified variable.
	 * @param configVarName
	 * @return The double value of the specified variable. Returns -1 if no variable name was found.
	 */
	public double getNumValue(String configVarName){
		if (configVarName.equals("BALL_SPEED_MULTIPLIER")){
			return ballSpeed;
		} else if (configVarName.equals("HIT_POINTS")){
			return hitPoints;
		} else if (configVarName.equals("DESTROY_POINTS")){
			return destroyPoints;
		} else if (configVarName.equals("LEVEL_POINTS")){
			return levelPoints;
		} else if (configVarName.equals("LEVEL_MULTIPLIER")){
			return levelMultiplier;
		} else if (configVarName.equals("PERK_SPAWN_CHANCE")){
			return spawnChance;
		} else if (configVarName.equals("BALL_SPEED_STEP")){
			return ballSpeedStep;
		} else if (configVarName.equals("BALL_SPEED_STEP_POINT")){
			return ballSpeedStepPoint;
		} else{
			return -1;
		}
	}
	
	/**
	 * Gets the specified starting level.
	 * @return The file name for the startring level.
	 */
	public String getStartLevel(){
		return startLevel;
	}
	
	
	/**
     * Gets the next valid line (does not return comments or blank lines);
     * @return
     */
    private static String getNextLine(BufferedReader reader){
        while(true){
            try {
            	if (!reader.ready()){
            		return null;
            	}
                String line = reader.readLine();
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
                System.out.println("Error: Level format exeption: "+CONFIG_FILE);
                e.printStackTrace();
                return null;
            }
        }
    }
	
}
