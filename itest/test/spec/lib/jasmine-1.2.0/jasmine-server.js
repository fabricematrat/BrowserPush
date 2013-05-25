    var ServerReporter = function (url) {
        this.serviceUrl = url;
        this.started = false;
        this.finished = false;

        //results helpers
        this.results = {};
        this.resultsCnt = 0;
        this.passedCnt = 0;
        this.failedCnt = 0;
        this.startTime = null;
        this.endTime = null;
    };

    ServerReporter.prototype = {
        reportRunnerStarting: function (runner) {
            this.startTime = (new Date()).getTime();
        },
        reportSpecStarting: function (spec) {
            //called once a spec is reported
            var data = {test: "START: " + spec.description};
            this.sendTestResults(data, "/testStart");
        },
        reportSpecResults: function (spec) {
            var data = {test: spec.description + " started"};
            var results = spec.results();
            if (results.passed()) {
                this.sendTestResults({test: "PASSED: " + spec.description}, "/testEnd");
            } else {
                var failure = "";
                var failures = 0;
                var resultItems = results.getItems();
                for (var i = 0; i < resultItems.length; i++) {
                    var result = resultItems[i];
                    if (result.type == 'expect' && result.passed && !result.passed()) {
                        failures += 1;
                        failure += (failures + ": " + result.message + " ");
                    }
                }
                this.sendTestResults({test: "FAILED: " + spec.description, failure: failure}, "/testEnd");

            }
            this.reportSpec(spec);
        },
        reportSuiteResults: function (suite) {
            //called for the suite results last of suite
        },

        reportRunnerResults: function (runner) {
            //last called for a detailed summary last of entire lifecycle
            this.endTime = (new Date()).getTime();
            this.summarize(runner);
            console.debug("reportRunnerResults");
            this.sendTestResults({results: this.results}, "/results");
        },

        reportSpec: function (spec) {
            var suite = spec.suite;
            this.results.suites = this.results.suites || {};
            var resultsSuite = this.results.suites[suite.id + ""];
            if (!resultsSuite) {
                resultsSuite = this.results.suites[suite.id + ""] = {};
                resultsSuite.description = suite.description;
                resultsSuite.failed = false;
                resultsSuite.specs = [];
            }
            var resultsSpec = {};
            resultsSpec.description = spec.description;
            resultsSpec.failed = !spec.results().passed();
            if (spec.results().passed()) {
                this.passedCnt++;
            } else {
                resultsSuite.failed = true;
                this.failedCnt++;
            }
            resultsSuite.specs.push(resultsSpec);
            this.resultsCnt++;
        },
        summarize: function () {
            var statistics = this.results.statistics = {};
            var href = window.location.href;
            //we cut off params we want only the href
            if (href.indexOf("?") != -1) {
                href = href.substr(0, href.indexOf("?"));
            }
            statistics.origin = href;
            statistics.numberOfTests = this.resultsCnt;
            statistics.numberOfFails = this.failedCnt;
            statistics.numberOfPassed = this.passedCnt;
            statistics.executionTime = this.endTime - this.startTime;
        },

        sendTestResults: function (data, path) {
            var xhr = new XMLHttpRequest();
            xhr.open("post", this.serviceUrl + path, true);
            xhr.setRequestHeader("Content-Type","application/json");
            xhr.send(JSON.stringify(data));
        }
    };
