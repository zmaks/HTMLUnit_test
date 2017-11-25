package tk.dzrcc.selenium.stages;

/**
 * Created by mazh0416 on 6/26/2017.
 */
public enum HintStatus {
    LOOSE_HINT_1_MIN(1, 29, null, "1 минтуа до слива"),
    LOOSE_HINT_3_MIN(1, 27, LOOSE_HINT_1_MIN, "3 минуты до слива"),
    LOOSE_HINT_5_MIN(1, 25, LOOSE_HINT_3_MIN, "5 минут до слива"),
    SECOND_HINT(1, 0, LOOSE_HINT_5_MIN, "Пришла подсказка 2"),
    SECOND_HINT_1_MIN(0, 59, SECOND_HINT, "1 минтуа до второй подсказки"),
    SECOND_HINT_3_MIN(0, 57, SECOND_HINT_1_MIN, "3 минуты до второй подсказки"),
    FIRST_HINT(0, 30, SECOND_HINT_3_MIN, "Пришла подсказка 1"),
    FIRST_HINT_1_MIN(0, 29, FIRST_HINT, "1 минута до первой подсказки"),
    FIRST_HINT_3_MIN(0, 27, FIRST_HINT_1_MIN, "3 минуты до первой подсказки");


    private int hour;
    private int min;
    private HintStatus next;
    private String message;

    HintStatus(int hour, int min, HintStatus next, String message) {
        this.hour = hour;
        this.min = min;
        this.next = next;
        this.message = message;
    }

    public boolean isTimeToChange(int hour, int min){
        return this.hour == hour && this.min == min;
    }

    public HintStatus getNext() {
        return next;
    }

    public String getMessage() {
        return message;
    }
}
