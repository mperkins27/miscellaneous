/*
 *
 * Max Perkins
 * CSCI 7332 - Parallel Algorithms Design & Analysis
 * Fall 2018
 *
 * Final Project: IDW-based Spatiotemporal Interpolation using CUDA
 *
 *
*/


#include "cuda_runtime.h"
#include "device_launch_parameters.h"
#include "helper_functions.h"

#include <iostream>
#include <string>
#include <fstream>
#include <sstream>
#include <vector>
#include <random>
#include <algorithm>

using namespace std;

typedef struct site {
	double x, y;
	double pm25;
	double d;
	int t;
} SITE;

typedef struct errs {
	double err;
	double p;
	double est_pm25;
} ERRS;

struct {
	bool operator()(SITE a, SITE b) const
	{
		return a.d < b.d;
	}
} customComp;

SITE *pm25data = NULL;
SITE *modData = NULL;

SITE *trainingData = NULL;
SITE *testData = NULL;

const int dataSize = 146125;
const int testSize = (int)dataSize / 10;
const int trainingSize = dataSize - testSize;

const double c = 0.1086;

void seqDistanceCalc(SITE *data, int dataSize, double x, double y, int t, double c);
double idwCalc(int N, double p, SITE *);
void loadData();

__global__
void distanceCalc(SITE *data, const int size, double x, double y, int t, const double c) {
	int i = blockIdx.x * blockDim.x + threadIdx.x;
	double dt, dy, dx;

	if (i < size) {
		dx = data[i].x - x;
		dy = data[i].y - y;
		dt = data[i].t - t;
					
		data[i].d = (dx*dx) + (dy*dy) + (dt*dt)*(c*c);

		// 1.4^2 = 1.96, 7^2 = 49
		if (data[i].d > 1.96 || dt > 49)
			data[i].d = INFINITY;

	}
}

int main() {	
	cout << "Allocating host memory...\n";

	pm25data = (SITE *)malloc(sizeof(SITE) * dataSize);
	modData = (SITE *)malloc(sizeof(SITE) * dataSize);

	trainingData = (SITE *)malloc(sizeof(SITE) * trainingSize);
	testData = (SITE *)malloc(sizeof(SITE) * testSize);

	SITE *d_pm25data = NULL;
	SITE *d_trainingData = NULL;	
	
	cudaError_t cudaStatus;

	StopWatchInterface *gpuTimer = NULL;
	sdkCreateTimer(&gpuTimer);

	StopWatchInterface *hTimer = NULL;
	sdkCreateTimer(&hTimer);	

	cout << "Reading PM 2.5 2009 data file and building array...\n";
	loadData();	

	cout << "Setting the default device and allocating device memory...\n";
	cudaSetDevice(0);
	cudaMalloc(&d_pm25data, dataSize * sizeof(SITE));
	cudaMalloc(&d_trainingData, trainingSize * sizeof(SITE));

	cout << "Copying PM 2.5 data to device memory...\n";
	cudaMemcpy(d_pm25data, pm25data, dataSize * sizeof(SITE), cudaMemcpyHostToDevice);

	double x, y;	
	int t, nn, i;		

	nn = 3;

	cout << "\n";
	while(true) {
		cout << "\nc = " << to_string(c) << ", N = " << to_string(nn) << "\n";
		cout << "Enter t, y, x (or 1's to run error calculations, 0's to quit): ";		
		cin >> t >> y >> x;

		if (!x && !y && !t)
			break;
		else if (x==1 && y==1 && t==1) {
			// Run error checker						
			random_device                  rand_dev;
			mt19937                        generator(rand_dev());
			uniform_int_distribution<int>  distr(0, dataSize - 1);

			int randNums[testSize];
			
			cout << "Initializing test data...\n";
			// Get random indices in [0, dataSize)
			for (int i = 0; i < testSize; i++)
				randNums[i] = distr(generator);
			
			// Initialize test data
			for (int i = 0; i < testSize; i++)
				testData[i] = pm25data[randNums[i]];
			
			cout << "Initializing \"training\" data...\n";
			// Initialize "training" data
			int tr = 0;
			for (int i = 0; i < dataSize; i++) {
				bool found = false;
				for (int j = 0; j < testSize && !found; j++)
					found = (i == randNums[j]) ? true : false;

				if (!found && tr < trainingSize)
					trainingData[tr++] = pm25data[i];									
			}

			int n = 10;

			ERRS bestErr[10];

			double bestE = 10, worstE = 90;
			double bestP = 6, worstP = 0.5;

			cout << "Running " << to_string(n) << " calculations...\n";

			sdkResetTimer(&gpuTimer);
			sdkResetTimer(&hTimer);
			
			double hVal = 0;
			double gVal = 0;
			
			for (int i = 0; i < n; i++) {							
				cout << "Data point: " << to_string(i+1) << " out of " << to_string(n) << ": "
					 << to_string(testData[i].t) << " " << to_string(testData[i].y) << " "
					 << to_string(testData[i].x) << " " << to_string(testData[i].pm25) << " "<< endl;

				cout << "\tCopying \"training\" data to device memory...\n";
				cudaMemcpy(d_trainingData, trainingData, trainingSize * sizeof(SITE), cudaMemcpyHostToDevice);

				sdkStartTimer(&gpuTimer);
				cout << "\tCalculating square of the distances in parallel...\n";
				distanceCalc << < 1169, 125 >> > (d_trainingData, trainingSize, testData[i].x, testData[i].y, testData[i].t, c);
				sdkStopTimer(&gpuTimer);
				
				gVal += sdkGetTimerValue(&gpuTimer) / 1000;
				
				sdkStartTimer(&hTimer);
				cout << "\tCalculating square of the distances in serial...\n";
				seqDistanceCalc(trainingData, trainingSize, testData[i].x, testData[i].y, testData[i].t, c);
				sdkStopTimer(&hTimer);

				hVal += sdkGetTimerValue(&hTimer) / 1000;

				cout << "\tCopying results to host memory...\n";
				cudaMemcpy(trainingData, d_trainingData, trainingSize * sizeof(SITE), cudaMemcpyDeviceToHost);
				
				cout << "\tSorting the distances...\n";				
				sort(trainingData, trainingData + trainingSize - 1, customComp);

				double est_pm25 = 0;

				bestErr[i].err = 5;
				bestErr[i].p = 0;
				
				cout << "\tCalculating errors with 0.5 <= p <= 6, calculating best p and error...\n";
				for (double p = 0.5; p <= 6; p += 0.5) {
					est_pm25 = idwCalc(nn, p, trainingData);

					// calc error
					double err = abs(((est_pm25 - testData[i].pm25) / testData[i].pm25) * 100);

					if (err > worstE) {
						worstE = err;
						worstP = p;
					}
						
					if (err < bestE) {
						bestE = err;
						bestP = p;
					}						

					if (err < bestErr[i].err) {
						bestErr[i].err = err;
						bestErr[i].p = p;
						bestErr[i].est_pm25 = est_pm25;
					}
						
					cout << "\t\tp = " << to_string(p) << " Est: " << to_string(est_pm25) << " ";
					cout << "O: " << to_string(testData[i].pm25) << " Error: " << to_string(err) << endl;
				}		
			
				cout << "\tNearest neighbors (" << to_string(nn) << ") :\n";

				for (int i = 0; i < nn; i++) {
					cout << "\t\t";
					cout << to_string(trainingData[i].t) << " " << to_string(trainingData[i].y) << " "
						<< to_string(trainingData[i].x) << " " << to_string(trainingData[i].pm25) << " "
						<< to_string(trainingData[i].d) << "\n";
				}
			}
			

			cout << "Complete!\n ";
		
			cout << "Parallel Time: " << to_string(gVal) << " seconds\n";
			cout << "Serial Time  : " << to_string(hVal) << " seconds\n";
			cout << "Speedup      : " << to_string(hVal / gVal) << "\n";

			cout << "Best Error : " << to_string(bestE)  << " Best P: " << to_string(bestP) << endl;
			cout << "Worst Error: " << to_string(worstE) << " Worst P: " << to_string(worstP) << endl;
			
			double allP[] = { 0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0, 5.5, 6.0};
			int freq[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

			for (int j = 0; j < n; j++) {
				int index = ((double)bestErr[j].p / 0.5) - 1;

				freq[index]++;
			}

			cout << "Frequency of Best P's:\n";
			for (int i = 0; i < 12; i++) {
				if(freq[i] > 0)
					cout << to_string(allP[i]) << " - " << to_string(freq[i]) << "\n";
			}
		}
		else {			
			cout << "Calculating square of the distance in parallel...\n";

			// start the timers
			sdkResetTimer(&gpuTimer);
			sdkStartTimer(&gpuTimer);

			//distanceCalc<<< 2048, 128 >>> (d_pm25data, dataSize, x, y, t, c);
			distanceCalc <<< 1169, 125 >>> (d_pm25data, dataSize, x, y, t, c);

			sdkStopTimer(&gpuTimer);

			// get milliseconds
			double gTimerValue = sdkGetTimerValue(&gpuTimer);

			cout << "Parallel time: " << to_string(gTimerValue) << " milliseconds\n";

			// Check for any errors launching the kernel
			cudaStatus = cudaGetLastError();
			if (cudaStatus != cudaSuccess)
				cout << "distanceCalc kernel launch failed: "
				<< cudaGetErrorString(cudaStatus) << "\n";

			cout << "Synchronizing device threads...\n";

			// cudaDeviceSynchronize waits for the kernel to finish, and returns
			// any errors encountered during the launch.
			cudaStatus = cudaDeviceSynchronize();
			if (cudaStatus != cudaSuccess)
				cout << "cudaDeviceSynchronize returned error code "
				<< to_string(cudaStatus)
				<< "after launching distanceCalc kernel!\n";

			cout << "Copying results to host memory...\n";
			cudaMemcpy(modData, d_pm25data, dataSize * sizeof(SITE), cudaMemcpyDeviceToHost);

			cout << "Calculating the distances sequentially...\n";
			// start the timers
			sdkResetTimer(&hTimer);
			sdkStartTimer(&hTimer);

			seqDistanceCalc(pm25data, dataSize, x, y, t, c);

			sdkStopTimer(&hTimer);

			// get milliseconds
			double sTimerValue = sdkGetTimerValue(&hTimer);

			cout << "Serial time: " << to_string(sTimerValue) << " milliseconds\n";

			cout << "Speedup is: " << to_string(sTimerValue / gTimerValue) << "\n";

			cout << "Sorting the distances...\n";
			sort(modData, modData + dataSize - 1, customComp);
			//quickSort(modData, 0, dataSize - 1);

			cout << "Nearest neighbors (" << to_string(nn) << ") :\n";

			for (i = 0; i < nn; i++) {
				cout << to_string(modData[i].t) << " " << to_string(modData[i].y) << " "
					<< to_string(modData[i].x) << " " << to_string(modData[i].pm25) << " "
					<< to_string(modData[i].d) << "\n";
			}

			double p, est_pm25;

			for (p = 0.5; p < 6.5; p += 0.5) {
				est_pm25 = idwCalc(nn, p, modData);
				cout << "p = " << to_string(p) << " Est: " << to_string(est_pm25) << "\n";
			}
		}				
	}

	cout << "\nFreeing host memory...\n";
	free(trainingData);
	free(testData);	
	free(modData);
	free(pm25data);	
	
	cout << "Freeing device memory...\n";
	cudaFree(d_trainingData);
	cudaFree(d_pm25data);

	cudaDeviceReset();

	return 0;
}

void loadData() {
	fstream file;
	string line;
	int i = 0;

	file.open("pm25_2009_data.csv");	
	while (getline(file, line, '\n') && i < dataSize) {
		istringstream templine(line);
		string data;		

		getline(templine, data, ',');
		pm25data[i].t = atoi(data.c_str());

		getline(templine, data, ',');
		pm25data[i].y = atof(data.c_str());

		getline(templine, data, ',');
		pm25data[i].x = atof(data.c_str());

		getline(templine, data, ',');
		pm25data[i].pm25 = atof(data.c_str());

		pm25data[i].d = 0;		

		i++;
	}
	file.close();
}

void seqDistanceCalc(SITE *data, int dataSize, double x, double y, int t, double c) {
	double dt, dy, dx;

	for (int i = 0; i < dataSize; i++) {
		dt = data[i].t - t;
		dx = data[i].x - x;
		dy = data[i].y - y;

		data[i].d = (dx*dx) + (dy*dy) + (dt*dt)*(c*c);

		// d > 1.4, t > 7
		if (data[i].d > 1.96 || dt > 49)
			data[i].d = INFINITY;

		//if (data[i].d != modData[i].d && data[i].d != INFINITY)
		//	cout << "Result " << to_string(i) << " is not the same!\n";
	}
}

double idwCalc(int N, double p, SITE *data) {
	double w, wi;
	double lambdaD, lambda;

	w = 0;
	for (int i = 0; i < N; i++) {
		wi = data[i].pm25;

		lambdaD = 0;
		for (int k = 0; k < N; k++)
			lambdaD += pow(1 / sqrt(data[k].d), p);

		lambda = pow(1 / sqrt(data[i].d), p) / lambdaD;

		w += wi*lambda;
	}
	return w;
}