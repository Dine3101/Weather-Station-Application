import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/***WeatherStation Entity:
 * city - String
 * measurements - List<Measurement>
 *stations - static List<WeatherStation>
 */
public class WeatherStation implements Serializable {

    private String city;
    private List<Measurement> measurements;
    private static List<WeatherStation> stations;
    private static SparkConf sparkConf;
    private static JavaSparkContext sparkContext;

    /**
     * Implemented the following task using Spark and RDD
     * Number of times a temperature in the intervals
     *  [t1-r..t1+r], [t2-r..t2+r]
     *  has been measured so far by any
     * of the weather stations in stations.*/
    public static int countTemperature(double t){
        int count=0;

        // Creating a spark context if not initialized previously in other methods
        if(sparkContext==null){
            initSparkContext();
        }

        count=(int)sparkContext.parallelize(stations) // Creating a RDD of weather stations - JavaRDD<WeatherStation>
                // Transformation - collect temperatures measured by each weather station in a new RDD<Double>
                .flatMap(station -> station.getMeasurements().parallelStream()
                        .map(measurement->measurement.getTemperature())
                        .iterator())
                // Transformation - checking if the temperature measured a weather station can be a candidate solution
                .filter(temp -> temp>=t-1 && temp<=t+1)
                // Action - count the temperatures in resulting RDD<Double> temperatures
                .count();

        return count;
    }

    /*** Return max temperature measured by the station in [startTime,endTime}*/
    public double maxTemperature(int startTime, int endTime){

        double maxTemperature=0.0;

        if(startTime>endTime){
            return maxTemperature;
        }

        maxTemperature=measurements.stream()
                .filter(measurement->measurement.getTime()>=startTime && measurement.getTime()<=endTime)
                .map(measurement->measurement.getTemperature()) // Stream<Measurement> -> Stream<Double>
                .max(Double::compareTo) // Using compareTo in Double class
                .orElse(0.0); // if Double object is null, return 0.0 - default temperature

        return maxTemperature;
    }

    /**
     * Number of times a temperature in the intervals
     *  [t1-r..t1+r], [t2-r..t2+r]
     *  has been measured so far by any
     * of the weather stations in stations.*/
    public static List<Map.Entry<Double,Integer>> countTemperatures(double t1,double t2,double r){

        // Using Map.Entry<> for Pairing Temperature and Count
        List<Map.Entry<Double,Integer>> countList=new ArrayList<>();
        countList.add(Map.entry(t1,countTemperatures(t1,r)));
        countList.add(Map.entry(t2,countTemperatures(t2,r)));
        return countList;
    }

    /**
     * Count temperatures measured by each weatherstation
     * that fit in [t-r..t+r]*/
    public static int countTemperatures(double t,double r){

        //Following MapReduce Architecture/Approach

        return stations.stream()// if large number of stations are expected, then will proceed with parallel stream here
                .flatMap(weatherStation ->
                        weatherStation
                                .getMeasurements()
                                .parallelStream() /**Splitting - Measurements ( each thread process some measurement objects ) */
                                .filter(measurement-> // checking if the temperature can be a candidate ( between t-r and t+r )
                                        measurement
                                                .getTemperature()>=t-r && measurement.getTemperature()<=t+r)
                                .map(measurement-> /** Mapping - creating intermediate key ( temperature, 1 - default count value ) */
                                        Map.entry(measurement.getTemperature(),1))) // Stream<Entry<Double,Integer>>
                // flatmap flattened Stream<Stream<Entry<Double,Integer>> to Stream<Entry<Double,Integer>>
                /**Shuffling Phase - grouping by temperature as key and value as List<Map.Entry<temperature,count>>*/
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.toList()))
                // returns Map<Temperature, List<Entries> - [{t,1},{t,1},{t,1}..,{t,1}]
                .entrySet() // After shuffling, {temp,[{temp,1}....{temp,1}]}
                .parallelStream() // giving entries to different reduce threads or workers
                /**reducing {temp,[{temp,1}....{temp,1}]} - {temp,count}*/
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        pair->pair
                                .getValue()
                                .stream()
                                .mapToInt(Map.Entry::getValue)
                                .sum()
                ))
                // Next calculating total count of all candidate temperatures
                .values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    /**Add Measurement Object to measurements list*/
    public void addMeasurement(Measurement measurement){

        if(measurement==null){
            return;
        }

        if(this.measurements==null){
            this.measurements=new ArrayList<>();
        }

        this.measurements.add(measurement);
    }

    /***Constructors, Getters, Setters and toString methods*/
    public WeatherStation(){}

    public WeatherStation(String city, List<Measurement> measurements) {
        this.city = city;
        this.measurements = measurements;
    }

    public WeatherStation(String city){
        this.city=city;
    }


    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<Measurement> measurements) {
        this.measurements = measurements;
    }

    public static List<WeatherStation> getStations() {
        return stations;
    }

    public static void setStations(List<WeatherStation> stations) {
        WeatherStation.stations = stations;
    }

    public static void addStation(WeatherStation station){

        if(station==null){
            return;
        }

        if(stations==null){
            stations=new ArrayList<>();
        }

        stations.add(station);
    }

    @Override
    public String toString() {
        return "WeatherStation{" +
                "city='" + city +
                "', measurements=" + measurements +
                "}\n";
    }

    /***
     * Initializing Spark context
     */
    public static void initSparkContext(){
        sparkConf=new SparkConf().setMaster("local").setAppName("Weather-Station");
        sparkContext=new JavaSparkContext(sparkConf);
    }

    /***
     * Closing Spark Context
     */
    public static void closeSparkContext(){
        if(sparkContext!=null){
            sparkContext.close();
        }
    }
}
