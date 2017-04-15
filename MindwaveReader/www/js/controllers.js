angular.module('starter.controllers', [])

.controller('AppCtrl', function($scope, $ionicModal, $timeout) {

    // With the new view caching in Ionic, Controllers are only called
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
    };
})

.controller('HomeCtrl', function($scope, $rootScope, $cordovaFile, $cordovaEmailComposer, $q) {
    $scope.deleteAllFile = function() {
            /*   console.log("Directory inside");
               $cordovaFile.removeRecursively(cordova.file.externalDataDirectory, "")
                   .then(function(success) {
                       // success
                       console.log("Directory removed..");
                   }, function(error) {
                       // error
                       console.log(error);
                       console.log("Directory not removed.." + error);
                   });*/


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
                            var fileavg = [];
                            for (i = 0; i < entries.length; i++) {
                                if (entries[i].isFile) {
                                    (function(i) {
                                        $cordovaFile.readAsText(cordova.file.externalDataDirectory + "/naveen/" + _date, entries[i].name)
                                            .then(function(success) {
                                                //if (entries[i].name.includes(_date)) {
                                                var str = success.split(" ");
                                                var temp = 0;
                                                for (j = 0; j < str.length; j++) {
                                                    temp = temp + parseFloat(str[j]);
                                                }
                                                fileavg.push(temp / 10000);
                                                //}
                                                (function(fileavg) {
                                                    if (i == entries.length - 1) {
                                                        // console.log("fileavg : inside if");
                                                        var avg = 0;
                                                        for (l = 0; l < fileavg.length; l++) {
                                                            avg = avg + fileavg[l];
                                                        }
                                                        if (fileavg.length != 0) {
                                                            avg = avg / fileavg.length;
                                                        }
                                                        $scope.avgdiv = avg;
                                                        $scope.$apply();

                                                    }
                                                }(fileavg));

                                            }, function(error) {
                                                console.log(error);
                                            });
                                    }(i));
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
                                                                        to: 'naveen15038@iiitd.ac.in',
                                                                        attachments: filenames,
                                                                        subject: 'Sensor Data File',
                                                                        body: 'Files are attached below till today.',
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
                                        console.log("naveensuccessfull " + data.length);
                                        for (n = 0; n < data.length; n++) {
                                            msg = msg + data[n] + "<br/>";
                                        }
                                        $scope.avgdiv = msg;
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
                        }, 1);
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


    /*    $scope.sayHelloAsync = function(name, i) {
            var defer = $q.defer();
            setTimeout(function() {
                //Greet when your name is 'naveen'
                if (name == 'naveen') {
                    defer.resolve('naveenHello, ' + name + '! : ' + i);
                } else {
                    defer.reject('naveenGreeting ' + name + ' is not allowed.');
                }
            }, i);
            return defer.promise
        }

        $scope.click = function() {
            var helloPromise = $scope.sayHelloAsync('naveen', 2000);
            var helloPromise1 = $scope.sayHelloAsync('naveen', 1000);
            var pros = [];
            pros.push(helloPromise);
            pros.push(helloPromise1);
            $q.all(pros)
                .then(function(data) {
                        console.log("naveen" + data[0]);
                    },
                    function(error) {
                        console.error("naveen" + data);
                    })
        }*/
})



.controller('ItemlistCtrl', function($scope, $stateParams) {});