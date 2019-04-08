/*
 *  © 2019 by Patrick Matthew Chan
 *  File: CM.java
 *  Package: pong
 *  Description: The CM class.
 */
package pong;

/**
 *
 * @author Patrick Matthew J. Chan
 */
public enum CM {    //Controller Manager List of Keys
    LEFT_ANALOG_STICK_X(true,"Left Analog Stick"),
    LEFT_ANALOG_STICK_Y(true,"Left Analog Stick"),
    RIGHT_ANALOG_STICK_X(true,"Right Analog Stick"),
    RIGHT_ANALOG_STICK_Y(true,"Right Analog Stick"),
    A_FACE_BUTTON(false,"A Button"),
    B_FACE_BUTTON(false,"B Button"),
    X_FACE_BUTTON(false,"X Button"),
    Y_FACE_BUTTON(false,"Y Button"),
    UP_DPAD(false,"D-Pad Up"),
    DOWN_DPAD(false,"D-Pad Down"),
    LEFT_DPAD(false,"D-Pad Left"),
    RIGHT_DPAD(false,"D-Pad Right"),
    START_BUTTON(false,"Start Button"),
    SELECT_BUTTON(false,"Select Button"),
    LEFT_STICK_BUTTON(false,"Left Analog Stick Button (L3)"),
    RIGHT_STICK_BUTTON(false,"Right Analog Stick Button (R3)"),
    LEFT_SHOULDER(false,"Left Shoulder Button (L1)"),
    RIGHT_SHOULDER(false,"Right Shoulder Button (R1)"),
    LEFT_TRIGGER(false,"Left Trigger Button (L2)"),
    RIGHT_TRIGGER(false,"Right Trigger Button (R2)");
    
    private boolean isAD;
    private String keyName;
    CM(boolean isAnalogDirection,String keyName){
        this.isAD = isAnalogDirection;
        this.keyName = keyName;
    }
    
    boolean isAnalogAxis(){
        return isAD;
    }
    
    String getName(){
        return keyName;
    }
}
