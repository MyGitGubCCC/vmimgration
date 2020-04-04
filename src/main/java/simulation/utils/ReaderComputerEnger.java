package simulation.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class ReaderComputerEnger {
    public static Double[] readerByFile(String name) {
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
        Double[] resource = new Double[length];

        //9 10 11 12
        for (int i = 0; i < length; i++) {
            Double s = Double.valueOf(dataSet.get(i));
            resource[i] = s;
        }
        return resource;
    }
}
