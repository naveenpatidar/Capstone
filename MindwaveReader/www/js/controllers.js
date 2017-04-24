angular.module('starter.controllers', [])

.controller('AppCtrl', function($scope, $ionicModal, $timeout) {

    /*    // With the new view caching in Ionic, Controllers are only called
        // when they are recreated or on app start, instead of every page change.
        // To listen for when this page is active (for example, to refresh data),
        // listen for the $ionicView.enter event:
        //$scope.$on('$ionicView.enter', function(e) {
        //});

        // Form data for the login modal
        $scope.loginData = {};

        // Create the login modal that we will use later
        $ionicModal.fromTemplateUrl('templates/login.html', {
            scope: $scope
        }).then(function(modal) {
            $scope.modal = modal;
        });

        // Triggered in the login modal to close it
        $scope.closeLogin = function() {
            $scope.modal.hide();
        };

        // Open the login modal
        $scope.login = function() {
            $scope.modal.show();
        };

        // Perform the login action when the user submits the login form
        $scope.doLogin = function() {
            console.log('Doing login', $scope.loginData);

            // Simulate a login delay. Remove this and replace with your login
            // code if using a login system
            $timeout(function() {
                $scope.closeLogin();
            }, 1000);
        };*/
})

.controller('HomeCtrl', function($scope, $rootScope, $cordovaFile, $cordovaEmailComposer, $q) {
    /*    $scope.deleteAllFile = function() {
                window.resolveLocalFileSystemURL(cordova.file.externalDataDirectory,
                    function(fileSystem) {
                        fileSystem.getDirectory("naveen", {
                                create: true,
                                exclusive: false
                            },
                            function(entry) {
                                entry.removeRecursively(function() {
                                    console.log("Remove Recursively Succeeded");
                                    $scope.files = [];
                                    $scope.selectdiv = "";
                                    $scope.$apply();
                                    $cordovaFile.createDir(cordova.file.externalDataDirectory, "naveen", true)
                                        .then(function(success) {
                                            // success
                                        }, function(error) {
                                            // error
                                        });

                                }, function(err) {
                                    console.log(err);
                                });
                            },
                            function(err) {
                                console.log(err);
                            });
                    },
                    function(err) {
                        console.log(err);
                    }
                );
            },
            function(err) {
                console.log("Directory outside" + err);
            }
    */

    $rootScope.showFile = function(_date) {
            $scope.files = [];
            $scope.selectdiv = "";
            $scope.avgdiv = "";
            window.resolveLocalFileSystemURL(cordova.file.externalDataDirectory + "/naveen/" + _date,
                function(fileSystem) {
                    var reader = fileSystem.createReader();
                    reader.readEntries(
                        function(entries) {
                            console.log(entries.length);
                            for (i = 0; i < entries.length; i++) {
                                if (entries[i].isFile) {
                                    console.log(entries[i].name);
                                    //fname = fname + entries[i].name;
                                    //if (entries[i].name.includes(_date)) {
                                    $scope.files.push({
                                        name: entries[i].name
                                    });
                                    //}
                                }
                            }
                            $scope.selectdiv = "";
                            $scope.$apply();
                            $scope.showAvg(_date);
                        },
                        function(err) {
                            console.log(err);
                        }
                    );
                },
                function(err) {
                    console.log(err);
                }
            );
        },
        function(err) {
            console.log(err);
        }


    $scope.readFileAtGivenPath = function(textfilename) {
            var dir = textfilename.split(" ")[0];
            $cordovaFile.readAsText(cordova.file.externalDataDirectory + "/naveen/" + dir, textfilename)
                .then(function(success) {
                    var str = success.split(" ");
                    var temp = 0;
                    for (i = 0; i < str.length; i++) {
                        temp = temp + parseFloat(str[i]);
                    }
                    $scope.selectdiv = temp / 10000;
                }, function(error) {
                    // error
                });
        },
        function(err) {
            console.log(err);
        }


    $scope.showAvg = function(_date) {
            $scope.avgdiv = "";
            $scope.$apply();
            window.resolveLocalFileSystemURL(cordova.file.externalDataDirectory + "/naveen/" + _date,
                function(fileSystem) {
                    var reader = fileSystem.createReader();
                    reader.readEntries(
                        function(entries) {
                            var functionCalls = [];
                            for (i = 0; i < entries.length; i++) {
                                if (entries[i].isFile) {
                                    functionCalls.push((function(i) {
                                        var defer = $q.defer();
                                        $cordovaFile.readAsText(cordova.file.externalDataDirectory + "/naveen/" + _date, entries[i].name)
                                            .then(function(success) {
                                                var str = success.split(" ");
                                                var temp = 0;
                                                for (j = 0; j < str.length; j++) {
                                                    temp = temp + parseFloat(str[j]);
                                                }
                                                defer.resolve(temp / 10000);
                                            }, function(error) {
                                                console.log(error);
                                                defer.reject('file can not be read');
                                            });
                                        return defer.promise;
                                    }(i)));
                                }
                            }

                            setTimeout(function() {
                                $q.all(functionCalls).then(function(data) {
                                        var avg = 0;
                                        for (n = 0; n < data.length; n++) {
                                            avg = avg + parseFloat(data[n]);
                                        }
                                        $scope.avgdiv = (avg / data.length).toFixed(5);
                                    },
                                    function(error) {
                                        console.log(error);
                                    })
                            }, 3);
                        },
                        function(err) {
                            console.log(err);
                        }
                    );
                },
                function(err) {
                    console.log(err);
                }
            );
        },
        function(err) {
            console.log(err);
        }


    //plot graph
    $scope.plotGraph = function() {
        var month_year = $rootScope.todays_date.substring(3, 11);
        window.resolveLocalFileSystemURL(cordova.file.externalDataDirectory + "/naveen",
            function(fileSystem) {
                var reader = fileSystem.createReader();
                reader.readEntries(
                    function(entries) {
                        var total_dirs = 0;
                        var curr_count = 0;
                        for (k = 0; k < entries.length; k++) {
                            if (entries[k].isDirectory && entries[k].name.includes(month_year)) {
                                total_dirs++;
                            }
                        }
                        var filenames = [];
                        var functionCalls = [];
                        for (j = 0; j < entries.length; j++) {
                            if (entries[j].isDirectory) {
                                curr_count++;
                                (function(j, curr_count, filenames) {
                                    $scope.readDayDirectory(entries, filenames, i, j, total_dirs, curr_count, functionCalls);
                                }(j, curr_count, filenames));
                            }
                        }

                        var pgcounter = 0;
                        var pgbar = setInterval(function() {
                            if (pgcounter++ > 2) {
                                $q.all(functionCalls).then(function(data) {
                                        var msg = "";
                                        data.sort();
                                        $scope.showMonthwiseAvg(data);
                                        console.log("naveensuccessfull " + data.length);
                                        for (n = 0; n < data.length; n++) {
                                            msg = msg + data[n] + "<br/>";
                                        }
                                        $scope.avgdiv = $scope.avgdiv + msg;
                                    },
                                    function(error) {
                                        console.log(error);
                                    })
                                clearInterval(pgbar);
                            }
                            /* else {
                                                            $scope.avgdiv = "please wait for " + (3 - pgcounter) + "seconds";
                                                            $scope.$apply();
                                                        }*/
                        }, 2);
                    },
                    function(err) {
                        console.log(err);
                    }
                );
            },
            function(err) {
                console.log(err);
            }
        );
    }


    $scope.readDayDirectory = function(entries, filenames, i, j, total_dirs, curr_count, functionCalls) {
        window.resolveLocalFileSystemURL(cordova.file.externalDataDirectory + "/naveen/" + entries[j].name,
            function(innerfileSystem) {
                var readers = innerfileSystem.createReader();
                readers.readEntries(
                    function(files) {
                        for (i = 0; i < files.length; i++) {
                            if (files[i].isFile) {
                                (function(i, j, filenames) {
                                    functionCalls.push($scope.readSingleFile(entries, files, filenames, i, j, total_dirs, curr_count));
                                }(i, j, filenames));
                            }
                        }

                    },
                    function(err) {
                        console.log(err);
                    }
                );
            },
            function(err) {
                console.log(err);
            }
        );
    }


    $scope.readSingleFile = function(entries, files, filenames, i, j, total_dirs, curr_count) {
        var defer = $q.defer();
        $cordovaFile.readAsText(cordova.file.externalDataDirectory + "/naveen/" + entries[j].name, files[i].name)
            .then(function(success) {
                //defer.resolve('naveenHello, ' + name + '! : ' + j + i);
                (function(filenames) {
                    var str = success.split(" ");
                    var temp = 0;
                    for (x = 0; x < str.length; x++) {
                        temp = temp + parseFloat(str[x]);
                    }
                    var temp = temp / 10000;
                    filenames.push(files[i].name + " " + temp);
                    defer.resolve(files[i].name + " " + temp);
                    /*if (curr_count == total_dirs && i == files.length - 1) {
                        filenames.sort();
                        var msg = "";
                        for (n = 0; n < filenames.length; n++) {
                            msg = msg + filenames[n] + "<br/>";
                        }
                        $scope.avgdiv = msg;
                    }*/
                }(filenames));
            }, function(error) {
                console.log(error);
                defer.reject('naveenGreeting ' + name + ' is not allowed.');
            });

        return defer.promise;
    }

    $scope.showMonthwiseAvg = function(data) {
        var keyset = [];
        for (i = 0; i < data.length; i++) {
            var key = data[i].split(" ")[0];
            if (!keyset.includes(key)) {
                keyset.push(key);
            }
        }
        var msg = "";
        var x = 0;
        for (i = 0; i < keyset.length; i++) {
            var avg = 0;
            var counter = 0;
            for (; x < data.length; x++) {
                if (data[x].includes(keyset[i])) {
                    avg = avg + parseFloat(data[x].split(" ")[2]);
                    counter++;
                } else {
                    break;
                }
            }
            msg = msg + keyset[i] + " " + (avg / counter).toFixed(5) + "<br/>";
        }
        $scope.avgdiv = msg;
    }
})


.controller('EmailCtrl', function($scope, $rootScope, $cordovaEmailComposer) {
    $scope.destEmail = 'naveen15038@iiitd.ac.in';
    $scope.emailSubject = 'Sensor Data File';
    $scope.emailBody = 'Files attached till ' + $rootScope.todays_date;
    // Email File function
    $scope.emailFile = function() {
            window.resolveLocalFileSystemURL(cordova.file.externalDataDirectory + "/naveen",
                function(fileSystem) {
                    var reader = fileSystem.createReader();
                    reader.readEntries(
                        function(entries) {
                            var filenames = [];
                            for (j = 0; j < entries.length; j++) {
                                if (entries[j].isDirectory) {
                                    (function(j) {
                                        window.resolveLocalFileSystemURL(cordova.file.externalDataDirectory + "/naveen/" + entries[j].name,
                                            function(innerfileSystem) {
                                                var readers = innerfileSystem.createReader();
                                                readers.readEntries(
                                                    function(files) {
                                                        for (i = 0; i < files.length; i++) {
                                                            if (files[i].isFile) {
                                                                filenames.push(cordova.file.externalDataDirectory + "/naveen/" + entries[j].name + "/" + files[i].name);
                                                            }
                                                            (function(filenames) {
                                                                if (j == entries.length - 1) {
                                                                    // code for sending email with attachments
                                                                    $cordovaEmailComposer.isAvailable().then(function() {
                                                                        // is available
                                                                    }, function() {
                                                                        // not available
                                                                    });

                                                                    var email = {
                                                                        to: $scope.destEmail,
                                                                        attachments: filenames,
                                                                        subject: $scope.emailSubject,
                                                                        body: $scope.emailBody,
                                                                        isHtml: true
                                                                    };

                                                                    $cordovaEmailComposer.open(email).then(null, function() {
                                                                        // user cancelled email
                                                                    });
                                                                }
                                                            }(filenames));
                                                        }
                                                    },
                                                    function(err) {
                                                        console.log(err);
                                                    }
                                                );
                                            },
                                            function(err) {
                                                console.log(err);
                                            }
                                        );
                                    }(j));
                                }
                            }
                        },
                        function(err) {
                            console.log(err);
                        }
                    );
                },
                function(err) {
                    console.log(err);
                }
            );
        },
        function(err) {
            console.log(err);
        }
})

.controller('LoginCtrl', function($scope, $state, $ionicHistory, $rootScope) {
    /*var name = localStorage.getItem("name"); 
       if (name != null || name != "") {
            $state.go('app.home');
        }*/

    $scope.login = function() {
        window.plugins.googleplus.login({
                'scopes': '', // optional, space-separated list of scopes, If not included or empty, defaults to `profile` and `email`.
                //'webClientId': 'client id of the web app/server side', // optional clientId of your Web application from Credentials settings of your project - On Android, this MUST be included to get an idToken. On iOS, it is not required.
                'offline': false, // optional, but requires the webClientId - if set to true the plugin will also return a serverAuthCode, which can be used to grant offline access to a non-Google server
            },
            function(obj) {
                /*localStorage.setItem("name", obj.displayName);
                localStorage.setItem("imageUrl", obj.imageUrl);*/
                $rootScope._name = obj.displayName;
                $rootScope._imgUrl = obj.imageUrl;
                $ionicHistory.nextViewOptions({
                    disableBack: true
                });
                $state.go('app.home');
            },
            function(msg) {
                alert('error: ' + msg);
            }
        );
    }
})

.controller('ProfileCtrl', function($scope, $state, $ionicHistory, $rootScope) {
    /*$scope.name = localStorage.getItem("name");
    $scope.imgurl = localStorage.getItem("imageUrl");*/
    $scope.name = $rootScope._name;
    $scope.imgurl = $rootScope._imgUrl;
    $scope.logout = function() {
        window.plugins.googleplus.logout(
            function(msg) {
                alert(msg);
                $scope.$apply(function() {
                    /*                    localStorage.removeItem("name");
                                        localStorage.removeItem("imageUrl");*/
                    $rootScope._name = "";
                    $rootScope._imgUrl = "";
                    $scope.name = "";
                    $scope.imgurl = "";
                });
                $ionicHistory.nextViewOptions({
                    disableBack: true
                });
                $state.go('app.login');
            }
        );
    }
});