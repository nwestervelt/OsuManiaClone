OsuManiaClone.jar: MainFrame.class HighwayPanel.class ScorePanel.class ../images/* ../song.wav ../noteFile.txt
	jar -cvfe OsuManiaClone.jar MainFrame *.class ../images ../song.wav ../noteFile.txt;

MainFrame.class: ../MainFrame.java ../HighwayPanel.java ../ScorePanel.java
	javac --release=8 -cp .. ../MainFrame.java
	mv ../*.class .
