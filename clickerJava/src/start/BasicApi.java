package start;

import java.awt.Robot;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.text.Position;

import org.apache.commons.io.IOUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Point;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.Proxy.ProxyType;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.sun.jna.platform.FileUtils;

import start.BasicApi.Mouse;

public class BasicApi {
    
  void FLOG(String s)
  {
    Common.FLOG(s);
  }
  void FERR(String s)
  {
    Common.FERR(s);
  }
    
    public long curTime()
    {
      return System.currentTimeMillis();
    }

    class Mouse
    {
        int  x, y;
    };
    
    long timeToSurf = -1;
    int countSurf = -1;
    
    final String WORK_DIR = "D:\\delme\\eclipse-workspace";
    final String logFileName = "log.txt";
    final String proxyLogin = "reirjkerer";
    final String proxyPassword  = "M3s0PiS";
    final String IP_SERVER = "http://rescue03.ru/ip.php";
    final String ECLIPSE_WORKSPACE_PATH = "D:\\delme\\eclipse-workspace\\eclipse-workspace.zip";
    final String YANDEX_RU = "https://yandex.ru";
    
    Random rand = new Random(System.currentTimeMillis());
    
    public WebDriver driver;
    boolean proxyFound = false;
    String lastProxy = "";
    final long PROX_CHECK_TIMEOUT = 5;
    final int MAXIMUM_SEARCH_PAGE = 5;
    List<WebElement> foundLinks = null;
    WebElement lastFoundElement = null;
    Mouse mouse;
    int lastPage = 0;
    ArrayList<String> userAgents = null;
    ArrayList<String> socks = null;
    ArrayList<String> socksTestKeys = null;
    
    Dimension windowSize = null;
    
    ArrayList<ResolutionProb> deskTopRes = null;
    float fullProb = 0;

    class ResolutionProb
    {
      Dimension res;
      float probab;
    }
    
    int random(int base)
    {
        return Math.abs(rand.nextInt()) % base;
    }
    
  float random(float base)
  {
    return Math.abs(0 + rand.nextFloat() * (base - 0));
  }
    
  long random(long base)
  {
    return Math.abs(rand.nextLong()) % base;
  }
     
    void setupProxy()
    {
        FLOG("setupProxy()");
        lastProxy = null;
        if (socks != null && !socks.isEmpty() && socksTestKeys != null && !socksTestKeys.isEmpty())
        {
            ArrayList <String> work = socks;
            while(!work.isEmpty())
            {
                int i = random(work.size());
                String proxy = work.get(i);
                if(checkProxy(proxy, socksTestKeys.get(random(socksTestKeys.size()))))
                {
                  proxyFound = true;
                  lastProxy = proxy;
                  FLOG("Proxy found " + lastProxy);
                  break;
                }
                work.remove(i);
            }
        }
        else
            FLOG("setupProxy() failed, no proxy or test keys");
    }
    
    void setupDriver()
    {
        ChromeOptions option = new ChromeOptions();
        if (proxyFound)
        {
            Proxy proxy = new org.openqa.selenium.Proxy();
            proxy.setHttpProxy(lastProxy);
            proxy.setSslProxy(lastProxy);
            proxy.setFtpProxy(lastProxy);
            option.addExtensions(new File(WORK_DIR + "\\eclipse-workspace.zip"));
            option.setCapability(CapabilityType.PROXY, proxy);    
            FLOG("Using proxy: " + lastProxy);
        }
        //{
        option.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        option.setCapability(CapabilityType.SUPPORTS_ALERTS, true);
        option.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT);
        
        //this does not fail, can try
        //option.setCapability(CapabilityType.PLATFORM, Platform.IOS);
        //option.setCapability(CapabilityType.BROWSER_NAME, org.openqa.selenium.remote.BrowserType.IPHONE);
        //option.setCapability(CapabilityType.BROWSER_VERSION, "1.0");
        //option.setCapability(CapabilityType.VERSION, org.openqa.selenium.remote.BrowserType.IPHONE);
        //}     
            
        int userAgentId = random(userAgents.size());
        FLOG("Use user-agent #" + userAgentId + " -> " + userAgents.get(userAgentId));      
        option.addArguments("user-agent=" + userAgents.get(userAgentId));
        driver = new ChromeDriver(option);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        driver.manage().deleteAllCookies();

        if (deskTopRes != null && !deskTopRes.isEmpty())
        {
            try 
            {
              float rVal = random(fullProb);
              FLOG("float random val is " + rVal + "/" + fullProb);
              int i = 0;
              float s = 0;
              for(i = 0; i < deskTopRes.size(); i++)
              {
                s += deskTopRes.get(i).probab;
                if (rVal < s)
                  break;
              }
          FLOG("i is " + i);
              ResolutionProb p = deskTopRes.get(i);
        String textScreen = p.res.width + " " + p.res.height;
        FLOG("Use screen size " + textScreen);
              Runtime runtime = Runtime.getRuntime();
            try {
               String cmd = WORK_DIR + "\\changeDisplaySize.exe " + textScreen;
               FLOG(cmd);
           runtime.exec(cmd);
            } catch (IOException e) {
               e.printStackTrace();
            }
                driver.manage().window().setSize(p.res);
            }
            catch(Exception e)
            {
                FERR("Failed to maximize window");
            }
        }

        mouse = new Mouse();
    }

    String getMyIp()
    {
        ChromeOptions option = new ChromeOptions();
        {
            option.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            option.setCapability(CapabilityType.SUPPORTS_ALERTS, true);
            option.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT);
            option.setCapability(CapabilityType.PLATFORM, Platform.IOS);
            option.setCapability(CapabilityType.BROWSER_NAME, org.openqa.selenium.remote.BrowserType.IPHONE);
            option.setCapability(CapabilityType.BROWSER_VERSION, "1.0");
            option.setCapability(CapabilityType.VERSION, org.openqa.selenium.remote.BrowserType.IPHONE);
            option.setCapability("general.useragent.override", "Gadzilla/5.0 (Windows NT 6.1; rv:15.0) Gecko/20100101 Firefox/15.0");
        }       
      option.addArguments("--window-size=800,600");
      option.addArguments("user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:51.0) Gecko/20100101 Firefox/51.0");
      driver = new ChromeDriver(option);
      driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
      driver.manage().deleteAllCookies();
      driver.get(IP_SERVER);
      sleep(PROX_CHECK_TIMEOUT);
      
      String [] left = driver.getPageSource().split("YOURIP:");
      for(String s: left)
      {
          FLOG(s);
      }
      driver.quit();
      if (left.length < 2)
        return null;
      String [] right = left[1].split("</body>");
      if (right.length < 1)
        return null;
      return right[0];
    }

    public void waitForPageLoad() {

        Wait<WebDriver> wait = new WebDriverWait(driver, 30);
        wait.until(new Function<WebDriver, Boolean>() {
            public Boolean apply(WebDriver driver_) {
                System.out.println("Current Window State       : "
                    + String.valueOf(((JavascriptExecutor) driver_).executeScript("return document.readyState")));
                return String
                    .valueOf(((JavascriptExecutor) driver_).executeScript("return document.readyState"))
                    .equals("complete");
            }
        });
    }
    
    boolean checkProxy(String proxyStr, String test_key)
    {
      FLOG("checkProxy " + proxyStr);
      ChromeOptions option = new ChromeOptions();
      Proxy proxy = new org.openqa.selenium.Proxy();
      proxy.setHttpProxy(proxyStr);
      proxy.setSslProxy(proxyStr);
      proxy.setFtpProxy(proxyStr);
      try
      {
          String content = new String(Files.readAllBytes(Paths.get(WORK_DIR + "\\background.tpl")));
          String [] list = proxyStr.split(":");
          content = content.replace("%%HOST%%", list[0]);
          content = content.replace("%%USERNAME%%", Config.getString("socksLogin"));
          content = content.replace("%%PASSWORD%%", Config.getString("socksPassword"));
          PrintStream out = new PrintStream(new FileOutputStream(WORK_DIR + "\\background.js"));
          out.print(content);
          out.close();
          ArrayList<File> filesToAdd = new ArrayList<File>();
          filesToAdd.add(new File(WORK_DIR + "\\background.js"));
          filesToAdd.add(new File(WORK_DIR + "\\manifest.json"));
          ByteArrayOutputStream bo = new ByteArrayOutputStream();
          ZipOutputStream zipOut= new ZipOutputStream(bo);
          for(File xlsFile:filesToAdd){
              if(!xlsFile.isFile())continue;
              ZipEntry zipEntry = new ZipEntry(xlsFile.getName());
              zipOut.putNextEntry(zipEntry);
              zipOut.write(IOUtils.toByteArray(new FileInputStream(xlsFile)));
              zipOut.closeEntry();
          }
          zipOut.close();

          FileOutputStream fos = null;
          try
          {
              fos = new FileOutputStream(new File(WORK_DIR + "\\eclipse-workspace.zip"));
              fos.write(bo.toByteArray());
          }
          catch(Exception e)
          {
              FERR("zip write failed " + e.getStackTrace());
          }
          finally
          {
              if (fos != null)
                fos.close();
          }
          bo.close();
      }
      catch (IOException e)
      {
          
      }      
      
      option.addExtensions(new File(ECLIPSE_WORKSPACE_PATH));
      option.setCapability(CapabilityType.PROXY, proxy);
      option.addArguments("--window-size=1200,800");
      option.setPageLoadStrategy(PageLoadStrategy.NONE);
      driver = new ChromeDriver(option);
      driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
      driver.manage().deleteAllCookies();
      FLOG("Try open yandex");
      try{
          openUrl(YANDEX_RU);
      }
      catch(Exception e)
      {
          FLOG("Proxy check request timeout");
          driver.quit();          
          return false;
      }
      sleepRandom(1, 7);
      FLOG("Try to fill text");
      if (driver.getPageSource().contains("ERR_PROXY_CONNECTION_FAILED"))
      {
          FLOG("proxyCheck: proxy does not work");
          driver.quit();
          return false;
      }
      try 
      {
          if (!input(By.name("text"), test_key))
          {
              FLOG("proxyCheck: not yandex element found");
              driver.quit();
              return false;
          }    
      }
      catch (Exception e)
      {
          FLOG("proxyCheck: failed to find element, exception");
          driver.quit();
          return false;
      }
      driver.quit();
      return true;
    }
    void openUrl(String url)
    {
        driver.get(url);
    }   

    void sleepRandom(long from, long to)
    {
      long time = 1000 * from + random(1000 * (int)(to - from));
      FLOG("sleepRandom " + time);    
      sleep(time);    
    }
    
    boolean input(By what, String val)
    {
        WebElement elem = driver.findElement(what);
        if (elem == null)
          return false;     
        sendKeys(elem, val);
        return true;
    }
    
    boolean pushButton(By what, CharSequence key)
    {
        if (what == null)
        {
            FLOG("pushButton: what is null");
            return false;
        }
        WebElement elem = driver.findElement(what);
        if (elem == null)
        {
          FLOG("pushButton: elem not found " + what.toString());
          return false;
        }
        FLOG("pushButton: ENTER");
        elem.sendKeys(key);
        return true;
    }
    
    public void sendKeys(WebElement elem, String text)
    {
        if (elem == null)
            return;
        String[] arr = text.split("");
        FLOG("sendKeys: " + text);
        for (String letter : arr)
        {
            elem.sendKeys(letter);
            sleep(250 + random(250));
        }
    }



public void sleep(long ms)
{
    try 
    {
      Thread.sleep(ms);
    } catch (InterruptedException e) 
    {
      e.printStackTrace();
    }
}

public void scroll(int x, int y)
{
  ((JavascriptExecutor)driver).executeScript("window.scrollBy(" + x + ","
          + y + ");");
}

public void scrollWithOffset(WebElement webElement, int x, int y) 
{
  if(webElement == null)
  {
      FLOG("scrollWithOffset: element not found");
      return;
  }
    String code = "window.scroll(" + (webElement.getLocation().x + x) + ","
                                 + (webElement.getLocation().y + y) + ");";
 
    ((JavascriptExecutor)driver).executeScript(code, webElement, x, y);
 
}

public List <WebElement> findAllLinks(By by, String filter)
{
  List<WebElement> links = driver.findElements(by);
  FLOG("findAllLinks: cc all = " + links.size() + ", filter: " + filter);
  if (links.isEmpty())
    return null;
  if (filter != null)
      filter = filter.toLowerCase();
  List<WebElement> ret = new ArrayList<WebElement>();
  for(WebElement link : links)
  { 
    if (filter == null || filter.isEmpty() ||
        (link.getAttribute("href") != null && link.getAttribute("href").toLowerCase().contains(filter)))
      ret.add(link);
  }
  return ret;
}

public boolean findNextPage(By by, String filter)
{
  lastPage++;
  List<WebElement> links = findAllLinks(by, filter + lastPage);
  if (lastPage > MAXIMUM_SEARCH_PAGE)
  {
      FLOG("Page is " + lastPage +", link not found");
      return false;
  }
  if (links == null || links.isEmpty() || links.size() > 2)
  {
    if (links != null)
    {
      FLOG("links count error " + links.size());
      for(WebElement w: links)
      {
        FLOG("links count error " + w.getAttribute("href"));
      }
    }
    return false;
  }  
  scrollWithOffset(links.get(0), 1, 100);
  moveMouseElement(links.get(0), 3000);
  links.get(0).click();
  //openUrl(links.get(0).getAttribute("href"));  
  return true;
}

public void clickRandomLink(String filter)
{
  try
  {
      List<WebElement> links = driver.findElements(By.tagName("a"));
      System.out.append("links = " + links.size() + "\n");
      if (links.isEmpty())
          return;
      for(WebElement link : links)
      {
          if (filter.length() > 0 && !link.getAttribute("href").contains(filter))
          {
              links.remove(link);
              continue;
          }
          System.out.append("LINK: "+ link.getAttribute("href") + "\n");
      }
      WebElement clickLink = getRandomVisibleElement(links);
      System.out.append("clickLink: "+ clickLink + "\n");
      if (clickLink != null)
      {
          moveMouseElement(clickLink, 2000);    
          clickLink.click();
          sleep(3000 + random(2000));
      }
      else
      {
          System.out.append("clickLink: failed to found\n");
          throw(new Exception());
      }
  }
  catch(Exception e)
  {
      System.out.append("randomClickFailed "+e.getLocalizedMessage() + "\n");
      return;
  }
}

public void scrollDecl(int down, int up)
{   
  try {
      Robot robot = new Robot();
      int steps = 2 + random(5);
      float disp = down / steps;
      for (int i = 0; i < steps && down > 0; i++)
      {
          int kal = Math.max(1, random(Math.round(disp)));
          robot.mouseWheel(kal);
          moveMouseRandom(1000);
          steps -= kal;
      }
      if (steps > 0)
      {
          robot.mouseWheel(steps);
          moveMouseRandom(1000);
      }
      robot.mouseWheel(-up);
      moveMouseRandom(1000);
  }
  catch(Exception e)
  {
      System.out.append("robot failedo\n");
      return;
  }
}

public void openSearch(String url, String key, String myUrl)
{
}

public WebElement getRandomVisibleElement(List<WebElement> list)
{
    if (list == null)
    {
        FLOG("getRandomVisibleElement: list not found");
        return null;
    }
    if (list.isEmpty())
    {
        FLOG("getRandomVisibleElement: list is empty!");
        return null;
    }
    WebElement ret = null;
    do
    {
        ret = list.get(random(list.size()));
    }
    while(!ret.isDisplayed() || !ret.isEnabled());
    return ret;
}

public void setMouse(int x, int y)
{
  try {
    Robot robot = new Robot();
    robot.mouseMove(x, y);
    mouse.x = x;
    mouse.y = y;
  }  
  catch(Exception e)
  {
      
  }
}

public boolean closeTo(int x, int xe)
{
    if (Math.abs(x - xe) < 5)
        return true;
    return false;
}

public void moveMouseRandom(int travel_time)
{
    moveToMouse(50 + random(800), 50 + random(800), travel_time);
}

public void moveMouseElement(WebElement element,int travel_time)
{
  if (element != null)
    moveToMouse(element.getLocation().x, element.getLocation().y, (int)(travel_time + random(2000) ));
  else
    FLOG("moveMouseElement:element not found");
}

public void moveToMouseLine(int x, int y, int travel_time)
{
  if (x > windowSize.width)
    x = windowSize.width;
  if (y > windowSize.height)
    y = windowSize.height;

  try {
    float travelTime = (float)(travel_time + random(500));
    int dispersion = 1;
    int nx = mouse.x;
    int ny = mouse.y;
    float dx = (x - nx) / travelTime;
    System.out.append("moveToMouseLine from " + mouse.x + ":" + mouse.y + " to " + x + ":" + y+"\n");
    System.out.append("move dx " + dx + "\n");
    float k = (y - ny)/(x - nx);
    float b = y - k * x;
    int sleepTime = 10;
    float dt = 0.01f;
    int i = 0;
    for (float t = 0; i * sleepTime < travelTime; t += dt, i++)
    {
        nx += (int)(dx * t);
        ny = (int)(k * nx + b);
        if (nx < 0)
            nx = 0;
        if (ny < 0)
        {
            ny = 0;
        }
        if (nx > windowSize.width)
            nx = windowSize.width;
        if (ny > windowSize.height)
            ny = windowSize.height;
        setMouse(nx + random(dispersion), ny + random(dispersion));
        sleep(sleepTime);
        if (closeTo(x, nx))
            break;
    }
  }
  catch(Exception e)
  {
      
  } 
}

public void moveToMouseParabola(int x, int y, int travel_time)
{
  if (x > windowSize.width)
        x = windowSize.width;
      if (y > windowSize.height)
        y = windowSize.height;
  try {
    int dispersion = 1;
    int nx = mouse.x;
    int ny = mouse.y;
    int baseY = ny;
    FLOG("moveToMouseParabola from " + mouse.x + ":" + mouse.y + " to " + x + ":" + y+" travel_time " + travel_time);
    long sleepTime = 10;    
    float PI = 3.14f;
    float HALF_PI = PI / 2.f;
    float dx = (x - nx);
    float dy = y - ny;
    float dt = HALF_PI / ((float)travel_time / (float)sleepTime);
    FLOG("dt = " + dt);
    FLOG("dx = " + dx);
    FLOG("dy = " + dy);
    int ticks = 0;
    for (float t = 0; t < HALF_PI; t += dt, ticks++)
    {
        nx += (int)(dt * dx / HALF_PI);
        ny = baseY + (int)(dy * Math.sin(t));
        if (nx < 0)
        {
            nx = 0;
            dx = -dx;
        }
        if (ny < 0)
        {
            ny = 0;
            dy = -dy;
        }
        if (nx > windowSize.width)
            nx = windowSize.width;
        if (ny > windowSize.height)
            ny = windowSize.height;
        setMouse(nx + random(dispersion), ny + random(dispersion));
        sleep(sleepTime);
        if (closeTo(x, nx))
            break;
    }
    FLOG("ticks had " + ticks + " time spent,ms " + ((ticks * sleepTime)));
    setMouse(x + 1 + random(2), y + 1 + random(2));
  }
  catch(Exception e)
  {
      
  } 
}

public void moveToMouse(int x, int y, int travel_time)
{
  int part_count = 5;
  FLOG("moveToMouse: tracks count = " + part_count); 
  for(int i = 0; i < part_count; i++)
  {
      int tx = random(windowSize.width);
      int ty = random(windowSize.height);
      moveToMouseParabola(tx, ty, (int)(travel_time / part_count));
  }
  moveToMouseParabola(x, y, random(500));
}
}
