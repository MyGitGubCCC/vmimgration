package simulation.vmShedulePolicy;

import simulation.core.Host;
import simulation.core.Vm;
import simulation.example.THREE_WAY;
import simulation.utils.ExampleConstant;
import simulation.utils.ExampleUtils;
import simulation.utils.NetworkCalculate;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: VmSheduleHostTHREE_WAY2
 * @description: 主机负载三分的迁移算法
 *
 */
public class VmSheduleHostTHREE_WAY_1 extends VmSheduleHost {

    @Override
    public double[] getVmMigrationHost(List<Host> hostList, int current,
                                       double migEnergy, int migNumber,
                                       Double m1, Double m2,List<Vm> vmList) {
        // 1、初始化变量
        List<Host> delayMigrationHost = new ArrayList<Host>();
        List<Host> highHost = new ArrayList<Host>();
        List<Host> normalHost = new ArrayList<Host>();
        List<Host> lowHost = new ArrayList<Host>();
        List<Host> nullHost = new ArrayList<Host>();
        int tmin = 1;
        double up = 0.7;
        double low = 0.3;
        // 迁移能耗
        double[] migMessage = new double[2];
        // 网络相关性平衡因子
        double balance = 0.1;

        // 2、先区分主机的负载，高负载、正常负载、低负载
        for(Host host : hostList) {
            if(host.getCpuUtilization() > up) highHost.add(host);
            else if(host.getCpuUtilization() <= up && host.getCpuUtilization() >= low) normalHost.add(host);
            else if(host.getCpuUtilization() < low && host.getCpuUtilization() > 0) lowHost.add(host);
            else nullHost.add(host);
        }

        // 3、从高负载主机开始遍历虚拟机列表
        // 路径：a1-...
        for(Host host : highHost) {
            // 需要构建虚拟机迁移组的列表
            List<Vm> needGroupList = new ArrayList<Vm>();
            // 不需要构建虚拟机迁移组的列表
            List<Vm> notNeedGroupList = new ArrayList<Vm>();
            // 筛选出主机的虚拟机列表中的所有需要构建迁移组的虚拟机
            // 如果需要构建，返回vm.isNeedGroup() = true
            IsgroupMigrationVm(host, up);

            // 遍历主机的虚拟机列表
            // 判断迁移时机，以及选择迁移的虚拟机
            for (Vm vm : host.getVmList()) {
                //不需要构建迁移组列表
                if (vm.isNeedGroup() == false) notNeedGroupList.add(vm);
                if (vm.isNeedGroup() == true) needGroupList.add(vm);
            }

            // 4、优先选择不需要构建虚拟机迁移组的虚拟机
            if (notNeedGroupList.size() != 0) {
                for (Vm vm : notNeedGroupList) {
                    // 剩余执行时间大于tmin，说明需要迁移,d1
                    if (vm.getFinishTime() - current > tmin) {
                        // 需要迁移，选择迁移的虚拟机和目标主机
                        // 计算单个虚拟机网络相关度netValue[0]是主机内网络，[1]是主机之间网络
                        double[] netValue = NetworkCalculate.netValueBefore(vm,null);
                        // 判断高负载主机不同相关性的不同决策
                        // 路线：a1-b2-d1-(c1->D2)/(c2->D1)/(c3->D1)
                        double[] netmessage = decisionUpVmMigration(normalHost,
                                netValue, vm, null, current,
                                balance,host,lowHost,nullHost, vm.getFinishTime());

                        // 路线：a1-b2-d1-c1->D2
                        if(netmessage[0] == 0 && netmessage[1] == 0){
                            delayMigrationHost.add(host);
                            System.out.println("主机" + vm.getHost().getId() +
                                    "上的虚拟机" + vm.getId() + "考虑延迟迁移");
                            continue;
                        }else {
                            // migMessage[0]是能耗，migMessage[1]是次数
                            migMessage[0] += netmessage[0];
                            migMessage[1] += netmessage[1];
                            // 表示迁移成功
                            //highHost.remove(host);
                            break;
                        }
                    }//执行时间小于tmin，d2
                    else {
                        // 该虚拟机不迁移D3
                        // 路线：a1-b2-d2->D3
                        continue;
                    }
                }
            }
            // 说明此时主机还是高负载
            // notNeedGroupList中没有虚拟机迁移，需要进行迁移组迁移
            if ((host.getMips() - host.getAvailablemips())/host.getMips() > up ) {
                // 其次需要构建迁移虚拟机
                // 路线：a1-b1-...
                System.out.println("主机" + host.getId() + "上的虚拟机需要创建迁移组");
                // 是否构建迁移组成功
                List<Vm> groupVm = groupMigrationVm(host, up,needGroupList);
                // 如果groupVm大于1个虚拟机，表示迁移组构建成功
                if(groupVm.size() > 1) {
                    System.out.println("主机" + host.getId() + "上的虚拟机构建迁移组成功");
                    //计算迁移组的剩余执行时间
                    int maxtime = Integer.MIN_VALUE;
                    for(Vm vm : groupVm) {
                        if (vm.getFinishTime() > maxtime) {
                            maxtime = vm.getFinishTime();
                        }
                    }
                    // 比较剩余执行时间与tmin
                    if(maxtime - current < tmin) {
                        // 路线：a1-b1-Y-d2-D3
                        System.out.println("虚拟机迁移组不迁移的决策");
                        continue;
                    }
                    // 计算迁移组的网络相关度
                    // 路线：a1-b1-Y-d1-(c1->D2)/(c2->D1)/(c3->D1)
                    double[] netgroup = NetworkCalculate.netValueBefore(null, groupVm);
                    double[] netmessage = decisionUpVmMigration(normalHost,
                            netgroup, null, groupVm, current,
                            balance,host,lowHost,nullHost,maxtime);
                    if (netmessage[0] == 0 && netmessage[1] == 0){
                        // 延迟迁移
                        // 路线：a1-b1-Y-d1-c1->D2
                        //migMessage[1] += 1;
                        System.out.println("高负载主机" + host.getId() + "上的虚拟机迁移组需要延迟迁移");
                        delayMigrationHost.remove(host);
                        continue;
                    }else {
                        migMessage[0] += netmessage[0];
                        migMessage[1] += netmessage[1];
                    }
                }
                //构建不成功
                else {
                    // 路线：a1-b1-N
                    System.out.println("高负载主机" + host.getId() + "上的虚拟机迁移组构建失败");
                    //不迁移
                    continue;
                }
            }
        }

        // 5、遍历低负载主机
        // 路径：a3-...
        for (Host host : lowHost) {
            double[] netValue = new double[2];
            //把所有低负载主机的虚拟机作为一个整体
            int vm1Finish = 0;
            //计算最长执行时间
            for(Vm vm : host.getVmList()) {
                if(vm.getFinishTime() > vm1Finish) {
                    vm1Finish = vm.getFinishTime();
                }
            }
            // 执行时间大于tmin ，d1
            // 路径：a3-d1-(c1->D2)/(c2->D2)/(c3->D1)
            if(vm1Finish - current > tmin) {
                for(Vm vm : host.getVmList()){
                    //计算虚拟机组的网络相关度，迁移组是整合主机的所有虚拟机
                    double[] net = NetworkCalculate.netValueBefore(vm,null);
                    netValue[0] += net[0];
                    netValue[1] += net[1];
                }
                //迁移信息netmessage[0]是能耗，[1]是次数
                double[] netmessage = new double[2];
                netmessage[0] = 0;
                netmessage[1] = 0;
                //判断低负载主机不同相关性的不同决策
                // 路径：a3-d1-c3->D1
                if (netValue[0] == 0 && netValue[1] == 0 ) {
                    //D1立即迁移
                    migMessage[1] += 1;
                    for(Vm vm1 : host.getVmList()) {
                        THREE_WAY.mipsRequest += vm1.getMips();
                        Host goalHost = null;
                        goalHost = immMigrationVmFormHost(
                                vm1, normalHost, netValue, lowHost,nullHost);
                        if(goalHost!=null){
                            //更新资源
                            netmessage[0] = updateVmAndHost(vm1,host,goalHost,netValue ,netmessage)[0];

                        }
                    }
                    System.out.println("低负载主机" + host.getId() + "上的虚拟机迁移组立即迁移");
                    continue;
                }
                else {
                    // 延迟迁移 D2
                    // 路径：a3-d1-(c1->D2)/(c2->D2)
                    delayMigrationHost.add(host);
                    //migMessage[1] += 1;
                    System.out.println("低负载主机" + host.getId() + "上的虚拟机迁移组需要延迟迁移");
                }
            }
            else {
                // 路径：a3-d2->D3
                // 不迁移
            }
        }

        // 6、遍历正常负载主机
        // 路径：a2-...
        for(Host host : normalHost) {
            // 可选的虚拟机列表
            List<Vm> selectVmList = new ArrayList<Vm>();
            // 计算主机的资源均衡度
            double averageHost = (host.getAvailablemips() + host.getAvailableram() + host.getAvailablebw())/3;
            double varHost = (Math.pow((host.getMips() - averageHost),2)
                    + Math.pow((host.getRam() - averageHost),2)
                    + Math.pow((host.getBw() - averageHost),2))/3;
            for(Vm vm : host.getVmList()) {
                double averageHostAfter = (host.getMips() - vm.getMips() + host.getRam()
                        - vm.getRam() + host.getBw() - vm.getBw())/3;
                double varHostAfter = (Math.pow((host.getMips() - vm.getMips() - averageHostAfter),2)
                        + Math.pow((host.getRam() - vm.getRam() - averageHostAfter),2)
                        + Math.pow((host.getBw() - vm.getBw() - averageHostAfter),2))/3;
                // 不利于资源均衡e2
                // 路径：a2-e2
                if(varHostAfter < varHost) {
                    // 路径：a2-e2-d1
                    if(vm.getFinishTime() - current > tmin){
                        //double cha = varHost - varHostAfter;
                        selectVmList.add(vm);
                    }
                    // 路径：a2-e2-d2->D3
                    else break;
                }
                //有利于资源均衡e1
                else {
                    // 不迁移
                    // 路径：a2-e1->D3
                    break;
                }
            }

            // 路径：a2-e2-d1-(c1->D2)/(c2->D2)/(c3->D1)
            if(selectVmList.size() != 0) {
                double[] netmessage = new double[2];
                netmessage[0] = 0;
                netmessage[1] = 0;
                //比较网络相关度
                double[] netgroup = NetworkCalculate.netValueBefore(null, selectVmList);
                if(netgroup[0] == 0 && netgroup[1] == 0){
                    migMessage[1] += 1;
                    for (Vm vm : selectVmList){
                        // 路径：a2-e2-d1-c3->D1
                        THREE_WAY.mipsRequest += vm.getMips();
                        Host goalHost = immMigrationVmFormHost(
                                vm, normalHost,netgroup,lowHost,nullHost);
                        if(goalHost != null){
                            //返回能耗
                            netmessage[0] = updateVmAndHost(
                                    vm,host,goalHost,netgroup,
                                    netmessage)[0];
                            migMessage[0] += netmessage[0];
                        }
                    }
                    System.out.println("主机" + host.getId() + "上的虚拟机迁移组立即迁移");
                    //migEnergy += (netgroup[0]+netgroup[1]) * ExampleConstant.DATACENTER_COST_BW;
                    break;
                }
                else {
                    //延迟迁移
                    //migMessage[1] += 1;
                    //System.out.println("主机" + host.getId() + "上的虚拟机延迟迁移");
                }
            }
        }

        // 返回迁移的信息
        /*migMessage[0] = migEnergy;
        migMessage[1] = migNumber;*/
        return migMessage;
    }

    /**
     * 立即迁移虚拟机方法,目标主机的选择
     * @param selectVm
     * @param normalHost
     * @param netValue
     */
    public static Host immMigrationVmFormHost(
            Vm selectVm,
            List<Host> normalHost,
            double[] netValue,
            List<Host> lowHost,
            List<Host> nullHost) {
        Host goalHost = null;
        double netValueOut = 0.0;
        double netValueIn = 0.0;
        //目标主机选择
        if(selectVm != null) {
            goalHost = hostSelect(selectVm,netValue,normalHost);
            if(goalHost == null) {
                goalHost = hostSelect(selectVm,netValue,lowHost);
            }
            if(goalHost == null && nullHost.size() > 0) {
                goalHost = nullHost.get(0);
                nullHost.remove(goalHost);
                normalHost.add(goalHost);
            }
        }
        return goalHost;
    }

    /**
     * 主机选择
     * @param selectVm
     * @param netValue
     * @param hostList
     * @return
     */
    public static Host hostSelect(Vm selectVm, double[] netValue,
                                  List<Host> hostList) {
        Host goalHost = null;
        for(Host host : hostList) {
            boolean put = ExampleUtils.vmIsAvailableHost(selectVm, host);
            if(put == true) {
                if((host.getMips() - host.getAvailablemips() + selectVm.getMips())/host.getMips() > 0.7){
                    continue;
                }
                goalHost = host;
            }
        }
        return goalHost;
    }

    /**
     * 筛选出主机的虚拟机列表中的所有需要构建迁移组的虚拟机
     * @param host
     * @param up
     * @return
     */
    public void IsgroupMigrationVm(Host host, double up){
        //遍历主机的虚拟机列表，如果移除该虚拟机不能使主机降为正常负载，则需要构建虚拟机迁移组
        for(Vm vm1 : host.getVmList()) {
            if((host.getMips() - host.getAvailablemips()
                    - vm1.getMips())/host.getMips() > up) {
                //该虚拟机需要构建迁移组，返回true
                vm1.setNeedGroup(true);
            }else
                //该虚拟机需要构建迁移组，返回true
                vm1.setNeedGroup(false);
        }
    }

    /**
     * 构建迁移组是否成功
     * @return
     */
    public List<Vm> groupMigrationVm(Host host, double up, List<Vm> needGroupList) {
        List<Vm> miggroup = new ArrayList<Vm>();
        if (needGroupList.size() > 1) {
            double summips = 0;
            double maxmips = Double.MIN_VALUE;
            Vm selectVm = null;
            for (Vm vm1 : needGroupList) {
                if (vm1.getMips() > maxmips) selectVm = vm1;
            }
            if(selectVm !=null){
                miggroup.add(selectVm);
                summips += selectVm.getMips();
                needGroupList.remove(selectVm);
            }

            //选择相关性最小的虚拟机
            while ((host.getMips() - host.getAvailablemips()
                    - summips) / host.getMips() > up) {
                double minmips = Double.MAX_VALUE;
                selectVm = null;
                for (Vm vm2 : needGroupList) {
                    double[] net = NetworkCalculate.netValueBefore(vm2,null);
                    if(net[0] + net[1] < minmips) {
                        selectVm = vm2;
                        minmips = vm2.getMips();
                    }
                }
                if(selectVm!=null){
                    summips += selectVm.getMips();
                    miggroup.add(selectVm);
                    needGroupList.remove(selectVm);
                }
            }
        }

            /*// 构建迁移组，使用返减的方法，首先全部相加，然后减去相关性最大的，直到不满足条件为止
            double summipsFromGroup = 0.0;
            for(Vm vm1 : needGroupList) {
                summipsFromGroup += vm1.getMips();
            }
            if((host.getMips() - host.getAvailablemips() - summipsFromGroup)/host.getMips() < up) {
                //把组内相关性大的虚拟机减去
                Vm maxVm = subVmFormGroup(needGroupList);
                if(maxVm != null){
                    summipsFromGroup -= maxVm.getMips();
                    needGroupList.remove(maxVm);
                }
            }
        }*/
        return miggroup;
    }

    /**
     * 把组内相关性大的虚拟机减去
     * @param vmgroup 迁移组
     * @return
     */
    public Vm subVmFormGroup(List<Vm> vmgroup){
        double maxNet = Double.MIN_VALUE;
        Vm selectVm = null;
        // 遍历迁移组
        for(Vm vm : vmgroup) {
            //计算网络相关性
            double[] net = NetworkCalculate.netValueBefore(vm,null);
            double netGroup = net[0] + net[1];
            if (netGroup > maxNet) {
                selectVm = vm;
            }
        }
        return selectVm;
    }

    /**
     * 判断网络相关性，并进行不同的迁移决策
     * @param normalHost 正常主机负载
     * @param netValue 虚拟机的网络相关度
     * @param vm 虚拟机
     * @param groupVm 虚拟机迁移组
     * @param current 当前时间
     * @param balance 平衡因子
     */
    public static double[] decisionUpVmMigration(
            List<Host> normalHost,
            double[] netValue,
            Vm vm,
            List<Vm> groupVm,
            int current,
            double balance,
            Host host,
            List<Host> lowHost,
            List<Host> nullHost,
            int maxtime) {
        //netmessage[0]是能耗，[1]是次数
        double[] netmessage = new double[2];
        // 路线：a1-b2-d1-(c1->D2)/(c2->D1)/(c3->D1)
        // 判断网络相关性
        // 不相关c3 低相关c2
        if(vm == null && groupVm == null) {
            netmessage[0] = 0;
            netmessage[1] = 0;
            return netmessage;
        }
        double a  = (netValue[0] + netValue[1]) * balance * ExampleConstant.DATACENTER_COST_BW;
        double b = ((host.getPowerModel().getPower(host.getCpuUtilization())) - 108)
                * (maxtime - current);

        //System.out.println(a);
        if (netValue[0] == 0 && netValue[1] == 0 ||
                ((netValue[0] + netValue[1]) * balance * ExampleConstant.DATACENTER_COST_BW <=
                ((host.getPowerModel().getPower(host.getCpuUtilization())) - 108)
                        * (maxtime - current))) {
            // D1立即迁移
            // 路线：a1-b2-d1-c3->D1
            // 路线：a1-b2-d1-c2->D1
            Host goalHost = null;
            if(vm != null) {
                // 虚拟机请求资源
                THREE_WAY.mipsRequest += vm.getMips();
                // 触发立即迁移次数+1
                netmessage[1] = 1;
                // 迁移单个虚拟机
                goalHost = immMigrationVmFormHost(
                        vm, normalHost, netValue,
                        lowHost,nullHost);
                if(goalHost!=null) {
                    System.out.println("主机" + vm.getHost().getId() +
                            "上的虚拟机立即迁移到主机" + goalHost.getId());
                    //更新资源，返回的是能耗
                    netmessage[0] = updateVmAndHost(vm,host,goalHost,netValue,netmessage)[0];
                    return netmessage;
                }else {
                    System.out.println("虚拟机请求迁移资源失败！");
                    netmessage[0] = 0;
                    netmessage[1] = 0;
                    return netmessage;
                }
            }
            if(groupVm != null) {
                // 触发立即迁移次数+1
                netmessage[1] = 1;
                for(Vm vm1 : groupVm){
                    // 虚拟机请求资源
                    THREE_WAY.mipsRequest += vm1.getMips();
                    goalHost = immMigrationVmFormHost(
                            vm1, normalHost, netValue,lowHost,nullHost);
                    if (goalHost != null){
                        // 返回迁移能耗
                        netmessage[0] = updateVmAndHost(vm1,host,goalHost,netValue,netmessage)[0];
                    }else {
                        System.out.println("！！！！！！！！！！！！！！迁移失败！！！！！！！！！！！！！！！！！");
                        netmessage[0] = 0;
                        netmessage[1] = 0;
                        return netmessage;
                    }
                }
                System.out.println("主机上的虚拟机迁移组立即迁移成功！");
                return netmessage;
            }
        }
        //高相关c1
        else {
            //D2延迟迁移
            netmessage[0]  = 0;
            netmessage[1] = 0;
        }
        return netmessage;
    }


    /**
     * 更新资源
     * @param vm 迁移的虚拟机
     * @param host 虚拟机的原主机
     * @param goalHost 迁移的目标主机
     * @param netValue 网络相关度
     * @param netmessage 迁移信息
     * @return
     */
    public static double[] updateVmAndHost(Vm vm, Host host, Host goalHost,
                                       double[] netValue, double[] netmessage) {

        // 分配虚拟机资源
        THREE_WAY.mipsAllcation += vm.getMips();
        //原主机减少资源
        ExampleUtils.finishVmInHost(vm,host);
        //目标主机增加资源
        ExampleUtils.updateVmInHost(goalHost,vm);
        vm.setHost(goalHost);
        host.getVmList().remove(vm);
        goalHost.getVmList().add(vm);
        double migEnergy = (netValue[0]+netValue[1]) * ExampleConstant.DATACENTER_COST_BW;
        netmessage[0] = migEnergy;
        return netmessage;
    }
}