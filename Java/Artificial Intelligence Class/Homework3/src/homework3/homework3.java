/*
 * 
 * Name         : Max Perkins
 * Course       : CSCI 5330
 * Instructor   : Dr. Li
 * Date Created : 2/24/2013
 * 
 * Description  : Homework 3 -- Implements the randomized select
 *                              and Monte Carlo methodology for two functions
 * 
 */
package homework3;

import java.util.*;

public class homework3 {              	
        static int x = 27;      // seed
        static int p1 = 727272;
        static int p2= 272;
        
       /*
        * Method myMonteCarloRandom(int length)
        * 
        * Purpose: Implements a Monte Carlo random number generator
        * Parameters: range of the random number
        * Return Value: the random number
        * 
        */    
        public static int myMonteCarloRandom(int range){                    
            x = (x * p1 + p2) % range;
           
            return x;
        }
        
       /*
        * Method randomizedPartition(int[] A, int p, int r)
        * 
        * Purpose: Implements partitioning of an array with a random pivot
        * Parameters: Array A, integers p and r
        * Return Value: the pivot
        * 
        */    
        public static int randomizedPartition(int[] A, int p, int r)
        {
            //int i = random.nextInt(r - p) + p;
            int i = myMonteCarloRandom(A.length);
            int exchange = A[i];
            A[i] = A[r];
            A[r] = exchange;

            return partition(A, p, r);
        }
        
       /*
        * Method partition(int[] A, int p, int r)
        * 
        * Purpose: Helper function for randomizedPartition
        * Parameters: Array A, integers p and r
        * Return Value: the pivot
        * 
        */   
        public static int partition(int[] A, int p, int r)
        {
            int x = A[r];
            int i = p - 1;
            int exchange;
            for(int j = p; j <= r - 1; j++) { 
                if(A[j] <= x){
                    i++;
                    exchange = A[i];
                    A[i] = A[j];
                    A[j] = exchange;
                }
            }

            exchange = A[i+1];
            A[i+1] = A[r];
            A[r] = exchange;

            return i+1;        
        }
        
       /*
        * Method randomizedSelect( int A[], int p, int r, int i )
        * 
        * Purpose: Implements a randomized version of select
        * Parameters: Array A, integers p, r and i
        * Return Value: the i-th element of A
        * 
        */           
	public static int randomizedSelect( int A[], int p, int r, int i ) {
            if( p == r )
                return A[p];
            int q = randomizedPartition( A, p, r );

            int k = q - p + 1;
            if( i == k )
                return A[q];
            else if(i < k)
                return randomizedSelect( A, p, q - 1, i );
            else
                return randomizedSelect( A, q + 1, r, i - k );
	}
                 
        /*
        * Method main
        * 
        * Purpose: Runs the program
        * Parameters: command line arguments
        * Return Value: none
        * 
        */
        public static void main(String[] args) {
            doRandomizedSelect();
            
            int numPoints = 100;
            doMonteCarloMadness(numPoints);
            
            numPoints = 10000;
            doMonteCarloMadness(numPoints);                        
            
            numPoints = 100000000;
            doMonteCarloMadness(numPoints);                        
        }
        
        /*
        * Method doRandomizedSelect()
        * 
        * Purpose: Runs randomizedSelect()
        * Parameters: none
        * Return Value: none
        * 
        */        
        public static void doRandomizedSelect() {        
            int[] A = new int[]{ 11,4,5,12,3,7,9,13,8,12,16,14,13 }; 


            System.out.print("Array 1: ");
            for (int i : A)
                System.out.print(String.format("%5d", i));        
            System.out.println();

            System.out.println("5th smallest element in array: " 
                                + randomizedSelect(A, 0, A.length-1, 5) );

            A = new int[15];              

            for (int i = 0; i < A.length; i++)
                A[i] = myMonteCarloRandom(100);        

            System.out.println();
            System.out.print("Array 2: ");
            for (int i : A)
                System.out.print(String.format("%5d", i));        
            System.out.println();

            int i = myMonteCarloRandom(A.length);
            System.out.println(i + "th smallest element in array: " 
                                + randomizedSelect(A, 0, A.length-1, i) + "\n");
        }

        /*
        * Method doMonteCarloMadness()
        * 
        * Purpose: Implements and runs Monte Carlo methodology
        * Parameters: numPoints - number of points
        * Return Value: none
        * 
        */        
        public static void doMonteCarloMadness(int numPoints) {                        
            Random rand = new Random();
            
            double x , f1, f2;
            double calc1, calc2;
            double actual1, actual2;
            
            x = f1 = f2 = calc1 = actual1 = calc2 = actual2 = 0.0;            
            for(int i = 0; i < numPoints; i++) {
                x = rand.nextDouble();
                f1 += function1(x);
                f2 += function2(x);
            }
            
            
            calc1 = f1 / numPoints;
            calc2 = f2 / numPoints;
            // (1/3)x^3 + x^2 + x, plugging in 1
            actual1 = 7.0/3.0;
            
            //3x^4 + x^3 + x^2 + 27x, plugging in 1
            actual2 = 34.0;
            
            double percentError1 = calcError(calc1, actual1);
            double percentError2 = calcError(calc2, actual2);
            System.out.println("Number of points = " + numPoints);
            System.out.println("f1: Approx: " + calc1 + ", Actual: " + actual1 + ", Error: " + percentError1);
            System.out.println("f2: Approx: " + calc2 + ", Actual: " + actual2 + ", Error: " + percentError2);
            System.out.println("");
        }

        /*
        * Method function1
        * 
        * Purpose: Computes the value of f1(x)
        * Parameters: x
        * Return Value: f2(x)
        * 
        */        
        public static double function1(double x) {
            return (x*x + 2*x + 1);
        }
        
        /*
        * Method function2
        * 
        * Purpose: Computes the value of f2(x)
        * Parameters: x
        * Return Value: f2(x)
        * 
        */        
        public static double function2(double x) {
            return (12*x*x*x + 3*x*2 + 2*x + 27);
        }
        
        /*
        * Method calcError
        * 
        * Purpose: Calculates the percent error
        * Parameters: calculated value and actual value
        * Return Value: percent error
        * 
        */        
        public static double calcError(double calc, double actual)
        {
            return (calc - actual)/actual * 100;
        }
}
  