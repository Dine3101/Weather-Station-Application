/***Measurement Entity:
 * time - int
 * temperature - double
 */
public class Measurement {

    private int time;

    private double temperature;

    /***Constructors, Getters, Setters and toString methods*/
    public Measurement(){}

    public Measurement(int time, double temperature) {
        this.time = time;
        this.temperature = temperature;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    /***HHMM -> HH:MM*/
    public String getTimeString(){

        int hr=this.time/100;
        int min=this.time%100;
        String hrStr=((hr<10)?"0":"")+hr;
        String minStr=((min<10)?"0":"")+min;

        return hrStr+":"+minStr;
    }

    @Override
    public String toString() {
        return "{" +
                "time=" + getTimeString() +
                ", temperature=" + temperature +
                '}';
    }
}
