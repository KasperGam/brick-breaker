package base;
 
import java.awt.Color;
import java.awt.Rectangle;
 
public class Brick {
 
    private double X;
    private double Y;
    private double Width;
    private double Height;
    private int Strength = 1;
    private int MaxStrength = 1;
    private Color color = Color.BLACK;
    private boolean hasColor = false;
     
     
    public Brick(double x, double y, double width, double height){
        X = x; Y = y; Width = width; Height = height;
    }
     
    public Brick(double x, double y, double width, double height, int strength){
        X = x; Y = y; Width = width; Height = height;
        Strength = strength;
        MaxStrength = strength;
    }
     
    public Brick(double x, double y, double width, double height, int strength, Color clr){
        X = x; Y = y; Width = width; Height = height;
        Strength = strength;
        MaxStrength = strength;
        hasColor = true;
        color = clr;
    }
 
    /**
     * Gets the color of this brick.
     * @return Returns the Color of this brick.
     */
    public Color getColor(){
        return color;
    }
     
    /**
     * Sets the color of this brick.
     * @param clr The new color.
     */
    public void setColor(Color clr){
        color = clr;
    }
     
    public void setStrength(int strength){
        Strength = strength;
        if (strength>MaxStrength){
        	MaxStrength = getStrength();
        }
    }
    
    public int getStrength(){
    	return Strength;
    }
    
    //Returns the percent damage of this brick.
    public double getPercentDamaged(){
    	return ((double)MaxStrength-(double)Strength)/MaxStrength;
    }
    
    public void setSpecialColor(boolean val){
        hasColor = val;
    }
     
    /**
     * Tells whether or not this brick should be independent of the color scheme.
     * @return
     */
    public boolean getHasSpecialColor(){
        return hasColor;
    }
     
    /**
     * Gets the x (as a double from 0.0 - 1.0)
     * @return Returns the x, as a percentage of the width of the applet.
     */
    public double getX(){
        return X;
    }
     
    /**
     * Gets the y (as a double from 0.0 - 1.0)
     * @return Returns the y, as a percentage of the height of the applet.
     */
    public double getY(){
        return Y;
    }
     
    /**
     * Gets the width of this brick, as a percentage of the width of the applet.
     * @return
     */
    public double getWidth(){
        return Width;
    }
     
    /**
     * Gets the height of this brick, as a percentage of the height of the applet.
     * @return
     */
    public double getHeight(){
        return Height;
    }
     
    /**
     * Resizes the brick to fit the screen.
     * @param x The new x position.
     * @param width The new width of the brick.
     */
    public void resize(int x, int width){
        X = x;
        Width = width;
    }
     
    /**
     * Gets the rectangle object of this brick.
     * @return
     */
    public Rectangle getRectangle(){
        return new Rectangle((int)X, (int)Y, (int)Width, (int)Height);
    }
}
