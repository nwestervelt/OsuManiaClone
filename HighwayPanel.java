//class for the panel containing the notes and note highway
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class HighwayPanel extends JPanel
{
    private Toolkit toolkit;
    private boolean[] keysPressed;

    public HighwayPanel()
    {
        //get the toolkit for making animation smoother
        toolkit = getToolkit();

        //instantiate pressed state of keys
        keysPressed = new boolean[]{false, false, false, false};

        //add listener for the keys
        addKeyListener(new KeyHandler());

        //create and start the animation thread
        AnimationThread animThread = new AnimationThread();
        animThread.start();

        //set this panel's appearance
        setPreferredSize(new Dimension(1080, 720));
        setFocusable(true);
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
    }
    private class KeyHandler extends KeyAdapter
    {
    }
    private class AnimationThread extends Thread
    {
        public void run()
        {
            try
            {
                while(true)
                {
                    sleep(12);
                    repaint();
                    toolkit.sync();
                }
            }
            catch(InterruptedException ie){}
        }
    }
}
