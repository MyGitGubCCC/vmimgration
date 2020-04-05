package simulation.utils;

import simulation.core.Cloudlet;
import simulation.core.Host;
import simulation.core.Vm;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @program: vmimgration
 * @description: 计算网络相关公式
 * @author: 杨翎
 * @createDate: 2020-02-21 17:48
 */
public class NetworkCalculate {
    /**
     * 虚拟机在迁移之前的网络代价,分为主机内网络相关、主机之间网络相关
     * @param vm 单个虚拟机
     * @param vmGroupList 虚拟机迁移组
     * @return
     */
    public static double[] netValueBefore(Vm vm, List<Vm> vmGroupList) {
        double netvalueIn = 0.0;
        double netvalueOut = 0.0;
        if(vm != null){
            Host host = vm.getHost();
            double[] net = caluateNet(vm,host,netvalueIn,netvalueOut);
            netvalueIn = net[0];
            netvalueOut = net[1];
        }
        if(vmGroupList != null){
            for(Vm vm1 : vmGroupList) {
                double[] net = caluateNet(vm1,vm1.getHost(),netvalueIn,netvalueOut);
                netvalueIn += net[0];
                netvalueOut += net[1];
            }
        }
        double[] netvalueSum = {netvalueIn, netvalueOut};
        return netvalueSum;
    }

    /**
     * 虚拟机在迁移之前的网络代价,分为主机内网络相关、主机之间网络相关
     * @param vm 单个虚拟机迁移
     * @param goalhost 目标主机
     * @param vmList 迁移组迁移
     * @return
     */
    public static double[] netValueAfter(Vm vm, Host goalhost, List<Vm> vmList) {
        double netIn = 0.0;
        double netOut = 0.0;
        double net[] = new double[2];
        if(vm != null) {
            net = caluateNet(vm,goalhost,netIn,netOut);
        }else {
            for (Vm vm1 : vmList) {
                net = caluateNet(vm1,goalhost,netIn,netOut);
            }
        }
        double[] netNew = {net[0],net[1]};
        return netNew;
    }

    /**
     * 计算网络
     * @param vm
     * @param host
     * @param netIn
     * @param netOut
     * @return
     */
    public static double[] caluateNet(Vm vm, Host host, double netIn, double netOut) {
        double[] net = new double[2];
        if(vm.getNetRelatedVm() == null) {
            net[0] = 0;
            net[1] = 0;
            return net;
        }else {
            for (int i = 0; i < vm.getNetRelatedVm().size(); i++) {
                if(vm.getNetRelatedVm().get(i) != null) {
                    if (vm.getNetRelatedVm() == vm) continue;
                    // 主机内相关
                    if (vm.getNetRelatedVm().get(i).getHost().getId() == host.getId()) {
                        for (int j = 0; j < vm.getNetRelatedVm().get(i).getCloudletList().size(); j++) {
                            net[0] += netvalue(vm,i, j);
                        }
                    }else {
                        for (int j = 0; j < vm.getNetRelatedVm().get(i).getCloudletList().size(); j++) {
                            net[1] += netvalue(vm,i, j);
                        }
                    }
                }
            }
        }
        return net;
    }

    /**
     * 求的键值对中的网络值
     * @param vm
     * @param i
     * @param j
     * @return
     */
    public static double netvalue( Vm vm, int i, int j) {
        double netvalueIn = 0.0;
        Cloudlet cloudlet = vm.getNetRelatedVm().get(i).getCloudletList().get(j);
        Map<List<Cloudlet>, Integer> cloudlets =  cloudlet.getRelatedCloulet();
        if(cloudlets == null) return netvalueIn = 0;
        Collection<Integer> coll = cloudlets.values();
        Iterator<Integer> it = coll.iterator();
        while (it.hasNext()) {
            netvalueIn += it.next();
        }
        return netvalueIn;
    }

}
