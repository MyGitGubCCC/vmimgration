package simulation.vmShedulePolicy;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import simulation.core.Host;
import simulation.core.Vm;
import simulation.example.THREE_WAY;
import simulation.utils.*;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @program: vmimgration
 * @description:
 * @author: 曹成成
 * @createDate: 2020-04-04 11:25
 */
public class VmSheduleHostDTCM extends VmSheduleHost{

    /**
     * CM算法迁移虚拟机
     * @param hostList 所有主机列表
     * @param current 当前时间
     * @param migEnergy 能耗
     * @param migNumber
     * @param mipsRequest
     * @param mipsAllcation
     * @param vmList
     * @return
     * @throws FileNotFoundException
     */
    @Override
    public double[] getVmMigrationHost(List<Host> hostList, int current, double migEnergy, int migNumber, Double mipsRequest, Double mipsAllcation, List<Vm> vmList) throws FileNotFoundException {
        // 迁移能耗
        double[] migMessage = new double[2];
        // migMessage[0]是迁移次数
        // migMessage[1]是迁移能耗

        // 静态阈值
        double tUpper = 0.7;
        double tLower = 0.45;
        // 动态阈值
        double tSupper;
        double tSlower;
        // 平衡因子
        double w = 1000;
        double DEV = 0;

        /*
         * 1、阈值计算
         * 2、组合算法选择虚拟机
         * 3、选择目标主机
         */

        // 1、阈值计算
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

        // 判断主机负载
        // 初始化不同负载主机列表
        List<Host> highHostList = new ArrayList<Host>();
        List<Host> normalHostList = new ArrayList<Host>();
        List<Host> lowHostList = new ArrayList<Host>();

        //迁移开始
        for(Host host : hostList){
            if (host.getCpuUtilization() > tSupper) highHostList.add(host);
            else if(host.getCpuUtilization() <= tSupper && host.getCpuUtilization() >= tSlower) normalHostList.add(host);
            else lowHostList.add(host);
        }

        // 2、组合算法选择虚拟机
        for(Host host : highHostList){
            Vm migVm = null;
            // 请求虚拟机资源
            THREE_WAY.mipsRequest += migVm.getMips();
            migMessage[1] += 1;
            // 如果dev>DEV
            if(dev > DEV){
                for(Vm vm : host.getVmList()){
                    // 最大相关系数策略选择虚拟机
                    final double numberVms = host.getVmList().size();
                    final double minHistorySize = ToArrayByFileReader.calculateCpuHistoricalMin(FilePath.DATA_PATH
                            + "\\" + "Threeway-Migration" + "\\cpuUtilization\\vm_" + host.getId());
                    final double[][] utilization = new double[(int)numberVms][(int)minHistorySize];

                    final List<Double> metrics = getCorrelationCoefficients(utilization);
                    double maxMetric = Double.MIN_VALUE;
                    for (int i = 0; i < metrics.size(); i++) {
                        final double metric = metrics.get(i);
                        if (metric > maxMetric) {
                            maxMetric = metric;
                            migVm = vm;
                        }
                    }
                }
            }else {
                // 如果dev<=DEV
                // 迁移最大CPU利用率的虚拟机
                double mipsMax = Double.MIN_VALUE;
                for(Vm vm : host.getVmList()){
                    if(vm.getMips() > mipsMax){
                        mipsMax = vm.getMips();
                        migVm = vm;
                    }
                }
            }

            // 3、选择目标主机
            if(migVm!=null){
                Host goalHost = null;
                for(Host host1: lowHostList){
                    boolean put = ExampleUtils.vmIsAvailableHost(migVm, host1);
                    if(put == true) {
                        goalHost = host;
                        break;
                    }
                }
                //资源更新
                if(goalHost !=null ){
                    // 分配虚拟机资源
                    THREE_WAY.mipsAllcation += migVm.getMips();
                    double[] net = NetworkCalculate.netValueBefore(migVm,null);
                    //返回能耗
                    migMessage[0] += updateVmAndHost(
                            migVm,host,goalHost,net[0]+net[1]);
                }
            }

        }

        return migMessage;
    }

    protected List<Double> getCorrelationCoefficients(final double[][] data) {
        final int rows = data.length;
        final int cols = data[0].length;
        final List<Double> correlationCoefficients = new LinkedList<Double>();
        for (int i = 0; i < rows; i++) {
            final double[][] x = new double[rows - 1][cols];
            int k = 0;
            for (int j = 0; j < rows; j++) {
                if (j != i) {
                    x[k++] = data[j];
                }
            }

            // Transpose the matrix so that it fits the linear model
            final double[][] xT = new Array2DRowRealMatrix(x).transpose().getData();

            // RSquare is the "coefficient of d etermination"
            correlationCoefficients.add(MathUtil.createLinearRegression(xT, data[i]).calculateRSquared());
        }
        return correlationCoefficients;
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
