package simulation.vmShedulePolicy;

import simulation.core.Host;
import simulation.core.Vm;
import simulation.example.SRVMP;
import simulation.utils.ExampleConstant;
import simulation.utils.ExampleUtils;
import simulation.utils.NetworkCalculate;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * @program: vmimgration
 * @description: 基于服务相关性的虚拟机迁移_2019
 * @author: 杨翎
 * @createDate: 2020-02-03 13:40
 */
public class VmSheduleHostSRVMP extends VmSheduleHost{
    @Override
    public double[] getVmMigrationHost(List<Host> hostList, int current,
                                       double migEnergy, int migNumber,
                                       Double mipsRequest, Double mipsAllcation,
                                       List<Vm> vmList) throws FileNotFoundException {
        // 初始化变量
        // 负载阈值0.85
        double up = 0.85;
        double bound = 0.15;
        List<Host> highHost = new ArrayList<Host>();
        List<Host> normalHost = new ArrayList<Host>();
        // 迁移信息
        double[] migMessage = new double[2];

        // 区别主机负载的类型
        for(Host host : hostList) {
            if(host.getCpuUtilization() > up ||
                    host.getRamUtilization() > up || host.getBwUtilization() > up) highHost.add(host);
            else normalHost.add(host);
        }

        // 遍历高负载主机
        for (Host host : highHost) {
            // 触发迁移次数+1
            migNumber++;
            Vm selectVm = null;
            // 所有的虚拟机作为候选虚拟机的集合
            List<Vm> selectVmList = new ArrayList<Vm>();
            selectVmList.addAll(host.getVmList());
            double selectVmMips = 0;
            // 最终迁移的组是migVm
            List<Vm> migVm = new ArrayList<Vm>();
            // 找到最大Mips的虚拟机
            double maxMips = Double.MIN_VALUE;
            for(Vm vm : selectVmList){
                if(vm.getMips() > maxMips){
                    selectVm = vm;
                    maxMips = vm.getMips();
                }
            }
            selectVmList.remove(selectVm);
            migVm.add(selectVm);
            selectVmMips = selectVm.getMips();
            if((host.getMips() - host.getAvailablemips() - selectVmMips)/host.getMips() > up||
                    (host.getMips() - host.getAvailablemips() - selectVmMips)/host.getMips() < up-bound){
                // 选择网络相关性最大的虚拟机
                double maxnet = 0;
                for(Vm vm : selectVmList) {
                    if(vm.getNet() >= maxnet) {
                        selectVm = vm;
                        maxnet = vm.getNet();
                    }
                }
            }
            selectVmMips += selectVm.getMips();
            if((host.getMips() - host.getAvailablemips() - selectVmMips)/host.getMips() > up||
                    (host.getMips() - host.getAvailablemips() - selectVmMips)/host.getMips() < up-bound){
                migVm.add(selectVm);
            }
            System.out.println("主机" + host.getId() + "上的虚拟机迁移组需要进行迁移！");

            // 迁移虚拟机组已经构成，是migVm，现在找目标主机
            for(Vm vm:migVm){
                // 迁移组请求的mips
                SRVMP.mipsRequest += vm.getMips();

            }
            Host selectHost;
            selectHost = selectHostByVm(migVm,normalHost);
            if(selectHost != null) {
                double[] netValue = NetworkCalculate.netValueBefore(null,migVm);
                for (Vm vm : migVm) {
                    SRVMP.mipsAllcation += vm.getMips();
                    ExampleUtils.finishVmInHost(vm,host);
                    ExampleUtils.updateVmInHost(selectHost,vm);
                    host.getVmList().remove(vm);
                    vm.setHost(selectHost);
                    selectHost.getVmList().add(vm);
                }
                migEnergy += (netValue[0]+netValue[1]) * ExampleConstant.DATACENTER_COST_BW;
                System.out.println("虚拟机迁移到目标主机" + selectHost.getId() + "上");
            }else System.out.println("虚拟机目前没有可以迁移的主机");
        }
        migMessage[0] = migEnergy;
        migMessage[1] = migNumber;
        return migMessage;
    }

    /**
     * 选择目标主机方法
     */
    public static Host selectHostByVm(
            List<Vm> vm,
            List<Host> normalHost) {
        Host selectHost = null;
        //double[] netBefore = NetworkCalculate.netValueBefore(null,vm);
        double mips=0;
        double ram=0;
        double bw=0;
        for(Vm vm1 : vm){
            mips += vm1.getMips();
            ram += vm1.getRam();
            bw += vm1.getBw();
        }
        Vm vm1 = new Vm(-1,mips,ram,bw);

        Double max = Double.MIN_VALUE;
        for(Host host1 : normalHost) {
            boolean is = ExampleUtils.vmIsAvailableHost(vm1, host1);
            if (is == true) {
                double netAftersum = 0;
                for(Vm vm2 : vm){
                    double[] netAfter = NetworkCalculate.netValueAfter(null,host1,vm);
                    netAftersum += netAfter[0]+netAfter[1];
                }
                if (netAftersum >= max ) {
                    selectHost = host1;
                    max = netAftersum;
                }
            }
        }

        return selectHost;
    }
}

