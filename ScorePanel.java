//class for the panel containing information about player's performance
import java.awt.*;
import javax.swing.*;

public class ScorePanel extends JPanel
{
    private JTextField scoreField, accuracyField;
    public ScorePanel()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        //score field, label, and panel
        JPanel scorePanel = new JPanel();
//        scorePanel.setBackground(Color.BLACK);
        JLabel scoreLabel = new JLabel("Score: ");
        scoreField = new JTextField("0", 10);
        add(scorePanel);
        scorePanel.add(scoreLabel);
        scorePanel.add(scoreField);

        //accuracy field, label, and panel
        JPanel accuracyPanel = new JPanel();
//        accuracyPanel.setBackground(Color.BLACK);
        JLabel accuracyLabel = new JLabel("Accuracy: ");
        accuracyField = new JTextField("100%", 10);
        add(accuracyPanel);
        accuracyPanel.add(accuracyLabel);
        accuracyPanel.add(accuracyField);

        //set this panel's appearance
        setPreferredSize(new Dimension(200, 980));
        setFocusable(false);
    }
    public void updateScore(int score)
    {
        scoreField.setText(Integer.toString(score));
    }
}
