#!/bin/bash
validPlatform=0
if hash npm 2>/dev/null; then
  :
else
  validPlatform=1
  echo Please do install npm  -- Node Package Manager
fi

if hash node 2>/dev/null; then
  :
else
  validPlatform=1
  echo Please do install node -- Node.js
fi

if hash android 2>/dev/null; then
  :
else
  validPlatform=1
  echo Please do install the android sdk	
fi

if hash cordova 2>/dev/null; then
  echo Cordova cli is installed
else
  if [ -z "$1" ]; then
    echo Please do install cordova cli
    echo sudo npm -g install cordova
    echo sudo chown -R <username> /usr/lib/node_modules/cordova
    exit 1
  else 
    echo Install cordova cli
    sudo npm -g install cordova
    sudo chown -R <username> /usr/local/lib/node_modules/cordova
  fi
fi

if (($validPlatform != 0 )); then
  echo Please do set your environment
  exit 1
fi

currentLocation="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
pluginLocation=$(readlink -f "$currentLocation/..")

echo Clean
testlocation=$HOME/test-cordovabrowserpush
rm -fr $testlocation

echo Create Sample Application in $testlocation
if cordova create $testlocation; then : ; else exit -1; fi

echo Copy our application
cp -f $currentLocation/test/spec/EventSourceSpec.js $testlocation/www/spec/EventSourceSpec.js
cp -f $currentLocation/test/spec/lib/jasmine-1.2.0/jasmine-server.js $testlocation/www/spec/lib/jasmine-1.2.0/jasmine-server.js
cp -f $currentLocation/test/index.html $testlocation/www

echo Create a platform android
cd $testlocation
if cordova platform add android; then : ; else exit -1; fi

echo Add the BrowserPush plugin $pluginLocation
if cordova plugin add "$pluginLocation"; then : ; else exit -1; fi
# Here is probably a bug in cordova cli (2.7.4) as it wont copy in the www the file coming from the asset tag in plugin.xml
# so I do it myself
cp -f $testlocation/plugins/BrowserPush/www/browserpush.js $testlocation/www/browserpush.js
# Build copy from plugin dir so you get the js file coming from asset tag in plugin.xml

echo Build the project
if cordova build; then : ; else exit -1; fi
# Emulate copy the www dir so you don't get the file anymore (hopefully I did copy it)
# may be just use the $testlocation/platforms/android/cordova/run
# as it won't modify your file structure

echo Start the emulator
cordova emulate

echo Start the node server
cd $currentLocation/node
npm install
node server.js
rc=$?

echo Kill the emulator
#adb logcat&
adb emu kill

if [[ $rc == 0 ]] ; then
  echo SUCCESS
else 
  echo FAILED
fi
exit $rc
