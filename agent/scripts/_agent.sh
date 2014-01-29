#!/bin/bash

date
echo "Restarting netatalk, hci ..."

/etc/init.d/netatalk restart

hciconfig hci0 down
hciconfig hci0 up

su pi -c "cd ~/zeroadv; git pull; cd agent; make"

cd /home/pi/zeroadv/agent

# http://stackoverflow.com/questions/696839/how-do-i-write-a-bash-script-to-restart-a-process-if-it-dies
until ./zeroadv
do
	sleep 1
	date
	echo "Exited with exit code $?, restarting ..."
done
