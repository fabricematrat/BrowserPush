/**
 * Test are coming from
 * https://github.com/w3c/web-platform-tests/tree/master/eventsource
 * to test the compliance of an EventSource implementation by the W3C
 */
var baseURL;
var cordova = window.cordova;
if (cordova) {
    baseURL = "http://10.0.2.2:9090/";
} else {
    if(window.location.protocol === "file:") {
        baseURL = "http://localhost:9090/";
    } else {
        baseURL = "";
    }
}

var send = function(message) {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open( "GET", baseURL + message, false );
    xmlHttp.send( null );
};

describe("EventSource", function() {
    var eventSource;

    beforeEach(function() {
        eventSource = null;
    });
    afterEach(function () {
        if (eventSource) {
            eventSource.onerror = function() {};
            eventSource.onmessage = function() {};
            eventSource.onopen = function() {};
            eventSource.close();
        }
    });

    it("Should be able to create an Event Source", function() {
        eventSource = new EventSource(baseURL + "stream");
        expect(eventSource).not.toBe(null);
        eventSource.close();
    });

    it("Prototype should work", function() {
        EventSource.prototype.ReturnTrue = function() { return true }
        eventSource = new EventSource(baseURL + "stream");
        expect(eventSource.ReturnTrue()).toBe(true);
        expect(window.EventSource).toBeTruthy();
        eventSource.close()
    });
  
    it("Close should set readyState to closed", function() {
        var flag;
        flag = false;
        eventSource = new EventSource(baseURL + "stream");
        eventSource.onopen = function() {
            eventSource.close();
            flag = true;
        };
        waitsFor(function() {
            return flag;
        }, "The call is done", 10000);
        runs(function() {
            expect(eventSource.readyState).toBe(EventSource.CLOSED);
        });
    });
    

    it("Test source url in eventsource", function() {
          var source = new EventSource(1);
          expect(/\/1$/.test(source.url)).toBeTruthy();
          source.close();

          source = new EventSource(null);
          expect(/\/null$/.test(source.url)).toBeTruthy();
          source.close();

          source = new EventSource(undefined);
          expect(/\/undefined$/.test(source.url)).toBeTruthy();
          source.close();

          source = new EventSource("http://10.0.2.2:9090/stream");
          expect(source.url).toEqual("http://10.0.2.2:9090/stream");
          source.close();
    });


    it("Test invalid url in eventsource", function() {
        expect(function() {
            var eventSource = new EventSource("http://this is invalid/");
            if (eventSource != null) {
                eventSource.close();
            }
        }).toThrow();
    });
    
    it("Test event listener message ", function() {
        var data, flag;
        flag = false;
        eventSource = new EventSource(baseURL + "stream");
        eventSource.addEventListener("message", function(e) {
            data = e.data;
            flag = true;
            eventSource.close();
        }, false);
        eventSource.onopen = function() {
            send("eventsource-onmessage");
        };
        waitsFor(function() {
            return flag;
        }, "The call is done", 10000);
        runs(function() {
            expect(data).toBe("data");
        });
    });
    
    it("Test format", function() {
        var data, flag;
        var first, second, third = false;
        flag = false;
        eventSource = new EventSource(baseURL + "stream");
        eventSource.onmessage = function(e) {
          if(e.data="1") {
            first = true;
          }
          if(e.data="2") {
            second = true;
          }
          if(e.data="3") {
          flag = first && second;
          }
        };
        eventSource.onopen = function() {
          send("eventsource-onmessage?message=%EF%BB%BFdata%3A1%0A%0A%EF%BB%BFdata%3A2%0A%0Adata%3A3");
        };
        waitsFor(function() {
            return flag;
        }, "The call is done", 10000);
        runs(function() {
            expect(eventSource.readyState).toBe(EventSource.OPEN);
            eventSource.close();
        });
    });

    it("Test format bom 2 (need to find a real name here)", function() {
        var data, flag;
        var first, second, third = false;
        flag = false;
        eventSource = new EventSource(baseURL + "stream");
        eventSource.onmessage = function(e) {
          if(e.data="1") {
            first = true;
          }
          if(e.data="2") {
            second = true;
          }
          if(e.data="3") {
            third = true;
          }
          flag = second;
        };
        eventSource.onopen = function() {
          send("eventsource-onmessage?message=%EF%BB%BF%EF%BB%BFdata%3A1%0A%0Adata%3A2%0A%0Adata%3A3");
        };
        waitsFor(function() {
            return flag;
        }, "The call is done", 10000);
        runs(function() {
            expect(eventSource.readyState).toBe(EventSource.OPEN);
            eventSource.close();
        });
    });

    it("Test data before empty line", function() {
        var data, flag, success;
        var first, second = false;
        flag = false;
        eventSource = new EventSource(baseURL + "stream");
        var count = 0;
        eventSource.onmessage = function(e) {
          if (count === 0) {
            if(e.lastEventId == "" && e.data == "test1") {
              first=true;
            }
            count++;
          } else if (count === 1) {
            if(e.lastEventId == "test" && e.data == "test2") {
              second=true;
            }
            flag = first && second;
          }
        };
        eventSource.onopen = function() {
          send("eventsource-onmessage?message=" + encodeURIComponent("message:1000\ndata:test1\n\nid:test\ndata:test2\n\n") + "&newline=none");
        };
        waitsFor(function() {
            return flag;
        }, "The call is done", 10000);
        runs(function() {
            expect(eventSource.readyState).toBe(EventSource.OPEN);
            eventSource.close();
        });
    });
    
    it("Test field data", function() {
        var data, flag;
        var first, second, third = false;
        flag = false;
        eventSource = new EventSource(baseURL + "stream");
        eventSource.onmessage = function(e) {
          if("" == e.data) {
            first = true;
          } else if("\n" == e.data) {
            second = true;
          } else if("test" == e.data) {
            third =true;
          }
          flag = first && second && third;
        };
        eventSource.onopen = function() {
          send("eventsource-onmessage?message=data%3A%0A%0Adata%0Adata%0A%0Adata%3Atest");
        };
        waitsFor(function() {
            return flag;
        }, "The call is done", 10000);
        runs(function() {
            expect(eventSource.readyState).toBe(EventSource.OPEN);
            eventSource.close();
        });
    });
    
    
    

    it("Test retry 2s or bogus retry", function() {
        var data, flag;
        var first, second, third = false;
        var opened =0;
        var error = false;
        flag = false;
        eventSource = new EventSource(baseURL + "stream");
        eventSource.onerror = function() {
            error = true;
        };
        eventSource.onopen = function() {
          if(opened == 0) {
            send("eventsource-onmessage?message=retry%3A2000%0Aretry%3A500x%0Adata%3Ax");
            opened = new Date().getTime()
          } else {
            var diff = (new Date().getTime()) - opened;
            flag = (Math.abs(2 - diff / 1000) < 1); // allow 1s difference
          }
        };
        waitsFor(function() {
            return flag;
        }, "The call is done", 10000);
        runs(function() {
            expect(error).toBeTruthy();
            expect(eventSource.readyState).toBe(EventSource.OPEN);
            eventSource.close();
        });
    });
    
    it("Test format comment", function() {
        var data, flag;
        var first, second, third = false;
        flag = false;
        eventSource = new EventSource(baseURL + "stream");
        eventSource.onmessage = function(e) {
          if(e.data="1\n2\n3\n4") {
            flag=true;
          }
        };
        eventSource.onopen = function() {
          var longstring = (new Array(2*1024+1)).join("x"); // cannot make the string too long; causes timeout
          message = encodeURI("data:1\r:\0\n:\r\ndata:2\n:" + longstring + "\rdata:3\n:data:fail\r:" + longstring + "\ndata:4\n\n");
          send("eventsource-onmessage?message=" + message + "&newline=none");
        };
        waitsFor(function() {
            return flag;
        }, "The call is done", 10000);
        runs(function() {
            expect(eventSource.readyState).toBe(EventSource.OPEN);
            eventSource.close();
        });
    });

    it("Test format new line", function() {
        var data, flag;
        var first, second, third = false;
        flag = false;
        eventSource = new EventSource(baseURL + "stream");
        eventSource.onmessage = function(e) {
          if("test\n\ntest" == e.data) {
            flag = true;
          }
        };
        eventSource.onopen = function() {
          send("eventsource-onmessage?message=data%3Atest%0D%0Adata%0Adata%3Atest%0D%0A%0D&newline=none");
        };
        waitsFor(function() {
            return flag;
        }, "The call is done", 10000);
        runs(function() {
            expect(eventSource.readyState).toBe(EventSource.OPEN);
            eventSource.close();
        });
    });

    it("Test disconnection put ready state to connecting", function() {
        var data, flag;
        flag = false;
        eventSource = new EventSource(baseURL + "stream");
        eventSource.onerror = function() {
            data = eventSource.readyState;
            flag = true;
        };
        
        eventSource.onopen = function() {
            send("eventsource-disconnect");
        };

        waitsFor(function() {
            return flag;
        }, "The call is done", 10000);

        runs(function() {
            expect(data).toBe(EventSource.CONNECTING);
            eventSource.close();
        });
    });
    
    it("Test reconnection ", function() {
        var data, flag;
        var called = false;
        flag = false;
        eventSource = new EventSource(baseURL + "stream");
        eventSource.onopen = function() {
            if (!called) {
              send("eventsource-disconnect");
              called = true;
            } else {
              flag = true;
            }
        };
        waitsFor(function() {
            return flag;
        }, "The call is done", 10000);
        runs(function() {
            expect(eventSource.readyState).toBe(EventSource.OPEN);
            eventSource.close();
        });
    });
    
    it("Test reconnection can receive message", function() {
        var data, flag;
        var called = false;
        flag = false;
        eventSource = new EventSource(baseURL + "stream");
        eventSource.onopen = function() {
            if (!called) {
              send("eventsource-disconnect");
              called = true;
            } else {
              send("eventsource-onmessage");
            }
        };
        eventSource.onmessage = function(e) {
            flag = true;
        };
        waitsFor(function() {
            return flag;
        }, "The call is done", 10000);
        runs(function() {
            expect(eventSource.readyState).toBe(EventSource.OPEN);
            eventSource.close();
        });
    });

    it("Test event sent to open", function() {
        var flag = false;
        eventSource = new EventSource(baseURL + "stream");
        eventSource.onopen = function(e) {
            expect(eventSource.readyState).toBe(EventSource.OPEN);
            expect(e.hasOwnProperty('data')).toBeFalsy();
            expect(e.bubbles).toBeFalsy();
            expect(e.cancelable).toBeFalsy();
            flag = true;
            send("eventsource-onmessage");
        };
        waitsFor(function() {
            return flag;
        }, "The call is done", 10000);
        runs(function() {
            eventSource.close();
        });
    });

    it("Test field id", function() {
        var data, flag1, flag2, unreached, seenhello;
        flag1 = false;
        flag2 = false;
        unreached = false;
        seenhello = false;
        eventSource = new EventSource(baseURL + "stream");
        eventSource.addEventListener("message", function(e) {
            if(e.data == "hello" && !seenhello) {
              seenhello = true;
              expect(e.lastEventId).toBe("…");
              flag1 = true;
            } else if(seenhello) {
              expect("data").toBe(e.data)
              expect("…").toBe(e.lastEventId);
              flag2 = true;
            } else {
                unreached = true;
            }}, false);
        eventSource.onopen = function() {
            var message = encodeURIComponent("id: …\ndata: hello\n\n") + "&newline=none";
            send("eventsource-onmessage?message=" + message);
            send("eventsource-onmessage");
        };
        waitsFor(function() {
            return flag1 && flag2 && !unreached;
        }, "The call is done", 10000);
        runs(function() {
            eventSource.close();
        });
    });
    
    it("Test field id 2", function() {
        var data, flag, unreached, counter;
        flag = false;
        unreached = false;
        counter = 0;
        eventSource = new EventSource(baseURL + "stream"); // mettre ici
        eventSource.addEventListener("message", function(e) {
            if(e.data == "hello" && counter == 0) {
              counter++
              expect(e.lastEventId).toBe("…");
            } else if(counter == 1) {
              counter++
              expect("data").toBe(e.data);
              expect("…").toBe(e.lastEventId);
            } else if(counter == 2) {
              counter++
              expect("data").toBe(e.data);
              expect("…").toBe(e.lastEventId);
              flag = true;
            } else {
                unreached = true;
            }}, false);
        eventSource.onopen = function() {
            var message = encodeURIComponent("id: …\ndata: hello\n\n") + "&newline=none";
            send("eventsource-onmessage?message=" + message);
            send("eventsource-onmessage");
            send("eventsource-onmessage");
        };
        waitsFor(function() {
            return flag && !unreached;
        }, "The call is done", 10000);
        runs(function() {
            eventSource.close();
        });
    });
    
    it("Test event listener new event type ", function() {
        var test, data, flag1, flag2;
        flag1 = false;
        flag2 = false;
        eventSource = new EventSource(baseURL + "stream");
        eventSource.addEventListener("test", function(e) {
            test = e.data;
            flag1 = true;
            eventSource.close();
        }, false);
        
        eventSource.onmessage = function(e) {
            data = e.data;
            flag2 = true;
        };
        
        eventSource.onopen = function() {
            send("eventsource-onmessage?message=event%3Atest%0Adata%3Ax%0A%0Adata%3Ay");
        };
        waitsFor(function() {
            return flag1 && flag2;
        }, "The call is done", 10000);
        runs(function() {
            expect(test).toBe("x");
            expect(data).toBe("y");
            eventSource.close();
        });
    });
    
    it("Test event listener event type empty", function() {
        var data, flag;
        flag = false;
        eventSource = new EventSource(baseURL + "stream");
        eventSource.onmessage = function(e) {
            data = e.data;
            flag = true;
        };
        eventSource.onopen = function() {
            send("eventsource-onmessage?message=event%3A%20%0Adata%3Adata");
        };
        waitsFor(function() {
            return flag;
        }, "The call is done", 10000);
        runs(function() {
            expect(data).toBe("data");
            eventSource.close();
        });
    });
    
    it("Test field parsing ", function() {
        var data, flag;
        var called = false;
        flag = false;
        eventSource = new EventSource(baseURL + "stream");
        eventSource.onmessage = function(e) {
            data = e.data;
            flag = true;
        };
        eventSource.onopen = function() {
            var message = encodeURI("data:\0\ndata:  2\rData:1\ndata\0:2\ndata:1\r\0data:4\nda-ta:3\rdata_5\ndata:3\rdata:\r\n data:32\ndata:4");
            send("eventsource-onmessage?message=" + message);
        };
        waitsFor(function() {
            return flag;
        }, "The call is done", 10000);
        runs(function() {
            expect(data).toBe("\0\n 2\n1\n3\n\n4");
            eventSource.close();
        });
    });
    
    it("Test format null character", function() {
        var data, flag;
        var first, second, third = false;
        flag = false;
        eventSource = new EventSource(baseURL + "stream");
        eventSource.onmessage = function(e) {
          if("\x00" == e.data) {
            flag = true;
          }
        };
        eventSource.onopen = function() {
          send("eventsource-onmessage?message=data%3A%00%0A%0A");
        };
        waitsFor(function() {
            return flag;
        }, "The call is done", 10000);
        runs(function() {
            expect(eventSource.readyState).toBe(EventSource.OPEN);
            eventSource.close();
        });
    });
/*
    it("Test redirect 301", function() {
        var flag= false;
        eventSource = new EventSource(baseURL + "eventsource-redirect?status=301");
        eventSource.onopen = function(e) {
            flag = true;
        };
        eventSource.onerror = function(e) {
            flag = true;
        };
        waitsFor(function() {
            return flag;
        }, "The call is done", 10000);
        runs(function() {
            expect(eventSource.readyState).toBe(EventSource.OPEN);
            eventSource.close();
        });
    });
*/
});