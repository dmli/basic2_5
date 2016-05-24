package com.ldm.basic.utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.ldm.basic.bean.BasicContactBean;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;

/**
 * Created by LDM on 2013-5-22. 
 * 通讯录使用助手 仅能查询出：名称、电话、头像
 */
public class ContactHelper {

	private static List<BasicContactBean> contacts, contacts2;

	/**
	 * 得到手机通讯录联系人信息
	 *
	 * @param context Context
	 */
	public static void initContactsToCache(Context context) {
		ContentResolver resolver = context.getContentResolver();
		// 获取手机联系人
		Cursor phoneCursor = resolver.query(Phone.CONTENT_URI, new String[] { Phone.DISPLAY_NAME, Phone.NUMBER, Phone.CONTACT_ID, Phone.PHOTO_ID }, null, null, null);
		if (phoneCursor != null && phoneCursor.moveToFirst()) {
			contacts = new ArrayList<BasicContactBean>();

			int PHONES_DISPLAY_NAME_INDEX = phoneCursor.getColumnIndex(Phone.DISPLAY_NAME);
			int PHONES_NUMBER_INDEX = phoneCursor.getColumnIndex(Phone.NUMBER);
			int PHONES_PHOTO_ID_INDEX = phoneCursor.getColumnIndex(Phone.PHOTO_ID);
			int PHONES_CONTACT_ID_INDEX = phoneCursor.getColumnIndex(Phone.CONTACT_ID);

			do {
				// 得到手机号码
				String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
				// 当手机号码为空的或者为空字段 跳过当前循环
				if (TextUtils.isEmpty(phoneNumber))
					continue;
				// 得到联系人名称
				String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);
				// 得到联系人ID
				Long contactId = phoneCursor.getLong(PHONES_CONTACT_ID_INDEX);
				// 得到联系人头像ID
				Long photoId = phoneCursor.getLong(PHONES_PHOTO_ID_INDEX);
				// 得到联系人头像Bitamp
				Drawable contactPhoto = null;
				if (photoId > 0) {
					Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
					InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(resolver, uri);
					BitmapFactory.decodeStream(input);
					contactPhoto = new BitmapDrawable(context.getResources(), input);
				}
				contacts.add(new BasicContactBean(contactName, phoneNumber, contactPhoto));
			} while (phoneCursor.moveToNext());
			if (phoneCursor != null) {
				phoneCursor.close();
			}
		}
	}

	/**
	 * 得到手机通讯录联系人信息（不返回头像）
	 *
	 * @param context Context
	 */
	public static void initContactsToCache2(Context context) {
		try {
			ContentResolver resolver = context.getContentResolver();
			// 获取手机联系人
			Cursor phoneCursor = resolver.query(Phone.CONTENT_URI, new String[] { Phone.DISPLAY_NAME, Phone.NUMBER, Phone.CONTACT_ID, Phone.PHOTO_ID }, null, null, null);
			if (phoneCursor != null && phoneCursor.moveToFirst()) {
                contacts2 = new ArrayList<BasicContactBean>();

                int PHONES_DISPLAY_NAME_INDEX = phoneCursor.getColumnIndex(Phone.DISPLAY_NAME);
                int PHONES_NUMBER_INDEX = phoneCursor.getColumnIndex(Phone.NUMBER);
                do {
                    // 得到手机号码
                    String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
                    // 当手机号码为空的或者为空字段 跳过当前循环
                    if (TextUtils.isEmpty(phoneNumber))
                        continue;
                    // 得到联系人名称
                    String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);
                    contacts2.add(new BasicContactBean(contactName, phoneNumber, null));
                } while (phoneCursor.moveToNext());
                if (phoneCursor != null) {
                    phoneCursor.close();
                }
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取通讯录数据，优先使用缓存
	 *
	 * @param context Context
	 * @return 列表集合
	 */
	public static List<BasicContactBean> getContactsToCache(Context context) {
		if (contacts == null || contacts.size() == 0) {
			initContactsToCache(context);
		}
		return contacts;
	}

	/**
	 * 获取通讯录数据，优先使用缓存(不反悔用户头像)
	 *
	 * @param context Context
	 * @return 列表集合
	 */
	public static List<BasicContactBean> getContactsToCache2(Context context) {
		if (contacts == null || contacts.size() == 0) {
			initContactsToCache(context);
		}
		return contacts;
	}

	/**
	 * 获取通讯录，不使用缓存
	 *
	 * @param context Context
	 * @return 列表集合
	 */
	public static List<BasicContactBean> getContacts(Context context) {
		contacts.clear();
		initContactsToCache(context);
		return contacts;
	}

	/**
	 * 获取通讯录，不使用缓存
	 *
	 * @param context Context
	 * @return 列表集合
	 */
	public static List<BasicContactBean> getContacts2(Context context) {
		contacts2.clear();
		initContactsToCache(context);
		return contacts2;
	}

}
