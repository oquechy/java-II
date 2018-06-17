#! /bin/bash

if [ -z "$1" ]; then
        address="$(whoami)@localhost"
    else
        address="$1"
fi

ip=""
for i in $(hostname -I) ; do
	if [[ $i == 192.168.* ]] ; then
		ip=$i
		break
	fi
done

if [ -z "$ip" ]; then
	echo "no network"
	exit 1
fi

echo "server ip: $ip"

if [ ! -f build/libs/admission.jar ] ; then
	gradle jar
fi

java -cp  build/libs/admission.jar ru.spbau.mit.oquechy.stress.server.Server $ip $address
#sleep 1
#echo "connecting to $address"
#ssh $address "java -cp admission.jar ru.spbau.mit.oquechy.stress.client.Client $ip 3030"
#echo "session finished"
