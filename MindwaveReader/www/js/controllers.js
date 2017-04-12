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

.controller('HomeCtrl', function($scope, $rootScope, $cordovaFile, $cordovaEmailComposer) {
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
            //          var deferred = $q.defer();
            window.resolveLocalFileSystemURL(cordova.file.externalDataDirectory + "/naveen",
                function(fileSystem) {
                    var reader = fileSystem.createReader();
                    reader.readEntries(
                        function(entries) {
                            var total_dirs = 0;
                            var curr_count = 0;
                            for (k = 0; k < entries.length; k++) {
                                if (entries[k].name.includes(month_year)) {
                                    total_dirs++;
                                }
                            }
                            var filenames = [];
                            for (j = 0; j < entries.length; j++) {
                                if (entries[j].isDirectory) {
                                    curr_count++;
                                    (function(j, curr_count, filenames) {
                                        window.resolveLocalFileSystemURL(cordova.file.externalDataDirectory + "/naveen/" + entries[j].name,
                                            function(innerfileSystem) {
                                                var readers = innerfileSystem.createReader();
                                                readers.readEntries(
                                                    function(files) {
                                                        for (i = 0; i < files.length; i++) {
                                                            if (files[i].isFile) {
                                                                //filenames.push(cordova.file.externalDataDirectory + "/naveen/" + entries[j].name + "/" + files[i].name);
                                                                (function(i, j, filenames) {
                                                                    $cordovaFile.readAsText(cordova.file.externalDataDirectory + "/naveen/" + entries[j].name, files[i].name)
                                                                        .then(function(success) {
                                                                            (function(filenames) {
                                                                                var str = success.split(" ");
                                                                                var temp = 0;
                                                                                for (x = 0; x < str.length; x++) {
                                                                                    temp = temp + parseFloat(str[x]);
                                                                                }
                                                                                var temp = temp / 10000;
                                                                                //console.log("Filenamesare :" + files[i].name + temp);
                                                                                filenames.push(files[i].name + " " + temp);
                                                                                if (curr_count == total_dirs && i == files.length - 1) {
                                                                                    filenames.sort();
                                                                                    var msg = "";
                                                                                    for (n = 0; n < filenames.length; n++) {
                                                                                        msg = msg + filenames[n] + "<br/>";
                                                                                    }
                                                                                    $scope.avgdiv = msg;
                                                                                }
                                                                            }(filenames));
                                                                        }, function(error) {
                                                                            console.log(error);
                                                                        });
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
                                    }(j, curr_count, filenames));
                                }
                            }
                        },
                        function(err) {
                            console.log(err);
                        }
                    );
                },
                function(err) {
                    //                deferred.reject();
                    console.log(err);
                }
            );

            /*            deferred.promise.then(function(data) {
                            console.log("naveendaka::: " + data);
                        }, function(error) {
                            console.error(data);
                        })*/

        },
        function(err) {
            console.log(err);
        }


    //defer promise example
    $scope.synch = function() {
            var helloPromise = sayHelloAsync('naveen');
            helloPromise.then(function(data) {
                console.log("naveendaka: " + data);
            }, function(error) {
                console.error(data);
            })
        },
        function(err) {
            console.log(err);
        }

    $scope.sayHelloAsync = function(name) {
            return function() {
                var defer = $q.defer()
                setTimeout(function() {
                    if (name == 'naveen') {
                        defer.resolve('Hello, ' + name + '!');
                    } else {
                        defer.reject('Greeting ' + name + ' is not allowed.');
                    }
                }, 1000);

                return defer.promise
            }
        },
        function(err) {
            console.log(err);
        }

})



.controller('ItemlistCtrl', function($scope, $stateParams) {});