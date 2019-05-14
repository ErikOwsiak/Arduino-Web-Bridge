
var bt = {

	name: null,
	adr: null,

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
		/*$("#blueDevs .blue-devs-lst").fadeOut();
		$("#blueDevs .bt-scan").fadeIn();*/
	},
	
	startBlueDevBuffer(ADR){
		$.post(`/exe/StartBlueDev`, {ADR}, (jobj) => {
				console.log(job);
			});
	},
	
	readBlueDevBuffer(){
		$.post(`/exe/PeekUartBuffer`, {}, (jobj) => {
				let d = new Date(),
					arr = jobj.apiReturnVal.split(";");
				d.setTime(parseInt(arr[0])/1000);
				setTimeout(app.btnPeekUartClick, 1000);
			});
	}
	
};
