import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;

/**
* This class provides the GUI and functionality for the game BOGGLE.
* It includes methods to manage the game state, handle user interactions, validate words, update scores, and display statistics.
* The game involves a grid of 9 letters (8 consonants and 1 vowel), where the player forms valid words by selecting adjacent letters.
* The player is given a time limit, and the game tracks various statistics such as score, words found, and highest-scoring words.
*
* The game ends when the timer reaches zero, and the final statistics are displayed to the user.
*
* SOURCES:
* - https://docs.oracle.com/javase/8/docs/api/javax/swing/JFrame.html
* - https://docs.oracle.com/javase/8/docs/api/java/awt/Component.html
* - https://docs.oracle.com/javase/8/docs/api/javax/swing/JComponent.html
* - https://web.mit.edu/6.005/www/sp14/psets/ps4/java-6-tutorial/components.html
* - https://www.youtube.com/watch?v=5G2XM1nlX5Q&t=609s
* - https://stackoverflow.com/questions/859691/a-fast-way-to-determine-whether-a-componet-is-found-in-jpanel
*/
public class MainFrame extends JFrame {
   final private Font mainFont = new Font("Segoe print", Font.BOLD, 18);
   // Panels/Layout
   private CardLayout cardLayout;
   private JPanel cardPanel;
   private JPanel finalPanel;


   // Labels
   private JLabel lbWord;
   private JLabel lbPoints;
   private JLabel timerLabel;


   // Stats
   private ArrayList<String> maxSingleWordScores;
   private ArrayList<Integer> maxFinalScores;
   private int highestScore; 
   private int score = 0;
   private ArrayList<Integer> wordCountPerGame;
   private ArrayList<String> wordsFound;
   private JTextArea foundWordsArea; 


   // Buttons/Grid Letters
   private JButton[][] buttons;
   private String[][] letterGrid;
   private ArrayList<String> selectedCoordinates;
   private String lastClickedCoordinate;


   // Timer
   private Timer timer;
   private int countdownTime = 90;


   private String word;
   private final ArrayList<String> dictionary = fileToArrayList(new File("/Users/dvizcarra/Documents/GitHub/Midterm25/MidtermProjectFinal/src/english.txt"));


   /**
    * Initializes the game window, sets up the grid, and prepares game components
    * CONTRIBUTOR: Drae
    */
   public void initialize() {
       // Initialize instance variables
       wordsFound = new ArrayList<>();
       maxSingleWordScores = new ArrayList<>();
       maxFinalScores = new ArrayList<>();
       wordCountPerGame = new ArrayList<>();
       highestScore = 0;
       letterGrid = new String[3][3]; 
       cardLayout = new CardLayout();
       cardPanel = new JPanel(cardLayout);
  
       // Create panels
       JPanel welcomePanel = createWelcomePanel();
       JPanel gridPanel = createGridPanel();
       finalPanel = createFinalPanel();
  
       // Initialize card panel with all 3 panels
       cardPanel.add(welcomePanel, "Welcome");
       cardPanel.add(gridPanel, "Grid");
       cardPanel.add(finalPanel, "Final");
  
       add(cardPanel);
      
       // Display GUI
       setSize(500, 500);
       setMinimumSize(new Dimension(300, 400));
       setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
       setVisible(true);
   }
  
   /**
    * Starts the countdown timer, initializes the label and sets the timer action.
    * CONTRIBUTORS: Lara and Elle
    */
   private void startTimer(int selectedTime) {
       countdownTime = selectedTime;
       timerLabel.setText("Time left: " + countdownTime + " seconds"); //updates countdown time
      
       // Making timer
       timer = new Timer(1000, (ActionEvent e) -> {
           countdownTime--; //decrement countdown time
           if (countdownTime >= 0) {
               timerLabel.setText("Time left: " + countdownTime + " seconds");
           } else { //when timer reaches 0
               timer.stop(); //stops timer
               makeFinalStats(); 
               updateFinalPanel();
               cardLayout.show(cardPanel, "Final"); //takes to final page
           }
       });


       timer.start(); // Start timer
   }


   /**
    * Handles button click action to select or deselect a button on the grid.
    * CONTRIBUTOR: Drae
    *
    * @param coordinate The coordinate of the clicked button
    * @param letter The letter at the clicked coordinate
    */
   private void buttonClickAction(String coordinate) {
       if (selectedCoordinates.contains(coordinate)) {
           // If the button is already selected, deselect it and reset the board
           deselectButton();
       } else {
           // If the button is not selected, check if it's adjacent to the last selected one
           if (lastClickedCoordinate == null || checkAdjacent(lastClickedCoordinate, coordinate)) {
               selectButton(coordinate);
           }
       }
   }


   /**
    * Selects a button on the grid and updates the current word.
    * CONTRIBUTOR: Drae
    *
    * @param coordinate The coordinate of the clicked button
    * @param letter The letter at the clicked coordinate
    */
   private void selectButton(String coordinate) {
       selectedCoordinates.add(coordinate);
      
       int row = Integer.parseInt(coordinate.split(",")[0]);
       int col = Integer.parseInt(coordinate.split(",")[1]);
       String currentLetter = letterGrid[row][col];
      
       word += currentLetter;
       lbWord.setText(word);
  
       buttons[row][col].setBackground(new Color(120, 120, 255));
       lastClickedCoordinate = coordinate;
   }


   /**
    * Resets the colors of all the buttons back to the default color (white).
    * CONTRIBUTOR: Drae
    *
    */
   private void resetButtonColors() {
       // reset all button colors to default (white)
       for (int i = 0; i < 3; i++) {
           for (int j = 0; j < 3; j++) {
               buttons[i][j].setBackground(Color.WHITE);
           }
       }
   }


   /**
    * Deselects the selected button and checks if the word is valid, too short, or already found, and
    * updates the word list and score accordingly.
    * CONTRIBUTOR: Drae
    *
    * @param coordinate The coordinate of the deselected button
    */
   private void deselectButton() {
       if (word.length() <= 2) {
           word = "WORD TOO SHORT";
           lbWord.setText(word);
       }
       else if (wordsFound.contains(word.toUpperCase())) {
           word = "WORD ALREADY FOUND";
           lbWord.setText(word);
       }
       // Check if word is valid, then update points and foundWords box
       else if (isValidWord(word.toLowerCase())) {
           wordsFound.add(word.toUpperCase());
           updatePoints();
           foundWordsArea.setText("WORDS FOUND: " + String.join(", ", wordsFound));
       }
       else {
           word = word + " NOT FOUND";
           lbWord.setText(word);
       }
      
       // Reset everything
       word = "";
       lbWord.setText(word);
       selectedCoordinates.clear();
       resetButtonColors();
       lastClickedCoordinate = null;
   }


   /**
    * Checks if two coordinates are adjacent to each other on the grid.
    * CONTRIBUTOR: Drae
    *
    * @param lastCoordinate The last clicked coordinate
    * @param currentCoordinate The current clicked coordinate
    * @return true if the coordinates are adjacent, false otherwise
    */
   private boolean checkAdjacent(String lastCoordinate, String currentCoordinate) {
       String[] currentCoords = currentCoordinate.split(",");
       int[] current = {Integer.parseInt(currentCoords[0]), Integer.parseInt(currentCoords[1])};


       // Check adjacency based on the 8 possible directions around the current cell using coordinate adjacency map
       Map<String, ArrayList<int[]>> coordinateMap = createCoordinateMap();


       ArrayList<int[]> adjacentCoords = coordinateMap.get(lastCoordinate);
       for (int[] adj : adjacentCoords) {
           if (Arrays.equals(adj, current)) {
               return true;
           }
       }


       return false;
   }


   /**
    * Creates a map of coordinates with their  adjacent coordinates on the grid.
    * CONTRIBUTOR: Drae
    *
    * @return A map of coordinates to their adjacent coordinates
    */
   private Map<String, ArrayList<int[]>> createCoordinateMap() {
       Map<String, ArrayList<int[]>> coordinateMap = new HashMap<>();


       for (int i = 0; i < 3; i++) {
           for (int j = 0; j < 3; j++) {
               ArrayList<int[]> adjacentCoords = new ArrayList<>();
               adjacentCoords.add(new int[]{i - 1, j - 1});  // Top-left
               adjacentCoords.add(new int[]{i, j - 1});      // Left
               adjacentCoords.add(new int[]{i + 1, j - 1});  // Bottom-left
               adjacentCoords.add(new int[]{i - 1, j});      // Top
               adjacentCoords.add(new int[]{i, j + 1});      // Right
               adjacentCoords.add(new int[]{i + 1, j});      // Bottom
               adjacentCoords.add(new int[]{i - 1, j + 1});  // Top-right
               adjacentCoords.add(new int[]{i + 1, j + 1});  // Bottom-right


               // Remove out-of-bound coordinates
               for (int k = adjacentCoords.size() - 1; k >= 0; k--) {
                   int[] coord = adjacentCoords.get(k);
                   if (coord[0] < 0 || coord[1] < 0 || coord[0] >= 3 || coord[1] >= 3) {
                       adjacentCoords.remove(k);
                   }
               }


               coordinateMap.put(i + "," + j, adjacentCoords);
           }
       }
       return coordinateMap;
   }


   /**
    * Checks if a word is valid by comparing it against a dictionary.
    * CONTRIBUTOR: Lara
    *
    * @param userGuess The word to check
    * @return true if the word is valid, false otherwise
    */
   private boolean isValidWord(String userGuess){
       // Check if it is english dictionary (lowercaseWords)
       for (int i = 0; i< dictionary.size(); i++){
           if (userGuess.equals(dictionary.get(i))){
               return true; // Return true if it is the dictionary
           }
       }
       return false; // Return false if not in dictionary
   }
  
   /**
    * Updates the score based on the length of the found word (100 Points are granted per letter).
    * CONTRIBUTOR: Lara
    */
   private void updatePoints(){
       Map<Integer, Integer> pointSystem = new HashMap<>(); //Makes a map for the point


       // Prevent points being added if the length of the word is 1 or 2
       pointSystem.put(1, 0);
       pointSystem.put(2, 0);


       // Setting up the point system
       for (int i = 3; i <= 9; i++) {
           pointSystem.put(i, i * 100);
       }


       score += pointSystem.get(word.length()); // Update current score
       lbPoints.setText("SCORE: " + score); // Send current score to the button
   }  


   /**
    * Generates a 3x3 letter grid with random letters based on frequency data from the dictionary.
    * CONTRIBUTOR: Chloe
    *
    * @return A 2D array representing the letter grid
    */
    private String[][] generateLetterArr() { //CHLOE
       String letters = generate8Letters(findFrequency(dictionary));
       String vowel = generate1Vowel(findVowelFrequency(dictionary));
       String nineLetters = combineNine(letters, vowel);


       //Make letters uppercase so it looks nice in the display
       String[][] lowerCaseMatrix = toMatrix(nineLetters);
       String[][] upperCaseMatrix = new String[3][3];


       for (int i = 0; i < 3; i++){
           for (int j = 0; j < 3; j++){
               upperCaseMatrix[i][j] = lowerCaseMatrix[i][j].toUpperCase();
           }
       }


       return upperCaseMatrix;
   }


   /**
    * Convert words in dictionary file to an ArrayList of strings.
    * CONTRIBUTORS: Elle (Main), Chloe (Editor)
    *
    * @param filename The file to read
    * @return A list of words from the file
    */
   public static ArrayList<String> fileToArrayList(File filename) {
      ArrayList<String> words = new ArrayList<>();
      try (Scanner scan = new Scanner(filename)) {
          if (scan.hasNext()) {
              while (scan.hasNextLine()) {
                  String key = scan.nextLine();
                  if (key.indexOf(": [") != -1) {
                      String toAdd = key.substring(key.indexOf("\"") + 1, key.indexOf("\"", key.indexOf("\"") + 1));
                      words.add(toAdd.toLowerCase()); // Convert to lowercase
                  }
              }
          }
          return words;
      } catch (Exception ex) {
          ex.printStackTrace();
          return words;
      }
  }


   /**
    * Finds the frequency of each letter in the dictionary
    * CONTRIBUTOR: Chloe
    *
    * @param allWords The list of words to analyze
    * @return A list of frequencies for each letter in the alphabet
    */
  public ArrayList<Double> findFrequency(ArrayList<String> allWords) {
       ArrayList<Integer> alphabetCount = new ArrayList<>();
       for (int i = 0; i < 26; i++) {
           alphabetCount.add(0);
       }

       String letters = "abcdefghijklmnopqrstuvwxyz"; //whole alphabet
       for (int i = 0; i < allWords.size(); i++) {
           for (int j = 0; j < allWords.get(i).length(); j++) {
               char currentChar = allWords.get(i).charAt(j); //gets each character in a given word
               int index = letters.indexOf(currentChar); //gets index of that character in the alphabet, will line up with the index in the array list that we want to put it in at
               if (index != -1) { //only valid letters
                   alphabetCount.set(index, alphabetCount.get(index) + 1); //increment in alphabetCount
               }
           }
       }

       int totalCharacters = 0;
       for (int i = 0; i < 26; i++) { //count total characters for denominator of bucket variable
           totalCharacters += alphabetCount.get(i);
       }




       ArrayList<Double> frequencies = new ArrayList<>(); //create frequency array with 0.0 in every slot
       for (int i = 0; i < 26; i++) {
           frequencies.add(0.0);
       }


       //divide each count by total characters to get the frequencies
       for (int i = 0; i < 26; i++) {
           if (totalCharacters > 0) {
               frequencies.set(i, ((double) alphabetCount.get(i) / totalCharacters));
           }
       }
       return frequencies;
   }


   /**
    * Generates 8 random letters based on their frequencies.
    * CONTRIBUTOR: Chloe
    *
    * @param frequencies The letter frequencies
    * @return A string of 8 random letters
    */
   public String generate8Letters(ArrayList<Double> frequencies) {
       String letters = "abcdefghijklmnopqrstuvwxyz"; //whole alphabet
       String result = "";
 
       for (int i = 0; i < 8; i++) {
           double rand = Math.random(); //generate random number between 0 and 1
           double cumulativeProbability = 0.0;
 
           for (int j = 0; j < frequencies.size(); j++) {
               cumulativeProbability += frequencies.get(j); //to create gaps (EX. a = 0-0.08, b = 0.08 - 0.12, etc)
               if (rand <= cumulativeProbability) {
                   result += letters.charAt(j);
                   break;
               }
           }
       }
 
       return result;
   }
  
   /**
    * Finds the frequency of vowels in the given list of words.
    * CONTRIBUTOR: Chloe
    *
    * @param allWords The list of words to analyze
    * @return A list of vowel frequencies
    */
   public ArrayList<Double> findVowelFrequency(ArrayList<String> allWords) {
       ArrayList<Integer> vowelCount = new ArrayList<>(); //same thing but for vowels
       for (int i = 0; i < 5; i++) {
           vowelCount.add(0);
       }
       String letters = "aeiou"; // all vowels
       for (int i = 0; i < allWords.size(); i++) {
           for (int j = 0; j < allWords.get(i).length(); j++) {
               char currentChar = allWords.get(i).charAt(j); //gets each character in a given word
               int index = letters.indexOf(currentChar); //gets index of that character in the alphabet, will line up with the index in the array list that we want to put it in at
               if (index != -1) { // Only valid letters
                   vowelCount.set(index, vowelCount.get(index) + 1); // increment in alphabetCount
               }
           }
       }
       int totalVowels = 0;
       for (int i = 0; i < 5; i++) { // count total characters
           totalVowels += vowelCount.get(i);
       }
       ArrayList<Double> vowelFrequencies = new ArrayList<>();
       for (int i = 0; i < 5; i++) {
           vowelFrequencies.add(0.0);
       }
       for (int i = 0; i < 5; i++) {
           if (totalVowels > 0) {
               vowelFrequencies.set(i, ((double) vowelCount.get(i) / totalVowels)); // divide each count by total characters to get the frequencies
           }
       }
       return vowelFrequencies;
   }
  
   /**
    * Generates one random vowel based on vowel frequencies.
    * CONTRIBUTOR: Chloe
    *
    * @param vowelFrequencies The vowel frequencies
    * @return A string containing a single vowel
    */
   public String generate1Vowel(ArrayList<Double> vowelFrequencies) { //chloe
       String vowels = "aeiou";
       String result = "";
 
       for (int i = 0; i < 1; i++) {
           double rand = Math.random(); //generate random number between 0 and 1
           double cumulativeProbability = 0.0;
 
           for (int j = 0; j < vowelFrequencies.size(); j++) {
               cumulativeProbability += vowelFrequencies.get(j); //to create gaps (EX. a = 0-0.08, b = 0.08 - 0.12, etc)
               if (rand <= cumulativeProbability) {
                   result += vowels.charAt(j);
                   break;
               }
           }
       }
 
       return result;
   }


   /**
    * Combines the 8 letters and 1 vowel into a string of 9 letters, with the vowel in the middle
    * CONTRIBUTOR: Chloe
    *
    * @param eightLetters The string of 8 letters
    * @param oneVowel The single vowel to add
    * @return A string combining the 8 letters and 1 vowel
    */
   public String combineNine(String eightLetters, String oneVowel){ 
       String allNine = "";
       String firstFour = eightLetters.substring(0,4);
       allNine += firstFour;
       allNine += oneVowel; //add vowel at fourth index
       String lastFour = eightLetters.substring(4);
       allNine += lastFour;
       return allNine;
   }


   /**
    * Converts a string of 9 letters into a 3x3 matrix.
    * CONTRIBUTOR: Chloe
    *
    * @param nineLetters The string of 9 letters
    * @return A 2D array representing a 3x3 matrix
    */
   public static String[][] toMatrix(String nineLetters){
       String[][] grid = new String[3][3];
       grid[0][0] = nineLetters.charAt(0) + "";
       grid[0][1] = nineLetters.charAt(1) + "";
       grid[0][2] = nineLetters.charAt(2) + "";
       grid[1][0] = nineLetters.charAt(3) + "";
       grid[1][1] = nineLetters.charAt(4) + "";
       grid[1][2] = nineLetters.charAt(5) + "";
       grid[2][0] = nineLetters.charAt(6) + "";
       grid[2][1] = nineLetters.charAt(7) + "";
       grid[2][2] = nineLetters.charAt(8) + "";
       return grid;
   }


   /**
    * Makes the final stats at the end of the game, including word count, highest scoring words, and final score.
    * CONTRIBUTOR: Lara (Main), Drae (Editor)
    */
   private void makeFinalStats() {
       // Store number of words found in this game
       wordCountPerGame.add(wordsFound.size());
      
       // Add the largest word from this round to the instance variable max word list
       String currentMax = "";
       for (String wordFound : wordsFound) { //go through the list of words found
           if (wordFound.length() > currentMax.length()) {
               currentMax = wordFound; // Reset currentMax word if the word in wordsFound is longer
           }
       }
       maxSingleWordScores.add(currentMax);
  
       // Update the max score list with the current round's score
       maxFinalScores.add(score);
       if (score > highestScore) {
           highestScore = score;
       }
   }


    /**
    * Returns the final statistics text to display at the end of the game.
    * CONTRIBUTOR: Lara (Main), Elle (Main), Drae (Editor)
    *
    * @return A string containing the final stats
    */
   private String getFinalStatsText() {
       StringBuilder statsText = new StringBuilder();
      
       // Find max word (LARA)
       String maxWord = "";
       for (int i = 0; i< maxSingleWordScores.size(); i++){  //loop through all the max words
           if ((maxSingleWordScores.get(i).length() > maxWord.length())){ 
               maxWord = maxSingleWordScores.get(i); //change max word if the found word is longer than the previous max word
           }
       }
      
       if (maxWord.isEmpty()) {
           statsText.append("The word that earned you the most points was: None")
                    .append(" earning you 0 points\n");
       } else {
           statsText.append("The word that earned you the most points was: ")
                    .append(maxWord)
                    .append(" earning you ")
                    .append(maxWord.length() * 100)
                    .append(" points\n");
       }
  
       // Max total score (ELLE)
       int maxScore = 0;
       if (maxFinalScores.isEmpty()) {
           maxScore = 0;
       } else {
            //iterate through max final scores
           for (int i = 0; i<maxFinalScores.size(); i++){
               if ((maxFinalScores.get(i) > maxScore)){
                    //if greater, update maxScore
                   maxScore = maxFinalScores.get(i);
               }
           }
   
       }
       statsText.append("Your current high score is: ").append(maxScore).append("\n");


       // Max amount of words that they found out of all of their games (ELLE)
       int maxAmount = 0;
       if (wordCountPerGame.isEmpty()) {
           maxAmount = 0;
       } else {
            //iterate through wordCountPerGame and find maxAmount (update Max Amount if the new value is greater)
           for (int i =0; i<wordCountPerGame.size(); i++){
               if (wordCountPerGame.get(i) > maxAmount){
                   maxAmount = wordCountPerGame.get(i);
               }
           }
   
       }
       statsText.append("Most words found in a single game: ").append(maxAmount);
  
       statsText.append("\nTo play again, click a desired number of seconds again below!");       
       return statsText.toString();
   }


   /**
    * Creates and returns the first panel/screen that is displayed when the game starts.
    * This panel contains the title, instructions, and the play button.
    * CONTRIBUTOR: Drae (Main), Lara (Editor)
    *
    * @return JPanel - the title panel
    */
   private JPanel createWelcomePanel() {
       Color purple = new Color(78, 12, 176);
       //Initialize panel
       JPanel welcomePanel = new JPanel();
       welcomePanel.setLayout(new BorderLayout(0, 20));
       welcomePanel.setBackground(purple);
       welcomePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
  
       // Create title label
       JLabel welcomeLabel = new JLabel("BOGGLE");
       welcomeLabel.setFont(mainFont.deriveFont(48f));
       welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
       welcomeLabel.setForeground(Color.WHITE);
  
       // Directions
       JLabel directionsLabel = new JLabel(
           "<html><div style='text-align: left; width: 400px;'>" +
           "Directions:<br><br>" +
           "1. Click adjacent letters to form words<br><br>" +
           "2. Words must be atleast 3 letters. Longer words score more points.<br><br>" +
           "3. Click the number of seconds below to start!" +
           "</div></html>"
       );
       directionsLabel.setFont(mainFont);
       directionsLabel.setForeground(Color.WHITE);
       directionsLabel.setHorizontalAlignment(SwingConstants.LEFT);

       // Displays all of the timer option buttons (LARA)
       JPanel timerOptionsPanel = new JPanel (new FlowLayout(FlowLayout.CENTER,30,0));
       timerOptionsPanel.setBackground(purple);
       timerOptionsPanel.add(thirtySeconds()); // 30 second timer button
       timerOptionsPanel.add(sixtySeconds()); //60 second timer button
       timerOptionsPanel.add(ninetySeconds()); // 90 second timer button
       timerOptionsPanel.add(oneTwentySeconds()); //120 second timer button
  
       // Center panel for directions
       JPanel centerPanel = new JPanel(new GridBagLayout());
       centerPanel.setBackground(purple);
       GridBagConstraints gbc = new GridBagConstraints();
       gbc.gridx = 0;
       gbc.gridy = 0;
       gbc.anchor = GridBagConstraints.WEST;  // Align the label to the left
       centerPanel.add(directionsLabel, gbc);
  
       // Adding all components to panel
       welcomePanel.add(welcomeLabel, BorderLayout.NORTH);
       welcomePanel.add(centerPanel, BorderLayout.CENTER);
       welcomePanel.add(timerOptionsPanel, BorderLayout.SOUTH);
  
       return welcomePanel;
   }


   /**
   * Creates and returns a JButton configured for a "30 Seconds" game mode.
   * CONTRIBUTER: Lara
   * 
   * @return JButton A button labeled "30"
   */
   private JButton thirtySeconds(){
       //setting up button preferences: 
        JButton thirtySec = new JButton("30"); 
       thirtySec.setFont(mainFont);
       thirtySec.setBackground(Color.WHITE);
       thirtySec.setForeground(Color.BLACK);
       thirtySec.setOpaque(true);
       thirtySec.setBorderPainted(false);
       thirtySec.setFocusPainted(false);
       thirtySec.setPreferredSize(new Dimension(80,50));
       thirtySec.addActionListener (e -> {
           cardLayout.show(cardPanel, "Grid"); // Switch to grid panel when pressed
           resetGame(30); // for when it is the 2nd+ time playing
    });
       return thirtySec;
   }


   /**
   * Creates and returns a JButton configured for a "60 Seconds" game mode.
   * CONTRIBUTER: Lara
   * 
   * @return JButton A button labeled "60"
   */
   private JButton sixtySeconds(){ //Lara
        //setting up button preferences:  
        JButton sixtySec = new JButton("60");
       sixtySec.setFont(mainFont);
       sixtySec.setBackground(Color.WHITE);
       sixtySec.setForeground(Color.BLACK);
       sixtySec.setOpaque(true);
       sixtySec.setBorderPainted(false);
       sixtySec.setFocusPainted(false);
       sixtySec.setPreferredSize(new Dimension(80,50));
       sixtySec.addActionListener (e -> {
           cardLayout.show(cardPanel, "Grid"); // Switch to grid panel when pressed
           resetGame(60); //for when it is the 2nd+ time playing
   });
       return sixtySec;
   }

   /**
   * Creates and returns a JButton configured for a "90 Seconds" game mode.
   * CONTRIBUTER: Lara
   * 
   * @return JButton A button labeled "90"
   */
   private JButton ninetySeconds(){ //Lara
    //setting up button preferences: 
       JButton ninetySec = new JButton("90");
       ninetySec.setFont(mainFont);
       ninetySec.setBackground(Color.WHITE);
       ninetySec.setForeground(Color.BLACK);
       ninetySec.setOpaque(true);
       ninetySec.setBorderPainted(false);
       ninetySec.setFocusPainted(false);
       ninetySec.setPreferredSize(new Dimension(80,50));
       ninetySec.addActionListener (e -> {
           cardLayout.show(cardPanel, "Grid"); // Switch to grid panel when pressed
           resetGame(90); //for when it is the 2nd+ time playing
    });
       return ninetySec;  
   }

   /**
   * Creates and returns a JButton configured for a "120 Seconds" game mode.
   * CONTRIBUTER: Lara
   * 
   * @return JButton A button labeled "120"
   */
   private JButton oneTwentySeconds(){ //Lara
    //setting up button preferences: 
       JButton oneTwentySec = new JButton("120");
       oneTwentySec.setFont(mainFont);
       oneTwentySec.setBackground(Color.WHITE);
       oneTwentySec.setForeground(Color.BLACK);
       oneTwentySec.setOpaque(true);
       oneTwentySec.setBorderPainted(false);
       oneTwentySec.setFocusPainted(false);
       oneTwentySec.setPreferredSize(new Dimension(80,50));
       oneTwentySec.addActionListener (e -> {
           cardLayout.show(cardPanel, "Grid"); // Switch to grid panel when pressed
           resetGame(120); //for when it is the 2nd+ time playing
    });
       return oneTwentySec;
   }

   /**
    * Creates and returns the grid panel, which is displayed during the game.
    * This panel contains the letter grid, word/score display, and the timer.
    * CONTRIBUTOR: Drae
    *
    * @return JPanel - the grid panel
    */
   private JPanel createGridPanel() {
       // Initialize instance vairalbes
       word = "";
       selectedCoordinates = new ArrayList<>();
       lastClickedCoordinate = null;
       buttons = new JButton[3][3];
       letterGrid = generateLetterArr();
  
       JPanel gridPanel = new JPanel();
       gridPanel.setLayout(new BorderLayout()); 
  
       JPanel topPanel = new JPanel();
       topPanel.setLayout(new BorderLayout());
      
       // Word Label
       lbWord = new JLabel();
       lbWord.setFont(mainFont);
       lbWord.setText("");
       lbWord.setHorizontalAlignment(SwingConstants.LEFT); 
      
       // Score label
       lbPoints = new JLabel();
       lbPoints.setFont(mainFont);
       lbPoints.setText("SCORE: " + score); 
       lbPoints.setHorizontalAlignment(SwingConstants.RIGHT); 
  
       topPanel.add(lbWord, BorderLayout.WEST); 
       topPanel.add(lbPoints, BorderLayout.EAST);
  
       // Timer
       timerLabel = new JLabel("Time left: 60 seconds", SwingConstants.CENTER);
       timerLabel.setFont(mainFont);
       timerLabel.setPreferredSize(new Dimension(500, 30));
      
       //Buttons
       JPanel buttonsPanel = new JPanel();
       buttonsPanel.setLayout(new GridLayout(3, 3, 5, 5));
  
       for (int i = 0; i < 3; i++) {
           for (int j = 0; j < 3; j++) {
               final int row = i;
               final int col = j;
               buttons[i][j] = new JButton(letterGrid[i][j]);
               buttons[i][j].setFont(mainFont);
               buttons[i][j].setOpaque(true);
               buttons[i][j].setBorderPainted(false);
               buttons[i][j].setBackground(Color.WHITE);
              
               buttons[i][j].addActionListener(e -> {
                   String coordinate = row + "," + col;
                   buttonClickAction(coordinate);
               });
              
               buttonsPanel.add(buttons[i][j]);
           }
       }
  
       // Found words box
       foundWordsArea = new JTextArea();
       foundWordsArea.setFont(new Font("Segoe print", Font.PLAIN, 14));
       foundWordsArea.setEditable(false);
       foundWordsArea.setBackground(new Color(240, 240, 240));
       foundWordsArea.setText("WORDS FOUND: ");
      
       JScrollPane scrollPane = new JScrollPane(foundWordsArea);
       scrollPane.setPreferredSize(new Dimension(500, 100));
  
       JPanel bottomPanel = new JPanel();
       bottomPanel.setLayout(new BorderLayout());
       bottomPanel.add(timerLabel, BorderLayout.NORTH);
       bottomPanel.add(scrollPane, BorderLayout.CENTER);
  
       // Adding all components to penl
       gridPanel.add(topPanel, BorderLayout.NORTH); 
       gridPanel.add(buttonsPanel, BorderLayout.CENTER); 
       gridPanel.add(bottomPanel, BorderLayout.SOUTH); 
  
       return gridPanel;
   }


   /**
    * Creates and returns the final panel that is displayed at the end of the game.
    * This panel contains the game-over message, the final score, and additional stats.
    * CONTRIBUTOR: Elle (Main), Lara (Main), Drae (Main)
    *
    * @return JPanel - the final panel
    */
   private JPanel createFinalPanel() {
       finalPanel = new JPanel();
       finalPanel.setLayout(new BorderLayout(10, 10));
       finalPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
  
       // "GAME OVER" label
       JLabel finalLabel = new JLabel("GAME OVER");
       finalLabel.setFont(mainFont);
       finalLabel.setHorizontalAlignment(SwingConstants.CENTER);
  
       // Score label
       final JLabel scoreLabel = new JLabel(); 
       scoreLabel.setFont(mainFont);
       scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
  
       JPanel labelPanel = new JPanel();
       labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
      
       finalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
       scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
      
       labelPanel.add(finalLabel);
       labelPanel.add(scoreLabel);
  
       JPanel contentPanel = new JPanel();
       contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
  
       // Box for stats
       JTextArea statsArea = new JTextArea();
       statsArea.setFont(mainFont.deriveFont(Font.PLAIN, 14));
       statsArea.setEditable(false);
       statsArea.setLineWrap(true);
       statsArea.setWrapStyleWord(true);
       statsArea.setBackground(finalPanel.getBackground());
       statsArea.setPreferredSize(new Dimension(300, 100));
  
       // Box for words found
       JTextArea wordsFoundArea = new JTextArea();
       wordsFoundArea.setFont(mainFont.deriveFont(Font.PLAIN, 14));
       wordsFoundArea.setEditable(false);
       wordsFoundArea.setLineWrap(true);
       wordsFoundArea.setWrapStyleWord(true);
      
       JScrollPane scrollPane = new JScrollPane(wordsFoundArea);
       scrollPane.setPreferredSize(new Dimension(300, 150));
       scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
  
       contentPanel.add(statsArea);
       contentPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add spacing
       contentPanel.add(scrollPane);

       // Adding timer buttons (LARA)
       JPanel timerOptions = new JPanel (new FlowLayout(FlowLayout.CENTER,30,0));
       timerOptions.setBackground(new Color(255, 255, 255));
       timerOptions.add(thirtySeconds()); // 30 second timer button
       timerOptions.add(sixtySeconds()); // 60 second timer button
       timerOptions.add(ninetySeconds()); // 90 second timer button
       timerOptions.add(oneTwentySeconds()); // 120 second timer button
  
  
       // Adding all components to panel
       finalPanel.add(labelPanel, BorderLayout.NORTH);     
       finalPanel.add(contentPanel, BorderLayout.CENTER); 
       finalPanel.add(timerOptions, BorderLayout.SOUTH);
  
       // Store references to components we need to update
       finalPanel.putClientProperty("scoreLabel", scoreLabel);
       finalPanel.putClientProperty("statsArea", statsArea);
       finalPanel.putClientProperty("wordsFoundArea", wordsFoundArea);
  
       return finalPanel;
   }
  
   /**
    * Updates the final panel with the current score, stats, and words found.
    * This method is called when the game ends to reflect the final score and game statistics.
    * CONTRIBUTOR: Drae
    */
   private void updateFinalPanel() {
       // Update the score label with current score
       JLabel scoreLabel = (JLabel) finalPanel.getClientProperty("scoreLabel");
       scoreLabel.setText("SCORE: " + score);
  
       // Update other components
       Component[] components = finalPanel.getComponents();
       for (Component component : components) {
           if (component instanceof JPanel && ((JPanel) component).getLayout() instanceof BoxLayout) { // Check if the component is a JPanel and whether it uses a BoxLayout
               Component[] contentComponents = ((JPanel) component).getComponents(); // If it is, retrieve components inside JPanel
               for (Component contentComponent : contentComponents) {
                   if (contentComponent instanceof JTextArea) {
                       // Update stats area
                       ((JTextArea) contentComponent).setText("STATS:\n" + getFinalStatsText());
                   } else if (contentComponent instanceof JScrollPane) {
                       // Update words found area
                       JTextArea wordsFoundArea = (JTextArea) ((JScrollPane) contentComponent).getViewport().getView();
                       wordsFoundArea.setText("Words Found:\n" + String.join(", ", wordsFound));
                   }
               }
           }
       }
   }


  /**
   * Resets the game state, including the score, timer, word formation, and the grid.
   * CONTRIBUTOR: Drae (Main), Lara (Editor)
   */
   private void resetGame(int selectedTime) {
       // Clear game state
       score = 0;
       lbPoints.setText("SCORE: " + score);
       wordsFound.clear();
     
       // Reset timer
       countdownTime = selectedTime;
       timerLabel.setText("Time left: " + countdownTime + " seconds");
     
       // Clear word formation state
       word = "";
       lbWord.setText("");
       selectedCoordinates.clear();
       lastClickedCoordinate = null;
     
       // Generate and set new letters
       letterGrid = generateLetterArr();
     
       // Update button display
       for (int i = 0; i < 3; i++) {
           for (int j = 0; j < 3; j++) {
               buttons[i][j].setText(letterGrid[i][j]);
               buttons[i][j].setBackground(Color.WHITE);
           }
       }


       startTimer(selectedTime);
   }


   /**
    * Main method
    */
   public static void main(String[] args) {
       MainFrame myFrame = new MainFrame();
       myFrame.initialize();
   }
}

