//controller for clinical connectathon...

var myApp = angular.module('myApp',[]);


myApp.run(function($rootScope) {
    $rootScope.name = "David Hay";
});

myApp.controller('MyController', function($scope,$http) {
    $scope.person = {
        name: "David hay controller"
    };

    $scope.status = {
        loggedIn : false
    }

    //represtents data collected
    $scope.data = {hx : []};    //hx is the list of

    $scope.vitals = {condition:[],med:[],allergy:[]};

    $scope.history = {allergy : []};

    //get the list of profiles from the server
    $scope.getData = function() {
        $http({
            method: 'GET',
            url: '/cc/profilelist'
        }).success(function(data, status, headers, config) {
            console.log(data)
            $scope.profiles = data.profile;
        }).error(function(data, status, headers, config) {
        });
    }

    //when a single profile has been selected from the combo...
    $scope.profileSelected = function() {
        //console.log($scope.status.profileName);

        //todo I know this is wrong - will fix later...
        //url = "http://fhir-dev.healthintersections.com.au/open/_web/Profile/"+$scope.status.profileName.name;

        //$("#profileIframe").attr("src",url);

        //alert(name);
        $scope.status.profileSelected = true;
        //$scope.status.profileName = $scope.status.profileName.name;
    }


    //when a profile narrative has been completed
    $scope.saveProfileData = function() {
        //alert ($scope.data.narrative)
        //$scope.data.hx.push({profile:$scope.status.profileName,narrative:$scope.data.narrative})
        var newResource = {profile:$scope.status.profileName.name,narrative:$scope.data.narrative};
        $scope.data.hx.push(newResource);
        checkNewProfile(newResource);

        //disable the profile details
        $scope.status.profileSelected = false;
        $scope.status.profileName = "";
        $scope.data.narrative="";


    };

    //remove an allergy from the allergy list
    $scope.deleteAllergy = function(a,inx) {
        console.log(a);
        alert(inx);



        var deletedReason = prompt('Why do you wish to remove this allergy from the patients list of allergies','Incorrect');
        if (deletedReason){

            $scope.vitals.allergy.splice(inx,1);    //remove from the current list...


            var mostRecentList = $scope.history.allergy[$scope.history.allergy.length-1];    //clone the current list
            console.log(mostRecentList)
            var newList = [];
            angular.forEach(mostRecentList.list,function(obj,inx1){

                if (inx1 == inx) {
                    //mark the one just deleted...
                    var newObj = angular.copy(obj);


                    newObj.deletedReason = deletedReason;
                    newList.push(newObj);
                } else {
                    //if the allergy was previously deleted, then stop adding it...
                    console.log(obj.deletedReason);
                    if (! obj.deletedReason) {
                        // newList.push(obj);
                        newList.push(angular.copy(obj));
                    }

                }

            })

            //newList.push({display:narrative});
            //console.log(newList)
            $scope.history.allergy.push({userName:$scope.status.user.userName,list:newList});

            //show the updated list
            //$("#allergyHxDiv").html(global.template.showAllergyHistoryTemplate({review:global.history.allergy}))

            //showVitals();

            console.log($scope.history.allergy);
        };
    }

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
            console.log($scope)
            var mostRecentList = $scope.history.allergy[$scope.history.allergy.length-1];    //clone the current list

            if (! mostRecentList) {mostRecentList = [];}

            console.log(mostRecentList)
            var newList = [];
            angular.forEach(mostRecentList.list,function(obj,inx){
            //$.each(

                console.log(obj.deletedReason);
                //don't add one that was deleted
                if (! obj.deletedReason) {

                    newList.push(obj);
                }
                console.log(newList)
            })

            newList.push({display:vo.narrative});
            console.log(newList)
            $scope.history.allergy.push({userName:$scope.status.user.userName,list:newList});

            //show the updated list
            //$("#allergyHxDiv").html(global.template.showAllergyHistoryTemplate({review:global.history.allergy}))






        }
    }

});


myApp.controller('NavController', function($scope,$http) {

    $scope.login = function() {
        //alert('login');


        if (!$scope.status.loggedIn) {


            $http({
                method: 'GET',
                url: "auth/login?username=Dr Jones"
            }).success(function (data, status, headers, config) {
                console.log(data)
                $scope.status.user = data;
                $scope.status.loggedIn = true;  //defined in MyController - assume that controller is a parent...
                $scope.getPatient("T100");
            }).error(function (data, status, headers, config) {
                alert('there was an error getting the user');
            });


            $scope.getPatient = function (patId) {
                $http({
                    method: 'GET',
                    url: "fhir/Patient/" + patId,
                    headers: {
                        Authorization: 'Bearer ' + $scope.status.user.userToken,
                        Accept: 'application/json+fhir'
                    }
                }).success(function (data, status, headers, config) {
                    console.log(data)
                    $scope.status.patient = data;
                    // $scope.status.loggedIn = true;  //defined in MyController - assume that controller is a parent...
                }).error(function (data, status, headers, config) {
                    alert('there was an error getting the patient');
                });


            }

        }
        else {
            //logout
            $scope.status.loggedIn = false;
        }
    }

});


/*
*
*
*
*myApp.controller('MyController', function($scope,$http) {
 $scope.person = {
 name: "David hay controller"
 };

 $scope.

 $scope.sayHello = function() {
 alert('here');
 $scope.person.name = "Nick";

 //$scope.myDiv = document.createElement('div');

 //$scope.myDiv.innerHTML = "Dynamic, Man"

 }

 var updateClock = function() {
 $scope.clock = new Date();
 };
 var timer = setInterval(function() {
 $scope.$apply(updateClock);
 }, 1000);
 updateClock();

 $scope.counter = 0;
 $scope.add = function(amount) { $scope.counter += amount; };
 $scope.subtract = function(amount) { $scope.counter -= amount; };

 $scope.getData = function() {
 $http({
 method: 'GET',
 url: '/cc/profilelist'
 }).success(function(data, status, headers, config) {
 // data contains the response
 // status is the HTTP status
 // headers is the header getter function
 // config is the object that was used to create the HTTP request
 console.log(data)
 $scope.profiles = data.profile;
 }).error(function(data, status, headers, config) {
 });
 }

 $scope.profileSelected = function(name) {
 alert(name);

 }


 });


 *
* */

/*
myApp.controller('MyCtrl', ['$scope', function($scope) {
    $scope.visible = true;

    $scope.toggle = function() {
        $scope.visible = !$scope.visible;
    };
}]);
*/



/*
function MyCtrl($scope) {
    $scope.visible = true;

    $scope.toggle = function() {
        $scope.visible = !$scope.visible;
    };
}
*/

//console.log(MyCtrl);