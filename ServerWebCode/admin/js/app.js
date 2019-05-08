
var app = {
	
	init(){
		$(".menu > div").off().click(app.menuClick);
	},
	
	menuClick(){
		try {
			app[`${this.id}Click`]();
		}catch(e){
			console.log(e);
		}
	},
	
	/* btnSendSms */
	btnSendSmsClick(){
		let ondone = function(txt){
				console.log(txt);
			};
		console.log("send sms...");
		let msg = encodeURIComponent("Hello, World!");
		$.post(`/exeapi/SendSms/883279496/${msg}`, {}, ondone); 
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
	},
	
	processBluetoothMsg(txt){
		let arr = txt.trim().split("|");
		console.log(arr);
		alert(arr.join("\n"));
	}
	
};


/* on doc load */
document.addEventListener("DOMContentLoaded", app.init);
