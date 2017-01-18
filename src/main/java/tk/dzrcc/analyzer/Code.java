package tk.dzrcc.analyzer;

/**
 * Created by Maksim on 18.01.2017.
 */
public class Code {
    private static final String TO_STRING_PATTERN = "Сектор: %s;\nСложность: %s;\nПорядковый номер: %s.";
    private String value;
    private Integer sector;
    private String level;
    private Integer numberInLevel;
    private Boolean isGotten = false;

    public Code() {
    }

    public Code(String value, String level) {
        this.value = value;
        this.level = level;
    }

    public Code(String value, String level, Integer sector, Integer numberInLevel) {
        this.value = value;
        this.sector = sector;
        this.level = level;
        this.numberInLevel = numberInLevel;
    }

    public void setSector(Integer sector) {
        this.sector = sector;
    }

    public Integer getSector(){
        return sector;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Boolean getGotten() {
        return isGotten;
    }

    public void setGotten(Boolean gotten) {
        isGotten = gotten;
    }

    public Integer getNumberInLevel() {
        return numberInLevel;
    }

    public void setNumberInLevel(Integer numberInLevel) {
        this.numberInLevel = numberInLevel;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format(TO_STRING_PATTERN, sector, level, numberInLevel);
    }
}
