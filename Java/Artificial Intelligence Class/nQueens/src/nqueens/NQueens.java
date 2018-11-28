/* Max Perkins
 * Artificial Intelligence In Class Assignment
 * 
 * n Queens
 * 
 * 1/27/2014
 */
package nqueens;

public class NQueens {

    /**
     * Put n queens on an n × n board with no 
     * two queens on the same row, column, or
     * diagonal
     * 
     *  0  1  2  3
     *  4  5  6  7
     *  8  9 10 11
     * 12 13 14 15
     */
    
    static boolean [][] board;
    static int queensPlaced;
    static int tries;
    
    public static void main(String[] args) {                       
        queensPlaced = tries = 0;
        
        // Increasing the number of tries changes the starting
        // position of the first Queen. So reset the board each
        // time and attempt to place 4 queens with each new
        // placement
        while(queensPlaced < 4 && tries < 4) {
            initBoard();
            placeQueens();        
            tries++;
        }
        printBoard();               
    }
    
    // put everything empty (false)
    public static void initBoard() {
        board = new boolean[4][4];        
        for(int i  = 0; i < 4; i++)
            for(int j = 0; j < 4; j++)
                board[i][j] = false;        
    }
    
    // Queens for true positions, empty (X) for false
    public static void printBoard() {
        for(int i  = 0; i < 4; i++) {            
            for(int j = 0; j < 4; j++) {
                if(board[i][j] == true)
                    System.out.print("Q ");
                else
                    System.out.print("X ");
            }
            System.out.print("\n");
        }
    }
    
    // See if there's a Queen in the row at i,j
    public static boolean checkRow(int i, int j) {
        for(int a = 0; a < 4; a++)
            if(board[i][a])
                 return false;
        return true;
    }
    
    // See if there's a Queen in the column at i,j
    public static boolean checkColumn(int i, int j) {
        for(int a = 0; a < 4; a++)
            if(board[a][j])
                 return false;
        return true;
    }
    
    // See if there's a Queen in the diagonals of i,j
    public static boolean checkDiag(int i, int j) {                
        /*        
        00 01 02 03
        10 11 12 13
        20 21 22 23
        30 31 32 33        
        */        
        if(i == j)
            return !(board[0][0] || board[1][1] || board[2][2] || board[3][3]);
        
        int saveI = i;
        int saveJ = j;
        if(j == 0) {
            while(i >= 0 && j <= 3)          
                if(board[i--][j++])
                    return false;
            i = saveI;
            j = saveJ;
            while(i <= 3 && j <= 3)
                if(board[i++][j++])                
                    return false;            
        }
        else if(i == 0) {
            while(j >= 0 && i <= 3)
                if(board[i++][j--])
                    return false;            
            i = saveI;
            j = saveJ;
            while(i <= 3 && j <= 3)
                if(board[i++][j++])
                    return false;            
        }
        else if(i == 3){
            while(j >= 0 && i >= 0)
                if(board[i--][j--])
                    return false;            
            i = saveI;
            j = saveJ;
            while(i >= 0 && j <= 3)
                if(board[i--][j++])
                    return false;            
        }
        else if(j == 3) {
            while(i >= 0 && j >= 0)
                if(board[i--][j--])
                    return false;
            i = saveI;
            j = saveJ;
            while(i <= 3 && j >= 0)
                if(board[i++][j--])
                    return false;            
        }
        else if(i == 1 && j == 2)
            return !(board[3][0] || board[1][2] || board[2][1] || board[0][3] || board[2][3] || board[0][1]);
        else if(i == 2 && j == 1)
            return !(board[3][0] || board[1][2] || board[2][1] || board[0][3] || board[3][2] || board[1][0]);
        return true;
    }
    
    // Place queens, checking the rows, columns and diagonals before placing
    public static void placeQueens() {       
        int i,j;
                        
        for(i = 0 ; i < 4; i++) {                                    
            for(j = (i == 0) ? tries: 0 ; j < 4; j++)                
                if(checkRow(i,j) && checkColumn(i,j) && checkDiag(i,j)){
                    board[i][j] = true;
                    queensPlaced++;                    
                } 
        }
    }
}











