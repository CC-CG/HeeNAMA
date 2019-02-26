

# HeeNAMA

This repository contains implementation and dataset for **Heuristic and Neural Network based Prediction of Non-API Member Access**. 

 - ./dataset: subject applications used in the evaluation.
 - ./docker image: image for easily replicating evaluation on HeeNAMA and compared tools.
 - ./plugin: easy-to-use eclipse plug-in of HeeNAMA.
 - ./source code: implementation of HeeNAMA.

## Experiment Replication
With the docker image under ./docker image, one can easily replicate our evaluation experiment on HeeNAMA as well as compared approach and tool following these steps:

### Step 1

    sudo apt-get install x11-xserver-utils
    xhost +
    
    docker load < path_to_image/replication_image.tar
    
    docker run -it \
	    -v /tmp/.X11-unix:/tmp/.X11-unix \
	    -e DISPLAY=unix$DISPLAY \
	    --name HeeNAMA \
	    lin/replication:v1 \
	    /bin/bash

### Step 2

    eclipse
   
 - Import one of projects in the path /root/projects into eclipse.
 - Copy the others into /root/data/train.
 - Click on the imported project in Project Explorer, and then click the menu HeeNAMA/Start. 
 - Select the directory /root/data/.
 - Examine files generated under /root/data/features. *_results.csv records completion results of three tools. *_features.csv is the feature file. 
 - Copy *_features.csv into /root/features/train or /root/features/test.


### Step 3

    python ~/filter.py
