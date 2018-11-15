import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.awt.image.ImageObserver;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.awt.event.KeyListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;
import javax.swing.JApplet;


public class Liquid extends JApplet implements Runnable, MouseListener, MouseMotionListener, KeyListener
{
    Particle[] particles;
    int gsizeX;
    int gsizeY;
    int mul;
    Node[][] grid;
    ArrayList<Node> active;
    Thread animationThread;
    Image backBuffer;
    Graphics2D backBufferGraphics;
    boolean pressed;
    boolean pressedprev;
    int mx;
    int my;
    int mxprev;
    int myprev;
    String[] settingNames;
    float[] settings;
    float[] maxValues;
    int[] sliderValues;
    float[] max;
    Color[] colors;
    int numSliders;
    int sliderWidth;
    int sliderHeight;
    int mode;
    boolean space;
    
    public Liquid() {
        this.gsizeX = 163;
        this.gsizeY = 102;
        this.mul = 6;
        this.grid = new Node[this.gsizeX][this.gsizeY];
        this.active = new ArrayList<Node>();
        this.my = 100;
        this.settingNames = new String[] { "Density", "Stiffness", "Bulk Viscosity", "Elasticity", "Viscosity", "Yield Rate", "Gravity", "Smoothing" };
        this.settings = new float[] { 2.0f, 1.0f, 1.0f, 0.0f, 0.1f, 0.0f, 0.05f, 0.0f };
        this.maxValues = new float[] { 10.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.05f, 1.0f };
        this.mode = -1;
    }
    
    public void init() {
        this.setSize(960, 600);
        this.backBuffer = this.createImage(960, 600);
        this.backBufferGraphics = (Graphics2D)this.backBuffer.getGraphics();
        for (int i = 0; i < this.gsizeX; ++i) {
            for (int j = 0; j < this.gsizeY; ++j) {
                this.grid[i][j] = new Node();
            }
        }
        (this.animationThread = new Thread(this)).start();
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addKeyListener(this);
        int n = 0;
        this.particles = new Particle[10000];
        float mul2 = 1.0f / (float)Math.sqrt(this.settings[0]);
        if (mul2 > 0.72f) {
            mul2 = 0.72f;
        }
        for (int k = 0; k < 100; ++k) {
            for (int l = 0; l < 100; ++l) {
                Particle p = new Particle((float)((l + Math.random()) * mul2) + 4.0f, (float)((k + Math.random()) * mul2) + 4.0f, 0.0f, 0.0f);
                this.particles[n++] = p;
            }
        }
        this.numSliders = this.settings.length;
        this.sliderWidth = 959 / this.numSliders;
        this.colors = new Color[this.numSliders];
        this.sliderValues = new int[this.numSliders];
        for (int m = 0; m < this.numSliders; ++m) {
            this.sliderValues[m] = (int)(this.settings[m] / this.maxValues[m] * this.sliderWidth);
        }
        for (int m = 0; m < this.numSliders; ++m) {
            this.colors[m] = Color.getHSBColor((float)Math.random(), 0.6f, 1.0f);
        }
    }
    
    public void paint(Graphics g) {
        this.simulate();
        this.backBufferGraphics.clearRect(0, 0, 960, 600);
        this.backBufferGraphics.setColor(Color.BLUE);
        for (int i = 0; i < 10000; ++i) {
            Particle p = this.particles[i];
            this.backBufferGraphics.drawLine((int)(this.mul * (p.x - 1.0f)), (int)(this.mul * p.y), (int)(this.mul * (p.x - 1.0f - p.gu)), (int)(this.mul * (p.y - p.gv)));
        }
        this.backBufferGraphics.clearRect(0, 0, 960, 10);
        for (int i = 0; i < this.numSliders; ++i) {
            this.backBufferGraphics.setColor(this.colors[i]);
            this.backBufferGraphics.fillRect(i * this.sliderWidth, 0, this.sliderValues[i], 10);
            this.backBufferGraphics.setColor(Color.LIGHT_GRAY);
            this.backBufferGraphics.drawRect(i * this.sliderWidth, 0, this.sliderWidth, 10);
            this.backBufferGraphics.setColor(Color.BLACK);
            this.backBufferGraphics.drawString(this.settingNames[i], i * this.sliderWidth + 5, 25);
        }
        if (this.my <= 10) {
            this.backBufferGraphics.setColor(Color.black);
            int i = this.mx / this.sliderWidth;
            this.backBufferGraphics.drawRect(i * this.sliderWidth, 0, this.sliderWidth, 10);
        }
        else if (this.mode > -1) {
            this.backBufferGraphics.setColor(Color.black);
            this.backBufferGraphics.drawRect(this.mode * this.sliderWidth, 0, this.sliderWidth, 10);
        }
        g.drawImage(this.backBuffer, 0, 0, this);
    }
    
    public void run() {
        while (true) {
            this.repaint();
        }
    }
    
    public void simulate() {
        if (this.space) {
            this.settings[0] = Math.min(10.0f, this.settings[0] + 0.05f);
        }
        boolean drag = false;
        float mdx = 0.0f;
        float mdy = 0.0f;
        if (this.pressed && this.pressedprev) {
            if (this.mode == -1) {
                drag = true;
                mdx = (this.mx - this.mxprev) / this.mul;
                mdy = (this.my - this.myprev) / this.mul;
            }
            else {
                int sx = this.mode * this.sliderWidth;
                int v = this.mx - sx;
                if (v < 0) {
                    v = 0;
                }
                else if (v > this.sliderWidth) {
                    v = this.sliderWidth;
                }
                this.sliderValues[this.mode] = v;
                this.settings[this.mode] = this.sliderValues[this.mode] / this.sliderWidth * this.maxValues[this.mode];
            }
        }
        this.pressedprev = this.pressed;
        this.mxprev = this.mx;
        this.myprev = this.my;
        for (Node n : this.active) {
            n.ay = 0.0f;
            n.ax = 0.0f;
            n.v = 0.0f;
            n.u = 0.0f;
            n.gy = 0.0f;
            n.gx = 0.0f;
            n.m = 0.0f;
            n.active = false;
        }
        this.active.clear();
        for (int a = 0; a < this.particles.length; ++a) {
            Particle p = this.particles[a];
            p.cx = (int)(p.x - 0.5f);
            p.cy = (int)(p.y - 0.5f);
            float x = p.cx - p.x;
            p.px[0] = 0.5f * x * x + 1.5f * x + 1.125f;
            p.gx[0] = x + 1.5f;
            ++x;
            p.px[1] = -x * x + 0.75f;
            p.gx[1] = -2.0f * x;
            ++x;
            p.px[2] = 0.5f * x * x - 1.5f * x + 1.125f;
            p.gx[2] = x - 1.5f;
            float y = p.cy - p.y;
            p.py[0] = 0.5f * y * y + 1.5f * y + 1.125f;
            p.gy[0] = y + 1.5f;
            ++y;
            p.py[1] = -y * y + 0.75f;
            p.gy[1] = -2.0f * y;
            ++y;
            p.py[2] = 0.5f * y * y - 1.5f * y + 1.125f;
            p.gy[2] = y - 1.5f;
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 3; ++j) {
                    int cxi = p.cx + i;
                    int cyj = p.cy + j;
                    Node n = this.grid[cxi][cyj];
                    if (!n.active) {
                        this.active.add(n);
                        n.active = true;
                    }
                    float phi = p.px[i] * p.py[j];
                    n.m += phi;
                    float dx = p.gx[i] * p.py[j];
                    float dy = p.px[i] * p.gy[j];
                    n.gx += dx;
                    n.gy += dy;
                    n.u += phi * p.u;
                    n.v += phi * p.v;
                }
            }
        }
        Iterator<Node> iterator2 = this.active.iterator();
        while (iterator2.hasNext()) {
            Node n = iterator2.next();
            if (n.m > 0.0f) {
                n.u /= n.m;
                n.v /= n.m;
            }
        }
        for (int a = 0; a < this.particles.length; ++a) {
            Particle p = this.particles[a];
            float dudx = 0.0f;
            float dudy = 0.0f;
            float dvdx = 0.0f;
            float dvdy = 0.0f;
            for (int k = 0; k < 3; ++k) {
                for (int l = 0; l < 3; ++l) {
                    Node n = this.grid[p.cx + k][p.cy + l];
                    float gx = p.gx[k] * p.py[l];
                    float gy = p.px[k] * p.gy[l];
                    dudx += n.u * gx;
                    dudy += n.u * gy;
                    dvdx += n.v * gx;
                    dvdy += n.v * gy;
                }
            }
            float w1 = dudy - dvdx;
            float wT0 = w1 * p.T01;
            float wT2 = 0.5f * w1 * (p.T00 - p.T11);
            float D00 = dudx;
            float D2 = 0.5f * (dudy + dvdx);
            float D3 = dvdy;
            float trace = 0.5f * (D00 + D3);
            D00 -= trace;
            D3 -= trace;
            p.T00 += -wT0 + D00 - this.settings[5] * p.T00;
            p.T01 += wT2 + D2 - this.settings[5] * p.T01;
            p.T11 += wT0 + D3 - this.settings[5] * p.T11;
            float norm = p.T00 * p.T00 + 2.0f * p.T01 * p.T01 + p.T11 * p.T11;
            if (this.mode > -1 || norm > 5.0f) {
                float t00 = 0.0f;
                p.T11 = t00;
                p.T01 = t00;
                p.T00 = t00;
            }
            int cx = (int)p.x;
            int cy = (int)p.y;
            int cxi2 = cx + 1;
            int cyi = cy + 1;
            float p2 = this.grid[cx][cy].m;
            float x2 = this.grid[cx][cy].gx;
            float y2 = this.grid[cx][cy].gy;
            float p3 = this.grid[cx][cyi].m;
            float x3 = this.grid[cx][cyi].gx;
            float y3 = this.grid[cx][cyi].gy;
            float p4 = this.grid[cxi2][cy].m;
            float x4 = this.grid[cxi2][cy].gx;
            float y4 = this.grid[cxi2][cy].gy;
            float p5 = this.grid[cxi2][cyi].m;
            float x5 = this.grid[cxi2][cyi].gx;
            float y5 = this.grid[cxi2][cyi].gy;
            float pdx = p4 - p2;
            float pdy = p3 - p2;
            float C20 = 3.0f * pdx - x4 - 2.0f * x2;
            float C21 = 3.0f * pdy - y3 - 2.0f * y2;
            float C22 = -2.0f * pdx + x4 + x2;
            float C23 = -2.0f * pdy + y3 + y2;
            float csum1 = p2 + y2 + C21 + C23;
            float csum2 = p2 + x2 + C20 + C22;
            float C24 = 3.0f * p5 - 2.0f * x3 - x5 - 3.0f * csum1 - C20;
            float C25 = -2.0f * p5 + x3 + x5 + 2.0f * csum1 - C22;
            float C26 = 3.0f * p5 - 2.0f * y4 - y5 - 3.0f * csum2 - C21;
            float C27 = -2.0f * p5 + y4 + y5 + 2.0f * csum2 - C23;
            float C28 = x3 - C27 - C26 - x2;
            float u = p.x - cx;
            float u2 = u * u;
            float u3 = u * u2;
            float v2 = p.y - cy;
            float v3 = v2 * v2;
            float v4 = v2 * v3;
            float density = p2 + x2 * u + y2 * v2 + C20 * u2 + C21 * v3 + C22 * u3 + C23 * v4 + C24 * u2 * v2 + C25 * u3 * v2 + C26 * u * v3 + C27 * u * v4 + C28 * u * v2;
            float pressure = this.settings[1] / Math.max(1.0f, this.settings[0]) * (density - this.settings[0]);
            if (pressure > 2.0f) {
                pressure = 2.0f;
            }
            float fx = 0.0f;
            float fy = 0.0f;
            if (p.x < 3.0f) {
                fx += 3.0f - p.x;
            }
            else if (p.x > this.gsizeX - 4) {
                fx += this.gsizeX - 4 - p.x;
            }
            if (p.y < 3.0f) {
                fy += 3.0f - p.y;
            }
            else if (p.y > this.gsizeY - 4) {
                fy += this.gsizeY - 4 - p.y;
            }
            trace *= this.settings[1];
            float T00 = this.settings[3] * p.T00 + this.settings[4] * D00 + pressure + this.settings[2] * trace;
            float T2 = this.settings[3] * p.T01 + this.settings[4] * D2;
            float T3 = this.settings[3] * p.T11 + this.settings[4] * D3 + pressure + this.settings[2] * trace;
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 3; ++j) {
                    Node n = this.grid[p.cx + i][p.cy + j];
                    float phi = p.px[i] * p.py[j];
                    float dx = p.gx[i] * p.py[j];
                    float dy = p.px[i] * p.gy[j];
                    n.ax += -(dx * T00 + dy * T2) + fx * phi;
                    n.ay += -(dx * T2 + dy * T3) + fy * phi;
                }
            }
        }
        Iterator<Node> iterator3 = this.active.iterator();
        while (iterator3.hasNext()) {
            Node n = iterator3.next();
            if (n.m > 0.0f) {
                n.ax /= n.m;
                n.ay /= n.m;
                n.u = 0.0f;
                n.v = 0.0f;
            }
        }
        for (int a = 0; a < this.particles.length; ++a) {
            Particle p = this.particles[a];
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 3; ++j) {
                    Node n = this.grid[p.cx + i][p.cy + j];
                    float phi = p.px[i] * p.py[j];
                    p.u += phi * n.ax;
                    p.v += phi * n.ay;
                }
            }
            p.v += this.settings[6];
            if (drag) {
                float vx = Math.abs(p.x - this.mx / this.mul);
                float vy = Math.abs(p.y - this.my / this.mul);
                if (vx < 10.0f && vy < 10.0f) {
                    float weight = (1.0f - vx / 10.0f) * (1.0f - vy / 10.0f);
                    p.u += weight * (mdx - p.u);
                    p.v += weight * (mdy - p.v);
                }
            }
            float x = p.x + p.u;
            float y = p.y + p.v;
            if (x < 2.0f) {
                p.u += 2.0f - x + (float)Math.random() * 0.01f;
            }
            else if (x > this.gsizeX - 3) {
                p.u += this.gsizeX - 3 - x - (float)Math.random() * 0.01f;
            }
            if (y < 2.0f) {
                p.v += 2.0f - y + (float)Math.random() * 0.01f;
            }
            else if (y > this.gsizeY - 3) {
                p.v += this.gsizeY - 3 - y - (float)Math.random() * 0.01f;
            }
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 3; ++j) {
                    Node n = this.grid[p.cx + i][p.cy + j];
                    float phi = p.px[i] * p.py[j];
                    n.u += phi * p.u;
                    n.v += phi * p.v;
                }
            }
        }
        Iterator<Node> iterator4 = this.active.iterator();
        while (iterator4.hasNext()) {
            Node n = iterator4.next();
            if (n.m > 0.0f) {
                n.u /= n.m;
                n.v /= n.m;
            }
        }
        for (int a = 0; a < this.particles.length; ++a) {
            Particle p = this.particles[a];
            float gu = 0.0f;
            float gv = 0.0f;
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 3; ++j) {
                    Node n = this.grid[p.cx + i][p.cy + j];
                    float phi = p.px[i] * p.py[j];
                    gu += phi * n.u;
                    gv += phi * n.v;
                }
            }
            p.gu = gu;
            p.gv = gv;
            p.x += gu;
            p.y += gv;
            p.u += this.settings[7] * (gu - p.u);
            p.v += this.settings[7] * (gv - p.v);
        }
    }
    
    public void keyPressed(KeyEvent arg0) {
    }
    
    public void keyReleased(KeyEvent arg0) {
    }
    
    public void keyTyped(KeyEvent arg0) {
    }
    
    public void mouseDragged(MouseEvent arg0) {
        this.pressed = true;
        this.mx = arg0.getX();
        this.my = arg0.getY();
    }
    
    public void mouseMoved(MouseEvent arg0) {
        this.mx = arg0.getX();
        this.my = arg0.getY();
    }
    
    public void mouseClicked(MouseEvent arg0) {
        this.mx = arg0.getX();
        this.my = arg0.getY();
    }
    
    public void mouseEntered(MouseEvent arg0) {
        this.mx = arg0.getX();
        this.my = arg0.getY();
    }
    
    public void mouseExited(MouseEvent arg0) {
        this.mx = arg0.getX();
        this.my = arg0.getY();
    }
    
    public void mousePressed(MouseEvent arg0) {
        this.mx = arg0.getX();
        this.my = arg0.getY();
        this.pressed = true;
        if (this.my < 10) {
            this.mode = this.mx / this.sliderWidth;
        }
        else {
            this.mode = -1;
        }
    }
    
    public void mouseReleased(MouseEvent arg0) {
        this.pressed = false;
        this.mode = -1;
    }
}
