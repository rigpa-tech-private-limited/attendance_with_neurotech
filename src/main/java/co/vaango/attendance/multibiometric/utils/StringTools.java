package co.vaango.attendance.multibiometric.utils;

import android.text.Html;
import android.text.Spanned;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class StringTools {

    public static Spanned changeFontFullText(Object text, boolean bold, String color, boolean biggerText) {
        return changeFontPartOfText(text, text, bold, color, biggerText);
    }

    public static Spanned changeFontPartOfText(Object fullText, Object textToApplyFont, boolean bold, String color,
                                               boolean biggerText) {
        Spanned ret = Html.fromHtml(fullText.toString());
        try {
            int start = fullText.toString().indexOf(textToApplyFont.toString());
            int end = fullText.toString().indexOf(textToApplyFont.toString()) + textToApplyFont.toString().length();
            String boldStartTag = "<b>";
            String boldCloseTag = "</b>";
            if (!bold) {
                boldStartTag = "";
                boldCloseTag = "";
            }
            String bigStartTag = "<big>";
            String bigCloseTag = "</big>";
            if (!biggerText) {
                bigStartTag = "";
                bigCloseTag = "";
            }
            if (color == null)
                ret = Html.fromHtml(fullText.toString().substring(0, start) + boldStartTag + bigStartTag
                        + textToApplyFont + bigCloseTag + boldCloseTag + fullText.toString().substring(end));
            else {
                ret = Html.fromHtml(fullText.toString().substring(0, start) + boldStartTag + bigStartTag
                        + "<font color=\"" + color + "\">" + textToApplyFont + "</font>" + bigCloseTag + boldCloseTag
                        + fullText.toString().substring(end));
            }
        } catch (Exception e) {
        }
        return ret;
    }

    public static String getCapitalizedString(String string) {
        return string.substring(0, 1).toUpperCase(Locale.getDefault())
                + string.substring(1).toLowerCase(Locale.getDefault());
    }

    public static boolean isEmpty(String string) {
        if (string == null)
            return true;
        else {
            string = string.trim();
            if (string.equals("") || string.equalsIgnoreCase("null") || string.length() == 0)
                return true;
        }
        return false;
    }

    public static float getSimilarity(String string1, String string2) {
        string1 = string1.toLowerCase(Locale.getDefault());
        string2 = string2.toLowerCase(Locale.getDefault());
        if (string1.equals(string2))
            return 100f;
        Set<String> string1Factors = new HashSet<String>();
        Set<String> string2Factors = new HashSet<String>();
        for (int i = 0; i < string1.length() - 2; i++) {
            string1Factors.add(string1.substring(i));
            string1Factors.add(string1.substring(0, string1.length() - 1 - i));
        }
        for (int i = 0; i < string2.length() - 2; i++) {
            string2Factors.add(string2.substring(i));
            string2Factors.add(string2.substring(0, string2.length() - 1 - i));
        }
        Set<String> commonFactors = new HashSet<String>();
        for (String string1Factor : string1Factors)
            for (String string2Factor : string2Factors)
                if (string2Factor.contains(string1Factor))
                    commonFactors.add(string1Factor);
        for (String string2Factor : string2Factors)
            for (String string1Factor : string1Factors)
                if (string1Factor.contains(string2Factor))
                    commonFactors.add(string2Factor);
        float commonality = (commonFactors.size() / ((string1Factors.size() + string2Factors.size()) / 2f)) * 100f;
        return commonality;
    }

    public static String MD5(String string) {
        String hashedString = "";
        MessageDigest mdEnc;
        try {
            mdEnc = MessageDigest.getInstance("MD5");
            mdEnc.update(string.getBytes(), 0, string.length());
            string = new BigInteger(1, mdEnc.digest()).toString(16);
            while (string.length() < 32)
                string = "0" + string;
            hashedString = string;
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        return hashedString;
    }

    public static String getMinutesSecondsTimerString(long milliseconds) {
        long durationInSeconds = milliseconds / 1000;
        long minutes = durationInSeconds / 60;
        String minutesInString = minutes < 10 ? "0" + minutes : String.valueOf(minutes);
        long seconds = durationInSeconds % 60;
        String secondsInString = seconds < 10 ? "0" + seconds : String.valueOf(seconds);
        return minutesInString + ":" + secondsInString;
    }
}
