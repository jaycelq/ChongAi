package me.qiang.android.chongai.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by LiQiang on 1/2/15.
 */
public class IMEI {
    public static boolean isIMEIValid(String verifyCode){
        Pattern p = Pattern.compile("^\\d{15}$");
        Matcher m = p.matcher(verifyCode);
        return m.matches();
    }
}
