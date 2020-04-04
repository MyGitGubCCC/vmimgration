package simulation.core;

import java.util.List;

public class Job {
    private double jobId;
    private double mips;
    private double ram;
    private double disk;
    private int contraint;
    private List<Cloudlet> cloudletList;

    public Job(
            int jobId,
            int mips,
            int ram,
            long disk,
            int contraint){
        setId(jobId);
        setMips(mips);
        setRam(ram);
        setDisk(disk);
        setContraint(contraint);
    }
    public double getId() {
        return jobId;
    }
    public double getMips() {
        return mips;
    }
    public double getRam() {
        return ram;
    }
    public double getDisk() {
        return disk;
    }
    public int getContraint(){
        return contraint;
    }
    public void setId(double jobId) {
        this.jobId = jobId;
    }
    public void setMips(double mips) {
        this.mips = mips;
    }
    public void setRam(double ram) {
        this.ram = ram;
    }
    public void setDisk(double disk) {
        this.disk = disk;
    }
    public void setContraint(int contraint) {
        this.contraint = contraint;
    }
}
