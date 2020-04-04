package simulation.vmShedulePolicy;

import simulation.core.Host;
import simulation.core.Vm;
import simulation.utils.FilePath;
import simulation.utils.ToArrayByFileReader;

import java.io.FileNotFoundException;
import java.util.ArrayList;
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
        double tSupper = 0;
        double tSlower = 0;
        // 过载程度阈值
        double edup = 0;
        double eddown = 0;

        /*
         * 1、阈值计算
         * 2、组合算法选择虚拟机
         * 3、选择目标主机
         */

        // 1、阈值计算

        // 2、组合算法选择虚拟机

        // 3、选择目标主机

        return new double[0];
    }
}
