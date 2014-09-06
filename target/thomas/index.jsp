
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
      <script src="js/libs/jquery-1.9.0.min.js"></script>
      <script src="js/libs/handlebars-v1.3.0.js"></script>
      <script src="js/libs/underscore-min.js"></script>
      <script src="js/libs/backbone-min.js"></script>
      <script src="js/libs/moment.min.js"></script>
      <script src="js/libs/bootstrap.min.js"></script>
      <link rel="stylesheet" type="text/css" href="css/bootstrap.min.css"/>
    <title></title>

  </head>
  <body style="padding: 8px">

  <nav class="navbar navbar-default" role="navigation">
      <div class="container-fluid">
            <p class="navbar-brand">My EMR</p>
            <p id="selectedPatient" class="navbar-text"></p>

          <ul class="nav navbar-nav" id="patientOptionsMenu">

              <li><a href="#" id="patientNewRecord" title="New Record" style="display: none">
                  <i class="glyphicon glyphicon-briefcase"></i>
                  </a>
              </li>

              <li class="dropdown">
                  <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                      <i class="glyphicon glyphicon-play"></i> </a>
                  <ul class="dropdown-menu" role="menu">
                      <li><a href="#" id="patSMART">SMART</a></li>
                      <li><a href="#">Another action</a></li>
                      <li><a href="#">Something else here</a></li>
                      <li class="divider"></li>
                      <li><a href="#">Separated link</a></li>
                  </ul>
              </li>
          </ul>

          <!-- Collect the nav links, forms, and other content for toggling -->
          <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
              <ul class="nav navbar-nav navbar-right">
                  <li><a href="#" id="searchPatient">Search</a></li>
                  <li><a href="#"  id="login">Login & Select Patient</a></li>
                  <li class="dropdown">
                      <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                          <i class="glyphicon glyphicon-cog"></i> </a>
                      <ul class="dropdown-menu" role="menu">
                          <li><a href="#">Logout</a></li>
                          <li><a href="#">Another action</a></li>
                          <li><a href="#">Something else here</a></li>
                          <li class="divider"></li>
                          <li><a href="#">Separated link</a></li>
                      </ul>
                  </li>
              </ul>
          </div><!-- /.navbar-collapse -->
      </div><!-- /.container-fluid -->
  </nav>


  <div class="row">
      <div class="col-md-3">





<!--
          <ul class="nav nav-pills nav-stacked">
              <li class="active sidebar"><a href="#">Home</a></li>
              <li class="sidebar" data-id="profile"><a href="#">Profile</a></li>
              <li class="sidebar" data-id="launchApp"><a href="#">SMART</a></li>
          </ul>
-->

          <!-- Where the vitals accordion will go...-->
          <div id="vitalsDiv"></div>



      </div>
      <div class="col-md-9">
        <div id="introText">
            <p>Click the <em>Login & Select Patient</em> Link above to start with a demo user and patient</p>
        </div>

          <div id="areaAddingNewConsult" style="display:none">

              <ul class="nav nav-tabs" role="tablist">
                  <li class="active"><a href="#editNew" role="tab" data-toggle="tab">New Consult</a></li>
                  <li><a href="#previewNew" role="tab" data-toggle="tab">Preview record</a></li>
                  <li><a href="#allergyHx" role="tab" data-toggle="tab">Allergy History</a></li>
              </ul>

              <br />

              <div class="tab-content">

              <div class="tab-pane active" id="editNew">
                    <div id="newRecordDiv">
                        <div class="row">
                              <div class="col-md-8">
                                  <!-- adding a new resource-->
                                  <div id="addNewResource" >

                                      <div class="row" style="margin-bottom: 5px">
                                          <div class="col-md-10">
                                              <select id="selNewResource" class="form-control"></select>
                                          </div>
                                          <div class="col-md-2">
                                                <a href="#" id="btn_save" class="btn btn-primary form-control">Save</a>
                                          </div>
                                      </div>
                                      <div class="row" style="margin-bottom: 5px">
                                          <div class="col-md-12">
                                              <textarea placeholder="Enter Narrative" class="form-control" id="resource_narrative"></textarea>
                                          </div>
                                      </div>
                                      <div class="row">
                                          <div class="col-md-12">
                                              <iframe id='profileIframe' width='100%' height='600px'></iframe>
                                          </div>
                                      </div>
                                  </div>
                              </div>
                              <div class="col-md-4">
                                  <!-- as new resources are created, add them here -->
                                <div id="newResourceHx"></div>
                              </div>

                          </div>

                    </div>

                  </div>

              <div class="tab-pane" id="previewNew">

                  <div id="previewNewConsult"></div>
                  <p>This display is a summary of the information selected during this consultation</p>
              </div>

              <div class="tab-pane" id="allergyHx">
                  <div id="allergyHxDiv"></div>
              </div>


          </div>

          </div>

          <div id="launchFrameDiv"></div>
          <br />
          <hr />
          <div id="lstPatientResults"></div>
      </div>
  </div>


<script>
  $(document).ready(function(){

      var global = {user : {},consult:[],template:{},vital:{},originalVital:{}};
      global.template.consult = Handlebars.compile($('#consultList').html());
      global.template.consultPreviewTemplate = Handlebars.compile($('#consultPreviewTemplate').html());

      global.template.problemsTemplate = Handlebars.compile($('#problemsTemplate').html());
      global.template.medsTemplate = Handlebars.compile($('#medsTemplate').html());
      global.template.allergyTemplate = Handlebars.compile($('#allergyTemplate').html());
      global.template.showAllergyHistoryTemplate = Handlebars.compile($('#showAllergyHistoryTemplate').html());



      //set vitals
      global.vital.problem = ["Asthma","Diabetes"]
      global.vital.meds = ["Atenolol 50mg, 1 nocte","Frusemide 10mg, 1 mane","Simvastatin 25mg 1 mane"];
      global.vital.allergy = [{display:"Peanuts cause anaphylaxis"}];


      global.history = {allergy: []};   //history of the allergy list
      global.history.allergy.push({userName:"John Smith",list: [{display:"Peanuts cause anaphylaxis"}]}); //the first list

      //global.history.allergy.push({userName:"Mary Jones",list: $.extend({}, global.vital.allergy)}); //the first list



      $("#allergyHxDiv").html(global.template.showAllergyHistoryTemplate({review:global.history.allergy}))


      //global.originalVital.allergy = $.extend({}, global.vital.allergy);

      console.log(global)

      $("#patientOptionsMenu").hide();      //hide the patient specific stuff

        $("#btn_save").on('click',function(){
            var narrative = $("#resource_narrative").val();
            var resourceName = $("#selNewResource").val();


            checkResource(resourceName,narrative);

            //reset combo and narrative..
            $("#resource_narrative").val("");
            $("#selNewResource").val($("#target option:first").val());

            global.consult.push({resourceName:resourceName,narrative:narrative});

            //the list of resources in the main editing window. should allow editing etc...
            $("#newResourceHx").html(global.template.consult({item:global.consult}));
            //the preview
            $("#previewNewConsult").html(global.template.consultPreviewTemplate({item:global.consult}));

            $(".remove-resource").on('click',function(ev){
                var inx = $(ev.currentTarget).attr("data-inx");
                //alert('del '+inx)
                global.consult.splice(inx,1);
                $("#newResourceHx").html(global.template.consult({item:global.consult}));
                $("#previewNewConsult").html(global.template.consultPreviewTemplate({item:global.consult}));
            })



            $("#profileIframe").attr("src","empty.html");
        })

        $("#selNewResource").on("change",function(ev){
            var name = $(ev.currentTarget).val();
            //$("#profileTitle").html("Loading "+name+"...");
            url = "http://fhir-dev.healthintersections.com.au/open/_web/Profile/"+name;
            //$("#displayDiv").html("Loading " + name);
            //$("#profileIframe").attr("src","loading.html");

            var localS = "loading...";
            //document.getElementById('output_iframe1').src = "data:text/html;charset=utf-8," + escape(localS);

            //$('#profileIframe').attr("src","data:text/html;charset=utf-8," + escape(localS))
            $("#profileIframe").attr("src",url);

        })


      //get the list of knows profiles...
      $.get("cc/profilelist",function(data){
          global.profileList = data;
          console.log(global);

          var template = Handlebars.compile($('#selectProfileTemplate').html());

          $("#selNewResource").append(template(global.profileList));
      })


      var iframe = "<iframe id='launchIframe' width='100%' height='400px'></iframe>";

      $('#login').on('click',function(){
          $.get("auth/login",function(user){
              global.user = user;

              showUser(user);
              getPatient("t100");
          })
      });


      $("#patSMART").on('click',function(){
          alert('smart')
      });

      $("#patientNewRecord").on('click',function(){
          alert('setup for new record');
      });

      $('.sidebar').on("click",function(ev){
          clearWorkArea();
          $('.sidebar').removeClass("active");
          $(ev.currentTarget).addClass("active");
          var data_id = $(ev.currentTarget).attr("data-id");
          //console.log(data_id);
          switch (data_id){
              case "launchApp" :
                  var url = "auth/launch?dummy="+new Date().getTime() + "&usertoken=" + global.user.userToken;     //ensure not caching...
                  url += "&patientid=PRP1660";
                    //console.log(global,url);
                  $("#launchFrameDiv").append(iframe);
                  $("#launchIframe").attr("src",url);

                  break;
              case "profile" :
                  var template = Handlebars.compile($('#listProfileDiv').html());

                  $("#launchFrameDiv").append(template(global.profileList));

                   $('.oneprofile').on('click',function(ev){
                       var name = $(ev.currentTarget).attr('data-name');
                       $("#profileTitle").html("Loading "+name+"...");
                       url = "http://fhir-dev.healthintersections.com.au/open/_web/Profile/"+name;
                       $("#displayDiv").html("Loading " + name);
                       $("#profileIframe").attr("src","about:blank");
                       $("#profileIframe").attr("src",url);

                   })

                  break;
          }
      });

      $('#searchPatient').on('click',function(){
          clearWorkArea();
          var template = Handlebars.compile($('#selectPatientDiv').html());
          $("#launchFrameDiv").append(template());
          $("#doSearch").on('click',function(){
              //alert('test')
              var url = "fhir/Patient?name=jones";
              $("#throbber").html("Loading...");

              $.ajax({
                  type: "GET",
                  headers: {
                      Authorization : 'Bearer ' + global.user.userToken,
                      Accept : 'application/json+fhir'
                  },
                  url: url
              })
                      .done(function( rslt ) {
                          console.log( rslt );
                          $("#throbber").html("");
                          var listTemplate = Handlebars.compile($('#listPatientDiv').html());
                          $("#lstPatientResults").html(listTemplate(rslt));
                      });


          })
      });

      function getPatient(id) {
          $.ajax({
              type: "GET",
              headers: {
                  Authorization : 'Bearer ' + global.user.userToken,
                  Accept : 'application/json+fhir'
              },
              url: "fhir/Patient/"+id
          })
          .done(function( pat ) {
              console.log( pat );
              setUpPatient(pat);

          });
      }


      function showUser(user) {
          console.log(user);
          $('#login').html("Dr Smith");
      }


      //see if the resource is a 'special' one - like a condition
      function checkResource(resourceName,narrative) {
          if (resourceName.toLowerCase().indexOf("condition") > -1) {
              if (confirm("Do you wish to add this Condition to the patients problem list")) {
                  global.vital.problem.push(narrative);
                 // $("#patProblemList").html(global.template.problemsTemplate({entry:global.vital.problem}));
                  //$("#problemCnt").html(global.vital.problem.length);
                  showVitals();
              }
          }

          if (resourceName.toLowerCase().indexOf("medication") > -1) {
              if (confirm("Do you wish to add this Medication to the patients current medication list")) {
                  global.vital.meds.push(narrative);
                  showVitals();
              }
          }

          if (resourceName.toLowerCase().indexOf("allergy") > -1) {
              if (confirm("Do you wish to add this Allergy to the patients list of allergies")) {
                  global.vital.allergy.push({display:narrative});

                  //update the history of the list
                  // global.history.allergy.push({userName:"Mary Jones",list: $.extend({}, global.vital.allergy)}); //the first list

                  var mostRecentList = global.history.allergy[global.history.allergy.length-1];    //clone the current list
                  console.log(mostRecentList)
                  var newList = [];
                  $.each(mostRecentList.list,function(inx,obj){
                      //this is a list of lists...
                      /*
                      var ar = [];
                      console.log(lst);
                      $.each(lst.list,function(inx1,obj){
                          //obj will be a single allergy...
                          ar.push($.extend({},obj));
                          console.log(ar)
                      })
                      */
                      newList.push(obj);
                      console.log(newList)
                  })

                  newList.push({display:narrative});
                  console.log(newList)
                  global.history.allergy.push({userName:global.user.userName,list:newList});

                  //show the updated list
                  $("#allergyHxDiv").html(global.template.showAllergyHistoryTemplate({review:global.history.allergy}))




                  showVitals();
              }
          }
      }


      function showVitals() {
          $("#patMedList").html(global.template.medsTemplate({entry:global.vital.meds}));
          $("#medCnt").html(global.vital.meds.length);

          $("#patProblemList").html(global.template.problemsTemplate({entry:global.vital.problem}));
          $("#problemCnt").html(global.vital.problem.length);

          $("#patAllergyList").html(global.template.allergyTemplate({entry:global.vital.allergy}));
          $("#allergyCnt").html(global.vital.allergy.length);



          $(".remove-med").on('click',function(ev){
              if (confirm('Do you wish to remove this medication from the patients usual list')){
                  var inx = $(ev.currentTarget).attr("data-inx");
                  global.vital.meds.splice(inx,1);
                  showVitals();
              };
          });

          $(".remove-problem").on('click',function(ev){
              if (confirm('Do you wish to remove this problem from the patients problem list')){
                  var inx = $(ev.currentTarget).attr("data-inx");
                  global.vital.problem.splice(inx,1);
                  showVitals();
              };
          })

          $(".remove-allergy").on('click',function(ev){
              if (confirm('Do you wish to remove this allergy from the patients list of allergies')){
                  var inx = $(ev.currentTarget).attr("data-inx");
                  global.vital.allergy.splice(inx,1);
                  showVitals();
              };
          })

      }

      //called after a patient has been selected...
      function setUpPatient(pat){
          $('#selectedPatient').html(pat.text.div)
          $("#patientOptionsMenu").show();
          var vitalsTemplate = Handlebars.compile($('#vitalsTemplate').html());
          $("#vitalsDiv").html(vitalsTemplate());

          $("#patientNewRecord").show();      //menu option
          $("#areaAddingNewConsult").show();        //div with new resources


          showVitals();


          $('#introText').hide();

      }



  });       //end of the jquery 'onload'



    function clearWorkArea() {
        $("#launchFrameDiv").empty();
        $("#lstPatientResults").empty();

    }





</script>


  <script type="handlebars/text" id="showAllergyHistoryTemplate">
      <h5 class="text-center">History of changes to Allergy List</h5>
      <table class="table table-bordered table-condensed">
        <thead>
            <tr><th>User</th><th>List</th></tr>
        </thead>
        <tbody>

          {{#each review}}
                <tr>
                    <td>{{userName}}</td>
                    <td>
                        <ul>
                        {{#each list}}
                            <li>{{display}}</li>
                        {{/each}}
                        </ul>
                    </td>
                </tr>
          {{/each}}
      </tbody></table>

</script>


  <script type="handlebars/text" id="selectPatientDiv">
      <form class="navbar-form navbar-left" role="search">
          <div class="form-group">
              <input type="text" class="form-control" placeholder="Patient Name">
          </div>
          <button type="submit" class="btn btn-primary" id="doSearch">Submit</button>
          <span id='throbber'></span>
      </form>

  </script>



  <!-- The drop down of profiles available to select-->
  <script type="handlebars/text" id="selectProfileTemplate">
    <option> -- please select profile to use --</option>
        {{#each profile}}
            <option value={{this}}>{{this}}</option>
        {{/each}}
  </script>



  <!-- List the resources that have been created for this consult...-->
  <script type="handlebars/text" id="consultList">
    <div class='list-group'>
        {{#each item}}
        <a href="#" class="list-group-item">
            <div><i class="glyphicon glyphicon-remove pull-right remove-resource" data-inx="{{@index}}"></i></div>
            <div><strong>{{resourceName}}</strong></div>
            <div style="padding-left: 10px"><em>{{narrative}}</em></div>
        </a>

        {{/each}}
    </div>
    </script>



  <!-- Generate the preview for the consult-->
  <script type="handlebars/text" id="consultPreviewTemplate">
    <table class="table table-bordered table-condensed">
        <thead>
            <tr><th>Narrative</th><th>Resource</th></tr>
        </thead>
        <tbody>
        {{#each item}}
            <tr>
                <td>{{narrative}}</td>
                <td>{{resourceName}}</td>
            </tr>


        {{/each}}
        </tbody>
    </table>
    </script>


  <script type="handlebars/text" id="listProfileDiv">
    <h5>Profiles</h5>

        <div class="row">
            <div class="col-md-3">
                    <div class='list-group'>
                      {{#each profile}}
                        <a href='#' class='list-group-item oneprofile' data-name={{this}}>{{this}}</a>
                      {{/each}}
                    </div>
                </div>
            <div class="col-md-9">
                <div id="profileTitle"></div>
                <iframe id='profileIframeDEP' width='100%' height='600px'></iframe>
            </div>
        </div>

  </script>

  <script type="handlebars/text" id="listPatientDiv">
      {{#each entry}}
        {{{content.text.div}}}
      {{/each}}
  </script>

  <!-- Display all the vitals in an accordian -->
  <script type="handlebars/text" id="vitalsTemplate">
      <div class="panel-group" id="accordion">
  <div class="panel panel-default">
    <div class="panel-heading">
      <h4 class="panel-title">
        <a data-toggle="collapse" data-parent="#accordion" href="#collapseOne">Current Medications
        (<span id="medCnt"></span>)</a>
      </h4>
    </div>
    <div id="collapseOne" class="panel-collapse collapse in">
      <div class="panel-body">
        <div id="patMedList"></div>
        </div>
    </div>
  </div>

  <div class="panel panel-default">
    <div class="panel-heading">
      <h4 class="panel-title">
        <a data-toggle="collapse" data-parent="#accordion" href="#collapseTwo">
        Problem List (<span id="problemCnt"></span>)</a>
      </h4>
    </div>
    <div id="collapseTwo" class="panel-collapse collapse">
      <div class="panel-body">
        <div id="patProblemList"></div>
      </div>
    </div>
  </div>

  <div class="panel panel-default">
    <div class="panel-heading">
      <h4 class="panel-title">
        <a data-toggle="collapse" data-parent="#accordion" href="#collapseThree">Allergies (<span id="allergyCnt"></span>)</a>
      </h4>
    </div>
    <div id="collapseThree" class="panel-collapse collapse">
      <div class="panel-body">
        <div id="patAllergyList"></div>
      </div>
    </div>
  </div>
</div>

  </script>

  <!-- Display the problems within the accordian -->
  <script type="handlebars/text" id="problemsTemplate">
    <div class='list-group'>
      {{#each entry}}
        <a href='#' class='list-group-item' data-name={{this}}>
            <div><i class="glyphicon glyphicon-remove pull-right remove-problem" data-inx="{{@index}}"></i></div>
            {{this}}
        </a>
      {{/each}}
    </div>

  </script>


  <!-- Display the allergies within the accordian-->
  <script type="handlebars/text" id="allergyTemplate">
    <div class='list-group'>
      {{#each entry}}
        <a href='#' class='list-group-item' data-name={{display}}>
            <div><i class="glyphicon glyphicon-remove pull-right remove-allergy" data-inx="{{@index}}"></i></div>
            {{display}}
        </a>
      {{/each}}
    </div>

  </script>

  <!-- Display the meds within the accordian -->
  <script type="handlebars/text" id="medsTemplate">
    <div class='list-group'>
      {{#each entry}}
        <a href='#' class='list-group-item' data-name={{this}}>
            <div><i class="glyphicon glyphicon-remove pull-right remove-med" data-inx="{{@index}}"></i></div>
            {{this}}
        </a>
      {{/each}}
    </div>

  </script>


  </body>
</html>
