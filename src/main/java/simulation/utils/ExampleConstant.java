package simulation.utils;

import simulation.powerModel.PowerModel;
import simulation.powerModel.PowerModelSpecPowerHpProLiantMl;

/**
 * @program: ExampleConstant
 * @description: Example中的常量
 */
public class ExampleConstant {
    /**
     * 数据中心的各属性的成本
     */
    public static final double DATACENTER_COST_RAM = 0.05;
    public static final double DATACENTER_COST_BW = 0.1;

    /**
     * 任务长度
     */
    public final static int[] CLOUDLET_LENGTH = {2200, 1000, 2000,800, 1500};
    public final static int[] CLOUDLET_FILESIZE	= {500,300,200,150,100};

    /**
     * 虚拟机之间的网络联系
     */
    public final static int NETWORK_MAXNUMBER = 3;
    public final static int[] NETWORK_NUMBER = {0,1,2,3};
    public final static int NETWORK_MAXDISTANCENUMBER = 4;
    public final static int[] NETWORK_DISTANCE = {1,3,2,4,5};

    /*
     * VM instance types:
     *   High-Memory Extra Large Instance: 3.25 EC2 Compute Units, 8.55 GB // too much MIPS
     *   High-CPU Medium Instance: 2.5 EC2 Compute Units, 0.85 GB
     *   Extra Large Instance: 2 EC2 Compute Units, 3.75 GB
     *   Small Instance: 1 EC2 Compute Unit, 1.7 GB
     *   Micro Instance: 0.5 EC2 Compute Unit, 0.633 GB
     *   We decrease the memory size two times to enable oversubscription
     *
     */
    public final static int VM_TYPES= 6;
    public final static double[] VM_MIPS	= {312,512,800,920,1000,1500};//{ 312,512,800,920,1000,1500}
    public final static double[] VM_RAM	= { 1536,684,750,550,1630,1700};//{ 1536,684,750,550,1630,1700}
    public final static double[] VM_BW	= { 1000, 1500, 2000, 2500, 3000, 3500}; // { 1000,  1500, 2000, 2500, 3000, 3500, 4000}

    /*
     * Host types:
     *   HP ProLiant ML110 G4 (1 x [Xeon 3040 1860 MHz, 2 cores], 4GB)
     *   HP ProLiant ML110 G5 (1 x [Xeon 3075 2660 MHz, 2 cores], 4GB)
     *   We increase the memory size to enable over-subscription (x4)
     */
    public final static int HOST_TYPES	 = 2;
    public final static int[] HOST_MIPS	 = { 1860 * 2 , 1860 * 2 };//2933 * 2
    public final static int[] HOST_RAM	 = { 4096 * 2 , 4096 * 2 };//4096
    public final static int[] HOST_BW = {20000 ,20000}; // 100GB

    public final static PowerModel[] HOST_POWER = {new PowerModelSpecPowerHpProLiantMl(),};

    /**
     * 主机、虚拟机、任务数量
     */
    public final static int HOSTS_NUMBER = 100;
    public final static int VMS_NUMBER = 200;
    public final static int CLOUDLET_NUMBER = 2000;

}
