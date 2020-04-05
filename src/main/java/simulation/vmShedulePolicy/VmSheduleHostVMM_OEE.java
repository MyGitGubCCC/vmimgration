package simulation.vmShedulePolicy;

import simulation.core.Host;
import simulation.core.Vm;
import simulation.utils.ExampleConstant;
import simulation.utils.ExampleUtils;
import simulation.utils.NetworkCalculate;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * @program: vmimgration
 * @description: 面向数据中心能效优化的虚拟机迁移调度方法_2016
 * @author: 杨翎
 * @createDate: 2020-02-03 13:08
 */
public class VmSheduleHostVMM_OEE extends VmSheduleHost{

    /**
     * 设置优先级，和迁移的触发条件，来决定是否迁移。触发迁移的条件有3个
     * 虚拟机的选择策略是设置虚拟机选择的优先级，优先级最高的进行迁移
     * @param hostList
     * @param current
     * @throws FileNotFoundException
     */
    @Override
    public double[] getVmMigrationHost(List<Host> hostList, int current,
                                       double migEnergy, int migNumber,
                                       Double mipsRequest, Double mipsAllcation,List<Vm> vmList) throws FileNotFoundException {
        // 初始化变量
        double rcmax = 0.98;
        double rmmax = 0.85;
        double rcmin = 0.02;
        double rmmin = 0.4;
        double coffset = 0.01;
        double moffset = 0.1;
        double up = 0.7;
        double maxOffset = 0.03;
        double minOffset = 0.02;
        // 关于迁移的信息
        double[] migMessage = new double[2];

        // 遍历高负载主机列表
        for(Host host : hostList) {
            // selectVm是需要迁移的虚拟机
            Vm selectVm = null;
            // 触发条件1
            if(((host.getMips() - host.getAvailablemips()) >=
                    (host.getMips() * rcmax + 2 * host.getMips() * coffset)
                    && (host.surplusTime() - current > 2 * 6)) ||
                    ((host.getRam() - host.getAvailableram()) >=
                    (host.getRam() * rmmax + 2 * host.getRam() * coffset)
                            && (host.surplusTime() - current > 2 * 6))) {
                // 触发迁移次数+1
                migNumber ++;
                selectVm = imgration(host);
                System.out.println("主机" + host.getId() +
                        "上的虚拟机" + selectVm.getId() + "需要迁移！");
            }
            // 触发条件2
            else if(((host.getMips() - host.getAvailablemips()) >=
                    (host.getMips() * rcmax + host.getMips() * coffset)
                    && (host.surplusTime() - current > 2 * 6)) ||
                    ((host.getRam() - host.getAvailableram()) <=
                    (host.getRam() * rmmax - host.getRam() * coffset)
                            && (host.surplusTime() - current > 2 * 6))) {
                // 触发迁移次数+1
                migNumber ++;
                selectVm = imgration(host);
                System.out.println("主机" + host.getId() +
                        "上的虚拟机" + selectVm.getId() + "需要迁移！");
            }
            // 触发条件3
            else if(((host.getMips() - host.getAvailablemips()) >=
                    (host.getMips() * rcmax) && (host.surplusTime() - current > 5 *6))
                    || ((host.getRam() - host.getAvailableram()) <=
                    (host.getRam() * rmmax) && (host.surplusTime() - current > 5 * 6))) {
                // 触发迁移次数+1
                migNumber ++;
                selectVm = imgration(host);
                System.out.println("主机" + host.getId() + "上的虚拟机" + selectVm.getId() + "需要迁移！");
            }
            if(selectVm != null) {
                // 请求虚拟机mips
                //VMM_OEE.mipsRequest += selectVm.getMips();
                //计算迁移代价
                double[] netValue = NetworkCalculate.netValueBefore(null,host.getVmList());
                MinMIgration(selectVm, hostList, host);
                /*double[] netNew = NetworkCalculate.netValueAfter(selectVm,host,null);
                double netValueOut = netValue[0] + netNew[1];
                double netValueIn = netNew[0];
                double migrationValue = netNew[0] - netValueIn;*/
                migEnergy += (netValue[0]+netValue[1]) * ExampleConstant.DATACENTER_COST_BW;
                //System.out.println(migEnergy);
            }
        }
        migMessage[0] = migEnergy;
        migMessage[1] = migNumber;
        return migMessage;
    }

    /**
     * 选择虚拟机算法，设置优先级
     * @param host
     * @return
     */
    public static Vm imgration(Host host) {
        Vm selectVm = null;
        List<Vm> vmselectList = new ArrayList<Vm>();
        double up = 0.7;
        for(Vm vm : host.getVmList()) {
            if ((host.getMips() - host.getAvailablemips() - vm.getMips()) / host.getMips() < up) {
                vmselectList.add(vm);
            }
        }
        int maxtime = Integer.MIN_VALUE;
        for(Vm vm : vmselectList) {
            if(vm.getStartTime() > maxtime) {
                selectVm = vm;
                maxtime = vm.getStartTime();
            }
        }
        if(selectVm == null) {
            // 选择第一个满足的虚拟机,一般没有这种情况
            selectVm = host.getVmList().get(0);
        }
        return selectVm;
    }

    /**
     * 目标主机放置策略
     * @param selectVm
     * @param normalHost
     * @param host
     */
    public static void MinMIgration(Vm selectVm,
                                    List<Host> normalHost,
                                    Host host) {
        // 选择Sci最小的主机
        Host selectHost = null;
        List<Host> selectHostList = new ArrayList<Host>();
        for(Host host1 : normalHost) {
            boolean is = ExampleUtils.vmIsAvailableHost(selectVm, host1);
            if(is == true) {
                selectHostList.add(host1);
            }
        }
        double minC = Double.MAX_VALUE;
        for(Host host1 : selectHostList) {
            double c = host1.getCpuUtilization() - (host1.getAvailablemips() + selectVm.getMips() + 3*0.03);
            if(c < minC) {
                minC = c;
                selectHost = host1;
            }
        }
        if(selectHost != null) {
            //VMM_OEE.mipsAllcation += selectVm.getMips();
            ExampleUtils.updateVmInHost(selectHost,selectVm);
            ExampleUtils.finishVmInHost(selectVm,host);
            selectVm.setHost(selectHost);
            host.getVmList().remove(selectVm);
            selectHost.getVmList().add(selectVm);
            System.out.println("虚拟机迁移到主机" + selectHost.getId() + "！");
        }else System.out.println("虚拟机目前没有合适的主机放置");
    }
}
