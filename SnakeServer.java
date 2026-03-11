import java.io.*;
import java.net.*;
import java.util.*;

public class SnakeServer {

    static final int W = 20;
    static final int H = 20;

    static LinkedList<int[]> snake1 = new LinkedList<>();
    static LinkedList<int[]> snake2 = new LinkedList<>();

    static int dir1 = 1;
    static int dir2 = 3;

    static int foodX;
    static int foodY;

    static int score1 = 0;
    static int score2 = 0;

    static boolean gameOver = false;

    static List<PrintWriter> clients = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        // Spawn snakes far apart
        snake1.add(new int[]{3,10});
        snake1.add(new int[]{2,10});
        snake1.add(new int[]{1,10});

        snake2.add(new int[]{16,10});
        snake2.add(new int[]{17,10});
        snake2.add(new int[]{18,10});

        spawnFood();

        ServerSocket server = new ServerSocket(5000);

        System.out.println("Waiting for players...");

        while(clients.size() < 2){

            Socket socket = server.accept();

            PrintWriter out =
                    new PrintWriter(socket.getOutputStream(), true);

            clients.add(out);

            int player = clients.size();

            System.out.println("Player "+player+" connected");

            new Thread(new ClientHandler(socket,player)).start();
        }

        System.out.println("Both players connected. Starting game.");

        gameLoop();
    }

    static void gameLoop() throws Exception {

        while(true){

            if(!gameOver){

                move(snake1,dir1);
                move(snake2,dir2);

                checkFood(snake1,1);
                checkFood(snake2,2);

                checkCollision();
            }

            broadcastBoard();

            Thread.sleep(150);
        }
    }

    static void move(LinkedList<int[]> snake,int dir){

        int[] head = snake.getFirst();

        int x=head[0];
        int y=head[1];

        if(dir==0) y--;
        if(dir==1) x++;
        if(dir==2) y++;
        if(dir==3) x--;

        snake.addFirst(new int[]{x,y});
        snake.removeLast();
    }

    static void checkFood(LinkedList<int[]> snake,int player){

        int[] head = snake.getFirst();

        if(head[0]==foodX && head[1]==foodY){

            int[] tail = snake.getLast();
            snake.addLast(new int[]{tail[0],tail[1]});

            if(player==1) score1++;
            if(player==2) score2++;

            spawnFood();
        }
    }

    static void checkCollision(){

        int[] h1 = snake1.getFirst();
        int[] h2 = snake2.getFirst();

        if(outOfBounds(h1) || outOfBounds(h2))
            gameOver=true;

        for(int i=1;i<snake1.size();i++)
            if(Arrays.equals(h1,snake1.get(i)))
                gameOver=true;

        for(int i=1;i<snake2.size();i++)
            if(Arrays.equals(h2,snake2.get(i)))
                gameOver=true;

        for(int i=1;i<snake2.size();i++)
            if(Arrays.equals(h1,snake2.get(i)))
                gameOver=true;

        for(int i=1;i<snake1.size();i++)
            if(Arrays.equals(h2,snake1.get(i)))
                gameOver=true;
    }

    static boolean outOfBounds(int[] p){
        return p[0]<0||p[0]>=W||p[1]<0||p[1]>=H;
    }

    static void spawnFood(){

        while(true){

            int x=(int)(Math.random()*W);
            int y=(int)(Math.random()*H);

            boolean onSnake=false;

            for(int[] s:snake1)
                if(s[0]==x && s[1]==y)
                    onSnake=true;

            for(int[] s:snake2)
                if(s[0]==x && s[1]==y)
                    onSnake=true;

            if(!onSnake){
                foodX=x;
                foodY=y;
                return;
            }
        }
    }

    static void broadcastBoard(){

        int[][] board = new int[H][W];

        for(int[] s:snake1)
            if(inBounds(s)) board[s[1]][s[0]] = 1;

        for(int[] s:snake2)
            if(inBounds(s)) board[s[1]][s[0]] = 2;

        board[foodY][foodX] = 3;

        for(PrintWriter out:clients){

            out.println("BOARD");

            for(int y=0;y<H;y++){

                StringBuilder line = new StringBuilder();

                for(int x=0;x<W;x++)
                    line.append(board[y][x]);

                out.println(line.toString());
            }

            out.println("SCORE "+score1+" "+score2);

            if(gameOver)
                out.println("GAMEOVER");
        }
    }

    static boolean inBounds(int[] p){
        return p[0]>=0&&p[0]<W&&p[1]>=0&&p[1]<H;
    }

    static class ClientHandler implements Runnable{

        Socket socket;
        int player;

        ClientHandler(Socket s,int p){
            socket=s;
            player=p;
        }

        public void run(){

            try{

                BufferedReader in =
                        new BufferedReader(
                                new InputStreamReader(socket.getInputStream()));

                String cmd;

                while((cmd=in.readLine())!=null){

                    int dir =
                            cmd.equals("UP")?0:
                            cmd.equals("RIGHT")?1:
                            cmd.equals("DOWN")?2:
                            3;

                    if(player==1 && !reverse(dir,dir1))
                        dir1=dir;

                    if(player==2 && !reverse(dir,dir2))
                        dir2=dir;
                }

            }catch(Exception e){}
        }

        boolean reverse(int d,int current){
            return (d==0 && current==2) ||
                   (d==2 && current==0) ||
                   (d==1 && current==3) ||
                   (d==3 && current==1);
        }
    }
}