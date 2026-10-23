import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Stream;

/***Test Class*/
public class Main {

    private static Scanner scanner;
    private static Random random;
    private static int totMeasurementCount; //Count for Measurement list used by all stations during test data generation
    private static int totStationCount; //Count for Station list created during test data generation
    private static int totTestRuns; //Count of tests for each station during random test by genTest method
    private static List<WeatherStation> stations; //List of stations created during test data generation by generateStations method

    /***Random Hour and Minute Generation*/
    public static int getRandTime(){
        int randMin=random.nextInt(60); //0->59
        int randHr=random.nextInt(24); //0->23
        int time=randHr*100+randMin; //HH:MM -> HHMM
        return time;
    }

    /**HHMM -> HH:MM*/
    public static String getTimeString(int time){

        int hr=time/100;
        int min=time%100;
        String hrStr=((hr<10)?"0":"")+hr;
        String minStr=((min<10)?"0":"")+min;

        return hrStr+":"+minStr;
    }

    /***Generate List of Measurements with random temperature and time*/
    public static List<Measurement> generateMeasurements(){

        return Stream.generate(()->{

            int time=getRandTime();
            double temperature=Math.round((random.nextDouble(100)*100)/100);
            return new Measurement(time,temperature);

        }).limit(totMeasurementCount).toList();

    }

    /***Generate List of Stations with Measurements created in generateMeasurements() method**/
    public static List<WeatherStation> generateStations(List<Measurement> measurements){

        List<WeatherStation> stations=new ArrayList<>();

        for(int i=1;i<=totStationCount;i++){
            WeatherStation station=new WeatherStation("city-"+i);
            for(int j=0;j<Math.min(i,totMeasurementCount);j++){
                station.addMeasurement(measurements.get(j));
            }
            stations.add(station);
        }
        return stations;
    }

    /***Interactive Test Method - Get City, StartTime and EndTime from user*/
    public static void intTest(){

        try{

            System.out.print("Enter location: ");
            String location = scanner.nextLine().trim();

            System.out.print("Enter start time (HHMM) : ");
            int startTime = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter end time (HHMM) : ");
            int endTime = Integer.parseInt(scanner.nextLine().trim());

            WeatherStation station = WeatherStation.getStations().stream()
                    .filter(s -> s.getCity().equalsIgnoreCase(location))
                    .findFirst()
                    .orElse(null);

            if (station == null) {
                System.out.println("No weather station found for " + location);
            } else if (startTime > endTime) {
                System.out.println("Start time must be before or equal to end time.");
            } else {
                double maxTemperature = station.maxTemperature(startTime, endTime);
                System.out.printf(
                        "Maximum temperature in %s from %s to %s: %.2f°C%n",
                        station.getCity(), getTimeString(startTime), getTimeString(endTime), maxTemperature
                );
            }

        } catch (Exception e) {
            System.out.println("Enter valid location!");
        }


    }

    /***Random Test Method - Generate random starttime and endtime and run test for totTestRuns for each station*/
    public static void genTest(List<WeatherStation> stations){

        for(WeatherStation station:stations){
            for(int i=1;i<=totTestRuns;i++){
                int randStartTime=getRandTime();
                int randEndTime=getRandTime();
                int loopBreaker=5; //to break the following loop to get valid starttime and endtime
                while(randStartTime>randEndTime && loopBreaker>0){
                    randStartTime=getRandTime();
                    randEndTime=getRandTime();
                    loopBreaker--;
                }

                double maxTemperature=station.maxTemperature(randStartTime,randEndTime);

                System.out.println("Maximum Temperature measured between ("+getTimeString(randStartTime)+","
                        +getTimeString(randEndTime)+") by weather station at "+station.getCity()+": "+maxTemperature);
            }

        }
    }

    /***Interactive session to choose test method - Random Test method or Interactive Test method*/
    public static void startTest1(){

        boolean continueTest=true;

        try {
            while(continueTest){

                System.out.println("\nData:");
                System.out.println("Stations:"+stations);

                System.out.println("\nStarting Test....");

                System.out.println("""
                        
                        Choose the test method:
                        1.RandomTest
                        2.InteractiveTest
                        3.exit""");
                System.out.print("Enter your option: ");

                int option=Integer.parseInt(scanner.nextLine().trim());
                switch(option){
                    case 1:genTest(stations); break;
                    case 2:intTest();break;
                    case 3:continueTest=false;break;
                    default:System.out.println("Enter a valid option!");
                }

                System.out.println("\nTest completed...");
                Thread.sleep(2000);
            }
        }catch(Exception e){
            System.out.println("Error while choosing test method");
        }

    }

    public static void main(String[] args){

        random=new Random();
        totMeasurementCount=5;
        totStationCount=5;
        totTestRuns=2;

        //Generate test data
        List<Measurement> measurements=generateMeasurements();
        stations=generateStations(measurements);
        WeatherStation.setStations(stations);


        //Start Test
        System.out.println("*****************Testing*****************");

        scanner=new Scanner(System.in);

        //Task1 Test
        System.out.println("Testing Task1: maxTemperature");
        startTest1();

        System.out.println("Exited!");
    }
}
