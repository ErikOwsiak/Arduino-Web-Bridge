
var HtmlT = {

	blueDevItem(arr){
		let id = arr[1].trim().replace(/\:/g, "_");
		return `<div id="${id}" class="blue-dev-item"><div>${arr[0]}</div>` +
			`<div>${arr[1].trim()}</div></div>`;
	},
	
	get smsBox(){
		return `<div id="smsBox" class="op-box sms-box">` +
			`<div class="op-box-hdr">Send Sms</div>` +
			`<div class="op-box-ln">` +
				`<input type="text" id="txtTelNum" maxlength="24" placeholder="phone number" /></div>` +
			`<div class="op-box-ln">` + 
				`<input type="text" id="txtSMSText" maxlength="140" placeholder="sms text" /></div>` + 
			`<div class="op-box-ln">` +
				`<input type="button" id="btnPushSms2Fone" value="Send SMS" /></div>` +
			`<div class="fb-box"></div></div>`;
	},
	
	get blueDevs(){	
		return `<div id="blueDevs" class="op-box">` +
			`<div class="op-box-hdr">Bluetooth Devices</div>` +
			`<div class="blue-devs-lst">` + 
			`<div class="loading-data"></div></div>` +
			`<div class="bt-scan"></div></div>`;
	},
	
	blueDevReader(i){
		let title = i,
			btn = `<div><input id="btnReadBlueDev" type="button" value="Start Read" />` + 
				`<input id="btnStopReadBlueDev" type="button" value="Stop Read" /></div>`;
		return `<div id="blueDevScan" class="op-box">` + 
			`<div class="col-left"><div class="op-box-hdr">${title}</div>` + 
			`<div class="blue-dev-btns">${btn}</div></div>` +
			`<div id="colRight" class="col-right"></div></div>`;	
	},
	
	uartMsg(i){
		let str = i.returnVal.split(";")[0]; 
			ts = parseInt(str), d = new Date();
		d.setTime(ts);
		return `<div class="uart-msg"><div>dts:&nbsp;${d.toLocaleString()}</div>` + 
			`<div>returnCode:&nbsp;${i.returnCode}</div>` + 
			`<div>returnMsg:&nbsp;${i.returnMsg}</div>` + 
			`<div>returnVal:&nbsp;${i.returnVal}</div></div>`;
	}
	
};
