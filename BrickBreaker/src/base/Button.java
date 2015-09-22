package base;

import java.awt.Image;
import java.awt.Rectangle;

public class Button {

	private Image buttonImage = null;
	private Image buttonImagePressed = null;
	
	private int xPos;
	private int yPos;
	
	private boolean isPressed = false;
			
	public Button(Image image, Image pressedImage, int x, int y){
		buttonImage = image;
		buttonImagePressed = pressedImage;
		xPos = x;
		yPos = y;
	}
	
	public int getX(){
		return xPos;
	}
	
	public void setX(int x){
		xPos = x;
	}
	
	public void setY(int y){
		yPos = y;
	}
	
	public int getY(){
		return yPos;
	}
	
	public int getWidth(){
		return buttonImage.getWidth(null);
	}
	
	public int getHeight(){
		return buttonImage.getHeight(null);
	}
	
	public void setPressed(boolean val){
		isPressed = val;
	}
	
	public Image getButtonImage(){
		if (isPressed){
			return buttonImagePressed;
		} else {
			return buttonImage;
		}
	}
	
	public Rectangle getBounds(){
		return new Rectangle(xPos, yPos, buttonImage.getWidth(null), buttonImage.getHeight(null));
	}
	
}
