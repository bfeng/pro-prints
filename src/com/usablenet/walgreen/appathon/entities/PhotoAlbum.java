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

import android.graphics.Bitmap;

import java.util.ArrayList;

import com.usablenet.walgreen.appathon.entities.AlbumThumbnailBean;

public class PhotoAlbum
{
	private int albumID;
	private String albumName;
	private Bitmap albumThumbnail;
	
	private  ArrayList<AlbumThumbnailBean> albumThumbnailList=new ArrayList<AlbumThumbnailBean>();
	private static ArrayList<PhotoAlbum> photoAlbumList=new ArrayList<PhotoAlbum>();
	
	public static void clearAll()
	{
		photoAlbumList.clear();
	}
	
	public void setAlbumID(int ID){
		albumID=ID;
	}
	
	public int getAlbumID(){
	  return albumID;
	}
	
	public void setAlbumName(String Name){
		albumName=Name;
	}
	
	public String getAlbumName(){
	  return albumName;
	}
	
	public void setAlbumThumb(Bitmap TumbnailImg)
	{
		 albumThumbnail=TumbnailImg;
	}
	public Bitmap getAlbumThumb(){
		  return albumThumbnail;
		}
	
	public static PhotoAlbum  createAlbum(int AlbumID,String AlbumName,int ImageID,String ImageName,String imageURI,Bitmap TumbnailImg)
	{
		PhotoAlbum albumObj=new PhotoAlbum();
		albumObj.setAlbumID(AlbumID);
		albumObj.setAlbumName(AlbumName);
		albumObj.setAlbumThumb(TumbnailImg);
		albumObj.addToAlbumThumbnailList(new AlbumThumbnailBean(ImageID, ImageName,imageURI));
		return albumObj;
	}
	
	public  void addToAlbumThumbnailList(AlbumThumbnailBean Thumbnail)
	{
		albumThumbnailList.add(Thumbnail);
	}
	
	public  ArrayList<AlbumThumbnailBean> getAlbumThumbnailList()
	{
		return albumThumbnailList;
	}

	public static void addToPhotoAlbumsList(PhotoAlbum Album) {
		photoAlbumList.add(Album);
		
	}
	public static ArrayList<PhotoAlbum> getPhotoAlbumsList() {
		
		return photoAlbumList;
		
	}
	
	public static PhotoAlbum getPhotoAlbum(String AlbumName) {
		
		for (PhotoAlbum albumObj : photoAlbumList) {
			if(albumObj.getAlbumName().equals(AlbumName))
			{
				return albumObj;
			}
		}
		
		return null;
		
	}
	public static int getTotalAlbumsSize()
	{
		ArrayList<PhotoAlbum> Albums=getPhotoAlbumsList();
		int totalSize=0;
		for (PhotoAlbum photoAlbum : Albums) {
			totalSize+=photoAlbum.getAlbumThumbnailList().size();
		}
		
		return totalSize;
		
	}
}
