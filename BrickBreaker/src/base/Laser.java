package base;

import java.awt.Color;
import java.awt.Rectangle;

public class Laser {

	private double yVelocity = 1;	//in pixels per millisecond
	
	private Rectangle bounds = new Rectangle();
	
	private Color color = Color.blue;
	
	public Laser(int x, int y, int width, int height){
		bounds.setBounds(x, y, width, height);
	}
	
	public Laser(int x, int y, int width, int height, double vy){
		bounds.setBounds(x, y, width, height);
		yVelocity = vy;
	}
	
	public Laser(int x, int y, int width, int height, double vy, Color clr){
		bounds.setBounds(x, y, width, height);
		yVelocity = vy;
		color = clr;
	}
	
	public double getVelocity(){
		return yVelocity;
	}
	
	public int getY(){
		return (int) bounds.getY();
	}
	
	public void setY(double y){
		bounds.setLocation((int)bounds.getX(), (int)y);
	}
	
	public void setX(double x) {
		bounds.setLocation((int)x, (int)bounds.getY());
	}
	
	public Color getColor(){
		return color;
	}
	
	public Rectangle getBounds(){
		return bounds;
	}

	public int getHeight() {
		return (int)bounds.getHeight();
	}
	
	public void resize(int width, int height){
		int newWidth = (int)(width*.02);
		if (newWidth<5){ newWidth = 5; }
		bounds.setBounds((int)bounds.getX(), (int)bounds.getY(), newWidth, height/8);
	}

	public double getWidth() {
		return bounds.getWidth();
	}


	
}
