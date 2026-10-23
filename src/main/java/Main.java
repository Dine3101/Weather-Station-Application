import java.util.ArrayList;
import java.util.List;

/*** Test Class ***/
public class Main {

    /*** Creating three weather stations to test CountTemperature() ***/
    public static void main(String[] args) {

        WeatherStation station1 = new WeatherStation("Station-1");
        station1.addMeasurement(new Measurement(0, 9.0));
        station1.addMeasurement(new Measurement(100, 10.0));
        station1.addMeasurement(new Measurement(1900, 11.0));
        station1.addMeasurement(new Measurement(2100, 14.0));

        WeatherStation station2 = new WeatherStation("Station-2");
        station2.addMeasurement(new Measurement(300, 10.5));
        station2.addMeasurement(new Measurement(500, 15.0));
        station2.addMeasurement(new Measurement(1100, 19.0));
        station2.addMeasurement(new Measurement(1800, 20.0));

        WeatherStation station3 = new WeatherStation("Station-3");
        station3.addMeasurement(new Measurement(120, 8.9));
        station3.addMeasurement(new Measurement(1050, 16.0));
        station3.addMeasurement(new Measurement(1440, 21.0));
        station3.addMeasurement(new Measurement(2043, 22.1));

        //Creating the main Stations list in WeatherStation

        List<WeatherStation> testStations = new ArrayList<>();
        testStations.add(station1);
        testStations.add(station2);
        testStations.add(station3);

        WeatherStation.setStations(testStations);

        //Initializing Spark context
        WeatherStation.initSparkContext();

        //Testing
        List<String> testResults=new ArrayList<>();
        testResults.add(checkCount(10.0, 4));
        testResults.add(checkCount(20.0, 3));
        testResults.add(checkCount(15.0, 3));

        //Closing Spark Context
        WeatherStation.closeSparkContext();

        //Reporting Test Results
        System.out.println("\nInput:\nStations: "+WeatherStation.getStations());
        System.out.println("\nTest Results:");
        for(String result:testResults){
            System.out.println(result);
        }
    }

    private static String checkCount(double t, int expected) {
        int actual = WeatherStation.countTemperature(t);

        return String.format("Target %.1f: expected count: %d, got %d — %s",
                t,
                expected,
                actual,
                actual == expected ? "PASS" : "FAIL"
        );
    }
}
