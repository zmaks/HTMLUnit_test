package tk.dzrcc.selenium;


import tk.dzrcc.selenium.stages.HintStatus;
import tk.dzrcc.selenium.stages.SpoilerStatus;

import static tk.dzrcc.Utils.buildGameLink;

/**
 * Created by mazh0416 on 6/26/2017.
 */
public class WardenGame {
    private String taskNumber = "";
    private HintStatus hintStatus = HintStatus.FIRST_HINT_3_MIN;
    private SpoilerStatus spoilerStatus = SpoilerStatus.NO_SPOILER;

    private String authLink;
    private String gameLink;
    private String gameLogin;
    private String gamePass;

    public WardenGame(String authLink, String gameLogin, String gamePass) {
        this.authLink = authLink;
        this.gameLink = buildGameLink(authLink, gameLogin, gamePass);
        this.gameLogin = gameLogin;
        this.gamePass = gamePass;
    }

    public String getTaskNumber() {
        return taskNumber;
    }

    public void setTaskNumber(String taskNumber) {
        this.taskNumber = taskNumber;
    }

    public HintStatus getHintStatus() {
        return hintStatus;
    }

    public void setHintStatus(HintStatus hintStatus) {
        this.hintStatus = hintStatus;
    }

    public SpoilerStatus getSpoilerStatus() {
        return spoilerStatus;
    }

    public void setSpoilerStatus(SpoilerStatus spoilerStatus) {
        this.spoilerStatus = spoilerStatus;
    }

    public String getAuthLink() {
        return authLink;
    }

    public void setAuthLink(String authLink) {
        this.authLink = authLink;
    }

    public String getGameLink() {
        return gameLink;
    }

    public void setGameLink(String gameLink) {
        this.gameLink = gameLink;
    }

    public String getGameLogin() {
        return gameLogin;
    }

    public void setGameLogin(String gameLogin) {
        this.gameLogin = gameLogin;
    }

    public String getGamePass() {
        return gamePass;
    }

    public void setGamePass(String gamePass) {
        this.gamePass = gamePass;
    }
}
