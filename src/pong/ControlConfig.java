/*
 *  © 2019 by Patrick Matthew Chan
 *  File: ControlConfig.java
 *  Package: pong
 *  Description: The ControlConfig class.
 */
package pong;
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
import java.lang.reflect.Field;//optional,for toString shortcut
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import static pong.Pong.UART_HOLD_DOWN_WAIT_TIME;
/* @author Patrick Matthew J. Chan*/
public class ControlConfig implements ActionListener,KeyListener{
    //fields
    public static ControlConfig ctrlConfig;
    public JFrame jframe;
    public int width = 700, height = 700;
    public Pong pong;
    public ConfigRenderer renderer;
    //////////////////
    int screenNo = 0;   //0-splash Menu; 1-controller changing
    int splashOptionNo = 0; //for splash screen menu
    int setupPNo = 1;   //screen 1 setup
    int setupController = 0;    //screen 1 setup.   //0-kybd, 1-uart
    String curScreen1Key = "";
    int curScreen1Code = -1;    //keyboard code.  -1 means [not set]
    CM curScreen1CM = null;     //CM code.  null means [not set]
    public static final int ITERS_TO_SHOW_KEY = 20;
    int curShowKeyIters = 0;    
    int curControlsIdx = 0;  //to iterate over each game control
    int UpCtrlIdx;
    int DownCtrlIdx;
    int LeftCtrlIdx;
    int RightCtrlIdx;
    int EnterCtrlIdx;
    int maxPlayers;
    //to track changes in UART input
    PlayerState oldP;
    PlayerState newP;
    
    

    //methods
    public ControlConfig(Pong pong) {
        this.pong = pong;
        //this.ctrlConfig = pong.ctrlConfig;
    }
    
    public void openSettings(int maxPlayers, int UpCtrlIdx, int DownCtrlIdx, int LeftCtrlIdx, int RightCtrlIdx, int EnterCtrlIdx){
        //initialize basic controls to control settings
        this.maxPlayers = maxPlayers;
        ////
        this.UpCtrlIdx = UpCtrlIdx;
        this.DownCtrlIdx = DownCtrlIdx;
        this.LeftCtrlIdx = LeftCtrlIdx;
        this.RightCtrlIdx = RightCtrlIdx;
        this.EnterCtrlIdx = EnterCtrlIdx;
        /////////////////////////////
        //timer
        Timer timer = new Timer(20, this);  //loops renderer
        //jframe
        jframe = new JFrame("Control Settings");
        renderer = new ConfigRenderer();
        
        jframe.setSize(width + 15,height + 38);  //JFrame window size
        jframe.setVisible(true);
        jframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jframe.add(renderer);
        jframe.addKeyListener(this);
        
        timer.start();
    }
    
    //render graphics on screen
    public void render(Graphics2D g){
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);    //draw the black bg
        //for Graphics2D:
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //System.out.println("rendering control config");
        // screenNo = 0 case is below.
        //System.out.println("render: screenNo="+screenNo);
        
        if (screenNo == 1){
            if(curControlsIdx < pong.gameControls[setupPNo].length){
                Font TopFont = new Font("Courier", 1, 55);
                String TopText1 = "~Controls Change~";
                float TopFontX = width/2 - g.getFontMetrics(TopFont).stringWidth(TopText1)/2;
                float TopFontY = height/8 + g.getFontMetrics(TopFont).getAscent()/2;

                g.setColor(Color.WHITE);
                g.setFont(TopFont);
                g.drawString(TopText1, TopFontX, TopFontY);

                //underline
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(4f));
                g.drawLine((int)TopFontX, (int)TopFontY+5, 
                        (int)TopFontX+g.getFontMetrics(TopFont).stringWidth(TopText1), (int)TopFontY+5);
                /////////////////////


                Font DescFont = new Font("Courier",Font.PLAIN, 25);
                String DescText1 = "Player: "+setupPNo;
                StringBuilder temp = new StringBuilder("Controller: ");
                //uart [actually connected, enabled] parsing handled by ActEnter
                switch(setupController){
                    case 0:
                        temp.append("Keyboard");
                        break;
                    case 1:
                        temp.append("External (Con Mgr)");
                        break;
                }
                String DescText2 = temp.toString();

                g.setColor(Color.WHITE);
                g.setFont(DescFont);
                g.drawString(DescText1, width/2 - g.getFontMetrics(DescFont).stringWidth(DescText1)/2, (int)(TopFontY + g.getFontMetrics(DescFont).getAscent()*1.5));
                g.drawString(DescText2, width/2 - g.getFontMetrics(DescFont).stringWidth(DescText2)/2, (int)(TopFontY + g.getFontMetrics(DescFont).getAscent()*2.6));
                ///////////////////////



                Font MainFont = new Font("Courier",Font.PLAIN, 35);
                String MainText1 = "Please press the new Assignment for:";
                String MainText2 = "["+pong.gameControls[setupPNo][curControlsIdx]+"]";
                ///
                Font MiniFont = new Font("Courier",Font.PLAIN, 15);
                    // vvv  CHANGE LATER ///////////////////////////////////////////////////////////////////////////////////////// << TODO !!
                String MiniText1 = "[Move Analog Stick to assign this set of directions there.";
                String MiniText2 = "...or, you may choose to individually assign each direction to a button.]";

                g.setColor(Color.WHITE);
                g.setFont(MainFont);
                g.drawString(MainText1, width/2 - g.getFontMetrics(MainFont).stringWidth(MainText1)/2, (int)(height/2 - g.getFontMetrics(MainFont).getAscent()/4));
                g.drawString(MainText2, width/2 - g.getFontMetrics(MainFont).stringWidth(MainText2)/2, (int)(height/2 + g.getFontMetrics(MainFont).getAscent()));
                ///
                g.setColor(Color.WHITE);
                g.setFont(MiniFont);
                g.drawString(MiniText1, width/2 - g.getFontMetrics(MiniFont).stringWidth(MiniText1)/2, (int)(height/2 + g.getFontMetrics(MainFont).getAscent()*5/4 + g.getFontMetrics(MiniFont).getAscent()*1.5));
                g.drawString(MiniText2, width/2 - g.getFontMetrics(MiniFont).stringWidth(MiniText2)/2, (int)(height/2 + g.getFontMetrics(MainFont).getAscent()*5/4 + g.getFontMetrics(MiniFont).getAscent()*2.6));
                /////////////////////////



                Font KeyFont = new Font("Courier", 1, 50);
                String KeyText = curScreen1Key;

                g.setColor(Color.CYAN);
                g.setFont(KeyFont);
                g.drawString(KeyText, width/2 - g.getFontMetrics(KeyFont).stringWidth(KeyText)/2, (int)(3*height/4 + g.getFontMetrics(KeyFont).getAscent()/2));
                
                if(curScreen1Key.length() > 0){
                    curShowKeyIters++;
                    if(curShowKeyIters >= ITERS_TO_SHOW_KEY){
                        curShowKeyIters = 0;
                        ////////////
                        if(setupController == 0){   //keyboard
                            pong.kybdControls[setupPNo][curControlsIdx] = curScreen1Code;
                            curControlsIdx++;                        
                            ///
                            curScreen1Key = "";
                            curScreen1Code = -1;
                        } else if (setupController == 1){   //controller manager
                            pong.uartControls[setupPNo][curControlsIdx] = curScreen1CM;
                            if(curScreen1CM.isAnalogAxis()){
                                pong.uartControls[setupPNo][curControlsIdx+1] = curScreen1CM;
                                ///
                                if((int)(pong.directionalGroupNo[setupPNo][curControlsIdx+1]) == (int)(pong.directionalGroupNo[setupPNo][curControlsIdx+2])){
                                    pong.uartControls[setupPNo][curControlsIdx+2] = curScreen1CM.getCorrespondingXorY();    //assumed not null
                                    pong.uartControls[setupPNo][curControlsIdx+3] = curScreen1CM.getCorrespondingXorY();    //assumed not null
                                    ///
                                    curControlsIdx = curControlsIdx+4;
                                } else {
                                    ///
                                    curControlsIdx = curControlsIdx+2;
                                }
                            } else {
                                ///
                                curControlsIdx++; 
                            }                       
                            ///
                            curScreen1Key = "";
                            curScreen1CM = null;
                        }
                    }
                }
            } else {
                screenNo = 0;
            }
        }
          /// /// /// /// /// /// /// /// /// /// /// /// /// ///
         /// /// /// /// /// /// /// /// /// /// /// /// /// ///
        /// /// /// /// /// /// /// /// /// /// /// /// /// ///
        if(screenNo == 0){
            Font TopFont = new Font("Courier", 1, 55);
            String TopText1 = "Control Settings";
            float TopFontX = width/2 - g.getFontMetrics(TopFont).stringWidth(TopText1)/2;
            float TopFontY = height/8 + g.getFontMetrics(TopFont).getAscent()/2;

            g.setColor(Color.WHITE);
            g.setFont(TopFont);
            g.drawString(TopText1, TopFontX, TopFontY);
            
            //underline
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(4f));
            g.drawLine((int)TopFontX, (int)TopFontY+5, 
                    (int)TopFontX+g.getFontMetrics(TopFont).stringWidth(TopText1), (int)TopFontY+5);
            /////////////////////
            
            
            
            Font MenuFont = new Font("Courier", Font.PLAIN, 30);
            float firstMenuItemY = height*3/8;
            String MenuTexts[] = new String[6];
                //texts[0]
            MenuTexts[0] = "Read Controller Manager Input:";
                //texts[1]
            StringBuilder temp = new StringBuilder();
            if(pong.disableCMInput){
                temp.append(" ENABLE    [DISABLE]");
            } else {
                temp.append("[ENABLE]    DISABLE ");
            }
            MenuTexts[1] = temp.toString();
                //texts[2]
            MenuTexts[2] = "Change Controls for:                    ";
                //texts[3]
            MenuTexts[3] = "Player:  <<"+setupPNo+">>";
                //texts[4]
            temp = new StringBuilder("Controller:  ");
            if(pong.ucmi.isPortConnected && !pong.disableCMInput){
                temp.append("<<");
                switch(setupController){
                    case 1:
                        temp.append("Configure External");
                        break;
                    case 0:
                    default:
                        temp.append("Configure Keyboard");
                        break;
                }
                temp.append(">>");
            } else {
                temp.append("Keyboard");
            }
            MenuTexts[4] = temp.toString();
                //tests[5]
            MenuTexts[5] = "[Start Configuration]";
            ////
            ////
            //place on screen
            for (int i = 0; i < MenuTexts.length; i++) {
                String MenuText = MenuTexts[i];
                /////
                if((i == 0 || i == 1) && splashOptionNo == 0){
                    g.setColor(Color.YELLOW);
                } else if (i!=2  && i == splashOptionNo + 2){   //can't "choose" MenuTexts[2]
                    g.setColor(Color.YELLOW);
                } else {
                    g.setColor(Color.WHITE);
                }
                /////
                g.setFont(MenuFont);
                if(i == 0 || i==1){
                    g.drawString(MenuText, width/2 - g.getFontMetrics(MenuFont).stringWidth(MenuText)/2, (int)(firstMenuItemY + (i*1.1)*g.getFontMetrics(MenuFont).getAscent()));
                } else if(i == 2){
                    g.drawString(MenuText, width/8, (int)(firstMenuItemY + (1+i*1.1)*g.getFontMetrics(MenuFont).getAscent()));
                } else if(i == 5){
                    g.drawString(MenuText, width/2 - g.getFontMetrics(MenuFont).stringWidth(MenuText)/2, (int)(firstMenuItemY + (1.5+i*1.1)*g.getFontMetrics(MenuFont).getAscent()));
                } else {
                    g.drawString(MenuText, 3*width/16, (int)(firstMenuItemY + (1+i*1.1)*g.getFontMetrics(MenuFont).getAscent()));
                }             
            }
        }
        
        
        
        ///////////////////////////////////////////////////////////////////////////
        ////////////////////////////  SINCE THIS SEEMS TO GET CALLED EVERY LOOP:
        /////////////////////////////////////////////////////////////////////////
        //System.out.println("wee.");   //debug
        // READ UART Inputs
        if(screenNo == 0){
            if (pong.ucmi.isPortConnected && !pong.disableCMInput){
                ////////------------ OTHER CONTROLS ["accidental" repeated press is  bad, so timeout applied]
                    //vvvv Remember: Controls indexing Starts with [0]; players indexing starts with [1].
                    //String[] genControlNames = new String[]{"Up","Down","Left","Right","Start","Select","Quit"}; <--from top-((as of 10:34 pc clk))]
                for(int i=1;i<=maxPlayers;i++){
                    //any: left,  any: right     [may repeat press]
                    if(pong.uartControls[i][LeftCtrlIdx].isAnalogAxis()){
                        int val1 = pong.ucmi.p[i].readAnalogAxis(pong.uartControls[i][LeftCtrlIdx]);  //0 to 255
                        val1 = val1 - 128;
                        //System.out.println("val1 = " + val1);
                        if(val1 > 64){
                            switch (pong.uartHolddownWaitCount[i][RightCtrlIdx]){
                                case UART_HOLD_DOWN_WAIT_TIME:
                                    actRightScr0();
                                    break;
                                case 0:
                                    actRightScr0();
                                    pong.uartHolddownWaitCount[i][RightCtrlIdx]++;
                                    break;
                                default:
                                    pong.uartHolddownWaitCount[i][RightCtrlIdx]++;
                                    break;
                            }
                        } else {pong.uartHolddownWaitCount[i][RightCtrlIdx]=0;}
                        /////
                        if (val1 < -64){
                            switch (pong.uartHolddownWaitCount[i][LeftCtrlIdx]){
                                case UART_HOLD_DOWN_WAIT_TIME:
                                    actLeftScr0();
                                    break;
                                case 0:
                                    actLeftScr0();
                                    pong.uartHolddownWaitCount[i][LeftCtrlIdx]++;
                                    break;
                                default:
                                    pong.uartHolddownWaitCount[i][LeftCtrlIdx]++;
                                    break;
                            }
                        } else {pong.uartHolddownWaitCount[i][LeftCtrlIdx]=0;}
                    } else {
                        if(pong.ucmi.p[i].readButton(pong.uartControls[i][RightCtrlIdx])){
                            switch (pong.uartHolddownWaitCount[i][RightCtrlIdx]){
                                case UART_HOLD_DOWN_WAIT_TIME:
                                    actRightScr0();
                                    break;
                                case 0:
                                    actRightScr0();
                                    pong.uartHolddownWaitCount[i][RightCtrlIdx]++;
                                    break;
                                default:
                                    pong.uartHolddownWaitCount[i][RightCtrlIdx]++;
                                    break;
                            }
                        } else {pong.uartHolddownWaitCount[i][RightCtrlIdx]=0;}
                        /////
                        if(pong.ucmi.p[i].readButton(pong.uartControls[i][LeftCtrlIdx])){
                            switch (pong.uartHolddownWaitCount[i][LeftCtrlIdx]){
                                case UART_HOLD_DOWN_WAIT_TIME:
                                    actLeftScr0();
                                    break;
                                case 0:
                                    actLeftScr0();
                                    pong.uartHolddownWaitCount[i][LeftCtrlIdx]++;
                                    break;
                                default:
                                    pong.uartHolddownWaitCount[i][LeftCtrlIdx]++;
                                    break;
                            }
                        } else {pong.uartHolddownWaitCount[i][LeftCtrlIdx]=0;}
                    }
                    
                    
                    //any: up,  any: down     [may repeat press]
                    if(pong.uartControls[i][UpCtrlIdx].isAnalogAxis()){
                        int val1 = pong.ucmi.p[i].readAnalogAxis(pong.uartControls[i][UpCtrlIdx]);  //0 to 255
                        val1 = val1 - 128;
                        //System.out.println("val1 = " + val1);
                        if(val1 > 64){
                            switch (pong.uartHolddownWaitCount[i][UpCtrlIdx]){
                                case UART_HOLD_DOWN_WAIT_TIME:
                                    actUpScr0();
                                    break;
                                case 0:
                                    actUpScr0();
                                    pong.uartHolddownWaitCount[i][UpCtrlIdx]++;
                                    break;
                                default:
                                    pong.uartHolddownWaitCount[i][UpCtrlIdx]++;
                                    break;
                            }
                        } else {pong.uartHolddownWaitCount[i][UpCtrlIdx]=0;}
                        /////
                        if (val1 < -64){
                            switch (pong.uartHolddownWaitCount[i][DownCtrlIdx]){
                                case UART_HOLD_DOWN_WAIT_TIME:
                                    actDownScr0();
                                    break;
                                case 0:
                                    actDownScr0();
                                    pong.uartHolddownWaitCount[i][DownCtrlIdx]++;
                                    break;
                                default:
                                    pong.uartHolddownWaitCount[i][DownCtrlIdx]++;
                                    break;
                            }
                        } else {pong.uartHolddownWaitCount[i][DownCtrlIdx]=0;}
                    } else {
                        if(pong.ucmi.p[i].readButton(pong.uartControls[i][UpCtrlIdx])){
                            switch (pong.uartHolddownWaitCount[i][UpCtrlIdx]){
                                case UART_HOLD_DOWN_WAIT_TIME:
                                    actUpScr0();
                                    break;
                                case 0:
                                    actUpScr0();
                                    pong.uartHolddownWaitCount[i][UpCtrlIdx]++;
                                    break;
                                default:
                                    pong.uartHolddownWaitCount[i][UpCtrlIdx]++;
                                    break;
                            }
                        } else {pong.uartHolddownWaitCount[i][UpCtrlIdx]=0;}
                        /////
                        if(pong.ucmi.p[i].readButton(pong.uartControls[i][DownCtrlIdx])){
                            switch (pong.uartHolddownWaitCount[i][DownCtrlIdx]){
                                case UART_HOLD_DOWN_WAIT_TIME:
                                    actDownScr0();
                                    break;
                                case 0:
                                    actDownScr0();
                                    pong.uartHolddownWaitCount[i][DownCtrlIdx]++;
                                    break;
                                default:
                                    pong.uartHolddownWaitCount[i][DownCtrlIdx]++;
                                    break;
                            }
                        } else {pong.uartHolddownWaitCount[i][DownCtrlIdx]=0;}
                    }
                    
                    
                    //any:  start   [NO repeat press on hold]
                    //System.out.println("pong.uartControls[i][EnterCtrlIdx] = " + pong.uartControls[i][EnterCtrlIdx]);
                    //System.out.println("pong.uartHolddownWaitCount[i][EnterCtrlIdx] = " + pong.uartHolddownWaitCount[i][EnterCtrlIdx]);
                    if(pong.ucmi.p[i].readButton(pong.uartControls[i][EnterCtrlIdx])){
                        /// [SPECIAL CASE]  (since executing this stops loop from executnig this block.)
                        for (int j = 0; j < pong.uartHolddownWaitCount.length; j++) {
                            for (int k = 0; k < pong.uartHolddownWaitCount[j].length; k++) {
                                pong.uartHolddownWaitCount[j][k] = 0;
                            }
                        }
                        // what it executes
                        actEnterScr0();
                        //in next iteration, code goes thru screenNo = 1 block instead
                    } else {pong.uartHolddownWaitCount[i][EnterCtrlIdx]=0;}
                }
            }
        } else if (screenNo == 1 && setupController == 1 && curScreen1Key.length() == 0){
            newP = pong.ucmi.p[setupPNo].copy();    //we need to COPY (deep), otherwise, only the pointer gets copied, no data is stored.
            //for the case of analog sticks
            CM tempKey = PlayerState.readStickChange(oldP, newP);
            if(tempKey != null){
                double grpNo = pong.directionalGroupNo[setupPNo][curControlsIdx];
                double decPart = grpNo - (int)grpNo;
                if(decPart > 0 && decPart <  0.2){
                    //it is the "up" in a directional set
                    curScreen1CM = tempKey;
                    curScreen1Key = curScreen1CM.getName();    //cleared by render()
                }
            } else {
            //for the case of buttons
                tempKey = PlayerState.readBtnPressed(oldP, newP);
                if(tempKey != null){
                    curScreen1CM = tempKey;
                    curScreen1Key = curScreen1CM.getName();    //cleared by render()
                }
            }
            oldP = newP;     
        }
    }
    
    
    ///
    //actions on button press
    private void actUpScr0(){
        //System.out.println("actUp");
        //if(screenNo == 0){
        //4 options only
        splashOptionNo--;
        if(splashOptionNo<0){
            splashOptionNo = 0;
        }
        //}
    }

    private void actDownScr0() {
        //System.out.println("actDown");
        //if(screenNo == 0){
        //4 options only
        splashOptionNo++;
        if(splashOptionNo>3){
            splashOptionNo = 3;
        }
        //}
    }

    private void actLeftScr0() {
        //System.out.println("actLeft");
        //if(screenNo == 0){
        switch(splashOptionNo){
            case 0:
                pong.disableCMInput = !pong.disableCMInput;
                break;

            case 1:
                //maxPlayers PLAYERS
                setupPNo--;
                if(setupPNo < 1){
                    setupPNo = maxPlayers;
                }
                break;

            case 2:
                setupController = (2+setupController-1)%2;    //2 choices
                break;
        }
        //}
    }

    private void actRightScr0() {
        //System.out.println("actRight");
        //if(screenNo == 0){
        switch(splashOptionNo){
            case 0:
                pong.disableCMInput = !pong.disableCMInput;
                break;

            case 1:
                //maxPlayers PLAYERS
                setupPNo++;
                if(setupPNo > maxPlayers){
                    setupPNo = 1;
                }
                break;

            case 2:
                setupController = (setupController+1)%2;    //2 choices
                break;
        }
        //}
    }

    private void actEnterScr0() {
        System.out.println("actEnter");
        //if(screenNo == 0){
        if(splashOptionNo == 3){
            screenNo = 1;
            curShowKeyIters = 0;
            curControlsIdx = 0;
            curScreen1Key = "";
            if(setupController == 1  && (!pong.ucmi.isPortConnected || pong.disableCMInput)){
                setupController = 0;
            } else {
                //setupController = 1  (understood)
                oldP = pong.ucmi.p[setupPNo].copy();
            }
        }
        System.out.println("screenNo = " + screenNo);
        //}
    }
    
    
    // <editor-fold defaultstate="collapsed" desc="toString shortcut">
    //++toString shortcut
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Field f: getClass().getDeclaredFields()) {
            try {
            result
            .append(f.getName())
            .append(" : ")
            .append(f.get(this))
            .append(System.getProperty("line.separator"));
            }
            catch (IllegalStateException ise) {
                result
                .append(f.getName())
                .append(" : ")
                .append("[cannot retrieve value]")
                .append(System.getProperty("line.separator"));
            }
            // nope
            catch (IllegalAccessException iae) {}
        }
        return result.toString();
    }
    // </editor-fold>

    @Override
    public void actionPerformed(ActionEvent e) {
        renderer.repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int id = e.getKeyCode();
        //System.out.println("pressed:"+KeyEvent.getKeyText(id));
        
        if(screenNo == 0){
            boolean isFound = false;
            
            for(int i=1; !isFound && i<=maxPlayers; i++){       //player indexing starts at 1
                if(id == pong.kybdControls[i][UpCtrlIdx]){//any up
                    actUpScr0();
                    isFound = true;
                }
                if(id == pong.kybdControls[i][DownCtrlIdx]){//any down
                    actDownScr0();
                    isFound = true;
                }
                if(id == pong.kybdControls[i][LeftCtrlIdx]){//any left
                    actLeftScr0();
                    isFound = true;
                }
                if(id == pong.kybdControls[i][RightCtrlIdx]){//any right
                    actRightScr0();
                    isFound = true;
                } 
                if(id == pong.kybdControls[i][EnterCtrlIdx]){//any enter
                    actEnterScr0();
                    isFound = true;
                } 
            }
        } else if (screenNo == 1 && setupController == 0 && curScreen1Key.length() == 0){
            curScreen1Code = id;
            curScreen1Key = KeyEvent.getKeyText(id);    //cleared by render()
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int id = e.getKeyCode();
        System.out.println("released:"+KeyEvent.getKeyText(id));
    }
}
//Alt+Insert for constructors,Getters&Setters [after declaring fields]
