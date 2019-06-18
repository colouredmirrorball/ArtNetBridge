import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

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

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class ArtNetBridge extends PApplet {
















//Art-Net library object, stores incoming messages
ArtNetClient artnet;


//byte[] dmxData = new byte[512];

DeviceRegistry registry;
TestObserver testObserver;

ArrayList<StripOutput> outputs = new ArrayList(8);

boolean display = true;

public void setup()
{
  
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
      
      StripOutput newOutput = new StripOutput(PApplet.parseInt(parse[0]), PApplet.parseInt(parse[1]), PApplet.parseInt(parse[3]), PApplet.parseInt(parse[4]));
      newOutput.setRange(PApplet.parseInt(parse[6]), PApplet.parseInt(parse[7]), parse[5].equals("r"));
      outputs.add(newOutput);
    }
  }

  println("Added", outputs.size(), "strips");
}

public void draw()
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
      for (int c : output.buffer)
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


public void sendPixels()
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


public StripOutput getOutput(int stripIdx)
{
  for (StripOutput output : outputs)
  {
    if (output.strip == stripIdx) return output;
  }
  return null;
}
class StripOutput
{
  int length = 120;
  int[] buffer;

  int subnet, universe;

  boolean reversed;
  int strip;
  int begin;
  int end;

  int beginActual, endActual;

  static final int RGBW = 0;
  int order = RGBW;

  public StripOutput(int subnet, int universe, int length, int strip)
  {
    this.subnet = subnet;
    this.universe = universe;
    this.length = length;
    this.strip = strip;
    buffer = new int[length];
    beginActual = 0;
    endActual = length*4/3-1;
  }

  public void parseDmx()
  {
    byte[] data = artnet.readDmxData(subnet, universe);
    int j = 0;
    for (int i = 0; i < min(data.length, length*3)-2; i+=3)
    {
      int c = color(data[i] & 0xff, data[i+1] & 0xff, data[i+2] & 0xff);
      int w = 0;
      if (saturation(c) < 25 ) w = (int) ((brightness(c)/255)*(255-saturation(c)*10));
      buffer[j++] = color(red(c), green(c), blue(c), w);
    }
    if (reversed) buffer = reverse(buffer);
  }

  public void setRange(int begin, int end, boolean reversed)
  {
    this.begin= begin;
    this.end = end;
    this.reversed = reversed;
    beginActual = begin*4/3;
    endActual = end*4/3+1;
    println("beginActual", beginActual, "endActual", endActual);
  }


  /*
  public void scrape()
   {
   if (testObserver.hasStrips) 
   {
   registry.startPushing();
   List<Strip> strips = registry.getStrips();
   
   for (Strip strip : strips) 
   {
   for (int stripx = beginActual; stripx < min(strip.getLength(), endActual); stripx++)  
   {
   strip.setPixel(getCorrectedColour(stripx), stripx);
   }
   }
   }
   }
   */

  public int getCorrectedColour(int idx)
  {
    int result = 0;
    int offset = 3*((idx-beginActual)/4);
    switch(idx%4)
    {
    case 0 : 
      result=(((int)red(buffer[offset]) & 0xff) << 16) | (((int)blue(buffer[offset]) & 0xff) << 8) | ((int)green(buffer[offset]) & 0xff);
      break;
    case 1:
      result=  (((int)green(buffer[offset+1]) & 0xff) << 16) | (((int)red(buffer[offset+1]) & 0xff) << 8) | ((int)alpha(buffer[offset]) & 0xff);
      break;
    case 2:
      result= (((int)alpha(buffer[offset+1]) & 0xff) << 16) | (((int)green(buffer[offset+2]) & 0xff) << 8) | ((int)blue(buffer[offset+1]) & 0xff);
      break;
    default:
      result=(((int)blue(buffer[offset+2])& 0xff) << 16 ) | (((int)alpha(buffer[offset+2]) & 0xff) << 8) | ((int)red(buffer[offset+2]) & 0xff);
    }
    return result;
  }
}
class TestObserver implements Observer {
  public boolean hasStrips = false;
  public void update(Observable registry, Object updatedDevice) {
    println("Registry changed!");
    if (updatedDevice != null) {
      println("Device change: " + updatedDevice);
    }
    this.hasStrips = true;
  }
};
  public void settings() {  size(512, 250); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "ArtNetBridge" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
