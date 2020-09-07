#!/bin/bash
# Slurm job submission script for MoDS.
# It is being used by a wrapper script.
# DO NOT EXECUTE THIS DIRECTLY ON THE COMMAND LINE!
# DO NOT SUBMIT THIS DIRECTLY TO SLURM!

#SBATCH -p como
#SBATCH -A COMO-SL2-CPU
#SBATCH --mem=64000
#SBATCH --time=96:00:00
#SBATCH --nodes=10
#SBATCH --ntasks-per-node=16
#SBATCH --output slurm.%u.%j.%N.stdout.txt   # (%u,%j,%N)=(user, job allocation #, node)  
#SBATCH --error slurm.%u.%j.%N.errout.txt    #
#SBATCH --mail-type=END,FAIL                 # notifications for job done & fail

# Load the environment seen by the application
eval "$(conda shell.bash hook)"
conda activate pycantera                     #REQUIRED - loads the cantera environment

#! Number of nodes and tasks per node allocated by SLURM (do not change):
numnodes=$SLURM_JOB_NUM_NODES
mpi_tasks_per_node=$(echo "$SLURM_TASKS_PER_NODE" | sed -e  's/^\([0-9][0-9]*\).*$/\1/')

#! Number of MPI tasks to be started by the application per node and in total (do not change):
np=$[${numnodes}*${mpi_tasks_per_node}]

# Create folder for the execution
SCRATCH_DIRECTORY=/rds/user/$USER/hpc-work/scratch/$SLURM_JOBID/
export MODSDIR=$SCRATCH_DIRECTORY
mkdir -p $MODSDIR
cd $MODSDIR

# Copy input files to the execution folder
cp $SLURM_SUBMIT_DIR/*.zip . 
mv *.zip input.zip
unzip input.zip
cp -r $SLURM_JOB_NAME/* . 
rm -rf $SLURM_JOB_NAME/
chmod +x *.sh

# Execute the simulation
MODS_MPI=/home/jb2197/Codes_kinetics/mods-backend/outputs/Release/bin/MoDS_mpi
CMD="mpirun -ppn $mpi_tasks_per_node -np $np \"$MODS_MPI\""
echo -e "\nExecuting command:\n$CMD\n==================\n"
eval $CMD

echo
echo 'Slurm job diagnostics:'
sacct --job $SLURM_JOBID --format "JobName,Submit,Elapsed,AveCPU,CPUTime,UserCPU,TotalCPU,NodeList,NTasks,AveDiskRead,AveDiskWrite"

# Pack all output files to output.zip (3 steps)
cd $MODSDIR
rm -rf All/ Initial/ Working_dir/			 # 1 - remove the input files
for FOLDER in *; do							 # 2 - remove subfolders in the output folder
	if [ -d "$FOLDER" ]; then
		cd $FOLDER
		for SUB_FOLDER in *; do
			if [ -d "$SUB_FOLDER" ]; then
				rm -rf $SUB_FOLDER
			fi
		done
		cd ..
	fi
done
zip -r output.zip */						 # 3 - zip output files to output.zip

cp -pr $SCRATCH_DIRECTORY/* $SLURM_SUBMIT_DIR
cd $SLURM_SUBMIT_DIR
rm -rf $SCRATCH_DIRECTORY || exit $?
