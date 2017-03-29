package tk.dzrcc.game;

import tk.dzrcc.entities.Code;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static tk.dzrcc.TextConstants.DEFAULT_CODE;

/**
 * Created by Maksim on 29.03.2017.
 */
public class CodesAnalyzer {

    public static String getLevelStatistic(List<ArrayList<Code>> sectors, Integer sector, String level) {
        if (sectors.get(sector-1).size()==1) return null;
        List<Code> levelList = sectors.get(sector-1)
                .stream()
                .filter(x -> x.getLevel().equals(level))
                .collect(Collectors.toList());

        Long countFound = levelList
                .stream()
                .filter(Code::getGotten)
                .count();
        return countFound+"/"+levelList.size();
    }

    public static String getSectorStatistic(List<ArrayList<Code>> sectors, Integer sector){
        Long countGottenInSector = 0L;
        countGottenInSector += sectors.get(sector-1)
                .stream()
                .filter(Code::getGotten)
                .count();
        return countGottenInSector+"/"+sectors.get(sector-1).size();
    }

    public static Code analyzeCodes(List<ArrayList<Code>> parsedSectors, List<ArrayList<Code>> sectors, String codeVal){
        ArrayList<Code> curSector;
        Code currentCode = null;
        Code parsedCode;
        boolean found = false;

        if (!sectors.isEmpty()){
            for(int sector = 0; sector < parsedSectors.size(); sector++){
                curSector = parsedSectors.get(sector);
                for(int code = 0; code < curSector.size(); code++){
                    currentCode = sectors.get(sector).get(code);
                    parsedCode = parsedSectors.get(sector).get(code);
                    if (parsedCode.getValue().length()>4 && !currentCode.getGotten()){
                        currentCode.setGotten(true);
                        if (codeVal != null) {
                            currentCode.setCode(codeVal);
                            found = true;
                        } else {
                            currentCode.setCode(DEFAULT_CODE);
                        }
                    }
                    if (found) break;
                }
                if (found) break;
            }
        }

        return found ? currentCode : null;
    }
}
