package com.howell.push;



import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/4/6.
 */

public class JsonUtil {
    public static JSONObject createAlarmPushConnectJsonObject(int cseq,String session,String username) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("Message",0x0001);
        obj.put("CSeq",cseq);
        JSONObject request = new JSONObject();
        if (session!=null) request.put("Session",session);
        request.put("DeviceToken",username);
        obj.put("Request",request);
        return obj;
    }

    public static JSONObject createAlarmAliveJsonObject(int cseq, long systemUpTime,  double longitude, double latitude, boolean useLongitudeOrLatitude) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("Message",0x0002);
        obj.put("CSeq",cseq);
        JSONObject request = new JSONObject();
        JSONObject keepAlive = new JSONObject();
        keepAlive.put("SystemUpTime",systemUpTime);
        if (useLongitudeOrLatitude){
            keepAlive.put("Longitude",longitude);
            keepAlive.put("Latitude",latitude);
        }
        request.put("Request",keepAlive);
        obj.put("Request",request);
        return obj;
    }

    public static JSONObject createADCEventResJsonObject(int cseq) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("Message",0x8003);
        obj.put("CSeq",cseq);
        JSONObject response = new JSONObject();
        response.put("Result",0);
        obj.put("Response",response);
        return obj;
    }

    public static JSONObject createADCNoticeResJsonObject(int cseq) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("Message",0x8004);
        obj.put("CSeq",cseq);
        JSONObject response = new JSONObject();
        response.put("Result",0);
        obj.put("Response",response);
        return obj;
    }

    public static JSONObject createPushResJsonObject(int cseq) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("Message",0x8003);
        obj.put("CSeq",cseq);
        JSONObject res = new JSONObject();
        res.put("Result",0);
        obj.put("Response",res);
        return obj;
    }


    public static JSONObject createMCUSendMessage(WSRes.AlarmNotice n) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("Message",0x0004);
        obj.put("CSeq",n.getCseq());
        JSONObject notice = new JSONObject();
        notice.put("Id",n.getId());
        notice.put("Message",n.getMsg());
        notice.put("Classification",n.getClassification());
        notice.put("Time",n.getTime());
        notice.put("Status",n.getState());
        notice.put("Sender",n.getSender());
        obj.put("Notice",notice);
        return obj;
    }



    public static WSRes parseResJsonString(String jsonString) throws JSONException {
        JSONObject obj = new JSONObject(jsonString);
        int message = obj.getInt("Message");
        int cseq = obj.getInt("CSeq");
        switch (message){
            case 0x8001: { //c->s  c receive ask
                WSRes.AlarmLinkRes res = parseAlarmLinkResJsonObject(message, cseq, obj.getJSONObject("Response"));
                return new WSRes(WSRes.WS_TYPE.ALARM_LINK, res);
            }
            case 0x8002: { //c->s  c receive ask
                WSRes.AlarmAliveRes res = parseAlarmAliveResJsonObject(message, cseq, obj.getJSONObject("Response"));
                return new WSRes(WSRes.WS_TYPE.ALARM_ALIVE,res);
            }
            case 0x0003:{ //s->c  c need to send ask back;
//                WSRes.AlarmEvent event = parseAlarmEventJsonObject(message,cseq,obj.getJSONObject("Request"));
//                return new WSRes(WSRes.WS_TYPE.ALARM_EVENT,event);
                WSRes.PushMessage ps = parsePushMessageJsonObject(message,cseq,obj.getJSONObject("Request"));
                return new WSRes(WSRes.WS_TYPE.PUSH_MESSAGE,ps);

            }
            case 0x0004:{//s->c
                WSRes.AlarmNotice notice = parseAlarmNoticeJsonObject(message,cseq,obj.getJSONObject("Request"));
                return new WSRes(WSRes.WS_TYPE.ALARM_NOTICE,notice);
            }
            case 0x8004:
                break;
        }
        return null;

    }

    private static WSRes.AlarmLinkRes parseAlarmLinkResJsonObject(int message,int cseq,JSONObject obj) throws JSONException {
        return new WSRes.AlarmLinkRes(message,cseq,obj.getInt("Result"));
    }

    private static WSRes.AlarmAliveRes parseAlarmAliveResJsonObject(int message,int cseq,JSONObject obj) throws JSONException {
        int result = obj.getInt("Result");
        JSONObject keepAlive = obj.getJSONObject("KeepAlive");
        String time = keepAlive.getString("Time");
        int heart = keepAlive.getInt("HeartbeatInterval");
        return new WSRes.AlarmAliveRes(message,cseq,result,time,heart);
    }

    private static WSRes.AlarmEvent parseAlarmEventJsonObject(int message,int cseq,JSONObject obj) throws JSONException {
        JSONObject event = obj.getJSONObject("EventNotify");
        String id = event.getString("Id");
        String name = event.getString("Name");
        String type = event.getString("EventType");
        String state = event.getString("EventState");
        String time = event.getString("Time");
        String path = null;
        String description = null;
        String extendInformation = null;
        String eventId = null;
        ArrayList<String> imageUrlArray = null;
        try{path = event.getString("Path");}catch (JSONException e){}
        try{description = event.getString("Description");}catch (JSONException e){}
        try{extendInformation = event.getString("ExtendInformation");}catch (JSONException e){}
        try{eventId = event.getString("EventId");}catch (JSONException e){}
        try {
            JSONArray urlobjs = event.getJSONArray("ImageUrl");
            imageUrlArray = new ArrayList<String>();
            for (int i=0;i<urlobjs.length();i++){
                String imageUrl = urlobjs.getString(i);
                imageUrlArray.add(imageUrl);
            }
        }catch (JSONException e){

        }
        WSRes.AlarmEvent alarmEvent = new WSRes.AlarmEvent();
        alarmEvent.setMessage(message);
        alarmEvent.setCseq(cseq);
        alarmEvent.setId(id);
        alarmEvent.setName(name);
        alarmEvent.setEventType(type);
        alarmEvent.setEventState(state);
        alarmEvent.setTime(time);
        alarmEvent.setPath(path);
        alarmEvent.setDescription(description);
        alarmEvent.setExtendInformation(extendInformation);
        alarmEvent.setEventID(eventId);
        alarmEvent.setImageurl(imageUrlArray);
        return alarmEvent;
    }

    private static WSRes.AlarmNotice parseAlarmNoticeJsonObject(int message,int cseq,JSONObject obj) throws JSONException {
        JSONObject notice = obj.getJSONObject("Notice");
        String id = notice.getString("Id");
        String msg = notice.getString("Message");
        String classification = notice.getString("Classification");
        String time = notice.getString("Time");
        String status = notice.getString("Status");
        String sender = notice.getString("Sender");
        String componentId = null;
        String componentName = null;
        try{componentId = notice.getString("ComponentId");}catch (JSONException e){}
        try{componentName = notice.getString("ComponentName");}catch (JSONException e){}
        WSRes.AlarmNotice alarmNotice = new WSRes.AlarmNotice();
        alarmNotice.setMessage(message);
        alarmNotice.setCseq(cseq);
        alarmNotice.setId(id);
        alarmNotice.setMsg(msg);
        alarmNotice.setClassification(classification);
        alarmNotice.setTime(time);
        alarmNotice.setState(status);
        alarmNotice.setSender(sender);
        alarmNotice.setComponentId(componentId);
        alarmNotice.setComponentName(componentName);
        return alarmNotice;
    }

    private static WSRes.PushMessage parsePushMessageJsonObject(int message,int cseq,JSONObject obj) throws JSONException {
        JSONObject ps = obj.getJSONObject("PushMessage");
        String content = ps.getString("Content");
        return new WSRes.PushMessage(cseq,content);
    }

}
