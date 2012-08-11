/*
* Copyright 2012 Walgreen Co. All rights reserved *

* Licensed under the Walgreens Developer Program and Portal Terms of Use and API License Agreement, Version 1.0 (the “Terms of Use”)
* You may not use this file except in compliance with the License.
* A copy of the License is located at https://developer.walgreens.com/page/terms-use
*
* This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
* express or implied. See the License for the specific language governing  permissions and limitations under the License.
*/
package com.usablenet.walgreen.appathon.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

public class Common {

    public static void showToast(Context context,String Mesg)
    {
        Toast.makeText(context,Mesg,Toast.LENGTH_LONG).show();
    }
    
    public static boolean hasSDCardMounted() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        try {
            if ((Build.MANUFACTURER).equalsIgnoreCase("HTC")
                    && (Build.DEVICE).equalsIgnoreCase("inc")) {
                return true;
            }
        } catch (Exception pEx) {
            return false;
        }
        return false;
    }
    
   
    
    public static final boolean isInternetAvailable(Context ctx) {
        boolean lRetVal = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) ctx
            .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo nInfo = cm.getActiveNetworkInfo();
                if (null != nInfo) {
                    lRetVal = nInfo.isConnectedOrConnecting();
                }
            }
        } catch (Exception e) {
            return lRetVal;
        }

        return lRetVal;
    }
}
