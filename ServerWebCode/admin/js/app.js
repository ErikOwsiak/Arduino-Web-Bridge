
var app = {
	
	init(){
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
	},
	
	menuItemClick(){
		try {
			console.log(this);
			app.topMenu[`${this.id}Click`]();
			$("#menuBox").fadeOut(200);
		}catch(e){
			console.log(e);
		}
	},
	
	btnAppMenuClick(){
		console.log(this);
	},
	
	topMenu: {
		btnSendSmsClick(){
			$("#smsBox").fadeIn(200);
			$("#btnPushSms2Fone").off().click(app.phone.sendSmsToPhone);
		},
		btnPhoneDateTimeClick(){
			console.log("btnPhoneDateTime...");
			$.post(`/exeapi/PhoneDateTime`, {}, (txt) => { 
					console.log(txt);
				});
		},
		btnScanUartsClick(){
			console.log("btnScanUarts...");
			$.post(`/exeapi/ScanUarts`, {}, (jobj) => { 
					console.log(jobj.apiReturnVal);
					alert(jobj.apiReturnVal);
				}); 
		},
		btnScanBluetoothClick(){
			console.log("btnScanBluetooth...");
			$.post(`/exeapi/ScanBluetooth`, {}, (jobj) => { 
					console.log(jobj);
					if(parseInt(jobj.apiReturnCode) != 0)
						alert(jobj.apiReturnMsg);
					else
						app.processBluetoothMsg(jobj.apiReturnVal);
				}); 
		},
		btnPeekUartClick(){
			console.log("btnPeekUartClick...");
			$.post(`/exeapi/PeekUartBuffer`, {}, (jobj) => { 
					console.log(jobj.apiReturnVal);
					let arr = jobj.apiReturnVal.split(";")
					let d = new Date();
					d.setTime(parseInt(arr[0])/1000);
					console.log(d.toString());
					/*if(parseInt(jobj.apiReturnCode) == 0)
						alert(jobj.apiReturnVal);*/
					setTimeout(app.btnPeekUartClick, 1000);
				});
		}
	},
	
	phone: {
		sendSmsToPhone(){
			let ondone = (jobj) => {
					$("#smsBox .fb-box").html(jobj.apiReturnVal);
				};
			let TNUM = $("#txtTelNum").val(),
				SMSTXT = $("#txtSMSText").val();
			$.post(`/exeapi/SendSms`, {TNUM, SMSTXT}, ondone);
		}
	},
	
	processBluetoothMsg(txt){
		let arr = txt.trim().split("|");
		console.log(arr);
		alert(arr.join("\n"));
	}
	
};


/* on doc load */
document.addEventListener("DOMContentLoaded", app.init);
