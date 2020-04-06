package simulation.utils;

import simulation.core.Cloudlet;
import simulation.core.Host;
import simulation.core.Vm;
import sun.misc.VM;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: migration
 * @description: 辅助工具类
 * @author: 杨翎
 * @createDate: 2020-01-28 11:40
 */
public class ExampleUtils {

    /**
     * 判断是否可以放置算法
     */
    // 判断任务是否可以在虚拟机上执行,查看虚拟机剩余内存是否够用
    public static boolean cloudletIsAvailableVm(Cloudlet cloudlet, Vm vm) {
        if (vm.getAvailableRam() >= cloudlet.getRam()) {
            return true;
        }
        return false;
    }
    // 判断虚拟机是否可以放置在主机上
    public static boolean vmIsAvailableHost(Vm vm, Host host) {
        if (host.getAvailablemips() >= vm.getMips()
                && host.getAvailableram() >= vm.getRam()
                && host.getAvailablebw() >= vm.getBw()) {
            return true;
        }
        return false;
    }


    /**
     * 更新资源
     */
    // 主机放置虚拟机后，更新主机资源
    public static void updateVmInHost(Host host, Vm vm) {
        if(host!=null && vm!=null){
            host.setAvailablemips(host.getAvailablemips() - vm.getMips() > 0 ?
                    (host.getAvailablemips() - vm.getMips()) : 0);
            host.setAvailableram(host.getAvailableram() - vm.getRam() > 0 ?
                    (host.getAvailableram() - vm.getRam()) : 0);
            host.setAvailablebw(host.getAvailablebw() - vm.getBw() > 0 ?
                    (host.getAvailablebw() - vm.getBw()) : 0);
            host.getVmList().add(vm);
            vm.setHost(host);
        }
    }

    //任务放置在虚拟机之后，更新虚拟机资源
    public static boolean updateAfterCloudletInVm(Vm vm, Cloudlet cloudlet, List<Cloudlet> cloudletList, int currentTime, List<Cloudlet> removeCloudletList) {
        if (vm != null) {
            // 虚拟机更新资源
            vm.setAvailableRam(vm.getAvailableRam() - cloudlet.getRam() < 0 ? 0 : (vm.getAvailableRam() - cloudlet.getRam()));
            //vm.setAvailableMips((1-((vm.getRam() - vm.getAvailableRam())/vm.getRam()))*vm.getMips());
            vm.setAvailableMips(vm.getAvailableMips() - vm.getMips()*0.1);
            vm.setAvailableBw(0);
            cloudlet.setVm(vm);
            vm.getCloudletList().add(cloudlet);
            // 放置完更新每个任务预期完成时间，当达到该时间，任务移除，表示已完成
            cloudlet.setExecStartTime(currentTime);
            cloudlet.setExpectFinishTime(currentTime + (int) Math.ceil(cloudlet.getLength() / cloudlet.getVm().getMips()));
            removeCloudletList.add(cloudlet);
            //System.out.println("任务id:" + cloudlet.getId() + "已经放置到虚拟机：" + vm.getId() + "上\t结束时间：" + cloudlet.getExpectFinishTime());
            return true;
        } else {
            //System.out.println("任务" + cloudlet + "没找到vm放置");
            return false;
        }
    }

    //任务在虚拟机中完成，更新虚拟机和cloudlet
    public static void updateCloudletFinishedInVm(
            Vm vm,
            List<Cloudlet> cloudletList,
            List<Cloudlet> finishCloudLetList) {
        vm.getCompletedCloudletList().addAll(finishCloudLetList);
        vm.getCloudletList().removeAll(finishCloudLetList);
        for (Cloudlet cloudlet : finishCloudLetList) {
            if(vm.getCloudletList() == null) {
                vm.setAvailableMips(vm.getMips());
                vm.setAvailableBw(vm.getBw());
            }
            else {
                vm.setAvailableMips(0);
                vm.setAvailableBw(0);
            }
            vm.setAvailableRam(vm.getAvailableRam() + cloudlet.getRam() > vm.getRam() ?
                    vm.getRam() : vm.getAvailableRam() + cloudlet.getRam());
        }
    }
    //虚拟机执行结束，释放主机资源
    public static void finishVmInHost(Vm selectVm, Host oldHost) {
        oldHost.setAvailablemips((oldHost.getAvailablemips() + selectVm.getMips()) > oldHost.getMips()
                ? oldHost.getMips() : (oldHost.getAvailablemips() + selectVm.getMips()));
        oldHost.setAvailableram((oldHost.getAvailableram() + selectVm.getRam()) > oldHost.getRam()
                ? oldHost.getRam() : (oldHost.getAvailableram() + selectVm.getRam()));
        oldHost.setAvailablebw((oldHost.getBw() + selectVm.getBw()) > oldHost.getBw()
                ? oldHost.getBw() : (oldHost.getBw() + selectVm.getBw()));
        oldHost.getVmList().remove(selectVm);
        selectVm.setHost(null);
    }


    /**
     * 查看算法
     */
    // 查看目前正在执行的任务,并且输出vm执行利用率和host功耗
    public static void getInProgressCloudletSize(
            String datacenterName,
            List<Host> hostList,
            List<Vm> vmList) {
        for (Host host : hostList) {
            String cpuEnergyPath = FilePath.DATA_PATH + "\\" + datacenterName + "\\cpuEnergy\\host_" + host.getId();
            String ramEnergyPath = FilePath.DATA_PATH + "\\" + datacenterName + "\\ramEnergy\\host_" + host.getId();
            String bwEnergyPath = FilePath.DATA_PATH + "\\" + datacenterName + "\\bwEnergy\\host_" + host.getId();
            String cpuUtilizationPath = FilePath.DATA_PATH + "\\" + datacenterName + "\\cpuUtilization\\host_" + host.getId();
            //String ramUtilizationPath = FilePath.DATA_PATH + "\\" + datacenterName + "\\ramUtilization\\host_" + host.getId();
            //String bwUtilizationPath = FilePath.DATA_PATH + "\\" + datacenterName + "\\bwUtilization\\host_" + host.getId();


            double cpuEnergy = host.getPowerModel().getPower(host.getCpuUtilization());
            //如果利用率为0，设功耗为0
            if (host.getCpuUtilization() ==0 ){
                cpuEnergy = 0;
            }
            double ramEnergy = host.getRamUtilization() * host.getRam() * ExampleConstant.DATACENTER_COST_RAM;
            double bwEnergy = host.getBwUtilization() * host.getBw() * ExampleConstant.DATACENTER_COST_BW;

            WriteUtile.writeToFile(cpuEnergy + "", cpuEnergyPath);
            WriteUtile.writeToFile(ramEnergy + "", ramEnergyPath);
            WriteUtile.writeToFile(bwEnergy + "", bwEnergyPath);
            WriteUtile.writeToFile(host.getCpuUtilization() + "", cpuUtilizationPath);
            //WriteUtile.writeToFile(ramEnergy + "", ramUtilizationPath);
            //WriteUtile.writeToFile(bwEnergy + "", bwUtilizationPath);
        }
        //输出虚拟机的利用率
        for (Vm vm : vmList) {
            /*if (vm.getCloudletList().size() != 0) {
                inProgressCloudletSize += vm.getCloudletList().size();
            }*/
            //输出每个vm的利用率
            String cpuUtilizationFilePath = FilePath.DATA_PATH + "\\" + datacenterName + "\\VMUtilization\\vm_" + vm.getId();
            //String ramUtilizationFilePath = FilePath.DATA_PATH + "\\" + datacenterName + "\\ramUtilization\\vm_" + vm.getId();
            //String bwUtilizationFilePath = FilePath.DATA_PATH + "\\" + datacenterName + "\\diskUtilization\\vm_" + vm.getId();

            WriteUtile.writeToFile(vm.getCpuUtilization() + "", cpuUtilizationFilePath);
            //WriteUtile.writeToFile(vm.getRamUtilization() + "", ramUtilizationFilePath);
            //WriteUtile.writeToFile(vm.getBwUtilization() + "", bwUtilizationFilePath);

        }

    }
    // 查看有没有任务已经完成，如果有，移除任务
    public static void updateFinishedCloudlet(
            int currentTime,
            List<Host> hostList,
            List<Cloudlet> cloudletList) {
        int finishCloudletNumber = 0;
        for (Host host : hostList) {
            for (Vm vm : host.getVmList()) {
                List<Cloudlet> finishCloudLetList = new ArrayList<Cloudlet>();
                for (Cloudlet cloudlet : vm.getCloudletList()) {
                    // 任务的完成时间小于当前时间，就说明任务已经完成了
                    if (cloudlet.getExpectFinishTime() <= currentTime) {
                        finishCloudLetList.add(cloudlet);
                        finishCloudletNumber++;
                    }
                }
                // 删除虚拟机列表中的任务，添加到执行完的列表中。并且把更新虚拟机的剩余资源
                if (finishCloudLetList.size() > 0) {
                    ExampleUtils.updateCloudletFinishedInVm(vm, cloudletList, finishCloudLetList);
                }
            }
        }
    }

    // 打印信息
    public static void printFinishedCloudlet(List<Cloudlet> cloudletList) {
        System.out.println("\n任务完成情况");
        System.out.println("任務id\t作业id\t放置的虚拟机id\t开始时间\t完成时间\t执行时间");
        for (Cloudlet cloudlet : cloudletList) {
            System.out.println(
                    cloudlet.getId()
                            + "\t\t" + cloudlet.getVm().getId()
                            + "\t\t\t" + cloudlet.getExecStartTime()
                            + "\t\t" + cloudlet.getExpectFinishTime()
                            + "\t\t" + (cloudlet.getExpectFinishTime() - cloudlet.getExecStartTime())
            );
        }
    }



    /**
     *  求最大值方法，找到mips最大的虚拟机，并返回
     */
    public static Vm max(List<Vm> selectVmList, Host host) {
        Vm maxVm = null;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < selectVmList.size(); i++) {
            if (max < host.getVmList().get(i).getMips()) {
                max = host.getVmList().get(i).getMips();
                maxVm = host.getVmList().get(i);
            }
        }
        return maxVm;
    }

}
