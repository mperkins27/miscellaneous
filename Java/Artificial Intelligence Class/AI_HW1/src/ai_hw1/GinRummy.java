/*
 * Name       : Max Perkins
 * Last4      : 6175
 * Class      : A.I.
 * Assignment : HW 1
 * Description: Gin Rummy Algorithms
 *
 * Class GinRummy
 *
 */
package ai_hw1;
import java.lang.*;
import java.util.Random;

public class GinRummy {             
    
    // take an individual card and converts it to a string
    String indToString(int card) {
        String sCard = "";
        String suite = "";
        
        // Clubs
        if(card >= 0 && card <= 12) {
            card++;
            suite = "C";
        }
        // Diamonds
        else if(card >= 13 && card <= 25) {
            card -= 12;
            suite = "D";
        }
        // Hearts
        else if(card >= 26 && card <= 38){
            card -= 25;
            suite = "H";
        }
        // Spades
        else if(card >= 39 && card <= 51) {
            card -= 38;
            suite = "S";
        }     
        
        // Special cases for Ace, Jack, Queen and King
        if(card == 1)       sCard = "A" + suite;                            
        else if(card == 11) sCard = "J" + suite;
        else if(card == 12) sCard = "Q" + suite;
        else if(card == 13) sCard = "K" + suite;
        
        // Otherwise just tack on the number
        else                sCard = Integer.toString(card) + suite;        
        
        return sCard;
    }
    
    // converts the entire hand to a string
    String toString(int [] hand) {                                
        /*        
        Clubs           Diamonds
         0-9 : A-10     13-22: A-10
        10-12: J-K      23-25: J-K                                 
        
        Hearts          Spades
         26-35: A-10    39-48: A-10
         36-38: J-K     49-51: J-K
        */                        
        
        String sHand = "";
        if(hand.length < 11)    return "Not a full hand.\n";
        for(int i = 0; i < hand.length; i++) {            
            sHand += indToString(hand[i]);            
            if(i != hand.length-1) sHand  += " ";
        }
        return sHand;
    }        
    
    // chooses a card to let go    
    int discard(boolean [] knownCards, int [] hand) {
        
        // first  array: the sum with the card taken out
        // second array: card to be discarded
        int [][] calcVals = new int[11][2];
        int toDiscard = 0;
        
        // run calculate taking turns discarding each of
        // the 11 cards. find the sum of each run
        for(int i = 0; i < 11; i++) {
            for(int j = 0; j < 2; j++) {                
                int [] dHand = takeOneOut(hand, i);                
                int sum = calculate(dHand);
                
                calcVals[i][0] = sum;
                calcVals[i][1] = hand[i];
            }
        }
        
        // find the minimum sum of the discards
        int mini = 0;
        for(int i = 1; i < 11; i++) {
            if(calcVals[i][0] < mini)
                mini = i;
        }
        // the minimum sum
        int minSum     = calcVals[mini][0];
        // the card associated with the
        // minimum sum
        int minSumDisc = calcVals[mini][1];
        
        // if the minimum sum is 10 or less, discard here
        if(minSum <= 10)
            return minSumDisc;
       
        // Otherwise attempt to hold cards to complete sets of 3 or 4 numbers
        // or runs of at least 3 cards of the same suit
                      
        // numbers associated with each card
        int    [] numHand   = new int[11];        
        // string representation of each card
       // String [] strHand   = new String[11];     
        // suit of each card
        String [] suiteHand  = new String[11];
        // 11x2 array of each card in the hand and a value
        // of 0 or 1 which tells whether to hold it or not
        int [][] holdHand = new int[11][2];
        
        // fill in the above arrays using the given hand
        for(int i = 0; i < 11; i++) {
            String strCard = this.indToString(hand[i]);
            String number = strCard.substring(0, strCard.length()-2);                        
            
            if     (number == "A")  numHand[i] = 1;                      
            else if(number == "J")  numHand[i] = 11;                      
            else if(number == "Q")  numHand[i] = 12;                       
            else if(number == "K")  numHand[i] = 13;
            else if(number == "2" || number == "3" || number == "4" || number == "5" ||
                    number == "6" || number == "7" || number == "8" || number == "9" ||
                    number == "10") numHand[i] = Integer.valueOf(number);                           
            else                    continue;
            
            //strHand [i] = strCard;
            suiteHand[i] = strCard.substring(strCard.length()-1);
                        
            // copy the hand to the array            
            holdHand[i][0] = hand[i];
            // discard by default
            holdHand[i][1] = 0;                    
        }
                                
        // count how many sets of the same number there are and
        // of the same suite
        // 0 - discard, 1 - num keep, 2 - suite keep, 3 - both
        for(int i = 0; i < numHand.length; i++) {
            int num = numHand[i];
            String suite = suiteHand[i];
            
            int numFreq = countNumFreq(numHand, num);
            int suiteFreq = countSuiteFreq(suiteHand, suite);
            
            if(numFreq >= 2 && suiteFreq >= 2)                
                holdHand[i][1] = 3;
            else if(numFreq >= 2)
                holdHand[i][1] = 1;
            else if(suiteFreq >= 2)
                holdHand[i][1] = 2;
        }
        
        // prune this list of cards to keep based on the knownCards array
        for(int i = 0; i < 11; i++) {
            if(holdHand[i][1] == 0 || holdHand[i][1] == 3) continue;                
            else if(holdHand[i][1] == 1)
                holdHand[i][1] = isNumKeepable  (knownCards, hand[i]) ? 1 : 0;            
            else if(holdHand[i][1] == 2)
                holdHand[i][1] = isSuiteKeepable(knownCards, hand[i]) ? 2 : 0;            
        }
        
        // discard a card that isn't worth keeping
        boolean discarded = false;
        for(int i = 0; i < holdHand.length; i++) {
            if(holdHand[i][1] == 0) {
                toDiscard = holdHand[i][0];
                discarded = true;
            }                
        }
        
        // if all are worth discarding, then just discard randomly
        // not a good idea if a good potential run is in hand
        if(!discarded) {
            int index = new Random().nextInt(11);            
            while(numHand[index] >= 10)
                index = new Random().nextInt(11);            
            toDiscard = hand[index];
        }
            
        
        return toDiscard;
    }        
    
    // checks to see if a card is keepable based on its number
    // that is, since it's been marked keepable with a set of at least
    // 2, then having at least one more number available makes it keepable
    private boolean isNumKeepable(boolean [] knownCards, int card) {
        int [] other3 = new int[3];
        
        other3[0] = 0;
        other3[1] = 0;
        other3[2] = 0;
        
        // Clubs
        if(card >= 0 && card <= 12) {
           other3[0] = card + 13;
           other3[1] = card + 26;
           other3[2] = card + 39;
        }
        // Diamonds
        else if(card >= 13 && card <= 25) {
           other3[0] = card - 13;
           other3[1] = card + 26;
           other3[2] = card + 13;
        }
        // Hearts
        else if(card >= 26 && card <= 38){
           other3[0] = card - 13;
           other3[1] = card - 26;
           other3[2] = card + 13;
        }
        // Spades
        else if(card >= 39 && card <= 51) {
           other3[0] = card - 13;
           other3[1] = card - 26;
           other3[2] = card - 39;
        }     

        if(knownCards[other3[0]] || knownCards[other3[1]] || knownCards[other3[2]])
            return true;
        return false;
    }
    
    // checks to see if a card is keepable based on its suite
    // since it was marked keepable based on having 2 of the same suite,
    // it just checks to see if one more is available in its suite
    // if it is, it's keepable
    private boolean isSuiteKeepable(boolean [] knownCards, int card) {
        int freq = 0;
        
        if(card >= 0 && card <= 12) {
            for(int i = 0; i <= 12; i++) {
                if(knownCards[i])
                    freq++;
            }
        }
        // Diamonds
        else if(card >= 13 && card <= 25) {
            for(int i = 13; i <= 25; i++) {
                if(knownCards[i])
                    freq++;
            }
        }
        // Hearts
        else if(card >= 26 && card <= 38){
            for(int i = 26; i <= 38; i++) {
                if(knownCards[i])
                    freq++;
            }
        }
        // Spades
        else if(card >= 39 && card <= 51) {
            for(int i = 39; i <= 51; i++) {
                if(knownCards[i])
                    freq++;
            }
        }         
        if(freq >= 1)
            return true;
        return false;
    }
    
    // counts how many of the suite are in the current hand
    private int countSuiteFreq(String [] array, String suite) {
        int freq = 0;
        for(int i = 0; i < array.length; i++) {
            if(array[i] == suite)
                freq++;
        }
        return freq;        
    }
    
    // counts how many of the same number are in the current hand
    private int countNumFreq(int [] array, int num) {
        int freq = 0;
        for(int i = 0; i < array.length; i++) {
            if(array[i] == num)
                freq++;
        }
        return freq;
    }
    
    // return the hand with one of the cards taken out
    // input is full hand of 11 and the card to take out
    // returns the hand of 10
    private int [] takeOneOut(int [] hand, int ind) {
        int [] dHand = new int[10];
        int j = 0;
        
        for(int i = 0; i < ind; i++, j++)
            dHand[j] = hand[i]; 
        
        for(int i = ind+1; i < 11; i++, j++)
            dHand[j] = hand[i];
        
        return dHand;
    }    
    
    // Unimplemented because I have an odd last digit
    int calculate(int [] hand) {
        return 0;
    }
    
    // checks to see if a card exists in the hand already
    // exists to check for duplicates in the main function
    public  boolean isDuplicated(int [] hand, int num) {
        boolean dup = false;
        for(int i = 0; i < hand.length; i++) {
            if(hand[i] == num) {
                dup = true;
                break;
            }
        }
        return dup;
    }
}
