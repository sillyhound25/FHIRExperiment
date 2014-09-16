//controller for clinical connectathon...
/*
Server dependencies (expected to be in the same domain):
 fhir/Patient?name=?        - search for a patient  (this is a proxy to another FHIR server) (fhir.PatientResourceProvider)
 cc/profilelist             - returns a collection of the profiles that are available  (cc.GetProfilesListServlet)

*/


var myApp = angular.module('myApp',['ngSanitize']);
//angular.module('myApp',['ngSanitize']);

//this is the patient service
angular.module('myApp').service('patientService', function($http) {
    var serviceInstance = {}; // Our first service return serviceInstance;

    var runUserRequest = function(uri) { // Return the promise from the $http service // that calls the Github API using JSONP
    return $http({
        method: 'GET',
            url: uri
    })};

    return {
        getUser: function (userId) {
            var uri = "fhir/Practitioner/" + userId + "?_format=json";
            //returns a promise...

            return runUserRequest(uri);
            /*
            runUserRequest(uri).success(function (data, status, headers, config) {
                return data;
            }).error(function (data, status, headers, config) {
                alert('there was an error executing the function');
                return null;
            });
            */
        }
    };


});



myApp.run(function($rootScope) {
    $rootScope.name = "David Hay";
});


//this is the main controller - currently at page level
myApp.controller('MyController', function($scope,$http ,patientService) {
    $scope.person = {
        name: "David hay controller"
    };

    //status holds many of the 'workflow' type functions. Probably should be re-factored...
    //status.loggedIn - true if there is a logged in user
    //status.profileSelected - true if a profile has been selected for data entry.
    //status.profileName - the name of the selected profile
    //status.resourceUrl - the url of the resource from grahames server...
    //status.user - user object (a FHIR person object)
    //status.patientSelected - true if a patient has been selected
    //status.searchPatient - true if a patient is being selected
    //status.user - a Practitioner resource
    //status.user.meta.userName - the name of the current user
    //status.user.meta.id - the userID
    //status.queryingServer - true when the client is waiting for the server to respond to a query

    $scope.status = {
        loggedIn : false,
        startUp : true,
        showHistory : true,
        showDataEntry : false   //hide the data entry parts of the app
    }

    //represents data collected
    $scope.data = {hx : []};    //hx is the list of

    //the data that is shown in the sidebar
    $scope.vitals = {condition:[],med:[],allergy:[]};

    //holds the history of changes to the allergy list
    $scope.history = {allergy : []};

    //get the list of profiles from the server
    $scope.getData = function() {
        $http({
            method: 'GET',
            url: 'cc/profilelist'
        }).success(function(data, status, headers, config) {
            console.log(data)
            $scope.profiles = data.profile;
        }).error(function(data, status, headers, config) {
        });
    }

    //when a single profile has been selected from the combo...
    $scope.profileSelected = function() {
        //console.log($scope.status.profileName);

        //alert($scope.status.profileName.name)
        $scope.status.resourceUrl = "http://fhir-dev.healthintersections.com.au/open/_web/Profile/"+$scope.status.profileName.name;

        console.log("url",$scope.status.resourceUrl);
        //todo I know this is wrong - will fix later...  for some reason, can't get to work with ng-src...

        $("#myIframe").attr("src",$scope.status.resourceUrl)

        $scope.status.profileSelected = true;
    }


    //when a profile narrative has been completed
    $scope.saveProfileData = function() {
        var newResource = {profile:$scope.status.profileName.name,narrative:$scope.data.narrative};

        newResource.userName = $scope.status.user.meta.userName;

        $scope.data.hx.push(newResource);
        checkNewProfile(newResource);       //some profiles have extra work...

        //disable the profile details
        $scope.status.profileSelected = false;
        $scope.status.profileName = "";
        $scope.data.narrative="";


    };

    //remove an allergy from the allergy list
    //when something is removed, we leave it in the list marked as deleted so we know who deleted it and why.
    //the next time the list is updated, the deleted item is properly removed...
    $scope.deleteAllergy = function(a,inx) {
        var deletedReason = prompt('Why do you wish to remove this allergy from the patients list of allergies','Incorrect');
        if (deletedReason){

            $scope.vitals.allergy.splice(inx,1);    //remove from the current list...
            var mostRecentList = $scope.history.allergy[$scope.history.allergy.length-1];    //clone the current list

            var newList = [];
            angular.forEach(mostRecentList.list,function(obj,inx1){

                if (inx1 === inx) {
                    //mark the one just deleted...
                    var newObj = angular.copy(obj);
                    newObj.deletedReason = deletedReason;
                    newObj.deleted = true;
                    newList.push(newObj);
                } else {
                    //if the allergy was previously deleted, then stop adding it...
                    console.log(obj.deletedReason);
                    if (! obj.deletedReason) {
                        // newList.push(obj);
                        newList.push(angular.copy(obj));
                    }

                }

            });

            $scope.history.allergy.push({userName:$scope.status.user.meta.userName,list:newList});

        };
    };

        //-----------------

    //}

    var checkNewProfile = function(vo) {
        var profileName = vo.profile;
        if (profileName.toLowerCase().indexOf("condition") > -1) {
            addResourceToConditionList(vo);
        }
        if (profileName.toLowerCase().indexOf("medication") > -1) {
            addResourceToMedList(vo);
        }
        if (profileName.toLowerCase().indexOf("allerg") > -1) {
            addResourceToAllergyList(vo);
        }

        //alert('check')
    }

    var addResourceToConditionList = function(vo){
        if (confirm("Do you wish to add this Condition to the patients problem list")) {
            $scope.vitals.condition.push(vo);
        }
    }

    var addResourceToMedList = function(vo){
        if (confirm("Do you wish to add this Medication to the patients list of medications")) {
            $scope.vitals.med.push(vo);
        }
    }

    var addResourceToAllergyList = function(vo){
        if (confirm("Do you wish to add this Allergy to the patients allergy list")) {
            $scope.vitals.allergy.push(vo);
            //now add to the list of allergies

            var mostRecentList = $scope.history.allergy[$scope.history.allergy.length-1];    //clone the current list

            if (! mostRecentList) {mostRecentList = [];}
            var newList = [];
            angular.forEach(mostRecentList.list,function(obj,inx){
                //don't add one that was deleted
                if (! obj.deletedReason) {
                    newList.push(obj);
                }
            });

            newList.push({display:vo.narrative});
            $scope.history.allergy.push({userName:$scope.status.user.meta.userName,list:newList});
        }
    }
});


//this is a child controller (nested in MyController)
//handles the functions from the top bar - ie login & patient select...
myApp.controller('NavController', function($scope,$http,patientService) {

    $scope.searchPatient = function() {
        $scope.status.patientSelected=false;
        delete $scope.status.patient;

        $scope.search = {};
        $scope.status.searchPatient = true;     //to indicate that searching for a patient...
        $scope.search.params = {name:"eve"};
    };

    //finds patients with the matching names...
    $scope.findPatient = function() {
        $scope.status.queryingServer = true;    //shows the 'please wait' message
        $http({
            method: 'GET',
            url: "fhir/Patient?name="+$scope.search.params.name
        }).success(function (data, status, headers, config) {
            console.log(data)
            $scope.search.results = data;
            $scope.status.queryingServer = false;
        }).error(function (data, status, headers, config) {
            $scope.status.queryingServer = false;
            alert('there was an error getting the list of patients');
        });

    };




    //when a patient has been selected from the list...
    $scope.PatientSelected = function(entry) {
        $scope.status.searchPatient = false;    //don't need to show the search screen
        console.log(entry);

        //get the virtual id - this is a hack...
        var ar = entry.id.split('/');
        $scope.getPatient(ar[ar.length-1]);
    };


    //============== user search functions ===============

    $scope.searchUser = function() {
        $scope.status.userSelected=false;
        delete $scope.status.user;

        $scope.search = {};
        $scope.status.searchUser = true;     //to indicate that searching for a patient...
        $scope.search.params = {name:"a"};
    };


    //finds patients with the matching names...
    $scope.findUser = function() {
        $scope.status.queryingServer = true;    //shows the 'please wait' message
        $http({
            method: 'GET',
            url: "fhir/Practitioner?name="+$scope.search.params.name
        }).success(function (data, status, headers, config) {
            console.log(data)
            $scope.search.results = data;
            $scope.status.queryingServer = false;
        }).error(function (data, status, headers, config) {
            $scope.status.queryingServer = false;
            alert('there was an error getting the list of Users');
        });

    };

    //when a user has been selected from the list...
    $scope.UserSelected = function(entry) {
        $scope.status.searchUser = false;    //don't need to show the search screen
        console.log(entry);

        //get the virtual id - this is a hack...
        var ar = entry.id.split('/');
        $scope.getUser(ar[ar.length-1]);
    };

    //=========== login - note overlap with User search...

    //perform a user login. Right now - it always logs in the same person...
    //there's a cross over with find user - this was part of the SMART stuff...
    $scope.login = function() {

        $scope.status.startUp = false;


        //$scope.getUser("dummy");
        var userId = "dummy";
        patientService.getUser(userId).success(function (data) {

            //add som emetadata...
            data.meta = {};
            data.meta.id = userId;
            data.meta.userName = data.name.family[0] + ", " + data.name.given[0];



            console.log("user=",data)
            $scope.status.user = data;
            $scope.status.loggedIn = true;
        }).error(function (data, status, headers, config) {
            alert('there was an error getting the User');
        });


        $scope.getPatient("dummy");     //gets a dummy patient
    };


    $scope.logout = function() {
        $scope.status.loggedIn = false;
        $scope.status.patientSelected = false;
        $scope.status.startUp = true;
        delete $scope.status.user;
    };

    $scope.getPatient = function (patId) {
        $http({
            method: 'GET',
            url: "fhir/Patient/" + patId+ "?_format=json",
            headers: {
                //Authorization: 'Bearer ' + $scope.status.user.userToken,
                Accept: 'application/json+fhir'
            }
        }).success(function (data, status, headers, config) {
            data.meta = {id:patId};
            //console.log(data)
            $scope.status.patientSelected=true;
            $scope.status.patient = data;
        }).error(function (data, status, headers, config) {
            alert('there was an error getting the patient');
        });
    };

    $scope.getUser = function (userId) {
        $http({
            method: 'GET',
            url: "fhir/Practitioner/" + userId + "?_format=json",
            headers: {
                //Authorization: 'Bearer ' + $scope.status.user.userToken,
                Accept: 'application/json+fhir'
            }
        }).success(function (data, status, headers, config) {

            //add som emetadata...
            data.meta = {};
            data.meta.id = userId;
            data.meta.userName = data.name.family[0] + ", " + data.name.given[0];



            console.log("user=",data)
            $scope.status.user = data;
            $scope.status.loggedIn = true;
        }).error(function (data, status, headers, config) {
            alert('there was an error getting the User');
        });
    };


    //http://localhost:8081/fhir/Practitioner?name=henry

});
