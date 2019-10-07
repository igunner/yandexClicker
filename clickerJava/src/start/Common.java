 package start;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Common 
{
  static Common ref = null;
  String logFile = null;
  static void Init(String log_file)
  {
    if (ref == null)
      ref = new Common();    
    ref.logFile = log_file;
    FLOG("Full log path: " + ref.logFile);
  }
  public static ArrayList<String> readFile(String name)
  {
    File file = new File(name);
    if (!file.canRead())
      return null;
      try { 
          BufferedReader br = new BufferedReader(new FileReader(file));
          ArrayList<String> list = new ArrayList<String>();
          {
              String line;
              while ((line = br.readLine()) != null) 
              {
                list.add(line);
              }
          }
          return list;
      } catch(IOException e){
          System.out.println(e.getMessage());
      }
      return null;
  }
  static public void FLOG(String s)
  {
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(Calendar.getInstance().getTime());
    String content = timeStamp + ": " + s + "\n";
      System.out.print(content);
      try 
      {
        PrintStream out = new PrintStream(new FileOutputStream(ref.logFile, true));
        out.print(content);
        out.close();
      }
      catch (IOException e)
      {
        
      }
  }
  
  static public void FERR(String s)
  {
    FLOG("ERROR: " + s);
  }
  static public void RESET_LOG() 
  {
      try 
      {
        PrintStream out = new PrintStream(new FileOutputStream(ref.logFile));
        out.close();
      }
      catch (IOException e)
      {
        
      }
  }
}
