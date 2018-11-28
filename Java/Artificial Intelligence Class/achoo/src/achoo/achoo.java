/*
 * Name       : Max Perkins
 * Last4      : 6175
 * Class      : A.I.
 * Assignment : HW 2
 * Description: Game of Achoo with A.I. - uses minimax tree search
 *               with alpha and beta pruning
 *
 * Class achoo
 */
package achoo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class achoo extends JFrame
{
    // Window title and dimensions
    private static final String TITLE = "Achoo";
    private static final int WIDTH=700;
    private static final int HEIGHT=400;

    // GUI elements
    private Container content;
    private JLabel result;
    private JButton[] cells;
    private JButton exitButton;
    private JButton initButton;
    private CellButtonHandler[] cellHandlers;
    private ExitButtonHandler exitHandler;
    private InitButtonHandler initHandler;

    private boolean blues;
    private boolean gameOver;

    // Player colors
    private Color you = Color.blue;
    private Color AI     = Color.red;
    private Color empty = Color.green;
    
    // Tokens
    private int youTokens = 0;
    private int AITokens = 0;
    
    // Number of slides
    private int slides = 0;
    private int youClicks = 0;
    private int fromPos = 0;        
    
    public achoo() {
        //Necessary initialization code
        setTitle(TITLE);
        setSize(WIDTH, HEIGHT);
        this.setLocationByPlatform(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        //Get content pane
        content = getContentPane();
        content.setBackground(Color.blue.darker());

        //Set layout
        content.setLayout(new GridLayout(4,7));                

        //Create cells and handlers
        cells = new JButton[10];
        cellHandlers = new CellButtonHandler[10];
        for(int i = 0; i < 10; i++) {			
            cells[i] = new JButton("");
            cells[i].setFont(new Font("Serif", Font.BOLD, 32));
            cells[i].setBackground(Color.green);
            cellHandlers[i] = new CellButtonHandler();
            cells[i].addActionListener(cellHandlers[i]);
        }

        //Create init and exit buttons and handlers
        exitButton = new JButton("Exit");
        exitHandler = new ExitButtonHandler();
        exitButton.addActionListener(exitHandler);
        initButton = new JButton("Clear");
        initHandler = new InitButtonHandler();
        initButton.addActionListener(initHandler);

        //Create result label
        result = new JLabel("Blue's Turn", SwingConstants.CENTER);        

        // Add elements to the grid content pane	
        // Below is the arrangement. cells has the
        // board elements, and the empty JButtons are just spacing
        /*                
         0  1  2  3  4  5  6
         7  8  9 10 11 12 13
        14 15 16 17 18 19 20
        21 22 23 24 25 26 27                                
        */                                                
        content.add(new JButton("  "), 0);
        content.add(cells[0], 1);
        content.add(new JButton("  "), 2);
        content.add(cells[1], 3);
        content.add(new JButton("  "), 4);
        content.add(cells[2], 5);
        content.add(new JButton("  "), 6);                                

        content.add(cells[3], 7);
        content.add(new JButton("  "), 8);                
        content.add(cells[4], 9);
        content.add(new JButton("  "), 10);                
        content.add(cells[5], 11);        
        content.add(new JButton("  "), 12);
        content.add(cells[6], 13);
        content.add(new JButton("  "), 14);

        content.add(cells[7], 15);
        content.add(new JButton("  "), 16);
        content.add(cells[8], 17);
        content.add(new JButton("  "), 18);
        content.add(cells[9], 19);
        content.add(new JButton("  "), 20);                

        content.add(initButton, 21);
        content.add(new JButton("  "), 22);        
        content.add(new JButton("  "), 23);
        content.add(result, 24);
        content.add(new JButton("  "), 25);
        content.add(new JButton("  "), 26);                
        content.add(exitButton, 27);                

        content.getComponent(22).setBackground(Color.BLACK);
        content.getComponent(23).setBackground(Color.BLACK);
        content.getComponent(25).setBackground(Color.BLACK);
        content.getComponent(26).setBackground(Color.BLACK);
        
                
        content.setBackground(Color.DARK_GRAY);                
        content.setForeground(Color.DARK_GRAY);                
        result.setForeground(Color.WHITE);
        
        exitButton.setBackground(Color.DARK_GRAY);        
        exitButton.setForeground(Color.WHITE);
        
        initButton.setBackground(Color.DARK_GRAY);        
        initButton.setForeground(Color.WHITE);
                
        init();                        
    }

    // Initialize board - player 1 is you, player 2 is AI
    private void init() {
        blues = true;                
        result.setText("Blue's Turn");                    
        gameOver = false;

        AITokens = 0;
        youTokens = 0;
        slides = 0;
        youClicks = 0;
        fromPos = 0;                
        
        //Initialize text in buttons and set color
        for(int i = 0; i < 10; i++) {                    
            cells[i].setText("" + i);
            cells[i].setForeground(empty);
            cells[i].setBackground(empty);
        }
        setVisible(true);
    }

    /* Make a move Numbers are used to keep track of cell position
     * The colors are for player visual. So, set the foreground (text)
     * and background of the cell to keep numbers hidden.
     ^
     * Cells:    
         0 1 2
        3 4 5 6
         7 8 9        
     *
    */
    private void makeMove(int pos, Color player) {
        cells[pos].setForeground(player);
        cells[pos].setBackground(player);
    }
    
    // Find horizontal sequences of 3 to see if anyone won
    public boolean checkWinner() {        
        if(cells[0].getBackground() == cells[1].getBackground() && cells[0].getBackground() == cells[2].getBackground()
           && (cells[0].getBackground() == Color.red || cells[0].getBackground() == Color.blue))
                return true;
        else if(cells[3].getBackground() == cells[4].getBackground() && cells[3].getBackground() == cells[5].getBackground()
           && (cells[3].getBackground() == Color.red || cells[3].getBackground() == Color.blue))
                return true;                
        else if(cells[4].getBackground() == cells[5].getBackground() && cells[4].getBackground() == cells[6].getBackground()
           && (cells[4].getBackground() == Color.red || cells[4].getBackground() == Color.blue))
                return true;                
        else if(cells[7].getBackground() == cells[8].getBackground() && cells[7].getBackground() == cells[9].getBackground()
           && (cells[7].getBackground() == Color.red || cells[7].getBackground() == Color.blue))
                return true;                
        else
            return false;
    }

    // Start the achoo game
    public static void main(String[] args) {		
            achoo gui = new achoo();		
    }

    // This is invoked when Player 1 (YOU) click a button
    private class CellButtonHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e) {
            //If game over, ignore
            if(gameOver)
                    return;            
            
            //Get button pressed
            JButton pressed = (JButton)(e.getSource());			
            Color color = pressed.getBackground();                        
            int pos = Integer.valueOf(pressed.getText());        
            
            //If repeat button hit (blues or reds) ignore
            if(color == AI || (color == you && youTokens < 4))
                    return;

            // Else, make it red or blue
            if(blues) {
                // Make a move at the current click if we're not sliding
                // tokens yet
                if(youTokens <= 3) {
                    makeMove(pos, you);
                    youTokens++;
                }            
                // Time to start sliding tokens
                // First click is starting position
                // Next click is destination.
                // Validity checking is within
                else {                                        
                    // First click
                    if(youClicks == 0 && color == you) {
                        fromPos = pos;
                        youClicks++;
                        return;
                    }
                    // Second click
                    // Make sure it's an empty cell                    
                    else if(youClicks > 0 && color == empty) {                        
                        java.util.List<Integer> proxim = checkProximity(pos, you);
                        
                        boolean isValid = false;
                        
                        // If it's empty, it's valid only if you have a neighbor
                        for(int i = 0; i < proxim.size(); i++)
                            if(proxim.get(i) == fromPos) {
                                isValid = true;
                                youClicks = 0;   
                                
                                makeMove(fromPos, empty);
                                makeMove(pos, you);
                                slides++;
                            }                  
                        
                        // Let's you know it's invalid
                        // Reset clicks
                        if(!isValid) {
                            youClicks = 0;
                            result.setText("Invalid");                                                                                    
                            return;
                        }
                    }
                    else
                        return;
                }                                                
            }         
            //Check winner
            if(checkWinner()) {
                //End of game
                gameOver = true;

                //Display winner message
                if(blues)
                    result.setText("BLUE wins!!");
                else
                    result.setText("RED wins!!");
            }
            // All tokens are used and all slides made = DRAW
            else if(AITokens > 3 && youTokens > 3 && slides > 3) {
                result.setText("DRAW!!");
            }
            // NO WINNER - have AI make a move
            else {
                //Change player
                blues = !blues;

                //Display player message
                if(blues)
                    result.setText("Blue's Turn");
                else
                    result.setText("Red's Turn");
                
                AIturn();                
            }
        }
    }

    // Follows same format for Player 1's button click
    // Except the move is made with minimax with alpha/beta pruning
    public void AIturn() {    
        
        // Calls move, which is the tree search        
        int theMove = move()[1];
                        
        // Use up tokens first
        if(AITokens <= 3) {                    
            makeMove(theMove, AI);
            AITokens++;
        }     
        // Now start sliding
        else {
            java.util.List<Integer> proxim = checkProximity(theMove, AI);                                                  
            
            // If we're sliding, it makes the same best move determination
            // Difference is that it takes what's closeby of it's pieces,
            // checks to see if sliding it will win, and makes that move.
            // If it doesn't make a winning move, it just takens the last
            // neighbor checked and slides it
            for(int i = 0; i < proxim.size(); i++) { 
                int slide = proxim.get(i);

                // Slide first neighbor
                makeMove(theMove, AI);                                                                               
                makeMove(slide, empty);
                slides++;
                
                // Check to see if sliding won OR
                // Slide last neigbbor
                if(checkWinner() || i == proxim.size()-1)
                    break;                                        
                // Sliding didn't produce a winner, so check next piece to
                // slide
                else {
                    slides--;
                    makeMove(theMove, empty);
                    makeMove(slide, AI);
                }
            }
        }                
        //Check winner
        if(checkWinner()) {
            //End of game
            gameOver = true;

            //Display winner message
            if(blues)
                result.setText("BLUE wins!!");
            else
                result.setText("RED wins!!");
        }
        else if(AITokens > 3 && youTokens > 3 && slides > 3) {
            result.setText("DRAW!!");
        }
        else {
            //Change player
            blues = !blues;

            //Display player message
            if(blues)
                result.setText("Blue's Turn");
            else
                result.setText("Red's Turn");
        }        
    }
    
    // Make a move - calls minimax
    int [] move() {
      int[] res = minimax(2, AI, Integer.MIN_VALUE, Integer.MAX_VALUE);
          
      return new int[] {res[0], res[1]};   // score, pos
   }
   
    // Minimax (recursive) at level of depth for maximizing or minimizing player
    // with alpha-beta cut-off.         
   private int[] minimax(int depth, Color player, int alpha, int beta) {      
      java.util.List<Integer> nextMoves = generateMoves();
       
      int score;
      int bestPos = -1;      
 
      if (nextMoves.isEmpty() || depth == 0) {
         // Gameover or depth reached, evaluate score
         score = evaluate();         
         return new int[] {score, bestPos};
      } 
      else {
        for (int move : nextMoves) {
              makeMove(move, player);              
              
              if (player == you) {
                 score = minimax(depth - 1, AI, alpha, beta)[0];
                 if (score > alpha) {
                    alpha = score;
                    bestPos = move;
                 }
              }               
              else {                
                 score = minimax(depth - 1, you, alpha, beta)[0];
                 if (score < beta) {
                    beta = score;
                    bestPos = move;
                 }
              }
              // undo move
              makeMove(move, empty);              
              // cut-off
              if (alpha >= beta) break;
           }            
        
          return new int[] {(player == you) ? alpha : beta, bestPos};
       }
   }
 
   // Find all valid next moves.
   // Return list of moves or empty list if gameover */
   private java.util.List<Integer> generateMoves() {
      java.util.List<Integer> nextMoves = new ArrayList<>(); // allocate List
 
      // If gameover, i.e., no next move
      if (checkWinner()) {
         return nextMoves;   // return empty list
      }
 
      // Search for empty cells and add to the List
      for (int i = 0; i < 10; i++) {
        if (cells[i].getBackground() == empty) {            
            // If it's sliding time, only return moves to AI that
            // have empty cells of AI neighbors
            if(AITokens >= 4 && checkProximity(i, AI).isEmpty())
                    continue;
            nextMoves.add(i);
        }
      }
      return nextMoves;
   }    
   
   // Checks position for neighbors and returns the position of the neighbors,
   // if any
   private java.util.List<Integer> checkProximity(int pos, Color player) {
      //  0 1 2
      // 3 4 5 6
      //  7 8 9
       
       java.util.List<Integer> proxim = new ArrayList<>();
       
       // Go position by position.... 
       if(pos == 0) {
           if(cells[1].getBackground() == player)
               proxim.add(1);
           if(cells[3].getBackground() == player)
               proxim.add(3);
           if(cells[4].getBackground() == player)
               proxim.add(4);                                 
       }
       else if(pos == 1) {
           if(cells[0].getBackground() == player)
               proxim.add(0);
           if(cells[4].getBackground() == player)
               proxim.add(4);
           if(cells[5].getBackground() == player)               
               proxim.add(5);                
           if(cells[2].getBackground() == player)
               proxim.add(2);                                 
       }
       else if(pos == 2) {
           if(cells[1].getBackground() == player)
               proxim.add(1);
           if(cells[5].getBackground() == player)
               proxim.add(5);
           if(cells[6].getBackground() == player)
               proxim.add(6);                                 
       }
       else if(pos == 3) {
           if(cells[0].getBackground() == player)
               proxim.add(0);
           if(cells[4].getBackground() == player)
               proxim.add(4);
           if(cells[7].getBackground() == player)
               proxim.add(7);                                 
       }
       else if(pos == 4) {
           if(cells[0].getBackground() == player)
               proxim.add(0);
           if(cells[3].getBackground() == player)
               proxim.add(3);
           if(cells[7].getBackground() == player)
               proxim.add(7);                                 
           if(cells[1].getBackground() == player)
               proxim.add(1);
           if(cells[5].getBackground() == player)
               proxim.add(5);
           if(cells[8].getBackground() == player)
               proxim.add(8);                    
       }
       else if(pos == 5) {
           if(cells[1].getBackground() == player)
               proxim.add(1);
           if(cells[4].getBackground() == player)
               proxim.add(4);
           if(cells[8].getBackground() == player)
               proxim.add(8);                                 
           if(cells[2].getBackground() == player)
               proxim.add(2);
           if(cells[6].getBackground() == player)
               proxim.add(6);
           if(cells[9].getBackground() == player)
               proxim.add(9);                                     
       }
       else if(pos == 6) {
           if(cells[2].getBackground() == player)
               proxim.add(2);
           if(cells[5].getBackground() == player)
               proxim.add(5);
           if(cells[9].getBackground() == player)
               proxim.add(9);                                 
       }
       else if(pos == 7) {
           if(cells[8].getBackground() == player)
               proxim.add(8);
           if(cells[3].getBackground() == player)
               proxim.add(3);
           if(cells[4].getBackground() == player)
               proxim.add(4);                                 
       }
       else if(pos == 8) {
           if(cells[9].getBackground() == player)
               proxim.add(9);
           if(cells[5].getBackground() == player)
               proxim.add(5);
           if(cells[4].getBackground() == player)
               proxim.add(4);                                 
           if(cells[7].getBackground() == player)
               proxim.add(7);                  
       }       
       else if(pos == 9) {
           if(cells[6].getBackground() == player)
               proxim.add(6);
           if(cells[5].getBackground() == player)
               proxim.add(5);
           if(cells[8].getBackground() == player)
               proxim.add(8);                                             
       }       
       
       return proxim;
   }
   
   /* 
       The heuristic evaluation function for the current board
       Return +100, +10, +1 for EACH 3-, 2-, 1-in-a-line for computer.
              -100, -10, -1 for EACH 3-, 2-, 1-in-a-line for opponent.
               0 otherwise 
   */
   private int evaluate() {
      int score = 0;
      // Evaluate score for each of the 8 lines (3 rows, 3 columns, 2 diagonals)
      score += evaluateLine(0,1,2);
      score += evaluateLine(3,4,5);
      score += evaluateLine(4,5,6);
      score += evaluateLine(7,8,9);

      return score;
   }
 
   /*
     The heuristic evaluation function for the given line of 3 cells
     Return +100, +10, +1 for 3-, 2-, 1-in-a-line for computer.
            -100, -10, -1 for 3-, 2-, 1-in-a-line for opponent.
            0 otherwise
   */
   private int evaluateLine(int pos1, int pos2, int pos3) {
      int score = 0;
 
      // First cell
      if (cells[pos1].getBackground() == you) {
         score = 1;
      } else if (cells[pos1].getBackground() == AI) {
         score = -1;
      }
 
      // Second cell
      if (cells[pos2].getBackground() == you) {
         if (score == 1) {   // cell1 is mySeed
            score = 10;
         } else if (score == -1) {  // cell1 is oppSeed
            return 0;
         } else {  // cell1 is empty
            score = 1;
         }
      } else if (cells[pos2].getBackground() == AI) {
         if (score == -1) { // cell1 is oppSeed
            score = -10;
         } else if (score == 1) { // cell1 is mySeed
            return 0;
         } else {  // cell1 is empty
            score = -1;
         }
      }
 
      // Third cell
      if (cells[pos3].getBackground() == you) {
         if (score > 0) {  // cell1 and/or cell2 is mySeed
            score *= 10;
         } else if (score < 0) {  // cell1 and/or cell2 is oppSeed
            return 0;
         } else {  // cell1 and cell2 are empty
            score = 1;
         }
      } else if (cells[pos3].getBackground() == AI) {
         if (score < 0) {  // cell1 and/or cell2 is oppSeed
            score *= 10;
         } else if (score > 1) {  // cell1 and/or cell2 is mySeed
            return 0;
         } else {  // cell1 and cell2 are empty
            score = -1;
         }
      }
      return score;
   } 
   
   // Handles exit button
    private class ExitButtonHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
                System.exit(0);
        }
    }

    // Handles the clear button
    private class InitButtonHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
                init();
        }
    }
}