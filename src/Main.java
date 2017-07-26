import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class Main {
	
	private static OkHttpClient okClient;
	public static List<List<String>> lists;
	
	private static int position,childPostion,total;
	private static String fileCatalog;
	
	public static void main(String[] args) {

        okClient = new OkHttpClient();
        
        //init(8,2,new File("C:\\Users\\Administrator\\Desktop\\b\\我的图片根目录下面.txt"));
//        init(8,0,new File("C:\\Users\\Administrator\\Desktop\\b\\宝贝图片.txt"));
		
	}

	/**
	 * 
	 * @param num  list.list的长度  
	 * @param firstNum  list.list首行被文件夹占用的位数
	 */
	private static void init(int num,int firstNum,File file){
        lists = new ArrayList<>();
        position =0;
        childPostion = 0;
        List<String> list = new ArrayList<String>();
        list = readTxtFile(file);
        List<String> tempList = new ArrayList<String>();
        int tempNum = 1;
        for(int i = 0; i < list.size(); i++){
        	tempList.add(list.get(i));
        	if(i == num-firstNum-1 || (i == list.size()-1)){ //第一列  or 遍历到最后了
        		lists.add(i/num,tempList);
        		tempList = new ArrayList<String>();
        		tempNum = 1;
        	}else{
        		if(tempNum == num){
        			tempNum = 0;
            		lists.add(i/num,tempList);
            		tempList = new ArrayList<String>();
        		}
        		++tempNum;
        	}
        }
        fileCatalog = file.getName().split("\\.")[0];
        execute();
	}
	
	
	/**
	 * 
	 * @param fileCatalog 目录名
	 * @param i
	 * @param ii
	 */
	private static void execute(){
		String temp = lists.get(position).get(childPostion);
		try {
			Response response = okClient.newCall(new Request.Builder().url(temp).build()).execute();
			if(response.isSuccessful()){
				long netByteSize,localByteSize;
				netByteSize = response.body().contentLength(); //网络图片字节的大小
				
				String suffix = "."+temp.split("\\.")[temp.split("\\.").length-1]; //后缀
				
				File file = new File("C:\\Users\\Administrator\\Desktop\\b\\"+fileCatalog+"\\" + (position + 1) + "_"+(childPostion + 1) + suffix);
				writeFileFromIS(file, response.body().byteStream(), false);
				
				localByteSize = file.length();
				if(localByteSize < 10){ //获取本地文件的大小
					System.out.println("任务执行出错！"+ (position + 1) + "_"+(childPostion + 1));
				}else{ 
					if(netByteSize == localByteSize){  //比较两个大小
						++total;
						System.out.println("第 "+(position + 1) + "_"+(childPostion + 1)+" 任务顺利完成！");
						if(childPostion == lists.get(position).size()-1){ //list.list执行到最后一个了
							if(position == lists.size()-1){ //list执行到最后一个了，完成
								System.out.println("共 "+total+" 任务，已全部完成");
							}else{
								childPostion = 0;
								++position;
								execute();
							}
						}else{
							++childPostion;
							execute();
						}
					}else{
						System.out.println("任务执行出错！"+ (position + 1) + "_"+(childPostion + 1));
					}
				}
			}else System.out.println("任务执行出错！"+ (position + 1) + "_"+(childPostion + 1));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("任务执行出错！" + (position + 1) + "_"+(childPostion + 1));
		}
	}


    /**
     * 将输入流写入文件
     *
     * @param file   文件
     * @param is     输入流
     * @param append 是否追加在文件末
     * @return {@code true}: 写入成功<br>{@code false}: 写入失败
     */
    public static boolean writeFileFromIS(File file, InputStream is, boolean append) {
        if (file == null || is == null) return false;
        if (!createOrExistsFile(file)) return false;
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file, append));
            byte data[] = new byte[1024];
            int len;
            while ((len = is.read(data, 0, 1024)) != -1) {
                os.write(data, 0, len);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            close(is, os);
        }
    }

    /**
     * 判断文件是否存在，不存在则判断是否创建成功
     *
     * @param file 文件
     * @return {@code true}: 存在或创建成功<br>{@code false}: 不存在或创建失败
     */
    public static boolean createOrExistsFile(File file) {
        if (file == null) return false;
        // 如果存在，是文件则返回true，是目录则返回false
        if (file.exists()) return file.isFile();
        if (!createOrExistsDir(file.getParentFile())) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 判断目录是否存在，不存在则判断是否创建成功
     *
     * @param file 文件
     * @return {@code true}: 存在或创建成功<br>{@code false}: 不存在或创建失败
     */
    public static boolean createOrExistsDir(File file) {
        // 如果存在，是目录则返回true，是文件则返回false，不存在则返回是否创建成功
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    /**
     * 关闭IO
     *
     * @param closeables closeable
     */
    public static void close(Closeable... closeables) {
        if (closeables == null) return;
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

	/**
	 * 按行读取某个txt文件内容，返回集合
	 * @param file
	 * @return
	 */
    public static List<String> readTxtFile(File file){
    	List<String> list1 = new ArrayList<String>();
        try {
                String encoding="GBK";
                if(file.isFile() && file.exists()){ //判断文件是否存在
                    InputStreamReader read = new InputStreamReader(
                    new FileInputStream(file),encoding);//考虑到编码格式
                    BufferedReader bufferedReader = new BufferedReader(read);
                    String lineTxt = null;
                    while((lineTxt = bufferedReader.readLine()) != null){
                        list1.add(lineTxt);
                    }
                    read.close();
        }else{
            System.out.println("找不到指定的文件");
        }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
        return list1;
    }

}
