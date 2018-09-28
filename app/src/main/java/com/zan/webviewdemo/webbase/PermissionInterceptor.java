
package com.zan.webviewdemo.webbase;

public interface PermissionInterceptor {

    boolean intercept(String url, String[] permissions, String action);

}
