public class data {
    private double ax;
    private double ay;
    private double az;
    private double mx;
    private double my;
    private double mz;
    private double gpslat;
    private double gpslong;
    public data()
    {}

    public data(double ax, double ay, double az, double mx, double my, double mz, double gpslat, double gpslong) {
        this.ax = ax;
        this.ay = ay;
        this.az = az;
        this.mx = mx;
        this.my = my;
        this.mz = mz;
        this.gpslat = gpslat;
        this.gpslong = gpslong;
    }



    public double getAx() {
        return ax;
    }

    public double getAy() {
        return ay;
    }

    public double getAz() {
        return az;
    }

    public double getMx() {
        return mx;
    }

    public double getMy() {
        return my;
    }

    public double getMz() {
        return mz;
    }

    public double getGpslat() {
        return gpslat;
    }

    public double getGpslong() {
        return gpslong;
    }



}
