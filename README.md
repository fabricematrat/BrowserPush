BrowserPush Cordova Plugin
========================

Event Source and WebSocket are not implemented in the Android browser.
Here comes this cordova plugin which allow the usage of SSE and WebSocket for Cordova application.

For EventSource the implementation should be compliant with the specification
http://www.w3.org/TR/2012/WD-eventsource-20121023/

Prerequisite
===========

- JDK 5 or 6
- Android sdk
- Cordova 2.5 or more
- Node Package Manager for test and cli
- Node.js for test and cli


Install
===========

If you use Cordova command line interface :

- Create an application
 cordova create YourApplication
- Add an Android platform
 cordova platform add android
- Bug in some version of the command line interface
 cp -f YourApplication/plugins/BrowserPush/www/browserpush.js YourApplication/www/browserpush.js
- Build your project
 cordova build
- Start the emulator (You need to have an AVD installed)
 cordova emulate

To test it
===========

Go to itest where you will have an integration test

TODO
===========

- Finish the implementation of Event source
-- Redirection
-- Credentails
-- Test https
-- Test Thread Safety
-- Test Performance

- Finish the implementation of WebSocket
-- Only the java part is done
-- Need to continue to work on it to be usable.



Give it a trial and send us feedback!
====================================

3musket33rs on twitter @3musket33rs
- Athos is Corinne Krych (@corinnekrych)
- Aramis is Sebastien Blanc (@sebi2706)
- Porthos is Fabrice Matrat (@fabricematrat)
- D'artagnain is Mathieu Bruyen (@mathbruyen)