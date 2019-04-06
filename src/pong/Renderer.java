/*
 *  © 2019 by Patrick Matthew Chan
 *  File: Renderer.java
 *  Package: pong
 *  Description: The Renderer class.
 */

package pong;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/*@author Patrick Matthew J. Chan*/
public class Renderer extends JPanel{

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); //To change body of generated methods, choose Tools | Templates.
        
        Pong.pong.render((Graphics2D) g);  //*only because  Graphics in Java is Graphics2D in default (extend)
    }
    
    
}
