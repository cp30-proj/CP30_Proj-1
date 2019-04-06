/*
 *  File: Paddle.java
 *  Package: pong
 *  Description: The Paddle class.
 */

package pong;

import java.awt.Color;
import java.awt.Graphics;


public class Paddle {//actually player also i think
    
    public int paddleNumber;
    private Pong pong;
    public static int PADDLE_SPEED = 15;
    
    //create rect
    public int x,y;
    public int oldx,oldy;
    public static int WIDTH = 30,HEIGHT = 200;
    public static int CTOL = 7;    //corner tolerance (SUPER corner square WIDTH)
    public static boolean SHOW_PAST_FRAME = false;
    
    
    public int score;
    
    
    public Paddle(Pong pong, int paddleNumber) {
        this.paddleNumber = paddleNumber;
        this.pong = pong;
        
        
        this.y = pong.height/2 - this.HEIGHT/2;   //y init pos at ctr
        oldy = y;
        
        //note: 0,0 is upper left in Java.
        //note: x,y is upper left point of paddle
        if (paddleNumber == 1){
            this.x = 0;
        }else if (paddleNumber == 2){
            this.x = pong.width - WIDTH;
        }
        oldx = x;
    }
    
    public void render(Graphics g){
        
        if(SHOW_PAST_FRAME){
            g.setColor(Color.GRAY);
            g.fillRect(oldx, oldy, WIDTH, HEIGHT);

            //draw SUPER corners
            if (paddleNumber == 1){
                g.setColor(Color.PINK);
                g.fillRect(oldx+WIDTH-CTOL, oldy, CTOL, CTOL);

                g.setColor(Color.PINK);
                g.fillRect(oldx+WIDTH-CTOL, oldy+HEIGHT-CTOL, CTOL, CTOL);
            }else if (paddleNumber == 2){
                g.setColor(Color.PINK);
                g.fillRect(oldx, oldy, CTOL, CTOL);

                g.setColor(Color.PINK);
                g.fillRect(oldx, oldy+HEIGHT-CTOL, CTOL, CTOL);
            }
        }
        
        
        
        
        g.setColor(Color.WHITE);
        g.fillRect(x, y, WIDTH, HEIGHT);
        
        //draw SUPER corners
        if (paddleNumber == 1){
            g.setColor(Color.RED);
            g.fillRect(x+WIDTH-CTOL, y, CTOL, CTOL);
            
            g.setColor(Color.RED);
            g.fillRect(x+WIDTH-CTOL, y+HEIGHT-CTOL, CTOL, CTOL);
        }else if (paddleNumber == 2){
            g.setColor(Color.RED);
            g.fillRect(x, y, CTOL, CTOL);
            
            g.setColor(Color.RED);
            g.fillRect(x, y+HEIGHT-CTOL, CTOL, CTOL);
        }
    }
    
    
    public void moveUp(){//possible TODO: can give player capability to control speed, or just give player acceleration controls rather than constant speed
        int speed = PADDLE_SPEED;
        //oldy = y;
        
        if(y-speed <= 0){
            y=0; 
        } else {
            y-=speed;
        }
    }
    public void moveDn(){
        //oldy = y;
        int speed = PADDLE_SPEED;
        
        if(y+HEIGHT+speed >= pong.height){
            y=pong.height-HEIGHT;
        } else {
            y+=speed;
        }
    }
    
    public void moveUp(double percentSpd){
        int speed = (int)percentSpd/100*PADDLE_SPEED;
        //oldy = y;
        
        if(y-speed <= 0){
            y=0; 
        } else {
            y-=speed;
        }
    }
    public void moveDn(double percentSpd){
        //oldy = y;
        int speed = (int)percentSpd/100*PADDLE_SPEED;
        
        if(y+HEIGHT+speed >= pong.height){
            y=pong.height-HEIGHT;
        } else {
            y+=speed;
        }
    }
    
    
    
    //corners of the paddle
    public int getLX(){  //upper left corner
        return x;
    } 
    public int getOldLX(){  //well, x doesn't change
        return x;
    }
    public int getUY(){
        return y;
    }
    public int getOldUY(){
        return oldy;
    }
    ///
    public int getRX(){
        return x+WIDTH;
    }
    public int getOldRX(){
        return x+WIDTH;
    }
    public int getOldDY(){
        return oldy+HEIGHT;
    }
    public int getDY(){
        return y+HEIGHT;
    }
    
    
}
