function getDNS(hostname){
	var dns_server;
	switch(hostname){
		case "Razorbreak": dns_server="razorbreak.servegame.com";
			break;
		/*	Add your case below following this template:
		case "your_name": dns_server="your_url";
			break;
		*/
		default: dns_server=getServerIP();
	}
	return dns_server;
}