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
        
        //init(8,2,new File("C:\\Users\\Administrator\\Desktop\\b\\�ҵ�ͼƬ��Ŀ¼����.txt"));
//        init(8,0,new File("C:\\Users\\Administrator\\Desktop\\b\\����ͼƬ.txt"));
		
	}

	/**
	 * 
	 * @param num  list.list�ĳ���  
	 * @param firstNum  list.list���б��ļ���ռ�õ�λ��
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
        	if(i == num-firstNum-1 || (i == list.size()-1)){ //��һ��  or �����������
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
	 * @param fileCatalog Ŀ¼��
	 * @param i
	 * @param ii
	 */
	private static void execute(){
		String temp = lists.get(position).get(childPostion);
		try {
			Response response = okClient.newCall(new Request.Builder().url(temp).build()).execute();
			if(response.isSuccessful()){
				long netByteSize,localByteSize;
				netByteSize = response.body().contentLength(); //����ͼƬ�ֽڵĴ�С
				
				String suffix = "."+temp.split("\\.")[temp.split("\\.").length-1]; //��׺
				
				File file = new File("C:\\Users\\Administrator\\Desktop\\b\\"+fileCatalog+"\\" + (position + 1) + "_"+(childPostion + 1) + suffix);
				writeFileFromIS(file, response.body().byteStream(), false);
				
				localByteSize = file.length();
				if(localByteSize < 10){ //��ȡ�����ļ��Ĵ�С
					System.out.println("����ִ�г���"+ (position + 1) + "_"+(childPostion + 1));
				}else{ 
					if(netByteSize == localByteSize){  //�Ƚ�������С
						++total;
						System.out.println("�� "+(position + 1) + "_"+(childPostion + 1)+" ����˳����ɣ�");
						if(childPostion == lists.get(position).size()-1){ //list.listִ�е����һ����
							if(position == lists.size()-1){ //listִ�е����һ���ˣ����
								System.out.println("�� "+total+" ������ȫ�����");
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
						System.out.println("����ִ�г���"+ (position + 1) + "_"+(childPostion + 1));
					}
				}
			}else System.out.println("����ִ�г���"+ (position + 1) + "_"+(childPostion + 1));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("����ִ�г���" + (position + 1) + "_"+(childPostion + 1));
		}
	}


    /**
     * ��������д���ļ�
     *
     * @param file   �ļ�
     * @param is     ������
     * @param append �Ƿ�׷�����ļ�ĩ
     * @return {@code true}: д��ɹ�<br>{@code false}: д��ʧ��
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
     * �ж��ļ��Ƿ���ڣ����������ж��Ƿ񴴽��ɹ�
     *
     * @param file �ļ�
     * @return {@code true}: ���ڻ򴴽��ɹ�<br>{@code false}: �����ڻ򴴽�ʧ��
     */
    public static boolean createOrExistsFile(File file) {
        if (file == null) return false;
        // ������ڣ����ļ��򷵻�true����Ŀ¼�򷵻�false
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
     * �ж�Ŀ¼�Ƿ���ڣ����������ж��Ƿ񴴽��ɹ�
     *
     * @param file �ļ�
     * @return {@code true}: ���ڻ򴴽��ɹ�<br>{@code false}: �����ڻ򴴽�ʧ��
     */
    public static boolean createOrExistsDir(File file) {
        // ������ڣ���Ŀ¼�򷵻�true�����ļ��򷵻�false���������򷵻��Ƿ񴴽��ɹ�
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    /**
     * �ر�IO
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
	 * ���ж�ȡĳ��txt�ļ����ݣ����ؼ���
	 * @param file
	 * @return
	 */
    public static List<String> readTxtFile(File file){
    	List<String> list1 = new ArrayList<String>();
        try {
                String encoding="GBK";
                if(file.isFile() && file.exists()){ //�ж��ļ��Ƿ����
                    InputStreamReader read = new InputStreamReader(
                    new FileInputStream(file),encoding);//���ǵ������ʽ
                    BufferedReader bufferedReader = new BufferedReader(read);
                    String lineTxt = null;
                    while((lineTxt = bufferedReader.readLine()) != null){
                        list1.add(lineTxt);
                    }
                    read.close();
        }else{
            System.out.println("�Ҳ���ָ�����ļ�");
        }
        } catch (Exception e) {
            System.out.println("��ȡ�ļ����ݳ���");
            e.printStackTrace();
        }
        return list1;
    }

}
