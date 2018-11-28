#!/bin/bash

# Times to replicate
#SBATCH --array=1-1000  ##doing the same job for multiple times

#SBATCH --output=/work/mperkins/cfung/outdir/B17-%J-%a.out

#SBATCH --partition=talon-mech

#SBATCH --mail-user=mperkins@georgiasouthern.edu
#SBATCH --mail-type=FAIL


module load R/R-3.4.1
Rscript /work/mperkins/cfung/2017-04-27_B_17array.r $1