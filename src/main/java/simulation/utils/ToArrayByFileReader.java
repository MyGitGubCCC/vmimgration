package simulation.utils;

import simulation.core.Vm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class ToArrayByFileReader {

    /**
     * 这个是读取谷歌trace文件的方法
     * @param name
     * @return
     */
    public static ArrayList<String[]> readerByFile(String name) {
        ArrayList<String> dataSet = new ArrayList<String>();
        try {
            FileReader fr = new FileReader(name);
            BufferedReader bf = new BufferedReader(fr);
            String str;
            //按行读取字符串
            while ((str = bf.readLine()) != null) {
                dataSet.add(str);
            }
            bf.close();
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //对ArrayList中存储的字符串进行处理
        int length = dataSet.size();
        ArrayList<String[]> arrayList = new ArrayList<String[]>();

        //9 10 11 12
        for (int i = 0; i < length; i++) {
            String[] s = dataSet.get(i).split(",");
            String[] resource = new String[6];
            resource[0] = s[2];
            resource[1] = s[9];
            resource[2] = s[10];
            resource[3] = s[11];
            resource[4] = s[12];
            resource[5] = s[8];
            arrayList.add(resource);
        }
        return arrayList;
    }

    /**
     * 计算cpu历史利用率的平均值
     * @param filename 历史利用率文件名
     * @return
     */
    public static double calculateCpuHistoricalAverage(String filename){
        ArrayList<String> dataSet = new ArrayList<String>();
        try {
            FileReader fr = new FileReader(filename);
            BufferedReader bf = new BufferedReader(fr);
            String str;
            //按行读取字符串
            while ((str = bf.readLine()) != null) {
                dataSet.add(str);
            }
            bf.close();
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //对ArrayList中存储的字符串进行处理
        int length = dataSet.size();
        double avg = 0.0;

        //9 10 11 12
        for (int i = 0; i < length; i++) {
            String s = dataSet.get(i);
            double d = Double.parseDouble(s);
            avg += d;
        }
        avg /= length;
        return avg;

    }

    /**
     * 获取cpu历史利用率，CDLD算法需要
     * @param filename
     * @return
     */
    public static double calculateCpuHistorical(String filename, Vm vm){
        ArrayList<String> dataSet = new ArrayList<String>();
        double U = 0;
        try {
            FileReader fr = new FileReader(filename);
            BufferedReader bf = new BufferedReader(fr);
            String str;
            //按行读取字符串
            while ((str = bf.readLine()) != null) {
                dataSet.add(str);
            }
            bf.close();
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //对ArrayList中存储的字符串进行处理
        int length = dataSet.size();
        double avg = 0.0;

        //9 10 11 12
        for (int i = 0; i < length; i++) {
            String s = dataSet.get(i);
            double d = Double.parseDouble(s);
            U += d * vm.getMips()/vm.getHost().getMips();
        }
        return U;

    }

    /**
     * 对ed聚类时使用的，读取文件的方法
     * @param filename
     */
    public static double[] historyUtilzationToDouble(String filename){
        ArrayList<String> dataSet = new ArrayList<String>();
        try {
            FileReader fr = new FileReader(filename);
            BufferedReader bf = new BufferedReader(fr);
            String str;
            //按行读取字符串
            while ((str = bf.readLine()) != null) {
                dataSet.add(str);
            }
            bf.close();
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //对ArrayList中存储的字符串进行处理
        int length = dataSet.size();
        double datas[] = new double[length];
        //9 10 11 12
        for (int i = 0; i < length; i++) {
            String s = dataSet.get(i);
            double d = Double.parseDouble(s);
            datas[i] = d;
        }
        return datas;
    }
    /**
     * 历史cpu利用率中最小的
     * @param filename
     * @return
     */
    public static double calculateCpuHistoricalMin(String filename){
        ArrayList<String> dataSet = new ArrayList<String>();
        try {
            FileReader fr = new FileReader(filename);
            BufferedReader bf = new BufferedReader(fr);
            String str;
            //按行读取字符串
            while ((str = bf.readLine()) != null) {
                dataSet.add(str);
            }
            bf.close();
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //对ArrayList中存储的字符串进行处理
        int length = dataSet.size();

        //9 10 11 12
        double mincpu = Double.MAX_VALUE;
        for (int i = 0; i < length; i++) {
            String s = dataSet.get(i);
            double d = Double.parseDouble(s);
            if(d < mincpu){
                mincpu = d;
            }
        }
        return mincpu;

    }


    public static void main(String[] args) {
        double avg = ToArrayByFileReader.calculateCpuHistoricalAverage(FilePath.DATA_PATH
                + "\\Threeway-Migration\\cpuUtilization\\host_" + 0);
        System.out.println(avg);
    }
}
