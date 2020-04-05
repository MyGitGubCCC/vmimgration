package simulation.vmShedulePolicy;

import simulation.core.Host;
import simulation.core.Vm;
import simulation.example.THREE_WAY;
import simulation.utils.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * @program: VmSheduleHostTHREE
 * @description: 主机负载三分的迁移算法
 *
 */
public class VmSheduleHostTHREE_WAY extends VmSheduleHost {

    @Override
    public double[] getVmMigrationHost(List<Host> hostList, int current,
                                       double migEnergy, int migNumber,
                                       Double m1, Double m2,List<Vm> vmList) {
        // 迁移
        double[] migMessage = new double[2];
        // migMessage[0]是迁移次数
        // migMessage[1]是迁移能耗

        // 静态阈值
        double tUpper = 0.7;
        double tLower = 0.45;
        // 动态阈值
        double tSupper;
        double tSlower;
        // 过载程度阈值
        double edup = 111;
        double eddown = 65;
        // 平衡因子
        double w = 0;
        // 根据TEST算法计算出来的
        double netMax = 18500;


        // 动态阈值计算
        // load是当前所有主机负载平均值
        double load = 0;
        // avr是历史CPU负载平均值
        double avr = 0;
        double average = 0;
        for(Host host : hostList){
            load += host.getCpuUtilization();
            average += ToArrayByFileReader.calculateCpuHistoricalAverage(FilePath.DATA_PATH
                    + "\\" + "Threeway-Migration" + "\\cpuUtilization\\vm_" + host.getId());
        }
        load = load/hostList.size();
        avr=average/hostList.size();
        double dev = Math.sqrt(Math.pow((load - avr),2)/hostList.size());

        tSupper = ((load - avr) * dev)/hostList.size() * w + tUpper;
        tSlower = ((load - avr) * dev)/hostList.size() * w + tLower;
        System.out.println("此次动态阈值为：" + tSupper + " / " + tSlower);
        // 初始化不同负载主机列表
        List<Host> highHostList = new ArrayList<Host>();
        List<Host> normalHostList = new ArrayList<Host>();
        List<Host> lowHostList = new ArrayList<Host>();
        List<Host> nullHostList = new ArrayList<Host>();

        //迁移开始
        for(Host host : hostList){
            if (host.getCpuUtilization() > tSupper) highHostList.add(host);
            else if(host.getCpuUtilization() <= tSupper && host.getCpuUtilization() >= tSlower) normalHostList.add(host);
            else if(host.getCpuUtilization() < tSlower && host.getCpuUtilization() > 0) lowHostList.add(host);
            else nullHostList.add(host);
        }

        // 遍历高负载主机
        if(highHostList.size() >= 1){
            for(Host host : highHostList){
                Vm migVm;
                Host goalHost;
                // 三支决策
                // 计算ed
                double ed = (int)caluate(host,tSupper,netMax);

                // 第一种迁移决策
                if(ed > edup){
                    // 选择虚拟机策略1
                    migVm = mig1(host);
                    // 请求虚拟机资源
                    THREE_WAY.mipsRequest = migVm.getMips();
                    migMessage[1] += 1;
                    // 目标主机策略
                    goalHost = goalHostPolicy(migVm,normalHostList,lowHostList,nullHostList);
                    //资源更新
                    if(goalHost !=null ){
                        // 分配虚拟机资源
                        THREE_WAY.mipsAllcation = migVm.getMips();
                        double[] net = NetworkCalculate.netValueBefore(migVm,null);
                        //返回能耗
                        migMessage[0] += updateVmAndHost(
                                migVm,host,goalHost,net[0]+net[1]);
                    }
                }
                else if(ed <= edup && ed >= eddown){
                    // 第二种迁移决策
                    List<Vm> migVmGroup = mig2(host,tSupper);
                    if(migVmGroup != null){
                        migMessage[1] += 1;
                        for(Vm vm: migVmGroup){
                            // 请求虚拟机资源
                            THREE_WAY.mipsRequest = vm.getMips();
                            // 目标主机策略
                            goalHost = goalHostPolicy(vm,normalHostList,lowHostList,nullHostList);
                            //资源更新
                            if(goalHost !=null ){
                                // 分配虚拟机资源
                                THREE_WAY.mipsAllcation = vm.getMips();
                                double[] net = NetworkCalculate.netValueBefore(vm,null);
                                //返回能耗
                                migMessage[0] += updateVmAndHost(
                                        vm,host,goalHost,net[0]+net[1]);
                            }
                        }
                    }
                }
                else if(ed < eddown){
                    // 第三种迁移决策
                    migVm = mig3(host);
                    if(migVm !=null){
                        // 请求虚拟机资源
                        THREE_WAY.mipsRequest = migVm.getMips();
                        migMessage[1] += 1;
                        // 目标主机放置
                        goalHost = goalHostPolicy(migVm,normalHostList,lowHostList,nullHostList);
                        if(goalHost !=null ){
                            // 分配虚拟机资源
                            THREE_WAY.mipsAllcation = migVm.getMips();
                            double[] net = NetworkCalculate.netValueBefore(migVm,null);
                            //返回能耗
                            migMessage[0] += updateVmAndHost(
                                    migVm,host,goalHost,net[0]+net[1]);
                        }
                    }
                }
            }
        }
        // 遍历低负载主机
        if(lowHostList!=null){
            for(Host host : lowHostList){
                List<Vm> migGroup = new ArrayList<Vm>();
                for (Vm vm : host.getVmList()){
                    migGroup.add(vm);
                    if(vm.getNet() != 0){
                        // 如果网络不为0
                        // 不进行迁移
                        break;
                    }
                }
                if(migGroup.size() == host.getVmList().size()){
                    // 进行迁移
                    for(Vm vm : migGroup){
                        // 目标主机
                        // 请求虚拟机资源
                        THREE_WAY.mipsRequest = vm.getMips();
                        // 目标主机策略
                        Host goalHost = goalHostPolicy(vm,normalHostList,lowHostList,nullHostList);
                        //资源更新
                        if(goalHost !=null ){
                            // 分配虚拟机资源
                            THREE_WAY.mipsAllcation = vm.getMips();
                            double[] net = NetworkCalculate.netValueBefore(vm,null);
                            //返回能耗
                            migMessage[0] += updateVmAndHost(
                                    vm,host,goalHost,net[0]+net[1]);
                        }
                    }

                }
            }
        }
        // 返回迁移的信息
        return migMessage;
    }

    /**
     * 虚拟机迁移策略3
     * @param host
     * @return
     */
    private Vm mig3(Host host) {
        Vm migVm = null;
        // 选择网络相关性最小的虚拟机
        double netMin = Double.MAX_VALUE;
        for(Vm vm : host.getVmList()){
            double[] net = NetworkCalculate.netValueBefore(vm,null);
            double netValue = net[0]+net[1];
            if(netValue < netMin){
                migVm = vm;
                netMin = net[0]+net[1];
            }else if(netValue == netMin){
                // 如果有多个虚拟机都是最小的，则选择mips最合适的
                if(vm.getMips() < migVm.getMips()){
                    migVm = vm;
                }
            }
        }
        return migVm;
    }

    /**
     * 虚拟机迁移策略2
     * @param host
     * @param tSupper
     */
    private List<Vm> mig2(Host host,double tSupper) {
        // 构建迁移组
        List<Vm> selectVmList = new ArrayList<Vm>();
        List<Vm> migGroup = new ArrayList<Vm>();
        double minNet = Double.MAX_VALUE;
        Vm noGroupVm = null;
        for(Vm vm : host.getVmList()){
            if((host.getMips() - host.getAvailablemips()
                    - vm.getMips())/host.getMips() > tSupper){
                selectVmList.add(vm);
            }else {
                double[] net = NetworkCalculate.netValueBefore(vm,null);
                double netValue = net[0]+net[1];
                if(netValue < minNet){
                    noGroupVm = vm;
                    minNet = netValue;
                }
            }
        }
        // 排序虚拟机可选列表
        // 迁移的虚拟机按照mips排序，从小到大
        List<Vm> removeVmList = new ArrayList<Vm>();
        for(int i=0; i< selectVmList.size();i++) {
            for(int j =1; j< selectVmList.size();j++){
                if(selectVmList.get(j).getMips() > selectVmList.get(i).getMips()){
                    Collections.sort(selectVmList, new Comparator<Vm>() {
                        @Override
                        public int compare(Vm o1, Vm o2) {
                            if(o1.getMips() > o2.getMips()){
                                return 1;
                            }else if(o1.getMips() < o2.getMips()){
                                return -1;
                            }
                            return 0;
                        }
                    });
                }
                if(selectVmList.get(j).getMips() == selectVmList.get(i).getMips()){
                    if(selectVmList.get(j).getNet() > selectVmList.get(i).getNet()){
                        removeVmList.add(selectVmList.get(j));
                    }
                }
            }
        }
        selectVmList.removeAll(removeVmList);
        //minMips = selectVmList.get(0).getMips() + selectVmList.get(selectVmList.size()-1).getMips();
        double minMips = Double.MAX_VALUE;
        Vm maxMipsVmFromGroup = null;
        double Utilizationcha = host.getCpuUtilization() - tSupper;
        double mipscha = Utilizationcha * host.getMips();

        if(selectVmList.size()!=0){
            // 构建最小MIPS的虚拟机迁移组
            for(int i=0;i<selectVmList.size()/2;i++){
                for(int j=selectVmList.size()-1;j>=selectVmList.size()/2;j--){
                    Vm vms = selectVmList.get(i);
                    double mipsSum = selectVmList.get(i).getMips() + selectVmList.get(j).getMips();
                    if ((host.getMips() - host.getAvailablemips()
                            - mipsSum)/host.getMips() > tSupper){

                    }else if((host.getMips() - host.getAvailablemips()
                            - mipsSum)/host.getMips() < tSupper && mipsSum < minMips){
                        minMips = mipsSum;
                        migGroup.removeAll(migGroup);
                        migGroup.add(selectVmList.get(i));
                        migGroup.add(selectVmList.get(j));
                    }
                }
            }
            if(migGroup==null){
                migGroup.add(noGroupVm);
            }
        }
        return migGroup;
    }

    /**
     * 虚拟机迁移策略1
     * @param host
     * @return
     */
    private Vm mig1(Host host) {
        // 选择虚拟机策略
        Vm migVm = null;
        // 选择Mips最大的虚拟机
        double mipsMax = Double.MIN_VALUE;
        Vm mipsMaxVm = null;
        for(Vm vm : host.getVmList()){
            if(vm.getMips() > mipsMax){
                migVm = vm;
                mipsMax = vm.getMips();
            }else if(vm.getMips() == mipsMax){
                double[] net = NetworkCalculate.netValueBefore(vm,null);
                double netValue = net[0]+net[1];
                double[] netmipsMax = NetworkCalculate.netValueBefore(mipsMaxVm,null);
                double netValuemipsMax = netmipsMax[0]+netmipsMax[1];
                // 当前的网络小于mipsMaxVm的网络
                // 则选择网络小的
                if(netValue < netValuemipsMax){
                    migVm = vm;
                }else {
                    continue;
                }
            }
        }
        /*// 若多个虚拟机满足，则选择网络相关最小的虚拟机
        List<Vm> selectVmList = new ArrayList<Vm>();
        double netMin = Double.MAX_VALUE;
        selectVmList.add(mipsMaxVm);
        for(Vm vm : host.getVmList()){
            if(vm.getMips() == mipsMaxVm.getMips()){
                selectVmList.add(vm);
            }
        }
        if(selectVmList.size()>=2){
            for(Vm vm : selectVmList){
                double[] net = NetworkCalculate.netValueBefore(vm,null);
                double netValue = net[0]+net[1];
                if(netValue < netMin){
                    migVm = vm;
                    netMin = net[0]+net[1];
                }
            }
        }*/
        return migVm;
    }

    /**
     * 目标主机确定算法
     * @param migVm
     * @param normalHostList
     * @param lowHostList
     * @param nullHostList
     * @return
     */
    private Host goalHostPolicy(Vm migVm,
                                List<Host> normalHostList,
                                List<Host> lowHostList,
                                List<Host> nullHostList) {
        Host goalHost = null;
        if(migVm !=null){
            goalHost = hostSelect(migVm, normalHostList);
            if(goalHost == null){
                if(nullHostList.size()>0){
                    goalHost = nullHostList.get(0);
                    nullHostList.remove(goalHost);
                    //lowHostList.add(goalHost);
                }
            }
        }
        return goalHost;
    }

    /**
     * 计算ed的值
     * @param host
     * @param tSupper
     * @return
     */
    private double caluate(Host host, double tSupper,double netMAX) {
        double over = (host.getCpuUtilization() - tSupper)/(1-tSupper)*10;
        double netValue = 0;
        for(Vm vm : host.getVmList()){
            netValue = 0;
            double[] net = NetworkCalculate.netValueBefore(vm,null);
            netValue += net[0]+net[1];
            vm.setNet(netValue);
        }
        double ed = Math.pow(over,2)/(netValue/18500*10);

        return ed;
    }

    /**
     * 主机选择策略
     * @param selectVm
     * @param hostList
     * @return
     */
    public static Host hostSelect(Vm selectVm,
                                  List<Host> hostList) {
        Host goalHost = null;
        for(Host host : hostList) {
            boolean put = ExampleUtils.vmIsAvailableHost(selectVm, host);
            if(put == true) {
                if((host.getMips() - host.getAvailablemips() + selectVm.getMips())/host.getMips() <= 0.7){
                    goalHost = host;
                    break;
                }
            }
        }
        return goalHost;
    }

    /**
     * 更新资源
     * @param vm 迁移的虚拟机
     * @param host 虚拟机的原主机
     * @param goalHost 迁移的目标主机
     * @param netValue 网络相关度
     * @return
     */
    public static double updateVmAndHost(
            Vm vm, Host host, Host goalHost,
            double netValue) {
        double netEnger = 0;
        //原主机减少资源
        ExampleUtils.finishVmInHost(vm,host);
        //目标主机增加资源
        ExampleUtils.updateVmInHost(goalHost,vm);
        vm.setHost(goalHost);
        host.getVmList().remove(vm);
        goalHost.getVmList().add(vm);
        netEnger = (netValue) * ExampleConstant.DATACENTER_COST_BW;
        return netEnger;
    }
}