/*
 *  © 2019 by Patrick Matthew Chan
 *  File: Pong.java
 *  Package: pong
 *  Description: The Pong class.
 */
package pong;

import java.applet.Applet;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.InputMismatchException;
import javax.swing.JFrame;
import javax.swing.Timer;

/* @author Patrick Matthew J. Chan*/
public class Pong implements ActionListener, KeyListener{

    public boolean bot = false;
    public boolean w,s,up,dn;       //better simultaneous key handling
    
    public static Pong pong;
    public int width = 700, height = 700;
    public Renderer renderer;
    public Paddle player1;
    public Paddle player2;
    public Ball ball;
    public int gameStatus = 0;  //0=start/splash, 1=paused, 2=playing
    

    public Pong() {
        //timer
        Timer timer = new Timer(20, this);
        
        JFrame jframe = new JFrame("Pong Game");
        renderer = new Renderer();
        
        jframe.setSize(width + 15,height + 38);  //JFrame window size
        jframe.setVisible(true);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.add(renderer);
        jframe.addKeyListener(this);
        
        start();
        
        timer.start();
    }
    
    public void start(){
        player1 = new Paddle(this,1);
        player2 = new Paddle(this,2);
        ball = new Ball(this);
    }
    
    public void render(Graphics2D g){         //(Graphics2D, in order to be able to set Stroke)
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        //later
        if(gameStatus == 0){
            Font startFont = new Font("Verdana", 1, 70);
            String startText1 = "PONG GAME";
            
            g.setColor(Color.WHITE);
            g.setFont(startFont);
            g.drawString(startText1, width/2 - g.getFontMetrics(startFont).stringWidth(startText1)/2, height/2 - g.getFontMetrics(startFont).getAscent()/2);
        
            
            
            Font startFont2 = new Font("Verdana", 1, 15);
            String startText2 = "=(Press Space to play)=";
            
            g.setColor(Color.WHITE);
            g.setFont(startFont2);
            g.drawString(startText2, width/2 - g.getFontMetrics(startFont2).stringWidth(startText2)/2, height/2 + g.getFontMetrics(startFont2).getAscent()*1/2);
        
            
            
            Font startFont3 = new Font("Verdana", 1, 13);
            String startText3 = "[Press Shift:   vs. BOT]";
            
            g.setColor(Color.WHITE);
            g.setFont(startFont3);
            g.drawString(startText3, width/2 - g.getFontMetrics(startFont3).stringWidth(startText3)/2, height/2 + g.getFontMetrics(startFont3).getAscent()*7/2);
        
        
        } else if(gameStatus == 1){
            Font pausedFont = new Font("Verdana", 1, 50);
            String pausedText1 = "- PAUSED -";
            
            g.setColor(Color.WHITE);
            g.setFont(pausedFont);
            g.drawString(pausedText1, width/2 - g.getFontMetrics(pausedFont).stringWidth(pausedText1)/2, height/2 - g.getFontMetrics(pausedFont).getAscent()/2);
        
            
            
            Font pausedFont2 = new Font("Verdana", 1, 15);
            String startText2 = "[Press Space to Resume]";
            
            g.setColor(Color.WHITE);
            g.setFont(pausedFont2);
            g.drawString(startText2, width/2 - g.getFontMetrics(pausedFont2).stringWidth(startText2)/2, height/2 + g.getFontMetrics(pausedFont2).getAscent()*1/2);
        }else if(gameStatus == 2){
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(4f));
            g.drawLine(width/2, 0, width/2, height);

            player1.render(g);
            player2.render(g);
            ball.render(g);
        }
    }
    
    public void update(){
        player1.oldx = player1.x;
        player1.oldy = player1.y;
        player2.oldx = player2.x;
        player2.oldy = player2.y;
        //System.out.println("Hello again :)");
        if(w){
            player1.moveUp();
        }
        if(s){
            player1.moveDn();
        }
        //////
        if(up){
            player2.moveUp();
        }
        if(dn){
            player2.moveDn();
        }
        
        ball.oldx = ball.x;
        ball.oldy = ball.y;
        //// redraw ball
        ball.update(player1, player2);
        
    }
    
    
    
    
    
    
    


    @Override
    public void actionPerformed(ActionEvent e) {
        if(gameStatus==2){
            update();
        }
        renderer.repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int id = e.getKeyCode();
        
        if(id == KeyEvent.VK_W){
            w = true;
        } else if(id == KeyEvent.VK_S){
            s = true;
        } else if(id == KeyEvent.VK_UP){
            up = true;
        } else if(id == KeyEvent.VK_DOWN){
            dn = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int id = e.getKeyCode();
        
        if(id == KeyEvent.VK_W){
            w = false;
        } else if(id == KeyEvent.VK_S){
            s = false;
        } else if(id == KeyEvent.VK_UP){
            up = false;
        } else if(id == KeyEvent.VK_DOWN){
            dn = false;
        } else if(id == KeyEvent.VK_SHIFT){
            if(gameStatus == 0){
                gameStatus = 2;
                bot = true;
            }
        } else if(id == KeyEvent.VK_SPACE){
            if(gameStatus == 0 || gameStatus == 1){
                gameStatus = 2;
            } else if (gameStatus == 2){
                gameStatus = 1;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Main Class ~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        pong = new Pong();
    }

}
