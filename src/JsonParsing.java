import org.json.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

public class JsonParsing {
    private JsonParsing() {

    }

    private static String getStringFroUrl(String url) throws IOException {
        try(Scanner urlScanner = new Scanner(new URL(url).openStream(), StandardCharsets.UTF_8.toString())){
            urlScanner.useDelimiter("\\A");
            return urlScanner.hasNext() ? urlScanner.next() : "";
        }
    }

    public static Map<String, String> getCurrencyCodesNames() throws IOException {
        Map<String, String> currencyNames = new HashMap<>();
        currencyNames.put("USD", "Dolar amerykański");
        currencyNames.put("EUR", "Euro");
        currencyNames.put("CHF", "Frank szwajcarski");
        return currencyNames;
    }
    private static Currency createCurrencyOfJSON(String jsonString){
        JSONObject currencyObject = new JSONObject(jsonString);
        String name = currencyObject.getString("currency");
        String code = currencyObject.getString("code");
        JSONArray rates = currencyObject.getJSONArray("rates");
        Map<LocalDate, Double> ratesMap = new HashMap<>();
        for(int i = 0; i<rates.length(); i++){
            JSONObject rate = rates.getJSONObject(i);
            String stringDate = rate.getString("effectiveDate");
            Double mid = Double.valueOf(rate.getFloat("mid"));
            LocalDate ratingDate = LocalDate.parse(stringDate);
            ratesMap.put(ratingDate, mid);
        }
        return new Currency(name, code, ratesMap);
    }

    public static Currency getCurrencyDetails(String code, LocalDate start, LocalDate end)
            throws IOException {
        if(Duration.between(end.atStartOfDay(), start.atStartOfDay()).toDays() > 377) return null;
        String urlString = "https://api.nbp.pl/api/exchangerates/rates/a/"+code+"/"+start+"/"+end+"?format=json";
        String jsonString;
        try {
            jsonString = getStringFroUrl(urlString);
        }catch (FileNotFoundException ex){
            urlString = "https://api.nbp.pl/api/exchangerates/rates/b/"+code+"/"+start+"/"+end+"?format=json";
            jsonString = getStringFroUrl(urlString);
        }
        Currency currency = createCurrencyOfJSON(jsonString);
        return currency;
    }

}
