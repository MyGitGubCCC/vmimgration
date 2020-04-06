package simulation.core;

import simulation.powerModel.PowerModel;
import sun.misc.VM;

import java.util.ArrayList;
import java.util.List;

public class Host {
    //主机id
    private int id;
    //主机的mips
    private double mips;
    //主机剩余mips
    private double availablemips;
    //主机内存
    private double ram;
    //主机剩余内存
    private double availableram;
    //主机磁盘
    private double bw;
    //主机剩余磁盘
    private double availablebw;
    //主机能耗模型
    private PowerModel powerModel;
    //主机上虚拟机列表
    private List<Vm> vmList;
    private boolean ifMig;
    private double net;

    /**
     * 有能耗模型的构造器
     * @param id
     * @param mips
     * @param ram
     * @param bw
     * @param powerModel
     */
    public Host(int id, double mips, double ram, double bw, PowerModel powerModel) {
        this.id = id;
        this.mips = mips;
        this.availablemips = mips;
        this.ram = ram;
        this.availableram = ram;
        this.bw = bw;
        this.availablebw = bw;
        this.powerModel = powerModel;
        vmList = new ArrayList<Vm>();
    }

    /**
     * 没有功耗模型的构造器
     * @param id
     * @param mips
     * @param ram
     * @param bw
     */
    public Host(int id, double mips, double ram, double bw) {
        this.id = id;
        this.mips = mips;
        this.availablemips = mips;
        this.ram = ram;
        this.availableram = ram;
        this.bw = bw;
        this.availablebw = bw;
        vmList = new ArrayList<Vm>();
    }

    public int surplusTime() {
        int surTime = 0;
        for(Vm vm : vmList) {
            if(vm.getFinishTime() > surTime) {
                surTime = vm.getFinishTime();
            }
        }
        return surTime;
    }


    /**
     * 计算主机的CPU利用率
     * @return
     */
    public double getCpuUtilization(){
        double usedMips = 0;
        for (Vm vm : vmList) {
            usedMips += vm.getMips();
        }
        double cpuUtilizaiton = usedMips / getMips();
        if (cpuUtilizaiton >= 1) cpuUtilizaiton = 1;
        if (cpuUtilizaiton <= 0) cpuUtilizaiton = 0;
        return cpuUtilizaiton;

    }

    /**
     * 计算ram利用率
     * @return
     */
    public double getRamUtilization(){
        double usedRam = 0;
        for (Vm vm : vmList) {
            usedRam += vm.getRam() - vm.getAvailableRam();
        }
        double ramUtilizaiton = usedRam / getRam();
        return ramUtilizaiton;
    }
    /**
     * 计算disk利用率
     * @return
     */
    public double getBwUtilization(){
        double usedDisk = 0;
        for (Vm vm : vmList) {
            usedDisk += vm.getBw() - vm.getAvailableBw();
        }
        double diskUtilizaiton = usedDisk / getBw();
        return diskUtilizaiton;
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

    public double getAvailablemips() {
        return availablemips;
    }

    public void setAvailablemips(double availablemips) {
        this.availablemips = availablemips;
    }

    public double getRam() {
        return ram;
    }

    public void setRam(double ram) {
        this.ram = ram;
    }

    public double getAvailableram() {
        return availableram;
    }

    public void setAvailableram(double availableram) {
        this.availableram = availableram;
    }

    public double getBw() {
        return bw;
    }

    public void setBw(double bw) {
        this.bw = bw;
    }

    public double getAvailablebw() {
        return availablebw;
    }

    public void setAvailablebw(double availablebw) {
        this.availablebw = availablebw;
    }

    public PowerModel getPowerModel() {
        return powerModel;
    }

    public void setPowerModel(PowerModel powerModel) {
        this.powerModel = powerModel;
    }

    public List<Vm> getVmList() {
        return vmList;
    }

    public void setVmList(List<Vm> vmList) {
        this.vmList = vmList;
    }

    public boolean isIfMig() {
        return ifMig;
    }

    public void setIfMig(boolean ifMig) {
        this.ifMig = ifMig;
    }

    public double getNet() {
        return net;
    }

    public void setNet(double net) {
        this.net = net;
    }
}
