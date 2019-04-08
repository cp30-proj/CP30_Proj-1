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
    LEFT_ANALOG_STICK_X(true),
    LEFT_ANALOG_STICK_Y(true),
    RIGHT_ANALOG_STICK_X(true),
    RIGHT_ANALOG_STICK_Y(true),
    A_FACE_BUTTON(false),
    B_FACE_BUTTON(false),
    X_FACE_BUTTON(false),
    Y_FACE_BUTTON(false),
    UP_DPAD(false),
    DOWN_DPAD(false),
    LEFT_DPAD(false),
    RIGHT_DPAD(false),
    START_BUTTON(false),
    SELECT_BUTTON(false),
    LEFT_STICK_BUTTON(false),
    RIGHT_STICK_BUTTON(false),
    LEFT_SHOULDER(false),
    RIGHT_SHOULDER(false),
    LEFT_TRIGGER(false),
    RIGHT_TRIGGER(false);
    
    private boolean isAD;
    CM(boolean isAnalogDirection){
        this.isAD = isAnalogDirection;
    }
    
    boolean isAnalogDirection(){
        return isAD;
    }
}
