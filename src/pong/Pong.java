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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Random;
import javafx.scene.input.KeyCode;
import javax.swing.JFrame;
import javax.swing.Timer;


public class Pong implements ActionListener, KeyListener{

    UCMI ucmi = new UCMI();
    ////////////////////////-----C O N T R O L S--------/////////////////////
    //first index = player no. |  second index = the "item"  [[all indices correspond to one another]]
    String[][] gameControls; //in-game "name" of controls (general)
    double[][] directionalGroupNo;   //say: {up,down,left,right,a,b}  would be {1.1, 1.2, 1.3, 1.4, 2, 3} //assumed .1=up .2=dn .3=lf .4=rt | .0=not directional
    int[][] kybdControls;   //keyCodes for keyboard input
    CM[][] uartControls;
    static final int UART_HOLD_DOWN_WAIT_TIME = 50; 
    int[][] uartHolddownWaitCount;   //timeout before computer reads button hold input as multiple presses
        //^^ start as 0, counts up to wait time, stays there until released, in which it resets to 0.
    boolean disableCMInput = false;
    //////////////////////------------------------------/////////////////////
    
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
    public ArrayList<Ball> balls = new ArrayList<>();
    //public Ball[] balls;         //changed since after v1.0 --> have multiball
    //public Ball ball2;
    public int gameStatus = 0;  //0=start/splash, 1=paused, 2=playing, 3=game over
    public int scoreLimit = 7;
    public int winnerP;
    public int botDir = 0;  //-1 up, 0 neutral, 1 down
    double botSpeedPercent = 100;
    

    public Pong() {
        ucmi.init();
        if(ucmi.isPortConnected){
            ucmi.ReqPlayer(1);
            ucmi.ReqPlayer(2);
            System.out.println("port connected. 1st requests sent.");
        }
        String[] genControlNames = new String[]{"Up","Down","Left","Right","Start","Select","Quit"}; //please insrt new controls at end, unless wiling to change indexing in code.
        double[]  genDirGrpNo = new double[]{1.1,1.2,1.3,1.4,2,3,4};
        ///////
        gameControls = new String[3][]; //player indexing starts at '1'
        for (int i = 1; i < gameControls.length; i++) {
            gameControls[i] =  genControlNames;
        }
        ////////
        directionalGroupNo = new double[3][]; //player indexing starts at '1'
        for (int i = 1; i < directionalGroupNo.length; i++) {
            directionalGroupNo[i] = genDirGrpNo;
        }
        ////////
        kybdControls = new int[3][]; //player indexing starts at '1'
        kybdControls[1] = new int[]{KeyEvent.VK_W,KeyEvent.VK_S,KeyEvent.VK_A,KeyEvent.VK_D,KeyEvent.VK_SPACE,KeyEvent.VK_SHIFT,KeyEvent.VK_ESCAPE};    //P1 default
        kybdControls[2] = new int[]{KeyEvent.VK_UP,KeyEvent.VK_DOWN,KeyEvent.VK_LEFT,KeyEvent.VK_RIGHT,KeyEvent.VK_SPACE,KeyEvent.VK_SHIFT,KeyEvent.VK_ESCAPE};    //P2 default
        ////////
        uartControls = new CM[3][]; //player indexing starts at '1'
        CM[] genUart = new CM[]{CM.LEFT_ANALOG_STICK_Y,CM.LEFT_ANALOG_STICK_Y,CM.LEFT_ANALOG_STICK_X,CM.LEFT_ANALOG_STICK_X,CM.A_FACE_BUTTON,
            CM.SELECT_BUTTON,CM.B_FACE_BUTTON};
        for (int i = 1; i < uartControls.length; i++) {
            uartControls[i] = genUart;
        }
        ////////
        uartHolddownWaitCount = new int[gameControls.length][gameControls[1].length];
        for (int i = 1; i < uartHolddownWaitCount.length; i++) {
            for (int j = 0; j < uartHolddownWaitCount[i].length; j++) {
                uartHolddownWaitCount[i][j] = 0;
            }
        }
        
        
        
        
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
        balls.clear();
        balls.add(new Ball(this));
        //balls = new Ball(this);
        //ball2 = new Ball(this);
    }
    
    public void render(Graphics2D g){         //(Graphics2D, in order to be able to set Stroke)
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        
        if(gameStatus == 1 || gameStatus == 2){ //graphics during game play
            //req ucmi
            if(ucmi.isPortConnected){
                ucmi.ReqPlayer(1);
                if(!bot){
                    ucmi.ReqPlayer(2);
                }
            }
            
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
            for (Ball ball : balls) {
                if(!ball.isDead){
                    ball.render(g);
                }
            }
            //ball.render(g);
            //ball2.render(g);
        }
        ////////  "PONG"  text
        if(gameStatus == 0 || gameStatus == 3){
            //req ucmi
            if(ucmi.isPortConnected){
                ucmi.ReqPlayer(1);
                ucmi.ReqPlayer(2);
            }
            
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
            String startText2 = "=(Press "+gameControls[1][4]+" Key to play)=";

            g.setColor(Color.WHITE);
            g.setFont(startFont2);
            g.drawString(startText2, width/2 - g.getFontMetrics(startFont2).stringWidth(startText2)/2, height/2 + g.getFontMetrics(startFont2).getAscent()*1/2);
        

            
            if(!selectingDifficulty){
                Font startFont3a = new Font("Verdana", 1, 13);
                String startText3 = "[Press "+gameControls[1][5]+" Key:   vs. BOT]";

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
                        
                    case 3:
                        diffString = "Very Hard";
                        break;
                        
                    case 4:
                        diffString = "Insane";
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
            String startText2 = "[Press "+gameControls[1][4]+" Key to Resume]";
            
            g.setColor(Color.WHITE);
            g.setFont(pausedFont2);
            g.drawString(startText2, width/2 - g.getFontMetrics(pausedFont2).stringWidth(startText2)/2, height/2 + g.getFontMetrics(pausedFont2).getAscent()*1/2);
        
            
            
            Font pausedFont3 = new Font("Verdana", 1, 15);
            String startText3 = "[Press "+gameControls[1][6]+" Key for Title Screen]";
            
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
            String gOverText2a = "["+gameControls[1][4]+" Key to Play Again]";
            String gOverText2b = "["+gameControls[1][6]+" Key for Title Screen]";
            
            g.setColor(Color.WHITE);
            g.setFont(gOverFont2);
            g.drawString(gOverText2a, width/2 - g.getFontMetrics(gOverFont2).stringWidth(gOverText2a)/2, height/2 + g.getFontMetrics(gOverFont2).getAscent()/2*3);
            g.drawString(gOverText2b, width/2 - g.getFontMetrics(gOverFont2).stringWidth(gOverText2b)/2, height/2 + g.getFontMetrics(gOverFont2).getAscent()/2*11/2);
        }
        
        ///////////////////////////////////////////////////////////////////////////
        ////////////////////////////  SINCE THIS SEEMS TO GET CALLED EVERY LOOP:
        /////////////////////////////////////////////////////////////////////////
        System.out.println("wee.");
        //uart controls (NON-PADDLE)
        if (ucmi.isPortConnected && !disableCMInput){/////////////////BUG (seems like it)
            ////////------------ OTHER CONTROLS ["accidental" repeated press is  bad, so timeout applied]
                //vvvv Remember: Controls indexing Starts with [0]; players indexing starts with [1].
                //String[] genControlNames = new String[]{"Up","Down","Left","Right","Start","Select","Quit"}; <--from top-((as of 10:34 pc clk))]
            for(int i=1;i<=2;i++){
                //any: left,  any: right     [may repeat press]
                if(uartControls[i][2].isAnalogAxis()){
                    int val1 = ucmi.p[i].readAnalogAxis(uartControls[i][2]);  //0 to 255
                    val1 = val1 - 128;
                    System.out.println("val1 = " + val1);
                    if(val1 > 64){
                        switch (uartHolddownWaitCount[i][3]){
                            case UART_HOLD_DOWN_WAIT_TIME:
                                actRight();
                                break;
                            case 0:
                                actRight();
                                uartHolddownWaitCount[i][3]++;
                                break;
                            default:
                                uartHolddownWaitCount[i][3]++;
                                break;
                        }
                    } else {uartHolddownWaitCount[i][3]=0;}
                    /////
                    if (val1 < -64){
                        switch (uartHolddownWaitCount[i][2]){
                            case UART_HOLD_DOWN_WAIT_TIME:
                                actLeft();
                                break;
                            case 0:
                                actLeft();
                                uartHolddownWaitCount[i][2]++;
                                break;
                            default:
                                uartHolddownWaitCount[i][2]++;
                                break;
                        }
                    } else {uartHolddownWaitCount[i][2]=0;}
                } else {
                    if(ucmi.p[i].readButton(uartControls[i][3])){
                        switch (uartHolddownWaitCount[i][3]){
                            case UART_HOLD_DOWN_WAIT_TIME:
                                actRight();
                                break;
                            case 0:
                                actRight();
                                uartHolddownWaitCount[i][3]++;
                                break;
                            default:
                                uartHolddownWaitCount[i][3]++;
                                break;
                        }
                    } else {uartHolddownWaitCount[i][3]=0;}
                    /////
                    if(ucmi.p[i].readButton(uartControls[i][2])){
                        switch (uartHolddownWaitCount[i][2]){
                            case UART_HOLD_DOWN_WAIT_TIME:
                                actLeft();
                                break;
                            case 0:
                                actLeft();
                                uartHolddownWaitCount[i][2]++;
                                break;
                            default:
                                uartHolddownWaitCount[i][2]++;
                                break;
                        }
                    } else {uartHolddownWaitCount[i][2]=0;}
                }
                
                
                //any:  start   [NO repeat press on hold]
                if(ucmi.p[i].readButton(uartControls[i][4])){
                    switch (uartHolddownWaitCount[i][4]){
                        case UART_HOLD_DOWN_WAIT_TIME:
                            //actStart();   [NO repeat press on hold]
                            break;
                        case 0:
                            actStart();
                            uartHolddownWaitCount[i][4]++;
                            break;
                        default:
                            uartHolddownWaitCount[i][4]++;
                            break;
                    }
                } else {uartHolddownWaitCount[i][4]=0;}
                
                //any: select   [NO repeat press on hold]
                if(ucmi.p[i].readButton(uartControls[i][5])){
                    switch (uartHolddownWaitCount[i][5]){
                        case UART_HOLD_DOWN_WAIT_TIME:
                            //actSelect();   [NO repeat press on hold]
                            break;
                        case 0:
                            actSelect();
                            uartHolddownWaitCount[i][5]++;
                            break;
                        default:
                            uartHolddownWaitCount[i][5]++;
                            break;
                    }
                } else {uartHolddownWaitCount[i][5]=0;}
                
                //any: quit   [NO repeat press on hold]
                if(ucmi.p[i].readButton(uartControls[i][6])){
                    switch (uartHolddownWaitCount[i][6]){
                        case UART_HOLD_DOWN_WAIT_TIME:
                            //actQuit();   [NO repeat press on hold]
                            break;
                        case 0:
                            actQuit();
                            uartHolddownWaitCount[i][6]++;
                            break;
                        default:
                            uartHolddownWaitCount[i][6]++;
                            break;
                    }
                } else {uartHolddownWaitCount[i][6]=0;}
                
            }
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
            botSpeedPercent = 100;
            if(botDir == 1){
                player2.moveDn(botSpeedPercent);
            } else if(botDir == -1){
                player2.moveUp(botSpeedPercent);
            }
            ///
            int quarterPaddle = Paddle.HEIGHT/4;
            Ball closestBall = null;
            for (Ball ball : balls) {
                if(!ball.isDead){
                    if ((closestBall==null) || (Math.abs(ball.x+ball.diam - player2.getLX()) <  Math.abs(closestBall.x+closestBall.diam - player2.getLX()))){
                        closestBall = ball;
                    }
                }
            }
            if(closestBall != null){    //otherwise all balls are presumed dead
                if(botMoves < 1){
                    if(closestBall.getCtrY() < player2.y+quarterPaddle){
                        //player2.moveUp(botSpeedPercent);
                        botDir = -1;
                        botMoves++;
                    } else if (closestBall.getCtrY() > player2.y + 3*quarterPaddle){
                        //player2.moveDn(botSpeedPercent);
                        botDir = 1;
                        botMoves++;
                    } else {
                        botDir = 0;
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
                        switch (botDifficulty) {
                            case 1:
                                botCooldown = 12;
                                break;
                            case 2:
                                botCooldown = 7;
                                botSpeedPercent = 120;
                                break;
                            case 3:
                                botCooldown = 1;
                                botSpeedPercent = 170;
                                break;
                            case 4:
                                botCooldown = -100;
                                botMoves = 0;
                                botSpeedPercent = 5300;
                                break;
                            default:
                            case 0:
                                botCooldown = 22;
                                botSpeedPercent = 80;
                                break;
                        }
                    }
                }
            }
                    
        }
        //uart controls (PADDLE only)
        // ucmi version (both players)
        if (ucmi.isPortConnected && !disableCMInput){/////////////////BUG (seems like it)
            //---- [0],[1]    PADDLE CONTROLS
            //paddle control ("UP" and "DOWN") ==> index [0] and [1]
            if(uartControls[1][0].isAnalogAxis()){
                int val1 = ucmi.p[1].readAnalogAxis(uartControls[1][0]);  //0 to 255
                val1 = val1 - 128;
                double percentage = val1;  //(val1/100)*100;      //i made maximum analog stick 100 (instead of 127)
                if(percentage > 0){
                    if(percentage>100){
                        percentage = 100;
                    }
                    player1.moveUp(percentage);
                } else if (percentage < 0){
                    percentage = -percentage;
                    if(percentage>100){
                        percentage = 100;
                    }
                    player1.moveDn(percentage);
                }
            } else {
                if(ucmi.p[1].readButton(uartControls[1][0])){
                    player1.moveUp();
                }
                if(ucmi.p[1].readButton(uartControls[1][1])){
                    player1.moveDn();
                }
                
            }
            
            //Player2
            if(!bot){
                if(uartControls[2][0].isAnalogAxis()){
                    int val2 = ucmi.p[2].readAnalogAxis(uartControls[2][0]);  //0 to 255
                    val2 = val2 - 128;
                    double percentage = val2;  //(val1/100)*100;      //i made maximum analog stick 100 (instead of 127)
                    if(percentage > 0){
                        if(percentage>100){
                            percentage = 100;
                        }
                        player2.moveUp(percentage);
                    } else if (percentage < 0){
                        percentage = -percentage;
                        if(percentage>100){
                            percentage = 100;
                        }
                        player2.moveDn(percentage);
                    }
                } else {
                    if(ucmi.p[2].readButton(uartControls[2][0])){
                        player2.moveUp();
                    }
                    if(ucmi.p[2].readButton(uartControls[2][1])){
                        player2.moveDn();
                    }
                }
            }
        }
        
        
        
        /////update screen
        //remove dead balls, update alive ones
        int nextBallRequestCount = 0;
        int aliveBalls = 0;
        for (Ball ball : balls) {
            //0. count requests for next balls
            if(ball.triggerNextBall){
                nextBallRequestCount++;
                ball.nextBallTriggered = true;
                ball.triggerNextBall = false;
            }
            
            //1. count alive ones
            if(!ball.isDead){
                aliveBalls++;
            }
        }
        //System.out.println("After 0. and 1.");  //debug
        //System.out.println("aliveBalls = " + aliveBalls);  //debug
        //System.out.println("nextBallR.Cnt = " + nextBallRequestCount);  //debug
        //System.out.println("balls.size() = " + balls.size());   //debug
        
        //2. grant next ball requests
        for (Ball ball : balls) {
            if(ball.isDead && nextBallRequestCount > 0){
                nextBallRequestCount--;
                aliveBalls++;
                ball.spawn();
            }
        }
        //System.out.println("After 2a");  //debug
        //System.out.println("aliveBalls = " + aliveBalls);  //debug
        //System.out.println("nextBallR.Cnt = " + nextBallRequestCount);  //debug
        //System.out.println("balls.size() = " + balls.size());   //debug
        
        while(nextBallRequestCount > 0){
            balls.add(new Ball(this));  //since auto spawn on creation
            nextBallRequestCount--;
            aliveBalls++;
        }
        //System.out.println("After 2b");  //debug
        //System.out.println("aliveBalls = " + aliveBalls);  //debug
        //System.out.println("nextBallR.Cnt = " + nextBallRequestCount);  //debug
        //System.out.println("balls.size() = " + balls.size());   //debug
        
        //3. if all dead, spawn new ball
        if(aliveBalls <= 0){
            if(balls.get(0) != null){
                balls.get(0).spawn();
            } else {
                balls.clear();
                balls.add(new Ball(this));
            }
        }
        //System.out.println("After 3");  //debug
        //System.out.println("aliveBalls = " + aliveBalls);  //debug
        //System.out.println("nextBallR.Cnt = " + nextBallRequestCount);  //debug
        //System.out.println("balls.size() = " + balls.size());   //debug
        
        //4. update each ball
        for (Ball ball : balls) {
            if(!ball.isDead){
                ball.oldx = ball.x;
                ball.oldy = ball.y;
                //// redraw ball
                ball.update(player1, player2);
                /////////
            }
        }
        //System.out.println("After 4");  //debug
        //System.out.println("aliveBalls = " + aliveBalls);  //debug
        //System.out.println("nextBallR.Cnt = " + nextBallRequestCount);  //debug
        //System.out.println("balls.size() = " + balls.size());   //debug
        //System.out.println("========================"); //debug
        
        
        
        
        
        
        //ball2.oldx = ball2.x;
        //ball2.oldy = ball2.y;
        //// redraw ball
        //ball2.update(player1, player2);
    }
    
    
    
    ///////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////
    ////////////      CONTROL ACTIONS
    ////////////////////////////////////
    private void actRight() {
        //any right
        System.out.println("actR."); //debug
        
        if(gameStatus == 0){
            if(selectingDifficulty){
                botDifficulty++;
                if(botDifficulty > 4){
                    botDifficulty = 0;
                }
            } else {
                scoreLimit ++;
            }
        } 
    }

    private void actLeft() {
        //any left
        System.out.println("actL");  //debug
        
        if(gameStatus == 0){
            if(selectingDifficulty){
                botDifficulty--;
                if(botDifficulty < 0){
                    botDifficulty = 4;
                }
            } else if (scoreLimit > 1){
                scoreLimit --;
            }
        }
    }

    private void actQuit() {
        //any quit
        System.out.println("actQ.");  //debug
        
        if(gameStatus == 1 || gameStatus == 3){
            gameStatus = 0;
            selectingDifficulty = false;
        }
    }

    private void actStart() {
        //any start
        System.out.println("actSt.");     //debug
        
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
    }

    private void actSelect() {
        //any select
        System.out.println("actSel.");    //debug
        
        if(gameStatus == 0){
            bot = true;
            selectingDifficulty = true;
        }
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
        //vvvv Remember: Controls indexing Starts with [0]; players indexing starts with [1].
        //String[] genControlNames = new String[]{"Up","Down","Left","Right","Start","Select","Quit"}; <--from top-((as of 10:34 pc clk))]
        int id = e.getKeyCode();
        //System.out.println("id = " + id);
        //System.out.println("pressed: "+e.getKeyText(id));
        //System.out.println("kybdCtrls[1]:"+Arrays.toString(kybdControls[1]));
        //System.out.println("kybdCtrls[2]:"+Arrays.toString(kybdControls[2]));
        
        if(id == kybdControls[1][0]){//p1 up
            w = true;
        }
        if(id == kybdControls[1][1]){//p1 down
            s = true;
        }
        if(id == kybdControls[2][0]){//p2 up
            up = true;
        }
        if(id == kybdControls[2][1]){//p2 down
            dn = true;
        }
        if(id == kybdControls[1][2] || id == kybdControls[2][2]){//any left
            actLeft();
        }
        if(id == kybdControls[1][3] || id == kybdControls[2][3]){//any right
            actRight();
        } 
    }
    

    @Override
    public void keyReleased(KeyEvent e) {
        //vvvv Remember: Controls indexing Starts with [0]; players indexing starts with [1].
        //String[] genControlNames = new String[]{"Up","Down","Left","Right","Start","Select","Quit"}; <--from top-((as of 10:34 pc clk))]
        int id = e.getKeyCode();
        
        if(id == kybdControls[1][0]){//p1 up
            w = false;
        } 
        if(id == kybdControls[1][1]){//p1 dn
            s = false;
        } 
        if(id == kybdControls[2][0]){//p2 up
            up = false;
        } 
        if(id == kybdControls[2][1]){//p2 dn
            dn = false;
        } 
        if(id == kybdControls[1][5]  ||  id == kybdControls[2][5]){//any select
            actSelect();
        } 
        if(id == kybdControls[1][4]  ||  id == kybdControls[2][4]){//any start
            actStart();
        } 
        if(id == kybdControls[1][6]  ||  id == kybdControls[2][6]){//any Quit
            actQuit();
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
