//class for the panel containing information about player's performance
import java.awt.*;
import javax.swing.*;

public class ScorePanel extends JPanel
{
    private JTextField scoreField, accuracyField, hitField, missField;
    public ScorePanel()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        //score field, label, and panel
        JPanel scorePanel = new JPanel();
        JLabel scoreLabel = new JLabel("Score: ");
        scoreField = new JTextField("0", 10);
        scoreField.setFocusable(false);
        add(scorePanel);
        scorePanel.add(scoreLabel);
        scorePanel.add(scoreField);

        //accuracy field, label, and panel
        JPanel accuracyPanel = new JPanel();
        JLabel accuracyLabel = new JLabel("Accuracy: ");
        accuracyField = new JTextField("100.0%", 10);
        accuracyField.setFocusable(false);
        add(accuracyPanel);
        accuracyPanel.add(accuracyLabel);
        accuracyPanel.add(accuracyField);

        //hit field, label, and panel
        JPanel hitPanel = new JPanel();
        JLabel hitLabel = new JLabel("Hit Count: ");
        hitField = new JTextField("0", 10);
        hitField.setFocusable(false);
        add(hitPanel);
        hitPanel.add(hitLabel);
        hitPanel.add(hitField);

        //miss field, label, and panel
        JPanel missPanel = new JPanel();
        JLabel missLabel = new JLabel("Miss Count: ");
        missField = new JTextField("0", 10);
        missField.setFocusable(false);
        add(missPanel);
        missPanel.add(missLabel);
        missPanel.add(missField);

        //information field, label, and panel
        JPanel infoPanel = new JPanel();
        JLabel infoLabel = new JLabel("Controls: ");
        JTextArea infoArea = new JTextArea("Start playing by pressing Space.\n" +
            "Pressing P will stop the game and reset it.\n" +
            "D, F, J, and K are used for hitting notes.\n" +
            "D and F are the left two, J and K are the right two.", 4, 20);
        infoArea.setFocusable(false);
        add(infoPanel);
        infoPanel.add(infoLabel);
        infoPanel.add(infoArea);

        //set this panel's appearance
        setPreferredSize(new Dimension(300, 980));
        setFocusable(false);
    }
    public void updateAccuracy(double accuracy)
    {
        accuracyField.setText(String.format("%3.1f", accuracy) + "%");
    }
    public void updateHit(int hitCount)
    {
        hitField.setText(Integer.toString(hitCount));
    }
    public void updateMiss(int missCount)
    {
        missField.setText(Integer.toString(missCount));
    }
    public void updateScore(int score)
    {
        scoreField.setText(Integer.toString(score));
    }
}
