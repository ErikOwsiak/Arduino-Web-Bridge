
var bt = {

	name: null,
	adr: null,
	ADR: null,
	readerTimeout: null,

	init(n, a){
		bt.name = n;
		bt.adr = a;
	},

	startScan(){	
	},
	
	click(){
		/* - - */
		let onfadein = function(){
				$("#btnClearRead").off().click(bt.clearReads);
				$("#btnStartStop").off().click(bt.startStopRead);
				$("#btnConnectDisconnect").off().click(bt.connectDisconnect);
			};
		/* - - */
		bt.ADR = this.id.replace(/_/g, ":");
		app.vp.html(HtmlT.blueDevReader(bt.ADR));
		$("#blueDevScan").fadeIn(onfadein);
		bt.checkBlueDev();
	},
	
	clearReads(){
		$("#colRight").html("");
	},
	
	connectDisconnect(){
		let refThis = this;
		let ondone = function(jobj){
				$(refThis).removeClass("btn-off");
				$(refThis).removeAttr("disabled");
				$("#blueDevScan .fb-box-a").html(jobj.returnVal);
				if(jobj.returnVal == "Connected"){
					bt.btnStartStopActive();
					$("#btnConnectDisconnect").val("Disconnect");
					$("#btnConnectDisconnect").css("color", "red");
				}else{
					$("#btnConnectDisconnect").val("Connect");
					$("#btnConnectDisconnect").css("color", "green");
				}
			};
		let bits = parseInt($(this).attr("bits")),
			url = (bits == 0) ? `/exe/ConnectBlueDev` : `/exe/DisconnectBlueDev`;
		$(this).addClass("btn-off");
		$(this).attr("disabled", "1");
		$(this).attr("bits", (bits == 0) ? 1 : 0);
		$.post(url, {"ADR": bt.ADR}, ondone);
	},
	
	startStopRead(){
		try {
			let mode = $(this).attr("mode");
			bt[`${mode}Reader`]();
		}catch(e){
			console.log(e);
		}
	},
	
	startReader(){
		$("#btnStartStop").attr("mode", "stop");
		$("#btnStartStop").css("color", "red");
		$("#btnStartStop").val("Stop Reading");
		$.post(`/exe/ReadBlueDevBuffer`, {"ADR": bt.ADR}, (jobj) => {
				$("#colRight").append(HtmlT.uartMsg(jobj));
				bt.readerTimeout = setTimeout(bt.startReader, 800);
			});
	},
	
	stopReader(){
		$("#btnStartStop").attr("mode", "start");
		$("#btnStartStop").css("color", "green");
		$("#btnStartStop").val("Start Reading");
		clearTimeout(bt.readerTimeout);
	},
	
	checkBlueDev(){
		let ondone = function(jobj){
				$("#blueDevScan .fb-box-a").html(jobj.returnVal);
			};
		$.post("/exe/CheckBlueDev", {"ADR": bt.ADR}, ondone);
	},
	
	btnStartStopActive(){
		$("#btnStartStop").removeClass("btn-off");
		$("#btnStartStop").removeAttr("disabled");
	}
	
};
