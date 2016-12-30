package gxp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class GxpServiceController {
	
	@Value("${nge.disney.gs.host}")
	private String guestServiceHost;

	@Value("${nge.disney.gs.context.root}")
	private String guestServiceContextRoot;

	@Value("${nge.disney.gs.storeguestkeys.endpoint}")
	private String guestServiceEndpoint;
	
	@Value("${nge.disney.gs.fetchxid.endpoint}")
	private String guestServiceFetchxidEndpoint;
	
	@RequestMapping(value = "/invokegxpservice", method = RequestMethod.POST,consumes="text/plain")
	public ResponseEntity<String> storeGuestKeys(@RequestParam("swid") String swid,@RequestParam("guestIdType") String guestIdType,@RequestParam("guestIdValue") String guestIdValue,@RequestHeader("from") String listener){
		String id;
		ResponseEntity<String> resp;
		try{
			
			String xid = invokeGuestServiceToFetchXid(swid);
			
			if(xid != null && !xid.equals("")){
				invokeGuestServiceApp(xid, guestIdType, guestIdValue);
				invokeGuestServiceApp(xid, "swid", swid);
			}else{
				System.out.println(" else of FastPass process");
				invokeGuestServiceApp(xid, guestIdType, guestIdValue);
			}
			resp = new ResponseEntity<String>("success",HttpStatus.OK);
		}catch (Exception e) {
			resp = new ResponseEntity<String>("failed",HttpStatus.INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		}
		return resp;
	}
	
	private String invokeGuestServiceToFetchXid(String swid) {
		String xid = "";
		try{
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_PLAIN);
			headers.set("from", "GXP");
			
			HttpEntity<String> entity = new HttpEntity<String>(swid, headers);
	
			String serviceUri = guestServiceHost + "/" + guestServiceContextRoot + guestServiceFetchxidEndpoint + "?swid="+swid;
	
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> resp = restTemplate.getForEntity(serviceUri, String.class);
			xid = resp.getBody();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return xid;
	}
	
	private void invokeGuestServiceApp(String xid,String gxpLinkIdType,String gxpLinkIdValue) {
		
		try{
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_PLAIN);
			headers.set("from", "GXP");
			
			HttpEntity<String> entity = new HttpEntity<String>(xid, headers);
	
			String serviceUri = guestServiceHost + "/" + guestServiceContextRoot + guestServiceEndpoint + "?xid="+xid+"&guestIdType="+gxpLinkIdType+"&guestIdValue="+gxpLinkIdValue;
	
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getForEntity(serviceUri,String.class);
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
