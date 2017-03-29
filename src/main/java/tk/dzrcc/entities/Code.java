package tk.dzrcc.entities;

/**
 * Created by Maksim on 18.01.2017.
 */
public class Code {
    private static final String TO_STRING_PATTERN = "Сектор: %s\nСложность: %s\nПорядковый номер: %s";
    private String code;
    private String value;
    private Integer sector;
    private String level;
    private Integer numberInSector;
    private Boolean isGotten = false;

    public Code() {
    }

    public Code(String value, String level) {
        this.value = value;
        this.level = level;
    }

    public Code(String value, String level, Integer sector, Integer numberInSector) {
        this.value = value;
        this.sector = sector;
        this.level = level;
        this.numberInSector = numberInSector;
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

    public Integer getNumberInSector() {
        return numberInSector;
    }

    public void setNumberInSector(Integer numberInSector) {
        this.numberInSector = numberInSector;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format(TO_STRING_PATTERN, sector, level, numberInSector);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Code)) return false;

        Code code = (Code) o;

        return getLevel() != null ? getLevel().equals(code.getLevel()) : code.getLevel() == null;
    }

    @Override
    public int hashCode() {
        return getLevel() != null ? getLevel().hashCode() : 0;
    }
}
