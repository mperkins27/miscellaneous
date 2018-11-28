/*
 * 
 * Name         : Max Perkins
 * Course       : CSCI 5330
 * Instructor   : Dr. Li
 * Date Created : Jan 19, 2013
 * 
 * Description  : Implement the following sorting algorithms: bubble, insertion, selection and
 *                quick sort. The input should be integer array of various sizes.

    Requirements:
     1) Your program computes the time (in terms of microseconds) of each algorithm spent on sorting
           for each input array
     2) Make an empirical conclusion on which algorithm is best based on various array size
           (large enough), you need to have a specific set of array sizes from small ones to large ones
     3) Make proper documentations
     4) Do not directly copy programs from other sources, I will check on this. Given credit if you
           reference any source code which is not written by you
     
     You present your program in class, and it will be graded in real time.
 * 
 */
;

/**
 *
 * @author Max
 * 
 * Class Homework1 
 * Purpose: Performs insertion, bubble, selection, and quick sorts on various array sizes
 *          And computes how long it takes in microseconds
 * 
 * Constructors: None
 * 
 * Methods: Contains the main method (public) and sorting functions (private)   
 * 
 */
public class Homework1 {
    
    private static long lngArray [];
    private static long lngArray1 [];
    private static long lngArray2 [];
    private static long lngArray3 [];
    private static long lngArray4 [];   
    
    public static void main(String[] args) {
        
        // format output
        System.out.println("      Size\t    Bubble(μs)\t Insertion(μs)\t Selection(μs)\t    Quick(μs)");               
        
        // loop for different array sizes
        for(int arraySize = 10, num = 100; arraySize < 110000; ) {                                              
            
            // create 4 of the same array - one for each sort
            lngArray1 = new long[arraySize];
            lngArray2 = new long[arraySize];
            lngArray3 = new long[arraySize];
            lngArray4 = new long[arraySize];

            // generate random numbers and copy to each of the arrays
            long value;
            for (int i = 0 ; i < arraySize ; i++){
                value = (long) (Math.random () * 1000);
                lngArray1[i] = value;
                lngArray2[i] = value;
                lngArray3[i] = value;
                lngArray4[i] = value;
            }
            
            long bubStart, bubTime;
            long insStart, insTime;
            long selStart, selTime;
            long quiStart, quickTime;

            // set the current array and compute nanosecond time for
            // each of the sorts duration
            lngArray = lngArray1;
            bubStart = System.nanoTime();
            bubbleSort();
            bubTime = System.nanoTime() - bubStart;

            lngArray = lngArray2;
            insStart = System.nanoTime();               
            insertionSort();
            insTime = System.nanoTime() - insStart;

            lngArray = lngArray3;
            selStart = System.nanoTime();        
            selectionSort();
            selTime = System.nanoTime() - selStart;

            lngArray = lngArray4;
            quiStart = System.nanoTime();        
            quickSort();        
            quickTime = System.nanoTime() - quiStart;

            // convert nanoseconds to microseconds
            bubTime /= 1000;
            insTime /= 1000;
            selTime /= 1000;
            quickTime /= 1000;            
            
            // produce formatted output in a nice table
            System.out.format("%10d", arraySize);
            System.out.print("\t");
            System.out.format("%10d", bubTime);
            System.out.print("\t");
            System.out.format("%10d", insTime);
            System.out.print("\t");
            System.out.format("%10d", selTime);
            System.out.print("\t");
            System.out.format("%10d", quickTime);
            
            // find the quickest sort for the current array size
            String strSmallest = "Bubble";
            long smallest = bubTime;    
            
            if(smallest > insTime) {
                strSmallest = "Insertion";
                smallest = insTime;
            }
            
            if(smallest > selTime) {
                strSmallest = "Selection";
                smallest = selTime;
            }
            
            if(smallest > quickTime) {
                strSmallest = "Quick";
                smallest = quickTime;
            }
            
            System.out.println("\t---\t" + strSmallest + " Sort is quickest");
            
            // alter increment based on array size
            if(arraySize > 50000)
                arraySize *= 2;
            else if(arraySize > 10000 && arraySize < 50000)
                arraySize += num*200;
            else if(arraySize > 1000 && arraySize < 10000)
                arraySize += num*20;
            else
                arraySize += num;
            
        }        
        System.out.println("\t\t\t\t\t\t\t\t\t\t" + "COMPLETE");
    }    
        
    /*
     * Method swap(int index1, int index2)
     * 
     * Purpose: Swaps two array values
     * Parameters: index1 and index2
     * Return Value: None
     * 
     */
    private static void swap(int index1, int index2) {
        long temp = lngArray[index1];
        lngArray[index1] = lngArray[index2];
        lngArray[index2] = temp;
    }

    /*
     * Method selectionSort()
     * 
     * Purpose: Performs selection sort on the array
     * Parameters: None
     * Return Value: None
     * 
     */    
    private static void selectionSort() {
        int i, j, minIndex;
        long temp;
        
        for (i = 0; i < lngArray.length - 1; i++) {
            minIndex = i;
            for (j = i + 1; j < lngArray.length; j++)
                if (lngArray[j] < lngArray[minIndex])
                    minIndex = j;
                if (minIndex != i)
                    swap(i, minIndex);
        }        
    }

    /*
     * Method quickSort()
     * 
     * Purpose: Performs quick sort on the array
     * Parameters: None
     * Return Value: None
     * 
     */        
    private static void quickSort() {
	quickSort(0, lngArray.length - 1);
    }
    
    /*
     * Method quickSort(int start, int end)
     * 
     * Purpose: Helper function for quickSort() - performs quick sort on the array
     * Parameters: start and end 
     * Return Value: None
     * 
     */        
    private static void quickSort(int start, int end) {
        int i = start;
        int k = end;

        if (end - start >= 1) {
            long pivot = lngArray[start];

            while (k > i) {
                    while (lngArray[i] <= pivot && i <= end && k > i)
                            i++;
                    while (lngArray[k] > pivot && k >= start && k >= i)
                        k--;
                    if (k > i)
                            swap(i, k);
            }
            swap(start, k);

            quickSort(start, k - 1);
            quickSort(k + 1, end);
        }
    }
       
    /*
     * Method bubbleSort()
     * 
     * Purpose: Performs quick sort on the array
     * Parameters: None
     * Return Value: None
     * 
     */        
    private static void bubbleSort() {        
        long temp = 0;
              
        for(int i=0; i < lngArray.length; i++)
            for(int j=1; j < lngArray.length - i; j++)
                if(lngArray[j-1] > lngArray[j])
                    swap(j, j-1);
    }
    
    /*
     * Method insertionSort()
     * 
     * Purpose: Performs insertion sort on the array
     * Parameters: None
     * Return Value: None
     * 
     */        
    private static void insertionSort() {
        int j = 0;
        long temp = 0;
      
        for (int i = 1; i < lngArray.length; i++) {
            temp = lngArray[i];
            j = i;
        
            while (j > 0 && lngArray[j - 1] > temp) {
                lngArray[j] = lngArray[j - 1];
                j--;
            }
            lngArray[j] = temp;
        }        
    }           
}
