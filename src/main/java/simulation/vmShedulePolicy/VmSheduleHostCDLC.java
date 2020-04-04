
package simulation.vmShedulePolicy;

import simulation.core.Host;
import simulation.core.Vm;
import simulation.example.CDLC;
import simulation.utils.*;

import java.io.FileNotFoundException;
import java.util.*;

/**
 * @program: vmimgration
 * @description: 基于容量感知和负载特征的虚拟机选择算法_张冬生_2015
 * @author: 杨翎
 * @createDate: 2020-02-03 12:49
 */
public class VmSheduleHostCDLC extends VmSheduleHost{

    /**
     * 该算法对大容量的服务器采用最小迁移次数和MMT策略选择迁移的虚拟机=MM策略
     * MM策略：优先选择满足虚拟机移除之后能使主机负载降低到阈值之下的虚拟机
     * 如果同时有多个虚拟机满足，优先选择平均贡献度最高的虚拟机
     * @param hostList
     * @param current
     * @throws FileNotFoundException
     */
    @Override
    public double[] getVmMigrationHost(List<Host> hostList, int current,
                                       double migEnergy, int migNumber,
                                       Double mipsRequest, Double mipsAllcation,
                                       List<Vm> vmList) throws FileNotFoundException {
        // 统一比较实验中对负载阈值的判断
        double up = 0.7;
        // 迁移信息
        double[] migMessage = new double[2];
        // 定义一些变量
        List<Host> highHost = new ArrayList<Host>();
        List<Host> normalHost = new ArrayList<Host>();

        // 1、区分主机的类型，判断是否过载
        for(Host host : hostList) {
            if(host.getCpuUtilization() > up) highHost.add(host);
            else normalHost.add(host);
        }

        // 2、当主机过载时，采用MM策略
        for(Host host : highHost) {
            // 触发迁移次数+1
            migNumber ++;
            // 优先处理单个迁移就可以满足条件的虚拟机
            Vm selectVm = null;
            // 可选的虚拟机队列
            List<Vm> selectVmList = new ArrayList<Vm>();
            double maxVm = Double.MIN_VALUE;
            // 遍历虚拟机，找到可以选择的虚拟机列表
            for(Vm vm : host.getVmList()) {
                if((host.getMips() - host.getAvailablemips() - vm.getMips())/host.getMips() < up) {
                    selectVmList.add(vm);
                }
            }
            // 从selectVmList可选虚拟机队列中再寻找虚拟机
            // 读取数据
            double uii = 0;
            double U = 0;
            for(Vm vm : selectVmList) {
                ArrayList<String[]> vmMips
                        = ToArrayByFileReader.readerByFile(FilePath.DATA_PATH + "\\" + "CDLD" + "\\cpuUtilization\\vm_" + vm.getId());
                String a[] = new String[100];
                for(int i=0;i<vmMips.size();i++){
                    a = vmMips.get(i);

                }
                for(int i=0;i<a.length;i++){
                    uii = Double.parseDouble(a[i]);
                    U += uii*vm.getMips()/vm.getHost().getMips();
                }
                // 选择平均贡献最高的虚拟机
                if(U > maxVm) {
                    selectVm = vm;
                    maxVm = vm.getMips();
                }
            }
            // 如果selectVm为空，说明selectVmList为空
            if(selectVm == null) {
                maxVm = Double.MIN_VALUE;
                // 选择虚拟机
                for(Vm vm : host.getVmList()) {
                    if(vm.getMips() > maxVm) {
                        selectVm = vm;
                        maxVm = vm.getMips();
                    }
                }
            }
            // selectVm就是需要迁移的虚拟机
            if(selectVm != null) {
                // 虚拟机请求迁移mips
                CDLC.mipsRequest += selectVm.getMips();
                // 迁移网络为netValue
                double[] netValue = NetworkCalculate.netValueBefore(selectVm,null);
                // 打印迁移信息
                System.out.println("主机" + host.getId() + "上的虚拟机需要迁移！");
                // 如果不为空，则开始迁移
                Host goalHost = MinMIgration(selectVm,normalHost,host);
                if(goalHost != null) {
                    //计算迁移代价
                    migEnergy += (netValue[0]+netValue[1]) * ExampleConstant.DATACENTER_COST_BW;
                    CDLC.mipsAllcation += selectVm.getMips();
                    //System.out.println(migEnergy);
                }else {
                    System.out.println("虚拟机目前没有合适的主机放置");
                }
            }
        }
        migMessage[0] = migEnergy;
        migMessage[1] = (double) migNumber;
        return migMessage;
    }

    /**
     * 目标主机放置策略
     * @param selectVm
     * @param normalHost
     * @param host
     */
    public static Host MinMIgration(
            Vm selectVm, List<Host> normalHost,
            Host host) {
        Host goalHost = null;
        //选择主机
        goalHost = update(normalHost,selectVm,host);
        if (goalHost == null) System.out.println("虚拟机目前不能进行迁移，没有合适的主机资源！");
        return goalHost;
    }

    /**
     * 更新资源
     * @param hostList
     * @param selectVm
     * @param host
     */
    public static Host update(List<Host> hostList, Vm selectVm,Host host) {
        Host goalHost = null;
        for(Host host1 : hostList) {
            boolean is = ExampleUtils.vmIsAvailableHost(selectVm, host1);
            if(is == true) {
                goalHost = host1;
                ExampleUtils.finishVmInHost(selectVm,host);
                ExampleUtils.updateVmInHost(goalHost,selectVm);
                host.getVmList().remove(selectVm);
                goalHost.getVmList().add(selectVm);
                selectVm.setHost(goalHost);
                System.out.println("虚拟机迁移到主机" + host1.getId() + "上");
                return  goalHost;
            }
        }
        return goalHost;
    }
}
