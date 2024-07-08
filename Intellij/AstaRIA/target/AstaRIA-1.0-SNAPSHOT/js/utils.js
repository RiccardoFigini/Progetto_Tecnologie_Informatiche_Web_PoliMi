
function makeCall(method, url, formElement, cback, reset = true) {
    let req = new XMLHttpRequest(); // visible by closure
    req.onreadystatechange = function() {
        switch (req.readyState) {
            case XMLHttpRequest.DONE:
                cback(req);
                break;
        }
    };

    req.open(method, url);
    if (formElement === null) {
        req.send();
    } else {
        req.send(new FormData(formElement));
    }
    if (formElement !== null && reset === true) {
        formElement.reset();
    }
}

function makeCallJson(method, url, json, cback) {
    let req = new XMLHttpRequest();
    req.onreadystatechange = function() {
        switch (req.readyState) {
            case XMLHttpRequest.UNSENT:
                break;
            case XMLHttpRequest.OPENED:
                break;
            case XMLHttpRequest.HEADERS_RECEIVED:
                break;
            case XMLHttpRequest.LOADING:
                break;
            case XMLHttpRequest.DONE:
                cback(req);
                break;
        }
    };
    req.open(method, url);
    req.setRequestHeader("Content-type", "application/json");
    req.send(json);
}