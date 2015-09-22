package base;

import java.awt.Color;

public class Perk {
	private boolean isGood = true;
	private double velocity = 20.0;		//in pixels per second
	private int xPos;
	private int yPos;
	
	public Perk(int x, int y, int v, boolean good){
		xPos = x;
		yPos = y;
		velocity = v;
		isGood = good;
	}
	
	public void setX(int x){
		xPos=  x;
	}
	
	public void setY(int y){
		yPos = y;
	}
	
	public void setVelocity(double v){
		velocity = v;
	}
	
	public int getX(){
		return xPos;
	}
	
	public int getY(){
		return yPos;
	}
	
	public double getVelocity(){
		return velocity;
	}
	
	public boolean isGood(){
		return isGood;
	}
	
	public Color getColor(){
		if (isGood){
			return Color.green;
		} else {
			return Color.red;
		}
	}
}
