/*
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
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.Timer;


public class Pong implements ActionListener, KeyListener{

    public boolean w,s,up,dn;       //better simultaneous key handling
    public boolean bot = false;     //true if vs. computer.
    public boolean selectingDifficulty;
    public int botDifficulty;
    public int botMoves;
    public int botCooldown = 0;
    public Random rng;
    
    public static Pong pong;
    public JFrame jframe;
    public int width = 700, height = 700;
    public Renderer renderer;
    public Paddle player1;
    public Paddle player2;
    public Ball ball;
    //public Ball ball2;
    public int gameStatus = 0;  //0=start/splash, 1=paused, 2=playing, 3=game over
    public int scoreLimit = 7;
    public int winnerP;
    

    public Pong() {
        //timer
        Timer timer = new Timer(20, this);
        
        jframe = new JFrame("Pong Game");
        renderer = new Renderer();
        
        jframe.setSize(width + 15,height + 38);  //JFrame window size
        jframe.setVisible(true);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.add(renderer);
        jframe.addKeyListener(this);
        
        timer.start();
    }
    
    public void start(){
        gameStatus = 2;
        player1 = new Paddle(this,1);
        player2 = new Paddle(this,2);
        ball = new Ball(this);
        //ball2 = new Ball(this);
    }
    
    public void render(Graphics2D g){         //(Graphics2D, in order to be able to set Stroke)
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        
        if(gameStatus == 1 || gameStatus == 2){ //graphics during game play
            g.setColor(Color.LIGHT_GRAY);
            g.setStroke(new BasicStroke(4f));
            g.drawLine(width/2, 0, width/2, height);
            
            
            Font scoreFont = new Font("Monospaced", 1, 70);
            String scoreText1 = Integer.toString(player1.score);
            String scoreText2 = ":";
            String scoreText3 = Integer.toString(player2.score);
            g.setColor(Color.WHITE);
            g.setFont(scoreFont);
            g.drawString(scoreText1, 3*width/8 - g.getFontMetrics(scoreFont).stringWidth(scoreText1)/2, (int)(g.getFontMetrics(scoreFont).getAscent()*1.1));
            g.drawString(scoreText2, width/2 - g.getFontMetrics(scoreFont).stringWidth(scoreText2)/2, (int)(g.getFontMetrics(scoreFont).getAscent()*1.1));
            g.drawString(scoreText3, 5*width/8 - g.getFontMetrics(scoreFont).stringWidth(scoreText3)/2, (int)(g.getFontMetrics(scoreFont).getAscent()*1.1));
        

            player1.render(g);
            player2.render(g);
            ball.render(g);
            //ball2.render(g);
        }
        ////////  "PONG"  text
        if(gameStatus == 0 || gameStatus == 3){
            Font startFont = new Font("Verdana", 1, 70);
            String startText1 = "PONG GAME";
            
            g.setColor(Color.WHITE);
            g.setFont(startFont);
            if(gameStatus == 0){
                g.drawString(startText1, width/2 - g.getFontMetrics(startFont).stringWidth(startText1)/2, height/2 - g.getFontMetrics(startFont).getAscent()/2);
            } else {
                g.drawString(startText1, width/2 - g.getFontMetrics(startFont).stringWidth(startText1)/2, height/4 - g.getFontMetrics(startFont).getAscent()/2);
            }
        }
        //////////////////////////////////-------------------------------///////////////////////////////////////
        
        //WELCOME and PAUSE screens/overlay
        if(gameStatus == 0){
            Font startFont2 = new Font("Verdana", 1, 15);
            String startText2 = "=(Press Space to play)=";

            g.setColor(Color.WHITE);
            g.setFont(startFont2);
            g.drawString(startText2, width/2 - g.getFontMetrics(startFont2).stringWidth(startText2)/2, height/2 + g.getFontMetrics(startFont2).getAscent()*1/2);
        

            
            if(!selectingDifficulty){
                Font startFont3a = new Font("Verdana", 1, 13);
                String startText3 = "[Press Shift:   vs. BOT]";

                g.setColor(Color.WHITE);
                g.setFont(startFont3a);
                g.drawString(startText3, width/2 - g.getFontMetrics(startFont3a).stringWidth(startText3)/2, height/2 + g.getFontMetrics(startFont3a).getAscent()*7/2);
            } else {
                Font startFont3 = new Font("Verdana", 1, 13);
                String startText3a = "Select BOT difficulty:";
                String diffString = "Easy"; //for 0, or default
                switch(botDifficulty){
                    case 1:
                        diffString = "Normal";
                        break;
                        
                    case 2:
                        diffString = "Hard";
                        break;
                }
                String startText3b = "<< ["+diffString+"] >>";

                g.setColor(Color.WHITE);
                g.setFont(startFont3);
                g.drawString(startText3a, width/2 - g.getFontMetrics(startFont3).stringWidth(startText3a)/2, height/2 + g.getFontMetrics(startFont3).getAscent()*7/2);
                g.drawString(startText3b, width/2 - g.getFontMetrics(startFont3).stringWidth(startText3b)/2, height/2 + g.getFontMetrics(startFont3).getAscent()*5);
            }

            if(!selectingDifficulty){
                Font startFont4 = new Font("Verdana", 1, 13);
                String startText4a = "Points to WIN: ";
                String startText4b = "<< ["+scoreLimit+"] >>";
                String dummyTxt = "Points to WIN: << [ww] >>"; 

                g.setColor(Color.WHITE);
                g.setFont(startFont4);
                int pos4a = width/2 - g.getFontMetrics(startFont4).stringWidth(dummyTxt)/2;
                int pos4b = pos4a + g.getFontMetrics(startFont4).stringWidth(startText4a) + g.getFontMetrics(startFont4).stringWidth(startText4b)/3;
                g.drawString(startText4a, pos4a, height/2 + g.getFontMetrics(startFont4).getAscent()*5);
                g.drawString(startText4b, pos4b, height/2 + g.getFontMetrics(startFont4).getAscent()*5);
            }
        
        } else if(gameStatus == 1){
            //g.drawRect(0, 0, width, height);
            g.setColor(new Color(0, 0, 0, 200));
            g.fillRect(0, 0, width, height);
            
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
        
            
            
            Font pausedFont3 = new Font("Verdana", 1, 15);
            String startText3 = "[Press ESC for Title Screen]";
            
            g.setColor(Color.WHITE);
            g.setFont(pausedFont3);
            g.drawString(startText3, width/2 - g.getFontMetrics(pausedFont3).stringWidth(startText3)/2, height/2 + g.getFontMetrics(pausedFont3).getAscent()*2);
        } else if (gameStatus == 3){
            Font gOverFont1 = new Font("Verdana", 1, 40);
            String gOverText1;
            if (bot && winnerP == 2){
                    gOverText1="=The Bot Wins!=";
            } else {
                    gOverText1="=Player " + winnerP + " Wins!=";
            }
            
            g.setColor(Color.WHITE);
            g.setFont(gOverFont1);
            g.drawString(gOverText1, width/2 - g.getFontMetrics(gOverFont1).stringWidth(gOverText1)/2, height/2 - g.getFontMetrics(gOverFont1).getAscent()/2);
            
            
            
            Font gOverFont2 = new Font("Verdana", 1, 20);
            String gOverText2a = "[Space to Play Again]";
            String gOverText2b = "[ESC for Title Screen]";
            
            g.setColor(Color.WHITE);
            g.setFont(gOverFont2);
            g.drawString(gOverText2a, width/2 - g.getFontMetrics(gOverFont2).stringWidth(gOverText2a)/2, height/2 + g.getFontMetrics(gOverFont2).getAscent()/2*3);
            g.drawString(gOverText2b, width/2 - g.getFontMetrics(gOverFont2).stringWidth(gOverText2b)/2, height/2 + g.getFontMetrics(gOverFont2).getAscent()/2*11/2);
        }
    }
    
    public void update(){
        player1.oldx = player1.x;
        player1.oldy = player1.y;
        player2.oldx = player2.x;
        player2.oldy = player2.y;
        //System.out.println("Hello again :)");
        if (player1.score >= scoreLimit){
                winnerP = 1;
                gameStatus = 3;
        }

        if (player2.score >= scoreLimit){
                gameStatus = 3;
                winnerP = 2;
        }
        ////////
        if(w){
            player1.moveUp();
        }
        if(s){
            player1.moveDn();
        }
        //////
        if(!bot){   //human opponent
            if(up){
                player2.moveUp();
            }
            if(dn){
                player2.moveDn();
            }
        } else {    //computer opponent
            int paddleCtr = (player2.getUY() + player2.getDY())/2;
            
            if(botMoves < 10){
                if(ball.y < paddleCtr){
                    player2.moveUp();
                    botMoves++;
                }
                if(ball.y > paddleCtr){
                    player2.moveDn();
                    botMoves++;
                }
            } else {
                if(botCooldown > 0){
                    //cooldown in progress
                    botCooldown--;
                    if(botCooldown <= 0){
                        botMoves = 0;
                    }
                } else {
                    //start cooldown
                    if (botDifficulty == 0){
                            botCooldown = 27;
                    }
                    if (botDifficulty == 1){
                            botCooldown = 20;
                    }
                    if (botDifficulty == 2){
                            botCooldown = 15;
                    }
                }
            }
                    
        }
        ball.oldx = ball.x;
        ball.oldy = ball.y;
        //// redraw ball
        ball.update(player1, player2);
        
        
        
        //ball2.oldx = ball2.x;
        //ball2.oldy = ball2.y;
        //// redraw ball
        //ball2.update(player1, player2);
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
        } else if(id == KeyEvent.VK_A || id == KeyEvent.VK_LEFT){
            if(gameStatus == 0){
                if(selectingDifficulty){
                    botDifficulty--;
                    if(botDifficulty < 0){
                        botDifficulty = 2;
                    }
                } else if (scoreLimit > 1){
                    scoreLimit --;
                }
            }
        } else if(id == KeyEvent.VK_D || id == KeyEvent.VK_RIGHT){
            if(gameStatus == 0){
                if(selectingDifficulty){
                    botDifficulty++;
                    if(botDifficulty > 2){
                        botDifficulty = 0;
                    }
                } else if (scoreLimit < 20){
                    scoreLimit ++;
                }
            }
        } else if(id == KeyEvent.VK_SHIFT){
            if(gameStatus == 0){
                bot = true;
                selectingDifficulty = true;
            }
        } else if(id == KeyEvent.VK_SPACE){
            if(gameStatus == 0){
                if(!selectingDifficulty){
                    bot = false;
                } else{
                    selectingDifficulty = false;
                }
                start();
            } else if(gameStatus == 1){
                gameStatus = 2;
            } else if (gameStatus == 2){
                gameStatus = 1;
            } else if (gameStatus == 3){
                start();
            }
        } else if(id == KeyEvent.VK_ESCAPE){
            if(gameStatus == 1 || gameStatus == 3){
                gameStatus = 0;
                selectingDifficulty = false;
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
