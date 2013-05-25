var express = require('express');
var app = express();

var eventSourceRes;

var open = function(status) {
  if (!status) {
    status = 200;
  }
  if (status == 301) {
    content = {"Location": "eventsource-onmessage", "Content-Type":"text/event-stream", "Cache-Control":"no-cache", "Connection":"keep-alive"}
  } else {
    content = {"Content-Type":"text/event-stream", "Cache-Control":"no-cache", "Connection":"keep-alive"}
  }
  eventSourceRes.writeHead(status, content);
  // Should write otherwise nothing happens and open is never call !!!
  eventSourceRes.write(":");
  if (status == 301) {
    eventSourceRes.end();
  }
};

app.use(express.static(__dirname + "/../test")); // Current directory is root
app.use(express.bodyParser());

app.get('/stream', function(req, res){
	eventSourceRes = res;
    eventSourceRes.socket.setTimeout(Infinity);
    req.connection.addListener("close", function () {
      if (eventSourceRes) {
        eventSourceRes.shouldKeepAlive = false;
        if (eventSourceRes.socket) {
          eventSourceRes.socket.destroy();
        }
        eventSourceRes.end();
      }
      lastEventId = null;      
    }, false);
    open();
    return;
});

app.get('/eventsource-onmessage', function(req, res) {
    var message = req.query.message ? req.query.message : "data: data";
    var newline = req.query.newline ? (newline == "none" ? "" : newline) : "\n\n";
    if (req.query.id) {
      eventSourceRes.write(id + '\n');
      lastEventId = id;
    } else if (lastEventId !== null) {
      eventSourceRes.write(lastEventId + '\n');
    }
    eventSourceRes.write(message + newline);
    res.writeHead(200, {"Content-Type":"text/html"});
    res.end("kool", "utf-8");
    if (message.indexOf("retry") !== -1) {
      eventSourceRes.shouldKeepAlive = false;
      if (eventSourceRes.socket) {
        eventSourceRes.socket.destroy();
      }
      eventSourceRes.end();
    }
});

app.get('/eventsource-disconnect', function(req, res) {
    eventSourceRes.shouldKeepAlive = false;
    if (eventSourceRes.socket) {
      eventSourceRes.socket.destroy();
    }
    eventSourceRes.end();
    res.writeHead(200, {"Content-Type":"text/html"});
    res.end("kool", "utf-8");
});

app.post('/results', function(req, res) {
    var failure = req.body.results.statistics.numberOfFails;
    if (failure > 0) {
        process.exit(failure*-1);
    } else {
        process.exit(0);
    }
});

app.post('/testStart', function(req, res) {
    console.log(req.body);
});

app.post('/testEnd', function(req, res) {
    console.log(req.body);
});

app.listen(9090);
console.log('Listening on port 9090');

