
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
                  </a></li>


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

          <div id="newRecordDiv">
              <div class="row">
                  <div class="col-md-8">
                      <!-- adding a new resource-->
                      <div id="addNewResource" style="display:none">
                          <select id="selNewResource" class="form-control"></select>
                          <iframe id='profileIframe' width='100%' height='600px'></iframe>
                      </div>



                  </div>
                  <div class="col-md-4">
                      <!-- as new resources are created, add them here -->
                    <div id="newResourceHx"></div>
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

      var global = {user : {}};


      $("#patientOptionsMenu").hide();      //hide the patien specific stuff

      //create the vitals accordian



        $("#selNewResource").on("change",function(ev){
            //alert($(ev.currentTarget).val());


            var name = $(ev.currentTarget).val();
            //$("#profileTitle").html("Loading "+name+"...");
            url = "http://fhir-dev.healthintersections.com.au/open/_web/Profile/"+name;
            //$("#displayDiv").html("Loading " + name);
            //$("#profileIframe").attr("src","about:blank");
            $("#profileIframe").attr("src",url);

        })


      //get the list of knows profiles...
      $.get("cc/profilelist",function(data){
          global.profileList = data;
          console.log(global);


          var template = Handlebars.compile($('#selectProfileTemplate').html());

          $("#selNewResource").append(template(global.profileList));




          //$("#selNewResource").append('<option value=1>My option</option>');

      })


      var iframe = "<iframe id='launchIframe' width='100%' height='400px'></iframe>";

      $('#login').on('click',function(){
          $.get("auth/login",function(user){
              global.user = user;
              //console.log(user);
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
                      //alert('x')
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
          $('#login').html("Dr Smith");
      }

  });

    function clearWorkArea() {
        $("#launchFrameDiv").empty();
        $("#lstPatientResults").empty();

    }


  //called after a patient has been selected...
    function setUpPatient(pat){
        $('#selectedPatient').html(pat.text.div)
        $("#patientOptionsMenu").show();
        var vitalsTemplate = Handlebars.compile($('#vitalsTemplate').html());
        $("#vitalsDiv").html(vitalsTemplate());

        $("#patientNewRecord").show();      //menu option
        $("#addNewResource").show();        //div with new resources

        var problemsTemplate = Handlebars.compile($('#problemsTemplate').html());
        $("#patProblemList").html(problemsTemplate({entry:["Asthma","Diabetes"]}));
        $("#problemCnt").html("2");
    }


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

  <script type="handlebars/text" id="vitalsTemplate">
      <div class="panel-group" id="accordion">
  <div class="panel panel-default">
    <div class="panel-heading">
      <h4 class="panel-title">
        <a data-toggle="collapse" data-parent="#accordion" href="#collapseOne">Current Medications</a>
      </h4>
    </div>
    <div id="collapseOne" class="panel-collapse collapse in">
      <div class="panel-body">
        <ul></ul>
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
        <a data-toggle="collapse" data-parent="#accordion" href="#collapseThree">Allergies</a>
      </h4>
    </div>
    <div id="collapseThree" class="panel-collapse collapse">
      <div class="panel-body">
        <ul></ul>
      </div>
    </div>
  </div>
</div>

  </script>

  <script type="handlebars/text" id="problemsTemplate">
    <div class='list-group'>
      {{#each entry}}
        <a href='#' class='list-group-item oneprofile' data-name={{this}}>{{this}}</a>
      {{/each}}
    </div>

  </script>

  </body>
</html>
