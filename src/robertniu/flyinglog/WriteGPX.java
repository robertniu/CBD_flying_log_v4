package robertniu.flyinglog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.xmlpull.v1.XmlSerializer;

import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Environment;
import android.util.Xml;
import android.widget.Toast;


public class WriteGPX 
{

	//private static final String SDCARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
	private static final String directory ="/CBDflyinglog2";
	private final Context ct;
	XmlSerializer serializer = Xml.newSerializer();  
	StringWriter writer = new StringWriter();  
	
	public WriteGPX(Context context)
	{
		ct = context;
		try{  
		serializer.setOutput(writer);  
		writer.flush();
		}
		catch(Exception e)  
		{  
		throw new RuntimeException(e);  
		} 
		
		
	}

	public String WriteGpxFileHead()
	{
		try{
			writer.flush();
			 
		// <?xml version="1.0" encoding="UTF-8" standalone="yes"?>  
		serializer.startDocument("UTF-8",true);  
		 

		// <gpx xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.cluetrust.com/XML/GPXDATA/1/0 http://www.cluetrust.com/Schemas/gpxdata10.xsd" xmlns:gpxdata="http://www.cluetrust.com/XML/GPXDATA/1/0" version="1.1" ">
		serializer.startTag("","gpx");  
		serializer.attribute("","xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");  
		 
 
		serializer.startTag("","trk");  
		

		serializer.startTag("","name");  
		serializer.text("CBD_FLYING_LOG");  
		serializer.endTag("","name");  
		
		serializer.startTag("","trkseg");  
		return writer.toString();  
		}
		
		catch(Exception e)  
		{  
		throw new RuntimeException(e);  
		} 
		
	}
	
	public String WriteGpxFileEnd()
	{
		try{
			writer.flush();
			// </trkseg>  
			serializer.endTag("","trkseg");  
			//</trk>  
			serializer.endTag("","trk");  
			// </gpx>  
			serializer.endTag("","gpx");  
			
			serializer.endDocument();  
			return writer.toString();  
		}
		catch(Exception e)  
		{  
		throw new RuntimeException(e);  
		} 
		
	}
	
	public String WriteGpxString(double Latitude,double Longitude,double High,String Time)
	{  

		try{
			writer.flush();

 		// <trkpt lat="40.005426" lon="116.33007">
		serializer.startTag("","trkpt");  
		serializer.attribute("","lat",Double.toString(Latitude));  
		serializer.attribute("","lon",Double.toString(Longitude));
		
		serializer.startTag("","ele");  
		serializer.text(Double.toString(High));  
		serializer.endTag("","ele");  
		
		serializer.startTag("","time");  
		serializer.text(Time);  
		serializer.endTag("","time");  
		 
		//</trkpt>  
		serializer.endTag("","trkpt");  
		return writer.toString();  
		}  
		catch(Exception e)  
		{  
		throw new RuntimeException(e);  
		}  
		}  


	public void WriteGpxFile(String content,String FILE_NAME)
	{
		try
		{	
			//如果手机插入了SD卡，而且应用程序具有访问SD的权限
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			{
				//获取SD卡的目录
				File sdCardDir = Environment.getExternalStorageDirectory();
				File targetDir = new File(sdCardDir.getCanonicalPath()+directory);
				  if (!targetDir.exists()) {
					  targetDir.mkdirs();
					  }
				File targetFile = new File(targetDir+FILE_NAME);

				//以指定文件创建	RandomAccessFile对象
				RandomAccessFile raf = new RandomAccessFile(targetFile , "rw");
				//将文件记录指针移动到最后
				raf.seek(targetFile.length());
				// 输出文件内容
				raf.write(content.getBytes());
				raf.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	
	
	
	/*
	
    public boolean WriteFile(String path,String txt)  
    {  
    try 
    {  
    OutputStream os = ct.openFileOutput(path,Context.MODE_PRIVATE);  
    OutputStreamWriter osw=new OutputStreamWriter(os);  
    osw.write(txt);  
    osw.close();  
    os.close();  
    }  
    catch(FileNotFoundException e)  
    {  
    return false;  
    }  
    catch(IOException e)  
    {  
    return false;  
    }  
    return true;  
    }



	   public void WriteFileTest(String filename)  
	    {  
	    try 
	    {  
	    OutputStream os = ct.openFileOutput(filename,Context.MODE_PRIVATE);  
	    OutputStreamWriter osw=new OutputStreamWriter(os);  
	    osw.write("shshshs");  
	    osw.close();  
	    os.close();  
	    }  
	    catch(FileNotFoundException e)  
	    {  
	     
	    }  
	    catch(IOException e)  
	    {  
	    
	    }  
	    
	    }


		public String readgpxfile(String FILE_NAME)
		{
			try
			{
				//如果手机插入了SD卡，而且应用程序具有访问SD的权限
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				{
					//获取SD卡对应的存储目录
					File sdCardDir = Environment.getExternalStorageDirectory();
					//获取指定文件对应的输入流
					FileInputStream fis = new FileInputStream(sdCardDir.getCanonicalPath()+directory+FILE_NAME);
					//将指定输入流包装成BufferedReader
					BufferedReader br = new BufferedReader(new InputStreamReader(fis));
					StringBuilder sb = new StringBuilder("");
					String line = null;
					while((line = br.readLine()) != null)
					{
						sb.append(line);
					}
					return sb.toString();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}


*/







	
}

