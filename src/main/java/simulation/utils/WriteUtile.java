package simulation.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WriteUtile {

    static double sum = 0;
    List<Double> doublesList = new ArrayList<Double>();

    public void writecpu(double a){
        try {
            File f = new File("D:\\IdeaProjects\\yl-leetcode\\src\\simulation1\\file\\utilization_cpu");
            FileWriter fw = new FileWriter(f,true);
            BufferedWriter bufferedWriter = new BufferedWriter(fw);
            String msg = String.valueOf(a) + "\n";
            bufferedWriter.write(msg);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void writeram(double a){
        try {
            File f = new File("D:\\IdeaProjects\\yl-leetcode\\src\\simulation1\\file\\utilization_ram");
            FileWriter fw = new FileWriter(f,true);
            BufferedWriter bufferedWriter = new BufferedWriter(fw);
            String msg = String.valueOf(a) + "\n";
            bufferedWriter.write(msg);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void writerdisk(double a){
        try {
            File f = new File("D:\\IdeaProjects\\yl-leetcode\\src\\simulation1\\file\\utilization_disk");
            FileWriter fw = new FileWriter(f,true);
            BufferedWriter bufferedWriter = new BufferedWriter(fw);
            String msg = String.valueOf(a) + "\n";
            bufferedWriter.write(msg);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void writeToFile(String s, String Filename){
        try {

            File file = new File(Filename);
            File fileParent = file.getParentFile();
            //判断文件夹存不存在，不存在，创建文件夹
            if (!fileParent.exists()){
                fileParent.mkdirs();
            }
            //判断文件存不存在，不存在创建文件
            if (!file.exists()){
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file,true);
            BufferedWriter bufferedWriter = new BufferedWriter(fw);
            String msg = s + "\n";
            bufferedWriter.write(msg);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
