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

<pre>
cordova create YourApplication
</pre>
- Add an Android platform


<pre>
cd YourApplication
cordova platform add android
</pre>

- Add the plugin

<pre>
cordova plugin add https://github.com/fabricematrat/BrowserPush.git
</pre>

- Bug in some version of the command line interface

<pre>
cp -f YourApplication/plugins/BrowserPush/www/browserpush.js YourApplication/www/browserpush.js
</pre>

- Include a reference to browserpush.js in your index.html file after cordova.js

<pre>
  &lt;script type=&quot;text/javascript&quot; src=&quot;cordova.js&quot;&gt;&lt;/script&gt;
  &lt;script type=&quot;text/javascript&quot; src=&quot;browserpush.js&quot;&gt;&lt;/script&gt;
</pre>

- Add in your index.html an EventSource like for example 

<pre>
var source = new EventSource('http://googlecodesamples.com/html5/sse/sse.php');
source.addEventListener('message', function(event) {
  alert(event.data);
}, false);
</pre>

  
- Build your project

<pre>
cordova build
</pre>
- Start the emulator (You need to have an AVD created)

<pre>
cordova emulate
</pre>

To test it
===========

Go to itest where you will have an integration test
itest.sh will create a cordova project, launch it in an emulator. The project itself will test the EventSource with tests influenced by 
https://github.com/w3c/web-platform-tests/tree/master/eventsource

TODO
===========

Finish the implementation of Event source
- Redirection
- Credentails
- Test https
- Test Thread Safety
- Test Performance

Finish the implementation of WebSocket
- Only the java part is done
- Need to continue to work on it to be usable.

RELEASE NOTES
================

### 20130526 ###
* Initial Version

Give it a trial and send us feedback!
====================================

3musket33rs on twitter @3musket33rs
- Athos is Corinne Krych (@corinnekrych)
- Aramis is Sebastien Blanc (@sebi2706)
- Porthos is Fabrice Matrat (@fabricematrat)
- D'artagnain is Mathieu Bruyen (@mathbruyen)
