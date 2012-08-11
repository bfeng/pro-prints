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



import android.app.AlertDialog;
import android.content.Context;

public class Alert {
	public static void showAlert(Context context, String Title, String Message) {
		AlertDialog.Builder alertbox = new AlertDialog.Builder(context);
		alertbox.setTitle(Title);
		alertbox.setMessage(Message);
		alertbox.setCancelable(false);
		alertbox.setPositiveButton("OK",null);
		alertbox.show();
	}
	
	
}
