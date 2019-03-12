#!/usr/bin/env bash

# ip address regular expression: ([0-9]{1,3}\.){3}[0-9]{1,3}
# port regular expression: [0-9]{1,4}

confPath="/etc/nginx/nginx.conf"

# note: the semicolons before end of brace is to signal termination of a command, see: https://unix.stackexchange.com/questions/290146/multiple-logical-operators-a-b-c-and-syntax-error-near-unexpected-t
if { [ $1 != "redis" ] && grep -q -e $1 $confPath; } || { [ $1 = "redis" ] && grep -q -e REDIS_HOST $confPath; }
then

  # if PARAM is an ip address and ip address exists in file, or if PARAM is 'redis' and REDIS_HOST already exists in file
  echo "This host is the one currently in use. No hotswap needed!"

else

  if [ $1 = "redis" ]
  then
    sed -i -r "s/(([0-9]{1,3}\.){3}[0-9]{1,3}|%\{REDIS_HOST\})/%{REDIS_HOST}/g" $confPath
    sed -i -r "s/(\:[0-9]{1,4}|\:%\{REDIS_PORT\})/\:%\{REDIS_PORT\}/g" $confPath
    sed -i -r "s/(listen ([0-9]{1,4}|%\{PROXY_PORT\}))/listen %\{PROXY_PORT\}/g" $confPath
  else
    sed -i -r "s/(([0-9]{1,3}\.){3}[0-9]{1,3}|%\{REDIS_HOST\})/$1/g" $confPath
    sed -i -r "s/(\:[0-9]{1,4}|\:%\{REDIS_PORT\})/\:6379/g" $confPath
    sed -i -r "s/(listen ([0-9]{1,4}|%\{PROXY_PORT\}))/listen 8888/g" $confPath
  fi

  /usr/sbin/nginx -s reload  # reload nginix to enable these changes
  # FIXME: reload untested

fi
