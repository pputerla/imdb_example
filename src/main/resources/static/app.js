var stompClient = null;

function connect() {
    var socket = new SockJS('/hitCountSocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        stompClient.subscribe('/topic/hitCounts', function (message) {
            showHitCounts(JSON.parse(message.body).payload);
        });
    });
}


function showHitCounts(message) {
    $("#hitCounts").html("<tr><td>" + message + "</td></tr>");
}

$(document).ready(function () {
    connect();
});
