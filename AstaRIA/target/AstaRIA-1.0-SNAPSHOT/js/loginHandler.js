
let loginButton = document.getElementById("login_button");
let errorMessageSpan = document.getElementById("error_message_span");
let error_message_div = document.getElementById("error_message_div");

window.addEventListener("load", function() {
    error_message_div.style.display="none";
    loginButton.addEventListener("click", function(e) {
        let form = e.target.closest("form");
        if(form.checkValidity()){
            e.preventDefault();
            sendToServer(form);
        }
        else{
            form.reportValidity(); //metodo del form in grado di riportare l'esisto in caso di parametri errati
        }
    });
});

function sendToServer(form){
    makeCall("POST", "CheckLogin", form, function(req){
        if (req.readyState === 4) {
            if (req.status === 200) {
                let data = JSON.parse(req.responseText);
                localStorage.setItem('id', data.id);
                localStorage.setItem('username', data.username);
                localStorage.setItem('timestamp', data.timestamp);
                window.location.href="Home.html"; /*Una delle funzionalità fornite dall'oggetto
                window (che rappresenta il broswer) è quella di cambiare la pagina che si sta mostrando,
                come in questo caso. Da traccia, dopo l'accesso, era necessario spostarsi in una
                pagina "home" */
            } else {
                error_message_div.style.display="block";
                errorMessageSpan.textContent = "Error! Some problem with login. Try Again";
            }
        }
    });
}