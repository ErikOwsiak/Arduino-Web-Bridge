
var app = {
	
	cookieJar: {},
	vp: null,
	
	init(){
		/* - - */
		app.vp = $("#viewPort");
		/* - - */
		$(".menu-btn").click(app.menuItemClick);
		/* - - */
		$("#btnAppMenu").click(() => {
				$("#menuBox").fadeIn();
			});
		/* - - */
		$("#menuBoxX").click(() => {
				$("#menuBox").fadeOut();
			});
		/* - - */
		app.loadCookies();
		let {devMaker, devModel} = app.cookieJar;
		$("#devStr").html(`${devMaker} : ${devModel}`);
	},
	
	menuItemClick(){
		try {
			$("#menuBox").fadeOut(100);
			app.topMenu[`${this.id}Click`]();
		}catch(e){
			console.log(e);
		}
	},
	
	btnAppMenuClick(){
		console.log(this);
	},
	
	topMenu: {
		btnSendSmsClick(){
			app.vp.html(HtmlT.smsBox);
			$("#smsBox").fadeIn();
			$("#btnPushSms2Fone").click(app.phone.sendSmsToPhone);
		},
		btnPhoneDateTimeClick(){
			$.post(`/exeapi/PhoneDateTime`, {}, (txt) => { 
					console.log(txt);
				});
		},
		btnScanUartsClick(){
			$.post(`/exeapi/ScanUarts`, {}, (jobj) => { 
					alert(jobj.apiReturnVal);
				}); 
		},
		btnScanBluetoothClick(){
			$(".op-box").fadeOut();
			$.post(`/exeapi/ScanBluetooth`, {}, (jobj) => {
					if(parseInt(jobj.apiReturnCode) != 0)
						alert(jobj.apiReturnMsg);
					else
						app.processBluetoothMsg(jobj.apiReturnVal);
				});
		},
		btnPeekUartClick(){
			$.post(`/exeapi/PeekUartBuffer`, {}, (jobj) => {
					let d = new Date(),
						arr = jobj.apiReturnVal.split(";");
					d.setTime(parseInt(arr[0])/1000);
					setTimeout(app.btnPeekUartClick, 1000);
				});
		}
	},
	
	phone: {
		sendSmsToPhone(){
			$("#smsBox div.fb-box").html("Sending SMS...");
			let ondone = (jobj) => {
					$("#smsBox div.fb-box").html(jobj.apiReturnVal);
				};
			let TNUM = $("#txtTelNum").val(),
					SMSTXT = $("#txtSMSText").val();
			$.post(`/exeapi/SendSms`, {TNUM, SMSTXT}, ondone);
		}
	},
	
	//$("#blueDevs, #blueDevs .blue-devs-lst").fadeIn();
	processBluetoothMsg(txt){
		app.vp.html(HtmlT.blueDevs);
		$("#blueDevs").fadeIn();
		let html= "", arr = txt.trim().split("|");
		$("#blueDevs .blue-devs-lst").html("");
		arr.forEach((str) => {
				if(!str)
					return;
				html = HtmlT.blueDevItem(str.split(";"));
				$("#blueDevs .blue-devs-lst").append(html);
			});
		/* - - */
		$(".blue-dev-item").off().click(bt.click);
	},
	
	loadCookies(){
		let t = null,
			arr = document.cookie.split(";");
		arr.forEach((kv) => {
				t = kv.split("=");
				app.cookieJar[t[0].trim()] = t[1].trim();
			}); 
	},
	
	cookie(key){
		return app.cookieJar[key];
	}
	
};


/* on doc load */
document.addEventListener("DOMContentLoaded", app.init);
