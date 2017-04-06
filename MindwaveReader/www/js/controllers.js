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

            // var filenames = "";
            // // for (i = 0; i < 10; i++) {
            // var name = "Naveen" + i + ".txt";
            // console.log(name);
            // $cordovaFile.checkFile(cordova.file.externalDataDirectory, name)
            //     .then(function(success) {
            //         filenames = filenames + name;
            //         console.log(filenames + "Patidar");
            //         $scope.selectdiv = filenames;
            //     }, function(error) {
            //         // error
            //     });
            // // }

            // // $scope.selectdiv = filenames;
            //console.log("Show file" + _date);
            //$scope.showFileName = _date;
            window.resolveLocalFileSystemURL(cordova.file.externalDataDirectory + "/naveen",
                function(fileSystem) {
                    var reader = fileSystem.createReader();
                    reader.readEntries(
                        function(entries) {
                            console.log(entries.length);
                            //var fname = "";
                            $scope.files = [];
                            for (i = 0; i < entries.length; i++) {
                                if (entries[i].isFile) {
                                    console.log(entries[i].name);
                                    //fname = fname + entries[i].name;
                                    if (entries[i].name.includes(_date)) {
                                        $scope.files.push({
                                            name: entries[i].name
                                        });
                                    }

                                }

                                $scope.selectdiv = "";
                                $scope.$apply();
                            }
                            $scope.showAvg(_date);
                            /*if ("06-Apr-2017 10-12-34-234".includes(_date)) {
                                $scope.showAvg();
                            } else {
                                $scope.avgdiv = "";
                                $scope.$apply();
                            }*/

                            // $scope.selectdiv = fname;
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
            $cordovaFile.readAsText(cordova.file.externalDataDirectory + "/naveen", textfilename)
                .then(function(success) {
                    var str = success.split(" ");
                    var temp = 0;
                    for (i = 0; i < str.length; i++) {
                        /*temp = temp + str[i] + "<br/>";*/
                        temp = temp + parseFloat(str[i]);
                    }
                    /*$scope.selectdiv = temp;*/
                    $scope.selectdiv = temp / 10000;
                    // $scope.selectdiv = success;
                    //console.log(success);
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
            window.resolveLocalFileSystemURL(cordova.file.externalDataDirectory + "/naveen",
                function(fileSystem) {
                    var reader = fileSystem.createReader();
                    reader.readEntries(
                        function(entries) {
                            var fileavg = [];
                            for (i = 0; i < entries.length; i++) {
                                if (entries[i].isFile && entries[i].name.includes(_date)) {
                                    (function(i) {
                                        $cordovaFile.readAsText(cordova.file.externalDataDirectory + "/naveen", entries[i].name)
                                            .then(function(success) {
                                                var str = success.split(" ");
                                                var temp = 0;
                                                for (j = 0; j < str.length; j++) {
                                                    temp = temp + parseFloat(str[j]);
                                                }
                                                fileavg.push(temp / 10000);
                                                (function(fileavg) {
                                                    if (i == entries.length - 1) {
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
                            for (i = 0; i < entries.length; i++) {
                                if (entries[i].isFile) {
                                    filenames.push(cordova.file.externalDataDirectory + "/naveen/" + entries[i].name);
                                }
                            }

                            // code for sending email with attachments
                            $cordovaEmailComposer.isAvailable().then(function() {
                                // is available
                            }, function() {
                                // not available
                            });

                            //var file1 = cordova.file.externalDataDirectory + "/naveen/Naveen0.txt";
                            var email = {
                                to: 'naveen15038@iiitd.ac.in',
                                /*attachments: [
                                    file1, file1
                                ],*/
                                attachments: filenames,
                                subject: 'Sensor Data File',
                                body: 'Files are attached below till today.',
                                isHtml: true
                            };

                            $cordovaEmailComposer.open(email).then(null, function() {
                                // user cancelled email
                            });
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

    /*
        //copy file to external storage
        $scope.copyFiles = function() {
                $cordovaFile.copyDir(cordova.file.externalDataDirectory, "naveen", cordova.file.externalDataDirectory, "naveen")
                    .then(function(success) {
                        console.log("Successfully copied");
                        $scope.emailFile();
                    }, function(error) {
                        // error
                    });

            },
            function(err) {
                console.log(err);
            }

        $scope.makeDire = function() {
                $cordovaFile.createDir(cordova.file.externalDataDirectory, "naveen", false)
                    .then(function(success) {
                        $cordovaFile.createFile(cordova.file.externalDataDirectory + "/naveen", "Naveen0.txt", true)
                            .then(function(success) {
                                // success
                                $scope.emailFile();
                            }, function(error) {
                                // error
                            });

                    }, function(error) {
                        // error
                    });

            },
            function(err) {
                console.log(err);
            }
    */

    /*-------------------------------------------------------------------------------------*/
})

.controller('ItemlistCtrl', function($scope, $stateParams) {});