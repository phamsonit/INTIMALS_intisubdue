package convert;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

public class Util {
    /**
     * Encode a String using UTF-8 characters
     * @param s
     * @return
     */
    public static String encode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "ENCODING ERROR";
        }
    }

    /**
     * Decode a UTF-8 String
     * @param s
     * @return
     */
    public static String decode(String s) {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "ENCODING ERROR";
        }
    }

    public static String formatLabel(String label){
        Set<Character> speChars = new HashSet<>();
        speChars.add('.');
        speChars.add('%');
        speChars.add('$');
        speChars.add('-');
        speChars.add('"');

        for(Character spe:speChars){
            label = label.replace(spe,'_');
        }

        label = label.replace("__3C","_");
        label = label.replace("_3C","");
        label = label.replace("_3E","");

        return label;
    }
}
