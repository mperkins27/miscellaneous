/*  Max Perkins
 *  CSCI 7332 - Parallel Algorithms Design and Analysis
 *  Fall 2018
 *
 *  Homework 1: Parallel Prefix Scan
 *  
 *  Running instructions:
    
    module purge
    module load system/mpich-3.2
    mpicc -o mpi_prefixsum mpi_prefixsum.c -std=gnu99
    
    Do salloc (i.e. salloc -n p)
    
    mpirun ./mpi_prefixsum n
    
    where p is the processors and n is the array aize
    
 *
 */

#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>

void print_array(int *arr, int size) {
	for (int i = 0; i < size; i++) 
	    printf("%d, ", arr[i]);
	printf("\n");
}

int main(int argc, char** argv) {
    // Variable init
	int size, rank;
	char processor_name[MPI_MAX_PROCESSOR_NAME];
	int processor_name_len;
	
	int master = 0;
    int  N, n, p, chunkSize;
    int retVal = 0;
    int *array;
    
    struct timespec start, end;
    
	MPI_Status status;

    // MPI Init
	int rc = MPI_Init(&argc, &argv);
	if (rc != MPI_SUCCESS) {
		printf("Error starting MPI program. Terminating. \n");
		MPI_Abort(MPI_COMM_WORLD, rc);
	}
	
	MPI_Comm_size(MPI_COMM_WORLD, &size);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);
	MPI_Get_processor_name(processor_name, &processor_name_len);
	
	// Input checking
    if(argc != 2 && rank == master) {
            printf("Usage: %s n\nWhere n is the size of the array\n",argv[0]);
            retVal = 1;
    }
    else {
        N = atoi(argv[1]);
        
        if(N <= 1|| N > 2048) {
            if(rank == master) printf("Error: Must enter a size greater than 1 and less than 2048.\n");
            retVal = 1;
        }
        if(N % 2 != 0) {
            if(rank == master) printf("N and must be even.\n");
            retVal = 1;
        }
        if(size > N / 2) {
            if(rank == master) printf("Don't use any more than N/2 processors.\n");
            retVal = 1;
        }
        if(size % 2 != 0) {
            if(rank == master) printf("p and must be even.\n");
            retVal = 1;
        }
        if(N % size != 0) {
            if(rank == master) printf("N must be divisible by p\n");
            retVal = 1;
        }
        if(size < 1) {
            if(rank == master) printf("p must be greater than 1\n");
            retVal = 1;
        }
        
        if(retVal == 1 ) {
            MPI_Finalize();
            return 1;
        }
    
        n = N;
    	p = size;
    }
    int *seqArray;
    
    // Initialize the main array and an
    // array to do sequential scan
	if (rank == master) {
		array = malloc(sizeof(int) * n);
		seqArray = malloc(sizeof(int) * n);
		
		for (int i = 0; i < n; i++) {
            array[i] = i+1;
            seqArray[i] = i+1;
		}
        print_array(array, n);
	}
	
	// Send out equal chunks to each processor
	chunkSize = n / p;
	
	int *subArr = malloc( sizeof(int) * chunkSize);
	int done = 0;
	int prev = 0;
	
	// Start the timer
	if(rank == master) clock_gettime(CLOCK_REALTIME,&start);
	
	// Send out all the chunks
	MPI_Scatter(array, chunkSize, MPI_INT, subArr, chunkSize, MPI_INT, master, MPI_COMM_WORLD);

    // Each local processor does its own prefix sum
	for(int i = 1; i < chunkSize; i++) 
	    subArr[i] += subArr[i-1];
	
	// Each processor sends its maximum to the next processor
	// 0 -> 1 -> 2 -> 3   etcetera
	if(rank == master) {
        MPI_Send(&subArr[chunkSize-1], 1, MPI_INT, rank + 1, 0, MPI_COMM_WORLD);
    } else {
    	MPI_Recv(&prev, 1, MPI_INT, rank - 1, 0, MPI_COMM_WORLD, &status);
    	    
	    for(int i = 0; i < chunkSize; i++)
	        subArr[i] += prev;
    	        
	    if(rank + 1 < p) 
	        MPI_Send(&subArr[chunkSize-1], 1, MPI_INT, rank + 1, 0, MPI_COMM_WORLD);
	}

    // Once all the max's are sent, gather back the results
	MPI_Gather(subArr, chunkSize, MPI_INT, array, chunkSize, MPI_INT, master, MPI_COMM_WORLD);
	
	// Stop the timer
    if(rank == master) clock_gettime(CLOCK_REALTIME,&end);

	free(subArr);
	
	// Print arrays and execution times
	if (rank == master) {
	    print_array(array,n); 
	    free(array);
	    
	    double extime = (end.tv_sec - start.tv_sec) + (end.tv_nsec - start.tv_nsec) / 1000000000.0;
        printf("\n\tExecution time for the parallel is %.9f seconds\n ",extime);
	    
	    clock_gettime(CLOCK_REALTIME,&start);
	    for(int i = 1; i < n; i++)
	        seqArray[i] += seqArray[i-1];
	    clock_gettime(CLOCK_REALTIME,&end);
	    
	    extime = (end.tv_sec - start.tv_sec) + (end.tv_nsec - start.tv_nsec) / 1000000000.0;
        printf("\tExecution time for the sequential is %.9f seconds\n ",extime);
	}

	MPI_Finalize();
	return 0;
}

