/*
 *  © 2019 by Patrick Matthew Chan
 *  File: PlayerState.java
 *  Package: uart_try
 *  Description: The PlayerState class.
 */
package pong;
import java.lang.reflect.Field;//optional,for toString shortcut
/* @author Patrick Matthew J. Chan [LBYCP24-EQ1]*/
public class PlayerState {
    //fields
    //int pNo = 0;
    int conType = 0;
    int lasX = 0;   //left analog stick
    int lasY = 0;
    int rasX = 0;   //right analog stick
    int rasY = 0;
    boolean fbA = false;
    boolean fbB = false;
    boolean fbX = false;
    boolean fbY = false;
    boolean dpD = false;
    boolean dpR = false;
    boolean dpL = false;
    boolean dpU = false;
    boolean btnStart = false;
    boolean btnSelect = false;
    boolean stickL = false;
    boolean stickR = false;
    boolean shL = false;    //shoulder button
    boolean shR = false;
    boolean trigL = false;    //trigger button
    boolean trigR = false;

    
    //methods
    public int getBit(byte input,int position){ //LSB is posn 0
       return (input >> position) & 1;
    }
    
    boolean boolFrBit(int input){   //1/0 to true/false  [logic similar to C]
        return input!=0;
    }
    
    boolean boolFrByte(byte input,int position){   //1/0 to true/false  [logic similar to C]
        return boolFrBit(getBit(input,position));
    }
    
    void type1(byte[] byteBuffer){
        //pNo = byteBuffer[2] >> 4;
        conType 	= byteBuffer[2] & 0b00001111;
        lasX 		= byteBuffer[3];   //left analog stick
        lasY 		= byteBuffer[4];
        rasX 		= byteBuffer[5];   //right analog stick
        rasY 		= byteBuffer[6];
        fbA 		= boolFrByte(byteBuffer[7],7);
        fbB 		= boolFrByte(byteBuffer[7],6);
        fbX 		= boolFrByte(byteBuffer[7],5);
        fbY 		= boolFrByte(byteBuffer[7],4);
        dpD 		= boolFrByte(byteBuffer[7],3);
        dpR 		= boolFrByte(byteBuffer[7],2);
        dpL 		= boolFrByte(byteBuffer[7],1);
        dpU 		= boolFrByte(byteBuffer[7],0);
        btnStart 	= boolFrByte(byteBuffer[8],7);
        btnSelect 	= boolFrByte(byteBuffer[8],6);
        stickL 		= boolFrByte(byteBuffer[8],5);
        stickR 		= boolFrByte(byteBuffer[8],4);
        shL 		= boolFrByte(byteBuffer[8],3);    //shoulder button
        shR 		= boolFrByte(byteBuffer[8],2);
        trigL 		= boolFrByte(byteBuffer[8],1);    //trigger button
        trigR 		= boolFrByte(byteBuffer[8],0);
    }
    
    //reqByteNum = requested Byte Number.
    void type2(int reqByteNum,byte[] byteBuffer){
        //pNo = byteBuffer[2] >> 4;
        conType 	= byteBuffer[2] & 0b00001111;
        switch (reqByteNum){
            case 3:
                lasX 		= byteBuffer[3];   //left analog stick
                break;
                
            case 4:
                lasY 		= byteBuffer[3];
                break;
                
            case 5:
                rasX 		= byteBuffer[3];   //right analog stick
                break;
                
            case 6:
                rasY 		= byteBuffer[3];
                break;
                
            case 7:
                fbA 		= boolFrByte(byteBuffer[3],7);
                fbB 		= boolFrByte(byteBuffer[3],6);
                fbX 		= boolFrByte(byteBuffer[3],5);
                fbY 		= boolFrByte(byteBuffer[3],4);
                dpD 		= boolFrByte(byteBuffer[3],3);
                dpR 		= boolFrByte(byteBuffer[3],2);
                dpL 		= boolFrByte(byteBuffer[3],1);
                dpU 		= boolFrByte(byteBuffer[3],0);
                break;
                
            case 8:
                btnStart 	= boolFrByte(byteBuffer[3],7);
                btnSelect 	= boolFrByte(byteBuffer[3],6);
                stickL 		= boolFrByte(byteBuffer[3],5);
                stickR 		= boolFrByte(byteBuffer[3],4);
                shL 		= boolFrByte(byteBuffer[3],3);    //shoulder button
                shR 		= boolFrByte(byteBuffer[3],2);
                trigL 		= boolFrByte(byteBuffer[3],1);    //trigger button
                trigR 		= boolFrByte(byteBuffer[3],0);
                break;
                                
            case 1:
            case 2:
            case 9:
            default:
                //case 1,2 & 9 onwards are currently invalid
                System.err.println("Type 2 for player "+(byteBuffer[2] >> 4)+
                        " -- invalid [requested byte ("+reqByteNum+
                        ")]!");
                break;
        }
    }
    
    
    // <editor-fold defaultstate="collapsed" desc="toString shortcut">
    //++toString shortcut
    @Override
    public String toString() {
        //System.out.println("PlayerState toString invoked.");  //debug
        StringBuilder result = new StringBuilder();
        for (Field f: getClass().getDeclaredFields()) {
            //System.out.println("f = " + f);   //debug
            //System.out.println("result = " + result.toString());  //debug
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
}
//Alt+Insert for constructors,Getters&Setters [after declaring fields]
