package simulation.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class Enger_Example {
    public static void main(String[] args) throws Exception {
        //ReaderComputerEnger.readerByFile("D:\\IdeaProjects\\yl-leetcode\\src\\simulation\\file\\DATA\\FCFS_TWA\\cpuEnergy");
        outputAVGOrSUMtofile(FilePath.DATA_PATH + "\\Threeway-VmMigration");
    }

     public static void outputAVGOrSUMtofile(String filePath) throws Exception {
        String zongFolderName = filePath;
        File Folder1 = new File(zongFolderName);
        File[] files1 = Folder1.listFiles();
        double Esum = 0.0;
        double avg = 0;
        List<Double> sumCPUList = new ArrayList<Double>();
        List<Double> sumRAMList = new ArrayList<Double>();
        List<Double> sumBWList = new ArrayList<Double>();
        double cpusumavg = 0.0;
        double ramsumavg = 0.0;
        double bwsumavg = 0.0;
        for (File aFiles1 : files1) {
            File Folder = new File(aFiles1.toString());
            if (Folder.getName().contains("E")) {
                File[] files = Folder.listFiles();
                for (int i = 0; i < files.length; ++i) {

                    BufferedReader input = new BufferedReader(new FileReader(files[i]));
                    double sum = 0.0;
                    while (input.ready()) {
                        sum += Double.valueOf(input.readLine());
                        Esum += sum;
                    }
                    input.close();
                    FileWriter writer = new FileWriter(files[i], true);
                    writer.write("总功耗：" + sum);
                    writer.close();
                }
            } else if (Folder.getName().contains("uU")) {
                File[] files = Folder.listFiles();
                for (int i = 0; i < files.length; ++i) {

                    BufferedReader input = new BufferedReader(new FileReader(files[i]));
                    double p = 0.0;
                    double sum = 0.0;
                    int index = 0;
                    while (input.ready()) {
                        p = Double.valueOf(input.readLine());
                        sum += p;
                        if (p != 0.0) {
                            index++;
                        }
                    }
                    double avg1 = 0;
                    if (index != 0) avg1 = sum / index;
                    input.close();
                    FileWriter writer = new FileWriter(files[i], true);
                    writer.write("CPU平均利用率：" + avg1);
                    sumCPUList.add(avg1);
                    writer.close();
                }
            } else if (Folder.getName().contains("mU")) {
                File[] files = Folder.listFiles();
                for (int i = 0; i < files.length; ++i) {
                    BufferedReader input = new BufferedReader(new FileReader(files[i]));
                    double sum = 0.0;
                    int index = 0;
                    double p = 0.0;
                    while (input.ready()) {
                        p = Double.valueOf(input.readLine());
                        sum += p;
                        if (p != 0) {
                            index++;
                        }
                    }

                    if (index != 0) avg = sum / index;
                    input.close();
                    FileWriter writer = new FileWriter(files[i], true);
                    writer.write("RAM平均利用率：" + avg);
                    sumRAMList.add(avg);
                    writer.close();
                }
            }else if (Folder.getName().contains("wU")) {
                File[] files = Folder.listFiles();
                for (int i = 0; i < files.length; ++i) {
                    BufferedReader input = new BufferedReader(new FileReader(files[i]));
                    double sum = 0.0;
                    int index = 0;
                    double p = 0.0;
                    while (input.ready()) {
                        p = Double.valueOf(input.readLine());
                        sum += p;
                        if (p != 0) {
                            index++;
                        }
                    }

                    if (index != 0) avg = sum / index;
                    input.close();
                    FileWriter writer = new FileWriter(files[i], true);
                    writer.write("BW平均利用率：" + avg);
                    sumBWList.add(avg);
                    writer.close();
                }
            }
        }
        System.out.println("数据中心总消耗" + Esum);
        double cpuavg = 0.0;
        double ramavg = 0.0;
        double bwavg = 0.0;
        double num = 0;
        for(int i=0;i<sumCPUList.size();i++){
            cpuavg += sumCPUList.get(i);
            if(sumCPUList.get(i)!=0){
                num ++;
            }
        }
        for(int i=0;i<sumRAMList.size();i++){
            ramavg += sumRAMList.get(i);
        }
         for(int i=0;i<sumBWList.size();i++){
             bwavg += sumBWList.get(i);
         }
        double sumavg = (cpuavg + ramavg + bwavg)/num;
        double sum1 = 0.0;
        double sum2 = 0.0;
        double sum3 = 0.0;
        for(int i =0;i<sumCPUList.size();i++){
            sum1 += (sumCPUList.get(i) - sumavg)*(sumCPUList.get(i) - sumavg);
        }
        for(int i =0;i<sumRAMList.size();i++){
            sum2 += (sumRAMList.get(i) - sumavg)*(sumRAMList.get(i) - sumavg);
        }
        for(int i =0;i<sumBWList.size();i++){
            sum3 += (sumBWList.get(i) - sumavg)*(sumBWList.get(i) - sumavg);
        }
        System.out.println("负载均衡：" + cpuavg/num);
        System.out.println("主机多维资源不均衡度为：" + Math.log((sum2 + sum1 + sum3)/3));
        System.out.println("SLA违约率为：" + Math.log10(2.0 + cpuavg/num - 0.8));
    }
}
