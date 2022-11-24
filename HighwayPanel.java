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
    private NotePositionThread notePosThread;
    private AnimationThread animThread;
    private SongThread songThread;
    private ArrayList<Note> activeNotes;
    private Note currentNote;
    private int hitCount, hitWindow, missCount, score, accuracy;
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

        //initialize hit window (milliseconds before or after a note's hit time in which a hit is counted)
        hitWindow = 60;

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

        //create the threads
        noteThread = new NoteReadingThread();
        notePosThread = new NotePositionThread();
        songThread = new SongThread();
        animThread = new AnimationThread();

        //set this panel's appearance
        setPreferredSize(new Dimension(600, 980));
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

            //draw notes in outside columns
            if(currentNote.getX() == 100 || currentNote.getX() == 400)
            {
                g.drawImage(noteImages[0], currentNote.getX(), currentNote.getY(), null);

                //if is a long note, draw it's body
                if(currentNote.isLong())
                {
                    g.drawImage(currentNote.getScaledBody(), currentNote.getX(), currentNote.getY() - currentNote.getLength() + 50, null);
                    g.drawImage(noteImages[0], currentNote.getX(), currentNote.getY() - currentNote.getLength() , null);
                }
            }
            //draw notes in inside columns
            else if(currentNote.getX() == 200 || currentNote.getX() == 300)
            {
                g.drawImage(noteImages[1], currentNote.getX(), currentNote.getY(), null);

                //if is a long note, draw it's body
                if(currentNote.isLong())
                {
                    g.drawImage(currentNote.getScaledBody(), currentNote.getX(), currentNote.getY() - currentNote.getLength() + 50, null);
                    g.drawImage(noteImages[1], currentNote.getX(), currentNote.getY() - currentNote.getLength() , null);
                }
            }
            noteCreationTime = currentNote.getCreationTime();

            //check for a hit or miss
            for(int j = 0; j < keysPressed.length; j++)
            {
                //if key pressed matches column of note
                if((boolean)keysPressed[j][0] && currentNote.getX() == j * 100 + 100)
                {
                    //if within the hit window
                    if((long)keysPressed[j][1] - noteCreationTime <= 816 + hitWindow &&
                        (long)keysPressed[j][1] - noteCreationTime >= 816 - hitWindow)
                    {
                        //if not hit and not missed, mark as hit and set hold value for long notes
                        if(!currentNote.isHit() && !currentNote.isMissed())
                        {
                            currentNote.hit();
                            currentNote.setHeld(true);
                        }
                    }
                    //to prevent key mashing from working, mark as missed if hit slightly before the hit window
                    else if(!currentNote.isMissed() && (long)keysPressed[j][1] - noteCreationTime < 816 - hitWindow &&
                        (long)keysPressed[j][1] - noteCreationTime >= 816 - hitWindow + 50)
                    {
                        currentNote.miss();
                    }
                }
                //long note hold
                else if(currentNote.isLong() && currentNote.isHeld())
                {
                }
                //miss
                else if(!currentNote.isHit() && !currentNote.isMissed()
                    && System.currentTimeMillis() - noteCreationTime > 816 + hitWindow)
                {
                    currentNote.miss();
                }
            }

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
                notePosThread.start();
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
        private int x, y, length, duration;
        private long creationTime;
        private boolean isLong, isHit, isMissed, isHeld;
        private Image scaledBody;

        public Note(int column, boolean isLong, int duration)
        {
            x = (column * 100) + 100;
            y = -600;
            this.isLong = isLong;
            creationTime = System.currentTimeMillis();
            isHit = false;
            isMissed = false;
            isHeld = false;

            if(isLong)
            {
                length = (int)(duration * 15.0 / 8);
                scaledBody = noteImages[2].getScaledInstance(100, length - 50, Image.SCALE_FAST);
            }
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
        //get the duration of the long note
        public int getDuration()
        {
            return duration;
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
        //return the scaled image used for the body
        public Image getScaledBody()
        {
            return scaledBody;
        }
        //set hit state of this note
        public void hit()
        {
            score += 50;
            isHit = true;
            parent.updateHit(++hitCount);
            parent.updateScore(score);
        }
        //return if this note has been hit
        public boolean isHit()
        {
            return isHit;
        }
        //set miss state of this note
        public void miss()
        {
            isMissed = true;
            parent.updateMiss(++missCount);
        }
        //return if this note has been missed
        public boolean isMissed()
        {
            return isMissed;
        }
        //set state of long note being held
        public void setHeld(boolean isHeld)
        {
            this.isHeld = isHeld;
        }
        //return if this note has been held
        public boolean isHeld()
        {
            return isHeld;
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
                if(System.currentTimeMillis() - startTime >= noteTime)
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
    private class NotePositionThread extends Thread
    {
        long startTime;

        public void run()
        {
            try
            {
                //offset this thread from the animating thread, to avoid collisions
                sleep(4);

                Note currentNote;

                while(true)
                {
                    startTime = System.currentTimeMillis();

                    for(int i = activeNotes.size() - 1; i > -1; i--)
                    {
                        currentNote = activeNotes.get(i);

                        //update y position
                        currentNote.setY(currentNote.getY() + 15);

                        //remove regular note if it's offscreen
                        if(currentNote.getY() > 1100 && !currentNote.isLong())
                        {
                            synchronized(activeNotes)
                            {
                                activeNotes.remove(i);
                            }
                        }
                        //remove long note if it's tail is offscreen
                        else if(currentNote.isLong() && currentNote.getY() - (currentNote.getLength() * 50) > 1100)
                        {
                            synchronized(activeNotes)
                            {
                                activeNotes.remove(i);
                            }
                        }
                    }
                    sleep(8 - (System.currentTimeMillis() - startTime));
                }
            }
            catch(InterruptedException ie){}
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
