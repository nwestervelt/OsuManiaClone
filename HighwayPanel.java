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
    private NoteReadingThread noteThread;
    private AnimationThread animThread;
    private SongThread songThread;
    private volatile ArrayList<Note> activeNotes;
    private Note currentNote;
    private int noteX, noteY;
    private BufferedImage[] keys, noteImages;
    private Toolkit toolkit;
    private boolean playing;
    private boolean[] keysPressed;

    public HighwayPanel()
    {
        //get the toolkit for making animation smoother
        toolkit = getToolkit();

        //initialize playing variable
        playing = false;

        //instantiate pressed state of keys
        keysPressed = new boolean[]{false, false, false, false};

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
        g.fillRect(0, 0, 600, 720);

        //draw vertical lines on highway
        g.setColor(Color.LIGHT_GRAY);
        g.drawLine(100, 0, 100, 720);
        g.drawLine(200, 0, 200, 720);
        g.drawLine(300, 0, 300, 720);
        g.drawLine(400, 0, 400, 720);
        g.drawLine(500, 0, 500, 720);

        //draw active notes
        for(int i = 0; i < activeNotes.size(); i++)
        {
            currentNote = activeNotes.get(i);
            noteY = currentNote.getY() + 5;
            noteX = currentNote.getX();

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
                            g.drawImage(noteImages[1], noteX, noteY - (j * 50), null);
                        else
                            g.drawImage(noteImages[0], noteX, noteY - (j * 50), null);
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

            //update y position of note
            currentNote.setY(noteY);

            //remove regular note if it's offscreen
            if(noteY == 800 && !currentNote.isLong())
                activeNotes.remove(i);

            //remove long note if it's tail is offscreen
            else if(currentNote.isLong() && noteY - (currentNote.getLength() * 50) == 800)
                activeNotes.remove(i);
        }
        //draw images associated with pressed keys
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
            //update status of pressed keys
            if(ke.getKeyCode() == KeyEvent.VK_D && !keysPressed[0])
                keysPressed[0] = true;
            if(ke.getKeyCode() == KeyEvent.VK_F && !keysPressed[1])
                keysPressed[1] = true;
            if(ke.getKeyCode() == KeyEvent.VK_J && !keysPressed[2])
                keysPressed[2] = true;
            if(ke.getKeyCode() == KeyEvent.VK_K && !keysPressed[3])
                keysPressed[3] = true;

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
            //set status of key pressed to false when released
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
    private class Note
    {
        private int x, y, length;
        private boolean isLong;

        public Note(int column, boolean isLong, int length)
        {
            x = (column * 100) + 100;
            y = -600;
            this.length = length;
            this.isLong = isLong;
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
        //return if this is a long note
        public boolean isLong()
        {
            return isLong;
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
