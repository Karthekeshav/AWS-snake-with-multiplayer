import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class SnakeClient extends JPanel implements KeyListener {

    static final int W = 20;
    static final int H = 20;
    static final int CELL = 25;

    int[][] board = new int[H][W];

    int score1=0;
    int score2=0;

    boolean gameOver=false;

    PrintWriter out;

    public SnakeClient(String ip) throws Exception {

        Socket socket = new Socket(ip,5000);

        BufferedReader in =
                new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

        out = new PrintWriter(socket.getOutputStream(),true);

        new Thread(() -> {

            try{

                String line;
                int y=0;

                while((line=in.readLine())!=null){

                    if(line.equals("BOARD")){
                        y=0;
                        continue;
                    }

                    if(line.startsWith("SCORE")){
                        String[] p=line.split(" ");
                        score1=Integer.parseInt(p[1]);
                        score2=Integer.parseInt(p[2]);
                        repaint();
                        continue;
                    }

                    if(line.equals("GAMEOVER")){
                        gameOver=true;
                        repaint();
                        continue;
                    }

                    for(int x=0;x<W;x++)
                        board[y][x]=line.charAt(x)-'0';

                    y++;

                    if(y==H)
                        repaint();
                }

            }catch(Exception e){}

        }).start();

        JFrame frame = new JFrame("Multiplayer Snake");

        frame.setSize(W*CELL+16,H*CELL+60);
        frame.add(this);
        frame.addKeyListener(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void paintComponent(Graphics g){

        super.paintComponent(g);

        for(int y=0;y<H;y++){
            for(int x=0;x<W;x++){

                if(board[y][x]==1) g.setColor(Color.GREEN);
                else if(board[y][x]==2) g.setColor(Color.BLUE);
                else if(board[y][x]==3) g.setColor(Color.RED);
                else g.setColor(Color.BLACK);

                g.fillRect(x*CELL,y*CELL,CELL,CELL);
            }
        }

        g.setColor(Color.WHITE);
        g.drawString("Score P1: "+score1+"   P2: "+score2,10,H*CELL+15);

        if(gameOver){
            g.setColor(Color.RED);
            g.drawString("GAME OVER",W*CELL/2-40,H*CELL/2);
        }
    }

    public void keyPressed(KeyEvent e){

        if(e.getKeyCode()==KeyEvent.VK_UP) out.println("UP");
        if(e.getKeyCode()==KeyEvent.VK_RIGHT) out.println("RIGHT");
        if(e.getKeyCode()==KeyEvent.VK_DOWN) out.println("DOWN");
        if(e.getKeyCode()==KeyEvent.VK_LEFT) out.println("LEFT");
    }

    public void keyReleased(KeyEvent e){}
    public void keyTyped(KeyEvent e){}

    public static void main(String[] args) throws Exception{

        new SnakeClient("13.235.74.89");

    }
}