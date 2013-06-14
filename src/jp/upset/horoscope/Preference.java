package jp.upset.horoscope;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.provider.Settings.Secure;

public class Preference {
	private static SharedPreferences mPref;

	//这里是4个标签的链接地址
	public static String[] Urls = {
			"http://senzaki.neocosmic.info/iinekko/index.php/Diagnosiss/index",
			"http://senzaki.neocosmic.info/iinekko/index.php/Results/distance",
			"http://senzaki.neocosmic.info/iinekko/index.php/Comments/news",
			"http://senzaki.neocosmic.info/iinekko/index.php/Informations/index",
			"http://senzaki.neocosmic.info/iinekko/index.php/Publics/userSession" };
	
	public static String getUuid(Context context){
		return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
	}

	public static SharedPreferences getPref(Context context) {
		if (mPref == null) {
			mPref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
		}
		return mPref;
	}

	public static void setMessageCount(Context context, int count) {
		Editor editor = getPref(context).edit();
		editor.putInt("msg_count", count);
		editor.commit();
	}

	public static int getMessageCount(Context context) {
		return getPref(context).getInt("msg_count", 0);
	}
}
