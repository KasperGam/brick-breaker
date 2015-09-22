package base;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.io.PrintWriter;
import java.util.ArrayList;

public class SaveFileLoader {

	ArrayList<SaveFile> saves = new ArrayList<SaveFile>();

	SaveFile currentSave = null;
	File saveDir;
	String separator = System.getProperty("file.separator");
	
	/**
	 * Constructor. Creates the save directory in the Desktop if not already there. Also gets
	 * all past saves.
	 */
	public SaveFileLoader(){
		saveDir = new File(System.getProperty("user.home")+separator+"Desktop", "saves"+separator);
		if (!saveDir.exists() && !saveDir.mkdirs()){
			throw new RuntimeException("Could not create save directory. ");
		}
		for (File save: saveDir.listFiles()){
			if (!save.getName().equals(".DS_Store")){
				saves.add(generateSave(save.getName()));
			}
		}
	}
	
	/**
	 * Saves the specified file, assumes normal play mode.
	 * @param fileName The name of the file.
	 * @param level The level name.
	 * @param lives The beginning level number of lives.
	 * @param points The beginning level point number.
	 * @param lasers The beginning level laser count.
	 */
	public void SaveFile(String fileName, String level, int lives, int points, int lasers, int numLevels){
		File file = new File(saveDir, fileName);
		if (!file.exists()){
			SaveFile save = new SaveFile(fileName, false);
			save.setLevel(level);
			save.setPoints(points);
			save.setLasers(lasers);
			save.setNumLevels(numLevels);
			
			System.out.println("Creating new Save File: "+fileName);
			saves.add(save);
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.out.println("Error creating save: "+file.getPath());
				e.printStackTrace();
			}
		} else {
			System.out.println("File exists: "+file.getPath());
			System.out.println("Overriding...");
		}
		
		for(int i=0; i<saves.size(); i++){
			if(saves.get(i).getFileName().equals(fileName)){
				saves.remove(i);
				break;
			}
		}
		SaveFile save = new SaveFile(fileName, false);
		save.setLevel(level);
		save.setPoints(points);
		save.setLasers(lasers);
		save.setNumLevels(numLevels);
		saves.add(save);
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		} catch (IOException e) {
			System.out.println("error creating Print Writer: "+file.getPath());
			e.printStackTrace();
		}
		writer.println("false");
		writer.println(level);
		writer.println(lives);
		writer.println(points);
		writer.println(lasers);
		writer.println(numLevels);
		
		writer.close();
		
	}
	
	/**
	 * Saves the specified file, assumes free play mode.
	 * @param fileName The name of the file.
	 * @param round The current round.
	 * @param lives The beginning level number of lives.
	 * @param points The beginning level point number.
	 * @param lasers The beginning level laser count.
	 */
	public void saveFreePlay(String fileName, int round, int lives, int points, int lasers){
		File file = new File(saveDir, fileName);
		if (!file.exists()){
			SaveFile save = new SaveFile(fileName, true);
			save.setRound(round);
			save.setPoints(points);
			save.setLasers(lasers);
			
			System.out.println("Creating new Save File: "+fileName);
			saves.add(save);
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.out.println("Error creating save: "+file.getPath());
				e.printStackTrace();
			}
		} else {
			System.out.println("File exists: "+file.getPath());
			System.out.println("Overriding...");
		}
		
		for(int i=0; i<saves.size(); i++){
			if(saves.get(i).getFileName().equals(fileName)){
				saves.remove(i);
				break;
			}
		}
		SaveFile save = new SaveFile(fileName, false);
		save.setRound(round);
		save.setPoints(points);
		save.setLasers(lasers);
		saves.add(save);
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		} catch (IOException e) {
			System.out.println("error creating Print Writer: "+file.getPath());
			e.printStackTrace();
		}
		writer.println("true");
		writer.println(round);
		writer.println(lives);
		writer.println(points);
		writer.println(lasers);
		writer.println(0);
		
		writer.close();
		
	}
	
	/**
	 * Sets the statistics to the specified file. 
	 * @param fileName The name of the save file.
	 */
	public void setSave(String fileName){
		File file = new File(saveDir, fileName);
		System.out.println("Trying to read save file: "+fileName);
		if (file.exists()){
			for(int i=0; i<saves.size(); i++){
				if(saves.get(i).getFileName().equals(fileName)){
					currentSave = saves.get(i);
					break;
				}
			}
		}
	}
	
	public SaveFile getSave(String fileName){
		File file = new File(saveDir, fileName);
		System.out.println("Trying to read save file: "+fileName);
		if (file.exists()){
			for(int i=0; i<saves.size(); i++){
				if(saves.get(i).getFileName().equals(fileName)){
					currentSave = saves.get(i);
					return currentSave;
				}
			}
		}
		return null;
	}
	
	public SaveFile generateSave(String fileName){
		File file = new File(saveDir, fileName);
		if(file.exists()){
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException e1) {
				System.out.println("Failed to read save file: "+file);
				e1.printStackTrace();
			}
			SaveFile save = null;
			try {
				save = new SaveFile(fileName, Boolean.parseBoolean(reader.readLine()));
				if(save.inFreePlay()){
					save.setRound(Integer.parseInt(reader.readLine()));
				} else {
					save.setLevel(reader.readLine());
				}
				save.setLives(Integer.parseInt(reader.readLine()));
				save.setPoints(Integer.parseInt(reader.readLine()));
				System.out.println(save.getPoints()+" "+fileName);
				save.setLasers(Integer.parseInt(reader.readLine()));
				save.setNumLevels(Integer.parseInt(reader.readLine()));
				reader.close();
				return save;
			} catch (IOException e) {
				System.out.println("Save file corrupt: "+file.getPath());
				e.printStackTrace();
				return null;
			} 
		} else {
			return null;
		}
	}
	
	/**
	 * Deletes the current save file. 
	 * @param fileName The name of the file to delete.
	 */
	public void deleateSave(String fileName){
		File file = new File(saveDir, fileName);
		if (file.delete()){	
			System.out.println("Deleated save: "+fileName);
			for (int i=0; i<saves.size(); i++){
				if (saves.get(i).equals(fileName)){
					saves.remove(i);
					break;
				}
			}
		} else {
			System.out.println("Save file not found: "+fileName);
		}
	}
	
	/**
	 * Gets all of the save file names. Usually used to create a JComboBox. 
	 * Example: new JComboBox(SaveFileLoaderInstance.getAllSaves());
	 * @return An array containing all of the save file names. 
	 */
	public SaveFile[] getAllSaves(){
		return saves.toArray(new SaveFile[saves.size()]);
	}
}
