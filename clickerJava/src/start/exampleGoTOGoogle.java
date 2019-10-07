package start;


import static org.junit.Assert.*;

import java.awt.Robot;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.text.Position;

import org.junit.After;

import org.junit.Before;

import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;

import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;

import org.openqa.selenium.WebElement;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;


public class exampleGoTOGoogle extends BasicApi
{

    ArrayList<String> keywords = null;
  boolean failIfNoProxy = false;
@Before
public void setUp()
{
  try
  {
    Config.Init(WORK_DIR + "\\config.txt");
    Common.Init(WORK_DIR + "\\" + logFileName);
    Common.RESET_LOG();
    FLOG("logfile = " + Config.getString("logFileName"));
    System.setProperty("webdriver.chrome.driver", Config.getString("driverPath"));
    
    windowSize = new Dimension(1000, 800); 
    
    socksTestKeys = Common.readFile(WORK_DIR + "\\" + Config.getString("testKeys"));
    if (socksTestKeys.isEmpty())
      FLOG("No socksTestKeys loaded!");
    else 
      FLOG("Loaded socksTestKeys = " + socksTestKeys.size());
    userAgents = Common.readFile(WORK_DIR + "\\" + Config.getString("userAgents"));
    if (userAgents.isEmpty())
      FLOG("No userAgents loaded!");
    else 
        FLOG("Loaded userAgents = " + userAgents.size());
    
    ArrayList<String> availModes = new ArrayList<String>();
    Runtime runtime = Runtime.getRuntime();
    try {
       String cmd = WORK_DIR + "\\changeDisplaySize.exe";
       FLOG(cmd);
       Process proc = runtime.exec(cmd);
       InputStream is = proc.getInputStream();
       BufferedReader in = new BufferedReader(new InputStreamReader(is));
       String line = null;
       while ((line = in.readLine()) != null) 
       {
         availModes.add(line);
       }
    } catch (IOException e) {
       e.printStackTrace();
    }
    ArrayList<String> deskTop = Common.readFile(WORK_DIR + "\\" + Config.getString("screenRes"));
    if (deskTop.isEmpty())
      FLOG("No deskTopRes loaded!");
    else 
    {
      FLOG("Loaded deskTopRes = " + deskTop.size());
      deskTopRes = new ArrayList<ResolutionProb>();
      for(String line: deskTop)
      {
          String [] list = line.split("\"");
          if (availModes.indexOf(list[1]) < 0)
          {
            FLOG("Mode " + list[1] + " not supported");
            continue;
          } 
          list = list[1].split("x");          
          int x = Integer.parseInt(list[0]);
          int y = Integer.parseInt(list[1]);
          Dimension p = new Dimension(x, y);
          ResolutionProb rp = new ResolutionProb();
          rp.res = p;
          list = line.split(",");
          rp.probab = Float.parseFloat(list[1]);
          fullProb += rp.probab; 
          deskTopRes.add(rp);
      }   
    }
    
    socks = Common.readFile(WORK_DIR + "\\" + Config.getString("proxy"));
    failIfNoProxy = Config.getBool("failIfNoProxy");
    if (!parseBehFile(WORK_DIR + "\\" + Config.getString("behTree")))
        return;
    keywords = Common.readFile(WORK_DIR + "\\" + Config.getString("keys"));
    if (keywords == null || keywords.isEmpty())
    {
        return;
    }
    
    timeToSurf = Config.getInt("timeToSurf") * 1000;
    countSurf = Config.getInt("countSurf");
  }
  catch(Exception e)
  {
    FERR("testGoogle:" + e.getStackTrace());
  }
}
    


public enum Action {
    MOUSE_MOVE_RANDOM ("MouseMoveRandom"), //+ 
    MOUSE_MOVE_TO ("MouseMoveTo"), 
    MOUSE_MOVE_TO_ELEMENT ("MouseMoveToElement"), //+
    OPEN_URL ("OpenUrl"), //+
    CLICK_ELEM ("ClickElement"), //+
    SCROLL_XY ("ScrollXY"), //+
    SCROLL_ELEM ("ScrollElem"), //+
    SCROLL_DECL ("ScrollDecl"), //+
    PAUSE_FROM_TO ("Pause"), //+
    INPUT_TEXT ("InputText"), //+
    PUSH_BUTTON ("PushButton"), //+
    GET_ALL_LINKS ("GetAllLinks"), //+
    SELECT_VISIBLE_RANDOM_LINK ("SelectVisibleRandomLink"), //+
    SWITCH_WINDOW ("SwitchWindow"), //+
    WHILE ("While"), //+
    BREAK ("Break"), //+
    END ("End"), //+
    NEXT_PAGE ("NextPage"),
    QUIT ("Quit");//+
    
    String name;
    Action(String name)
    {
        this.name = name;
    }
    boolean isName(String name)
    {
        return this.name.equals(name);
    }
}

class Node
{
    Action todo;
    String textParam;
    int x;
    int y;
    int z;
    By by;
    int start;
    int end;
}

ArrayList<Node> behTree = new ArrayList<Node>();

public enum Bytype
{
    UNDEF ("undef"),
    NAME ("name"),
    CLASS_NAME ("className"),
    CSS_SELECTOR ("cssSelector"),
    ID ("id"),
    LINK_TEXT ("linkText"),
    PARTIAL_LINK_TEXT ("partialLinkText"),
    TAG_NAME ("tagName"),
    XPATH ("xpath")
    ;
    String name;
    Bytype(String name)
    {
        this.name = name;
    }
    boolean isName(String name)
    {
        return this.name.equals(name);
    }
}

By parseBy(String what, String arg)
{
    Bytype my = Bytype.UNDEF;
    for (Bytype b : Bytype.values()) 
    {
        if (b.isName(what))
        {
          my = b;
          break;
        }
    }
    
    if (my == Bytype.UNDEF)
        return null;
    
  By ret = null;    
  switch(my)
  {
    case CLASS_NAME:
        ret = By.className(arg);
    break;
    case CSS_SELECTOR:
        ret = By.cssSelector(arg);
    break;
    case ID:
        ret = By.id(arg);
    break;
    case LINK_TEXT:
        ret = By.linkText(arg);
    break;
    case NAME:
        ret = By.name(arg);
    break;
    case PARTIAL_LINK_TEXT:
        ret = By.partialLinkText(arg);
    break;
    case TAG_NAME:
        ret = By.tagName(arg);
    break;
    case XPATH:
        ret = By.xpath(arg);
    break;
  }
    return ret; 
}

boolean parseBehFile(String name)
{
    ArrayList<String> beh = Common.readFile(name);
    if (beh == null)
    {
        FLOG("Failed to read behfile " + name);
        return false;
    }
    int lastStart = -1;
    for(String line: beh)
    {
        String [] args = line.split("\\s+");
        if (args[0].startsWith("//"))
            continue;
        for (Action p : Action.values()) 
        {
            if (p.isName(args[0]))
            {               
                Node n = new Node();
                n.start = lastStart;
                n.end = -1;
                n.todo = p;
                switch(p)
                {
                    case OPEN_URL:
                        n.textParam = args[1];
                    break;
                    case MOUSE_MOVE_RANDOM:
                        n.x = Integer.parseInt(args[1]);
                    break;
                    case MOUSE_MOVE_TO:
                        n.x = Integer.parseInt(args[1]);
                        n.y = Integer.parseInt(args[2]);
                        n.z = Integer.parseInt(args[3]);
                    break;
                    case MOUSE_MOVE_TO_ELEMENT:
                        n.x = Integer.parseInt(args[1]);
                    break;
                    case CLICK_ELEM:
                    break;
                    case SCROLL_XY:
                    case SCROLL_ELEM:
                    case SCROLL_DECL:
                        n.x = Integer.parseInt(args[1]);
                        n.y = Integer.parseInt(args[2]);
                    break;
                    case PAUSE_FROM_TO:
                        n.x = Integer.parseInt(args[1]);
                        n.y = Integer.parseInt(args[2]);
                    break;
                    case INPUT_TEXT:
                        n.by = parseBy(args[1], args[2]);
                        int keyLen = args.length - 3;
                        String [] myArgs = new String [keyLen];
                        System.arraycopy(args, 3, myArgs, 0, keyLen);
                        n.textParam = String.join(" ", myArgs);
                    break;
                    case PUSH_BUTTON:
                        n.by = parseBy(args[1], args[2]); 
                    break;
                    case GET_ALL_LINKS:
                        n.by = parseBy(args[1], args[2]);
                        if (args.length > 3)
                            n.textParam = args[3];
                    break;
                    case SELECT_VISIBLE_RANDOM_LINK:
                    break;
                    case SWITCH_WINDOW:
                    break;
                    case WHILE:
                        lastStart = behTree.size();
                    break;
                    case BREAK:                     
                    break;
                    case END:
                        for(int i = lastStart; i < behTree.size(); i++)
                        {
                            behTree.get(i).end = behTree.size();
                        }
                        lastStart = -1;
                    break;
                    case NEXT_PAGE:
                        n.by = parseBy(args[1], args[2]);
                        if (args.length > 3)
                            n.textParam = args[3];
                    break;
                    case QUIT:
                    break;
                    default:
                        FLOG("UNHANDLED COMMAND: " + args[0]);
                    return false;

                }               
                behTree.add(n);
                break;
            }           
        }
    }
    return true;
}

void dumpBehTree()
{
  for(Node n: behTree)
  {
      FLOG(n.todo.name);
  }
}

void processBehTree()
{
  for(int i = 0; i < behTree.size(); i++)
  {
      Node n = behTree.get(i);
      FLOG(n.todo.name);
      switch(n.todo)
      {
        case OPEN_URL:
            openUrl(n.textParam);
            FLOG("OPEN_URL " + n.textParam);
        break;
        case MOUSE_MOVE_RANDOM:
            moveMouseRandom(n.x);
        break;
        case MOUSE_MOVE_TO:
            moveToMouse(n.x, n.y, n.z);
        break;
        case MOUSE_MOVE_TO_ELEMENT:
            moveMouseElement(lastFoundElement, n.x);
        break;
        case CLICK_ELEM:
            if (lastFoundElement != null && lastFoundElement.isDisplayed() && lastFoundElement.isEnabled())
            {
              FLOG("CLICK_ELEM " + lastFoundElement.getAttribute("href"));
              lastFoundElement.click();
            }
            else
              FLOG("CLICK_ELEM: element not found " + (lastFoundElement != null ? lastFoundElement.toString() : "null"));
        break;
        case SCROLL_XY:
            scroll(n.x, n.y);
        break;
        case SCROLL_ELEM:
            scrollWithOffset(lastFoundElement, n.x, n.y);
        break;
        case SCROLL_DECL:
            scrollDecl(n.x, n.y);
        break;
        case PAUSE_FROM_TO:
            sleepRandom(n.x, n.y);
        break;
        case INPUT_TEXT:
            if (n.textParam.indexOf("%%KEYWORD%%") >= 0)
            {
              String keyWord = keywords.get(random(keywords.size()));
              FLOG("INPUT_TEXT " + keyWord);
                input(n.by, keyWord);
            }
            else
                input(n.by, n.textParam);
        break;
        case PUSH_BUTTON:
            pushButton(n.by, Keys.ENTER);
        break;
        case GET_ALL_LINKS:
          foundLinks = findAllLinks(n.by, n.textParam);
        break;
        case SELECT_VISIBLE_RANDOM_LINK:
          lastFoundElement = getRandomVisibleElement(foundLinks);
        break;
        case SWITCH_WINDOW:
          for(String winHandle : driver.getWindowHandles())
          {
            driver.switchTo().window(winHandle);
          }
        break;
        case WHILE:
        break;
        case BREAK:
            if (lastFoundElement != null)
                i = n.end;
        break;
        case END:
            i = n.start;
        break;
        case NEXT_PAGE:
            if (!findNextPage(n.by, n.textParam))
            {
              FLOG("findNextPage failed...");
              return;
            }
        break;
        case QUIT:
            driver.quit();
        return;
        default:
        break;
      }
  }
}

@Test
public void testGoogle()
{
  try{    
    final long startTime = curTime();
    long newTimeToSurf = timeToSurf / 2 + random(timeToSurf);
    FLOG("Will use time " + newTimeToSurf + " instead of " + timeToSurf);
    timeToSurf = newTimeToSurf;
    int newCountSurf = countSurf / 2 + random(countSurf);
    FLOG("Will use count " + newCountSurf + " instead of " + countSurf);
    countSurf = newCountSurf;
    final long eachTime = timeToSurf / countSurf;
    FLOG("eachTime = " + eachTime);
    FLOG("timeToSurf = " + timeToSurf);
    int i = 0;
    for(; i < countSurf && (startTime + timeToSurf > curTime()); i++)
    {
      final long cycleStartTime = curTime();
        FLOG("-----------------------------------Cycle " + i);
        setupProxy();
        if (failIfNoProxy && lastProxy == null)
      {
        FLOG("failIfNoProxy");
        return ;
      }
        setupDriver();  
        lastPage = 0;
        processBehTree();
        FLOG("-----------------------------------end");
        long usedTime = curTime() - cycleStartTime;
        FLOG("usedTime =" + usedTime);
        long sleepTime = (eachTime - usedTime);
        FLOG("sleepTime =" + sleepTime);
        long halfSleepTime = sleepTime / 2;
        FLOG("halfSleepTime =" + halfSleepTime);
        long randomEach = random(eachTime);
        FLOG("randomEach =" + randomEach);
        long realSleep =  halfSleepTime + randomEach;
        FLOG("realSleep =" + realSleep);
        if (realSleep > 0)
          sleep(realSleep);
    }
    FLOG("This is the End!");
    FLOG("Took time " + (curTime() - startTime) + " instead of " + timeToSurf);
    FLOG("Iterations " + i + " instead of " + countSurf);
  }
  catch(Exception e)
  {
    FERR("testGoogle:" + e.getMessage());
    e.printStackTrace();
  }
}
@After
public void tearDown()
{
  try{
    if (driver != null)
      driver.quit();
  }catch(Exception e)
  {
    FERR("tearDown:" + e.getStackTrace());
  }
}
}
