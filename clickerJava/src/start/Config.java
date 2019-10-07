package start;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class Config
{
  static Config ref = null;
  static java.util.Map<String,String> paramList = new HashMap< String, String>();
  static void Init(String path)
  {
    if (ref == null)
      ref = new Config();
    ArrayList <String> lines = Common.readFile(path);
    if (lines == null)
      return;
    for(String l: lines)
    {
      l = l.trim();
      if (l.startsWith("//"))
        continue;
      String [] list = l.split("=");      
      paramList.put(list[0].trim(), list[1].trim());
    }
  }
  
  static int getInt(String name)
  {
    int ret = -1;
    try
    {
      ret = Integer.parseInt(paramList.get(name));
    }
    catch(Exception e)
    {
      Common.FERR("getInt:" + e.getLocalizedMessage());
      e.printStackTrace();
    }
    return ret;
  }
  static String getString(String name)
  {
    try
    {
      return paramList.get(name);
    }
    catch(Exception e)
    {
      Common.FERR("getString:" + e.getLocalizedMessage());
      e.printStackTrace();
    }
    return null;
  }
  static boolean getBool(String name)
  {
    boolean ret = false;
    try
    {
      ret = Integer.parseInt(paramList.get(name)) == 0 ? false : true;
    }
    catch(Exception e)
    {
      Common.FERR("getBool:" + e.getLocalizedMessage());
      e.printStackTrace();
    }
    return ret;
  }
}
