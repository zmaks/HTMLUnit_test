package tk.dzrcc;

import tk.dzrcc.entities.Code;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Maksim on 31.03.2017.
 */
public class CodeHandler {
    private static final Integer GOTTEN_CODE_VALUE_LENGTH = 4;
    private static Pattern codePattern = Pattern.compile("\\d\\+?");

    public static Boolean isTakenCode(String value){
        return value.length() > GOTTEN_CODE_VALUE_LENGTH;
    }

    public static Code setCodeAsGotten(Code code, String codeVal){
        code.setGotten(true);
        String c = codeVal == null ? "?" : codeVal;
        code.setCode(c);
        return code;
    }

    public static Code createCode(String value, Integer sector, Integer num){
        Matcher curCodeMatcher = codePattern.matcher(value);
        String level = null;
        if (curCodeMatcher.find()) level = curCodeMatcher.group();
        Code code = new Code(value, level, sector, num);
        if (isTakenCode(value)) {
            setCodeAsGotten(code, null);
        }

        return code;
    }
}
