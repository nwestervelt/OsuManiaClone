//class for the panel containing the notes and note highway
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.imageio.*;

public class HighwayPanel extends JPanel
{
    private MainFrame parent;
    private NoteReadingThread noteThread;
    private AnimationThread animThread;
    private SongThread songThread;
    private ArrayList<Note> activeNotes;
    private Note currentNote;
    private int noteX, noteY, hitCount, score, accuracy;
    private long noteCreationTime;
    private BufferedImage[] keys, noteImages;
    private Toolkit toolkit;
    private boolean playing;
    private Object[][] keysPressed;

    public HighwayPanel(MainFrame parent)
    {
        this.parent = parent;

        //get the toolkit for making animation smoother
        toolkit = getToolkit();

        //initialize playing variable
        playing = false;

        //instantiate pressed state of keys
        keysPressed = new Object[4][2];

        for(int i = 0; i < keysPressed.length; i++)
        {
            keysPressed[i][0] = false;
            keysPressed[i][1] = System.currentTimeMillis();
        }

        //instantiate arraylist of active notes
        activeNotes = new ArrayList<Note>();

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

        //create the threads for reading notes, the song, and the animation
        noteThread = new NoteReadingThread();
        songThread = new SongThread();
        animThread = new AnimationThread();

        //set this panel's appearance
        setPreferredSize(new Dimension(600, 720));
        setFocusable(true);
        requestFocus();
    }
    public void paintComponent(Graphics g)
    {
        //draw black background of highway
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 600, 980);

        //draw gray border on highway
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, 100, 980);
        g.fillRect(500, 0, 500, 980);

        //draw vertical lines on highway
        g.drawLine(200, 0, 200, 980);
        g.drawLine(300, 0, 300, 980);
        g.drawLine(400, 0, 400, 980);

        //draw judgement line
        g.drawLine(100, 930, 500, 930);

        //draw active notes
        for(int i = activeNotes.size() - 1; i > -1; i--)
        {
            currentNote = activeNotes.get(i);
            noteY = currentNote.getY() + 15;
            noteX = currentNote.getX();
            noteCreationTime = currentNote.getCreationTime();

            //draw notes in outside columns
            if(noteX == 100 || noteX == 400)
            {
                g.drawImage(noteImages[0], noteX, noteY, null);

                //if is a long note, draw it's body
                if(currentNote.isLong())
                {
                    int length = currentNote.getLength();

                    for(int j = 1; j <= length; j++)
                    {
                        //if at end of long note, draw another regular note as a tail
                        if(j == length)
                            g.drawImage(noteImages[0], noteX, noteY - (j * 50), null);
                        else
                            g.drawImage(noteImages[2], noteX, noteY - (j * 50), null);
                    }
                }
            }
            //draw notes in inside columns
            else if(noteX == 200 || noteX == 300)
            {
                g.drawImage(noteImages[1], noteX, noteY, null);

                //if is a long note, draw it's body
                if(currentNote.isLong())
                {
                    int length = currentNote.getLength();

                    for(int j = 1; j <= length; j++)
                    {
                        //if at end of long note, draw another regular note as a tail
                        if(j == length)
                            g.drawImage(noteImages[1], noteX, noteY - (j * 50), null);
                        else
                            g.drawImage(noteImages[2], noteX, noteY - (j * 50), null);
                    }
                }
            }

            //check if key is pressed for note
            for(int j = 0; j < keysPressed.length; j++)
            {
                if((boolean)keysPressed[j][0] && noteX == j * 100 + 100 &&
                    ((long)keysPressed[j][1] - noteCreationTime <= 816 + 80 &&
                    (long)keysPressed[j][1] - noteCreationTime >= 816 - 80))
                {
                    if(!currentNote.isHit())
                    {
                        currentNote.hit();
                        score += 50;
                        parent.updateScore(score);
                    }
                }
            }

            //update y position of note
            currentNote.setY(noteY);

            //remove regular note if it's offscreen
            if(noteY > 1100 && !currentNote.isLong())
                activeNotes.remove(i);

            //remove long note if it's tail is offscreen
            else if(currentNote.isLong() && noteY - (currentNote.getLength() * 50) > 1100)
                activeNotes.remove(i);
        }
        //draw images associated with pressed keys
        if((boolean)keysPressed[0][0])
            g.drawImage(keys[0], 100, 780, null);
        if((boolean)keysPressed[1][0])
            g.drawImage(keys[1], 200, 780, null);
        if((boolean)keysPressed[2][0])
            g.drawImage(keys[2], 300, 780, null);
        if((boolean)keysPressed[3][0])
            g.drawImage(keys[3], 400, 780, null);
    }
    private class KeyHandler extends KeyAdapter
    {
        public void keyPressed(KeyEvent ke)
        {
            //update state of keys
            setKeyState(ke, true);

            //if space is pressed, start the game and it's threads
            if(ke.getKeyCode() == KeyEvent.VK_SPACE && !playing)
            {
                noteThread.start();
                //songThread.start();
                animThread.start();
                playing = true;
            }
        }
        public void keyReleased(KeyEvent ke)
        {
            //update state of keys
            setKeyState(ke, false);
        }
        private void setKeyState(KeyEvent ke, boolean pressed)
        {
            if(ke.getKeyCode() == KeyEvent.VK_D && !(boolean)keysPressed[0][0] ||
                !pressed)
            {
                keysPressed[0][0] = pressed;
                keysPressed[0][1] = System.currentTimeMillis();
            }
            if(ke.getKeyCode() == KeyEvent.VK_F && !(boolean)keysPressed[1][0] ||
                !pressed)
            {
                keysPressed[1][0] = pressed;
                keysPressed[1][1] = System.currentTimeMillis();
            }
            if(ke.getKeyCode() == KeyEvent.VK_J && !(boolean)keysPressed[2][0] ||
                !pressed)
            {
                keysPressed[2][0] = pressed;
                keysPressed[2][1] = System.currentTimeMillis();
            }
            if(ke.getKeyCode() == KeyEvent.VK_K && !(boolean)keysPressed[3][0] ||
                !pressed)
            {
                keysPressed[3][0] = pressed;
                keysPressed[3][1] = System.currentTimeMillis();
            }
        }
    }
    private class Note
    {
        private int x, y, length;
        private long creationTime;
        private boolean isLong, isHit;

        public Note(int column, boolean isLong, int length)
        {
            x = (column * 100) + 100;
            y = -600;
            this.length = length;
            this.isLong = isLong;
            creationTime = System.currentTimeMillis();
            isHit = false;
        }
        //get the raw x coordinate of the note
        public int getX()
        {
            return x;
        }
        //set the raw y coordinate of the note
        public void setY(int y)
        {
            this.y = y;
        }
        //get the raw y coordinate of the note
        public int getY()
        {
            return y;
        }
        //get the length of the note
        public int getLength()
        {
            return length;
        }
        //return the time this note was created
        public long getCreationTime()
        {
            return creationTime;
        }
        //return if this is a long note
        public boolean isLong()
        {
            return isLong;
        }
        //set state of note being hit
        public void hit()
        {
            isHit = true;
        }
        //return if this note has been hit
        public boolean isHit()
        {
            return isHit;
        }
    }
    private class AnimationThread extends Thread
    {
        public void run()
        {
            long startTime;

            try
            {
                while(true)
                {
                    startTime = System.currentTimeMillis();
                    repaint();
                    toolkit.sync();
                    sleep(8 - (System.currentTimeMillis() - startTime));
                }
            }
            catch(InterruptedException ie){}
        }
    }
    private class NoteReadingThread extends Thread
    {
        private int noteTime, noteColumn, noteLength;
        private boolean noteLong;
        private Scanner noteFile;
        private long startTime;

        public NoteReadingThread()
        {
            //open the note file
            try
            {
                noteFile = new Scanner(new File("noteFile.txt"));
            }
            catch(FileNotFoundException fnfe)
            {
                System.out.println(fnfe);
                System.exit(1);
            }
        }

        public void run()
        {
            //set the starting time
            startTime = System.currentTimeMillis();

            //priming read of note information
            noteTime = noteFile.nextInt();
            noteColumn = noteFile.nextInt();
            noteLong = noteFile.nextBoolean();
            noteLength = noteFile.nextInt();

            while(true)
            {
                //when note's time is reached, add it
                if(System.currentTimeMillis() - startTime == noteTime)
                {
                    //add note to active notes
                    activeNotes.add(new Note(noteColumn, noteLong, noteLength));

                    //stop reading when end of file is reached
                    if(!noteFile.hasNext()) break;

                    //read next note information
                    noteTime = noteFile.nextInt();
                    noteColumn = noteFile.nextInt();
                    noteLong = noteFile.nextBoolean();
                    noteLength = noteFile.nextInt();
                }
            }
        }
    }
    private class SongThread extends Thread
    {
        private Clip song;

        public void run()
        {
            try
            {
                //get clip to play audio from
                song = AudioSystem.getClip();

                //create the stream to play the audio from
                AudioInputStream ais = AudioSystem.getAudioInputStream(new File("pathToSongFile"));
                song.open(ais);

                //start playing the audio
                song.start();
            }
            catch(Exception e)
            {
                System.out.println(e);
                System.exit(1);
            }
        }
    }
}
