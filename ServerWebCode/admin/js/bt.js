
var bt = {

	name: null,
	adr: null,
	ADR: null,
	readTimeOut: null,

	init(n, a){
		bt.name = n;
		bt.adr = a;
	},

	startScan(){
		
	},
	
	click(){
		let adr = this.id.replace(/_/g, ":"),
			html = HtmlT.blueDevReader(adr);
		app.vp.html(html);
		$("#blueDevScan").fadeIn();
		bt.startBlueDevBuffer(adr);
	},
	
	startBlueDevBuffer(ADR){
		bt.ADR = ADR;
		$.post(`/exe/StartBlueDev`, {ADR}, (jobj) => {
				console.log(jobj);
				$("#btnReadBlueDev").off().click(bt.readBlueDevBuffer);
				$("#btnStopReadBlueDev").off().click(bt.stopReadBlueDevBuffer);
			});
	},
	
	readBlueDevBuffer(){
		/* - - */
		$.post(`/exe/ReadBlueDevBuffer`, {"ADR": bt.ADR}, (jobj) => {
				let html = HtmlT.uartMsg(jobj);
				$("#colRight").append(html);
				bt.readTimeOut = setTimeout(bt.readBlueDevBuffer, 800);
			});
		/* - - */
	},
	
	stopReadBlueDevBuffer(){
		$("#blueDevScan .col-right").html("");
		clearTimeout(bt.readTimeOut);
	}
	
};
