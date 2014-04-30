package com.howell.webcam;

import java.io.Serializable;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.util.Log;

@SuppressWarnings("serial")
public class SoapManager implements Serializable {

    private static String sNameSpace = "http://www.haoweis.com/HomeServices/MCU/";

    private static String sEndPoint = "http://www.haoweis.com:8800/HomeService/HomeMCUService.svc?xsd=xsd0";

    //private static String sSoapAction = null;

    private static SoapManager sInstance = new SoapManager();

    private LoginRequest mLoginRequest;
    private LoginResponse mLoginResponse;
    private GetNATServerRes mGetNATServerRes;

    private ArrayList<NodeDetails> nodeDetails = new ArrayList<NodeDetails>();
    private static final int REPLAYTIME = 10 * 24 * 60 * 60 * 1000;

    private SoapManager() {

    }

    public static SoapManager getInstance() {
        return sInstance;
    }
    

    public LoginRequest getLoginRequest() {
        return mLoginRequest;
    }
//
    public void setLoginRequest(LoginRequest loginRequest) {
        mLoginRequest = loginRequest;
    }

    public LoginResponse getLoginResponse() {
        return mLoginResponse;
    }

    public void setLoginResponse(LoginResponse loginResponse) {
        mLoginResponse = loginResponse;
    }

    public GetNATServerRes getLocalGetNATServerRes() {
		return mGetNATServerRes;
	}

	public void setGetNATServerRes(GetNATServerRes mGetNATServerRes) {
		this.mGetNATServerRes = mGetNATServerRes;
	}
	
	public ArrayList<NodeDetails> getNodeDetails() {
		return nodeDetails;
	}

	public SoapObject initEnvelopAndTransport(SoapObject rpc , String sSoapAction) {

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER10);

        envelope.bodyOut = rpc;
        envelope.dotNet = true;
        envelope.encodingStyle = "UTF-8";
        envelope.setOutputSoapObject(rpc);

        HttpTransportSE transport;
		transport = new HttpTransportSE(sEndPoint);
		transport.debug = true;
		try {
		    transport.call(sSoapAction, envelope);
		} catch (SocketTimeoutException e) {
			Log.e("", "SocketTimeoutException");
		    e.printStackTrace();
		} catch (Exception e) {
			Log.e("", "Exception");
		    e.printStackTrace();
		}

        SoapObject soapObject = (SoapObject) envelope.bodyIn;
        return soapObject;
    }

//	MCU登录
    public LoginResponse getUserLoginRes(LoginRequest loginRequest) {
    	Log.e("SoapManager", "getUserLoginRes");
        setLoginRequest(loginRequest);
        mLoginResponse = new LoginResponse();
        SoapObject rpc = new SoapObject(sNameSpace, "userLoginReq");

        rpc.addProperty("Account", loginRequest.getAccount());
        rpc.addProperty("PwdType", loginRequest.getPwdType());
        rpc.addProperty("Password", loginRequest.getPassword());
        rpc.addProperty("Version", loginRequest.getVersion());

        SoapObject object = initEnvelopAndTransport(rpc,"http://www.haoweis.com/HomeServices/MCU/userLogin");

//        if (object == null) {
//            return null;
//        }
        try{
	        Object result = object.getProperty("result");
	        mLoginResponse.setResult(result.toString());
        }catch (Exception e) {
			// TODO: handle exception
		}
	    if (mLoginResponse.getResult().toString().equals("OK")) {
	    	try{
		        Object session = object.getProperty("LoginSession");
		        mLoginResponse.setLoginSession(session.toString());
	    	}catch (Exception e) {
				// TODO: handle exception
	    		mLoginResponse.setLoginSession("");
			}
	        try{
			    Object nodeList = object.getProperty("NodeList");
			    mLoginResponse.setNodeList(AnalyzingDoNetOutput.analyzing(nodeList.toString()));
	        }catch (Exception e) {
					// TODO: handle exception
	        	ArrayList<Device> list = new ArrayList<Device>();
	            mLoginResponse.setNodeList(list);
	        }
	        try{
		        Object username = object.getProperty("Username");
	            mLoginResponse.setUsername(username.toString());
	        }catch (Exception e) {
					// TODO: handle exception
	        	mLoginResponse.setUsername("");
			}
	        try{
	            Object Account = object.getProperty("Account");
	            mLoginResponse.setAccount(Account.toString());
	        }catch (Exception e) {
					// TODO: handle exception
	        	mLoginResponse.setAccount("");
	        }
	    }
       
        return mLoginResponse;
    }

    public boolean reLogin(){
    	mLoginResponse = getUserLoginRes(mLoginRequest);
    	return mLoginResponse.getResult().equals("OK") ? true : false;
    } 
    
    
    // 设备状态查询
    public QueryDeviceRes getQueryDeviceRes(QueryDeviceReq req) {
    	Log.e("SoapManager", "getQueryDeviceRes");
        QueryDeviceRes queryDeviceRes = new QueryDeviceRes();
        SoapObject rpc = new SoapObject(sNameSpace, "queryDeviceReq");
        rpc.addProperty("Account", req.getAccount());
        rpc.addProperty("LoginSession", req.getLoginSession());
        rpc.addProperty("DevID", req.getDevID());
        SoapObject object = initEnvelopAndTransport(rpc,"http://www.haoweis.com/HomeServices/MCU/queryDevice");
        try{
	        Object result = object.getProperty("result");
	        
	        if(result.toString().equals("SessionExpired")){
	        	Log.d("------------->>>>", "result = " + result.toString());
	        	reLogin();
	        	return null;
	        }
	        queryDeviceRes.setResult(result.toString());
	        //System.out.println("queryDeviceRes:"+queryDeviceRes.getResult());
	        SoapObject NodeList = (SoapObject)object.getProperty("NodeList");
	        System.out.println("queryDevice NodeList:"+NodeList.toString());
	        //System.out.println(req.getDevID().toString());
	        if(req.getDevID() != null){
		        SoapObject NodeDetails = (SoapObject)NodeList.getProperty("NodeDetails");
		        System.out.println("NodeDetails:"+NodeDetails.toString());
		        Object DevID = NodeDetails.getProperty("DevID");
		        queryDeviceRes.setDevID(DevID.toString());
		        
		        Object ChannelNo = NodeDetails.getProperty("ChannelNo");
		        queryDeviceRes.setChannelNo(Integer.valueOf(ChannelNo.toString()));
		       
		        Object Name = NodeDetails.getProperty("Name");
		        queryDeviceRes.setName(Name.toString());
		        
		        Object OnLine = NodeDetails.getProperty("OnLine");
		        queryDeviceRes.setOnLine(Boolean.parseBoolean(OnLine.toString()));
		        
		        Object PtzFlag = NodeDetails.getProperty("PtzFlag");
		        queryDeviceRes.setPtzFlag(Boolean.parseBoolean(PtzFlag.toString()));
		        
		        Object SecurityArea = NodeDetails.getProperty("SecurityArea");
		        queryDeviceRes.setSecurityArea(Integer.valueOf(SecurityArea.toString()));
		        
		        Object EStoreFlag = NodeDetails.getProperty("EStoreFlag");
		        queryDeviceRes.seteStoreFlag(Boolean.parseBoolean(EStoreFlag.toString()));
		       
		        Object UpnpIP = NodeDetails.getProperty("UpnpIP");
		        queryDeviceRes.setUpnpIP(UpnpIP.toString());
		        
		        Object UpnpPort = NodeDetails.getProperty("UpnpPort");
		        queryDeviceRes.setUpnpPort(Integer.valueOf(UpnpPort.toString()));
		        
		        Object DevVer = NodeDetails.getProperty("DevVer");
		        queryDeviceRes.setDevVer(DevVer.toString());
		        
		        Object CurVideoNum = NodeDetails.getProperty("CurVideoNum");
		        queryDeviceRes.setCurVideoNum(Integer.valueOf(CurVideoNum.toString()));
		       
		        Object LastUpdated = NodeDetails.getProperty("LastUpdated");
		        queryDeviceRes.setLastUpdated(LastUpdated.toString());
		        
		        Object SMSSubscribedFlag = NodeDetails.getProperty("SMSSubscribedFlag");
		        queryDeviceRes.setsMSSubscribedFlag(Integer.valueOf(SMSSubscribedFlag.toString()));
		        
		        Object EMailSubscribedFlag = NodeDetails.getProperty("EMailSubscribedFlag");
		        queryDeviceRes.seteMailSubscribedFlag(Integer.valueOf(EMailSubscribedFlag.toString()));
		       
		        Object SharingFlag = NodeDetails.getProperty("SharingFlag");
		        queryDeviceRes.setSharingFlag(Integer.valueOf(SharingFlag.toString()));
		        
		        Object ApplePushSubscribedFlag = NodeDetails.getProperty("ApplePushSubscribedFlag");
		        queryDeviceRes.setApplePushSubscribedFlag(Integer.valueOf(ApplePushSubscribedFlag.toString()));
		        
		        Object AndroidPushSubscribedFlag = NodeDetails.getProperty("AndroidPushSubscribedFlag");
		        queryDeviceRes.setAndroidPushSubscribedFlag(Integer.valueOf(AndroidPushSubscribedFlag.toString()));
	        }else{
	        	if(nodeDetails.size() > 0)
	        		nodeDetails.clear();
	        	for(int i = 0 ;i<NodeList.getPropertyCount();i++){
	        		//System.out.println(NodeList.getProperty(i).toString());
	        		SoapObject NodeDetails = (SoapObject)NodeList.getProperty(i);
	        		//System.out.println(NodeDetails.toString());
	        		NodeDetails node = new NodeDetails(NodeDetails.getProperty("DevID").toString(),Integer.valueOf(NodeDetails.getProperty("ChannelNo").toString())
	        				,NodeDetails.getProperty("Name").toString(),Boolean.parseBoolean(NodeDetails.getProperty("OnLine").toString()),Boolean.parseBoolean(NodeDetails.getProperty("PtzFlag").toString())
	        				,Integer.valueOf(NodeDetails.getProperty("SecurityArea").toString()),Boolean.parseBoolean(NodeDetails.getProperty("EStoreFlag").toString()),NodeDetails.getProperty("UpnpIP").toString()
	        				,Integer.valueOf(NodeDetails.getProperty("UpnpPort").toString()),NodeDetails.getProperty("DevVer").toString(),Integer.valueOf(NodeDetails.getProperty("CurVideoNum").toString())
	        				,NodeDetails.getProperty("LastUpdated").toString(),Integer.valueOf(NodeDetails.getProperty("SMSSubscribedFlag").toString()),Integer.valueOf(NodeDetails.getProperty("EMailSubscribedFlag").toString())
	        				,Integer.valueOf(NodeDetails.getProperty("SharingFlag").toString()),Integer.valueOf(NodeDetails.getProperty("ApplePushSubscribedFlag").toString()),Integer.valueOf(NodeDetails.getProperty("AndroidPushSubscribedFlag").toString())
	        				,Integer.valueOf(NodeDetails.getProperty("InfraredFlag").toString()),Integer.valueOf(NodeDetails.getProperty("WirelessFlag").toString()));
	        		//System.out.println("node : "+i+" :"+node.toString());
	        		if(node.getWirelessFlag() == 0){
	        			node.setIntensity(0);
	        		}else{
		        		SoapObject wirelessNetwork = (SoapObject)NodeDetails.getProperty("WirelessNetwork");
		        		System.out.println(wirelessNetwork.toString());
		        		Object intensity = wirelessNetwork.getProperty("Intensity");
		        		System.out.println(intensity.toString());
		        		node.setIntensity(Integer.valueOf(intensity.toString()));
	        		}
	        		nodeDetails.add(node);
	        	}
	        }
//	        Object NodeList = object.getProperty("NodeList");
//	        queryDeviceRes.setNodeDetails(new NodeDetails(AnalyzingDoNetOutput
//	                .analyzingIPandPort(NodeList.toString())[0].substring(7),
//	                Integer.parseInt(AnalyzingDoNetOutput
//	                        .analyzingIPandPort(NodeList.toString())[1]
//	                        .substring(9))));
	        //System.out.println("queryDevice:"+queryDeviceRes.toString());
        }catch (Exception e) {
			// TODO: handle exception
        	System.out.println("queryDevice:Crash");
		}
        return queryDeviceRes;
    }

    // 请求视频流
    public InviteResponse getIviteRes(InviteRequest req) {
    	Log.e("SoapManager", "getIviteRes");
        InviteResponse inviteRes = new InviteResponse();
        SoapObject rpc = new SoapObject(sNameSpace, "inviteReq");
        rpc.addProperty("Account", req.getAccount());
        rpc.addProperty("LoginSession", req.getLoginSession());
        rpc.addProperty("DevID", req.getDevID());
        rpc.addProperty("ChannelNo", req.getChannelNo());
        rpc.addProperty("StreamType", req.getStreamType());
        rpc.addProperty("DialogID", req.getDialogID());
        rpc.addProperty("SDPMessage", req.getSDPMessage());
        SoapObject object = initEnvelopAndTransport(rpc,null);
        try{
	        Object result = object.getProperty("result");
	        if(result.toString().equals("SessionExpired")){
	        	Log.d("------------->>>>", "result = " + result.toString());
	        	reLogin();
	        	return null;
	        }
	        inviteRes.setResult(result.toString());
	        Object dialogID = object.getProperty("DialogID");
	        inviteRes.setDialogID(dialogID.toString());
	
	        Object SDPMessage = object.getProperty("SDPMessage");
	        inviteRes.setSDPMessage(SDPMessage.toString());
        }catch (Exception e) {
			// TODO: handle exception
		}
        return inviteRes;
    }
    
//    视频流断开
    public ByeResponse getByeRes(ByeRequest req) {
    	Log.e("SoapManager", "getByeRes");
        ByeResponse byeRes = new ByeResponse();
        SoapObject rpc = new SoapObject(sNameSpace, "byeReq");
        rpc.addProperty("Account", req.getAccount());
        rpc.addProperty("LoginSession", req.getLoginSession());
        rpc.addProperty("DevID", req.getDevID());
        rpc.addProperty("ChannelNo", req.getChannelNo());
        rpc.addProperty("StreamType", req.getStreamType());
        rpc.addProperty("DialogID", req.getDialogID());
        SoapObject object = initEnvelopAndTransport(rpc,null);
        try{
	        Object result = object.getProperty("result");
	        if(result.toString().equals("SessionExpired")){
	        	Log.d("------------->>>>", "result = " + result.toString());
	        	reLogin();
	        	return null;
	        }
	        byeRes.setResult(result.toString());
        }catch (Exception e) {
			// TODO: handle exception
		}
        return byeRes;
    }

//    查询账户信息
    public AccountResponse getAccountRes(AccountRequest req) {
    	Log.e("SoapManager", "getAccountRes");
        AccountResponse accountRes = new AccountResponse();
        SoapObject rpc = new SoapObject(sNameSpace, "getAccountReq");
        rpc.addProperty("Account", req.getAccount());
        rpc.addProperty("LoginSession", req.getLoginSession());

        SoapObject object = initEnvelopAndTransport(rpc,null);
        try{
	        Log.e("-------------->>>>>", "object = " + object.toString());
	        
	        Object result = object.getProperty("result");
	        if(result.toString().equals("SessionExpired")){
	        	Log.d("------------->>>>", "result = " + result.toString());
	        	reLogin();
	        	return null;
	        }
	        accountRes.setResult(result.toString());
        }catch (Exception e) {
			// TODO: handle exception
        	accountRes.setResult("");
		}
        try{
	        Object Username = object.getProperty("Username");
	        accountRes.setUsername(Username.toString());
        }catch (Exception e) {
			// TODO: handle exception
        	accountRes.setUsername("");
		}
        try{
	        Object Email = object.getProperty("Email");
	        accountRes.setEmail(Email.toString());
        }catch (Exception e) {
			// TODO: handle exception
        	accountRes.setEmail("");
		}
        try{
	        Object MobileTel = object.getProperty("MobileTel");
	        accountRes.setMobileTel(MobileTel.toString());
        }catch (Exception e) {
			// TODO: handle exception
        	accountRes.setMobileTel("");
		}
        try{
	        Object Account = object.getProperty("Account");
	        accountRes.setAccount(Account.toString());
        }catch (Exception e) {
			// TODO: handle exception
        	accountRes.setAccount("");
		}
        return accountRes;
    }

//    摄像头音视频流编码参数查询
    public CodingParamRes getCodingParamRes(CodingParamReq req) {
    	Log.e("SoapManager", "getCodingParamRes");
        CodingParamRes res = new CodingParamRes();
        SoapObject rpc = new SoapObject(sNameSpace, "getCodingParamReq");
        rpc.addProperty("Account", req.getAccount());
        rpc.addProperty("LoginSession", req.getLoginSession());
        rpc.addProperty("DevID", req.getDevID());
        rpc.addProperty("ChannelNo", req.getChannelNo());
        rpc.addProperty("StreamType", req.getStreamType());

        res.setAccount(req.getAccount());
        res.setLoginSession(req.getLoginSession());
        res.setDevID(req.getDevID());
        res.setChannelNo(req.getChannelNo());
        res.setStreamType(req.getStreamType());

        SoapObject object = initEnvelopAndTransport(rpc,null);
        try{
	        Object result = object.getProperty("result");
	        if(result.toString().equals("SessionExpired")){
	        	Log.d("------------->>>>", "result = " + result.toString());
	        	reLogin();
	        	return null;
	        }
	        res.setResult(result.toString());
	        System.out.println("getCodingParamRes:"+result.toString());
	        Object FrameSize = object.getProperty("FrameSize");
	        res.setFrameSize(FrameSize.toString());
	
	        Object FrameRate = object.getProperty("FrameRate");
	        res.setFrameRate(FrameRate.toString());
	
	        Object RateType = object.getProperty("RateType");
	        res.setRateType(RateType.toString());
	
	        Object BitRate = object.getProperty("BitRate");
	        res.setBitRate(BitRate.toString());
	
	        Object ImageQuality = object.getProperty("ImageQuality");
	        res.setImageQuality(ImageQuality.toString());
	
	        Object AudioInput = object.getProperty("AudioInput");
	        res.setAudioInput(AudioInput.toString());
        }catch (Exception e) {
			// TODO: handle exception
		}
        return res;
    }

//    public void setCodingParamFrameSize(CodingParamRes res) {
//        SoapObject rpc = new SoapObject(sNameSpace, "setCodingParamReq");
//        rpc.addProperty("Account", res.getAccount());
//        rpc.addProperty("LoginSession", res.getLoginSession());
//        rpc.addProperty("DevID", res.getDevID());
//        rpc.addProperty("ChannelNo", res.getChannelNo());
//        rpc.addProperty("StreamType", res.getStreamType());
//        rpc.addProperty("FrameSize", res.getFrameSize());
//        rpc.addProperty("FrameRate", res.getFrameRate());
//        rpc.addProperty("RateType", res.getRateType());
//        rpc.addProperty("BitRate", res.getBitRate());
//        rpc.addProperty("ImageQuality", res.getImageQuality());
//        rpc.addProperty("AudioInput", res.getAudioInput());
//
//        SoapObject object = initEnvelopAndTransport(rpc);
//        Object result = object.getProperty("result");
//        Log.e("-----111----->>>>", "result = " + result.toString());
//    }
//
//    public void setCodingParamImageQuality(CodingParamRes res) {
//        SoapObject rpc = new SoapObject(sNameSpace, "setCodingParamReq");
//        rpc.addProperty("Account", res.getAccount());
//        rpc.addProperty("LoginSession", res.getLoginSession());
//        rpc.addProperty("DevID", res.getDevID());
//        rpc.addProperty("ChannelNo", res.getChannelNo());
//        rpc.addProperty("StreamType", res.getStreamType());
//        rpc.addProperty("FrameSize", res.getFrameSize());
//        rpc.addProperty("FrameRate", res.getFrameRate());
//        rpc.addProperty("RateType", res.getRateType());
//        rpc.addProperty("BitRate", res.getBitRate());
//        rpc.addProperty("ImageQuality", res.getImageQuality());
//        rpc.addProperty("AudioInput", res.getAudioInput());
//        SoapObject object = initEnvelopAndTransport(rpc);
//        Object result = object.getProperty("result");
//        Log.e("-----222----->>>>", "result = " + result.toString());
//    }

//		摄像头音视频编码参数设置 
    public void setCodingParam(CodingParamRes res) {
    	Log.e("SoapManager", "setCodingParam");
        SoapObject rpc = new SoapObject(sNameSpace, "setCodingParamReq");
        rpc.addProperty("Account", res.getAccount());
        rpc.addProperty("LoginSession", res.getLoginSession());
        rpc.addProperty("DevID", res.getDevID());
        rpc.addProperty("ChannelNo", res.getChannelNo());
        rpc.addProperty("StreamType", res.getStreamType());
        rpc.addProperty("FrameSize", res.getFrameSize());
        rpc.addProperty("FrameRate", res.getFrameRate());
        rpc.addProperty("RateType", res.getRateType());
        rpc.addProperty("BitRate", res.getBitRate());
        rpc.addProperty("ImageQuality", res.getImageQuality());
        rpc.addProperty("AudioInput", res.getAudioInput());

        SoapObject object = initEnvelopAndTransport(rpc,null);
        try{
	        Object result = object.getProperty("result");
	        if(result.toString().equals("SessionExpired")){
	        	reLogin();
	        }
	        Log.e("-----111----->>>>", "result = " + result.toString());
        }catch (Exception e) {
			// TODO: handle exception
		}
    }
    
//    运动侦测参数查询
    public VMDParamRes getVMDParam(VMDParamReq req) {
    	Log.e("SoapManager", "getVMDParam");
    	VMDParamRes res = new VMDParamRes();
        SoapObject rpc = new SoapObject(sNameSpace, "getVMDParamReq");
        rpc.addProperty("Account", req.getAccount());
        rpc.addProperty("LoginSession", req.getLoginSession());
        rpc.addProperty("DevID", req.getDevID());
        rpc.addProperty("ChannelNo", req.getChannelNo());

        res.setAccount(req.getAccount());
        res.setLoginSession(req.getLoginSession());
        res.setDevID(req.getDevID());
        res.setChannelNo(req.getChannelNo());

        SoapObject object = initEnvelopAndTransport(rpc,null);
        try{
        	Log.v("sopa","object: "+object);
	        Object result = object.getProperty("result");
	        if(result.toString().equals("SessionExpired")){
	        	reLogin();
	        	return null;
	        }
	        res.setResult(result.toString());
	
	        Object v = object.getProperty("Enabled");
	        res.setEnabled(Boolean.parseBoolean(v.toString()));
	        
	        v = object.getProperty("Sensitivity");
	        if (v!=null) {
	        	res.setSensitivity(Integer.valueOf(v.toString()));
	        }
	        Object v1 = object.getProperty("RowGranularity");
	        v = object.getProperty("ColumnGranularity");
	//        if (v!=null && v1!=null) {
	//        	int row = Integer.valueOf(v1.toString());
	//        	int col = Integer.valueOf(v.toString());
	//        	res.setRowColumn(row,col);
	//        }
	        /*
	        res.setStartTriggerTime(Integer.valueOf(object.getProperty("StartTriggerTime").toString()));
	        res.setEndTriggerTime(Integer.valueOf(object.getProperty("EndTriggerTime").toString()));
	         */
	        Object grids = object.getProperty("Grid");
	        Log.v("soap","vmd get grids: "+grids);
	        //String[] s = new String[res.getRows()];
	        //analyzingGrids(grids.toString(),s);
	        //res.setGrids(s);
        }catch (Exception e) {
			// TODO: handle exception
		}
        
        return res;
    }
    
//    运动侦测参数设置
    public void setVMDParam(VMDParamRes res) {
    	//TODO
    	
    	SoapObject rpc = new SoapObject(sNameSpace, "setVMDParamReq");
        rpc.addProperty("Account", res.getAccount());
        rpc.addProperty("LoginSession", res.getLoginSession());
        rpc.addProperty("DevID", res.getDevID());
        rpc.addProperty("ChannelNo", res.getChannelNo());
        rpc.addProperty("Enabled",res.getEnabled());
        rpc.addProperty("Sensitivity",res.getSensitivity());
        rpc.addProperty("StartTriggerTime",res.getStartTriggerTime());
        rpc.addProperty("EndTriggerTime",res.getEndTriggerTime());

        SoapObject so = new SoapObject(sNameSpace,"VMDGrid");
        for (String s: res.getGrids().getRows()) {
        	so.addProperty("Row", s);
        }
        rpc.addProperty("Grid", so);
    	
    	SoapObject object = initEnvelopAndTransport(rpc,null);
    	try{
	        Object result = object.getProperty("result");
	        if(result.toString().equals("SessionExpired")){
	        	
	        }
	        Log.d("-----222----->>>>", "result = " + result.toString());
    	}catch (Exception e) {
			// TODO: handle exception
		}
    }

//		设备视频调节参数设置
    public SetVideoParamRes getSetVideoParamRes(SetVideoParamReq req){
    	SetVideoParamRes res = new SetVideoParamRes();
    	SoapObject rpc = new SoapObject(sNameSpace, "setVideoParamReq");
    	rpc.addProperty("Account", req.getAccount());
        rpc.addProperty("LoginSession", req.getLoginSession());
        rpc.addProperty("DevID", req.getDevID());
        rpc.addProperty("ChannelNo", req.getChannelNo());
        rpc.addProperty("RotationDegree", req.getRotationDegree());
        SoapObject object = initEnvelopAndTransport(rpc,null);
        try{
	        Object result = object.getProperty("result");
	        res.setResult(result.toString());
        }catch (Exception e) {
			// TODO: handle exception
		}
    	return res;
    }
    
//    设备视频调节参数查询 （旋转 亮度等）
    public GetVideoParamRes getGetVideoParamRes(GetVideoParamReq req){
    	GetVideoParamRes res = new GetVideoParamRes();
    	SoapObject rpc = new SoapObject(sNameSpace, "getVideoParamReq");
    	rpc.addProperty("Account", req.getAccount());
        rpc.addProperty("LoginSession", req.getLoginSession());
        rpc.addProperty("DevID", req.getDevID());
        rpc.addProperty("ChannelNo", req.getChannelNo());
        SoapObject object = initEnvelopAndTransport(rpc,null);
        try{
	        Object result = object.getProperty("result");
	        res.setResult(result.toString());
	        Object rotationDegree = object.getProperty("RotationDegree");
	        Log.e("RotationDegree", rotationDegree.toString());
	        res.setRotationDegree(Integer.valueOf(rotationDegree.toString()));
        }catch (Exception e) {
			// TODO: handle exception
		}
        return res;
    }
    
//    查询视频存储记录
    public VodSearchRes getVodSearchReq(String account, String loginSession,
            String devID, int channelNo, String streamType,int pageNo,String startTime,String endTime) {
        SoapObject rpc = new SoapObject(sNameSpace, "vodSearchReq");
        rpc.addProperty("Account", account);
        rpc.addProperty("LoginSession", loginSession);
        rpc.addProperty("DevID", devID);
        rpc.addProperty("ChannelNo", channelNo);
        rpc.addProperty("StreamType", streamType);
        try {
            SimpleDateFormat foo = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Log.e("", "11111");
            if(endTime.equals("") || startTime.equals("")){
            	Log.e("", "22222");
            	Date endDate = new Date();
                Date startDate = new Date(System.currentTimeMillis() - REPLAYTIME);
                endTime = foo.format(endDate);
                startTime = foo.format(startDate);
            }
            Log.e("", startTime+","+endTime);
            Log.e("", "33333");
            rpc.addProperty("StartTime", startTime);
            rpc.addProperty("EndTime", endTime);
        } catch (Exception e) {
            // TODO Auto-generated catch block
        	Log.e("", "SimpleDateFormat fail");
            e.printStackTrace();
        }

        rpc.addProperty("PageNo", pageNo);
//        rpc.addProperty("SearchID", 0);
//        rpc.addProperty("PageSize", 100);

        VodSearchRes res = new VodSearchRes();
        SoapObject object = initEnvelopAndTransport(rpc,null);
        try{
	        Object result = object.getProperty("result");
	        System.out.println("result:"+result.toString());
	        if(result.toString().equals("SessionExpired")){
	        	Log.d("------------->>>>", "result = " + result.toString());
	        	reLogin();
	        	return null;
	        }
	        res.setResult(result.toString());
        }catch (Exception e) {
			// TODO: handle exception
		}
        try{
	        Object PageNo = object.getProperty("PageNo");
	        res.setPageNo(Integer.valueOf(PageNo.toString()));
	        System.out.println("pageNO:"+PageNo.toString());
	        Object PageCount = object.getProperty("PageCount");
	        res.setPageCount(Integer.valueOf(PageCount.toString()));
	        System.out.println("PageCount:"+PageCount.toString());
	        Object RecordCount = object.getProperty("RecordCount");
	        res.setRecordCount(Integer.valueOf(RecordCount.toString()));
	        System.out.println("RecordCount:"+RecordCount.toString());
	        
	        int count = object.getPropertyCount();
	        System.out.println(count);
	        ArrayList<VODRecord> list = new ArrayList<VODRecord>();
	        for (int i = 4; i < count ; i++) {
	          Object o = object.getProperty(i);
	          System.out.println("vodrecord:"+o.toString());
	          AnalyzingDoNetOutput.analyzingVODRecord(o.toString(), list);
	        }
	        res.setRecord(list);
	        System.out.println("list����"+list.size());
        }catch (Exception e) {
			// TODO: handle exception
        	ArrayList<VODRecord> list = new ArrayList<VODRecord>();
        	res.setRecord(list);
        	Log.e("", "SoapObject fail");
		}
        return res;
    }
    
//    递交NAT结果（Other-其他 TURN-转发 STUN-穿透 UPnP-直连）
    public NotifyNATResultRes getNotifyNATResultRes(NotifyNATResultReq req){
    	Log.e("SoapManager", "getNotifyNATResultRes");
    	NotifyNATResultRes res = new NotifyNATResultRes();
    	SoapObject rpc = new SoapObject(sNameSpace, "notifyNATResultReq");
    	rpc.addProperty("Account", req.getAccount());
    	rpc.addProperty("LoginSession", req.getLoginSession());
    	rpc.addProperty("DialogID", req.getDialogID());
    	rpc.addProperty("NATType", req.getNATType());
    	SoapObject object = initEnvelopAndTransport(rpc,null);
    	try{
 	       	Object result = object.getProperty("result");
 	        res.setResult(result.toString());
        }catch (Exception e) {
 			// TODO: handle exception
        	Log.e("getNotifyNATResultRes", "error");
 		}
    	return res;
    }
    
//    NAT服务器查询（STUN TURN server）
    public GetNATServerRes getGetNATServerRes(GetNATServerReq req){
    	Log.e("SoapManager", "getGetNATServerRes");
//    	GetNATServerRes res = new GetNATServerRes();
    	mGetNATServerRes = new GetNATServerRes();
    	SoapObject rpc = new SoapObject(sNameSpace, "getNATServerReq");
    	rpc.addProperty("Account", req.getAccount());
    	rpc.addProperty("LoginSession", req.getLoginSession());
    	SoapObject object = initEnvelopAndTransport(rpc,null);
    	try{
 	       	Object result = object.getProperty("result");
 	       	mGetNATServerRes.setResult(result.toString());
 	        SoapObject STUNServerList = (SoapObject) object.getProperty("STUNServerList");
 	        SoapObject STUNServer = (SoapObject) STUNServerList.getProperty("STUNServer");
 	        Object STUNIPv4Address = STUNServer.getProperty("IPv4Address");
 	        mGetNATServerRes.setSTUNServerAddress(STUNIPv4Address.toString());
 	        Object STUNPort = STUNServer.getProperty("Port");
 	        mGetNATServerRes.setSTUNServerPort(Integer.valueOf(STUNPort.toString()));
 	        
	        SoapObject TURNServerList = (SoapObject)object.getProperty("TURNServerList");
	        SoapObject TURNServer = (SoapObject) TURNServerList.getProperty("TURNServer");
 	        Object TURNIPv4Address = TURNServer.getProperty("IPv4Address");
 	        mGetNATServerRes.setTURNServerAddress(TURNIPv4Address.toString());
	        Object TURNPort = TURNServer.getProperty("Port");
	        mGetNATServerRes.setTURNServerPort(Integer.valueOf(TURNPort.toString()));
	        Object userName = TURNServer.getProperty("Username");
	        mGetNATServerRes.setTURNServerUserName(userName.toString());
	        Object password = TURNServer.getProperty("Password");
	        mGetNATServerRes.setTURNServerPassword(password.toString());
	        Log.e("SoapManager", mGetNATServerRes.toString());
        }catch (Exception e) {
 			// TODO: handle exception
        	Log.e("getGetNATServerRes", "error");
 		}
    	return mGetNATServerRes;
    }
    
//    Android推送服务注册
    public UpdateAndroidTokenRes GetUpdateAndroidTokenRes(UpdateAndroidTokenReq req){
    	UpdateAndroidTokenRes res = new UpdateAndroidTokenRes();
    	SoapObject rpc = new SoapObject(sNameSpace, "updateAndroidTokenReq");
    	rpc.addProperty("Account", req.getAccount());
    	rpc.addProperty("LoginSession", req.getLoginSession());
    	rpc.addProperty("UDID", req.getUDID());
    	rpc.addProperty("DeviceToken", req.getDeviceToken());
    	rpc.addProperty("APNs", req.isAPNs());
    	//rpc.addProperty("AndroidOS", "Android");
    	SoapObject object = initEnvelopAndTransport(rpc,null);
    	try{
 	       	Object result = object.getProperty("result");
 	       	res.setResult(result.toString());
    	}catch (Exception e) {
				// TODO: handle exception
		}
    	return res;
    }
    
//    查询Android推送服务
    public QueryAndroidTokenRes GetQueryAndroidTokenRes(QueryAndroidTokenReq req){
    	QueryAndroidTokenRes res = new QueryAndroidTokenRes();
    	SoapObject rpc = new SoapObject(sNameSpace, "queryAndroidTokenReq");
    	rpc.addProperty("Account", req.getAccount());
    	rpc.addProperty("LoginSession", req.getLoginSession());
    	rpc.addProperty("UDID", req.getUDID());
    	SoapObject object = initEnvelopAndTransport(rpc,null);
    	try{
 	       	Object result = object.getProperty("result");
 	       	res.setResult(result.toString());
 	       	Object UDID = object.getProperty("UDID");
	       	res.setUDID(UDID.toString());
	       	Object DeviceToken = object.getProperty("DeviceToken");
 	       	res.setDeviceToken(DeviceToken.toString());
 	       	Object APNs = object.getProperty("APNs");
	       	res.setAPNs(Boolean.parseBoolean(APNs.toString()));
    	}catch (Exception e) {
				// TODO: handle exception
		}
    	return res;
    }
    
//    MCU云台控制
    public PtzControlRes GetPtzControlRes(PtzControlReq req){
    	PtzControlRes res = new PtzControlRes();
    	SoapObject rpc = new SoapObject(sNameSpace, "ptzControlReq");
    	rpc.addProperty("Account", req.getAccount());
    	rpc.addProperty("LoginSession", req.getLoginSession());
    	rpc.addProperty("DevID", req.getDevID());
    	rpc.addProperty("ChannelNo", req.getChannelNo());
    	rpc.addProperty("PtzDirection", req.getPtzDirection());
    	SoapObject object = initEnvelopAndTransport(rpc,null);
    	try{
 	       	Object result = object.getProperty("result");
 	       	res.setResult(result.toString());
    	}catch (Exception e) {
				// TODO: handle exception
		}
    	return res;
    }
    
//    查询客户端软件的最新版本号
    public QueryClientVersionRes getQueryClientVersionRes(QueryClientVersionReq req){
    	QueryClientVersionRes res = new QueryClientVersionRes();
    	SoapObject rpc = new SoapObject(sNameSpace, "queryClientVersionReq");
    	rpc.addProperty("ClientType", req.getClientType());
    	SoapObject object = initEnvelopAndTransport(rpc,null);
    	try{
 	       	Object result = object.getProperty("result");
 	       	res.setResult(result.toString());
 	       	Object version = object.getProperty("Version");
	       	res.setVersion(version.toString());
	       	Object downloadAddress = object.getProperty("DownloadAddress");
 	       	res.setDownloadAddress(downloadAddress.toString());
    	}catch (Exception e) {
				// TODO: handle exception
		}
    	return res;
    }
    
//    设备固件版本查询
    public GetDevVerRes getGetDevVerRes(GetDevVerReq req){
    	GetDevVerRes res = new GetDevVerRes();
    	SoapObject rpc = new SoapObject(sNameSpace, "getDevVerReq");
    	rpc.addProperty("Account", req.getAccount());
    	rpc.addProperty("LoginSession", req.getLoginSession());
    	rpc.addProperty("DevID", req.getDevID());
    	SoapObject object = initEnvelopAndTransport(rpc,"http://www.haoweis.com/HomeServices/MCU/getDevVer");
    	try{
 	       	Object result = object.getProperty("result");
 	       	res.setResult(result.toString());
 	       	Object CurDevVer = object.getProperty("CurDevVer");
	       	res.setCurDevVer(CurDevVer.toString());
	       	Object NewDevVer = object.getProperty("NewDevVer");
 	       	res.setNewDevVer(NewDevVer.toString());
    	}catch (Exception e) {
				// TODO: handle exception
		}
    	return res;
    }
    
//    远程升级通知
    public UpgradeDevVerRes getUpgradeDevVerRes(UpgradeDevVerReq req){
    	UpgradeDevVerRes res = new UpgradeDevVerRes();
    	SoapObject rpc = new SoapObject(sNameSpace, "upgradeDevVerReq");
    	rpc.addProperty("Account", req.getAccount());
    	rpc.addProperty("LoginSession", req.getLoginSession());
    	rpc.addProperty("DevID", req.getDevID());
    	SoapObject object = initEnvelopAndTransport(rpc,"http://www.haoweis.com/HomeServices/MCU/upgradeDevVer");
    	try{
 	       	Object result = object.getProperty("result");
 	       	res.setResult(result.toString());
    	}catch (Exception e) {
				// TODO: handle exception
		}
    	return res;
    }
    
//    查询摄像机辅助器状态
    public GetAuxiliaryRes getGetAuxiliaryRes(GetAuxiliaryReq req){
    	GetAuxiliaryRes res = new GetAuxiliaryRes();
    	SoapObject rpc = new SoapObject(sNameSpace, "getAuxiliaryReq");
    	rpc.addProperty("Account", req.getAccount());
    	rpc.addProperty("LoginSession", req.getLoginSession());
    	rpc.addProperty("DevID", req.getDevID());
    	rpc.addProperty("Auxiliary", req.getAuxiliary());
    	SoapObject object = initEnvelopAndTransport(rpc,"http://www.haoweis.com/HomeServices/MCU/getAuxiliary");
    	try{
 	       	Object result = object.getProperty("result");
 	       	res.setResult(result.toString());
 	       	Object auxiliaryState = object.getProperty("AuxiliaryState");
	       	res.setAuxiliaryState(auxiliaryState.toString());
    	}catch (Exception e) {
				// TODO: handle exception
		}
    	return res;
    }
    
//    设置摄像机辅助器状态 （辅助照明 信号灯）
    public SetAuxiliaryRes getSetAuxiliaryRes(SetAuxiliaryReq req){
    	SetAuxiliaryRes res = new SetAuxiliaryRes();
    	SoapObject rpc = new SoapObject(sNameSpace, "setAuxiliaryReq");
    	rpc.addProperty("Account", req.getAccount());
    	rpc.addProperty("LoginSession", req.getLoginSession());
    	rpc.addProperty("DevID", req.getDevID());
    	rpc.addProperty("Auxiliary", req.getAuxiliary());
    	rpc.addProperty("AuxiliaryState", req.getAuxiliaryState());
    	SoapObject object = initEnvelopAndTransport(rpc,"http://www.haoweis.com/HomeServices/MCU/setAuxiliary");
    	try{
 	       	Object result = object.getProperty("result");
 	       	res.setResult(result.toString());
    	}catch (Exception e) {
				// TODO: handle exception
		}
    	return res;
    }

//    开通Android推送服务
    public SubscribeAndroidPushRes getSubscribeAndroidPushRes(SubscribeAndroidPushReq req){
    	SubscribeAndroidPushRes res = new SubscribeAndroidPushRes();
    	SoapObject rpc = new SoapObject(sNameSpace, "subscribeAndroidPushReq");
    	rpc.addProperty("Account", req.getAccount());
    	rpc.addProperty("LoginSession", req.getLoginSession());
    	rpc.addProperty("SubscribedFlag", req.getSubscribedFlag());
    	rpc.addProperty("DevID", req.getDevID());
    	rpc.addProperty("ChannelNo", req.getChannelNo());
    	SoapObject object = initEnvelopAndTransport(rpc,"http://www.haoweis.com/HomeServices/MCU/subscribeAndroidPush");
    	try{
 	       	Object result = object.getProperty("result");
 	       	res.setResult(result.toString());
    	}catch (Exception e) {
				// TODO: handle exception
		}
    	return res;
    }
    
//    查询无线网络状态
    public GetWirelessNetworkRes getGetWirelessNetworkRes(GetWirelessNetworkReq req){
    	GetWirelessNetworkRes res = new GetWirelessNetworkRes();
    	SoapObject rpc = new SoapObject(sNameSpace, "getWirelessNetworkReq");
    	rpc.addProperty("Account", req.getAccount());
    	rpc.addProperty("LoginSession", req.getLoginSession());
    	rpc.addProperty("DevID", req.getDevID());
    	SoapObject object = initEnvelopAndTransport(rpc,"http://www.haoweis.com/HomeServices/MCU/getWirelessNetwork");
    	try{
 	       	Object result = object.getProperty("result");
 	       	res.setResult(result.toString());
 	        Object wirelessType = object.getProperty("WirelessType");
	       	res.setWirelessType(wirelessType.toString());
	       	Object sSID = object.getProperty("SSID");
 	       	res.setsSID(sSID.toString());
 	        Object intensity = object.getProperty("Intensity");
	       	res.setIntensity(Integer.valueOf(intensity.toString()));
    	}catch (Exception e) {
				// TODO: handle exception
		}
    	return res;
    }
    
//    修改账户密码
    public UpdatePasswordRes getUpdatePasswordRes(UpdatePasswordReq req){
    	UpdatePasswordRes res = new UpdatePasswordRes();
    	SoapObject rpc = new SoapObject(sNameSpace, "updatePasswordReq");
    	rpc.addProperty("Account", req.getAccount());
    	rpc.addProperty("LoginSession", req.getLoginSession());
    	rpc.addProperty("Password", req.getPassword());
    	rpc.addProperty("NewPassword", req.getNewPassword());
    	SoapObject object = initEnvelopAndTransport(rpc,"http://www.haoweis.com/HomeServices/MCU/updatePassword");
    	try{
 	       	Object result = object.getProperty("result");
 	       	res.setResult(result.toString());
    	}catch (Exception e) {
				// TODO: handle exception
		}
    	return res;
    }
    
//    修改账户信息
    public UpdateAccountRes getUpdateAccountRes(UpdateAccountReq req){
    	UpdateAccountRes res = new UpdateAccountRes();
    	SoapObject rpc = new SoapObject(sNameSpace, "updateAccountReq");
    	rpc.addProperty("Account", req.getAccount());
    	rpc.addProperty("LoginSession", req.getLoginSession());
    	//rpc.addProperty("Username", "");
    	rpc.addProperty("MobileTel", req.getMobileTel());
    	SoapObject object = initEnvelopAndTransport(rpc,"http://www.haoweis.com/HomeServices/MCU/updateAccount");
    	try{
 	       	Object result = object.getProperty("result");
 	       	res.setResult(result.toString());
    	}catch (Exception e) {
				// TODO: handle exception
		}
    	return res;
    }
    
//    创建账户
    public CreateAccountRes getCreateAccountRes(CreateAccountReq req){
    	CreateAccountRes res = new CreateAccountRes();
    	SoapObject rpc = new SoapObject(sNameSpace, "createAccountReq");
    	rpc.addProperty("Account", req.getAccount());
    	rpc.addProperty("Username", " ");
    	rpc.addProperty("Password", req.getPassword());
    	rpc.addProperty("Email", req.getEmail());
    	//rpc.addProperty("MobileTel", " ");
    	SoapObject object = initEnvelopAndTransport(rpc,"http://www.haoweis.com/HomeServices/MCU/createAccount");
    	try{
    		Object result = object.getProperty("result");
    	 	res.setResult(result.toString());
    	}catch (Exception e) {
    		// TODO: handle exception
    	}
    	   	return res;
    }
    
//    用户绑定设备
    public AddDeviceRes getAddDeviceRes(AddDeviceReq req){
    	AddDeviceRes res = new AddDeviceRes();
    	SoapObject rpc = new SoapObject(sNameSpace, "addDeviceReq");
    	rpc.addProperty("Account", req.getAccount());
    	System.out.println(req.getAccount());
    	rpc.addProperty("LoginSession", req.getLoginSession());
    	
    	SoapObject so = new SoapObject(sNameSpace,"Device");
        so.addProperty("DevID", req.getDevID());
        so.addProperty("DevKey", req.getDevKey());
        System.out.println("devName:"+req.getDevName());
        so.addProperty("DevName", req.getDevName());
        
        SoapObject so2 = new SoapObject(sNameSpace,"ArrayOfDevice");
        so2.addProperty("Device", so);
        rpc.addProperty("DeviceAll", so2);
    	
    	rpc.addProperty("Forcible", req.isForcible());
    	SoapObject object = initEnvelopAndTransport(rpc,"http://www.haoweis.com/HomeServices/MCU/addDevice");
    	try{
    	 	Object result = object.getProperty("result");
    	 	res.setResult(result.toString());
    	 	System.out.println("getAddDeviceRes:"+result.toString());
    	}catch (Exception e) {
    		// TODO: handle exception
    	}
    	    return res;
    }
    
    //修改设备名
    public UpdateChannelNameRes getUpdateChannelNameRes(UpdateChannelNameReq req){
    	UpdateChannelNameRes res = new UpdateChannelNameRes();
    	SoapObject rpc = new SoapObject(sNameSpace, "updateChannelNameReq");
    	rpc.addProperty("Account", req.getAccount());
    	rpc.addProperty("LoginSession", req.getLoginSession());
    	rpc.addProperty("DevID", req.getDevID());
    	rpc.addProperty("ChannelNo", 0);
    	rpc.addProperty("ChannelName", req.getChannelName());
    	SoapObject object = initEnvelopAndTransport(rpc,"http://www.haoweis.com/HomeServices/MCU/updateChannelName");
    	try{
    		Object result = object.getProperty("result");
    	 	res.setResult(result.toString());
    	}catch (Exception e) {
    		// TODO: handle exception
    	}
    	   	return res;
    }
    
	@Override
	public String toString() {
		return "SoapManager [mLoginRequest=" + mLoginRequest
				+ ", mLoginResponse=" + mLoginResponse + ", mGetNATServerRes="
				+ mGetNATServerRes + "]";
	}
    
}
