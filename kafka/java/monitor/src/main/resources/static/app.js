var stompClient = null;
var eventsPerTransaction = {};

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    // TODO: Init by querying all events from REST
    //$("#greetings").html("");
}

function newEventReceived(event) {
    //console.log(event);

    // id used to find transaction via JavaScript, "-" from UUID not valid as JS-ID.
    var jsTransactionId = event.transactionId;
    
    if (!eventsPerTransaction[jsTransactionId]) {
        eventsPerTransaction[jsTransactionId] = [];
        $("#events").prepend('<tr><td><a href="/bpmn.html?traceId='+event.transactionId+'" target="blank_">'+event.transactionId+'</a></td><td id="' + event.transactionId + '"></td></tr>');
    }

    eventsPerTransaction[jsTransactionId].push(event);

    var color = 'info';
    if (event.type=='Command') {
        color = 'danger'; // or 'warning'?
    }
    var randomId = Math.floor((1 + Math.random()) * 0x10000);
    var prettyJson = JSON.stringify(JSON.parse(event.sourceJson), null, 2);
    var html = 
          '<div class="alert alert-'+color+'">'+event.type+': '+event.name+' (from '+event.sender+') '          
        + '<a label="Details" data-toggle="collapse" data-target="#'+randomId+'" class="btn btn-default table-row-btn"><span class="glyphicon glyphicon-eye-open"></span></a>'
        + '<div class="collapse" id="'+ randomId + '"><pre>' + prettyJson + '</pre></div>'
        + '</div>';
    $('td[id^="'+jsTransactionId+'"]').append(html);
}

function connect() {
    var socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/events', function (event) {
            newEventReceived(JSON.parse(event.body));
        });
    });
}

function disconnect() {
    if (stompClient != null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendName(); });
});