#!/bin/sh
USER=${USER:-"openttd"}

echo "
-----------------------------------
GID/UID
-----------------------------------
User uid:    $(id -u ${USER})
User gid:    $(id -g ${USER})
User Home:   $(grep ${USER} /etc/passwd | cut -d':' -f6)
-----------------------------------
"

cmd=""
for var in "$@"
do
  cmd="$cmd '$var' "
done

su -l openttd -c "/home/openttd/openttd-15/openttd -D ${cmd}"
exit 0
