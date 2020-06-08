import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;

public class Currency {
    private double mid;
    private String name;
    private String code;
    private Map<LocalDate, Double> rates;

    public Currency(String name, String code, Map<LocalDate, Double> rates){
        this.name = name;
        this.code = code;
        this.rates = rates;
        LocalDate newestDate = null;
        for(LocalDate date : rates.keySet()){
            if(newestDate == null || isCloserToCurrentDate(newestDate, date))
                newestDate = date;
        }
        this.mid = rates.get(newestDate).doubleValue();
    }

    @Override
    public String toString(){
        return name + " ("+code+") :"+mid;
    }

    public Map<LocalDate, Double> getRates() {
        return rates;
    }

    private boolean isCloserToCurrentDate(LocalDate oldNewest, LocalDate newNewest){
        long oldDiff = Math.abs(Duration.between(oldNewest.atStartOfDay(), LocalDate.now().atStartOfDay()).toDays());
        long newDiff = Math.abs(Duration.between(newNewest.atStartOfDay(), LocalDate.now().atStartOfDay()).toDays());
        return oldDiff > newDiff;
    }


    private LocalDate getMinValueDate(){
        LocalDate minValDate = null;
        for(LocalDate date : rates.keySet()){
            if(minValDate == null || rates.get(minValDate) > rates.get(date)){
                minValDate = date;
            }
        }
        return minValDate;
    }

    private LocalDate getMaxValueDate(){
        LocalDate maxValDate = null;
        for(LocalDate date : rates.keySet()){
            if(maxValDate == null || rates.get(maxValDate) < rates.get(date)){
                maxValDate = date;
            }
        }
        return maxValDate;
    }

    private LocalDate getClosestDate(LocalDate givenDate){
        LocalDate closest = null;
        for(LocalDate date : rates.keySet()){
            if(closest == null){
                closest = date;
                continue;
            }
            if( Math.abs(Duration.between(givenDate.atStartOfDay(), date.atStartOfDay()).toDays()) < Math.abs(Duration.between(givenDate.atStartOfDay(), closest.atStartOfDay()).toDays())){
                if(!date.equals(givenDate)){
                    closest = date;
                }
            }
        }
        return closest;
    }

    private LocalDate getMinShiftDate(){
        LocalDate minShiftDate = null;
        double minDiff = Double.MAX_VALUE;
        for(LocalDate date : rates.keySet()){
            double diff = Math.abs(rates.get(date) - rates.get(getClosestDate(date)));
            if(diff != 0 && diff < minDiff){
                minDiff = diff;
                minShiftDate = date;
            }
        }
        return minShiftDate;
    }

    private LocalDate getMaxShiftDate(){
        LocalDate maxShiftDate = null;
        double maxDiff = 0;
        for(LocalDate date : rates.keySet()){
            double diff = Math.abs(rates.get(date) - rates.get(getClosestDate(date)));
            if(diff >= maxDiff){
                maxDiff = diff;
                maxShiftDate = date;
            }
        }
        return maxShiftDate;
    }

    public String getName() {
        return name;
    }


    public String getCode() {
        return code;
    }

    public double getMin(){
        double smallest = Double.MAX_VALUE;
        for(Double d : rates.values()){
            if(d < smallest) smallest = d;
        }
        return smallest;
    }

    public double getMax(){
        double biggest = Double.MIN_VALUE;
        for(Double d : rates.values()){
            if(d> biggest) biggest = d;
        }
        return biggest;
    }

    public String[][] getTableData(){
        String[] firstRow = {"Today value", Double.toString(mid)};

        LocalDate minValueDate = getMinValueDate();
        double minValue = rates.get(minValueDate);
        String[] minValueRow = {"\n" +
                "Current course", minValue + " on " + minValueDate};

        LocalDate maxValueDate = getMaxValueDate();
        double maxValue = rates.get(maxValueDate);
        String[] maxValueRow = {"Maximal value", maxValue + " on " + maxValueDate};

        LocalDate maxShiftDate = getMaxShiftDate();
        LocalDate scndMaxShiftDate = getClosestDate(maxShiftDate);
        double maxValueChange = Math.abs(rates.get(maxShiftDate) - rates.get(scndMaxShiftDate));
        String[] maxShiftRaw = {"Maximal value change", maxValueChange + " on " + maxShiftDate};

        LocalDate minShiftDate = getMinShiftDate();
        LocalDate scndMinShiftDate = getClosestDate(minShiftDate);
        double minValueChange = Math.abs(rates.get(minShiftDate) - rates.get(scndMinShiftDate));
        String[] minShiftRaw = {"Minimal value change", minValueChange + " on " + minShiftDate};

        String[] todayWithMaxRow = {"Difference with max value", Double.toString(mid - maxValue)};
        String[] todayWithMinRow = {"Difference with min value", Double.toString(mid - minValue)};

        return new String[][]{
                firstRow,
                minValueRow,
                maxValueRow,
                minShiftRaw,
                maxShiftRaw,
                todayWithMinRow,
                todayWithMaxRow
        };
    }
}
