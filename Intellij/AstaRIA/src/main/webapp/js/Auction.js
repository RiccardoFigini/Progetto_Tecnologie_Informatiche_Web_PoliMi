var error_prompt = new Error_promptConstrutor(
        document.getElementById("error_prompt"),
        document.getElementById("error_title"),
        document.getElementById("error_desc"),
        document.getElementById("error_close")
);
var valid_prompt = new Valid_promptConstrutor(
    document.getElementById("valid_prompt"),
    document.getElementById("valid_title"),
    document.getElementById("valid_desc"),
    document.getElementById("valid_close")
);
var pageOrchestrator = new PageOrchestrator();
var map = new Map("users_clicks");
var mapLastAction = new MapLastAction('lastActionMap');

window.addEventListener("load", function ()  {
    let init = new Init();
    init.loadPage();
});
function Map(_name){
    this.name=_name;
    this.ttl=30 * 24 * 60 * 60 * 1000;
    //Questo dovrebbe restituire una mappa se questa esiste altrimenti un oggettp vuoto. (graffe perchè sono il contenitore di json?)
    this.map=JSON.parse(localStorage.getItem(_name)) || {};
    var self = this;

    this.add_id = function (user_id, new_id) {
        let currentDate = new Date();
        let expirationDate = new Date(currentDate.getTime() + self.ttl);
        if (!self.map[user_id]) {
            self.map[user_id] = [];
        }

        if (!self.map[user_id].some(item => item[0] === new_id)) {
            self.map[user_id].push([new_id, expirationDate]);
        }
        else
        {
            let index = self.map[user_id].findIndex(item => item[0] === new_id);
            if (index !== -1) {
                self.map[user_id][index][1] = expirationDate;
            }
        }
        localStorage.setItem(self.name, JSON.stringify(self.map));
    }

    this.get_array = function (user_id) {
        self.checkExpire(user_id);
        let vect = [];
        let entry = self.map[user_id];
        if (entry) {
            for (let i = 0; i < entry.length; i++) {
                vect.push(self.map[user_id][i][0]);
            }
            return vect;
        }

    }
    /** Toglie gli ID con data lista di user_id e salva la mappa aggiornata*/
    this.checkExpire = function (user_id) {
        let entry = self.map[user_id];
        let now = new Date();
        if(entry) {
            for (let i = 0; i < entry.length; i++) {
                if (now.getTime() > new Date(entry[i][1]).getTime()) {
                    entry.splice(i, 1);
                }
            }
            localStorage.setItem(self.name, JSON.stringify(self.map));
        }
    }
}
function MapLastAction(_name) {
    this.name=_name;
    this.ttl= 30 * 24 * 60 * 60 * 1000; // 30gg
    this.map=JSON.parse(localStorage.getItem(this.name)) || {};
    let self = this;

    this.changeStatus = function (user_id, new_status) {
        let currentDate = new Date();
        let expirationDate = new Date(currentDate.getTime() + self.ttl);
        self.map[user_id] = [new_status, expirationDate];
        localStorage.setItem(self.name, JSON.stringify(self.map));
    }
    /** Restituisce un array di ID non scaduti*/
    this.getStatus = function (user_id) {
        self.checkExpire(user_id);
        if(!self.map[user_id])
            return "otherwise";
        else
            return self.map[user_id][0];
    }
    this.checkExpire = function (user_id) {
        let entry = self.map[user_id];
        let now = new Date();
        if(entry) {
            if (now.getTime() > new Date(entry[1]).getTime()) {
                localStorage.removeItem(self.map[user_id]);
            }
        }
    }
}
function Init(){
    this.loadPage = function (){
        pageOrchestrator.start();
        if(mapLastAction.getStatus(localStorage.getItem("username")) === "auction_creation")
        {
            pageOrchestrator.show_sell()
        }
        else
        {
            pageOrchestrator.show_buy();
        }
    }
}
function PageOrchestrator() {
    let home;
    let sell;
    let buy;
    let auct_det;
    let side_panel;
    let self=this;

    /*Acquisisce gli oggetti di tutte le pagine. Le pagina sono "generali", gli elementi specifici dal document vengono poi estratti
             * dalle opportune classi*/
    this.start = function() {

        side_panel = new Side_panelConstructor(
            document.getElementById("aside"),
            document.getElementById("home_link"),
            document.getElementById("logout_link"),
            document.getElementById("side_panel_username"),
            document.getElementById("side_panel_timestamp")
        );

        home = new HomeConstructor(
            document.getElementById("containerHome"),
            document.getElementById("buttonSell"),
            document.getElementById("buttonBuy")
        );

        sell = new Sell(
            document.getElementById("sell")
        );

        auct_det = new Auct_det(
            document.getElementById("auct_det")
        );

        buy = new Buy(
            document.getElementById("buy")
        );
    };
    this.show_home= function(){
        home.show();
    };
    this.hide_home= function(){
        home.hide();
    };
    this.show_sell = function(){
        side_panel.hideFunction=self.hide_sell;
        sell.show();
    };
    this.hide_sell = function(){
        sell.hide();
    };
    this.show_auction_details = function(id,isClosed){
        side_panel.hideFunction=self.hide_auct_det;
        auct_det.show(id,isClosed);
    };
    this.auct_det_reload_closure = function (id) {
        auct_det.reload_closure(id);
    }
    this.hide_auct_det = function(){
        auct_det.hide();
    };
    this.show_buy = function (){
        side_panel.hideFunction=self.hide_buy;
        buy.show();
    }
    this.hide_buy = function (){
        buy.hide();
    }
    this.do_logout = function ()
    {
        localStorage.removeItem('id');
        localStorage.removeItem('username');
        localStorage.removeItem('timestamp');
        window.location.href="login.html";
    }
}
function Error_promptConstrutor(prompt,title,desc,close) {
    this.prompt=prompt;
    this.title=title;
    this.desc=desc;
    this.close=close;
    let self=this;
    this.close.addEventListener("click", (e) => {
        self.desc.textContent="";
        self.prompt.style.display="none";
    });
    this.show= function (text) {
        prompt.style.display="flex";
        self.desc.textContent=text;
        close.focus();
    }
}
function Valid_promptConstrutor(prompt,title,desc,close) {
    this.prompt=prompt;
    this.title=title;
    this.desc=desc;
    this.close=close;
    let self=this;
    this.close.addEventListener("click", (e) => {
        self.prompt.style.display="none";
    });
    this.show= function (text) {
        prompt.style.display="flex";
        self.desc.textContent=text;
        close.focus();
    }
}
function HomeConstructor(_div,_sellButton, _buyButton) {
    this.div=_div;
    this.sellButton = _sellButton;
    this.buyButton = _buyButton;
    let self = this;
    this.sellButton.addEventListener("click", (e) => {
        e.preventDefault();
        pageOrchestrator.hide_home();
        pageOrchestrator.show_sell();
    });
    this.buyButton.addEventListener("click", (e) => {
        e.preventDefault();
        pageOrchestrator.hide_home();
        pageOrchestrator.show_buy();
    });
    this.hide=function (){
        self.div.style.display="none";
    }
    this.show=function (){
        self.div.style.display="block";
    }

}
function Side_panelConstructor(_div,_home_link,_logout_link,_username_span,_timestamp_span) {
    this.div=_div;
    this.home_link=_home_link;
    this.username_span=_username_span;
    this.timestamp_span=_timestamp_span;
    this.logout_link=_logout_link;
    this.hideFunction=undefined;
    let self = this;
    this.home_link.addEventListener("click", (e) => {
        this.hideFunction();
        pageOrchestrator.show_home();
    });
    this.logout_link.addEventListener("click", (e) => {
        makeCall("GET", 'LogoutServlet', null, (req) =>{
            switch(req.status){
                case 200:
                    self.hideFunction();
                    pageOrchestrator.do_logout();
                    break;
                case 401:
                    pageOrchestrator.do_logout();
                    break;
                case 500: // server error
                default: //Error
            }
        });
    });
    this.username_span.textContent="User: " + localStorage.getItem("username");
    this.timestamp_span.textContent="Login time: " + localStorage.getItem("timestamp");
}

////////////////////////////////////PARTE DI SELL////////////////////////////////////////
function Sell(_div) {
    this.div=_div;
    let self = this;
    this.sell__open_auction = new Sell__open_auctionConstructor(
        document.getElementById("sell__open_auction_tbody"),
    );
    this.sell__closed_auction = new Sell__closed_auctionConstructor(
        document.getElementById("sell__closed_auction_tbody"),
    );
    this.sell__auction_form = new Sell__auction_formConstructor(
        document.getElementById("sell__auction_form_tbody"),
        document.getElementById("sell__auction_form_submit"),
        this.sell__open_auction
    );
    this.sell__article_form = new Sell__articles_formConstructor(
        document.getElementById("sell__article_form_submit"),
        this.sell__auction_form
    );

    this.hide= function () {
        this.reset();
        self.div.style.display="none";
    }
    this.show= function () {
        self.sell__open_auction.show();
        self.sell__closed_auction.show();
        self.sell__auction_form.show();
        self.div.style.display="block";
    }
    this.reset= function () {
        self.sell__open_auction.reset();
        self.sell__closed_auction.reset();
        self.sell__auction_form.reset();
    }
}
function Sell__open_auctionConstructor(_tbody) {
    this.tbody=_tbody;
    let self = this;
    this.reset =  function(){
        while (self.tbody.firstChild) {
            self.tbody.removeChild(self.tbody.firstChild);
        }
    };
    this.show = function(){
        makeCall("GET", 'MyOpenAuctionServlet', null, (req) =>{
            switch(req.status){
                case 200:
                    let auctionList = JSON.parse(req.responseText);
                    self.update_whole(auctionList);
                    break;
                case 400:
                    error_prompt.show(req.responseText);
                    break;
                case 401:
                    pageOrchestrator.do_logout();
                    break;
                case 500: // server error
                default: //Error
            }
        });
    };
    this.update_whole = function (_list) {
        this.reset();
        if (_list.length === 0) {
            let tr = document.createElement("tr");
            let td = document.createElement("td");
            td.setAttribute("colspan", "7");
            td.setAttribute("style", "text-align: center;");
            td.textContent = "No open auctions found";
            tr.classList.add("to_be_removed");
            tr.appendChild(td);
            self.tbody.appendChild(tr);
        }
        else
        {
            _list.forEach((auction) => {
                let tr = document.createElement("tr");
                let articlesCell = document.createElement("td");
                articlesCell.className = "articles-td";
                auction.listOfArticle.forEach(function(item) {
                    let span = document.createElement("span");
                    span.textContent = "[" + item.code + "] " + item.name;
                    articlesCell.appendChild(span);
                });
                let maxOfferCell = document.createElement("td");
                maxOfferCell.textContent = auction.maxOffer != null ? auction.maxOffer : "-----";

                let usernameOfferCell = document.createElement("td");
                usernameOfferCell.textContent = auction.usernameOffer != null ? auction.usernameOffer : "-----";

                let remainingTimeCell = document.createElement("td");
                remainingTimeCell.textContent = auction.remainingTime;

                let startDateCell = document.createElement("td");
                startDateCell.textContent = auction.startDate;

                let endDateCell = document.createElement("td");
                endDateCell.textContent = auction.endDate;

                let detailsCell = document.createElement("td");
                let detailsButton = document.createElement("button");
                detailsButton.className = "styled-table-button";
                detailsButton.textContent = "Details";
                detailsButton.addEventListener("click", (e) => {
                    e.preventDefault();
                    pageOrchestrator.hide_sell();
                    pageOrchestrator.show_auction_details(auction.id,false);
                });
                detailsCell.appendChild(detailsButton);

                tr.appendChild(articlesCell);
                tr.appendChild(maxOfferCell);
                tr.appendChild(usernameOfferCell);
                tr.appendChild(remainingTimeCell);
                tr.appendChild(startDateCell);
                tr.appendChild(endDateCell);
                tr.appendChild(detailsCell);

                self.tbody.appendChild(tr);
            });
        }
    }

    this.update_single = function (auction) {
        let rows = self.tbody.getElementsByTagName("tr");
        if (rows.length!==0 && rows[0].classList.contains("to_be_removed")) {
            self.tbody.removeChild(rows[0]);
        }


        let tr = document.createElement("tr");
        let articlesCell = document.createElement("td");
        articlesCell.className = "articles-td";
        auction.listOfArticle.forEach(function(item) {
            let span = document.createElement("span");
            span.textContent = "[" + item.code + "] " + item.name;
            articlesCell.appendChild(span);
        });
        let maxOfferCell = document.createElement("td");
        maxOfferCell.textContent = auction.maxoffer != null ? auction.maxoffer : "-----";

        let usernameOfferCell = document.createElement("td");
        usernameOfferCell.textContent = auction.usernameOffer != null ? auction.usernameOffer : "-----";

        let remainingTimeCell = document.createElement("td");
        remainingTimeCell.textContent = auction.remainingTime;

        let startDateCell = document.createElement("td");
        startDateCell.textContent = auction.startDate;

        let endDateCell = document.createElement("td");
        endDateCell.textContent = auction.endDate;

        let detailsCell = document.createElement("td");
        let detailsButton = document.createElement("button");
        detailsButton.className = "styled-table-button";
        detailsButton.textContent = "Details";
        detailsButton.addEventListener("click", (e) => {
            e.preventDefault();
            pageOrchestrator.hide_sell();
            pageOrchestrator.show_auction_details(auction.id,false);
        });
        detailsCell.appendChild(detailsButton);

        tr.appendChild(articlesCell);
        tr.appendChild(maxOfferCell);
        tr.appendChild(usernameOfferCell);
        tr.appendChild(remainingTimeCell);
        tr.appendChild(startDateCell);
        tr.appendChild(endDateCell);
        tr.appendChild(detailsCell);

        self.tbody.appendChild(tr);
    }

}
function Sell__closed_auctionConstructor(_tbody) {
    this.tbody=_tbody;
    let self = this;
    this.reset =  function(){
        while (self.tbody.firstChild) {
            self.tbody.removeChild(self.tbody.firstChild);
        }
    };
    this.show = function(){
        makeCall("GET", 'MyClosedAuctionServlet', null, (req) =>{
            switch(req.status){
                case 200:
                    let auctionList = JSON.parse(req.responseText);
                    self.update(auctionList);
                    break;
                case 400:
                    error_prompt.show(req.responseText);
                    break;
                case 401:
                    pageOrchestrator.do_logout();
                    break;
                case 500: // server error
                default: //Error
            }
        });
    };
    this.update = function (_list){
        if (_list.length === 0) {
            let tr = document.createElement("tr");
            let td = document.createElement("td");
            td.setAttribute("colspan", "6");
            td.setAttribute("style", "text-align: center;");
            td.textContent = "No closed auctions found";
            tr.appendChild(td);
            self.tbody.appendChild(tr);
        }
        else
        {
            _list.forEach((auction) => {
                let tr = document.createElement("tr");


                let articlesCell = document.createElement("td");
                articlesCell.className = "articles-td";
                auction.listOfArticle.forEach(function(item) {
                    let span = document.createElement("span");
                    span.textContent = "[" + item.code + "] " + item.name;
                    articlesCell.appendChild(span);
                });


                let maxOfferCell = document.createElement("td");
                maxOfferCell.textContent = auction.maxOffer != null ? auction.maxOffer : "-----";

                let usernameOfferCell = document.createElement("td");
                usernameOfferCell.textContent = auction.usernameOffer != null ? auction.usernameOffer : "-----";

                let startDateCell = document.createElement("td");
                startDateCell.textContent = auction.startDate;

                let endDateCell = document.createElement("td");
                endDateCell.textContent = auction.endDate;

                let detailsCell = document.createElement("td");
                let button = document.createElement("button");
                button.className = "styled-table-button";
                button.textContent = "Details";
                button.addEventListener("click", (e) => {
                    e.preventDefault();
                    pageOrchestrator.hide_sell();
                    pageOrchestrator.show_auction_details(auction.id,true);
                });
                detailsCell.appendChild(button);


                tr.appendChild(articlesCell);
                tr.appendChild(maxOfferCell);
                tr.appendChild(usernameOfferCell);
                tr.appendChild(startDateCell);
                tr.appendChild(endDateCell);
                tr.appendChild(detailsCell);


                self.tbody.appendChild(tr);
            });
        }
    }
}
function Sell__articles_formConstructor(_submit,_auction_form) {
    this.submit=_submit;
    this.auction_form=_auction_form;
    let self = this;

    this.submit.addEventListener("click", (e) => {
        let article_form = e.target.closest("form");
        e.preventDefault();
        if(article_form.checkValidity()){
            makeCall("POST", 'CreateArticleServlet', article_form, (req) =>{
                switch(req.status){
                    case 200:
                        mapLastAction.changeStatus(localStorage.getItem("username"),"otherwise");
                        self.auction_form.update_single(JSON.parse(req.responseText));
                        break;
                    case 400:
                        error_prompt.show(req.responseText);
                        break;
                    case 401:
                        pageOrchestrator.do_logout();
                        break;
                    case 500:
                    default:
                        error_prompt.show("Request reported status " + req.status);
                }
            });
        }else{
            article_form.reportValidity();
        }
    });

}
function Sell__auction_formConstructor(_tbody,_submit,_open_auction_table) {
    this.submit = _submit;
    this.tbody = _tbody;
    this.open_auction_table=_open_auction_table;
    let self = this;
    this.reset =  function(){
        while (self.tbody.firstChild) {
            self.tbody.removeChild(self.tbody.firstChild);
        }
    };
    this.submit.addEventListener("click", (e) => {
        let auction_form = e.target.closest("form");
        e.preventDefault();
        if(auction_form.checkValidity()){
            makeCall("POST", 'CreateAuctionServlet', auction_form, (req) =>{
                switch(req.status){
                    case 200:
                        let response = JSON.parse(req.responseText);
                        mapLastAction.changeStatus(localStorage.getItem("username"), "auction_creation")
                        self.open_auction_table.update_single(response.auction);
                        self.remove_selected(response.article_codes);
                        break;
                    case 400:
                        error_prompt.show(req.responseText);
                        break;
                    case 401:
                        pageOrchestrator.do_logout();
                        break;
                    case 500:
                    default: //Error
                        error_prompt.show("Request reported status " + req.status);
                }
            });
        }else{
            auction_form.reportValidity();
        }
    });
    this.show = function () {
        makeCall("GET", 'MyAvailableArticlesServlet', null, (req) => {
            switch (req.status) {
                case 200:
                    let articleList = JSON.parse(req.responseText);
                    self.update_whole(articleList);
                    break;
                case 400:
                    error_prompt.show(req.responseText);
                    break;
                case 401:
                    pageOrchestrator.do_logout();
                    break;
                case 500: // server error
                default: //Error
            }
        });
    };
    this.update_whole = function (_list) {
        this.reset();
        if (_list.length === 0) {
            let tr = document.createElement("tr");
            let td = document.createElement("td");
            td.setAttribute("colspan", "7");
            td.setAttribute("style", "text-align: center;");
            td.textContent = "No available articles";
            tr.classList.add("to_be_removed");
            tr.appendChild(td);
            self.tbody.appendChild(tr);
        } else {
            _list.forEach((item) => {
                let tr = document.createElement("tr");
                tr.id= "Ar"+item.code;

                let checkboxCell = document.createElement("td");
                let checkbox = document.createElement("input");
                checkbox.type = "checkbox";
                checkbox.name = "articleCode";
                checkbox.value = item.code;
                checkboxCell.appendChild(checkbox);
                tr.appendChild(checkboxCell);

                let codeCell = document.createElement("td");
                let codeSpan = document.createElement("span");
                codeSpan.textContent = item.code;
                codeCell.appendChild(codeSpan);
                tr.appendChild(codeCell);

                let nameCell = document.createElement("td");
                let nameSpan = document.createElement("span");
                nameSpan.textContent = item.name;
                nameCell.appendChild(nameSpan);
                tr.appendChild(nameCell);

                let descriptionCell = document.createElement("td");
                let descriptionSpan = document.createElement("span");
                descriptionSpan.textContent = item.description;
                descriptionCell.appendChild(descriptionSpan);
                tr.appendChild(descriptionCell);

                let minimumPriceCell = document.createElement("td");
                let minimumPriceSpan = document.createElement("span");
                minimumPriceSpan.textContent = item.minimumPrice;
                minimumPriceCell.appendChild(minimumPriceSpan);
                tr.appendChild(minimumPriceCell);

                let keyWordCell = document.createElement("td");
                let keyWordSpan = document.createElement("span");
                keyWordSpan.textContent = item.keyWord;
                keyWordCell.appendChild(keyWordSpan);
                tr.appendChild(keyWordCell);

                let imageCell = document.createElement("td");
                let imageDiv = document.createElement("div");
                imageDiv.className = "imageDiv";
                let image = document.createElement("img");
                image.src = 'data:image/png;base64,' + item.encodedImage;
                imageDiv.appendChild(image);
                imageCell.appendChild(imageDiv);
                tr.appendChild(imageCell);

                self.tbody.appendChild(tr);
            });
        }
    }
    this.update_single = function (item) {
        // tolgo la scritta NO ARTICLES FOUND, se c'è
        let rows = self.tbody.getElementsByTagName("tr");
        if (rows.length!==0 && rows[0].classList.contains("to_be_removed")) {
            self.tbody.removeChild(rows[0]);
        }


        let tr = document.createElement("tr");
        tr.id= "Ar"+item.code;

        let checkboxCell = document.createElement("td");
        let checkbox = document.createElement("input");
        checkbox.type = "checkbox";
        checkbox.name = "articleCode";
        checkbox.value = item.code;
        checkboxCell.appendChild(checkbox);
        tr.appendChild(checkboxCell);

        let codeCell = document.createElement("td");
        let codeSpan = document.createElement("span");
        codeSpan.textContent = item.code;
        codeCell.appendChild(codeSpan);
        tr.appendChild(codeCell);

        let nameCell = document.createElement("td");
        let nameSpan = document.createElement("span");
        nameSpan.textContent = item.name;
        nameCell.appendChild(nameSpan);
        tr.appendChild(nameCell);

        let descriptionCell = document.createElement("td");
        let descriptionSpan = document.createElement("span");
        descriptionSpan.textContent = item.description;
        descriptionCell.appendChild(descriptionSpan);
        tr.appendChild(descriptionCell);

        let minimumPriceCell = document.createElement("td");
        let minimumPriceSpan = document.createElement("span");
        minimumPriceSpan.textContent = item.minimumPrice;
        minimumPriceCell.appendChild(minimumPriceSpan);
        tr.appendChild(minimumPriceCell);

        let keyWordCell = document.createElement("td");
        let keyWordSpan = document.createElement("span");
        keyWordSpan.textContent = item.keyWord;
        keyWordCell.appendChild(keyWordSpan);
        tr.appendChild(keyWordCell);

        let imageCell = document.createElement("td");
        let imageDiv = document.createElement("div");
        imageDiv.className = "imageDiv";
        let image = document.createElement("img");
        image.src = 'data:image/png;base64,' + item.encodedImage;
        imageDiv.appendChild(image);
        imageCell.appendChild(imageDiv);
        tr.appendChild(imageCell);

        self.tbody.appendChild(tr);
    }
    this.remove_selected = function (_articles_id) {
        _articles_id.forEach((id) =>
        {
            let tr = document.getElementById("Ar"+id);
            self.tbody.removeChild(tr);
        });
        if(self.tbody.childNodes.length === 0)
        {
            let tr = document.createElement("tr");
            let td = document.createElement("td");
            td.setAttribute("colspan", "7");
            td.setAttribute("style", "text-align: center;");
            tr.classList.add("to_be_removed");
            td.textContent = "No available articles";
            tr.appendChild(td);
            self.tbody.appendChild(tr);
        }
    }
}
//////////////////////////////////////////////////////////////////////////////////////////////
function Auct_det(_div) {
    this.div=_div;

    this.auct_det__details = new Auct_det__detailsContructor(
        document.getElementById("auct_det__auction_tbody"),
        document.getElementById("auct_det__articles_tbody")
    );
    this.auct_det__offers = new Auct_det__offersContructor(
        document.getElementById("auct_det__offers_tbody")
    );
    this.auct_det__winner = new Auct_det__winnerContructor(
        document.getElementById("auct_det__winner"),
        document.getElementById("auct_det__winner_tbody")
    );
    this.auct_det__closeButton = new Auct_det__buttonContructor(
        document.getElementById("auct_det__closeButton"),
        document.getElementById("auct_det__closeButton_submit")
    );
    let self = this;

    this.hide= function () {
        self.auct_det__details.reset();
        self.auct_det__offers.reset();
        self.auct_det__winner.reset();
        self.auct_det__winner.hide();
        self.auct_det__closeButton.hide();
        self.div.style.display="none";
    }
    this.show= function (id,isClosed) {
        self.auct_det__details.show(id);
        self.auct_det__offers.show(id);
        if(isClosed)
        {
            self.auct_det__winner.show(id);
        }
        else
        {
            self.auct_det__closeButton.show(id);
        }
        self.div.style.display="block";
    }
    /**Questo metodo è chiamato alla chiusura dell'asta, quando bisogna aggiornare la pagina
     * andando a cercare il vincitore, cambiato lo stato dell'asta da aperto a chiuso e nascondendo
     * il bottone per chiudere l'asta*/
    this.reload_closure = function (id){
        self.auct_det__details.write_closed();
        self.auct_det__winner.show(id);
        self.auct_det__closeButton.hide();
    }
}
function Auct_det__detailsContructor(_tbody,_tbody_articles) {
    this.tbody=_tbody;
    this.tbody_articles=_tbody_articles;
    let self = this;
    this.reset =  function(){
        while (self.tbody.firstChild) {
            self.tbody.removeChild(self.tbody.firstChild);
        }
        while (self.tbody_articles.firstChild) {
            self.tbody_articles.removeChild(self.tbody_articles.firstChild);
        }
    };
    this.show = function(id){
        makeCall("GET", 'AuctionDetailsServlet?ID='+id, null, (req) =>{
            switch(req.status){
                case 200:
                    let auction = JSON.parse(req.responseText);
                    self.update(auction);
                    break;
                case 400:
                    error_prompt.show(req.responseText);
                    break;
                case 401:
                    pageOrchestrator.do_logout();
                    break;
                case 500: // server error
                default: //Error
            }
        });
    };
    this.update = function (auction) {
        this.reset();
        self.updateDetails(auction);
        self.updateArticles(auction);
    }
    this.updateDetails = function (auction) {
        let tr = document.createElement("tr");

        let auctionId = document.createElement("td");
        auctionId.textContent = auction.id;
        tr.appendChild(auctionId);

        let startPrice = document.createElement("td");
        startPrice.textContent = auction.startPrice;
        tr.appendChild(startPrice);

        let minimumRise = document.createElement("td");
        minimumRise.textContent = auction.minimumRise;
        tr.appendChild(minimumRise);

        let startDate = document.createElement("td");
        startDate.textContent = auction.startDate;
        tr.appendChild(startDate);

        let endDate = document.createElement("td");
        endDate.textContent = auction.endDate;
        tr.appendChild(endDate);

        let isClosed = document.createElement("td");
        isClosed.id="isClosed";
        isClosed.textContent = auction.isClosed ? 'Closed' : 'Open';
        tr.appendChild(isClosed);

        self.tbody.appendChild(tr);
    }
    this.updateArticles = function (auction) {
        auction.listOfArticle.forEach(function(item) {
            // Creazione di un elemento tr
            var tr = document.createElement("tr");

            // Creazione degli elementi td e impostazione dei loro testi
            var code = document.createElement("td");
            code.textContent = item.code;
            tr.appendChild(code);

            var name = document.createElement("td");
            name.textContent = item.name;
            tr.appendChild(name);

            var description = document.createElement("td");
            description.textContent = item.description;
            tr.appendChild(description);

            var minimumPrice = document.createElement("td");
            minimumPrice.textContent = item.minimumPrice;
            tr.appendChild(minimumPrice);


            let imageCell = document.createElement("td");
            let imageDiv = document.createElement("div");
            imageDiv.className = "imageDiv";
            let image = document.createElement("img");
            image.src = 'data:image/png;base64,' + item.encodedImage;
            imageDiv.appendChild(image);
            imageCell.appendChild(imageDiv);
            tr.appendChild(imageCell);
            // Inserimento della riga tr nel tbody
            self.tbody_articles.appendChild(tr);
        });
    }
    this.write_closed = function ()
    {
        document.getElementById("isClosed").textContent="Closed";
    }
}
function Auct_det__offersContructor(tbody_offers) {
    this.tbody_offers=tbody_offers;
    let self = this;
    this.reset =  function(){
        while (self.tbody_offers.firstChild) {
            self.tbody_offers.removeChild(self.tbody_offers.firstChild);
        }
    };
    this.show = function(id){
        makeCall("GET", 'AuctionOffersServlet?ID='+id, null, (req) =>{
            switch(req.status){
                case 200:
                    let offerList = JSON.parse(req.responseText);
                    self.update(offerList);
                    break;
                case 400:
                    error_prompt.show(req.responseText);
                    break;
                case 401:
                    pageOrchestrator.do_logout();
                    break;
                case 500: // server error
                default: //Error
            }
        });
    };
    this.update = function (_list) {
        if (_list.length === 0) {
            let tr = document.createElement("tr");
            let td = document.createElement("td");
            td.setAttribute("colspan", "3");
            td.setAttribute("style", "text-align: center;");
            td.textContent = "No offers found";
            tr.appendChild(td);
            self.tbody_offers.appendChild(tr);
        }
        else
        {
            _list.forEach((offer) => {
                let tr = document.createElement("tr");
                let userCell = document.createElement("td");
                let datetimeCell = document.createElement("td");
                let priceCell = document.createElement("td");
                userCell.textContent=offer.userOffer;
                datetimeCell.textContent=offer.datatime;
                priceCell.textContent=offer.price;

                tr.appendChild(userCell);
                tr.appendChild(datetimeCell);
                tr.appendChild(priceCell);
                self.tbody_offers.appendChild(tr);
            });
        }
    }
}
function Auct_det__winnerContructor(_div,_tbody) {
    this.div=_div;
    this.tbody=_tbody;
    let self = this;

    this.hide =  function(){
        self.div.style.display="none";
    };
    this.reset =  function(){
        while (self.tbody.firstChild) {
            self.tbody.removeChild(self.tbody.firstChild);
        }
    };
    this.show = function(id){
        makeCall("GET", 'AuctionWinnerServlet?ID='+id, null, (req) =>{
            switch(req.status){
                case 200:
                    let response = JSON.parse(req.responseText);
                    /*Viene restituito il vinciotore, però se nel caso questo non esiste
                    * perché l'asta richiesta in realà non è ancora conclusa lo verifico
                    * tramite il flag "isClosed" che imposto nella servlet*/
                    if(response.isClosed) {
                        self.update(response.winnerOffer);
                        self.div.style.display="block";
                    }
                    break;
                case 400:
                    error_prompt.show(req.responseText);
                    break;
                case 401:
                    pageOrchestrator.do_logout();
                    break;
                case 500: // server error
                default: //Error
            }
        });
    };
    this.update = function (winner) {
        this.reset();
        if(winner==null)
        {
            let tr = document.createElement("tr");
            let td = document.createElement("td");
            td.setAttribute("colspan", "3");
            td.setAttribute("style", "text-align: center;");
            td.textContent = "No winner found";
            tr.appendChild(td);
            self.tbody.appendChild(tr);
        }
        else
        {
            let tr = document.createElement("tr");
            let usernameCell = document.createElement("td");
            let priceCell = document.createElement("td");
            let addressCell = document.createElement("td");
            usernameCell.textContent=winner.userOffer;
            priceCell.textContent=winner.price;
            addressCell.textContent=winner.userAddress;
            tr.appendChild(usernameCell);
            tr.appendChild(priceCell);
            tr.appendChild(addressCell);

            self.tbody.appendChild(tr);
        }
    }
}
function Auct_det__buttonContructor(_div,_submit) {
    this.div=_div;
    this.submit=_submit;
    let self = this;
    this.hide = function ()
    {
        self.div.style.display="none";
    }
    this.show = function(id){
        self.div.style.display="block";
        self.submit.addEventListener("click", (e) => {
            makeCall("GET", 'CloseAuctionServlet?ID='+id, null, (req) =>{
                switch(req.status){
                    case 200:
                        pageOrchestrator.auct_det_reload_closure(id);
                        break;
                    case 400:
                        error_prompt.show(req.responseText);
                        break;
                    case 401:
                        pageOrchestrator.do_logout();
                        break;
                    case 500: // server error
                    default: //Error
                }
            });
        });
    };
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
/*Oggetto che gestisce tutto il lato "buy"*/
function Buy(_div) {
    this.div=_div;
    let self=this;

    this.buy__recent_visited_auction = new Buy__recent_visited_auctionConstructor(
        document.getElementById("recent_visited_auction_div"),
        document.getElementById("recent_visited_auction_tbody"),
        self
    )

    this.buy__keyword_table = new Buy__keyword_tableConstructor(
        document.getElementById("keyword_tbody"),
        document.getElementById("inputKeyWord"),
        document.getElementById("keywordButton"),
        document.getElementById("keyword_form"),
        document.getElementById("span_keyword"),
        self
    );

    this.buy__close_and_won_auction = new Buy__close_and_won_auctionConstructor(
        document.getElementById("tbody_closeAndWonAuction"),
        self
    );

    this.buy__auction_offer_page = new Buy__auction_offer_pageConstructor(
        document.getElementById("auction_information"),
        document.getElementById("tbody_auction_information"),
        document.getElementById("auction_offers"),
        document.getElementById("tbody_auction_offers"),
        document.getElementById("no_offer_row"),
        document.getElementById("close_open_auction_page"),
        document.getElementById("open_auction_page"),
        document.getElementById("make_a_offer"),
        document.getElementById("form_make_a_offer"),
        document.getElementById("title_auction_details"),
        document.getElementById("make_a_offer_input"),
        self
    );

    this.hide= function () {
        self.reset();
        self.buy__auction_offer_page.div_open_auction_page.style.display='none'
        self.div.style.display="none";
    }
    this.show= function () {
        this.reset();
        self.buy__close_and_won_auction.show();
        self.buy__recent_visited_auction.show();
        self.div.style.display="block";
    }
    this.reset= function () {
        self.buy__close_and_won_auction.reset();
        self.buy__auction_offer_page.resetBothTable();
        self.buy__keyword_table.resetTable();
        self.buy__recent_visited_auction.reset();
    }
}
/*Oggetto che gestisce la sezione dedicata alle aste vinte recentemente,
* chiamato ogni volta che si accede alla pagina buy. Si preoccupa di chiedere al server quali delle aste
* che ha salvato in memoria sono ancora disponibili*/
function Buy__recent_visited_auctionConstructor(div_, tbody_, buyManager_){
    this.div=div_;
    this.tbody=tbody_;
    this.buyManager=buyManager_;
    this.listOfAuctionPrinted = [];
    let self = this;
    /*Questo metodo prepara la tabella da mostrare. In particolare contatta il server inoltrando la lista
    * delle aste visitate recentemente. Una volta ottenuta la risposta è in grado di mostrare le aste
    * effettivamente chiamando la funzione "showAfterCheck"
    * */
    this.show = function (){
        let jsonData = JSON.stringify(map.get_array(localStorage.getItem('username')));
        makeCallJson("POST", "VerifyStillOpen", jsonData, (req)=>{
                switch (req.status) {
                    case 200:
                        let data = JSON.parse(req.responseText);
                        self.showAfterCheck(data);
                        break;
                    case 500:
                        error_prompt.show(req.responseText);
                        break;
                    case 400:
                        error_prompt.show(req.responseText);
                        break;
                    case 401:
                        pageOrchestrator.do_logout();
                        break;
                    default:
                        error_prompt.show(req.status);
                }
        });
    }
    /*Questo metodo gestisce l'apertura delle sezione relativa ad un asta nella quale è possibile svolgere una
    * offerta. Quinid non va altro che preparare tutti i dati necessari ed inoltrarli a auctionOfferPage tramite
    * la funzione show*/
    this.openInformationAboutAuction = function (req, auctionId){
        let data = JSON.parse(req.responseText);
        let offers = data.offers;
        let articles = data.articles;
        let minimumRise = data.minimumRise;
        let actualOffer = data.actualOffer;
        self.buyManager.buy__auction_offer_page.resetBothTable();
        self.buyManager.buy__auction_offer_page.setIdAuction(auctionId);
        self.buyManager.buy__auction_offer_page.show(offers, articles, minimumRise, actualOffer);
    }
    /*Questa funzione stampa effettivamente le aste, con tutte le informazioni. Nel caso in cui non si siano
    * visitate asta di recente viene stampata una riga che segnala la situazione
    * All'interno la parte più interessante è la creazione del "bottone" che permette l'apertura della sezione con
    * le informazioni relative all'asta. Gli attrbuti passati sono ovviamente gli id dell'asta, la risposta dalla servlet
    * con le informazioni relative all'asta specifica e la cella (td) di riferimento. In questo modo, nel caso in cui
    * svolgo una offerta, posso modificare anche questa tabella con l'attuale valore dell'offerta.
    * Successivamente ho aggiunto "addARow" perchè almeno posso aggiungere una sola riga alla volta nel caso in cui dopo la ricerca
    * di un asta nell'altra tabella clicco su una*/
    this.showAfterCheck = function (listOfAuction){
        if(!listOfAuction || listOfAuction.length===0 || listOfAuction==="true"){
            let tr = document.createElement('tr');
            let td = document.createElement('td');
            td.setAttribute("colspan", "8");
            td.setAttribute("style", "text-align: center;");
            td.textContent = "No recent auctions found";
            tr.classList.add("to_be_removed");
            tr.appendChild(td);
            self.tbody.appendChild(tr);
        }
        else {
            listOfAuction.forEach(auction => {
                /*In questo caso gli preparo già il textNode così gli passo direttamente la stringa con dentro tutti gli articoli
                * Questo è necessario perche AddARow viene chiamato anche altre volte e in quei casi non si ha la lista di
                * articoli ma direttamente la stringa*/
                this.emptyTable=false;
                let nodeItem = document.createTextNode("");
                auction.listOfArticle.forEach(article => {
                    nodeItem.appendData(article.name + ", ");
                });
                let nodeItem2 = document.createTextNode(nodeItem.textContent.slice(0, -2));
                this.AddARow(nodeItem2.textContent, auction.startPrice, auction.minimumRise, auction.usernameOwner, auction.maxOffer, auction.startDate, auction.endDate, auction.remainingTime, auction.id)
            });
        }
    }
    /*Resetta la tabella*/
    this.reset = function (){
        while (self.tbody.firstChild) {
            self.tbody.removeChild(self.tbody.firstChild);
        }
        self.listOfAuctionPrinted = [];
    }
    /*Creato questa funzione per poter aggiungere una sola riga alla volta. Sono tutte già stringhe che andrò a stampre in un node
    * da aggiungere poi alla cella*/
    this.AddARow = function (listOfArticle, startPrice, minimumRise, usernameOwner, maxOffer, startDate, endDate, remainingTime, id){
        let tdLastOffer = document.createElement('td');
        if(self.listOfAuctionPrinted.filter(element => element[0]===id).length>0){
            return;
        }
        let rows = self.tbody.getElementsByTagName("tr");
        if (rows.length!==0 && rows[0].classList.contains("to_be_removed"))
        {
            self.tbody.removeChild(rows[0]);
        }


        self.listOfAuctionPrinted.push([id, tdLastOffer]);
        self.index=this.index+1;
        let tr = document.createElement('tr');
        let tdGroupItem = document.createElement('td');
        let tdMinimumPrice = document.createElement('td');
        let tdMinimumRise = document.createElement('td');
        let tdUserOwner = document.createElement('td');
        let tdStartDate = document.createElement('td');
        let tdEndDate = document.createElement('td');
        let tdCountDown = document.createElement('td');

        let items = document.createTextNode(listOfArticle);
        tdGroupItem.appendChild(items);
        tr.appendChild(tdGroupItem);
        let nodeMinimumPrice = document.createTextNode(startPrice);
        tdMinimumPrice.appendChild(nodeMinimumPrice);
        tr.appendChild(tdMinimumPrice);
        let nodeMinimumRise = document.createTextNode(minimumRise);
        tdMinimumRise.appendChild(nodeMinimumRise);
        tr.appendChild(tdMinimumRise);
        let nodeUserNameOwner = document.createTextNode(usernameOwner);
        tdUserOwner.appendChild(nodeUserNameOwner);
        tr.appendChild(tdUserOwner);
        let nodeLastOffer = document.createTextNode(maxOffer);
        tdLastOffer.appendChild(nodeLastOffer);
        tdLastOffer.addEventListener("click", (e) => {
            e.preventDefault();
            mapLastAction.changeStatus(localStorage.getItem("username"), "otherwise");
            makeCall("GET", "GoToAuctionOffer?auctionId=" + id, null, (req) => {
                    switch (req.status) {
                        case 200:
                            self.openInformationAboutAuction(req, id);
                            document.getElementById("open_auction_page").style.display = 'block';
                            map.add_id(localStorage.getItem('username'), id);
                            break;
                        case 500:
                            error_prompt.show(req.responseText);
                            break;
                        case 400:
                            error_prompt.show(req.responseText);
                            break;
                        case 401:
                            pageOrchestrator.do_logout();
                            break;
                        default:
                            error_prompt.show(req.status);
                    }

            });
        });
        tr.appendChild(tdLastOffer);

        tdLastOffer.addEventListener("mouseover", function (){
           tdLastOffer.classList.add("custom-cursor");
        });
        tdLastOffer.addEventListener("mouseout", function (){
           tdLastOffer.classList.remove("custom-cursor");
        });

        let nodeStartDate = document.createTextNode(startDate);
        tdStartDate.appendChild(nodeStartDate);
        tr.appendChild(tdStartDate);
        let nodeEndDate = document.createTextNode(endDate);
        tdEndDate.appendChild(nodeEndDate);
        tr.appendChild(tdEndDate);
        let nodeCountdown = document.createTextNode(remainingTime);
        tdCountDown.appendChild(nodeCountdown);
        tr.appendChild(tdCountDown);
        self.tbody.appendChild(tr);
    }
    /*Questo metodo è chiamato dalla pagina in cui è possibile inserire un offerta, controlla se l'asta a cui
    * è stata fatta l'offerta è tra quelle nella tabella e nel caso aggiorna il valore "lastOffer" con quella
    * che è stata appena inserita*/
    this.updateLastOfferCell = function (id, offer){
        for(let i=0; i<self.listOfAuctionPrinted.length; i++){
            let element = self.listOfAuctionPrinted[i];
            if(element[0]===id){
                element[1].firstChild.textContent=offer;
            }
        }
    }
}
/*Oggetto che gestisce la sezione dedicata alla ricerca delle aste.
* Quando si apre fa vedere una sola riga con la possibilità di ricercare le aste*/
function Buy__keyword_tableConstructor(tbody_, inputText_, button_, form_, span_keyword_, buyManager_){
    this.span_keyword=span_keyword_;
    this.tbody=tbody_;
    this.inputText=inputText_;
    this.button=button_;
    this.form=form_;
    this.buyManager=buyManager_;
    this.keyword="";
    this.printedAuction = [];
    let self = this;
    /*Creazione del listener per cercare le aste. Questa sezione permette di cercare le aste al click del bottone e di
    * chiamare "show table" una volta restituite le aste*/
    this.button.addEventListener("click", (e) =>{
        e.preventDefault();
        if(self.form.checkValidity()){
            self.keyword=document.getElementById("inputKeyWord").value;
            makeCall("POST", "KeyWordServlet", this.form, (req)=>{
                    switch (req.status) {
                        case 200:
                            let data = JSON.parse(req.responseText);
                            self.showTable(data);
                            break;
                        case 500:
                            error_prompt.show(req.responseText);
                            break;
                        case 400:
                            error_prompt.show(req.responseText);
                            break;
                        case 401:
                            pageOrchestrator.do_logout();
                            break;
                        default:
                            error_prompt.show(req.status);
                    }
            });
        }
        else{
            error_prompt.show("Input error");
        }
    });
    /*Resetta la tabella*/
    this.resetTable = function (){
        while (self.tbody.firstChild) {
            self.tbody.removeChild(self.tbody.firstChild);
        }
        self.printedAuction = [];
    };
    /*Mostra le aste nello stesso modo di prima, anche qui con il controllo che siano presente effettivamente delle aste
    * Anche in questo caso si tratta solo di una creazione di tabella, la parte più interessante è sicuramente
    * l'aggiunta del listener che permette di aprire la sezione della tabella. COme nel caso di prima vengono passati
    * id dell'asta, risposta con le informazioni specifiche dell'asta e td per modificare la cella dell'offerta di questa
    * tabella nel caso l'utente ne faccia una */
    this.showTable = function (listOfAuction){
        self.resetTable(this.tbody);
        if(listOfAuction.length===0){
            let tr = document.createElement('tr');
            let td = document.createElement('td');
            td.setAttribute("colspan", "8");
            td.setAttribute("style", "text-align: center;");
            td.textContent = "No auctions with word: " + self.keyword;
            tr.classList.add("to_be_removed");
            tr.appendChild(td);
            self.tbody.appendChild(tr);
        }
        else {
            listOfAuction.forEach(auction => {
                let tr = document.createElement('tr');
                let tdGroupItem = document.createElement('td');
                let tdMinimumPrice = document.createElement('td');
                let tdMinimumRise = document.createElement('td');
                let tdUserOwner = document.createElement('td');
                let tdLastOffer = document.createElement('td');
                let tdStartDate = document.createElement('td');
                let tdEndDate = document.createElement('td');
                let tdCountDown = document.createElement('td');

                let nodeItem = document.createTextNode("");
                auction.listOfArticle.forEach(article => {
                    nodeItem.appendData(article.name + ", ");
                });
                let nodeItem2 = document.createTextNode(nodeItem.textContent.slice(0, -2));
                tdGroupItem.appendChild(nodeItem2);
                tr.appendChild(tdGroupItem);

                let nodeMinimumPrice = document.createTextNode(auction.startPrice);
                tdMinimumPrice.appendChild(nodeMinimumPrice);
                tr.appendChild(tdMinimumPrice);

                let nodeMinimumRise = document.createTextNode(auction.minimumRise);
                tdMinimumRise.appendChild(nodeMinimumRise);
                tr.appendChild(tdMinimumRise);

                let nodeUserNameOwner = document.createTextNode(auction.usernameOwner);
                tdUserOwner.appendChild(nodeUserNameOwner);
                tr.appendChild(tdUserOwner);

                let nodeLastOffer = document.createTextNode(auction.maxOffer);
                tdLastOffer.appendChild(nodeLastOffer);
                tdLastOffer.addEventListener("click", (e) => {
                    mapLastAction.changeStatus(localStorage.getItem("username"), "otherwise");
                    let auctionId = auction.id;
                    map.add_id(localStorage.getItem('username'), auctionId);
                    makeCall("GET", "GoToAuctionOffer?auctionId=" + auctionId, null, (req) => {
                            switch (req.status) {
                                case 200:
                                    document.getElementById("open_auction_page").style.display = 'block';
                                    document.getElementById("open_auction_page").style.display = 'block';
                                    let thisRow = tdLastOffer.closest('tr');
                                    let tdList = thisRow.getElementsByTagName('td');
                                    /*Istruzione fornita dal DOM che permette di prendere tutti gli elementi dell'albero sottostante all'elemento thisRow
                                    * di tipo TD. Questo infatti restituisce la lista di tutte le cella della riga selezionata e quindi posso accedere
                                    * al testo per aggiornare la tabella recentAuction
                                    * */
                                    self.buyManager.buy__recent_visited_auction.AddARow(
                                        tdList[0].firstChild.textContent,
                                        tdList[1].firstChild.textContent,
                                        tdList[2].firstChild.textContent,
                                        tdList[3].firstChild.textContent,
                                        tdList[4].firstChild.textContent,
                                        tdList[5].firstChild.textContent,
                                        tdList[6].firstChild.textContent,
                                        tdList[7].firstChild.textContent,
                                        auctionId
                                    );
                                    self.openInformationAboutAuction(req, auctionId);
                                    break;
                                case 500:
                                    error_prompt.show(req.responseText);
                                    break;
                                case 400:
                                    error_prompt.show(req.responseText);
                                    break;
                                case 401:
                                    pageOrchestrator.do_logout();
                                    break;
                                default:
                                    error_prompt.show(req.status);
                            }

                    });
                });
                tr.appendChild(tdLastOffer);

                tdLastOffer.addEventListener("mouseover", function (){
                    tdLastOffer.classList.add("custom-cursor");
                });
                tdLastOffer.addEventListener("mouseout", function (){
                    tdLastOffer.classList.remove("custom-cursor");
                });

                let nodeStartDate = document.createTextNode(auction.startDate);
                tdStartDate.appendChild(nodeStartDate);
                tr.appendChild(tdStartDate);

                let nodeEndDate = document.createTextNode(auction.endDate);
                tdEndDate.appendChild(nodeEndDate);
                tr.appendChild(tdEndDate);

                let nodeCountdown = document.createTextNode(auction.remainingTime);
                tdCountDown.appendChild(nodeCountdown);
                tr.appendChild(tdCountDown);
                self.printedAuction.push([auction.id, tdLastOffer]);
                self.tbody.appendChild(tr);
            });
        }
    }
    /*Metodo che permette di aprire di aprire la sezione con le informazioni relative ad una specifica asta (come il caso
    * di prima).*/
    this.openInformationAboutAuction = function (req, auctionId){
        let data = JSON.parse(req.responseText);
        let offers = data.offers;
        let articles = data.articles;
        let minimumRise = data.minimumRise;
        let actualOffer = data.actualOffer;
        self.buyManager.buy__auction_offer_page.resetBothTable();
        self.buyManager.buy__auction_offer_page.setIdAuction(auctionId);
        self.buyManager.buy__auction_offer_page.show(offers, articles, minimumRise, actualOffer);
    }
    /*Questo metodo è chiamato dalla pagina in cui è possibile inserire un offerta, controlla se l'asta a cui
    * è stata fatta l'offerta è tra quelle nella tabella e nel caso aggiorna il valore "lastOffer" con quella
    * che è stata appena inserita*/
    this.updateLastOfferCell = function (id, offer){
        for(let i=0; i<self.printedAuction.length; i++){
            let element = self.printedAuction[i];
            if(element[0]===id){
                element[1].firstChild.textContent=offer;
            }
        }
    }
}
/*Oggetto dedicato a quella che sarebbe dovuta essere la seconda pagina id buy. Siccome sembrava più carino
* abbiamo deciso di lasciar anche questa parte nella stessa pagina. Al click di una delle aste contenute nelle due
* tabelle superiori si apre una sezione con la quale è possibile svolgere le offerte*/
function Buy__auction_offer_pageConstructor(div_auction_information_, tbody_auction_information_, div_auction_offer_, tbody_auction_offer_, no_offer_row, close_page_button_, div_open_auction_page_, make_a_offer_, form_make_a_offer_, title_, input_form_, buyManager_) {
    this.input_form=input_form_;
    this.div_auction_information=div_auction_information_;
    this.tbody_auction_information=tbody_auction_information_;
    this.div_auction_offer=div_auction_offer_;
    this.tbody_auction_offer=tbody_auction_offer_;
    this.no_offer_row = no_offer_row;
    this.close_page_button=close_page_button_;
    this.div_open_auction_page=div_open_auction_page_;
    this.make_a_offer=make_a_offer_;
    this.form_make_a_offer=form_make_a_offer_;
    this.title=title_;
    this.buyManager = buyManager_;
    let minimumRise = 0;
    let auctionId = NaN;
    let lastOffer = -1;
    let self=this;
    this.setIdAuction = function (id){
        auctionId = id;
    }
    this.setMinimumPrice = function (price){
        minimumRise = price;
    }
    this.setLastOffer = function (last){
        lastOffer = last;
    }
    this.make_a_offer.addEventListener("click", (e)=>{
        e.preventDefault();
        if(self.form_make_a_offer.checkValidity()){
            let offerValue = self.input_form.value;
            self.input_form.value="";
            makeCall("GET", "MakeAOffer?offer=" + offerValue + "&auctionId=" + auctionId + "&lastOffer=" + lastOffer, null, (req) => {
                switch (req.status) {
                    case 200:
                        let listOfOffer = JSON.parse(req.responseText);
                        let enter = false;
                        listOfOffer.forEach(offer =>{
                            self.addAcceptedOffer(offer);
                            if( (""+offer.price) === offerValue && offer.userOffer === localStorage.getItem("username") ){
                                valid_prompt.show("Your offer: " + offer.price);
                                enter = true;
                            }
                        });
                        if(enter===false){
                            error_prompt.show("Not valid offer");
                        }
                        break;
                    case 500:
                        error_prompt.show(req.responseText);
                        break;
                    case 400:
                        error_prompt.show(req.responseText);
                        break;
                    case 401:
                        pageOrchestrator.do_logout();
                        break;
                    default:
                        error_prompt.show(req.status);
                }
            });
        }
        else {
            error_prompt.show("Error input");
        }
    });
    /*Funzione che mette tutti gli elementi nelle due tabelle: offers and articles. Vengono definiti anche i listener
    * che permetto di effettuare una offerta e di chiudere la sezione relativa all'asta*/
    this.show = function (offers, articles, minimumRise, actualOffer) {
        self.title.innerHTML="";
        if (offers.length === 0) {
            let tr = document.createElement('tr');
            let td = document.createElement('td');
            td.setAttribute("colspan", "3");
            td.setAttribute("style", "text-align: center;");
            td.textContent = "No offers";
            tr.classList.add("to_be_removed");
            tr.appendChild(td);
            self.tbody_auction_offer.appendChild(tr);
            self.setMinimumPrice(minimumRise);
            self.title.innerHTML="Minimum rise: "+ minimumRise + ", actual offer:" + actualOffer;

        }
        else
        {
            self.title.innerHTML="Minimum rise: "+ minimumRise + ", actual offer:" + offers[0].price;
            self.setMinimumPrice(minimumRise);
            this.setLastOffer(offers[0].price);
            offers.forEach(offer => {
                let tr = document.createElement('tr');
                let username = document.createElement('td');
                let date = document.createElement('td');
                let price = document.createElement('td');
                let nodeUsername = document.createTextNode(offer.userOffer);
                let nodeDate = document.createTextNode(offer.datatime);
                let nodePrice = document.createTextNode(offer.price);
                username.appendChild(nodeUsername);
                date.appendChild(nodeDate);
                price.appendChild(nodePrice);
                tr.appendChild(username);
                tr.appendChild(date);
                tr.appendChild(price);
                self.tbody_auction_offer.appendChild(tr);
            });
        }
        articles.forEach(article => {
            let tr = document.createElement('tr');
            let item = document.createElement('td');
            let description = document.createElement('td');
            let minimumPrice = document.createElement('td');
            let keyword = document.createElement('td');

            let tdImage = document.createElement('td');
            let imageDiv = document.createElement('div');
            imageDiv.className = "imageDiv";
            let image = document.createElement('img');
            image.src = 'data:image/png;base64,' + article.encodedImage;
            imageDiv.appendChild(image);
            tdImage.appendChild(imageDiv);
            tr.appendChild(tdImage);

            let nodeItem = document.createTextNode(article.name);
            let nodeDescription = document.createTextNode(article.description);
            let nodeMinimumPrice = document.createTextNode(article.minimumPrice);
            let nodeKeyWord = document.createTextNode(article.keyWord);
            item.appendChild(nodeItem);
            description.appendChild(nodeDescription);
            minimumPrice.appendChild(nodeMinimumPrice);
            keyword.appendChild(nodeKeyWord);
            tr.appendChild(item);
            tr.appendChild(description);
            tr.appendChild(minimumPrice);
            tr.appendChild(keyword);
            self.tbody_auction_information.appendChild(tr);
        });
        self.close_page_button.addEventListener("click", (e) => {
            self.div_open_auction_page.style.display = 'none';
            self.resetBothTable();
        });
    }
    /*Questa funziona viene chiamata nel caso in cui l'esisto dell'offerta fatta è positivo, quindi
    * non fa altro che aggiornare la tabella delle offerte aggiungendo una riga. Aggiorna anche
    * il td (cella relativa all'ultima offerta) della tabella da cui l'asta era stata aperta*/
    this.addAcceptedOffer = function (offer){
        let tr = document.createElement('tr');
        let rows = self.tbody_auction_offer.getElementsByTagName("tr");
        if (rows.length!==0 && rows[0].classList.contains("to_be_removed"))
        {
            self.tbody_auction_offer.removeChild(rows[0]);
        }

        let tdOffer = document.createElement('td');
        let tdUsername = document.createElement('td');
        let tdDate = document.createElement('td');
        let nodeOffer = document.createTextNode(offer.price);
        let nodeUsername = document.createTextNode(offer.userOffer);
        let nodeDate = document.createTextNode(offer.datatime);
        tdOffer.appendChild(nodeOffer);
        tdUsername.appendChild(nodeUsername);
        tdDate.appendChild(nodeDate);
        tr.appendChild(tdUsername);
        tr.appendChild(tdDate);
        tr.appendChild(tdOffer);
        self.tbody_auction_offer.insertBefore(tr, self.tbody_auction_offer.firstChild);

        self.buyManager.buy__keyword_table.updateLastOfferCell(auctionId, offer.price);
        self.buyManager.buy__recent_visited_auction.updateLastOfferCell(auctionId, offer.price);
        self.title.innerHTML="Minimum rise: "+ minimumRise + ", actual offer:" + offer.price;
        self.setLastOffer(offer.price);
    };
    /*Reset delle tabelle, da chiamare ogni volta che la sezione viene chiusa*/
    this.resetBothTable = function (){
        while (self.tbody_auction_information.firstChild) {
            self.tbody_auction_information.removeChild(self.tbody_auction_information.firstChild);
        }
        while (self.tbody_auction_offer.firstChild) {
            self.tbody_auction_offer.removeChild(self.tbody_auction_offer.firstChild);
        }
        this.setLastOffer(-1);
        this.setIdAuction(NaN);
        this.setMinimumPrice(0);
    };
}
/*Oggetto dedicato alla creazione dell'ultima tabella della pagina con all'interno le aste vinte dall'utente e quale informazione
* relativa ad esse*/
function Buy__close_and_won_auctionConstructor(tbody_, buyManager_) {
    this.tbody=tbody_;
    this.buyManager = buyManager_;
    let self = this;
    /*Chiedo al server quali sono le aste da stampare*/
    this.show = function (){
        makeCall("GET", "closeAndWonAuction" , null, (req) => {
                switch (req.status) {
                    case 200:
                        self.print(JSON.parse(req.responseText));
                        break;
                    case 500:
                        error_prompt.show(req.responseText);
                        self.print(null);
                        break;
                    case 401:
                        pageOrchestrator.do_logout();
                        break;
                    default:
                        self.print(null);
                        error_prompt.show(req.status);
                }
        });
    }
    /*Semplice stampa di una tabella, senza alcun listener */
    this.print = function (list){
        if(list  === null || list.length<=0){
            let tr = document.createElement('tr');
            let td = document.createElement('td');
            td.setAttribute("colspan", "6");
            td.setAttribute("style", "text-align: center;");
            td.textContent = "No won auction";
            tr.classList.add("to_be_removed");
            tr.appendChild(td);
            self.tbody.appendChild(tr);
        }
        else {
            list.forEach(auction => {
                let tr = document.createElement('tr');

                let tdEndDate = document.createElement('td');
                let nodeEndDate = document.createTextNode(auction.endDate);
                tdEndDate.appendChild(nodeEndDate);
                tr.appendChild(tdEndDate);

                let tdLastOffer = document.createElement('td');
                let nodeLastOffer = document.createTextNode(auction.maxOffer);
                tdLastOffer.appendChild(nodeLastOffer);
                tr.appendChild(tdLastOffer);

                //Celle in cui mettere le "subTable" con le informazioni sui vari elementi
                let tdItem = document.createElement('td');
                let tdMinimumPrice = document.createElement('td');
                let tdKeyword = document.createElement('td');
                let tdDescription = document.createElement('td');

                let tableItem = document.createElement('table');
                let tableKeyword = document.createElement('table');
                let tableDescription = document.createElement('table');
                let tableMinimumPrice = document.createElement('table');

                auction.listOfArticle.forEach(article => {

                    let rowInSubTableName = document.createElement('tr');
                    let cellInSubTableName = document.createElement('td');
                    let nodeTextSubTableName = document.createTextNode(article.name);
                    cellInSubTableName.appendChild(nodeTextSubTableName);
                    rowInSubTableName.appendChild(cellInSubTableName);
                    tableItem.appendChild(rowInSubTableName);
                    let rowInSubTableKeyword = document.createElement('tr');
                    let cellInSubTableKeyword = document.createElement('td');
                    let nodeTextSubTableKeyword = document.createTextNode(article.keyWord);
                    cellInSubTableKeyword.appendChild(nodeTextSubTableKeyword);
                    rowInSubTableKeyword.appendChild(cellInSubTableKeyword);
                    tableKeyword.appendChild(rowInSubTableKeyword);
                    let rowInSubTableDescription = document.createElement('tr');
                    let cellInSubTableDescription = document.createElement('td');
                    let nodeTextSubTableDescription = document.createTextNode(article.description);
                    cellInSubTableDescription.appendChild(nodeTextSubTableDescription);
                    rowInSubTableDescription.appendChild(cellInSubTableDescription);
                    tableDescription.appendChild(rowInSubTableDescription);
                    let rowInSubTableMinimumPrice = document.createElement('tr');
                    let cellInSubTableMinimumPrice = document.createElement('td');
                    let nodeTextSubTableMinimumPrice = document.createTextNode(article.minimumPrice);
                    cellInSubTableMinimumPrice.appendChild(nodeTextSubTableMinimumPrice);
                    rowInSubTableMinimumPrice.appendChild(cellInSubTableMinimumPrice);
                    tableMinimumPrice.appendChild(rowInSubTableMinimumPrice);

                });

                tdItem.appendChild(tableItem);
                tdDescription.appendChild(tableDescription);
                tdKeyword.appendChild(tableKeyword);
                tdMinimumPrice.appendChild(tableMinimumPrice);

                tr.appendChild(tdItem);
                tr.appendChild(tdDescription);
                tr.appendChild(tdMinimumPrice);
                tr.appendChild(tdKeyword);

                self.tbody.appendChild(tr);
            });
            const tableCells = document.querySelectorAll('#table_closeAndWonAuction td');
            tableCells.forEach(cell => {
                cell.style.borderBottom = '1px solid black';
                cell.style.borderTop = '1px solid black';
            });
        }
    };
    /*Reset della tabella*/
    this.reset = function (){
        while (self.tbody.firstChild) {
            self.tbody.removeChild(self.tbody.firstChild);
        }
    }
}