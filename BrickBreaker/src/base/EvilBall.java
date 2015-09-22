package base;

import java.awt.Color;
import java.awt.geom.Ellipse2D;

public class EvilBall {
    public Ellipse2D ball;
    
    double XVelocity = 0;
    double YVelocity = 0;
    
    double relativeX = 0;
    double relativeY = 0;
    
    int cycleTime = 0;
    int currentTime = 0;
    
    boolean first = true;
    
    Color firstColor = Color.red;
    Color secondColor = Color.yellow;
     
    int arrayPos = 0;
     
    public EvilBall(int x, int y, int rad, int time){
    	ball = new Ellipse2D.Double(x, y, rad, rad);
    	cycleTime = time;
    	currentTime=  cycleTime;
    }
    
    public EvilBall(int x, int y, int rad, double XSpeed, double YSpeed, int time){
        ball = new Ellipse2D.Double(x,y,rad,rad);
        XVelocity = XSpeed;
        YVelocity = YSpeed;
        cycleTime = time;
        currentTime = cycleTime;
    }
    
    public void update(int timePassed){
    	int tot = timePassed/cycleTime;
    	int rem = timePassed%cycleTime;
    	if(currentTime<rem){
    		tot++;
    		currentTime = cycleTime-(rem-currentTime);
    	} else {
    		currentTime-=rem;
    	}
    	if(tot%2==1){
    		first = !first;
    	}
    }
    
    public Color getCurrentColor(){
    	if(first){
    		return firstColor;
    	} else{
    		return secondColor;
    	}
    }
    
    public void setFirstColor(Color clr){
    	firstColor = clr;
    }
    
    public void setSecondColor(Color clr){
    	secondColor = clr;
    }
     
    public void setXVelocity(double vx){
        XVelocity = vx;
    }
     
    public void setYVelocity(double vy){
        YVelocity = vy;
    }
    
    public void setRelativeX(double x){
    	relativeX = x;
    }
    
    public void setRelativeY(double y){
    	relativeY = y;
    }
    
    public double getRelativeX(){
    	return relativeX;
    }
    
    public double getRelativeY(){
    	return relativeY;
    }
     
    public double getXVelocity(){
        return XVelocity;
    }
     
    public double getYVelocity(){
        return YVelocity;
    }
     
    public double getX(){
        return ball.getX();
    }
     
    public double getY(){
        return ball.getY();
    }
     
    public double getWidth(){
        return ball.getWidth();
    }
     
    public double getHeight(){
        return ball.getHeight();
    }
     
    public void setFrame(double x, double y, double w, double h){
        ball.setFrame(x,y,w,h);
    }
}
