//class for the main frame of the program, which houses all panels
import java.awt.*;
import javax.swing.*;

public class MainFrame extends JFrame
{
    public MainFrame()
    {
        //call super constructor and set this frame's title
        super("Osu Mania Clone");

        //create the HighwayPanel
        HighwayPanel highwayPanel = new HighwayPanel();
        add(highwayPanel);

        //create the ScorePanel
        ScorePanel scorePanel = new ScorePanel();
        add(scorePanel, BorderLayout.EAST);

        //set the appearance and behavior of this frame
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }
    public static void main(String[] args)
    {
        new MainFrame();
    }
}
