import ch.bildspur.artnet.*;
import ch.bildspur.artnet.packets.*;
import ch.bildspur.artnet.events.*;

import com.heroicrobot.controlsynthesis.*;
import com.heroicrobot.dropbit.common.*;
import com.heroicrobot.dropbit.devices.*;
import com.heroicrobot.dropbit.devices.pixelpusher.*;
import com.heroicrobot.dropbit.discovery.*;
import com.heroicrobot.dropbit.registry.*;


import java.util.*;


//Art-Net library object, stores incoming messages
ArtNetClient artnet;


//byte[] dmxData = new byte[512];

DeviceRegistry registry;
TestObserver testObserver;

ArrayList<StripOutput> outputs = new ArrayList(8);

boolean display = true;

void setup()
{
  size(512, 250);
  surface.setLocation(20, 20);
  artnet = new ArtNetClient();
  artnet.start();

  registry = new DeviceRegistry();
  testObserver = new TestObserver();
  registry.addObserver(testObserver);

  //Order:
  //  subnet universe order strip standard/reverse begin end
  String[] args = loadStrings("arguments.txt");
  int i = 0;
  for (String s : args)
  {
    String[] parse = split(s, ' ');
    if (parse.length > 7)
    {
      
      StripOutput newOutput = new StripOutput(int(parse[0]), int(parse[1]), int(parse[3]), int(parse[4]));
      newOutput.setRange(int(parse[6]), int(parse[7]), parse[5].equals("r"));
      outputs.add(newOutput);
    }
  }

  println("Added", outputs.size(), "strips");
}

void draw()
{
  background(0);
  noStroke();
  int y = 0;
  for (StripOutput output : outputs)
  {
    output.parseDmx();
    if (display)
    {
      int x = 0;
      int w = width/(output.length+1);
      int h = height/(outputs.size()+1);
      for (color c : output.buffer)
      {
        fill(red(c), green(c), blue(c));
        rect(x, y, w, h);
        x+= w;
      }
      y+=h;
    }
  }


  sendPixels();
}


void sendPixels()
{
  if (testObserver.hasStrips) 
  {
    registry.startPushing();
    List<Strip> strips = registry.getStrips();
    for (StripOutput output : outputs)
    {
      if (output != null)
      {
        Strip strip = null;
        for (Strip s : strips)
        {
          if (s.getStripNumber() == output.strip) strip = s;
        }
        if (strip != null)
        {
          for (int stripx = output.beginActual; stripx < min(strip.getLength(), output.endActual); stripx++) 
          {
            strip.setPixel(output.getCorrectedColour(stripx), stripx);
          }
        }
      }
    }
  }
}


StripOutput getOutput(int stripIdx)
{
  for (StripOutput output : outputs)
  {
    if (output.strip == stripIdx) return output;
  }
  return null;
}
