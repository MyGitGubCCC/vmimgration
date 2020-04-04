package simulation.vmShedulePolicy;

import simulation.core.Host;
import simulation.core.Vm;
import simulation.example.KAI_MPCM;
import simulation.utils.ExampleConstant;
import simulation.utils.ExampleUtils;
import simulation.utils.NetworkCalculate;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * @program: vmimgration
 * @description: 面向云数据中心的虚拟机部署与迁移优化机制_张磊_2019
 * KAI_MPCM
 * KAI：三阈值阈值决策
 * MPCM：基于CPU利用率与内存大小知己最小的迁移选择算法MPCM
 * @author: 杨翎
 * @createDate: 2020-02-03 17:52
 */
public class VmSheduleHostKAI_MPCM extends VmSheduleHost{
    /**
     * 本文设置一种三负载阈值
     * r = 0.5
     * MAD = 0.6
     * tl = 0.5 * (1 - r* MAD) = 0.35
     * tm = 0.9 * (1 - r* MAD) = 0.63
     * th = 1 - r*MAD = 0.7
     *
     * @param hostList
     * @param current
     * @throws FileNotFoundException
     */
    @Override
    public double[] getVmMigrationHost(List<Host> hostList, int current,
                                       double a, int b,
                                       Double mipsRequest, Double mipsAllcation,
                                       List<Vm> vmList) throws FileNotFoundException {
        // 初始化一些变量
        double tl = 0.35;
        double tm = 0.63;
        double th = 0.7;
        List<Host> highHostList = new ArrayList<Host>();
        List<Host> lowHostList = new ArrayList<Host>();
        List<Host> normalHostList = new ArrayList<Host>();
        List<Host> nullHostList = new ArrayList<Host>();
        // 迁移信息
        double[] migMessage = new double[2];
        double migEnergy = 0.0;
        double migNumber = 0.0;

        // 区分主机负载类型
        for (Host host : hostList) {
            // 主机重载
            if(host.getCpuUtilization() > th) {
                highHostList.add(host);
            }
            // 主机轻载
            else if(host.getCpuUtilization() >= tl && host.getCpuUtilization() < tm) {
                lowHostList.add(host);
            }
            // 正常负载
            else if(host.getCpuUtilization() >= tm && host.getCpuUtilization() <= th) {
                normalHostList.add(host);
            }
            // 空负载
            else {
                nullHostList.add(host);
            }
        }

        // MPCM：基于CPU利用率与内存大小知己最小的迁移选择算法MPCM
        List<Vm> selectVmList = new ArrayList<Vm>();
        for(Host host : highHostList) {
            // 触发迁移，次数+1
            migNumber++;
            Vm selectvm;
            selectvm = selectVm(host);
            if(selectvm != null) {
                System.out.println("主机" + host.getId() +
                        "上的虚拟机" + selectvm.getId() + "需要进行虚拟机迁移！");
                // 虚拟机请求mips
                KAI_MPCM.mipsRequest += selectvm.getMips();
                selectVmList.add(selectvm);
            }
        }
        // 迁移的虚拟机排序，从大到小
        for(int i=0; i< selectVmList.size();i++) {
            for(int j =1; j< selectVmList.size();j++){
                if(selectVmList.get(j).getMips() > selectVmList.get(i).getMips()){
                    int idi = selectVmList.get(i).getId();
                    int idj = selectVmList.get(j).getId();
                    selectVmList.get(i).setId(idj);
                    selectVmList.get(j).setId(idi);
                }
            }
        }

        // 遍历迁移的虚拟机列表
        for(Vm vm : selectVmList) {
            Host selectHost;
            // 目标主机放置算法：选择虚拟机分配后带来功耗最小的主机
            selectHost = goalHost(vm,lowHostList );
            if(selectHost == null) {
                selectHost = goalHost(vm,normalHostList);
            }
            if(selectHost == null) {
                selectHost = goalHost(vm,nullHostList);
            }
            if(selectHost != null) {
                // 分配虚拟机的mips
                KAI_MPCM.mipsAllcation += vm.getMips();
                //计算虚拟机组的网络相关度
                double[] netValue = NetworkCalculate.netValueBefore(vm,null);
                migEnergy += (netValue[0] + netValue[1]) *
                        ExampleConstant.DATACENTER_COST_BW;
                //更新资源
                Host host  = vm.getHost();
                ExampleUtils.finishVmInHost(vm,host);
                ExampleUtils.updateVmInHost(selectHost,vm);
                vm.setHost(selectHost);
                host.getVmList().remove(vm);
                selectHost.getVmList().add(vm);
                System.out.println("虚拟机" + vm.getId() + "迁移到主机"+selectHost.getId());
            }else {
                System.out.println("虚拟机" + vm.getId() + "目前找不到迁移的目标主机");
            }
        }
        migMessage[0] = migEnergy;
        migMessage[1] = migNumber;
        return migMessage;
    }

    /**
     * 选择虚拟机，选择mips * ram最小的虚拟机
     * @param host
     */
    public static Vm selectVm(Host host) {
        double MinValue = Double.MAX_VALUE;
        Vm select = null;
        for(Vm vm : host.getVmList()) {
            double value = vm.getMips() * vm.getRam();
            if(value < MinValue) {
                MinValue = value;
                select = vm;
            }
        }
        return select;
    }

    /**
     * 目标主机的选择
     * @param vm 迁移的虚拟机
     * @return
     */
    public static Host goalHost (Vm vm,List<Host> List){
        Host selectHost = null;
        double minEnger = Double.MAX_VALUE;
        for(Host host1 : List) {
            boolean is = ExampleUtils.vmIsAvailableHost(vm, host1);
            if(is == true) {
                double cpuEnergy = (host1.getPowerModel().getPower(
                        (host1.getMips() - host1.getAvailablemips() + vm.getMips())/host1.getMips() > 1 ?
                                1 : (host1.getMips() - host1.getAvailablemips() + vm.getMips())/host1.getMips()))
                        - (host1.getPowerModel().getPower(host1.getCpuUtilization()));
                if(cpuEnergy < minEnger) {
                    selectHost = host1;
                }
            }
        }
        return selectHost;
    }
}
