/**
 * Created by davidhay on 20/07/14.
 */

function linkButton() {
    //alert("click!");
    console.log("sending bundle");
    $.get( "/serve", function( data ) {
        //$( ".result" ).html( data );
        console.log( data);
    });

}