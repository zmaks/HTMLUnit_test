package tk.dzrcc.analyzer;

import java.util.Date;

/**
 * Created by Maksim on 18.01.2017.
 */
public class GottenCode {
    private String code;
    private String player;
    private Date date;

    public GottenCode(String code, String player, Date date) {
        this.code = code;
        this.player = player;
        this.date = date;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
