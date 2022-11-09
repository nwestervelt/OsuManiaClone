//class for the panel containing the notes and note highway
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.imageio.*;

public class HighwayPanel extends JPanel
{
    private Toolkit toolkit;
    private boolean[] keysPressed;
    private BufferedImage[] keys, noteImages;

    public HighwayPanel()
    {
        //get the toolkit for making animation smoother
        toolkit = getToolkit();

        //instantiate pressed state of keys
        keysPressed = new boolean[]{false, false, false, false};

        //stores key images
        keys = new BufferedImage[4];

        //stores note images
        noteImages = new BufferedImage[3];
        try
        {
            //create key images
            keys[0] = ImageIO.read(new File("images/redKey.png"));
            keys[1] = ImageIO.read(new File("images/blueKey.png"));
            keys[2] = ImageIO.read(new File("images/blueKey.png"));
            keys[3] = ImageIO.read(new File("images/redKey.png"));

            //create note images
            noteImages[0] = ImageIO.read(new File("images/redNote.png"));
            noteImages[1] = ImageIO.read(new File("images/blueNote.png"));
            noteImages[2] = ImageIO.read(new File("images/longNoteBody.png"));
        }
        catch(IOException ioe)
        {
            System.out.println(ioe);
            System.exit(1);
        }

        //add listener for the keys
        addKeyListener(new KeyHandler());

        //create and start the animation thread
        AnimationThread animThread = new AnimationThread();
        animThread.start();

        //set this panel's appearance
        setPreferredSize(new Dimension(1080, 720));
        setFocusable(true);
        requestFocus();
    }
    public void paintComponent(Graphics g)
    {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 1080, 720);

        g.setColor(Color.LIGHT_GRAY);
        g.drawLine(100, 0, 100, 720);
        g.drawLine(200, 0, 200, 720);
        g.drawLine(300, 0, 300, 720);
        g.drawLine(400, 0, 400, 720);
        g.drawLine(500, 0, 500, 720);

        if(keysPressed[0])
            g.drawImage(keys[0], 100, 520, null);
        if(keysPressed[1])
            g.drawImage(keys[1], 200, 520, null);
        if(keysPressed[2])
            g.drawImage(keys[2], 300, 520, null);
        if(keysPressed[3])
            g.drawImage(keys[3], 400, 520, null);
    }
    private class KeyHandler extends KeyAdapter
    {
        public void keyPressed(KeyEvent ke)
        {
            if(ke.getKeyCode() == KeyEvent.VK_D)
            {
                keysPressed[0] = true;
            }
            if(ke.getKeyCode() == KeyEvent.VK_F)
            {
                keysPressed[1] = true;
            }
            if(ke.getKeyCode() == KeyEvent.VK_J)
            {
                keysPressed[2] = true;
            }
            if(ke.getKeyCode() == KeyEvent.VK_K)
            {
                keysPressed[3] = true;
            }
        }
        public void keyReleased(KeyEvent ke)
        {
            if(ke.getKeyCode() == KeyEvent.VK_D)
                keysPressed[0] = false;
            if(ke.getKeyCode() == KeyEvent.VK_F)
                keysPressed[1] = false;
            if(ke.getKeyCode() == KeyEvent.VK_J)
                keysPressed[2] = false;
            if(ke.getKeyCode() == KeyEvent.VK_K)
                keysPressed[3] = false;
        }
    }
    private class AnimationThread extends Thread
    {
        public void run()
        {
            try
            {
                while(true)
                {
                    sleep(8);
                    repaint();
                    toolkit.sync();
                }
            }
            catch(InterruptedException ie){}
        }
    }
}
