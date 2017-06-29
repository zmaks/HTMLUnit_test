package tk.dzrcc;

/**
 * Created by mazh0416 on 6/29/2017.
 */
public class Utils {
    private static final String SPLITTER = "//";

    public static String buildGameLink(String authLink, String gameLogin, String gamePass) {
        String[] parts = authLink.split(SPLITTER);
        return parts[0]+SPLITTER+gameLogin+":"+gamePass+"@"+parts[1]+"go";
    }
}

