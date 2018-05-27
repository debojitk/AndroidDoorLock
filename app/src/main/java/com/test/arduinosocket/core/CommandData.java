package com.test.arduinosocket.core;

import com.test.arduinosocket.common.Constants;

/**
 * Created by administrator on 5/21/2017.
 * COMMAND:deviceId:deviceKey:(ACK:data|NACK:data|data)
 */

public class CommandData {
    private String command;
    private String deviceId;
    private String deviceKey;
    private String deviceType;
    private String data;
    private String rawData;
    private boolean response;
    private boolean error;



    public CommandData(){

    }

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public boolean isResponse() {
        return response;
    }

    public CommandData setResponse(boolean response) {
        this.response = response;
        return this;
    }

    public boolean isError() {
        return error;
    }

    public CommandData setError(boolean error) {
        this.error = error;
        return this;
    }



    public CommandData(String commandData){
        this.rawData=commandData;
        parseCommand();
    }
    public String getCommand() {
        return command;
    }

    public CommandData setCommand(String command) {
        this.command = command;
        return this;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public CommandData setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public CommandData setDeviceKey(String deviceKey) {
        this.deviceKey = deviceKey;
        return this;
    }

    public String getData() {
        return data;
    }

    public CommandData setData(String data) {
        this.data = data;
        return this;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    private void parseCommand(){
        if(rawData!=null && rawData.trim().length()>0){
            String []commandArray=rawData.split("\\:");
            if(commandArray.length>0){
                setCommand(commandArray[0]);
                setDeviceId(commandArray[1]);
                if(commandArray.length>2){
                    setDeviceKey(commandArray[2]);
                }
                if(commandArray.length>3){
                    setDeviceType(commandArray[3]);
                }

                if(commandArray.length>4){
                    if(commandArray[4].startsWith("ACK")){
                        response=true;
                        error=false;
                        if(commandArray.length>5){
                            data=commandArray[5];
                        }
                    }else if(commandArray[4].startsWith("NACK")){
                        response=true;
                        error=true;
                        if(commandArray.length>5){
                            data=commandArray[5];
                        }
                    }else{
                        response=false;
                        error=false;
                        data=commandArray[4];
                    }
                }
            }
        }
    }

    public String buildCommandString(){
        StringBuffer buffer=new StringBuffer();
        buffer.append(command).append(":").append(deviceId).append(":").append(deviceKey);
        if(response){
            if(error){
                buffer.append(":").append(Constants.NACK);
            }else{
                buffer.append(":").append(Constants.ACK);
            }
        }
        if(data!=null){
            buffer.append(":").append(data);
        }
        return buffer.toString();
    }

}
