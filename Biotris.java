/* Mohini Banerjee
   May 23, 2014
   Biotris.java
   This program demonstrates my knowledge I gained throughout this year in java. This is a modified version of tetris with an educational aspect of biology questions. Blocks will be falling from the top of the screen on a timer. They will land in the next available grid square. The user can contol the postion of the blocks by usuing 'a' to move it to the left, 's' to move it down, 'w' to move it up, and 'd' to move it to right. When there is one empty grid square surrounded by full grid squares, a question mark will appear. The user may click on the question mark and a question will appear on the screen. If the question is answered correctly, 100 points are awarded and a block appears on that square. If answered incorrectly, 50 points are taken away. In addition, points are awarded when an entire row of blocks are filled. If the user wishes to reset the game at any moment, they may do so by pressing 'r'. 
*/

import java.awt.*; 
import java.awt.event.*; 
import javax.swing.*; 
import javax.swing.event.*;
import java.util.Random;	
import java.io.*; 
import java.util.Scanner;
import javax.imageio.*;


public class Biotris{
    JFrame frame;		//The overall frame in which the game is played.
    MyCardPanel myCardPanel;	//The start, instructions, and game panel are created in this class, using a card layout.
    int difficulty;		//The difficulty or level of the game, selected by the user. 5 is the easist and 1 is the hardest.
    Player[] players = new Player[100];	  //This array of players contains information about each player:
                                          //(their name, their highest score, their level) and is written to a text file Players.txt.			
    StatusBar statusBar;	//The status bar that displays the status and score of the game, and indicates when the game is over. 
	
    public static void main(String [] args){	//Main method.
	Biotris b = new Biotris();
	b.run();
    }

    public void run(){		//Creates the main JFrame and creates and adds a card panel to it. 
	frame = new JFrame("Biotris!"); 
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	myCardPanel = new MyCardPanel();
	frame.getContentPane().add(myCardPanel); 	
	frame.setSize(616,688);
	frame.setVisible(true);
    }

    public void readPlayers(Player[] players) {			//This method opens up the file Players.txt and reads the previous players, with their name, level, and highest score, and adds it to a players array. 
	Scanner playersFile=null;
	try{		//Opens Players.txt.		
	    playersFile = new Scanner(new File("Players.txt"));
	}catch(FileNotFoundException e){
	    System.err.println("File Players.txt not found ");
	    System.exit(1);
	}
	int index = 0;	//Pointing to the current index of the array.
	while(playersFile.hasNext()) {	//While players.txt isn't empty: 
	    //System.out.println("playersFile isn't empty");
	    Player player = new Player();	//Creating a new player. 
	    //Adding the player's name, highest score, and level to player. 
	    player.name = playersFile.nextLine();	
	    //System.out.println("Reading player: " + player.name + " from players.txt");
	    player.highestScore = Integer.parseInt(playersFile.nextLine());
	    player.level = Integer.parseInt(playersFile.nextLine());
	    players[index] = player;	//Filling the parameter array with objects of type player. 
	    index++;
	}
	try {
	    playersFile.close();
	} catch (Exception e) {
	    System.out.println("Error writing players.txt: " + e);
	    System.exit(1);
	}
    }

    public Player getPlayer(String name) {	//This method creates a new player if the player is not already registered in the players array. 
	for (int i=0; i<players.length; i++) {
	    if (players[i] != null) {
		if (players[i].name.equalsIgnoreCase(name))	
		    return players[i];
	    }
	}
			
	//New player - so create a new player.
	Player player = new Player();
	player.name = name;
	player.level = 5;
	player.highestScore = 0;
	//Add player to list.
	//System.out.println("Adding new player: " + name + " to players");
	for (int i=0; i<players.length; i++)
	    if (players[i] == null) {
		players[i] = player;
		return player;
	    }
	return player;		
    }
	
    public void writePlayers(Player[] players) {       //This method writes each player's information to the text file Players.txt. 
	//System.out.println("Writing players.txt");
	PrintWriter writer;
	try{
	    File playersFile = new File("Players.txt");
	    writer = new PrintWriter(playersFile);
	    for (int i=0; i<players.length; i++) {
		if (players[i] != null) {
		    //System.out.println("players is not null");
		    writer.print(players[i].name); writer.println(); 
		    writer.print(players[i].highestScore); writer.println();
		    writer.print(players[i].level);writer.println();
		}
	    }
	    writer.close();
	}catch(Exception e){
	    System.err.println("error writing players.txt: " + e);
	    System.exit(1);
	}
    }

    class MyCardPanel extends JPanel{
	StartPanel panel1;          //The staring panel of the game.
	InstructionsPanel panel2;   //The instructions panel of the game. 
	GamePanel  gamePanel;       //The game panel.
	CardLayout cards;           //The layout manager for each panel.      
	Player player;              //This variable keeps track of the players that play the game. 
		
	public MyCardPanel(){
	    //Creating each panel and setting the layout.
	    cards = new CardLayout();
	    setLayout(cards);
	    panel1 = new StartPanel(Color.white);
	    panel2 = new InstructionsPanel(Color.green);
	    gamePanel = new GamePanel(Color.white);

	    add(panel1, "Start"); add(panel2, "Instructions"); add(gamePanel,"Game");
	}

	class StartPanel extends JPanel  implements ActionListener{
	    public Color textColor = Color.black;      //Text color is originally black. 
	    JButton clickToViewInstructions;           //JButton which leads to the instructions panel. 
	    Timer colorTimer;                          //The timer that controls the color of the text. 
	    JTextField nameField;                      //JTextField where user enters their name.
	    Image tetrisImage;                         //Screenshot of the game in play. 
	    String imageName;                          //Name of the image. 
					
	    public StartPanel(Color c){
		setBackground(c);
		ChangeColor cc = new ChangeColor(this);
		colorTimer = new Timer(1000, cc);
		colorTimer.start();

		setLayout(null);
		readPlayers(players);

		//Creating the name field. 
		nameField = new JTextField(30);
		nameField.setText("Enter your name and press return.");
		nameField.setBounds(100, 500, 400, 25);
		nameField.addActionListener(this);
		add(nameField);

		setButtonForInstructions();
		readImage();

		ButtonListenerToInstructions bli = new ButtonListenerToInstructions();
		clickToViewInstructions.addActionListener(bli);	
	    }

	    public void readImage(){    //This method reads the image loaded on this panel. 
		imageName = "tetris.png";
		try{
		    tetrisImage = ImageIO.read(new File(imageName));		
		}catch(IOException e){
		    System.err.println("ERROR: Cannot read image file." + e);
		    System.exit(1);
		}
	    }

	    public void actionPerformed(ActionEvent evt) {      //When the user enteres their name, it shows on the status bar. 
		String name = nameField.getText();      //The players name is what is entered on the name field. 		        
		nameField.selectAll();
		//System.out.println("name: " + name);
		player = getPlayer(name);
		statusBar.setText("Hello " + name + " !");
	    }
							
	    public void setButtonForInstructions(){     //This method creates the JButton to continue on to the instructions panel. 
		clickToViewInstructions = new JButton();
		clickToViewInstructions.setText("Click to view further instructions");
		clickToViewInstructions.setPreferredSize(new Dimension(50,100));
		clickToViewInstructions.setBounds(100,550,400,50);
		add(clickToViewInstructions);
	    }

	    class ButtonListenerToInstructions implements ActionListener{       //Pressing the button makes the instructions panel appear. 
		public void actionPerformed(ActionEvent e){
		    cards.show(myCardPanel, "Instructions");
		    colorTimer.stop();
		}
	    }

	    public void paintComponent(Graphics g){
		//System.out.println(">StartPanel.paintComponent");
		super.paintComponent(g);
		//Drawing the title with a flashing text color. 
		g.setFont(new Font("Arial", Font.BOLD, 50));
		g.setColor(textColor);
		g.drawString("Welcome to Biotris", 60, 50);

		//System.out.println("<StartPanel.paintComponent");

		g.drawImage(tetrisImage, 100, 70, 400, 400, this);
	    }

	    public class ChangeColor implements ActionListener{     //This class flashes the text color of the title. 
		StartPanel sp;

		public ChangeColor(StartPanel panel1){
		    sp = panel1;
		}

		public void actionPerformed(ActionEvent e){
		    //System.out.println(">StartPanel.actionPerformed");
		    int i = (int)(Math.random()*6);
		    switch(i){
		    case 0: sp.textColor = Color.red; break;
		    case 1: sp.textColor = Color.orange; break;
		    case 2: sp.textColor = Color.yellow; break;
		    case 3: sp.textColor = Color.green; break;
		    case 4: sp.textColor = Color.blue; break;
		    case 5: sp.textColor = Color.magenta; break;
		    }
		    repaint();
		    //System.out.println("<StartPanel.actionPerformed");
		}
	    }  
	}

	class InstructionsPanel extends JPanel{       //Within the instructions panel, there is a card layout. The overall instructions panel consists of 3 sub panels.
	    CardPanel cardPanel;       
	    JLabel instructions;       //The instructions title. 

	    public InstructionsPanel(Color c){
		setBackground(c);

		cardPanel = new CardPanel();
		setLayout(new BorderLayout());
		add(cardPanel, BorderLayout.CENTER);

		//Creating the instructions title. 
		instructions = new JLabel("                 Instructions");
		instructions.setFont(new Font("Arial", Font.BOLD, 40));
		instructions.setForeground(Color.yellow);
		instructions.setPreferredSize(new Dimension(616,100));
		add(instructions, BorderLayout.NORTH);	
	    }

	    class CardPanel extends JPanel{
		//These panels are sub panels of the instruction panel. 
		Instruction1 panel1;        
		Instruction2 panel2;
		Instruction3 panel3;
		CardLayout card;

		public CardPanel(){
		    card = new CardLayout();
		    setLayout(card);

		    panel1 = new Instruction1(Color.white); 
		    panel2 = new Instruction2(Color.white);
		    panel3 = new Instruction3(Color.white);

		    add(panel1, "Instruction1"); add(panel2, "Instruction2"); add(panel3, "Instruction3");
		}

		class Instruction1 extends JPanel{
		    JTextArea instruction1;                  //Displays the instructions on the first sub instructions panel. 
		    JScrollPane spForInstruction1;           //Scroll pane for scrolling.
		    String textForInstruction1;              //The text displayed in the text area.
		    JButton clickToContinueToInstruction2;   //This button continues on to the next sub instructions panel. 

		    public Instruction1(Color c){
			setBackground(c);
			setLayout(new BorderLayout());
			textForInstruction1 = "Overview of Tetris: \n \n \n Blocks of random shapes and colors will be falling from the top of the screen on a timer. When the block hits the next available, empty grid square, it will stop moving. When a row is completely filled with blocks, 100 points are awarded. \n \n \n \n You, as the user, can control the block's movement by pressing 'a' to move it to the left, 's' to move it down, 'w' to move it up, and 'd' to move it to the right.";
			setTextOfInstruction1();
			setButtonForInstruction2();
			ButtonListenerToInstruction2 bl2 = new ButtonListenerToInstruction2();
			clickToContinueToInstruction2.addActionListener(bl2);
		    }

		    public void setTextOfInstruction1(){     //Creates the JTextArea. 
			instruction1 = new JTextArea();
			spForInstruction1 = new JScrollPane(instruction1);

			instruction1.setText(textForInstruction1);
			instruction1.setFont(new Font("Arial", Font.PLAIN, 30));
			instruction1.setMargin(new Insets(7,7,7,7));
			instruction1.setLineWrap(true);
			instruction1.setWrapStyleWord(true);
			instruction1.setForeground(Color.blue);
			instruction1.setEditable(false);
			add(spForInstruction1,BorderLayout.CENTER);
		    }

		    public void setButtonForInstruction2(){      //Creates the JButton for moving on to the next sub instructions panel. 
			clickToContinueToInstruction2 = new JButton();
			clickToContinueToInstruction2.setText("Click to view further instructions");
			clickToContinueToInstruction2.setPreferredSize(new Dimension(50,100));
			add(clickToContinueToInstruction2, BorderLayout.SOUTH);
		    }

		    class ButtonListenerToInstruction2 implements ActionListener{       //Displays the next sub instructions panel. 
			public void actionPerformed(ActionEvent e){
			    card.show(cardPanel, "Instruction2");
			}
		    }
		}

		class Instruction2 extends JPanel{
		    JTextArea instruction2;                      //Displays instructions for this sub instruction panel.
		    JScrollPane spForInstruction2;              //Scroll pane for JTextArea.
		    String textForInstruction2;                 //Text for JTextArea.
		    JButton clickToContinueToInstruction3;      //Moves on to the next sub instruction panel.

		    public Instruction2(Color c){
			setBackground(c);
			setLayout(new BorderLayout());
			textForInstruction2 = "Biotris: \n As the name suggests, there must be some aspect of biology to this game. \n \n If there is one empty grid square, surrouned by squares which contain blocks, a question mark will appear on that isolated block. The user may click on it and a question will appear. If answered correctly, a block will appear and 100 points will be awarded. If answered incorrectly, 50 points will be taken."; 
			setTextOfInstruction2();
			setButtonToInstruction3();
			ButtonListenerForInstruction3 bl3= new ButtonListenerForInstruction3();
			clickToContinueToInstruction3.addActionListener(bl3);
		    }

		    public void setTextOfInstruction2(){     //Creates the JTextArea. 
			instruction2 = new JTextArea();
			spForInstruction2 = new JScrollPane(instruction2);

			instruction2.setText(textForInstruction2);
			instruction2.setFont(new Font("Arial", Font.PLAIN, 30));
			instruction2.setMargin(new Insets(7,7,7,7));
			instruction2.setLineWrap(true);
			instruction2.setWrapStyleWord(true);
			instruction2.setForeground(Color.blue);
			instruction2.setEditable(false);
			add(spForInstruction2,BorderLayout.CENTER);
		    }

		    public void setButtonToInstruction3(){     //Creates the JButton to move on to the next sub instruction panel. 
			clickToContinueToInstruction3 = new JButton();
			clickToContinueToInstruction3.setText("Click to view game settings");
			clickToContinueToInstruction3.setPreferredSize(new Dimension(50,100));
			add(clickToContinueToInstruction3, BorderLayout.SOUTH);
		    }

		    class ButtonListenerForInstruction3 implements ActionListener{       //Displays the next sub instruction panel. 
			public void actionPerformed(ActionEvent e){
			    card.show(cardPanel, "Instruction3");
			}
		    }
		}

		class Instruction3 extends JPanel{

		    JSlider levelOfDifficulty;        //Lets the user choose the difficulty.
		    JTextField settingInstructions;   //Instructions on how to choose the difficulty.
		    JButton clickToContinueToGame;    //Button that displays the game panel. 

		    public Instruction3(Color c){
			setBackground(c);
			setLayout(null);

			instructionsForSetting();
			setDifficulty();
			setButtonToGame();

			ButtonListenerForGame blg = new ButtonListenerForGame();
			clickToContinueToGame.addActionListener(blg);

			SliderHandler sh = new SliderHandler();
			levelOfDifficulty.addChangeListener(sh);		
		    }	

		    public void instructionsForSetting(){     //Creates the JTextField to display instructions. 
			settingInstructions = new JTextField();
			settingInstructions.setText("            1) Choose your difficulty - 1 as the hardest and 5 as the easiest.");
			settingInstructions.setFont(new Font("Arial", Font.BOLD, 10));
			settingInstructions.setForeground(Color.blue);
			settingInstructions.setBackground(Color.yellow);
			settingInstructions.setFont( new Font("Serif", Font.PLAIN, 14 ));
			settingInstructions.setMargin( new Insets(7,7,7,7));
			settingInstructions.setBounds(0,100,600,100);
			settingInstructions.setEditable(false);
			add(settingInstructions);
		    }	

		    public void setDifficulty(){       //Creates the JSlider to choose difficulty. 
			levelOfDifficulty = new JSlider(1,5,5);
			levelOfDifficulty.setBackground(Color.lightGray);
			levelOfDifficulty.setMajorTickSpacing(1);
			levelOfDifficulty.setSnapToTicks(true);
			levelOfDifficulty.setPaintLabels(true);
			levelOfDifficulty.setBounds(150, 200, 300, 100);
			levelOfDifficulty.setBackground(Color.green);
			difficulty=5;
			add(levelOfDifficulty);				
		    }

		    class SliderHandler implements ChangeListener{        //Sets the JSlider value to difficulty and updates the status bar. 
			public void stateChanged(ChangeEvent e){
			    difficulty = levelOfDifficulty.getValue();
			    //System.out.println(difficulty);
			    statusBar.setText("Your difficulty is " + difficulty);
			}
		    }

		    public void setButtonToGame(){       //Creating the JButton to move on to the game panel. 
			clickToContinueToGame = new JButton();
			clickToContinueToGame.setText("Click to play");
			clickToContinueToGame.setPreferredSize(new Dimension(50,100));
			clickToContinueToGame.setBounds(0,400,616,100);
			add(clickToContinueToGame);
		    }

		    class ButtonListenerForGame implements ActionListener{      //Displays the game panel and starts the block timer. 
			public void actionPerformed(ActionEvent e){
			    //System.out.println("ButtonListenerForGame.actionPerformed");
			    gamePanel.startTimer();
			    cards.show(myCardPanel, "Game");
			}
		    }
		}
	    }
	}

	class GamePanel extends JPanel implements KeyListener,MouseListener{

	    int NUM_QUESTIONS = 19;        //The total number of questions.
	    Block [][] groundedBlocks = new Block [12][12];        //Grounded blocks stores blocks that have reached the next available grid square and can't move anymore. It is a 2D Array the same size as my grid. It is set to null if the particular index does not have a grounded block. 
	    Block fallingBlock;        //The block falling at this specific moment.
	    int positionOfBlock = 11;        //Each falling block has an initial y coordinate position of 11. 
	    public int height, width;        //Height and width of the frame.
	    int [] xGrid = new int[12];       //The x positions of each grid line. 
	    int [] yGrid = new int[12];       //The y positions of each grid line. 
	    boolean [][] isFilled = new boolean[12][12];        //This boolean array is set to true if that particular grid square contains a block.
	    boolean [][] eligibleQuestions = new boolean[12][12];        //This boolean array is set to true if the particular square is eligable for a question, meaning there is one empty square surrounded by filled squares.
	    Color [][] colors = new Color[12][12];        //This color array keeps track of which color belongs to what square on the grid.
	    Timer blockTimer;        //Timer that controls how fast the blocks fall.
	    Question [] questions = new Question[NUM_QUESTIONS];       //Array of questions that keeps track of which questions should be displayed. 
	    Scanner questionsFile, answersFile;        //The scanner that reads the questions and answers file from text files.
	    JPanel questionPanel;        //The panel that appears whenever a question is eligable to be shown. 
	    int arrayIndex=0;       //The array index that specifies which questions should be shown. 
	    int score = 0;        //An integer that keeps track of the current score. 
	    JRadioButton answer;      //A JRadioButton which is assigned to the correct answer of a specific answer. 
	    int questionGridX, questionGridY;      //The x and y position of the square which is eligable for a question. 
				
	    public GamePanel(Color backGround){ 
		setBackground(backGround);
		setPreferredSize(new Dimension(616,638));
		setLayout(new FlowLayout());
		//System.out.println(difficulty);
		//blockTimer.start(); dont start in the constructor. This causes the app to hang. 
		statusBar = new StatusBar();
		frame.getContentPane().add(statusBar, java.awt.BorderLayout.SOUTH);
		readQuestionsAndAnswers();

		for(int i=0; i<12; i++){      //Setting all the position of the isFilled array to false. No blocks have been grounded yet. 
		    for( int j=0; j<12; j++){
			isFilled[i][j]=false;
		    }
		}
		//Creating a new falling block with these properties. 
		fallingBlock = getRandomShape();
		fallingBlock.color = getRandomColor();
		fallingBlock.gridX = 0; 
		fallingBlock.gridY = 0;
		fallingBlock.falling = true;
		this.setFocusable(true);
		addKeyListener(this);
		addMouseListener(this);
	    }

	    public void readQuestionsAndAnswers(){    //This method reads the questions and answers file and places them into arrays.
		try{      //Reading the files. 
		    questionsFile = new Scanner(new File("Questions.txt"));
		    answersFile = new Scanner(new File("Answers.txt"));
		}catch(FileNotFoundException e){
		    System.err.println("error");
		    System.exit(1);
		}
		int index = 0;     //Keeps track of the current position in the questions array. 
		while(questionsFile.hasNext()){
		    questions[index] = new Question();       //For each question, store it in  postion index of the questions array. 
		    questions[index].ques = questionsFile.nextLine();    //The question itself is placed into variable ques of each question object. 
		    //System.out.println(questions[index].ques);
		    for(int i=0; i<3; i++){      //There are 3 possible answer choices for each question. Read all 3 and place them into an answer array part of each question object. 
			questions[index].answerChoices[i] = answersFile.nextLine();
			if(questions[index].answerChoices[i].startsWith("*")){      //The answer choice that starts with '*' is the correct answer.
			    questions[index].correctAnswer = i;     //Storing the correct answer as part of each question object.  
			    questions[index].answerChoices[i]= questions[index].answerChoices[i].replaceAll("\\*", "");      //When the answer choices are displayed on the screen, replace the '*' with an empty string. 
			    //System.out.println(questions[index].answerChoices[i]);
			}
			//System.out.println(questions[index].answerChoices[i]);
		    }
		    answersFile.nextLine();
		    index++;
		}
	    }

	    public void startTimer() {     //This method starts the block timer which is called as soon as the game panel is called to appear on the screen. 
		BlockMover bm = new BlockMover();
		//System.out.println("startTimer: difficulty: " + difficulty);
		blockTimer = new Timer(difficulty*250, bm);
		blockTimer.start();
	    }

	    public Block getRandomShape(){      //This method decides on what shape the falling block should be. 
		Block shape = new Square();  
		int i = (int)(Math.random()*9);
		switch(i){
		case 0: shape = new Square(); break;
		case 1: shape = new LShape(); break;
		case 2: shape = new JShape(); break;
		case 3: shape = new TShape(); break;
		case 4: shape = new SShape(); break;
		case 5: shape = new UpsideDownTShape(); break;
		case 6: shape = new Square(); break;
		case 7: shape = new VerticalLine(); break;
		case 8: shape = new SidewaysLShape(); break;
		}
		return shape;
	    }

	    public int getFallingBlockXCoordinate(){     //This method decides on the initial x coordinate of the falling block. 
		int i = (int)(Math.random()*10);
		return i;
	    }

	    public Color getRandomColor(){         //This method decides on the color of the falling block. 
		Random random = new Random();
		float hue = random.nextFloat();
		float saturation = 0.9f;
		float luminance = 1.0f; 
		Color color = Color.getHSBColor(hue, saturation, luminance);
		return color;
	    }

	    public void setUpShapeDrawing(Block shape, int x, int y){     //This method assigns values to the isFilled and Color arrays as falling blocks become grounded. Using the offsets (explained in the block classes ), each grid square contained in the specific block is assigned a value in these 2 arrays. 
		isFilled[shape.xCoordinateOffsets[0] + x][shape.yCoordinateOffsets[0] + y] = true; 
		isFilled[shape.xCoordinateOffsets[1] + x][shape.yCoordinateOffsets[1] + y] = true;
		isFilled[shape.xCoordinateOffsets[2] + x][shape.yCoordinateOffsets[2] + y] = true;
		isFilled[shape.xCoordinateOffsets[3] + x][shape.yCoordinateOffsets[3] + y] = true; 
		colors[shape.xCoordinateOffsets[0] + x][shape.yCoordinateOffsets[0] + y] = shape.color; 
		colors[shape.xCoordinateOffsets[1] + x][shape.yCoordinateOffsets[1] + y] = shape.color;
		colors[shape.xCoordinateOffsets[2] + x][shape.yCoordinateOffsets[2] + y] = shape.color;
		colors[shape.xCoordinateOffsets[3] + x][shape.yCoordinateOffsets[3] + y] = shape.color; 
	    }

	    public boolean isValid(Block shape, int x, int y){       //This method returns true if the fallingBlock is still valid to move. It checks and restrains the block from going off screen. 
		for(int i=0; i<4; i++){     //There are 4 offsets. 
		    if ((shape.xCoordinateOffsets[i] + x) >11) return false;     //Returns false if block is too far to the right.
		    if ((shape.xCoordinateOffsets[i] + x) <0) return false;      //Returns false if block is too far to the left.
		    if ((shape.yCoordinateOffsets[i] + y) >11) return false;     //Returns false if block is too far up.
		    if ((shape.yCoordinateOffsets[i] + y) <0 ) return false;     //Returns false if block is too far down
		    if(isFilled[shape.xCoordinateOffsets[i] + x][shape.yCoordinateOffsets[i] + y]) return false;     //If there is already a block in the next square, return false. 
		}
		return true;
	    }

	    public boolean isRowFull(int row) {       //This method returns true whenever an entire row is filled with blocks. 
		//System.out.println("isRowFull: row: " + row);
		for(int i=0; i<12; i++)
		    if (!isFilled[i][row]) {         //Return false if the row is not entirely filled. 
			//System.out.println("<isRowFull: false: i: " + i);
			return false;
		    }
		//System.out.println("<isRowFull: true");
		return true;
	    }
				
	    public void isEligibleForQuestions(){          //This method checks if a particular square is eligable for a question. 
		//System.out.println(">isEligibleForQuestions");
		if(!isFilled[0][11] && isFilled[1][11] && isFilled[0][10]){//Checking bottom left corner.
		    //System.out.println("Bottom corner left eligible for questions");
		    eligibleQuestions[0][11] = true;
		}
		if(!isFilled[11][11] && isFilled[10][11] && isFilled[11][10]){        //Checking bottom right corner.
		    eligibleQuestions[11][11] = true;
		    //System.out.println("Bottom right eligible for questions");
		}
		if(!isFilled[11][0] && isFilled[10][0] && isFilled[11][1]){       //Checking top right corner.
		    eligibleQuestions[11][0] = true;
		    //System.out.println("top right corner eligible for questions" );
		}
		if(!isFilled[0][0] && isFilled[0][1] && isFilled[1][0]){    // Checking top left corner.
		    eligibleQuestions[0][0] = true;
		    //System.out.println("top left corner eligible for questions" );
		}
		for(int i=0; i<12; i++){
		    for(int j=0;j<12;j++){
			if(i!=0 && i!=11 && !isFilled[i][11] && isFilled[i+1][11] && isFilled[i-1][11] && isFilled[i][10]){//Right side check.
			    eligibleQuestions[i][11] = true;
			    //System.out.println("right edge eligible for questions: i" +i);
			}
			if(j!=11 && j!=0 && !isFilled[11][j]&& isFilled[10][j] && isFilled[11][j+1] && isFilled[11][j-1]){//Bottom side check.
			    eligibleQuestions[11][j] = true;
			    //System.out.println("bottom  row eligible for questions: j" + j);		
			}
			if(i!=11 && i!=0 && !isFilled[i][0] && isFilled[i-1][0] && isFilled[i+1][0] && isFilled[i][1]){//Top side check.
			    eligibleQuestions[i][0] = true;
			}
			if(j!=0 && j!=11 && !isFilled[0][j] && isFilled[0][j+1] && isFilled[0][j-1] && isFilled[1][j]){//Left side check.
			    eligibleQuestions[0][j] = true;
			}
			if(i!=0 && i!=11 && j!=0 && j!=11 && !isFilled[i][j] && isFilled[i+1][j] && isFilled[i-1][j] && isFilled[i][j-1] && isFilled[i][j+1]){   //Check for any block in the middle of the grid. 
			    eligibleQuestions[i][j] = true;
			    //System.out.println("i: " + i + " j: " + j  + "eligible for questions: j" );
			}
			if ((isFilled[i][j]) && ((j+1)<12) && !(isFilled[i][j+1]))
			    eligibleQuestions[i][j+1] = true;
			//System.out.print(eligibleQuestions[i][j]);
		    }
							
		}
		//System.out.println("<isEligibleForQuestions");
	    }

	    public void paintComponent(Graphics g){
		super.paintComponent(g);
		g.setColor(Color.gray);
		for(int i=0; i<12; i++){           //Drawing the grid. 
		    g.drawLine((int)(width*i/12), 0, (int)(width*i/12), height);
		    xGrid[i] = (int)(width*i/12);
		    g.drawLine(0,(int)(height*i/12), width, (int)(height*i/12));
		    yGrid[i] = (int)(height*i/12);
		}
		width = this.getWidth();
		height = this.getHeight();
		//System.out.println(" width: "  + width + " height : " + height);

		for(int i=0; i<isFilled.length; i++) {       //If isFilled is true, paint a block with the assigned color on that square. 
		    for (int j=0; j<isFilled[i].length; j++) {
			//System.out.println("groundedBlocks: i : " + i + " j: " + j);
			if(isFilled[i][j]){
			    //System.out.println("not null");
			    g.setColor(colors[i][j]);
			    g.fillRect(xGrid[i], yGrid[j],51,51);
			}
			//System.out.println("gridX = " + groundedBlocks[i][j].gridX + " gridY = " + groundedBlocks[i][j].gridY);
		    }
		}

		if(fallingBlock!=null){      //Painting the falling block. 
		    g.setColor(fallingBlock.color);
		    for(int i=0; i<4; i++){
			//System.out.println(fallingBlock.xCoordinateOffsets[i] + " " + fallingBlock.gridX + " " + fallingBlock.yCoordinateOffsets[i] + " " + fallingBlock.gridY);
			g.fillRect(xGrid[fallingBlock.xCoordinateOffsets[i] + fallingBlock.gridX], yGrid[fallingBlock.yCoordinateOffsets[i] + fallingBlock.gridY],51,51);
		    }
		}

		for(int i=0; i<12; i++){      //Painting a 'Q' on squares where eligibleQuestions is true. 
		    for(int j=0; j<12; j++){
			if(eligibleQuestions[i][j]){
			    //System.out.println("Eligible questions: i: " + i  + " j: " + j);
			    g.setColor(Color.black);
			    g.setFont(new Font("Arial", Font.BOLD, 50));
			    g.drawString("?", xGrid[i]+10, yGrid[j]+40);							
			}	
		    }
		}

		requestFocus();
	    }

	    public boolean anyEligibleQuestions() {       //This method returns true if all squares are eligible for questions. 
		for(int i=0; i<12; i++)
		    for (int j=0;j<12; j++)
			if (eligibleQuestions[i][j]) return true;
		return false;
	    }
						
	    public void resetGame() {     //This method resets the game. 
		for(int i=0;i<12; i++)
		    for (int j=0; j<12; j++)      //False since no blocks are on the screen. 
			isFilled[i][j] = false;
						
		for(int i=0; i<12; i++)		 //False since no questions are needed yet. 
		    for(int j=0; j<12; j++)
			eligibleQuestions[i][j] = false;
							
		fallingBlock = new Block();
		fallingBlock = getRandomShape();
		fallingBlock.color = getRandomColor();
		fallingBlock.gridX = 0; 
		fallingBlock.gridY = 0;
		fallingBlock.falling = true;
		blockTimer.stop();
		blockTimer.start();
	    }
			
	    public class BlockMover implements ActionListener{
					
		public void actionPerformed(ActionEvent e){
		    //System.out.println(">BlockMover.actionPerformed");
		    if (fallingBlock == null)  {
			//If there are no more questions then announce game over and reset game.
			if (!anyEligibleQuestions()) {
			    statusBar.setText("Game Over! Your score is " + score);
			    if ((player!= null) && (score > player.highestScore)) {
				player.highestScore = score;
				if (player !=null) 
				    player.level = difficulty;
			    }
			    writePlayers(players);      //Write high score and difficulty to players.txt. 
			    resetGame();
			}
			repaint();
			return;
		    }
		    //System.out.println("falingBlock.gridY: " + fallingBlock.gridY);
		    if(isValid(fallingBlock, fallingBlock.gridX, fallingBlock.gridY+1)){     //If the next y grid square is valid, keep the block moving down. 
			//System.out.println(isValid());
			fallingBlock.gridY++;      //Block is still falling.
			//System.out.println("gridY = " + fallingBlock.gridY);    
		    } else{
			//Block just touched bottom.
			//System.out.println("Touched bottom: " + " fallingBlock.gridX: " + fallingBlock.gridX + " falingBlock.gridY: " + fallingBlock.gridY );

			setUpShapeDrawing(fallingBlock, fallingBlock.gridX, fallingBlock.gridY);     //Assign true to these specifc spots in isFilled and the color array. 
			for(int i=0; i<4; i++){      //Put the blocks in the groundedBlocks array when they can not travel any further. 
			    groundedBlocks[fallingBlock.xCoordinateOffsets[i] + fallingBlock.gridX][fallingBlock.yCoordinateOffsets[i] + fallingBlock.gridY] = fallingBlock;
			}
			isEligibleForQuestions();
			//If row gridY is full then add 100 to score.
			if (isRowFull(fallingBlock.gridY+1)) {
			    score+=100;
			    statusBar.setText("You have 100 more points for  a new full row! Your new score: " + score);								
			}
			if (fallingBlock.gridY>0) {      //Generate new blocks. 
			    fallingBlock = getRandomShape();
			    fallingBlock.color = getRandomColor();
			    fallingBlock.gridX = getFallingBlockXCoordinate(); 
			    fallingBlock.gridY = 0;

			    for (int i=0; i<5; i++) {
				if (isValid(fallingBlock, fallingBlock.gridX, fallingBlock.gridY)) {
				    break;
				} else {
				    fallingBlock.gridX = getFallingBlockXCoordinate(); 
				    fallingBlock.gridY = 0;
				}
			    }
			    fallingBlock.falling = true;

			} else {
			    fallingBlock = null;
			}
		    }

		    repaint();
		}
	    }
	    public void mouseClicked(MouseEvent e){}
	    public void mouseExited(MouseEvent e){}
	    public void mouseEntered(MouseEvent e){}
	    public void mouseReleased(MouseEvent e){}
	    public void mousePressed(MouseEvent e){
		// Get grid coordinates i,j from pixel coordinates pixelX e.getX(), pixelY e.getY().
		int gridX = (12*e.getX())/width;
		int gridY = (12*e.getY())/height;
		//System.out.println("pixelX: " + e.getX() + " pixelY: " + e.getY() + " gridX: " + gridX + " gridY: " + gridY);
		if(eligibleQuestions[gridX][gridY]){      //If the user clicks on a square which is eligable for a question, display the question panel. 
		    //System.out.println("pop a question");
		    questionPanel = new JPanel(new GridLayout(3,1));
		    questionPanel.setPreferredSize(new Dimension(500, 300));
		    //Add the question and answer choices to the question panel. 
		    add(questionPanel);
		    addQuestions();
		    displayAndChooseAnswerChoices();
		    questionGridX = gridX;
		    questionGridY = gridY;
		    //Once all the questions have been displayed, go back to the beginning and rewind. 
		    if(arrayIndex!=18){
			arrayIndex++;
		    }
		    else if(arrayIndex==18){
			arrayIndex=0;
		    }
		    repaint();
		    blockTimer.stop();     //Stop the timer when a question is being asked. 
		}
	    }

	    public void addQuestions(){     //This method adds a question to the question panel. 
		//System.out.println("addQuestions: ");
		JLabel question = new JLabel();
		questionPanel.add(question);
		question.setText("<html>" + questions[arrayIndex].ques + "</html>");
		question.setFont(new Font(("SansSerif"), Font.PLAIN,24));
		//System.out.println(questions[arrayIndex].ques);					
	    }
				
	    public void displayAndChooseAnswerChoices(){     //This method displays the answer choices on the question panel. 
		answer = null;      //Sets the correct answer to null. 
		//Creates 3 JRadioButtons - one for each answer choice.  
		JRadioButton answer1 = new JRadioButton("<html>" + questions[arrayIndex].answerChoices[0] + "</html>");
		answer1.setFont(new Font(("SansSerif"), Font.PLAIN,16));
		JRadioButton answer2 = new JRadioButton("<html>" + questions[arrayIndex].answerChoices[1] + "</html>");
		answer2.setFont(new Font(("SansSerif"), Font.PLAIN,16));
		JRadioButton answer3 = new JRadioButton("<html>" + questions[arrayIndex].answerChoices[2] + "</html>");
		answer3.setFont(new Font(("SansSerif"), Font.PLAIN,16));
		//System.out.println("<html>"+questions[arrayIndex].answerChoices[0]+"</html>");
		//System.out.println("<html>"+questions[arrayIndex].answerChoices[1]+"</html>");
		//System.out.println("<html>"+questions[arrayIndex].answerChoices[2]+"</html>");

		//ButtonGroup that controls the answer choices. 
		ButtonGroup answerGroup = new ButtonGroup();
		answerGroup.add(answer1);
		answerGroup.add(answer2);
		answerGroup.add(answer3);
						
		CheckForCorrectAnswer cfca = new CheckForCorrectAnswer();
		answer1.addActionListener(cfca);
		answer2.addActionListener(cfca);
		answer3.addActionListener(cfca);
				
		//Sets answer to the correct answer. 
		if(questions[arrayIndex].correctAnswer==0){
		    answer = answer1;
		}				
		else if(questions[arrayIndex].correctAnswer ==1){
		    answer = answer2;
		}				
		else if(questions[arrayIndex].correctAnswer ==2){
		    answer = answer3;
		}
				
		//Creates a JPanel to put the answer choices on. 
		JPanel radioPanel = new JPanel(new GridLayout(3,1));
		radioPanel.setPreferredSize(new Dimension(400,400));
		//radioPanel.setBounds(100,100,150,150);
		radioPanel.add(answer1);
		radioPanel.add(answer2);
		radioPanel.add(answer3);

		questionPanel.add(radioPanel);
		repaint();
					
	    }
				
	    class CheckForCorrectAnswer implements ActionListener{
		public void actionPerformed(ActionEvent e){     //When a JRadioButton answer choice is selected...
		    //System.out.println("CheckForCorrectAnswer.actionPerformed");
		    if(answer.isSelected()){
			score+=100;      //If answer is correct, increment score by 100. 
			//System.out.println("TRUE score: " + score);
			statusBar.setText("Correct! Your new score: " + score);         //Show new score on staus bar. 
						
			//Painting a block. 
			isFilled[questionGridX][questionGridY] = true;        
			colors[questionGridX][questionGridY] = getRandomColor();
			eligibleQuestions[questionGridX][questionGridY] = false;
			if (isRowFull(questionGridY)) {      //Check to see if the new block makes a full row. 
			    score+=100;
			    statusBar.setText("You have earned 100 more points for filling up a row! Your new score: " + score);						
			}
		    }
		    else{         //If answer is incorrect, the score is decremented by 50. 
			score-=50;
			System.out.println("FALSE score: " + score);
			statusBar.setText("Oops! Better luck next time. Your new score: " + score);      //Show new score on status bar. 						
		    }
		    remove(questionPanel);      //Remove the question panel after an answer has been selected. 
		    blockTimer.start();
							
		}
	    }

	    public void keyPressed(KeyEvent e){}
	    public void keyReleased(KeyEvent e){}
	    public void keyTyped(KeyEvent e){
		//System.out.println("key typed:" + e.getKeyChar());
		switch(e.getKeyChar()){
		case 'a': if (isValid(fallingBlock, fallingBlock.gridX-1, fallingBlock.gridY))     //Block moves to the left if valid. 
			fallingBlock.gridX = fallingBlock.gridX - 1; 
		    break;
		case 's' : if (isValid(fallingBlock, fallingBlock.gridX, fallingBlock.gridY+1))    //Block moves to down if valid.
			fallingBlock.gridY = fallingBlock.gridY + 1;
		    break;
		case 'd' : if (isValid(fallingBlock, fallingBlock.gridX+1, fallingBlock.gridY))    //Block moves to the right if valid.
			fallingBlock.gridX = fallingBlock.gridX + 1;
		    break;
		case 'w': if(isValid(fallingBlock, fallingBlock.gridX, fallingBlock.gridY-1))      //Blocks moves up if valid.
			fallingBlock.gridY = fallingBlock.gridY - 1;
		    break;
		case 'r':                                                                          //Resets the game.
		    statusBar.setText("Starting over!");       
		    resetGame();
		}
	    }
	}
    }
}

class Block{
    public Color color;               //Color of the block.
    public int gridX; int gridY;      //Most top left coordinate of block.
    public boolean falling;           //Is the block falling?
    //An array that holds the distance of each square in the block from the most left square. For example, in a square shape, the most left square is known as 0,0. The square to the right of it is known as 1,0. The square below the original is known as 0,1. The square diagonally across the orignal is known as 1,1.
    int [] xCoordinateOffsets;        
    int [] yCoordinateOffsets;
}

class Square extends Block{
    public Square() {
	xCoordinateOffsets = new int[]{0,0,1,1};
	yCoordinateOffsets = new int[]{0,1,0,1};
    }
}
class LShape extends Block{
    public LShape() {
	xCoordinateOffsets = new int[]{0,0,0,1};
	yCoordinateOffsets = new int[]{0,1,2,2};
    }
}
class JShape extends Block{
    public JShape() {
	xCoordinateOffsets = new int[]{1,1,1,0};
	yCoordinateOffsets = new int[]{0,1,2,2};
    }
}
class UpsideDownTShape extends Block{
    public UpsideDownTShape() {
	xCoordinateOffsets = new int[]{0,1,1,2};
	yCoordinateOffsets = new int[]{1,1,0,1};
    }
}
class SShape extends Block{
    public SShape() {
	xCoordinateOffsets = new int[]{0,1,1,2};
	yCoordinateOffsets = new int[]{0,0,1,1};
    }
}
class TShape extends Block{
    public TShape() {
	xCoordinateOffsets = new int[]{0,1,2,1};
	yCoordinateOffsets = new int[]{0,0,0,1};
    }
}

class VerticalLine extends Block{
    public VerticalLine(){
	xCoordinateOffsets = new int[]{0,0,0,0};
	yCoordinateOffsets = new int[]{0,1,2,3};
    }
}

class SidewaysLShape extends Block{
    public SidewaysLShape(){
	xCoordinateOffsets = new int[]{0,1,2,2};
	yCoordinateOffsets = new int[]{0,0,0,1};
    }
}

class Question{                                   //Each question object in the questions arry has one of the following. 
    String ques;                                  //The question.    
    String[]answerChoices = new String[3];        //3 possible answer choices. 
    int correctAnswer;                            //The index of the correct answer in the answer choices array mentioned above. 
}

class Player {         //Each player object in the players array has one of the following.
    String name;       //Name of each registered player.
    int highestScore;  //Player's highest score.
    int level;         //The level the player chooses.
}



class StatusBar extends JLabel {       //Displays the status bar label. 
    
    String currentMessage="";
	 
    /** Creates a new instance of StatusBar */
    public StatusBar() {
        super();     //Uses the properties of JLabel. 
        super.setPreferredSize(new Dimension(100, 50));
        setMessage("Welcome to Biotris");     //Original message. 
    }
    
    public void setMessage(String message) {      //Changes the message in the status bar.   
        setText(currentMessage+message); 
        currentMessage+=message;
    }        
}























































