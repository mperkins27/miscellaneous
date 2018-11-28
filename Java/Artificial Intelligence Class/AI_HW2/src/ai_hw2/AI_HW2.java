/* 
 */
package ai_hw2;

import java.io.*;

public class AI_HW2 {
    public static void main(String[] args) {
        Achoo game = new Achoo();
        
        boolean done = false;
        String player = "Blue";
        
        System.out.println("Game of Achoo");
        System.out.println("BLUE - 1st Player, RED - 2nd Player");
        
        while(!done) {
            game.printBoard();                        
            
            System.out.println(player + ", enter move (Q to quit): ");
                        
            String s = "";
            boolean valid = false;
            do { 
                try {  BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                        s = bufferRead.readLine(); 
                        if(s == "Q" || s == "q") {
                            System.out.println("\n\nThank you, play again.");
                            return;   
                        }                            
                } catch(IOException e) {};
                
                valid = game.processMove(s);
                if(!valid)
                    System.out.println("Invalid move." + player + ", enter move (Q to quit): ");
                else
                    valid = true;
            } while(!valid);  
            if(player == "Blue")
                player = "Red";
            else
                player = "Blue";
        }        
    }    
}
