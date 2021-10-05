#!/bin/bash

# Build the application:
./build.sh

# In first terminal window:
./run.sh --id 1 --hosts ../example/hosts --output ../example/output/1.output ../example/configs/perfect-links.config

# In second terminal window:
./run.sh --id 2 --hosts ../example/hosts --output ../example/output/2.output ../example/configs/perfect-links.config

# In third terminal window:
./run.sh --id 3 --hosts ../example/hosts --output ../example/output/3.output ../example/configs/perfect-links.config

# Wait enough time for all processes to finish processing messages.
# Type Ctrl-C in every terminal window to create the output files.
# Of course, you will NOT find any output files after running this because there is nothing implemented now!
