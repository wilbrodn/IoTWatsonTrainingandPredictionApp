package com.ibm.watson.WatsonVRTraining.util;

public class ThreadMessage_hook {
    private String msg;
	public static ThreadMessage_hook classifier_shutDown_hook_obj = new ThreadMessage_hook("create classifier");

    
    public ThreadMessage_hook(String str){
        this.msg=str;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String str) {
        this.msg=str;
    }

}