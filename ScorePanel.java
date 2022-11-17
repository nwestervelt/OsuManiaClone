//class for the panel containing information about player's performance
import java.awt.*;
import javax.swing.*;

public class ScorePanel extends JPanel
{
    public ScorePanel()
    {
        //set this panel's appearance
        setPreferredSize(new Dimension(200, 980));
        setFocusable(false);
        setBackground(Color.BLACK);
    }
}
