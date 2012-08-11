/*
* Copyright 2012 Walgreen Co. All rights reserved *

* Licensed under the Walgreens Developer Program and Portal Terms of Use and API License Agreement, Version 1.0 (the �Terms of Use�)
* You may not use this file except in compliance with the License.
* A copy of the License is located at https://developer.walgreens.com/page/terms-use
*
* This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
* express or implied. See the License for the specific language governing  permissions and limitations under the License.
*/
package edu.iit.cs.pp;

import org.json.JSONObject;

import com.usablenet.walgreen.appathon.entities.UserInfoBean;
import com.usablenet.walgreen.appathon.utils.Constants;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;



public class SettingsMenu extends Activity {
	
	public static final String FIRST_NAME = "fName";
	public static final String LAST_NAME = "lName";
	public static final String EMAIL = "email@mail.com";
	public static final String PHONE_NUMBER = "1234567";
	public static final String AFF_ID = "extest1.";
	public static final String APP_KEY = "89df9168988ca48e3c37bda983972ed4";
	public static final String CHECKOUT_URL  = "";
	
	public static final String VEN_GENERAL = "extest1";
	public static final String VEN_GENERAL_API_KEY = "";
	public static final String VEN_WAG = "photon";
	public static final String VEN_FACEBOOK = "extest2";
	public static final String VEN_FACEBOOK_API_KEY = "*********Facebook user API key******";
	
	EditText fName,lName,email,phNumber, checkoutUrl;
	RadioGroup rGroup;
	RadioButton wagApp,faceBook,vendor;
	Button saveBtn;
	private UserInfoBean user;
	
	 @Override
	    public void onCreate(Bundle savedInstanceState) 
	    {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.settings_customer_info);
	        fName=(EditText)findViewById(R.id.firstNameEdt);
	        lName=(EditText)findViewById(R.id.lastNameEdt);
	        email=(EditText)findViewById(R.id.emailEdt);
	        phNumber=(EditText)findViewById(R.id.phNumberEdt);
	        checkoutUrl = (EditText)findViewById(R.id.checkout_url);
	        rGroup=(RadioGroup)findViewById(R.id.radioGroup);
	        wagApp=(RadioButton)findViewById(R.id.radioWag);
	        user = new UserInfoBean();
	        wagApp.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked) {
						user.setAff_id(VEN_WAG);
						if(Constants.IS_PRODUCTION_DEMO)
						{
						    user.setApiKey(Constants.PROD_API_KEY);
						}
						else
						{
						    user.setApiKey(Constants.STAG_API_KEY);
						}
		            	
					}
				}
	        });
	        faceBook=(RadioButton)findViewById(R.id.radioFaceBook);
	        faceBook.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked) {
						user.setAff_id(VEN_FACEBOOK);
		            	user.setApiKey(VEN_FACEBOOK_API_KEY);
					}
				}
	        });
	        vendor=(RadioButton)findViewById(R.id.radioVendor);
	        vendor.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked) {
						user.setAff_id(VEN_GENERAL);
		            	user.setApiKey(VEN_GENERAL_API_KEY);
					}
				}
	        });
	        saveBtn=(Button)findViewById(R.id.saveBtn);
	        
	        saveBtn.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SettingsMenu.this);
					String userInfo  =  getUserJson(setiingUserInfo());
			        prefs.edit().putString("Share_userInfo", userInfo).commit();
			        finish();
			}
				
        });
	      
	    }
	 
	 @Override
	protected void onResume() {
	    super.onResume();
	    UserInfoBean userInfoBean = getUser();
	    fName.setText(userInfoBean.fName);
	    lName.setText(userInfoBean.lName);
	    email.setText(userInfoBean.email);
	    phNumber.setText(userInfoBean.phNumber);
	    checkoutUrl.setText(userInfoBean.checkoutUrl);
	    
	    if(userInfoBean.affId.equalsIgnoreCase(VEN_WAG)){
	        
	        wagApp.setChecked(true);
	    }else if(userInfoBean.affId.equalsIgnoreCase(VEN_FACEBOOK))
	    {
	        faceBook.setChecked(true);
	    }else
	    {
	        vendor.setChecked(true);
	    }
	    
	    
	}
	 
	 private UserInfoBean getUser(){
	        UserInfoBean user = new UserInfoBean();
	        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SettingsMenu.this);
	        String mJsonUser = prefs.getString("Share_userInfo", null);
	        
	        try{
	            
	            if(null != mJsonUser ){
	                
	                JSONObject j = new JSONObject(mJsonUser);
	                user.fName = j.getString(SettingsMenu.FIRST_NAME);
	                user.lName = j.getString(SettingsMenu.LAST_NAME);
	                user.email = j.getString(SettingsMenu.EMAIL);
	                user.phNumber = j.getString(SettingsMenu.PHONE_NUMBER);
	                user.affId = j.getString(SettingsMenu.AFF_ID);
	                user.apiKey = j.getString(SettingsMenu.APP_KEY);
	                user.checkoutUrl = j.getString(SettingsMenu.CHECKOUT_URL);
	            }

	        }catch(Exception e){
	            e.printStackTrace();
	        }
	        
	        return user;
	    }
	 private String getUserJson(UserInfoBean user){
		 JSONObject json = new JSONObject();
		 try{
			 json.put(FIRST_NAME, user.fName);
			 json.put(LAST_NAME, user.lName);
			 json.put(EMAIL, user.email);
			 json.put(PHONE_NUMBER, user.phNumber);
			 json.put(AFF_ID, user.affId);
			 json.put(APP_KEY, user.apiKey);
			 json.put(CHECKOUT_URL, user.checkoutUrl);
		 }catch(Exception e){
		 }
		 return json.toString();
	 }

	private UserInfoBean setiingUserInfo() {
		
		user.setFirstName(fName.getText().toString());
		user.setLastName(lName.getText().toString());
		user.setEmail(email.getText().toString());
		user.setPhNumber(phNumber.getText().toString());
		user.setCheckoutUrl(checkoutUrl.getText().toString());
		return user;
		
	}  

}
