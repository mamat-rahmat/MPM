public class Particle
{
  public float x;
  public float y;
  public float u;
  public float v;
  public float gu;
  public float gv;
  public float T00;
  public float T01;
  public float T11;
  public int cx;
  public int cy;
  public float[] px = new float[3];
  public float[] py = new float[3];
  public float[] gx = new float[3];
  public float[] gy = new float[3];
  public boolean broken;
  
  public Particle(float x, float y, float u, float v)
  {
    this.x = x;
    this.y = y;
    this.u = u;
    this.v = v;
  }
}
