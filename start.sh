#start.sh
/home/tmon/tmonpush-logger/bin stop

rm -rf /home/tmon/tmonpush-logger/bin
rm -rf /home/tmon/tmonpush-logger/conf
rm -rf /home/tmon/tmonpush-logger/lib
mkdir /home/tmon/tmonpush-logger/logs

unzip push-logger-*.zip

chmod 755 /home/tmon/tmonpush-logger/bin/*

/home/tmon/tmonpush-logger/bin start
