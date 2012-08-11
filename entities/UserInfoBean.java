/*
* Copyright 2012 Walgreen Co. All rights reserved *

* Licensed under the Walgreens Developer Program and Portal Terms of Use and API License Agreement, Version 1.0 (the “Terms of Use”)
* You may not use this file except in compliance with the License.
* A copy of the License is located at https://developer.walgreens.com/page/terms-use
*
* This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
* express or implied. See the License for the specific language governing  permissions and limitations under the License.
*/
package com.usablenet.walgreen.appathon.entities;

import com.usablenet.walgreen.appathon.SettingsMenu;


public class UserInfoBean {
	
	public String fName;
	public String lName;
	public String email;
	public String phNumber;
	public String affId;
	public String apiKey;
	public String checkoutUrl; 
	
	public UserInfoBean(){
		this.fName = "";
		this.lName = "";
		this.email = "";
		this.phNumber = "";
		this.affId = SettingsMenu.VEN_GENERAL;
		this.checkoutUrl = "";
		this.apiKey = SettingsMenu.VEN_GENERAL_API_KEY;
	}
	
	public void setFirstName(String fName){
		this.fName=fName;
		
	}
	
	public void setLastName(String lName){
		this.lName=lName;
		
	}
	
	public void setEmail(String email){
		this.email=email;
		
	}
	
	public void setPhNumber(String phNo){
		this.phNumber=phNo;
		
	}
	
	public void setAff_id(String aff_id){
		this.affId=aff_id;
		
	}
	
	public void setApiKey(String apiKey){
		this.apiKey=apiKey;
		
	}
	
	public void setCheckoutUrl(String url){
		this.checkoutUrl = url;
	}
	
	public String getFirstName(){
		return fName;
	}
	
	
	public String getLastName(){
		return lName;
	}
	
	
	public String getEmail(){
		return email;
	}
	
	
	public String getPhNumber(){
		return phNumber;
	}
	
	public String getAff_Id(){
		return affId;
	}
	
	public String getApiKey(){
		return apiKey;
	}
	
	public String getCheckoutUrl(){
		return checkoutUrl;
	}
	

}
