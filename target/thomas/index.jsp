<%--
  Created by IntelliJ IDEA.
  User: davidhay
  Date: 20/07/14
  Time: 8:09 AM
  To change this template use File | Settings | File Templates.
--%>
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

          <!-- Collect the nav links, forms, and other content for toggling -->
          <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">


              <ul class="nav navbar-nav navbar-right">
                  <li><a href="#" id="searchPatient">Search</a></li>
                  <li><a href="#"  id="login">Login</a></li>
                  <li class="dropdown">
                      <a href="#" class="dropdown-toggle" data-toggle="dropdown"><i class="glyphicon glyphicon-cog"></i> </a>
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
      <div class="col-md-2">

          <ul class="nav nav-pills nav-stacked">
              <li class="active sidebar"><a href="#">Home</a></li>
              <li class="sidebar"><a href="#">Profile</a></li>
              <li class="sidebar" data-id="launchApp"><a href="#">Show SMART</a></li>
          </ul>

      </div>
      <div class="col-md-10">
          <div id="launchFrameDiv"></div>
          <br />
          <hr />
          <div id="lstPatientResults"></div>
      </div>
  </div>



<script>
  $(document).ready(function(){

      var global = {user : {}};

      var iframe = "<iframe id='launchIframe' width='100%' height='400px'></iframe>";

      $('#login').on('click',function(){
          $.get("/auth/login",function(user){
              global.user = user;
              //console.log(user);
              showUser(user);
              getPatient("t100");
          })
      });


      $('.sidebar').on("click",function(ev){
          clearWorkArea();
          $('.sidebar').removeClass("active");
          $(ev.currentTarget).addClass("active");
          var data_id = $(ev.currentTarget).attr("data-id");
          //console.log(data_id);
          switch (data_id){
              case "launchApp" :
                  var url = "/auth/launch?dummy="+new Date().getTime() + "&usertoken=" + global.user.userToken;     //ensure not caching...
                  url += "&patientid=PRP1660";
                    //console.log(global,url);
                  $("#launchFrameDiv").append(iframe);
                  $("#launchIframe").attr("src",url);

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

              /*
              $.get(url,function(rslt){
                  $("#throbber").html("");
                  console.log(rslt);
                  var listTemplate = Handlebars.compile($('#listPatientDiv').html());

                  $("#lstPatientResults").html(listTemplate(rslt));


              })
              */
              //http://localhost:8081/local/patient?name=jones

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
              $('#selectedPatient').html(pat.text.div)
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

  <script type="handlebars/text" id="listPatientDiv">
      {{#each entry}}
        {{{content.text.div}}}
      {{/each}}
  </script>

  </body>
</html>
