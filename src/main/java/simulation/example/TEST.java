package simulation.example;

import simulation.cloudletPlacement.CloudletPlacement;
import simulation.cloudletPlacement.CloudletPlacementFistFit;
import simulation.cloudletSchedulPolicy.CloudletSchedulPolicy;
import simulation.cloudletSchedulPolicy.CloudletSchedulPolicyFCFS;
import simulation.core.Cloudlet;
import simulation.core.Host;
import simulation.core.Vm;
import simulation.utils.*;
import simulation.vmAllocationPolicy.VmAllocationPolicy;
import simulation.vmAllocationPolicy.VmAllocationPolicySimple;
import simulation.vmShedulePolicy.VmSheduleHost;
import simulation.vmShedulePolicy.VmSheduleHostTHREE_WAY2;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;


public class TEST {

    //任务列表
    private static List<Cloudlet> cloudletList;
    //主机列表
    private static List<Host> hostList;
    //虚拟机列表
    private static List<Vm> vmList;
    // 虚拟机请求的mips
    public static Double mipsRequest;
    // 虚拟机实际分配的mips
    public static Double mipsAllcation;


    public static void main(String[] args) throws FileNotFoundException {
        //1、初始化对象
        // 创建cloudletlist、hostlist、vmlist类
        cloudletList = new ArrayList<Cloudlet>();
        hostList = new ArrayList<Host>();
        vmList = new ArrayList<Vm>();
        // 创建HostList、VmList、CloudletList
        CreateDatacenterUtiles.createHostList(hostList);
        CreateDatacenterUtiles.createVmList(vmList);
        CreateDatacenterUtiles.createCloudletList(cloudletList);
        //初始化虚拟机网络相关性
        CreateDatacenterUtiles.createClouletRelated(cloudletList);

        // 定义模拟的开始时钟，调度间隔（1秒）createClouletRelated
        int starSimTime = 0;
        // 数据中心名称为Threeway-VmMigration
        String DatacenterName = "Threeway-Migration";
        // 初始能耗
        double cpuEnergy = 0.0;
        double ramEnergy = 0.0;
        double engerSum = 0.0;
        double sumMig = 0.0;
        int migNumber = 0;
        double balance = 0.0;
        double average = 0.0;
        double[] migMessage = new double[2];
        // 虚拟机请求的mips
        mipsRequest= 0.0;
        // 虚拟机实际分配的mips
        mipsAllcation = 0.0;


        // 初始化虚拟机放置策略,使用firstfit
        VmAllocationPolicy vmAllocationPolicy = new VmAllocationPolicySimple();
        // 初始化虚拟机迁移策略,使用Three-way Migration
        VmSheduleHost vmSheduleHost = new VmSheduleHostTHREE_WAY2();
        // 初始化任务选择策略
        CloudletPlacement cloudletPlacement = new CloudletPlacementFistFit();
        // 任务调度算法，使用FCFS
        CloudletSchedulPolicy cloudletSchedulPolicy = new CloudletSchedulPolicyFCFS();

        // 开始模拟（没有代理和数据中心类，直接操作host、vm、cloudlet）
        System.out.println("模拟" + DatacenterName + "开始!");
        // 虚拟机放置主机，采用fistfit策略
        for (Vm vm : vmList) {
            //使用vmAllocationPolicy策略
            Host host = vmAllocationPolicy.getHost(hostList, vm);
            if(host != null) {
                //更新主机资源及信息
                ExampleUtils.updateVmInHost(host, vm);
                vm.setStartTime(starSimTime);
                vm.setFinishTime(starSimTime);
                mipsRequest += vm.getMips();
                mipsAllcation += vm.getMips();
            }
            else mipsRequest += vm.getMips();
        }

        // 2、开始任务的调度执行过程（重点）
        // 查看任务列表有无任务
        List<Cloudlet> cloudletListQueue = new ArrayList<Cloudlet>();
        cloudletListQueue.addAll(cloudletList);

        //循环开始，任务开始执行
        while (true){
            double migEnergy = 0.0;
            int num = 0;
            // 在这里输出利用率和功耗
            ExampleUtils.getInProgressCloudletSize(DatacenterName, hostList, vmList);
            // 查看有没有任务已经完成，如果有，移除任务
            ExampleUtils.updateFinishedCloudlet(starSimTime, hostList, cloudletList);
            if (cloudletListQueue.size() != 0 ){
                // 每个一个调度间隔检查一次虚拟机列表，看看有没有可以让任务放置的虚拟机
                // removeCloudletList为本次已经被放置任务列表，需要从任务队列中删除
                List<Cloudlet> removeCloudletList = new ArrayList<Cloudlet>();
                // 任务放置
                cloudletSchedulPolicy.cloudletPutInVm(cloudletPlacement,
                        vmList,cloudletListQueue,cloudletList,starSimTime,
                        removeCloudletList);
                // 虚拟机之间由于任务而创建的相关性
                CreateDatacenterUtiles.createVmRelated(vmList);
                // 从队列中移除已经放置的任务
                cloudletListQueue.removeAll(removeCloudletList);
                // 进行虚拟机的迁移（重点），每个比较实验，不同的迁移策略，都是在这个的过程不同
                /*migMessage = vmSheduleHost.getVmMigrationHost(
                        hostList, starSimTime, migEnergy, num,
                        mipsRequest, mipsAllcation,vmList);*/
                //System.out.println(migEnergy);
            }else {
                break;
            }
            // 时间加1
            starSimTime ++;
            // 计算能耗
            //double netMax = Double.MIN_VALUE;
            for(Host host : hostList) {
                double netsum = 0;
                double ed = 0;
                if(host.getCpuUtilization() > 0.7){
                    String loadPath2 = FilePath.DATA_PATH + "\\" + "TEST" + "\\load2";
                    String loadPath = FilePath.DATA_PATH + "\\" + "TEST" + "\\load";
                    String netPath = FilePath.DATA_PATH + "\\" + "TEST" + "\\netEnger";
                    String edPath = FilePath.DATA_PATH + "\\" + "TEST" + "\\ed";
                    WriteUtile.writeToFile(host.getCpuUtilization()*100-70 + "", loadPath);
                    WriteUtile.writeToFile(Math.pow((host.getCpuUtilization()*100-70)/30*10,2) + "", loadPath2);

                    double[] net;
                    for(Vm vm : host.getVmList()){
                        net = NetworkCalculate.netValueBefore(vm,null);
                        netsum += net[0] + net[1];
                    }
                    /*System.out.println("主机网络相关：" + netsum);
                    if(netsum>netMax){
                        netMax = netsum;
                    }*/
                    double netMax = 18500;
                    //System.out.println((1*host.getCpuUtilization()/0.3)/(1*(net1+net2))/3823250);
                    WriteUtile.writeToFile(netsum/netMax*10 + "", netPath);
                    ed = (Math.pow((host.getCpuUtilization()*100-70)/30*10,2))/(netsum/netMax*10);
                    WriteUtile.writeToFile(ed + "", edPath);
                }

            }
            //System.out.println("最大网络相关是"+netMax);
        }
    }

}
