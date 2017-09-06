package com.howell.push;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/4/7.
 */

public class WSRes {

    WS_TYPE type;
    Object resultObject;

    public WS_TYPE getType() {
        return type;
    }

    public void setType(WS_TYPE type) {
        this.type = type;
    }

    public Object getResultObject() {
        return resultObject;
    }

    public void setResultObject(Object resultObject) {
        this.resultObject = resultObject;
    }

    public WSRes(WS_TYPE type, Object resultObject) {
        this.type = type;
        this.resultObject = resultObject;
    }

    public WSRes() {
    }



    @Override
    public String toString() {
        return "WSRes{" +
                "type=" + type +
                ", resultObject=" + resultObject.toString() +
                '}';
    }

    public static enum WS_TYPE{
        ALARM_LINK,
        ALARM_ALIVE,
        ALARM_EVENT,
        ALARM_NOTICE,
        PUSH_MESSAGE,
    }

    public static class AlarmLinkRes{
        int message;
        int cSeq;
        int result;

        public int getResult() {
            return result;
        }

        public void setResult(int result) {
            this.result = result;
        }

        public int getMessage() {
            return message;
        }

        public void setMessage(int message) {
            this.message = message;
        }

        public int getcSeq() {
            return cSeq;
        }

        public void setcSeq(int cSeq) {
            this.cSeq = cSeq;
        }

        public AlarmLinkRes() {
        }

        public AlarmLinkRes(int message, int cSeq, int result) {
            this.message = message;
            this.cSeq = cSeq;
            this.result = result;
        }

        @Override
        public String toString() {
            return "AlarmPushConnectRes{" +
                    "message=" + message +
                    ", cSeq=" + cSeq +
                    ", result='" + result + '\'' +
                    '}';
        }
    }

    public static class AlarmAliveRes{
        int message;
        int cSeq;
        int result;
        String time;
        int heartbeatinterval;

        public int getMessage() {
            return message;
        }

        public void setMessage(int message) {
            this.message = message;
        }

        public int getcSeq() {
            return cSeq;
        }

        public void setcSeq(int cSeq) {
            this.cSeq = cSeq;
        }

        public int getResult() {
            return result;
        }

        public void setResult(int result) {
            this.result = result;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public int getHeartbeatinterval() {
            return heartbeatinterval;
        }

        public void setHeartbeatinterval(int heartbeatinterval) {
            this.heartbeatinterval = heartbeatinterval;
        }

        public AlarmAliveRes(int message, int cSeq, int result, String time, int heartbeatinterval) {
            this.message = message;
            this.cSeq = cSeq;
            this.result = result;
            this.time = time;
            this.heartbeatinterval = heartbeatinterval;
        }

        public AlarmAliveRes() {
        }

        @Override
        public String toString() {
            return "AlarmAliveRes{" +
                    "message=" + message +
                    ", cSeq=" + cSeq +
                    ", result='" + result + '\'' +
                    ", time='" + time + '\'' +
                    ", heartbeatinterval=" + heartbeatinterval +
                    '}';
        }
    }

    public static class AlarmEvent{
        int message;
        int cseq;
        String id;
        String name;
        String eventType;
        String eventState;
        String time;
        String path;
        String description;
        String extendInformation;
        String eventID;
        ArrayList<String> imageurl;

        public int getMessage() {
            return message;
        }

        public void setMessage(int message) {
            this.message = message;
        }

        public int getCseq() {
            return cseq;
        }

        public void setCseq(int cseq) {
            this.cseq = cseq;
        }

        public String getId() {
            return id;
        }

        public AlarmEvent setId(String id) {
            this.id = id;
            return this;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEventType() {
            return eventType;
        }

        public AlarmEvent setEventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public String getEventState() {
            return eventState;
        }

        public void setEventState(String eventState) {
            this.eventState = eventState;
        }

        public String getTime() {
            return time;
        }

        public AlarmEvent setTime(String time) {
            this.time = time;
            return this;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getExtendInformation() {
            return extendInformation;
        }

        public void setExtendInformation(String extendInformation) {
            this.extendInformation = extendInformation;
        }

        public String getEventID() {
            return eventID;
        }

        public void setEventID(String eventID) {
            this.eventID = eventID;
        }

        public ArrayList<String> getImageurl() {
            return imageurl;
        }

        public void setImageurl(ArrayList<String> imageurl) {
            this.imageurl = imageurl;
        }

        public AlarmEvent(int message, int cseq, String id, String name, String eventType, String eventState, String time, String path, String description, String extendInformation, String eventID, ArrayList<String> imageurl) {
            this.message = message;
            this.cseq = cseq;
            this.id = id;
            this.name = name;
            this.eventType = eventType;
            this.eventState = eventState;
            this.time = time;
            this.path = path;
            this.description = description;
            this.extendInformation = extendInformation;
            this.eventID = eventID;
            this.imageurl = imageurl;
        }

        public AlarmEvent() {
        }

        @Override
        public String toString() {
            return "AlarmEvent{" +
                    "message=" + message +
                    ", cseq=" + cseq +
                    ", id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", eventType='" + eventType + '\'' +
                    ", eventState='" + eventState + '\'' +
                    ", time='" + time + '\'' +
                    ", path='" + path + '\'' +
                    ", description='" + description + '\'' +
                    ", extendInformation='" + extendInformation + '\'' +
                    ", eventID='" + eventID + '\'' +
                    ", imageurl=" + imageurl +
                    '}';
        }
    }

    public static class AlarmNotice{
        int message;
        int cseq;
        String id;
        String msg;
        String classification;
        String time;
        String state;
        String sender;
        String componentId;
        String componentName;

        public int getMessage() {
            return message;
        }

        public void setMessage(int message) {
            this.message = message;
        }

        public int getCseq() {
            return cseq;
        }

        public void setCseq(int cseq) {
            this.cseq = cseq;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getClassification() {
            return classification;
        }

        public void setClassification(String classification) {
            this.classification = classification;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public String getComponentId() {
            return componentId;
        }

        public void setComponentId(String componentId) {
            this.componentId = componentId;
        }

        public String getComponentName() {
            return componentName;
        }

        public void setComponentName(String componentName) {
            this.componentName = componentName;
        }

        public AlarmNotice(int message, int cseq, String id, String msg, String classification, String time, String state, String sender, String componentId, String componentName) {
            this.message = message;
            this.cseq = cseq;
            this.id = id;
            this.msg = msg;
            this.classification = classification;
            this.time = time;
            this.state = state;
            this.sender = sender;
            this.componentId = componentId;
            this.componentName = componentName;
        }

        public AlarmNotice() {
        }

        @Override
        public String toString() {
            return "AlarmNotice{" +
                    "message=" + message +
                    ", cseq=" + cseq +
                    ", id='" + id + '\'' +
                    ", msg='" + msg + '\'' +
                    ", classification='" + classification + '\'' +
                    ", time='" + time + '\'' +
                    ", state='" + state + '\'' +
                    ", sender='" + sender + '\'' +
                    ", componentId='" + componentId + '\'' +
                    ", componentName='" + componentName + '\'' +
                    '}';
        }
    }

    public static class PushMessage{
        int cseq;
        String content;

        public PushMessage(int cseq, String content) {
            this.cseq = cseq;
            this.content = content;
        }

        @Override
        public String toString() {
            return "PushMessage{" +
                    "cseq=" + cseq +
                    ", content='" + content + '\'' +
                    '}';
        }

        public int getCseq() {
            return cseq;
        }

        public void setCseq(int cseq) {
            this.cseq = cseq;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }


    }

}
