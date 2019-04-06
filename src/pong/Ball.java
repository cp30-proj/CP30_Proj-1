/*
 *  File: Ball.java
 *  Package: pong
 *  Description: The Ball class.
 */

package pong;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;


public class Ball {
    //debug ish
    public static boolean ALL_SERVE_LEFT = false;
    public static boolean PRINT_SPAWN_SPEEDS = false;
    public static boolean SHOW_PAST_FRAME = false;
    public static boolean SLOW_BALL = false;
    
    public static int HITS_BEFORE_SPEED_UP = 5; //0 means no speed up   //paddle hits
    public static int DEFAULT_HITS_B4_NEXT_BALL = 4;
    
    
    public int hitsBeforeNextBall = DEFAULT_HITS_B4_NEXT_BALL; //0 means this does not apply    
    public int x,y;
    public int oldx,oldy;
    public int diam = 30;
    public int width = diam, height = diam;
    public int dx,dy;   //velocities
    public int amtOfHits;
    public boolean isDead = false;
    public boolean triggerNextBall = false;
    public boolean nextBallTriggered = false;
    public int totHitCtr = 0;
    //
    private Pong pong;
    public Random rng;
    public int SPEED_CAP;
    
    public Ball(Pong pong) {
        this.pong = pong;
        rng = new Random();
        SPEED_CAP = Paddle.WIDTH-1; //pong.width/10;
        spawn();
    }
    
    public void spawn(){
            hitsBeforeNextBall = DEFAULT_HITS_B4_NEXT_BALL;
            isDead = false;
            triggerNextBall = false;
            nextBallTriggered  = false;
            this.amtOfHits = 0;
            this.totHitCtr = 0;
            this.x = pong.width / 2 - this.width / 2;   oldx=x;
            this.y = pong.height / 2 - this.height / 2; oldy=y;
            
            //currently, randomly throw at any direction at any speed
            int  maxXSpeed = Paddle.PADDLE_SPEED + 1;
            int  maxYSpeed = Paddle.PADDLE_SPEED/2;     //avoid spawning ones that keep bouncing off walls
            int  minXSpeed = Paddle.PADDLE_SPEED/3;
            int  minYSpeed = 2;
            
            if(SLOW_BALL){
                maxXSpeed = Paddle.PADDLE_SPEED /2;
                maxYSpeed = Paddle.PADDLE_SPEED/3;     //avoid spawning ones that keep bouncing off walls
                minXSpeed = Paddle.PADDLE_SPEED/4;
                minYSpeed = 2;
            }
            /////MAGNITUDE
            dx = rng.nextInt(maxXSpeed - minXSpeed) + minXSpeed;  //cant be zero kasi 
            dy = rng.nextInt(maxYSpeed-minYSpeed) + minYSpeed;
//            int minSumSpeed = Paddle.PADDLE_SPEED/3;
//            if (dx + dy < minSumSpeed){
//                dy += minSumSpeed;
//            }
            
            /////DIRECTION
            if(ALL_SERVE_LEFT || rng.nextBoolean()){  //directionX
                dx = -dx;
            }
            if(rng.nextBoolean()){   //directionY
                dy = -dy;
            }
            
            
            if(PRINT_SPAWN_SPEEDS){
                System.out.println("dx = " + dx);
                System.out.println("dy = " + dy);
            }
    }
    
    
    
    public void update(Paddle paddle1, Paddle paddle2){
        //move ball
        this.x += dx;
        this.y += dy;
        
        
        //collision physics  (check collision integrated)
        //upper, lower (safe) walls
        if(y<=0){  //up wall
            dy = -dy;
            y = 0;                                
        } else if(y+diam>=pong.height){  //up wall
            dy = -dy;
            y = pong.height - height;                                
        }
        //left right (death) walls
        if(x< 0-diam-3){   //left wall (death)
            paddle2.score++;
            //spawn();
            isDead = true;
        } else if(x> pong.width + diam + 3){  //right wall (death)
            paddle1.score++;
            //spawn();
            isDead = true;
        }
        
        
        //[[S I M P L E]] paddle collision physics
        if(x<=paddle1.getRX()  && (x+diam/2)>=paddle1.getLX()){ //LEFT paddle
            if(y+diam >= paddle1.getUY()  &&  y<=  paddle1.getDY()){
                dx = Math.abs(dx);//ball shd move forward
                ///
                if(amtOfHits >= HITS_BEFORE_SPEED_UP && HITS_BEFORE_SPEED_UP!=0){
                    if(dx<SPEED_CAP){
                        dx++;   //forward kasi
                    }
                    amtOfHits = 0;
                }
                amtOfHits++;
                totHitCtr++;
            }
            //corner
            if(y+diam >= paddle1.getUY()  &&  y+diam <= paddle1.getUY() + Paddle.CTOL){ //super corner
                dy = -Math.abs(dy);//upwards
                //dy  = -dy;
            }
            if(y >= paddle1.getDY()-Paddle.CTOL  &&  y+diam <= paddle1.getDY()){ //super corner
                dy = Math.abs(dy);//downwards
                //dy  = -dy;
            }
        }
        
        if(x+diam >= paddle2.getLX()  && (x+diam/2)<=paddle2.getRX()){  //RIGHT paddle
            if(y+diam >= paddle2.getUY()  &&  y<=  paddle2.getDY()){
                dx = -Math.abs(dx);//ball shd move backward
                ///
                if(amtOfHits >= HITS_BEFORE_SPEED_UP && HITS_BEFORE_SPEED_UP!=0){
                    if(dx<SPEED_CAP){
                        dx--;   //backward kasi
                    }
                    amtOfHits = 0;
                }
                amtOfHits++;
                totHitCtr++;
            }
            //corner
            if(y+diam >= paddle2.getUY()  &&  y+diam <= paddle2.getUY() + Paddle.CTOL){ //super corner
                dy = -Math.abs(dy);//upwards
                //dy  = -dy;
            }
            if(y >= paddle2.getDY()-Paddle.CTOL  &&  y+diam <= paddle2.getDY()){ //super corner
                dy = Math.abs(dy);//downwards
                //dy  = -dy;
            }
        }
        
        if(totHitCtr == hitsBeforeNextBall && !nextBallTriggered && hitsBeforeNextBall!=0){
            triggerNextBall = true;    //to be deactivated by "Pong" class.
            hitsBeforeNextBall = 3* hitsBeforeNextBall;
        }
        if(nextBallTriggered && totHitCtr<hitsBeforeNextBall){
            nextBallTriggered = false;
        }
    }
    
    
    public void render(Graphics g){
        if(SHOW_PAST_FRAME){
            g.setColor(Color.GRAY);
            g.fillRect(oldx, oldy, width, height);
        }
        
        g.setColor(Color.WHITE);
        g.fillRect(x, y, width, height);
    }

    
    public int getCtrX(){
        return x + diam/2;
    }
    public int getCtrY(){
        return y + diam/2;
    }
}


