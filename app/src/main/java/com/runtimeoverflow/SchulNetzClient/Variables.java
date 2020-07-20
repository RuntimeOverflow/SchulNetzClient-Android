package com.runtimeoverflow.SchulNetzClient;

import android.content.Context;

import com.runtimeoverflow.SchulNetzClient.Data.User;

public class Variables {
    private static Variables instance;

    public Context currentContext;

    public Account account;
    public User user;

    private Variables(){

    }

    public static Variables get() {
        if(instance == null){
            instance = new Variables();
        }

        return instance;
    }
}
