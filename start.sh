#!/bin/bash

trap kill_programs INT

kill_programs()
{
    echo "Killing Program"
    killall -q predator
    killall -q prey
    killall -q pursuit_monitor
    killall -q pursuit
    exit 0
}

echo "*****************************************************************"
echo "* Pursuit Domain - University of Amsterdam, The Netherlands     *"
echo "* Created by Jelle Kok                                          *"
echo "* Copyright 2002-2003.  All rights reserved.                    *"
echo "*****************************************************************"

export CLASSPATH=${CLASSPATH}:.

pursuit_dir="/home/nikolas/Desktop/pursuit/bin"
pursuit="${pursuit_dir}/pursuit"
pursuit_conf="pursuit.conf"
monitor_dir="${pursuit_dir}"
monitor="${monitor_dir}/pursuit_monitor"
monitor_conf="monitor.conf"
predator="/usr/lib/jvm/java-7-openjdk-i386/bin/java Predator"
prey="${pursuit_dir}/prey"

${pursuit} -conf ${pursuit_conf} &    # start the server (no visualization yet)
sleep 1
${predator}   &                       # start clients, only output info pred. 1
sleep 1
${predator} > /dev/null & 
sleep 1
${prey}     > /dev/null & 
#sleep 1
#${prey}     > /dev/null & 

${monitor} -conf ${monitor_conf}       # start visualization

wait
