// changeDisplaySize.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include "windows.h"
#include <stdlib.h>


bool haveAlready(char ** list, char * new_item)
{
  for(int i = 0; list[i]; i++)
  {
	  if (!lstrcmpA(list[i], new_item))
	    return true;
  }
  return false;
}

bool printModes()
{ 
  DWORD i = 0;
  DWORD j;
  DISPLAY_DEVICE dc;  
  dc.cb = sizeof(dc);

  char ** list = new char*[1000];
  memset(list, 0, sizeof(char*) * 1000);
  int modes = 0;

  while(EnumDisplayDevices(NULL, i, &dc, 0x01) != 0)
  {
    if ((dc.StateFlags & DISPLAY_DEVICE_ACTIVE) && !(dc.StateFlags & DISPLAY_DEVICE_MIRRORING_DRIVER))
    {
      DEVMODE dm;
      j = 0;
      while(EnumDisplaySettings(dc.DeviceName, j, &dm) != 0)
      {
        char s[200];
        sprintf(s, "%dx%d", dm.dmPelsWidth, dm.dmPelsHeight);
        if (!haveAlready(list, s))
        {
          list[modes++] = strdup(s);
        }		
        ++j;
      }
    }
    ++i;
  }

  if (i == 0)
    return false;

  for(int i = 0; list[i]; i++)
  {
    printf("%s\n", list[i]);
  }

  return true;
}

int main(int argc, char * argv[])
{
	if (argc < 3)
	{
		printModes();
		return -1;
	}
  for(int i = 0; i < argc; i++)
  {
    printf("[%s]\n", argv[i]);
  }
	DEVMODE dm;
	memset(&dm,0,sizeof(DEVMODE));
	dm.dmSize=sizeof(DEVMODE);
	dm.dmPelsWidth = atoi(argv[1]);
	dm.dmPelsHeight = atoi(argv[2]);
	dm.dmFields=DM_PELSWIDTH|DM_PELSHEIGHT;
	return (ChangeDisplaySettings(&dm,CDS_UPDATEREGISTRY));
}

