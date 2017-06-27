package tk.dzrcc.entities;

import java.util.Date;

/**
 * Created by Maksim on 18.01.2017.
 */
public class CodeResponse {
    private String message;
    private Code code;
    private GottenCode gottenCode;
    private String levelStat;
    private String sectorStat;

    public CodeResponse(String message, Code code) {
        this.message = message;
        this.code = code;
    }

    public CodeResponse(String message, Code code, GottenCode gottenCode) {
        this.message = message;
        this.code = code;
        this.gottenCode = gottenCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public GottenCode getGottenCode() {
        return gottenCode;
    }

    public void setGottenCode(GottenCode gottenCode) {
        this.gottenCode = gottenCode;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(message);
        result.append("\n\n");
        if (code!=null){
            result.append(code.toString());
        }
        if (levelStat != null && code != null) {
            result.append("\n\nВзято ");
            result.append(code.getLevel());
            result.append(" сложности: ");
            result.append(levelStat);
        }
        if (sectorStat != null && code != null) {
            result.append("\nВзято в секторе: ");
            result.append(sectorStat);
        }
        if (gottenCode != null) {
            result.append("Код вбил ");
            result.append(gottenCode.getPlayer());
            result.append(" ");
            Integer min = Math.abs((int)(gottenCode.getDate().getTime() - new Date().getTime())/60000);
            result.append(min);
            result.append(" минут назад.");
        }
        return result.toString();
    }

    public String getLevelStat() {
        return levelStat;
    }

    public void setLevelStat(String levelStat) {
        this.levelStat = levelStat;
    }

    public String getSectorStat() {
        return sectorStat;
    }

    public void setSectorStat(String sectorStat) {
        this.sectorStat = sectorStat;
    }
}
