package base;
 
import java.awt.geom.Ellipse2D;
 
public class Ball {
     
    public Ellipse2D ball;
     
    double XVelocity = 0;
    double YVelocity = 0;
    
    double relativeX = 0;
    double relativeY = 0;
          
    public Ball(int x, int y, int w, int h){
        ball = new Ellipse2D.Double(x,y,w,h);
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
