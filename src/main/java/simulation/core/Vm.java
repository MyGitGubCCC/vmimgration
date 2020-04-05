package simulation.core;

import java.util.ArrayList;
import java.util.List;

public class Vm {
    //虚拟机id
    private int id;
    //虚拟机请求主机的mips
    private double mips;
    //虚拟机剩余mips资源
    private double availableMips;
    //虚拟机请求主机的ram
    private double ram;
    //虚拟机剩余的ram资源
    private double availableRam;
    //虚拟机的带宽请求
    private double bw;
    //虚拟机的剩余带宽
    private double availableBw;
    //虚拟机上的任务列表
    private List<Cloudlet> cloudletList;
    //虚拟机上已完成的任务列表
    private List<Cloudlet> completedCloudletList;
    //虚拟机在主机上
    private Host host;
    //虚拟机排除的主机列表
    private List<Host> excludedHostList;
    //虚拟机的开始时间
    private int startTime;
    //虚拟机的结束时间
    private int finishTime;
    //虚拟机相关性的虚拟机列表
    private List<Vm> netRelatedVm;
    //虚拟机是否需要构建迁移组
    private boolean isNeedGroup;
    private int vmLoadByHost;
    // 虚拟机的网络相关
    private double net;

    public Vm(int id, double mips, double ram, double bw) {
        this.id = id;
        this.mips = mips;
        setAvailableMips(mips);
        this.ram = ram;
        setAvailableRam(ram);
        this.bw = bw;
        setAvailableBw(bw);
        cloudletList = new ArrayList<Cloudlet>();
        completedCloudletList = new ArrayList<Cloudlet>();
    }

    public int getVmLoadByHost() {
        return vmLoadByHost;
    }

    public void setVmLoadByHost(int vmLoadByHost) {
        this.vmLoadByHost = vmLoadByHost;
    }

    /**
     * 任务在该虚拟机上预计跑的时间
     * @param cloudlet
     * @return
     */
    public double getTimeForCloudletInVm(Cloudlet cloudlet){
        double time = cloudlet.getLength() / (this.getMips());
        return time;
    }

    /**
     * 计算CPU利用率
     * @return
     */
    public double getCpuUtilization(){
        double cpuUtilization = (getMips() - getAvailableMips()) / getMips();
        return cpuUtilization;
    }

    /**
     * 计算内存利用率
     * @return
     */
    public double getRamUtilization(){
        double ramUtilization = (getRam() - getAvailableRam()) / getRam();
        return ramUtilization;
    }

    /**
     * 计算带宽利用率
     * @return
     */
    public double getBwUtilization(){
        double bwUtilization = (getBw() - getAvailableBw()) / getBw();
        return bwUtilization;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getMips() {
        return mips;
    }

    public void setMips(double mips) {
        this.mips = mips;
    }

    public double getAvailableMips() {
        return availableMips;
    }

    public void setAvailableMips(double availableMips) {
        this.availableMips = availableMips;
    }

    public double getRam() {
        return ram;
    }

    public void setRam(double ram) {
        this.ram = ram;
    }

    public double getAvailableRam() {
        return availableRam;
    }

    public void setAvailableRam(double availableRam) {
        this.availableRam = availableRam;
    }

    public double getBw() {
        return bw;
    }

    public void setBw(double bw) {
        this.bw = bw;
    }

    public double getAvailableBw() {
        return availableBw;
    }

    public void setAvailableBw(double availableBw) {
        this.availableBw = availableBw;
    }

    public List<Cloudlet> getCloudletList() {
        return cloudletList;
    }

    public void setCloudletList(List<Cloudlet> cloudletList) {
        this.cloudletList = cloudletList;
    }

    public List<Cloudlet> getCompletedCloudletList() {
        return completedCloudletList;
    }

    public void setCompletedCloudletList(List<Cloudlet> completedCloudletList) {
        this.completedCloudletList = completedCloudletList;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(int finishTime) {
        this.finishTime = finishTime;
    }

    public List<Vm> getNetRelatedVm() {
        return netRelatedVm;
    }

    public void setNetRelatedVm(List<Vm> netRelatedVm) {
        this.netRelatedVm = netRelatedVm;
    }

    public boolean isNeedGroup() {
        return isNeedGroup;
    }

    public void setNeedGroup(boolean needGroup) {
        isNeedGroup = needGroup;
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public List<Host> getExcludedHostList() {
        return excludedHostList;
    }

    public void setExcludedHostList(List<Host> excludedHostList) {
        this.excludedHostList = excludedHostList;
    }

    public double getNet() {
        return net;
    }

    public void setNet(double net) {
        this.net = net;
    }
}
