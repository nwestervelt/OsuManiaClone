//class for the panel containing the notes and note highway
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.imageio.*;
import java.net.*;

public class HighwayPanel extends JPanel
{
    private MainFrame parent;
    private NoteReadingThread noteThread;
    private NotePositionThread notePosThread;
    private AnimationThread animThread;
    private ArrayList<Note> activeNotes;
    private Note currentNote;
    private int hitCount, hitWindow, missCount, score, delay;
    private BufferedImage[] keys, noteImages;
    private Toolkit toolkit;
    private volatile boolean playing;
    private Object[][] keysPressed;
    private Clip song;

    public HighwayPanel(MainFrame parent)
    {
        this.parent = parent;

        //get the toolkit for making animation smoother
        toolkit = getToolkit();

        //initialize playing variable
        playing = false;

        //initialize hit window (milliseconds before or after a note's hit time in which a hit is counted)
        hitWindow = 60;

        //initialize delay (which compensates for input delay and tells when the notes can be hit)
        delay = 816;

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
            keys[0] = ImageIO.read(new URL("jar:file:OsuManiaClone.jar!/images/redKey.png"));
            keys[1] = ImageIO.read(new URL("jar:file:OsuManiaClone.jar!/images/blueKey.png"));
            keys[2] = ImageIO.read(new URL("jar:file:OsuManiaClone.jar!/images/blueKey.png"));
            keys[3] = ImageIO.read(new URL("jar:file:OsuManiaClone.jar!/images/redKey.png"));

            //create note images
            noteImages[0] = ImageIO.read(new URL("jar:file:OsuManiaClone.jar!/images/redNote.png"));
            noteImages[1] = ImageIO.read(new URL("jar:file:OsuManiaClone.jar!/images/blueNote.png"));
            noteImages[2] = ImageIO.read(new URL("jar:file:OsuManiaClone.jar!/images/longNoteBody.png"));

            //get clip to play audio from
            song = AudioSystem.getClip();

            //create the stream to play the audio from
            AudioInputStream ais = AudioSystem.getAudioInputStream(new URL("jar:file:OsuManiaClone.jar!/song.wav"));
            song.open(ais);
        }
        catch(Exception e)
        {
            System.out.println(e);
            System.exit(1);
        }

        //add listener for the keys
        addKeyListener(new KeyHandler());

        //create the threads
        noteThread = new NoteReadingThread();
        notePosThread = new NotePositionThread();
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
            //check for a hit or miss
            for(int j = 0; j < keysPressed.length; j++)
            {
                //if current note's column matches this key
                if(currentNote.getX() == j * 100 + 100)
                {
                    //if key pressed
                    if((boolean)keysPressed[j][0])
                    {
                        //if within the hit window, not hit, and not missed
                        if(!currentNote.isHit() && !currentNote.isMissed() &&
                            (long)keysPressed[j][1] - currentNote.getCreationTime() <= delay + hitWindow &&
                            (long)keysPressed[j][1] - currentNote.getCreationTime() >= delay - hitWindow)
                        {
                            //mark as hit and held
                            currentNote.hit();
                            currentNote.setHeld(true);
                            updateAccuracy();

                        }
                        else if(currentNote.isLong() && currentNote.isHeld() && currentNote.isHit() && !currentNote.isMissed() &&
                            System.currentTimeMillis() - currentNote.getCreationTime() >  delay + currentNote.getDuration() + hitWindow)
                        {
                            currentNote.miss();
                            currentNote.setHeld(false);
                            updateAccuracy();
                        }
                        //to prevent key mashing from working, mark as missed if hit slightly before the hit window
                        else if(!currentNote.isMissed() && (long)keysPressed[j][1] - currentNote.getCreationTime() < delay - hitWindow &&
                            (long)keysPressed[j][1] - currentNote.getCreationTime() >= delay - hitWindow + 50)
                        {
                            currentNote.miss();
                            updateAccuracy();
                        }
                    }
                    //if key not pressed and note not missed
                    else if(!currentNote.isMissed())
                    {
                        //if current note is long and is being held
                        if(currentNote.isLong() && currentNote.isHeld())
                        {
                            //if key is released within hit window of long note's end
                            if((long)keysPressed[j][1] - currentNote.getCreationTime() <= delay + currentNote.getDuration() + hitWindow &&
                                (long)keysPressed[j][1] - currentNote.getCreationTime() >= delay + currentNote.getDuration() - hitWindow)
                            {
                                //mark as hit and set release time to sentinel value to keep from triggering hit repeatedly
                                currentNote.hit();
                                currentNote.setHeld(false);
                                keysPressed[j][1] = 0l;
                                updateAccuracy();
                            }
                            //if key is released during the long note
                            if((long)keysPressed[j][1] - currentNote.getCreationTime() > delay &&
                                (long)keysPressed[j][1] - currentNote.getCreationTime() < delay + currentNote.getDuration() - hitWindow)
                            {
                                //mark as missed and not held
                                currentNote.miss();
                                currentNote.setHeld(false);
                                updateAccuracy();
                            }
                        }
                        //if note not hit and goes past note hit area + size of hit window, mark as missed
                        else if(!currentNote.isHit() && System.currentTimeMillis() - currentNote.getCreationTime() > delay + hitWindow)
                        {
                            //mark as missed
                            currentNote.miss();
                            updateAccuracy();
                        }
                    }
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

        //if song is done playing, end the game and prepare for restart
        if(!song.isActive())
        {
            playing = false;
            hitCount = 0;
            missCount = 0;
            score = 0;

            noteThread = new NoteReadingThread();
            notePosThread = new NotePositionThread();
            animThread = new AnimationThread();

            try
            {
                song = AudioSystem.getClip();

                AudioInputStream ais = AudioSystem.getAudioInputStream(new URL("jar:file:OsuManiaClone.jar!/song.wav"));
                song.open(ais);

            }
            catch(Exception e)
            {
                System.out.println(e);
                System.exit(1);
            }

            g.setColor(Color.YELLOW);
            g.fillRect(200, 400, 200, 50);
            g.setColor(Color.BLACK);
            g.drawString("Not Playing", 270, 428);
        }
    }
    private void updateAccuracy()
    {
        if(hitCount != 0 || missCount != 0)
            parent.updateAccuracy(100 * ((double)hitCount / (hitCount + missCount)));

        else
            parent.updateAccuracy(100);
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
                activeNotes = new ArrayList<Note>();
                parent.updateHit((hitCount=0));
                parent.updateMiss((missCount=0));
                parent.updateScore((score=0));
                updateAccuracy();

                playing = true;
                noteThread.start();
                notePosThread.start();
                song.start();
                animThread.start();
            }
            else if(ke.getKeyCode() == KeyEvent.VK_P && playing)
            {
                //set not playing, and stop song
                playing = false;
                song.stop();

                //reset the program for restarting
                noteThread = new NoteReadingThread();
                notePosThread = new NotePositionThread();
                animThread = new AnimationThread();

                try
                {
                    song = AudioSystem.getClip();

                    AudioInputStream ais = AudioSystem.getAudioInputStream(new URL("jar:file:OsuManiaClone.jar!/song.wav"));
                    song.open(ais);

                }
                catch(Exception e)
                {
                    System.out.println(e);
                    System.exit(1);
                }
                activeNotes = new ArrayList<Note>();
                parent.updateHit((hitCount=0));
                parent.updateMiss((missCount=0));
                parent.updateScore((score=0));
                updateAccuracy();

                repaint();
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
                ke.getKeyCode() == KeyEvent.VK_D && !pressed)
            {
                keysPressed[0][0] = pressed;
                keysPressed[0][1] = System.currentTimeMillis();
            }
            if(ke.getKeyCode() == KeyEvent.VK_F && !(boolean)keysPressed[1][0] ||
                ke.getKeyCode() == KeyEvent.VK_F && !pressed)
            {
                keysPressed[1][0] = pressed;
                keysPressed[1][1] = System.currentTimeMillis();
            }
            if(ke.getKeyCode() == KeyEvent.VK_J && !(boolean)keysPressed[2][0] ||
                ke.getKeyCode() == KeyEvent.VK_J && !pressed)
            {
                keysPressed[2][0] = pressed;
                keysPressed[2][1] = System.currentTimeMillis();
            }
            if(ke.getKeyCode() == KeyEvent.VK_K && !(boolean)keysPressed[3][0] ||
                ke.getKeyCode() == KeyEvent.VK_K && !pressed)
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
            this.duration = duration;
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
            //give double weight to misses on long note beginnings
            if(isLong && !isHit)
                ++missCount;

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
            long startTime, elapsedTime;

            try
            {
                while(playing)
                {
                    startTime = System.currentTimeMillis();
                    repaint();
                    toolkit.sync();

                    if((elapsedTime = (System.currentTimeMillis() - startTime)) < 8)
                        sleep(8 - elapsedTime);
                }
            }
            catch(InterruptedException ie){}
        }
    }
    private class NoteReadingThread extends Thread
    {
        private final int noteTime = 0, noteColumn = 1, noteLong = 2, noteLength = 3;
        private BufferedReader noteFile;
        private long startTime;

        public NoteReadingThread()
        {
            //open the note file
            try
            {
                URL ul = new URL("jar:file:OsuManiaClone.jar!/noteFile.txt");
                noteFile = new BufferedReader(new InputStreamReader(ul.openStream()));
            }
            catch(Exception e)
            {
                System.out.println(e);
                System.exit(1);
            }
        }

        public void run()
        {
            //set the starting time
            startTime = System.currentTimeMillis();

            try
            {
                //priming read of note information
                String line = noteFile.readLine();
                String[] noteStrings = line.split(" ");

                while(playing)
                {
                    //when note's time is reached, add it
                    if(System.currentTimeMillis() - startTime >= Integer.parseInt(noteStrings[noteTime]))
                    {
                        //add note to active notes
                        activeNotes.add(new Note(Integer.parseInt(noteStrings[noteColumn]),
                            Boolean.parseBoolean(noteStrings[noteLong]), Integer.parseInt(noteStrings[noteLength])));

                        line = noteFile.readLine();

                        //stop reading when end of file is reached
                        if(line == null) break;
                        else noteStrings = line.split(" ");
                    }
                }
            }
            catch(IOException ioe)
            {
                System.out.println(ioe);
                System.exit(1);
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

                while(playing)
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
}
