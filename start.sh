#start.sh
~/request_handler/bin stop

rm -rf ~/request_handler/bin
rm -rf ~/request_handler/conf
rm -rf ~/request_handler/lib
mkdir ~/request_handler/logs

unzip push-logger-*.zip

chmod 755 ~/request_handler/bin/*

~/request_handler/bin start
