//class for the main frame of the program, which houses all panels
import java.awt.*;
import javax.swing.*;

public class MainFrame extends JFrame
{
    ScorePanel scorePanel;

    public MainFrame()
    {
        //call super constructor and set this frame's title
        super("Osu Mania Clone");

        //create the HighwayPanel
        HighwayPanel highwayPanel = new HighwayPanel(this);
        add(highwayPanel);

        //create the ScorePanel
        scorePanel = new ScorePanel();
        add(scorePanel, BorderLayout.EAST);

        //set the appearance and behavior of this frame
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);
    }
    public void updateAccuracy(double accuracy)
    {
        scorePanel.updateAccuracy(accuracy);
    }
    public void updateHit(int hitCount)
    {
        scorePanel.updateHit(hitCount);
    }
    public void updateMiss(int missCount)
    {
        scorePanel.updateMiss(missCount);
    }
    public void updateScore(int score)
    {
        scorePanel.updateScore(score);
    }
    public static void main(String[] args)
    {
        new MainFrame();
    }
}
