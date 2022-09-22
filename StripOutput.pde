
class StripOutput
{
  int length = 120;
  int[] buffer;

  int subnet, universe;

  boolean reversed;
  int strip;
  int begin;
  int end;
  int colourByteSize = 4;
  int beginActual, endActual;

  static final int RGBW = 0, GRB = 1;
  int order = RGBW;

  public StripOutput(int subnet, int universe, int length, int strip)
  {
    this.subnet = subnet;
    this.universe = universe;
    this.length = length;
    this.strip = strip;
    buffer = new int[length];
    setOrder(RGBW);
  }

  public void setOrder(int order)
  {
    this.order = order;
    if (order == GRB)
    {
      beginActual = 0;
      endActual = length*3;
      colourByteSize = 3;
    } else
    {
      beginActual = 0;
      endActual = length*4/3-1;
      colourByteSize = 4;
    }
  }

  public void parseDmx()
  {
    byte[] data = artnet.readDmxData(subnet, universe);
    int j = 0;
    for (int i = 0; i < min(data.length, length*3); i+=3)
    {
      color c = color(data[i] & 0xff, data[i+1] & 0xff, data[i+2] & 0xff);
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
    beginActual = colourByteSize == 4 ? begin*4/3 : begin;
    endActual = colourByteSize == 4 ? end*4/3+1 : end;
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

  int getCorrectedColour(int idx)
  {
    if (order == GRB) 
    {
      color bufferColour = buffer[idx-beginActual];

      return color(green(bufferColour), blue(bufferColour), red(bufferColour));
    } else
    {
      int result = 0;
      int offset = 3*((idx-beginActual)/4);

      switch(idx%colourByteSize)
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
}
