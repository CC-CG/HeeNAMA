*Notably, we release our docker image at https://drive.google.com/open?id=1HcmskGKFo3Mpf7qmV9Bn2UyyHrx7koS6 because of the file and repository size limitations posted by Github.

## Experiment Replication
With the image, one can easily replicate our evaluation on HeeNAMA and baseline approaches following these steps.

### Step 1: Operations on Console 

    sudo apt-get install x11-xserver-utils
    xhost +
    
    docker load < path_to_image/replication_image.tar
    
    docker run -it \
	    -v /tmp/.X11-unix:/tmp/.X11-unix \
	    -e DISPLAY=unix$DISPLAY \
	    --name HeeNAMA \
	    lin/replication:v1 \
	    /bin/bash

### Step 2:  Operations on  Eclipse
   
 - Import one of projects in the path /root/projects into eclipse.
 - Copy the others into /root/data/train.
 - Click on the imported project in Project Explorer, and then click the menu HeeNAMA/Start. 
 - Select the directory /root/data/.
 - Examine files generated under /root/data/features. *_results.csv records completion results of three tools. *_features.csv is the feature file. 
 - Copy *_features.csv into /root/features/train or /root/features/test.


### Step 3: Operation on Console

    python ~/filter.py
