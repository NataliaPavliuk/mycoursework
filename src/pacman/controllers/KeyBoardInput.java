package pacman.controllers;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyBoardInput extends KeyAdapter
{
    private int key;

    public int getKey()
    {
    	return key;
    }

    public void keyPressed(KeyEvent e) 
    {
        key=e.getKeyCode();
    }
}