/*
 * Name       : Max Perkins
 * Last4      : 6175
 * Class      : A.I.
 * Assignment : HW 1
 * Description: Gin Rummy Algorithms
 *
 * Class AI_HW1 -- the main class
 *
 */
package ai_hw1;
import java.util.Random;

public class AI_HW1 {
    public static void main(String[] args) {
        GinRummy game = new GinRummy();
        Random rndGen = new Random();
                
        int []   hand;
        int [][] hands = new int[20][11];    
        int i, j, k;
        
        boolean [][] knownCards = new boolean[20][52];       
        
        // initialize knownCards
        for(i = 0; i < 20; i++)
            for(j = 0; j < 52; j++)
                knownCards[i][j] = false;
        
        // create 20 runs with random cards
        // and ensure the impossibility of 2
        // of the same cards doesn't happen
        for(k = 0; k < 20; k++) {
            hand  = new int[11];
            for (i = 0; i < 11; i++){        
                int num = rndGen.nextInt(52);
                while(game.isDuplicated(hand, num))
                    num = rndGen.nextInt(52);
                hand[i] = num;                
                knownCards[k][num] = true;
            }                    
            hands[k] = hand;            
        }
        
        // print the hands
        for(k = 0; k < 20; k++) {
            System.out.println(game.toString(hands[k]));
            
            int discardCard = game.discard(knownCards[k], hands[k]);
            String strCard = game.indToString(discardCard);
            
            System.out.println("Discarding: " + strCard + "\n");
        }            
    }            
}
